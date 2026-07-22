FROM eclipse-temurin:17-jre-alpine
LABEL Author="Europeana Foundation <development@europeana.eu>"

# Clear out any security patches published since the base image release
RUN apk upgrade --no-cache

# Configure APM and add APM agent
ENV ELASTIC_APM_VERSION=1.56.0
ADD https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/$ELASTIC_APM_VERSION/elastic-apm-agent-$ELASTIC_APM_VERSION.jar  /opt/app/elastic-apm-agent.jar

# Copy unzipped directory so we can mount config files in Kubernetes pod
#COPY entity-web/target/entity-web ./ROOT/

COPY ./entity-web/target/entity-web-executable.jar /opt/app/entity-web-executable.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/app/entity-web-executable.jar"]