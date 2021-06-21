// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.algorithms;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;
import org.xml.sax.SAXException;

import com.google.common.io.Files;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.univr.di.cstnu.algorithms.Checker.RunMeter;
import it.univr.di.cstnu.algorithms.STNU.CheckAlgorithm;
import it.univr.di.cstnu.algorithms.STNU.STNUCheckStatus;
import it.univr.di.cstnu.graph.Edge;
import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.cstnu.graph.EdgeSupplier;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.STNUEdge;
import it.univr.di.cstnu.graph.STNUEdgeInt;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.cstnu.graph.TNGraphMLReader;
import it.univr.di.cstnu.graph.TNGraphMLWriter;
import it.univr.di.labeledvalue.Constants;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;

/**
 * Reads a CSTNU instance and makes it dense adding up to (n log n)/5 edges, where n is the number of nodes.
 * For now, it works only on instance made of lanes.
 *
 * @author posenato
 * @version $Id: $Id
 */
public class STNUDensifier {

	/**
	 * Version of the class
	 */
	static public final String VERSIONandDATE = "Version 1.0 - September, 21 2020";

	/**
	 * CSV separator
	 */
	static final String CSVSep = ";\t";

	/**
	 * DateFormatter
	 */
	static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	/**
	 * Default instance name suffix for dense transformation
	 */
	static final String DENSE_FILE_NAME_SUFFIX = "DENSE";

	/**
	 * Default instance name suffix for contingent node reduction
	 */
	static final String CTG_FILE_NAME_SUFFIX = "SQRT_CTG";

	/**
	 * logger
	 */
	static final Logger LOG = Logger.getLogger(STNUDensifier.class.getName());

	/**
	 * Maximum checks for a network
	 */
	static final int MAX_CHECKS = 50;

	/**
	 * Checker
	 */
	static final Class<STNU> STNU_CLASS = it.univr.di.cstnu.algorithms.STNU.class;

	/**
	 * Allows to check the execution time of DC checking algorithm giving a set of instances.
	 * The set of instances are checked in parallel if the machine is a multi-cpus one.<br>
	 * Moreover, this method tries to exploit <a href="https://github.com/OpenHFT/Java-Thread-Affinity">thread affinity</a> if kernel allows it.<br>
	 * So, if it is possible to reserve some CPU modifying the kernel as explained in <a href="https://github.com/OpenHFT/Java-Thread-Affinity">thread affinity
	 * page</a>.
	 * it is possible to run the parallel thread in the better conditions.
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws org.xml.sax.SAXException if instances contains syntax errors
	 * @throws javax.xml.parsers.ParserConfigurationException if input parameters contains errors
	 * @throws java.io.IOException if files are not readable
	 */
	@SuppressWarnings("null")
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

		LOG.finest("STNUDensifier " + VERSIONandDATE + "\nStart...");
		System.out.println("Checker " + VERSIONandDATE + "\n" + getNow() + ": Start of execution.");
		final STNUDensifier densifier = new STNUDensifier();

		if (!densifier.manageParameters(args))
			return;
		LOG.finest("Parameters ok!");
		if (densifier.versionReq)
			return;
		// All parameters are set

		/**
		 * <a id="affinity">AffinityLock allows to lock a CPU to a thread.</a>
		 * It seems that allows better performance when a CPU-bound task has to be executed!
		 * To work, it requires to reserve some CPUs.
		 * In our server I modified /boot/grub/grub.cfg adding "isolcpus=4,5,6,7,8,9,10,11" to the line that boot the kernel to reserve 8 CPUs.
		 * Such CPU have (socketId-coreId): 4(0-4), 5(0-5), 6(1-0), 7(1-1), 8(1-2), 9(1-3), 10(1-4), 11(1-5).
		 * Then I reboot the server.
		 * This class has to be started as normal (no using taskset!)
		 * I don't modify in /etc/default/grub and, then, update-grub because it changes a lot of things.
		 * **NOTE**
		 * After some simulations on AMD AMD Opteron™ 4334, I discovered that:
		 * 0) The best performance is obtained checking one file at time!
		 * 1) It doesn't worth to run more than 2 processor in parallel because this kind of app does not allow to scale. For each added process,
		 * the performance lowers about 10%.
		 * 2) Running two processes in the two different sockets lowers the performance about the 20%! It is better to run the two process on the same socket.
		 * 3) Therefore, I modified /boot/grub/grub.cfg setting "isolcpus=8,9,10,11"
		 */
		int nCPUs = densifier.nCPUs;// Runtime.getRuntime().availableProcessors();

