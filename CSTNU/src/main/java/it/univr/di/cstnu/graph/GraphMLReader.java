/**
 *
 */
package it.univr.di.cstnu.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.GraphReader;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.ExceptionConverter;
import edu.uci.ics.jung.io.graphml.GraphMLConstants;
import edu.uci.ics.jung.io.graphml.GraphMLDocument;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;
import edu.uci.ics.jung.io.graphml.Key;
import edu.uci.ics.jung.io.graphml.NodeMetadata;
import edu.uci.ics.jung.io.graphml.parser.ElementParserRegistry;
import edu.uci.ics.jung.io.graphml.parser.GraphMLEventFilter;
import it.univr.di.cstnu.graph.LabeledIntEdge.ConstraintType;
import it.univr.di.labeledvalue.ALabelAlphabet;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledContingentIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;

/**
 * <p>
 * GraphMLReader class.
 * </p>
 * * Allows to read a graph from a file written in GraphML format.<br>
 * GraphML format allows the definition of different attributes for the graph, vertices and edges.<br>
 * All attributes are defined in the first part of a GraphML file. Examples of GraphML file that can read by this class are given in the Instances directory
 * under CstnuTool one.
 * 
 * @author posenato
 * @version $Id: $Id
 * @param <G> type of graph
 */
public class GraphMLReader<G extends Hypergraph<LabeledNode, LabeledIntEdge>> implements GraphReader<G, LabeledNode, LabeledIntEdge> {
	// <G extends Hypergraph<V, E>, V, E> implements GraphReader<G, V, E>

	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static Logger LOG = Logger.getLogger(GraphMLReader.class.getName());

	/**
	 * ALabel alphabet
	 */
	ALabelAlphabet aLabelAlphabet;

	/**
	 * 
	 */
	final protected GraphMLDocument document = new GraphMLDocument();

	/**
	 * LabeledIntEdge transformer
	 */
	Transformer<EdgeMetadata, LabeledIntEdge> edgeTransformer = new Transformer<EdgeMetadata, LabeledIntEdge>() {
		Pattern pattern = Pattern.compile("^e[0-9]+$");

		@Override
		public LabeledIntEdge transform(final EdgeMetadata metaData) {
			// final boolean optimized =
			// Boolean.getBoolean(metaData.getProperty("Optimized"));
			LabeledIntEdgeFactory<? extends LabeledIntMap> edgeFactory = new LabeledIntEdgeFactory<>(GraphMLReader.this.mapTypeImplementation);
			final LabeledIntEdge e = edgeFactory.create();
			final String name = metaData.getId();
			e.setName(metaData.getId());
			if (this.pattern.matcher(name).matches()) {// check the LabeledIntEdge.getFactory(): there you can find/define the format for name create using the mouse
													// in the app.
				final int n = Integer.parseInt(name.substring(1));
				if (AbstractLabeledIntEdge.idSeq <= n) {
					AbstractLabeledIntEdge.idSeq = n + 1;
				}
			}
			e.setConstraintType(ConstraintType.valueOf(metaData.getProperty("Type")));

			String data = metaData.getProperty("LabeledValues");
			if (data != null) {
				LabeledIntMap map = AbstractLabeledIntMap.parse(data);
				if (data.length() > 2 && map == null) {
					throw new IllegalArgumentException("Labeled values in a wrong format: " + data + " in edge " + name);
				}
				e.setLabeledValue(map);
			}

			data = metaData.getProperty("LowerCaseLabeledValues");
			LabeledContingentIntTreeMap map1 = LabeledContingentIntTreeMap.parse(data, GraphMLReader.this.aLabelAlphabet);
			if (data != null && data.length() > 2 && map1 == null)
				throw new IllegalArgumentException("Lower Labeled values in a wrong format: " + data + " in edge " + name);
			e.setLabeledLowerCaseValue(map1);
			data = metaData.getProperty("UpperCaseLabeledValues");
			map1 = LabeledContingentIntTreeMap.parse(data, GraphMLReader.this.aLabelAlphabet);
			if (data != null && data.length() > 2 && map1 == null)
				throw new IllegalArgumentException("Upper Labeled values in a wrong format: " + data + " in edge " + name);
			e.setLabeledUpperCaseValue(map1);
			// I parse also value parameter that was present in the first version of the graph file
			String v = metaData.getProperty("Value");
			if (v != null && !v.isEmpty()) {
				// e.setInitialValue(Integer.parseInt(v));
				e.putLabeledValue(Label.emptyLabel, Integer.parseInt(v));
			}
			return e;
		}
	};

