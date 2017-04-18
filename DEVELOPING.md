# Developing

This document contains instructions on how to build and test this pipeline.

## Building
### Local Build
To build all images for this pipeline locally, you need docker and maven installed. Running 
`mvn install` will compile all java sources and build all docker images. After the build is complete, 
all resulting images will be present in your local docker repository.

### Cloud Build
Google Cloud Container Builder can also be used to build images in the pipeline. The 
[Google Cloud SDK](https://cloud.google.com/sdk/) must be installed locally in order to use Google
Cloud Container Builder. We provide a script to make it easy to build using Container Builder:

```bash
# the following commands will build and push an image named "gcr.io/my-gcp-project/runtime-builder:tag"
PROJECT_ID=my-gcp-project
TAG=tag
./scripts/build.sh gcr.io/$PROJECT_ID $TAG
```

## Running
The [run-pipeline](scripts/run-pipeline.sh) script can be used to build and run the pipeline. It 
builds all build steps locally, pushes to GCR, and executes the build pipeline via Google Cloud 
Container Builder. The script uses the default GCP project that is configured in your Cloud SDK
settings.
```bash
./scripts/run-pipeline.sh /path/to/some/source/to/build
```