		// Logging stuff for learning Affinity behaviour.
		// System.out.println("Base CPU affinity mask: " + AffinityLock.BASE_AFFINITY);
		// System.out.println("Reserved CPU affinity mask: " + AffinityLock.RESERVED_AFFINITY);
		// System.out.println("Current CPU affinity: " + Affinity.getCpu());
		// CpuLayout cpuLayout = AffinityLock.cpuLayout();
		// System.out.println("CPU Layout: " + cpuLayout.toString());
		// for (int k = 11; k > 3; k--) {
		// System.out.println("Cpu " + k + "\nSocket: " + cpuLayout.socketId(k) + ". Core:" + cpuLayout.coreId(k));
		// }
		/**
		 * check all files in parallel.
		 */
		/*
		 * 1st method using streams (parallelStream)
		 * Very nice, but it suffers of a known problem with streams:
		 * the use of default ForkJoinPool in the implementation of parallel() makes possible that
		 * a heavy task can block following tasks.
		 */
		// tester.inputCSTNFile.parallelStream().forEach(file -> cstnWorker(tester, file, executor, edgeFactory));

		/*
		 * 2nd method using Callable.
		 * A newFixedThreadPool executor create nProcessor threads and pipeline all process associated to file to such pool.
		 * There is no problem if one thread requires a lot of time.
		 * Final synchronization is obtained requesting .get from Callable.
		 * AffinityThreadFactory allows to lock a thread in one core for all the time (less overhead)
		 */
		final ExecutorService stnuDensifierExecutor = (nCPUs > 0) ? Executors.newFixedThreadPool(nCPUs,
				new AffinityThreadFactory("cstnWorker", AffinityStrategies.DIFFERENT_CORE)) : null;

		System.out.println(getNow() + ": #Processors for computation: " + nCPUs);
		System.out.println(getNow() + ": Instances to check are STNU instances.");
		RunMeter runMeter = new RunMeter(System.currentTimeMillis(), densifier.instances.size(), 0);
		runMeter.printProgress(0);

		List<Future<Boolean>> future = new ArrayList<>();

		int nTaskSuccessfullyFinished = 0;
		for (File file : densifier.instances) {
			if (nCPUs > 0) {
				future.add(stnuDensifierExecutor.submit(() -> densifier.worker(file, runMeter)));
			} else {
				if (densifier.worker(file, runMeter))
					nTaskSuccessfullyFinished++;
			}
		}
		if (nCPUs > 0) {
			// System.out.println(getNow() + ": #Tasks queued: " + future.size());
			// wait all tasks have been finished and count!
			for (Future<Boolean> f : future) {
				try {
					if (f.get()) {
						nTaskSuccessfullyFinished++;
					}
				} catch (Exception ex) {
					System.out.println("\nA problem occured during a check: " + ex.getMessage() + ". File ignored.");
				} finally {
					if (!f.isDone()) {
						LOG.warning("It is necessary to cancel the task before continuing.");
						f.cancel(true);
					}
				}
			}
		}
		String msg = "Number of instances processed successfully over total: " + nTaskSuccessfullyFinished + "/" + densifier.instances.size() + ".";
		LOG.info(msg);
		System.out.println("\n" + getNow() + ": " + msg);

		if (nCPUs > 0) {
			// executor shutdown!
			try {
				System.out.println(getNow() + ": Shutdown executors.");
				stnuDensifierExecutor.shutdown();
				stnuDensifierExecutor.awaitTermination(2, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				System.err.println(getNow() + ": Tasks interrupted.");
			} finally {
				if (!stnuDensifierExecutor.isTerminated()) {
					System.err.println(getNow() + ": Cancel non-finished tasks.");
				}
				stnuDensifierExecutor.shutdownNow();
				System.out.println(getNow() + ": Shutdown finished.\nExecution finished.");
			}
		}
	}

	/**
	 * @return the current date formatted.
	 */
	private static String getNow() {
		return dateFormatter.format(new Date());
	}

	/**
	 * 
	 */
	EdgeSupplier<Edge> currentEdgeFactory;

