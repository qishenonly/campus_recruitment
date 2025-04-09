# 使用官方Maven镜像作为构建环境
FROM maven:3.8.6-openjdk-11-slim AS build

# 设置工作目录
WORKDIR /app

# 复制Maven依赖文件
COPY backend/pom.xml .

# 下载依赖项（这一步将被缓存，除非pom.xml更改）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY backend/src ./src

# 构建应用
RUN mvn package -DskipTests

# 使用轻量级的JRE运行环境
FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 从构建阶段复制构建好的JAR文件
COPY --from=build /app/target/*.jar app.jar

# 复制上传目录（确保目录存在）
RUN mkdir -p /app/uploads

# 暴露应用端口
EXPOSE 8080

# 设置Java运行参数
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 创建配置目录
RUN mkdir -p /app/config

# 启动应用，使用外部配置文件（如果存在）
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar --spring.config.location=file:/app/application.yml,classpath:/application.yml"]