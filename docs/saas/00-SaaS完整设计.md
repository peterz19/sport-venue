# SaaS 化完整设计（总册）

> 状态：**A–F 主链路已实现**（真实微信/支付为 mock；冒烟 `scripts/saas_smoke.sh`）  
> 日期：2026-07-28  
> 产品/技术落地对照：[`../00-产品总览.md`](../00-产品总览.md) · [`../00-技术总览.md`](../00-技术总览.md) · [`../运维手册-本地部署.md`](../运维手册-本地部署.md)  
> 细节附录：[01-最小改造设计-隔离与开户.md](./01-最小改造设计-隔离与开户.md)

---

## 0. 一句话与范围

**目标形态：** 平台运营多家体育场馆商户；每家商户用同一套 B 端（收银/订场/球队）；每家商户可绑定**自己的**微信公众号 + 小程序，让顾客在该商户小程序里登录、订场、充值。

**技术底座：** 共享进程 + 共享库 + **行级 `merchant_id` 隔离**（已有雏形）。本设计补齐「安全隔离、开户运营、能力开关、微信租户路由、C 端与支付」。

### 0.1 在范围内

| 域 | 内容 |
|----|------|
| 平台 | 开户、启停、审计、商户概况、微信/支付配置 |
| 租户隔离 | API 白名单、JWT 强制商户、跨租户防探测 |
| 能力 | 功能开关与配额（收银/订场/赛果/C 端等） |
| C 端 | 小程序登录、个人订场、余额充值（与 B 端共享片场库存） |
| 支付 | 每商户微信支付配置；B 端扫码收款与 C 端支付共用密钥模型 |

### 0.2 明确不在本册（远期）

- 订阅计费/自动扣款、独立域名白标、每商户独立库/独立部署  
- 微信开放平台「第三方代开发」全链路（仅预留 `auth_type`）  
- 根目录早期愿景里的预测/社交/ML 等微服务  

---

## 1. 现状基线 vs 缺口

### 1.1 已落地（产品可用）

| 能力 | 说明 |
|------|------|
| 多商户数据行 | 商品/销售/片场/球队/订场/赛果等带 `merchant_id` |
| 商户 JWT | `/auth/merchant/login`；B 端 `/business/**` |
| Admin 开户 | `POST /merchants/onboard`（商户 + 老板） |
| B 端经营 | 收银 Phase1（现金+占位码）、员工、片场、订场、球队、赛果、双业绩、看板 |
| C 字段预留 | `bookings.source` / `customer_user_id` |

### 1.2 未实现 / 半成品（本册要设计闭环）

| # | 缺口 | 风险/影响 |
|---|------|-----------|
| G1 | Security 过宽：`/**` 对多角色开放；商户可能调 `/merchants`、跨商户 `/venues` | **不能安全接第二家** |
| G2 | 停用商户后登录未校验 `merchants.status` | 关停无效 |
| G3 | 开户无向导/无首场馆/无平台审计/无详情聚合 | 运营成本高 |
| G4 | 无功能开关与配额 | 无法按商户裁剪能力 |
| G5 | 无每商户微信公众号/小程序凭证表 | C 端无法按 AppId 路由租户 |
| G6 | 无 C 端登录/订场/充值 API | 组织 P4 未做 |
| G7 | 无真实微信支付（B 收银占位；C 充值不存在） | 线上收款不可用 |
| G8 | 订场无自动过期任务 | 可能长期占场 |
| G9 | Gateway/统一入口未强制；密钥无加密规范 | 生产运维弱 |

> **判定：** 当前是「单商户验证过的多租户雏形」，**不是**可对外售卖的 SaaS。本册按迭代把 G1–G9 补齐。

---

## 2. 目标架构

```text
┌─────────────┐  ┌──────────────┐  ┌─────────────────┐
│ Admin :3000 │  │ Merchant:3001│  │ 商户自有小程序/公众号 │
└──────┬──────┘  └──────┬───────┘  └────────┬──────────┘
       │ ADMIN JWT       │ B JWT             │ C JWT
       │                 │                   │ appId→merchant
       ▼                 ▼                   ▼
              ┌─────────────────────┐
              │  venue-service:8082 │
              │  context-path=/api  │
              └──────────┬──────────┘
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
      MySQL          Redis           微信/支付 API
   (行级租户)     (token/缓存)      (每商户凭证)
```