	/**
	 * Class for representing edge .
	 */
	Class<? extends Edge> currentEdgeImplClass;

	/**
	 * The node out-degree
	 */
	@Option(required = false, depends = {
			"--makeDense" }, name = "-d", usage = "The density of the network. The network is made dense adding edges to it that does not chenge its DC property till a density index is satisfied.")
	private double density = 1.0 / 2;

	/**
	 * The input file names. Each file has to contain a CSTN graph in GraphML format.
	 */
	@Argument(required = true, index = 0, usage = "Input files. Each input file has to be a CSTN graph in GraphML format.", metaVar = "CSTN_file_names", handler = StringArrayOptionHandler.class)
	private String[] inputFiles;

	/**
	 * 
	 */
	private List<File> instances;

	/**
	 * Dense request
	 */
	@Option(required = false, name = "--makeDense", usage = "Make the instance dense, i.e., #edges= density(n log_2 n), where n is the number of nodes.")
	private boolean makeDense = false;

	/**
	 * Dense request
	 */
	@Option(required = false, name = "--reduceCtg", usage = "Reduce the number of contingents to n^(0.5), where n is the number of nodes.")
	private boolean reduceCtg = false;

	/**
	 * 
	 */
	@Option(required = false, name = "--alsoNotDC", usage = "Transform the input instance and find a not DC version decreasing edge values.")
	private boolean alsoNotDCInstances = false;

	/**
	 * Roberto: I verified that with such kind of computation, using more than one thread to check more files in parallel reduces the single performance!!!
	 */
	@Option(required = false, name = "--nCPUs", usage = "Number of virtual CPUs that are reserved for this execution. Default is 0=no CPU reserved, there is only one thread for all the DC checking executions: such thread can allocated to a core, then desallocated and reallocated to another core. With nCPUs=1, there is only thread but such thread is allocated to a core till its end. With more thread, the global performance increases, but each file can requires more time because there is a competition among threads to access to the memory.")
	private int nCPUs = 0;

	/**
	 * Timeout in seconds for the check.
	 */
	@Option(required = false, name = "-t", aliases = "--timeOut", usage = "Timeout in seconds for the check", metaVar = "seconds")
	private int timeOut = 60 * 15;

	/**
	 * Local temporary network
	 */
	private File tmpNetwork;

	/**
	 * Software Version.
	 */
	@Option(required = false, name = "-v", aliases = { "--version" }, usage = "Version")
	private boolean versionReq = false;

	/**
	 * weight adjustment. This value is determined in the constructor.
	 */
	private int weightAdjustment;

