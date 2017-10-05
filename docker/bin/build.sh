#!/bin/bash

scriptPos=${0%/*}

source "$scriptPos/image_conf.sh"

pushd "$scriptPos/.." > /dev/null
	if docker build -t $imageName image
    then
        echo -en "\033[1;34m  image created: $imageName \033[0m\n"
    else
        echo -en "\033[1;31m  error while create image: $imageName \033[0m\n"
    fi
popd > /dev/null

