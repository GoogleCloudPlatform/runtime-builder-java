# Integration Testing
This directory contains an integration testing framework for testing the java-runtime-builder 
pipeline. Each immediate subdirectory of [`test_cases`](test_cases) is considered a distinct test 
case. Each test case directory should contain the following files:
  * a repo.cfg file, which must define a GIT_REPO that will be cloned for the test
  * a structure.yaml file to specify the structure test spec for the built image
  * (optionally) an app.yaml config file

For each test case, the repo specified in repo.cfg is cloned, and the pipeline is invoked on the 
repo, using the provided app.yaml file as configuration. Once the application container is built, 
structure tests are run on it using the provided structure.yaml file.

## Running
To run the tests, you need to first have the Cloud SDK and git installed locally. The test suite
invokes each test.
```bash
test-suite.sh gcr.io/my-gcp-project/my-builder-image
```

