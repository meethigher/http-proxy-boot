# 第一阶段：构建阶段
FROM maven:3.6.3-openjdk-11 AS builder
WORKDIR /app

# 1. 只复制 pom.xml（利用缓存）
COPY pom.xml .

# 2. 预先下载依赖（只要 pom.xml 没变，这一步就不会重新执行）
RUN mvn dependency:go-offline -B

# 3. 再复制源码（只有源码变时才会重新编译）
COPY src ./src

# 4. 编译打包
RUN mvn clean package -DskipTests

# 第二阶段：运行阶段。将第一阶段的构建产物复制到第二阶段中
FROM eclipse-temurin:11.0.26_4-jre
WORKDIR /app
COPY --from=builder /app/target/http-proxy-boot.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]

# 第三阶段，编译 docker build -t http-proxy-boot .

# 第四阶段，运行 docker run -it --rm -w /root/abc -v /root/abc:/root/abc http-proxy-boot