	/**
	 * It cannot be used outside.
	 */
	private STNUDensifier() {
		try {
			this.tmpNetwork = File.createTempFile("currentNetwork", "xml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Print version of the this class in System.out.
	 */
	public void printVersion() {
		// I use a non-static method for having a general method that prints the right name for each derived class.
		System.out.println(this.getClass().getName() + " " + VERSIONandDATE + ".\nAcademic and non-commercial use only.\n"
				+ "Copyright © 2020, Roberto Posenato");
	}

	/**
	 * Adds 'edgesForNode' redundant edges having source 'node'. For determining the destination node, it makes 'hop' hops.
	 * If all the edges cannot be added because already present or it is not possible making them after 'hop' hops, it tries to add the remaining making fewer
	 * hops.
	 * 
	 * @param source node
	 * @param dest node
	 * @param weight weight new edge
	 * @param requiredEdges if <=0, the procedure returns 0.
	 * @param hop # of hops.
	 * @param denseDc graph where to add
	 * @param addedEdges added edges
	 * @return the number of edges added if it was possible. If there is no possibility to add edges because there not |paths| = hop, returns
	 *         {@link Constants#INT_NULL}.
	 */
	private int addRedundantNewEdges(LabeledNode source, LabeledNode dest, int weight, int hop, int requiredEdges, TNGraph<STNUEdge> denseDc,
			ObjectList<STNUEdge> addedEdges) {
		if (requiredEdges <= 0)
			return 0;
		if (hop == 0) {
			STNUEdge e = denseDc.findEdge(source, dest);
			if (e == null && source != dest) {
				STNUEdge newEdge = denseDc.getEdgeFactory().get(source.getName() + "-" + dest.getName());
				newEdge.setValue(weight + 10);
				denseDc.addEdge(newEdge, source, dest);
				LOG.finer("Added edge " + newEdge);
				addedEdges.add(newEdge);
				return 1;
			}
			LOG.finer("Cannot add an edge because it is already present: " + e);
			return 0;
		}
		// I have to make a hop
		int added = Constants.INT_NULL;
		int w;
		if (dest == null)
			dest = source;
		if (denseDc.outDegree(dest) == 0) {
			LOG.finer("Node " + dest.getName() + " has no neighbours.");
			return added;
		}
		int rAdd = 0;
		for (STNUEdge edge : denseDc.getOutEdges(dest)) {
			if (added != Constants.INT_NULL && requiredEdges - added <= 0) {
				LOG.finer("No more edges are necessary!");
				break;
			}
			w = edge.getValue();
			if (w == Constants.INT_NULL) {
				LOG.finer("Edge is contingent, ignore. Details: " + edge);
				continue;// contingent link
			}
			LabeledNode nextNode = denseDc.getDest(edge);
			int stillRequiredEdges = (added == Constants.INT_NULL) ? requiredEdges : requiredEdges - added;
			rAdd = addRedundantNewEdges(source, nextNode, weight + w, hop - 1, stillRequiredEdges, denseDc, addedEdges);
			if (rAdd != Constants.INT_NULL) {
				if (added == Constants.INT_NULL)
					added = rAdd;
				else
					added += rAdd;
			}
		}
		if (added > 0) {
			LOG.finer("Added " + added + " edges. Return.");
			return added;
		}
		// since it was no possible to add at desired hop, I try to add here
		if (source != dest) {
			LOG.finer("No edges were added. Try to add with present node" + dest);
			if (denseDc.findEdge(source, dest) == null) {
				STNUEdge newEdge = denseDc.getEdgeFactory().get(source.getName() + "-" + dest.getName());
				newEdge.setValue(weight + 10);
				denseDc.addEdge(newEdge, source, dest);
				LOG.finer("Added edge " + newEdge);
				addedEdges.add(newEdge);
				return 1;
			}
		}
		return added;
	}

	/**
	 * Adjusts the weight of all edges (increase/decrease) considering {@link #weightAdjustment}.
	 * 
	 * @param addedEdges addedEdge to modify
	 * @param increase true if the weight must be increased.
	 */
	private void adjustEdgeWeights(ObjectList<STNUEdge> addedEdges, boolean increase) {
		/*
		 * Don't implement the following actions:
		 * 1. Limit the adjustment to <= this.maxWeight because, otherwise, many values are squeezed to this.maxWeight.
		 * 2. Adjust only positive values when increase and negative value when !increase because it never reach a DC instance.
		 */
		int sign = (increase) ? +1 : -1;
		for (STNUEdge e : addedEdges) {
			int oldV = e.getValue();
			int adjustment = sign * this.weightAdjustment;
			if (increase && oldV < 0)
				adjustment /= 2;

			e.setValue(oldV + adjustment);
		}
	}

	/**
	 * Given an instance, determines a dense version of it adding redundant edges.
	 * 
	 * @param instance input instance to make dense
	 * @param addedEdges list containing the added edges
	 * @return the dense version
	 */
	private TNGraph<STNUEdge> makeDenseInstance(TNGraph<STNUEdge> instance, ObjectList<STNUEdge> addedEdges) {
		if (instance == null)
			return null;
		int nNodes = instance.getVertexCount();
		int log2OfN = 32 - Integer.numberOfLeadingZeros(nNodes);
		int nEdgesRequired = (int) ((nNodes * log2OfN) * this.density);
		LOG.finer("Making a dense instance. Number of nodes: " + nNodes + ". log_2 n: " + log2OfN + ". Required density: " + this.density + ". Required edges: "
				+ nEdgesRequired);

		TNGraph<STNUEdge> denseI = new TNGraph<>(instance, instance.getEdgeImplClass());
		int nCurrentEdges = denseI.getEdgeCount(), missingEdges = nEdgesRequired - nCurrentEdges;
		LOG.finer("Current #edges: " + nCurrentEdges + ". Missing edges: " + missingEdges);

		if (missingEdges <= 0)
			return instance;
		int nNodeWithOutEdges = 0;
		for (LabeledNode node : denseI.getVertices()) {
			if (denseI.outDegree(node) > 0)
				nNodeWithOutEdges++;
		}
		if (missingEdges > 0) {
			int hop = 4;
			int edgesForNode = missingEdges / nNodeWithOutEdges;
			int added;
			LOG.finer("#edges for node: " + edgesForNode);
			while (missingEdges > 0) {
				boolean stillPossibleToAdd = false;
				for (LabeledNode node : denseI.getVertices()) {
					added = addRedundantNewEdges(node, null, 0, hop, edgesForNode, denseI, addedEdges);
					if (added != Constants.INT_NULL) {
						missingEdges -= added;
						stillPossibleToAdd = true;
						LOG.finer("Added #edges: " + added + ". Missing edges: " + missingEdges);
					}
					if (missingEdges <= 0)
						break;
				}
				if (!stillPossibleToAdd) {
					LOG.warning("It is not possible to add more edges! Givin up!");
					break;
				}
				hop++;
			}
		}
		return denseI;
	}

	/**
	 * Given an instance, randomly reduce its contingent links to be, in total, sqrt(n), where n is the number of nodes.
	 * 
	 * @param instance input instance to modify
	 * @return the instance with sqrt(n) contingent links at maximum.
	 */
	private static TNGraph<STNUEdge> reduceCtg(TNGraph<STNUEdge> instance) {
		if (instance == null)
			return null;
		int nNodes = instance.getVertexCount();
		int nCtg = instance.getContingentNodeCount();
		int nCtgRequired = (int) (Math.sqrt(nNodes));
		LOG.finer("Reducing the number of contingent links from " + nCtg + " to " + nCtgRequired);

		TNGraph<STNUEdge> newInstance = new TNGraph<>(instance, instance.getEdgeImplClass());
		if ((nCtg - nCtgRequired) <= 0)
			return newInstance;

		int nodeC = nNodes;
		for (STNUEdge e : newInstance.getEdges()) {
			if (e.isContingentEdge()) {
				LabeledNode s = newInstance.getSource(e);
				LabeledNode d = newInstance.getDest(e);
				// make sure that the two endpoints have contingent property false.
				d.setALabel(null);
				d.setContingent(false);
				s.setALabel(null);
				s.setContingent(false);
				STNUEdge invertedE = newInstance.findEdge(d, s);
				// make links normal
				e.setConstraintType(ConstraintType.normal);
				invertedE.setConstraintType(ConstraintType.normal);
				s.setName("n_" + (nodeC++));
				d.setName("n_" + (nodeC++));
				LabeledNode[] list = { s, d };
				for (LabeledNode node : list) {
					for (STNUEdge e1 : newInstance.getInEdges(node)) {
						LabeledNode source = newInstance.getSource(e1);
						e1.setName("e" + source.getName() + "-" + node.getName());
					}
					for (STNUEdge e1 : newInstance.getOutEdges(node)) {
						LabeledNode dest = newInstance.getSource(e1);
						e1.setName("e" + node.getName() + "-" + dest.getName());
					}
				}
				nCtg--;
				if ((nCtg - nCtgRequired) == 0)
					break;
			}
		}
		return newInstance;

	}

	/**
	 * Builds a pair of DC and not DC of CSTN instances using the building parameters.
	 * The not DC instance is build adding one or more constraints to the previous generated DC instance.
	 * 
	 * @param g the input DC instance
	 * @return a pair of DC and not DC of CSTN instances. If the first member is null, it means that a generic error in the building
	 *         has occurred. If alsoNotDcInstance is false, the returned not DC instance is null.
	 */
	private ObjectPair<TNGraph<STNUEdge>> makePairSTNUInstances(TNGraph<STNUEdge> g) {

		LOG.info("Start transforming the instance.");
		TNGraph<STNUEdge> workingGraph = null;
		TNGraph<STNUEdge> notDCNewGraph = null;

		ObjectList<STNUEdge> addedEdges = new ObjectArrayList<>();

		if (this.reduceCtg) {
			workingGraph = reduceCtg(g);
			addedEdges = new ObjectArrayList<>(workingGraph.getEdges());
		}

		if (this.makeDense) {
			workingGraph = makeDenseInstance((workingGraph != null) ? workingGraph : g, addedEdges);
		}

		TNGraph<STNUEdge> lastDC = workingGraph;

		TNGraphMLWriter cstnWriter = new TNGraphMLWriter(null);

		int checkN = 0;// number of checks
		boolean nonDCfound = false, DCfound = false;
		STNU stnu = new STNU(workingGraph, this.timeOut);
		stnu.setDefaultConsistencyCheckAlg(CheckAlgorithm.RUL2020);

		STNUCheckStatus status = new STNUCheckStatus();

		while (true) {
			if (checkN > 0) {
				stnu.reset();
				stnu.setG(new TNGraph<>(workingGraph, EdgeSupplier.DEFAULT_STNU_EDGE_CLASS));
			}

			if (LOG.isLoggable(Level.FINER)) {
				try {
					cstnWriter.save(stnu.getG(), this.tmpNetwork);
				} catch (IOException e) {
					System.err.println(
							"It is not possible to save the result. File "+this.tmpNetwork +" cannot be created: " + e.getMessage()+"\n Computation  continues.");
				}
				LOG.finer("Current cstn saved as 'current.stnu' before checking.");
			}

			try {
				LOG.fine("DC Check started.");
				// if (this.reduceCtg && !this.makeDense) {
				status.consistency = true;
				status.finished = true;
				status.timeout = false;
				// } else {
				// status = stnu.dynamicControllabilityCheck();
				// }
				checkN++;
				LOG.fine("DC Check finished.");
			} catch (Exception ex) {
				ex.printStackTrace();

				// String fileName = "error" + System.currentTimeMillis() + ".stnu";
				// LOG.finer("DC Check interrupted for the following reason: " + ex.getMessage() + ". Instance is saved as " + fileName + ".");
				// File d = new File(fileName);
				// try {
				// Files.move(this.tmpNetwork, d);
				// } catch (IOException e1) {
				// LOG.finer("Problem to save 'current.stnu' as non valid instance for logging. Program continues anyway.");
				// }
				// return null;
			}

			if (status.timeout) {
				String fileName = "timeOut" + System.currentTimeMillis() + ".stnu";
				LOG.finer("DC Check finished for timeout. Instance is saved as " + fileName + ".");
				File d = new File(fileName);
				try {
					Files.move(this.tmpNetwork, d);
				} catch (IOException ex) {
					LOG.finer("Problem to save 'current.cstn' as time out instance. Program continues anyway.");
				}
				return null;
			}

			if (status.isControllability()) {
				LOG.finer("Random instance is DC.");
				lastDC = new TNGraph<>(workingGraph, EdgeSupplier.DEFAULT_STNU_EDGE_CLASS);
				DCfound = true;
				if (!nonDCfound && this.alsoNotDCInstances) {
					LOG.finer("Now, a not DC instance must be generated. Tentative #" + checkN);
					// we lower the edge value
					adjustEdgeWeights(addedEdges, false);
				} else {
					LOG.finer("The pair has been found after " + checkN + " iterations.");
					return new ObjectPair<>(lastDC, notDCNewGraph);
				}
			} else {
				LOG.finer("Random instance is not DC.");
				notDCNewGraph = new TNGraph<>(workingGraph, EdgeSupplier.DEFAULT_STNU_EDGE_CLASS);
				nonDCfound = true;
				if (!DCfound) {
					LOG.finer("Now, a DC instance must be generated. Tentative #" + checkN);
					adjustEdgeWeights(addedEdges, true);
				} else {
					LOG.finer("The pair has been found after " + checkN + " iterations.");
					return new ObjectPair<>(lastDC, notDCNewGraph);
				}
			}

			if (checkN > MAX_CHECKS) {
				LOG.finer("This network was checked more than " + MAX_CHECKS
						+ " times without finding the wanted pair. Program continues witho another network.");
				return null;
			}
		}
	}

	/**
	 * Simple method to manage command line parameters using args4j library.
	 * 
	 * @param args input args
	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
	 */
	private boolean manageParameters(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			// if there's a problem in the command line, you'll get this exception. this will report an error message.
			System.err.println(e.getMessage());
			System.err.println("java -cp CSTNU-<version>.jar -cp it.univr.di.cstnu.STNUDensifier [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			// System.err.println("Example: java -jar Checker.jar" + parser.printExample(OptionHandlerFilter.REQUIRED) +
			// " <CSTN_file_name0> <CSTN_file_name1>...");
			return false;
		}
		if (this.versionReq) {
			System.out.print(this.getClass().getName() + " " + VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2017-2020, Roberto Posenato");
			return true;
		}

		if (this.density < 0 || this.density > 1) {
			throw new IllegalArgumentException(
					"The density is not valid. It must be in [0, 1].");
		}
		if (!this.makeDense && !this.reduceCtg) {
			throw new IllegalArgumentException(
					"No modification required. So, nothing has to be done.");
		}

		if (this.reduceCtg && !this.makeDense) {
			this.alsoNotDCInstances = false;// FIXME It is not managed.
		}
		String suffix = "";
		this.currentEdgeImplClass = STNUEdgeInt.class;
		suffix = "stnu";
		this.currentEdgeFactory = new EdgeSupplier<>(this.currentEdgeImplClass);

		// LOG.finest("File number: " + this.fileNameInput.length);
		// LOG.finest("File names: " + Arrays.deepToString(this.fileNameInput));
		this.instances = new ArrayList<>(this.inputFiles.length);
		for (String fileName : this.inputFiles) {
			File file = new File(fileName);
			if (!file.exists()) {
				System.err.println("File " + fileName + " does not exit.");
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
			if (!file.getName().endsWith(suffix)) {
				System.err.println("File " + fileName + " has not the right suffix associated to the suffix of the given network type (right suffix: "
						+ suffix + "). Game over :-/");
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
			this.instances.add(file);
		}

		return true;
	}

	/**
	 * @param fileName the input file name.
	 * @return new file name con the right suffixes.
	 */
	private String getNewFileName(String fileName) {
		StringBuffer sb = new StringBuffer(fileName.replace(".stnu", ""));
		if (this.reduceCtg) {
			sb.append("_");
			sb.append(CTG_FILE_NAME_SUFFIX);
		}
		if (this.makeDense) {
			sb.append("_");
			sb.append(DENSE_FILE_NAME_SUFFIX);
		}
		sb.append(".stnu");
		return sb.toString();
	}

	/**
	 * @param file input file
	 * @param runState current state
	 * @return true if required task ends successfully, false otherwise.
	 */
	@SuppressWarnings("unchecked")
	private boolean worker(File file, RunMeter runState) {
		// System.out.println("Analyzing file " + file.getName() + "...");
		LOG.finer("Loading " + file.getName() + "...");
		TNGraphMLReader<STNUEdge> graphMLReader = new TNGraphMLReader<>();
		TNGraph<STNUEdge> graphToAdjust = null;
		try {
			graphToAdjust = graphMLReader.readGraph(file, (Class<STNUEdge>) this.currentEdgeImplClass);
		} catch (IOException | ParserConfigurationException | SAXException e2) {
			String msg = "File " + file.getName() + " cannot be parsed. Details: " + e2.getMessage() + ".\nIgnored.";
			LOG.warning(msg);
			System.out.println(msg);
			return false;
		}
		LOG.finer("...done!");
		if (graphToAdjust == null) {
			LOG.warning("File " + file.getName() + " does not contain a valid STNU instance.\nIgnored.");
			return false;
		}

		ObjectPair<TNGraph<STNUEdge>> pair = makePairSTNUInstances(graphToAdjust);
		TNGraphMLWriter stnuWriter = new TNGraphMLWriter(null);

		if (pair != null) {
			// save the two instances.
			String fileName = getNewFileName(file.getName());
			File outputFile = new File(fileName);

			try {
				stnuWriter.save(pair.getFirst(), outputFile);
			} catch (IOException e) {
				System.err.println(
						"It is not possible to save the result. File "+outputFile +" cannot be created: " + e.getMessage()+". Computation continues.");
			}
			
			if (pair.getSecond() != null) {
				fileName = "NOT" + fileName;
				outputFile = new File(fileName);
				
				try {
					stnuWriter.save(pair.getSecond(), outputFile);
				} catch (IOException e) {
					System.err.println(
							"It is not possible to save the result. File " + outputFile + " cannot be created: " + e.getMessage() + ". Computation continues.");
				}
				System.out.println("NOT DC instance " + fileName + " saved.");
			}
			runState.printProgress();
			return true;
		}
		System.out.println("It was not possible to densify " + file.getName() + ".");
		runState.printProgress();
		return false;
	}

}
