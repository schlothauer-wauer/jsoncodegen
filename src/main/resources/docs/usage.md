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
# 2. use two internal java_bean generators: java_beans, swagger
# 3. gives the extra parameter packageBase with value 'de.sw.atlas' to the generator
# 4. writes the output to ./test/mein_output
de.lisaplus.atlas.DoCodeGen -o ./test/mein_output -m ./test/mein_modell.json -g java_beans -g swagger \
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
* generator parameter marked with (!) are needed

### Java related
* java_beans - creates a set of Java beans from model
  - packageName - what is the package for the generated beans (!)
  - removeEmptyLines - if set, then empty lines will be removed before file is written 
  - containsAttrib - name of an attribute that is needed for the generation
  - missingAttrib - name of an attribute that the type doesn't contain for generation
  
* java_interfaces - creates a set of Java interfaces from the model 
  - packageName - what is the package for the generated beans (!)
  - removeEmptyLines - if set, then empty lines will be removed before file is written 
  - containsAttrib - name of an attribute that is needed for the generation
  - missingAttrib - name of an attribute that the type doesn't contain for generation
  
* java_interfaced_beans - creates a set of Java beans from model that implements a given interface
  - packageName - what is the package for the generated beans (!)
  - interfacePackageName - Name of the interface package
  - removeEmptyLines - if set, then empty lines will be removed before file is written 
  - containsAttrib - name of an attribute that is needed for the generation
  - missingAttrib - name of an attribute that the type doesn't contain for generation

* java_interfaced_generic_derived_beans - creates a set of Java beans from model that implements a given interface
  - packageName - what is the package for the generated beans (!)
  - interfacePackageName - Name of the interface package
  - genericClass - Name of the generic class, inclusive the package name (!)
  - removeEmptyLines - if set, then empty lines will be removed before file is written 
  - containsAttrib - name of an attribute that is needed for the generation
  - missingAttrib - name of an attribute that the type doesn't contain for generation

* java_meta_bean - creates beans that describe the content of a typed bean
  
### Service related 
* swagger - creates a swagger file from model
  - host - name for the host entry
  - basePath - text for the basePath entry
  - appendVersionToPath - if true the versions is append to an additional configured basePath 
  - destFileName - name of the generated file (!)
  - removeEmptyLines - if set, then empty lines will be removed before file is written 

* swagger-ext - creates a more rest oriented swagger file from model (with hyperlinks and type descriptions)
  - host - name for the host entry
  - basePath - text for the basePath entry
  - appendVersionToPath - if true the versions is append to an additional configured basePath 
  - destFileName - name of the generated file (!)
  - removeEmptyLines - if set, then empty lines will be removed before file is written
  
### JSON Schema related
* json_schema - creates a new JSON schema from the existing one - don't make much sense, but good for debugging 
  - destFileName - name of the generated file
  - removeEmptyLines - if set, then empty lines will be removed before file is written

* hist_model - extends the current schema with data for entry history
  - destFileName - name of the generated file
  - removeEmptyLines - if set, then empty lines will be removed before file is written

### PlantUML
  - printTags - if set to true then plantuml prints also tags to the diagrams
  - guidTypeColor - if given that color is used as background for the guid-types, f.e. -gp guidTypeColor=adadad
  
### Base generators
* multifiles - creates multiple files from model and extra given template
  - includeExtTypes - also use external type to generate files
  - outputDirExt - a needed subpath below the given outputBasePath
  - removeEmptyLines - if set, then empty lines will be removed before file is written 
  - destFileNamePre - standard prefix for the created file names
  - destFileNameExt - file extension for the generated files

* singlefile - creates single file from model and extra given template
  - outputDirExt - a needed subpath below the given outputBasePath
  - destFileName - filename for the generated file
  - removeEmptyLines - if set, then empty lines will be removed before file is written 
