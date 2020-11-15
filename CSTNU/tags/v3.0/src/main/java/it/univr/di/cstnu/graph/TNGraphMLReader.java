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
 * Allows the reading of a Temporal Network (TM) graph from a file in GraphML format.<br>
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
	 * The result of the loading action.
	 */
	TNGraph<E> tnGraph;

	/**
	 * ALabel alphabet for UC a-labels
	 */
	private ALabelAlphabet aLabelAlphabet;
	/**
	 * 
	 */
	private Supplier<E> edgeFactory;

	/**
	 * Class for representing internal labeled values.
	 */
	private Class<? extends E> edgeImpl;

	/**
	 * Input file reader
	 */
	private Reader fileReader;

	/**
	 * 
	 */
	private Supplier<LabeledNode> nodeFactory;

	/**
	 * Allows to read a Temporal Network (TM) from a file written in GraphML format.<br>
	 * GraphML format allows the definition of different attributes for a TNGraph, vertices and edges.<br>
	 * All attributes are defined in the first part of a GraphML file. Examples of GraphML file that can read by this class are given in the Instances directory
	 * under CstnuTool one.
	 * 
	 * @param graphFile
	 * @param edgeImplClass
	 * @throws FileNotFoundException if the graphFile is not found
	 */
	public TNGraphMLReader(final File graphFile, Class<? extends E> edgeImplClass) throws FileNotFoundException {
		if (graphFile == null) {
			throw new FileNotFoundException("The given file does not exist.");
		}
		this.fileReader = new FileReader(graphFile);
		this.aLabelAlphabet = new ALabelAlphabet();
		this.edgeImpl = edgeImplClass;

		this.edgeFactory = new InternalEdgeFactory<>(this.edgeImpl);
		this.nodeFactory = new InternalVertexFactory();
		this.tnGraph = new TNGraph<>(this.edgeImpl, this.aLabelAlphabet);
		this.tnGraph.setInputFile(graphFile);
	}

	/**
	 * @return the graphML as TNGraph.
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public TNGraph<E> readGraph() throws IOException, ParserConfigurationException, SAXException {
		/*
		 * I use TNGraphMLReader instead of GraphMLReader2 because on 2017-11-01 I discovered that GraphMLReader2 does not allow to read
		 * edge attributes that are very long, like the labeled upper case values of a checked CSTNU.
		 * TNGraphMLReader is a little less intuitive but it manages all attributes in a right way!
		 */
		GraphMLReader<TNGraph<E>, LabeledNode, E> graphReader = new GraphMLReader<>(this.nodeFactory, this.edgeFactory);
		// populate the graph.
		graphReader.load(this.fileReader, this.tnGraph);

		// Now graph contains all vertices and edges with default names (the factory cannot set the right names).

		/*
		 * Node attribute setting!
		 */
		// Name
		graphReader.getVertexIDs().forEach(new BiConsumer<LabeledNode, String>() {
			@Override
			public void accept(LabeledNode n, String s) {
				n.setName(s);
				if (s.equals(AbstractCSTN.ZeroNodeName)) {
					TNGraphMLReader.this.tnGraph.setZ(n);
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

		for (LabeledNode n : this.tnGraph.getVertices()) {
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
					s = TNGraphMLReader.this.tnGraph.getSource(e).getName() + "_" + TNGraphMLReader.this.tnGraph.getDest(e).getName();
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
		boolean notCSTNUCSTNPSU = this.tnGraph.getType() == NetworkType.STN || this.tnGraph.getType() == NetworkType.CSTN
				|| this.tnGraph.getType() == NetworkType.STNU;
		for (E e : this.tnGraph.getEdges()) {
			// Type
			e.setConstraintType(ConstraintType.valueOf(edgeTypeF.apply(e)));
			// Labeled Value
			data = "";
			LabeledNode s = this.tnGraph.getSource(e);
			LabeledNode d = this.tnGraph.getDest(e);

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
					if (this.tnGraph.getType() == NetworkType.STN || this.tnGraph.getType() == NetworkType.STNU) {
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
					if (this.tnGraph.getType() == NetworkType.STNU) {
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
				this.tnGraph.removeEdge(e);
			}
		}
		if (notCSTNUCSTNPSU) {
			return this.tnGraph;
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
			return this.tnGraph;
		}
		Function<E, String> edgeLabeledUCValueF = edgeLabeledUCValueMD.transformer;
		Function<E, String> edgeLabeledLCValueF = edgeLabeledLCValueMD.transformer;

		for (E e1 : this.tnGraph.getEdges()) {
			BasicCSTNUEdge e = (BasicCSTNUEdge) e1;
			// Labeled UC Value
			data = edgeLabeledUCValueF.apply(e1);
			LabeledALabelIntTreeMap upperCaseMap = LabeledALabelIntTreeMap.parse(data, this.aLabelAlphabet);
			if (data != null && data.length() > 2 && (upperCaseMap == null || upperCaseMap.isEmpty()))
				throw new IllegalArgumentException("Upper Case values in a wrong format: " + data + " in edge " + e);
			if (upperCaseMap == null)
				upperCaseMap = new LabeledALabelIntTreeMap();
			e.setUpperCaseValueMap(upperCaseMap);
			// Labeled LC Value
			data = edgeLabeledLCValueF.apply(e1);
			if (this.tnGraph.getType() == NetworkType.CSTNU) {
				LabeledLowerCaseValue lowerCaseValue = LabeledLowerCaseValue.parse(data, this.aLabelAlphabet);
				if (data != null && data.length() > 2 && (lowerCaseValue == null || lowerCaseValue.isEmpty()))
					throw new IllegalArgumentException("Lower Case values in a wrong format: " + data + " in edge " + e);
				if (lowerCaseValue == null)
					lowerCaseValue = LabeledLowerCaseValue.emptyLabeledLowerCaseValue;
				((CSTNUEdge) e1).setLowerCaseValue(lowerCaseValue);
			}
			if (this.tnGraph.getType() == NetworkType.CSTNPSU) {
				LabeledALabelIntTreeMap lowerCaseValue = LabeledALabelIntTreeMap.parse(data, this.aLabelAlphabet);
				if (data != null && data.length() > 2 && (lowerCaseValue == null || lowerCaseValue.isEmpty()))
					throw new IllegalArgumentException("Lower Case values in a wrong format: " + data + " in edge " + e);
				if (lowerCaseValue == null)
					lowerCaseValue = new LabeledALabelIntTreeMap();
				((CSTNPSUEdge) e1).setLowerCaseValue(lowerCaseValue);
			}

			if (e.isEmpty()) {
				this.tnGraph.removeEdge(e1);
			}
		}

		return this.tnGraph;
	}
}
