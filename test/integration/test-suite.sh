#!/bin/bash
#
# Runs the full suite of integration tests for a builder pipeline.

set -e

DIR=$(dirname $0)
TEST_CASES_DIR=$DIR/tests

IMAGE_UNDER_TEST=$1
if [ -z $IMAGE_UNDER_TEST ]; then
  echo "Usage: $0 <image_under_test>"
  exit 1;
fi

# make sure git submodules are initialized
git submodule update --init

# run the full pipeline on all test cases
TEST_CASES=$(find $TEST_CASES_DIR -maxdepth 1 -mindepth 1 -type d)
for TEST_CASE in $TEST_CASES
do
  echo "Running test case $TEST_CASE"
  $DIR/run-test.sh --test-dir "$TEST_CASE" --img $IMAGE_UNDER_TEST
done