**租户主键：** `merchants.id`  
**B 端路由键：** JWT.`merchantId`  
**C 端路由键：** 小程序 `appId` → `merchant_wx_channels` → `merchant_id`（**禁止**信前端传的 merchantId）

---

## 3. 角色与数据范围

| 主体 | userType | 入口 | 数据范围 |
|------|----------|------|----------|
| 平台管理员 | `ADMIN`，`merchantId=null` | Admin | 全平台；禁止当店员乱调 business（见决策） |
| 商户老板 | `B_MERCHANT` | 商户端 | 本商户全部 |
| 商户店员 | `B_STAFF` | 商户端 | 本商户；业绩等按既有收窄 |
| C 端顾客 | `C_USER`（或独立 customer 表 + JWT） | 该商户小程序 | **仅本人**订场/余额；共享该商户片场库存 |

```text
平台 ADMIN
 └── Merchant
      ├── B 用户 / 场馆 / 片场 / 商品 / 球队 / 订场 / 销售…
      ├── merchant_wx_channels（小程序 + 公众号）
      ├── merchant_wx_pay（支付）
      ├── merchant_features（开关与配额）
      └── C 用户身份 user_wx_identities → 订场 source=C / 充值流水
```

---

## 4. 迭代总览（完整路线图）

| 迭代 | 主题 | 关闭缺口 | 是否写代码前置依赖 |
|------|------|----------|-------------------|
| **A** | 硬隔离 + 关停 | G1 G2 | 无；**优先** |
| **B** | 开户体验 + 审计 | G3 | A 建议先完成 |
| **C** | 功能开关 + 配额 | G4 | A |
| **D** | 每商户微信渠道 | G5 | A；表可先落 DDL |
| **E** | C 端订场 + 充值 | G6 部分 | D |
| **F** | 真实支付 + 过期任务 | G7 G8 | D（支付表）；E 可并行 B 端扫码 |
| **G** | 生产加固 | G9 | 上线前 |

**建议默认开工顺序：** A → B →（并行 C DDL + D DDL）→ D 配置 API → F 支付骨架 → E C 端 → G。

产品可对外「接第二家商户」的最低合格线：**A + B**。  
产品可对外「商户小程序订场充值」合格线：**A + D + E + F（支付）**。

---

## 5. 迭代 A：硬隔离 + 关停

### 5.1 Security 目标矩阵

| 范围 | 规则 |
|------|------|
| 匿名 | `/auth/login`、`/auth/merchant/login`、`/health/**`、swagger（生产可关） |
| 仅 ADMIN | `/merchants/**`、平台 `/venues/**`、后续 `/admin/**` |
| B 端 | `/business/**` → `ADMIN`（建议禁止）或仅 `B_MERCHANT`/`B_STAFF` |
| 默认 | **denyAll**（取消过宽 `/**`） |

### 5.2 租户上下文

- `/business/**`：`merchantId` **只来自 JWT**；请求体/Query 外来 ID **丢弃**  
- 按 ID 读详情：校验 `entity.merchantId`，失败统一 **404**  
- ADMIN：**建议禁止**调 `/business/**`，平台只用 `/merchants`、`/venues`

### 5.3 关停闭环

1. 商户登录增加：`merchants.status = ACTIVE`  
2. Admin `PUT /merchants/{id}/status` → INACTIVE/SUSPENDED  
3. 增强：`/business/**` 请求期二次校验商户状态（可缓存 1–5 分钟）  
4. 可选：`users.token_version` 踢在线 token  

### 5.4 必过用例

商户 A 调 `/merchants`→403；伪造 B 的 bookingId→404；伪造 body.merchantId 写库仍属 A；停用后不可登录。

> 展开见附录 `01` §3。

---

## 6. 迭代 B：开户体验 + 审计

### 6.1 开户向导

```text
步骤1 商户资料 → 步骤2 老板账号 → 步骤3 可选首场馆 → 完成
```

- 同事务；失败整单回滚  
- 完成页：merchantId、merchantCode、ownerUsername（密码仅当时可见）  
- **不强制**绑微信（见迭代 D）

### 6.2 商户详情

`GET /merchants/{id}/overview`：基础信息、老板、场馆/员工数、微信绑定状态、最近审计。

### 6.3 平台审计表 `platform_audit_logs`

记录开户/改资料/启停/微信绑定等；**禁止**密码与 AppSecret 明文。

---

## 7. 迭代 C：功能开关 + 配额

### 7.1 表 `merchant_features`

