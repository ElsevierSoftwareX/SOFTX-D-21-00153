# RELEASE NOTES
<!-- START SNIPPET: rn -->
## v3.2
	date: 2021-01-14
	
	Fixed a coding error on saving network into file. From this release, the file encoding is UTF8 independently of the execution platform.
	Fixed an initialization error in CSTNU class.
	Fixed almost all Javadoc errors.

## v3.1
	date: 2020-12-28
	
	Some cleaning actions.
	Added copyright and licenses for publication on archive.softwareheritage.org

## v3.0
	date: 2020-11-15
	
	STNU class added with different DC checking algorithms: Morris2016, RUL2018, and RUL2020 faster one.
	CSTNPSU class offers a DC check sound-and-complete that can also adjust guarded links to the right ranges for an execution.
	Fixed some minor bugs.

## v2.11
	date: 2020-02-02
	
	Graphical interface simplified. 
	Now it is more intuitive to add/remove nodes/edges.
	CSTNPSU class offers a DC check sound-and-complete.

## v2.10
	date: 2020-02-02
	
	CSTN class restored to a previous algorithms.
	Removed the possibility to check without using unknown literals.
	This version considers the new rule and algorithm names published in a work presented at ICAPS 2020. 
	CSTN class implements two DC checking algorithms. With option --limitedToZ, the algorithm is the one presented at IJCAI18 (algorithm HR_18).
	Without --limitedToZ, the algorithm is the one presented at ICAPS19 (algorithm HR_19). This last version is not efficient as IJCAI18 one.
	For a very efficient version, consider CSTNPotential class (reintroduced but in a new form) that makes DC checking assuming IR semantics and node 
	without labels (algorithm HR_20). 
	CSTNPotential class implements the new DC checking algorithm based on single-sink shortest paths and potential R0 and potential R3 rules.
	For all objects containing a labeled values field, the default implementation class is LabeledIntTreeMap. For changing this default class,
	modify the field DEFAULT_LABELEDINTMAP_CLASS of LabeledIntMapSupplier and recompile sources.
	CSTNPSU class allows the representation and the verification of temporal constraints containing guarded links. The DC checking algorithm is sound but not complete.
	Moreover, the guarded link bounds are not guaranteed to be shrunk in the right way.

## v2.00
	date: 2019-11-07
	
	CSTN class implements 9Rule DC algorithm. Such algorithm consists in 3 rules (LP, R0, and R3*) for generating also -∞ value and 6 rules for
	managing -∞ values that are stored as potential values in nodes.
	Added CSTNSPFA class that implements the DC checking algorithm as single-sink BellmanFord one. Therefore, only node potential values are generated.
	This class uses only 3 rules.    	

## v1.26.vassar
	date: 2019-10-21
	
	Rewritten many classes for representing efficiently edges and graphs for STN networks. 
	Removed class CSTNPotential (last svn-version 296).
	Added class STN & companions for some STN specific algorithms.

## v1.26.0
	date:	2019-03-23
	
	CSTNU class has a new field, contingentAlsoAsOrdinary, default false.
	When it is true, the DC checking method propagates contingent links also as ordinary constraints. 
	This allows one to see some ordinary values more accurate.

## v1.25.0
	date:	2018-11-22
	
	Minor code optimization and minor bug fixes.
	From this release it is necessary to run the software using JRE 1.8.

## v1.24.0
	date:	2018-07-18
	
	CSTN class allows the DC checking without using unknown literals (that are necessary for the completeness): it just a tool for studying the necessity of unknown literals.
	Literal and Label class are now immutable.
	
	Minor code optimization.

## v1.23.2
	date:	2018-02-21
	
	Class CSTNU cleaned and optimized. From this release, its DC checking algorithm is sound-and-complete.
	Contingent link can have label even if it is not required for sound-and-completeness.
	
	Class CSTNPSU.java added.
	Sub-package 'attic' removed. (last svn revision 243).
	
	SVN version: 245.

## v1.22.4
	date:	2018-01-10
	
	Code cleaning

## v1.22.3
	date:	2017-12-14
	
	Class CSTNU optimized.
	Class CSTNURunningTime removed.
	Class CSTNRunningTime was renamed Checker.

