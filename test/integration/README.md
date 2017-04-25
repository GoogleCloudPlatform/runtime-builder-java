# Integration Testing
This directory contains an integration testing framework for testing the java-runtime-builder 
pipeline. Each immediate subdirectory of [`test_cases`](test_cases) is considered a distinct test 
case. Each test case directory should contain the following files:
  * a repo.cfg file, which must define a `TEST_APP_DIR` variable
  * a structure.yaml file to specify the structure test spec for the built image
  * (optionally) an app.yaml config file

For each test case, the builder pipeline is invoked on the `TEST_APP_DIR` directory specified in 
repo, using the provided app.yaml file as configuration. Once the application container is built, 
structure tests are run on it using the provided structure.yaml file.

## Running
To run the tests, make sure that:
* The Cloud SDK is installed locally
* The image under test is avaialble in a gcr.io docker registry
* Git submodules in this repo have been checked out
```bash
git submodule update --init
test-suite.sh gcr.io/my-gcp-project/my-builder-image
```

