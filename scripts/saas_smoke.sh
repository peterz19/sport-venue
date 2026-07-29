#!/usr/bin/env bash
# SaaS 冒烟：隔离 / 开户 / 微信 / C端 / 关停
set -u
BASE="${BASE:-http://127.0.0.1:8082/api}"
pass=0
fail=0

ok() { echo "PASS  $1"; pass=$((pass+1)); }
bad() { echo "FAIL  $1"; fail=$((fail+1)); }

echo "== health =="
code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/health" || echo 000)
[ "$code" = "200" ] && ok "health 200" || bad "health 200 ($code)"

echo "== admin login =="
ADMIN=$(curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' -d '{"username":"admin","password":"123456"}' || true)
AT=$(echo "$ADMIN" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('token',''))" 2>/dev/null || true)
[ -n "$AT" ] && ok "admin token" || bad "admin token"

echo "== merchant login =="
MLOGIN=$(curl -s -X POST "$BASE/auth/merchant/login" -H 'Content-Type: application/json' -d '{"username":"merchant001","password":"123456"}' || true)
MT=$(echo "$MLOGIN" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('token',''))" 2>/dev/null || true)
[ -n "$MT" ] && ok "merchant token" || bad "merchant token"

echo "== A: merchant cannot list merchants =="
code=$(curl -s -o /tmp/saas_r.json -w "%{http_code}" "$BASE/merchants" -H "Authorization: Bearer $MT" || echo 000)
[ "$code" = "403" ] && ok "merchant GET /merchants 403" || bad "merchant GET /merchants ($code)"

echo "== A: merchant cannot call platform venues =="
code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/venues" -H "Authorization: Bearer $MT" || echo 000)
[ "$code" = "403" ] && ok "merchant GET /venues 403" || bad "merchant GET /venues ($code)"

echo "== B: onboard with first venue =="
SUF=$(date +%s)
ONB=$(curl -s -X POST "$BASE/merchants/onboard" -H "Authorization: Bearer $AT" -H 'Content-Type: application/json' \
  -d "{\"name\":\"Smoke$SUF\",\"ownerUsername\":\"owner$SUF\",\"ownerPassword\":\"123456\",\"ownerRealName\":\"Boss$SUF\",\"firstVenue\":{\"name\":\"V$SUF\",\"address\":\"addr1\",\"type\":\"GYM\",\"spaceType\":\"INDOOR\",\"chargeType\":\"PAID\"}}" || true)
echo "$ONB" | head -c 400; echo
NID=$(echo "$ONB" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('merchantId') or '')" 2>/dev/null || true)
VID=$(echo "$ONB" | python3 -c "import sys,json; v=json.load(sys.stdin).get('data',{}).get('firstVenueId'); print(v if v is not None else '')" 2>/dev/null || true)
[ -n "$NID" ] && ok "onboard merchantId=$NID" || bad "onboard merchantId"
[ -n "$VID" ] && ok "onboard firstVenueId=$VID" || bad "onboard firstVenueId"

echo "== B: overview =="
OV=$(curl -s "$BASE/merchants/$NID/overview" -H "Authorization: Bearer $AT" || true)
vcount=$(echo "$OV" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('venueCount',0))" 2>/dev/null || echo 0)
[ "$vcount" -ge 1 ] && ok "overview venueCount=$vcount" || bad "overview venueCount=$vcount"

echo "== D: bind mini program =="
APPID="wx_smoke_$SUF"
WX=$(curl -s -X PUT "$BASE/merchants/$NID/wx-channels" -H "Authorization: Bearer $AT" -H 'Content-Type: application/json' \
  -d "{\"channelType\":\"MINI_PROGRAM\",\"appId\":\"$APPID\",\"appSecret\":\"mock-secret\"}" || true)
bind=$(echo "$WX" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('bindStatus',''))" 2>/dev/null || true)
[ "$bind" = "BOUND" ] && ok "wx BOUND" || bad "wx bind ($bind) body=$WX"
has=$(echo "$WX" | python3 -c "import sys,json; print('appSecret' in (json.load(sys.stdin).get('data') or {}))" 2>/dev/null || echo True)
[ "$has" = "False" ] && ok "wx no secret echo" || bad "wx secret echoed"

echo "== C: enable c-end + recharge =="
curl -s -X PUT "$BASE/merchants/$NID/features" -H "Authorization: Bearer $AT" -H 'Content-Type: application/json' \
  -d '{"enableCEnd":true,"enableRecharge":true,"enableBooking":true}' >/dev/null || true

echo "== E: c login =="
CL=$(curl -s -X POST "$BASE/c/auth/wx-login" -H 'Content-Type: application/json' \
  -d "{\"appId\":\"$APPID\",\"code\":\"user$SUF\"}" || true)
CT=$(echo "$CL" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('token',''))" 2>/dev/null || true)
CMID=$(echo "$CL" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('merchantId',''))" 2>/dev/null || true)
[ -n "$CT" ] && ok "c token" || bad "c token body=$CL"
[ "$CMID" = "$NID" ] && ok "c merchant matches" || bad "c merchant $CMID vs $NID"

echo "== E: recharge =="
curl -s -X POST "$BASE/c/wallet/recharge" -H "Authorization: Bearer $CT" -H 'Content-Type: application/json' -d '{"amount":100}' >/dev/null || true
bal=$(curl -s "$BASE/c/wallet" -H "Authorization: Bearer $CT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('balance',0))" 2>/dev/null || echo 0)
python3 -c "import decimal; assert decimal.Decimal(str('$bal'))==decimal.Decimal('100')" 2>/dev/null && ok "balance 100" || bad "balance=$bal"

echo "== A: disable merchant blocks login =="
curl -s -X PUT "$BASE/merchants/$NID/status" -H "Authorization: Bearer $AT" -H 'Content-Type: application/json' -d '{"status":"INACTIVE"}' >/dev/null || true
OWN=$(curl -s -X POST "$BASE/auth/merchant/login" -H 'Content-Type: application/json' \
  -d "{\"username\":\"owner$SUF\",\"password\":\"123456\"}" || true)
ocode=$(echo "$OWN" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code'))" 2>/dev/null || true)
[ "$ocode" = "40301" ] && ok "disabled login 40301" || bad "disabled login code=$ocode"

curl -s -X PUT "$BASE/merchants/$NID/status" -H "Authorization: Bearer $AT" -H 'Content-Type: application/json' -d '{"status":"ACTIVE"}' >/dev/null || true

echo
echo "==== RESULT pass=$pass fail=$fail ===="
exit $fail
