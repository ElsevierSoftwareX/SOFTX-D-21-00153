## Welcome to Conditional Simple Temporal Network with Uncertainties Tool Project

### Introduction

Constraint-based temporal reasoning has been widely used in many different applications across many different domains. 
Over the years, different formalisms have been presented to address specific requirements that frequently arise in real-world applications. 
The most commonly used formalism is probably **the Simple Temporal Network (STN)**, in which a set of real-valued variables, called time-points, are subject 
to binary difference constraints [\[1\]](#c1).

Recently, a significant amount of research has focused on temporal reasoning in the presence of uncertainty.    
Temporal uncertainty arises, for example, when the duration of some activities (i.e., the duration of some temporal intervals) are not controlled by 
the executor (or agent), but instead are only observed in real-time as the activities complete.  
In such settings, the executor seeks a dynamic *strategy* for executing the controllable time-points such that all relevant constraints will necessarily 
be satisfied no matter how the uncertain durations turn out.
To accommodate this kind of uncertainty, STNs have been augmented to include *contingent links*, where each contingent link represents an interval whose
duration is bounded but uncontrollable; the resulting network is called a **Simple Temporal Network with Uncertainty (STNU)** [\[2\]](#c2).  
The most important property of an STNU is whether it is *dynamically controllable (DC)*---that is, whether there exists a strategy for executing 
the controllable time-points such that all relevant constraints are guaranteed to be satisfied no matter how the durations of the contingent links turn out.  

Although STNUs have been successful in some domains, many domains require a richer set of constraints.  
For example, in the health-care domain, where workflow management systems are being developed to automate medical-treatment processes, 
medical tests for any given patient frequently generate information in real-time that can affect which pathway that patient will follow [\[3\]](#c3).    
The system must guarantee that any possible execution of the workflow strictly satisfies all specified temporal constraints no matter which test outcomes are observed.
A set of test outcomes is called *scenario*.

The **Conditional Simple Temporal Network (CSTN)** model allows the representation of temporal constraints in conjunction with scenarios. 
In this way, it is possible to specify, for each possible scenario, a proper set of constraints that must be satisfied if the scenario occurs in an execution.
For CSTNs, different works determined many results and algorithms for managing CSTN properly [\[8\]](#c8), [\[9\]](#c9), [\[11\]](#c11), [\[12\]](#c12), [\[13\]](#c13), [\[15\]](#c15)
[\[16\]](#c16).
 
The **Conditional Simple Temporal Network with Uncertainty (CSTNU)** model extends the CSTN one allowing the representation of contingent links. 
Different works determined properties and possible applications of CSTNUs [\[5\]](#c5), [\[6\]](#c6), [\[7\]](#c7), [\[10\]](#c10), [\[14\]](#c14).

   **The goal of this project is to publish a prototype allowing one to design and check the controllability of a CSTN(U) by a graphical editor.**

### Benchmarks

The directory containing benchmarks used in papers [\[9\]](#c9)
[\[10\]](#c10)
[\[11\]](#c11)
[\[12\]](#c12)
[\[13\]](#c13)
[\[14\]](#c14)
[\[15\]](#c15)
[\[16\]](#c16).
is https://profs.scienze.univr.it/~posenato/software/benchmarks/ .
	
### Bibliography

<a name="c1">[1]</a> R. Dechter, I. Meiri, and J. Pearl, “Temporal constraint networks,” Artificial Intelligence, vol. 49, pp. 61–95, 1991.

<a name="c2">[2]</a> P. Morris, N. Muscettola, and T. Vidal, “Dynamic control of plans with temporal uncertainty,” in 17th International Joint Conference on Artificial Intelligence (IJCAI-01), 
	Morgan Kaufmann, 2001, pp. 494–499. 

<a name="c3">[3]</a> C. Combi, M. Gambini, S. Migliorini, and R. Posenato, “Representing business processes through a temporal data-centric workflow modeling language: 
  An application to the management of clinical pathways,” Systems, Man, and Cybernetics: Systems, IEEE Transactions on, 2014.
  Available online: https://ieeexplore.ieee.org/xpl/abstractReferences.jsp?arnumber=6733362

<a name="c4">[4]</a> L. Hunsberger, R. Posenato, and C. Combi, “The Dynamic Controllability of Conditional STNs with Uncertainty,” in Workshop on Planning and Plan Execution 
	for Real-World Systems: Principles and Practices (PlanEx) @ ICAPS-2012, Atibaia, Jun. 2012, pp. 1–8.
	Available online: https://arxiv.org/abs/1212.2005
	
<a name="c5">[5]</a> C. Combi, L. Hunsberger, and R. Posenato, “An algorithm for checking the dynamic controllability of a conditional simple temporal network with uncertainty,”
  	in Proc. of the 5th Int. Conf. on Agents and Art. Int. (ICAART-2013), vol. 2, pp. 144–156, SCITEPRESS, Feb. 2013
  	
<a name="c6">[6]</a> C. Combi, L. Hunsberger, and R. Posenato, “An algorithm for checking the dynamic controllability of a conditional simple temporal network with uncertainty
    - revisited,” in Agents and Artificial Intelligence, vol. 449 of Communications in Computer and Information Science, pp. 314–331,
     Springer-Verlag, 2014.
     Available online: https://doi.org/10.1007/978-3-662-44440-5_19
    
<a name="c7">[7]</a> A. Cimatti, L. Hunsberger, A. Micheli, R. Posenato, and M. Roveri, “Sound and complete algorithms for checking the dynamic controllability of temporal 
    networks with uncertainty, disjunction and observation,” in 21st International Symposium on Temporal Representation and Reasoning (TIME 2014), 
    pp. 27–36, IEEE Computer Society, Sept. 2014.
    Available online: https://doi.org/10.1109/TIME.2014.21
    
<a name="c8">[8]</a> L. Hunsberger, R. Posenato, and C. Combi, “A Sound-and-Complete Propagation-based Algorithm for Checking the Dynamic Consistency of Conditional Simple Temporal Networks,”
   in 22st International Symposium on Temporal Representation and Reasoning (TIME 2015), pp. 4–18, IEEE Computer Society, Sept. 2015.
   Available online: https://doi.org/10.1109/TIME.2015.26
   
<a name="c9">[9]</a> L. Hunsberger and R. Posenato, “Checking the Dynamic Consistency of Conditional Simple Temporal Networks with Bounded Reaction Times”, 
   in ICAPS 2016: International Conference on Automated Planning and Scheduling (ICAPS 2016), pp. 175–183, 2016.
   Available online: https://www.aaai.org/ocs/index.php/ICAPS/ICAPS16/paper/view/13108
   
<a name="c10">[10]</a> A. Cimatti, L. Hunsberger, A. Micheli, R. Posenato, and M. Roveri, ‘Dynamic controllability via Timed Game Automata’, 
    Acta Inform., vol. 53, no. 6–8, pp. 681–722, Oct. 2016.
	Available online: https://dx.doi.org/10.1007/s00236-016-0257-2
    
<a name="c11">[11]</a>  M. Cairo, L. Hunsberger, R. Posenato, and R. Rizzi, ‘A Streamlined Model of Conditional Simple Temporal Networks – Semantics and Equivalence Results’, 
    in 24th International Symposium on Temporal Representation and Reasoning (TIME 2017), 2017, vol. 90, no. 10, pp. 1–10.
    Available online: https://dx.doi.org/10.4230/LIPIcs.TIME.2017.10
    
<a name="c12">[12]</a>  M. Cairo, C. Combi, C. Comin, L. Hunsberger, R. Posenato, R. Rizzi, M. Zavatteri, ‘Incorporating Decision Nodes into Conditional Simple Temporal Networks’, 
    in 24th International Symposium on Temporal Representation and Reasoning (TIME 2017), 2017, vol. 90, p. 9:1--9:18.   
        Available online: https://dx.doi.org/10.4230/LIPIcs.TIME.2017.9
    
<a name="c13">[13]</a> L. Hunsberger and R. Posenato, ‘Simpler and Faster Algorithm for Checking the Dynamic Consistency of Conditional Simple Temporal Networks’, 
  	in Proceedings of the Twenty-Seventh International Joint Conference on Artificial Intelligence, 2018, pp. 1324–1330.
  		Available online: https://dx.doi.org/10.24963/ijcai.2018/184
  		    
<a name="c14">[14]</a> L. Hunsberger, R. Posenato, ‘Sound-and-Complete Algorithms for Checking the Dynamic Controllability of Conditional Simple Temporal Networks with Uncertainty’, 
    in 25th International Symposium on Temporal Representation and Reasoning (TIME 2018), 2018, vol. 120, no. 14, p. 1--17.   
        Available online: https://dx.doi.org/10.4230/LIPIcs.TIME.2018.14

<a name="c15">[15]</a> L. Hunsberger, R. Posenato, ‘Reducing ε-DC Checking for Conditional Simple Temporal Networks to DC Checking’, 
    in 25th International Symposium on Temporal Representation and Reasoning (TIME 2018), 2018, vol. 120, no. 15, pp. 1–15.
    Available online: https://dx.doi.org/10.4230/LIPIcs.TIME.2018.15
    
<a name="c16">[16]</a> L. Hunsberger and R. Posenato, “Faster Dynamic-Consistency Checking for Conditional Simple Temporal Networks”, 
   in ICAPS 2020: International Conference on Automated Planning and Scheduling (ICAPS 2020), 2020.

