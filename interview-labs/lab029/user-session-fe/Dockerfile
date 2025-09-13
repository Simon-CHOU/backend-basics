# 多阶段构建Dockerfile
# 第一阶段：构建应用
FROM node:18-alpine as builder

# 设置工作目录
WORKDIR /app

# 复制package文件
COPY package.json package-lock.json* ./

# 安装依赖
RUN npm install

# 复制源代码
COPY . .

# 构建应用
RUN npm run build

# 第二阶段：运行应用
FROM nginx:alpine

# 复制自定义nginx配置
COPY nginx.conf /etc/nginx/nginx.conf

# 从构建阶段复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost/ || exit 1

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]