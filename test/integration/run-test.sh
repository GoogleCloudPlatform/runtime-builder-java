#!/bin/sh
#
# Invokes a builder pipeline on a given test application, producing a container in the desired
# docker registry. Then, runs the provided structure test spec on the built image.

set -e

usage() {
  echo "Usage: run-test.sh --img <image_under_test> --test-dir <test_case_directory>"
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

if [ -z "${TEST_DIR}" -o -z "${IMAGE_UNDER_TEST}" ]; then
  usage
  exit 1
fi

DIR=$(dirname $0)
PROJECT_ROOT=$DIR/../..

# locate test configuration files in the provided test directory
GIT_CFG_FILE=$TEST_DIR/repo.cfg
STRUCTURE_TEST_CONFIG=$TEST_DIR/structure.yaml
BUILDER_CONFIG=$TEST_DIR/app.yaml # this file is optional
if [ ! -f $GIT_CFG_FILE -o ! -f $STRUCTURE_TEST_CONFIG ]; then
  echo "Integration test configuration files are missing from directory $TEST_DIR"
  exit 1
fi

# read config from file
source $GIT_CFG_FILE
if [ -z $GIT_REPO ]; then
  echo "Error: Git repo config file $GIT_CFG_FILE must specify the GIT_REPO field"
  exit 1
fi

# clone the git repo, invoke the build pipeline under test on it, then perform verifications on the
# resulting built image.
echo "Cloning from git repo $GIT_REPO"
APP_DIR=$(mktemp -d)
git clone $GIT_REPO $APP_DIR --quiet --depth=10

# copy necessary config files into app directory
if [ -f $BUILDER_CONFIG ]; then
  cp $BUILDER_CONFIG $APP_DIR
fi
cp $STRUCTURE_TEST_CONFIG $APP_DIR

# escape special characters in the builder image string so we can use it as a sed substitution below
ESCAPED_IMG_UNDER_TEST=$(echo $IMAGE_UNDER_TEST | sed -e 's/[\/&]/\\&/g')
DOCKER_NAMESPACE=gcr.io/$(gcloud config get-value project 2> /dev/null)
OUTPUT_IMAGE=$DOCKER_NAMESPACE/test-output-img:$(date -u +%Y-%m-%d_%H_%M)

# prepare the build pipeline config file for the test
mkdir -p $PROJECT_ROOT/target
PIPELINE_CONFIG=$PROJECT_ROOT/target/java_templated.yaml
cp $PROJECT_ROOT/java.yaml $PIPELINE_CONFIG
# replace the builder image name
sed -i -e "s/gcr.io\/gcp-runtimes\/java\/runtime-builder\:latest/$ESCAPED_IMG_UNDER_TEST/" $PIPELINE_CONFIG
# remove the image push step
sed -i -e "s/^images.*$//" $PIPELINE_CONFIG
# append structure tests to the build
cat $DIR/structure_test.yaml >> $PIPELINE_CONFIG

gcloud container builds submit $APP_DIR \
  --config $PIPELINE_CONFIG \
  --substitutions "_OUTPUT_IMAGE=$OUTPUT_IMAGE,_STRUCTURE_TEST_SPEC=structure.yaml"

