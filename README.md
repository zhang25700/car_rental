# 汽车租赁管理系统

本科毕业设计项目，采用前后端分离架构：

- 后端：Spring Boot 3、MyBatis-Plus、MySQL、JWT、Swagger、支付宝沙盒
- 前端：Vue 3、Vite、Element Plus、百度地图 JavaScript API

## 功能范围

### 用户端

- 首页轮播图、热门车辆、系统公告
- 地图选点租车、车型查询、收藏车辆
- 身份证信息提交与人工审核
- 下单与支付方式选择
- 我的订单、还车结算、超时费用展示
- 用户反馈与公告查看

### 管理端

- 系统总览与图表统计
- 轮播图管理
- 车辆、品牌、类型、营业网点管理
- 订单管理与结算确认
- 用户审核、冻结、管理员信息管理
- 反馈回复、系统公告发布

## 默认账号

- 管理员：`admin / 123456`
- 普通用户：`zhangsan / 123456`

## 启动方式

### 1. 初始化数据库

1. 启动 MySQL
2. 修改 [application.yml](/d:/毕设/car-rental/backend/src/main/resources/application.yml) 中的数据库账号密码
3. 项目已配置自动执行 [schema.sql](/d:/毕设/car-rental/backend/src/main/resources/schema.sql) 和 [data.sql](/d:/毕设/car-rental/backend/src/main/resources/data.sql)，首次启动会自动创建 `car_rental` 数据库并导入示例数据

### 2. 启动后端

在 [backend](/d:/毕设/car-rental/backend) 目录执行：

```powershell
mvn spring-boot:run
```

后端地址：`http://localhost:8080`

Swagger：`http://localhost:8080/swagger-ui.html`

也可以直接在 IDEA 中运行 [CarRentalApplication.java](/d:/毕设/car-rental/backend/src/main/java/com/carrental/CarRentalApplication.java)。

### 3. 启动前端

在 [frontend](/d:/毕设/car-rental/frontend) 目录执行：

```powershell
npm install
npm run dev
```

前端地址：`http://localhost:5173`

## 第三方配置

### 百度地图

在 `frontend/.env` 中配置：

```env
VITE_BAIDU_MAP_AK=你的百度地图AK
```

未配置时系统会自动降级为站点选择模式。

### 支付宝沙盒

在 [application.yml](/d:/毕设/car-rental/backend/src/main/resources/application.yml) 中配置：

- `app.alipay.enabled=true`
- `app.alipay.app-id`
- `app.alipay.notify-url`
- `app.alipay.return-url`

密钥文件默认从 `backend/src/main/resources` 读取：

- `app_private_key.pem`
- `alipay_public_key.pem`

## 已验证

- 前端 `npm run build` 通过
- 后端 `mvn -DskipTests package` 通过
