# INSTANCES README

In this directory there are the following files representing CSTN/CSTNU instances:

## 4Alt.cstnu
Instance representing a process with 5 tasks: 2 alternative tasks (after observation time-point A?) followed by one task and, then, by other two alternative tasks 
(after observation time point B?).

The network is dynamic controllable (DC). 

4Alt.cstnu.tga.q and 4Alt.cstnu.tga.xml represent the translation of the network in TIGA format.

## 4AlternativeWFpaths.cstn
It similar to 4Alt.cstnu but where tasks are without uncertainty.
THe network has 18 nodes, 44 edges, 2 propositions, edges weights are in [-22,34]. 	
The network is dynamic consistent (DC).

4AlternativeWFpaths.cstn.tga.q 4AlternativeWFpaths.cstn.tga.xml represent the translation of the network in TIGA format.

## ex1C.cstnu
Simple CSTNU representing a process with two nested alternatives and three tasks.
The network is dynamic controllable (DC). 

ex1C.cstnu.tga.q and ex1C.cstnu.tga.xml represent the translation of the network in TIGA format.

## ex2C.cstn
Simple CSTN in which there are only one observation time-point ('A?') and three other time-points: 

- 'n1' is in scenario '¬a', 
- 'n2' in scenario 'a', and 
- 'n3' always present as 'A?'. 
The network is dynamic consistent (DC). 

ex2C.cstn.tga.q and ex2C.cstn.tga.xml represent the translation of the network in TIGA format.

## ex2NC.cstn
It is like ex2C.cstn instance but made INCONSISTENT setting the overall constraint between 'A?' and 'n3' more stringent.
The network is NOT dynamic consistent (DC). 

## fig2Paper.cstnu
Another simple CSTNU representing a process with two nested alternatives and 3 tasks.
The network is dynamic controllable (DC). 

fig2Paper.cstnu.tga.q and fig2Paper.cstnu.tga.xml represent the translation of the network in TIGA format.

## fig2paper.cstnu2cstn.cstn
It is `fig2Paper.cstnu` converted in an equivalente (from the point of view od dynamic controllability) to a CSTN.
