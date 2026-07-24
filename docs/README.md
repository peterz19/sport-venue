# 本次迭代归档总览

> 归档日期：2026-07-24  
> 范围：B 端轻量收银（已实现）+ 组织经营方案（员工/片场订场/球队/双业绩，已定稿待开发）  
> 状态：方案已锁定；收银 Phase 1 已联调通过

---

## 1. 本轮交付了什么

### A. 已实现代码：轻量收银 Phase 1

| 能力 | 说明 |
|------|------|
| 商户真实 JWT 登录 | `/auth/merchant/login`，替代 mock |
| 商品录入（无库存） | 名称 + 价格，上下架 |
| 收银台 | 选品 → 确认 → 收款页 |
| 现金支付 | 默认扫码占位 + 现金一键确认 |
| 销售日报 | 按日/商品/支付方式 |
| 数据隔离 | 按 `merchant_id` |

**联调账号：** `merchant001` / `123456`  
**种子 SQL：** `sport-venue-venue/src/main/resources/db/seed_merchant_user.sql`

### B. 已定稿文档：组织经营（下一步）

| 能力 | 说明 |
|------|------|
| 员工账号 | 老板/店员两级；老板干活计入个人业绩 |
| 片场订场 | 片场 + 半小时；重叠互斥 |
| 球队 | 必绑对接员工；历史快照；变更审计 |
| 双业绩 | 订场：操作单量 vs 对接单量；收银只算操作人 |
| 赛果排名 | 双方必须是系统内球队 |

---

## 2. 文档目录

```text
docs/
├── README.md                          ← 本文件（归档入口）
├── b-end-sales/                       ← 收银模块（已实现）
│   ├── 01-产品交互方案.md
│   ├── 02-技术方案.md
│   └── 03-Phase1实现说明.md
└── b-end-org/                         ← 组织经营（已定稿，待开发）
    ├── 01-组织经营产品方案.md         ← v1.1 最终定稿
    └── 02-页面字段与分期任务.md
```

### 阅读顺序建议

1. 先看本 README 了解全貌  
2. 收银实现细节 → `b-end-sales/`  
3. 下一阶段需求 → `b-end-org/01` → `b-end-org/02`  

---

## 3. 代码改动清单（收银 Phase 1）

### 后端 `sport-venue-venue`

| 类型 | 路径 |
|------|------|
| DDL | `resources/db/V3__sales_module.sql` |
| 种子 | `resources/db/seed_merchant_user.sql` |
| Entity | `entity/Product|SalesOrder|SalesOrderItem|SalesPayment` |
| Repo/Service/Controller | `BusinessProduct*` / `BusinessSales*` / sales dto |
| 鉴权 | `SecurityConfig`、`JwtAuthenticationFilter`、`AuthController` 商户登录 |
| 工具 | `util/SecurityUtils` |

### 前端 `sport-venue-merchant`

| 类型 | 路径 |
|------|------|
| 登录/请求 | `views/login/Login.vue`、`utils/request.js` |
| API/路由/菜单 | `api/index.js`、`router/index.js`、`components/Layout.vue` |
| 新页面 | `views/cashier/*`、`views/products/*`、`views/sales/*` |
| 场馆 | `views/venue/VenueList.vue`、`VenueDetail.vue` |

---

## 4. 与现有系统的关系

```text
已有：Admin 场馆档案 + Merchant 场馆查看
本轮新增：Merchant 收银售卖（可用）
已定稿下轮：员工账号 → 片场订场 → 球队对接 → 赛果排行 →（预留 C 端）
```

组织经营分期（详见 `b-end-org/02`）：

| 阶段 | 内容 |
|------|------|
| P0 | 员工账号 + 收银按人业绩 |
| P1 | 片场 + 半小时订场互斥 |
| P2 | 球队对接人 + 订场双业绩 |
| P3 | 赛果 + 球队排行 |
| P4 | C 端个人订场（预留） |

---

## 5. 本地验证要点（收银）

```bash
# 后端（Java 21）
cd sport-venue-venue && mvn spring-boot:run -Dspring-boot.run.profiles=local

# 前端
cd sport-venue-merchant && npm run dev
# http://localhost:3001
```

流程：登录 → 商品管理 → 收银台 → 现金支付 → 销售报表。

---

## 6. 明确不在本轮代码内

- 微信/支付宝真实扫码（收银 Phase 2）  
- 员工管理 / 片场 / 球队 / 订场 / 赛果（仅文档定稿）  
- 根目录无关文件：`index.html`、`package.json`、`test.html`、`x.xml`（未纳入提交）  

---

*本归档作为本轮需求与实现的统一入口；后续迭代请更新本文件「本轮交付」与目录索引。*