	/**
	 * 
	 */
	protected Reader fileReader;

	/**
	 * Just to satisfy requirements, a trivial LabeledIntGraph Transformer.
	 */
	protected Transformer<GraphMetadata, LabeledIntGraph> graphTransformer = new Transformer<GraphMetadata, LabeledIntGraph>() {
		public LabeledIntGraph transform(final GraphMetadata metaData) {
			final String name = metaData.getProperty("Name");
			// final boolean optimized = Boolean.getBoolean(metaData.getProperty("Optimized"));
			LabeledIntGraph graph = new LabeledIntGraph(name, GraphMLReader.this.mapTypeImplementation, GraphMLReader.this.aLabelAlphabet);
			return graph;
		}
	};
	/**
	 * HyperEdgeMetadata transformer that it is necessary to GraphMLreader2 but it not used.
	 */
	Transformer<HyperEdgeMetadata, LabeledIntEdge> hyperEdgeTransformer = new Transformer<HyperEdgeMetadata, LabeledIntEdge>() {
		@Override
		public LabeledIntEdge transform(final HyperEdgeMetadata metadata) {
			LabeledIntEdgeFactory<? extends LabeledIntMap> edgeFactory = new LabeledIntEdgeFactory<>(GraphMLReader.this.mapTypeImplementation);
			final LabeledIntEdge e = edgeFactory.create();
			return e;
		}
	};
	/**
	 * 
	 */
	protected boolean initialized;
	/**
	 * it is necessary for creating the right factory (reflection doesn't work due to reification!)
	 */
	Class<? extends LabeledIntMap> mapTypeImplementation;
	/**
	 * 
	 */
	final protected ElementParserRegistry<LabeledIntGraph, LabeledNode, LabeledIntEdge> parserRegistry;

