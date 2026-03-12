# 汽车租赁系统

本科毕业设计项目，采用前后端分离架构：

- 后端：Spring Boot 3、MyBatis-Plus、MySQL、JWT、Swagger、支付宝沙盒支付
- 前端：Vue 3、Vite、Element Plus、百度地图 JavaScript API

## 功能范围

- 用户注册、登录、个人会话管理
- 车辆信息管理、车辆筛选查询、车辆状态维护
- 客户信息管理、后台用户管理
- 租赁订单创建、地图选点、支付宝沙盒支付、取消、归还
- 取车点管理、收藏与评价
- 管理员运营数据统计

## 项目结构

- `backend`：后端工程
- `frontend`：前端工程
- `backend/src/main/resources/schema.sql`：MySQL 建表和初始化数据

## 默认账号

- 管理员：`admin / 123456`
- 演示用户：`zhangsan / 123456`

## 启动方式

### 1. 初始化数据库

1. 启动 MySQL
2. 按需修改 [application.yml](/d:/毕设/car-rental/backend/src/main/resources/application.yml) 中的数据库账号密码
3. 项目已配置 `createDatabaseIfNotExist=true` 和自动执行 [schema.sql](/d:/毕设/car-rental/backend/src/main/resources/schema.sql)，首次启动会自动创建 `car_rental` 数据库并导入示例数据

### 2. 配置支付宝沙盒

如需启用支付功能，先设置环境变量：

```powershell
$env:ALIPAY_APP_ID="你的沙盒APPID"
$env:ALIPAY_PRIVATE_KEY="你的应用私钥"
$env:ALIPAY_PUBLIC_KEY="支付宝沙盒公钥"
$env:ALIPAY_NOTIFY_URL="http://localhost:8080/api/pay/alipay/notify"
$env:ALIPAY_RETURN_URL="http://localhost:8080/api/pay/alipay/return"
```

然后把 [application.yml](/d:/毕设/car-rental/backend/src/main/resources/application.yml) 中 `app.alipay.enabled` 改为 `true`。

### 3. 启动后端

在 [backend](/d:/毕设/car-rental/backend) 目录执行：

```powershell
mvn spring-boot:run
```

后端地址：`http://localhost:8080`

Swagger：`http://localhost:8080/swagger-ui.html`

也可以在 IDEA 中直接运行 [CarRentalApplication.java](/d:/毕设/car-rental/backend/src/main/java/com/carrental/CarRentalApplication.java)。

### 4. 配置百度地图

在 [frontend/.env.example](/d:/毕设/car-rental/frontend/.env.example) 基础上创建 `frontend/.env`：

```env
VITE_BAIDU_MAP_AK=你的百度地图JavaScript API AK
```

### 5. 启动前端

在 [frontend](/d:/毕设/car-rental/frontend) 目录执行：

```powershell
npm install
npm run dev
```

前端地址：`http://localhost:5173`

## 已完成校验

- 前端 `npm run build` 通过
- 后端 `mvn -DskipTests package` 通过
