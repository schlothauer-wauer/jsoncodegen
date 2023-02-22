scriptPos=${0%/*}

imageBase=schlothauer/jsoncodegen
imageTag=0.11.3

if [ -f $scriptPos/../../build.gradle ]; then
    imageTag=$(cat $scriptPos/../../build.gradle | grep project.version | awk '{ print $3 }' | sed -e "s-'--g")
fi

imageName="$imageBase:$imageTag"

echo "$imageName"
