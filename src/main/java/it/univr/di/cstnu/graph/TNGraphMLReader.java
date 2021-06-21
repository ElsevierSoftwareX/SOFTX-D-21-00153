// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 *
 */
package it.univr.di.cstnu.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
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
import it.univr.di.cstnu.algorithms.AbstractCSTN;
import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.cstnu.graph.TNGraph.NetworkType;
import it.univr.di.labeledvalue.ALabelAlphabet;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntMapSupplier;
import it.univr.di.labeledvalue.LabeledLowerCaseValue;

/**
 * Allows the reading of a Temporal Network (TM) graph from a file or a string in GraphML format.<br>
 * GraphML format allows the definition of different attributes for the graph, vertices and edges.<br>
 * All attributes are defined in the first part of a GraphML file. Examples of GraphML file that can read by this class are given in the Instances directory
 * under CstnuTool one.
 *
 * @author posenato
 * @version $Id: $Id
 * @param <E> the type of edge
 */
public class TNGraphMLReader<E extends Edge> {
	/**
	 * Since we want to preserve edge names given by in the file and such name can conflict with the ones given by the standard edgeFactory,
	 * we modify the standard factory altering the default name
	 * 
	 * @author posenato
	 * @param <E>
	 */
	static private class InternalEdgeFactory<E extends Edge> implements Supplier<E> {
		/**
		 * 
		 */
		Supplier<E> edgeFactory;

		/**
		 * @param edgeImpl
		 */
		public <E1 extends E> InternalEdgeFactory(Class<E1> edgeImpl) {
			super();
			this.edgeFactory = new EdgeSupplier<>(edgeImpl);
		}

		@Override
		public E get() {
			E e = this.edgeFactory.get();
			e.setName(prefix + e.getName());
			return e;
		}
	}

	/**
	 * Since we want to preserve edge names given by in the file and such name can conflict with the ones given by the standard edgeFactory,
	 * we modify the standard factory altering the default name
	 * 
	 * @author posenato
	 */
	static private class InternalVertexFactory implements Supplier<LabeledNode> {

		/**
		 * 
		 */
		Supplier<LabeledNode> nodeFactory;

		/**
		 */
		public InternalVertexFactory() {
			super();
			this.nodeFactory = new LabeledNodeSupplier();
		}

		@Override
		public LabeledNode get() {
			LabeledNode e = this.nodeFactory.get();
			e.setName(prefix + e.getName());
			return e;
		}
	}

	/**
	 * Labeled value class used in the class.
	 */
	public static final Class<? extends LabeledIntMap> labeledValueMapImpl = LabeledIntMapSupplier.DEFAULT_LABELEDINTMAP_CLASS;

	/**
	 * logger
	 */
	static final Logger LOG = Logger.getLogger("it.univr.di.cstnu.graph.TNGraphMLReader");

	/**
	 * 
	 */
	static final String prefix = "__";

	/**
	 * A TNGraphMLReader object can be now used many times for reading different graphs.
	 */
	public TNGraphMLReader() {

	}

	/**
	 * Reads graphXML and returns the corresponding graph as a TNGraph object. Edges of TNGraph are created using the edgeImplClass.
	 * In this way, such a reader can create more kinds of TNGraph according to the given type of edge.
	 * 
	 * @param graphXML a string representing the graph in GraphML format.
	 * @param edgeImplClass the type for the edges of the graph.
	 * @return the graphML as TNGraph.
	 * @throws IOException if any error occurs during the graphXML reading
	 * @throws ParserConfigurationException if graphXML contains character that cannot be parsed
	 * @throws SAXException if graphXML is not valid
	 */
	public TNGraph<E> readGraph(final String graphXML, Class<? extends E> edgeImplClass) throws IOException, ParserConfigurationException, SAXException {
		if (graphXML == null || graphXML.isEmpty()) {
			throw new IllegalArgumentException("The given input is null or empty.");
		}
		Reader fileReader = new StringReader(graphXML);
		TNGraph<E> tnGraph = this.load(fileReader, edgeImplClass);
		return tnGraph;
	}

