/**
 *
 */
package it.univr.di.cstnu.graph;

import it.univr.di.cstnu.graph.LabeledIntEdge.Type;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledContingentIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntNodeSetTreeMap;
import it.univr.di.labeledvalue.Literal;

import java.io.FileReader;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;
import edu.uci.ics.jung.io.graphml.NodeMetadata;

/**
 * @author posenato
 */
public class GraphMLReader extends edu.uci.ics.jung.io.graphml.GraphMLReader2<LabeledIntGraph, LabeledNode, LabeledIntEdge> {

	/**
	 * Just to satisfy requirements, a trivial LabeledIntGraph Transformer.
	 */
	static Transformer<GraphMetadata, LabeledIntGraph> graphTransformer = new Transformer<GraphMetadata, LabeledIntGraph>() {
		@Override
		public LabeledIntGraph transform(final GraphMetadata metaData) {
			final String name = metaData.getProperty("Name");
			final boolean optimized = Boolean.getBoolean(metaData.getProperty("Optimized"));
			return new LabeledIntGraph(name, optimized);
		}
	};

	/**
	 * Vertex Transformer
	 */
	static Transformer<NodeMetadata, LabeledNode> vertexTransformer = new Transformer<NodeMetadata, LabeledNode>() {
		@Override
		public LabeledNode transform(final NodeMetadata metaData) {
			final LabeledNode v = LabeledNode.getFactory().create();
			final String name = metaData.getId();
			v.setName(metaData.getId());
			if (name.matches("^n[0-9]+$")) {// check the LabeledNode.getFactory(): there you can find/define the format for name create using the mouse in the
											// app.
				final int n = Integer.parseInt(name.substring(1));
				if (LabeledNode.idSeq <= n) {
					LabeledNode.idSeq = n + 1;
				}
			}
			String s = metaData.getProperty("Obs");
			if ((s != null) && (s.length() > 0)) {
				v.setObservable(Literal.parse(s));
			}
			v.setX(Double.parseDouble(metaData.getProperty("x")));
			v.setY(Double.parseDouble(metaData.getProperty("y")));
			s = metaData.getProperty("Label");
			if ((s != null) && (s.length() > 0)) {
				v.setLabel(Label.parse(s));
			}
			return v;
		}
	};

	/**
	 * LabeledIntEdge transformer
	 */
	static Transformer<EdgeMetadata, LabeledIntEdge> edgeTransformer = new Transformer<EdgeMetadata, LabeledIntEdge>() {
		@Override
		public LabeledIntEdge transform(final EdgeMetadata metaData) {
			final boolean optimized = Boolean.getBoolean(metaData.getProperty("Optimized"));
			final LabeledIntEdge e = new LabeledIntEdge(optimized); // .getFactory().create(optimized);
			final String name = metaData.getId();
			e.setName(metaData.getId());
			if (name.matches("^e[0-9]+$")) {// check the LabeledIntEdge.getFactory(): there you can find/define the format for name create using the mouse in
											// the app.
				final int n = Integer.parseInt(name.substring(1));
				if (LabeledIntEdge.idSeq <= n) {
					LabeledIntEdge.idSeq = n + 1;
				}
			}
			e.setType(Type.valueOf(metaData.getProperty("Type")));
			// String v = metaData.getProperty("Value");
			// if (v == null || v.isEmpty())
			// e.setInitialValue(null);
			// else
			// e.setInitialValue(Integer.parseInt(v));
			e.setLabeledValue(LabeledIntNodeSetTreeMap.parse(metaData.getProperty("LabeledValues"), optimized));
			e.setLabeledLowerCaseValue(LabeledContingentIntTreeMap.parse(metaData.getProperty("LowerCaseLabeledValues"), optimized));
			e.setLabeledUpperCaseValue(LabeledContingentIntTreeMap.parse(metaData.getProperty("UpperCaseLabeledValues"), optimized));
			return e;
		}
	};

	/**
	 * HyperEdgeMetadata transformer that it is necessary to GraphMLreader2 but it not used.
	 */
	static Transformer<HyperEdgeMetadata, LabeledIntEdge> hyperEdgeTransformer = new Transformer<HyperEdgeMetadata, LabeledIntEdge>() {
		@Override
		public LabeledIntEdge transform(final HyperEdgeMetadata metadata) {
			final LabeledIntEdge e = LabeledIntEdge.getFactory().create();
			return e;
		}
	};

	/**
	 * @param fileReader
	 */
	public GraphMLReader(final FileReader fileReader) {
		super(fileReader, GraphMLReader.graphTransformer, GraphMLReader.vertexTransformer,
				GraphMLReader.edgeTransformer, GraphMLReader.hyperEdgeTransformer);
	}
}
