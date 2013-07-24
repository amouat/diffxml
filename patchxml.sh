#!/bin/bash
# This script will set up the java classpath with the required libraries
# then call patchxml with the given arguments

#First find out where we are relative to the user dir
callPath=${0%/*}

if [[ -n "${callPath}" ]]; then
    callPath=${callPath}/
fi
java -cp ${callPath}build:${callPath}lib/diffxml.jar org.diffxml.patchxml.PatchXML "$@"
