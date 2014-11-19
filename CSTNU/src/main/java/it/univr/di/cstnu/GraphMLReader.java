/**
 * 
 */
package it.univr.di.cstnu;

import it.univr.di.cstnu.Edge.Type;

import java.io.FileReader;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;
import edu.uci.ics.jung.io.graphml.NodeMetadata;

/**
 * @author posenato
 */
public class GraphMLReader extends edu.uci.ics.jung.io.graphml.GraphMLReader2<Graph, Node, Edge> {

	/**
	 * Just to satisfy requirements, a trivial Graph Transformer.
	 */
	static Transformer<GraphMetadata, Graph> graphTransformer = new Transformer<GraphMetadata, Graph>() {
		@Override
		public Graph transform(GraphMetadata metaData) {
			final String name = metaData.getProperty("Name");
			return new Graph(name);
		}
	};

	/**
	 * Vertex Transformer
	 */
	static Transformer<NodeMetadata, Node> vertexTransformer = new Transformer<NodeMetadata, Node>() {
		@Override
		public Node transform(NodeMetadata metaData) {
			final Node v = Node.getFactory().create();
			String name = metaData.getId();
			v.setName(metaData.getId());
			if (name.matches("^n[0-9]+$")) {//check the Node.getFactory(): there you can find/define the format for name create using the mouse in the app.  
				int n = Integer.parseInt(name.substring(1));
				if (Node.idSeq <= n) Node.idSeq = n + 1;
			}
			String s = metaData.getProperty("Obs");
			if ((s != null) && (s.length() > 0)) v.setObservable(new Literal(s));
			v.setX(Double.parseDouble(metaData.getProperty("x")));
			v.setY(Double.parseDouble(metaData.getProperty("y")));
			s = metaData.getProperty("Label");
			if ((s != null) && (s.length() > 0)) v.setLabel(Label.parse(s));
			return v;
		}
	};

	/**
	 * Edge transformer
	 */
	static Transformer<EdgeMetadata, Edge> edgeTransformer = new Transformer<EdgeMetadata, Edge>() {
		@Override
		public Edge transform(EdgeMetadata metaData) {
			final Edge e = Edge.getFactory().create();
			String name = metaData.getId();
			e.setName(metaData.getId());
			if (name.matches("^e[0-9]+$")) {//check the Edge.getFactory(): there you can find/define the format for name create using the mouse in the app. 
				int n = Integer.parseInt(name.substring(1));
				if (Edge.idSeq <= n) Edge.idSeq = n + 1;
			}
			e.setType(Type.valueOf(metaData.getProperty("Type")));
			String v = metaData.getProperty("Value");
			if (v == null || v.isEmpty())
				e.setInitialValue(null);
			else
				e.setInitialValue(Integer.parseInt(v));
			e.setLabeledValue(LabeledValueMap.parse(metaData.getProperty("LabeledValues")));
			e.setLabeledLowerCaseValue(LabeledCaseCValueMap.parse(metaData.getProperty("LowerCaseLabeledValues")));
			e.setLabeledUpperCaseValue(LabeledCaseCValueMap.parse(metaData.getProperty("UpperCaseLabeledValues")));
			return e;
		}
	};

	/**
	 * HyperEdgeMetadata transformer that it is necessary to GraphMLreader2 but it not used.
	 */
	static Transformer<HyperEdgeMetadata, Edge> hyperEdgeTransformer = new Transformer<HyperEdgeMetadata, Edge>() {
		@Override
		public Edge transform(HyperEdgeMetadata metadata) {
			final Edge e = Edge.getFactory().create();
			return e;
		}
	};

	/**
	 * @param fileReader
	 */
	public GraphMLReader(FileReader fileReader) {
		super(fileReader, GraphMLReader.graphTransformer, GraphMLReader.vertexTransformer,
				GraphMLReader.edgeTransformer, GraphMLReader.hyperEdgeTransformer);
	}
}
