# Используем официальный образ Tomcat с Java 21
FROM tomcat:10.1-jdk21-temurin

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
