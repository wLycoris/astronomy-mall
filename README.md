# 🌌 天文器材商城系统 - 项目主控文档 v8.5 (完整版)

> **重要提示:** 这是整个项目的核心文档,记录所有关键信息和开发进度
> **创建日期:** 2025-01-XX
> **项目周期:** 16周(4个月)
> **最后更新:** 2026-04-16
> **文档版本:** v8.60 完整版 ✨
> **项目状态:** 商城100%(2.1商品浏览✅+2.2支付✅+2.3后台8子模块✅+2.4个人空间4子模块✅+2.5售后3子模块✅+2.6商品收藏✅+2.7NASA集成3子模块✅) | 后台100% | 通知100%(全部完成) | AI识别100%(4.1-4.5全部完成) | 课程模块5.1✅5.2✅5.3✅5.4✅5.5✅5.6✅ | NASA API集成✅(2.7.1+2.7.2+2.7.3全部完成) | 地理位置模块6.0✅6.1✅6.2✅6.3✅6.4✅6.5✅(全部完成) | 论坛社区模块7.1✅7.2✅7.3✅7.4✅7.5✅7.6✅7.7✅7.8✅(基础建设+帖子发布+列表详情+评论系统+点赞收藏+关注系统+用户主页+搜索功能+后台管理+小红书web风格+通知集成+热度计算+跨模块联动) | 推荐系统8.0✅8.1✅8.2✅8.3✅8.4✅(全部完成,含 v8.60 标签匹配+热门兜底 bugfix) | 整体规划100% | **🎓 毕设核心功能全部开发完成 ✅**

---

## 📑 文档组织说明

### ✅ 重要理解

**代码组织:**
```
notification模块代码位置:
com.astronomy.mall.module.notification/
├── entity/          (3个实体类) ← Notification.java 新增 deleted 字段 ✅
├── mapper/          (3个Mapper) ← NotificationMapper 新增 insertBatch/selectAnnouncementByRelatedId ✅
├── service/         (NotificationService + 实现) ← 新增 getAnnouncementDetail ✅
├── helper/          (NotificationHelper ← 业务模块调用这个)
├── controller/      (8个API接口) ← 新增 GET /notification/announcement/{id} ✅
├── dto/             (4个DTO)
├── vo/              (3个VO)
├── enums/           (2个枚举)
└── config/          (AsyncConfig)

admin模块公告管理:
com.astronomy.mall.module.admin/
├── controller/AdminAnnouncementController.java ✅ 🆕
├── controller/AdminNotificationController.java ✅ 🆕
├── controller/AdminNotificationTemplateController.java ✅ 🆕
├── service/AdminAnnouncementService.java ✅ 🆕
├── service/AdminNotificationService.java ✅ 🆕
├── service/AdminNotificationTemplateService.java ✅ 🆕
├── service/impl/AdminAnnouncementServiceImpl.java ✅ 🆕
├── service/impl/AdminNotificationServiceImpl.java ✅ 🆕
├── service/impl/AdminNotificationTemplateServiceImpl.java ✅ 🆕
├── mapper/AdminAnnouncementMapper.java ✅ 🆕
├── mapper/AdminAnnouncementMapper.xml ✅ 🆕
├── mapper/AdminNotificationMapper.java ✅ 🆕
├── mapper/AdminNotificationMapper.xml ✅ 🆕
├── dto/AnnouncementCreateDTO.java ✅ 🆕
├── dto/AnnouncementQueryDTO.java ✅ 🆕 (含priority字段)
├── dto/NotificationRecordQueryDTO.java ✅ 🆕
├── dto/TemplateUpdateDTO.java ✅ 🆕
├── dto/TemplateStatusDTO.java ✅ 🆕
├── vo/AnnouncementVO.java ✅ 🆕
├── vo/NotificationRecordVO.java ✅ 🆕
├── vo/NotificationStatsVO.java ✅ 🆕
└── vo/NotificationTemplateVO.java ✅ 🆕

所有通知相关代码都在这个模块下，不分散！
```

**开发计划分布:**
```
✅ 通知核心框架 → 已完成 80% (见第3模块)
⬜ 商城通知集成 → 第8周 Day 1-6 (见第2模块)
✅ 商品收藏+通知 → 第8周 Day 2-4 (见第2模块) 🆕
⬜ 退款审核+通知 → 第8周 Day 5-6 (见第2模块)
⬜ 后台消息管理 → 第8周 Day 7-10 (见第3模块)
⬜ 论坛/课程通知 → 第13-15周 (见对应模块)
```

**⚠️ 当前状态说明:**
```
✅ 已完成的通知功能 (核心框架 80%):
- [x] 消息通知发送 (支持单个/批量) ✅
- [x] 未读数量统计 (总数+按模块统计) ✅
- [x] 标记已读 (单个/批量/全部) ✅
- [x] 消息删除 ✅
- [x] 消息跳转 (点击跳转到相关页面) ✅
- [x] 通知模板系统 ✅
- [x] 前端通知铃铛组件 ✅

✅ 已完成的通知集成 (第8周重点 ⭐⭐⭐⭐⭐):
- [x] 订单支付成功通知 (PaymentServiceImpl) ✅
- [x] 订单发货通知 (AdminOrderServiceImpl) ✅
- [x] 订单派送通知 (AdminOrderServiceImpl) ✅
- [x] 订单完成通知 (OrderServiceImpl) ✅
- [x] 订单取消通知 (OrderServiceImpl + AdminOrderServiceImpl) ✅
- [x] 商品收藏功能开发 + 上架/降价通知 ✅ 2026-03-12完成
- [x] 退款审核功能开发 + 审核通知(通过/拒绝/到账) ✅ 2026-02-25完成

✅ 已完成的后台消息管理 (第8周重点 ⭐⭐⭐⭐):
- [x] 系统公告管理 (创建/发送/列表/删除，5个接口) ✅ 2026-03-16完成
- [x] 通知记录管理 (查看/筛选/统计/批量删除，3个接口) ✅ 2026-03-16完成
- [x] 通知模板管理 (列表/编辑/启用禁用/恢复默认，5个接口) ✅ 2026-03-16完成
```

### 🎯 核心原则

**代码在notification模块，调用在业务模块**

例如：
- `NotificationHelper.sendOrderPaidNotification()` 代码在 `notification.helper` 包下
- `PaymentServiceImpl` 注入并调用这个方法
- 这样保持代码统一管理，业务模块只负责调用

---


---

---

## 📊 项目基本信息

### 项目名称
基于Spring Boot的天文器材商城系统

### 项目定位
为天文爱好者提供集**器材选购、AI星图识别、课程学习、地理位置推荐、社区交流**于一体的智能化服务平台

### 核心创新点
- 🌠 AI星图识别(Astrometry.net API)
- 🧠 基于内容相似度的混合推荐算法（标签Jaccard相似度 + 特征向量余弦相似度，契合论文主题）
- 🌍 地理位置智能联动(高德地图API)
- 📚 NASA多API对接(APOD首页每日图片 + 课程自动同步 + 火星车图库)
- 💳 完整支付流程(支付宝/微信/余额) ✅
- 🔄 "学习→识别→购买→分享"生态闭环
- 📝 完整的商品调整日志系统 ✅ 🆕

---

## 💻 开发环境版本 (已确认)

### 核心环境
```
JDK版本:           JDK 1.8 (Java 8)
Node.js版本:       18.17.0 (LTS)
Python版本:        3.9.13
MySQL版本:         8.0.33
Git版本:           2.42.0 或更高
Maven版本:         3.8.8 或 3.9.4
```

### 开发工具
```
后端IDE:           IntelliJ IDEA 2023.2
  - Lombok插件:    已安装并启用 ✅
  - MyBatisX插件:  已安装 ✅
前端IDE:           Visual Studio Code 1.82+
  - Volar插件:     已安装 ✅
  - ESLint插件:    已安装 ✅
数据库工具:        Navicat Premium 16.2+ 或 DBeaver
API测试:           Postman 10.17+ 或 Apifox 2.3+
```

---

## 🛠️ 技术栈清单

### 后端技术
```
核心框架:         Spring Boot 2.7.14
持久层:           MyBatis-Plus 3.5.3.1
数据库:           MySQL 8.0.33
数据库连接池:     Druid 1.2.18
权限认证:         JWT 0.9.1 (有效期7天)
API文档:          Knife4j 3.0.3
工具类库:         Hutool 5.8.18
参数校验:         Hibernate Validator
Lombok:           1.18.30 ✅
FastJSON:         1.2.83
AOP切面:          Spring AOP (商品日志+通知日志) ✅
Excel处理:        Apache POI 5.2.3 ✅
异步任务:         Spring @Async (通知发送) ✅ 🆕
缓存:             Redis 7.x (推荐系统浏览去重 SETNX 30min TTL，8.0已启用) ✅ 🆕
```

### 前端技术
```
核心框架:     Vue 3.3.4
UI组件库:     Element Plus 2.3.12
状态管理:     Pinia 2.1.6
路由管理:     Vue Router 4.2.4
HTTP请求:     Axios 1.5.0
构建工具:     Vite 4.4.9
Cookie管理:   js-cookie 3.0.5
富文本编辑器: Tinymce 6.7.0
地图组件:     高德地图 JS API 2.0
图表组件:     ECharts 5.4.3
```

---

## 🚨 重要避坑指南

### ⚠️ 类型转换陷阱 (MySQL + MyBatis)

**正确写法:**
```java
// ✅ 方式1: BigDecimal 转 Double (推荐)
Object avgRatingObj = stats.get("avgRating");
Double avgRating = avgRatingObj == null ? 0.0 : ((BigDecimal) avgRatingObj).doubleValue();

// ✅ 方式2: 使用工具类
Double avgRating = Convert.toDouble(avgRatingObj, 0.0);
```

### ⚠️ 订单状态定义

```java
订单状态值:
0 - 待支付  ✅ 可以取消/支付
1 - 待发货  ✅ 可以申请退款 📌数据库已支持物流字段
2 - 待收货  ✅ 可以确认收货/申请退款
3 - 已完成  ✅ 可以评价/申请退款
4 - 已取消  ❌ 终态
```

### ⚠️ 支付状态定义

```java
支付状态值:
0 - 待支付  ✅ 可以支付
1 - 支付成功 ✅ 可以退款
2 - 支付失败 ❌ 终态
3 - 已退款  ❌ 终态
```

### ⚠️ 退款状态定义

```java
退款状态值:
0 - 待审核  ✅ 可以取消
1 - 审核通过 ✅ 等待退款
2 - 审核拒绝 ❌ 终态
3 - 退款成功 ❌ 终态
4 - 退款失败 ❌ 终态
```

---

## 📁 系统功能模块(8个)

### ✅ 1. 用户管理模块
**开发时间:** 第1-2周  
**状态:** ✅ 已完成

**功能清单:**
- [x] 用户注册(邮箱/手机号)
- [x] 用户登录(JWT认证)
- [x] 个人信息管理
- [x] 兴趣标签设置
- [x] 角色权限管理
- [x] 登录日志记录

**数据库表:**
- `tb_user` (用户表) ✅
- `tb_login_log` (登录日志表) ✅

---

### ✅ 2. 天文器材商城模块
**开发时间:** 第3-8周  
**状态:** ✅ **全部完成 100%** — 2.1 商品浏览✅ + 2.2 支付系统(含 2.4.4 钱包+v8.58 bug修复)✅ + 2.3 后台管理 8 子模块✅ + 2.4 个人空间 4 子模块✅ + 2.5 售后服务 3 子模块(安装/保养/回收)✅ + 2.6 商品收藏✅ + 2.7 NASA 集成 3 子模块✅

**✅ 已完成 - 商城通知集成:**

**🔔 订单通知(5种):**
- [x] 订单支付成功通知 (PaymentServiceImpl) ✅
- [x] 订单发货通知 (AdminOrderServiceImpl) ✅
- [x] 订单派送通知 (AdminOrderServiceImpl) ✅
- [x] 订单完成通知 (OrderServiceImpl) ✅
- [x] 订单取消通知 (OrderServiceImpl + AdminOrderServiceImpl) ✅

**🔔 商品通知(2种):**
- [x] 商品上架通知 (AdminProductServiceImpl.updateStatus()) ✅ 2026-03-12完成
- [x] 商品降价通知 (PriceDropScheduler 定时任务，每天凌晨2点) ✅ 2026-03-12完成

**🔔 退款通知(3种):**
- [x] 退款审核通过通知 ✅ 2026-02-25完成
- [x] 退款审核拒绝通知 ✅ 2026-02-25完成
- [x] 退款到账通知 ✅ 2026-02-25完成

**开发时机:** 第8周 Day 1-6  
**集成方式:** 在对应Service方法中调用NotificationHelper  
**详见:** 第8周开发计划

---

#### 2.1 商品浏览与购物 (已完成 ✅)
- [x] 商品分类展示 (一级+二级)
- [x] 商品搜索(关键词/价格/品牌)
- [x] 商品详情页
- [x] 商品评价展示
- [x] 购物车管理 (增删改查)
- [x] 订单创建流程
- [x] 订单列表查询
- [x] 订单详情查看
- [x] 订单状态流转
- [x] 商品评价发布
- [x] 评价统计分析

**数据库表:**
- `tb_category` (分类表) ✅
- `tb_product` (商品表) ✅
- `tb_review` (评价表) ✅
- `tb_review_like` (评价点赞表) ✅
- `tb_cart` (购物车表) ✅
- `tb_order` (订单表) ✅ **已扩展物流字段** 🆕
- `tb_order_item` (订单详情表) ✅

#### 2.2 支付系统 (已完成 ✅)
**开发时间:** 第7周  
**完成日期:** 2025-12-11  

**已完成功能:**
- [x] 创建支付订单
- [x] 支付方式选择(支付宝/微信/余额)
- [x] 模拟支付流程
- [x] 支付状态查询
- [x] 支付倒计时(15分钟)
- [x] 支付成功页面
- [x] 申请退款
- [x] 退款原因选择
- [x] 退款金额校验
- [x] 退款记录查询
- [x] 取消退款申请

**数据库表:**
- `tb_payment` (支付记录表) ✅
- `tb_refund` (退款记录表) ✅ **已包含审核字段** 🆕

#### 2.3 后台管理系统 (已完成 ✅)
**开发时间:** 第8周 (5-7天)  
**优先级:** 最高 ⭐⭐⭐⭐⭐

**方案说明:**
- 后台功能由**管理员账号**承担 (tb_user.role=1)
- 管理员拥有完整的后台管理权限
- 实现前后端分离的管理后台

---

##### 2.3.1 商品管理 (已完成 ✅)
**优先级:** 最高 ⭐⭐⭐⭐⭐  
**当前进度:** 100% 🎉

**已完成功能:**
- [x] 商品列表查询 (分页/搜索/筛选) ✅
- [x] 商品新增 ✅
- [x] 商品编辑 ✅
- [x] 商品上下架 ✅
- [x] 批量上下架 ✅
- [x] 库存管理 ✅
- [x] 库存调整日志 ✅
- [x] **商品调整日志** ✅ (2026-01-28完成)
- [x] 库存预警 ✅
- [x] 商品删除(物理删除) ✅
- [x] **商品批量导入** ✅ 🆕 (2026-01-28完成)
- [x] **商品批量导出** ✅ 🆕 (2026-01-28完成)
- [x] **下载导入模板** ✅ 🆕 (2026-01-28完成)

**接口设计:**
```
GET    /api/admin/product/list          - 商品列表(分页) ✅
POST   /api/admin/product/add           - 新增商品 ✅
PUT    /api/admin/product/update/:id    - 编辑商品 ✅
POST   /api/admin/product/status        - 上下架商品 ✅
PUT    /api/admin/product/stock/:id     - 调整库存 ✅
DELETE /api/admin/product/delete/:id    - 删除商品 ✅
GET    /api/admin/product/stock-warning - 库存预警列表 ✅
GET    /api/admin/stock-log/list        - 库存日志列表 ✅
GET    /api/admin/product-log/list      - 商品日志列表 ✅
GET    /api/admin/product-log/product/:id - 商品日志详情 ✅
POST   /api/admin/product/import        - 批量导入商品 ✅ 🆕
GET    /api/admin/product/export        - 批量导出商品 ✅ 🆕
GET    /api/admin/product/download-template - 下载导入模板 ✅ 🆕
```

**🆕 商品调整日志功能 (2026-01-28完成):**

**功能特性:**
- ✅ AOP切面自动记录商品变更
- ✅ 字段级别的变更追踪
- ✅ 完整的审计信息(操作人/IP/时间)
- ✅ 支持新增/修改/上下架/删除操作
- ✅ JSON格式存储变更详情

**技术实现:**
- 使用 `@ProductLog` 注解标记需要记录的方法
- `ProductLogAspect` AOP切面自动拦截并记录
- 对比操作前后的商品对象,提取变更字段
- 异步保存,不影响业务性能

**日志示例:**
```json
{
  "id": 1,
  "productId": 10,
  "productName": "天文望远镜",
  "operationType": "修改商品",
  "changeFields": [
    {
      "field": "price",
      "fieldName": "价格",
      "oldValue": "1999.00",
      "newValue": "2999.00"
    },
    {
      "field": "stock",
      "fieldName": "库存",
      "oldValue": "100",
      "newValue": "50"
    }
  ],
  "operatorId": 1,
  "operatorName": "admin",
  "ipAddress": "192.168.1.100",
  "createTime": "2026-01-28 15:30:00"
}
```

**数据库表:**
- `tb_product_log` (商品调整日志表) ✅

**已完成文件:**
```
├── entity
│   └── ProductLog.java ✅
├── mapper
│   └── ProductLogMapper.java ✅
├── service
│   ├── ProductLogService.java ✅
│   └── impl
│       └── ProductLogServiceImpl.java ✅
├── controller
│   └── ProductLogController.java ✅
├── dto
│   └── ProductLogQueryDTO.java ✅
├── vo
│   └── ProductLogVO.java ✅
├── common/annotation
│   └── ProductLog.java ✅
└── config
    └── ProductLogAspect.java ✅
```

---

**🆕 商品批量导入/导出功能 (2026-01-28完成):**

**功能特性:**
- ✅ 支持Excel批量导入商品(.xlsx/.xls)
- ✅ 文件大小限制10MB,单次最多1000条
- ✅ 完整的数据校验(商品名称/分类/价格/库存等)
- ✅ 逐行导入,失败不影响其他行
- ✅ 详细的错误信息提示和统计
- ✅ 支持按条件筛选导出(名称/状态/品牌)
- ✅ 导出完整的商品信息(23个字段)
- ✅ 提供标准的Excel导入模板下载
- ✅ 自动格式化时间和关联查询分类名称

**技术实现:**
- 使用 Hutool ExcelUtil 处理Excel读写
- 支持多字段映射(productName/categoryId/price等)
- 完整的异常处理和事务管理
- 前端使用 el-upload 组件实现文件上传
- 导出使用 Blob 实现浏览器自动下载

**导入模板格式:**
| 商品名称 | 分类ID | 副标题 | 品牌 | 价格 | 原价 | 库存 | 主图URL | 是否推荐 | 是否热卖 | 是否新品 |
|---------|--------|--------|------|------|------|------|---------|---------|---------|---------|
| 天文望远镜 | 1 | 入门级 | 星特朗 | 1999.00 | 2999.00 | 100 | http://... | 1 | 0 | 1 |

**数据校验规则:**
- 商品名称: 必填,不超过200字符
- 分类ID: 必填,必须存在于系统中
- 价格: 必填,大于0,不超过99999999.99
- 原价: 可选,但不能低于现价
- 库存: 必填,不能为负数
- 主图URL: 必填

**使用场景:**
- 新商城上线批量导入商品数据
- 供应商提供商品清单快速导入
- 定期导出商品数据进行备份
- 导出数据进行Excel分析和统计
- 批量修改商品信息(导出→修改→导入)

**已完成文件:**
```
├── dto
│   ├── ProductImportDTO.java ✅
│   └── ProductQueryDTO.java ✅
├── vo
│   ├── ProductExportVO.java ✅
│   └── AdminProductVO.java ✅
├── controller
│   └── AdminProductController.java ✅ (新增3个接口)
├── service
│   ├── AdminProductService.java ✅ (新增3个方法)
│   └── impl
│       └── AdminProductServiceImpl.java ✅ (完整实现)
└── 前端
    ├── api/admin/product.js ✅ (新增3个API方法)
    └── views/admin/ProductManage.vue ✅ (新增导入导出按钮)
```

---

##### 2.3.2 订单管理 (已完成 ✅) 🎉
**优先级:** 最高 ⭐⭐⭐⭐⭐  
**当前进度:** 100% 🎉  
**完成日期:** 2026-01-30

**已完成功能:**
- [x] 订单列表查询 (分页/搜索/筛选) ✅
  - 支持按状态筛选 (待支付/待发货/待收货/已完成/已取消)
  - 支持按物流状态筛选 (未发货/运输中/派送中/已签收)
  - 支持时间范围筛选
  - 支持订单号/收货人/电话搜索
- [x] 订单详情查看 ✅
  - 订单基本信息 (订单号/用户/金额/状态)
  - 商品明细列表
  - 收货地址信息
  - 物流信息 (公司/单号/状态)
  - 备注信息 (用户备注/管理员备注)
- [x] 订单发货 ✅
  - 选择物流公司 (8家快递公司)
  - 填写物流单号
  - 更新订单状态 (待发货→待收货)
  - 更新物流状态 (未发货→运输中)
  - 添加发货备注
- [x] 订单派送 ✅ 🆕
  - 一键切换物流状态 (运输中→派送中)
  - 用户确认收货自动同步 (派送中→已签收)
- [x] 订单取消 ✅
  - 取消原因记录
  - 自动库存回滚
  - 支付退款处理
  - 状态校验 (只能取消待支付/待发货订单)
- [x] 订单备注 ✅
  - 管理员备注添加
  - 备注内容保存到 admin_remark 字段
  - 支持多行文本
- [x] 订单导出 ✅
  - Excel格式导出
  - 按条件筛选导出
  - 自动生成文件名 (订单列表_时间戳.xlsx)

**接口设计:**
```
GET    /api/admin/order/list            - 订单列表(分页) ✅
GET    /api/admin/order/detail/:id      - 订单详情 ✅
POST   /api/admin/order/ship            - 订单发货 ✅
POST   /api/admin/order/deliver         - 订单派送 ✅ 🆕
POST   /api/admin/order/cancel          - 订单取消 ✅
POST   /api/admin/order/remark          - 添加备注 ✅
GET    /api/admin/order/export          - 订单导出 ✅
```

**完整物流流程:**
```
用户下单 → 待支付 → 用户支付 → 待发货
    ↓
管理员发货 → 待收货 (物流状态: 运输中)
    ↓
管理员派送 → 待收货 (物流状态: 派送中) 🆕
    ↓
用户确认收货 → 已完成 (物流状态: 已签收) ✅
```

**已完成文件:**
```
后端:
├── dto
│   ├── OrderQueryDTO.java ✅
│   ├── OrderShipDTO.java ✅
│   ├── OrderDeliverDTO.java ✅ 🆕
│   ├── OrderCancelDTO.java ✅
│   └── OrderRemarkDTO.java ✅
├── vo
│   ├── AdminOrderVO.java ✅
│   ├── OrderItemVO.java ✅
│   └── OrderExportVO.java ✅
├── controller
│   └── AdminOrderController.java ✅ (7个接口)
├── service
│   ├── AdminOrderService.java ✅
│   └── impl
│       └── AdminOrderServiceImpl.java ✅

前端:
├── api/admin
│   └── order.js ✅ (7个API方法)
└── views/admin
    └── OrderManage.vue ✅ (完整页面+派送功能)
```

**技术亮点:**
- ✅ 表格布局优化 (使用min-width解决空白问题)
- ✅ 物流状态流转完整
- ✅ 库存自动回滚机制
- ✅ 订单状态校验严格
- ✅ Excel导出功能
- ✅ 用户确认收货自动同步物流状态

**数据库表:**
- `tb_order` (订单表,已包含物流字段) ✅
- `tb_order_item` (订单明细表) ✅

---


##### 2.3.3 退款审核管理 (已完成 ✅) 🆕
**优先级:** 高 ⭐⭐⭐⭐  
**当前进度:** 100% 🎉  
**完成日期:** 2026-02-25

**已完成功能:**
- [x] 退款申请列表 (分页/状态筛选/订单号搜索/时间范围)
- [x] 退款详情查看
  - 完整退款信息 (退款单号/金额/原因/类型/状态)
  - 用户信息 (用户名/昵称/手机号)
  - 订单信息 (订单金额/收货地址/订单状态)
  - 支付信息 (支付方式/支付金额/支付时间)
  - 订单商品列表
- [x] 审核通过 → 自动触发退款处理 → 发送通过通知
- [x] 审核拒绝 → 记录拒绝原因 → 发送拒绝通知
- [x] 退款处理 (自动模拟退款 + 失败重试机制)
- [x] 退款成功后同步更新订单状态 (待发货→已取消)
- [x] 更新支付记录状态为已退款(3)
- [x] 3种退款通知集成 (审核通过/拒绝/到账)
- [x] @AdminLog 操作日志记录

**接口设计:**
```
GET    /api/admin/refund/list           - 退款列表(分页) ✅
GET    /api/admin/refund/detail/:id     - 退款详情 ✅
POST   /api/admin/refund/approve/:id    - 审核通过 ✅
POST   /api/admin/refund/reject/:id     - 审核拒绝 ✅
POST   /api/admin/refund/process/:id    - 处理退款(失败重试) ✅
```

**退款状态流转:**
```
待审核(0) → 审核通过(1) → 退款成功(3)  [同时: 订单→已取消(若待发货), 支付→已退款]
待审核(0) → 审核拒绝(2)
退款失败(4) → 手动重试 → 退款成功(3)
```

**已完成文件:**
```
后端:
├── dto
│   ├── RefundQueryDTO.java ✅
│   └── RefundAuditDTO.java ✅
├── vo
│   ├── AdminRefundVO.java ✅
│   └── AdminRefundDetailVO.java ✅
├── controller
│   └── AdminRefundController.java ✅ (5个接口)
├── service
│   ├── AdminRefundService.java ✅
│   └── impl
│       └── AdminRefundServiceImpl.java ✅

前端:
├── api/admin
│   └── refund.js ✅ (5个API方法)
└── views/admin
    └── RefundManage.vue ✅ (完整退款审核页面)
```

**技术亮点:**
- ✅ 审核通过后自动触发退款，无需手动操作
- ✅ 退款成功同步订单状态（待发货→已取消）
- ✅ 失败重试机制（状态4可重新处理）
- ✅ 事务保证（@Transactional，失败自动回滚）
- ✅ 3种退款通知自动发送（@Async异步）
- ✅ 完整的状态校验和异常处理

**数据库表:**
- `tb_refund` (退款记录表,已包含admin_id/admin_remark/audit_time等审核字段) ✅
- `tb_payment` (退款成功后status更新为3) ✅
- `tb_order` (待发货退款成功后status更新为4) ✅

---

##### 2.3.4 评价管理 (已完成 ✅)
**优先级:** 中 ⭐⭐⭐  
**当前进度:** 100% 🎉  
**完成日期:** 2026-02-26  
**最后修复:** 2026-02-28

**已完成功能:**
- [x] 评价列表查询 (分页/商品名称模糊搜索/评分筛选/状态筛选/关键词/时间范围/置顶筛选) ✅
- [x] 评价详情查看 (含商品/用户/订单冗余信息) ✅
- [x] 商家回复评价 (支持新增回复和修改回复，用户端同步显示) ✅
- [x] 评价审核
  - [x] 审核通过 (待审核→正常) ✅
  - [x] 审核拒绝/删除不当评价 (待审核→删除) ✅
- [x] 置顶/取消置顶评价 (自动切换，记录置顶时间，多条置顶按时间排序) ✅
- [x] 删除评价 (status=0，用户我的评价页面保留记录显示"已被删除") ✅
- [x] 举报评价 (3次自动转待审核，防重复举报) ✅
- [x] 用户修改评价 (直接UPDATE，不走删除重发) ✅
- [x] 用户删除评价 (物理删除，可重新评价) ✅
- [x] 用户端置顶标识显示 (📌 置顶 Tag) ✅
- [x] 用户端商家回复显示 ✅
- [x] 用户端举报按钮 (确认后提交，防重复) ✅

**接口设计:**
```
后台管理端:
GET    /api/admin/review/list           - 评价列表(分页) ✅
GET    /api/admin/review/detail/:id     - 评价详情 ✅
POST   /api/admin/review/reply/:id      - 回复评价 ✅
POST   /api/admin/review/audit/:id      - 审核评价 ✅
POST   /api/admin/review/top/:id        - 置顶/取消置顶 ✅
DELETE /api/admin/review/delete/:id     - 删除评价 ✅

用户端:
POST   /api/review/publish              - 发布评价 ✅
PUT    /api/review/:id                  - 修改评价 ✅
DELETE /api/review/:id                  - 删除评价(用户自己) ✅
POST   /api/review/report/:id           - 举报评价 ✅
GET    /api/review/my/list              - 我的评价列表 ✅
GET    /api/review/list/advanced        - 商品评价列表(筛选+排序) ✅
```

**评价状态说明:**
```
status=0  已被管理员删除 → 用户我的评价页保留显示"已被删除"，无法修改
status=1  正常           → 可以回复、置顶、举报、修改
status=2  待审核         → 举报3次自动进入，管理员审核通过(→1)或拒绝(→0)
deleted=0 未删除         → 正常记录
deleted=1 用户自己删除   → 物理删除，可重新发布评价
```

**删除策略说明（关键设计）:**
```
管理员删除: status=0, deleted 不变(保持0)
  → 用户我的评价页仍能看到该记录，显示"已被管理员删除"
  → 用户无法重新评价(checkProductReviewed 检查 status=0 的记录)

用户自己删除: @TableLogic 自动设 deleted=1
  → 从我的评价页消失
  → 可以重新发布该订单商品的评价
```

**已完成文件:**
```
后端 (admin模块):
├── dto
│   ├── ReviewQueryDTO.java ✅
│   ├── ReviewReplyDTO.java ✅
│   └── ReviewAuditDTO.java ✅
├── vo
│   └── AdminReviewVO.java ✅ (@JsonInclude(ALWAYS))
├── entity
│   └── ReviewEntity.java ✅ (topTime, isTop IGNORED策略)
├── mapper
│   ├── AdminReviewMapper.java ✅
│   └── AdminReviewMapper.xml ✅
├── controller
│   └── AdminReviewController.java ✅ (6个接口)
└── service/impl
    └── AdminReviewServiceImpl.java ✅ (删除只设status=0，不动deleted)

后端 (product模块):
├── entity
│   └── Review.java ✅ (新增 reportCount 字段)
├── dto
│   ├── ReviewVO.java ✅ (status字段判断管理员删除)
│   └── ReviewDetailVO.java ✅ (isTop字段)
├── mapper
│   └── ReviewMapper.java ✅ (举报SQL，getUserReviewList加deleted=0过滤)
├── controller
│   └── ReviewController.java ✅ (新增举报/修改接口)
└── service/impl
    └── ReviewServiceImpl.java ✅ (reportReview, updateReview, getUserReviews修复)

前端:
├── api
│   └── review.js ✅ (新增 reportReview, updateReview)
├── api/admin
│   └── review.js ✅ (6个API方法)
├── views/admin
│   └── ReviewManage.vue ✅
└── views/product
    ├── ProductDetail.vue ✅ (举报按钮, 置顶Tag, 商家回复)
    └── MyReviews.vue ✅ (已删除提示, 修改改为直接PUT)
```

**数据库变更:**
```sql
-- ✅ 已执行(v7.0)：新增置顶字段
ALTER TABLE `tb_review`
    ADD COLUMN `is_top` tinyint(4) DEFAULT '0' COMMENT '是否置顶(0-否 1-是)' AFTER `reply_time`;

-- ✅ 已执行(v7.1)：新增置顶时间字段
ALTER TABLE `tb_review`
    ADD COLUMN `top_time` datetime DEFAULT NULL COMMENT '置顶时间' AFTER `is_top`;

-- ✅ 已执行(v7.2)：新增举报次数字段
ALTER TABLE `tb_review`
    ADD COLUMN `report_count` int(11) DEFAULT '0' COMMENT '被举报次数，达到3次自动转待审核' AFTER `top_time`;

-- ✅ 已执行(v7.2)：新建举报记录表
CREATE TABLE `tb_review_report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `review_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `reason` varchar(255) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_user` (`review_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价举报记录表';

-- ✅ 已执行(v7.2)：修正历史数据（管理员删除的记录deleted改回0）
UPDATE tb_review SET deleted = 0 WHERE status = 0;
```

**技术亮点:**
- ✅ 多表关联查询（评价+商品+用户+订单），一次查询获取所有信息
- ✅ 置顶时间排序（ORDER BY is_top DESC, top_time DESC），多条置顶按先后顺序
- ✅ 举报自动审核：用户举报3次 → status自动转2 → 管理端审核队列
- ✅ 删除策略分离：管理员删除(status=0)与用户删除(@TableLogic deleted=1)使用不同字段，避免逻辑冲突
- ✅ @TableLogic 与手写SQL共存：手写SQL必须手动加 `deleted=0` 过滤
- ✅ Integer.valueOf().equals() 代替 == 防止 null 拆箱 NPE
- ✅ 修改评价走 PUT 直接UPDATE，不走删除重发，解决重复评价校验冲突

**数据库表:**
- `tb_review` (评价表，已新增 is_top、top_time、report_count 字段) ✅
- `tb_review_report` (评价举报记录表) ✅

---

##### 2.3.5 用户管理 (已完成 ✅) 🎉
**优先级:** 中 ⭐⭐⭐  
**当前进度:** 100% 🎉  
**完成日期:** 2026-03-02

**已完成功能:**
- [x] 用户列表查询 (分页/多维度搜索/筛选) ✅
  - 关键词模糊搜索（用户名/昵称/手机/邮箱）
  - 按角色/状态/观测等级筛选
  - 按注册时间范围筛选
  - 排序：管理员置顶，同角色内按注册时间升序
- [x] 用户详情查看 ✅
  - 基本信息（头像/昵称/联系方式/城市/等级/标签）
  - 消费统计（订单数/完成数/消费总额/退款次数/评价次数）
  - 近期订单（最近5条）
  - 近期登录日志（最近5条）
- [x] 用户状态管理 ✅
  - 禁用/启用用户（支持填写原因）
  - 操作记录到管理员日志（@AdminLog）
- [x] 用户角色管理 ✅
  - 普通用户↔管理员角色切换
  - 前端二次确认弹窗

**接口设计:**
```
GET    /api/admin/user/list             - 用户列表(分页) ✅
GET    /api/admin/user/detail/:id       - 用户详情 ✅
POST   /api/admin/user/status/:id       - 修改状态 ✅
PUT    /api/admin/user/role/:id         - 设置角色 ✅
```

**技术亮点:**
- ✅ CAST(field AS UNSIGNED) AS 唯一别名 — 根本解决 MySQL JDBC tinyint→Boolean 映射问题
- ✅ 跨模块统计：汇总 order/payment/product 模块数据
- ✅ 排序：`ORDER BY role DESC, create_time ASC`（管理员置顶，同角色早注册在上）
- ✅ selectMapsPage 不暴露 password 等敏感字段

**已完成文件:**
```
后端 (admin模块):
├── dto
│   ├── UserQueryDTO.java ✅
│   ├── UserStatusDTO.java ✅
│   └── UserRoleDTO.java ✅
├── vo
│   ├── AdminUserVO.java ✅ (列表展示，含订单统计)
│   └── AdminUserDetailVO.java ✅ (详情，含消费统计+订单历史+登录日志)
├── service
│   ├── AdminUserService.java ✅
│   └── impl
│       └── AdminUserServiceImpl.java ✅
└── controller
    └── AdminUserController.java ✅ (4接口+@AdminLog日志)

前端:
├── api/admin
│   └── user.js ✅ (4个API方法)
└── views/admin
    └── UserManage.vue ✅ (搜索/列表/详情抽屉/状态弹窗/角色变更/统计卡片)
```

---

##### 2.3.6 数据统计 (已完成 ✅) 🎉
**优先级:** 高 ⭐⭐⭐⭐  
**当前进度:** 100% 🎉  
**完成日期:** 2026-03-02

**已完成功能:**
- [x] 数据概览 ✅
  - 今日订单数/销售额（含同比增长率）
  - 本月订单数/销售额
  - 待处理订单数、待审核退款数、库存预警数
- [x] 销售统计 ✅
  - 销售趋势折线图（支持近7/30/90天）
  - 商品销售排行TOP10
  - 分类销售占比饼图
- [x] 订单统计 ✅
  - 订单状态分布圆环图
  - 订单金额区间分布横向柱状图
- [x] 用户统计 ✅
  - 新增用户趋势（支持近7/30天）
  - 活跃度仪表盘（近30天活跃用户/总用户）
  - 省份分布TOP10、用户等级分布
- [x] 评价统计 ✅
  - 评分分布（1-5星）、好评率/总评价数/平均分
  - 评价趋势折线图

**接口设计:**
```
GET    /api/admin/statistics/overview      - 数据概览 ✅
GET    /api/admin/statistics/sales-trend   - 销售趋势(支持7/30/90天) ✅
GET    /api/admin/statistics/order-status  - 订单统计 ✅
GET    /api/admin/statistics/user-trend    - 用户统计(支持7/30天) ✅
GET    /api/admin/statistics/review        - 评价统计(支持7/30天) ✅
```

**技术亮点:**
- ✅ 连续日期序列SQL（LEFT JOIN，无数据日期自动补0）
- ✅ BigDecimal/tinyint类型安全转换
- ✅ 10个ECharts图表（折线/柱状/饼图/仪表盘）
- ✅ 窗口resize自动调整图表尺寸

**已完成文件:**
```
后端:
├── controller/AdminStatisticsController.java ✅
├── service/AdminStatisticsService.java ✅
├── service/impl/AdminStatisticsServiceImpl.java ✅
├── mapper/AdminStatisticsMapper.java ✅
├── mapper/AdminStatisticsMapper.xml ✅
└── vo/ StatisticsOverviewVO / SalesTrendVO / OrderStatisticsVO / UserStatisticsVO / ReviewStatisticsVO ✅

前端:
├── api/admin/statistics.js ✅
└── views/admin/Statistics.vue ✅ (10个ECharts图表)
```

**字段名避坑记录:**
```
tb_order_item: price → product_price(单价), 销售额直接用 total_price
tb_category:   name  → category_name
tb_product:    warning_stock 不存在 → 固定阈值 stock <= 10
tb_order:      actual_amount → payment_amount
tb_login_log:  create_time  → login_time
```

---

##### 2.3.7 分类管理 (已完成 ✅) 🎉
**优先级:** 中 ⭐⭐⭐
**当前进度:** 100% 🎉
**完成日期:** 2026-03-03

**已完成功能:**
- [x] 分类列表(树形结构) ✅
  - 一级+二级树形展示，默认全展开
  - 显示商品数量、排序值、显示状态、描述、创建时间
- [x] 分类新增 ✅
  - 新增一级分类（parentId=0）
  - 新增二级分类（从一级行点击"添加子分类"）
  - 同级名称唯一性校验
- [x] 分类编辑 ✅
  - 修改名称/图标/排序/描述/显示状态
  - 不允许修改 parentId 和 level（层级不可变）
- [x] 分类删除 ✅
  - 删除前检查关联商品数量，有商品则拒绝并提示数量
  - 级联逻辑删除子分类（deleted=1）
  - 前端确认弹窗提示子分类数量
- [x] 分类排序 ✅
  - 上移/下移按钮（sort±10）
  - 批量更新sort字段
- [x] 显示/隐藏状态切换 ✅
  - el-switch直接切换，立即生效

**接口设计:**
```
GET    /api/admin/category/tree         - 分类树（含商品数量）✅
POST   /api/admin/category/add          - 新增分类 ✅
PUT    /api/admin/category/update/:id   - 编辑分类 ✅
DELETE /api/admin/category/delete/:id   - 删除分类 ✅
POST   /api/admin/category/sort         - 分类排序 ✅
```

**技术亮点:**
- ✅ 树形构建用 Map 方式（O(n)，解决sort降序导致子分类先于父分类处理的Bug）
- ✅ @TableLogic 自动过滤 deleted，查询无需手动加 deleted=0
- ✅ 商品关联检查：统计分类及所有子分类下的商品总数
- ✅ el-dialog 加 align-center + CSS 修复滚动页面弹窗位置偏移问题

**⚠️ Bug记录 & 修复:**
```
Bug: 书籍资料等sort值较小的一级分类，其子分类在voMap中先被遍历，
     后遍历到父分类时执行 setChildren(new ArrayList<>()) 覆盖了已添加的子分类。
修复: 在第一步存入voMap时统一初始化children，第二步组装树不再setChildren。
```

**已完成文件:**
```
后端 (admin模块):
├── dto
│   ├── AddCategoryDTO.java ✅
│   └── SortCategoryDTO.java ✅
├── vo
│   └── CategoryTreeVO.java ✅
├── service
│   ├── AdminCategoryService.java ✅
│   └── impl
│       └── AdminCategoryServiceImpl.java ✅
└── controller
    └── AdminCategoryController.java ✅ (5个接口+@AdminLog日志)

前端:
├── api/admin
│   └── category.js ✅ (5个API方法)
└── views/admin
    └── CategoryManage.vue ✅ (树形表格+新增/编辑对话框+排序+开关)
```

**数据库表:**
- `tb_category` (分类表，已存在，无需新建) ✅

---

##### 2.3.7 操作日志 (已完成 ✅) 🎉
**优先级:** 低 ⭐⭐  
**当前进度:** 100% 🎉  
**完成日期:** 2026-03-04

**已完成功能:**
- [x] 日志列表查询 ✅
  - 支持按操作类型模糊筛选
  - 支持按管理员姓名模糊筛选
  - 支持时间范围筛选
  - 支持状态筛选（成功/失败）
- [x] 日志详情查看 ✅
  - 操作人、操作类型、操作状态
  - 请求方法（全限定类名.方法名）
  - 请求参数（JSON格式化展示）
  - IP地址、执行耗时、User-Agent
  - 错误信息（失败时展示）
- [x] 日志导出（Excel格式） ✅

**接口设计:**
```
GET    /api/admin/log/list              - 日志列表(分页) ✅
GET    /api/admin/log/detail/:id        - 日志详情 ✅
GET    /api/admin/log/export            - 导出日志(Excel) ✅
```

**技术亮点:**
- ✅ 不加 @AdminLog 注解（避免循环记录日志）
- ✅ 列表页不查 params 大字段（按需加载，提升翻页性能）
- ✅ exportLog 使用独立原生 axios 实例（避免 responseType:blob 污染全局 request）
- ✅ 操作类型 Tag 颜色自动识别（删除/禁用→红，通过/启用→绿，修改→橙）
- ✅ 执行耗时颜色区分（<200ms绿，<1000ms橙，≥1000ms红）
- ✅ 时间格式自动补全（前端传 yyyy-MM-dd，后端自动补 00:00:00/23:59:59）

**已完成文件:**
```
后端:
├── dto
│   └── AdminLogQueryDTO.java ✅ (pageNum/pageSize/operation/adminName/status/时间范围)
├── vo
│   └── AdminLogVO.java ✅ (含statusText展示字段，LocalDateTime时间类型)
├── mapper
│   ├── AdminLogMapper.java ✅ (selectPageByCondition + selectListForExport)
│   └── AdminLogMapper.xml ✅ (列表不查params大字段，导出查全字段，LIMIT 10000)
├── service
│   ├── AdminLogService.java ✅
│   └── impl
│       └── AdminLogServiceImpl.java ✅ (Hutool ExcelWriter导出，LocalDateTime格式化)
└── controller
    └── AdminLogController.java ✅ (3个接口，无@AdminLog注解)

前端:
├── api/admin
│   └── log.js ✅ (getLogList/getLogDetail/exportLog，exportLog用独立axios实例)
└── views/admin
    └── LogManage.vue ✅ (筛选+分页列表+详情弹窗+Excel导出)
```

**⚠️ 重要避坑记录:**
```
问题: responseType:'blob' 传入全局 request 实例后，
     后续所有请求响应都被当作 blob 处理，res.data 取不到数据，页面全空白
修复: exportLog 改用独立原生 axios({...}) 实例，完全隔离，不影响全局 request
```

**数据库表:**
- `tb_admin_log` (管理员操作日志表，已存在，无需新建) ✅

---

##### 2.3.8 系统设置 (已完成 ✅) 🎉
**优先级:** 低 ⭐⭐
**当前进度:** 100% 🎉
**完成日期:** 2026-03-05

**已完成功能:**
- [x] 基础设置 (商城名称/Logo/简介/联系方式/备案/版权) ✅
- [x] 运费设置 (默认运费/包邮开关/包邮金额) ✅
- [x] 支付设置 (支付方式开关/超时/自动确认/自动关闭) ✅
- [x] SEO设置 (网站标题/关键词/描述) ✅
- [x] 注册设置 (注册开关/邮箱验证/邀请制/默认头像) ✅
- [x] 维护模式 (开关/提示语/预计恢复时间/二次确认弹窗) ✅
- [x] 运费联动 (OrderServiceImpl.calcFreight() 动态读取/CheckoutPage.vue 实时显示) ✅
- [x] 支付联动 (PaymentPage.vue 动态过滤支付方式/读取超时时间驱动倒计时) ✅
- [x] 注册联动 (Register.vue 读取开关，关闭时显示提示拒绝注册) ✅
- [x] 维护模式联动 (MaintenancePage.vue 新建/router 守卫拦截普通用户/管理员不受影响) ✅
- [x] 基础设置联动 (Home.vue 读取商城名称+版权信息) ✅
- [x] 自动关闭订单定时任务 (OrderScheduleTask.java 每天凌晨2点读取 auto_close_days 关闭超时订单并释放库存) ✅

**技术要点:**
- 键值对表设计 (`tb_system_setting`)，新增配置项只需 INSERT，无需 ALTER TABLE
- setting_key 全部使用蛇形命名（snake_case），与后端 Java 读取一致
- Upsert 逻辑：selectByGroupAndKey → 存在 updateById，不存在 insert
- 前端左侧菜单改用普通 div（不用 el-menu），避免与 AdminLayout 外层 el-menu(router) 冲突导致页面空白
- 按需加载：切换分组时才发起对应 GET 请求
- 维护模式开启有二次确认弹窗（高危操作防误触）
- 支付方式至少开启一种（前后端双重校验）
- 所有 PUT 接口加 @AdminLog 记录操作日志
- JwtInterceptor 白名单 + AdminInterceptor excludePathPatterns 放行3个公开接口（maintenance/register/payment GET 无需登录）
- router 守卫维护模式放行 /login、/register，管理员退出后可正常回到登录页
- payTimeoutMinutes：前端倒计时用（分钟级实时提示）；autoCloseDays：后端定时任务用（天级兜底清理）
- @EnableScheduling 开启定时任务支持

**接口设计:**
```
GET    /api/admin/setting/basic          - 获取基础设置
PUT    /api/admin/setting/basic          - 更新基础设置
GET    /api/admin/setting/freight        - 获取运费设置
PUT    /api/admin/setting/freight        - 更新运费设置
GET    /api/admin/setting/payment        - 获取支付设置
PUT    /api/admin/setting/payment        - 更新支付设置
GET    /api/admin/setting/seo            - 获取SEO设置
PUT    /api/admin/setting/seo            - 更新SEO设置
GET    /api/admin/setting/register       - 获取注册设置
PUT    /api/admin/setting/register       - 更新注册设置
GET    /api/admin/setting/maintenance    - 获取维护模式
PUT    /api/admin/setting/maintenance    - 更新维护模式
```

**后端文件清单:**
```
module/admin/
├── entity/SystemSetting.java ✅
├── mapper/SystemSettingMapper.java ✅
├── dto/BasicSettingDTO.java ✅
├── dto/FreightSettingDTO.java ✅
├── dto/PaymentSettingDTO.java ✅
├── dto/SeoSettingDTO.java ✅
├── dto/RegisterSettingDTO.java ✅
├── dto/MaintenanceSettingDTO.java ✅
├── vo/BasicSettingVO.java ✅
├── vo/FreightSettingVO.java ✅
├── vo/PaymentSettingVO.java ✅
├── vo/SeoSettingVO.java ✅
├── vo/RegisterSettingVO.java ✅
├── vo/MaintenanceSettingVO.java ✅
├── service/AdminSettingService.java ✅
├── service/impl/AdminSettingServiceImpl.java ✅
└── controller/AdminSettingController.java ✅

module/order/
└── task/OrderScheduleTask.java ✅  ← 新建，每天凌晨2点自动关闭超时订单

AstronomyMallApplication.java ✅  ← 新增 @EnableScheduling
```

**前端文件清单:**
```
src/
├── api/admin/setting.js ✅ (12个API方法)
├── views/admin/SystemSetting.vue ✅
├── views/MaintenancePage.vue ✅ (新建)
├── views/Register.vue ✅ (注册开关联动)
├── views/payment/PaymentPage.vue ✅ (支付方式+超时时间联动)
├── views/Home.vue ✅ (商城名称+版权信息联动)
└── router/index.js ✅ (维护模式守卫)
```

**数据库:**
- `tb_system_setting` (系统设置键值对表) ✅ 新建

---

#### 2.4 个人空间 (已完成 ✅)
**开发时间:** 待定  
**优先级:** 中 ⭐⭐⭐

**模块说明:**  
为用户提供统一的个人中心，参考淘宝/京东风格，左侧导航 + 右侧内容区布局。将现有分散的功能（订单、评价）整合进来，并补充三块实质性新功能：**收货地址管理**（填补现有体验缺口）、**钱包系统**（支撑结算抵扣与二手回收到账）、**账号安全**（修改密码）。

**⚠️ 现有问题说明（开发前必读）:**
```
tb_order 的收货人信息 (receiver_name/phone/province/city/district/address)
目前是在 CheckoutPage.vue 每次下单时手动填写，没有地址簿。
用户每次购物都要重新填地址，体验很差。
本模块将新建 tb_address 表，改造 CheckoutPage 为"选地址"模式。
```

---

##### 2.4.1 个人中心布局与概览页 ✅
**完成时间:** 2026-03-07

**页面结构:**
```
/user                          ← UserLayout.vue (左侧导航容器)
├── /user/overview             ← 概览首页
├── /user/orders               ← 我的订单 (复用 OrderList.vue)
├── /user/address              ← 收货地址
├── /user/favorites            ← 我的收藏 (第8周开发，位置预留)
├── /user/reviews              ← 我的评价 (复用 MyReviews.vue)
├── /user/wallet               ← 我的钱包
├── /user/after-sale           ← 我的售后 (2.5节开发后接入)
└── /user/settings             ← 账号设置
```

**概览页 (UserOverview.vue) 实际布局 (3行紧凑设计):**
```
┌──────────────────────────────────────────────────────────────┐
│ 第一行: 用户信息卡                                            │
│  [头像] 昵称  ★★★★★ 专家级玩家   │  累计订单 │ 累计消费 │ 评价数 │
│         城市 · 注册时间             │   18    │ ¥2.4k  │   6    │
│         🔭行星 🔭深空               └──────────────────────────┘
├───────────────────────────────────┬──────────────────────────┤
│ 第二行左: 我的订单 (5格)            │ 第二行右: 我的钱包        │
│  [待付][待发][待收][待评][退款/售后] │  ¥0.00   [充值][提现]    │
│  (数字角标显示待处理数量)            │  最近: 回收入账 +¥200    │
├───────────────────────────────────┴──────────────────────────┤
│ 第三行: 最近订单 (最新2条，有则显示，无则隐藏)                  │
│  [图] 订单号 商品名                         ¥xxx  待收货 →    │
└──────────────────────────────────────────────────────────────┘
```

**接口 (概览页专用，新增1个):**
```
GET /api/user/overview   - 用户概览数据
  返回: 头像/昵称/等级/城市/标签 + 各状态订单数 + 余额 + 最近一笔流水
       + 累计订单数 + 累计消费金额 + 已发布评价数
  ⚠️ 聚合查询，一个接口返回所有概览数据，避免前端多次请求
```

**新增/修改的文件清单:**
```
后端 (6个):
  com.astronomy.mall.module.user.vo.UserOverviewVO            ← 聚合VO (新增totalOrders/totalSpent/totalReviews)
  com.astronomy.mall.module.user.mapper.UserOverviewMapper    ← 6个查询方法
  resources/mapper/UserOverviewMapper.xml                     ← CASE WHEN SUM聚合SQL
  com.astronomy.mall.module.user.service.UserOverviewService  ← 接口定义
  com.astronomy.mall.module.user.service.impl.UserOverviewServiceImpl ← 聚合查询实现
  com.astronomy.mall.module.user.controller.UserOverviewController   ← GET /api/user/overview

前端 (8个):
  src/api/user/overview.js           ← getUserOverview() API方法
  src/views/user/UserLayout.vue      ← 个人中心容器 (左侧导航 + router-view)
  src/views/user/UserOverview.vue    ← 概览首页 (3行紧凑布局)
  src/views/user/UserAddress.vue     ← ✅ 2.4.2已完成 (地址管理完整页面)
  src/views/user/UserWallet.vue      ← 占位页 (2.4.4节完整实现)
  src/views/user/UserSettings.vue    ← 占位页 (2.4.5节完整实现)
  src/router/index.js                ← 新增 /user 嵌套路由
  src/views/Home.vue                 ← 个人中心跳转改为 /user/overview
```

**⚠️ 路由改造说明:**
```
现有独立路由:
  /order/list   → 保留原路径，同时在 /user/orders 也可访问
  /review/my    → 保留原路径，同时在 /user/reviews 也可访问
原路径全部保留不删除，防止通知跳转链接失效。
新增 /user 嵌套路由，子页面复用原有组件。
```

**检查清单:**
- [x] 开发 UserLayout.vue (左侧导航 + 激活状态)
- [x] 开发 UserOverview.vue (用户信息卡 + 统计格 + 订单格子 + 钱包 + 最近订单)
- [x] 开发 /api/user/overview 聚合接口
- [x] 配置 /user 嵌套路由，子页面复用已有组件
- [x] 顶部导航头像点击跳转 /user/overview

**预计工时:** 1.5天  
**实际工时:** 1.5天

---

##### 2.4.2 收货地址管理 (已完成 ✅)
**完成日期:** 2026-03-08

**功能说明:**  
用户可管理多个收货地址，最多5个，可设置默认地址。结算页改为"选地址"模式，不再每次手填。

**用户端功能:**
- [x] 地址列表 (最多5个，默认地址置顶显示)
- [x] 新增地址 (姓名/手机/省市区/详细地址/设为默认)
- [x] 编辑地址
- [x] 删除地址 (有订单关联的地址可以删，订单里已有快照，互不影响)
- [x] 设为默认地址

**结算页改造 (CheckoutPage.vue):**
```
改造完成: 手动填写4个输入框 → 展示地址列表单选卡片
- 自动选中默认地址（isDefault=1）
- 无地址时显示空状态，跳转地址管理页
- 提交订单时只传 addressId，后端自动快照
```

**接口设计 (5个):** ✅
```
GET    /api/address/list          - 我的地址列表
POST   /api/address/add           - 新增地址
PUT    /api/address/update/:id    - 编辑地址
DELETE /api/address/delete/:id    - 删除地址
POST   /api/address/default/:id   - 设为默认地址
```

**数据库表:** ✅ tb_address 已创建
```sql
CREATE TABLE `tb_address` (
  `id`             bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id`        bigint(20)   NOT NULL COMMENT '用户ID 📌关联tb_user.id',
  `receiver_name`  varchar(50)  NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20)  NOT NULL COMMENT '收货人手机号',
  `province`       varchar(50)  NOT NULL COMMENT '省份',
  `city`           varchar(50)  NOT NULL COMMENT '城市',
  `district`       varchar(50)  NOT NULL COMMENT '区县',
  `detail`         varchar(200) NOT NULL COMMENT '详细地址',
  `is_default`     tinyint(4)   DEFAULT '0' COMMENT '是否默认地址(0-否 1-是)',
  `create_time`    datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';
```

**已完成文件:**
```
后端 (module/user/):
├── entity/Address.java                    ✅
├── mapper/AddressMapper.java              ✅ clearDefault + setDefault
├── mapper/AddressMapper.xml               ✅ 默认地址置顶排序
├── dto/AddressDTO.java                    ✅ 含手机号正则校验
├── vo/AddressVO.java                      ✅ 含 getFullAddress()
├── service/AddressService.java            ✅
├── service/impl/AddressServiceImpl.java   ✅ 事务/最多5个/删默认自动转移
└── controller/AddressController.java      ✅

前端:
├── src/api/address.js                     ✅
├── src/utils/regionData.js                ✅ 全国34省市区完整数据
├── src/views/user/UserAddress.vue         ✅ 地址列表+新增/编辑弹窗
└── src/views/order/CheckoutPage.vue       ✅ 改造完成：地址选择替换手填表单
```

**OrderServiceImpl & CreateOrderDTO 改造:** ✅
```
CreateOrderDTO: 删除6个地址字段 → 新增 addressId (Long)
OrderServiceImpl.createOrder(): 注入 AddressService
  → 通过 addressId 查询 tb_address
  → 将地址字段快照到 tb_order 的 receiver_* 字段
  → 地址删除后历史订单收货信息不受影响
```

**检查清单:**
- [x] 建表 tb_address
- [x] 开发地址 CRUD 接口 (5个)
- [x] 开发 UserAddress.vue (省市区完整数据 regionData.js)
- [x] 改造 CheckoutPage.vue (地址选择模式)
- [x] 改造 OrderServiceImpl.createOrder() (接收 addressId，自动快照到订单)
- [x] 测试：新增地址→结算选地址→下单→删除地址→历史订单地址不受影响

**实际工时:** 1.5天

---

##### 2.4.3 账号设置 (已完成 ✅)
**完成日期:** 2026-03-09

**功能说明:**  
用户编辑个人资料、上传头像和修改密码。个人信息编辑的 GET/PUT 接口模块1已完成，本节新增修改密码接口，前端整合进个人中心。

**用户端功能:**
- [x] 基本资料编辑 (昵称/邮箱/手机) ← 复用已有接口
- [x] 所在地区选择 (自定义两列弹窗：左选省份右选城市，内联34省数据，不依赖外部文件)
- [x] 兴趣标签编辑 (预设12个标签，最多选8个，chip风格) ← 复用已有接口
- [x] 观测等级展示 (只读，5级颜色区分)
- [x] 头像照片Tab：本地图片上传（FileReader转base64，无需后端接口）+ URL直链输入 + 精选头像
- [x] 修改密码 (需验证旧密码，三色密码强度条，新密码二次确认) ← **新增接口**

**新增接口 (1个):**
```
POST /api/user/change-password    - 修改密码
  入参: oldPassword, newPassword, confirmPassword
  校验: 旧密码正确 + 新密码长度6-20位 + 两次新密码一致 + 新旧密码不能相同
  成功: 前端清除Token → 跳转 /login?message=password_changed
```

**⚠️ 修改密码后的处理:**
```
JWT无状态设计: 不做服务端Token失效
密码修改成功 → 前端收到成功响应后主动 removeToken() + userStore.$reset()
→ 跳转 /login?message=password_changed
→ Login.vue onMounted 检测到 message 参数，提示密码已修改，请重新登录
```

**⚠️ 数据库变更（需手动执行）:**
```sql
-- 头像字段扩容，支持base64图片存储（原VARCHAR(255)不够）
ALTER TABLE tb_user MODIFY COLUMN avatar MEDIUMTEXT COMMENT '头像URL或base64图片数据';
```

**前端页面结构 (三Tab):**
```
AccountSettings.vue
├── Tab1 基本资料   昵称/邮箱/手机 + 所在地区(自定义省市弹窗) + 兴趣标签 + 观测等级(只读)
├── Tab2 头像照片   本地上传按钮(FileReader base64) + URL输入框 + 精选头像网格 + 效果预览
└── Tab3 修改密码   旧密码 + 新密码(三色强度条ul/li) + 确认密码 + 二次确认弹窗
```

**已完成文件:**
```
后端 (module/user/):
├── dto/ChangePasswordDTO.java              ✅ 含@NotBlank校验
├── service/UserService.java                ✅ 新增 changePasswordSecure 方法声明
├── service/impl/UserServiceImpl.java       ✅ 新增 changePasswordSecure 实现
└── controller/UserController.java          ✅ 新增 POST /change-password 端点

前端:
├── src/api/user.js                         ✅ 新增 changePasswordSecure 方法
├── src/views/user/AccountSettings.vue      ✅ 三Tab完整页面（替换原占位UserSettings.vue）
├── src/views/user/UserOverview.vue         ✅ 新增storeToRefs同步，头像/昵称优先读store
├── src/router/index.js                     ✅ settings路由改为AccountSettings.vue
└── src/views/Login.vue                     ✅ onMounted检测password_changed参数弹出提示
```

**检查清单:**
- [x] 开发 change-password 接口
- [x] 开发 AccountSettings.vue（三Tab）
- [x] 本地头像上传（FileReader base64）
- [x] 省市自定义两列弹窗选择器（内联34省数据）
- [x] 密码强度三色条（ul/li方案，红/橙/绿固定色）
- [x] UserOverview.vue store同步（保存后概览页头像实时更新）
- [x] 执行 ALTER TABLE 扩容 avatar 字段为 MEDIUMTEXT
- [x] 测试修改密码后自动登出

**实际工时:** 1天

---

##### 2.4.4 钱包系统 (已完成 ✅)

**功能说明:**  
用户钱包，余额来源包括：二手回收到账、模拟充值。在支付页面直接选择"余额支付"扣款，余额不足时引导充值。退款时判断原支付方式，余额支付的退款原路返回钱包。

**用户端功能:**
- [x] 钱包页 (余额卡片 + 充值/提现按钮 + 流水列表分页)
- [x] 模拟充值 (任意金额，立即到账，记录流水)
- [x] 模拟提现 (扣减余额，记录流水)
- [x] 余额流水分页列表 (类型图标区分：充值🟢 提现🔴 回收到账🟢 购物扣款🔴)

**支付页联动 (PaymentPage.vue 改造):**
- [x] 余额支付选项实时显示当前余额（绿色=充足 / 红色=不足）
- [x] 余额不足时显示"需充值 ¥XX"提示条 + "去充值"按钮（跳转钱包页充值Tab）
- [x] 确认支付按钮余额不足时置灰，前后端双重校验
- [x] 余额支付走 PaymentServiceImpl → BalanceService.changeBalance() 真实扣款

**结算页 (CheckoutPage.vue 简化):**
- [x] 删掉余额抵扣区域，恢复简洁下单流程
- [x] 下单后统一跳支付页，在支付页选择支付方式（含余额支付）

**退款回钱包 (AdminRefundServiceImpl 改造):**
- [x] 审核通过后 doProcessRefund() 判断原支付方式
- [x] paymentType=3（余额支付）→ 退款金额加回钱包，写流水（备注退款单号）
- [x] paymentType=1/2（支付宝/微信）→ 原逻辑不变，模拟原渠道退款

**个人概览页钱包实时刷新 (UserOverview.vue 改造):**
- [x] 钱包数据独立调用 getWallet() 接口，与 overview 完全解耦
- [x] 30秒轮询 + onActivated（keep-alive 切回立刻刷新）+ visibilitychange（切回Tab刷新）
- [x] 手动刷新按钮 + "刚刚更新/X秒前更新"时间提示

**余额变动统一入口 (BalanceService):**
```java
// 文件: BalanceService.java (module/user/ 下)
// ⚠️ 所有余额变动必须走此方法，严禁直接 UPDATE tb_user SET balance=xxx

@Transactional
public void changeBalance(Long userId, BigDecimal amount, Integer type,
                          String remark, Long relatedId, String relatedType) {
    // 1. SELECT ... FOR UPDATE 行锁，防止并发扣款超额
    User user = userMapper.selectByIdForUpdate(userId);
    BigDecimal before = user.getBalance();
    BigDecimal after = before.add(amount);

    // 2. 余额不能为负
    if (after.compareTo(BigDecimal.ZERO) < 0) {
        throw new BusinessException("余额不足");
    }

    // 3. 更新余额
    userMapper.updateBalance(userId, after);

    // 4. 同事务记录流水 (余额与流水必须同时成功或回滚)
    BalanceLog log = BalanceLog.builder()
        .userId(userId).type(type).amount(amount)
        .balanceBefore(before).balanceAfter(after)
        .remark(remark).relatedId(relatedId).relatedType(relatedType)
        .build();
    balanceLogMapper.insert(log);
}
```

**接口设计 (4个):**
```
GET    /api/user/wallet               - 查询余额 + 近20条流水
GET    /api/user/balance-log/list     - 完整流水列表(分页)
POST   /api/user/wallet/recharge      - 模拟充值
POST   /api/user/wallet/withdraw      - 模拟提现
```

**文件结构:**
```
后端新建 (module/user/):
├── entity/BalanceLog.java               ✅
├── mapper/BalanceLogMapper.java         ✅
├── mapper/xml/BalanceLogMapper.xml      ✅
├── service/BalanceService.java          ✅  统一入口，含行锁
├── service/impl/BalanceServiceImpl.java ✅
├── controller/WalletController.java     ✅  4个接口
├── dto/RechargeDTO.java                 ✅
├── dto/WithdrawDTO.java                 ✅
└── vo/WalletVO.java                     ✅  余额 + 流水列表

后端改造:
├── module/user/mapper/UserMapper.java                    ✅  新增 selectByIdForUpdate + updateBalance
├── module/user/mapper/xml/UserMapper.xml                 ✅  新增2条SQL
├── module/payment/service/impl/PaymentServiceImpl.java   ✅  payment_type=3 真实扣款
├── module/admin/service/impl/AdminRefundServiceImpl.java ✅  doProcessRefund 判断支付方式退回钱包

前端新建:
├── api/wallet.js                        ✅
└── views/user/Wallet.vue                ✅  余额卡片 + 充值/提现弹窗 + 流水列表

前端改造:
├── views/order/CheckoutPage.vue         ✅  删掉余额抵扣区域，简化下单流程
├── views/payment/PaymentPage.vue        ✅  余额支付选项显示余额 + 不足引导充值
├── views/user/UserOverview.vue          ✅  钱包数据独立实时刷新
└── router/index.js                      ✅  wallet路由指向 Wallet.vue
```

**检查清单:**
- [x] 执行 ALTER TABLE tb_user ADD COLUMN balance
- [x] 建表 tb_balance_log
- [x] 开发 BalanceService (行锁防并发)
- [x] 开发 WalletController (4个接口)
- [x] 改造 UserMapper (selectByIdForUpdate + updateBalance)
- [x] 改造 PaymentServiceImpl (payment_type=3 真实扣款)
- [x] 改造 CheckoutPage.vue (删掉余额抵扣，恢复简洁下单)
- [x] 改造 PaymentPage.vue (余额支付显示余额 + 不足引导充值)
- [x] 开发 Wallet.vue (完整钱包页)
- [x] 改造 AdminRefundServiceImpl (doProcessRefund 余额退回钱包)
- [x] 改造 UserOverview.vue (钱包实时刷新：轮询+onActivated+visibilitychange)
- [x] 测试：充值→余额支付→余额不足充值引导→提现→退款回钱包→流水全流程

**实际工时:** 2天

---

**📋 2.4 个人空间 汇总**

**新增数据库表 (2张):**
- `tb_address` (收货地址表) ⬜
- `tb_balance_log` (余额流水表) ✅ 2.4.1已创建

**tb_user 字段扩展 (1个):**
- 新增 `balance` 字段 ✅ 2.4.1已执行

**新增接口汇总:**
```
概览:    GET  /api/user/overview              (1个)
地址:    GET/POST/PUT/DELETE/POST × 5         (5个)
密码:    POST /api/user/change-password       (1个)
钱包:    GET/GET/POST/POST × 4               (4个)
合计: 11个新接口
```

**需改造的现有文件:**
- `OrderServiceImpl.createOrder()` ← 接收 addressId 参数 ✅
- `PaymentServiceImpl` ← payment_type=3 真实扣款 ✅
- `CheckoutPage.vue` ← 地址选择（余额抵扣已删除，改为支付页直选） ✅
- `PaymentPage.vue` ← 余额支付显示余额 + 不足引导充值 ✅
- `AdminRefundServiceImpl` ← doProcessRefund 余额退回钱包 ✅
- `UserOverview.vue` ← 钱包实时独立刷新 ✅
- `router/index.js` ← 新增 /user 嵌套路由，wallet指向 Wallet.vue ✅

**个人空间总预计工时:** 5.5天 ✅ 已全部完成
(概览+布局1.5天 + 地址管理1.5天 + 账号设置0.5天 + 钱包2天)

---

#### 2.5 售后服务 (已完成 ✅)
**开发时间:** 待定  
**优先级:** 低 ⭐⭐

---

##### 2.5.1 安装预约 (已完成 ✅) 🎉
**完成日期:** 2026-03-10

**功能说明:**  
用户购买望远镜、赤道仪等大型器材后，可在线预约上门安装调试服务。提交时地址自动从关联订单带入，无需重填。管理员确认后发送通知。

**⚠️ 前置校验 (提交预约时后端必须校验):**
```
1. order_id 必须属于当前登录用户 (防越权)
2. 订单状态必须是 status=2(待收货) 或 status=3(已完成)
   → 待支付/待发货的订单还没到手，不能预约安装
3. 同一订单不能重复提交预约
```

**已完成功能:**
- [x] 提交安装预约 (选择订单/商品，自动带入订单地址，填写期望时间和备注) ✅
- [x] 查看我的预约列表与状态 ✅
- [x] 取消待确认的预约 ✅
- [x] 管理员预约列表查询 (分页/状态筛选/时间范围) ✅
- [x] 管理员确认预约 (填写确认时间/工程师姓名/联系方式) ✅
- [x] 管理员取消预约 (填写取消原因) ✅
- [x] 确认后自动发送 MALL_INSTALLATION_CONFIRMED 通知 ✅

**状态定义:**
```
0 - 待确认  ✅ 用户可取消
1 - 已确认  ❌ 等待上门
2 - 已取消  ❌ 终态 (用户取消或管理员取消)
```

**接口设计 (共6个):** ✅
```
用户端:
POST   /api/installation/submit          - 提交安装预约 (含前置校验)
GET    /api/installation/my/list         - 我的预约列表
POST   /api/installation/cancel/:id      - 取消预约

管理员端 (在 module/admin/controller/ 下):
GET    /api/admin/installation/list      - 预约列表(分页)
POST   /api/admin/installation/confirm/:id - 确认预约(填写工程师信息+发通知)
POST   /api/admin/installation/cancel/:id  - 取消预约
```

**数据库表:** ✅ tb_installation 已创建

**通知集成 (2个):** ✅
```
管理员确认后 → 发送 MALL_INSTALLATION_CONFIRMED 通知
管理员取消后 → 发送 MALL_INSTALLATION_CANCELLED 通知 (含取消原因)
NotificationType.java 已新增: INSTALLATION_CONFIRMED / INSTALLATION_CANCELLED
tb_notification_template 已插入2条对应模板
```

**⚠️ 避坑记录 — LocalDateTime 反序列化失败:**
```
问题: Jackson 默认要求 ISO-8601 格式 (含T), 无法将 "2026-03-12 00:03:00" 
     反序列化为 LocalDateTime。application.yml 的 date-format 只对 java.util.Date 生效。
修复: 在 DTO 字段上加 @JsonFormat 注解

// InstallationApplyDTO.java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime expectedTime;

// InstallationConfirmDTO.java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime confirmedTime;
```

**已完成文件:**
```
后端 (module/aftersale/ 新建):
├── entity/Installation.java              ✅
├── mapper/InstallationMapper.java        ✅
├── mapper/InstallationMapper.xml         ✅
├── dto/InstallationApplyDTO.java         ✅
├── vo/InstallationVO.java                ✅
├── service/InstallationService.java      ✅
├── service/impl/InstallationServiceImpl.java ✅
└── controller/InstallationController.java ✅

后端 (module/admin/ 扩展):
├── dto/InstallationQueryDTO.java         ✅
├── dto/InstallationConfirmDTO.java       ✅
├── dto/InstallationAdminCancelDTO.java   ✅
├── vo/AdminInstallationVO.java           ✅
├── service/AdminInstallationService.java ✅
├── service/impl/AdminInstallationServiceImpl.java ✅
└── controller/AdminInstallationController.java ✅

通知:
└── helper/NotificationHelper.java       ✅ (新增 sendInstallationConfirmedNotification / sendInstallationCancelledNotification)

前端:
├── api/installation.js                  ✅
├── api/admin/installation.js            ✅
├── views/afterSale/InstallationList.vue ✅ (卡片式订单+商品选择)
└── views/admin/InstallationManage.vue   ✅
```

**现有文件改造:**
```
router/index.js       ✅ 新增3条路由 (/after-sale/installation + /user/installation + /admin/installation)
AdminLayout.vue       ✅ 侧边栏新增"安装预约管理"菜单项 (Tools图标)
UserLayout.vue        ✅ 侧边栏"我的售后"(disabled) → "安装预约"(enabled)
UserOverview.vue      ✅ 订单状态格子 5列 → 6列，新增"安装预约"格
```

**检查清单:**
- [x] 建表 tb_installation ✅
- [x] 开发用户端接口 (3个，含订单归属+状态校验) ✅
- [x] 开发管理员端接口 (3个，含通知集成) ✅
- [x] NotificationType.java 新增 INSTALLATION_CONFIRMED / INSTALLATION_CANCELLED ✅
- [x] tb_notification_template 新增2条模板 (已手动执行SQL) ✅
- [x] InstallationApplyDTO / InstallationConfirmDTO 加 @JsonFormat 修复 LocalDateTime 反序列化 ✅
- [x] 开发 InstallationList.vue (卡片列表式订单/商品选择弹窗) ✅
- [x] 开发 InstallationManage.vue ✅
- [x] 改造 router/index.js ✅
- [x] 改造 AdminLayout.vue ✅
- [x] 改造 UserLayout.vue ✅
- [x] 改造 UserOverview.vue ✅
- [x] 测试完整预约流程 ✅

**实际工时:** 2天

---

##### 2.5.2 器材保养提醒 (已完成 ✅)

**功能说明:**  
用户可为自己的天文器材设置定期保养提醒（光学清洁、赤道仪校准等），系统在前端展示"即将到期"提示。**不做定时推送通知**，用户主动进入页面查看，类似手机备忘录。

> ⚠️ **命名说明:** 后端所有类名使用 `ServiceReminder` 前缀，避免与已有的 `MaintenanceSettingDTO`（系统维护模式 DTO）发生命名冲突。

**用户端功能:**
- [x] 查看我的保养提醒列表 (按到期时间排序，到期≤7天标红提示) ✅
- [x] 新增保养提醒 (选择器材名称、保养类型、提醒日期) ✅
- [x] 编辑保养提醒 ✅
- [x] 删除保养提醒 ✅
- [x] 标记已完成 (完成后可选择下次提醒日期) ✅

**接口设计 (共4个，仅用户端):**
```
GET    /api/service-reminder/list        - 我的提醒列表
POST   /api/service-reminder/add         - 新增
PUT    /api/service-reminder/update/:id  - 编辑
DELETE /api/service-reminder/delete/:id  - 删除
```

> **无管理员端接口**，管理员不需要管用户的个人保养计划。

**数据库表设计:**
```sql
CREATE TABLE `tb_service_reminder` (
  `id`            bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '提醒ID',
  `user_id`       bigint(20)   NOT NULL COMMENT '用户ID 📌关联tb_user.id',
  `product_name`  varchar(200) NOT NULL COMMENT '器材名称(用户自填，不强制关联商品表)',
  `remind_type`   varchar(20)  DEFAULT 'custom' COMMENT '保养类型(clean光学清洁/calibrate校准/check常规检查/custom自定义)',
  `remind_title`  varchar(100) NOT NULL COMMENT '提醒标题',
  `remind_date`   date         NOT NULL COMMENT '提醒日期',
  `is_done`       tinyint(4)   DEFAULT '0' COMMENT '是否已完成(0否/1是)',
  `done_time`     datetime     DEFAULT NULL COMMENT '完成时间',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_remind_date` (`remind_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='器材保养提醒表';
```

**前端"即将到期"提示逻辑:**
```javascript
// views/afterSale/ServiceReminderList.vue
// 前端根据 remind_date 计算，不依赖后端推送
const getDaysUntil = (remindDate) => {
  const diff = dayjs(remindDate).diff(dayjs(), 'day')
  return diff
}
// diff <= 7 && !isDone → 标红显示"还有X天"
// diff < 0 && !isDone → 显示"已逾期"
```

**文件结构:**
```
后端 (module/aftersale/):
├── entity/ServiceReminder.java
├── mapper/ServiceReminderMapper.java
├── dto/ServiceReminderDTO.java
├── vo/ServiceReminderVO.java
├── service/ServiceReminderService.java
├── service/impl/ServiceReminderServiceImpl.java
└── controller/ServiceReminderController.java

前端:
├── api/serviceReminder.js
└── views/afterSale/ServiceReminderList.vue
```

**检查清单:**
- [x] 建表 tb_service_reminder ✅
- [x] 开发4个接口 ✅
- [x] 开发 ServiceReminderList.vue (含到期颜色提示) ✅
- [x] 测试增删改查流程 ✅

**预计工时:** 1.5天

---

##### 2.5.3 二手回收 (已完成 ✅) 🆕
**完成日期:** 2026-03-11

**功能说明:**  
用户提交旧器材回收申请，填写器材信息和文字描述。管理员审核后给出回收报价，用户确认后管理员安排快递上门取件，货到平台后将回收款以余额形式发放到用户钱包。

> ⚠️ **依赖说明:** 本模块依赖 `2.4 个人空间与钱包` 中的 `BalanceService`，已在钱包模块完成后开发。

**已完成功能:**
- [x] 提交回收申请 (器材名称/品牌/型号/成色/文字描述) ✅
- [x] 查看申请列表与状态 ✅
- [x] 查看报价详情 ✅
- [x] 确认或拒绝管理员报价 ✅
- [x] 取消待审核的申请 ✅
- [x] 管理员回收申请列表 (分页/状态筛选) ✅
- [x] 管理员查看申请详情 ✅
- [x] 管理员提交报价 (填写回收金额和备注) ✅
- [x] 管理员拒绝申请 (填写原因) ✅
- [x] 管理员安排取件 (填写快递公司/快递单号) ✅
- [x] 管理员标记已回收 → **自动将余额发放到用户钱包** ✅

**状态定义:**
```
回收状态:
0 - 待审核   ✅ 用户可取消
1 - 已报价   ✅ 等待用户确认/拒绝
2 - 用户确认 ✅ 等待管理员安排取件
3 - 待取件   ✅ 快递正在上门取件中 (已有快递单号)
4 - 已回收   ❌ 终态 (余额已自动到账)
5 - 已拒绝   ❌ 终态 (管理员拒绝)
6 - 用户取消 ❌ 终态
```

**状态流转图:**
```
待审核(0) → 已报价(1) → 用户确认(2) → 待取件(3) → 已回收(4) [余额自动到账]
         ↘ 已拒绝(5)  ↘ 用户取消(6)
待审核(0) → 用户取消(6)
```

**成色等级:**
```
S - 全新/几乎未使用
A - 九成新，无明显磨损
B - 七八成新，有轻微使用痕迹
C - 六成以下，有明显使用痕迹或瑕疵
```

**接口设计 (共12个):** ✅
```
用户端 (6个):
POST   /api/recycling/submit             - 提交回收申请 ✅
GET    /api/recycling/my/list            - 我的申请列表 ✅
GET    /api/recycling/detail/:id         - 申请详情 ✅
POST   /api/recycling/confirm/:id        - 确认报价 ✅
POST   /api/recycling/reject-quote/:id   - 拒绝报价 ✅
POST   /api/recycling/cancel/:id         - 取消申请 ✅

管理员端 (6个):
GET    /api/admin/recycling/list         - 申请列表(分页) ✅
GET    /api/admin/recycling/detail/:id   - 申请详情 ✅
POST   /api/admin/recycling/quote/:id    - 提交报价 ✅
POST   /api/admin/recycling/reject/:id   - 拒绝申请 ✅
POST   /api/admin/recycling/arrange/:id  - 安排取件(填快递信息) ✅
POST   /api/admin/recycling/complete/:id - 标记已回收(触发余额发放) ✅
```

**数据库表设计:**
```sql
CREATE TABLE `tb_recycling` (
  `id`               bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `recycle_no`       varchar(32)   NOT NULL COMMENT '回收单号',
  `user_id`          bigint(20)    NOT NULL COMMENT '用户ID 📌关联tb_user.id',
  `product_name`     varchar(200)  NOT NULL COMMENT '器材名称',
  `brand`            varchar(100)  DEFAULT NULL COMMENT '品牌',
  `model`            varchar(100)  DEFAULT NULL COMMENT '型号',
  `condition_level`  varchar(5)    NOT NULL COMMENT '成色等级(S/A/B/C)',
  `description`      varchar(1000) DEFAULT NULL COMMENT '器材描述(问题/配件/使用情况等)',
  `assessed_price`   decimal(10,2) DEFAULT NULL COMMENT '管理员报价金额',
  `admin_remark`     varchar(500)  DEFAULT NULL COMMENT '管理员报价备注/拒绝原因',
  `logistics_company` varchar(100) DEFAULT NULL COMMENT '上门取件快递公司',
  `tracking_number`  varchar(100)  DEFAULT NULL COMMENT '取件快递单号',
  `status`           tinyint(4)    DEFAULT '0' COMMENT '状态(0待审核/1已报价/2用户确认/3待取件/4已回收/5已拒绝/6用户取消)',
  `confirm_time`     datetime      DEFAULT NULL COMMENT '用户确认时间',
  `complete_time`    datetime      DEFAULT NULL COMMENT '回收完成时间',
  `admin_id`         bigint(20)    DEFAULT NULL COMMENT '操作管理员ID',
  `create_time`      datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time`      datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recycle_no` (`recycle_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手回收申请表';
```

**标记已回收的核心逻辑:**
```java
// 文件: AdminRecyclingServiceImpl.java
// 方法: completeRecycling(Long id)
// 📌 依赖: BalanceService (module/user/)

@Transactional
public void completeRecycling(Long id) {
    Recycling recycling = recyclingMapper.selectById(id);

    // 1. 校验状态 (必须是待取件状态)
    if (recycling.getStatus() != 3) {
        throw new BusinessException("状态不正确，请先安排取件");
    }

    // 2. 调用 BalanceService 发放余额 (原子操作 + 自动记录流水)
    balanceService.changeBalance(
        recycling.getUserId(),
        recycling.getAssessedPrice(),           // 正数 = 收入
        3,                                       // type=3 回收入账
        "二手回收: " + recycling.getRecycleNo(),
        recycling.getId(),
        "recycling"
    );

    // 3. 更新回收申请状态
    recycling.setStatus(4);
    recycling.setCompleteTime(LocalDateTime.now());
    recyclingMapper.updateById(recycling);

    // 4. 发送通知
    notificationHelper.sendRecyclingCompleteNotification(
        recycling.getUserId(),
        recycling.getRecycleNo(),
        recycling.getAssessedPrice(),
        recycling.getId()
    );
}
```

**通知集成 (1个):**
```
已回收时 → 发送 MALL_RECYCLING_COMPLETED 通知
需在 NotificationType.java 新增: RECYCLING_COMPLETED("recycling_completed", "二手回收款已到账")
需在 tb_notification_template 新增对应模板
content: 您的回收申请{recycleNo}已完成，¥{amount}已到账至您的钱包
```

**已完成文件:**
```
后端:
module/aftersale/
├── entity/Recycling.java                        ✅
├── mapper/RecyclingMapper.java                  ✅
├── dto/RecyclingApplyDTO.java                   ✅  (含 images 字段: String JSON)
├── vo/RecyclingVO.java                          ✅  (含 images 字段透传)
├── service/RecyclingService.java                ✅
├── service/impl/RecyclingServiceImpl.java       ✅
└── controller/RecyclingController.java          ✅  @RequestMapping("/api/recycling")

module/admin/
├── dto/RecyclingQueryDTO.java                   ✅
├── dto/RecyclingQuoteDTO.java                   ✅
├── dto/RecyclingRejectDTO.java                  ✅
├── dto/RecyclingArrangeDTO.java                 ✅
├── vo/AdminRecyclingVO.java                     ✅  (含 images 字段透传)
├── service/AdminRecyclingService.java           ✅
├── service/impl/AdminRecyclingServiceImpl.java  ✅
└── controller/AdminRecyclingController.java     ✅  @RequestMapping("/api/admin/recycling")

通知:
└── helper/NotificationHelper.java              ✅ (新增 sendRecyclingCompleteNotification)

前端:
├── api/recycling.js                            ✅  (用户端6个API)
├── api/admin/recycling.js                      ✅  (管理员端6个API)
├── views/afterSale/RecyclingList.vue           ✅  (卡片列表+提交弹窗)
└── views/admin/RecyclingManage.vue             ✅  (申请管理+快递填写+完成按钮+图片画廊)
```

**检查清单:**
- [x] 确认 2.4 钱包模块已完成 (BalanceService 可用) ✅
- [x] 建表 tb_recycling ✅ (含 images MEDIUMTEXT 字段)
- [x] 开发用户端接口 (6个) ✅
- [x] 开发管理员端接口 (6个) ✅
- [x] 在 NotificationType.java 新增 RECYCLING_COMPLETED ✅
- [x] 在 tb_notification_template 新增模板 ✅
- [x] 在 NotificationHelper 新增 sendRecyclingCompleteNotification() ✅
- [x] 开发 RecyclingList.vue + RecyclingManage.vue ✅
- [x] 前端图片上传：Canvas压缩(1200px/0.82) → base64 → JSON存储 ✅
- [x] 卡片缩略图 + 详情画廊 + el-image-viewer 大图预览 ✅
- [x] 测试完整回收流程 + 余额到账验证 ✅

**实际工时:** 2天

---

**📋 2.5 售后服务 汇总**

**数据库表 (新增3张):**
- `tb_installation` (安装预约表) ✅ 2026-03-10已创建
- `tb_service_reminder` (器材保养提醒表) ✅
- `tb_recycling` (二手回收申请表) ✅ 2026-03-11已创建

**通知模板 (需新增2个):**
- `MALL_INSTALLATION_CONFIRMED` (安装预约已确认) ✅ 2026-03-10已完成
- `MALL_RECYCLING_COMPLETED` (二手回收款已到账) ✅ 2026-03-11已完成

**NotificationType.java 需新增枚举值 (2个):**
```java
// 售后服务模块 (2种) - 新增到 NotificationType.java
INSTALLATION_CONFIRMED("installation_confirmed", "安装预约已确认"),  // ✅ 已完成
RECYCLING_COMPLETED("recycling_completed", "二手回收款已到账"),
```

**售后服务总实际工时:** 5.5天 (安装预约2天 + 保养提醒1.5天 + 二手回收2天)

---

#### 2.6 商品收藏 ✅ 已完成 (2026-03-12)
**开发时间:** 2026-03-12  
**优先级:** 低 ⭐

**功能说明:**  
用户对感兴趣的商品打心标收藏，随时查看，方便比价或下单。侧边栏"我的收藏"入口（现为"即将上线"占位）正式开放。

**用户端功能:**
- [x] 商品详情页「收藏」按钮（已收藏变红心，再点取消）✅
- [x] 个人中心 → 我的收藏 列表（商品图片 + 名称 + 价格 + 去购买 + 取消收藏）✅
- [x] 商品下架/删除后，收藏列表标注"已下架"，不影响浏览 ✅
- [x] 价格变化标注（↓降价了红色标签 / ↑涨价了橙色标签 + 划线原价）✅

**数据库:**
```sql
CREATE TABLE `tb_product_favorite` (
  `id`            bigint(20)     NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id`       bigint(20)     NOT NULL COMMENT '用户ID',
  `product_id`    bigint(20)     NOT NULL COMMENT '商品ID',
  `product_name`  varchar(200)   DEFAULT NULL COMMENT '商品名称(冗余快照)',
  `product_price` decimal(10,2)  DEFAULT NULL COMMENT '收藏时价格(用于涨跌对比)',
  `product_image` varchar(500)   DEFAULT NULL COMMENT '商品图片(冗余快照)',
  `create_time`   datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';
```

**接口设计 (4个):**
```
POST   /api/favorite/toggle/{productId}   - 收藏/取消（幂等切换）
GET    /api/favorite/list                 - 我的收藏列表（分页）
GET    /api/favorite/check/{productId}    - 查询是否已收藏（商品详情页用）
DELETE /api/favorite/{productId}          - 取消收藏
```

**文件结构:**
```
后端 (module/favorite/ 新模块):
├── entity/ProductFavorite.java
├── mapper/ProductFavoriteMapper.java
├── service/FavoriteService.java
├── service/impl/FavoriteServiceImpl.java
├── controller/FavoriteController.java
├── vo/FavoriteVO.java              ← 含 isOffShelf/isPriceDown/isPriceUp/currentPrice/favoritePrice
└── task/PriceDropScheduler.java    ← 每天凌晨2点降价检测定时任务

前端:
├── api/favorite.js
├── views/user/UserFavorite.vue     ← 收藏列表页（网格布局+下架遮罩+涨跌标签+分页）
└── 改造: views/product/ProductDetail.vue  ← 新增收藏按钮
```

**改造现有文件:**
- `router/index.js` ← favorites路由改为 UserFavorite.vue
- `ProductDetail.vue` ← 进入页面时调 check 接口，按钮状态同步；点击调 toggle
- `UserLayout.vue` ← 侧边栏"我的收藏"去掉"即将上线"标签
- `AdminProductServiceImpl.java` ← updateStatus()末尾集成上架通知（收藏用户批量发）

**通知集成:**
- 上架通知：商品上架时，遍历 tb_product_favorite 批量通知所有收藏用户（MALL_PRODUCT_ON_SALE）
- 降价通知：PriceDropScheduler 每天凌晨2点比对价格，降价则发通知（MALL_PRODUCT_PRICE_DOWN）

**⚠️ 启用条件:**
- AstronomyMallApplication.java 需加 `@EnableScheduling` 注解（定时任务生效）

**检查清单:**
- [x] 建表 tb_product_favorite ✅
- [x] 开发 FavoriteService (toggle 幂等逻辑) ✅
- [x] 开发 FavoriteController (4个接口) ✅
- [x] 开发 UserFavorite.vue ✅
- [x] 改造 ProductDetail.vue (收藏按钮) ✅
- [x] 改造 router/index.js ✅
- [x] 集成上架通知 (AdminProductServiceImpl) ✅
- [x] 开发降价检测定时任务 (PriceDropScheduler) ✅

**预计工时:** 1天（实际完成）

---

---

#### 2.7 NASA API 集成 ✅
**开发时间:** 第11周（课程模块开始前，必须先完成）  
**状态:** ✅ 2.7.1 + 2.7.2 已完成（2026-03-19）| 2.7.3 随模块5同期开发  
**优先级:** 高 ⭐⭐⭐⭐（模块5的前置依赖，已满足）  
**实际工时:** 1天  
**API Key:** `faxoHiBTRduPxmHntIYuRhpExwhnwk34m5NUOOVj`

> ⚠️ **必须先于模块5开发**：课程模块的 `APODSyncScheduler` 和 `MarsRoverSyncScheduler`
> 都注入 `NasaApiService`，如果 `module/nasa/` 不存在，模块5项目会启动报错。

**核心设计：NasaApiService 共享缓存**
```
module/nasa/
  NasaApiService
    ├── getTodayApod()          → 商城首页 ApodCard + APODSyncScheduler 共用
    └── getLatestMarsPhotos()   → MarsRoverSyncScheduler 调用
```
> NASA API 每小时限1000次，当日内存缓存保证每天实际最多调用 NASA 一次。

**application.yml 配置（开发前追加）:**
```yaml
nasa:
  api-key: faxoHiBTRduPxmHntIYuRhpExwhnwk34m5NUOOVj
```

---

##### 2.7.1 NasaApiService 核心服务 ✅
**完成目标:** 搭建 module/nasa/ 模块，实现 NASA API 统一调用入口和当日内存缓存，供课程模块定时任务注入使用。这是整个 NASA 集成的基础，必须第一个完成。  
**完成日期:** 2026-03-19

**功能:**
- [x] NasaApiService.getTodayApod()（调用 NASA APOD API，当日内存缓存）✅
- [x] NasaApiService.getLatestMarsPhotos()（调用 Mars Rover API，Perseverance 优先切 Curiosity）✅
- [x] GET /api/nasa/apod 接口（公开，无需登录）✅
- [x] WebMvcConfig excludePathPatterns 新增 `/api/nasa/**` ✅

**新增后端文件:**
```
module/nasa/
├── service/NasaApiService.java             ← 接口定义
├── service/impl/NasaApiServiceImpl.java    ← @Value nasa.api-key + synchronized 内存缓存
├── controller/NasaController.java          ← GET /api/nasa/apod
└── vo
    ├── ApodVO.java                         ← date/title/explanation/url/hdurl/mediaType/copyright
    └── MarsPhotoVO.java                    ← imgSrc/cameraFullName/earthDate
```

**NasaApiServiceImpl 核心逻辑:**
```java
@Value("${nasa.api-key}")
private String nasaApiKey;

// APOD 当日缓存
private ApodVO todayApodCache = null;
private LocalDate apodCacheDate = null;

public synchronized ApodVO getTodayApod() {
    LocalDate today = LocalDate.now();
    if (todayApodCache != null && today.equals(apodCacheDate)) return todayApodCache;
    String url = "https://api.nasa.gov/planetary/apod?api_key=" + nasaApiKey;
    ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
    Map<String, Object> body = resp.getBody();
    ApodVO vo = new ApodVO();
    vo.setDate((String) body.get("date"));
    vo.setTitle((String) body.get("title"));
    vo.setExplanation((String) body.get("explanation"));
    vo.setUrl((String) body.get("url"));
    vo.setHdurl((String) body.get("hdurl"));
    vo.setMediaType((String) body.get("media_type"));
    vo.setCopyright((String) body.get("copyright"));
    todayApodCache = vo;
    apodCacheDate = today;
    return vo;
}

public List<MarsPhotoVO> getLatestMarsPhotos() {
    // 1. 先请求 Perseverance
    String url = "https://api.nasa.gov/mars-photos/api/v1/rovers/perseverance/latest_photos?api_key=" + nasaApiKey;
    // 2. 若 latest_photos 为空，切换 Curiosity
    // 3. 取前3张，组装 MarsPhotoVO { imgSrc, cameraFullName, earthDate }
}
```

**新增接口 (1个):**
```
GET /api/nasa/apod     - 获取今日 APOD（公开，含当日内存缓存）
```

**改造现有文件:**
```
WebMvcConfig.java     ← JwtInterceptor excludePathPatterns 新增 "/api/nasa/**"
application.yml       ← 新增 nasa.api-key 配置
```

**检查清单:**
- [x] application.yml 新增 `nasa.api-key` ✅
- [x] 新建 module/nasa/ 目录结构（NasaApiService + NasaController + ApodVO + MarsPhotoVO + NasaConfig）✅
- [x] NasaApiServiceImpl 实现 getTodayApod()（内存缓存 + synchronized）✅
- [x] NasaApiServiceImpl 实现 getLatestMarsPhotos()（Perseverance → Curiosity 降级）✅
- [x] NasaController 开发 GET /api/nasa/apod ✅
- [x] WebMvcConfig 新增 /api/nasa/** 白名单 ✅
- [x] 本地测试：调用接口确认 APOD 数据返回正常，再次调用确认命中缓存 ✅

---

##### 2.7.2 APOD 首页展示 ✅
**完成目标:** 商城首页在商品推荐区域上方展示 NASA 当日天文图片横幅，失败时静默隐藏不影响主内容。  
**完成日期:** 2026-03-19（随 2.7.1 同步完成）

**功能:**
- [x] ApodCard.vue 组件（骨架屏 + 图片/视频自适应 + 说明文字折叠 + 全屏预览 + **中英文切换**）✅
- [x] Home.vue 引入 ApodCard（失败静默隐藏）✅
- [x] api/nasa.js 封装 getTodayApod() ✅

**UI 交互:**
```
加载中   → el-skeleton 骨架屏（1行大图 + 2行文字）
image类型 → el-image（fit=cover，hdurl优先，支持点击全屏预览）
video类型 → <iframe>（YouTube embed，frameborder=0 allowfullscreen）
explanation → 超过100字折叠，「展开全文」展开
右上角   → 📡 NASA APOD · 日期 小角标
🇨🇳/🇺🇸  → MyMemory 免费翻译API，分段处理长文，翻译结果前端缓存，中英一键切换
失败     → loadFailed=true，整个卡片 v-if 隐藏，静默不报错
```

**新增前端文件:**
```
api/nasa.js                 ← getTodayApod()  ✅
components/ApodCard.vue     ← 完整组件（骨架屏+折叠+全屏预览+video iframe+中英切换）✅
```

**改造现有文件:**
```
views/Home.vue    ← import ApodCard，在 user-center 与 recommend-section 之间插入 <ApodCard />  ✅
```

**检查清单:**
- [x] 开发 api/nasa.js（getTodayApod）✅
- [x] 开发 ApodCard.vue（骨架屏+图片/视频+折叠+全屏预览+失败静默隐藏+**中英文切换**）✅
- [x] Home.vue 引入 ApodCard 组件 ✅
- [x] 测试：图片类型正常显示 / 失败时整卡隐藏不影响商城内容 ✅

---

##### 2.7.3 Mars Rover 同步（随模块5同期开发）✅ 2026-03-20完成
**完成目标:** NasaApiService 的 getLatestMarsPhotos() 已在 2.7.1 完成，本节只需在模块5建表后添加 MarsRoverSyncScheduler 定时任务，以及预置火星课程种子数据。

> ⚠️ 前置条件：tb_course 已建表（含 is_mars_course 字段），模块5建表完成后执行。  
> 📌 **已在模块5（5.2节）中声明：** `MarsRoverSyncScheduler` 列在 5.2 检查清单，开发课程模块时一并完成。

**功能:**
- [ ] MarsRoverSyncScheduler（每天凌晨2:30，在 APODSyncScheduler 之后30分钟）
- [ ] 以 earth_date 去重，已同步过的日期跳过
- [ ] 每日最多同步3张图，生成图文富文本章节
- [ ] 同步成功后复用 `sendCourseApodUpdatedNotification()` 通知收藏用户
- [ ] 预置「火星探测车日志」课程种子数据（is_mars_course=1）

**新增后端文件:**
```
module/course/task/MarsRoverSyncScheduler.java   ← @Scheduled(cron="0 30 2 * * ?")
  注入: NasaApiService / CourseMapper / CourseChapterMapper / NotificationHelper
```

**预置种子数据（tb_course 建表后执行）:**
```sql
INSERT INTO tb_course (title, subtitle, type, is_mars_course, status, difficulty, tags)
VALUES ('火星探测车日志',
        '每天自动同步 NASA 毅力号/好奇号最新火星表面影像，见证红色星球探索',
        1, 1, 1, 1,
        '["NASA科普","火星探测","天体物理"]');
```

**检查清单:**
- [x] 开发 MarsRoverSyncScheduler（注入 NasaApiService，凌晨2:30执行）
- [x] 执行火星课程种子数据 INSERT SQL
- [ ] 测试：手动触发定时任务，确认章节正常插入 + 通知正常发送

---

**📋 2.7 开发顺序总结**

```
开发顺序（重要！）:

Step 1 ← 模块5前必须完成 ✅ 已完成
  2.7.1 NasaApiService + NasaController（0.5天）✅ 2026-03-19
  2.7.2 ApodCard.vue + Home.vue（0.5天）✅ 2026-03-19
  ↓ 完成后才能开始模块5 ✅ 前置依赖已满足

Step 2 ← 随模块5同期 ✅ 已完成
  2.7.3 MarsRoverSyncScheduler + 火星课程种子数据（0.5天）✅ 2026-03-20完成
  APODSyncScheduler 改为注入 NasaApiService（5分钟改造）✅ 2026-03-20完成

总工时: 1.5天（2.7.1+2.7.2 = 1天✅，2.7.3 = 0.5天随模块5⬜）
```


### 🔔 3. 消息通知模块 (独立模块)
**开发时间:** 第8周  
**状态:** ✅ **全部完成 100%** — 核心框架 + 后台消息管理 + 全模块业务集成(35条模板/12种通知) + 铃铛推荐Tab

#### 3.1 模块概述

```
设计理念: 为全系统提供统一的消息通知服务
架构特点: 独立通用模块,高度解耦
核心功能: 通知发送、未读管理、模板系统、用户偏好
扩展方向: 短信、邮件、App推送
```

---

#### 3.2 核心功能 (已完成 80% ✅)

**✅ 已完成功能:**
- [x] 消息通知发送 (支持单个/批量) ✅
- [x] 未读数量统计 (总数+按模块统计) ✅
- [x] 标记已读 (单个/批量/全部) ✅
- [x] 消息删除 ✅
- [x] 消息跳转 (点击跳转到相关页面) ✅
- [x] 通知模板系统 ✅
- [x] 前端通知铃铛组件 ✅

**⬜ 待完成功能:**
- [ ] 通知偏好设置 (用户可关闭某类通知)
- [ ] 定时自动刷新未读数 (30秒)
- [ ] 按优先级显示 (普通/重要/紧急)

**数据库表:** ✅ 已创建 (3张，无需新增)
- `tb_notification` (消息通知表) ✅
- `tb_notification_template` (通知模板表，已初始化16个模板) ✅
- `tb_user_notification_setting` (用户通知设置表) ✅

**API接口:** ✅ 已完成7个
```
GET    /api/notification/list           - 通知列表(分页/筛选) ✅
GET    /api/notification/unread-count   - 未读数量统计 ✅
POST   /api/notification/mark-read      - 标记已读(支持批量) ✅
POST   /api/notification/mark-all-read  - 全部标记为已读 ✅
DELETE /api/notification/:id            - 删除通知 ✅
GET    /api/notification/settings       - 获取通知设置 ✅
POST   /api/notification/settings       - 更新通知设置 ✅
```

**前端组件:** ✅ 已完成
- `components/NotificationBell.vue` (通知铃铛组件) ✅
- `components/NotificationSettings.vue` (通知设置页面) ✅
- `api/notification.js` (API封装) ✅

**技术亮点:**
- ✅ 独立通用模块设计,高度解耦
- ✅ 模板化通知内容,支持变量替换
- ✅ 异步发送机制(@Async),不阻塞业务流程
- ✅ 支持按模块分类管理

---

#### 3.3 已初始化的通知模板 (22个) ✅

**商城模块模板(13个):** ✅
1. MALL_ORDER_PAID - 订单支付成功
2. MALL_ORDER_SHIPPED - 订单已发货
3. MALL_ORDER_DELIVERING - 订单派送中
4. MALL_ORDER_COMPLETED - 订单已完成
5. MALL_ORDER_CANCELLED - 订单已取消
6. MALL_REFUND_APPROVED - 退款审核通过
7. MALL_REFUND_REJECTED - 退款审核拒绝
8. MALL_REFUND_COMPLETED - 退款已到账
9. MALL_PRODUCT_ON_SALE - 商品上架提醒
10. MALL_PRODUCT_PRICE_DOWN - 商品降价提醒
11. MALL_INSTALLATION_CONFIRMED - 安装预约已确认 ✅ (2026-03-10)
12. MALL_INSTALLATION_CANCELLED - 安装预约已取消 ✅ (2026-03-10)
13. MALL_RECYCLING_COMPLETED - 二手回收款已到账 ✅ (2026-03-11)

**系统模块模板(4个):** ✅
14. SYSTEM_ANNOUNCEMENT - 系统公告
15. SYSTEM_SECURITY - 账号安全
16. SYSTEM_VERSION_UPDATE - 版本更新
17. SYSTEM_PROMOTION - 活动推广

**AI识别模块模板(2个):** ✅ (2026-03-16已执行INSERT)
18. AI_RECOGNITION_COMPLETED - 星图识别成功（发现天体：{objectNames}）
19. AI_RECOGNITION_FAILED - 星图识别失败（原因：{failReason}）

**课程模块模板(3个):** ✅ 2026-03-22已执行INSERT
20. COURSE_CHAPTER_ADDED - 课程新增章节通知（通知收藏该课程的用户）
21. COURSE_APOD_UPDATED - NASA每日图片更新通知（通知收藏APOD课程的用户）
22. COURSE_COMPLETED - 课程学习完成通知（通知用户自己完成了某门课程）

**地理位置模块模板(2个):** ⬜ (地理位置模块开发时执行INSERT)
23. LOCATION_CHECKIN_SUCCESS - 观测点签到成功通知
24. LOCATION_WEATHER_SUITABLE - 今晚观测条件极佳提醒（预留，按需启用）

⚠️ 注: 实际模板总数33个（商城13 + 系统4 + AI识别2 + 课程3 + 地理位置2 + 论坛9 + 推荐系统2）

**⚠️ 其他模块模板待开发时初始化:**
- ~~AI识别模块: 2个模板~~ ✅ 已完成
- ~~课程模块: 3个模板~~ ✅ 2026-03-22已完成
- ~~地理位置: 2个模板~~ ⬜ 地理位置模块开发时初始化
- 论坛模块: 9个模板 (第14-15周) ⬜ 规划完成，含完整SQL（见7.7节）
- 推荐系统: 2个模板 (第16周) ⬜

---

#### 3.4 后台消息管理 (已完成 ✅)

**优先级:** 高 ⭐⭐⭐⭐  
**开发时间:** 第8周 Day 7-10

**⚠️ 设计决策：不新建 tb_announcement 表**
> 公告本质是"批量发给所有用户的系统通知"，直接复用 `tb_notification` 表和 `SYSTEM_ANNOUNCEMENT` 模板即可。
> 无需引入新表，避免公告记录与通知记录两套体系并存。

**3.4.1 系统公告管理 ✅ (2026-03-16完成)**
```
功能实现:
- [x] 创建公告（填写标题+内容+priority，后端批量写入 tb_notification）
- [x] 公告列表（查询 type=announcement 的通知记录，支持 priority 过滤）
- [x] 公告详情（管理端 + 用户端双入口）
- [x] 删除公告（软删除，@TableLogic deleted字段）
- [x] 发送公告（单独 send 接口，NotificationMapper.insertBatch 批量写入）

新增接口 (5个) ✅:
POST   /api/admin/announcement            - 创建公告
GET    /api/admin/announcement/list       - 公告列表（支持priority过滤: HAVING MIN(n.priority)=#{dto.priority}）
GET    /api/admin/announcement/{id}       - 公告详情（管理端）
DELETE /api/admin/announcement/{id}       - 删除公告
POST   /api/admin/announcement/send/{id}  - 发送公告（批量写 tb_notification）

用户端新增接口 (+1，归属 NotificationController):
GET    /notification/announcement/{id}    - 用户查看公告详情

前端页面 ✅:
views/admin/AnnouncementManage.vue   ✅ (公告列表+创建/编辑对话框+发送确认)
views/notice/NoticeDetail.vue        ✅ (用户公告详情页，路由: /notice/detail)
AdminLayout.vue 改造                 ✅ (新增 Bell 图标 + 系统公告菜单)
router/index.js 改造                 ✅ (新增 /notice/detail 路由)

数据库变更 ✅:
ALTER TABLE tb_notification ADD COLUMN deleted tinyint(1) DEFAULT 0  (支持@TableLogic软删除)
```

**3.4.2 通知记录管理 ✅ (2026-03-16完成)**
```
功能规划:
- [x] 查看所有通知记录（全用户，后台视角）
- [x] 按用户/模块/时间筛选
- [x] 统计分析（按模块分布/按日期统计）
- [x] 批量删除

新增接口 (3个):
GET    /api/admin/notification/record/list   - 通知记录列表（分页+筛选）
GET    /api/admin/notification/record/stats  - 统计分析
DELETE /api/admin/notification/record/batch  - 批量删除

前端页面:
views/admin/NotificationRecord.vue
```

**3.4.3 通知模板管理 ✅ (2026-03-16完成)**
```
功能规划:
- [x] 查看所有模板（按模块分组展示）
- [x] 编辑模板标题/内容/跳转链接
- [x] 启用/禁用模板
- [x] 模板预览（变量占位符替换示例）
- [x] 恢复默认内容

新增接口 (5个):
GET  /api/admin/notification/template/list      - 模板列表（按模块分组）
GET  /api/admin/notification/template/:id       - 模板详情
PUT  /api/admin/notification/template/:id       - 编辑模板
POST /api/admin/notification/template/status    - 启用/禁用
POST /api/admin/notification/template/reset/:id - 恢复默认

前端页面:
views/admin/NotificationTemplate.vue
```

**后台管理菜单扩展:**
```
后台管理
├── 数据看板 ✅
├── 商品管理 ✅
├── 订单管理 ✅
├── 消息管理 ✅
│   ├── 系统公告 ✅ (3.4.1完成)
│   ├── 通知记录 ✅ (3.4.2完成)
│   └── 通知模板 ✅ (3.4.3完成)
├── 用户管理 ✅
├── 评价管理 ✅
└── 系统设置 ✅
```

**接口汇总: 13个 (全部完成)**
```
系统公告管理:   5个 ✅ (含send接口)
通知记录管理:   3个 ✅
通知模板管理:   5个 ✅
```

---

#### 3.5 业务通知集成计划 (已全部完成 ✅)

**⚠️ 重要说明:**
- 各模块的通知集成功能已分别列在各自模块的"待开发功能"中
- 开发各模块时,按照模块内的通知集成计划进行开发
- 所有通知代码统一在 `com.astronomy.mall.module.notification` 包下
- 业务模块只需注入 `NotificationHelper` 并调用对应方法

**通知集成分布:**
```
✅ 商城模块(12种) → 全部完成(退款3种✅ 安装2种✅ 回收1种✅ 商品收藏2种✅ 订单4种✅)
✅ AI识别模块(2种) → 已完成(识别成功/失败通知，NotificationHelper已集成，模板已INSERT) 🆕
⬜ 课程模块(3种) → 见"5. 天文课程模块"（新章节通知/APOD更新通知/学习完成通知）
⬜ 地理位置模块(2种) → 见"6. 地理位置推荐模块"（签到成功 / 天气适宜预留）
⬜ 论坛模块(9种) → 见"7. 论坛社区模块"
⬜ 推荐系统(3种) → 见"8. 推荐系统模块"

总计: 35种通知类型（含课程3种）
```

---

#### 3.6 开发检查清单

**核心功能(已完成):**
- [x] 3个数据库表创建 ✅
- [x] 20个后端Java文件 ✅
- [x] 7个API接口 ✅
- [x] 3个前端文件 ✅
- [x] 16个通知模板初始化 ✅ (含安装预约确认/取消、二手回收、商品收藏相关)

**后台消息管理(全部完成 ✅):**
- [x] 无需新增数据库表（复用 tb_notification）✅
- [x] ALTER TABLE tb_notification ADD COLUMN deleted（@TableLogic）✅
- [x] 公告管理：AdminAnnouncementController/Service/Mapper（5个接口）✅
- [x] 通知记录管理：AdminNotificationController/Service/Mapper（3个接口）✅
- [x] 通知模板管理：AdminNotificationTemplateController/Service（5个接口）✅
- [x] 前端页面：AnnouncementManage.vue / NoticeDetail.vue / NotificationRecord.vue / NotificationTemplate.vue ✅
- [x] AdminLayout.vue + router/index.js 改造完成 ✅

**商城订单通知集成(已完成):**
- [x] 订单支付成功通知 (PaymentServiceImpl) ✅
- [x] 订单发货通知 (AdminOrderServiceImpl) ✅
- [x] 订单派送通知 (AdminOrderServiceImpl) ✅
- [x] 订单完成通知 (OrderServiceImpl) ✅
- [x] 订单取消通知 (OrderServiceImpl + AdminOrderServiceImpl) ✅

**其他模块通知集成(后期):**
- [x] AI识别: 2个通知集成点 ✅ (sendRecognitionCompletedNotification + sendRecognitionFailedNotification)
- [x] 课程模块: 3个通知集成点（COURSE_CHAPTER_ADDED / COURSE_APOD_UPDATED / COURSE_COMPLETED）✅ 2026-03-22
- [ ] 地理位置: 2个通知集成点（LOCATION_CHECKIN_SUCCESS签到通知 / LOCATION_WEATHER_SUITABLE预留）
- [ ] 论坛模块: 9个通知集成点 (第14-15周)
- [ ] 推荐系统: 3个通知集成点 (第16周)

---

### ✅ 4. AI星图识别模块
**开发时间:** 第9-10周  
**状态:** ✅ 已完成 (4.1✅ 4.2✅ 4.3✅ 4.4✅)  
**外部依赖:** Astrometry.net API (已确认可访问 ✅)  
**预计工时:** 5天

#### Astrometry.net API 配置

**API Key:** `svucokqxqvdbdfla`

**application.yml 配置:**
```yaml
astrometry:
  api-key: svucokqxqvdbdfla
  base-url: https://nova.astrometry.net/api
```

**后端注入方式:**
```java
@Value("${astrometry.api-key}")
private String apiKey;

@Value("${astrometry.base-url}")
private String baseUrl;
```

**⚠️ 重要注意事项:**
1. **下载文件必须带 Referer 请求头**，否则会被拦截（防爬虫机制）：
   ```
   Referer: https://nova.astrometry.net/api/login
   ```
   凡是调用 Astrometry.net 下载接口（标注图片等）时必须加此 Header，`AstrometryService` 开发时统一处理，业务层无需关心。

2. **识别耗时较长（30秒~3分钟）**，必须用异步方式处理，不能同步等待。

3. **API Key 与账号绑定**，所有通过 API 上传的图片会出现在账号 Profile 页面，这是正常现象。

**数据库表:**
- `tb_recognition` (识别记录表) ✅ 已创建

```sql
CREATE TABLE `tb_recognition` (
  `id`                   bigint(20)     NOT NULL AUTO_INCREMENT,
  `user_id`              bigint(20)     NOT NULL COMMENT '用户ID',
  `image_data`           mediumtext     COMMENT '原始图片(base64，前端压缩后上传)',
  `submission_id`        varchar(50)    DEFAULT NULL COMMENT 'Astrometry submission_id',
  `job_id`               varchar(50)    DEFAULT NULL COMMENT 'Astrometry job_id',
  `status`               tinyint(4)     DEFAULT 0 COMMENT '0-识别中 1-成功 2-失败',
  `objects_in_field`     text           COMMENT '识别到的天体(JSON数组，如["Orion Nebula"])',
  `machine_tags`         text           COMMENT '机器标签(JSON数组，如["nebula","galaxy"])',
  `ra`                   decimal(10,6)  DEFAULT NULL COMMENT '赤经',
  `dec`                  decimal(10,6)  DEFAULT NULL COMMENT '赤纬',
  `orientation`          decimal(8,4)   DEFAULT NULL COMMENT '方向角(度)',
  `radius`               decimal(8,4)   DEFAULT NULL COMMENT '视野半径(度)',
  `result_image_url`     varchar(500)   DEFAULT NULL COMMENT 'Astrometry返回的标注图片URL',
  `recommended_products` text           COMMENT '推荐商品ID列表(JSON数组)',
  `fail_reason`          varchar(200)   DEFAULT NULL COMMENT '失败原因',
  `create_time`          datetime       DEFAULT CURRENT_TIMESTAMP,
  `update_time`          datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI星图识别记录表';
```

---

#### 4.1 图片上传与识别提交 ✅
**完成目标:** 用户上传星图图片，后端提交给 Astrometry.net 并返回识别ID，前端进入等待状态。
**完成日期:** 2026-03-16

**用户端功能:**
- [x] 图片上传区（拖拽/点击选择，支持 jpg/png/fits）✅
- [x] 前端 Canvas 压缩（最长边1200px，JPEG质量0.85）✅
- [x] base64 编码后 POST 给后端 ✅
- [x] 提交成功后返回 recognitionId，前端跳转识别等待页 ✅

**新增接口 (2个):** ✅
```
POST /api/recognition/submit         ✅
  入参: imageData (base64字符串)
  处理: 保存图片到 tb_recognition(status=0) → @Async 调用 AstrometryService 提交
  返回: { recognitionId, status: 0 }

GET  /api/recognition/status/{id}    ✅ (等待页轮询)
  返回: { id, status, submissionId, jobId, ... }
```

**Astrometry.net 调用流程:**
```
POST https://nova.astrometry.net/api/login   → session_key (缓存，有效期内复用)
    ↓
POST https://nova.astrometry.net/api/upload  → submission_id (存入 tb_recognition)
    ↓
后台 @Async 轮询任务启动（见 4.2）
```

**后端文件:**
```
module/recognition/
├── entity/Recognition.java                                    ✅ ⚠️ dec字段@TableField("`dec`")
├── mapper/RecognitionMapper.java                              ✅
├── mapper/xml/RecognitionMapper.xml                           ✅
├── service/external/AstrometryService.java                    ✅  封装第三方API
├── service/external/impl/AstrometryServiceImpl.java           ✅  Session缓存+form-urlencoded登录+multipart上传
├── service/external/dto/AstrometryJobResult.java              ✅  calibration+jobinfo内部DTO
├── service/RecognitionService.java                            ✅
├── service/impl/RecognitionServiceImpl.java                   ✅
├── controller/RecognitionController.java                      ✅
├── config/RecognitionConfig.java                              ✅  recognitionExecutor+RestTemplate(120s超时)
├── dto/SubmitRecognitionDTO.java                              ✅  { imageData }
└── vo/RecognitionVO.java                                      ✅  识别结果VO
```

**⚠️ 数据库变更（需手动执行）:**
```sql
CREATE TABLE tb_recognition (...);  -- 见上方建表语句
```

**检查清单:**
- [x] 建表 tb_recognition ✅
- [x] 开发 AstrometryService（login + upload）✅
- [x] 开发 submit 接口，@Async 异步提交 ✅
- [x] 前端图片压缩（Canvas，最长边1200px）✅
- [x] 前端提交后跳转等待页 ✅

**⚠️ 重要Bug记录:**
```
dec 是 MySQL 保留关键字，MyBatis-Plus 自动生成 selectById 时不加反引号会报 SQL 语法错误。
修复: Recognition.java 的 dec 字段必须加 @TableField("`dec`") 注解。
```

**⚠️ application.yml 变更:**
```yaml
# mapper-locations 必须改为 **/*.xml 才能扫描到 mapper/xml/ 子目录
mapper-locations: classpath:mapper/**/*.xml   # 原来是 mapper/*.xml
astrometry:
  api-key: svucokqxqvdbdfla
  base-url: https://nova.astrometry.net/api
```

---

#### 4.2 识别状态轮询 ✅
**完成目标:** 后台定时轮询 Astrometry.net，识别完成后更新数据库并发送通知；前端每5秒查一次状态，完成后自动跳转结果页。
**完成日期:** 2026-03-16

**用户端功能:**
- [x] 等待页展示识别进度动画（星空旋转效果）✅
- [x] 3步进度提示（已提交 → AI分析中 → 生成结果）✅
- [x] 前端每5秒轮询一次状态接口 ✅
- [x] status=1（成功）自动跳转结果页，status=2（失败）显示失败提示 ✅

**新增接口 (2个):** ✅
```
GET /api/recognition/{id}           ✅  识别详情（结果页使用）
GET /api/recognition/history        ✅  用户历史记录（分页）
```

**后台轮询任务 (RecognitionPollScheduler):** ✅
```java
// @Scheduled(fixedDelay=30000, initialDelay=60000) 每30秒执行一次
// 状态机流程:
// 1. GET /api/submissions/{subId} → 获取 job_id（写入数据库）
// 2. GET /api/jobs/{jobId}        → status: solving/success/failure
// 3. GET /api/jobs/{jobId}/calibration → ra/dec/orientation/radius
// 4. GET /api/jobs/{jobId}/info        → objectsInField/machineTags
// 5. 更新 tb_recognition（status=1）
// 6. 发送成功/失败通知（notificationHelper）
// ⚠️ 超过10分钟自动标记失败
```

**后端文件:** ✅
```
├── task/RecognitionPollScheduler.java   ✅  @Scheduled 每30秒定时轮询
```

**🔔 通知集成 (2种):** ✅ 已完成
```java
// 识别成功（天体名最多展示3个，用顿号拼接）
notificationHelper.sendRecognitionCompletedNotification(
    userId, objectNames, recognitionId
);
// 识别失败（含超时/job failure/系统异常）
notificationHelper.sendRecognitionFailedNotification(
    userId, failReason, recognitionId
);
```

**通知模板（开发完成后执行 INSERT）:**
```sql
INSERT INTO `tb_notification_template`
(`code`, `module`, `type`, `title_template`, `content_template`, `jump_url_template`, `variables`, `enabled`, `remark`)
VALUES
('AI_RECOGNITION_COMPLETED', 'ai', 'recognition_completed',
 '星图识别完成',
 '您的星图已识别成功！发现天体：{objectNames}，快来查看吧',
 '/recognition/result?id={recognitionId}',
 '{"objectNames":"识别到的天体名称","recognitionId":"识别记录ID"}',
 1, '星图识别成功通知'),
('AI_RECOGNITION_FAILED', 'ai', 'recognition_failed',
 '星图识别失败',
 '您的星图识别未能成功，原因：{failReason}。请尝试上传更清晰的图片',
 '/recognition/history',
 '{"failReason":"失败原因"}',
 1, '星图识别失败通知');
```

**检查清单:**
- [x] 开发 RecognitionPollScheduler（每30秒轮询 submission → job → calibration → info）✅
- [x] 开发 detail / history 接口 ✅
- [x] NotificationHelper 新增 sendRecognitionCompletedNotification / sendRecognitionFailedNotification ✅
- [x] 执行通知模板 INSERT SQL（AI_RECOGNITION_COMPLETED / AI_RECOGNITION_FAILED）✅
- [x] 前端轮询逻辑（5秒间隔，完成后自动跳转）✅
- [x] 等待页进度动画 ✅
- [x] 结果页（RecognitionResult.vue）✅
- [x] 历史列表页（RecognitionHistory.vue）✅
- [x] router/index.js 新增3条识别路由 ✅
- [x] Home.vue goToAI() 改为跳转 /recognition ✅

---

#### 4.3 识别结果展示 ✅
**完成目标:** 识别成功后展示完整结果，包括 Astrometry 返回的标注图片、识别到的天体列表和坐标信息。
**完成日期:** 2026-03-16

**用户端功能:**
- [x] 标注图片展示（Astrometry 返回的带标注图片 URL）✅
- [x] 识别到的天体列表（中英文双语 Tag，按 type 分4种颜色：nebula=蓝/galaxy=紫/cluster=绿/constellation=黄）✅
- [x] 坐标信息展示（赤经 RA / 赤纬 Dec / 方向角 / 视野半径，后端格式化字段优先）✅
- [x] 结果分享按钮（复制结果文字）✅
- [x] 识别失败页新增拍摄建议提示 ✅

**新增接口 (1个):** ✅
```
GET /api/recognition/result/{id}
  返回: RecognitionVO {
    id, status,
    celestialObjects,       // 中英文天体对照列表 List<CelestialObjectVO>
    machineTags,            // 机器标签
    ra, dec, orientation, radius,
    raFormatted,            // "05h 35m 17.3s"
    decFormatted,           // "-05° 23' 28.0\""
    orientationFormatted,   // "178.50°"
    radiusFormatted,        // "1.230°"
    resultImageUrl          // 标注图片URL
  }
```

**天体名称中英文映射（后端维护静态Map，55个天体）:**
```java
"Orion Nebula"     → "猎户座大星云"
"Andromeda Galaxy" → "仙女座星系"
"Pleiades"         → "昴星团"
"Crab Nebula"      → "蟹状星云"
// ... 共55个常见天体
// CelestialObjectVO.type: nebula/galaxy/cluster/constellation/unknown
```

**RecognitionVO 新增字段:**
```java
List<CelestialObjectVO> celestialObjects  // 中英文天体对照
String raFormatted       // "05h 35m 17.3s"
String decFormatted      // "-05° 23' 28.0\""
String orientationFormatted // "178.50°"
String radiusFormatted   // "1.230°"

// 内部类
class CelestialObjectVO {
    String en;   // 英文名
    String zh;   // 中文名
    String type; // nebula/galaxy/cluster/constellation/unknown
}
```

**后端文件:** ✅
```
├── service/RecognitionService.java              ✅  新增 getResult()
├── service/impl/RecognitionServiceImpl.java     ✅  新增 CELESTIAL_NAME_MAP/CELESTIAL_TYPE_MAP/
│                                                    getResult()/formatRA()/formatDec()/
│                                                    formatRadius()/buildCelestialObjects()
├── controller/RecognitionController.java        ✅  新增 GET /result/{id}
└── vo/RecognitionVO.java                        ✅  新增格式化字段+CelestialObjectVO内部类
```

**检查清单:**
- [x] 开发 result 接口 ✅
- [x] 后端天体名称中英文映射（55个，CELESTIAL_NAME_MAP + CELESTIAL_TYPE_MAP）✅
- [x] RecognitionResult.vue 天体列表升级为中英双语Tag（4种颜色按type区分）✅
- [x] 坐标格式化字段（后端优先，前端计算降级）✅
- [x] 新增分享卡片（复制结果文字）✅
- [x] 识别失败页拍摄建议提示 ✅

---

#### 4.4 器材推荐 ✅
**完成目标:** 根据识别结果的 machine_tags 匹配商品标签，推荐最相关的天文器材，展示在结果页底部。
**完成日期:** 2026-03-16

**用户端功能:**
- [x] 结果页底部展示推荐器材（横向滑动卡片，最多6个）✅
- [x] 每张卡片：商品图 + 名称 + 价格 + 推荐理由 + 「去看看」按钮 ✅
- [x] 点击「去看看」跳转商品详情页 /product/{id} ✅
- [x] 骨架屏加载动画，推荐接口异步加载不阻塞主内容 ✅

**新增接口 (1个):** ✅
```
GET /api/recognition/recommend/{id}
  处理: 读取 tb_recognition.machine_tags → TAG_MAPPING → JdbcTemplate查tb_product
        无匹配时按 tb_product.sales 倒序取热销前6个兜底
        推荐结果ID写回 tb_recognition.recommended_products
  返回: List<RecognitionProductVO>（最多6个）
```

**推荐逻辑（TAG_MAPPING 完整映射）:**
```java
"nebula"          → ["深空摄影", "天文相机", "窄带滤镜", "CCD"]
"galaxy"          → ["深空摄影", "大口径望远镜", "天文相机"]
"planet"          → ["行星观测", "高倍目镜", "巴洛镜", "行星相机"]
"star cluster"    → ["双筒望远镜", "广角目镜", "寻星镜"]
"open cluster"    → ["双筒望远镜", "广角目镜"]
"globular cluster"→ ["大口径望远镜", "高倍目镜"]
"moon"            → ["月面摄影", "滤镜", "月球滤镜"]
"comet"           → ["广角望远镜", "赤道仪", "追踪"]
"star"            → ["入门望远镜", "寻星镜"]
"emission"        → ["窄带滤镜", "Ha滤镜", "深空摄影"]
"reflection"      → ["天文相机", "深空摄影"]
// 未匹配到 → 按 tb_product.sales 倒序返回热销前6个
```

**RecognitionProductVO:**
```java
Long id
String productName
String mainImage
BigDecimal price
String reason       // TAG_REASON_MAP 推荐理由文案
Integer salesCount  // 对应 tb_product.sales 字段
```

**后端文件:** ✅
```
├── service/RecognitionRecommendService.java              ✅
├── service/impl/RecognitionRecommendServiceImpl.java     ✅  TAG_MAPPING/TAG_REASON_MAP/JdbcTemplate
├── controller/RecognitionController.java                 ✅  新增 GET /recommend/{id}
│                                                             新增注入 RecognitionRecommendService
└── vo/RecognitionProductVO.java                          ✅  新增
```

**⚠️ 注意:** 查询 tb_product 时排序字段为 `sales`（非 `sales_count`），已确认字段存在。

**检查清单:**
- [x] 开发 RecognitionRecommendService（标签匹配逻辑）✅
- [x] 开发 recommend 接口 ✅
- [x] 推荐结果 ID 写回 tb_recognition.recommended_products（JSON数组）✅
- [x] 前端推荐器材横向滑动卡片（骨架屏+商品图+名称+价格+reason+去看看）✅
- [x] 无推荐结果时兜底展示热销商品 ✅

---

#### 4.5 识别历史记录 ✅
**完成目标:** 用户可以查看自己所有的历史识别记录，支持状态筛选，可删除记录。  
**完成日期:** 2026-03-16

**用户端功能:**
- [x] 历史记录列表（分页，每页10条，倒序）✅
- [x] 状态筛选（全部/识别中/成功/失败）✅
- [x] 每条记录展示：星空占位图 + 识别状态Tag + 主要天体名(中文,最多2个) + 识别时间 ✅
- [x] 点击查看完整结果（成功）或查看失败原因（失败）✅
- [x] 删除单条记录（确认弹窗）✅
- [x] 识别统计卡片4个（总次数/成功/成功率/识别中）✅
- [x] 面包屑导航 + 右侧「识别新星图」快捷按钮 ✅

**新增接口 (3个):** ✅
```
GET    /api/recognition/history    - 历史列表（分页，支持status筛选，null=全部）
DELETE /api/recognition/{id}       - 删除单条记录（只能删自己的，checkOwnership校验）
GET    /api/recognition/stats      - 统计（总次数/成功/失败/识别中/成功率保留1位小数）
```

**新建文件 (1个):**
```
RecognitionStatsVO.java → module/recognition/vo/
  字段: total / successCount / failCount / pendingCount / successRate(Double,保留1位小数)
```

**改造文件 (8个):**
```
RecognitionVO.java              末尾新增2个字段（createTime之后，内部类之前）:
                                  private Boolean hasImage;      // 历史列表专用
                                  private List<String> mainObjects; // 天体中文名，历史列表专用

RecognitionService.java         3处:
  1. getHistory 第4参数改为 Integer status（null=全部）
  2. 新增 void deleteRecord(Long recognitionId, Long userId)
  3. 新增 RecognitionStatsVO getStats(Long userId)

RecognitionServiceImpl.java     3处（原有代码不动）:
  1. getHistory 改用 LambdaQueryWrapper分页，.eq(status!=null, Recognition::getStatus, status)
     返回VO时附加 hasImage / mainObjects
  2. 新增 deleteRecord（调 checkOwnership 再 deleteById）
  3. 新增 getStats（只 select status 字段避免加载 image_data）

RecognitionController.java      3处:
  1. getHistory 新增 @RequestParam(required=false) Integer status
  2. 新增 DELETE /{id} → deleteRecord
  3. 新增 GET /stats → getStats（精确路径，无路径冲突）

recognition.js                  3处:
  1. getRecognitionHistory 新增第3个参数 status=null，有值才传
  2. 新增 deleteRecognition(id) → DELETE /recognition/{id}
  3. 新增 getRecognitionStats() → GET /recognition/stats

RecognitionHistory.vue          完整重写（原版是4.2空壳）:
  - 统计卡片4个（总次数/成功/成功率/识别中）
  - 状态筛选Tab（全部/识别中/识别成功/识别失败）
  - 记录列表（分页，后端返回 { list, total, pageNum, pageSize }，用 res.data.list）
  - 每条记录：星空占位图+状态Tag+天体名(中文,最多2个)+时间+操作按钮
  - 删除确认弹窗
  - 面包屑：← 返回首页 / 🌠 AI识别 / 识别历史，右侧「识别新星图」按钮

router/index.js                 新增路由:
  { path: '/recognition/history', name: 'RecognitionHistory',
    component: () => import('@/views/recognition/RecognitionHistory.vue'),
    meta: { title: '识别历史', requiresAuth: true } }

UserLayout.vue                  2处:
  1. import 加 Search
  2. 「我的服务」分组末尾加 { path: '/user/recognition', label: '识别历史', icon: Search, badge: 0 }

StarRecognition.vue             2处:
  1. .back-btn 后加历史入口按钮（绝对定位右上角）
  2. style 加 .history-btn 样式
```

**检查清单:**
- [x] 新建 RecognitionStatsVO.java ✅
- [x] RecognitionVO.java 新增 hasImage / mainObjects 字段 ✅
- [x] RecognitionService/Impl 新增 deleteRecord / getStats，改造 getHistory ✅
- [x] RecognitionController 新增 DELETE/{id} / GET/stats，改造 getHistory ✅
- [x] recognition.js 新增 deleteRecognition / getRecognitionStats，改造 getRecognitionHistory ✅
- [x] RecognitionHistory.vue 完整重写 ✅
- [x] router/index.js 新增 /recognition/history 路由 ✅
- [x] UserLayout.vue 新增识别历史侧边栏入口 ✅
- [x] StarRecognition.vue 新增「识别历史」快捷按钮 ✅

---

**总检查清单（AI模块）:**
- [x] 建表 tb_recognition ✅
- [x] 开发 AstrometryService（login/upload/getSubmissionJobId/getJobStatus/getJobCalibration/getJobInfo/buildAnnotatedImageUrl）✅
- [x] 开发 RecognitionPollScheduler（@Scheduled 每30秒定时轮询）✅
- [x] 开发 RecognitionController（9个接口：submit/status/detail/history/result/recommend/delete/stats）✅
- [x] 开发 RecognitionRecommendService（标签匹配推荐）✅ 4.4节
- [x] 新建 RecognitionStatsVO.java ✅ 4.5节
- [x] NotificationHelper 新增2个通知方法 ✅
- [x] 执行通知模板 INSERT SQL（AI_RECOGNITION_COMPLETED / AI_RECOGNITION_FAILED）✅
- [x] 开发 StarRecognition.vue（上传页）✅
- [x] 开发 RecognitionWaiting.vue（等待页，5秒轮询）✅
- [x] 开发 RecognitionResult.vue（结果页，中英双语天体Tag+推荐卡片+分享）✅
- [x] 开发 RecognitionHistory.vue（历史列表，完整重写：统计卡片+状态筛选+删除）✅
- [x] router/index.js 新增 /recognition / /recognition/waiting / /recognition/result / /recognition/history 路由 ✅
- [x] UserLayout.vue 新增「识别历史」侧边栏入口 ✅
- [x] StarRecognition.vue 新增「识别历史」快捷按钮 ✅
- [x] application.yml 配置 astrometry.api-key + mapper-locations 改为 **/*.xml ✅

**实际工时:** 6天

---

### ✅ 5. 天文课程模块
**开发时间:** 第11-12周（⚠️ 必须先完成 2.7.1 + 2.7.2 再开始本模块）  
**状态:** ✅ 5.1-5.6 全部完成（2026-03-22）  
**前置依赖:** `module/nasa/` 已完成（APODSyncScheduler 和 MarsRoverSyncScheduler 注入 NasaApiService）  
**预计工时:** 6天

**设计原则:**
- 全部免费，无购买/证书/作业流程
- 两种类型：视频课（NASA YouTube / B站 iframe嵌入）/ 书本课（TinyMCE富文本）
- 视频课内容：嵌入 NASA 官方 YouTube 频道视频（NASA、NASA Goddard、Hubble Telescope、Webb Telescope 等），零版权风险，可在答辩中作为「NASA资源对接」亮点
- 书本课内容来源：管理员手动录入 + NASA APOD API 定时自动同步
- 课程标签（tags）：预设标签池 + 支持自定义，前端多选 AND 筛选
- 进度自动记录，下次进入同一课程直接跳上次章节
- 商品购买联动：用户购买商品后根据标签推荐相关课程
- 论坛联动预留：课程详情底部「去论坛讨论」按钮（论坛模块开发后生效）
- 评价：`tb_course_review` 表已激活（5.6完成），用户可提交/编辑评价，管理员可审核删除

**预设标签池（18个，管理员录课时勾选，也可自定义追加）:**
```
深空摄影 / 行星观测 / 月球 / 太阳系 / 星座入门 / 天体物理
望远镜使用 / 赤道仪 / 目镜选择 / 滤镜应用
NASA科普 / 哈勃望远镜 / 韦伯望远镜 / 星云 / 星系
天文摄影后期 / 天气与选址 / 星图使用
```

**数据库表 (5张):**
- `tb_course` (课程主表) ✅
- `tb_course_chapter` (章节表) ✅ 📌 video_url存完整嵌入URL，管理员后台按平台模板填写ID后自动拼接写入
- `tb_course_progress` (学习进度表，替代原 tb_user_course) ✅
- `tb_course_favorite` (课程收藏表) ✅
- `tb_course_review` (评价表，✅ 5.6已激活，用户提交/编辑评价，管理员审核删除)

---

#### 5.1 课程列表与详情 ✅ 2026-03-20完成
**完成目标:** 用户可浏览课程列表，通过类型/难度/标签筛选找到感兴趣的课程；进入详情后按章节学习，视频章节 iframe 播放，书本章节渲染富文本，进度自动记录。

> ✅ 2026-03-20 完成

**用户端功能:**
- [x] 课程列表页 ✅
  - 视频课 / 书本课 Tab 切换
  - 难度筛选（全部 / 入门 / 进阶 / 高级）
  - 标签多选筛选（chip 点击切换选中态，AND 关系，多标签同时选中）
  - 关键词搜索（匹配课程标题）
  - 课程卡片：封面 + 类型标签 + 标题 + 难度 + 章节数 + 标签chips + 收藏❤️
- [x] 课程详情页 ✅
  - 左侧章节目录（序号+标题，已完成章节显示 ✓）
  - 右侧内容区：视频章节 = iframe 嵌入，书本章节 = v-html 渲染富文本
  - 顶部：课程标题 + 难度 + 标签 + 收藏按钮
  - 底部评价区（5.1占位→5.6激活：提交/编辑/列表/我的评价横条）
  - 底部「💬 去论坛讨论」按钮 → 跳转 `/forum/list?courseId={courseId}`
    ⚠️ 注：ForumList.vue 检测到 courseId 参数时自动筛选包含该课程标签的帖子（见7.3.7节）
- [x] 点击章节后端自动记录进度，下次进入课程直接定位到上次章节 ✅

**新增接口 (3个):**
```
GET /api/course/list
  参数: pageNum / pageSize / type(0视频1书本) / keyword
        difficulty / tags(逗号分隔，多标签AND匹配)
  后端逻辑: tags不为空时对每个tag都做 JSON_CONTAINS 或 LIKE 匹配，取交集
  返回: 课程分页列表（登录用户附带 isFavorite 字段）

GET /api/course/{id}
  返回: 课程详情 + 章节列表（不含正文，只有 title/sort/type/duration）
        + lastChapterId（当前用户进度，未登录为 null）

GET /api/course/chapter/{chapterId}
  返回: 章节完整内容（videoUrl 或 content富文本）
  副作用: 登录用户自动 UPSERT tb_course_progress
          更新 last_chapter_id / last_learn_time
          将该章节加入 completed_chapters JSON数组（若不存在）
  🔔 完课检测: 若 completed_chapters.size() >= course.chapter_count
               **且 course.is_apod_course = 0 且 course.is_mars_course = 0**
               （APOD课和火星课每天自动增章节，永远追不上，排除在完课逻辑之外）
               → 调用 notificationHelper.sendCourseCompletedNotification()（见6.5触发点3）
```

**后端文件 (module/course/ 新模块):**
```
module/course/
├── controller/CourseController.java
├── dto/CourseQueryDTO.java            (pageNum/pageSize/type/keyword/difficulty/tags)
├── entity
│   ├── Course.java
│   ├── CourseChapter.java
│   ├── CourseProgress.java
│   └── CourseFavorite.java
├── mapper
│   ├── CourseMapper.java
│   ├── CourseChapterMapper.java
│   ├── CourseProgressMapper.java
│   └── CourseFavoriteMapper.java
├── service/CourseService.java
├── service/impl/CourseServiceImpl.java
└── vo
    ├── CourseVO.java           (含 isFavorite / lastChapterId / tags字段)
    └── CourseChapterVO.java
```

**前端文件:**
```
views/course/CourseList.vue
  - 视频/书本 Tab + 难度筛选 + 标签 chips 多选（选中高亮，再点取消）+ 关键词搜索
  - 筛选条件变化时重新调用 getCourseList，tags 参数传选中标签数组
  - 课程卡片：封面+类型标签+标题+难度+章节数+标签chips+收藏❤️

views/course/CourseDetail.vue
  - 左侧章节目录 + 右侧 iframe/富文本 + 评价占位 + 论坛入口

api/course.js  新增 getCourseList / getCourseDetail / getCourseChapter

router/index.js 新增:
  { path: '/course',     name: 'CourseList',   component: CourseList.vue,   requiresAuth: false }
  { path: '/course/:id', name: 'CourseDetail', component: CourseDetail.vue, requiresAuth: true  }
```

**⚠️ 数据库变更（需手动执行）:**
```sql
CREATE TABLE tb_course (...);
CREATE TABLE tb_course_chapter (...);
CREATE TABLE tb_course_progress (...);
CREATE TABLE tb_course_favorite (...);
CREATE TABLE tb_course_review (...);
-- 详见数据库表文档
```

**检查清单:**
- [x] 建5张课程相关表 ✅
- [x] 开发 module/course/ 后端模块（entity/mapper/service/vo）✅
- [x] 开发 list / detail / chapter 3个接口（含标签多选AND筛选逻辑）✅
- [x] 开发 CourseList.vue（标签chips多选 + Tab + 难度 + 搜索 + 卡片）✅
- [x] 开发 CourseDetail.vue（章节目录 + iframe/富文本 + 进度 + 评价占位 + 论坛入口）✅
- [x] router/index.js 新增2条课程路由 ✅
- [x] JwtInterceptor 新增 OPTIONAL_AUTH_LIST 可选认证（课程接口游客可访问，登录后记录进度）✅

---

#### 5.2 APOD定时同步 ✅ 2026-03-20完成
**完成目标:** 系统每天凌晨2点将 NASA APOD 当日数据自动同步为书本课「NASA每日天文图片精选」的新章节，复用项目已有的 APOD 接口。

**功能说明:**
- [ ] 定时任务每天凌晨2点自动执行
- [ ] 固定同步到 `is_apod_course=1` 的专属课程（预置1门）
- [ ] 以 `apod_date` 去重，已存在的日期自动跳过
- [ ] 新章节：title=APOD英文标题，content=explanation富文本，apod_image=图片URL，source='apod'
- [ ] 同步成功后 `tb_course.chapter_count +1`


**APOD同步核心逻辑:**
```java
// APODSyncScheduler.java
@Scheduled(cron = "0 0 2 * * ?")
public void syncTodayApod() {
    // 1. 查 is_apod_course=1 的课程
    // 2. 调用 NasaApiService.getTodayApod()（统一 NASA 调用入口，含当日缓存）
    //    ⚠️ 不要在这里直接用 RestTemplate，统一走 NasaApiService
    //    NasaApiService 同时被商城首页 ApodCard 复用，保证每日只请求 NASA 一次
    // 3. 检查 apod_date 是否已存在（去重）
    // 4. INSERT tb_course_chapter（source='apod', apod_date, apod_image, content=explanation）
    // 5. UPDATE tb_course SET chapter_count = chapter_count + 1
}

// 管理员手动批量同步历史数据:
// POST /api/admin/course/apod/sync  Body: { startDate, endDate }
// → 遍历日期范围逐日拉取，已存在的跳过
```

**后端文件:**
```
module/course/task/APODSyncScheduler.java      @Scheduled(cron="0 0 2 * * ?")  ← 改为注入 NasaApiService
module/course/task/MarsRoverSyncScheduler.java @Scheduled(cron="0 30 2 * * ?") 🆕 火星车同步
```

**⚠️ 预置种子数据（建完表后执行）:**
```sql
-- 预置1门APOD专属书本课
INSERT INTO tb_course (title, subtitle, type, is_apod_course, status, difficulty, tags)
VALUES ('NASA每日天文图片精选',
        '每天自动同步 NASA APOD 数据，感受宇宙之美',
        1, 1, 1, 1,
        '["NASA科普","星云","星系","天体物理"]');

-- 预置视频课种子数据（嵌入NASA YouTube）示例:
INSERT INTO tb_course (title, subtitle, type, status, difficulty, tags) VALUES
('哈勃望远镜——人类的宇宙之眼', '精选哈勃望远镜官方视频，探索宇宙深处', 0, 1, 1, '["哈勃望远镜","NASA科普","深空摄影"]'),
('韦伯望远镜揭秘宇宙', '韦伯望远镜最新成果精选', 0, 1, 2, '["韦伯望远镜","NASA科普","天体物理"]'),
('入门天文摄影指南', '从零开始学习天文摄影', 0, 1, 1, '["天文摄影后期","望远镜使用","深空摄影"]');
-- 共预置10~15门课程，答辩展示用
```

**检查清单:**
 ✅ 开发 APODSyncScheduler（@Scheduled + 注入 NasaApiService.getTodayApod() + 去重插入）
 ✅ APODSyncScheduler 同步成功后调用 notificationHelper.sendCourseApodUpdatedNotification()
 ✅ 开发 MarsRoverSyncScheduler（凌晨2:30，注入 NasaApiService.getAllLatestMarsPhotos()）
 ✅ 预置「NASA每日天文图片精选」课程及若干视频课种子数据（共12门）
 ✅ 预置「火星探测车日志」课程种子数据（is_mars_course=1）
 ✅ NasaApiService 新增 getApodByDate() / getAllLatestMarsPhotos()
 ✅ CourseMapper 新增 incrChapterCount()
 ✅ CourseChapterMapper 新增 getMaxSort()
 ✅ 3条课程通知模板 INSERT SQL（COURSE_CHAPTER_ADDED/COURSE_APOD_UPDATED/COURSE_COMPLETED）
 ✅ CourseMapper.xml / CourseChapterMapper.xml 补全并放入 mapper/xml/ 目录

---

#### 5.3 课程收藏与学习历史 ✅ 2026-03-21完成
**完成目标:** 用户可收藏课程，个人中心查看收藏列表和学习历史，点击历史记录直接跳转到上次章节。

> ✅ 收藏toggle / 收藏列表 / 学习历史 / 进度查询 4个接口已在5.1一并完成，无需单独开发

**用户端功能:**
- [x] 课程列表/详情页收藏❤️按钮（已收藏变红，再点取消，幂等）✅
- [x] 个人中心「课程收藏」页（网格布局 + 取消收藏 + 空状态提示）✅
- [x] 个人中心「学习历史」页（列表：封面 + 标题 + 上次章节名 + 时间）✅
- [x] 点击学习历史 → 直接跳转上次章节，`/course/{id}` 并自动定位到 lastChapterId

**新增接口 (4个):**
```
POST /api/course/favorite/toggle/{id}
  已收藏则删除，未收藏则插入（幂等）
  返回: { isFavorite: true/false }

GET  /api/course/favorite/list
  参数: pageNum / pageSize
  返回: 收藏课程列表

GET  /api/course/history
  参数: pageNum / pageSize
  返回: 按 last_learn_time 倒序，含 lastChapterId / lastChapterTitle

GET  /api/course/progress/{courseId}
  返回: { lastChapterId, completedChapters: [], lastLearnTime }
  用途: CourseDetail.vue 进入时查询，自动定位上次章节
```

**前端文件:**
```
views/user/CourseFavorite.vue     挂 UserLayout 侧边栏「课程收藏」
views/user/CourseHistory.vue      挂 UserLayout 侧边栏「学习历史」

UserLayout.vue 改造（「我的服务」分组末尾新增2项）:
  { path: '/user/course-history',  label: '学习历史', icon: VideoPlay, badge: 0 }
  { path: '/user/course-favorite', label: '课程收藏', icon: Star,       badge: 0 }

router/index.js 新增:
  { path: '/user/course-history',  name: 'CourseHistory',  requiresAuth: true }
  { path: '/user/course-favorite', name: 'CourseFavorite', requiresAuth: true }
```

**检查清单:**
- [x] 开发收藏 toggle / 收藏列表 / 学习历史 / 进度查询 4个接口 ✅（5.1已完成）
- [x] 开发 CourseFavorite.vue（网格布局 + 取消收藏 + 未学习badge + 进度条）✅ 2026-03-21
- [x] 开发 CourseHistory.vue（列表 + 上次章节 + 进度条 + 未学习badge + 点击跳转）✅ 2026-03-21
- [x] 改造 UserLayout.vue（新增「学习历史」「课程收藏」侧边栏）✅ 2026-03-21
- [x] router/index.js 新增2条个人中心路由 ✅ 2026-03-21

---

#### 5.4 购买商品→推荐课程 ✅ 2026-03-21完成
**完成目标:** 课程列表页底部展示「为你推荐」横向卡片，根据用户近3个月购买商品的标签匹配课程标签，实现商品购买与课程学习的生态闭环。

**用户端功能:**
- [x] 课程列表页底部「为你推荐」横向滑动卡片（最多6个，常驻不因访问课程而减少）✅
- [x] 未登录时不显示推荐区块；无购买记录时后端热门兜底 ✅
- [x] 每张卡片：封面 + 标题 + 难度 + 标签 + 收藏按钮 ✅

**新增接口 (1个):** ✅
```
GET /api/course/recommend
  逻辑（7步兜底）:
  1. userId=null（未登录）→ 直接热门兜底
  2. 查用户近3个月已完成订单商品的 tags（排除 status=4 已取消订单）
  3. 无购买记录 → 热门兜底
  4. 解析商品 tags JSON → Set 合并去重 → userTags
  5. userTags 为空（解析失败）→ 热门兜底
  6. LIKE 匹配 tb_course.tags（OR关系，命中任意一个标签即入选）
  7. 无命中 → 热门兜底
  ⚠️ 不排除已学习课程（推荐列表固定，点进去再返回不消失）
  返回: List<CourseVO>（最多6个）
  未登录: 前端不调用接口，区块不渲染
```

**后端文件:** ✅
```
CourseMapper.java     新增3个方法：getUserRecentOrderProductTags / getRecommendByTags / getHotCourses
CourseMapper.xml      新增3段SQL（LIKE标签OR匹配+热门兜底，均不含NOT IN进度过滤）
CourseService.java    新增 getRecommendCourses(Long userId) 接口
CourseServiceImpl.java 新增完整实现（7步兜底逻辑）
CourseController.java  新增 GET /api/course/recommend 端点（可选认证）
```

**前端文件:** ✅
```
api/course.js         新增 getRecommendCourses() 方法
CourseList.vue        改造：
  - 推荐区块移至页面底部常驻（不影响主列表浏览）
  - sessionStorage 会话缓存（同一会话内不重新请求，列表稳定）
  - min-width: max-content 修复横向滑动
  - 自定义紫色细滚动条（4px高度，给用户明显滑动提示）
  - padding-right: 24px 保证最后一张卡片完整显示
  - 分页新增 jumper + total（前往第X页 + 共XX条）
```

**检查清单:** ✅
- [x] 开发 recommend 接口（商品 tags → 课程 tags 匹配 + 热门兜底）✅
- [x] CourseList.vue 新增「为你推荐」底部常驻区块 ✅
- [x] 修复推荐区块横向滑动（min-width: max-content）✅
- [x] 修复推荐列表消失问题（sessionStorage缓存 + SQL去掉NOT IN）✅
- [x] 分页新增 jumper + total ✅
- [x] 新增10门课程种子数据（course_seed_data.sql）✅

---

#### 5.5 后台课程管理 ✅ 2026-03-22完成
**完成目标:** 管理员在后台维护课程和章节数据，支持视频章节填 NASA YouTube URL、书本章节用 TinyMCE 编辑，支持手动批量导入历史 APOD 数据。

**管理员端功能:**
- [x] 课程列表（分页 / 关键词 / type / status 筛选）
- [x] 新增课程（标题 / 副标题 / 封面 / 类型 / 难度 / 标签）
  - 标签选择：预设标签池 chips 多选 + 支持输入自定义标签追加（最多10个）
- [x] 编辑课程基本信息
- [x] 发布 / 下架（status 0↔1）
- [x] 删除课程（逻辑删除，deleted=1）
- [x] 章节管理抽屉：
  - 查看 / 新增 / 编辑 / 删除 / 拖拽排序
  - 视频章节：平台模板选择（B站/YouTube/抖音）+ 只填ID/BV号，系统自动拼成嵌入URL写入数据库
    - B站: 输BV号 → `https://player.bilibili.com/player.html?bvid={BV号}&page=1&high_quality=1&danmaku=0`
    - YouTube: 输视频ID → `https://www.youtube.com/embed/{视频ID}`
    - 抖音: 输视频ID → `https://open.douyin.com/player/video?vid={视频ID}`
    - 也支持直接粘贴完整URL兜底
    - 预览按钮：iframe实时预览效果
    - 📌 数据库始终存完整嵌入URL，前端直接用，无需转换
  - 书本章节：TinyMCE 富文本编辑器（TinyMCE Cloud，含图片插入按钮）
- [x] APOD 同步面板：选日期范围 → 一键批量导入历史 APOD 数据

**新增接口 (11个，管理员端):**
```
GET    /api/admin/course/list             - 课程列表(分页+搜索+type+status)
POST   /api/admin/course/add             - 新增课程
PUT    /api/admin/course/update/{id}     - 编辑课程
DELETE /api/admin/course/delete/{id}     - 删除课程(deleted=1)
POST   /api/admin/course/status/{id}     - 发布/下架
GET    /api/admin/course/{id}/chapters   - 章节列表
POST   /api/admin/course/chapter/add     - 新增章节
PUT    /api/admin/course/chapter/{id}    - 编辑章节
DELETE /api/admin/course/chapter/{id}    - 删除章节
POST   /api/admin/course/chapter/sort    - 章节排序(批量更新sort)
POST   /api/admin/course/apod/sync       - 手动触发APOD同步(指定日期范围)
```

**后端文件 (admin模块新增):**
```
module/admin/
├── controller/AdminCourseController.java   (11个接口)
├── dto
│   ├── CourseCreateDTO.java                (含 tags 字段: List<String>)
│   ├── ChapterCreateDTO.java
│   └── ApodSyncDTO.java                   { startDate, endDate }
├── service/AdminCourseService.java
├── service/impl/AdminCourseServiceImpl.java
└── vo/AdminCourseVO.java
```

**前端文件:**
```
views/admin/CourseManage.vue
  左侧：课程列表 + 搜索筛选 + 新增/编辑/发布/删除
  右侧抽屉：章节管理（视频填URL预览 / 书本TinyMCE编辑 / 拖拽排序）
  顶部：APOD同步面板（选日期范围 → 一键导入）
  新增/编辑课程弹窗：标签区域显示预设标签chips + 自定义标签输入框

api/admin/course.js  (11个方法)
```

**🔔 通知集成 (3种):**
```
触发点1: AdminCourseServiceImpl.addChapter() 章节保存成功后
  → notificationHelper.sendCourseChapterAddedNotification(courseId, courseTitle, chapterTitle)
  → 批量通知所有收藏了该课程的用户（@Async异步，不阻塞保存）

触发点2: APODSyncScheduler 同步成功后（见5.2）
  → notificationHelper.sendCourseApodUpdatedNotification(apodCourseId, apodTitle)

触发点3: CourseServiceImpl.getChapter() 写完进度后检测完课（见5.1）
  → notificationHelper.sendCourseCompletedNotification(userId, courseId, courseTitle)
```

**检查清单:**
- [x] 开发 AdminCourseController（11个接口）✅
- [x] 开发 AdminCourseServiceImpl（CRUD + 章节管理 + APOD批量同步）✅
  - ⚠️ deleteCourse 用 LambdaUpdateWrapper.set(deleted=1)，绕过 @TableLogic 拦截
  - APOD同步事务：syncApodRange() NOT_SUPPORTED + insertOneApodDay() REQUIRES_NEW
- [x] addChapter() 集成 sendCourseChapterAddedNotification（新增章节后通知收藏用户）✅
- [x] 执行3条通知模板 INSERT SQL（COURSE_CHAPTER_ADDED / COURSE_APOD_UPDATED / COURSE_COMPLETED）✅
- [x] NotificationType.java 新增3个枚举值 ✅
- [x] NotificationHelper.java 新增3个方法 ✅
- [x] 开发 CourseManage.vue（课程列表 + 章节抽屉 + APOD同步面板 + 标签选择器）✅
  - TinyMCE Cloud（API key: l3rpivelnt20uy1aj6qef5ml07uh0gwpaism3duwxc237h19）
  - 自定义「插入图片」按钮（FileReader → base64 → chapterForm.content）
  - waitForTinyMCE() 轮询等待 CDN 脚本加载完成，解决抽屉偶发降级问题
- [x] 开发 api/admin/course.js（11个方法）✅
- [x] 执行 seed_courses_v8_26.sql（25门新课程，共52门）✅
- [x] WikipediaSyncScheduler.java（15门书本课，应用启动自动同步，无需网络）✅
- [x] application.yml 新增 server.tomcat.max-http-form-post-size: 20MB ✅
- [x] index.html 引入 TinyMCE Cloud CDN ✅

**已修复的 Bug（5.5 开发期间）:**
- AdminCourseServiceImpl.deleteCourse：updateById 被 @TableLogic 拦截 → 改 LambdaUpdateWrapper
- CourseManage.vue handleDelete/handleToggleStatus：ElMessageBox.confirm 取消抛异常 → 分离 try/catch
- CourseManage.vue：所有写操作后 fetchCourseList() → await fetchCourseList()
- CourseManage.vue：删最后一条自动退页（pageNum--）
- CourseManage.vue toggleTag：加最多10个标签上限校验
- NotificationBell.vue：case 'course' 路径 /course/detail/ → /course/
- TinyMCE 初始化竞态：加 waitForTinyMCE() 轮询机制

---

**总检查清单（课程模块）:**
- [x] 建5张课程相关表 (tb_course / chapter / progress / favorite / review) ✅
- [x] 5.1 开发用户端列表/详情/章节3个接口 + CourseList.vue + CourseDetail.vue ✅
- [x] 5.2 开发 APODSyncScheduler + 预置种子数据（含APOD专属课+10~15门普通课） ✅
- [x] 5.3 开发收藏/历史4个接口 + CourseFavorite.vue + CourseHistory.vue + UserLayout.vue改造 ✅
- [x] 5.4 开发 recommend 接口 + CourseList.vue 推荐区块 ✅
- [x] 5.5 开发 AdminCourseController（11个接口）+ CourseManage.vue ✅ 2026-03-22
- [x] 5.6 课程评价模块全部完成 ✅ 2026-03-22
- [x] 改造 router/index.js（新增4条课程路由） ✅

**预计工时:** 6天

---

#### 5.6 课程评价模块 ✅ 2026-03-22完成
**完成目标:** 用户对已学习的课程进行打分和评论，管理员可在后台对评价进行审核和管理。

**用户端功能:**
- [x] 提交评价（1-5星 + 文字评论，每门课每人只能评一次）
- [x] 编辑已有评价（可修改星级和内容）
- [x] 查看评价列表（分页，按时间倒序，自己的评价有「我」标签高亮）
- [x] 评价入口：CourseDetail.vue 评价区（占位已替换为真实功能）
- [x] 查询我的评价（判断是否已评，已评展示横条+编辑按钮）
- [x] 我的课程评价列表页（MyCourseReviews.vue，含封面+编辑弹窗）
- [x] 课程列表卡片显示★avgRating评分+评价数

**管理员端功能（AdminCourseReview.vue）:**
- [x] 搜索栏：课程名/用户名关键词 + 星级下拉（全部/1-5星）
- [x] 顶部3个统计卡片：总评价数 / 本周新增 / 平均星级
- [x] 表格列：ID / 课程标题 / 用户名 / 星级（★渲染）/ 评价内容（截断+tooltip）/ 提交时间 / 操作
- [x] 操作列：删除（el-popconfirm二次确认，status=0逻辑删除）
- [x] 入口：AdminLayout.vue「课程管理」子菜单「课程评价」+ 用户中心侧边栏

**实际完成接口 (6个):**
```
用户端 (3个):
POST   /api/course/{id}/review        - 提交评价（需登录，一人一评校验）
GET    /api/course/{id}/reviews       - 评价列表（分页，status=1）
GET    /api/course/{id}/review/my     - 查询我的评价（未评返回null）
PUT    /api/course/{id}/review        - 编辑评价（只能改自己的）
GET    /api/course/review/my/list     - 我的课程评价列表（含课程信息，分页）

管理员端 (3个，挂在 AdminCourseController):
GET    /api/admin/course/reviews      - 评价列表（分页+courseId+rating+keyword）
GET    /api/admin/course/review/stats - 统计（total/thisWeek/avgRating）
DELETE /api/admin/course/review/{id}  - 逻辑删除（status=0）
```

**后端文件:**
```
module/course/entity/CourseReview.java              ✅ 新建实体（status手动软删除，无@TableLogic）
module/course/dto/CourseReviewSubmitDTO.java         ✅ 新建（rating必传1-5，content可选≤500字）
module/course/vo/CourseReviewVO.java                 ✅ 新建（含courseTitle/courseCover/updateTime）
module/admin/vo/AdminCourseReviewVO.java             ✅ 新建（含courseTitle/username）
module/course/mapper/CourseReviewMapper.java         ✅ 新建（@Select注解+XML方法声明）
resources/mapper/CourseReviewMapper.xml              ✅ 新建（管理员多条件分页查询）
module/course/service/CourseService.java             ✅ 新增5个方法声明
module/course/service/impl/CourseServiceImpl.java   ✅ 新增5个方法实现（rawList修复）
module/course/controller/CourseController.java       ✅ 新增3个用户端接口
module/admin/service/AdminCourseService.java         ✅ 新增2个方法声明
module/admin/service/impl/AdminCourseServiceImpl.java ✅ 新增2个方法+修复rawList bug
module/admin/controller/AdminCourseController.java   ✅ 新增3个管理员端接口
CourseVO.java                                        ✅ 新增avgRating/reviewCount字段
CourseMapper.xml                                     ✅ 三处SELECT追加avg_rating/review_count子查询
```

**前端文件:**
```
views/admin/AdminCourseReview.vue    ✅ 新建（3统计卡片+搜索+表格+分页+二次确认删除）
views/user/MyCourseReviews.vue       ✅ 新建（我的评价列表+封面缩略图+编辑弹窗）
views/course/CourseDetail.vue        ✅ 改造（评价区激活：提交/编辑/列表/我的评价横条）
views/course/CourseList.vue          ✅ 改造（卡片显示★评分+「我的评价」快捷入口）
views/user/UserLayout.vue            ✅ 改造（侧边栏新增「课程评价」ChatDotRound菜单项）
router/index.js                      ✅ 改造（/user children新增 course-reviews 路由）
api/course.js                        ✅ 改造（getCourseReviews升级+4个新方法）
api/admin/course.js                  ✅ 改造（新增3个评价管理方法）
AdminLayout.vue                      ✅ 已于5.5完成，course-review路由已存在
```

**关键技术细节:**
- ⚠️ `page.getRecords()` 在MyBatis-Plus @Select注解+Page参数时始终为空，数据在方法返回值里，必须用 `List<> rawList = mapper.method(page, ...)` 接收
- ⚠️ tb_course_review 无deleted字段，用 status=0 做软删除，不能用 @TableLogic
- ⚠️ avgRating/reviewCount 通过子查询实时计算，不冗余存储在tb_course表
- 一人一评校验在Service层通过 getUserReview() 查重，不依赖数据库唯一索引

**预计工时:** 1.5天 → **实际工时:** 1天

---

### ✅ 6. 地理位置推荐模块
**开发时间:** 第13周  
**状态:** ✅ 6.0✅6.1✅6.2✅6.3✅6.4✅6.5✅（全部完成，2026-03-26）
**预计工时:** 4天

**设计原则:**
- 核心体验：用户打开页面，一眼看到今晚能不能观测，以及附近哪里最适合
- 天气数据：调用高德天气 API，后端调用不暴露Key给前端
- 月相计算：后端纯算法计算（无需外部 API），精确到当日月相和照明比例
- 地图展示：高德地图 JS API 2.0，main.js 统一加载，全局共用
- 观测点数据：预置全国30~50个优质观测点种子数据 + 管理员后台可增删
- 通知：2种（签到成功实时触发 / 天气适宜预留按需启用）
- 评分：用户可对观测点提交1~5星评分，tb_spot_rating防重复
- 地址联动：改造已有地址页面，定位自动填充 + 坐标写入tb_user供推荐系统用

**分节依赖顺序（严格遵守）:**
```
6.0 基础建设 → 全部完成后才开始任何后续节
  ↓ (以下5节完成6.0后可并行开发)
6.1 观测点列表/地图/筛选/评分  → 创建 ObservationMap.vue 骨架
6.2 天气+今晚观测条件          → 补全 ObservationMap.vue 天气区块
6.3 用户签到+我的足迹          → 补全 ObservationMap.vue 签到区块 + 新建 CheckinHistory.vue
6.4 地址联动                   → 改造 UserAddress.vue / CheckoutPage.vue（纯前端+1个后端接口）
6.5 后台观测点管理              → 完全独立，新建 ObservationSpotManage.vue
```

**⚠️ 高德 API Key（已申请，直接使用）:**
```yaml
# application.yml 新增
amap:
  web-key: 2ce80d8a2c6b51db75fd2c6603086432  # 后端调天气API用，不暴露前端
```
```bash
# .env.development / .env.production 新增
VITE_AMAP_JS_KEY=45d0e6381bae07b6c8fbcb5981c34aa9
VITE_AMAP_SECURITY_CODE=79173ee15d14fd11a3c3d00186a3bd9d
```

**数据库表 (3张):**
- `tb_observation_spot` (观测点表，含 rating/rating_count 字段) ✅ 2026-03-23
- `tb_user_checkin` (用户签到记录表) ✅ 2026-03-23
- `tb_spot_rating` (观测点评分记录表，防重复) ✅ 2026-03-23

**数据统计变化:**
```
接口数:     +13（用户端8 + 管理员端5）
数据库表:   +3张（tb_observation_spot / tb_user_checkin / tb_spot_rating）
后端模块:   +1（module/location/）
前端页面:   +3（ObservationMap.vue / CheckinHistory.vue / ObservationSpotManage.vue）
前端API:    +2（api/location.js / api/admin/location.js）
通知:       +2种（签到成功 / 天气适宜预留enabled=0）
预计工时:   4天
```

---

#### 6.0 基础建设 ✅ 2026-03-23
**职责:** 把所有节需要的底层资源一次性备好，后续节不再碰基础层。必须全部完成才能开始6.1~6.5任意节。

**数据库（3张表 + 2条 ALTER）:**

```sql
-- 观测点表
CREATE TABLE `tb_observation_spot` (
  `id`                    bigint(20)    NOT NULL AUTO_INCREMENT,
  `spot_name`             varchar(100)  NOT NULL,
  `longitude`             decimal(10,7) NOT NULL COMMENT '高德GCJ-02坐标',
  `latitude`              decimal(10,7) NOT NULL,
  `province`              varchar(50)   DEFAULT NULL,
  `city`                  varchar(50)   DEFAULT NULL,
  `address`               varchar(200)  DEFAULT NULL,
  `altitude`              int(11)       DEFAULT 0 COMMENT '海拔(米)',
  `light_pollution_level` tinyint(4)    DEFAULT 5 COMMENT 'Bortle等级1-9，越小越暗越好',
  `rating`                decimal(3,2)  DEFAULT 0.00 COMMENT '综合评分(0-5)',
  `rating_count`          int(11)       DEFAULT 0 COMMENT '评分人数',
  `description`           text          DEFAULT NULL,
  `images`                text          DEFAULT NULL COMMENT 'JSON数组',
  `checkin_count`         int(11)       DEFAULT 0 COMMENT '总签到次数(冗余)',
  `deleted`               tinyint(1)    DEFAULT 0,
  `create_time`           datetime      DEFAULT CURRENT_TIMESTAMP,
  `update_time`           datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_location` (`longitude`, `latitude`),
  KEY `idx_province_city` (`province`, `city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='天文观测点表';

-- 用户签到记录表
CREATE TABLE `tb_user_checkin` (
  `id`           bigint(20)    NOT NULL AUTO_INCREMENT,
  `user_id`      bigint(20)    NOT NULL COMMENT '关联tb_user.id',
  `spot_id`      bigint(20)    NOT NULL COMMENT '关联tb_observation_spot.id',
  `longitude`    decimal(10,7) DEFAULT NULL COMMENT '签到时用户坐标经度',
  `latitude`     decimal(10,7) DEFAULT NULL COMMENT '签到时用户坐标纬度',
  `weather`      varchar(50)   DEFAULT NULL COMMENT '签到时天气快照',
  `moon_phase`   varchar(20)   DEFAULT NULL COMMENT '签到时月相快照',
  `checkin_date` date          NOT NULL COMMENT '签到日期(每日去重)',
  `create_time`  datetime      DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_spot_date` (`user_id`, `spot_id`, `checkin_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_spot_id` (`spot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户观测点签到记录表';

-- 观测点评分记录表（防重复评分）
CREATE TABLE `tb_spot_rating` (
  `id`          bigint(20)  NOT NULL AUTO_INCREMENT,
  `user_id`     bigint(20)  NOT NULL COMMENT '关联tb_user.id',
  `spot_id`     bigint(20)  NOT NULL COMMENT '关联tb_observation_spot.id',
  `score`       tinyint(4)  NOT NULL COMMENT '1-5星',
  `create_time` datetime    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_spot` (`user_id`, `spot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='观测点用户评分记录表';

-- ALTER：tb_user 经纬度字段注释明确用途（字段已存在，仅更新注释）
ALTER TABLE `tb_user`
  MODIFY `longitude` decimal(10,7) DEFAULT NULL
    COMMENT '用户常用位置经度(填写地址时更新，供推荐系统使用)',
  MODIFY `latitude`  decimal(10,7) DEFAULT NULL
    COMMENT '用户常用位置纬度(填写地址时更新，供推荐系统使用)';
```

**种子数据:** 执行30~50条全国优质天文观测点INSERT SQL（一次性，后续不再提）

**后端文件骨架（全部空类，6.0统一创建，后续各节填充）:**
```
module/location/
├── controller/LocationController.java
├── entity/ObservationSpot.java / UserCheckin.java / SpotRating.java
├── mapper/ObservationSpotMapper.java(.xml) / UserCheckinMapper.java(.xml) / SpotRatingMapper.java(.xml)
├── service/LocationService.java + impl/LocationServiceImpl.java
├── dto/CheckinDTO.java / SpotRatingDTO.java
└── vo/ObservationSpotVO.java / SpotDetailVO.java / WeatherVO.java / TonightVO.java / CheckinVO.java

admin/controller/AdminLocationController.java（空类骨架）
admin/service/AdminLocationService.java + impl/AdminLocationServiceImpl.java（空类骨架）
```

**main.js 统一加载高德JS API（全局一次，所有页面共用）:**
```javascript
import AMapLoader from '@amap/amap-jsapi-loader'

// ⚠️ 必须在 AMapLoader.load() 之前声明，否则地图加载失败（JS API 2.0 强制要求）
window._AMapSecurityConfig = {
  securityJsCode: import.meta.env.VITE_AMAP_SECURITY_CODE  // 79173ee15d14fd11a3c3d00186a3bd9d
}

AMapLoader.load({
  key: import.meta.env.VITE_AMAP_JS_KEY,   // 45d0e6381bae07b6c8fbcb5981c34aa9
  version: '2.0',
  plugins: ['AMap.Geolocation', 'AMap.Marker', 'AMap.InfoWindow',
            'AMap.Circle', 'AMap.Geocoder']
}).then(AMap => { window.AMap = AMap })
```
```

**WebMvcConfig 白名单新增:**
```java
"/api/location/spots", "/api/location/spot/*",
"/api/location/weather", "/api/location/tonight"
// checkin 和 rating 接口需要登录，不加白名单
```

**通知准备（notification模块，一次性完成）:**
```java
// NotificationType.java 新增
LOCATION_CHECKIN_SUCCESS("location_checkin_success", "观测点签到成功"),
LOCATION_WEATHER_SUITABLE("location_weather_suitable", "今晚观测条件极佳"),

// NotificationHelper.java 新增方法体（6.3直接调用）
@Async
public void sendCheckinNotification(Long userId, String spotName, Long spotId, int todayCount) {...}
@Async
public void sendWeatherSuitableNotification(Long userId, int score) {...}

// 执行2条通知模板INSERT SQL（WEATHER_SUITABLE初始enabled=0）
```

**6.0 检查清单:** ✅ 全部完成 2026-03-23
- [x] 建3张表（tb_observation_spot / tb_user_checkin / tb_spot_rating）✅
- [x] ALTER tb_user 两个字段注释 ✅
- [x] 执行种子数据SQL（35条观测点）✅
- [x] application.yml 新增 amap.web-key: 2ce80d8a2c6b51db75fd2c6603086432 ✅
- [x] .env 新增 VITE_AMAP_JS_KEY=45d0e6381bae07b6c8fbcb5981c34aa9 ✅
- [x] .env 新增 VITE_AMAP_SECURITY_CODE=79173ee15d14fd11a3c3d00186a3bd9d ✅
- [x] main.js 统一加载高德JS API（含AMap.Geocoder插件）✅
- [x] 创建 module/location/ 全部文件骨架（22个文件）✅
- [ ] 创建 admin/controller/AdminLocationController.java 骨架
- [x] NotificationType.java 新增2个枚举值 ✅
- [x] NotificationHelper.java 新增2个方法 ✅
- [x] 执行2条通知模板INSERT SQL ✅
- [x] WebMvcConfig 白名单新增4条 ✅

---

#### 6.1 观测点列表、地图与评分 ✅ 2026-03-24完成
**完成目标:** 观测点数据展示、地图交互、省市/光污染筛选、1~5星评分提交。ObservationMap.vue 完整实现，天气区块和签到区块以注释占位，待6.2/6.3补全。

**用户端功能:**
- [x] 页面进入时调用浏览器 `navigator.geolocation` 获取当前位置
- [x] 定位拒绝时：省市下拉切换搜索中心坐标（选省/市即切地图中心，500km内重新加载），已覆盖全国省市区坐标表
- [x] 高德地图展示（window.AMap，6.0已全局加载），彩色星星Marker标注观测点（颜色按Bortle等级：绿/黄绿/黄/橙/红）
- [x] 顶部筛选栏：省份/城市联动下拉 + 光污染Bortle等级（1-9）滑块 + 搜索半径选择
- [x] 地图/列表视图切换（卡片式，按距离排序，卡片含图片/Bortle角标/距离/海拔/评分）
- [x] 观测点详情弹窗：四格指标卡（暗天等级/综合评分/海拔/今日签到）+ 位置 + 介绍 + 评分区
- [x] 1~5星评分提交（tb_spot_rating UNIQUE约束防重复，已评分显示「您已评N星」并禁用提交）

**新增接口 (3个):**
```
GET /api/location/spots
  参数: longitude / latitude / radius(km默认100) / limit(默认20)
        maxLightPollution(可选,1-9)
  逻辑: Haversine公式计算距离，按距离排序，支持Bortle筛选
        ⚠️ 省市参数已移除SQL过滤，改为前端切换搜索中心坐标（updateCenter）
  返回: List<ObservationSpotVO>（含distance/myScore，不含weatherSummary）

GET /api/location/spot/{id}
  返回: SpotDetailVO（含完整描述/图片列表/签到统计/myScore）
  白名单中，userId可选传（6.2前需移入OPTIONAL_AUTH_LIST使myScore正确返回）

POST /api/location/spot/{id}/rating
  需要登录（Controller层判断userId==null返回error）
  入参: { score: 1-5 }
  逻辑: tb_spot_rating UNIQUE约束防重复 → 重新计算平均分 → 更新rating/rating_count
  返回: { newRating, ratingCount }
```

**新增/更新文件:**
```
后端:
  module/location/controller/LocationController.java   ← @RequestMapping("/api/location") 完整路径
  module/location/service/LocationService.java         ← 3个已实现 + 4个TODO签名
  module/location/service/impl/LocationServiceImpl.java ← getSpots/getSpotDetail/submitRating + enrichImages()
  module/location/mapper/ObservationSpotMapper.java    ← selectNearbySpots/selectSpotDetail
  module/location/mapper/ObservationSpotMapper.xml     ← Haversine SQL，列名：spot_name/light_pollution_level/checkin_date
  module/location/mapper/SpotRatingMapper.java         ← selectUserScore/updateSpotRating
  module/location/mapper/SpotRatingMapper.xml          ← AVG子查询原子更新
  module/location/vo/ObservationSpotVO.java            ← 含distance/myScore
  module/location/vo/SpotDetailVO.java                 ← 含images[]/todayCheckinCount/myScore
  module/location/dto/SpotRatingDTO.java               ← @Min(1)@Max(5)
  config/WebMvcConfig.java                             ← 白名单修复：spot/ → spot/*

前端:
  views/location/ObservationMap.vue   ← 新建（深色主题/grey地图/Marker/卡片/弹窗/评分）
  api/location.js                     ← 7个方法完整版
  router/index.js                     ← 新增 { path: '/location', requiresAuth: false }
  views/Home.vue                      ← 新增「观测点」顶部导航 + 快捷入口卡片（grid 4列→5列）
```

**6.1 检查清单:**
- [x] ObservationSpotVO / SpotDetailVO / SpotRatingDTO 填充字段 ✅
- [x] ObservationSpotMapper.xml 实现Haversine距离查询（列名对齐：spot_name/light_pollution_level/checkin_date）✅
- [x] SpotRatingMapper.xml 实现防重复评分+平均分更新 ✅
- [x] LocationServiceImpl 实现 getSpots / getSpotDetail / submitRating ✅
- [x] LocationController 实现3个接口（@RequestMapping("/api/location") 完整路径）✅
- [x] 创建 ObservationMap.vue（地图+列表+筛选+评分全部可用，天气/签到TODO占位）✅
- [x] 创建 api/location.js（7个方法完整版）✅
- [x] router/index.js 新增路由（/location，requiresAuth: false）✅
- [x] Home.vue 新增「观测点」导航入口 + 快捷入口卡片（5列网格）✅
- [x] WebMvcConfig.java 修复白名单：/api/location/spot/ → /api/location/spot/* ✅

⚠️ **已知问题（6.2开发前修复）:**
- `spot/*` 在JWT白名单中，detail接口拿不到userId，myScore后端返回null
- 前端用 `localRatingCache` 内存缓存临时补救（刷新丢失）
- **根本解法:** 将 `spot/*` 从白名单移除，加入 `JwtInterceptor.OPTIONAL_AUTH_LIST`（与课程接口相同处理方式）

---

#### 6.2 天气与今晚观测条件 ✅ 2026-03-26完成
**职责:** 天气数据接入和今晚综合评估。本节只补全 ObservationMap.vue 中6.1预留的天气TODO区块，不改动其他已完成结构。

**说明:** /spots 列表接口不返回天气（避免N+1），用户点击观测点详情时按需加载。今晚条件卡片使用用户当前位置坐标。/tonight 强依赖 /weather，两个接口在同一节开发，避免乱序。

**新增接口 (2个):**
```
GET /api/location/weather
  参数: longitude / latitude
  逻辑:
    1. 后端调用高德天气API（web-key: 2ce80d8a2c6b51db75fd2c6603086432，不暴露前端）
       GET https://restapi.amap.com/v3/weather/weatherInfo?key=WEB_KEY&...
    2. 解析天气：天气状况/温度/湿度/风力/风速
    3. 计算观测适宜度评分:
       晴天+40 / 湿度<60%+20 / 湿度60-80%+10 / 湿度>80%-20
       风力1-3级+20 / 4-5级+0 / 6级+-20 / 温度5-25℃+20
       总分≥80极佳(绿) / 60-79良好(浅绿) / 40-59一般(黄) / 20-39较差(橙) / <20不宜(红)
  返回: WeatherVO { condition/temp/humidity/windLevel/suitabilityScore/suitabilityLevel/suitabilityColor }

GET /api/location/tonight
  参数: longitude / latitude
  逻辑:
    1. 复用 getWeather() 获取天气适宜度分
    2. 后端纯算法计算月相（无需外部API）:
       基准: 2000-01-06为已知新月
       phase = (daysSince % 29.53059) / 29.53059  → 0.0~1.0
       illumination = (1 - cos(phase×2π)) / 2 × 100
       月相名称: 新月/峨眉月/上弦月/盈凸月/满月/亏凸月/下弦月/残月
    3. 月相评分: 新月100分→满月0分（线性）
    4. 综合评分 = 天气分×0.6 + 月相分×0.4
    5. 生成建议文字（5星对应5句预设文案）
  返回: TonightVO { weatherSuitability/moonPhase/moonIllumination/moonPhaseName
                    /overallScore/overallStars/suggestion }
```

**新增/更新文件:**
```
ObservationMap.vue   ← 补全天气相关TODO区块（顶部今晚卡片 + 详情弹窗天气懒加载）
api/location.js      ← 新增 getWeather / getTonightCondition
```

**6.2 检查清单:**
- [x] WeatherVO / TonightVO 填充字段 ✅
- [x] LocationServiceImpl 实现 getWeather（逆地理编码→高德天气API + 适宜度评分逻辑）✅
- [x] LocationServiceImpl 实现 calculateMoonPhaseValue + getMoonPhaseName ✅
- [x] LocationServiceImpl 实现 getTonightCondition（复用getWeather + 月相合并）✅
- [x] LocationController 实现2个接口（GET /weather + GET /tonight）✅
- [x] ObservationMap.vue 补全顶部今晚条件卡片（星级 + 天气分 + 月相 + 建议文字）✅
- [x] ObservationMap.vue 补全详情弹窗天气懒加载区域（当前天气面板 + 适宜度彩色标签）✅
- [x] api/location.js 已在6.0预留 getWeather / getTonightCondition（无需新增）✅

---

#### 6.3 用户签到与我的足迹 ✅ (2026-03-26完成)
**职责:** 签到打卡、签到历史、我的足迹页面。补全 ObservationMap.vue 签到区块，新建 CheckinHistory.vue。

**已实现接口 (2个):**
```
POST /api/location/checkin （需要登录）
  入参: { spotId, longitude, latitude }
  逻辑:
    1. 查询观测点坐标，Haversine计算用户距离 > 5km → 返回"距离太远，需在5km内"
    2. QueryWrapper查 user_id+spot_id+checkin_date 唯一组合 → 已存在 → 返回"今日已签到"
    3. 调用 getWeather() 获取天气快照 + getMoonPhaseName() 获取月相快照
    4. INSERT tb_user_checkin（含weather/moon_phase快照字段）
    5. UPDATE tb_observation_spot.checkin_count += 1
    6. 查今日该观测点签到总人数(todayCount)
    7. 异步调用 notificationHelper.sendCheckinNotification()
  返回: { todayCheckinCount, weather, moonPhaseName }

GET /api/location/checkin/my （需要登录）
  参数: pageNum / pageSize
  返回: { list: [CheckinVO], total, pageNum, pageSize }
```

**新增/更新文件:**
```
后端:
  LocationController.java        ← 占位方法替换为正式实现（接收CheckinDTO/@RequestBody）
  LocationService.java           ← 返回类型 Object → Map<String, Object>
  LocationServiceImpl.java       ← 实现 checkin()（Haversine距离校验+去重+快照+计数+通知）
                                   实现 getCheckinHistory()（分页+JOIN观测点表）
                                   新增 haversineKm() 距离计算工具方法
                                   评分改为支持修改（已评过则UPDATE，未评过则INSERT）
  SpotDetailVO.java              ← 新增 todayCheckedIn/checkinWeather/checkinMoonPhase 字段
  ObservationSpotMapper.xml      ← selectSpotDetail 新增3个子查询（签到状态+天气月相快照）
  WebMvcConfig.java              ← /api/location/spot/* 从白名单移至可选认证
  JwtInterceptor.java            ← OPTIONAL_AUTH_LIST 新增 /api/location/spot/

前端:
  ObservationMap.vue             ← 详情弹窗新增签到面板（登录判断→签到按钮→已签到状态）
                                   签到状态 localStorage 持久化（当天有效，次日自动清理）
                                   评分支持修改（已评分后显示「修改」按钮）
                                   弹窗打开时从后端恢复签到状态和评分（可选认证模式）
  views/location/CheckinHistory.vue ← 新建（我的足迹页面，日期卡片+观测点名+天气月相快照+分页）
  router/index.js                ← 启用 /user/checkin-history 路由（requiresAuth: true）
  UserLayout.vue                 ← 侧边栏新增「我的足迹」入口（Location图标）
  api/location.js                ← checkin / getCheckinHistory 方法已在6.0预留，无需修改
```

**⚠️ 重要改造: spot详情接口改为可选认证**
```
原问题: /api/location/spot/* 在JWT白名单中，即使登录也跳过Token解析，
        导致 myScore 和签到状态永远为 null
修复: 从 WebMvcConfig.excludePathPatterns 移除 → 加入 JwtInterceptor.OPTIONAL_AUTH_LIST
效果: 未登录仍可访问，已登录自动解析userId，myScore/todayCheckedIn 正确返回
      /spot/{id}/rating 仍需登录（Controller层判断 userId==null 返回错误）
```

**⚠️ 评分改为可修改**
```
原逻辑: 已评过分 → throw BusinessException("每人每点只能评一次")
新逻辑: 已评过分 → UpdateWrapper 更新 score → 重新计算平均分
前端: 已评分后显示「⭐⭐⭐⭐ 您已评4星 · 修改」，点击修改进入编辑模式
```

**6.3 检查清单:**
- [x] CheckinDTO / CheckinVO 6.0骨架已齐全，无需修改
- [x] UserCheckinMapper.xml 6.0骨架已有完整SQL，无需修改
- [x] LocationServiceImpl 实现 checkin（距离校验+去重+天气月相快照+计数+通知）
- [x] LocationServiceImpl 实现 getCheckinHistory（分页+total）
- [x] LocationController 实现2个接口（POST /checkin + GET /checkin/my）
- [x] ObservationMap.vue 补全签到面板（登录→签到→已签到三态 + localStorage持久化）
- [x] 新建 CheckinHistory.vue（日期卡片+天气月相标签+分页+空状态）
- [x] api/location.js 2个方法已在6.0预留
- [x] router 启用 /user/checkin-history 路由
- [x] UserLayout.vue 侧边栏新增「我的足迹」入口
- [x] spot详情改为可选认证，登录后实时显示评分+签到状态
- [x] 评分支持修改（UPDATE替代INSERT拒绝）

---

#### 6.4 地址联动 ✅（2026-03-26完成）
**职责:** 改造已有地址相关页面，接入定位自动填充省市区，同步写入tb_user坐标供推荐系统使用。本节不新建任何页面，只改造已有文件。

**新增接口 (1个):**
```
PUT /api/user/location
  需要登录
  入参: { longitude, latitude }
  逻辑: UPDATE tb_user SET longitude=?, latitude=? WHERE id=?
  说明: 推荐系统使用，用户使用「当前位置」填地址时顺带调用
```

**改造逻辑（UserAddress.vue 和 CheckoutPage.vue 新增地址弹窗，改造方式完全一致）:**
```javascript
// 点击「📍 使用当前位置」按钮
navigator.geolocation.getCurrentPosition(pos => {
  const { longitude, latitude } = pos.coords
  // 逆地理编码（window.AMap.Geocoder，6.0已全局加载）
  geocoder.getAddress([longitude, latitude], (status, result) => {
    if (status === 'complete') {
      // 自动填入省/市/区/街道
      form.province = addr.province
      form.city     = addr.city
      form.district = addr.district
      form.address  = formattedAddress
    }
  })
  // 顺带更新tb_user坐标（供推荐系统使用）
  updateUserLocation({ longitude, latitude })
}, err => {
  ElMessage.warning('获取位置失败，请手动填写')
})
```

**新增/更新文件:**
```
UserController.java  ← 新增 PUT /user/location 端点（Map<String,BigDecimal>入参）
UserService.java     ← 新增 updateLocation(userId, longitude, latitude) 声明
UserServiceImpl.java ← 新增 updateLocation 实现（selectById→setLongitude/Latitude→updateById）
UserAddress.vue      ← 新增「📍使用当前位置」按钮+逆地理编码+坐标写入
CheckoutPage.vue     ← 同上改造
api/user.js          ← 新增 updateUserLocation 方法
```

**关键技术细节:**
- 后端入参使用 `Map<String, BigDecimal>` 接收经纬度，避免新建DTO
- 前端通过 `navigator.geolocation.getCurrentPosition` 获取高精度坐标
- 逆地理编码使用 `window.AMap.Geocoder`（6.0已全局加载），从坐标解析出省/市/区/详细地址
- 地址自动填充后同时调用 `updateUserLocation` API 将坐标写入tb_user，供推荐系统使用
- 定位失败时友好降级提示"获取位置失败，请手动填写"

**6.4 检查清单:**
- [x] UserController 新增 PUT /user/location 接口
- [x] UserService/Impl 新增 updateLocation 方法
- [x] UserAddress.vue 新增「📍使用当前位置」按钮+逆地理编码+坐标写入
- [x] CheckoutPage.vue 同上改造
- [x] api/user.js 新增 updateUserLocation 方法

**预计工时:** 0.5天 → **实际工时:** 0.5天

---

#### 6.5 后台观测点管理 ✅（2026-03-26完成）
**职责:** 管理员端增删改查观测点，含高德地图坐标拾取。本节完全独立，不依赖6.1~6.4任何前端文件。

**新增接口 (5个，管理员端):**
```
GET    /api/admin/location/spot/list         观测点列表(分页+省市/关键词筛选)
POST   /api/admin/location/spot/add          新增观测点
PUT    /api/admin/location/spot/{id}         编辑观测点
DELETE /api/admin/location/spot/{id}         删除观测点（软删除）
GET    /api/admin/location/spot/{id}/stats   签到统计（总次数/近7日/近30日/TOP5用户）
```

**新增/更新文件:**
```
SpotCreateDTO.java                     ← 新建（@NotBlank/@NotNull/@DecimalMin等校验）
SpotUpdateDTO.java                     ← 新建（所有字段可选，非null字段覆盖）
AdminLocationService.java              ← 骨架→完整接口（5个方法声明）
AdminLocationServiceImpl.java          ← 骨架→完整实现（分页/BeanUtils拷贝/非null覆盖/@TableLogic软删/统计）
AdminLocationController.java           ← 骨架→完整实现（5个端点+@Valid+@AdminLog）
UserCheckinMapper.java                 ← 新增2方法（countBySpotIdAfterDate/selectTopUsersBySpotId）
views/admin/ObservationSpotManage.vue  ← 新建（列表+筛选+新增编辑弹窗左表单右地图坐标拾取+签到统计弹窗）
api/admin/location.js                 ← 6.0骨架→完整实现（5个方法+getSpotStats）
AdminLayout.vue                        ← 新增「观测点管理」菜单项（MapLocation图标）
router/index.js                        ← 启用 /admin/location 路由
```

**关键技术细节:**
- 省份/城市筛选使用 `like` 模糊匹配（兼容"福建"和"福建省"两种写法）
- 新增/编辑弹窗采用左右布局：左侧表单 + 右侧高德暗色地图坐标拾取
- 地图点击自动拾取坐标 + 逆地理编码填充省/市/地址
- Bortle等级使用 el-slider 滑块（1-9），表格中彩色 Tag 展示
- 签到统计：总次数读冗余字段，近7日/30日用 `countBySpotIdAfterDate` SQL查询，TOP5用户 GROUP BY 聚合
- `@TableLogic` 自动将 deleteById 转为 UPDATE deleted=1

**6.5 检查清单:**
- [x] SpotCreateDTO / SpotUpdateDTO 填充字段
- [x] AdminLocationServiceImpl 实现5个方法
- [x] AdminLocationController 实现5个接口
- [x] 新建 ObservationSpotManage.vue（列表+新增编辑+坐标拾取+统计弹窗）
- [x] api/admin/location.js 完善5个方法
- [x] AdminLayout.vue 新增菜单
- [x] router 启用 /admin/location 路由

**预计工时:** 1天 → **实际工时:** 0.5天

---

**数据统计变化:**
```
接口数:     +14（用户端8 + 管理员端5 + PUT/user/location 1）
数据库表:   +3张（tb_observation_spot / tb_user_checkin / tb_spot_rating）
后端模块:   +1（module/location/）
前端页面:   +3（ObservationMap.vue / CheckinHistory.vue / ObservationSpotManage.vue）
前端API:    +2（api/location.js / api/admin/location.js）
通知:       +2种（签到成功 / 天气适宜预留enabled=0）
预计工时:   4天
```

---

### 🔨 7. 论坛社区模块（小红书风格）
**开发时间:** 第14-15周（10天）
**状态:** ✅ 7.1✅7.2✅7.3✅7.4✅7.5✅7.6✅7.7✅7.8✅ 论坛社区全模块完成(2026-04-08)
**优先级:** 高
**预计工时:** 10天（分8个子节，阶段一7天可演示，阶段二3天完善）
**UI风格:** 小红书网页版 — 左侧深色边栏 + 瀑布流卡片 + 弹窗详情

**设计原则:**
```
- 天文爱好者的交流社区，围绕「观测分享 + 识别结果分享 + 器材讨论」三个核心场景
- 图片优先：帖子支持最多9张图片（base64压缩存储，与二手回收图片方案一致）
- 审核机制：支持自动通过开关（application.yml: forum.auto-approve: true），演示时开启
- 热度算法：(点赞×1 + 评论×2 + 收藏×3) / (发帖天数+2)^1.5
- 跨模块联动：AI识别结果一键分享 + 课程页「去论坛讨论」入口
- @提及功能：标记为扩展功能，优先级低，核心流程不依赖
```

**开发分阶段:**
```
阶段一（7天，核心可演示）:
  7.1 基础建设 — 建6张表+Entity+Mapper+骨架+配置              0.5天
  7.2 帖子发布 — 发帖/编辑/删除 + 图片上传 + ForumPublish.vue   1.5天
  7.3 帖子列表+详情 — 瀑布流+弹窗详情 + ForumLayout/List/Detail 1.5天
  7.4 评论+互动 — 两级评论 + 点赞/收藏                         1.5天
  7.5 关注+主页 — 关注系统 + UserProfile.vue                   1天
  7.7 后台管理 — ForumManage.vue 审核/管理/评论/统计四Tab       1.5天（先做后台）

阶段二（3天，完善增强）:
  7.6 搜索功能 — ForumSearch.vue + 热搜                        1天
  7.8 通知+热度+联动 — 9种通知 + ForumScheduler + 跨模块入口     1天
  ⭐ @提及功能（扩展，优先级低，时间不够可跳过）                  1天
```

---

#### 7.1 基础建设 ✅ (2026-03-27完成)
**职责:** 建6张数据库表 + Entity + Mapper + Service/Controller骨架 + application.yml配置。

**application.yml 新增配置:**
```yaml
forum:
  auto-approve: true    # 帖子自动审核通过（true=发布即公开，false=需管理员审核）
```

**数据库（6张表，砍掉tb_post_image/tb_post_tag冗余表）:**
```sql
-- ① tb_post 帖子主表
CREATE TABLE `tb_post` (
  `id`             bigint(20)   NOT NULL AUTO_INCREMENT,
  `user_id`        bigint(20)   NOT NULL COMMENT '📌关联tb_user.id',
  `title`          varchar(200) NOT NULL,
  `content`        text         NOT NULL,
  `images`         mediumtext   DEFAULT NULL COMMENT '图片(base64 JSON数组,最多9张,Canvas压缩1200px/0.82)',
  `tags`           varchar(500) DEFAULT NULL COMMENT '帖子标签(JSON数组) 📌推荐系统标签匹配',
  `status`         tinyint(4)   DEFAULT 1 COMMENT '0草稿 1审核中 2已发布 3已拒绝 4管理员删除',
  `reject_reason`  varchar(200) DEFAULT NULL,
  `like_count`     int(11)      DEFAULT 0,
  `comment_count`  int(11)      DEFAULT 0,
  `collect_count`  int(11)      DEFAULT 0,
  `view_count`     int(11)      DEFAULT 0,
  `is_top`         tinyint(1)   DEFAULT 0 COMMENT '是否置顶(管理员操作)',
  `hot_score`      double       DEFAULT 0 COMMENT '热度分,每小时更新',
  `is_hot`         tinyint(1)   DEFAULT 0 COMMENT '是否热门(热度超阈值自动置1)',
  `recognition_id` bigint(20)   DEFAULT NULL COMMENT 'AI识别分享关联 📌关联tb_recognition.id',
  `create_time`    datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_time`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        tinyint(1)   DEFAULT 0 COMMENT '@TableLogic',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_hot_score` (`hot_score`),
  KEY `idx_create_time` (`create_time`),
  FULLTEXT KEY `ft_post_search` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子表';

-- ② tb_post_comment 两级评论表
CREATE TABLE `tb_post_comment` (
  `id`                bigint(20)  NOT NULL AUTO_INCREMENT,
  `post_id`           bigint(20)  NOT NULL COMMENT '📌关联tb_post.id',
  `user_id`           bigint(20)  NOT NULL COMMENT '📌关联tb_user.id',
  `parent_id`         bigint(20)  DEFAULT 0 COMMENT '0=顶级,>0=回复(始终指向顶级)',
  `reply_to_user_id`  bigint(20)  DEFAULT NULL COMMENT '被回复用户ID 📌@通知依据',
  `reply_to_username` varchar(50) DEFAULT NULL COMMENT '被回复用户名(冗余)',
  `content`           text        NOT NULL,
  `like_count`        int(11)     DEFAULT 0,
  `status`            tinyint(4)  DEFAULT 1 COMMENT '0管理员删除 1正常',
  `create_time`       datetime    DEFAULT CURRENT_TIMESTAMP,
  `deleted`           tinyint(1)  DEFAULT 0 COMMENT '@TableLogic',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子评论表(两级)';

-- ③ tb_post_like 点赞表
CREATE TABLE `tb_post_like` (
  `id`          bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id`     bigint(20) NOT NULL,
  `user_id`     bigint(20) NOT NULL,
  `create_time` datetime   DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';

-- ④ tb_post_collect 收藏表
CREATE TABLE `tb_post_collect` (
  `id`          bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id`     bigint(20) NOT NULL,
  `user_id`     bigint(20) NOT NULL,
  `create_time` datetime   DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏表';

-- ⑤ tb_user_follow 关注关系表
CREATE TABLE `tb_user_follow` (
  `id`          bigint(20) NOT NULL AUTO_INCREMENT,
  `follower_id` bigint(20) NOT NULL COMMENT '关注者ID',
  `followed_id` bigint(20) NOT NULL COMMENT '被关注者ID',
  `create_time` datetime   DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow` (`follower_id`, `followed_id`),
  KEY `idx_follower` (`follower_id`),
  KEY `idx_followed` (`followed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';

-- ⑥ tb_search_log 搜索日志表
CREATE TABLE `tb_search_log` (
  `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
  `keyword`     varchar(100) NOT NULL,
  `user_id`     bigint(20)   DEFAULT NULL,
  `search_type` varchar(10)  DEFAULT 'post' COMMENT 'post/user',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_keyword` (`keyword`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索日志表';
```

**砍掉的2张冗余表:**
- ~~tb_post_image~~ → 用 `tb_post.images` JSON字段
- ~~tb_post_tag~~ → 用 `tb_post.tags` JSON字段

**后端骨架文件:**
```
module/forum/
├─entity/      Post.java / PostComment.java / PostLike.java / PostCollect.java / UserFollow.java
├─mapper/      PostMapper.java / PostCommentMapper.java / PostLikeMapper.java / PostCollectMapper.java / UserFollowMapper.java
├─service/     PostService.java / CommentService.java / FollowService.java / SearchService.java
├─service/impl/ (对应4个Impl，骨架方法占位)
├─controller/  PostController.java / AdminPostController.java
├─vo/          PostVO.java / PostCommentVO.java / UserProfileVO.java
└─task/        ForumScheduler.java (骨架)
```

**7.1 检查清单:**
- [x] 执行6张建表SQL（tb_post/tb_post_comment/tb_post_like/tb_post_collect/tb_user_follow/tb_search_log）
- [x] 创建6个Entity（Post/PostComment/PostLike/PostCollect/UserFollow/SearchLog，@TableLogic/@TableField注解）
- [x] 创建6个Mapper（继承BaseMapper，含SearchLogMapper）
- [x] 创建4个Service接口 + 4个Impl骨架（PostService/CommentService/FollowService/SearchService）
- [x] 创建2个Controller骨架（PostController用户端19端点 + AdminPostController后台8端点，后者放admin模块统一管理）
- [x] 创建3个VO（PostVO/PostCommentVO/UserProfileVO）+ 3个DTO（PostPublishDTO/PostCommentDTO放forum，PostAuditDTO放admin）
- [x] 创建ForumScheduler骨架（@Scheduled每小时热度计算，待7.8实现逻辑）
- [x] application.yml 新增 forum.auto-approve: true
- [x] 确认 @MapperScan 能扫到 forum.mapper 包（mvn compile + 启动验证通过）

---

#### 7.2 帖子发布/编辑/删除 ✅ (2026-03-30完成)
**职责:** 发帖（文字+图片+标签）、编辑（草稿/拒绝状态）、删除（@TableLogic），前端ForumPublish.vue。

**新增接口 (3个):**
```
POST   /api/post/publish              发布帖子
  入参: { title, content, images(base64 JSON数组), tags(JSON数组), recognitionId(可选) }
  逻辑: auto-approve=true → status=2直接发布; false → status=1等审核
PUT    /api/post/{id}                 编辑帖子（仅status=0草稿/3已拒绝可编辑）
DELETE /api/post/{id}                 删除帖子（@TableLogic deleted=1）
```

**图片上传方案（复用二手回收）:**
```
前端: Canvas压缩 → 最长边1200px / quality=0.82 → base64
存储: JSON数组写入 tb_post.images（mediumtext）
限制: 最多9张，单张约150~300KB，9张合计约2MB
```

**新增/修改文件:**
```
PostServiceImpl.java      ← publishPost/updatePost/deletePost 实现 ✅
PostController.java       ← 3个端点实现（7.1骨架已有，7.2标记更新） ✅
ForumPublish.vue           ← 🆕 发帖页（文字+图片拖拽上传+标签多选+recognitionId预填） ✅
api/forum.js              ← 🆕 publishPost/updatePost/deletePost 3个方法 ✅
router/index.js           ← 新增 /forum/publish 路由 ✅
```

**7.2 检查清单:**
- [x] PostServiceImpl 实现 publishPost（auto-approve判断，图片数量校验，计数字段初始化）
- [x] PostServiceImpl 实现 updatePost（作者身份校验+状态校验：仅草稿/已拒绝可编辑）
- [x] PostServiceImpl 实现 deletePost（作者身份校验+@TableLogic逻辑删除）
- [x] PostController 3个端点（7.1骨架已有，7.2标记注释更新）
- [x] ForumPublish.vue（小红书风格：标题+正文+图片拖拽上传Canvas压缩+预设/自定义标签+AI识别关联+编辑模式）
- [x] api/forum.js 3个方法（publishPost/updatePost/deletePost）
- [x] router/index.js 新增 /forum/publish 路由（requiresAuth: true）
- [x] mvn compile 验证通过 + git add

---

#### 7.3 帖子列表+详情（小红书瀑布流）✅ (2026-03-31完成)
**职责:** 论坛首页瀑布流+分类Tab+弹窗详情页，核心视觉体验。

**新增接口 (2个):**
```
GET    /api/post/list                 帖子列表
  参数: tab(all/follow/hot), tag(分类标签筛选), pageNum, pageSize
  逻辑: all=按create_time倒序; follow=关注用户的帖子; hot=按hot_score倒序
GET    /api/post/{id}                 帖子详情（含作者信息/isLiked/isCollected）
```

**小红书web一体化布局（v8.38优化）:**
```
┌──────────────────────────────────────────────────────────────────────┐
│ 🔭天文社区    │     [  搜索笔记  🔍]      │           返回商城       │ ← 顶部通栏(sticky)
├──────────────┼──────────────────────────────────────────────────────┤
│ 🔍 发现      │  推荐 │ 关注 │ 热门 ┃ 深空摄影 │ 望远镜 │ 更多 >    │ ← Tab栏(主tab+标签同行)
│ ✏️ 发布      │                                                      │
│ 🔔 通知      │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌────────┐ │
│ 👤 我        │  │ 封面图   │  │ 文字封面 │  │ 封面图   │  │封面图  │ │
│              │  │(裁剪适配)│  │(彩色卡片)│  │(裁剪适配)│  │        │ │
│ ── 快捷入口 ──│  ├─────────┤  ├─────────┤  ├─────────┤  ├────────┤ │
│ 🖼️ AI识别    │  │标题2行   │  │标题2行   │  │标题2行   │  │标题    │ │
│ 📚 课程      │  │👤昵称 ❤️n │  │👤昵称 ❤️n │  │👤昵称 ❤️n │  │👤 ❤️n │ │
│ 📍 观测点    │  └─────────┘  └─────────┘  └─────────┘  └────────┘ │
└──────────────┴──────────────────────────────────────────────────────┘
```
**v8.38 UI优化要点:**
- 顶部通栏：Logo+搜索框(红色搜索按钮)+返回商城，横跨全宽sticky
- 侧边栏：白底+灰线分隔（不再是深色#1a1a2e），与顶栏一体化
- Tab栏：主tab(推荐/关注/热门) + 竖线分隔 + 标签tab同行，和小红书一致
- 卡片封面：max-height:360px+object-fit:cover裁剪，无图帖子生成浅色文字封面
- 详情页：全屏覆盖层(非el-dialog)，左图+右内容，图片点击放大+滚轮缩放，背景滚动锁定
- ForumPublish去掉重复侧边栏，共用ForumLayout的统一侧边栏

**帖子详情弹窗（点击卡片弹出）:**
```
┌────────────────────────────────────────────────────────┐
│         帖子详情弹窗（el-dialog 80%宽）                  │
├──────────────────────┬─────────────────────────────────┤
│                      │  👤 作者头像  昵称  [+ 关注]     │
│   图片轮播区          │  帖子标题（大字）                │
│   (左右箭头切换)      │  帖子正文内容                   │
│   底部圆点指示器       │  #深空摄影 #望远镜              │
│                      │  发布时间                        │
│                      │  评论区（两级评论列表）            │
│                      │  [评论输入框]  ❤️赞 ⭐收藏 💬评论  │
└──────────────────────┴─────────────────────────────────┘
```

**配色方案（天文+小红书融合）:**
```
背景色: #f5f5f5    卡片色: #ffffff    主题色: #ff2442(小红书红)
侧边栏: #1a1a2e(天文蓝)   标题色: #333   副文字: #999   标签色: #4a90d9
```

**新增/修改文件:**
```
ForumLayout.vue    ← 🆕 论坛布局（左侧深色边栏+右侧router-view）
ForumList.vue      ← 🆕 瀑布流首页（搜索栏+分类Tab+CSS columns瀑布流+IntersectionObserver触底加载）
ForumDetail.vue    ← 🆕 帖子详情弹窗（左图片轮播+右正文评论）
PostCard.vue       ← 🆕 瀑布流卡片组件（封面图+标题+作者+❤️，3处复用）
PostWaterfall.vue  ← 🆕 瀑布流容器（CSS columns+响应式5/4/3/2列）
router/index.js    ← 新增 /forum 嵌套路由组
api/forum.js       ← getPostList/getPostDetail 2个方法
```

**瀑布流CSS（纯CSS，无需JS库）:**
```css
.waterfall-container {
  columns: 5; column-gap: 16px; padding: 0 16px;
}
.waterfall-container .post-card {
  break-inside: avoid; margin-bottom: 16px;
}
@media (max-width: 1400px) { .waterfall-container { columns: 4; } }
@media (max-width: 1100px) { .waterfall-container { columns: 3; } }
@media (max-width: 768px)  { .waterfall-container { columns: 2; } }
```

**路由设计（嵌套路由）:**
```javascript
{
  path: '/forum',
  component: ForumLayout,
  children: [
    { path: '', redirect: '/forum/list' },
    { path: 'list', component: ForumList },
    { path: 'detail/:id', component: ForumDetail },
    { path: 'publish', component: ForumPublish, meta: { requiresAuth: true } },
    { path: 'search', component: ForumSearch },
  ]
}
```

**新增/修改文件:**
```
PostMapper.xml             ← 🆕 3个XML SQL（selectPostList/countPostList/selectPostDetail，JOIN tb_user） ✅
PostMapper.java            ← 新增3个方法声明 ✅
PostServiceImpl.java       ← listPosts(tab分流+分页+JSON解析)/getPostDetail(互动状态子查询+浏览量+1) ✅
PostController.java        ← 2个端点注释更新（7.1骨架已有） ✅
JwtInterceptor.java        ← OPTIONAL_AUTH_LIST新增/api/post/list + /api/post/{id}正则匹配 ✅
ForumLayout.vue            ← 🆕 论坛布局（左侧深色边栏#1a1a2e + 右侧router-view） ✅
PostCard.vue               ← 🆕 瀑布流卡片组件（封面图+标题+作者+❤️数） ✅
PostWaterfall.vue           ← 🆕 瀑布流容器（CSS columns响应式5/4/3/2列） ✅
ForumList.vue              ← 🆕 瀑布流首页（搜索栏+推荐/关注/热门Tab+标签筛选+IntersectionObserver触底加载） ✅
ForumDetail.vue            ← 🆕 帖子详情弹窗（左图片轮播+右作者/正文/标签/评论占位/操作栏） ✅
api/forum.js               ← 新增 getPostList/getPostDetail 2个方法 ✅
router/index.js            ← /forum 嵌套路由组（ForumLayout + list/publish子路由） ✅
```

**7.3 检查清单:**
- [x] PostMapper.xml 3个SQL（列表+总数+详情，JOIN tb_user获取作者信息）
- [x] PostMapper.java 声明3个方法（selectPostList/countPostList/selectPostDetail）
- [x] PostServiceImpl 实现 listPosts（tab分流all/follow/hot + 标签筛选 + JSON解析images/tags + 分页）
- [x] PostServiceImpl 实现 getPostDetail（含isLiked/isCollected/isFollowed子查询 + 浏览量+1）
- [x] PostController 2个端点注释更新（7.1骨架已有，无需改动）
- [x] JwtInterceptor 可选认证配置（/api/post/list + /api/post/{id}正则匹配纯数字）
- [x] ForumLayout.vue（左侧深色天文蓝#1a1a2e边栏 + 发现/发布/通知/我的导航 + 底部AI识别/课程/观测点快捷入口）
- [x] ForumList.vue（搜索栏+推荐/关注/热门三Tab+20个天文标签筛选+IntersectionObserver触底加载）
- [x] ForumDetail.vue（el-dialog 80%宽弹窗：左图片轮播含箭头/圆点 + 右作者栏/正文/标签/评论占位/操作栏）
- [x] PostCard.vue（封面图+置顶/热门标签+标题2行截断+作者头像/昵称+❤️数）
- [x] PostWaterfall.vue（CSS columns瀑布流，响应式5/4/3/2列）
- [x] api/forum.js 新增 getPostList/getPostDetail 2个方法
- [x] router/index.js 重构为 /forum 嵌套路由组（ForumLayout + list/publish子路由）
- [x] Home.vue 已有「天文论坛」入口卡片（7.1规划时已添加）
- [x] mvn compile 验证通过

---

#### 7.4 评论系统+点赞收藏 ✅ (2026-04-02完成)
**职责:** 两级评论（顶级+楼中楼回复）、帖子点赞/收藏（幂等切换）、评论点赞（幂等切换）。

**新增接口 (6个):**
```
GET    /api/post/comment/list          评论列表（两级结构，可选认证，返回isLiked/isAuthor/replyCount）
POST   /api/post/comment              发布评论（含回复，parent_id+reply_to_user_id）
DELETE /api/post/comment/{id}         删除评论（用户自己）
POST   /api/post/comment/like/{id}    点赞评论（幂等切换，返回boolean）
POST   /api/post/like/{id}            帖子点赞/取消点赞（幂等切换）
POST   /api/post/collect/{id}         帖子收藏/取消收藏（幂等切换）
```

**两级评论结构:**
```
顶级评论（parent_id=0）
  └── 回复（parent_id=顶级评论ID，reply_to_user_id=被回复用户）
      └── 回复的回复（parent_id仍为顶级评论ID，只做两级，避免无限嵌套）
```

**通知触发（防自通知，7.8实现）:**
```
A 评论帖子 → 通知帖子作者（POST_COMMENTED），A==作者时跳过
A 回复 B 的评论 → 通知 B（COMMENT_REPLIED），A==B时跳过
```

**新增文件:**
```
CommentLike.java          ← 评论点赞实体（tb_comment_like，与PostLike同模式）
CommentLikeMapper.java    ← 评论点赞Mapper
PostCommentMapper.xml      ← 评论SQL（顶级评论分页+replyCount子查询+子回复查询+selectNicknameByUserId）
sql/7.4_comment_like.sql  ← 建表SQL（已执行）
```

**修改文件:**
```
CommentServiceImpl.java   ← 4方法全部实现（addComment两级+deleteComment+likeComment幂等+getCommentsByPostId含isLiked/isAuthor/replyCount）
PostServiceImpl.java      ← likePost/collectPost（INSERT/DELETE+更新计数，幂等切换）
CommentService.java       ← likeComment返回boolean，getCommentsByPostId加currentUserId参数
PostController.java       ← 新增GET /comment/list端点（可选认证），likeComment返回Boolean
PostCommentMapper.java    ← 声明3个XML方法+selectNicknameByUserId(@Select注解)
PostCommentVO.java        ← 新增isLiked/replyCount/isAuthor字段
JwtInterceptor.java       ← /api/post/comment/list加入可选认证列表
ForumDetail.vue           ← 完整评论区UI（小红书web风格）：作者"作者"标识+内联回复框(发送/取消)+回复折叠(展开/收起X条回复)+评论点赞切换+帖子点赞/收藏+尺寸放大90vw/90vh
ForumList.vue             ← 监听@updated事件，实时同步列表卡片点赞/收藏/评论计数
api/forum.js              ← 新增6个方法（getCommentList/addComment/deleteComment/likeComment/likePost/collectPost）
```

**7.4 检查清单:**
- [x] CommentServiceImpl 实现 addComment（两级，parent_id处理，replyToUsername冗余存储）
- [x] CommentServiceImpl 实现 deleteComment（作者校验+顶级评论连子评论一起减计数）
- [x] CommentServiceImpl 实现 likeComment（幂等切换，tb_comment_like INSERT/DELETE）
- [x] CommentServiceImpl 实现 getCommentsByPostId（两级结构+isLiked/isAuthor/replyCount）
- [x] PostServiceImpl 实现 likePost（插入/删除tb_post_like+更新like_count）
- [x] PostServiceImpl 实现 collectPost（同上逻辑）
- [x] PostController 6个端点（含新增GET /comment/list）
- [x] ForumDetail.vue 评论区完善（小红书风格：评论列表+内联回复+评论点赞+作者标识+回复折叠展开/收起）
- [x] ForumList.vue 列表点赞/收藏/评论数实时更新
- [x] tb_comment_like 建表（已执行）
- [x] mvn compile 验证通过

---

#### 7.5 关注系统+用户主页 ✅ (2026-04-03完成)
**职责:** 关注/取消关注、关注列表/粉丝列表、用户主页（小红书风格）、收藏/点赞可见性控制。

**新增接口 (8个):**
```
POST   /api/post/user/follow/{userId}  关注/取消关注（幂等切换）
GET    /api/post/user/follow/list      我关注的人（分页+互关判断）
GET    /api/post/user/fans/list        关注我的人（分页+互关判断）
GET    /api/post/my/list               我发布的帖子
GET    /api/post/my/collect            收藏的帖子（支持targetUserId查他人公开收藏）
GET    /api/post/my/like               点赞的帖子（支持targetUserId查他人公开点赞）
GET    /api/post/user/profile/{userId} 用户主页信息（帖子数+收藏数+关注粉丝数+isFollowed+collectVisible/likeVisible）
POST   /api/post/user/visibility       切换收藏/点赞列表可见性（type=collect|like）
```

**用户主页（小红书风格）:**
```
┌──────────────────────────────────────────────────────┐
│  🌌 星空背景Banner (深蓝渐变+星点装饰)                  │
│  [头像92px]  昵称  [+ 关注]  (他人主页时显示)           │
│              ★☆☆☆☆ Lv.1  城市                       │
│              12 关注  |  38 粉丝  |  56 获赞与收藏      │
│  ── [笔记] ── [收藏 🔒] ── [赞过 🔒] ──               │
│  瀑布流卡片（复用PostCard/PostWaterfall组件）            │
│  📌 收藏/点赞Tab旁有锁图标，自己可切换公开/私密           │
│  📌 他人主页：私密Tab不显示；关注/粉丝数不可点击          │
└──────────────────────────────────────────────────────┘
```

**数据库变更:**
```sql
ALTER TABLE tb_user ADD COLUMN collect_visible TINYINT NOT NULL DEFAULT 0 COMMENT '收藏列表是否公开(0-私密 1-公开)';
ALTER TABLE tb_user ADD COLUMN like_visible TINYINT NOT NULL DEFAULT 0 COMMENT '点赞列表是否公开(0-私密 1-公开)';
```

**新增/修改文件:**
```
User.java                 ← 新增 collectVisible/likeVisible 字段
UserProfileVO.java        ← 新增 collectVisible/likeVisible 字段
FollowServiceImpl.java    ← follow幂等切换+异步通知/getFollowList互关判断/getFansList互关判断
PostServiceImpl.java      ← getMyPosts/getMyCollects/getMyLikes/getUserProfile聚合统计
PostMapper.java           ← 新增selectLikedPosts/countLikedPosts/selectCollectedPosts/countCollectedPosts
PostMapper.xml            ← 新增4个SQL(收藏/点赞帖子列表+计数) + tab='user'条件
PostController.java       ← 8个端点(follow/followList/fansList/myList/myCollect/myLike/profile/visibility)
NotificationHelper.java   ← 新增sendUserFollowedNotification/getNickname
JwtInterceptor.java       ← OPTIONAL_AUTH_LIST新增/api/post/user/profile/
UserProfile.vue           ← 🆕 小红书风格用户主页（Banner+统计+三Tab瀑布流+锁图标可见性控制）
MyPosts.vue               ← 🆕 个人中心-我的帖子
MyCollects.vue             ← 🆕 个人中心-帖子收藏
ForumDetail.vue           ← 新增关注按钮@click+goToAuthorProfile
PostCard.vue              ← 新增作者头像点击跳转主页
ForumLayout.vue           ← "我"链接改为用户主页
UserLayout.vue            ← 侧边栏新增「社区互动」菜单组（我的帖子+帖子收藏）
api/forum.js              ← 新增8个方法(followUser/getFollowList/getFansList/getMyPosts/getMyCollects/getMyLikes/getUserProfile/toggleVisibility)
router/index.js           ← 新增forum/user/:id、user/my-posts、user/my-collects路由
```

**7.5 检查清单:**
- [x] FollowServiceImpl 实现 follow（插入/删除+发通知）
- [x] FollowServiceImpl 实现 getFollowList/getFansList（含互关判断）
- [x] PostServiceImpl 实现 getMyPosts/getMyCollects/getMyLikes
- [x] PostController 实现 getUserProfile（聚合6项统计+collectVisible/likeVisible）
- [x] PostController 实现 toggleVisibility（收藏/点赞可见性切换）
- [x] PostMapper.xml 新增4个SQL（收藏/点赞帖子列表+计数）
- [x] UserProfile.vue（星空Banner+关注粉丝+笔记/收藏/赞过Tab+瀑布流+锁图标）
- [x] ForumDetail.vue 关注按钮绑定@click+handleFollow
- [x] PostCard.vue 作者头像点击跳转主页
- [x] UserLayout.vue 侧边栏新增入口
- [x] router 新增 /forum/user/:id 路由（可选认证）
- [x] 他人主页：关注/粉丝不可点击查看列表
- [x] 他人主页：收藏/点赞Tab根据可见性设置显示/隐藏
- [x] /my/collect 和 /my/like 支持 targetUserId 参数查他人公开数据

---

#### 7.6 搜索功能（阶段二）✅ (2026-04-05完成)
**职责:** 搜索帖子+用户、热门搜索词、搜索历史localStorage、关键词高亮。

**新增接口 (2个):**
```
GET    /api/post/search               搜索（keyword/type=post|user/pageNum/pageSize）
GET    /api/post/search/hot           热门搜索词Top10（内存缓存1小时）
```

**搜索页布局（小红书风格）:**
```
┌──────────────────────────────────────────┐
│ ← [搜索框（自动聚焦）] [搜索]              │
├──────────────────────────────────────────┤
│ 搜索历史（localStorage，最近10条）         │
│  深空摄影 ×   望远镜 ×   火星 ×            │
│  [清除全部历史]                           │
├──────────────────────────────────────────┤
│ 🔥 热门搜索（后端统计）                    │
│  1. 天文摄影技巧    2. 望远镜选购          │
├──────────────────────────────────────────┤
│ 搜索结果：[帖子] [用户] Tab               │
│  瀑布流卡片（关键词高亮红色#ff2442）        │
└──────────────────────────────────────────┘
```

**后端搜索:**
```sql
-- 帖子：LIKE匹配 title+content, status=2, 按hot_score倒序
-- 用户：LIKE匹配 nickname+username, 最多20条
```

**热搜缓存（ConcurrentHashMap，无需Redis）:**
```java
private volatile List<String> hotSearchCache;
private volatile long hotSearchCacheTime = 0;
// getHotSearch(): 当前时间-cacheTime > 3600000ms 则重新查DB
// 第16周推荐系统引入Redis后可升级
```

**新增/修改文件:**
```
SearchServiceImpl.java    ← searchPosts/searchUsers/@Async写tb_search_log/热搜缓存/JSON.parseArray解析图片(修复base64) ✅
SearchLogMapper.java      ← @Select热搜Top10查询(7天内group by keyword) ✅
PostMapper.xml            ← searchPosts/countSearchPosts 2个SQL ✅
PostController.java       ← 2个端点(已有骨架，调用SearchService) ✅
ForumLayout.vue           ← 搜索下拉面板(小红书风格:历史标签+猜你想搜+编辑模式删除+按用户ID隔离localStorage) ✅
ForumSearch.vue           ← 🆕 纯结果页（笔记/用户Tab+瀑布流+高亮，无重复搜索框） ✅
api/forum.js              ← searchPost/getHotSearch 2个方法 ✅
router/index.js           ← /forum/search路由 ✅
```

**7.6 检查清单:**
- [x] SearchServiceImpl 实现搜索（LIKE+分页） ✅
- [x] SearchServiceImpl 实现热搜（ConcurrentHashMap缓存1小时） ✅
- [x] SearchServiceImpl @Async异步写 tb_search_log ✅
- [x] SearchServiceImpl 图片JSON解析修复（split→fastjson JSON.parseArray，兼容base64） ✅
- [x] ForumLayout.vue 搜索下拉面板（小红书风格：历史胶囊标签+编辑模式×删除+清空/完成+猜你想搜编号列表） ✅
- [x] ForumLayout.vue 搜索历史按用户ID隔离localStorage（forum_search_history_{uid}） ✅
- [x] ForumSearch.vue 纯结果页（笔记/用户Tab+瀑布流+关键词高亮） ✅
- [x] api/forum.js 2个方法 ✅
- [x] router/index.js 新增 /forum/search 路由 ✅

---

#### 7.7 后台管理 ✅ (2026-04-07完成)
**职责:** ForumManage.vue 单页面四Tab（帖子审核/管理/评论管理/统计），管理员端8个接口。

**新增接口 (8个):**
```
GET    /api/admin/post/list                帖子列表（分页+状态+关键词筛选）
POST   /api/admin/post/audit/{id}          审核（body: {action: approve/reject, reason}）
POST   /api/admin/post/top/{id}            置顶/取消置顶（幂等）
DELETE /api/admin/post/{id}                删除帖子（status=4，作者仍能看到"已被管理员删除"提示）
GET    /api/admin/post/comment/list        评论列表（分页+按帖子筛选）
DELETE /api/admin/post/comment/{id}        删除评论（@TableLogic 软删）
GET    /api/admin/post/stats               论坛数据统计（今日/总数/状态分布/7天趋势）
GET    /api/admin/post/pending/count       待审核数量（导航角标用）
```

**四个Tab:**
```
Tab1 帖子审核: 待审核列表(status=1) → 通过(status=2+通知) / 拒绝(status=3+原因+通知)
Tab2 帖子管理: 全部帖子 + 状态筛选 + 关键词搜索 + 置顶/删除操作
Tab3 评论管理: 评论列表 + 按帖子ID/关键词筛选 + 删除不当评论(@TableLogic)
Tab4 数据统计: 7个数字卡片（今日发帖/评论/活跃用户 + 总数 + 待审核）
              + ECharts 帖子状态分布饼图 + 近7天发帖&评论趋势折线图
```

**新增/修改文件:**
```
后端 (module/admin/):
  dto/PostQueryDTO.java               ← 🆕 帖子列表查询 DTO（status/keyword/分页）
  dto/PostCommentQueryDTO.java        ← 🆕 评论列表查询 DTO（postId/keyword/分页）
  vo/ForumStatsVO.java                ← 🆕 论坛数据统计 VO
  mapper/AdminPostMapper.java         ← 🆕 后台帖子查询/统计专用 Mapper（8 方法）
  mapper/AdminPostCommentMapper.java  ← 🆕 后台评论查询/统计专用 Mapper（6 方法）
  service/AdminPostService.java       ← 🆕 8 方法接口
  service/impl/AdminPostServiceImpl.java ← 🆕 8 方法实现（含 7 天趋势补齐）
  controller/AdminPostController.java ← 🆕 8 端点（写操作均加 @AdminLog）
resources/mapper/:
  AdminPostMapper.xml                 ← 🆕 8 条 SQL（list/count/stats/distribution/trend）
  AdminPostCommentMapper.xml          ← 🆕 6 条 SQL（含今日活跃用户 UNION 查询）
notification/helper/NotificationHelper.java ← 🔧 新增 2 方法
                                            sendPostApprovedNotification +
                                            sendPostRejectedNotification

前端:
  api/admin/forum.js                  ← 🆕 8 个 API 方法
  views/admin/ForumManage.vue         ← 🆕 四 Tab 后台管理页（~970 行）
  views/admin/AdminLayout.vue         ← 🔧 新增「论坛管理」菜单 + 待审核数角标 (60s 刷新)
  router/index.js                     ← 🔧 新增 /admin/forum 路由
```

**架构亮点:**
```
1. 后台 mapper 与 forum mapper 解耦：
   - forum/PostMapper / PostCommentMapper 仅保留用户视角查询（已发布、可见）
   - admin/AdminPostMapper / AdminPostCommentMapper 专注管理员视角（含所有状态）
   - 实体 CRUD（updateById/selectById/deleteById）仍走 forum 模块的 BaseMapper
2. 删除策略分层：
   - 帖子删除 → status=4（不走 @TableLogic），作者仍可见"已被管理员删除"
   - 评论删除 → @TableLogic 软删 + 帖子 commentCount-1
3. 7 天趋势在 Service 层补齐空白日期，保证 ECharts X 轴连续
4. 列表接口剥离 base64 images，仅返回 coverImage + imageCount，节省带宽
5. 待审核角标 60s 静默刷新，组件卸载时清理 timer
6. 审核通知采用 try/catch 包裹，通知失败不阻塞审核主流程
```

**7.7 检查清单:**
- [x] AdminPostServiceImpl 实现 8 个方法（含 7 天趋势补齐 + base64 剥离）
- [x] AdminPostController 8 个端点（写操作均加 @AdminLog）
- [x] AdminPostMapper / AdminPostCommentMapper 拆出至 admin 模块（与 forum 解耦）
- [x] PostQueryDTO / PostCommentQueryDTO / ForumStatsVO 创建
- [x] NotificationHelper 新增 sendPostApprovedNotification / sendPostRejectedNotification
- [x] ForumManage.vue 四 Tab（审核/管理/评论/统计 + ECharts 趋势/饼图）
- [x] AdminLayout.vue 新增菜单 + 待审核数角标（60s 刷新 + onBeforeUnmount 清理）
- [x] api/admin/forum.js 8 个方法
- [x] router 新增 /admin/forum
- [x] 待办：FORUM_POST_APPROVED / FORUM_POST_REJECTED 通知模板 INSERT SQL（见数据库表文档 7.7 节）

---

#### 7.7.x 后续修复与重新定位 ✅ (2026-04-08)

> 7.7 主体功能上线后的 4 个 Bug 修复 + 1 次架构重新定位。
> 起因：实测过程中发现通知跳转 404、通知铃铛计数翻倍、置顶按钮状态不变等问题；
> 同时用户反馈"小红书风格瀑布流下置顶视觉无效果"，决定将 is_top 重新定位为 8.x 推荐算法的加权信号。

**① 论坛通知模板入库 (2 条 → DB id 25/26)**
```sql
-- tb_notification_template
INSERT INTO tb_notification_template VALUES
  (25, 'FORUM_POST_APPROVED', 'forum', '帖子审核通过',
   '你的帖子《{postTitle}》已审核通过，已正式发布！', '/forum/list?postId={postId}', ...),
  (26, 'FORUM_POST_REJECTED', 'forum', '帖子审核未通过',
   '你的帖子《{postTitle}》审核未通过。原因：{reason}', '/forum/list?postId={postId}', ...);
```
模板变量 `{postTitle}/{reason}/{postId}` 由 NotificationHelper 注入；jumpUrl 指向 ForumList，
进入页面后由 `?postId=` query 参数自动打开详情弹窗。

**② Bug 修复 1: 通知点击 404**
- **现象:** 点击通知 → 跳转 `/forum/post/11` → 404
- **原因:** NotificationBell.vue 硬编码了不存在的路由
- **修复:** `NotificationBell.vue` case 'post' 改为 `item.jumpUrl || /forum/list?postId=${item.relatedId}`
- **配套:** `ForumList.vue` onMounted 中读取 `route.query.postId`，自动打开详情对话框

**③ Bug 修复 2: 通知铃铛计数翻倍**
- **现象:** 后端返回 `{total:5, mall:0, forum:5, ...}`，前端铃铛显示 10
- **原因:** `Object.assign(unreadCount, res.data)` 把 `total` 字段也写入 reactive 对象，
  随后 `Object.values().reduce((a,b)=>a+b)` 把 total 当作子模块再加了一次
- **修复:** `NotificationBell.vue` 把 `totalUnread` 由 computed 改为 ref，直接 `= data.total`；
  并把 Object.assign 改成显式逐字段赋值（mall/forum/course/location/recommend/ai/system）

**④ 新功能: 通知点击改为弹窗详情**
- **动机:** 列表项截断后看不到完整拒绝原因；router.push 体验割裂
- **方案:** `NotificationBell.vue` 新增 `el-dialog` 详情弹窗
  - 顶部 meta 行：模块标签 + 时间 + 已读状态
  - 正文区：完整 title + content（不再截断）
  - 底部「前往查看」按钮（仅 canJump 时显示）
- **辅助函数:** `buildTargetRoute / canJump / jumpFromDetail / moduleTagType / moduleLabel`

**⑤ Bug 修复 3: isTop 按钮点击后状态不变**
- **现象:** 点击「置顶」toast 显示成功，但按钮文本和样式不切换
- **根因:** **MySQL JDBC `tinyInt1isBit=true` 默认行为**
  - tinyint(1) 列在 `Map<String,Object>` 结果中被解析为 **Boolean**，而非 Integer
  - 实体类查询无此问题（MyBatis 会自动转 Integer 字段）
  - 后台列表走 Map 返回 → 前端拿到的是 `isTop: true/false` 而非 `1/0`
- **修复:** `ForumManage.vue` 把 3 处 `row.isTop === 1` 改为真值判断 `row.isTop`
  （isHot 同理），并加注释说明 JDBC 行为
- **教训:** 凡是 MyBatis 走 Map 返回的 tinyint(1) 列，前端都要按 Boolean 处理

**⑥ 架构重新定位: 置顶 → 管理员推荐 (重大变更)**

> 用户原话："我觉得这个功能定位很尴尬 因为我们的论坛列表并不是传统的排列 置顶好像没什么效果"
> "改成推荐 融入我们第8模块要开发的推荐算法 用户是看不到的"

**背景排查:**
- `PostMapper.xml` 用户列表 ORDER BY 确实有 `p.is_top DESC`
- 但 `PostCard.vue` 完全无视觉差异（无角标/无置顶图标/无背景色）
- 且小红书瀑布流是分散布局，传统"顶部置顶"无可视位置

**新定位:** is_top 字段语义重命名为「管理员推荐信号」
- 用户端：列表 / 卡片 / 详情都**不展示**、不感知
- 管理员端：「加入推荐 / 取消推荐」按钮（橙色 = 已推荐，灰色 = 未推荐）
- 8.x 推荐算法：读取 is_top=1 作为 hot_score 的额外加权因子（TODO）
- DB 字段名暂不 rename，避免 migration 风险

**变更文件清单 (8 处):**
```
后端:
  resources/mapper/PostMapper.xml             ← 删除 user list 的 is_top DESC 排序
                                                + 添加 7.7 重新定位说明注释
  module/forum/entity/Post.java               ← isTop 字段注释加"字段历史"段落
  module/admin/service/AdminPostService.java  ← toggleTopPost → toggleRecommendPost
                                                + 完整 JavaDoc + 接口头注释更新
  module/admin/service/impl/AdminPostServiceImpl.java
                                              ← 实现方法重命名 + 异常文案改"推荐"
                                                + log 字段 isTop= → isRecommend=
  module/admin/controller/AdminPostController.java
                                              ← URL /top/{id} 保留（避免前端改动）
                                                + @ApiOperation 改"加入/取消推荐"
                                                + @AdminLog 改"切换论坛帖子推荐"
                                                + 调用 toggleRecommendPost

前端:
  api/admin/forum.js                          ← 文件头方法 3 描述 + toggleTopPost JSDoc
                                                重写为"加入/取消推荐"（函数名保留）
  views/admin/ForumManage.vue                 ← 标题列 tag: 置顶 → 推荐(warning)
                                                操作按钮文案/颜色: 置顶 → 加入推荐/取消推荐
                                                handleToggleTop toast: 推荐相关文案
                                                Tab2 头部注释更新
  components/NotificationBell.vue             ← 大重构（详情弹窗 + 计数修复，见 ②③④）
```

**8.x 推荐算法 TODO:**
```java
// 推荐算法应当读取 is_top=1 的帖子，将其 hot_score 乘以 ~1.5x ~ 2x
// 作为额外加权因子，使「管理员推荐」帖子在 feed 中获得更多曝光机会
double finalScore = post.getHotScore() * (post.getIsTop() == 1 ? 1.8 : 1.0);
```

**⑦b 评论级联删除 + 列标题被浏览器误译 (2026-04-08 追加)**

- **问题 1: 列标题"身份证"** — Tab3 评论列表的「编号」列实际定义是 `label="ID"`，
  但 Chrome 中文环境下会对孤立英文 token "ID" 做单词级自动翻译，渲染成"身份证"。
  非代码 Bug，是浏览器行为。
  - **修复:** `ForumManage.vue` 三个 Tab 的 `label="ID"` 统一改为 `label="编号"`，
    纯中文不会被翻译。
- **问题 2: 删除父评论时子回复未级联删除** — 原 `AdminPostServiceImpl.deleteComment`
  只软删目标评论本身，顶级评论下的子回复仍然挂在帖子里"成为孤儿"。
  - **修复:** `deleteComment` 改造（`module/admin/service/impl/AdminPostServiceImpl.java`）
    1. 先按原逻辑软删目标评论
    2. 判断 `parentId == 0`（顶级评论）时，用 QueryWrapper 查
       `parent_id = commentId AND deleted = 0` 的所有子回复，逐条 `deleteById` 软删
    3. 帖子 `commentCount` 改为按实际删除条数 `-deletedCount`（原固定 -1）
    4. 日志记录"含级联子回复共 N 条"
  - **设计依据:** schema 中 `parent_id` 始终指向顶级评论 ID（两级评论结构），
    不存在嵌套回复，单次 `parent_id = commentId` 查询即可扫到全部子回复，无需递归。
  - **可恢复性:** 子回复同样走 `@TableLogic` 软删，未来需要恢复时 `deleted=0` 即可。

**⑦ ForumManage.vue Tab CSS 修复**
- **问题:** Element Plus 默认 tab 文字为深灰，在管理后台深紫色背景下不可见
- **修复:** SCSS 覆盖
  - `.el-tabs__item` color: `rgba(255,255,255,0.7)`
  - `.is-active` color: `#67c5ff`
  - `.el-tabs__active-bar`: 3px 圆角
  - `.el-tabs__nav-wrap::after`: 半透明分隔线

**7.7.x 新增检查清单:**
- [x] DB 写入 FORUM_POST_APPROVED / FORUM_POST_REJECTED 模板（id 25/26）
- [x] 通知点击 404 修复（NotificationBell + ForumList postId 接收）
- [x] 通知铃铛计数翻倍修复（totalUnread 改 ref）
- [x] 通知详情弹窗（el-dialog + 模块标签 + 前往查看按钮）
- [x] isTop tinyint Boolean Bug 修复（3 处 === 1 → 真值）
- [x] 置顶 → 推荐 重新定位（8 个文件）
- [x] ForumManage Tab CSS 暗色背景适配
- [x] 评论列表列标题 ID → 编号（绕开 Chrome 单词翻译）
- [x] 删除顶级评论时级联软删全部子回复（commentCount 同步按 N 条扣减）
- [ ] 8.x 待办：推荐算法读取 is_top 作为 hot_score 加权因子

---

#### 7.8 通知集成+热度+跨模块联动（阶段二）✅ (2026-04-08完成)
**职责:** 9种论坛通知、ForumScheduler热度定时任务、AI识别分享+课程讨论跨模块入口。

**NotificationType.java 新增9个枚举:**
```java
FORUM_POST_LIKED, FORUM_POST_COMMENTED, FORUM_COMMENT_REPLIED,
FORUM_POST_COLLECTED, FORUM_MENTIONED, FORUM_POST_APPROVED,
FORUM_POST_REJECTED, FORUM_USER_FOLLOWED, FORUM_POST_TRENDING
```

**NotificationHelper.java 新增9个方法:**
```java
sendPostLikedNotification(authorId, likerId, postTitle, postId)       // 防自通知
sendPostCommentedNotification(authorId, commenterId, ..., postId)     // 防自通知
sendCommentRepliedNotification(repliedUserId, replierId, ..., postId) // 防自通知
sendPostCollectedNotification(authorId, collectorId, ..., postId)     // 防自通知
sendMentionedNotification(mentionerId, mentionedUserIds, ..., postId) // ⭐扩展功能
sendPostApprovedNotification(userId, postTitle, postId)
sendPostRejectedNotification(userId, postTitle, reason, postId)
sendUserFollowedNotification(followedId, followerId)
sendPostTrendingNotification(authorId, postTitle, postId)
// 工具方法: truncate(s, maxLen) + getNickname(userId)
```

**ForumScheduler.java 热度定时任务:**
```java
@Scheduled(cron = "0 0 * * * ?")  // 每小时
public void calcHotScores() {
    // 1. 查 status=2 + 近7天帖子
    // 2. score = (likes×1 + comments×2 + collects×3) / Math.pow(daysSince+2, 1.5)
    // 3. 批量UPDATE hot_score, is_hot
    // 4. is_hot从0→1的帖子 → sendPostTrendingNotification（只通知一次）
}
```

**跨模块联动:**
```
联动1: RecognitionResult.vue → 「分享到论坛」→ /forum/publish?recognitionId={id}
       ForumPublish.vue 自动预填: 标注图片+天体列表+tags:["AI识别"]
联动2: CourseDetail.vue → 「去论坛讨论」→ /forum/list?tag={courseTag}
```

**7.8 检查清单:**
- [x] NotificationType.java 7个论坛枚举(POST_LIKED/POST_COMMENTED/COMMENT_REPLIED/POST_COLLECTED/MENTIONED/POST_APPROVED/POST_REJECTED)+
       重命名 3 个枚举对齐 helper type 字符串(USER_FOLLOWED/MENTIONED/POST_TRENDING)
- [x] NotificationHelper.java 新增 6 个方法 (sendPostLiked/Commented/CommentReplied/PostCollected/PostTrending/Mentioned)
       + 复用 7.5 sendUserFollowedNotification + 7.7 sendPostApproved/Rejected = 共9个论坛通知方法
- [x] 执行 7 条通知模板 INSERT SQL (id 27-33,加上 7.7 的 25/26 共9条)
       SQL文件: src/main/resources/sql/7.8_forum_notification_templates.sql
       注意: mysql -e 必须加 --default-character-set=utf8mb4 否则中文 1406 错误
- [x] PostServiceImpl: likePost/collectPost 注入 NotificationHelper,新点赞/收藏触发通知
- [x] CommentServiceImpl: addComment 顶级评论→帖子作者通知,子回复→被回复人通知
       (被回复人优先 dto.replyToUserId,回退 parentComment.userId)
- [x] ForumScheduler.calcHotScores 热度计算+热门通知
       公式: (likes×1 + comments×2 + collects×3) / Math.pow(daysSince+2, 1.5)
       阈值: HOT_SCORE_THRESHOLD = 3.0 (demo级,生产可调到 50+)
       一生只发一次: oldIsHot==1 即保持 1,不会降回 0
- [x] RecognitionResult.vue 新增「分享到论坛」按钮 → /forum/publish 携带 recognitionId
       预填: 标题(识别到的天体)+正文(完整列表+坐标)+图片([resultImageUrl])+tags(["AI识别","星空"])
- [x] ForumPublish.vue onMounted 扩展: isEdit OR recognitionId 都从 query 加载预填
- [x] CourseDetail.vue goToForum 改用 parseTags(course.tags)[0] 作为 ?tag= 参数
       (原先传 courseId 但 ForumList 不读取,无效跳转)

**已发现并修复的隐藏 Bug:**
- FollowServiceImpl 在 7.5 已经调用 `sendUserFollowedNotification`，但当时通知模板从未入库,
  导致从 7.5 到 7.7 期间所有"被关注"通知都静默失败。本次 7.8 INSERT SQL 补齐
  FORUM_USER_FOLLOWED 模板,follow 通知现已能正常下发。

---

**⭐ 扩展功能（@提及，优先级低，时间不够可跳过）:**
```
GET /api/user/search → 用户搜索（@提及下拉候选用）
前端@搜索下拉 + 后端解析@用户名 + 批量FORUM_MENTIONED通知
```

---

**与推荐系统联动（8.0+8.1 已实现 ✅）:**
```
tb_post.tags       → 帖子内容相似度（jaccardSimilarity）✅ RecommendServiceImpl.postContentSimilarity()
tb_post_browse_log → 用户浏览行为（Redis SETNX 30分钟去重）✅ logPostBrowse()
hot_score          → 热门帖子兜底（冷启动）✅ getPostRecommend() hot_score DESC 兜底
```

---

**第7模块数据统计:**
```
接口数:       +27（用户端19 + 后台8，@提及1个为扩展）
数据库表:     +6张（砍掉tb_post_image/tb_post_tag）
后端模块:     +1 (module/forum/)
前端页面:     +7 (ForumLayout/List/Detail/Publish/Search/UserProfile/ForumManage)
前端组件:     +2 (PostCard/PostWaterfall)
前端API:      +2 (api/forum.js / api/admin/forum.js)
通知模板:     +9种
总工时:       10天（阶段一7天可演示 + 阶段二3天完善）
```

---

### ✅ 8. 推荐系统模块 🔥 核心创新点
**开发时间:** 第16周（5.5天）
**状态:** ✅ 8.0✅基础建设 8.1✅核心算法 8.2✅商品主场景 8.3✅跨模块 8.4✅定时任务+通知集成
**优先级:** 高 ⭐⭐⭐⭐⭐
**论文对应:** 基于内容相似度推荐算法的天文器材交流与交易平台设计与实现
**UI 表现:** 纯曝光式推荐（在现有页面插入「为你推荐/相关推荐」区块），不做独立推荐中心

**设计原则:**
```
- 论文题目锚点：算法核心是「标签 Jaccard + 多维特征加权」，纯 Java 实现，无外部模型
- 混合策略：内容相似度(60%) + Item-based 协同过滤(30%) + 兴趣标签冷启动(10%)
  （相较旧版 CF 权重从 40% 下调到 30%，原因：demo 用户基数小，CF 矩阵稀疏，内容相似度更稳）
- 跨模块亮点：AI识别→课程 / 完课→下一门 / 签到→器材 / 帖子浏览→相关帖子，四路联动
- 埋点优先：商品浏览 + 帖子浏览双埋点写库，Redis SETNX 30 分钟去重防刷
- 缓存策略：首页/相似/推荐结果 30 分钟-1 小时 Redis 缓存 + 每周清理 90 天前埋点
- 冷启动三级兜底：兴趣标签 → 热门排序（sales×0.6 + is_recommend×0.4）→ 随机抽样
- 推荐通知谨慎：推荐系统本身是曝光渠道，PRODUCT_RECOMMEND/COURSE_RECOMMEND 通知仅在
  「AI 识别完成异步推课程」「价格变动命中兴趣标签」两种离散事件触发，避免骚扰
- 演示导向：demo 模式允许 CF 直接现算（不走预计算矩阵），答辩时口述"生产环境启用预计算"
```

**分节依赖顺序（严格遵守）:**
```
8.0 基础建设 → 建 2 张浏览日志表 + Redis 依赖 + 兴趣标签入口 + module/recommend/ 骨架
  ↓
8.1 核心推荐算法 → Service 层纯函数（Jaccard / 内容相似度 / CF / 冷启动），无接口
  ↓ (以下 3 节可并行)
8.2 商品推荐主场景       → 4 接口（browse 埋点 + home + similar + cart）+ 3 前端区块
8.3 跨模块联动推荐       → 5 接口（识别→课程 / 完课→下一门 / 签到→商品 / 帖子埋点 / 帖子推荐）
8.4 定时任务+通知集成+答辩演示 → Scheduler + 2 条通知模板 + 答辩流程固化
```

**⚠️ 已识别的合理性问题（规划阶段需要先对齐）:**
```
① 通知集成触发时机：推荐曝光本身不通知，只在 AI 识别完成后异步推 1 条 COURSE_RECOMMEND
   + 价格变动命中兴趣标签时推 1 条 PRODUCT_RECOMMEND。其他场景不发通知，避免骚扰。
② ForumList 新 Tab 命名：现有"推荐(all)"与新的"个性化推荐"冲突 → 将新 Tab 命名为
   「为你推荐」，现有"推荐(all)"改名为"发现"（需同步改 ForumList.vue Tab 文案）。
③ CF 权重下调至 30%：demo 数据少 CF 矩阵稀疏，答辩时说明"权重可配置，数据增长后重调"。
④ tb_user.interest_tags 入口：必须在 8.0 阶段给 AccountSettings.vue 加兴趣标签选择器，
   否则冷启动分支永远拿不到 tags，整个冷启动逻辑形同虚设。
⑤ 签到→商品混合策略：altitude / light_pollution_level 硬映射作为"前置过滤"，之后再在候选集内
   按 tags Jaccard 排序，保持"内容相似度"主线一致（论文题目不偏离）。
⑥ 识别→课程空结果兜底：machine_tags 为空 或 无匹配课程 → 返回 tb_course_progress COUNT(*) 倒序 Top6
   （报名人数代理，tb_course 无 view_count 字段，不新增冗余列，直接 GROUP BY course_id 聚合）。
⑦ CF 预计算 vs 现算：第一版直接现算（用户少，Service 方法内计算），Scheduler 预计算矩阵
   作为"生产优化"保留但 @ConditionalOnProperty 默认关闭，避免过度工程。
⑧ 答辩"推荐是否有效"话术：tb_recommend_record 必做（由规划不实现改为必做）。
   所有推荐接口返回结果时异步写入曝光记录（recommend_type/target_id/algorithm/position/score），
   前端点击推荐卡片时调 /api/recommend/click 回写 is_clicked=1 + click_time。
   答辩时直接 SELECT COUNT + 点击率一句话回答老师"怎么证明推荐有效"，不做 A/B test 不做离线指标。
⑨ 算法复杂度就按"教材级"：学校对算法深度要求不高，Jaccard + 加权和够用，
   答辩话术把创新点落在"跨模块异构信号编排"而非"算法本身"。
```

**推荐信号全景图（跨模块联动核心）:**
```
数据来源                            →  推荐目标                 归属
───────────────────────────────────────────────────────────────
tb_browse_log（商品浏览记录，必做） →  CF 候选 + 相似商品        8.2
tb_order_item（购买记录）           →  CF 权重 × 3（最高）       8.2
tb_product_favorite（收藏记录）     →  CF 权重 × 2               8.2
tb_user.interest_tags（兴趣标签）   →  冷启动首页推荐            8.2
───────────────────────────────────────────────────────────────
tb_recognition.machine_tags         →  推荐相关课程 🌟           8.3 (跨模块1)
tb_course_progress（完课记录）      →  推荐下一门课 🌟           8.3 (跨模块2)
tb_observation_spot 海拔/光污染     →  推荐适合器材 🌟           8.3 (跨模块3)
tb_post_browse_log（帖子浏览，必做）→  推荐相关帖子 🌟           8.3 (跨模块4)
hot_score（7.8 已上线）             →  帖子冷启动兜底            8.3
```

**数据统计变化:**
```
接口数:       +10（商品埋点1 + 商品推荐3 + 跨模块4 + 帖子埋点1 + 推荐点击回写1）
数据库表:     +3 张已创建 ✅（tb_browse_log / tb_post_browse_log / tb_recommend_record）2026-04-11
              +1 张可选（tb_recommend_config 仅 CF 权重持久化时启用，demo 默认不创建）
              ⭐ tb_recommend_record 由"规划不实现"改为"必做"，用于答辩时现场展示
                 曝光量/点击率数据，不做 A/B test 也能用一句话回答"推荐是否有效"
后端模块:     +1 ✅（module/recommend/ 17个Java文件 + 1个XML）
后端定时任务: +1 ✅（RecommendScheduler，每周清理 90 天前埋点，CF 矩阵预计算默认关闭）
前端页面:     +0（纯改造现有 9 个页面，8.2开始）
前端改造:     Home / ProductDetail / CartPage / RecognitionResult / CourseDetail /
              ObservationMap / ForumDetail / ForumList / AccountSettings 共 9 处（AccountSettings已改 ✅，其余待8.2-8.4）
前端API:      +1 ✅（api/recommend.js 10个方法）
通知模板:     +2 条（PRODUCT_RECOMMEND / COURSE_RECOMMEND，离散事件触发，待8.3实现）
预计工时:     5.5 天（8.0+8.1已完成约2天 ✅，剩余8.2-8.4约3.5天）
```

---

#### 8.0 基础建设 ✅ (2026-04-11完成)
**职责:** 建 3 张表（2 张浏览日志 + 1 张推荐曝光记录）+ Redis 依赖与配置 + AccountSettings 兴趣标签入口 + module/recommend/ 全部骨架文件。必须全部完成才能开始 8.1~8.4 任意节。

**数据库（3 张必做表）:**
```sql
-- ① tb_browse_log 商品浏览记录表
-- Redis 去重: SETNX "browse:dedup:{userId}:{productId}" TTL=1800s
-- CF 权重: 浏览(1) + 收藏(2) + 购买(3)
-- 定时清理: 每周一凌晨 3 点 DELETE WHERE browse_time < 90 天前
CREATE TABLE `tb_browse_log` (
  `id`          bigint(20)  NOT NULL AUTO_INCREMENT,
  `user_id`     bigint(20)  NOT NULL COMMENT '📌关联tb_user.id',
  `product_id`  bigint(20)  NOT NULL COMMENT '📌关联tb_product.id',
  `category_id` bigint(20)  DEFAULT NULL COMMENT '分类ID(冗余,加速按类推荐)',
  `source`      varchar(20) DEFAULT 'detail' COMMENT 'detail/list',
  `browse_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_time` (`user_id`, `browse_time`),
  KEY `idx_browse_time` (`browse_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品浏览记录(推荐系统CF数据源)';

-- ② tb_post_browse_log 帖子浏览记录表
-- Redis 去重: SETNX "post:browse:dedup:{userId}:{postId}" TTL=1800s
-- 定时清理: 与 tb_browse_log 同步每周清理 90 天前
CREATE TABLE `tb_post_browse_log` (
  `id`          bigint(20)  NOT NULL AUTO_INCREMENT,
  `user_id`     bigint(20)  NOT NULL COMMENT '📌关联tb_user.id',
  `post_id`     bigint(20)  NOT NULL COMMENT '📌关联tb_post.id',
  `browse_time` datetime    NOT NULL,
  `duration`    int(11)     DEFAULT 0 COMMENT '浏览时长(秒,可选)',
  `source`      varchar(50) DEFAULT NULL,
  `create_time` datetime    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_time` (`user_id`, `browse_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子浏览记录(推荐系统内容相似度数据源)';

-- ③ tb_recommend_record 推荐曝光与点击记录表
-- 用途: 答辩时现场展示曝光总量 / 点击率，无需做 A/B test
-- 写入: 所有 /api/recommend/*/home|similar|cart 等接口返回前异步批量 INSERT
-- 点击: 前端点击推荐卡片 → POST /api/recommend/click 回写 is_clicked + click_time
CREATE TABLE `tb_recommend_record` (
  `id`              bigint(20)  NOT NULL AUTO_INCREMENT,
  `user_id`         bigint(20)  NOT NULL COMMENT '📌关联tb_user.id',
  `recommend_type`  varchar(50) NOT NULL COMMENT 'product/course/post',
  `target_id`       bigint(20)  NOT NULL COMMENT '被推荐的目标ID',
  `algorithm`       varchar(50) NOT NULL COMMENT 'content/collaborative/hot/coldstart',
  `score`           double      DEFAULT NULL COMMENT '推荐得分(可空)',
  `position`        int(11)     DEFAULT NULL COMMENT '推荐位置(1-10)',
  `is_clicked`      tinyint(4)  DEFAULT 0 COMMENT '0-未点击 1-已点击',
  `click_time`      datetime    DEFAULT NULL,
  `create_time`     datetime    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_recommend_type` (`recommend_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐曝光与点击记录(答辩数据支撑)';
```

**Redis 依赖与配置:**
```xml
<!-- pom.xml 新增 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```yaml
# application.yml 新增
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 10
        max-idle: 5
        min-idle: 1

# 推荐系统配置（方便答辩时现场调权重）
recommend:
  content-weight: 0.6     # 内容相似度权重
  cf-weight: 0.3          # 协同过滤权重
  coldstart-weight: 0.1   # 冷启动权重
  cf-precompute: false    # demo 模式直接现算；生产环境改 true
  post-hot-fallback: true # 帖子推荐结果不足时用 hot_score 兜底
```

**后端骨架文件（module/recommend/）:**
```
module/recommend/
├─ controller/RecommendController.java     (10 端点骨架，含 POST /click)
├─ service/
│   ├─ RecommendService.java               (接口)
│   ├─ CfRecommendService.java             (接口,协同过滤独立)
│   └─ impl/
│       ├─ RecommendServiceImpl.java       (内容相似度 + 主调度 + 曝光落表)
│       └─ CfRecommendServiceImpl.java     (CF 矩阵构建 + Redis 存取)
├─ task/RecommendScheduler.java            (每周清理 browse_log，CF 预计算默认关闭)
├─ mapper/
│   ├─ BrowseLogMapper.java                (继承 BaseMapper)
│   ├─ PostBrowseLogMapper.java            (继承 BaseMapper)
│   └─ RecommendRecordMapper.java          (继承 BaseMapper + 统计 SQL)
├─ dto/
│   ├─ BrowseLogDTO.java                   (userId/productId/categoryId/source)
│   ├─ PostBrowseLogDTO.java               (userId/postId/source)
│   └─ RecommendClickDTO.java              (recommendType/targetId)
├─ vo/
│   ├─ RecommendProductVO.java             (id/productName/mainImage/price/reason/score)
│   └─ RecommendPostVO.java                (id/title/coverImage/hotScore/tags/authorNickname)
└─ entity/
    ├─ BrowseLog.java                      (@TableName tb_browse_log)
    ├─ PostBrowseLog.java                  (@TableName tb_post_browse_log)
    └─ RecommendRecord.java                (@TableName tb_recommend_record)
```

**前端骨架:**
```
api/recommend.js                  ← 新建,9 个方法对应 9 个接口
views/user/AccountSettings.vue    ← 新增「兴趣标签」Tab(8.0 必须做,否则冷启动失效)
```

**AccountSettings 兴趣标签入口（8.0 关键补丁）:**
```
预设标签池: ["深空摄影","行星摄影","星云","星团","星座观测","日月食",
            "天文摄影后期","入门望远镜","双筒望远镜","大口径望远镜",
            "便携设备","天文相机","目镜","赤道仪","极轴镜"]
交互: 多选 chips (最多 8 个) + 自定义输入
保存: PUT /api/user/profile (interest_tags 字段已在 tb_user 存在,直接复用现有接口)
```

**8.0 检查清单:**
- [x] 执行 3 张建表 SQL（tb_browse_log / tb_post_browse_log / tb_recommend_record）✅ 2026-04-11
- [x] pom.xml 新增 spring-boot-starter-data-redis 依赖 ✅
- [x] application.yml 新增 spring.redis 配置 ✅
- [x] application.yml 新增 recommend.* 推荐权重配置（content-weight:0.6/cf-weight:0.3/coldstart-weight:0.1）✅
- [x] 本地启动 Redis（Windows: D:/Redis/Redis-8.0.2-Windows-x64-msys2-with-Service/redis-server.exe）✅
- [x] 创建 module/recommend/ 全部文件（Controller + 2 Service + 2 Impl + Scheduler + 3 Mapper + 3 DTO + 2 VO + 3 Entity = 17个Java文件 + 1个XML）✅
- [x] JwtInterceptor OPTIONAL_AUTH_LIST 新增 5 条推荐系统路径（home/similar/spot/post/recognition）✅
- [x] AccountSettings.vue 兴趣标签 presetTags 扩展为15项（与商品标签体系对齐）✅
- [x] 确认 @MapperScan 能扫到 recommend.mapper ✅（主类 @MapperScan("com.astronomy.mall.module.*.mapper")）
- [x] mvn compile 编译通过 ✅

---

#### 8.1 核心推荐算法 ✅ (2026-04-11完成)
**职责:** 实现 Service 层的算法函数（Jaccard / 内容相似度 / CF / 冷启动），无对外接口，被 8.2/8.3 复用。
**论文核心章节对应:** 本节代码即论文第 4/5 章 "推荐算法设计与实现"

**算法说明:** 本系统采用标签 Jaccard 相似度 + 多维特征加权求和实现内容推荐，借鉴向量化思想将商品/帖子特征映射为多维向量后计算相似度，无需加载外部模型。论文中可描述为「基于特征向量加权相似度的内容推荐算法」。

**① 内容相似度算法（RecommendServiceImpl）:**
```java
// 1. 标签 Jaccard 相似度
public double jaccardSimilarity(String tags1Json, String tags2Json) {
    Set<String> set1 = new HashSet<>(JSON.parseArray(tags1Json, String.class));
    Set<String> set2 = new HashSet<>(JSON.parseArray(tags2Json, String.class));
    if (set1.isEmpty() && set2.isEmpty()) return 0;   // 无标签对不算相似
    long intersection = set1.stream().filter(set2::contains).count();
    long union = Stream.concat(set1.stream(), set2.stream()).distinct().count();
    return union == 0 ? 0 : (double) intersection / union;
}

// 2. 价格区间相似度（归一化,越近越相似）
public double priceSimilarity(BigDecimal p1, BigDecimal p2) {
    double maxPrice = 20000.0;
    double diff = Math.abs(p1.doubleValue() - p2.doubleValue());
    return Math.max(0, 1 - diff / maxPrice);
}

// 3. 商品综合内容相似度（加权求和）
public double contentSimilarity(Product p1, Product p2) {
    double tagSim      = jaccardSimilarity(p1.getTags(), p2.getTags());
    double categorySim = p1.getCategoryId().equals(p2.getCategoryId()) ? 1.0 : 0.0;
    double priceSim    = priceSimilarity(p1.getPrice(), p2.getPrice());
    // 权重: 标签 0.5 + 类别 0.3 + 价格 0.2
    return tagSim * 0.5 + categorySim * 0.3 + priceSim * 0.2;
}
```

**② Item-based 协同过滤（CfRecommendServiceImpl）:**
```java
// 相似度公式: sim(i,j) = |N(i) ∩ N(j)| / sqrt(|N(i)| × |N(j)|)
// N(i) = 与商品 i 有行为的用户集合（购买权重 3 + 收藏权重 2 + 浏览权重 1）
// demo 模式: 每次请求现算 Top20 相似商品（用户少无性能压力）
// 生产模式: RecommendScheduler 每 6 小时预计算矩阵存 Redis (recommend.cf-precompute=true)

public Map<Long, Double> computeCfSimilar(Long productId, int topN) {
    // 1. 查所有用户对 productId 的行为分数（browse + favorite + order）
    // 2. 查这些用户对其他商品的行为分数
    // 3. 构建 Map<otherProductId, coBehaviorCount>
    // 4. 按公式计算相似度
    // 5. 返回 Top N 相似商品
}
```

**③ 冷启动三级兜底:**
```java
public List<Product> coldStart(Long userId, int limit) {
    // Level 1: 有 interest_tags → 用户标签 LIKE 商品 tags,按 sales 倒序
    User user = userMapper.selectById(userId);
    if (StringUtils.isNotBlank(user.getInterestTags())) {
        List<String> tags = JSON.parseArray(user.getInterestTags(), String.class);
        List<Product> matched = productMapper.selectByTagsAny(tags, limit);
        if (!matched.isEmpty()) return matched;
    }
    // Level 2: 按 (sales × 0.6 + is_recommend × 0.4) 综合评分
    List<Product> hot = productMapper.selectHotProducts(limit);
    if (!hot.isEmpty()) return hot;
    // Level 3: 随机抽 limit 个（极端兜底）
    return productMapper.selectRandom(limit);
}
```

**④ 帖子内容相似度（复用 jaccardSimilarity）:**
```java
public double postContentSimilarity(Post p1, Post p2) {
    return jaccardSimilarity(p1.getTags(), p2.getTags());
    // 帖子不需要价格/类别维度,只看标签集合相似
}
```

**⑤ 首页混合推荐融合公式:**
```java
// finalScore = contentSim × 0.6 + cfSim × 0.3 + coldStartBoost × 0.1
// 其中:
//   contentSim 来自用户近 30 条浏览商品 tags 并集与候选商品 tags 的 Jaccard
//   cfSim      来自 CF 矩阵（demo 现算）的 Top 相似商品得分
//   coldStartBoost 对 interest_tags 命中的候选商品加权 0.1
```

**8.1 检查清单:**
- [x] RecommendServiceImpl 实现 jaccardSimilarity / priceSimilarity / contentSimilarity / postContentSimilarity ✅
- [x] RecommendServiceImpl 实现 coldStart 三级兜底（interest_tags → 热门 → 随机）✅
- [x] RecommendServiceImpl 实现 getHomeRecommend 融合公式（内容 0.6 + CF 0.3 + 冷启动 0.1）✅
- [x] RecommendServiceImpl 内嵌 selectByTagsAny / selectHotProducts / selectRandom 3 个查询方法（直接用 QueryWrapper 实现，无需额外 Mapper SQL）✅
- [x] CfRecommendServiceImpl 实现 computeCfSimilar（demo 现算 Top20，余弦相似度）✅
- [x] CfRecommendServiceImpl 实现 buildUserBehavior（聚合 browse×1 + favorite×2 + order×3 三路行为）✅
- [x] mvn compile 编译通过，算法函数签名与文档伪代码一致 ✅
- [x] 额外实现：getPostRecommend / getSimilarProducts / getCartRecommend / logProductBrowse / logPostBrowse / recordClick / saveExposureRecords ✅
- [x] 额外实现：RecommendController 10个端点（含 POST browse/post-browse/click + GET 7个推荐场景）✅

---

#### 8.2 商品推荐主场景 ✅ (2026-04-14 完成)
**职责:** 商品浏览埋点 + 首页「猜你喜欢」+ 详情页「相关商品」+ 购物车「为你推荐」。

**新增接口 (5 个):**
```
POST /api/recommend/browse                        记录商品浏览(埋点)
  入参: { productId, categoryId, source: "detail" }
  逻辑: Redis SETNX "browse:dedup:{userId}:{productId}" TTL=1800s
        首次命中 → INSERT tb_browse_log; 重复 → 直接返回 Result.ok()
  返回: Result<Void>

GET  /api/recommend/product/home?limit=10         首页猜你喜欢
  逻辑: 登录用户 → 混合推荐 (内容 60% + CF 30% + 冷启动 10%)
        未登录   → 热门商品 (sales × 0.6 + is_recommend × 0.4)
  Redis: recommend:product:home:{userId} TTL=30min
  返回: List<RecommendProductVO>
  ⭐ 接口返回前异步批量 INSERT tb_recommend_record（曝光落表）

GET  /api/recommend/product/similar/{productId}?limit=6  详情页相关商品
  逻辑: 取目标商品 → 遍历同类别商品 → contentSimilarity 倒序 Top6
        排除自身 + 已下架商品
  Redis: recommend:product:similar:{productId} TTL=1h
  返回: List<RecommendProductVO>
  ⭐ 同样异步落表 tb_recommend_record（recommend_type='product', algorithm='content'）

GET  /api/recommend/product/cart?limit=6          购物车为你推荐
  逻辑: 取购物车商品 tags 并集 → 按 tags Jaccard 匹配商品
        排除购物车已有商品 + 按 sales 倒序
  返回: List<RecommendProductVO>

POST /api/recommend/click                         推荐点击回写
  入参: { recommendType, targetId }  （前端点击推荐卡片时调用）
  逻辑: 取当前用户最近 10 分钟内 tb_recommend_record 中 recommend_type=X &
        target_id=Y & is_clicked=0 的最新一条 → UPDATE is_clicked=1, click_time=NOW()
  返回: Result<Void>（失败静默，不阻塞跳转）
```

**浏览埋点触发规则:**
```
触发时机: ProductDetail.vue → onMounted → 调用 POST /api/recommend/browse
        （失败静默处理,不阻塞用户浏览）
去重规则: Redis SETNX "browse:dedup:{userId}:{productId}" TTL=1800s
          30 分钟内同用户同商品只记录 1 次
数据格式: { userId(从 JWT), productId, categoryId, source: "detail" }
清理策略: RecommendScheduler 每周一凌晨 3 点清理 90 天前记录
未登录处理: 前端检测未登录时不发埋点（省流量 + 避免脏数据）
```

**Redis Key 设计（商品推荐）:**
```
recommend:product:home:{userId}            TTL 30min    首页推荐结果
recommend:product:similar:{productId}      TTL 1h       相似商品结果
recommend:product:cart:{userId}            TTL 10min    购物车推荐(短缓存因购物车变化频繁)
browse:dedup:{userId}:{productId}          TTL 30min    浏览去重标记
recommend:cf:matrix                        TTL 6h       CF 矩阵(仅 cf-precompute=true 时使用)
```

**新增/修改文件:**
```
后端:
  RecommendController.java         ← 5 端点 @ApiOperation（含 POST /click）
  RecommendServiceImpl.java        ← recommendHome/recommendSimilar/recommendCart/clickRecord 实现
  BrowseLogMapper.java             ← 继承 BaseMapper + selectRecentByUserId
  BrowseLogDTO.java                ← 埋点入参
  RecommendRecordMapper.java       ← 继承 BaseMapper（曝光落表 + 点击回写）
  RecommendProductVO.java          ← 已在 8.0 骨架创建

前端:
  api/recommend.js                 ← recordBrowse/getHomeRecommend/getSimilarProduct/getCartRecommend
  views/Home.vue                   ← 改造: 新增「猜你喜欢」区块(登录用户) / 未登录展示热门
  views/product/ProductDetail.vue  ← 改造: onMounted 末尾埋点 + 底部「相关商品」横向卡片
  views/CartPage.vue               ← 改造: 底部「为你推荐」区块
```

**8.2 检查清单:**
- [x] RecommendController 4 个端点（POST /browse、GET /home、GET /similar/{productId}、GET /cart、POST /click）✅（实际在 8.1 阶段已提前落地，8.2 接入 Redis 缓存）
- [x] RecommendServiceImpl 接入 Redis 缓存 —— home/similar/cart 三路读缓存优先 + TTL（30min/1h/10min）+ try-catch 降级 ✅
- [x] logProductBrowse / logPostBrowse 添加 Redis SETNX 去重的 try-catch 降级（Redis 挂也能写库）✅
- [x] parseTags 兼容 JSON 数组 + CSV 两种历史格式，消除 `标签JSON解析失败` WARN 刷屏 ✅
- [x] api/recommend.js 方法齐全（10 个方法，含 recordRecommendClick）✅
- [x] ProductDetail.vue onMounted 埋点调用（失败静默,try-catch 包裹）✅
- [x] ProductDetail.vue 新增「相关商品」横向滚动卡片区块 + 卡片点击前调 recordRecommendClick ✅
- [x] ProductDetail.vue 添加 `watch(route.params.id)` —— 同路由 `/product/:id` 切换时手动 re-init（否则组件复用导致点击相关商品不刷新）✅
- [x] Home.vue 新增「猜你喜欢」网格区块（登录用户展示）+ 卡片点击前调 recordRecommendClick ✅
- [x] CartPage.vue 新增「为你推荐」区块（5 列响应式网格）+ 卡片点击前调 recordRecommendClick ✅
- [x] recordRecommendClick 前端字段与后端 DTO 对齐（recommendType + targetId）✅
- [x] 未登录场景：home 接口返回热门商品不报错（JwtInterceptor OPTIONAL_AUTH 生效）✅
- [x] 去重测试：30 分钟内同一商品连续浏览多次,tb_browse_log 只有 1 条记录 ✅
- [x] 曝光落表：首页刷新 → tb_recommend_record 新增 10 条 recommend_type='product' 记录 ✅
- [x] 点击回写：点击推荐卡片 → 对应行 is_clicked=1, click_time≠NULL ✅
- [x] Redis 降级测试：停掉 Redis 后页面仍能加载推荐（走算法兜底）✅

---

#### 8.3 跨模块联动推荐 ✅ (2026-04-15 完成)
**职责:** 4 条跨模块推荐线（AI识别→课程 / 完课→下一门 / 签到→商品 / 帖子推荐）。答辩差异化亮点。
**工时:** 1.5 天（4 个场景 + 3 个后端接口 + 5 个前端页面改造）
**实际落地说明:**
- 后端 RecommendController 所有 URL 早在 8.0 已就位（含 5 个 8.3 端点），本轮只填充 Service 实现
- 8.3.4 前端策略调整: 用户反馈"直接植入现有推荐 Tab 更符合主流软件习惯",因此将 `getPostRecommend` 植入 `ForumList.vue` 原「推荐(all)」Tab 的第一页（无 tag 筛选时生效）,**不新增"为你推荐"Tab**（避免双 Tab 割裂体验）
- 课程完课弹窗采用方案 B（el-dialog 居中弹窗 + next-course-grid 网格展示）,未实现 CourseHistory.vue 的右侧推荐（非核心路径,后续可选）

##### 8.3.1 AI 识别结果 → 推荐相关课程 🌟
```
GET /api/recommend/course/recognition/{recognitionId}?limit=6

逻辑:
  1. 查 tb_recognition.machine_tags (如 ["nebula","galaxy","cluster"])
  2. 英文标签映射为中文关键词（硬编码 Map,答辩时现场展示）
     nebula/galaxy  → "星云","深空摄影","天体物理"
     planet         → "行星观测","太阳系"
     star cluster   → "星座入门","双筒望远镜"
  3. LIKE 匹配 tb_course.tags (status=1 已发布)
  4. 排除用户已有 tb_course_progress 记录的课程
  5. 按报名人数倒序返回 Top6（SELECT course_id, COUNT(*) cnt FROM tb_course_progress
     GROUP BY course_id 作为子查询 LEFT JOIN，IFNULL 为 0）
  空结果兜底: machine_tags 为空 或 0 匹配 → 按 tb_course_progress COUNT(*) 倒序 Top6
             （tb_course 无 view_count 列，用报名人数代理；无报名课程时按 create_time DESC）

前端位置: RecognitionResult.vue 结果页「推荐器材」下方新增「推荐课程」区块
```

##### 8.3.2 学完课程 → 推荐下一门课 🌟
```
GET /api/recommend/course/next/{courseId}?limit=3

触发: CourseDetail.vue 完课事件（章节全部学完）

逻辑:
  1. 取目标课程的 tags (如 ["深空摄影","天文摄影后期"])
  2. 查 tb_course 中 status=1 且 tags 有交集的候选课程
  3. 排除用户已完成/正在学的课程 (tb_course_progress)
  4. 按 jaccardSimilarity(已完成课程tags, 候选课程tags) 降序
  5. 返回 Top3,空结果 → 返回热门课程

前端位置: CourseDetail.vue 完课弹窗 + CourseHistory.vue 学完记录右侧"下一门推荐"
```

##### 8.3.3 签到观测点 → 推荐器材 🌟
```
GET /api/recommend/product/checkin/{spotId}?limit=6

触发: ObservationMap.vue 签到成功后弹窗

逻辑 (规则前置 + 相似度排序,保持内容相似度主线):
  1. 查 tb_observation_spot 的 altitude + light_pollution_level
  2. 规则前置过滤候选标签池:
       altitude > 2000 且 light_pollution_level ≤ 3 → ["深空摄影","大口径望远镜","天文相机"]
       altitude > 1000                              → ["深空摄影","便携设备","赤道仪"]
       其他                                   → ["入门望远镜","双筒望远镜","目镜"]
  3. 从 tb_product 中 tags LIKE 候选标签池
  4. 在候选集内按 jaccardSimilarity(候选标签池, 商品tags) × 0.6 + 销量归一化 × 0.4 排序
  5. 返回 Top6

前端位置: ObservationMap.vue 签到成功弹窗底部「适合这里的器材」区块
```

##### 8.3.4 帖子浏览 → 推荐相关帖子 🌟
```
POST /api/recommend/post/browse            帖子浏览埋点
  入参: { postId, source: "detail" }
  逻辑: Redis SETNX "post:browse:dedup:{userId}:{postId}" TTL=1800s
        首次命中 → INSERT tb_post_browse_log; 重复 → 直接返回 ok

GET  /api/recommend/post/list?limit=10     为你推荐帖子

逻辑（内容相似度主导 + hot_score 兜底）:
  1. 取用户近 30 条 tb_post_browse_log → 聚合浏览帖子的 tags → userPostTags
  2. 查 status=2 + deleted=0 + 非用户已浏览的帖子作为候选
  3. 候选帖子按 jaccardSimilarity(userPostTags, postTags) 降序
  4. 相似度结果不足 limit → 补充 hot_score 倒序的热门帖子填满
  5. 无浏览记录（新用户 / 未登录）→ 直接按 hot_score 倒序返回 Top10

Redis: recommend:post:list:{userId} TTL=30min

前端位置:
  ForumDetail.vue         → onMounted 调用埋点接口（失败静默）
  ForumList.vue           → 顶部 Tab 重新命名:
                            现有「推荐(all)」→ 改名为「发现」(仍然 tab='all')
                            新增「为你推荐」→ tab='personalized',调用 post/list 接口
                            未登录时「为你推荐」Tab 隐藏,或降级为 hot_score Top10
```

**新增 Redis Key:**
```
recommend:post:list:{userId}               TTL 30min    帖子推荐结果
post:browse:dedup:{userId}:{postId}        TTL 30min    帖子浏览去重
```

**新增/修改文件（8.3 总计）:**
```
后端:
  RecommendController.java         ← 新增 5 个跨模块端点
  RecommendServiceImpl.java        ← recommendCourseByRecognition / recommendNextCourse /
                                     recommendProductByCheckin / recommendPost / recordPostBrowse
  PostBrowseLogMapper.java         ← 继承 BaseMapper + selectRecentByUserId
  CourseMapper.xml                 ← 新增 selectByTagsExcluding SQL
  RecognitionMapper.java           ← 复用已有的 selectById

前端:
  api/recommend.js                 ← 新增 5 个方法
  RecognitionResult.vue            ← 结果页底部「推荐课程」区块
  CourseDetail.vue                 ← 完课弹窗内「推荐下一门」
  CourseHistory.vue                ← (可选) 右侧推荐下一门
  ObservationMap.vue               ← 签到成功弹窗底部「适合器材」
  ForumDetail.vue                  ← onMounted 末尾埋点
  ForumList.vue                    ← Tab 重命名 + 新增「为你推荐」Tab
```

**8.3 检查清单:**
- [x] RecommendServiceImpl.getRecognitionCourseRecommend（machine_tags → EN_TO_ZH_TAG_MAPPING → getRecommendByTags → 排除已学 → getHotCourses 兜底）✅
- [x] RecommendServiceImpl.getNextCourseRecommend（candidate = getRecommendByTags(limit*4) → Java 侧 Jaccard 降序 → 排除 self+已学 → getHotCourses 兜底）✅
- [x] RecommendServiceImpl.getSpotEquipmentRecommend（Redis 缓存 → buildSpotTargetTags 规则池 → productMapper.selectByTagsAny → Jaccard×0.6 + normSales×0.4）✅
- [x] RecommendServiceImpl.logPostBrowse + Redis SETNX（8.1 已落地,本轮 ForumDetail.vue 调用）✅
- [x] RecommendServiceImpl.getPostRecommend（8.1 已落地,本轮 ForumList.vue 植入推荐 Tab）✅
- [x] RecommendController 5 个跨模块端点（早在 8.0 已就位,URL 直接复用）✅
- [x] PostBrowseLogMapper.selectRecentPostsByUserId（limit 30,8.0 已落地）✅
- [x] api/recommend.js 5 个方法（8.0 已落地,本轮前端全部实际调用）✅
- [x] RecognitionResult.vue 新增「推荐课程」区块（结果页「推荐器材」下方,空结果 v-if 隐藏）✅
- [x] CourseDetail.vue 完课 el-dialog 方案 B（watch completedCount === chapterCount + hasShownCompletionDialog 防重触发 + 屏蔽 APOD/Mars 每日课）✅
- [x] ObservationMap.vue 签到后底部「适合这里的器材」面板（支持首次签到 + 已签到再次打开弹窗两种场景）✅
- [x] ForumDetail.vue onMounted 末尾 logPostBrowse 埋点（try-catch 静默）✅
- [x] ForumList.vue：保留原「推荐(all)」Tab,将 getPostRecommend 植入第一页（isRecommendMode: all+无 tag+pageNum=1）,接口失败降级到 getPostList ✅
- [x] 后端 mvn clean compile 通过（413 sources compiled）+ 前端 vite build 通过（20.08s）✅

---

#### 8.4 定时任务 + 通知集成 + 答辩演示 ✅
**职责:** RecommendScheduler（定时清理埋点 + 可选预计算 CF）、2 条推荐通知模板集成、答辩演示流程与亮点固化。

**RecommendScheduler 定时任务:**
```java
// 每周一凌晨 3 点清理 90 天前浏览记录
@Scheduled(cron = "0 0 3 ? * MON")
public void cleanBrowseLogs() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
    int productDeleted = browseLogMapper.delete(new LambdaQueryWrapper<BrowseLog>()
        .lt(BrowseLog::getBrowseTime, cutoff));
    int postDeleted    = postBrowseLogMapper.delete(new LambdaQueryWrapper<PostBrowseLog>()
        .lt(PostBrowseLog::getBrowseTime, cutoff));
    log.info("[Recommend] 清理浏览日志: product={}, post={}", productDeleted, postDeleted);
}

// 可选: 每 6 小时预计算 CF 矩阵（recommend.cf-precompute=true 时启用）
@Scheduled(cron = "0 0 */6 * * ?")
@ConditionalOnProperty(name = "recommend.cf-precompute", havingValue = "true")
public void precomputeCfMatrix() {
    cfRecommendService.buildCfMatrix();  // 结果存 recommend:cf:matrix, TTL=6h
}
```

**通知集成（2 条,离散事件触发,避免骚扰）:**
```
PRODUCT_RECOMMEND 触发时机:
  当 PriceDropScheduler 检测到降价商品时,对收藏过 + interest_tags 命中该商品标签的用户
  发送 1 条 PRODUCT_RECOMMEND 通知（复用现有降价通知逻辑,只是文案改为"推荐给你"）
  jumpUrl: /product/detail?id={productId}

COURSE_RECOMMEND 触发时机:
  AI 识别完成后（RecognitionService onComplete）,调用 recommendCourseByRecognition 拿 Top1 课程,
  异步发送 1 条 COURSE_RECOMMEND 通知（不是每次识别都推,只在识别成功且至少有 1 门匹配课程时推）
  jumpUrl: /course/{courseId}

NotificationType.java: 已存在 PRODUCT_RECOMMEND / COURSE_RECOMMEND / USER_RECOMMEND 枚举
  (本轮仅使用前 2 个,USER_RECOMMEND 保留但不触发)
NotificationHelper.java: 新增 sendProductRecommendNotification / sendCourseRecommendNotification
```

**通知模板 INSERT（2 条）:**
```sql
INSERT INTO tb_notification_template
  (code, module, type, title_template, content_template, jump_url_template, variables, enabled, remark)
VALUES
('RECOMMEND_PRODUCT_RECOMMEND','recommend','product_recommend','为你推荐',
 '根据您的兴趣,发现一款可能喜欢的商品:{productName}',
 '/product/detail?id={productId}',
 '{"productName":"商品名","productId":"商品ID"}',1,'推荐商品通知'),
('RECOMMEND_COURSE_RECOMMEND','recommend','course_recommend','为你推荐课程',
 '根据您的识别结果,推荐课程《{courseName}》',
 '/course/{courseId}',
 '{"courseName":"课程名","courseId":"课程ID"}',1,'推荐课程通知');
```

**答辩演示方案（8 分钟）:**
```
1. 冷启动展示 (1 min)
   - 新用户注册 → AccountSettings 设置兴趣标签「深空摄影/行星观测」
   - 首页立即出现「猜你喜欢」匹配商品 → 说明 coldStart 三级兜底

2. 内容相似度 (2 min)
   - 点开一款天文相机 → 底部「相关商品」
   - 现场打开 Knife4j 展示 contentSimilarity 接口响应的 reason 字段
   - 讲解权重：tag×0.5 + category×0.3 + price×0.2

3. 协同过滤 (1 min)
   - 展示浏览记录积累后推荐结果变化
   - 现场切换 application.yml 的 cf-weight 说明"可配置"

4. 跨模块联动 (3 min, 最亮点)
   - AI 识别猎户座大星云 → 自动推荐深空摄影课程 (亮点1)
   - 完课弹出"推荐下一门" (亮点2)
   - 签到高海拔观测点 → 推大口径望远镜 (亮点3)
   - 连续浏览几篇深空摄影帖 → 论坛「为你推荐」Tab 出现匹配帖 (亮点4)

5. 算法讲解 (1 min)
   - 展示 RecommendServiceImpl 的 jaccardSimilarity + contentSimilarity 代码
   - 说明 Redis 缓存 + 30 分钟去重防刷机制
```

**答辩亮点总结（印在 PPT 封底页）:**
```
✅ 论文题目完全对应: 内容相似度推荐算法（Jaccard + 特征向量加权）
✅ 跨模块联动: AI 识别 / 课程 / 地理签到 / 论坛 四路信号驱动推荐
✅ 混合策略: 内容 60% + CF 30% + 冷启动 10%（权重可配置）
✅ 工程完整: Redis 缓存 + 定时清理 + 商品/帖子双埋点 + 30min 去重 + 三级冷启动兜底
✅ 与论坛热度打通: 帖子推荐结果不足时回退 hot_score Top10（7.8 已实现的热度分复用）
```

**8.4 检查清单:**
- [x] RecommendScheduler.cleanBrowseLogs（每周一清理 90 天前两张日志）✅ 8.0已完成
- [x] RecommendScheduler.precomputeCfMatrix（@ConditionalOnProperty 默认关闭）✅ 8.0已完成
- [x] NotificationHelper 新增 sendProductRecommendNotification（降价命中 interest_tags 时）✅
- [x] NotificationHelper 新增 sendCourseRecommendNotification（识别完成后 Top1 课程）✅
- [x] RecognitionPollScheduler.handleSuccess 异步触发 sendCourseRecommendNotification（top1 课程）✅
- [x] PriceDropScheduler 降价检测时按 interest_tags 命中 → sendProductRecommendNotification ✅
- [x] tb_notification_template INSERT: PRODUCT_RECOMMEND / COURSE_RECOMMEND 两行（id=34/35, module=recommend, type=product_recommend / course_recommend）✅
- [x] NotificationBell.vue Tab 新增「推荐」过滤分组（module=recommend）✅
- [ ] 端到端演示脚本: 浏览器材 → 刷新首页看到 4 模块推荐 → 购物车看 similar → 论坛看热度推荐 → 识别一张图看通知中心收到 COURSE_RECOMMEND
- [ ] PPT 封底页: 贴出 4 张截图（算法公式 / home 推荐 / 识别后通知 / 购物车相似商品）

**与之前模块的联动说明（8.0→8.4 完成后生效）:**

| 联动方向 | 被动方 | 触发点 | 本模块做什么 |
|---------|-------|--------|-------------|
| 5. 识别 → 8. 推荐 | RecognitionServiceImpl.onComplete | 识别状态流转到 SUCCESS | 拿 machine_tags 异步调用 recommendCoursesByTags → sendCourseRecommendNotification |
| 8. 推荐 → 4. 课程 | CourseDetailPage.vue | 学员学完一门 | 调用 /api/recommend/course/next 展示「下一门推荐」卡片 |
| 8. 推荐 → 6. 地理 | CheckInSuccessDialog.vue | 签到成功回调 | 调用 /api/recommend/product/by-spot?spotId=X 在弹窗内展示 3 件器材 |
| 7. 论坛 → 8. 推荐 | PostDetailPage.vue mounted | 进入帖子详情 | tb_post_browse_log 埋点 + 底部「相关帖子」区 6 条 |
| 8. 推荐 → 7. 论坛 | ForumList.vue 「为你推荐」Tab | 用户点击 Tab | 调用 /api/recommend/post/for-you 展示 interest_tags 匹配 + CF |
| 8. 推荐 → 9. 通知 | NotificationHelper | 降价触达 / 识别完成 | 插入 PRODUCT_RECOMMEND / COURSE_RECOMMEND 两种新模板 |
| 2. 用户 → 8. 推荐 | AccountSettings.vue 兴趣标签区 | 用户保存 interest_tags | 写入 tb_user.interest_tags，被后续所有场景读取用于 Jaccard |

**第 8 模块数据统计（restructure 后）:**
- **接口数:** 10 个（商品埋点 1 + 商品推荐 3 + 跨模块 4 + 帖子埋点 1 + 推荐点击回写 1）
- **新建表:** 3 张（tb_browse_log ㊷ / tb_post_browse_log ㊸ / tb_recommend_record ㊺）
- **可选表:** 1 张（tb_recommend_config ㊹，demo 默认不创建；CF 权重直接用 application.yml）
- **新模块:** 1 个（module/recommend/）
- **Scheduler:** 1 个（RecommendScheduler，含 cleanBrowseLogs + precomputeCfMatrix）
- **前端改造:** 9 处（Home / Cart / ProductDetail / CourseDetail / CheckInSuccessDialog / ForumList / PostDetail / AccountSettings / NotificationCenter）
- **通知新增:** 2 种模板（PRODUCT_RECOMMEND / COURSE_RECOMMEND，枚举已存在无需 code 改动）
- **复用现有能力:** interest_tags / product.tags / machine_tags / hot_score / course_progress / observation_spot / NotificationHelper
- **答辩数据支撑:** tb_recommend_record 曝光落表 + 点击回写，答辩时一句 SQL 回答"推荐是否有效"

**预计工时:** 5.5 天（8.0 基建 1 天 + 8.1 算法 1 天 + 8.2 商品场景+曝光落表 1 天 + 8.3 跨模块 1.5 天 + 8.4 定时/通知/答辩 1 天）

---

## ⚠️ 数据库表扩展规划（重要！避免后期改表）

### 必须立即执行的扩展 ⭐⭐⭐⭐⭐

```sql
-- 1. 扩展商品表（为推荐系统准备）
ALTER TABLE `tb_product` 
ADD COLUMN `tags` VARCHAR(500) DEFAULT NULL COMMENT '商品标签(JSON数组,如["天文望远镜","入门级","便携式"])';

-- 2. 扩展订单表（为后台管理准备）
ALTER TABLE `tb_order` 
ADD COLUMN `logistics_company` VARCHAR(100) DEFAULT NULL COMMENT '物流公司',
ADD COLUMN `tracking_number` VARCHAR(100) DEFAULT NULL COMMENT '物流单号',
ADD COLUMN `logistics_status` TINYINT(4) DEFAULT 0 COMMENT '物流状态(0-未发货 1-运输中 2-派送中 3-已签收)',
ADD COLUMN `admin_remark` VARCHAR(500) DEFAULT NULL COMMENT '管理员备注';

-- 添加索引
ALTER TABLE `tb_order` ADD INDEX `idx_tracking_number` (`tracking_number`);
```

### 创建课程表时必须包含的字段（第11-12周）

```sql
-- tb_course 创建时必须包含：
`tags` VARCHAR(500) DEFAULT NULL COMMENT '课程标签(JSON数组,如["天文摄影","入门级","实操教学"])'
```

### 表关联说明

**推荐系统依赖：**
- `tb_browse_log` → `tb_product`（需要 tags 字段）
- `tb_course_browse_log` → `tb_course`（需要 tags 字段）
- `tb_user`（interest_tags 用于冷启动推荐）

**后台管理依赖：**
- `tb_admin_log` → `tb_user`（role=1）
- 订单发货需要 `tb_order` 的物流字段

**详细的表关联文档请查看：** 📊 数据库表关联与扩展规划文档

---

## 📊 数据库表统计

### 已创建的表 (44张) ✅ — v8.60 SHOW TABLES 实测对齐

| 模块 | 张数 | 表名 |
|------|------|------|
| 用户相关 | 2 | tb_user / tb_login_log |
| 商城商品 | 3 | tb_category / tb_product / tb_cart |
| 订单支付 | 4 | tb_order / tb_order_item / tb_payment / tb_refund |
| 评价举报 | 3 | tb_review / tb_review_like / tb_review_report |
| 个人空间 | 2 | tb_address / tb_balance_log |
| 售后服务 | 3 | tb_installation / tb_service_reminder / tb_recycling |
| 商品收藏 | 1 | tb_product_favorite |
| AI识别 | 1 | tb_recognition |
| 后台管理 | 4 | tb_admin_log / tb_product_log / tb_stock_log / tb_system_setting |
| 通知系统 | 3 | tb_notification / tb_notification_template(35条) / tb_user_notification_setting |
| 课程模块 | 5 | tb_course / tb_course_chapter / tb_course_progress / tb_course_favorite / tb_course_review |
| 地理位置 | 3 | tb_observation_spot / tb_user_checkin / tb_spot_rating |
| 论坛模块 | 7 | tb_post / tb_post_comment / tb_post_like / tb_post_collect / tb_comment_like / tb_user_follow / tb_search_log |
| 推荐系统 | 3 | tb_browse_log / tb_post_browse_log / tb_recommend_record |
| **合计** | **44** | ✅ |

**架构演进砍掉 (2张):** `tb_post_image` / `tb_post_tag` → 改用 JSON 字段替代 (v8.34)

**可选未建 (2张):** `tb_recommend_config` / `tb_course_browse_log` → demo 阶段无需建，由 application.yml + tb_browse_log 覆盖

**答辩口径:** 早期规划 48 − 砍掉 2 − 可选未建 2 = **实际 44 张** ✅

---

## 📝 开发进度追踪

### ✅ 已完成模块

#### 1. 用户管理模块 ✅
**完成时间:** 2025-11-04

#### 2. 商城模块-商品购物 ✅
**完成时间:** 2025-11-14

#### 3. 支付系统模块 ✅
**完成时间:** 2025-12-11

#### 4. 商品调整日志模块 ✅
**完成时间:** 2025-01-23

#### 5. 商品批量导入/导出模块 ✅
**完成时间:** 2025-01-23

#### 6. 后台订单管理模块 ✅
**完成时间:** 2026-01-30

#### 7. 消息通知中心模块 ✅
**完成时间:** 2026-02-06

#### 8. 退款审核管理模块 ✅
**完成时间:** 2026-02-25

---

#### 9. 后台数据统计模块 ✅ 🆕
**完成时间:** 2026-03-02

---

#### 10. 个人中心布局与概览页 (2.4.1) ✅ 🆕
**完成时间:** 2026-03-07

---

### ✅ 已完成阶段（全部）

#### 11. 安装预约模块 (2.5.1) ✅ 🆕
**完成时间:** 2026-03-10

#### 12. 二手回收模块 (2.5.3) ✅ 🆕
**完成时间:** 2026-03-11

**阶段:** 售后服务模块全部完成 ✅ 2.5.1安装预约✅ 2.5.2器材保养提醒✅ 2.5.3二手回收✅  
**日期:** 2026-03-11  
**完成度:** 90%

**下一步:** 
1. **优先级1:** 商城通知集成 (订单/退款通知调用点集成) 🔥
2. **优先级2:** 商品收藏功能 (2.6)
3. **优先级3:** 后台消息管理 (通知记录/模板/公告)

---

## 🎯 第8周开发计划 - 商城通知集成 + 后台消息管理

### ⚠️ 重要提醒
通知核心框架已完成80%,本周重点是**集成到业务模块**和**开发后台消息管理**

### 开发目标
1. 完成商城模块的10种通知集成(Day 1-6)
2. 完成后台消息管理功能(Day 7-10)

### 开发时间安排 (10天)

#### Day 1-2: ✅ 已完成 - 商品管理 + 库存日志 + 商品日志 + 批量导入导出
**已完成时间:** 2025-01-23

**已完成功能:**
- ✅ AdminProductController: 商品管理接口(13个)
- ✅ StockLogController: 库存日志接口(2个)
- ✅ ProductLogController: 商品日志接口(2个)
- ✅ ProductLogAspect: AOP切面自动记录
- ✅ 商品批量导入/导出/模板下载(3个接口)
- ✅ 前端ProductManage.vue完整实现
- ✅ 完整的数据校验和异常处理

**技术亮点:**
- AOP切面自动记录商品变更
- Hutool ExcelUtil处理Excel导入导出
- 字段级别的变更追踪
- 完整的日志审计体系

---

#### Day 1: 订单通知集成 (5个集成点) ⭐⭐⭐⭐⭐

**任务:** 在已有的Service中添加NotificationHelper调用

**集成点清单:**
- [x] PaymentServiceImpl.simulatePaymentSuccess() - 支付成功通知 ✅
- [x] OrderServiceImpl.confirmReceipt() - 订单完成通知 ✅
- [x] OrderServiceImpl.cancelOrder() - 用户取消订单通知 ✅
- [x] AdminOrderServiceImpl.shipOrder() - 订单发货通知 ✅
- [x] AdminOrderServiceImpl.deliverOrder() - 订单派送通知 ✅
- [x] AdminOrderServiceImpl.cancelOrder() - 管理员取消订单通知 ✅

**代码示例:**
```java
// 1. PaymentServiceImpl.simulatePaymentSuccess()
notificationHelper.sendOrderPaidNotification(
    order.getUserId(),
    order.getOrderNo(),
    order.getPaymentAmount().toString(),
    order.getId()
);

// 2. OrderServiceImpl.confirmReceipt()
notificationHelper.sendOrderCompletedNotification(
    order.getUserId(),
    order.getOrderNo(),
    order.getId()
);

// 3. AdminOrderServiceImpl.shipOrder()
notificationHelper.sendOrderShippedNotification(
    order.getUserId(),
    order.getOrderNo(),
    dto.getLogisticsCompany(),
    dto.getTrackingNumber(),
    order.getId()
);
```

**测试检查:**
- [ ] 支付成功后能收到通知
- [x] 订单发货后能收到通知 ✅
- [x] 订单派送后能收到通知 ✅
- [ ] 确认收货后能收到通知
- [ ] 取消订单后能收到通知
- [ ] 通知铃铛未读数正确更新
- [ ] 点击通知能正确跳转

---

#### Day 2-4: 商品收藏功能 + 通知集成 ✅ 已完成 (2026-03-12)

**Day 2: 数据库表 + 基础功能**
- [x] 创建tb_product_favorite表 ✅
- [x] 创建ProductFavorite实体类 ✅
- [x] 创建ProductFavoriteMapper (LEFT JOIN实时状态) ✅
- [x] 创建ProductFavoriteService + ServiceImpl ✅
- [x] 创建ProductFavoriteController (4个接口) ✅
- [x] 创建FavoriteVO (含isOffShelf/isPriceDown/isPriceUp) ✅

**Day 3: 前端页面 + 上架通知**
- [x] 开发api/favorite.js (4个方法) ✅
- [x] 商品详情页添加收藏按钮 ✅
- [x] 开发"我的收藏"页面 (网格+遮罩+涨跌标签) ✅
- [x] 在AdminProductServiceImpl.updateStatus()中集成上架通知 ✅
- [x] 测试收藏功能 ✅
- [x] 测试上架通知 ✅

**Day 4: 降价检测定时任务 + 降价通知**
- [x] 创建PriceDropScheduler类 ✅
- [x] 开发降价检测定时任务(每天凌晨2点) ✅
- [x] 集成降价通知 ✅
- [x] 测试定时任务 ✅
- [x] 测试降价通知 ✅

**数据库表SQL:**
```sql
CREATE TABLE `tb_product_favorite` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) DEFAULT NULL COMMENT '商品名称(冗余)',
  `product_price` decimal(10,2) DEFAULT NULL COMMENT '收藏时价格',
  `product_image` varchar(500) DEFAULT NULL COMMENT '商品图片(冗余)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';
```

---

#### Day 5-6: 退款审核功能 + 通知集成 (2天) ✅ 已完成 🆕
**完成日期:** 2026-02-25

**已完成功能:**
- [x] 创建AdminRefundController (5个接口) ✅
- [x] 创建AdminRefundService + ServiceImpl ✅
- [x] 创建RefundQueryDTO + RefundAuditDTO ✅
- [x] 创建AdminRefundVO + AdminRefundDetailVO ✅
- [x] 集成3种退款通知(通过/拒绝/到账) ✅
- [x] 退款成功同步更新订单状态 ✅
- [x] 开发api/admin/refund.js (5个方法) ✅
- [x] 开发RefundManage.vue (退款管理页面) ✅
  - 退款列表表格(状态/金额/原因/时间)
  - 状态标签颜色区分
  - 审核通过/拒绝对话框(含表单验证)
  - 退款详情抽屉(完整信息展示)
  - 失败重试按钮
- [x] 测试所有功能 ✅

**通知集成代码:**
```java
// 1. 审核通过通知 (relatedType=order, relatedId=orderId，点击跳转到订单详情)
notificationHelper.sendRefundApprovedNotification(
    refund.getUserId(),
    refund.getRefundAmount().toPlainString(),
    refund.getId(),
    refund.getOrderId()    // ← orderId，前端跳转到 /order/detail/:id
);

// 2. 审核拒绝通知
notificationHelper.sendRefundRejectedNotification(
    refund.getUserId(),
    reason,
    refund.getId(),
    refund.getOrderId()
);

// 3. 退款到账通知
notificationHelper.sendRefundCompletedNotification(
    refund.getUserId(),
    refund.getRefundAmount().toPlainString(),
    refund.getId(),
    refund.getOrderId()
);
```

---

#### Day 7-10: 后台消息管理模块 (4天) ⭐⭐⭐⭐

**Day 7-8: 系统公告管理**
- [ ] 创建tb_announcement表
- [ ] 创建Announcement实体类
- [ ] 创建AnnouncementMapper + XML
- [ ] 创建AnnouncementService + ServiceImpl
- [ ] 创建AnnouncementController (7个接口)
- [ ] 创建DTO/VO
- [ ] 开发发送公告逻辑(批量创建通知)
- [ ] 开发api/admin/announcement.js
- [ ] 开发AnnouncementManage.vue
- [ ] 测试公告发送功能

**Day 9: 通知记录管理 ✅**
- [x] 创建AdminNotificationController (3个接口)
- [x] 开发统计分析逻辑
- [x] 开发批量删除逻辑
- [x] 开发api/admin/notification.js
- [x] 开发NotificationRecord.vue
- [x] 添加统计图表(ECharts)
- [x] 测试所有功能

**Day 10: 通知模板管理 ✅**
- [x] 创建AdminNotificationTemplateController (5个接口)
- [x] 开发模板编辑逻辑
- [x] 开发启用/禁用逻辑
- [x] 开发恢复默认逻辑
- [x] 开发api/admin/notification.js（合并在通知记录API文件中）
- [x] 开发NotificationTemplate.vue
- [x] 添加模板预览功能
- [x] 添加"消息管理"菜单
- [x] 测试所有功能

---

### 第8周总结检查清单

**通知集成 (6个点):**
- [x] 订单支付成功通知 ✅
- [x] 订单发货通知 ✅
- [x] 订单派送通知 ✅
- [x] 订单完成通知 ✅
- [x] 订单取消通知(用户+管理员) ✅

**商品收藏 (1表 + 4接口 + 2通知):**
- [ ] tb_product_favorite表 ✅
- [ ] 收藏功能 ✅
- [ ] 商品上架通知 ✅
- [ ] 商品降价通知 + 定时任务 ✅

**退款审核 (5接口 + 3通知):**
- [x] 退款审核功能 ✅ 2026-02-25完成
- [x] 审核通过/拒绝通知 ✅
- [x] 退款到账通知 ✅
- [x] 退款成功同步订单状态 ✅

**后台消息管理 (13接口 + 3页面):**
- [x] 系统公告管理 ✅
- [x] 通知记录管理 ✅
- [x] 通知模板管理 ✅

**预计完成:**
- 数据库表: +2张
- API接口: +25个
- 前端页面: +5个
- 通知集成: +11个通知点

## 🎤 答辩准备 (更新版)

### 演示流程 (15-20分钟)

#### 1. 项目介绍 (2分钟)
- 项目背景和意义
- 核心功能模块
- 技术架构图

#### 2. 用户端完整流程 (8分钟)
1. 用户注册/登录 (30秒)
2. 浏览商品和分类 (30秒)
3. 搜索筛选商品 (30秒)
4. 查看商品详情和评价 (1分钟)
5. 加入购物车 (30秒)
6. 购物车管理 (1分钟)
7. 创建订单 (30秒)
8. 选择支付方式 (30秒)
9. 模拟支付成功 (30秒)
10. 查看订单列表 (30秒)
11. 确认收货和评价 (1分钟)
12. 申请退款 (30秒)

#### 3. 管理员端完整流程 (7分钟) 🆕
1. **管理员登录** (30秒)
2. **数据看板** (1分钟)
   - 今日/本月销售数据
   - 销售趋势图
   - 待处理事项
3. **商品管理** (1.5分钟)
   - 查看商品列表
   - 新增商品演示
   - 上下架操作
   - **查看商品调整日志** ✅ 🆕
4. **库存管理** (1分钟)
   - 调整库存
   - **查看库存调整日志** ✅
5. **订单管理** (1.5分钟)
   - 查看订单列表
   - 订单发货操作
   - 查看订单详情
6. **退款审核** (1.5分钟)
   - 查看退款申请
   - 审核通过操作
   - 退款处理

#### 4. 技术亮点 (3分钟)
- JWT双重认证(用户+管理员)
- 购物车实时同步
- 订单状态机
- 支付流程设计
- 退款流程设计
- **管理员权限控制** 
- **操作日志AOP切面** 
- **商品调整日志系统** ✅ 🆕
  - AOP切面自动记录
  - 字段级别变更追踪
  - 完整的审计追溯
- **数据统计可视化** 
- 库存管理
- 类型安全转换

---

### 常见问题准备

#### 技术问题

**Q1: 如何实现管理员权限控制?** 
- 双重拦截器: JwtInterceptor + AdminInterceptor
- JwtInterceptor 验证登录
- AdminInterceptor 验证角色(role=1)
- 路由级别的权限控制

**Q2: 操作日志如何记录?** 
- 使用AOP切面
- @AdminLog注解标记需要记录的方法
- 自动记录操作人、时间、参数、IP
- 异步保存,不影响业务性能

**Q3: 商品调整日志如何实现?** ✅ 🆕
- 使用AOP切面 (ProductLogAspect)
- @ProductLog注解标记需要记录的方法
- 对比操作前后的商品对象,提取变更字段
- JSON格式存储变更详情
- 字段级别的变更追踪
- 自动记录操作人、IP、时间

**Q4: 数据统计SQL如何优化?**
- 使用索引(时间、状态字段)
- GROUP BY + 聚合函数
- 适当使用缓存(Redis)
- 定时任务预计算

**Q5: Excel导出如何实现?**
- 使用Hutool的ExcelUtil
- 设置响应头(Content-Type)
- 流式写入,防止内存溢出

**Q6: 为什么选择JWT而不是Session?**
- 无状态,易扩展
- 跨域友好
- 移动端友好

---

## 📊 项目数据统计 (更新)

### 代码量预估
```
后端代码:         约 22000 行 (+1000) 🆕
前端代码:         约 14000 行 (+1000) 🆕
数据库:           约 3500 行
项目总代码量:      约 39500 行 (+2000)
```

### 功能完成度 (2026-04-16 v8.60 最终版)
```
用户管理:         100% ✅
商城商品:         100% ✅ (2.1商品浏览+购物✅ 2.2支付✅ 2.3后台8子模块✅ 2.4个人空间✅ 2.5售后3子模块✅ 2.6收藏✅)
NASA集成:         100% ✅ (2.7.1 APOD服务✅ 2.7.2 首页展示✅ 2.7.3 Mars同步✅)
通知系统:         100% ✅ (35条模板 / 12种业务通知 / 铃铛+推荐Tab)
AI识别:           100% ✅ (4.1上传✅ 4.2轮询✅ 4.3结果展示✅ 4.4器材推荐✅ 4.5课程推荐通知✅)
课程模块:         100% ✅ (5.1用户端✅ 5.2APOD同步✅ 5.3收藏历史✅ 5.4购买推荐✅ 5.5后台管理✅ 5.6评价✅)
地理位置:         100% ✅ (6.0建设✅ 6.1地图列表✅ 6.2天气月相✅ 6.3签到足迹✅ 6.4地址联动✅ 6.5后台管理✅)
论坛社区:         100% ✅ (7.1-7.8全部完成,小红书web风格+通知集成+热度计算)
推荐系统:         100% ✅ (8.0基建✅ 8.1算法✅ 8.2商品场景✅ 8.3跨模块✅ 8.4通知集成✅ v8.60 bugfix✅)

总体完成度:       100% 🎉 毕设核心功能全部开发完成
```

### 数据库表完成度
```
已建:    44 张 ✅ (SHOW TABLES 实测)
砍掉:     2 张 ❌ (tb_post_image / tb_post_tag → JSON字段替代)
可选未建: 2 张 ⬜ (tb_recommend_config / tb_course_browse_log → demo无需建)
答辩口径: 44 张 = 所有必要功能 100% 落地
```

### API接口统计 (v8.60 最终版，grep 实测 255 个端点注解)
```
用户/认证:        6个接口 ✅
商品/购物车:     17个接口 ✅
订单/支付:       18个接口 ✅
评价/举报:       10个接口 ✅
收藏/通知:       14个接口 ✅
售后(安装/保养/回收): 12个接口 ✅
后台-商品/分类:  18个接口 ✅ (含导入/导出)
后台-订单/退款/评价: 18个接口 ✅
后台-用户/日志/统计/设置: 24个接口 ✅
后台-公告/通知/模板: 18个接口 ✅
AI识别:           8个接口 ✅
NASA集成:         3个接口 ✅
课程模块:        20个接口 ✅ (含后台管理)
地理位置:        12个接口 ✅ (含后台管理)
论坛社区:        30个接口 ✅ (含后台管理)
推荐系统:        10个接口 ✅

总计:           ~255个接口 ✅ (grep @*Mapping 实测)
```

---

## 🎁 项目亮点总结 (更新)

### 技术亮点
1. ✅ 前后端分离架构 (Vue3 + Spring Boot)
2. ✅ JWT无状态认证 + 三层拦截器 (白名单/可选/保护)
3. ✅ 购物车实时同步 + 商品快照机制
4. ✅ 订单状态机设计 (待支付→待发货→待收货→已完成)
5. ✅ 库存自动管理 + 超卖防护
6. ✅ 支付/退款流程设计 (余额支付+行锁负值校验)
7. ✅ 双重权限拦截器 (JwtInterceptor + AdminInterceptor)
8. ✅ AOP操作日志切面 (@AdminLog + @ProductLog)
9. ✅ **消息通知中心** 🔔 — 35条模板 / @Async异步发送 / 铃铛角标 / 推荐Tab / 用户自定义偏好
10. ✅ **Jaccard + CF 混合推荐算法** — 内容相似度(tags×0.5+category×0.3+price×0.2) + Item-based CF余弦相似度 + 冷启动三级兜底 + Redis缓存
11. ✅ **跨模块异构信号编排** — 识别→课程 / 完课→下一门 / 签到→器材 / 帖子推荐 4条联动线
12. ✅ **AI星图识别** — Astrometry.net接入 + 30秒轮询调度 + 中英双语天体标签 + 器材/课程双推荐
13. ✅ **高德地图集成** — 观测点地图 + 天气月相评估 + 签到Haversine距离校验 + 足迹记录
14. ✅ **论坛社区** — 小红书web风格 + 瀑布流 + 关注/搜索/热度计算 + 9种通知类型
15. ✅ 数据统计可视化 — ECharts折线图/饼图/趋势 (后台管理 2.3.6)
16. ✅ 商品批量导入/导出 (Excel + 数据校验 + 模板下载)

### 业务亮点
1. ✅ 完整购物流程 (商品→购物车→订单→支付→收货→评价/退款)
2. ✅ 完整售后体系 (安装预约 + 器材保养提醒 + 二手回收)
3. ✅ 完整后台管理 (商品/订单/退款/评价/用户/统计/分类/日志/系统设置 9大模块)
4. ✅ 智能推荐系统 — 8.0基建 + 8.1算法 + 8.2商品场景 + 8.3跨模块 + 8.4通知集成 (v8.60 bugfix)
5. ✅ 天文课程生态 — APOD每日同步 + Mars探测车 + 完课推荐 + 收藏历史 + 课程评价
6. ✅ 地理位置服务 — 观测点推荐 + 天气适宜度评估 + 签到打卡 + 我的足迹
7. ✅ 论坛社区生态 — 发帖/评论/点赞/收藏/关注/搜索/后台审核 全功能

### 创新亮点 (论文核心)
1. ✅ 垂直领域定位 — 天文器材 + AI识别 + 课程 + 社区 + 推荐的生态闭环
2. ✅ 跨模块推荐信号编排 — 异构数据源(浏览/收藏/购买/识别/签到/帖子)统一进推荐管道
3. ✅ AI识别→商业转化链路 — 识别星图 → 推荐对应器材 + 课程 + 发送通知
4. ✅ 三层日志审计体系 — 登录日志 + 管理员操作日志 + 商品变更日志
5. ✅ 地理位置与器材推荐联动 — 观测点高度/光污染等级 → 推荐适合器材

---

## 💡 使用建议

### 开发阶段
1. 每完成一个模块,立即更新主控文档
2. 遇到问题记录在"避坑指南"
3. 定期备份代码和数据库
4. 重要功能截图保存
5. **后台功能完成后立即测试** 
6. **商品调整日志定期检查** 🆕

### 测试阶段
1. 准备完整的测试用例
2. 录制演示视频(备用)
3. 准备多套测试数据
4. 检查边界条件
5. **准备管理员测试账号** 
6. **测试日志记录功能** 🆕

### 答辩阶段
1. 提前演练流程
2. 准备问题应对
3. 检查网络和设备
4. 准备备用方案
5. **双端功能都要演示** 
6. **展示日志审计功能** 🆕

---

## 📄 更新日志

### 2026-04-16 v8.60 🔧 推荐系统 8.4 bugfix（COURSE_RECOMMEND 通知无法触发）

**背景:** v8.59 上线后用户实测「星图识别成功 → 推荐课程通知」未触发，经 DB 排查定位到两处逻辑缺陷（通知链路完全正常，问题在推荐候选集返回空 → NotificationHelper 收到 null 静默跳过）。

**Bug 1 — 英中标签映射匹配方式错误（`RecommendServiceImpl.getRecognitionCourseRecommend`）:**
- **现象:** `EN_TO_ZH_TAG_MAPPING.get(tag.toLowerCase().trim())` 走精确 key 查询，`zhKeywords` 永远为空
- **根因:** Astrometry.net 返回的 `machine_tags` 是带限定词的短语(如 `"Andromeda Galaxy"`/`"emission nebula"`/`"The star ν And"`/`"NGC 205"`/`"M 31"`)，与 Map 的原子 key(`"galaxy"`/`"nebula"`/`"star"`) 无法精确相等
- **修复:** 改为**子串包含匹配**(双层循环 `lowerTag.contains(entry.getKey())`)，`"andromeda galaxy".contains("galaxy") == true` 即命中
- **文件:** `RecommendServiceImpl.java:577-594`
- **影响:** 识别→课程标签推荐分支现在真正能走到(之前永远走热门兜底分支)

**Bug 2 — 热门兜底池太小被过滤穿底（同一方法）:**
- **现象:** Bug1 修复后仍空返回；DB 查证用户 10001 已学 29 门课(全库 51 门)
- **根因:** `getHotCourses(userId, limit*2)` 只拉 Top2 热门课(id=4,id=19)，两门恰好都在 learned 集合里 → filter 后为空
- **修复:** 兜底池大小改为 `Math.max(limit*10, 20)`，至少拉 20 门候选再过滤
- **文件:** `RecommendServiceImpl.java:611-626`
- **影响:** 活跃用户(学过的课程多)也能稳定返回 Top1 课程 → `sendCourseRecommendNotification` 能正常拿到 courseName/courseId

**测试验证:**
- 用户新上传一张仙女座星图 → identity 回调成功 → NotificationPollScheduler.handleSuccess 异步触发 → tb_notification 成功插入 type=`COURSE_RECOMMEND` 的记录 → 前端 NotificationBell 「推荐」Tab 红色角标 +1 ✅
- 用户回执「可以了」✅

**构建:** 后端 mvn compile 通过（仅局部修改单文件）；前端无改动（无需重新 build）

**数据库:** 零变更（纯业务逻辑调整，无 SQL/字段/模板变动）

**与 v8.59 的关系:** v8.59 的 8.4 实现仍然成立且已被此 bugfix 补齐，是 v8.59 的 hotfix 不是重写。

---

### 2026-04-16 v8.59 🎉 推荐系统通知集成完成 (8.4)

**后端改动:**
- `NotificationHelper.java` 新增 2 个 @Async 推荐通知方法: `sendProductRecommendNotification`（降价+兴趣标签命中→推荐商品通知）、`sendCourseRecommendNotification`（识别成功后推荐Top1课程通知）✅
- `PriceDropScheduler.java` 全面改造: 新增 `@Autowired UserMapper`、`hasInterestTagOverlap()` 兴趣标签交集判断、`parseTags()` 双格式解析(JSON数组+CSV)；降价通知后额外检测 interest_tags 命中→发送 PRODUCT_RECOMMEND 推荐通知 ✅
- `RecognitionPollScheduler.java` 增量改造: 新增 `@RequiredArgsConstructor` 注入 `RecommendService`；`handleSuccess()` 识别成功通知后追加课程推荐通知（调用 `getRecognitionCourseRecommend(recognitionId, 1)` 获取 Top1 课程 → `sendCourseRecommendNotification`）✅
- `8.4_recommendation_notification_templates.sql` 新建: 2 条推荐通知模板（id=34 RECOMMEND_PRODUCT_RECOMMEND / id=35 RECOMMEND_COURSE_RECOMMEND）✅

**前端改动:**
- `NotificationBell.vue` 新增「推荐」Tab（el-tab-pane name="recommend"，带未读角标，位于课程与系统之间）+ `moduleTagType` 新增 recommend → warning 配色 ✅

**数据库变更:**
- tb_notification_template 新增 2 行（id=34/35），执行 `8.4_recommendation_notification_templates.sql`

**构建验证:** 后端 mvn compile 通过 + 前端 vite build 通过 ✅

---

### 2026-04-16 v8.58 🔧 支付模块 4 Bug 修复 + PaymentPage 综合重构（2.4.4 补丁）

**背景:** 用户实机回归支付流程时发现 4 个 bug，全部集中在订单→支付页→余额扣款这条主链路上：
1. 进入支付页立刻弹"支付记录不存在"toast（全局拦截器误报）
2. 点"确认支付"报"订单已支付"（实际只是 status=0 的待支付记录被当成已支付）
3. 倒计时每次进来都从满格开始（忽略订单真实超时进度）
4. 余额 > 支付额却置灰"余额不足"（首帧 race：walletBalance=0 默认值早于 loadWallet() 完成）

**后端修复 — `PaymentServiceImpl.getPaymentByOrderId` 幂等化:**
- 查无记录时 **return null** 替代 `throw new BusinessException("支付记录不存在")`
- 理由：PaymentPage 初始化本来就要调这个接口"看看有没有历史支付记录"，首次进入查不到是**正常业务状态**，不应触发全局 error toast
- 文件：`module/payment/service/impl/PaymentServiceImpl.java:175-189`
- 前端 `api/payment.js` 无需改动（接口形态未变，仅语义由"404-like"→"可能为 null"）

**前端 `PaymentPage.vue` 综合重构（一文件改 6 处）:**
- ① 新增 `walletLoaded` ref + 改造 `balanceEnough` computed：未加载完乐观返回 true，加载完再按 `walletBalance >= orderAmount` 严格判断 → 修首帧置灰
- ② 新增 `pageReady` ref 门闩：onMounted 四步初始化（配置/订单/钱包/已有支付）全部完成才置 true，`handlePay` 入口判断 pageReady=false 时提示"页面正在加载，请稍候"
- ③ 新增 `orderCreateTime` ref + `computeCountdownFromOrder(timeoutMinutes)` 函数：拉 `getOrderDetail(orderId)` 取真实 `createTime`，倒计时 = `max(0, timeoutMinutes*60 - (Date.now() - createTime)/1000)`；进入时若已 ≤ 0 直接跳订单列表
- ④ 默认超时 `15*60` → `30*60`（15 分钟对下单-支付场景偏紧），后台 `payTimeoutMinutes` 配置仍优先生效
- ⑤ `getPaymentByOrderId` 返回 null 时静默跳过；返回 status=0 时复用已有 `id/orderNo/paymentType` 避免重复 createPayment 导致"订单已支付"假报
- ⑥ `getOrderDetail` 返回的 `paymentAmount` 优先于 route.query.amount（防 URL 参数被篡改，后端为准）
- 新增前端导入 `import { getOrderDetail } from '@/api/order'`

**与 2.4.4 主控文档的对齐核对:**
- 余额数据源：全程只通过 `getWallet()` → `/api/user/wallet` → `WalletVO.balance` → `tb_user.balance`，**与钱包页同源**，未引入本地缓存或 userStore 快照
- 前后端双重校验：按钮置灰（前）+ `BalanceService.changeBalance` 行锁负值校验（后）均保留
- 支付真实扣款：`PaymentServiceImpl.simulatePaymentSuccess` 中 paymentType=3 → `balanceService.changeBalance(userId, amount.negate(), 4, ...)` 链路未改动

**构建验证:**
- ✅ 后端 `mvn -DskipTests clean compile` 通过（413 sources，54.9s）
- ✅ 前端 `npm run build` 通过（2m 2s，PaymentPage-xxxx.js = 7.51 kB）

**数据库/接口/表结构变更:** 0
- 本轮为纯 bugfix + 前端重构
- `tb_user.balance` / `tb_balance_log` / `WalletVO` / `getWallet` 等 2.4.4 钱包链路一律**零改动**，仅修支付页的消费端逻辑

**回归清单:**
- [x] 新下单 → 支付页不再弹"支付记录不存在"
- [x] 点"确认支付"复用 status=0 记录，不再报"订单已支付"
- [x] 刷新支付页倒计时接续订单真实剩余时间
- [x] 余额充足账户选余额支付，按钮不再误置灰
- [x] 已支付订单再次进入正确跳转详情页

### 2026-04-15 v8.57 🎉 推荐系统跨模块联动完成 (8.3) — 4 条联动线全部上线
- ✅ **8.3.1 AI 识别 → 推荐课程 (RecommendServiceImpl.getRecognitionCourseRecommend):**
  - `EN_TO_ZH_TAG_MAPPING` 静态 Map（10 个英文天体词 → 中文课程关键词,nebula/galaxy/planet/star/moon/sun/comet/asteroid/cluster 等）
  - 流程: tb_recognition.machine_tags → 英中映射 → courseMapper.getRecommendByTags(limit*3) → Java 侧 getLearnedCourseIds 排除已学 → 不足时 getHotCourses 兜底
  - 前端: `RecognitionResult.vue` 在「推荐器材」下方新增「推荐课程」区块,空结果 v-if 隐藏
- ✅ **8.3.2 完课 → 推荐下一门 (RecommendServiceImpl.getNextCourseRecommend):**
  - 流程: 当前课程 tags → courseMapper.getRecommendByTags(limit*4) → 排除 self + 已学 → Java 侧 jaccardSimilarity 降序 → 不足时 getHotCourses 兜底
  - 前端: `CourseDetail.vue` 增加 watch(completedCount) 监听,当 `completedCount === chapterCount` 触发 el-dialog 方案 B(next-course-grid 网格弹窗)
  - 防重触发: `hasShownCompletionDialog` 标志位 + 屏蔽 APOD/Mars 每日课程
- ✅ **8.3.3 签到观测点 → 推荐器材 (RecommendServiceImpl.getSpotEquipmentRecommend):**
  - 规则前置 + 内容相似度: buildSpotTargetTags(altitude/light_pollution_level → 标签池) → productMapper.selectByTagsAny(limit*3) → jaccardSimilarity×0.6 + 销量归一化×0.4 排序
  - Redis 缓存: `recommend:spot:equip:{spotId}` TTL=30min(物理属性稳定,长缓存),try-catch 降级
  - 前端: `ObservationMap.vue` 签到成功后显示「适合这里的器材」面板,支持"首次签到"和"已签到再次打开弹窗"两种场景
- ✅ **8.3.4 帖子浏览 → 推荐相关帖子 (前端植入策略调整):**
  - 后端: 8.0/8.1 阶段已实现 logPostBrowse + getPostRecommend(浏览 tags 聚合 + Jaccard + hot_score 兜底)
  - **策略调整**: 用户反馈"直接植入现有推荐 Tab 更符合主流软件习惯",**不新增"为你推荐"Tab**
  - `ForumList.vue` 智能分流: `isRecommendMode = currentTab==='all' && !currentTag && pageNum===1` → 调用 getPostRecommend({limit:20})；其余 (关注/热门/有 tag/分页) 走原 getPostList；推荐接口失败降级到 getPostList
  - `ForumDetail.vue` onMounted 调用 logPostBrowse 埋点(try-catch 静默)
- ✅ **复用既有基础设施:** RecommendController 5 个跨模块端点早在 8.0 已就位,本轮只填充 Service 实现 + 前端调用,URL 完全复用避免破坏性变更
- ✅ **构建验证:** 后端 mvn clean compile 通过(413 sources compiled) + 前端 vite build 通过(20.08s)
- 📊 后端: RecommendServiceImpl.java 增量改动(+3 个核心方法 + 2 个 helper + EN_TO_ZH_TAG_MAPPING) | 前端: RecognitionResult.vue / CourseDetail.vue / ObservationMap.vue / ForumDetail.vue / ForumList.vue 共 5 个页面改造

### 2026-04-14 v8.56 🎉 推荐系统商品主场景完成 (8.2) — Redis 缓存 + 3 个前端推荐区块
- ✅ **Redis 缓存接入 (RecommendServiceImpl.java):**
  - `getHomeRecommend` → `recommend:home:{userId}` TTL=30min
  - `getSimilarProducts` → `recommend:similar:{productId}` TTL=1h
  - `getCartRecommend` → `recommend:cart:{userId}` TTL=10min
  - 所有读写 Redis 的代码用 try-catch 包裹，Redis 挂掉时自动降级直接走算法查库
- ✅ **埋点降级改造:** `logProductBrowse` / `logPostBrowse` 的 Redis SETNX 去重用 try-catch 包裹，Redis 异常时直接写库，不阻塞主流程
- ✅ **parseTags 兼容性修复:** 同时支持 JSON 数组 `["a","b"]` 和 CSV 字符串 `"a,b,c"` 两种历史 tags 格式，避免 `标签JSON解析失败` WARN 刷屏
- ✅ **前端「猜你喜欢」区块 (Home.vue):** 登录用户展示个性化推荐网格，未登录隐藏/降级热门，点击卡片前上报 `recordRecommendClick`
- ✅ **前端「相关商品」区块 (ProductDetail.vue):**
  - 商品详情页底部横向滚动卡片区
  - `onMounted` 调用 `logProductBrowse` 埋点（try-catch 包裹失败静默）
  - **关键 Bug 修复:** 添加 `watch(route.params.id)` 监听路由参数变化，解决 Vue Router 组件复用导致的"点击相关商品不跳转"问题（`/product/1` → `/product/2` 时自动 scrollTo(0) + 重新 init）
- ✅ **前端「为你推荐」区块 (CartPage.vue):** 购物车底部 5 列响应式网格，TTL=10min 短缓存匹配购物车变化频率
- ✅ **DTO 字段对齐修复:** 前端 `recordRecommendClick` 调用由 `{productId, source}` 修正为 `{recommendType, targetId}` 匹配后端 `RecommendClickDTO` 校验（`@NotBlank` / `@NotNull`），解决日志反复报"推荐类型/目标ID不能为空"的参数校验异常
- 📊 后端: RecommendServiceImpl.java 增量改动（缓存 + 降级）| 前端: Home.vue / ProductDetail.vue / CartPage.vue + api/recommend.js 已在 8.0 落地

### 2026-04-11 v8.55 🎉 推荐系统基础建设+核心算法完成 (8.0+8.1) — 论文核心创新点
- ✅ **数据库 +3 张表:** tb_browse_log(商品浏览记录) + tb_post_browse_log(帖子浏览记录) + tb_recommend_record(推荐曝光/点击追踪)
- ✅ **后端 module/recommend/ 全模块新建 (17个Java文件 + 1个XML):**
  - Entity: BrowseLog / PostBrowseLog / RecommendRecord
  - Mapper: BrowseLogMapper(@Select: selectRecentProductIds/selectUserIdsByProductId/selectUserBrowseCounts/deleteExpiredLogs) + PostBrowseLogMapper + RecommendRecordMapper(XML统计SQL)
  - DTO: BrowseLogDTO / PostBrowseLogDTO / RecommendClickDTO
  - VO: RecommendProductVO(含reason/score/algorithm) / RecommendPostVO
  - Service: RecommendService(14方法) + CfRecommendService(2方法)
  - Impl: RecommendServiceImpl(核心算法全部实现) + CfRecommendServiceImpl(余弦相似度CF)
  - Controller: RecommendController(10端点)
  - Task: RecommendScheduler(每周清理+CF预计算开关)
- ✅ **核心算法实现（论文第4/5章）:**
  - 标签 Jaccard 相似度: |A∩B|/|A∪B|
  - 商品综合内容相似度: tags×0.5 + category×0.3 + price×0.2 加权求和
  - Item-based 协同过滤: 余弦相似度 sim(i,j) = |N(i)∩N(j)| / √(|N(i)|×|N(j)|)
  - 冷启动三级兜底: interest_tags匹配 → 热门(sales×0.6+recommend×0.4) → 随机
  - 混合推荐融合: content×0.6 + CF×0.3 + coldstart×0.1
  - 帖子推荐: 浏览tags画像 → Jaccard → hot_score DESC 兜底
- ✅ **Redis SETNX 30min 浏览去重** (browse:dedup:{userId}:{productId})
- ✅ **@Async 异步曝光落表** tb_recommend_record (答辩展示点击率)
- ✅ **配置项:** pom.xml+spring-boot-starter-data-redis / application.yml+spring.redis+recommend.* / JwtInterceptor OPTIONAL_AUTH_LIST+5条推荐路径
- ✅ **前端:** src/api/recommend.js 10个API方法 / AccountSettings.vue presetTags 12→15项(与商品标签体系对齐)
- 📊 数据库表: 41→44张 | 后端Java文件: +17 | MapperXML: +1 | 前端API: +1

### 2026-04-09 v8.54 🔧 论坛通知中心页面 + 侧边栏 bug 修复 (7.8 补丁)
- ✅ 修复 Bug: router/index.js:117 `NotificationSettings` 路由 component 路径错误
  - **现象:** ForumLayout 左侧深色边栏"通知"按钮点击后毫无反应，无报错
  - **根因:** Vite 动态 `import('@/components/NotificationSettings.vue')` 指向不存在的文件，
    而真实文件在 `@/views/user/NotificationSettings.vue`；Vite 动态导入失败 UI 上完全静默，
    只在浏览器 console 报 `Failed to fetch dynamically imported module`，router-link 看起来像死链
  - **修复:** 将 router/index.js:117 的 component import 路径改为 `@/views/user/NotificationSettings.vue`
- ✅ 产品决策转向: 经用户确认后，论坛侧边栏"通知"按钮不再指向全局通知设置页，而是直接打开
  论坛专属通知中心（与首页 NotificationBell 功能重合度低，论坛有 9 种独立通知类型，独立页面更聚焦）
- ✅ 前端新增页面: `src/views/forum/ForumNotification.vue`（500+ 行）—— 论坛通知中心
  - 只拉取 `module: 'forum'` 通知（复用 `GET /api/notification/list?module=forum&...`，后端无需改动）
  - 顶部 Tabs: 全部 / 未读 / 已读（`readStatus` radio-group 切换）
  - 9 种论坛通知类型独立配色+图标（post_liked/commented/comment_replied/collected/
    user_followed/post_trending/approved/rejected/mentioned）
  - 点击通知 → markAsRead + 弹出详情对话框；详情内"前往查看"按钮调用 buildTargetRoute
    - `user_followed` → `/forum/user/{relatedId}`
    - 其他帖子相关类型 → `/forum/list?postId={relatedId}`
  - "全部已读"按钮调用 `markAllAsRead('forum')`（带确认）
  - 单条删除（带确认）
  - 分页 15/页
  - 自定义空状态图标+文案
  - 未读项 `#fff8f8` 浅粉背景 + hover 效果
- ✅ 前端路由: router/index.js 在 /forum 嵌套路由组下新增 `path: 'notification'` 子路由
  ```js
  {
    path: 'notification',
    name: 'ForumNotification',
    component: () => import('@/views/forum/ForumNotification.vue'),
    meta: { title: '论坛通知', requiresAuth: true }
  }
  ```
- ✅ 前端改造: ForumLayout.vue 侧边栏「通知」按钮
  - router-link `to` 从 `/notification/settings` 改为 `/forum/notification`
  - `isActive()` 新增 `notification` 分支用于高亮
  - 新增 `forumUnread: ref(0)` 状态 + 红色 `el-badge` 角标（只显示 forum 未读数）
  - `refreshForumUnread()` 直接取 `getUnreadCount().data.forum`，
    **避免 7.7.x 的 Object.values().reduce 双倍计数 bug**（`data` 里含 `total` 字段）
  - `onMounted`：首次 `refreshForumUnread()` + `setInterval(30000)` 轮询
  - `watch(() => route.path)`：路由切换立即刷新（从 ForumNotification 页返回时角标归零）
  - `onUnmounted`：clearInterval 清理定时器（防内存泄漏）
  - CSS 新增 `.nav-badge { margin-left: auto; }` + `:deep(.el-badge__content) transform`
- ✅ 前端页面数：57 → 58 (+1 ForumNotification.vue)
- ✅ API 接口数：保持不变（完全复用已有通知接口）
- ✅ 数据库：保持不变（0 张新表，0 条新通知模板）

### 2026-04-08 v8.45 🎉 论坛通知集成+热度计算+跨模块联动完成 (7.8)
- ✅ 后端枚举: NotificationType.java 论坛 9 个枚举，将 FOLLOW/MENTION/HOT_POST 重命名为
  USER_FOLLOWED/MENTIONED/POST_TRENDING（code 必须严格等于 NotificationHelper 发送的 type 字符串，
  否则 NotificationServiceImpl 用 module+type 拼出的 templateCode 取不到模板，会静默失败）
- ✅ 后端 NotificationHelper.java 新增 6 个论坛通知方法（位于 line 643-855）：
  sendPostLikedNotification / sendPostCommentedNotification / sendCommentRepliedNotification /
  sendPostCollectedNotification / sendPostTrendingNotification / sendMentionedNotification
  - 全部 @Async + try/catch，全部走 NotificationService 模板渲染
  - 防自通知统一在 helper 内部判断（authorId.equals(actorId) → return）
  - postTitle truncate(30), commentSnippet truncate(50)
  - sendMentionedNotification 支持批量 List<Long> mentionedUserIds，过滤掉 mentionerId 自己
- ✅ 后端 PostServiceImpl 注入 NotificationHelper：
  - likePost 仅在新点赞时（else 分支）触发 sendPostLikedNotification（取消点赞不通知）
  - collectPost 同上触发 sendPostCollectedNotification
- ✅ 后端 CommentServiceImpl 注入 NotificationHelper，addComment 移除 TODO 注释：
  - 顶级评论（parentComment==null）→ sendPostCommentedNotification(post.userId, ...)
  - 子回复（parentComment!=null）→ sendCommentRepliedNotification，被回复人优先取
    dto.replyToUserId（前端"回复某条子评论"），退化为 parentComment.userId（"回复父评论"）
- ✅ 后端 ForumScheduler.calcHotScores 实现完整逻辑（每小时执行）：
  - 查 status=2 + create_time >= now-7d 的所有帖子
  - 计算 score = (likes×1 + comments×2 + collects×3) / Math.pow(daysSince+2, 1.5)
  - 阈值 HOT_SCORE_THRESHOLD = 3.0（demo 模式偏低，便于触发演示）
  - 一生只发一次：oldIsHot==1 即固化保持 1，不会降回 0；is_hot 0→1 转换的帖子触发
    sendPostTrendingNotification
- ✅ 数据库: src/main/resources/sql/7.8_forum_notification_templates.sql 新增 7 条 INSERT
  （FORUM_POST_LIKED/COMMENTED/COMMENT_REPLIED/POST_COLLECTED/MENTIONED/USER_FOLLOWED/POST_TRENDING）
  加上 7.7 已有的 25/26（APPROVED/REJECTED）共 9 条论坛模板
  - ⚠️ 命令行执行 SQL 必须加 --default-character-set=utf8mb4，否则 mysql.exe 默认 latin1
    会把中文 UTF-8 字节当 latin1 单字节翻译，导致 ERROR 1406 Data too long for column
- ✅ 隐藏 Bug 修复: FollowServiceImpl 在 7.5 已经调用 sendUserFollowedNotification 但通知模板从未入库，
  从 7.5 到 7.7 期间所有"被关注"通知都静默失败。本次 7.8 INSERT FORUM_USER_FOLLOWED 模板补齐
- ✅ 前端 RecognitionResult.vue 新增「分享到论坛」按钮（share-card 内）：
  - handleShareToForum() 构造预填 title（"我用AI识别到了xxx"）+ content（完整天体列表+坐标）
    + images（[resultImageUrl]）+ tags（["AI识别","星空"]）
  - router.push('/forum/publish', query={ recognitionId, title, content, images, tags }）
- ✅ 前端 ForumPublish.vue onMounted 扩展：原先只在 isEdit 模式读 query 预填，
  现在 isEdit OR recognitionId 任一存在都加载预填字段，二者结构完全一致
- ✅ 前端 CourseDetail.vue goToForum 改造：从 query={ courseId } 改为 query={ tag: parseTags(course.tags)[0] }
  ForumList.vue 早已读取 ?tag=xxx 但旧实现传 courseId 无人接收
- ✅ 7.8 完结，论坛模块全部子节完成 🎉

### 2026-04-02 v8.39 🎉 评论系统+点赞收藏完成 (7.4)
- ✅ 后端新建：CommentLike.java + CommentLikeMapper.java（评论点赞实体+Mapper，与PostLike同模式）
- ✅ 后端新建：PostCommentMapper.xml（3个SQL：selectTopComments含replyCount子查询/countTopComments/selectChildComments + @Select selectNicknameByUserId）
- ✅ 后端实现：CommentServiceImpl 4方法（addComment两级评论/deleteComment作者校验/likeComment幂等切换/getCommentsByPostId含isLiked+isAuthor+replyCount）
- ✅ 后端实现：PostServiceImpl likePost/collectPost（INSERT/DELETE tb_post_like/tb_post_collect + 更新帖子计数，幂等切换）
- ✅ 后端修改：PostController 新增 GET /comment/list 端点（可选认证）；likeComment 返回 Boolean
- ✅ 后端修改：PostCommentVO 新增 isLiked/replyCount/isAuthor 字段
- ✅ 后端修改：JwtInterceptor 可选认证列表新增 /api/post/comment/list
- ✅ 数据库：新建 tb_comment_like 表（uk_comment_user唯一约束，已执行）
- ✅ 前端重写：ForumDetail.vue 完整评论区UI（小红书web风格）
  - 作者"作者"标识（粉底红字badge）
  - 内联回复框（发送/取消按钮，小红书风格）
  - 回复折叠（默认展示1条，展开/收起X条回复）
  - 评论点赞幂等切换（红心变色）
  - 帖子点赞/收藏幂等切换
  - 详情卡片尺寸放大至 90vw/90vh（匹配小红书）
- ✅ 前端修改：ForumList.vue 监听 @updated 事件，实时同步列表卡片点赞/收藏/评论计数
- ✅ 前端新增：api/forum.js 6个方法（getCommentList/addComment/deleteComment/likeComment/likePost/collectPost）

### 2026-03-22 v8.26 🎉 后台课程管理完成 (5.5)
- ✅ 后端新建：module/admin/controller/AdminCourseController.java（11个接口）
- ✅ 后端新建：module/admin/service/AdminCourseService.java + AdminCourseServiceImpl.java
  - deleteCourse 改用 LambdaUpdateWrapper.set(deleted=1)，绕过 @TableLogic 拦截
  - APOD同步事务：syncApodRange() @Transactional(NOT_SUPPORTED) + insertOneApodDay() @Transactional(REQUIRES_NEW)
  - self 字段 @Lazy 自注入，保证 REQUIRES_NEW 通过 AOP 代理生效
- ✅ 后端新建：3个DTO（CourseCreateDTO / ChapterCreateDTO / ApodSyncDTO）+ AdminCourseVO.java
- ✅ 后端新建：module/course/task/WikipediaSyncScheduler.java（本地硬编码15门书本课，@EventListener启动时自动同步，幂等去重）
- ✅ 数据库：执行 seed_courses_v8_26.sql（25门新课程 + 章节，共52门课程满足毕设要求）
- ✅ 数据库：执行 patch_v8_26_pending.sql（3条课程通知模板 INSERT，jump_url_template 路径已正确为 /course/{courseId}）
- ✅ 通知集成：NotificationType.java 新增 COURSE_CHAPTER_ADDED / COURSE_APOD_UPDATED / COURSE_COMPLETED
- ✅ 通知集成：NotificationHelper.java 新增 sendCourseChapterAddedNotification / sendCourseApodUpdatedNotification / sendCourseCompletedNotification
- ✅ 前端新建：api/admin/course.js（11个方法）
- ✅ 前端新建：views/admin/CourseManage.vue
  - APOD同步面板（日期范围+一键批量导入）
  - 课程列表（分页+搜索+type/status筛选+发布/下架/编辑/删除）
  - 新增/编辑课程弹窗（标签选择器：预设池chips+自定义输入，最多10个）
  - 章节管理抽屉（视频：平台模板+ID+iframe预览；图文：TinyMCE编辑器）
  - 自定义「插入图片」按钮（FileReader→base64→chapterForm.content，独立于TinyMCE）
  - waitForTinyMCE() 轮询机制，解决 CDN 异步加载竞态问题
- ✅ 前端改造：index.html 引入 TinyMCE Cloud CDN（API key已配置）
- ✅ 前端改造：AdminLayout.vue 新增课程管理菜单
- ✅ 前端改造：router/index.js 新增 admin/course 路由
- ✅ 后端配置：application.yml 新增 server.tomcat.max-http-form-post-size: 20MB（支持图文章节大body）
- ✅ Bug修复：NotificationBell.vue case 'course' 跳转路径 /course/detail/ → /course/
- ✅ API接口数：185 → 196 (+11)
- ✅ 前端页面数：49 → 50 (+1 CourseManage.vue)
- ✅ 通知模板：19 → 22 (+3 课程模块)
- ✅ 5.5 后台课程管理 全部完成 🎉

### 2026-03-21 v8.18 🎉 课程收藏与学习历史完成 (5.3) + 2.7.3完成
- ✅ course模块改造：CourseVO.java 新增 lastChapterTitle / completedCount 字段
- ✅ course模块改造：CourseProgressMapper.java 新增 insertIgnoreVisit()（INSERT IGNORE进入详情页埋点，不覆盖已有进度）
- ✅ course模块改造：CourseServiceImpl.java 修复3处：getCourseDetail埋点 / getMyHistory填充章节标题和进度 / getMyFavoriteList填充进度信息
- ✅ 前端新建：views/user/CourseFavorite.vue（3列网格+取消收藏二次确认+未学习badge+进度条+继续/开始学习）
- ✅ 前端新建：views/user/CourseHistory.vue（列表+封面+上次章节+进度条+未学习badge+点击跳转上次章节query.chapterId）
- ✅ 前端改造：views/course/CourseDetail.vue 修复：①收藏按钮去掉v-if=isLogin始终显示 ②query.chapterId精确定位章节
- ✅ 前端改造：views/course/CourseList.vue 修复：①收藏按钮始终显示 ②新增返回首页按钮 ③登录用户快捷入口（学习历史/课程收藏右上角）
- ✅ 前端改造：views/Login.vue 新增返回首页按钮（左上角，import ArrowLeft）
- ✅ 前端改造：UserLayout.vue import加VideoPlay；我的服务分组末尾新增「学习历史」「课程收藏」两项
- ✅ 前端改造：router/index.js /user children新增 course-favorite / course-history 两条路由（替换原错误占位）
- ✅ 前端改造：utils/request.js 修复401逻辑：只在requiresAuth页面跳转登录，游客页静默忽略（修复首页未登录被踢BUG）
- ✅ 2.7.3 MarsRoverSyncScheduler：随5.2完成 ✅（标记闭环）
- ✅ 前端页面数：47 → 49 (+2)
- ✅ 改造文件：8个

### 2026-03-16 v8.9 🌠 AI星图识别完成 (4.1+4.2)
- ✅ 新建 module/recognition 模块：Recognition.java / RecognitionMapper.java / RecognitionMapper.xml / SubmitRecognitionDTO.java / RecognitionVO.java / AstrometryService.java / AstrometryServiceImpl.java / AstrometryJobResult.java / RecognitionService.java / RecognitionServiceImpl.java / RecognitionController.java / RecognitionPollScheduler.java / RecognitionConfig.java
- ✅ 建表 tb_recognition (含 idx_user_id / idx_status / idx_create_time 索引)
- ✅ NotificationHelper.java 新增 sendRecognitionCompletedNotification / sendRecognitionFailedNotification
- ✅ tb_notification_template 插入 AI_RECOGNITION_COMPLETED / AI_RECOGNITION_FAILED 2条模板（共18个模板）
- ✅ application.yml：新增 astrometry.api-key/base-url；mapper-locations 从 mapper/*.xml 改为 mapper/**/*.xml（支持子目录扫描）
- ✅ 修复 Recognition.java dec字段为MySQL保留字，加 @TableField("`dec`") 注解
- ✅ 前端新建：api/recognition.js（4个API方法）
- ✅ 前端新建：views/recognition/StarRecognition.vue（上传页，Canvas压缩1200px/0.85）
- ✅ 前端新建：views/recognition/RecognitionWaiting.vue（等待页，5秒轮询+星空动画）
- ✅ 前端新建：views/recognition/RecognitionResult.vue（结果页，标注图片+坐标格式化）
- ✅ 前端新建：views/recognition/RecognitionHistory.vue（历史列表）
- ✅ 改造 router/index.js（新增4条识别路由 + user/recognition历史入口）
- ✅ 改造 Home.vue（goToAI跳转 /recognition，nav-link 改为 /recognition）
- ✅ API接口数：168 → 172 (+4)
- ✅ 前端页面数：39 → 43 (+4)
- ✅ 数据库表：26 → 27 (+1 tb_recognition)
- ✅ 通知模板：16 → 18 (+2 AI识别)
- ✅ 4.1 图片上传与识别提交 全部完成 🎉
- ✅ 4.2 识别状态轮询 全部完成 🎉

### 2026-03-12 v8.7 🖼️ 管理端图片展示完成
- ✅ 前端 AdminRecyclingManage.vue 改造：
    - 表格新增"图片"列，显示图片张数，点击跳详情
    - 详情弹窗加图片画廊 (90×90网格 + hover放大 + 点击大图预览)
    - el-image-viewer 全屏大图预览 (支持左右切换)
    - parseImages / openImagePreview 工具函数
- ✅ 2.5.3 二手回收模块全部完成 🎉

### 2026-03-12 v8.6 🖼️ 二手回收图片上传功能
- ✅ tb_recycling 新增 `images` MEDIUMTEXT 字段 (base64 JSON数组，最多6张)
- ✅ RecyclingApplyDTO 新增 `images` String 字段
- ✅ RecyclingVO / AdminRecyclingVO 新增 `images` 字段透传
- ✅ Recycling.java 实体新增 `images` 字段
- ✅ 前端 RecyclingList.vue 改造：
    - 申请弹窗加图片上传区 (点击/拖拽，Canvas压缩1200px/0.82质量，最多6张)
    - 列表卡片加缩略图条 (最多显示4张，超出显示 "+N")
    - 详情弹窗加图片画廊 (网格布局 + hover放大 + ZoomIn图标)
    - el-image-viewer 全屏大图预览 (支持左右切换)
- ✅ 已存在表执行: ALTER TABLE tb_recycling ADD COLUMN images mediumtext AFTER description

### 2026-03-11 v8.5 🎉 二手回收完成 (2.5.3)
- ✅ 新建 module/aftersale 新增：Recycling.java / RecyclingMapper.java / RecyclingApplyDTO.java / RecyclingVO.java / RecyclingService.java / RecyclingServiceImpl.java / RecyclingController.java (7个文件)
- ✅ admin模块扩展：RecyclingQueryDTO.java / RecyclingQuoteDTO.java / RecyclingRejectDTO.java / RecyclingArrangeDTO.java / AdminRecyclingVO.java / AdminRecyclingService.java / AdminRecyclingServiceImpl.java / AdminRecyclingController.java (8个文件)
- ✅ 建表 tb_recycling (含 UNIQUE KEY uk_recycle_no + idx_user_id + idx_status)
- ✅ NotificationHelper.java 新增 sendRecyclingCompleteNotification 方法
- ✅ NotificationType.java 已包含 RECYCLING_COMPLETED 枚举值 (状态改为已完成)
- ✅ tb_notification_template 插入 MALL_RECYCLING_COMPLETED 模板 (共16个模板)
- ✅ 前端新建：api/recycling.js (用户端6个API方法)
- ✅ 前端新建：api/admin/recycling.js (管理员端6个API方法)
- ✅ 前端新建：views/afterSale/RecyclingList.vue (卡片列表+提交弹窗)
- ✅ 前端新建：views/admin/RecyclingManage.vue (申请列表+操作弹窗)
- ✅ 改造 router/index.js (新增3条路由)
- ✅ 改造 AdminLayout.vue (侧边栏新增\"二手回收管理\"菜单项)
- ✅ 改造 UserLayout.vue (侧边栏新增\"二手回收\"菜单项)
- ✅ 改造 NotificationHelper.java (新增 sendRecyclingCompleteNotification + import BigDecimal/RoundingMode)
- ✅ API接口数：128 → 140 (+12)
- ✅ 前端页面数：32 → 34 (+2)
- ✅ 数据库表：24 → 25 (+1 tb_recycling)
- ✅ 通知模板：15 → 16 (+1 二手回收完成)
- ✅ 2.5 售后服务模块全部完成 🎉

### 2026-03-11 v8.4 🎉 器材保养提醒完成 (2.5.2)
- ✅ 新建 module/aftersale 新增：ServiceReminder.java / ServiceReminderMapper.java / ServiceReminderDTO.java / ServiceReminderVO.java / ServiceReminderService.java / ServiceReminderServiceImpl.java / ServiceReminderController.java (7个文件)
- ✅ 建表 tb_service_reminder (含 idx_user_id / idx_remind_date 索引)
- ✅ 前端新建：api/serviceReminder.js (4个API方法)
- ✅ 前端新建：views/afterSale/ServiceReminderList.vue (卡片列表+颜色预警+统计卡片+完成弹窗+自定义类型输入)
- ✅ 改造 router/index.js (新增 /user/service-reminder 路由)
- ✅ 改造 UserLayout.vue (新增"器材保养提醒"侧边栏菜单项)
- ✅ 改造 main.js (全局配置 Element Plus 中文 locale)
- ✅ 修复 BusinessException 调用方式：ResultCode枚举 → .getCode() 取Integer
- ✅ 修复 Result<Void> 返回：Result.success("字符串") → Result.success(null)
- ✅ API接口数：135 → 139 (+4)
- ✅ 前端页面数：31 → 32 (+1)
- ✅ 数据库表：23 → 24 (+1 tb_service_reminder)

### 2026-03-10 v8.2 🎉 安装预约完成 (2.5.1)
- ✅ 新建 module/aftersale 模块：Installation / InstallationMapper / InstallationMapper.xml / InstallationApplyDTO / InstallationVO / InstallationService / InstallationServiceImpl / InstallationController (8个文件)
- ✅ admin模块扩展：InstallationQueryDTO / InstallationConfirmDTO / InstallationAdminCancelDTO / AdminInstallationVO / AdminInstallationService / AdminInstallationServiceImpl / AdminInstallationController (7个文件)
- ✅ NotificationHelper.java 新增 sendInstallationConfirmedNotification 方法
- ✅ NotificationType.java 新增 INSTALLATION_CONFIRMED 枚举值
- ✅ tb_notification_template 插入 MALL_INSTALLATION_CONFIRMED 模板 (共15个模板)
- ✅ 建表 tb_installation (已创建，含 UNIQUE KEY uk_order_id 防重复预约)
- ✅ 前端新建：api/installation.js / api/admin/installation.js / views/afterSale/InstallationList.vue (卡片式订单+商品选择) / views/admin/InstallationManage.vue
- ✅ 改造 router/index.js (新增3条路由)
- ✅ 改造 AdminLayout.vue (新增安装预约管理侧边栏菜单)
- ✅ 改造 UserLayout.vue (售后待开发 → 安装预约)
- ✅ 改造 UserOverview.vue (订单格子 5列 → 6列，新增安装预约格)
- ✅ API接口数：129 → 135 (+6)
- ✅ 前端页面数：29 → 31 (+2)
- ✅ 数据库表：22 → 23 (+1 tb_installation)
- ✅ 通知模板：14 → 15 (+1 安装预约确认)

### 2026-03-10 v8.1 🎉 收藏功能规划 + 标题修正
- ✅ 修正 2.3/2.4 章节标题状态（开发中🔄 → 已完成✅）
- ✅ 新增 2.6 商品收藏模块规划（待开发，2.5售后服务完成后开发）
- ✅ 新增 tb_product_favorite 表设计 + 4个接口设计 + 文件结构规划

### 2026-03-10 v8.0 🎉 钱包系统完成 (2.4.4)
- ✅ user模块新建：BalanceLog / BalanceLogMapper / BalanceService / BalanceServiceImpl / WalletController / RechargeDTO / WithdrawDTO / WalletVO
- ✅ user模块改造：UserMapper 新增 selectByIdForUpdate + updateBalance
- ✅ payment模块改造：PaymentServiceImpl payment_type=3 真实扣款
- ✅ admin模块改造：AdminRefundServiceImpl doProcessRefund 判断支付方式，余额支付退款回钱包
- ✅ 前端新建：api/wallet.js + views/user/Wallet.vue（完整钱包页）
- ✅ 前端改造：CheckoutPage.vue（删掉余额抵扣，恢复简洁下单流程）
- ✅ 前端改造：PaymentPage.vue（余额支付显示余额 + 不足引导充值）
- ✅ 前端改造：UserOverview.vue（钱包实时刷新：30秒轮询 + onActivated + visibilitychange）
- ✅ router/index.js wallet路由改为 Wallet.vue
- ✅ API接口数：125 → 129 (+4 钱包接口)
- ✅ 2.4 个人空间模块全部完成 🎉

### 2026-03-09 v7.9 🎉 账号设置细节完善 (2.4.3)
- ✅ AccountSettings.vue 升级为三Tab（基本资料 / 头像照片 / 修改密码）
- ✅ 头像Tab：新增本地图片上传（FileReader转base64，直接存库，无需后端接口）
- ✅ 省市输入改为自定义两列弹窗选择器（左列省份 + 右列城市，内联34省数据，不依赖外部文件）
- ✅ 密码强度条改为ul/li方案（红/橙/绿三色固定，active控制亮暗，彻底解决scoped样式不显示问题）
- ✅ UserOverview.vue 新增storeToRefs同步（displayAvatar/displayNickname优先读store，头像保存后概览页实时更新）
- ✅ 数据库 tb_user.avatar 从 VARCHAR(255) 扩容为 MEDIUMTEXT（支持base64图片数据）

### 2026-03-08 v7.8 🎉 账号设置完成 (2.4.3)
- ✅ 新增 ChangePasswordDTO.java（含@NotBlank校验注解）
- ✅ UserService.java 新增 changePasswordSecure 方法声明
- ✅ UserServiceImpl.java 新增 changePasswordSecure 实现（旧密码验证 + 新旧不同 + 两次确认）
- ✅ UserController.java 新增 POST /api/user/change-password 端点（@Validated + JWT取userId）
- ✅ 前端 src/api/user.js 新增 changePasswordSecure 方法
- ✅ 新增 AccountSettings.vue（基本资料Tab + 修改密码Tab，替换原占位UserSettings.vue）
- ✅ 改造 router/index.js：settings路由组件改为 AccountSettings.vue
- ✅ 改造 Login.vue：onMounted 检测 password_changed 参数弹出提示
- ✅ 项目总体完成度：88% (+1%)

### 2026-03-07 v7.6 🎉 个人中心布局与概览页完成 (2.4.1)
- ✅ 新增 UserOverviewVO（聚合VO，含 totalOrders/totalSpent/totalReviews 三格统计）
- ✅ 新增 UserOverviewMapper（6个查询方法：状态统计/退款数/流水/累计订单/消费/评价）
- ✅ 新增 UserOverviewMapper.xml（CASE WHEN SUM 一条SQL聚合所有订单状态数）
- ✅ 新增 UserOverviewService + UserOverviewServiceImpl（聚合查询，各步骤独立容错）
- ✅ 新增 UserOverviewController（GET /api/user/overview）
- ✅ 新增 src/api/user/overview.js（getUserOverview API方法）
- ✅ 新增 UserLayout.vue（左侧导航容器，侧边栏仅保留"全部订单"入口，去掉状态子项）
- ✅ 新增 UserOverview.vue（3行紧凑布局：用户信息+统计格 / 订单格+钱包 / 最近订单预览）
- ✅ 新增 UserAddress.vue / UserWallet.vue / UserSettings.vue（占位页，待后续章节实现）
- ✅ 改造 router/index.js（新增 /user 嵌套路由，保留 /order/list 和 /review/my 原路径）
- ✅ 改造 Home.vue（个人中心跳转改为 /user/overview）
- ✅ 执行 ALTER TABLE tb_user ADD COLUMN balance（钱包余额字段）
- ✅ 执行 CREATE TABLE tb_balance_log（余额流水记录表）
- ✅ 项目总体完成度：85% (+1%)
- ✅ 商城模块：68% (+3%)


- ✅ PaymentPage.vue：读取支付方式开关动态过滤，读取 payTimeoutMinutes 驱动倒计时
- ✅ Register.vue：读取 registerEnabled，关闭时显示提示拒绝注册
- ✅ MaintenancePage.vue：新建维护模式页面，底部加管理员登录入口
- ✅ router/index.js：维护模式守卫放行 /login /register，管理员退出后可正常回登录页
- ✅ Home.vue：读取 mallName + copyright，联动基础设置
- ✅ JwtInterceptor + WebMvcConfig：放行 maintenance/register/payment 3个 GET 接口（无需登录）
- ✅ OrderScheduleTask.java：新建定时任务，每天凌晨2点读取 auto_close_days 自动关闭超时订单并释放库存
- ✅ AstronomyMallApplication.java：新增 @EnableScheduling
- ✅ OrderServiceImpl.calcFreight()：修复 setting_key 使用驼峰导致运费永远读不到的 bug（改为蛇形）
- ✅ tb_system_setting 初始化数据：setting_key 全部统一为蛇形（与后端读取一致）
- ✅ 项目总体完成度：84% (+2%)
- ✅ 后台管理：100% 全部完成

### 2026-03-05 v7.5.1 🔧 系统设置联动修复
- ✅ JwtInterceptor 白名单新增 3 条公开接口（maintenance/register/payment GET 无需登录）
- ✅ WebMvcConfig AdminInterceptor excludePathPatterns 排除同上 3 条
- ✅ router/index.js 维护模式守卫恢复（后端放行后可正常工作）
- ✅ Register.vue 注册开关联动、PaymentPage.vue 支付方式+超时时间联动正常生效
- ✅ MaintenancePage.vue 新建，维护模式下非管理员自动跳转

### 2026-03-05 v7.5 🎉 系统设置模块完成
- ✅ 完成后台系统设置模块完整功能（12个接口，6个配置分组）
- ✅ 后端新增 17 个 Java 文件（1实体+1Mapper+6DTO+6VO+1Service+1ServiceImpl+1Controller）
- ✅ 新建 tb_system_setting 表（键值对设计，含6组默认初始化数据）
- ✅ 前端新增 2 个文件（api/admin/setting.js + SystemSetting.vue）
- ✅ 修复 SystemSetting.vue 左侧菜单使用 el-menu 导致与 AdminLayout router菜单冲突，切换页面后所有页面空白的 Bug（改用普通 div 菜单）
- ✅ OrderServiceImpl 新增 calcFreight() 方法，创建订单时动态读取运费设置（不再写死 ¥0）
- ✅ CheckoutPage.vue 联动运费设置，实时展示运费、包邮提示、正确应付金额
- ✅ AdminLayout.vue menuItems 新增系统设置入口（Setting 图标）
- ✅ router/index.js 新增 /admin/setting 路由
- ✅ API接口总数: 115个 (+12个系统设置接口)
- ✅ 数据库表总数: 18张 (+1张 tb_system_setting)
- ✅ 后台管理整体完成度: 90%
- ✅ 项目总体完成度: 82% (+2%)

### 2026-03-03 v7.4 🎉 分类管理模块完成
- ✅ 完成后台分类管理模块完整功能（5个接口）
- ✅ 后端新增 6 个 Java 文件（Controller/Service/ServiceImpl/2个DTO/1个VO）
- ✅ 前端新增 2 个文件（api/admin/category.js + CategoryManage.vue）
- ✅ 修复树形构建 Bug（setChildren覆盖已添加子分类问题）
- ✅ 修复 el-dialog 弹窗在滚动页面中位置偏移问题
- ✅ API接口总数: 103个 (+5个分类管理接口)
- ✅ 后台管理整体完成度: 85%
- ✅ 项目总体完成度: 79% (+1%)

### 2026-03-02 v7.3 🎉 数据统计模块完成
- ✅ 完成后台数据统计模块完整功能（5个接口+10个图表）
- ✅ 后端新增 9 个 Java 文件（Controller/Service/Mapper/5个VO）
- ✅ 前端新增 2 个文件（statistics.js + Statistics.vue）
- ✅ 修复字段名：payment_amount / login_time / total_price / category_name / stock<=10
- ✅ 路由默认进入数据统计页 (/admin → /admin/statistics)
- ✅ API接口总数: 98个 (+5个统计接口)
- ✅ 后台管理整体完成度: 80%
- ✅ 项目总体完成度: 78% (+6%)

### 2026-02-25 v6.9 🎉 退款审核管理完成
- ✅ 完成退款审核管理模块完整功能
- ✅ 后端新增 8 个 Java 文件
  - AdminRefundController (5个接口)
  - AdminRefundService + AdminRefundServiceImpl
  - RefundQueryDTO + RefundAuditDTO
  - AdminRefundVO + AdminRefundDetailVO
- ✅ 前端新增 2 个文件
  - api/admin/refund.js (5个API方法)
  - views/admin/RefundManage.vue (完整退款审核页面)
- ✅ 修复 NotificationHelper 方法重复定义问题
- ✅ 修复所有错误 import 路径 (module.shop → module.order, UserHolder → UserContext)
- ✅ 退款成功后自动同步订单状态（待发货→已取消）
- ✅ 3种退款通知集成（审核通过/拒绝/到账）
- ✅ 退款失败重试机制
- ✅ 事务保证（@Transactional）
- ✅ API接口总数: 87个 (+5个退款审核接口)
- ✅ 后台退款审核完成度: 100%
- ✅ 后台管理整体完成度: 55%
- ✅ 项目总体完成度: 72% (+4%)
- ✅ 更新主控文档至v6.9

### 2026-02-06 v6.7 🔔 消息通知中心完成
- ✅ 完成消息通知中心核心功能(80%)
- ✅ 新增 3 张数据库表(tb_notification, tb_notification_template, tb_user_notification_setting)
- ✅ 初始化 14 个通知模板(10个商城+4个系统)
- ✅ 后端新增 20 个 Java 文件
  - 3个实体类 + 3个Mapper + 2个Service + 1个Helper
  - 1个Controller(7个接口) + 4个DTO + 3个VO + 2个枚举 + 1个配置
- ✅ 前端新增 3 个文件
  - 1个API封装 + 2个Vue组件(通知铃铛+通知设置)
- ✅ 支持 38 种通知类型定义(7个模块)
- ✅ 实现异步通知发送(@Async)
- ✅ 实现通知模板系统(变量替换)
- ✅ 实现通知偏好设置(用户可关闭)
- ✅ 实现智能跳转(relatedType + relatedId)
- ✅ 前端30秒自动刷新未读数
- ✅ 新增 7 个API接口
- ✅ 修复UserContext获取用户ID的Bug
- ✅ 修复OrderDetail和NotificationBell跳转Bug
- ✅ 创建完整开发文档(10个文档文件)
- ✅ API接口总数: 82个 (+7个通知接口)
- ✅ 数据库表总数: 17个 (+3张通知表)
- ✅ 后端代码新增: +2000行
- ✅ 前端代码新增: +500行
- ✅ 消息通知模块完成度: 80%
- ✅ 项目总体完成度: 75% (+5%)
- ✅ 更新主控文档至v6.7

### 2026-01-30 v6.6 🎉 订单物流派送功能完成
- ✅ 添加订单派送功能(运输中→派送中)
- ✅ 修复用户确认收货同步问题(OrderServiceImpl)
- ✅ 优化OrderManage.vue表格布局(使用min-width解决空白问题)
- ✅ 后端新增派送DTO、Service方法、Controller接口
- ✅ 前端新增派送按钮和派送方法
- ✅ 添加简化的消息通知提示(ElMessage)
- ✅ 消息通知功能规划(待后续开发完整版)
- ✅ API接口总数: 75个 (+1个派送接口)
- ✅ 更新4个核心文档至v6.6

### 2026-01-29 v6.5 🎉 订单管理模块完成 + 文档同步更新
- ✅ 完成订单管理模块前后端完整开发
- ✅ 后端: 9个Java文件(4个DTO + 2个VO + Service + ServiceImpl + Controller)
- ✅ 前端: 2个文件(order.js API + OrderManage.vue 页面)
- ✅ 新增 6 个订单管理API接口
- ✅ Order实体类添加4个物流相关字段(logisticsCompany, trackingNumber, logisticsStatus, adminRemark)
- ✅ 数据库tb_order表已包含物流字段(已验证)
- ✅ 实现完整订单管理功能(查询/详情/发货/取消/备注/导出)
- ✅ 前端页面支持多条件搜索、状态筛选、时间范围、Excel导出
- ✅ 更新主控文档至v6.5
- ✅ 更新数据库表文档
- ✅ 更新项目结构文档
- ✅ 更新公共前后端代码文档
- ✅ 后台管理完成度达到 40%
- ✅ 项目总体完成度达到 70%

### 2025-01-23 v6.3 🎉 商品批量导入/导出完成
- ✅ 完成商品批量导入功能
- ✅ 完成商品批量导出功能
- ✅ 完成导入模板下载功能
- ✅ 新增 ProductImportDTO 和 ProductExportVO
- ✅ 使用 Hutool ExcelUtil 处理Excel
- ✅ 完整的数据校验和异常处理
- ✅ 前端新增导入导出按钮
- ✅ 新增 3 个 API 接口
- ✅ 商品管理模块完成度达到 100%
- ✅ 更新项目完成度至 65%
- ✅ 更新技术亮点和答辩材料

### 2025-01-23 v6.2 🎉 商品调整日志完成
- ✅ 完成商品调整日志完整功能
- ✅ 新增 `tb_product_log` 表
- ✅ 新增 10 个相关文件
- ✅ 实现 AOP 切面自动记录
- ✅ 支持字段级别变更追踪
- ✅ 新增 2 个 API 接口
- ✅ 更新项目完成度至 60%
- ✅ 更新技术亮点和答辩材料

### 2025-12-12 v6.1 🎉 数据库表扩展完成
- ✅ 商品表添加 `tags` 字段（为推荐系统准备）
- ✅ 订单表添加物流相关字段（为后台管理准备）
- ✅ 添加相关索引优化查询性能
- ✅ 更新所有文档，标记扩展完成状态
- 📊 数据库结构已优化，后续开发无需频繁改表

### 2025-12-12 v6.0 🎉 重大更新
- ✅ 重构模块2.3为完整的后台管理系统
- ✅ 新增9大后台子模块详细设计
- ✅ 新增40+个后台管理API接口
- ✅ 新增管理员权限拦截器设计
- ✅ 新增操作日志AOP切面设计
- ✅ 新增数据统计SQL优化方案
- ✅ 新增Excel导出功能设计
- ✅ 新增后台前端页面规划
- ✅ 新增ECharts数据可视化方案
- ✅ 更新第8周详细开发计划(5-7天)
- ✅ 更新答辩演示流程(增加管理端7分钟)
- ✅ 预估完成后总体进度达75%
- 🆕 新增推荐系统完整设计方案
- 🆕 新增数据库表关联与扩展规划文档
- 🆕 新增推荐系统模块代码结构设计

### 2025-12-11 v5.0
- ✅ 支付系统模块开发完成
- ✅ 新增 tb_payment 和 tb_refund 表
- ✅ 完成10个支付相关API接口
- ✅ 完成 PaymentPage.vue 和 RefundPage.vue
- ✅ 项目完成度提升至55%

### 2025-12-08 v4.0
- ✅ 模块结构调整为7个主模块
- ✅ 新增支付记录表和退款记录表设计
- ✅ 新增管理员操作日志表设计

### 2025-11-14
- ✅ 商城模块第二阶段完成
- ✅ 实现购物车实时更新功能
- ✅ 实现订单状态流转

### 2025-11-12
- ✅ 商城模块第一阶段完成
- ✅ 解决 MySQL 聚合函数类型转换问题

### 2025-11-04
- ✅ 项目初始化完成
- ✅ 用户管理模块开发完成

---

## 🎓 项目学习总结

### 已掌握的技术点

#### 后端技术
1. **Spring Boot 核心**
   - 自动配置原理
   - 依赖注入和AOP
   - 拦截器配置
   - 全局异常处理

2. **MyBatis-Plus**
   - 代码生成器使用
   - 条件构造器
   - 分页查询
   - 字段自动填充
   - 逻辑删除

3. **JWT认证**
   - Token生成和解析
   - 请求拦截器
   - 白名单配置
   - ThreadLocal使用

4. **数据库设计**
   - 表结构设计
   - 索引优化
   - 类型转换处理
   - 事务管理

5. **支付系统**
   - 支付流水号生成
   - 支付状态管理
   - 退款流程设计
   - 状态同步机制

6. **AOP切面编程** 🆕
   - 环绕通知使用
   - 注解驱动开发
   - 切点表达式
   - 日志自动记录

7. **Excel文件处理** 🆕
   - Hutool ExcelUtil使用
   - Excel读写操作
   - 表头别名映射
   - Blob文件下载

8. **后台管理** 
   - 权限控制
   - AOP日志记录
   - 数据统计SQL
   - Excel导出

#### 前端技术
1. **Vue 3**
   - Composition API
   - 响应式原理
   - 生命周期钩子
   - 组件通信

2. **状态管理**
   - Pinia使用
   - Cookies管理
   - 自定义事件

3. **路由管理**
   - 路由守卫
   - 动态路由
   - 路由传参
   - 权限路由 

4. **UI组件**
   - Element Plus组件
   - 表单验证
   - 分页组件
   - 加载状态
   - Dialog对话框
   - 文件上传组件 🆕

5. **数据可视化** 
   - ECharts使用
   - 折线图/柱状图/饼图
   - 数据动态刷新

#### 项目经验
1. **前后端分离**
   - RESTful API设计
   - 跨域配置
   - 代理配置
   - 接口联调

2. **权限管理** 
   - 角色权限设计
   - 路由权限控制
   - 按钮权限控制

3. **审计日志系统** 🆕
   - 日志体系设计
   - AOP自动记录
   - 数据变更追踪
   - JSON格式存储

4. **Excel批量操作** 🆕
   - 批量导入设计
   - 批量导出优化
   - 数据校验体系
   - 错误处理机制

5. **代码规范**
   - 命名规范
   - 注释规范
   - 统一返回格式
   - 错误码定义

---

## 🚨 风险与应对

### 高风险项

1. **后台功能开发时间紧** 
   - 解决方案: 优先实现核心功能
   - 商品/订单/退款/统计为必做
   - 评价/用户/分类为重要
   - 日志/设置为可选
   
2. **Astrometry API不稳定**
   - 预案: 录制演示视频,准备多个成功案例
   - 说辞: "生产环境需自建识别服务"

3. **Python环境部署问题**
   - 预案: Docker容器化部署
   - 备用: 本地演示(localhost:5000)

### 中风险项

1. **数据统计SQL性能**
   - 预案: 添加必要索引
   - 使用缓存优化
   - 限制查询范围

2. **数据准备工作量大**
   - 预案: AI辅助生成,简化非核心数据
   - 进度: 已准备50+商品数据 ✅
   - **需要准备管理员测试场景** 

3. **第三方API限流**
   - 预案: 申请多个Key,本地缓存

---

## 🎯 项目特色

### 垂直领域定位
- 专注天文爱好者群体
- 细分市场精准定位
- 专业器材推荐

### 技术创新
- AI星图识别
- 语义推荐算法
- 地理位置智能联动
- 完整支付流程
- **双端权限管理系统** 
- **三层日志审计体系** 🆕

### 功能完整
- 完整购物流程
- 支付和退款
- **完整的后台管理** 
- **完整的日志系统** 🆕
- **数据统计分析** 

### 用户体验
- 实时状态同步
- 友好错误提示
- 响应式设计
- 流畅的交互
- 优雅的支付UI
- **专业的管理后台** 

---

## 📞 联系方式

**开发者:** [你的姓名]  
**邮箱:** [你的邮箱]  
**GitHub:** [你的GitHub]  
**学校:** [你的学校]  
**专业:** [你的专业]

---

## 🎉 总结

本项目是一个**功能完整、技术先进、用户体验良好**的天文器材商城系统。

### 核心优势:
1. ✅ **商城功能完整** - 从浏览到支付的完整闭环
2. ✅ **后台管理完善** - 商品/订单/退款/统计全覆盖 
3. ✅ **双端权限控制** - 用户端+管理端完整分离 
4. ✅ **三层日志审计** - 登录/操作/业务日志全覆盖 🆕
5. ✅ **技术栈先进** - Vue3 + Spring Boot + MyBatis-Plus + AOP
6. ✅ **代码规范** - 统一的命名、注释、异常处理
7. ✅ **用户体验** - 实时同步、友好提示、流畅交互
8. 🔄 **创新特色** - AI识别、推荐算法、地理位置联动

### 当前进度 (v7.5):
- **已完成:** 用户管理 + 商品购物 + 购物车订单 + 评价系统 + 支付系统 + 后台商品管理(100%) + 库存日志 + 商品日志 + 批量导入导出 + 后台订单管理(100%) + 消息通知中心(80%) + 后台退款审核(100%) + 后台评价管理(100%) + 后台用户管理(100%) + 后台数据统计(100%) + 后台分类管理(100%) + 后台操作日志(100%) + **后台系统设置(100%)** ✅ 🆕
- **开发中:** 商城通知集成 + 后台消息管理 🔥
- **待开发:** 论坛模块 + 课程模块 + 地理位置 + AI识别 + 推荐系统

### 已完成亮点:
- **完成度:** 82% 🎉 (+2%)
- **API接口:** 115个 (+12个系统设置接口)
- **数据库表:** 18张 (+1张 tb_system_setting)
- **可演示:** 用户端完整流程 + 管理端(商品/订单/退款/评价/用户/统计/分类/日志/**系统设置**)完整功能
- **答辩亮点:** 
  - ✅ 双端系统 (用户端+管理端)
  - ✅ 完整业务闭环 (浏览→购物→支付→发货→派送→确认收货→评价)
  - ✅ 完整退款审核闭环 (申请→审核→退款→状态同步→通知)
  - ✅ 三层日志审计 (登录日志+操作日志+业务日志)
  - ✅ Excel批量管理 (导入/导出/模板下载)
  - ✅ 订单全流程管理 (发货/派送/取消/备注/导出)
  - ✅ 物流状态完整流转 (未发货→运输中→派送中→已签收)
  - ✅ 消息通知中心设计 (独立通用模块,企业级架构)
  - ✅ 系统设置联动 (运费设置→下单自动计算→结算页实时展示) 🆕

---

**祝开发顺利! 加油! 🚀**

---

> 💡 **文档使用提示:**  
> 1. 定期更新开发进度  
> 2. 遇到问题及时记录  
> 3. 保持代码和文档同步  
> 4. 重要功能截图保存  
> 5. 测试日志记录功能 🆕
---

---

## 🔔 业务集成章节

> **说明:** 本章节详细说明各业务模块如何集成通知功能  
> **代码位置:** 所有通知代码都在 `com.astronomy.mall.module.notification` 包下  
> **集成方式:** 业务Service注入 `NotificationHelper` 并调用对应方法

---

### 📦 1. 订单模块通知集成

#### 1.1 用户端订单通知

**集成点1: 订单支付成功**
```
文件: com.astronomy.mall.module.payment.service.impl.PaymentServiceImpl
方法: simulatePaymentSuccess(Long paymentId, Long userId)
时机: 更新订单状态为"待发货"后
通知类型: ORDER_PAID

代码示例:
@Autowired
private NotificationHelper notificationHelper;

// 在支付成功后添加
notificationHelper.sendOrderPaidNotification(
    order.getUserId(),
    order.getOrderNo(),
    order.getPaymentAmount().toString(),
    order.getId()
);

通知效果:
- 标题: "订单支付成功"
- 内容: "您的订单{orderNo}已支付成功,金额¥{amount}"
- 跳转: /order/detail/{orderId}
- 优先级: 重要(1)
```

**集成点2: 订单确认收货**
```
文件: com.astronomy.mall.module.order.service.impl.OrderServiceImpl
方法: confirmReceipt(Long userId, Long orderId)
时机: 更新订单状态为"已完成"后
通知类型: ORDER_COMPLETED

代码示例:
notificationHelper.sendOrderCompletedNotification(
    order.getUserId(),
    order.getOrderNo(),
    order.getId()
);

通知效果:
- 标题: "订单已完成"
- 内容: "订单{orderNo}已完成,期待您的评价~"
- 跳转: /order/detail/{orderId}
- 优先级: 普通(0)
```

**集成点3: 用户取消订单**
```
文件: com.astronomy.mall.module.order.service.impl.OrderServiceImpl
方法: cancelOrder(Long userId, Long orderId)
时机: 回滚库存后
通知类型: ORDER_CANCELLED

代码示例:
notificationHelper.sendOrderCancelledNotification(
    order.getUserId(),
    order.getOrderNo(),
    order.getId()
);
```

**检查清单:**
- [ ] PaymentServiceImpl 注入 NotificationHelper
- [ ] simulatePaymentSuccess() 添加通知调用
- [ ] OrderServiceImpl 注入 NotificationHelper
- [ ] confirmReceipt() 添加通知调用
- [ ] cancelOrder() 添加通知调用
- [ ] 测试完整订单流程通知

---

#### 1.2 后台订单管理通知

**集成点4: 订单发货**
```
文件: com.astronomy.mall.module.admin.service.impl.AdminOrderServiceImpl
方法: shipOrder(OrderShipDTO dto)
时机: 更新物流信息后
通知类型: ORDER_SHIPPED

代码示例:
@Autowired
private NotificationHelper notificationHelper;

notificationHelper.sendOrderShippedNotification(
    order.getUserId(),
    order.getOrderNo(),
    dto.getLogisticsCompany(),
    dto.getTrackingNumber(),
    order.getId()
);

通知效果:
- 标题: "订单已发货"
- 内容: "您的订单{orderNo}已通过{logisticsCompany}发货,快递单号:{trackingNumber}"
- 跳转: /order/detail/{orderId}
- 优先级: 重要(1)
```

**集成点5: 订单派送**
```
文件: com.astronomy.mall.module.admin.service.impl.AdminOrderServiceImpl
方法: deliverOrder(OrderDeliverDTO dto)
时机: 更新物流状态为"派送中"后
通知类型: ORDER_DELIVERING

代码示例:
notificationHelper.sendOrderDeliveringNotification(
    order.getUserId(),
    order.getTrackingNumber(),
    order.getId()
);

通知效果:
- 标题: "订单派送中"
- 内容: "您的包裹正在派送中,请保持手机畅通,快递单号:{trackingNumber}"
```

**集成点6: 管理员取消订单**
```
文件: com.astronomy.mall.module.admin.service.impl.AdminOrderServiceImpl
方法: cancelOrder(OrderCancelDTO dto)
时机: 回滚库存后
通知类型: ORDER_CANCELLED

代码示例:
notificationHelper.sendOrderCancelledNotification(
    order.getUserId(),
    order.getOrderNo(),
    order.getId()
);
```

**检查清单:**
- [ ] AdminOrderServiceImpl 注入 NotificationHelper
- [ ] shipOrder() 添加发货通知
- [ ] deliverOrder() 添加派送通知
- [ ] cancelOrder() 添加取消通知
- [ ] 测试管理员操作通知流程

**预计工时:** 1天

---

### 📦 2. 商品收藏模块通知集成 (待开发)

#### 2.1 模块概述
```
目的: 用户收藏商品后，商品上架或降价时收到通知
依赖: 需要先开发商品收藏功能
新增表: tb_product_favorite
```

#### 2.2 数据库表设计
```sql
CREATE TABLE `tb_product_favorite` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';
```

#### 2.3 通知集成点

**集成点7: 商品上架通知**
```
文件: com.astronomy.mall.module.admin.service.impl.AdminProductServiceImpl
方法: updateStatus(Long productId, Integer status)
时机: 商品状态从0(下架)改为1(上架)时
通知类型: PRODUCT_ON_SALE

代码示例:
if (oldStatus == 0 && newStatus == 1) {
    // 查询收藏该商品的所有用户
    List<Long> userIds = favoriteMapper.selectUserIdsByProductId(productId);
    
    // 批量发送通知
    for (Long userId : userIds) {
        notificationHelper.sendProductOnSaleNotification(
            userId,
            product.getProductName(),
            product.getId()
        );
    }
}

通知效果:
- 标题: "商品上架提醒"
- 内容: "您关注的商品\"{productName}\"已上架"
- 跳转: /product/detail/{productId}
```

**集成点8: 商品降价通知**
```
文件: com.astronomy.mall.module.notification.scheduler.NotificationScheduler (新建)
方法: checkProductPriceDown() 
时机: 定时任务,每天凌晨2点检查
通知类型: PRODUCT_PRICE_DOWN

代码示例:
@Scheduled(cron = "0 0 2 * * ?")
public void checkProductPriceDown() {
    // 查询24小时内降价的商品
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    List<Product> priceDownProducts = productMapper.selectPriceDownProducts(yesterday);
    
    // 对每个商品发送通知给收藏用户
    for (Product product : priceDownProducts) {
        List<Long> userIds = favoriteMapper.selectUserIdsByProductId(product.getId());
        
        for (Long userId : userIds) {
            notificationHelper.sendProductPriceDownNotification(
                userId,
                product.getProductName(),
                product.getPrice().toString(),
                product.getId()
            );
        }
    }
}

通知效果:
- 标题: "商品降价提醒"
- 内容: "您关注的商品\"{productName}\"降价啦!现价¥{price}"
- 跳转: /product/detail/{productId}
```

#### 2.4 表关联关系
```
tb_product_favorite (收藏表)
  ├─→ tb_user (user_id) - 多对一
  └─→ tb_product (product_id) - 多对一

tb_notification (通知表)
  └─→ related_type='product', related_id=product_id

查询示例:
-- 查询收藏某商品的所有用户
SELECT user_id FROM tb_product_favorite WHERE product_id = 5;

-- 查询某商品的所有通知记录
SELECT * FROM tb_notification 
WHERE related_type = 'product' AND related_id = 5;
```

**检查清单:**
- [ ] 创建 tb_product_favorite 表
- [ ] 开发收藏功能 (增删查)
- [ ] 商品详情页添加收藏按钮
- [ ] AdminProductServiceImpl 集成上架通知
- [ ] 开发降价检查定时任务
- [ ] 集成降价通知
- [ ] 测试收藏和通知流程

**预计工时:** 3天

---

### 📦 3. 退款审核模块通知集成 (已完成 ✅) 🆕

#### 3.1 模块概述
```
目的: 管理员审核退款后，用户收到审核结果通知
状态: ✅ 已完成 (2026-02-25)
集成位置: AdminRefundServiceImpl
```

#### 3.2 通知集成点

**集成点9: 退款审核通过** ✅
```
文件: com.astronomy.mall.module.admin.service.impl.AdminRefundServiceImpl
方法: approveRefund(Long id, RefundAuditDTO auditDTO)
时机: 更新退款状态为"审核通过"后，自动触发退款处理前
通知类型: REFUND_APPROVED

实现代码:
notificationHelper.sendRefundApprovedNotification(
    refund.getUserId(),
    refund.getRefundAmount().toPlainString(),
    refund.getId(),
    refund.getOrderId()    // relatedType=order，点击跳转订单详情
);

通知效果:
- 标题: "退款审核通过"
- 内容: "您的退款申请已通过审核,退款金额¥{amount}将在1-3个工作日内到账"
- 跳转: /order/detail/{orderId}
- 优先级: 重要(1)
```

**集成点10: 退款审核拒绝** ✅
```
文件: com.astronomy.mall.module.admin.service.impl.AdminRefundServiceImpl
方法: rejectRefund(Long id, RefundAuditDTO auditDTO)
时机: 更新退款状态为"审核拒绝"后
通知类型: REFUND_REJECTED

实现代码:
notificationHelper.sendRefundRejectedNotification(
    refund.getUserId(),
    reason,   // adminRemark 或 "不符合退款条件"
    refund.getId(),
    refund.getOrderId()
);

通知效果:
- 标题: "退款审核拒绝"
- 内容: "您的退款申请未通过审核,原因:{reason}"
- 跳转: /order/detail/{orderId}
- 优先级: 重要(1)
```

**集成点11: 退款已到账** ✅
```
文件: com.astronomy.mall.module.admin.service.impl.AdminRefundServiceImpl
方法: doProcessRefund(Refund refund) [私有方法]
时机: 退款处理成功后
通知类型: REFUND_COMPLETED

实现代码:
notificationHelper.sendRefundCompletedNotification(
    refund.getUserId(),
    refund.getRefundAmount().toPlainString(),
    refund.getId(),
    refund.getOrderId()
);

通知效果:
- 标题: "退款已到账"
- 内容: "退款金额¥{amount}已到账,请查收"
- 跳转: /order/detail/{orderId}
```

#### 3.3 额外逻辑：退款成功同步订单状态 ✅ 🆕
```java
// doProcessRefund() 中新增逻辑
// 待发货(1)的订单退款 → 改为已取消(4)
Order order = orderMapper.selectById(refund.getOrderId());
if (order != null && order.getStatus() == 1) {
    order.setStatus(4);
    orderMapper.updateById(order);
}
// 注: 待收货(2)/已完成(3)的订单保持状态不变
```

#### 3.4 表关联关系
```
tb_refund (退款表)
  └─→ tb_notification (related_type='order', related_id=order_id)
  📌 注意: relatedType 存的是 order 而非 refund，
     这样前端点击通知可直接跳转到 /order/detail/:orderId

查询示例:
-- 查询某订单的退款相关通知
SELECT * FROM tb_notification 
WHERE related_type = 'order' AND related_id = 12
  AND type IN ('refund_approved', 'refund_rejected', 'refund_completed');
```

**检查清单:**
- [x] 创建 AdminRefundController (5个接口) ✅
- [x] 创建 AdminRefundService + ServiceImpl ✅
- [x] 开发退款审核前端页面 ✅
- [x] approveRefund() 集成通过通知 ✅
- [x] rejectRefund() 集成拒绝通知 ✅
- [x] doProcessRefund() 集成到账通知 ✅
- [x] 退款成功同步订单状态 ✅
- [x] 测试退款审核流程 ✅

---

### 📦 4. 消息管理后台模块 (部分完成)

#### 4.1 模块概述
```
目的: 管理员管理系统公告、查看通知记录、编辑模板
位置: 后台管理系统新增"消息管理"菜单
新增表: tb_announcement (系统公告表)
```

#### 4.2 数据库表设计
```sql
CREATE TABLE `tb_announcement` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(100) NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `type` varchar(20) DEFAULT 'announcement' COMMENT '公告类型',
  `priority` tinyint(4) DEFAULT '0' COMMENT '优先级',
  `target_type` varchar(20) DEFAULT 'all' COMMENT '目标类型(all/level/custom)',
  `target_users` text COMMENT '目标用户ID(JSON)',
  `send_status` tinyint(4) DEFAULT '0' COMMENT '发送状态(0草稿/1已发送)',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `send_count` int(11) DEFAULT '0' COMMENT '发送数量',
  `creator_id` bigint(20) DEFAULT NULL,
  `creator_name` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_send_status` (`send_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';
```

#### 4.3 子模块划分

**4.3.1 系统公告管理 ✅ (2026-03-16完成)**
```
功能:
- 创建公告
- 编辑公告
- 发送公告(批量发送通知，复用 tb_notification + insertBatch)
- 查看发送记录
- 按 priority 过滤（HAVING MIN(n.priority)=#{dto.priority}）

API接口 (5个) ✅:
POST   /api/admin/announcement          - 创建公告
GET    /api/admin/announcement/list     - 公告列表
GET    /api/admin/announcement/{id}     - 公告详情
DELETE /api/admin/announcement/{id}     - 删除公告
POST   /api/admin/announcement/send/{id} - 发送公告

用户端接口 (+1):
GET    /notification/announcement/{id}  - 用户查看公告详情 (NotificationController)

前端页面 ✅:
views/admin/AnnouncementManage.vue ✅ (公告列表+创建/编辑对话框+发送确认)
views/notice/NoticeDetail.vue ✅ (公告详情页, 路由: /notice/detail)
AdminLayout.vue 改造 ✅ (新增 Bell 图标 + 系统公告菜单)
router/index.js 改造 ✅ (新增 /notice/detail 路由)
```

**发送公告的通知集成:**
```java
// 文件: AnnouncementServiceImpl.java
// 方法: sendAnnouncement(Long announcementId)

// 1. 确定目标用户
List<Long> targetUserIds = determineTargetUsers(announcement);

// 2. 批量创建通知
List<Notification> notifications = new ArrayList<>();
for (Long userId : targetUserIds) {
    Notification notification = Notification.builder()
        .userId(userId)
        .module("system")
        .type("announcement")
        .title(announcement.getTitle())
        .content(announcement.getContent())
        .relatedId(announcement.getId())
        .relatedType("announcement")
        .priority(announcement.getPriority())
        .build();
    notifications.add(notification);
}

// 3. 批量插入数据库
notificationMapper.insertBatch(notifications);

// 4. 更新公告发送状态
announcement.setSendStatus(1);
announcement.setSendTime(LocalDateTime.now());
announcement.setSendCount(targetUserIds.size());
announcementMapper.updateById(announcement);
```

**表关联关系:**
```
tb_announcement (公告表)
  └─→ tb_notification (一对多)
      related_type='announcement'
      related_id=announcement_id

查询示例:
-- 查询某公告生成的所有通知
SELECT * FROM tb_notification 
WHERE related_type = 'announcement' AND related_id = 1;

-- 统计公告的已读率
SELECT 
  COUNT(*) as total,
  SUM(is_read) as read_count
FROM tb_notification
WHERE related_type = 'announcement' AND related_id = 1;
```

**4.3.2 通知记录管理 ✅ (2026-03-16完成)**
```
功能:
- 查看所有通知记录（全用户，后台视角）
- 按用户/模块/类型/已读/时间筛选
- 统计分析（总量/模块分布/近30天趋势/类型Top10）
- 批量逻辑删除

API接口 (3个) ✅:
GET    /api/admin/notification/record/list   - 通知记录列表（分页+筛选）
GET    /api/admin/notification/record/stats  - 统计分析
DELETE /api/admin/notification/record/batch  - 批量删除

前端页面 ✅:
views/admin/NotificationRecord.vue（列表+筛选+统计Tab含ECharts图表）

📌 实现说明:
- AdminNotificationMapper 独立于 notification 模块 NotificationMapper，面向后台跨用户查询
- 统计SQL: 一次 selectCountSummary + 三个分组查询，覆盖饼图/柱状图/折线图
- 批量删除使用逻辑删除（deleted=1），不物理删除
- AdminNotificationServiceImpl 内置模块/优先级中文标签映射 Map
```

**4.3.3 通知模板管理 ✅ (2026-03-16完成)**
```
功能:
- 查看所有模板（按模块分组展示）
- 编辑模板标题/内容/跳转链接/备注
- 启用/禁用模板（禁用后 sendNotification 查 enabled=1 时跳过发送）
- 模板预览（变量占位符替换示例，支持实时填入示例值）
- 恢复默认内容（与数据库初始化INSERT完全一致的17个默认值）

API接口 (5个) ✅:
GET  /api/admin/notification/template/list       - 模板列表（按模块分组）
GET  /api/admin/notification/template/{id}       - 模板详情
PUT  /api/admin/notification/template/{id}       - 编辑模板
POST /api/admin/notification/template/status     - 启用/禁用
POST /api/admin/notification/template/reset/{id} - 恢复默认

前端页面 ✅:
views/admin/NotificationTemplate.vue（按模块分组卡片+编辑/预览/启禁/恢复默认对话框）

📌 实现说明:
- AdminNotificationTemplateServiceImpl 复用 notification 模块的 NotificationTemplateMapper，不新建Mapper
- "恢复默认" 通过 DEFAULT_TEMPLATES 静态 Map 实现，Key=code，Value=[title,content,jumpUrl]
- 禁用 SYSTEM_ANNOUNCEMENT 不影响发公告（sendAnnouncement 直接批量插库，绕过模板检查）
- 模板分组按 MODULE_ORDER 排序: mall → system → forum → course → location → recommend → ai
```

#### 4.4 管理菜单扩展
```
后台管理
├── 商品管理 ✅
├── 订单管理 ✅
└── 消息管理 🆕
    ├── 系统公告 ✅ (3.4.1完成)
    ├── 通知记录 ✅ (3.4.2完成)
    └── 通知模板 ✅ (3.4.3完成)
```

**检查清单:**
- [x] 开发 AdminAnnouncementController (5个接口) ✅
- [x] 开发 AnnouncementManage.vue ✅
- [x] 开发 NoticeDetail.vue ✅
- [x] 改造 AdminLayout.vue（Bell图标+菜单）✅
- [x] 改造 router/index.js（/notice/detail路由）✅
- [x] ALTER TABLE tb_notification ADD COLUMN deleted ✅
- [x] 开发 AdminNotificationController (3个接口) ✅
- [x] 开发 NotificationRecord.vue ✅
- [x] 开发模板管理接口 (5个) ✅
- [x] 开发 NotificationTemplate.vue ✅
- [x] 改造 AdminLayout.vue（通知记录+通知模板菜单）✅
- [x] 改造 router/index.js（notification-record/notification-template路由）✅

**预计剩余工时:** 0天（消息管理后台全部完成 🎉）

---

### 📦 5. 论坛模块通知集成 ⬜

#### 5.1 模块概述
```
开发时机: 第14-15周开发论坛模块时同步集成
通知类型: 9种（详细规划见主控文档 7.7节）
代码位置: 仍在 notification 模块下（NotificationHelper.java + NotificationType.java）
完整SQL: 见 7.7节通知模板 INSERT SQL（9条，含完整模板内容）
```

#### 5.2 通知触发位置速查

```java
// PostServiceImpl.java
likePost()   → notificationHelper.sendPostLikedNotification(post.getUserId(), userId, post.getTitle(), postId)
addComment() → notificationHelper.sendPostCommentedNotification(...)  // 评论帖子
             → notificationHelper.sendCommentRepliedNotification(...)  // 回复评论
             → notificationHelper.sendMentionedNotification(...)       // @提及（批量）
collectPost()→ notificationHelper.sendPostCollectedNotification(...)

// AdminPostServiceImpl.java
auditPost(approve) → notificationHelper.sendPostApprovedNotification(...)
auditPost(reject)  → notificationHelper.sendPostRejectedNotification(...)

// FollowServiceImpl.java
follow() → notificationHelper.sendUserFollowedNotification(followedId, followerId)

// ForumScheduler.java（每小时，is_hot从0→1时触发）
calcHotScores() → notificationHelper.sendPostTrendingNotification(...)
```

> ⚠️ 所有论坛通知均需加「防自通知」逻辑：若操作者==被通知者，跳过不发送。
> 详见 7.7节 NotificationHelper 方法实现，已全部加入防自通知判断。

**预计工时:** 论坛模块总计10天，其中通知集成1.5天

---

### 📦 6. 课程模块通知集成 ⬜

#### 6.1 模块概述
```
开发时机: 课程模块开发时同步集成（随5.2/5.3/5.5同步开发）
通知类型: 3种
代码位置: 仍在 notification 模块下（NotificationHelper.java + NotificationType.java）
```

#### 6.2 NotificationType.java 新增枚举值
```java
// 课程模块 (3种) - 新增到 NotificationType.java
COURSE_CHAPTER_ADDED("course_chapter_added", "课程新增章节"),
COURSE_APOD_UPDATED("course_apod_updated",   "NASA每日图片已更新"),
COURSE_COMPLETED("course_completed",         "课程学习完成"),
```

#### 6.3 通知模板 INSERT SQL（课程模块开发完成后执行）
```sql
INSERT INTO `tb_notification_template`
(`code`, `module`, `type`, `title_template`, `content_template`, `jump_url_template`, `variables`, `enabled`, `remark`)
VALUES
-- 通知1: 管理员新增章节，通知收藏了该课程的用户
('COURSE_CHAPTER_ADDED', 'course', 'course_chapter_added',
 '课程更新啦',
 '您收藏的课程《{courseTitle}》新增了章节：{chapterTitle}，快去学习吧 📚',
 '/course/{courseId}',
 '{"courseTitle":"课程名称","chapterTitle":"新章节标题","courseId":"课程ID"}',
 1, '课程新增章节通知'),

-- 通知2: APOD定时同步成功，通知收藏「NASA每日天文图片精选」的用户
('COURSE_APOD_UPDATED', 'course', 'course_apod_updated',
 'NASA今日天文图片已更新',
 'NASA今日天文图片精选已更新：{apodTitle}，快来一起探索宇宙 🌌',
 '/course/{courseId}',
 '{"apodTitle":"APOD标题","courseId":"APOD课程ID"}',
 1, 'NASA APOD每日更新通知'),

-- 通知3: 用户学完课程全部章节，发给用户自己
('COURSE_COMPLETED', 'course', 'course_completed',
 '恭喜你完成了一门课程！',
 '🎉 你已完成课程《{courseTitle}》的全部学习内容，继续探索更多课程吧！',
 '/course/{courseId}',
 '{"courseTitle":"课程名称","courseId":"课程ID"}',
 1, '课程学习完成通知');
```

#### 6.4 NotificationHelper.java 新增方法
```java
// 文件: notification/helper/NotificationHelper.java

// 通知1: 课程新增章节 → 通知所有收藏了该课程的用户（@Async批量发送）
@Async
public void sendCourseChapterAddedNotification(Long courseId, String courseTitle, String chapterTitle) {
    // 1. 查询收藏了该课程的用户ID列表
    List<Long> favoriteUserIds = courseFavoriteMapper.selectUserIdsByCourseId(courseId);
    if (CollUtil.isEmpty(favoriteUserIds)) return;
    // 2. 批量发送通知
    Map<String, String> params = Map.of(
        "courseTitle", courseTitle,
        "chapterTitle", chapterTitle,
        "courseId", courseId.toString()
    );
    for (Long userId : favoriteUserIds) {
        sendNotification(userId, "COURSE_CHAPTER_ADDED", params, courseId, "course");
    }
}

// 通知2: APOD每日更新 → 通知所有收藏了APOD课程的用户（@Async批量发送）
@Async
public void sendCourseApodUpdatedNotification(Long apodCourseId, String apodTitle) {
    List<Long> favoriteUserIds = courseFavoriteMapper.selectUserIdsByCourseId(apodCourseId);
    if (CollUtil.isEmpty(favoriteUserIds)) return;
    Map<String, String> params = Map.of(
        "apodTitle", apodTitle,
        "courseId", apodCourseId.toString()
    );
    for (Long userId : favoriteUserIds) {
        sendNotification(userId, "COURSE_APOD_UPDATED", params, apodCourseId, "course");
    }
}

// 通知3: 课程学习完成 → 通知用户自己（单条）
@Async
public void sendCourseCompletedNotification(Long userId, Long courseId, String courseTitle) {
    Map<String, String> params = Map.of(
        "courseTitle", courseTitle,
        "courseId", courseId.toString()
    );
    sendNotification(userId, "COURSE_COMPLETED", params, courseId, "course");
}
```

#### 6.5 通知触发位置

**触发点1: 管理员新增/发布章节**
```java
// 文件: AdminCourseServiceImpl.java
// 方法: addChapter(ChapterCreateDTO dto)

// 插入章节成功后
Course course = courseMapper.selectById(dto.getCourseId());
notificationHelper.sendCourseChapterAddedNotification(
    course.getId(),
    course.getTitle(),
    dto.getTitle()
);
// @Async 异步发送，不阻塞章节保存流程
```

**触发点2: APOD定时同步成功**
```java
// 文件: APODSyncScheduler.java
// 方法: syncTodayApod()

// 成功插入新章节后
notificationHelper.sendCourseApodUpdatedNotification(
    apodCourse.getId(),
    apodData.getTitle()    // NASA APOD返回的英文标题
);
```

**触发点3: 用户获取章节内容时检测完课**
```java
// 文件: CourseServiceImpl.java
// 方法: getChapter(Long chapterId, Long userId)

// 更新 tb_course_progress 后检测是否全部完成
int totalChapters = course.getChapterCount();
int completedCount = JSON.parseArray(progress.getCompletedChapters()).size();
// ⚠️ 排除APOD课和火星课：这两类课程每天自动增章节，永远无法"完课"
boolean isAutoSyncCourse = course.getIsApodCourse() == 1 || course.getIsMarsCourse() == 1;
if (!isAutoSyncCourse && completedCount >= totalChapters && totalChapters > 0) {
    // 防重复：检查该课程是否已发送过完成通知（可在progress中加 is_notified 字段，或直接发送幂等）
    notificationHelper.sendCourseCompletedNotification(
        userId,
        course.getId(),
        course.getTitle()
    );
}
```

#### 6.6 检查清单
- [ ] NotificationType.java 新增3个枚举值（COURSE_CHAPTER_ADDED / COURSE_APOD_UPDATED / COURSE_COMPLETED）
- [ ] NotificationHelper.java 新增3个方法
- [x] 执行3条通知模板 INSERT SQL ✅ 2026-03-22
- [ ] AdminCourseServiceImpl.addChapter() 集成 sendCourseChapterAddedNotification
- [ ] APODSyncScheduler 集成 sendCourseApodUpdatedNotification
- [ ] CourseServiceImpl.getChapter() 集成 sendCourseCompletedNotification（完课检测）

**预计工时:** 0.5天（随课程模块开发同步完成）

---

---

## ✅ 总检查清单

### 商城模块 (10天)
- [x] 订单支付通知 (PaymentServiceImpl) ✅
- [x] 订单完成通知 (OrderServiceImpl) ✅
- [x] 订单取消通知 (OrderServiceImpl) ✅
- [x] 订单发货通知 (AdminOrderServiceImpl) ✅
- [x] 订单派送通知 (AdminOrderServiceImpl) ✅
- [x] 管理员取消通知 (AdminOrderServiceImpl) ✅
- [x] 商品收藏功能开发 ✅
- [x] 商品上架通知集成 ✅
- [x] 商品降价通知 + 定时任务 ✅
- [x] 退款审核功能开发 ✅
- [x] 退款审核通知集成 (通过/拒绝/到账) ✅
- [x] 退款成功同步订单状态 ✅

### 后台管理 (4天)
- [x] 系统公告管理 (5接口 + 2页面) ✅ 2026-03-16完成
- [x] 通知记录管理 (3接口 + 页面) ✅ 2026-03-16完成
- [x] 通知模板管理 (5接口 + 页面) ✅ 2026-03-16完成
- [x] 添加"消息管理"菜单 ✅

### 地理位置模块 (5天)
- [x] 6.0 建3张表（tb_observation_spot / tb_user_checkin / tb_spot_rating）✅ 2026-03-23
- [x] 6.0 ALTER TABLE tb_user longitude/latitude字段注释更新 ✅ 2026-03-23
- [x] 6.0 执行35条全国优质观测点种子数据SQL ✅ 2026-03-23
- [x] 6.0 配置 application.yml `amap.web-key: 2ce80d8a2c6b51db75fd2c6603086432` ✅ 2026-03-23
- [x] 6.0 配置 .env `VITE_AMAP_JS_KEY=45d0e6381bae07b6c8fbcb5981c34aa9` ✅ 2026-03-23
- [x] 6.0 main.js 统一加载高德JS API（含AMap.Geocoder插件）✅ 2026-03-23
- [x] 6.0 创建 module/location/ 全部文件骨架（22个文件）✅ 2026-03-23
- [x] 6.0 NotificationType.java 新增2个枚举值 ✅ 2026-03-23
- [x] 6.0 NotificationHelper.java 新增2个方法（sendCheckinNotification / sendWeatherSuitableNotification）✅ 2026-03-23
- [x] 6.0 执行2条通知模板INSERT SQL（LOCATION_WEATHER_SUITABLE 初始 enabled=0）✅ 2026-03-23
- [x] 6.0 WebMvcConfig 白名单新增4条（spots / spot/* / weather / tonight）✅ 2026-03-23
- [x] 6.1 后端3个接口（spots / spot/{id} / spot/{id}/rating）+ ObservationMap.vue 完整实现（天气/签到TODO占位）✅ 2026-03-24
- [ ] 6.2 后端2个接口（weather / tonight）+ ObservationMap.vue 天气区块补全
- [ ] 6.3 后端2个接口（checkin / checkin/my）+ ObservationMap.vue 签到区块补全 + CheckinHistory.vue
- [ ] 6.4 后端1个接口（PUT /user/location）+ UserAddress.vue + CheckoutPage.vue 地址联动改造
- [ ] 6.5 后端5个接口（管理员端）+ ObservationSpotManage.vue + AdminLayout.vue 菜单

### 推荐系统模块 (4.5天)
- [ ] 建表 tb_browse_log（商品浏览记录，含Redis 30分钟去重）
- [ ] 建表 tb_post_browse_log（帖子浏览记录，含Redis 30分钟去重）
- [ ] application.yml 新增 Redis 配置 + pom.xml 新增 spring-boot-starter-data-redis
- [ ] 开发 module/recommend/（Controller + RecommendServiceImpl + CfRecommendServiceImpl）
- [ ] 开发 RecommendScheduler（每6小时重算CF矩阵 + 每周清理browse_log + post_browse_log）
- [ ] 新增 RecommendPostVO（id/title/coverImage/hotScore/tags/authorNickname）
- [ ] 商品推荐：home / similar / cart 三个接口
- [ ] 跨模块联动：recognition→课程 / 完课→下一门 / 签到→器材 三个接口
- [ ] 帖子推荐：POST /api/recommend/post/browse 埋点 + GET /api/recommend/post/list
- [ ] 浏览埋点：ProductDetail.vue onMounted 调用商品埋点接口
- [ ] 浏览埋点：ForumDetail.vue onMounted 调用帖子埋点接口（失败静默）
- [ ] 前端改造：Home.vue「猜你喜欢」 / ProductDetail.vue「相关商品」 / CartPage.vue「为你推荐」
- [ ] 前端改造：RecognitionResult.vue「推荐课程」 / CourseDetail.vue 完课推荐 / ObservationMap.vue 签到推荐
- [ ] 前端改造：ForumList.vue 新增「推荐」Tab（登录用户可见）
- [ ] NotificationType.java 新增 RECOMMEND_PRODUCT / RECOMMEND_COURSE
- [ ] 执行2条通知模板 INSERT SQL

### 论坛模块 (10天)
- [ ] ALTER TABLE tb_post 新增 images/tags/status/reject_reason/hot_score/is_hot/recognition_id/deleted 字段 + FULLTEXT INDEX
- [ ] ALTER TABLE tb_post_comment 新增 reply_to_user_id/reply_to_username/deleted 字段
- [ ] CREATE TABLE tb_user_follow（关注关系表）
- [ ] CREATE TABLE tb_search_log（搜索日志表，热门搜索统计）
- [ ] 直接建表 tb_post_like / tb_post_collect / tb_post_image / tb_post_tag
- [ ] 开发 module/forum/（PostController 19接口 + AdminPostController 8接口 + PostServiceImpl + SearchServiceImpl + CommentServiceImpl + FollowServiceImpl）
- [ ] 开发 ForumScheduler（每小时热度计算 + 热门通知触发）
- [ ] NotificationType.java 新增9个枚举值（FORUM_*）
- [ ] NotificationHelper.java 新增9个通知方法（含防自通知逻辑）
- [ ] 执行9条通知模板 INSERT SQL
- [ ] 开发 ForumList.vue / ForumDetail.vue / ForumPublish.vue / ForumSearch.vue / UserProfile.vue / ForumManage.vue
- [ ] 开发 api/forum.js（19个方法）+ api/admin/forum.js（8个方法）
- [ ] RecognitionResult.vue 新增「分享到论坛」按钮（跨模块联动）
- [ ] AdminLayout.vue 新增「论坛管理」菜单 + 待审核数角标
- [ ] router/index.js 新增5条论坛路由（含 /forum/search）

### 课程模块 (6天)
- [x] 建5张课程相关表 (course含is_mars_course字段/chapter/progress/favorite/review) ✅ 2026-03-20
- [x] 开发用户端接口 (8个已完成，5.4新增recommend) ✅ 2026-03-21
- [x] 开发管理员端接口 (11个) ✅ 2026-03-22
- [x] 开发APODSyncScheduler定时任务（**注入NasaApiService**，不重复实现API调用）✅ 2026-03-20
- [x] 开发MarsRoverSyncScheduler定时任务（凌晨2:30，火星车照片同步）✅ 2026-03-20
- [x] 开发前端页面：CourseList✅ CourseDetail✅ CourseFavorite✅ CourseHistory✅ CourseManage✅ (5.1+5.3+5.4+5.5全部完成)
- [x] NotificationType.java 新增3个枚举值 (COURSE_CHAPTER_ADDED / COURSE_APOD_UPDATED / COURSE_COMPLETED) ✅
- [x] NotificationHelper.java 新增3个通知方法 ✅
- [x] 执行3条通知模板 INSERT SQL ✅ 2026-03-22
- [x] AdminCourseServiceImpl.addChapter() 集成章节更新通知 ✅
- [x] APODSyncScheduler 集成APOD更新通知 ✅
- [x] CourseServiceImpl.getChapter() 集成完课通知 ✅
- [x] 预置「NASA每日天文图片精选」+ 视频课种子数据 ✅ 2026-03-20
- [x] 预置「火星探测车日志」课程种子数据（is_mars_course=1）✅ 2026-03-20

### NASA API 集成 (1.5天，与课程模块同期)
- [x] 新建 module/nasa/（NasaApiService + NasaController + ApodVO + MarsPhotoVO + NasaConfig）✅ 2026-03-19
- [x] application.yml 新增 `nasa.api-key: faxoHiBTRduPxmHntIYuRhpExwhnwk34m5NUOOVj` ✅
- [x] WebMvcConfig.java JwtInterceptor excludePathPatterns 新增 `/api/nasa/**` ✅
- [x] 开发 api/nasa.js（getTodayApod）✅
- [x] 开发 components/ApodCard.vue（骨架屏+折叠+全屏预览+video iframe+**中英文切换**）✅
- [x] Home.vue 引入 ApodCard 组件（失败静默隐藏，不影响商城主内容）✅
- [x] 2.7.3 开发 MarsRoverSyncScheduler（随模块5，已在5.2节声明）✅ 2026-03-20

---

## 🎯 核心重点

**记住:**
1. 所有通知代码都在 `com.astronomy.mall.module.notification` 包下
2. 业务模块只需要注入 `NotificationHelper` 并调用对应方法
3. 表关联关系已在 2.3.3 消息通知中心章节完整梳理
4. 每个业务功能开发时，记得集成对应的通知
5. 新模块开发前，先在 `tb_notification_template` 表初始化对应模板

**代码在notification，调用在business！** ✅

