imageBase=schlothauer/jsoncodegen
imageTag=0.11.3

if [ -f build.gradle ]; then
    imageTag=`cat build.gradle | grep project.version | awk '{ print $3 }' | sed -e "s-'--g"`
fi

imageName="$imageBase:$imageTag"

echo "$imageName"
