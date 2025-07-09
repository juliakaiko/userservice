# Используем образ Tomcat с Java 17.
#FROM tomcat:9.0-jdk21

# Удаляем стандартные приложения Tomcat (опционально)
#RUN rm -rf /usr/local/tomcat/webapps/*

#WORKDIR /usr/local/tomcat

# Копируем WAR-файл в папку webapps Tomcat
#COPY target/userservice-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Открываем порт, на котором будет работать Tomcat
#EXPOSE 8080

# Команда для запуска Tomcat
#CMD ["catalina.sh", "run"]

# 1 mvn clean package
# 2 docker build -t tasksystem:latest .
# 3 docker run -p 8080:8080 tasksystem:latest

# Используем официальный образ Tomcat с Java 21
FROM tomcat:10.1-jdk21-temurin
#tomcat:10.1-jdk21-openjdk

# Удаляем дефолтные приложения (опционально)
RUN rm -rf /usr/local/tomcat/webapps/*

# Установить redis-tools
RUN apt-get update && apt-get install -y redis-tools

# Копируем WAR-файл в контейнер (переименовываем в ROOT.war для доступа по корневому URL)
COPY target/userservice-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Открываем порт Tomcat
EXPOSE 8080

# Запускаем Tomcat
CMD ["catalina.sh", "run"]