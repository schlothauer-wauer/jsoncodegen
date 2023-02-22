#!/bin/bash

scriptPos=${0%/*}

# retrieve the version from project file
version=`cat "$scriptPos/../build.gradle" | grep project.version | grep = | sed -e "s-.* '--" -e "s-'--"`
echo "version: $version"

pushd "$scriptPos/.." > /dev/null
if ! gradle publish; then
    echo "error while publish jars to nexus"
    popd > /dev/null
fi
popd > /dev/null

if [ -z "$NEXUS_USER" ]; then
    echo "NEXUS_USER not defined"
    exit 1
fi

if [ -z "$NEXUS_RAW_ARCHIVE" ]; then
    echo "NEXUS_RAW_ARCHIVE not defined"
    exit 1
fi

pushd "$scriptPos/.." > /dev/null
if ! gradle clean build; then
    echo 'error while build'
    exit 1
fi

if ! gradle buildRelease; then
    echo 'error while build release'
    exit 1
fi

cd build/release


releaseFile="jsonCodeGen_$version.tgz"

if [ -f "$releaseFile" ]; then
    rm -f "$releaseFile"
fi

tar -czf "$releaseFile" *

if ! curl -v --user "$NEXUS_USER" --upload-file "$releaseFile" \
    "$NEXUS_RAW_ARCHIVE/$releaseFile"
then
    echo "error while upload release to nexus raw repo"
fi

git tag "$version"

popd > /dev/null

