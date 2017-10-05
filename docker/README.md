Here are the place for docker related stuff ... Image description and starter scripts

The idea behind that approach is to run the code generation inside a docker container
and free the user from the dependency of an installed Java

```
# example call
bin/run_jsonCodeGen.sh -m ../src/test/resources/schemas/vera.json \
    -o /tmp -g plantuml -p destFileName=test.puml
```
