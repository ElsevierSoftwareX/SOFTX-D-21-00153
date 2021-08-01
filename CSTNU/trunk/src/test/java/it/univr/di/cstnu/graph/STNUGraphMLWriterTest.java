package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;

/**
 * @author posenato
 *
 */
public class STNUGraphMLWriterTest {

	private String fileName = "src/test/resources/testGraphML.stnu";

	private TNGraph<STNUEdge> g;

	/**
	 * @throws Exception  nope
	 */
	@Before
	public void setUp() throws Exception {
		this.g = new TNGraph<>(STNUEdgeInt.class);
		LabeledNode Z = this.g.getNodeFactory().get("Z");
		LabeledNode Ω = this.g.getNodeFactory().get("Ω");
		LabeledNode X = this.g.getNodeFactory().get("X");
		LabeledNode Y = this.g.getNodeFactory().get("Y");
		STNUEdge xy = new STNUEdgeInt("XY");
		xy.setLabeledValue(new ALetter("Y"), 2, false);
		xy.setConstraintType(ConstraintType.contingent);
		STNUEdge yx = new STNUEdgeInt("YX");
		yx.setLabeledValue(new ALetter("Y"), -5, true);
		yx.setConstraintType(ConstraintType.contingent);

		this.g.addVertex(Z);
		this.g.addVertex(Ω);
		this.g.addVertex(X);
		this.g.addVertex(Y);
		this.g.addEdge(xy, X, Y);
		this.g.addEdge(yx, Y, X);
	}

	/**
	 * @throws IOException  nope
	 */
	@Test
	public void testGraphMLWriterAbstractLayoutOfLabeledNodeLabeledIntEdge() throws IOException {
		final TNGraphMLWriter graphWriter = new TNGraphMLWriter(null);
		graphWriter.save(this.g, new File(this.fileName));
		try (BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(this.fileName), StandardCharsets.UTF_8))) { // don't use new
																																				// FileReader(this.fileName) because for Java 8 it does not accept "UTF-8"

			char[] fileAsChar = new char[4200];
			if (input.read(fileAsChar)==-1) {
				fail("Problem reading "+this.fileName);
			}
			String fileAsString = new String(fileAsChar);
			input.close();
			assertEquals(this.fileOk, fileAsString.trim());
		}
	}

	/**
	 *  nope
	 */
	@Test
	public void testGraphMLStringWriter() {
		final TNGraphMLWriter graphWriter = new TNGraphMLWriter(null);
		String graphXML = graphWriter.save(this.g).trim();
		assertEquals(this.fileOk, graphXML);
	}

	private String fileOk = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns/graphml\"\n" +
			"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  \n" +
			"xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns/graphml\">\n" +
			"<key id=\"nContingent\" for=\"graph\">\n" +
			"<desc>Number of contingents in the graph</desc>\n" +
			"<default>0</default>\n" +
			"</key>\n" +
			"<key id=\"nObservedProposition\" for=\"graph\">\n" +
			"<desc>Number of observed propositions in the graph</desc>\n" +
			"<default>0</default>\n" +
			"</key>\n" +
			"<key id=\"NetworkType\" for=\"graph\">\n" +
			"<desc>Network Type</desc>\n" +
			"<default>CSTNU</default>\n" +
			"</key>\n" +
			"<key id=\"nEdges\" for=\"graph\">\n" +
			"<desc>Number of edges in the graph</desc>\n" +
			"<default>0</default>\n" +
			"</key>\n" +
			"<key id=\"nVertices\" for=\"graph\">\n" +
			"<desc>Number of vertices in the graph</desc>\n" +
			"<default>0</default>\n" +
			"</key>\n" +
			"<key id=\"Name\" for=\"graph\">\n" +
			"<desc>Graph Name</desc>\n" +
			"<default></default>\n" +
			"</key>\n" +
			"<key id=\"Obs\" for=\"node\">\n" +
			"<desc>Proposition Observed. Value specification: [a-zA-F]</desc>\n" +
			"<default></default>\n" +
			"</key>\n" +
			"<key id=\"x\" for=\"node\">\n" +
			"<desc>The x coordinate for the visualitation. A positive value.</desc>\n" +
			"<default>0</default>\n" +
			"</key>\n" +
			"<key id=\"Label\" for=\"node\">\n" +
			"<desc>Label. Format: [¬[a-zA-F]|[a-zA-F]]+|⊡</desc>\n" +
			"<default>⊡</default>\n" +
			"</key>\n" +
			"<key id=\"y\" for=\"node\">\n" +
			"<desc>The y coordinate for the visualitation. A positive value.</desc>\n" +
			"<default>0</default>\n" +
			"</key>\n" +
			"<key id=\"Potential\" for=\"node\">\n" +
			"<desc>Labeled Potential Values. Format: {[('node name (no case modification)', 'integer', 'label') ]+}|{}</desc>\n" +
			"<default></default>\n" +
			"</key>\n" +
			"<key id=\"Type\" for=\"edge\">\n" +
			"<desc>Type: Possible values: normal|contingent|constraint|derived|internal.</desc>\n" +
			"<default>normal</default>\n" +
			"</key>\n" +
			"<key id=\"Value\" for=\"edge\">\n" +
			"<desc>Value for STN edge. Format: 'integer'</desc>\n" +
			"<default></default>\n" +
			"</key>\n" +
			"<key id=\"LabeledValue\" for=\"edge\">\n" +
			"<desc>Case Value. Format: 'LC(NodeName):integer' or 'UC(NodeName):integer'</desc>\n" +
			"<default></default>\n" +
			"</key>\n" +
			"<graph edgedefault=\"directed\">\n" +
			"<data key=\"nContingent\">1</data>\n" +
			"<data key=\"NetworkType\">STNU</data>\n" +
			"<data key=\"nEdges\">2</data>\n" +
			"<data key=\"nVertices\">4</data>\n" +
			"<node id=\"Z\">\n" +
			"<data key=\"x\">0.0</data>\n" +
			"<data key=\"y\">0.0</data>\n" +
			"</node>\n" +
			"<node id=\"X\">\n" +
			"<data key=\"x\">0.0</data>\n" +
			"<data key=\"y\">0.0</data>\n" +
			"</node>\n" +
			"<node id=\"Ω\">\n" +
			"<data key=\"x\">0.0</data>\n" +
			"<data key=\"y\">0.0</data>\n" +
			"</node>\n" +
			"<node id=\"Y\">\n" +
			"<data key=\"x\">0.0</data>\n" +
			"<data key=\"y\">0.0</data>\n" +
			"</node>\n" +
			"<edge id=\"XY\" source=\"X\" target=\"Y\">\n" +
			"<data key=\"Type\">contingent</data>\n" +
			"<data key=\"LabeledValue\">LC(Y):2</data>\n" +
			"</edge>\n" +
			"<edge id=\"YX\" source=\"Y\" target=\"X\">\n" +
			"<data key=\"Type\">contingent</data>\n" +
			"<data key=\"LabeledValue\">UC(Y):-5</data>\n" +
			"</edge>\n" +
			"</graph>\n" +
			"</graphml>";
}
