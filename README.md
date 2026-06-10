# 星旅智行旅游运营平台

这是一个用于求职展示的前后端分离旅游运营系统。项目核心目标是把“旅游路线运营 + 游客浏览预订 + 3D 航线展示 + 后台管理”做成一个完整可讲、可运行、可扩展的全栈项目。

## 技术栈

后端：Spring Boot 2.7、Spring Security、JWT 双 Token、Spring Data JPA、MySQL、Redis、Kafka

并发与一致性增强：数据库悲观锁、JPA 乐观锁版本号、幂等 requestId、Transactional Outbox

前端：Vue 3、Vite、TypeScript、Element Plus、Axios、ECharts、Three.js

数据库：默认使用 MySQL，保留 H2 备用配置

## 项目目录

```text
D:\JAVA_Project\Projrct1_Tourism
├─ backend        后端 Spring Boot 项目
├─ frontend       前端 Vue3 项目
├─ database       数据库脚本
├─ archive        旧版本或备份文件
└─ README.md      项目说明文档
```

## 后端代码怎么看

```text
backend\src\main\java\com\example\tourism
├─ config         配置类，比如跨域、安全、Redis 缓存配置
├─ controller     接口层，前端请求先到这里
├─ domain         实体类，对应数据库表
├─ dto            前后端传输对象，比如登录参数、统计结果
├─ mq             消息队列模块，负责 Kafka 订单事件
├─ repository     数据访问层，负责操作 MySQL
├─ security       登录认证、JWT、权限校验
└─ service        业务层，真正处理路线、订单、统计等业务逻辑
```

你可以这样理解后端调用链：

```text
前端页面 -> Controller 接口 -> Service 业务逻辑 -> Repository 操作数据库 -> MySQL
```

## 前端代码怎么看

```text
frontend\src
├─ api            Axios 请求封装，统一调用后端接口
├─ components     公共组件，比如 3D 航线、图表、弹窗组件
├─ router         路由配置，决定访问哪个页面
├─ stores         前端状态管理，比如登录信息
├─ styles         全局样式和粉色主题
├─ utils          工具方法，比如展示辅助方法
└─ views          页面文件，比如数据总览、路线运营、游客首页
```

## 默认账号

```text
管理员：admin / admin123
游客：traveler / user123
```

管理员登录后可以进入后台，做数据总览、路线运营、目的地管理、订单管理等操作。

游客登录后主要看旅游路线、3D 航线、路线对比和预订体验。

## 默认数据库 MySQL

后端默认连接 MySQL：

```text
数据库名：tourism_platform
用户名：root
密码：123456
端口：3306
```

第一次启动前，请先在 MySQL 里执行：

