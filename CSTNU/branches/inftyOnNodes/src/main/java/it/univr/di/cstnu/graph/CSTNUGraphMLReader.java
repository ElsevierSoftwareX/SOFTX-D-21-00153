/**
 *
 */
package it.univr.di.cstnu.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import edu.uci.ics.jung.io.GraphMLMetadata;
import edu.uci.ics.jung.io.GraphMLReader;
import it.univr.di.Debug;
import it.univr.di.cstnu.algorithms.CSTN;
import it.univr.di.cstnu.graph.LabeledIntEdge.ConstraintType;
import it.univr.di.labeledvalue.ALabelAlphabet;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledLowerCaseValue;

/**
 * Allows to read a graph from a file written in GraphML format.<br>
 * GraphML format allows the definition of different attributes for the graph, vertices and edges.<br>
 * All attributes are defined in the first part of a GraphML file. Examples of GraphML file that can read by this class are given in the Instances directory
 * under CstnuTool one.
 * 
 * @author posenato
 * @version $Id: $Id
 */
public class CSTNUGraphMLReader {
	/**
	 * * Since we want to preserve edge names given by in the file and such namescan conflict with the ones given by the standard edgeFactory,
	 * we modify the standard factory altering the default name
	 * 
	 * @author posenato
	 * @param <C>
	 */
	static private class InternalEdgeFactory<C extends LabeledIntMap> implements Supplier<LabeledIntEdge> {

		/**
		 * 
		 */
		Supplier<LabeledIntEdge> edgeFactory;

		/**
		 * @param mapTypeImplementation
		 */
		public InternalEdgeFactory(Class<C> mapTypeImplementation) {
			super();
			this.edgeFactory = new LabeledIntEdgeSupplier<>(mapTypeImplementation);
		}

		@Override
		public LabeledIntEdge get() {
			LabeledIntEdge e = this.edgeFactory.get();
			e.setName(prefix + e.getName());
			return e;
		}
	}

	/**
	 * logger
	 */
	static final Logger LOG = Logger.getLogger(CSTNUGraphMLReader.class.getName());

	/**
	 * 
	 */
	static final String prefix = "__";

	/**
	 * 
	 */
	private static Supplier<LabeledNode> vertexFactory = new Supplier<LabeledNode>() {

		Supplier<LabeledNode> factory = LabeledNode.getFactory();

		@Override
		public LabeledNode get() {
			LabeledNode node = this.factory.get();
			node.setName(prefix + node.getName());
			return node;
		}
	};

	/**
	 * The result of the loading action.
	 */
	LabeledIntGraph graph;

	/**
	 * ALabel alphabet for UC a-labels
	 */
	private ALabelAlphabet aLabelAlphabet;

	/**
	 * 
	 */
	private Supplier<LabeledIntEdge> edgeFactory;

	/**
	 * Input file reader
	 */
	private Reader fileReader;

	/**
	 * true if the given file ends with '.cstn'
	 */
	private boolean isCSTN;

	/**
	 * Class for representing internal labeled values.
	 */
	private Class<? extends LabeledIntMap> mapTypeImplementation;

	/**
	 * Allows to read a graph from a file written in GraphML format.<br>
	 * GraphML format allows the definition of different attributes for the graph, vertices and edges.<br>
	 * All attributes are defined in the first part of a GraphML file. Examples of GraphML file that can read by this class are given in the Instances directory
	 * under CstnuTool one.
	 * 
	 * @param graphFile
	 * @param labeledValueSetImplementationClass it is necessary for creating the right factory.
	 * @throws FileNotFoundException if the graphFile is not found
	 */
	public CSTNUGraphMLReader(final File graphFile, Class<? extends LabeledIntMap> labeledValueSetImplementationClass) throws FileNotFoundException {
		if (graphFile == null) {
			throw new FileNotFoundException("The given file does not exist.");
		}
		this.fileReader = new FileReader(graphFile);
		this.isCSTN = graphFile.getName().endsWith(".cstn");
		this.aLabelAlphabet = new ALabelAlphabet();
		this.mapTypeImplementation = labeledValueSetImplementationClass;
		this.edgeFactory = new InternalEdgeFactory<>(this.mapTypeImplementation);
		this.graph = new LabeledIntGraph(this.mapTypeImplementation, this.aLabelAlphabet);
		this.graph.setInputFile(graphFile);
	}

