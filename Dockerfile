FROM maven:3.3-jdk-8
COPY . /app
WORKDIR /app
# install dependencies
# RUN mvn dependency:resolve
# build the app
RUN mvn clean compile assembly:single
WORKDIR /app/target
ENV PORT 80
EXPOSE 80
# java -cp /app/target/*.jar fr.lefuturiste.statuer.App
CMD ["sh", "/app/start.sh"]