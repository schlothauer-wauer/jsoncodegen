#!/bin/bash
scriptPos=${0%/*}

source "$scriptPos/image_conf.sh"

function show_help() {
echo '''
JsonCodeGen is a tool to generate stuff like plantuml diagramms or source code from
JSON schemas.

usage:
./run_jsonCodeGen.sh -m MODEL_FILE -o DEST_DIR -g GENERATOR [-p GENERATOR_PARAMETER]

example:
./run_jsonCodeGen.sh -m /home/test/my_model.json -d ./out -g plantuml \
    -p destFileName=my_diagram.puml -p removeEmptyLines=true
'''
}

function checkInputAndExitIfEmpty() {
    variableToCheck=$1
    msgTxt=$2
    if [ -z "$variableToCheck" ]; then
        echo "$msgTxt"
        echo
        show_help
        exit 1
    fi
}

#    @Parameter(names = [ '-m', '--model' ], description = "Path to JSON schema to parse", required = true)
#    @Parameter(names = [ '-o', '--outputBase' ], description = "Base directory for the output", required = true)
#    @Parameter(names = ['-g', '--generator'], description = "generator that are used with the model. This parameter can be used multiple times")
#    @Parameter(names = ['-gp', '--generator-parameter'], description = "special parameter that are passed to template via maps")
#    @Parameter(names = ['-h','--help'], help = true)

generatorParameters="-gp removeEmptyLines=true"

while getopts "h?m:o:g:p:" opt; do
    case "$opt" in
    h|\?)
        show_help
        exit 0
        ;;
    m)  model=$OPTARG
        ;;
    o)  outputBase=$OPTARG
        ;;
    g)  generator=$OPTARG
        ;;
    p)  generatorParameters+=" -gp $OPTARG"
        echo $OPTARG
        ;;
    esac
done

shift $((OPTIND -1))

checkInputAndExitIfEmpty "$model" "model file is needed"
checkInputAndExitIfEmpty "$outputBase" "the directory to write the output is needed"
checkInputAndExitIfEmpty "$generator" "the generator to use is needed"

if ! [ -f "$model" ]; then
    echo
    echo "given model file not found in file system: $model"
    exit 1
fi

if ! [ -d "$outputBase" ]; then
    echo
    echo "disired output directory doesn't exist: $outputBase"
    exit 1
fi

modelDir=${model%/*}
absolutModelDir=`pushd "$modelDir" > /dev/null;pwd;popd > /dev/null`
modelFile=${model##*/}

outputDir=`pushd "$outputBase" > /dev/null;pwd;popd > /dev/null`

aktImgName=`docker images |  grep -G "$imageBase *$imageTag *" | awk '{print $1}'`
aktImgVers=`docker images |  grep -G "$imageBase *$imageTag *" | awk '{print $2}'`


if [ "$aktImgName" == "$imageBase" ] && [ "$aktImgVers" == "$imageTag" ]
then
        echo "run container from image: $aktImgName:$aktImgVers"
else
    if docker build -t $imageName $scriptPos/../image
    then
        echo -en "\033[1;34m  image created: $1 \033[0m\n"
    else
        echo -en "\033[1;31m  error while create image: $imageName \033[0m\n"
        exit 1
    fi
fi

docker run --rm -it -v "$absolutModelDir":/opt/model -v "$outputDir":/opt/output "$imageName" /opt/jsonCodeGen/jsonCodeGen.sh -o "/opt/output" -m "/opt/model/$modelFile" -g "$generator" $generatorParameters


