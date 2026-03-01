# 基于 OpenJDK 21 的官方镜像
FROM registry.cn-heyuan.aliyuncs.com/bubua12/openjdk:21-jdk-slim

# author
LABEL authors="bubua12"

# 挂载目录
VOLUME /home/atlas

# 创建目录
RUN mkdir -p /home/atlas

# 指定路径
WORKDIR /home/atlas

# 复制 jar文件到路径
COPY target/atlas-monitor-1.0.0.jar /home/atlas/app.jar

EXPOSE 9400

# 镜像的时间格式以及时区
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

# 启动认证服务
ENTRYPOINT ["java","-jar","app.jar"]