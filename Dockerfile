# 第一阶段：构建阶段
FROM maven:3.6.3-openjdk-11 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 第二阶段：运行阶段。将第一阶段的构建产物复制到第二阶段中
FROM eclipse-temurin:11.0.26_4-jdk
WORKDIR /app
COPY --from=builder /app/target/http-proxy-boot.jar app.jar
CMD ["java", "-jar", "app.jar"]