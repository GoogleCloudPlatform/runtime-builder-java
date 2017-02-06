FROM openjdk:8-jdk

# install utilities
RUN apt-get update && apt-get install -y curl

# install maven
ARG MAVEN_VERSION=3.3.9

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
    | tar -xzC /usr/share/maven --strip-components=1 \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV M2_HOME /usr/share/maven

ADD java-runtime-builder-app.jar /java-runtime-builder-app.jar
ENTRYPOINT ["java", "-jar", "/java-runtime-builder-app.jar"]
