/**
 * 
 */
package it.univr.di.cstnu.graph;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.logging.Logger;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.univr.di.cstnu.algorithms.Checker;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;

/**
 * Utility class for converting CSTN file in Luke format to GraphML format.
 * 
 * @author posenato
 */
public class Luke2GraphML {
	/**
	 * class logger
	 */
	static final Logger LOG = Logger.getLogger("Luke2GraphML");

	/**
	 * Version
	 */
	static final String VERSIONandDATE = "1.1, March, 11 2016";


	/**
	 * @param args
	 *            a CSTN file in Luke's format.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// System.out.println(Arrays.toString("<694,[A(-E)]>".split("<|,\\[|\\]>")));
		// System.out.println("<694,[A(-E)]>".split("<|,\\[|\\]>")[2].replace("(",
		// "").replace(")", "").replace("-", "¬"));

		LOG.finest("Start...");
		System.out.println("Start of execution...");
		Checker tester = new Checker();

		Luke2GraphML converter = new Luke2GraphML();

		if (!converter.manageParameters(args))
			return;

		LOG.finest("Parameters ok!");
		System.out.println("Parameters ok!");
		if (converter.versionReq) {
			System.out.print(tester.getClass().getName() + " " + VERSIONandDATE
					+ ". Academic and non-commercial use only.\n" + "Copyright © 2016, Roberto Posenato");
			return;
		}


		TNGraph<CSTNEdge> g = new TNGraph<>(EdgeSupplier.DEFAULT_CSTN_EDGE_CLASS);

		Int2ObjectMap<LabeledNode> int2Node = new Int2ObjectOpenHashMap<>();
		int2Node.defaultReturnValue(null);

		try (BufferedReader reader = new BufferedReader(new FileReader(converter.inputCSTNFile))) {
			while (reader.ready()) {
				String line = reader.readLine();
				LOG.finest("Line:" + line);
				if (line.startsWith(";") || line.startsWith("--") || line.startsWith("==") || line.isEmpty()) {
					// it is comment, ignore
					// it is a separator, ignore
					continue;
				}
				if (line.startsWith("TP(")) {
					// it is node
					addNode(reader, line, g, int2Node);
				}
				if (line.startsWith("EDGE")) {
					// it is the start of an egde
					addEdge(reader, line, g, int2Node);
				}
			}
		}

		System.out.println("TNGraph parsing ended.");

		SpringLayout2<LabeledNode, CSTNEdge> layout = new SpringLayout2<>(g);
		layout.setSize(new Dimension(1024, 800));
		layout.initialize();
		TNGraphMLWriter graphWriter = new TNGraphMLWriter(layout);

		try (PrintWriter writer = new PrintWriter(converter.output)) {
			graphWriter.save(g, writer);
			System.out.println("TNGraph saved into file " + converter.fOutput);
		}
	}

	/**
	 * @param reader
	 * @param line
	 * @param g
	 * @param int2Node
	 * @throws IOException
	 */
	private static void addEdge(BufferedReader reader, String line, TNGraph<CSTNEdge> g,
			Int2ObjectMap<LabeledNode> int2Node) throws IOException {
		final String patternEdge = new String("EDGE \\(|,|\\):");
		String[] nodeParts = line.split(patternEdge);
		// nodeParts[0] is empty!

		int sI = Integer.valueOf(nodeParts[1]);
		int dI = Integer.valueOf(nodeParts[2]);
		LabeledNode sourceNode = int2Node.get(sI);
		LabeledNode destNode = int2Node.get(dI);
		CSTNEdge edge = g.getEdgeFactory().get(sourceNode.name + "-" + destNode.name);
		Label label = null;
		String[] labelParts = null;
		while (reader.ready()) {
			String line1 = reader.readLine();
			if (line1.startsWith("<*POS-INF*") || line1.startsWith(";") || line1.isEmpty())
				continue;
			if (line1.startsWith("---"))
				break;
			LOG.info("line1:" + line1);
			labelParts = line1.split("<|,\\[|\\]>");
			if (line1.contains("[]")) {
				// empty label not captured by split
				label = Label.emptyLabel;
			} else {
				label = Label.parse(toLabel(labelParts[2]));
				LOG.info("line1:" + line1 + ". labelparts:" + Arrays.toString(labelParts) + ". label:" + label);
			}
			int value = Integer.valueOf(labelParts[1]);
			edge.mergeLabeledValue(label, value);
		}
		if (edge.getLabeledValueSet().size() > 0) {
			g.addEdge(edge, sourceNode, destNode);
		}
	}

