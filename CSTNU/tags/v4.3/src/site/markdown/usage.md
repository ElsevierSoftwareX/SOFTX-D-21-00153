## Usage:

1. Uncompress the archive `CstnuTool-<release>.tgz` 
  
2. In the directory `CstnuTool-<release>` there is the jar archive `CSTNU-<version>.jar` containing all Java classes for manipulating CSTN/CSTNU instances.   
  Please, refer to the file `RELEASE_NOTES.md` in this directory and to the Javadoc in this site for more details. 
  
3. Moreover, in the same directory `cstnuTool-<release>` there are the following shell commands:
	1. `cstnuEditor.sh`  
	A prototype CSTNU editor. It allows one to create/modify a CSTNU instance or to load one (GraphML format), and to
	check it. The resulting instance is shown near to the original one.

	2. `cstnuTranslator.sh`  
	It calls a Java program to translate a CSTNU to the corresponding TIGA.  
	The source file in the GraphML representation of a CSTNU.  
	The output is the XML description of the corresponding TIGA.  
	You have to install  `uppaal-tiga-0.18` software to run `cstnuTranslator.sh`  
	Please, adjust the location of `verifytga` program inside `cstnuTranslator.sh` according to the location where you installed `uppaal-tiga-0.18>`
	
	3. `CSTNRunningTime.sh`  
	This tool is intended for a subproject in which we are experimenting a new algorithm for checking the dynamic consistency of CSTN (network in which 
	uncertainty is not present).  
	In particular, this tool determines the average execution time of Dynamic Consistency Check Algorithm given a set of CSTN instances.
	
	4. `CSTNURunningTime.sh`
	This tool is intended for a subproject in which we are evaluating the algorithm for checking the dynamic consistency of CSTNU considering bulks of instances.  
	In particular, this tool determines the average execution time of Dynamic Consistency Check Algorithm given a set of CSTNU instances.