| 字段 | 默认 | 说明 |
|------|------|------|
| enable_cashier | 1 | 收银 |
| enable_booking | 1 | 订场 |
| enable_team_match | 1 | 球队/赛果 |
| enable_c_end | 0 | C 端小程序能力总开关 |
| enable_recharge | 0 | C 端充值 |
| max_staff / max_venues / max_courts | 如 50/20/100 | 配额 |
| max_wx_mini | 1 | 小程序绑定数 |

### 7.2 校验点

- Admin 改开关；商户端菜单按 overview 隐藏  
- API：对应 `/business/**`、`/c/**` 入口二次校验，关闭则 403 + 明确文案  
- 创建员工/场馆/片场时校验配额  

---

## 8. 迭代 D：每商户微信（公众号 + 小程序）

### 8.1 原则

- **不**在 `merchants` 宽表堆 Secret  
- AppId **全局唯一**，作为 C 端租户路由键  
- Secret/支付密钥：**加密存储**；GET 脱敏；审计不记明文  
- `access_token`：**Redis**，不进主表  

### 8.2 表

**`merchant_wx_channels`**

| 字段 | 说明 |
|------|------|
| merchant_id + channel_type | `MINI_PROGRAM` / `OFFICIAL_ACCOUNT`；UK(merchant_id, channel_type) |
| app_id | UK 全局唯一 |
| app_secret_enc | 加密 |
| oa_server_token / oa_encoding_aes_key | 公众号服务器配置 |
| bind_status | UNSET / BOUND / INVALID |
| auth_type | `SELF`（本期）/ `COMPONENT`（预留） |

**`merchant_wx_pay`**（可与 F 同落）

| 字段 | 说明 |
|------|------|
| merchant_id | UK |
| mch_id / mch_api_v3_key_enc / 证书信息 | 微信支付 |
| notify_path | 回调路径标识 |
| status | ACTIVE/INACTIVE |

**可选：** `merchants.wx_mini_bound` / `wx_oa_bound` 仅列表展示。

### 8.3 API / UI

- `GET/PUT /merchants/{id}/wx-channels`、`.../wx-pay`：**仅 ADMIN**  
- 开户后详情页配置（不阻塞开户）  
- 商户端默认**不展示** Secret  

### 8.4 租户解析

```text
小程序 code2session
  → appId 查 merchant_wx_channels
  → merchants.status=ACTIVE 且 enable_c_end
  → 签发 C JWT（merchantId + customerId）
```

---

## 9. 迭代 E：C 端订场与账户（组织 P4 产品化）

### 9.1 用户模型

**`user_wx_identities`**

| 字段 | 说明 |
|------|------|
| merchant_id, app_id, openid | UK(app_id, openid) |
| unionid | 可空 |
| user_id | 指向 C 用户 |

**C 用户表**（二选一，推荐独立以免与 B 员工混用）

- 方案 1：复用 `users` + `userType=C_USER` + `merchant_id`  
- 方案 2：新建 `customer_users`（id, merchant_id, nickname, phone, status…）  

**推荐方案 2**，B/C 账号体系分离更清晰。

### 9.2 C 端 API 前缀 `/c/**`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/c/auth/wx-login` | appId + code；permitAll |
| GET | `/c/venues`、`/c/courts` | 本商户可订资源 |
| GET | `/c/courts/{id}/slots` | 可订时段（与 B 互斥同一占用规则） |
| POST | `/c/bookings` | 个人订场；`source=C`，`customer_user_id`；**无球队/无对接业绩** |
| GET | `/c/bookings/mine` | 仅本人 |
| POST | `/c/bookings/{id}/cancel` | 仅本人且 BOOKED |
| GET | `/c/wallet` | 余额 |
| POST | `/c/wallet/recharge` | 创建充值单 → 拉起支付 |
| GET | `/c/wallet/ledger` | 流水 |

### 9.3 与 B 端规则对齐

| 规则 | C 端 |
|------|------|
| 片场半小时互斥 | **同一套**占用；B/C 抢同一库存 |
| 取消 | 仅 BOOKED；COMPLETED/有赛果不可取消 |
| 业绩 | C 单不计入对接业绩；操作人可记「系统/无人」或商户配置的默认前台（产品可后定，默认不记员工业绩） |
| 可见性 | C 仅见个人订场；不见全店/球队经营数据 |
| 赛果 | C 订场默认不录赛果（或仅 B 代录） |

### 9.4 余额模型（充值）

