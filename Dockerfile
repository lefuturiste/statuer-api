FROM maven:3.3-jdk-8
COPY . /app
WORKDIR /app
RUN mvn package
ENV PORT 80
EXPOSE 80
CMD ["mvn", "exec:java", "-Dexec.mainClass=fr.lefuturiste.statuer.App"]