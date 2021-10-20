# CSTNU Tool Project

## About
CSTNU Tool is a open-source software that offers an editor and some checking algorithms for analysing Conditional Simple Temporal Network with Uncertainty (CSTNU) 
and companion.
In particular, the checking algorithms verifies if a temporal network is consistent or dynamic controllable according to the kind of the network.
The implemented algorithms are:

### For Simple Temporal Networks (STN)
- AllPairsShortestPaths (Floyd-Warshall)
- Bannister Eppstein,
- Bellman-Ford
- Bellman Ford for a Single Sink
- Dijkstra,
- Johnson,
- Yen,
- Yen for a Single Sink
- BFCT (Tarjan with Subtree disassebly)

### For Conditional Simple Temporal Networks (CSTN)
- Hunsberger-Posenato 2018
- Hunsberger-Posenato 2019
- Hunsberger-Posenato 2020

### For Simple Temporal Networks with Uncertainty (STNU)
- Morris 2014
- RUL^-
- RUL 2020
- Bellman-Ford for STNU

### For Conditional Simple Temporal Networks with Uncertainty (CSTNU)
- Translation to quivalent Time Game Automaton
- Translation to equivalent CSTN
- Hunsberger-Posenato 2018

### For Conditional Simple Temporal Networks with Partially Shrinkable Uncertainty (CSTNPSU) or Flexible Simple Temporal Networks with Uncertainty (FTNU)
- Posenato & c. 2020

## Documentation

The main web site is https://profs.scienze.univr.it/~posenato/software/cstnu/
The main source repository is at https://profs.scienze.univr.it/posenato/svn/sw/CSTNU
The archived copy of the source repository is at https://archive.softwareheritage.org/browse/origin/?origin_url=https://profs.scienze.univr.it/posenato/svn/sw/CSTNU
In `Docs` directory present in the source repository there are some technical documents about the project.

## Requirements

Java &ge; 11

One feature of the tool allows one to translate a CSTNU instance into an equivalent Timed Game Automaton. 
Such a feature requires to use the `uppaal-tiga-0.18` library that must downloaded at [http://people.cs.aau.dk/~adavid/tiga/download.html](http://people.cs.aau.dk/~adavid/tiga/download.html).

## Installation

The CSTNU Tool library is a fat library that can be used alone.
It is present in the main directory of the binary distribution archive as JAR package with name `CSTNU-Tool-X.Y.jar`, where `X.Y` are two integers representing the version of the package.
(If the JAR package is built from the sources, it will be present in the `CstnuTool` sub directory).

It is sufficient to add the library to the JRE classpath for using any class or method of the library
(let us assume that the current JAR is `CSTNU-Tool-4.2.jar`):

```bash
$ java -cp CSTNU-Tool-4.2.jar ...
```

The Jar package is distributed with all debugging code removed. In case that it is necessary to have debugging messages, please refer to `BUILDING.md` document in the source repository where it is explained how to build and use a Jar with debugging messages.

### Usage

The main goal of CSTNU Tool library is to be a support library for representing and checking temporal constraint networks
inside other software.

The directory `Instances` constains some CSTN and CSTNU instances.

As side feature, all classes (`STN, STNU, CSTN*, CSTNU, CSTNPUS`) relative to the different kinds of temporal networks have the `main` method that allows the execution of the `dynamicCheck` method on a given input file directly.

#### Example of execution of CSTN class

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

Input file must be in GraphML format.
See `Docs/graphFileFormat.md` for details about the accepted GraphML format.


### GUI Editor
The library contains also a graphical editor for editing temporal networks and checking them using the different algorithms present in the library.

To execute the GUI editor (class `TNEditor`) it is sufficient invoke the package 

```bash
$java -jar CSTNU-Tool-4.2.jar
```

The script `tnEditor.sh` is a `bash` script that finds the library in the directory, checks if the installed JRE version is compatible,
and runs the `TNEditor` class.

### Script `CSTNRunningTime.sh`

This `bash` tool is intended for a subproject in which we are experimenting a new algorithm for checking the dynamic consistency of CSTNs.
In particular, this tool determines the average execution time of Dynamic Consistency Check Algorithm given a set of CSTN instances.

### Script `CSTNURunningTime.sh`

This `bash` tool is intended for a subproject in which we are experimenting a new algorithm for checking the dynamic consistency of CSTNU.
In particular, this tool determines the average execution time of Dynamic Consistency Check Algorithm given a set of CSTNU instances.

### Script`CSTNUTranslator.sh`

This `bash` tool executes a Java program of the library to translate a CSTNU instance to the corresponding TIGA instance.
The source file is the GraphML representation of a CSTNU graph.
The output is the XML description of the corresponding TIGA instance.

You have to install `uppaal-tiga-0.18` () software in order to run `cstnuTranslator.sh`.
Please, adjust the location of `verifytga` program inside `cstnuTranslator.sh` according to the location where you installed `uppaal-tiga-0.18`.


## Licenses
For full licenses text, please check files in `LICENSES` subdirectory.
For source files the licenses (in SPDX term) is
```java
// SPDX-FileCopyrightText: 2021 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later
```

For other file is
```java
// SPDX-FileCopyrightText: 2021 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: CC0-1.0
```

## Support
Author and contact: Roberto Posenato <roberto.posenato@univr.it>
