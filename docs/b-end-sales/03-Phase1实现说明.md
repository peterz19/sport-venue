# Phase 1 实现说明

已落地内容见：

- 产品方案：`01-产品交互方案.md`
- 技术方案：`02-技术方案.md`

## 如何验证

1. 执行 DDL（或依赖 JPA `ddl-auto: update` 自动建表）  
   `sport-venue-venue/src/main/resources/db/V3__sales_module.sql`

2. 确保存在 **B_MERCHANT / B_STAFF** 用户，且 `users.merchant_id` 已绑定商户。

3. 启动 venue-service（建议 `local` profile，context-path=`/api`）

4. 启动商户端：
   ```bash
   cd sport-venue-merchant && npm install && npm run dev
   ```
   访问 http://localhost:3001

5. 流程：登录 → 商品管理录入 → 收银台选品确认 → 收款页点「现金支付」→ 销售报表查看

## 已实现 API 前缀

`/business/products/**`  
`/business/sales/**`  
`/business/venues/mine`  
`/auth/merchant/login`
