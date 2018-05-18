#!/bin/bash

scriptPos=${0%/*}

if [ -z "$NEXUS_USER" ]; then
    echo "NEXUS_USER not defined"
    exit 1
fi

if [ -z "$NEXUS_RAW_ARCHIVE" ]; then
    echo "NEXUS_RAW_ARCHIVE not defined"
    exit 1
fi

version=""
read -r -s -p "desired release version: " version

if [ -z "$version" ]; then
    echo "need a version number, but nothing was given - cancel"
    exit 1
fi

pushd "$scriptPos/.." > /dev/null
if ! gradle clean buildRelease; then
    echo 'error while build release'
    exit 1
fi

cd build/release


releaseFile="jsonCodeGen_$version.tgz"

if [ -f "$releaseFile" ]; then
    rm -f "$releaseFile"
fi

tar -czf "$releaseFile" *

curl -v --user "$NEXUS_USER" --upload-file "$releaseFile" \
    "$NEXUS_RAW_ARCHIVE/$releaseFile"

popd > /dev/null

