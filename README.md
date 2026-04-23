# 点评系统微服务

一个基于 Spring Cloud 搭建的微服务点评系统，提供用户、商铺、点评三大核心功能。

## 简介

这是一个微服务技术栈项目，系统采用前后端分离架构，后端用 Spring Cloud 构建微服务，前端用 Vue3 + Element Plus。

主要包含：
- 用户模块：注册登录
- 商户模块：商铺的 CRUD 操作
- 点评模块：评论、打分

## 技术栈

**后端**
- Spring Boot 2.7.18
- Spring Cloud 2021.0.8
- Spring Cloud Gateway（网关）
- Alibaba Nacos（服务注册发现 + 配置中心）
- OpenFeign（服务间调用）
- LoadBalancer（负载均衡）
- RocketMQ（消息队列）
- Redis（缓存）
- MySQL + MyBatis Plus

**前端**
- Vue3
- Element Plus
- Vite

## 项目结构

```
evaluation_system/
├── evaluation-system-common/    # 公共模块，实体类、结果封装
├── evaluation-system-gateway/   # 网关服务，端口 8080
├── evaluation-system-user/      # 用户服务，端口 8081
├── evaluation-system-shop/      # 商户服务，端口 8082
├── evaluation-system-review/    # 点评服务，端口 8083
└── evaluation-system-vue/      # 前端项目
```

## 快速开始

### 1. 准备依赖

需要先启动以下服务：
- MySQL（端口 3306）
- Redis（端口 6379）
- Nacos（端口 8848）
- RocketMQ（端口 9876）

### 2. 初始化数据库

```sql
-- 创建数据库
CREATE DATABASE evaluation_system;

-- 导入 3个SQL 文件
source init_data.sql
```

### 3. 启动后端

按顺序启动各个服务：
1. evaluation-system-gateway
2. evaluation-system-user
3. evaluation-system-shop
4. evaluation-system-review

或者通过 IDE 依次启动各个模块的 Application 类。

### 4. 启动前端

```bash
cd evaluation-system-vue
npm install
npm run dev
```

### 5. 访问

- 前端地址：http://localhost:5173
- 网关端口：8080

## 接口文档

通过网关访问，格式：`http://localhost:8080/api/{服务名}/{接口路径}`

| 服务 | 路径 | 说明 |
|------|------|------|
| user-service | /api/user/login | 登录 |
| user-service | /api/user/register | 注册 |
| shop-service | /api/shop/list | 商户列表 |
| shop-service | /api/shop/type/list | 商户类型 |
| review-service | /api/review/save | 发表评论 |
| review-service | /api/review/shop/{id} | 商户评价 |

## 项目学到的东西

- 微服务拆分思路
- Nacos 做服务注册和配置管理
- Gateway 网关路由配置
- OpenFeign 跨服务调用
- Redis 缓存 Token 实现分布式 Session
- RocketMQ 异步消息处理

## License

MIT