package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.junit.Test;

import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;

public class STNUGraphMLWriterTest {

	String fileName = "src/test/resources/testGraphML.stnu";

	@Test
	public void testGraphMLWriterAbstractLayoutOfLabeledNodeLabeledIntEdge() throws IOException {
		TNGraph<STNUEdge> g = new TNGraph<>(STNUEdgeInt.class);
		LabeledNode Z = g.getNodeFactory().get("Z");
		LabeledNode Ω = g.getNodeFactory().get("Ω");
		LabeledNode X = g.getNodeFactory().get("X");
		LabeledNode Y = g.getNodeFactory().get("Y");
		STNUEdge xy = new STNUEdgeInt("XY");
		xy.setLabeledValue(new ALetter("Y"), 2, false);
		xy.setConstraintType(ConstraintType.contingent);
		STNUEdge yx = new STNUEdgeInt("YX");
		yx.setLabeledValue(new ALetter("Y"), -5, true);
		yx.setConstraintType(ConstraintType.contingent);

		g.addVertex(Z);
		g.addVertex(Ω);
		g.addVertex(X);
		g.addVertex(Y);
		g.addEdge(xy, X, Y);
		g.addEdge(yx, Y, X);

		final TNGraphMLWriter graphWriter = new TNGraphMLWriter(null);
		try (Writer out = new PrintWriter(new BufferedWriter(new FileWriter(this.fileName)))) {
			graphWriter.save(g, out);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		try (FileReader input = new FileReader(this.fileName)) {
			char[] fileAsChar = new char[4200];
			input.read(fileAsChar);
			String fileAsString = new String(fileAsChar);
			input.close();
			assertEquals(this.fileOk, fileAsString.trim());
		}
	}

	String fileOk = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
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
