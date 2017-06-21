# Usage
This program provides some kind of extendable code generation. As input it use
JSON schema files that describes a model

Examples:
```bash
# 1. reads a model from file ./test/mein_modell.json
# 2. use the internal java_bean generator
# 3. gives the extra parameter packageBase with value 'de.sw.atlas' to the generator
# 4. writes the output to ./test/mein_output
de.lisaplus.atlas.DoCodeGen -o ./test/mein_output -m ./test/mein_modell.json -g java_beans -gp packageBase=de.sw.atlas 

# 1. reads a model from file ./test/mein_modell.json
# 2. use two internal java_bean generators: java_beans, swagger_file
# 3. gives the extra parameter packageBase with value 'de.sw.atlas' to the generator
# 4. writes the output to ./test/mein_output
de.lisaplus.atlas.DoCodeGen -o ./test/mein_output -m ./test/mein_modell.json -g java_beans -g swagger_file \
    -gp packageBase=de.sw.atlas

# 1. reads a model from file ./test/mein_modell.json
# 2. use two internal 'multifiles'-generator with the external template ./test/myTemplate.txt
# 3. gives the extra parameter packageBase with value 'de.sw.atlas' to the generator
# 4. writes the output to ./test/mein_output
de.lisaplus.atlas.DoCodeGen -o ./test/mein_output -m ./test/mein_modell.json -g multifiles=./test/myTemplate.txt  \
    -gp packageName=de.sw.atlas
```
```bash
# creates a json schema from test schema - only for test cases
build/release/jsonCodeGen.sh -o /tmp -m src/test/resources/test_schemas/multiType.json \
    -g singlefile=src/main/resources/templates/meta/json_schema.txt \
    -gp destFileName=test.json

# creates java beans from test schema - only for test cases
build/release/jsonCodeGen.sh -o /tmp/test_beans -m src/test/resources/test_schemas/multiType.json \
    -g multifiles=src/main/resources/templates/java/java_bean.txt \
    -gp destFileNameExt=.java -gp packageName=de.sw.atlas.test
```

## Already defines internal generators with their extra parameters:

### Java related
* java_beans - creates a set of Java beans from model
  - packageName - what is the package for the generated beans
  - removeEmptyLines - if set, then empty lines will be removed before file is written 
  
### Service related 
* swagger_file - creates a swagger file from model
  - destFileName - name of the generated file
  - removeEmptyLines - if set, then empty lines will be removed before file is written 
  
### Base generators
* multifiles - creates multiple files from model and extra given template
  - includeExtTypes - also use external type to generate files
  - outputDirExt - a needed subpath below the given outputBasePath
  - removeEmptyLines - if set, then empty lines will be removed before file is written 

* singlefile - creates single file from model and extra given template
  - outputDirExt - a needed subpath below the given outputBasePath
  - destFileName - filename for the generated file
  - removeEmptyLines - if set, then empty lines will be removed before file is written 
