#!/bin/bash
#Simple script to invoke CSTN2UppaalTiga translator and to verify if the original CSTNU is controllable
#
# 2014 (C) Roberto Posenato
#

############################
# START parameter configuration
# Change this variable to point to 'verifytga' executable!
DIR="$( dirname "${BASH_SOURCE[0]}" )"
CSTNU="$DIR/$( cd $DIR  && echo `ls CSTNU-*.jar`)"
TIGAPATH="$DIR/uppaal-tiga-0.18/bin-Linux/verifytga"
# END parameter configuration
#############################
#Check Java version
# Changed code to remove the 'head -1' as per the suggestion in comment.
JAVA_VERSION=`java -version 2>&1 | head -n 1 | cut -d\" -f 2`
JAVA_WANTED='1.8'
if [[ $JAVA_VERSION != $JAVA_WANTED* ]]
then
	echo "To run the current tool, it is necessary to consider Java $JAVA_WANTED at least"
	echo "Your current Java version is $JAVA_VERSION"
	exit 1
fi

IN=$1

#TIGA 0.17 has a parameter change
TIGA="$TIGAPATH -s -w0"

if [ "x$IN" == "x" ]; then
    echo "usage $0 <CSTNU file>"
    exit 1
fi

STRAT="$1.tga.strategy"
MODEL="$1.tga.xml"
QUERY="$1.tga.q"

echo "Generating automaton"
echo "java -cp $CSTNU it.univr.di.cstnu.algorithms.CSTNU2UppaalTiga -o $MODEL $IN"
time java -cp $CSTNU it.univr.di.cstnu.algorithms.CSTNU2UppaalTiga -o $MODEL $IN 

echo "Model saved into file $MODEL"
echo "Query saved into file $QUERY"
echo "Running TIGA"
time $TIGA $MODEL $QUERY > $STRAT

if grep -q "Property is NOT satisfied" $STRAT ; then
    echo "The CSTNU is dynamically controllable!"
else
    echo "The CSTNU is NOT dynamically controllable!"
fi
