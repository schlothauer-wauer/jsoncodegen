#!/bin/bash
# Example script to illustrate the template debugging option of jsonCodeGen

scriptPos=${0%/*}

debugStarterScript=$scriptPos/../build/release/jsonCodeGenDebug.sh
if ! [ -f "$debugStarterScript" ]; then
    echo "debug starter script not found: $debugStarterScript"
    echo "Maybe release is not created. Try to create a release at first and re-run the script"
    echo "gradle clean buildRelease"
    exit 1
fi

$debugStarterScript -g "multifiles=$scriptPos/../src/test/resources/templates/handling.txt" \
    -gs "$scriptPos/../src/test/resources/templates/handling_helper.groovy" \
    -m "$scriptPos/../src/test/resources/test_schemas/ds/incident.json" \
    -o "$scriptPos/../tmp/handling" \
    -gp "packageName=de.debug"


