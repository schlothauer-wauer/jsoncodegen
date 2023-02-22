# jsonCodeGen
A simple Groovy based program to do generation tasks from a JSON schema.

## Requirements
* Java 17
* Gradle v7.*

## Unsupported JSON schema features
* patternProperties - make no sense in model description
* only references in the local document and local file system are supported

## Additional features
* easier version markup in model
* mark attributes as aggregation types (references) with suffix '_id'
* additional property attribute 'aggregationType' to set the specific attribute information
* allOf - to implement inheritance

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
            "__aggregationType": "aggregation",
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

# builds a release with all dependencies
# release is built in PROJECT_DIR/build/release
# before a release is build the tests are executed - skip not possible
gradle buildRelease

# builds release and copy artifacts to docker image dir as preparation for the image build
gradle copyReleaseToDockerImage

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

```bash
./jsonCodeGen.sh
Usage: de.lisaplus.atlas.DoCodeGen [options]
  Options:
    -at, --add-tag
      add a text as tag to a specific type, f.e. -at User=unused
      Default: []
    -b, --black-list
      black listed type, multiple usage possible
      Default: []
    -g, --generator
      generator that are used with the model. This parameter can be used 
      multiple times
      Default: []
    -gp, --generator-parameter
      special parameter that are passed to template via maps
      Default: []
    -h, --help

  * -m, --model
      Path to JSON schema to parse
    -o, --outputBase
      Base directory for the output
    -pmt, --print-main-types
      don't do any code generation, simply loads the model and print the 
      main-types of it
      Default: false
    -pmta, --print-main-types-attrib
      don't do any code generation, simply loads the model and print the 
      main-types of it
    -pmti, --print-main-types-info
      print with info header
      Default: false
    -pmts, --print-main-types-separator
      separator to use for printing main types
    -rt, --remove-tag
      remove a tag from a specific type, f.e. -rt User=unused
      Default: []
    -rta, --remove-tag-all
      remove a tag from all model types, f.e. -rta rest
      Default: []
    -w, --white-list
      white listed type, multiple usage possible
      Default: []
```

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

## Releases
[see here](Releases.md)

# Template Debugging
## General Remarks
From version 0.13.0 it is also possible to debug code from templates. This is
a common use case if the templates become more complicated. Unfortunately is it
not possible to debug code that is included direct into the template, but 
with the '-gs' command line switch a external Groovy script can be injected into
the code generation templates.

If the switch is used, then inside the template is a 'script' variable available.
This variable points to the injected script and allows to call functions that
are declared as clojure inside the generator-script.

```Groovy
// example usage of a generator script defined function
${script.generatorScriptDefinedFunction(possibleParameter)}
``` 

```Groovy
// example function definition in a generator script
def generatorScriptDefinedFunction(def someString) {
    return "Hello: $someString"
}
``` 
## Code Examples
* [example additional generator script](./src/test/resources/templates/handling_helper.groovy)
* [example template that utilize the script](./src/test/resources/templates/handling.txt)
* [test that combines both](./src/test/groovy/de/lisaplus/atlas/codegen/test/MultiFileTemplates.groovy)
* [test in a shell script](./bin/debug_example.sh)

## Steps to Debug template in IntelliJ
1. Open the jsonCodeGen project in IntelliJ
2. Open the generator library in the editor (example: `./src/test/resources/templates/handling_helper.groovy`)
3. Set a break point
4. Configure remote debugging in IntelliJ listen on port 8100
5. Run `bin/debug_example.sh` from command line
6. Start remote debugging in IntelliJ
7. Break-Point should be triggered in IDE

This approach can also be used for other projects.
