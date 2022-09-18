FROM adoptopenjdk/openjdk11:latest
RUN mkdir app4
COPY ./app/docker-spring-boot-4.0.jar app4
ADD ./app/application.yaml app4
ENTRYPOINT ["java", "-jar", "./app4/docker-spring-boot-4.0.jar", "./app4/application.yaml"]
EXPOSE 80