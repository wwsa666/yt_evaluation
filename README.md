# 校园美食雷达

面向大学生群体的本地生活点评平台，帮助学生发现校园周边美食和优质店铺。

## 简介

校园美食雷达是一款专注于校园场景的点评平台，致力于解决大学生"不知道吃什么"、"不知道去哪玩"的日常困扰。

现有的大众点评等平台侧重于通用场景，距离远、噪音多、不贴近校园。本项目聚焦校园周边1-3公里，为大学生提供：
- 真实可信的学长学姐评价
- 贴近校园的宝藏店铺推荐
- 去广告、纯净的点评体验

主要包含：
- 用户端：注册登录、浏览商铺、发表评价、收藏对比
- 商家端：店铺管理、评价查看、数据统计
- 管理员：学生认证审批、商家入驻审核、全站内容管理

  

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
- Sentinel（限流降级）
- MySQL + MyBatis Plus

**前端**
- Vue3 + Composition API
- Element Plus
- Vite
- Pinia（状态管理）
- Vue Router

## 项目结构

```
campus-food-radar/
├── evaluation-system-common/    # 公共模块，实体类、工具类、统一返回结果
├── evaluation-system-gateway/   # 网关服务，端口 8080
├── evaluation-system-user/     # 用户服务，端口 8081
├── evaluation-system-shop/     # 商铺服务，端口 8082
├── evaluation-system-review/   # 评论服务，端口 8083
└── evaluation-system-vue/      # 前端项目，端口 5173
```

![image-20260512183953811](C:\Users\PC\AppData\Roaming\Typora\typora-user-images\image-20260512183953811.png)

## 核心功能

### 用户端
- 首页浏览：按分类筛选商铺、搜索功能

- 商铺详情：查看信息、评分、评论列表、浏览量统计

- 多维度评价：口味 / 环境 / 服务 / 性价比 四维打分

- 评论点赞：防重复点赞

- 学生认证：上传学生证，管理员审核

- 商家申请：申请成为商家，审批后获得商家权限

  ![image-20260512184528225](C:\Users\PC\AppData\Roaming\Typora\typora-user-images\image-20260512184528225.png)

### 商家端
- 商家控制台：经营数据概览

- 店铺管理：新增/编辑/上下架店铺

- 评价监控：查看自家店铺用户评价

  ![image-20260512184553541](C:\Users\PC\AppData\Roaming\Typora\typora-user-images\image-20260512184553541.png)

### 管理端
- 数据看板：用户、商铺、评论统计数据

- 学生认证审批：审核学生证照片

- 商家入驻审批：审批商家开店申请

- 内容管理：商铺管理、评论管理、账号管理

  ![image-20260512184740116](C:\Users\PC\AppData\Roaming\Typora\typora-user-images\image-20260512184740116.png)

## 项目收获

- 微服务拆分与架构设计

- Nacos 服务注册发现与配置管理

- Gateway 网关路由与负载均衡

- OpenFeign 跨服务调用与数据聚合

- Redis 缓存与分布式 Session

- RocketMQ 异步消息处理

- Sentinel 接口限流与熔断降级

- JWT Token 鉴权机制


  ## 项目启动

  ### 环境依赖

  - JDK 1.8
  - MySQL 8.0
  - Redis
  - Nacos（服务注册发现）
  - RocketMQ（可选，消息发送失败会自动降级）
  - Sentinel Dashboard（可选，`java -jar sentinel-dashboard-1.8.6.jar --server.port=8858`）
  - Node.js 16+（前端构建）

  ### 启动顺序

  ```bash
  # 1. 启动基础设施
  启动 Nacos → 启动 Redis → 启动 MySQL
  
  # 2. 编译后端
  mvn install -DskipTests
  
  # 3. 启动微服务（按顺序）
  启动 Gateway (8080)
  启动 user-service (8081)
  启动 shop-service (8082)
  启动 review-service (8083)
  
  # 4. 启动前端
  cd evaluation-system-vue
  npm install
  npm run dev
  ```

  ###  默认账号

  | 角色 | 用户名 | 密码 |
  |------|--------|------|
  | 管理员 | admin | 123456 |

  ### 访问地址

  | 地址 | 说明 |
  |------|------|
  | http://localhost:5173 | 前端首页 |
  | http://localhost:5173/login | 用户登录 |
  | http://localhost:5173/admin/login | 管理员登录 |
  | http://localhost:8080 | API 网关 |
  | http://localhost:8858 | Sentinel Dashboard（需额外启动） |
