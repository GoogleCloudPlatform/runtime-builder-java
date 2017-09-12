#!/bin/bash
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
TEST_APPS_ROOT=$PROJECT_ROOT/test/integration/test_resources
DISABLE_SOURCE_BUILD="false"

# locate test configuration files in the provided test directory
REPO_CFG_FILE=$TEST_DIR/repo.cfg
STRUCTURE_TEST_CONFIG=$TEST_DIR/structure.yaml
BUILDER_CONFIG=$TEST_DIR/app.yaml # this file is optional
if [ ! -f $REPO_CFG_FILE -o ! -f $STRUCTURE_TEST_CONFIG ]; then
  echo "Integration test configuration files are missing from directory $TEST_DIR"
  exit 1
fi

# read config from file
source $REPO_CFG_FILE
if [ -z $TEST_APP_DIR ]; then
  echo "Error: Repo config file $REPO_CFG_FILE must specify the \$TEST_APP_DIR field"
  exit 1
fi

# use tmp directory to stage the test app dir
TEST_STAGING_DIR=$(mktemp -d)

# copy the test app to the staging directory
cp -r $TEST_APPS_ROOT/$TEST_APP_DIR/* $TEST_STAGING_DIR

# copy test config files into the staging directory
if [ -f $BUILDER_CONFIG ]; then
  cp $BUILDER_CONFIG $TEST_STAGING_DIR
fi
cp $STRUCTURE_TEST_CONFIG $TEST_STAGING_DIR

# prepare the build pipeline config file for the test
mkdir -p $PROJECT_ROOT/target
PIPELINE_CONFIG=$PROJECT_ROOT/target/java_templated.yaml
cp $PROJECT_ROOT/java.yaml $PIPELINE_CONFIG
# replace the builder image name
sed -i -e "s%gcr.io/gcp-runtimes/java/runtime-builder:latest%$IMAGE_UNDER_TEST%" $PIPELINE_CONFIG
# remove the image push step
sed -i -e "s/^images.*$//" $PIPELINE_CONFIG

if [ "$DISABLE_SOURCE_BUILD" = "false" ]; then
  # remove the --no-source-build flag, if it exists
  sed -i -e "s/^.*--no-source-build.*$//" $PIPELINE_CONFIG
fi

# append structure tests to the build
cat $DIR/structure_test_build_step.yaml.in >> $PIPELINE_CONFIG

echo 'Invoking container test build with configuration:'
cat $PIPELINE_CONFIG

set -x
gcloud container builds submit $TEST_STAGING_DIR \
  --config $PIPELINE_CONFIG \
  --substitutions "_OUTPUT_IMAGE=output-image,_STRUCTURE_TEST_SPEC=structure.yaml"

