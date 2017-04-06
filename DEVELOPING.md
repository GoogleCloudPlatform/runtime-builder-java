# Developing

This document contains instructions on how to build and test this pipeline.

## Local Build
To build all images for this pipeline locally, you need docker and maven installed. Running 
`mvn install` will compile all java sources and build all docker images. After the build is complete, 
all resulting images will be present in your local docker repository.

## Cloud Build
Google Cloud Container Builder can also be used to build images in the pipeline. The 
[Google Cloud SDK](https://cloud.google.com/sdk/) must be installed locally in order to use Google
Cloud Container Builder. We provide a script to make it easy to build using Container Builder:

```bash
# the following commands will build and push an image named "gcr.io/my-gcp-project/runtime-builder:tag"
PROJECT_ID=my-gcp-project
TAG_NAME=tag
./scripts/build.sh gcr.io/$PROJECT_ID $TAG
```

## Running locally-built images
Locally assembled build steps can be run locally, one at a time, using docker. Note that these
commands effectively mirror the steps in the [java.yaml](java.yaml) pipeline config file.

```bash
# compile my application's source and generate a dockerfile
docker run -v /path/to/my/java/app:/workspace -w /workspace runtime-builder \
    --jar-runtime=gcr.io/google-appengine/openjdk \
    --server-runtime=gcr.io/google-appengine/jetty
    
# package my application into a docker container
docker build -t my-java-app /path/to/my/java/app/.docker_staging
```
