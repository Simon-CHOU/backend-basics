# 🚀 部署指南

## 📋 项目概述
- **后端**: Spring Boot (Java 21) + GraalVM Native Image
- **前端**: React + Vite + TypeScript + Antd
- **当前**: Docker Compose 本地运行 (http://localhost)

## 🎯 推荐发布方案（免费）

### 方案一：分离部署（推荐）⭐
- **前端**: Vercel
- **后端**: Railway 或 Render

### 方案二：GitHub Actions自动化
- **前端**: GitHub Pages
- **后端**: Railway (通过GitHub Actions自动部署）

---

## 🚀 快速部署（方案一）

### 1. 前端部署到 Vercel（5分钟）

```bash
# 1. 安装 Vercel CLI
npm i -g vercel

# 2. 在 frontend 目录
cd frontend

# 3. 部署
vercel --prod
```

**或使用网页版**:
1. 访问 [vercel.com](https://vercel.com)
2. 连接GitHub账号
3. 导入项目
4. 选择 `frontend` 目录

### 2. 后端部署到 Railway（5分钟）

**方法A: 直接部署**
1. 访问 [railway.app](https://railway.app)
2. 连接GitHub账号
3. 点击 "New Project" → "Deploy from GitHub repo"
4. 选择你的仓库
5. Railway会自动检测Dockerfile并构建

**方法B: 使用Railway CLI**
```bash
# 1. 安装 Railway CLI
npm install -g @railway/cli

# 2. 登录
railway login

# 3. 初始化项目
railway init

# 4. 部署
railway up
```

### 3. 更新前端配置

部署后，你需要更新前端的API地址：

```javascript
// frontend/src/lib/api.ts (或类似文件)
const API_BASE_URL = import.meta.env.PROD
  ? 'https://your-backend.railway.app'  // Railway后端地址
  : 'http://localhost:8089';            // 本地开发地址
```

---

## 🔧 可选：GitHub Actions自动化

如果你想要自动化部署，可以使用GitHub Actions：

### 1. 前端自动部署到GitHub Pages
- 创建 `.github/workflows/deploy-frontend.yml`
- 每次push到main分支自动部署

### 2. 后端自动部署到Railway
- Railway已经集成GitHub，自动部署
- 无需额外配置

---

## 💡 免费方案限制

### Vercel 免费版
- ✅ 无限静态项目
- ✅ 100GB带宽/月
- ✅ 自定义域名
- ⚠️ 无服务器函数有限制

### Railway 免费版
- ✅ $5/月信用额度
- ✅ 500小时运行时间
- ⚠️ 闲置15分钟后会休眠
- ⚠️ 流量超限后需付费

### Render 免费版（替代Railway）
- ✅ 750小时/月
- ⚠️ 15分钟无访问休眠
- ⚠️ 冷启动需要等待

---

## 🎯 生产环境注意事项

1. **环境变量配置**
   - 数据库连接
   - API密钥
   - 其他敏感信息

2. **域名和SSL**
   - 所有平台都提供免费SSL
   - 可以绑定自定义域名

3. **监控和日志**
   - Vercel提供访问统计
   - Railway提供应用日志

---

## 🆘 常见问题

### Q: 应用休眠怎么办？
A: 这是免费版的正常现象。访问时会自动唤醒，需要等待10-30秒。

### Q: 如何连接前端和后端？
A: 在前端使用环境变量，开发时用localhost，生产时用实际域名。

### Q: 数据库怎么处理？
A: 可以使用Railway的免费PostgreSQL，或使用Supabase免费版。

---

## 📝 推荐学习路径

1. **第一步**: 手动部署（熟悉流程）
2. **第二步**: 学习基础GitHub Actions（可选）
3. **第三步**: 优化和监控

对于POC项目，手动部署已经完全足够！