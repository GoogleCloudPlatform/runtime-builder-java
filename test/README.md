# Integration Testing
This directory contains an integration testing framework for testing the java-runtime-builder 
pipeline. Each test case is an immediate subdirectory of [`test_cases`](test_cases), and is expected
to contain, at minimum, the following files:
  * an app.yaml config file
  * a repo.txt file, containing a git remote url
  * a structure.yaml file to specify the structure test spec for the built image

## Running
To run the tests, you need to first have the Cloud SDK and git installed locally. The test suite
invokes each test, exercising the provided builder image.
```bash
test-suite.sh gcr.io/my-gcp-project/my-builder-image
```

