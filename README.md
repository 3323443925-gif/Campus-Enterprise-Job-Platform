# 学校就业平台

基于 Spring Boot + Vue 3 构建的学校就业平台，提供学生、HR、教师、管理员多角色协同的就业服务系统。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.x
- **数据库**: MySQL 8.0+
- **ORM**: MyBatis 3.0.x
- **AI能力**: Spring AI (DeepSeek API)
- **PDF处理**: Apache PDFBox 3.0.x
- **文件存储**: 阿里云 OSS
- **认证**: JWT Token

### 前端
- **框架**: Vue 3.5.x
- **UI组件**: Element Plus 2.14.x
- **路由**: Vue Router 4.6.x
- **状态管理**: Vuex 4.1.x
- **图表**: ECharts 6.1.x
- **构建工具**: Vite 8.0.x
- **HTTP客户端**: Axios 1.18.x

## 项目结构

```
web-project-end/
├── parent/              # Maven父模块
├── pojo/                # 实体类模块
│   └── src/main/java/com/example/pojo/
├── utils/               # 工具类模块
│   └── src/main/java/com/example/utils/
├── end-project/         # 后端主应用
│   ├── src/main/java/com/example/
│   │   ├── controller/  # 控制器层
│   │   ├── service/     # 服务层
│   │   ├── mapper/      # 数据访问层
│   │   ├── config/      # 配置类
│   │   ├── interceptor/ # 拦截器
│   │   └── exception/   # 全局异常处理
│   ├── src/main/resources/
│   │   ├── mapper/      # MyBatis XML映射文件
│   │   └── application.yml
│   └── work.sql         # 数据库初始化脚本
└── frontend/            # 前端项目
    ├── src/
    │   ├── components/  # 公共组件
    │   ├── views/       # 页面视图
    │   ├── router/      # 路由配置
    │   ├── store/       # 状态管理
    │   └── utils/       # 工具函数
    └── dist/            # 构建输出目录
```

## 功能模块

| 角色 | 功能模块 | 说明 |
|------|---------|------|
| 学生 | 简历管理 | 上传、编辑个人简历，AI简历分析 |
| 学生 | 职位浏览 | 查看招聘职位，筛选搜索 |
| 学生 | 简历投递 | 投递简历到心仪职位 |
| 学生 | 面试管理 | 查看面试通知，接受/拒绝面试 |
| HR | 企业认证 | 提交企业资质审核申请 |
| HR | 职位管理 | 发布、管理招聘职位 |
| HR | 简历收件箱 | 查看投递简历，AI智能筛选 |
| HR | 面试通知 | 发送面试邀请通知 |
| HR | AI面试 | 与候选人进行AI面试对话 |
| 教师 | 企业管理 | 审核企业入驻申请 |
| 教师 | 职位审核 | 审核招聘职位 |
| 教师 | 投递统计 | 查看学生投递情况 |
| 管理员 | 用户管理 | 管理平台用户，角色分配 |
| 管理员 | 用户审核 | 审核HR和教师账号 |
| 管理员 | 专业管理 | 管理专业信息 |
| 管理员 | 企业审核 | 审核企业资质认证 |
| 管理员 | 统计大屏 | 就业数据可视化统计 |
| 管理员 | 公告管理 | 发布系统公告 |

## 快速开始

### 环境要求

- JDK 17+
- Node.js 22.18+ 或 24.12+
- MySQL 8.0+
- Maven 3.8+

### 1. 数据库配置

创建数据库并执行初始化脚本：

```sql
CREATE DATABASE school_employment_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE school_employment_platform;
```

执行 `end-project/work.sql` 初始化表结构和数据。

### 2. 后端启动

```bash
cd end-project
mvn clean package
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`

### 3. 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端服务默认运行在 `http://localhost:5173`

### 4. 访问地址

- 前端页面: `http://localhost:5173`
- 后端接口: `http://localhost:8080`

## API接口

### 认证接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/info` | GET | 获取当前用户信息 |
| `/api/auth/profile` | GET/PUT | 获取/更新个人资料 |
| `/api/auth/password` | PUT | 修改密码 |
| `/api/auth/logout` | POST | 退出登录 |