**`customer_wallets`**：merchant_id + customer_id，balance  
**`wallet_ledgers`**：充值/订场扣款/退款；关联支付单或 bookingId  

订场扣款策略（选定一种写死产品）：

1. **先充值再订场**（余额支付）— 推荐首版  
2. 订场时直接微信下单 — 可作二期  

首版推荐：**充值入账 → 订场扣余额**；退订规则：BOOKED 取消退回余额（与支付渠道退款解耦）。

---

## 10. 迭代 F：真实支付 + 订场过期

### 10.1 支付网关（按商户凭证）

```text
PaymentGateway
  ├── createPrepay(merchantId, order)  // 读 merchant_wx_pay
  ├── handleNotify(channel, payload)   // 按 mchId/attach.merchantId 反查
  └── queryStatus(...)
```

| 场景 | 订单类型 | 回调后 |
|------|----------|--------|
| B 端收银扫码 | sales_payments | 订单 PAID（替换占位码） |
| C 端充值 | wallet_recharge_orders | 余额入账 |
| （可选）C 直接付订场 | booking_payments | 订场确认 |

**禁止**全局一份微信商户号 yml 服务所有租户；平台级配置只放加密主密钥、第三方 component（若有）。

### 10.2 订场自动过期

- 定时任务（如每 5 分钟）：`BOOKED` 且 `end_time < now` → `EXPIRED`（或 `COMPLETED` 策略需产品定；建议 **EXPIRED 释放占用**）  
- 与取消一样释放互斥区间  
- 有赛果的单不自动改状态  

---

## 11. 迭代 G：生产加固

| 项 | 说明 |
|----|------|
| Swagger / `/auth/dev/**` | 生产关闭 |
| Secret 主密钥 | 环境变量 / KMS；库内仅密文 |
| HTTPS + 回调验签 | 支付/公众号消息 |
| Gateway | 统一入口与限流；或暂直连但文档化 |
| 监控 | 登录失败、跨租户 404 率、支付回调失败 |
| 备份与迁移 | Flyway/Liquibase 规范迁移脚本（V5+） |

---

## 12. 数据模型总览（相对现状增量）

```text
已有：merchants, users, venues, products, sales_*, courts, teams,
      bookings(source, customer_user_id), match_results, …

新增：
  platform_audit_logs          // B
  merchant_features            // C
  merchant_wx_channels         // D
  merchant_wx_pay              // D/F
  user_wx_identities           // E
  customer_users               // E（推荐）
  customer_wallets             // E
  wallet_ledgers               // E
  wallet_recharge_orders       // E/F

可选：
  merchants.disabled_at, wx_*_bound
  users.token_version
```

DDL 建议文件：`V5__saas_isolation_ops.sql`（A 可选列 + B 审计）→ `V6__merchant_features.sql` → `V7__merchant_wx.sql` → `V8__c_end_wallet.sql`。

---

## 13. API 分区总览

| 前缀 | 角色 | 租户来源 |
|------|------|----------|
| `/auth/login` | 匿名→ADMIN | 无 |
| `/auth/merchant/login` | 匿名→B | 用户行 merchantId + 商户 ACTIVE |
| `/merchants/**` | ADMIN | 路径/查询指定商户 |
| `/venues/**` | ADMIN | 可按 merchantId 筛选 |
| `/business/**` | B | **仅 JWT** |
| `/c/auth/**` | 匿名/C | appId |
| `/c/**` | C | **仅 JWT** |
| `/business/sales/payments/notify/**`、`/c/pay/notify/**` | 匿名+验签 | payload 反查商户 |

---

## 14. 前端改造要点

### Admin（:3000）

| 页 | 内容 |
|----|------|
| 登录 | 仅 ADMIN |
| 商户列表 | 状态、微信绑定标记、功能开关摘要 |
| 开户向导 | 商户→老板→可选首场馆 |
| 商户详情 | overview、启停、审计、**微信渠道/支付配置**、功能开关 |
| 场馆 | 按商户筛选 |

### 商户端（:3001）

| 项 | 内容 |
|----|------|
| 隔离 | 不传可篡改 merchantId |
| 菜单 | 读 features 隐藏关闭模块 |
| 停用文案 | code=40301 |
| 微信密钥 | 默认无配置页 |

### C 端小程序（每商户独立或模板发布）

| 项 | 内容 |
|----|------|
| 登录 | wx.login → `/c/auth/wx-login`（带 appId） |
| 首页 | 本馆场馆/片场 |
| 订场 | 选时段 → 余额支付 |
| 我的 | 订场列表、充值、流水 |

