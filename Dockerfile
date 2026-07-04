# ============ 多阶段构建 ============
# 阶段 1：Maven 构建
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build
# 先复制 pom.xml 下载依赖（利用 Docker 缓存层）
COPY pom.xml .
RUN mvn dependency:go-offline -B -q || true

# 复制源码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B -q

# 阶段 2：轻量运行时
FROM eclipse-temurin:17-jre-alpine

# 设置 UTF-8 编码（中文支持）
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    TZ=Asia/Shanghai

# 限制 JVM 内存（适配 Render 免费层 512MB）
ENV JAVA_OPTS="-Xmx320m -Xms128m -XX:+UseG1GC -XX:MaxMetaspaceSize=128m"

WORKDIR /app

# 从构建阶段拷贝 JAR
COPY --from=builder /build/target/*.jar app.jar

# Render 默认注入 PORT 环境变量
EXPOSE 8080

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT:-8080}"]
