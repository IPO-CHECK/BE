# syntax=docker/dockerfile:1

# ----------------------------
# build stage (JDK 21)
# ----------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Gradle 캐시 효율을 위해 먼저 래퍼/빌드스크립트만 복사
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# gradlew 실행 권한 보장
RUN chmod +x gradlew

# 의존성 먼저 받아 캐시 태우기 (테스트 제외)
RUN ./gradlew --no-daemon dependencies || true

# 나머지 소스 복사
COPY . .

# 빌드
RUN ./gradlew --no-daemon clean build -x test


# ----------------------------
# run stage (JRE 21)
# ----------------------------
FROM eclipse-temurin:21-jre
WORKDIR /opt/app

# Spring Boot jar 복사 (단일 jar 전제)
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
