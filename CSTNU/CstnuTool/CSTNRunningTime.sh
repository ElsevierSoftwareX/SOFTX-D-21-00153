#!/bin/bash
#Simple script to invoke CSTNRunningTime class for testing CSTN DC check algorithm running time.
#
# 2015 (C) Roberto Posenato
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
JAVA_WANTED='1.7'
if [[ "$JAVA_VERSION" < "$JAVA_WANTED" ]]
then
	echo "To run the current tool, it is necessary to consider Java $JAVA_WANTED at least".
	echo "Your current Java version is $JAVA_VERSION".
	exit 1
fi

IN=$@
echo "Start checking CSTNs contained in the following files $IN."

if [ "x$IN" == "x" ]; then
    echo "usage $0 [-excludeR1andR2rules] [-NOoptimized] <CSTN file1> <CSTN file2>... <CSTN fileN> "
    exit 1
fi

java -Djava.util.logging.config.file=$DIR/logging.properties -Xmx1g -cp $CSTN it.univr.di.cstnu.CSTNRunningTime $IN 
