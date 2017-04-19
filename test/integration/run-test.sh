#!/bin/sh
#
# Invokes a builder pipeline on a given test application, producing a container in the desired
# docker registry. Then, runs the provided structure test spec on the built image.

set -e

usage() {
  echo "Usage: run-test.sh --app-dir <application_dir> --test-config <structure_test_config> --builder <builder_image_under_test>"
}

while [[ $# -gt 1 ]]; do
key="$1"
case $key in
    --app-dir)
    APP_DIR="$2"
    shift
    ;;
    --test-config)
    STRUCTURE_TEST_CONFIG="$2"
    shift
    ;;
    --builder)
    IMAGE_UNDER_TEST="$2"
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

if [ -z "${APP_DIR}" -o -z "${STRUCTURE_TEST_CONFIG}" -o -z "${IMAGE_UNDER_TEST}" ]; then
  usage
  exit 1
fi

DIR=$(dirname $0)
PROJECT_ROOT=$DIR/../..
GCLOUD_PROJECT=$(gcloud config get-value project 2> /dev/null)
DOCKER_NAMESPACE=gcr.io/$GCLOUD_PROJECT

# invoke the pipeline under test, building the application container
OUTPUT_IMAGE=$DOCKER_NAMESPACE/test-output-img:$(date -u +%Y-%m-%d_%H_%M)
$PROJECT_ROOT/scripts/run-pipeline.sh --builder $IMAGE_UNDER_TEST --dir $APP_DIR --out $OUTPUT_IMAGE

# run structure tests on the image we just built
STRUCTURE_CONFIG_LOCAL="._structure.yaml"
cp $STRUCTURE_TEST_CONFIG $APP_DIR/$STRUCTURE_CONFIG_LOCAL

gcloud container builds submit $APP_DIR \
    --config $DIR/structure_test.yaml \
    --substitutions "_IMAGE_UNDER_TEST=$OUTPUT_IMAGE,_CONFIG_PATH=$STRUCTURE_CONFIG_LOCAL"
