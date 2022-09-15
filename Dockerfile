FROM adoptopenjdk/openjdk11:latest
RUN mkdir /home/ubuntu/app
COPY /home/ubuntu/app/docker-spring-boot.jar /home/ubuntu/app
ENTRYPOINT ["java", "-jar", "/home/ubuntu/app/docker-spring-boot.jar"]
EXPOSE 80