	/**
	 * @return the graphML as LabeledIntGraph.
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public LabeledIntGraph readGraph() throws IOException, ParserConfigurationException, SAXException {
		/*
		 * I use CSTNUGraphMLReader instead of GraphMLReader2 because on 2017-11-01 I discovered that GraphMLReader2 does not allow to read
		 * edge attributes that are very long, like the labeled upper case values of a checked CSTNU.
		 * CSTNUGraphMLReader is a little less intuitive but it manages all attributes in a right way!
		 */
		GraphMLReader<LabeledIntGraph, LabeledNode, LabeledIntEdge> graphReader = new GraphMLReader<>(vertexFactory, this.edgeFactory);
		// populate the graph.
		graphReader.load(this.fileReader, this.graph);

		// Now graph contains all vertices and edges with default names (the factory cannot set the right names).

		/*
		 * Node attribute setting!
		 */
		// Name
		graphReader.getVertexIDs().forEach(new BiConsumer<LabeledNode, String>() {
			@Override
			public void accept(LabeledNode n, String s) {
				n.setName(s);
				if (s.equals(CSTN.ZeroNodeName))
					CSTNUGraphMLReader.this.graph.setZ(n);
			}
		});
		// Label
		Function<LabeledNode, String> nodeLabelF = graphReader.getVertexMetadata().get(CSTNUGraphMLWriter.NODE_LABEL_KEY).transformer;
		// Observed proposition
		Function<LabeledNode, String> nodeObservedPropF = graphReader.getVertexMetadata().get(CSTNUGraphMLWriter.NODE_OBSERVED_KEY).transformer;
		// X position
		Function<LabeledNode, String> nodeXF = graphReader.getVertexMetadata().get(CSTNUGraphMLWriter.NODE_X_KEY).transformer;
		// Y position
		Function<LabeledNode, String> nodeYF = graphReader.getVertexMetadata().get(CSTNUGraphMLWriter.NODE_Y_KEY).transformer;
		// Potential
		GraphMLMetadata<LabeledNode> nodeLabeledPotentialValueMD = graphReader.getVertexMetadata().get(CSTNUGraphMLWriter.NODE_POTENTIAL_KEY);
		Function<LabeledNode, String> nodeLabeledPotentialValueF = (nodeLabeledPotentialValueMD != null) ? nodeLabeledPotentialValueMD.transformer : null;

		for (LabeledNode n : this.graph.getVertices()) {
			n.setLabel(Label.parse(nodeLabelF.apply(n)));
			String s = nodeObservedPropF.apply(n);
			if ((s != null) && (s.length() == 1)) {
				n.setObservable(s.charAt(0));
			}

			if (nodeLabeledPotentialValueF != null) {
				String data = nodeLabeledPotentialValueF.apply(n);
				LabeledIntMap potentialMap = AbstractLabeledIntMap.parse(data);
				if (data != null && data.length() > 2 && (potentialMap == null || potentialMap.isEmpty()))
					throw new IllegalArgumentException("Potential values in a wrong format: " + data + " in node " + n);
				n.setPotential(potentialMap);
			}
			n.setX(Double.parseDouble(nodeXF.apply(n)));
			n.setY(Double.parseDouble(nodeYF.apply(n)));
		}

