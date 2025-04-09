# Docker 部署说明

本文档介绍如何使用 Docker 构建和部署校园招聘系统后端。

## 前提条件

- Docker 已安装并正常运行
- Docker BuildX 已启用（用于跨平台构建）
- Git 已安装（用于获取源代码）

## 快速开始

我们提供了一个简单的脚本来自动化构建和保存镜像的过程：

```bash
# 给脚本添加执行权限
chmod +x run.sh

# 运行脚本
./run.sh
```

该脚本将：
1. 构建适用于 Linux AMD64 架构的 Docker 镜像
2. 将镜像保存为 tar 文件
3. 显示 tar 文件信息和使用说明

## 镜像分发和部署

通过 tar 文件分发镜像可以避免依赖 Docker Registry，特别适合离线环境部署。

### 在目标服务器上加载镜像

```bash
# 将 tar 文件复制到目标服务器
scp campus-recruitment_latest.tar user@target-server:/path/to/destination/

# 在目标服务器上加载镜像
docker load -i campus-recruitment_latest.tar

# 创建上传目录
mkdir -p ./uploads

# 运行容器
docker run -d \
  --name campus-recruitment-app \
  -p 8080:8080 \
  -v $(pwd)/uploads:/app/uploads \
  -v $(pwd)/application.yaml:/app/application.yaml \
  -e SPRING_CONFIG_LOCATION=file:/app/application.yaml \
  -e SPRING_PROFILES_ACTIVE=prod \
  campus-recruitment:latest
```

## 自定义配置文件

您可以通过挂载自定义的`application.yaml`文件来覆盖容器内部的配置：

1. 首先，从原始项目中复制配置文件并根据需要修改:

```bash
cp backend/src/main/resources/application.yaml ./application.yaml
```

2. 编辑`application.yaml`文件，根据您的环境进行必要的修改，例如：
   - 数据库连接配置
   - 日志级别
   - 文件上传路径
   - 安全配置
   - 其他应用特定设置

3. 在启动容器时挂载此配置文件：

```bash
docker run -d \
  --name campus-recruitment-app \
  -p 8080:8080 \
  -v $(pwd)/uploads:/app/uploads \
  -v $(pwd)/application.yaml:/app/application.yaml \
  -e SPRING_CONFIG_LOCATION=file:/app/application.yaml \
  campus-recruitment:latest
```

> 通过设置`SPRING_CONFIG_LOCATION`环境变量，您可以明确指定Spring Boot应该加载的配置文件位置。

## 手动构建和运行

如果您想手动构建镜像而不使用脚本，可以执行以下步骤：

### 1. 构建镜像

构建适用于 Linux AMD64 架构的镜像：

```bash
docker buildx build --platform linux/amd64 --load -t campus-recruitment:latest .
```

> 注意：`--load` 参数非常重要，它确保构建的镜像会被加载到本地Docker引擎中，这样才能保存为tar文件。如果不使用这个参数，镜像虽然会构建成功，但不会出现在本地Docker镜像列表中。

### 2. 保存镜像为 tar 文件

```bash
docker save -o campus-recruitment_latest.tar campus-recruitment:latest
```

### 3. 运行容器

```bash
# 创建上传目录
mkdir -p ./uploads

# 准备配置文件(如需自定义)
cp backend/src/main/resources/application.yaml ./application.yaml
# 根据需要编辑配置文件

# 运行容器
docker run -d \
  --name campus-recruitment-app \
  -p 8080:8080 \
  -v $(pwd)/uploads:/app/uploads \
  -v $(pwd)/application.yaml:/app/application.yaml \
  -e SPRING_CONFIG_LOCATION=file:/app/application.yaml \
  -e SPRING_PROFILES_ACTIVE=prod \
  campus-recruitment:latest
```

## 常用命令

- 查看容器日志：`docker logs -f campus-recruitment-app`
- 停止容器：`docker stop campus-recruitment-app`
- 启动容器：`docker start campus-recruitment-app`
- 删除容器：`docker rm campus-recruitment-app`
- 删除镜像：`docker rmi campus-recruitment:latest`
- 保存镜像为文件：`docker save -o campus-recruitment_latest.tar campus-recruitment:latest`
- 加载镜像文件：`docker load -i campus-recruitment_latest.tar`

## 访问应用

部署成功后，可以通过以下地址访问应用：

- 后端 API 地址：http://localhost:8080/api

## 故障排除

如果遇到问题，请尝试以下步骤：

1. 检查容器日志：`docker logs campus-recruitment-app`
2. 确保 Docker 服务正在运行
3. 确保端口 8080 未被其他应用占用
4. 确保本地上传目录拥有正确的权限
5. 如果使用的是 Mac M1/M2 芯片，确保 Docker BuildX 正确配置为构建 AMD64 架构

## 注意事项

- 默认情况下，容器内的应用程序将以生产模式运行
- 所有上传的文件将保存在宿主机的 `./uploads` 目录中
- 本镜像仅包含后端应用，前端应用需要单独部署
- 当使用挂载的`application.yaml`时，确保配置文件中的文件上传路径与容器内的路径一致(`/app/uploads`)
- 如果修改了配置文件，需要重启容器才能应用新配置 