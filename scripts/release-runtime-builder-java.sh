#!/bin/bash
export KOKORO_GITHUB_DIR=${KOKORO_ROOT}/src/github
source ${KOKORO_GFILE_DIR}/kokoro/common.sh

cd ${KOKORO_GITHUB_DIR}/runtime-builder-java

./scripts/build.sh ${DOCKER_NAMESPACE} ${TAG}
