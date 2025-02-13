FROM amazoncorretto:17-alpine-jdk
MAINTAINER baeldung.com
COPY target/psl-sync-dashboard.jar app.jar
ENV JAVA_OPTS="-Xms13g -Xmx15g"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]