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
import java.util.logging.Logger;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import cern.colt.Arrays;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.univr.di.cstnu.CSTNRunningTime;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * @author posenato
 */
public class Luke2GraphML {
	/**
	 * Version
	 */
	static final String VERSIONandDATE = "1.1, March, 11 2016";

	/**
	 * class logger
	 */
	static final Logger LOG = Logger.getLogger("it.univr.di.cstnu.graph.Luke2GraphML");

	/**
	 * The input file names. Each file has to contain a CSTN graph in GraphML
	 * format.
	 */
	@Argument(required = true, index = 0, usage = "Input file. It has to be a CSTN graph in Luke's format.", metaVar = "CSTN_file_name")
	private String fileNameInput;

	/**
	 * Output file where to write the CSTN in GraphML format.
	 */
	@Option(required = false, name = "-o", aliases = "--output", usage = "Output to this file in GraphML format.", metaVar = "outputFile")
	private File fOutput = null;

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
	 * Class for representing edge labeled values.
	 */
	static private final Class<? extends LabeledIntMap> labeledIntValueMapClass = LabeledIntTreeMap.class;

	/**
	 * 
	 */
	private File inputCSTNFile;

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
			// System.err.println("Example: java -jar CSTNRunningTime.jar" +
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
		CSTNRunningTime tester = new CSTNRunningTime();

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

		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMapClass);

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

		System.out.println("Graph parsing ended.");

		SpringLayout2<LabeledNode, LabeledIntEdge> layout = new SpringLayout2<>(g);
		layout.setSize(new Dimension(1024, 800));
		layout.initialize();
		GraphMLWriter graphWriter = new GraphMLWriter(layout);

		try (PrintWriter writer = new PrintWriter(converter.output)) {
			graphWriter.save(g, writer);
			System.out.println("Graph saved into file " + converter.fOutput);
		}
	}

	/**
	 * @param reader
	 * @param line
	 * @param g
	 * @param int2Node
	 * @throws IOException
	 */
	private static void addEdge(BufferedReader reader, String line, LabeledIntGraph g,
			Int2ObjectMap<LabeledNode> int2Node) throws IOException {
		final String patternEdge = new String("EDGE \\(|,|\\):");
		String[] nodeParts = line.split(patternEdge);
		// nodeParts[0] is empty!

		LabeledNode sourceNode = int2Node.get(Integer.valueOf(nodeParts[1]));
		LabeledNode destNode = int2Node.get(Integer.valueOf(nodeParts[2]));
		LabeledIntEdge edge = g.getEdgeFactory().create(sourceNode.name + "-" + destNode.name);
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
		if (edge.labeledValueSet().size() > 0) {
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
	private static void addNode(BufferedReader reader, String line, LabeledIntGraph g,
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
}