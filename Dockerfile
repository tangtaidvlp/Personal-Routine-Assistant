FROM eclipse-temurin:25
LABEL authors="tommtang2106"
RUN mkdir /opt/app
COPY build/libs/routinemanager-0.0.1-SNAPSHOT.jar /opt/app/app.jar
ENTRYPOINT ["java", "-jar", "./opt/app/app.jar"]