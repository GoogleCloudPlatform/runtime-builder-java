#!/bin/sh

set -e

cloudBuildFile=/usr/local/google/home/alexsloan/tmp/java.yaml
testApp=/usr/local/google/home/alexsloan/tmp/gs-spring-boot/complete

# build and push the builder image
mvn -DskipTests clean install
docker tag java-runtime-builder gcr.io/alexsloan-testing/java-runtime-builder
gcloud docker -- push gcr.io/alexsloan-testing/java-runtime-builder

export PROJECT_ID=alexsloan-testing
export _OUTPUT_IMAGE=gcr.io/$PROJECT_ID/appengine/dummy
envsubst < cloudbuild.yaml > $cloudBuildFile

# kick off the gcloud deploy process
pushd $testApp
gcloud container builds submit --config=$cloudBuildFile .
gcloud app deploy -q --image-url="$_OUTPUT_IMAGE" src/main/appengine/app.yaml
popd