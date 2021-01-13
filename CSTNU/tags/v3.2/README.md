# CSTNU Tool Project

## About
CSTNU Tool is a open-source software that offers an editor and some checking algorithms for analysing Conditional Simple Temporal Network with Uncertainty (CSTNU) and companion.

## Documentation 
The main web site is https://profs.scienze.univr.it/~posenato/software/cstnu/

The archived copy of the repository is at [![SWH](https://archive.softwareheritage.org/badge/swh:1:dir:154964a39f8f90a8f4242e021a5b111d6b2d251c/)](https://archive.softwareheritage.org/swh:1:dir:154964a39f8f90a8f4242e021a5b111d6b2d251c;origin=https://profs.scienze.univr.it/posenato/svn/sw/CSTNU;visit=swh:1:snp:5312997de5fcd230e7f304f0949e8007b4a76712;anchor=swh:1:rev:354b2e837b220e0449138843f6dcd9247bb92f71/)

## Support
Author and contact: Roberto Posenato <roberto.posenato@univr.it>

## Requirements
The tool is written in Java 8. No support for Java > 8.

One feature of the tool allows one to translate a CSTNU instance into an equivalent Timed Game Automaton. Such a feature requires to use the `uppaal-tiga-0.18` library that must (http://people.cs.aau.dk/~adavid/tiga/download.html).

## Installation
Please, refer to `README` file in `CstnuTool` subdirectory

## Directory summary
* `CstnuTool`
Contains some CSTNU instances, a link to uppaal-tiga, the CSTNU.jar and some shell scripts to start the editor, make some bunck of analysis and work with CSTNU-->TIGA translator
* `LICENSES`  
The licenses source files used in this project.
* `src` 
Contains all the source files of the project and the source file of the distribution site.
Moreover, in the main directory there are the following files:
* `README.md` 
This file
* `dependency-reduced-pom.xml`
* `pom.xml`
Maven configuration files.

## To use maven
* `mvn clean`   //Fundamental!!! Before doing anything regarding site
* `mvn compile` //for compiling
* `mvn test`    //for executing JUNIT4 test
* `mvn package` //for preparing the jar package	

## Licenses
For full licenses text, please check files in `LICENSES` subdirectory.
For source files the licenses (in SPDX term) is
```java
// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later
```

For other file is
```java
// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: CC0-1.0
```