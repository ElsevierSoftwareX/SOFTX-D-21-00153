#!/bin/bash

# SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
#
# SPDX-License-Identifier: CC0-1.0

#Simple script to invoke CSTNRunningTime class for testing CSTN DC check algorithm running time.

############################
# START parameter configuration
DIR="$( dirname "${BASH_SOURCE[0]}" )"
CSTN="$DIR/$( cd $DIR  && echo `ls CSTNU-*.jar`)"
# END parameter configuration

#############################
#Check Java version
# Changed code to remove the 'head -1' as per the suggestion in comment.
JAVA_MIN='1.8'
JAVA_MIN_1='11'
JAVA_VERSION_COMPLETE=`java -version 2>&1 | head -n 1 | cut -d\" -f 2`
JAVA_VERSION_MAJOR=`echo $JAVA_VERSION_COMPLETE | cut -d. -f 1`
if [[ $JAVA_VERSION_COMPLETE != $JAVA_MIN && $JAVA_VERSION_MAJOR < $JAVA_MIN_1 ]]
then
	echo -e "To run the current tool, it is necessary to have Java >= $JAVA_MIN.\nIt is better Java >= $JAVA_MIN_1."
	echo "Your current Java version is $JAVA_VERSION_COMPLETE."
	exit 1
fi

IN=$@
echo "Start checking CSTNs contained in the following files $IN."

if [ "x$IN" == "x" ]; then
    echo "usage $0 [options] <CSTN file1> <CSTN file2>... <CSTN fileN> "
    exit 1
fi

java -cp $CSTN \
	-Djava.util.logging.config.file=$DIR/logging.properties \
	-d64 \
    -Xms5g \
    -Xmx5g \
    -XX:NewSize=3G \
    -XX:MaxNewSize=3G \
    -XX:+UseG1GC \
    -Xnoclassgc \
    -XX:+AggressiveOpts \
    it.univr.di.cstnu.algorithms.Checker -type cstn $IN 