## v1.22.2
	date:	2017-11-24
	
	Code optimization

## v1.22.1
	date:	2017-11-22
	
	The following classes have been renamed re-ordering inside terms:
	1. CSTNEpsilon.java
	2. CSTNEpsilon3R.java
	3. CSTNEpsilon3RwoNodeLabels.java
	4. CSTNEpsilonwoNodeLabels.java
	5. CSTNIR.java
	6. CSTNIR3R.java
	7. CSTNIR3RwoNodeLabels.java
	8. CSTNIRwoNodeLabels.java

## v1.22.0
	date:	2017-11-21
	
	Introduced new classes and renamed old ones.
	There are 13 classes for checking CSTN/CSTNU:
	1. it.univr.di.algorithms.CSTN.java
	2. it.univr.di.algorithms.CSTN2CSTN0.java
	3. it.univr.di.algorithms.CSTN3RIR.java: as IR CSTN but DC checking uses only three rules.
	4. it.univr.di.algorithms.CSTN3RwoNodeLabelEpsilon.java
	5. it.univr.di.algorithms.CSTN3RwoNodeLabelIR.java
	6. it.univr.di.algorithms.CSTNEpsilon.java
	7. it.univr.di.algorithms.CSTNIR.java
	8. it.univr.di.algorithms.CSTNU.java
	9. it.univr.di.algorithms.CSTNU2CSTN.java
	10.it.univr.di.algorithms.CSTNU2UppaalTiga.java
	11.it.univr.di.algorithms.CSTNwoNodeLabel.java
	12.it.univr.di.algorithms.CSTNwoNodeLabelEpsilon.java
	13.it.univr.di.algorithms.CSTNwoNodeLabelIR.java
	Replaced Ω node with equivalent constraints in all CSTN classes.
	Removed Ω node from LabeledIntGraph and relative reader/writer.
	Improved CSTN Layout for laying out also nodes without explicit temporal relation with Z.
	Started classes re-factoring exploiting Java 8 new features.
	CSTNRunningTime and CSTNURunningTime made multi-thread.

## v1.21.0
	date:	2017-11-09
	
	CSTNUGraphMLReader is able to read CSTN files that do not contain meta information about UC and LC values.
	CSTNirRestricted renamed CSTNir3R.
	CSTNwoNodeLabel cleaned.
	Added classes CSTNirwoNodeLabel and CSTNir3RwoNodeLabel.
	Simplified CSTNRunningTime.

## v1.20.0
	date:	2017-11-03
	
	Jung library has been upgraded to 2.1.1 version.
	Such update required an adaptation of all GUI classes of the package.
	CSTNU DC checking algorithm is now sound-and-complete.
	CSTNEditor allows now to view a graph in a bigger window and to save it as PDF or other graphical format. The export menu is accessible clicking the mouse
	inside the window containing the graph to export.
	There are now 6 classes for checking CSTN/CSTNU:
	1. it.univr.di.algorithms.CSTN: it checks a CSTN instance assuming the standard semantics.
	2. it.univr.di.algorithms.CSTNepsilon: it checks a CSTN instance assuming an not-zero reaction time (epsilon).
	3. it.univr.di.algorithms.CSTNir: it checks a CSTN instance assuming instantaneous reactions.
	4. it.univr.di.algorithms.CSTNirRestricted: as it.univr.di.algorithms.CSTNir but the used rules are 3 instead of 6. Such checking can be faster than it.univr.di.algorithms.CSTNir one.
	5. it.univr.di.algorithms.CSTNU: it checks a CSTNU instance assuming instantaneous reactions.
	6. it.univr.di.algorithms.CSTNwoNodeLabel: it checks a CSTN instance assuming the standard semantics. The instance is translate into an equivalent CSTN instance without node labels and, then, checked.
	GraphMLReader has been rewritten because based on Jung GraphMLReader2 that is not able to manage big attribute data.
	GraphMLReader has been renamed CSTNUGraphMLReader.
	GraphMLWriter has been renamed CSTNUGraphMLWriter.   

