FROM adoptopenjdk/openjdk11:jre-11.0.8_10-alpine
WORKDIR /opt
COPY target/*.jar /opt/app.jar
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar --spring.profiles.active=aws --spring.config.location=/tmp/