### 学生接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/student/job/page` | GET | 岗位列表（分页筛选） |
| `/api/student/job/{id}` | GET | 岗位详情 |
| `/api/student/resume` | GET/POST/PUT | 获取/保存/更新简历 |
| `/api/student/deliver/{jobId}` | POST | 投递岗位 |
| `/api/student/deliver/page` | GET | 我的投递记录 |
| `/api/student/deliver/{id}` | GET | 投递详情 |
| `/api/student/interview/page` | GET | 面试通知列表 |
| `/api/student/interview/{id}/accept` | PUT | 接受面试 |
| `/api/student/interview/{id}/reject` | PUT | 拒绝面试 |

### HR接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/hr/audit/submit` | POST | 提交企业认证申请 |
| `/api/hr/audit/status` | GET | 查询认证状态 |
| `/api/hr/enterprise` | GET/PUT | 获取/更新企业信息 |
| `/api/hr/job/page` | GET | 我企业的岗位列表 |
| `/api/hr/job/{id}` | GET | 岗位详情 |
| `/api/hr/job` | POST/PUT | 新增/更新岗位 |
| `/api/hr/job/{id}` | DELETE | 删除岗位 |
| `/api/hr/job/{id}/publish` | PUT | 发布岗位 |
| `/api/hr/job/{id}/pause` | PUT | 暂停招聘 |
| `/api/hr/job/{id}/close` | PUT | 关闭岗位 |
| `/api/hr/deliver/page` | GET | 简历收件箱 |
| `/api/hr/deliver/{id}` | GET | 简历详情 |
| `/api/hr/deliver/{id}/status` | PUT | 更新投递状态 |
| `/api/hr/interview/page` | GET | 面试通知列表 |
| `/api/hr/interview/send` | POST | 发送面试通知 |

### 管理员接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/admin/user/page` | GET | 用户列表（分页筛选） |
| `/api/admin/user` | POST/PUT | 新增/更新用户 |
| `/api/admin/user/{id}` | DELETE | 删除用户 |
| `/api/admin/user/audit/page` | GET | 待审核用户列表 |
| `/api/admin/user/audit/{id}/pass` | PUT | 用户审核通过 |
| `/api/admin/user/audit/{id}/reject` | DELETE | 用户审核驳回 |
| `/api/admin/major/list` | GET | 专业列表 |
| `/api/admin/major` | POST/PUT | 新增/更新专业 |
| `/api/admin/audit/page` | GET | 企业审核列表 |
| `/api/admin/audit/{id}/pass` | PUT | 企业审核通过 |
| `/api/admin/audit/{id}/reject` | PUT | 企业审核驳回 |

### 教师接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/teacher/...` | - | 教师相关接口 |

### 公告接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/notice/list` | GET | 公告列表 |

### 统计接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/stat/...` | - | 统计数据接口 |

### AI接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/resume/ai/analyze` | POST | AI简历分析 |
| `/api/interview/chat` | POST | AI面试对话 |

## 配置说明

后端配置文件位于 `end-project/src/main/resources/application.yml`

### 环境变量配置

建议将敏感配置通过环境变量传入，避免硬编码：

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/school_employment_platform}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
  
  ai:
    openai:
      api-key: ${AI_API_KEY:your-api-key}
      base-url: ${AI_BASE_URL:https://api.deepseek.com}

aliyun:
  oss:
    endpoint: ${OSS_ENDPOINT:your-endpoint}
    bucketName: ${OSS_BUCKET_NAME:your-bucket}
```

### 配置项说明

| 环境变量 | 默认值 | 说明 |
|---------|-------|------|
| DB_URL | jdbc:mysql://localhost:3306/school_employment_platform | 数据库连接地址 |
| DB_USERNAME | root | 数据库用户名 |
| DB_PASSWORD | 123456 | 数据库密码 |
| AI_API_KEY | - | DeepSeek API Key |
| AI_BASE_URL | https://api.deepseek.com | AI服务地址 |
| OSS_ENDPOINT | - | 阿里云OSS端点 |
| OSS_BUCKET_NAME | - | 阿里云OSS Bucket名称 |

## 开发说明

### 代码规范
- 后端使用 Lombok 简化代码
- MyBatis 使用 XML 映射文件
- 统一响应格式使用 `Result` 类
- 使用 JWT 进行接口认证，Token 放在请求头 `token` 中

### 日志配置
日志文件输出到 `logs/` 目录，配置文件为 `end-project/src/main/resources/logback.xml`

### 跨域配置
已配置 CORS 跨域，前端可直接访问后端接口

## 许可证

MIT License
