#!/bin/bash

# SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
#
# SPDX-License-Identifier: CC0-1.0

#Simple script to invoke CSTNURunningTime class for testing CSTNU DC check algorithm running time.
#

############################
# START parameter configuration
DIR="$( dirname "${BASH_SOURCE[0]}" )"
CSTN="$DIR/$( cd $DIR  && echo `ls CSTNU-*.jar`)"
# END parameter configuration

#############################
#Check Java version
# Changed code to remove the 'head -1' as per the suggestion in comment.
JAVA_VERSION=`java -version 2>&1 | head -n 1 | cut -d\" -f 2`
JAVA_WANTED='1.8'
if [[ $JAVA_VERSION != $JAVA_WANTED* ]]
then
	echo "To run the current tool, it is necessary to consider Java $JAVA_WANTED at least".
	echo "Your current Java version is $JAVA_VERSION".
	exit 1
fi

IN=$@
echo "Start checking CSTNUs contained in the following files $IN."

if [ "x$IN" == "x" ]; then
    echo "usage $0 [options] <CSTNU file1> <CSTNU file2>... <CSTNU fileN> "
    exit 1
fi

java -cp $CSTN \
	-Djava.util.logging.config.file=$DIR/logging.properties \
	-d64 \
	-Xmx6g \
    -Xms6g \
    -XX:NewSize=3G \
    -XX:MaxNewSize=3G \
    -XX:+UseG1GC \
    -Xnoclassgc \
    -XX:+AggressiveOpts \
    it.univr.di.cstnu.algorithms.Checker -type cstnu $IN
