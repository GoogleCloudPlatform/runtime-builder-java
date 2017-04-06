# Developing

This document contains instructions on how to build and test this pipeline.

## Building
To build all images for this pipeline, you need docker and maven installed. Running `mvn install`
will compile all java sources and build all docker images. After the build is complete, the 
resulting image will be called `java-runtime-builder`.

## Running locally-built images
Locally assembled build steps can be run locally, one at a time, using docker:

```bash
# compile my application's source and generate a dockerfile
docker run -v /path/to/my/java/app:/workspace -w /workspace java-runtime-builder \
    --jar-runtime=gcr.io/google-appengine/openjdk \
    --server-runtime=gcr.io/google-appengine/jetty
    
# package my application into a docker container
docker build -t my-java-app /path/to/my/java/app/.docker_staging
```
