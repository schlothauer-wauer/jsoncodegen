#!/bin/bash

scriptPos=${0%/*}

# Uploads a binary release / release archive of jsonCodeGen to a NEXUS instance
# This script relies on these environment variabels:
# * nexus_user: Username use to authenticate at the NEXUS instance
# * nexus_password: Password used to authenticate at the NEXUS instance (optional)
# * nexus_raw_archive: URL to the directory, where the binary release is to be POSTed
# If no password is available, then curl will prompt for one!

# retrieve the version from project file
version=`cat "$scriptPos/../build.gradle" | grep project.version | grep = | sed -e "s-.* '--" -e "s-'--"`
echo "version: $version"

pushd "$scriptPos/.." > /dev/null
if ! gradle publish; then
    echo "error while publish jars to nexus"
    popd > /dev/null
fi
popd > /dev/null

if [ -z "$nexus_user" ]; then
    echo "nexus_user not defined"
    exit 1
fi

if [ -z "$nexus_raw_archive" ]; then
    echo "nexus_raw_archive not defined"
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

pushd "build/release" > /dev/null

releaseFile="jsonCodeGen_$version.tgz"

if [ -f "$releaseFile" ]; then
    rm -f "$releaseFile"
fi

tar -czf "$releaseFile" *

# use password if availagle
if [ -z "$nexus_password" ]; then
    user="$nexus_user"
else
    echo "Making us of env. variable nexus_password!"
    user="$nexus_user:$nexus_password"
fi

if ! curl -v --user $user --upload-file "$releaseFile" \
    "$nexus_raw_archive/$releaseFile"
then
    echo "error while upload release to nexus raw repo"
fi

git tag "$version"

echo "tagged version $version, please push it using command git push origin refs/tags/$version"

popd > /dev/null

popd > /dev/null