## v1.10.0
	date:	2017-06-22
	
	Package reorganization.
	CSTNEditor cleaned and optimized.

## v1.9.0
	date:	2016-03-31
	
	Labels are represented in a more compact way.
	Labeled value sets requires less memory.
	It is still possible to choose which kind of representation to use for labeled value sets.

## v1.8.0
	date:	2016-02-24
	
	Graphs are represented by a new optimized class.

## v1.7.9
	date:	2015-11-26
	
	Added the new feature to CSTN DC-checking. Now DC-checking algorithm considers a user specified 'reaction time' ε (ε>0) of the system during the checking of 
	a network. A reaction time ε means that an engine that executes a CSTN reacts in at least ε time units to a setting of a truth-value to a proposition.  

## v1.7.8
	date:	2015-10-28
	
	A new sanity check about the correctness of an input CSTNU added: each contingent time point has to have one incoming edge of type 'contingent' and one
	outgoing edge of type 'contingent' from/to the same node, representing the activation time point.

## v1.7.7
	date:	2015-10-22
	
	Fixed another issue with instantaneous reaction in CSTNU. Thanks to Dian Liu for his help in discovering such error.
	I repeat that, even in this release, it is better to set any timepoint---that depends from a observation timepoint or follows a contingent timepoint---to 
	a not 0 distance from the considered observation timepoint or contingent one.

## v1.7.6
	date:	2015-09-30
	
	Fixed a subtle bug in the generation of new edges. Thanks to Dian Liu for his help in discovering such error.

## v1.7.5
	date:	2015-09-23
	
	Cleaned some log messages. 
	We have discovered that instantaneous reaction feature requires a sharp adjustment in semantics of the network.
	We are currently working in the introduction of an ε-reaction (with ε ≥ 0) feature. 
	In the mean time, for CSTN it is possible to DC check assuming instantaneous reaction (ε = 0) 
	while for CSTNU it is only possible to DC check assuming non-instantaneous reaction (ε > 0).
	
	For now, it is better to set any timepoint---that depends from a observation timepoint or follows a contingent timepoint---to a not 0 distance from the 
	considered observation timepoint or contingent one.

## v1.7.4
	date:	2015-09-13
	
	In this release, a more strict check of contingent links has been introduced.
	Bounds of a contingent link A==[x,y]==>B must observe the property 0<x<y<∞. 
	Moreover, since for CSTNU instances it is not yet defined the concept of 'instantaneous reactions', it is not possible to define
	a CSTNU instance in which there exists a constraint like C--[0,0]-->B where B is contingent.
	This constraint requires that C has to be executed in at the same time of the contingent time point B, but this is not possible 
	because B is decided by the environment and the runtime engine has to observe it before to execute standard node like C.

## v1.7.3
	date:	2015-09-10
	
	This release contains some minor bug fixes and a revision of public methods of class it.univr.di.cstnu.algorithms.CSTNU.
	Now, it is possible to check the controllability of a CSTN instance instantiating a CSTNU object and calling its
	dynamicControllabilityCheck(LabeledIntGraph) method.
	It is sufficient to create a LabeledIntGraph object representing a given CSTNU instance (in LabeledIntGraph class there is also a method for loading 
	an instance from a file written in GraphML format), instantiate a CSTNU object and call its method dynamicControllabilityCheck(LabeledIntGraph) passing 
	the created graph.
	
	Moreover, I add the class it.univr.di.cstnu.algorithms.CSTNURunningTime and the script CSTNURunningTime for checking a bundle of CSTNU instances obtaining also 
	some execution time statistics.

## v1.7.2
	date:	2015-06-24
	
	This release contains some minor bug fixes and a README file explaining the proposed examples of CSTNU/CSTN instances.
	Thanks to Huan Wang for his comments.

## v1.7.1
	date:	2015-05-28
	
	This release contains a faster Dynamic Consistency check for CSTNs.
	The java class <<<CSTN.java>>> has been completely rewritten and almost optimized for a faster check.

## v1.7.0
	date:	2015-03-23
	
	This release is the first public release.

<!-- END SNIPPET: rn -->
