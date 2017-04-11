#!/bin/sh

# Copyright 2016 Google Inc. All rights reserved.

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#     http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -e

dir=$(dirname $0)
projectRoot=$dir/..

RUNTIME_NAME="runtime-builder"
DOCKER_NAMESPACE=$1
DOCKER_TAG=$2

if [ -z "${DOCKER_NAMESPACE}" ]; then
  echo "Usage: ${0} <docker_namespace> [docker_tag]"
  exit 1
fi

if [ -z "${DOCKER_TAG}" ]; then
  DOCKER_TAG=$(date -u +%Y-%m-%d_%H_%M)
fi

IMAGE="${DOCKER_NAMESPACE}/${RUNTIME_NAME}:${DOCKER_TAG}"
echo "IMAGE: $IMAGE"

# build and test the runtime image
gcloud container builds submit \
  --config=$dir/pipeline-cloudbuild.yaml \
  --substitutions="_IMAGE=$IMAGE" \
  $projectRoot