```sql
CREATE DATABASE tourism_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

项目启动时如果数据库为空，会自动初始化一批旅游目的地、路线、订单和账号数据。你后面在页面里新增、编辑、删除的数据会保存到 MySQL，重启后端也不会丢。

## 启动后端

打开 PowerShell：

```powershell
cd D:\JAVA_Project\Projrct1_Tourism\backend
$env:JAVA_HOME="D:\JDK8"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run
```

后端地址：

```text
http://localhost:8080
```

## 启动前端

再打开一个新的 PowerShell：

```powershell
cd D:\JAVA_Project\Projrct1_Tourism\frontend
& "C:\Program Files\nodejs\npm.cmd" run dev
```

注意：PowerShell 里路径带空格时，前面要加 `&`，否则会报“表达式或语句中包含意外的标记 run”。

前端地址通常是：

```text
http://localhost:5173
```

## Redis 是干什么的

Redis 可以理解成一个“高速缓存”。MySQL 是正式数据库，负责长期保存数据；Redis 是临时加速层，负责把经常查询的数据先放到内存里。

本项目里 Redis 主要缓存：

```text
目的地列表
旅游路线列表
后台数据总览统计
订单列表
```

当你新增、编辑、删除路线或订单时，系统会自动清理相关缓存，避免页面看到旧数据。

如果只想普通启动项目，不需要先装 Redis。默认启动不会强制连接 Redis。

如果你已经安装并启动 Redis，可以这样启用 Redis 缓存：

```powershell
cd D:\JAVA_Project\Projrct1_Tourism\backend
$env:JAVA_HOME="D:\JDK8"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run -Dspring-boot.run.profiles=redis
```

Redis 默认连接：

```text
localhost:6379
```

## Kafka 是干什么的

Kafka 可以理解成一个“消息中转站”。订单创建成功后，系统不一定要立刻把所有后续事情都做完，比如发送通知、更新统计、写日志等。可以先发一条消息到 Kafka，让其他模块慢慢处理。

本项目里 Kafka 的用法是：

```text
游客创建订单成功
-> 后端保存订单到 MySQL
-> 事务提交成功后发布 BookingCreatedEvent
-> Kafka 消费者接收订单创建事件
-> 后续可以扩展短信通知、邮件通知、运营统计、积分奖励等功能
```

这样做的好处是：订单主流程更干净，后续扩展不会把下单接口写得越来越复杂。

如果你没有安装 Kafka，项目仍然可以普通启动。默认启动不会强制连接 Kafka。

如果你已经启动 Kafka，可以这样启用 Redis + Kafka：

```powershell
cd D:\JAVA_Project\Projrct1_Tourism\backend
$env:JAVA_HOME="D:\JDK8"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run -Dspring-boot.run.profiles=redis,kafka
```

Kafka 默认连接：

```text
localhost:9092
```

订单事件主题：

```text
tourism.booking.created
```

## 现在项目里有哪些“正式工面试更爱问”的点

### 1. 高并发防超卖

订单创建不是简单地查库存再减库存，而是做了下面几层保护：

```text
前端为每次下单生成唯一 requestId
-> 后端先按 requestId 做幂等校验，防止重复提交
-> BookingService 对路线记录加数据库悲观锁（SELECT ... FOR UPDATE）
-> TravelRoute 实体带 @Version 版本号，表达乐观锁并发意图
-> 扣减库存、保存订单、写入 outbox 事件都放在同一个事务里
```

这样即使多个用户同时抢同一条路线，也不会把库存卖成负数。

### 2. 分布式一致性思路

这个项目现在还不是完整微服务架构，但已经加入了一个面试里很好讲的模式：`Transactional Outbox`。

做法是：

```text
订单事务提交时
-> 不直接依赖“立刻发 Kafka 就一定成功”
-> 先把订单事件写进 outbox_events 表
-> 定时任务扫描待发送事件
-> 再异步投递到 Kafka
```

这样可以把“本地事务成功”和“消息最终投递”解耦，是典型的分布式最终一致性思路。

### 3. 复杂事务

本项目里比较适合讲的复杂事务不是银行转账那种，而是“订单 + 库存 + 事件”的组合事务：

```text
创建订单
-> 锁定路线库存
-> 校验余位
-> 扣减库存
-> 保存订单
-> 生成 outbox 事件
-> 一起提交
```

取消订单和重新激活订单时，还会做库存回补和重新占用。

## 并发测试和压测

### 后端并发测试

项目里已经加了一个并发测试类：

```text
backend\src\test\java\com\example\tourism\service\BookingServiceConcurrencyTest.java
```

这个测试会验证两件事：

```text
高并发下单时不会超卖
相同 requestId 重复提交时不会重复扣库存
```

运行方式：

```powershell
cd D:\JAVA_Project\Projrct1_Tourism\backend
$env:JAVA_HOME="D:\JDK8"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn test
```

### k6 压测脚本

项目里也加了一个简单压测脚本：

```text
backend\perf\k6-booking.js
```

如果你本机安装了 k6，可以这样跑：

```powershell
cd D:\JAVA_Project\Projrct1_Tourism\backend\perf
k6 run .\k6-booking.js
```

也可以指定环境变量：

```powershell
$env:BASE_URL="http://localhost:8080"
$env:USERNAME="traveler"
$env:PASSWORD="user123"
$env:ROUTE_ID="1"
k6 run .\k6-booking.js
```

这个脚本会做登录、拿 token、并发下单，适合用来演示接口在压力下的响应情况。

## 现在你可以怎么面试讲

你可以这样讲 Redis：

“我在项目中引入 Redis 做热点数据缓存，主要缓存目的地列表、路线列表和后台统计数据。对于路线和订单的增删改操作，我通过缓存失效策略清理相关缓存，保证页面展示的数据和数据库保持一致。”

你可以这样讲 Kafka：

“我在订单创建流程中加入 Kafka 异步事件。订单保存成功并且事务提交之后，系统会发布订单创建事件。这样后续的通知、统计、积分等逻辑可以通过消费者扩展，不会和下单主流程强耦合。”

你可以这样讲双 Token：

“登录认证采用 Access Token + Refresh Token。Access Token 用于访问业务接口，Refresh Token 用于刷新登录状态。前端统一拦截 401，自动刷新 Token 并重放请求，提升用户体验。”

## 简历写法

项目名称：星旅智行旅游运营平台

项目简介：

基于 Spring Boot + Vue3 的前后端分离旅游运营平台，面向旅游路线管理、游客浏览预订、订单运营和 3D 航线可视化场景，支持管理员与游客双角色登录、路线 CRUD、订单管理、数据看板和路线对比等功能。

技术栈：

前端：Vue3、TypeScript、Element Plus、Axios、ECharts、Three.js

后端：Spring Boot、Spring Security、JWT、Spring Data JPA、MySQL、Redis、Kafka

核心工作：

1. 设计前后端分离架构，使用 Vue3 + Element Plus 构建管理端和游客端页面，结合 Axios 拦截器统一处理接口请求、Token 注入和异常提示。

2. 基于 Spring Security + JWT 实现登录认证和角色权限控制，采用 Access Token + Refresh Token 双 Token 机制，支持 Token 过期刷新和异常登录态清理。

3. 使用 MySQL 持久化旅游目的地、路线、订单和用户数据，实现路线、目的地、订单等模块的增删改查，保证数据重启后不丢失。

4. 引入 Redis 缓存热点路线、目的地和运营统计数据，并在新增、编辑、删除等操作后自动清理缓存，提升高频查询场景下的响应能力。

5. 基于 Kafka 实现订单创建事件异步发布，将订单主流程与后续通知、统计扩展逻辑解耦，提高系统扩展性。

6. 使用 Three.js 实现 3D 航线可视化效果，并结合 ECharts 展示收入趋势、热门目的地和库存预警，增强项目展示效果。

7. 在订单链路中加入 requestId 幂等控制、数据库锁和 Transactional Outbox，提升高并发下单场景下的库存一致性与消息可靠性。

8. 编写并发测试与压测脚本，验证高并发请求下不会出现重复下单和库存超卖问题。

项目总结：

通过该项目，我掌握了 Vue3 + Spring Boot 前后端分离开发流程，熟悉了 JWT 认证、角色权限、MySQL 持久化、Redis 缓存、Kafka 异步消息和 Three.js 可视化等主流技术在实际业务场景中的应用。
