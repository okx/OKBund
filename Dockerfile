FROM maven:3.8.7-openjdk-18-slim
LABEL authors="yukino.xin"

WORKDIR /app
COPY ./ /app/
RUN mvn clean package

ENTRYPOINT ["java", "-jar","./aa-starter/target/aa-starter-0.0.1.jar"]
