#!/bin/bash

# 设置JDK 21环境
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.7/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "使用JDK版本:"
java -version

echo "启动场馆服务..."
cd sport-venue-venue
mvn spring-boot:run -Dspring-boot.run.profiles=local 