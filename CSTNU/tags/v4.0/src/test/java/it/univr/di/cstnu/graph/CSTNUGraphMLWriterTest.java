package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.Literal;
/**
 * test for  CSTNUGraphMLWriter
 * @author posenato
 */
public class CSTNUGraphMLWriterTest {

	private String fileName = "src/test/resources/testGraphML.cstnu";

	/**
	 * testGraphMLWriterAbstractLayoutOfLabeledNodeLabeledIntEdge
	 * 
	 * @throws IOException clear
	 */
	@Test
	public void testGraphMLWriterAbstractLayoutOfLabeledNodeLabeledIntEdge() throws IOException {
		Label p = Label.valueOf('p', Literal.NEGATED);

		TNGraph<CSTNUEdge> g = new TNGraph<>(CSTNUEdgePluggable.class);
		LabeledNode Z = g.getNodeFactory().get("Z");
		LabeledNode Ω = g.getNodeFactory().get("Ω");
		LabeledNode X = g.getNodeFactory().get("X");
		LabeledNode Y = g.getNodeFactory().get("Y");
		Y.setObservable('p');
		X.setLabel(p);
		CSTNUEdge xy = new CSTNUEdgePluggable("XY");
		xy.setLowerCaseValue(p, new ALabel("X", g.getALabelAlphabet()), 2);
		xy.setConstraintType(ConstraintType.contingent);
		CSTNUEdge yx = new CSTNUEdgePluggable("YX");
		LabeledALabelIntTreeMap uc = new LabeledALabelIntTreeMap();
		uc.mergeTriple(p, new ALabel("X", g.getALabelAlphabet()), -5);
		yx.setUpperCaseValueMap(uc);
		yx.setConstraintType(ConstraintType.contingent);

		g.addVertex(Z);
		g.addVertex(Ω);
		g.addVertex(X);
		g.addVertex(Y);
		g.addEdge(xy, X, Y);
		g.addEdge(yx, Y, X);

		// A test should not depend on other class! :-)
		// CSTNU cstnu = new CSTNU(g);
		// cstnu.initAndCheck();
		final TNGraphMLWriter graphWriter = new TNGraphMLWriter(null);
		graphWriter.save(g, new File(this.fileName));
		try (BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(this.fileName), StandardCharsets.UTF_8))) { // don't use new FileReader(this.fileName)
																														// because for Java 8 it does not accept "UTF-8"
			char[] fileAsChar = new char[4200];
			if (input.read(fileAsChar)==-1) {
				fail("Problem reading "+ this.fileName);
			}
			String fileAsString = new String(fileAsChar);
			input.close();
			assertEquals(this.fileOk, fileAsString.trim());
		}
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
			"<key id=\"LowerCaseLabeledValues\" for=\"edge\">\n" +
			"<desc>Labeled Lower-Case Values. Format: {[('node name (no case modification)', 'integer', 'label') ]+}|{}</desc>\n" +
			"<default></default>\n" +
			"</key>\n" +
			"<key id=\"UpperCaseLabeledValues\" for=\"edge\">\n" +
			"<desc>Labeled Upper-Case Values. Format: {[('node name (no case modification)', 'integer', 'label') ]+}|{}</desc>\n" +
			"<default></default>\n" +
			"</key>\n" +
			"<key id=\"Value\" for=\"edge\">\n" +
			"<desc>Value for STN edge. Format: 'integer'</desc>\n" +
			"<default></default>\n" +
			"</key>\n" +
			"<key id=\"LabeledValues\" for=\"edge\">\n" +
			"<desc>Labeled Values. Format: {[('integer', 'label') ]+}|{}</desc>\n" +
			"<default></default>\n" +
			"</key>\n" +
			"<graph edgedefault=\"directed\">\n" +
			"<data key=\"nContingent\">1</data>\n" +
			"<data key=\"nObservedProposition\">1</data>\n" +
			"<data key=\"NetworkType\">CSTNU</data>\n" +
			"<data key=\"nEdges\">2</data>\n" +
			"<data key=\"nVertices\">4</data>\n" +
			"<node id=\"Z\">\n" +
			"<data key=\"x\">0.0</data>\n" +
			"<data key=\"Label\">⊡</data>\n" +
			"<data key=\"y\">0.0</data>\n" +
			"</node>\n" +
			"<node id=\"X\">\n" +
			"<data key=\"x\">0.0</data>\n" +
			"<data key=\"Label\">¬p</data>\n" +
			"<data key=\"y\">0.0</data>\n" +
			"</node>\n" +
			"<node id=\"Ω\">\n" +
			"<data key=\"x\">0.0</data>\n" +
			"<data key=\"Label\">⊡</data>\n" +
			"<data key=\"y\">0.0</data>\n" +
			"</node>\n" +
			"<node id=\"Y\">\n" +
			"<data key=\"Obs\">p</data>\n" +
			"<data key=\"x\">0.0</data>\n" +
			"<data key=\"Label\">⊡</data>\n" +
			"<data key=\"y\">0.0</data>\n" +
			"</node>\n" +
			"<edge id=\"XY\" source=\"X\" target=\"Y\">\n" +
			"<data key=\"Type\">contingent</data>\n" +
			"<data key=\"LowerCaseLabeledValues\">{(X, 2, ¬p) }</data>\n" +
			"<data key=\"LabeledValues\">{}</data>\n" +
			"</edge>\n" +
			"<edge id=\"YX\" source=\"Y\" target=\"X\">\n" +
			"<data key=\"Type\">contingent</data>\n" +
			"<data key=\"UpperCaseLabeledValues\">{(X, -5, ¬p) }</data>\n" +
			"<data key=\"LabeledValues\">{}</data>\n" +
			"</edge>\n" +
			"</graph>\n" +
			"</graphml>";

	// If the network is checked, then the result should be the following
	// String fileOk = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
	// "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns/graphml\"\n" +
	// "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
	// "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns/graphml\">\n" +
	// "<key id=\"nContingent\" for=\"graph\">\n" +
	// "<desc>Number of contingents in the graph</desc>\n" +
	// "<default>0</default>\n" +
	// "</key>\n" +
	// "<key id=\"nObservedProposition\" for=\"graph\">\n" +
	// "<desc>Number of observed propositions in the graph</desc>\n" +
	// "<default>0</default>\n" +
	// "</key>\n" +
	// "<key id=\"nEdges\" for=\"graph\">\n" +
	// "<desc>Number of edges in the graph</desc>\n" +
	// "<default>0</default>\n" +
	// "</key>\n" +
	// "<key id=\"nVertices\" for=\"graph\">\n" +
	// "<desc>Number of vertices in the graph</desc>\n" +
	// "<default>0</default>\n" +
	// "</key>\n" +
	// "<key id=\"Name\" for=\"graph\">\n" +
	// "<desc>Graph Name</desc>\n" +
	// "<default></default>\n" +
	// "</key>\n" +
	// "<key id=\"Obs\" for=\"node\">\n" +
	// "<desc>Proposition Observed. Value specification: [a-zA-F]</desc>\n" +
	// "<default></default>\n" +
	// "</key>\n" +
	// "<key id=\"x\" for=\"node\">\n" +
	// "<desc>The x coordinate for the visualitation. A positive value.</desc>\n" +
	// "<default>0</default>\n" +
	// "</key>\n" +
	// "<key id=\"Label\" for=\"node\">\n" +
	// "<desc>Label. Format: [¬[a-zA-F]|[a-zA-F]]+|⊡</desc>\n" +
	// "<default>⊡</default>\n" +
	// "</key>\n" +
	// "<key id=\"y\" for=\"node\">\n" +
	// "<desc>The y coordinate for the visualitation. A positive value.</desc>\n" +
	// "<default>0</default>\n" +
	// "</key>\n" +
	// "<key id=\"Potential\" for=\"node\">\n" +
	// "<desc>Labeled Potential Values. Format: {[('node name (no case modification)', 'integer', 'label') ]+}|{}</desc>\n" +
	// "<default></default>\n" +
	// "</key>\n" +
	// "<key id=\"Type\" for=\"edge\">\n" +
	// "<desc>Type: Possible values: normal|contingent|constraint|derived|internal.</desc>\n" +
	// "<default>normal</default>\n" +
	// "</key>\n" +
	// "<key id=\"LowerCaseLabeledValues\" for=\"edge\">\n" +
	// "<desc>Labeled Lower-Case Values. Format: {[('node name (no case modification)', 'integer', 'label') ]+}|{}</desc>\n" +
	// "<default></default>\n" +
	// "</key>\n" +
	// "<key id=\"UpperCaseLabeledValues\" for=\"edge\">\n" +
	// "<desc>Labeled Upper-Case Values. Format: {[('node name (no case modification)', 'integer', 'label') ]+}|{}</desc>\n" +
	// "<default></default>\n" +
	// "</key>\n" +
	// "<key id=\"LabeledValues\" for=\"edge\">\n" +
	// "<desc>Labeled Values. Format: {[('integer', 'label') ]+}|{}</desc>\n" +
	// "<default></default>\n" +
	// "</key>\n" +
	// "<graph edgedefault=\"directed\">\n" +
	// "<data key=\"nContingent\">1</data>\n" +
	// "<data key=\"nObservedProposition\">1</data>\n" +
	// "<data key=\"nEdges\">8</data>\n" +
	// "<data key=\"nVertices\">4</data>\n" +
	// "<node id=\"Z\">\n" +
	// "<data key=\"x\">0.0</data>\n" +
	// "<data key=\"Label\">⊡</data>\n" +
	// "<data key=\"y\">0.0</data>\n" +
	// "</node>\n" +
	// "<node id=\"X\">\n" +
	// "<data key=\"x\">0.0</data>\n" +
	// "<data key=\"Label\">⊡</data>\n" +
	// "<data key=\"y\">0.0</data>\n" +
	// "</node>\n" +
	// "<node id=\"Ω\">\n" +
	// "<data key=\"x\">0.0</data>\n" +
	// "<data key=\"Label\">⊡</data>\n" +
	// "<data key=\"y\">0.0</data>\n" +
	// "</node>\n" +
	// "<node id=\"Y\">\n" +
	// "<data key=\"Obs\">p</data>\n" +
	// "<data key=\"x\">0.0</data>\n" +
	// "<data key=\"Label\">⊡</data>\n" +
	// "<data key=\"y\">0.0</data>\n" +
	// "</node>\n" +
	// "<edge id=\"XY\" source=\"X\" target=\"Y\">\n" +
	// "<data key=\"Type\">contingent</data>\n" +
	// "<data key=\"LowerCaseLabeledValues\">{(X, 2, ¬p) }</data>\n" +
	// "<data key=\"LabeledValues\">{}</data>\n" +
	// "</edge>\n" +
	// "<edge id=\"X_Z\" source=\"X\" target=\"Z\">\n" +
	// "<data key=\"Type\">internal</data>\n" +
	// "<data key=\"LabeledValues\">{(0, ⊡) }</data>\n" +
	// "</edge>\n" +
	// "<edge id=\"YX\" source=\"Y\" target=\"X\">\n" +
	// "<data key=\"Type\">contingent</data>\n" +
	// "<data key=\"UpperCaseLabeledValues\">{(X, -5, ¬p) }</data>\n" +
	// "<data key=\"LabeledValues\">{}</data>\n" +
	// "</edge>\n" +
	// "<edge id=\"Y_Z\" source=\"Y\" target=\"Z\">\n" +
	// "<data key=\"Type\">internal</data>\n" +
	// "<data key=\"LabeledValues\">{(0, ⊡) }</data>\n" +
	// "</edge>\n" +
	// "<edge id=\"Z_X\" source=\"Z\" target=\"X\">\n" +
	// "<data key=\"Type\">internal</data>\n" +
	// "<data key=\"LabeledValues\">{(15, ⊡) }</data>\n" +
	// "</edge>\n" +
	// "<edge id=\"Z_Y\" source=\"Z\" target=\"Y\">\n" +
	// "<data key=\"Type\">internal</data>\n" +
	// "<data key=\"LabeledValues\">{(15, ⊡) }</data>\n" +
	// "</edge>\n" +
	// "<edge id=\"Z_Ω\" source=\"Z\" target=\"Ω\">\n" +
	// "<data key=\"Type\">internal</data>\n" +
	// "<data key=\"LabeledValues\">{(15, ⊡) }</data>\n" +
	// "</edge>\n" +
	// "<edge id=\"Ω_Z\" source=\"Ω\" target=\"Z\">\n" +
	// "<data key=\"Type\">internal</data>\n" +
	// "<data key=\"LabeledValues\">{(0, ⊡) }</data>\n" +
	// "</edge>\n" +
	// "</graph>\n" +
	// "</graphml>";

}