		/*
		 * Edge attribute setting!
		 */
		// Name
		graphReader.getEdgeIDs().forEach(new BiConsumer<LabeledIntEdge, String>() {
			@Override
			public void accept(LabeledIntEdge e, String s) {
				e.setName(s);
				if (!e.getName().equals(s)) {
					// there is a problem that the name has been already used...
					s = CSTNUGraphMLReader.this.graph.getSource(e).getName() + "_" + CSTNUGraphMLReader.this.graph.getDest(e).getName();
					e.setName(s);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING, "Fixing edge name using '" + s + "'.");
						}
					}
				}
			}
		});
		// Type
		Function<LabeledIntEdge, String> edgeTypeF = graphReader.getEdgeMetadata().get(CSTNUGraphMLWriter.EDGE_TYPE_KEY).transformer;
		// Labeled Value
		Function<LabeledIntEdge, String> edgeLabeledValueF = graphReader.getEdgeMetadata().get(CSTNUGraphMLWriter.EDGE_LABELED_VALUE_KEY).transformer;
		// I parse also value parameter that was present in the first version of the graph file
		Function<LabeledIntEdge, String> edgeOldValueF = null;
		if (graphReader.getEdgeMetadata().get("Value") != null)
			edgeOldValueF = graphReader.getEdgeMetadata().get("Value").transformer;

		for (LabeledIntEdge e : this.graph.getEdges()) {
			// Type
			e.setConstraintType(ConstraintType.valueOf(edgeTypeF.apply(e)));
			// Labeled Value
			String data = edgeLabeledValueF.apply(e);
			if (data != null) {
				LabeledIntMap map = AbstractLabeledIntMap.parse(data);
				if (data.length() > 2 && map == null) {
					throw new IllegalArgumentException("Labeled values in a wrong format: " + data + " in edge " + e);
				}
				e.setLabeledValueMap(map);
			}
			// I parse also value parameter that was present in the first version of the graph file
			if (edgeOldValueF != null) {
				data = edgeOldValueF.apply(e);
				if (data != null && !data.isEmpty()) {
					e.putLabeledValue(Label.emptyLabel, Integer.parseInt(data));
				}
			}
			if (this.isCSTN && e.isEmpty())
				this.graph.removeEdge(e);
		}
		if (this.isCSTN)
			return this.graph;

		// FROM HERE the graph is assumed to be a CSTNU graph!

		GraphMLMetadata<LabeledIntEdge> edgeLabeledUCValueMD = graphReader.getEdgeMetadata().get(CSTNUGraphMLWriter.EDGE_LABELED_UC_VALUE_KEY);
		GraphMLMetadata<LabeledIntEdge> edgeLabeledLCValueMD = graphReader.getEdgeMetadata().get(CSTNUGraphMLWriter.EDGE_LABELED_LC_VALUE_KEY);
		if (edgeLabeledUCValueMD == null || edgeLabeledLCValueMD == null) {
			// Graph file is still in old format!
			if (Debug.ON) {
				LOG.warning("The input file does not contain the meta declaration for upper case value or lower case value. Please, fix it adding" +
						"<key id=\"UpperCaseLabeledValues\" for=\"edge\"> \n" +
						"<default></default> \n" +
						"</key>\n" +
						" or \n" +
						"<key id=\"LowerCaseLabeledValues\" for=\"edge\"> \n" +
						"<default></default> \n" +
						"</key>\n" +
						"before <graph> tag.");
			}
			return this.graph;
		}
		Function<LabeledIntEdge, String> edgeLabeledUCValueF = edgeLabeledUCValueMD.transformer;
		Function<LabeledIntEdge, String> edgeLabeledLCValueF = edgeLabeledLCValueMD.transformer;

		for (LabeledIntEdge e : this.graph.getEdges()) {
			// Labeled UC Value
			String data = edgeLabeledUCValueF.apply(e);
			LabeledALabelIntTreeMap upperCaseMap = LabeledALabelIntTreeMap.parse(data, this.aLabelAlphabet);
			if (data != null && data.length() > 2 && (upperCaseMap == null || upperCaseMap.isEmpty()))
				throw new IllegalArgumentException("Upper Case values in a wrong format: " + data + " in edge " + e);
			if (upperCaseMap == null)
				upperCaseMap = new LabeledALabelIntTreeMap();
			e.setUpperCaseValueMap(upperCaseMap);
			// Labeled LC Value
			data = edgeLabeledLCValueF.apply(e);
			LabeledLowerCaseValue lowerCaseValue = LabeledLowerCaseValue.parse(data, this.aLabelAlphabet);
			if (data != null && data.length() > 2 && (lowerCaseValue == null || lowerCaseValue.isEmpty()))
				throw new IllegalArgumentException("Lower Case values in a wrong format: " + data + " in edge " + e);
			if (lowerCaseValue == null)
				lowerCaseValue = LabeledLowerCaseValue.emptyLabeledLowerCaseValue;
			e.setLowerCaseValue(lowerCaseValue);
			if (e.isEmpty())
				this.graph.removeEdge(e);
		}

		return this.graph;
	}
}
