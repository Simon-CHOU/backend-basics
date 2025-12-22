# Stage 1: Build
FROM node:18-alpine AS build
WORKDIR /app
# Copy only package files first to leverage cache
COPY package*.json ./
# Clean install dependencies
RUN npm ci
# Copy source code
COPY . .
# Build
RUN npm run build

# Stage 2: Serve
FROM nginx:alpine-slim
# Copy built assets from builder stage
COPY --from=build /app/dist /usr/share/nginx/html
# Copy nginx config
COPY nginx.conf /etc/nginx/templates/default.conf.template
EXPOSE 80
ENV BACKEND_URL=http://backend:8089
CMD ["nginx", "-g", "daemon off;"]
