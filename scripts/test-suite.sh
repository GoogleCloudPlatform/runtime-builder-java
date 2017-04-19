#!/bin/sh
#
# Runs a suite of integration tests for a builder pipeline.

DIR=$(dirname $0)
PROJECT_ROOT=$DIR/..
TEST_CASES_DIR=$PROJECT_ROOT/test/integration/test_cases

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
  $PROJECT_ROOT/test/integration/run-test.sh --test-dir "$TEST_CASE" --img $IMAGE_UNDER_TEST
done
