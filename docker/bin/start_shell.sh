#!/bin/bash

scriptPos=${0%/*}

source "$scriptPos/image_conf.sh"


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

docker run -it --rm "$imageName" /bin/sh


