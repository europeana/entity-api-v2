FROM tomcat:10-jre21
LABEL Author="Europeana Foundation <development@europeana.eu>"

# Clear out any security patches published since the base image release
RUN apk upgrade --no-cache

# Configure APM and add APM agent
# requires 
COPY ./misc/apm/ /usr/local/

COPY ./entity-web/target/entity-web-executable.jar /opt/app/entity-web-executable.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/app/entity-web-executable.jar"]