FROM --platform=linux/amd64 maven:3.8.7-openjdk-18-slim
LABEL authors="yukino.xin"

WORKDIR /app
COPY ./ /app/
COPY .m2/ /root/.m2/
RUN mvn -s ./settings.xml clean package

ENTRYPOINT ["java", "-jar","./aa-starter/target/aa-starter-0.0.1.jar"]
