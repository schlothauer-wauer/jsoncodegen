Here are the place for docker related stuff ... Image description and starter scripts

The idea behind that approach is to run the code generation inside a docker container
and free the user from the dependency of an installed Java

```
# example call for a simple plantuml generation
docker run -v ABSOLUTE_PATH_TO_SRC_FOLDER:/opt/input \
    -v ABSOLUTE_PATH_TO_DEST_FOLDER:/opt/output LIST_OF_JSONCODEGEN_PARAMETER

# for example ...
docker run -v /home/eiko/prog/gitlab/lisa-server_models/model:/opt/input \
    -v /tmp/output:/opt/output \
    schlothauer/jsoncodegen:0.4.1 \
    -g plantuml \
    -m /opt/input/junction.json \
    -o /opt/output \
    -gp destFileName=test.puml

bin/run_jsonCodeGen.sh -m ../src/test/resources/schemas/vera.json \
    -o /tmp -g plantuml -p destFileName=test.puml
```

## How to create a docker image
Requirements:
- installed Java v8
- installed Gradle
```bash
# show all available Gradle tasks (only for information)
gradle tasks --all

# preparation - create a release and copy the needed libraries to the docker image source
gradle buildDockerImage

# creates a docker image ...
schlothauer/jsoncodegen:TAG
```