---

## 15. 错误码（统一）

| code | 含义 |
|------|------|
| 401 | 未登录 / token 无效 |
| 403 | 角色不符 |
| 40301 | 商户已停用 |
| 40302 | 账号已停用 |
| 40303 | 功能未开通（features） |
| 40304 | 超过配额 |
| 404 | 资源不存在或不属于本租户 |
| 409 | 订场时段冲突 |
| 40201 | 余额不足 |

---

## 16. 安全与合规要点

1. 跨租户统一 404，降低探测  
2. Secret 加密 + 不回显 + 审计脱敏  
3. C/B 权限分离；C 不可访问 `/business/**`  
4. 支付回调验签；幂等入账  
5. 停用商户：登录拒绝 + business/c 请求拒绝  
6. openid 按 appId 隔离，勿全局唯一 openid 当主键  

---

## 17. 验收清单（按合格线）

### L1 — 可安全接第二家（A+B）

- [ ] A/B 数据互不可见；伪造 ID 失败  
- [ ] 商户无法调平台开户/列表  
- [ ] 停用后不可登录  
- [ ] 开户出老板账号；可选首场馆；有审计无密码  

### L2 — 可配置 C 通道（+C+D）

- [ ] 开关可关收银/订场/C 端  
- [ ] Admin 绑定小程序/公众号 AppId；Secret 不回显  
- [ ] 同 AppId 不可绑两商户  

### L3 — C 端可用（+E+F）

- [ ] 小程序登录落到正确商户  
- [ ] C/B 订场互斥同一片场  
- [ ] 充值入账、订场扣余额、取消退余额（若采用余额模型）  
- [ ] 支付回调验签且幂等  
- [ ] 过期任务释放占用  

### L4 — 生产（+G）

- [ ] 生产关闭调试入口；密钥不进仓库；监控与备份就绪  

---

## 18. 关键决策（已定稿建议）

| 议题 | 决策 |
|------|------|
| 多租户模式 | 共享库 + `merchant_id` 行级 |
| ADMIN 与 business | **禁止** ADMIN 调 `/business/**` |
| 微信字段位置 | 独立 `merchant_wx_channels`，不进 merchants 宽表 |
| C 用户表 | **独立 `customer_users`** |
| C 订场支付首版 | **余额：先充值再订场** |
| C 订场业绩 | 默认不计入员工业绩 |
| 开户是否绑微信 | **否**，详情后置配置 |
| 第三方代开发 | 仅 `auth_type` 预留 |
| 计费套餐 | 本册不做 |

---

## 19. 实施顺序（工程拆分）

```text
1. Security 收紧 + 越权用例（A）
2. 登录校验 merchants.status + 可选请求期校验（A）
3. TenantGuard / 详情从属校验（A）
4. onboard 向导字段 + 首场馆 + audit 表（B）
5. Admin overview + 启停文案（B）
6. merchant_features + 菜单/API 校验（C）
7. merchant_wx_channels/pay DDL + Admin 配置 API（D）
8. PaymentGateway 按商户 + B 收银真扫码（F 部分）
9. customer_* + /c/auth + 订场/钱包（E）
10. 充值支付回调 + 订场过期任务（F）
11. 生产开关与密钥（G）
```

人日粗估（供排期，非承诺）：A 3–5 · B 3–4 · C 2–3 · D 3–4 · E 8–12 · F 5–8 · G 2–3。

---

## 20. 文档关系

| 文档 | 角色 |
|------|------|
| **本文 `00-SaaS完整设计`** | SaaS 总册：缺口、路线图、C/支付/开关 |
| [`01-最小改造…`](./01-最小改造设计-隔离与开户.md) | A/B 细规 + §5.4 微信字段原稿 |
| `docs/00-产品总览.md` | 当前**已落地** B 端产品边界 |
| `b-end-org/01` §8 | C 端产品边界（与本文 E 对齐） |
| `b-end-sales/02` Phase2 | B 端扫码；密钥模型改为**每商户**（本文 F） |
| 待写 `02-功能开关与配额.md` | C 迭代展开（可选，本文 §7 已够开工） |
| 待写 `03-商户微信与C端租户路由.md` | D/E 联调手册（可选） |

---

*确认本总册后，工程默认从迭代 A 第 1 步开始；L1 完成后再排 D/E/F。*
