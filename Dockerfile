# For Java 11, try this
FROM adoptopenjdk/openjdk16:alpine-jre

# Refer to Maven build -> finalName
ARG JAR_FILE=target/booktracker-loader-0.0.1-SNAPSHOT.jar

# cd /opt/app
WORKDIR /opt/app

COPY secure-connect-booktracker.zip secure-connect-booktracker.zip

# cp target/spring-boot-web.jar /opt/app/app.jar
COPY ${JAR_FILE} app.jar

# java -jar /opt/app/app.jar
ENTRYPOINT ["java","-jar","app.jar"]