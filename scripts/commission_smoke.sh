#!/usr/bin/env bash
# 平台抽成台账冒烟
set -u
BASE="${BASE:-http://127.0.0.1:8082/api}"
pass=0; fail=0
ok(){ echo "PASS  $1"; pass=$((pass+1)); }
bad(){ echo "FAIL  $1"; fail=$((fail+1)); }

ADMIN=$(curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' -d '{"username":"admin","password":"123456"}')
AT=$(echo "$ADMIN" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
MLOGIN=$(curl -s -X POST "$BASE/auth/merchant/login" -H 'Content-Type: application/json' -d '{"username":"merchant001","password":"123456"}')
MT=$(echo "$MLOGIN" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
MID=$(echo "$MLOGIN" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['merchantId'])")
[ -n "$AT" ] && [ -n "$MT" ] && ok "login" || bad "login"

curl -s -X PUT "$BASE/admin/commissions/merchants/$MID/rule" -H "Authorization: Bearer $AT" -H 'Content-Type: application/json' \
  -d '{"rate":0.03,"includeCash":true,"includeWechat":true,"includeAlipay":true,"enabled":true}' >/dev/null
ok "rule 3% + cash"

# 固定用已知商品/场馆（merchant001 种子数据）
PID=1
VID=1
ORDER=$(curl -s -X POST "$BASE/business/sales/orders" -H "Authorization: Bearer $MT" -H 'Content-Type: application/json' \
  -d "{\"venueId\":$VID,\"items\":[{\"productId\":$PID,\"quantity\":2}]}")
OID=$(echo "$ORDER" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['orderId'])")
AMT=$(echo "$ORDER" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['totalAmount'])")
[ -n "$OID" ] && ok "order=$OID amt=$AMT" || { bad "order=$ORDER"; exit 1; }

PAY=$(curl -s -X POST "$BASE/business/sales/orders/$OID/pay/cash" -H "Authorization: Bearer $MT" -H 'Content-Type: application/json' \
  -d "{\"receivedAmount\":$AMT}")
echo "$PAY" | python3 -c "import sys,json; d=json.load(sys.stdin); assert d.get('code')==200, d" && ok "cash paid" || bad "cash pay"

DETAIL=$(curl -s "$BASE/admin/commissions/merchants/$MID" -H "Authorization: Bearer $AT")
PENDING=$(echo "$DETAIL" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('pendingCommission') or 0)")
python3 -c "import decimal; a=decimal.Decimal('$AMT'); p=decimal.Decimal(str('$PENDING')); exp=(a*decimal.Decimal('0.03')).quantize(decimal.Decimal('0.01')); assert p==exp, (p,exp)" \
  && ok "pending=$PENDING" || bad "pending=$PENDING for amt=$AMT"

TODAY=$(date +%F)
SETTLE=$(curl -s -X POST "$BASE/admin/commissions/merchants/$MID/settle" -H "Authorization: Bearer $AT" -H 'Content-Type: application/json' \
  -d "{\"periodType\":\"CUSTOM\",\"periodStart\":\"$TODAY\",\"periodEnd\":\"$TODAY\",\"voucherNo\":\"TEST-VOUCHER\",\"remark\":\"smoke\"}")
SNO=$(echo "$SETTLE" | python3 -c "import sys,json; print((json.load(sys.stdin).get('data') or {}).get('settlementNo') or '')")
HAS_SNAP=$(echo "$SETTLE" | python3 -c "import sys,json; d=(json.load(sys.stdin).get('data') or {}); print(bool(d.get('snapshotJson')))")
[ -n "$SNO" ] && [ "$HAS_SNAP" = "True" ] && ok "settlement=$SNO snapshot=yes" || bad "settle=$SETTLE"

AFTER=$(curl -s "$BASE/admin/commissions/merchants/$MID" -H "Authorization: Bearer $AT")
PEND2=$(echo "$AFTER" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('pendingCommission') or 0)")
python3 -c "import decimal; assert decimal.Decimal(str('$PEND2'))==0" && ok "pending cleared" || bad "pending after=$PEND2"
HIST=$(echo "$AFTER" | python3 -c "import sys,json; print(len(json.load(sys.stdin).get('data',{}).get('settlements') or []))")
[ "$HIST" -ge 1 ] && ok "history=$HIST" || bad "no history"

echo "==== pass=$pass fail=$fail ===="
exit $fail
