# 星旅智行旅游运营平台

这是一个展示的前后端分离旅游运营系统。项目核心目标是把“旅游路线运营 + 游客浏览预订 + 3D 航线展示 + 后台管理”做成一个完整可讲、可运行、可扩展的全栈项目。

## 技术栈

后端：Spring Boot 2.7、Spring Security、JWT 双 Token、Spring Security、MySQL

并发与一致性增强：数据库悲观锁、JPA 乐观锁版本号、幂等 requestId、

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
