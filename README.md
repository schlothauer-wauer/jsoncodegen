# jsonCodeGen
A simple Groovy based program to do generation tasks from a JSON schema.

Branchinfo: implements additional path operations (put, post, delete) for the swagger file.


## Requirements
* Java 8
* Gradle v4.*

## Unsupported JSON schema features
* patternProperties - make no sense in model description
* only references in the local document and local file system are supported

## Additional features
* easier version markup in model
* mark attributes as aggreation types (references) with suffix '_id'
* additional property attribute 'aggregationType' to set the specific attribute information

```javascript
    // How to mark different aggregation types in the model
    // Attention, this works only for "$ref" types!
    ...
    "Greens": {
      "type": "object",
      "additionalProperties": false,
      "properties": {
        "green_id": {
            // aggregation
            "$ref": "#/definitions/GreenType"
        },
        "green_obj": {
            // composition
            "$ref": "#/definitions/GreenType"
        },
        "green_obj": {
            // aggregation
            "aggregationType": "aggregation",
            "$ref": "#/definitions/GreenType"
        },
        "greens": {
          // composotion
          "type": "array",
          "items": {
            "$ref": "#/definitions/GreenType"
          }
        }
      }
    },
    ...
```


## Handle with gradle
### Using with gradle
```bash
# do a complete release to configured maven repository
gradle publish

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

# example swagger call
build/release/jsonCodeGen.sh -g swagger -o ~/tmp -m src/test/resources/test_schemas/ds/user.json \
  -gp removeEmptyLines=true -gp host=api.lisaplus.de
```

To get a better understanding how the program works see [here](src/test/groovy/de/lisaplus/atlas/codegen/test/PlantUml.groovy)

## Program design
![Basic class design](http://www.plantuml.com/plantuml/png/5SX13iCm20NHgxG7gDddCfMBKUp8XWW-olMJRaPFynxACvkaprS7pjY8l5vb7-Zvon1dKuYYi2qAxjFGQuf_hd_f25Es9hiehHfuLZEEnqE_0Kz6kfGprxm1)

[Diagram source](docs/main_structure.puml) (main structure) describes the
basic design.

## Usage
[see here](src/main/resources/docs/usage.md)
