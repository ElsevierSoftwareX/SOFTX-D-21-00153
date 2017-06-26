#!/bin/bash
#Simple script to invoke CSTNURunningTime class for testing CSTNU DC check algorithm running time.
#
# 2015 (C) Roberto Posenato
#

############################
# START parameter configuration
DIR="$( dirname "${BASH_SOURCE[0]}" )"
CSTNU="$DIR/$( cd $DIR  && echo `ls CSTNU-*.jar`)"
# END parameter configuration

#############################
#Check Java version
# Changed code to remove the 'head -1' as per the suggestion in comment.
JAVA_VERSION=`java -version 2>&1 | head -n 1 | cut -d\" -f 2`
JAVA_WANTED='1.7'
if [[ "$JAVA_VERSION" < "$JAVA_WANTED" ]]
then
	echo "To run the current tool, it is necessary to consider Java $JAVA_WANTED at least".
	echo "Your current Java version is $JAVA_VERSION".
	exit 1
fi

IN=$@
echo "Start checking CSTNUs contained in the following files $IN."

if [ "x$IN" == "x" ]; then
    echo "usage $0 [-excludeR1andR2rules] [-NOoptimized] <CSTNU file1> <CSTNU file2>... <CSTNU fileN> "
    exit 1
fi

java -Djava.util.logging.config.file=$DIR/logging.properties -Xmx1g -cp $CSTNU it.univr.di.cstnu.algorithms.CSTNURunningTime $IN 
