#!/bin/bash

# SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
#
# SPDX-License-Identifier: CC0-1.0

#Simple script to invoke CSTN Editor
#
############################
# START parameter configuration
DIR="$( dirname "${BASH_SOURCE[0]}" )"
CSTNU="$DIR/$( cd $DIR  && echo `ls CSTNU-Tool-*.jar`)"
# END parameter configuration

#############################
#Check Java version
# Changed code to remove the 'head -1' as per the suggestion in comment.
JAVA_VERSION=`java -version 2>&1 | head -n 1 | cut -d\" -f 2`
JAVA_WANTED='1.8'
if [[ $JAVA_VERSION < $JAVA_WANTED* ]]
then
    echo "To run the current tool, it is necessary to have Java $JAVA_WANTED".
    echo "Your current Java version is $JAVA_VERSION".
    exit 1
fi

java -cp $CSTNU it.univr.di.cstnu.visualization.TNEditor
