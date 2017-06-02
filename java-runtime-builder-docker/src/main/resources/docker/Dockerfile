# Use wget container to download gradle
FROM gcr.io/cloud-builders/wget as wget
ARG GRADLE_VERSION=3.4
RUN mkdir -p /usr/share/gradle \
  && wget -nv -O gradle-$GRADLE_VERSION-bin.zip https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip

FROM gcr.io/cloud-builders/java/mvn:3.5.0-jdk-8
ARG GRADLE_VERSION=3.4
# copy gradle installation from wget container
COPY --from=wget gradle-$GRADLE_VERSION-bin.zip .
# unpack and install gradle
RUN unzip -qq gradle-$GRADLE_VERSION-bin.zip \
  && mkdir -p /usr/share/ \
  && mv gradle-$GRADLE_VERSION /usr/share/ \
  && ln -s /usr/share/gradle-$GRADLE_VERSION/bin/gradle /usr/bin/gradle
ENV GRADLE_HOME /usr/share/gradle-$GRADLE_VERSION

# add the runtime builder application
ADD ${runtime.builder.artifact} /${runtime.builder.artifact}

ENTRYPOINT ["java", "-jar", "/${runtime.builder.artifact}"]
