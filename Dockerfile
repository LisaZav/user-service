FROM openjdk:17-alpine

RUN apk add --no-cache bash

COPY target/your-app.jar /app.jar

COPY wait-for-postgres.sh /wait-for-postgres.sh
RUN chmod +x /wait-for-postgres.sh

CMD ["/wait-for-postgres.sh", "postgres:5432", "--", "java", "-jar", "/app.jar"]