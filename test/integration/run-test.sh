#!/bin/sh
#
# Invokes a builder pipeline on a given test application, producing a container in the desired
# docker registry. Then, runs the provided structure test spec on the built image.

set -e

usage() {
  echo "Usage: run-test.sh --test-dir <test_case_directory>"
}

while [[ $# -gt 1 ]]; do
key="$1"
case $key in
    --img)
    IMAGE_UNDER_TEST="$2"
    shift
    ;;
    --test-dir)
    TEST_DIR="$2"
    shift
    ;;
    *)
    # unknown option
    usage
    exit 1
    ;;
esac
shift
done

if [ -z "${TEST_DIR}" ]; then
  usage
  exit 1
fi

# TODO instead of doing these as separate Argo invocations, instead we should just create/generate ne massive argo build the builds and tests each image in parallel

DIR=$(dirname $0)
PROJECT_ROOT=$DIR/../..

# locate test configuration files
GIT_CFG_FILE=$TEST_DIR/repo.cfg
STRUCTURE_TEST_CONFIG=$TEST_DIR/structure.yaml
BUILDER_CONFIG=$TEST_DIR/app.yaml
if [ ! -f $GIT_CFG_FILE -o ! -f $STRUCTURE_TEST_CONFIG ]; then
  echo "Integration test configuration files are missing from directory $TEST_DIR"
  exit 1
fi

# reset config variables
GIT_REPO=
# read config from file
source $GIT_CFG_FILE
if [ -z $GIT_REPO ]; then
  echo "Error: Git repo config file $GIT_CFG_FILE is missing the GIT_REPO field"
  exit 1
fi

# clone the git repo, invoke the build pipeline under test on it, then perform verifications on the
# resulting built image.
echo "Cloning from git repo $GIT_REPO"
APP_DIR=$(mktemp -d)
git clone $GIT_REPO $APP_DIR --quiet --depth=10
if [ -f $BUILDER_CONFIG ]; then
  cp $BUILDER_CONFIG $APP_DIR
fi

# invoke the pipeline under test, building the application container
GCLOUD_PROJECT=$(gcloud config get-value project 2> /dev/null)
DOCKER_NAMESPACE=gcr.io/$GCLOUD_PROJECT
OUTPUT_IMAGE=$DOCKER_NAMESPACE/test-output-img:$(date -u +%Y-%m-%d_%H_%M)
$PROJECT_ROOT/scripts/run-pipeline.sh --builder $IMAGE_UNDER_TEST --dir $APP_DIR --out $OUTPUT_IMAGE

# run structure tests on the image we just built
STRUCTURE_CONFIG_LOCAL="._structure.yaml"
cp $STRUCTURE_TEST_CONFIG $APP_DIR/$STRUCTURE_CONFIG_LOCAL

gcloud container builds submit $APP_DIR \
    --config $DIR/structure_test.yaml \
    --substitutions "_IMAGE_UNDER_TEST=$OUTPUT_IMAGE,_CONFIG_PATH=$STRUCTURE_CONFIG_LOCAL"
