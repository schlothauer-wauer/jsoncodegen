# jsonCodeGen
A simple Groovy based program to do generation tasks from a JSON schema.

## Requirements
* Java 8
* Gradle

## Unsupported JSON schema features
* patternProperties - make no sence in model description


## Using
```bash
# builds a release with all dependencies
# release is built in PROJECT_DIR/build/release
# before a release is build the tests are executed - skip not possible
gradle buildRelease

# run program without any arguments from project
gradle myRun

# run program with arguments ... opens test schema
gradle myRun -PmyArgs="-m,src/test/resources/schemas/ProcessDataEvent.json"
```