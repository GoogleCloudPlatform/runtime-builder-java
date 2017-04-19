#!/bin/sh
#
# Runs a suite of integration tests for a builder pipeline.

DIR=$(dirname $0)
PROJECT_ROOT=$DIR/..
TEST_CASES_DIR=$PROJECT_ROOT/test/integration/test_cases

GCLOUD_PROJECT=$(gcloud config get-value project 2> /dev/null)
DOCKER_NAMESPACE=gcr.io/$GCLOUD_PROJECT

IMAGE_UNDER_TEST=$1
if [ -z $IMAGE_UNDER_TEST ]; then
  echo "Usage: $0 <image_under_test>"
  exit 1;
fi

# run the full pipeline on all test cases
TEST_CASES=$(find $TEST_CASES_DIR -maxdepth 1 -mindepth 1 -type d)
for TEST_CASE in $TEST_CASES
do
  echo "Running test case $TEST_CASE"

  # read test data from the test_case directory
  GIT_REPO="$(cat $TEST_CASE/repo.txt)"
  BUILDER_CONFIG=$TEST_CASE/app.yaml
  STRUCTURE_TEST_CONFIG=$TEST_CASE/structure.yaml

  # clone a git repo, invoke the build pipeline under test on it, then perform verifications on the
  # resulting built image.
  echo "Cloning from git repo $GIT_REPO"
  APP_DIR=$(mktemp -d)
  git clone $GIT_REPO $APP_DIR --depth=10
  cp $BUILDER_CONFIG $APP_DIR
  $PROJECT_ROOT/test/integration/run-test.sh --app-dir "$APP_DIR" --test-config "$STRUCTURE_TEST_CONFIG" --builder "$IMAGE_UNDER_TEST"
done
