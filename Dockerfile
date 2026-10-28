FROM eclipse-temurin:21-jdk-alpine

COPY /target/auction-0.0.1-SNAPSHOT.jar /auction/auction.jar

WORKDIR /auction

EXPOSE 8080

ENTRYPOINT [ "java","-jar","auction.jar", "--spring.profiles.active=docker"]