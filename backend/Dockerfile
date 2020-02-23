#FROM openjdk:8-jre-alpine
#VOLUME /tmp
#ARG DOCKERIZE_VERSION=v0.6.1
#ARG ARTIFACT_NAME
#ENV SPRING_PROFILES_ACTIVE prod
#
#ADD ${ARTIFACT_NAME}.jar /app.jar
#RUN touch /app.jar
##EXPOSE 8088
#ENTRYPOINT ["java","-jar","/app.jar"]


FROM maven AS build
WORKDIR /app
COPY . .
RUN mvn package

FROM openjdk:8-jre-alpine
WORKDIR /tmp
COPY --from=build /app/target/sddm-archive-0.0.1.jar  .
ADD sddm-archive-0.0.1.jar /app.jar
RUN touch /app.jar
EXPOSE 8088
ENTRYPOINT ["java","-jar","/app.jar"]


