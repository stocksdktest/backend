FROM openjdk:8-jre-alpine

ADD /target/sddm-archive-0.0.1.jar /app.jar
RUN touch /app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]


