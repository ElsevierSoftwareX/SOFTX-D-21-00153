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
The archived copy of the repository is at https://archive.softwareheritage.org/browse/origin/?origin_url=https://profs.scienze.univr.it/posenato/svn/sw/CSTNU

## Support
Author and contact: Roberto Posenato <roberto.posenato@univr.it>

## Requirements

Java &ge; 11

One feature of the tool allows one to translate a CSTNU instance into an equivalent Timed Game Automaton. 
Such a feature requires to use the `uppaal-tiga-0.18` library that must downloaded at [http://people.cs.aau.dk/~adavid/tiga/download.html](http://people.cs.aau.dk/~adavid/tiga/download.html).

## Installation

Please, refer to `README.md` file in `CstnuTool` subdirectory

## Directory summary
* `CstnuTool`
  Contains some CSTNU instances (**Instances** subdirectory), the CSTNU-Tool-\*.\*.jar, and some shell scripts for 
  1) starting the graphical editor, 2) making some bunch of analysis, and 3) executing CSTNU-->TIGA translator.
* `LICENSES`  
The licenses source files used in this project.

* `src` 
Contains all the source files of the project and the source file of the distribution site.
Moreover, in the main directory there are the following files:

* `README.md` 
This file

* `dependency-reduced-pom.xml`

  Maven configuration files.

* `pom.xml`
Maven configuration files.

## To use maven
* `mvn clean`   //Fundamental!!! Before doing anything regarding site
* `mvn compile` //for compiling
* `mvn test`    //for executing JUNIT4 test
* `mvn package` //for building the jar package `CSTNU-Tool-*.*.jar,`
* `mvn package -Ddebug` //for bulding the jar package with all debug messages

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