	/**
	 * @param reader
	 * @param line
	 * @param g
	 * @param int2Node
	 * @throws Exception
	 */
	private static <C extends LabeledIntMap> void addNode(BufferedReader reader, String line, TNGraph<CSTNEdge> g,
			Int2ObjectMap<LabeledNode> int2Node) throws Exception {
		final String patternNode = new String("TP\\(|\\):[\\s\u00A0]+|,\\s+\\[|\\],\\s+|\\]");
		String[] nodeParts = line.split(patternNode);
		// nodeParts[0] is empty!
		LOG.info("NodeParts:" + Arrays.toString(nodeParts) + ". Lenght:" + nodeParts.length);
		LOG.info("nodeParts[2]: '" + nodeParts[2] + "'");// . Leading char code: "+ Character.codePointAt(nodeParts[2], 0));
		LabeledNode node = new LabeledNode(nodeParts[2]);
		boolean added = g.addVertex(node);
		if (!added)
			throw new Exception("Node " + node + " cannot be insert.");

		if (int2Node.put(Integer.valueOf(nodeParts[1]).intValue(), node) != null)
			throw new Exception("Node " + node + " already inserted.");
		if (nodeParts.length == 3) {
			node.setLabel(Label.emptyLabel);
		} else {
			node.setLabel(Label.parse(toLabel(nodeParts[3])));
		}
		if (nodeParts.length == 5) {
			// it is an observation time point
			LOG.info("nodeParts[4]:" + nodeParts[4]);
			node.setObservable(nodeParts[4].trim().charAt(0));
		}
	}

	/**
	 * @param lukeFormat
	 * @return label as string
	 */
	private static String toLabel(String lukeFormat) {
		return lukeFormat.trim().replace("(", "").replace(")", "").replace("-", "¬");
	}

	/**
	 * Class to use for managing labeled values of edges.
	 */
	Class<LabeledIntMap> internalMapImplementationClass;

	/**
	 * The input file names. Each file has to contain a CSTN tNGraph in GraphML
	 * format.
	 */
	@Argument(required = true, index = 0, usage = "Input file. It has to be a CSTN tNGraph in Luke's format.", metaVar = "CSTN_file_name")
	private String fileNameInput;

	/**
	 * Output file where to write the CSTN in GraphML format.
	 */
	@Option(required = false, name = "-o", aliases = "--output", usage = "Output to this file in GraphML format.", metaVar = "outputFile")
	private File fOutput = null;

	/**
	 * 
	 */
	private File inputCSTNFile;

	/**
	 * Output stream to fOutput
	 */
	private PrintStream output = null;

	/**
	 * Software Version.
	 */
	@Option(required = false, name = "-v", aliases = "--version", usage = "Version")
	private boolean versionReq = false;

	/**
	 * Simple method to manage command line parameters using args4j library.
	 * 
	 * @param args
	 * @return false if a parameter is missing or it is wrong. True if every
	 *         parameters are given in a right format.
	 */
	private boolean manageParameters(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			// if there's a problem in the command line, you'll get this
			// exception. this will report an error message.
			System.err.println(e.getMessage());
			System.err
					.println("java -cp CSTNU-<version>.jar -cp it.univr.di.cstnu.Luke2GraphML [options...] argument.");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			// System.err.println("Example: java -jar Checker.jar" +
			// parser.printExample(OptionHandlerFilter.REQUIRED) +
			// " <CSTN_file_name0> <CSTN_file_name1>...");
			return false;
		}
		LOG.finest("File name: " + this.fileNameInput);
		this.inputCSTNFile = new File(this.fileNameInput);
		if (!this.inputCSTNFile.exists()) {
			System.err.println("File " + this.inputCSTNFile + " does not exit.");
			parser.printUsage(System.err);
			System.err.println();
			return false;
		}
		LOG.finest("File: " + this.inputCSTNFile);

		if (this.fOutput != null) {
			if (this.fOutput.isDirectory()) {
				System.err.println("Output file is a directory.");
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
			if (!this.fOutput.getName().endsWith(".csv")) {
				this.fOutput.renameTo(new File(this.fOutput.getAbsolutePath() + ".cstn"));
			}
			if (this.fOutput.exists()) {
				this.fOutput.renameTo(new File(this.fOutput.getAbsoluteFile() + ".old"));
				this.fOutput.delete();
			}
			try {
				this.fOutput.createNewFile();
				this.output = new PrintStream(this.fOutput);
			} catch (IOException e) {
				System.err.println("Output file cannot be created: " + e.getMessage());
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
		} else {
			this.output = System.out;
		}
		return true;
	}
}