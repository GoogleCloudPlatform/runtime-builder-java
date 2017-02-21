# TODO choose better base image?
FROM openjdk:8-jdk

# install utilities
RUN apt-get -q update \
  && apt-get -q -y --no-install-recommends install curl \
  && apt-get clean \
  && rm /var/lib/apt/lists/*_*


# install maven
ARG MAVEN_VERSION=3.3.9
RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
    | tar -xzC /usr/share/maven --strip-components=1 \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV M2_HOME /usr/share/maven

# install gradle
ARG GRADLE_VERSION=3.4
RUN mkdir -p /usr/share/gradle \
  && curl -fsSLO https://services.gradle.org/distributions/gradle-3.4-bin.zip \
  && unzip -qq gradle-$GRADLE_VERSION-bin.zip \
  && rm gradle-$GRADLE_VERSION-bin.zip \
  && mkdir -p /usr/share/ \
  && mv gradle-$GRADLE_VERSION /usr/share/ \
  && ln -s /usr/share/gradle-$GRADLE_VERSION/bin/gradle /usr/bin/gradle
ENV GRADLE_HOME /usr/share/gradle-$GRADLE_VERSION

ADD java-runtime-builder-app.jar /java-runtime-builder-app.jar
ENTRYPOINT ["java", "-jar", "/java-runtime-builder-app.jar"]
