#!/bin/bash
#
#	-Djava.util.logging.config.file=~/RandomTemplateSampler/conf/logging.properties \
java	-classpath CSTNU-1.** \
    -d64 \
    -XX:-UseAdaptiveSizePolicy \
    -XX:+UseConcMarkSweepGC \
    -Xmx6g \
    -XX:NewSize=1G \
    -XX:MaxNewSize=1G \
	it.univr.di.cstnu.algorithms.CSTNRunningTime -noUseΩ -numRepetitionDCCheck 1 -timeInS $*

