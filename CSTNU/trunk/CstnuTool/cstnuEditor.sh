#!/bin/bash
#Simple script to invoke CSTN Editor
#
# 2014 (C) Roberto Posenato
#
CSTNU=`ls CSTNU-*.jar` 
#Check Java version
# Changed code to remove the 'head -1' as per the suggestion in comment.
JAVA_VERSION=`java -version 2>&1 | head -n 1 | cut -d\" -f 2`
JAVA_WANTED='1.7'
if [[ "$JAVA_VERSION" < "$JAVA_WANTED" ]]
then
	echo "To run the current tool, it is necessary to consider Java $JAVA_WANTED at least"
	echo "Your current Java version is $JAVA_VERSION"
	exit 1
fi

java -cp $CSTNU it.univr.di.cstnu.CSTNUEditor
