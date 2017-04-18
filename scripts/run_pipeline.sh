#!/bin/bash

# Copyright 2016 Google Inc. All rights reserved.

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#     http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.bash

# Assembles and runs the build pipeline on a local source direcory
set -e

DIR=$(dirname $0)
PROJECT_ROOT=$DIR/..
GCLOUD_PROJECT=$(gcloud config get-value project 2> /dev/null)

SOURCE_DIR=$1
OUTPUT_IMAGE=$2

if [ -z $SOURCE_DIR ]; then
  echo "Usage: $0 <source_directory> [output_image_name]"
  exit 1;
fi

if [ -z $OUTPUT_IMAGE ]; then
  OUTPUT_IMAGE=gcr.io/$GCLOUD_PROJECT/output-image
fi

mvn install

BUILDER_IMAGE="gcr.io/$GCLOUD_PROJECT/runtime-builder:$(date -u +%Y-%m-%d_%H_%M)"
docker tag runtime-builder $BUILDER_IMAGE
gcloud docker -- push $BUILDER_IMAGE

gcloud container builds submit $SOURCE_DIR \
  --config $PROJECT_ROOT/builder-config/builder-template.yaml \
  --substitutions "_OUTPUT_IMAGE=$OUTPUT_IMAGE,_BUILDER_IMAGE=$BUILDER_IMAGE"