	/**
	 *  Reads graphFile and returns the corresponding graph as a TNGraph object. Edges of TNGraph are created using the edgeImplClass.
	 * In this way, such a reader can create more kinds of TNGraph according to the given type of edge.

	 * @param graphFile file containing the graph in GraphML format.
	 * @param edgeImplClass the type for the edges of the graph.
	 * @return the graphML as TNGraph.
	 * @throws IOException if any error occurs during the graphFile reading.
	 * @throws ParserConfigurationException if graphXML contains character that cannot be parsed
	 * @throws SAXException if graphFile does not containt a valid GraphML instance.
	 */
	public TNGraph<E> readGraph(final File graphFile, Class<? extends E> edgeImplClass) throws IOException, ParserConfigurationException, SAXException {
		try (Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(graphFile), "UTF8"))) {
			TNGraph<E> tnGraph = this.load(fileReader, edgeImplClass);
			tnGraph.setInputFile(graphFile);
			return tnGraph;
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			throw new FileNotFoundException("There is a problem to read the file containing the network. Details: " + e.getMessage());
		}
	}

	/**
	 * Creates the graph object using the given reader for acquiring the input.
	 * 
	 * @param reader
	 * @param edgeImplClass
	 * @return the graphML as TNGraph.
	 * @throws java.io.IOException
	 * @throws org.xml.sax.SAXException
	 * @throws javax.xml.parsers.ParserConfigurationException
	 */
	TNGraph<E> load(Reader reader, Class<? extends E> edgeImplClass) throws IOException, ParserConfigurationException, SAXException {
		ALabelAlphabet aLabelAlphabet = new ALabelAlphabet();
		Supplier<E> edgeFactory = new InternalEdgeFactory<>(edgeImplClass);
		Supplier<LabeledNode> nodeFactory = new InternalVertexFactory();
		TNGraph<E> tnGraph = new TNGraph<>(edgeImplClass, aLabelAlphabet);

		/*
		 * I use TNGraphMLReader instead of GraphMLReader2 because on 2017-11-01 I discovered that GraphMLReader2 does not allow to read
		 * edge attributes that are very long, like the labeled upper case values of a checked CSTNU.
		 * TNGraphMLReader is a little less intuitive but it manages all attributes in a right way!
		 */
		GraphMLReader<TNGraph<E>, LabeledNode, E> graphReader = new GraphMLReader<>(nodeFactory, edgeFactory);
		// populate the graph.
		graphReader.load(reader, tnGraph);

		// Now graph contains all vertices and edges with default names (the factory cannot set the right names).

		/*
		 * Node attribute setting!
		 */
		// Name
		graphReader.getVertexIDs().forEach(new BiConsumer<LabeledNode, String>() {
			@Override
			public void accept(LabeledNode n, String s) {
				n.setName(s);
				if (s.equals(AbstractCSTN.ZERO_NODE_NAME)) {
					// TNGraphMLReader.tnGraph.setZ(n);
					tnGraph.setZ(n);
				}
			}
		});
		// Label
		Function<LabeledNode, String> nodeLabelF = graphReader.getVertexMetadata().get(TNGraphMLWriter.NODE_LABEL_KEY).transformer;
		// Observed proposition
		Function<LabeledNode, String> nodeObservedPropF = graphReader.getVertexMetadata().get(TNGraphMLWriter.NODE_OBSERVED_KEY).transformer;
		// X position
		Function<LabeledNode, String> nodeXF = graphReader.getVertexMetadata().get(TNGraphMLWriter.NODE_X_KEY).transformer;
		// Y position
		Function<LabeledNode, String> nodeYF = graphReader.getVertexMetadata().get(TNGraphMLWriter.NODE_Y_KEY).transformer;
		// Potential
		GraphMLMetadata<LabeledNode> nodeLabeledPotentialValueMD = graphReader.getVertexMetadata().get(TNGraphMLWriter.NODE_POTENTIAL_KEY);
		Function<LabeledNode, String> nodeLabeledPotentialValueF = (nodeLabeledPotentialValueMD != null) ? nodeLabeledPotentialValueMD.transformer : null;

		for (LabeledNode n : tnGraph.getVertices()) {
			n.setLabel(Label.parse(nodeLabelF.apply(n)));
			String s = nodeObservedPropF.apply(n);
			if ((s != null) && (s.length() == 1)) {
				n.setObservable(s.charAt(0));
			}

			if (nodeLabeledPotentialValueF != null) {
				String data = nodeLabeledPotentialValueF.apply(n);
				LabeledIntMap potentialMap = AbstractLabeledIntMap.parse(data, labeledValueMapImpl);
				if (data != null && data.length() > 2 && (potentialMap == null || potentialMap.isEmpty()))
					throw new IllegalArgumentException("Potential values in a wrong format: " + data + " in node " + n);
				n.setLabeledPotential(potentialMap);
			}
			n.setX(Double.parseDouble(nodeXF.apply(n)));
			n.setY(Double.parseDouble(nodeYF.apply(n)));
		}

		/*
		 * Edge attribute setting!
		 */
		// Name
		graphReader.getEdgeIDs().forEach(new BiConsumer<E, String>() {
			@Override
			public void accept(E e, String s) {
				e.setName(s);
				if (!e.getName().equals(s)) {
					// there is a problem that the name has been already used...
					// s = TNGraphMLReader.tnGraph.getSource(e).getName() + "_" + TNGraphMLReader.tnGraph.getDest(e).getName();
					s = tnGraph.getSource(e).getName() + "_" + tnGraph.getDest(e).getName();
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
		Function<E, String> edgeTypeF = graphReader.getEdgeMetadata().get(TNGraphMLWriter.EDGE_TYPE_KEY).transformer;
		// Labeled Value
		// For STN graph it can be not present.
		GraphMLMetadata<E> fieldReader = graphReader.getEdgeMetadata().get(TNGraphMLWriter.EDGE_LABELED_VALUE_KEY);
		Function<E, String> edgeLabeledValueF = (fieldReader != null) ? fieldReader.transformer : null;
		// I parse also value parameter that was present in the first version of the graph file
		fieldReader = graphReader.getEdgeMetadata().get(TNGraphMLWriter.EDGE_VALUE_KEY);
		Function<E, String> edgeValueF = (fieldReader != null) ? fieldReader.transformer : null;

		// STNU
		fieldReader = graphReader.getEdgeMetadata().get(TNGraphMLWriter.EDGE_CASE_VALUE_KEY);
		Function<E, String> edgeCaseValueF = (fieldReader != null) ? fieldReader.transformer : null;

		LabeledIntMapSupplier<? extends LabeledIntMap> LabIntMapSupplier = new LabeledIntMapSupplier<>(labeledValueMapImpl);
		String data;
		boolean notCSTNUCSTNPSU = tnGraph.getType() == NetworkType.STN || tnGraph.getType() == NetworkType.CSTN
				|| tnGraph.getType() == NetworkType.STNU;
		for (E e : tnGraph.getEdges()) {
			// Type
			e.setConstraintType(ConstraintType.valueOf(edgeTypeF.apply(e)));
			// Labeled Value
			data = "";
			LabeledNode s = tnGraph.getSource(e);
			LabeledNode d = tnGraph.getDest(e);

			boolean containsLabeledValues = CSTNEdge.class.isAssignableFrom(e.getClass());
			if (edgeLabeledValueF != null) {
				data = edgeLabeledValueF.apply(e);
				if (data != null && !data.isEmpty()) {
					LabeledIntMap map = LabIntMapSupplier.get((AbstractLabeledIntMap.parse(data)));
					if (data.length() > 2 && map == null) {
						throw new IllegalArgumentException("Labeled values in a wrong format: " + data + " in edge " + e);
					}
					if (!containsLabeledValues) {
						throw new IllegalArgumentException(
								"Labeled value set is present but it cannot be stored because edge type is not a CSTN o derived type: " + data);
					}
					((CSTNEdge) e).setLabeledValueMap(map);
				}
			}
			// I parse also value parameter that was present in the first version of the graph file or in STN graph
			if (edgeValueF != null) {
				data = edgeValueF.apply(e);
				if (data != null && !data.isEmpty()) {
					if (tnGraph.getType() == NetworkType.STN || tnGraph.getType() == NetworkType.STNU) {
						((STNEdge) e).setValue(Integer.parseInt(data));
						if (e.getConstraintType() == ConstraintType.contingent) {
							STNUEdge e1 = (STNUEdge) e;
							if (e1.getValue() <= 0) {
								s.setContingent(true);
							} else {
								d.setContingent(true);
							}
						}
					}
					if (containsLabeledValues) {
						if (((CSTNEdge) e).getLabeledValueMap().isEmpty()) {
							LabeledIntMap map = LabIntMapSupplier.get();
							map.put(Label.emptyLabel, Integer.parseInt(data));
							((CSTNEdge) e).setLabeledValueMap(map);
						}
					}
				}
			}
			// STNU
			if (edgeCaseValueF != null) {
				data = edgeCaseValueF.apply(e);
				if (data != null && !data.isEmpty()) {
					if (tnGraph.getType() == NetworkType.STNU) {
						STNUEdge e1 = ((STNUEdge) e);
						e1.setLabeledValue(data);
						if (e1.isUpperCase()) {
							s.setContingent(true);
						} else {
							d.setContingent(true);
						}
					}
				}
			}
			if (e.isEmpty() && notCSTNUCSTNPSU) {
				tnGraph.removeEdge(e);
			}
		}
		if (notCSTNUCSTNPSU) {
			return tnGraph;
		}
		// FROM HERE the graph is assumed to be a CSTNU or CSTNPSU graph!

		GraphMLMetadata<E> edgeLabeledUCValueMD = graphReader.getEdgeMetadata().get(TNGraphMLWriter.EDGE_LABELED_UC_VALUE_KEY);
		GraphMLMetadata<E> edgeLabeledLCValueMD = graphReader.getEdgeMetadata().get(TNGraphMLWriter.EDGE_LABELED_LC_VALUE_KEY);
		if (edgeLabeledUCValueMD == null || edgeLabeledLCValueMD == null) {
			// TNGraph file is still in old format!
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
			return tnGraph;
		}
		Function<E, String> edgeLabeledUCValueF = edgeLabeledUCValueMD.transformer;
		Function<E, String> edgeLabeledLCValueF = edgeLabeledLCValueMD.transformer;

		for (E e1 : tnGraph.getEdges()) {
			BasicCSTNUEdge e = (BasicCSTNUEdge) e1;
			// Labeled UC Value
			data = edgeLabeledUCValueF.apply(e1);
			LabeledALabelIntTreeMap upperCaseMap = LabeledALabelIntTreeMap.parse(data, aLabelAlphabet);
			if (data != null && data.length() > 2 && (upperCaseMap == null || upperCaseMap.isEmpty()))
				throw new IllegalArgumentException("Upper Case values in a wrong format: " + data + " in edge " + e);
			if (upperCaseMap == null)
				upperCaseMap = new LabeledALabelIntTreeMap();
			e.setUpperCaseValueMap(upperCaseMap);
			// Labeled LC Value
			data = edgeLabeledLCValueF.apply(e1);
			if (tnGraph.getType() == NetworkType.CSTNU) {
				LabeledLowerCaseValue lowerCaseValue = LabeledLowerCaseValue.parse(data, aLabelAlphabet);
				if (data != null && data.length() > 2 && (lowerCaseValue == null || lowerCaseValue.isEmpty()))
					throw new IllegalArgumentException("Lower Case values in a wrong format: " + data + " in edge " + e);
				if (lowerCaseValue == null)
					lowerCaseValue = LabeledLowerCaseValue.emptyLabeledLowerCaseValue;
				((CSTNUEdge) e1).setLowerCaseValue(lowerCaseValue);
			}
			if (tnGraph.getType() == NetworkType.CSTNPSU) {
				LabeledALabelIntTreeMap lowerCaseValue = LabeledALabelIntTreeMap.parse(data, aLabelAlphabet);
				if (data != null && data.length() > 2 && (lowerCaseValue == null || lowerCaseValue.isEmpty()))
					throw new IllegalArgumentException("Lower Case values in a wrong format: " + data + " in edge " + e);
				if (lowerCaseValue == null)
					lowerCaseValue = new LabeledALabelIntTreeMap();
				((CSTNPSUEdge) e1).setLowerCaseValue(lowerCaseValue);
			}

			if (e.isEmpty()) {
				tnGraph.removeEdge(e1);
			}
		}

		return tnGraph;
	}
}