	/**
	 * Vertex Transformer
	 */
	protected Transformer<NodeMetadata, LabeledNode> vertexTransformer = new Transformer<NodeMetadata, LabeledNode>() {
		Pattern pattern = Pattern.compile("^n[0-9]+$");

		@Override
		public LabeledNode transform(final NodeMetadata metaData) {
			final LabeledNode v = LabeledNode.getFactory().create();
			final String name = metaData.getId();
			v.setName(metaData.getId());
			if (this.pattern.matcher(name).matches()) {// check the LabeledNode.getFactory(): there you can find/define the format name create using the mouse in the
													// app.
				final int n = Integer.parseInt(name.substring(1));
				if (AbstractComponent.idSeq <= n) {
					AbstractComponent.idSeq = n + 1;
				}
			}
			String s = metaData.getProperty("Obs");
			if ((s != null) && (s.length() == 1)) {
				v.setObservable(s.charAt(0));
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
	 * 
	 */
	protected XMLEventReader xmlEventReader;
	/**
	 * @param graphFile
	 * @param labeledValueSetImplementationClass
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("resource")
	public GraphMLReader(final File graphFile, Class<? extends LabeledIntMap> labeledValueSetImplementationClass) throws FileNotFoundException {
		this(new FileReader(graphFile), labeledValueSetImplementationClass);
	}

	/**
	 * <p>
	 * Constructor for GraphMLReader.
	 * </p>
	 * Allows to read a graph from a file written in GraphML format.<br>
	 * GraphML format allows the definition of different attributes for the graph, vertices and edges.<br>
	 * All attributes are defined in the first part of a GraphML file. Examples of GraphML file that can read by this class are given in the Instances directory
	 * under CstnuTool one.
	 *
	 * @param fileReader a {@link java.io.FileReader} object.
	 * @param labeledValueSetImplementationClass it is necessary for creating the right factory (reflection doesn't work due to reification!)
	 */
	public GraphMLReader(final FileReader fileReader, Class<? extends LabeledIntMap> labeledValueSetImplementationClass) {
		this.fileReader = fileReader;
		this.mapTypeImplementation = labeledValueSetImplementationClass;
		this.aLabelAlphabet = new ALabelAlphabet();
		this.parserRegistry = new ElementParserRegistry<>(this.document.getKeyMap(), this.graphTransformer, this.vertexTransformer, this.edgeTransformer, this.hyperEdgeTransformer);
	}

	/**
	 * Closes the GraphML reader and disposes of any resources.
	 *
	 * @throws edu.uci.ics.jung.io.GraphIOException thrown if an error occurs.
	 */
	public void close() throws GraphIOException {
		try {
			// Clear the contents of the document.
			this.document.clear();
			if (this.xmlEventReader != null) {
				this.xmlEventReader.close();
			}
			if (this.fileReader != null) {
				this.fileReader.close();
			}
		} catch (IOException e) {
			throw new GraphIOException(e);
		} catch (XMLStreamException e) {
			throw new GraphIOException(e);
		} finally {
			this.fileReader = null;
			this.xmlEventReader = null;
			this.graphTransformer = null;
			this.vertexTransformer = null;
			this.edgeTransformer = null;
			this.hyperEdgeTransformer = null;
		}
	}

	/**
	 * Gets the current transformer that is being used for edge objects.
	 *
	 * @return the current transformer.
	 */
	public Transformer<EdgeMetadata, LabeledIntEdge> getEdgeTransformer() {
		return this.edgeTransformer;
	}

	/**
	 * Returns the object that contains the metadata read in from the GraphML document
	 *
	 * @return the GraphML document
	 */
	public GraphMLDocument getGraphMLDocument() {
		return this.document;
	}

	/**
	 * Gets the current transformer that is being used for graph objects.
	 *
	 * @return the current transformer.
	 */
	public Transformer<GraphMetadata, LabeledIntGraph> getGraphTransformer() {
		return this.graphTransformer;
	}

	/**
	 * Gets the current transformer that is being used for hyperedge objects.
	 *
	 * @return the current transformer.
	 */
	public Transformer<HyperEdgeMetadata, LabeledIntEdge> getHyperEdgeTransformer() {
		return this.hyperEdgeTransformer;
	}

	/**
	 * Gets the current transformer that is being used for vertex objects.
	 *
	 * @return the current transformer.
	 */
	public Transformer<NodeMetadata, LabeledNode> getVertexTransformer() {
		return this.vertexTransformer;
	}

	/**
	 * Verifies the object state and initializes this reader. All transformer properties must be set and be non-null or a <code>GraphReaderException
	 * </code> will be thrown. This method may be called more than once. Successive calls will have no effect.
	 *
	 * @throws edu.uci.ics.jung.io.GraphIOException thrown if an error occurred.
	 */
	public void init() throws GraphIOException {
		try {
			if (!this.initialized) {
				// Create the event reader.
				XMLInputFactory factory = XMLInputFactory.newInstance();
				this.xmlEventReader = factory.createXMLEventReader(this.fileReader);
				this.xmlEventReader = factory.createFilteredReader(this.xmlEventReader, new GraphMLEventFilter());
				this.initialized = true;
			}
		} catch (Exception e) {
			ExceptionConverter.convert(e);
		}
	}

	/**
	 * Reads a single graph object from the GraphML document. Automatically calls <code>init</code> to initialize the state of the reader.
	 *
	 * @return the graph that was read if one was found, otherwise null.
	 */
	@SuppressWarnings("unchecked")
	public G readGraph() throws GraphIOException {
		try {
			// Initialize if not already.
			init();
			while (this.xmlEventReader.hasNext()) {
				XMLEvent event = this.xmlEventReader.nextEvent();
				if (event.isStartElement()) {
					StartElement element = (StartElement) event;
					String name = element.getName().getLocalPart();
					// The element should be one of: key, graph, graphml
					if (GraphMLConstants.KEY_NAME.equals(name)) {
						// Parse the key object.
						Key key = (Key) this.parserRegistry.getParser(name).parse(this.xmlEventReader, element);
						// Add the key to the key map.
						this.document.getKeyMap().addKey(key);
					} else if (GraphMLConstants.GRAPH_NAME.equals(name)) {
						// Parse the graph.
						GraphMetadata graph = (GraphMetadata) this.parserRegistry.getParser(name).parse(this.xmlEventReader, element);

						// Add it to the graph metadata list.
						this.document.getGraphMetadata().add(graph);

						// Return the graph object.
						return (G) graph.getGraph();
					} else if (GraphMLConstants.GRAPHML_NAME.equals(name)) {
						// Ignore the graphML object.
					} else {
						// Encounted an unknown element - just skip by it.
						this.parserRegistry.getUnknownElementParser().parse(this.xmlEventReader, element);
					}
				} else if (event.isEndDocument()) {
					break;
				}
			}
		} catch (Exception e) {
			ExceptionConverter.convert(e);
		}
		// We didn't read anything from the document.
		throw new GraphIOException("Unable to read Graph from document - the document could be empty");
	}
}
