# PowerShell version of build and push script

# 配置变量
$IMAGE_NAME = "spike-approval"
$DOCKER_USERNAME = $env:DOCKER_USERNAME
if (-not $DOCKER_USERNAME) {
    $DOCKER_USERNAME = Read-Host "请输入你的Docker Hub用户名"
}
$VERSION = if ($args.Count -gt 0) { $args[0] } else { "latest" }

# 检查Docker是否登录
Write-Host "检查Docker登录状态..." -ForegroundColor Green
try {
    $dockerInfo = docker info --format "{{.ServerVersion}}"
    if (-not $dockerInfo) {
        Write-Host "❌ Docker未运行，请先启动Docker Desktop" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Docker未运行，请先启动Docker Desktop" -ForegroundColor Red
    exit 1
}

# 构建镜像
Write-Host "🏗️  构建Docker镜像: ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}" -ForegroundColor Yellow
try {
    docker build -f Dockerfile.unified -t "${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}" .
    docker tag "${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}" "${DOCKER_USERNAME}/${IMAGE_NAME}:latest"
    Write-Host "✅ 构建成功!" -ForegroundColor Green
} catch {
    Write-Host "❌ 构建失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 推送镜像
Write-Host "📤 推送到Docker Hub..." -ForegroundColor Yellow
try {
    docker push "${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}"
    docker push "${DOCKER_USERNAME}/${IMAGE_NAME}:latest"
    Write-Host "✅ 推送成功!" -ForegroundColor Green
} catch {
    Write-Host "❌ 推送失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 显示成功信息
Write-Host "🎉 成功构建并推送: ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}" -ForegroundColor Green
Write-Host "📦 Docker Hub链接: https://hub.docker.com/r/${DOCKER_USERNAME}/${IMAGE_NAME}" -ForegroundColor Blue

# 显示镜像信息
Write-Host "" -ForegroundColor White
Write-Host "📊 镜像信息:" -ForegroundColor Blue
docker images "${DOCKER_USERNAME}/${IMAGE_NAME}"

Write-Host "" -ForegroundColor White
Write-Host "🚀 准备部署到Railway!" -ForegroundColor Blue
Write-Host "使用镜像: ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}" -ForegroundColor White