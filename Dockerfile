# Maven
FROM maven:3.8.3-openjdk-16 as build
WORKDIR /app
COPY pom.xml .
#RUN mvn install
#RUN mvn -e -B dependency:resolve
#COPY src ./src
#RUN mvn clean -e -B package
#
## RTSDK Java
#FROM adoptopenjdk/openjdk16:x86_64-alpine-jre-16.0.1_9
#WORKDIR /app
#COPY --from=build /app/target/hideandseek-*.jar ./hideandseek.jar
#ENTRYPOINT ["java","-jar","./hideandseek.jar", "\"$@\""]
