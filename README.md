# jsonCodeGen
A simple Groovy based program to do generation tasks from a JSON schema.

## Requirements
* Java 8
* Gradle

## Unsupported JSON schema features
* patternProperties - make no sence in model description
* only references in the local document and local file system are supported

## Handle with gradle
### Using with gradle
```bash
# builds a release with all dependencies
# release is built in PROJECT_DIR/build/release
# before a release is build the tests are executed - skip not possible
gradle buildRelease

# run program without any arguments from project
gradle myRun

# run program with arguments ... opens test schema
gradle myRun -PmyArgs="-m,src/test/resources/schemas/ProcessDataEvent.json"

# complex example for debug mode run
gradle myRun -PDEBUG -PmyArgs="-o,/tmp/test_beans,-m,src/test/resources/test_schemas/multiType.json,\
-g,multifiles=src/main/resources/templates/java/java_bean.txt,\
-gp,destFileNameExt=java,-gp,packageName=de.sw.atlas.test"

# complex example without debug mode run
gradle myRun -PmyArgs="-o,/tmp/test_beans,-m,src/test/resources/test_schemas/multiType.json,\
-g,multifiles=src/main/resources/templates/java/java_bean.txt,\
-gp,destFileNameExt=java,-gp,packageName=de.sw.atlas.test"
```
### Usage of the release
After you built a release with gradle or you download a release bundle you can start
the program with the contained start script. If you start it with the help option you
get a full description of the possible parameters
```bash
# or a similar path
cd build/release
# start program in bash environment
./jsonCodeGen.sh

# show help in bash environment
./jsonCodeGen.sh --help
```

## Program design
The plant uml diagram ./docs/main_structure.puml describes the
basic design

## Usage
[see here](src/main/resources/docs/usage.md)