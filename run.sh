#!/bin/bash

# 定义颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # 无颜色

# 定义变量
IMAGE_NAME="campus-recruitment"
TAG="latest"
TAR_FILE="${IMAGE_NAME}_${TAG}.tar"

# 显示标题
echo -e "${BLUE}═════════════════════════════════════════════${NC}"
echo -e "${BLUE}       校园招聘系统 Docker 构建脚本          ${NC}"
echo -e "${BLUE}═════════════════════════════════════════════${NC}"

# 构建Docker镜像（指定平台为linux/amd64）并加载到本地
echo -e "\n${YELLOW}[1/3] 开始构建Docker镜像 (linux/amd64)...${NC}"
docker buildx build --platform linux/amd64 --load -t ${IMAGE_NAME}:${TAG} .

# 检查上一步是否成功
if [ $? -ne 0 ]; then
    echo -e "\n${RED}[错误] Docker镜像构建失败${NC}"
    exit 1
fi
echo -e "${GREEN}[完成] Docker镜像构建成功: ${IMAGE_NAME}:${TAG}${NC}"

# 创建配置文件示例
echo -e "\n${YELLOW}[2/4] 复制配置文件示例...${NC}"
if [ -f "config/application.yml.example" ]; then
    cp config/application.yml.example ${IMAGE_NAME}_application.yml.example
    echo -e "${GREEN}[完成] 配置文件示例已复制: ${IMAGE_NAME}_application.yml.example${NC}"
else
    echo -e "${YELLOW}[警告] 配置文件示例不存在，跳过复制${NC}"
fi

# 保存镜像为tar文件
echo -e "\n${YELLOW}[3/4] 正在保存镜像为tar文件...${NC}"
docker save -o ${TAR_FILE} ${IMAGE_NAME}:${TAG}

# 检查上一步是否成功
if [ $? -ne 0 ]; then
    echo -e "\n${RED}[错误] 保存镜像失败${NC}"
    exit 1
fi
echo -e "${GREEN}[完成] 镜像已保存为: ${TAR_FILE}${NC}"

# 显示tar文件信息
echo -e "\n${YELLOW}[4/4] 获取tar文件信息...${NC}"
TAR_SIZE=$(du -h ${TAR_FILE} | cut -f1)
echo -e "${GREEN}文件大小: ${TAR_SIZE}${NC}"

# 完成
echo -e "\n${GREEN}══════════════════════════════════════════════${NC}"
echo -e "${GREEN}✅ 镜像构建和保存完成!${NC}"
echo -e "${GREEN}   镜像文件: ${TAR_FILE}${NC}"
echo -e "${GREEN}   文件大小: ${TAR_SIZE}${NC}"
echo -e "${GREEN}   配置示例: ${IMAGE_NAME}_application.yml.example${NC}"
echo -e "${GREEN}══════════════════════════════════════════════${NC}"
echo -e "\n${YELLOW}部署说明:${NC}"
echo -e "1. 将 ${TAR_FILE} 和 ${IMAGE_NAME}_application.yml.example 文件复制到目标服务器"
echo -e "2. 在目标服务器上执行: ${BLUE}docker load -i ${TAR_FILE}${NC}"
echo -e "3. 修改配置文件: ${BLUE}cp ${IMAGE_NAME}_application.yml.example application.yml${NC} 并根据实际情况修改数据库连接等配置"
echo -e "4. 运行容器: ${BLUE}docker run -d \\${NC}"
echo -e "   ${BLUE}  --name campus-recruitment-app \\${NC}"
echo -e "   ${BLUE}  -p 18080:8080 \\${NC}"
echo -e "   ${BLUE}  -v \$(pwd)/uploads:/app/uploads \\${NC}"
echo -e "   ${BLUE}  -v \$(pwd)/application.yml:/app/application.yml \\${NC}"
echo -e "   ${BLUE}  ${IMAGE_NAME}:${TAG}${NC}"
echo -e "\n${YELLOW}注意事项:${NC}"
echo -e "1. 确保 application.yml 中配置了正确的数据库连接信息"
echo -e "2. 如果使用外部数据库，请确保数据库已创建且可访问"
echo -e "3. 第一次运行时，系统将自动创建所需的数据表"


docker run -d \
     --name campus-recruitment-app \
     -p 18080:8080 \
     -v $(pwd)/uploads:/app/uploads \
     -v $(pwd)/application.yml:/app/application.yml \
     campus-recruitment:latest
