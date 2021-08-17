# CSTNU TOOLS README

In this directory there are the following shell commands:

## `cstnuTranslator.sh`
It calls a Java program to translate a CSTNU to the corresponding TIGA.
The source file in the GraphML representation of a CSTNU.
The output is the xml description of the corresponding TIGA.

You have to install uppaal-tiga-0.18 software in order to run cstnuTranslator.sh
Please, adjust the location of 'verifytga' program inside cstnuTranslator.sh according to the location where you installed 'uppaal-tiga-0.18'.

## `tnEditor.sh`
The temporal constraint network GUI editor.

## `CSTNRunningTime.sh`
This tool is intended for a subproject in which we are experimenting a new algorithm for checking the dynamic consistency of CSTN
(network in which uncertainty is not present).
In particular, this tool determines the average execution time of Dynamic Consistency Check Algorithm given a set of CSTN instances.

## `CSTNURunningTime.sh`
This tool is intended for a subproject in which we are experimenting a new algorithm for checking the dynamic consistency of CSTNU.
In particular, this tool determines the average execution time of Dynamic Consistency Check Algorithm given a set of CSTNU instances.