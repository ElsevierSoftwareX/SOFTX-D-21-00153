# CSTNU Tool README

The CSTNU Tool library is a fat library that can be used alone. 
Therefore, it is sufficient add the library to the JRE classpath to use any class or method of the library
(let us assume that the current JAR is `CSTNU-Tool-4.2.jar`):

```bash
$ java -cp CSTNU-Tool-4.2.jar ...
```

It is required to have a JRE version >= 8. It is recommended to use JRE 11 or JRE 17.

All classes (`STN, STNU, CSTN*, CSTNU, CSTNPUS`) relative to the different kinds of temporal networks have the `main` method 
that allows the execution of the `dynamicCheck` method on a given input file.

### Example of execution of CSTN class

```bash 
$ java -cp CSTNU-Tool-4.2.jar it.univr.di.cstnu.algorithms.CSTN
it.univr.di.cstnu.algorithms.CSTN Version 7.0 - November, 07 2019
SPDX-License-Identifier: LGPL-3.0-or-later, Roberto Posenato.

Starting execution...
Checking finished!
The given CSTN is Dynamic consistent!
Details: The check is finished after 2 cycle(s).
The consistency check has determined that given network is consistent.
Some statistics:
Rule R0 has been applied 0 times.
Rule R3 has been applied 0 times.
Rule Labeled Propagation has been applied 3 times.
Potentials updated 0 times.
The global execution time has been 10918000 ns (~0.010918 s.)
```

The library contains also a graphical editor for editing temporal networks and checking them using the different algorithms present in the library.

To execute the GUI editor (class `TNEditor`) it is sufficient invoke the package 

```bash
$java -jar CSTNU-Tool-4.2.jar
```

The script `tnEditor.sh` is a `bash` script that finds the library in the directory, checks if the installed JRE version is compatible,
and runs the `TNEditor` class.

## Script `CSTNRunningTime.sh`

This `bash` tool is intended for a subproject in which we are experimenting a new algorithm for checking the dynamic consistency of CSTNs.
In particular, this tool determines the average execution time of Dynamic Consistency Check Algorithm given a set of CSTN instances.

## `CSTNURunningTime.sh`

This `bash` tool is intended for a subproject in which we are experimenting a new algorithm for checking the dynamic consistency of CSTNU.
In particular, this tool determines the average execution time of Dynamic Consistency Check Algorithm given a set of CSTNU instances.

## `cstnuTranslator.sh`
This `bash` tool executes a Java program of the library to translate a CSTNU instance to the corresponding TIGA instance.
The source file is the GraphML representation of a CSTNU graph.
The output is the XML description of the corresponding TIGA instance.

You have to install `uppaal-tiga-0.18` () software in order to run `cstnuTranslator.sh`.
Please, adjust the location of `verifytga` program inside `cstnuTranslator.sh` according to the location where you installed `uppaal-tiga-0.18`.
