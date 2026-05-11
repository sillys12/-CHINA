# 纹知CHINA项目

> 一个基于 Spring Boot + 现代化前端技术栈的全栈社交互动平台，展示完整的前后端开发能力

## 🌟 项目亮点

- **完整的用户认证体系**: JWT Token 安全认证，支持注册、登录、密码加密
- **社交互动功能**: 帖子发布、图片上传、评论、点赞、收藏
- **云存储集成**: 七牛云 Kodo 实现图片云端存储与 CDN 加速
- **前后端分离**: RESTful API 设计，支持跨域访问
- **安全防护**: Spring Security 安全框架，全局异常处理

## 📁 项目结构

```
纹知CHINA项目前后端/
├── pattern-user-backend/    # 后端服务
│   └── backend/             # Spring Boot 应用
│       ├── src/main/java/   # Java 源代码
│       ├── src/main/resources/  # 配置文件
│       └── pom.xml          # Maven 依赖管理
└── 可视化项目网页设计/       # 前端应用
    ├── dist/                # 生产构建产物
    └── README.md            # 前端说明
```

## 🛠 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.4 | 企业级 Java 后端框架 |
| Java | 17 | LTS 版本，性能稳定 |
| Spring Security | 6.x | 安全框架 |
| JWT (JJWT) | 0.13.0 | Token 认证 |
| Spring Data JPA | 3.2.x | 数据持久化 |
| MySQL | 8.0+ | 关系型数据库 |
| 七牛云 Kodo | 7.19.0 | 对象存储服务 |
| Maven | 3.8+ | 依赖管理 |

### 前端技术

| 技术 | 说明 |
|------|------|
| 现代化前端框架 | 响应式设计，组件化开发 |
| Vite | 极速构建工具 |
| npm | 包管理器 |
| 响应式布局 | 移动端适配 |

## ✨ 功能特性

### 🔐 用户认证模块
- **用户注册**: 用户名唯一性校验、密码加密存储 (BCrypt)
- **用户登录**: JWT Token 颁发、会话管理
- **Token 验证**: 请求拦截、过期处理

### 👤 用户管理模块
- 获取当前用户信息
- 更新个人资料
- 公共用户信息查询

### 📝 帖子模块
- 创建帖子（支持多图上传）
- 帖子列表分页查询
- 帖子详情查看
- 帖子更新与删除

### 💬 互动模块
- 评论功能（支持多级评论）
- 点赞/取消点赞
- 收藏/取消收藏

### 🖼️ 上传模块
- 图片上传至七牛云
- CDN 加速访问
- 图片链接管理

## 🚀 快速开始

### 环境要求

- **Java**: 17+ (推荐使用 JDK 21)
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **Node.js**: 18+

### 后端启动

```bash
# 进入后端目录
cd pattern-user-backend/backend

# 创建数据库
mysql -u root -p -e "CREATE DATABASE pattern_user CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 运行应用
mvn spring-boot:run
```

后端服务运行在: `http://localhost:8081`

### 前端启动

```bash
# 进入前端目录
cd 可视化项目网页设计

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务运行在: `http://localhost:5173`

## 🔧 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `SERVER_PORT` | 服务端口 | 8081 |
| `SPRING_DATASOURCE_URL` | 数据库连接URL | jdbc:mysql://127.0.0.1:3306/pattern_user |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | root |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | djh468#FQ |
| `QINIU_ACCESS_KEY` | 七牛云 AccessKey | 需自行配置 |
| `QINIU_SECRET_KEY` | 七牛云 SecretKey | 需自行配置 |
| `APP_JWT_SECRET` | JWT 密钥 | 生产环境必须设置 |

### 配置文件位置

后端配置: `backend/src/main/resources/application.yml`

## 📂 核心代码结构

```
backend/src/main/java/com/wenzhi/backend/
├── auth/                # 认证模块
│   ├── AuthController.java   # REST API 控制层
│   └── AuthService.java      # 业务逻辑层
├── config/              # 配置类
│   ├── SecurityConfig.java   # 安全配置
│   ├── CorsConfig.java       # 跨域配置
│   ├── JwtProperties.java    # JWT 配置
│   └── QiniuProperties.java  # 七牛云配置
├── security/            # 安全组件
│   ├── JwtAuthFilter.java    # JWT 过滤器
│   └── JwtService.java       # JWT 服务
├── square/              # 帖子模块
│   ├── PostController.java   # 帖子控制器
│   ├── InteractionController.java  # 互动控制器
│   ├── CommentController.java     # 评论控制器
│   └── repository/          # 数据访问层
├── upload/              # 上传模块
│   ├── UploadController.java
│   └── QiniuUrlService.java
├── user/                # 用户模块
│   ├── MeController.java
│   ├── UserPublicController.java
│   └── UserRepository.java
├── common/              # 公共组件
│   └── GlobalExceptionHandler.java  # 全局异常处理
└── PatternUserBackendApplication.java  # 启动类
```

## 🔑 核心技术实现

### 1. JWT 认证流程
```
用户登录 → 验证用户名密码 → 生成 JWT Token → 返回 Token
后续请求 → 携带 Token → JwtAuthFilter 验证 → 放行/拒绝
```

### 2. 图片上传流程
```
前端上传图片 → 后端接收 → 七牛云 SDK 上传 → 返回 CDN 链接 → 保存到数据库
```

### 3. 安全设计
- **密码加密**: BCrypt 算法，安全可靠
- **Token 机制**: 无状态认证，便于水平扩展
- **全局异常处理**: 统一错误响应格式

## 📊 API 接口示例

### 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
    "username": "testuser",
    "password": "password123"
}
```

### 创建帖子
```http
POST /api/posts
Authorization: Bearer <token>
Content-Type: multipart/form-data

{
    "content": "这是一篇新帖子",
    "images": [file1, file2]
}
```
