# Java Runtime Builder

[![Build Status](https://travis-ci.org/GoogleCloudPlatform/runtime-builder-java.svg?branch=master)](https://travis-ci.org/GoogleCloudPlatform/runtime-builder-java)

A [Google Cloud Container Builder](https://cloud.google.com/container-builder/docs/) pipeline for 
packaging Java applications into supported Google Cloud Runtime containers. It consists of several
docker containers which are run using the [cloudbuild.yaml](cloudbuild.yaml) configuration file.

## Building
To build all docker images:
```bash
mvn package
```

## Runnning Locally
TODO

## Contributing changes

* See [CONTRIBUTING.md](CONTRIBUTING.md)

## Licensing

* See [LICENSE](LICENSE)
