# Use the official Tomcat image with Java 21
FROM tomcat:10.1-jdk21-temurin

# Remove default Tomcat apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Install redis-tools for Redis CLI support
RUN apt-get update && apt-get install -y redis-tools

# Copy the WAR file into the container (renamed to ROOT.war for root URL access)
COPY target/userservice-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Expose Tomcat's default port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
