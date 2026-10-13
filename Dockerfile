FROM tomcat:10-jre21
LABEL Author="Europeana Foundation <development@europeana.eu>"

# Clear out any security patches published since the base image release
#for open jdk images
#RUN apk upgrade --no-cache
#for tomcat images
RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/*

# Configure APM and add APM agent
# requires 
<<<<<<< HEAD
COPY ./misc/apm/ /opt/app/
=======
COPY ./misc/apm/ /usr/local/
>>>>>>> branch 'develop' of https://github.com/europeana/entity-api-v2/

COPY ./entity-web/target/entity-web-executable.jar /opt/app/entity-web-executable.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/app/entity-web-executable.jar"]