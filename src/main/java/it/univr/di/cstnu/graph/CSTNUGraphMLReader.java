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
	 * logger
	 */
	static final Logger LOG = Logger.getLogger(CSTNUGraphMLReader.class.getName());

	/**
	 * ALabel alphabet for UC a-labels
	 */
	ALabelAlphabet aLabelAlphabet = new ALabelAlphabet();

	/**
	 * Input file reader
	 */
	Reader fileReader;

	/**
	 * The result of the loading action.
	 */
	LabeledIntGraph graph;

	/**
	 * Class for representing internal labeled values.
	 */
	Class<? extends LabeledIntMap> mapTypeImplementation;

	/**
	 * 
	 */
	Supplier<LabeledNode> vertexFactory = LabeledNode.getFactory();
	/**
	 * 
	 */
	Supplier<LabeledIntEdge> edgeFactory;

	/**
	 * Allows to read a graph from a file written in GraphML format.<br>
	 * GraphML format allows the definition of different attributes for the graph, vertices and edges.<br>
	 * All attributes are defined in the first part of a GraphML file. Examples of GraphML file that can read by this class are given in the Instances directory
	 * under CstnuTool one.
	 * 
	 * @param graphFile
	 * @param labeledValueSetImplementationClass it is necessary for creating the right factory (reflection doesn't work due to reification!)
	 * @throws FileNotFoundException if the graphFile is not found
	 */
	public CSTNUGraphMLReader(final File graphFile, Class<? extends LabeledIntMap> labeledValueSetImplementationClass) throws FileNotFoundException {
		this.fileReader = new FileReader(graphFile);
		this.mapTypeImplementation = labeledValueSetImplementationClass;
		this.edgeFactory = new LabeledIntEdgeSupplier<>(this.mapTypeImplementation);
		this.graph = new LabeledIntGraph(this.mapTypeImplementation);
		this.graph.setFileName(graphFile);
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
		GraphMLReader<LabeledIntGraph, LabeledNode, LabeledIntEdge> graphReader = new GraphMLReader<>(this.vertexFactory, this.edgeFactory);
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
				if (s.equals(CSTN.OmegaNodeName))
					CSTNUGraphMLReader.this.graph.setΩ(n);
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

		for (LabeledNode n : this.graph.getVertices()) {
			n.setLabel(Label.parse(nodeLabelF.apply(n)));
			String s = nodeObservedPropF.apply(n);
			if ((s != null) && (s.length() == 1)) {
				n.setObservable(s.charAt(0));
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
		// Labeled UC Value
		Function<LabeledIntEdge, String> edgeLabeledUCValueF = graphReader.getEdgeMetadata().get(CSTNUGraphMLWriter.EDGE_LABELED_UC_VALUE_KEY).transformer;
		if (edgeLabeledUCValueF == null)
			throw new IllegalArgumentException("The input file does not contain the meta declaration for upper case value. Please, fix it adding" +
					"<key id=\"UpperCaseLabeledValues\" for=\"edge\"> \n" +
					"<default></default> \n" +
					"</key>\n" +
					"before <graph> tag.");
		// Labeled UC Value
		Function<LabeledIntEdge, String> edgeLabeledLCValueF = graphReader.getEdgeMetadata().get(CSTNUGraphMLWriter.EDGE_LABELED_LC_VALUE_KEY).transformer;
		if (edgeLabeledLCValueF == null)
			throw new IllegalArgumentException("The input file does not contain the meta declaration for lower case value. Please, fix it adding" +
					"<key id=\"LowerCaseLabeledValues\" for=\"edge\"> \n" +
					"<default></default> \n" +
					"</key>\n" +
					"before <graph> tag.");

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
			// Labeled UC Value
			data = edgeLabeledUCValueF.apply(e);
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
			// I parse also value parameter that was present in the first version of the graph file
			if (edgeOldValueF != null) {
				data = edgeOldValueF.apply(e);
				if (data != null && !data.isEmpty()) {
					e.putLabeledValue(Label.emptyLabel, Integer.parseInt(data));
				}
			}

		}
		return this.graph;
	}
}
