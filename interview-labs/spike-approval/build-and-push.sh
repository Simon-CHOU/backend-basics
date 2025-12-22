#!/bin/bash

# 配置变量
IMAGE_NAME="spike-approval"
DOCKER_USERNAME="${DOCKER_USERNAME:-your-dockerhub-username}"
VERSION="${1:-latest}"

# 检查Docker是否登录
echo "Checking Docker login status..."
if ! docker info | grep -q "Username"; then
    echo "Please login to Docker Hub first:"
    echo "docker login"
    exit 1
fi

echo "Building Docker image: ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}"

# 构建镜像
docker build -f Dockerfile.unified -t ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION} .

# 标记为latest
docker tag ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION} ${DOCKER_USERNAME}/${IMAGE_NAME}:latest

echo "Pushing to Docker Hub..."

# 推送镜像
docker push ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}
docker push ${DOCKER_USERNAME}/${IMAGE_NAME}:latest

echo "✅ Successfully built and pushed: ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}"
echo "📦 Docker Hub URL: https://hub.docker.com/r/${DOCKER_USERNAME}/${IMAGE_NAME}"

# 显示镜像信息
echo ""
echo "📊 Image size:"
docker images ${DOCKER_USERNAME}/${IMAGE_NAME}

echo ""
echo "🚀 Ready to deploy to Railway!"
echo "Use image: ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}"