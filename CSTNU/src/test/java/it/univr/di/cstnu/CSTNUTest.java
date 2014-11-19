/**
 * 
 */
package it.univr.di.cstnu;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author posenato
 */
public class CSTNUTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {}

	/**
	 * @param g
	 */
	private final static void wellDefinition(Graph g) {
		try {
			CSTNU.checkWellDefinitionProperties(g);
		}
		catch (WellDefinitionException e) {
			fail("Graph not well formed: " + e.getMessage());
		}
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#caseLabelRemovalRule(it.univr.di.cstnu.Graph, it.univr.di.cstnu.Graph)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testCaseLabelRemovalRule() {
		// System.out.printf("LABEL REMOVAL CASE\n");
		Graph g = new Graph();
		final Edge ab = new Edge("AB");
		ab.mergeLowerLabelValue(Label.parse("B"), "b", 13);
		final Edge ca = new Edge("CA");
		ca.mergeUpperLabelValue(Label.parse("AB"), "B", 3);
		ca.mergeUpperLabelValue(Label.parse("A"), "B", 4);
		final Node A = new Node("A");
		g.addEdge(ab, A, new Node("B"));
		g.addEdge(ca, new Node("C"), A);
		Graph g1 = new Graph(g);
		//
		// System.out.printf("G: %s\n", g);
		// // System.out.printf("G1.hasSameEdge(G): %s\n", g1.hasAllEdgesOf(g));
		CSTNU.caseLabelRemovalRule(g, g1);
		// // System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
		// System.out.printf("G: %s\n", g1);
		//
		Edge abOk = new Edge("CA");
		abOk.mergeLabeledValue(Label.parse("AB"), 3);

		assertEquals("Upper Case values:", abOk.getLabeledValueMap(), g1.findEdge(g1.getNode("C"), g1.getNode("A"))
				.getLabeledValueMap());

	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#crossCaseRule(it.univr.di.cstnu.Graph, it.univr.di.cstnu.Graph)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testCrossCaseRule() {
		// System.out.printf("CROSS CASE\n");
		Graph g = new Graph();
		Node A = new Node("A");
		Node C = new Node("C");
		Node D = new Node("D");
		Edge dc = new Edge("DC");
		Edge ca = new Edge("CA");
		g.addEdge(dc, D, C);
		g.addEdge(ca, C, A);
		dc.mergeLowerLabelValue(Label.parse("ab"), "c", 3);
		ca.mergeUpperLabelValue(Label.parse("b¬c"), "D", -3);
		ca.mergeUpperLabelValue(Label.parse("b¬f"), "D", 3);
		Graph g1 = new Graph(g);
		// // System.out.printf("G: %s\n", g);
		// // System.out.printf("G1.hasSameEdge(G): %s\n", g1.hasAllEdgesOf(g));
		CSTNU.crossCaseRule(g, g1);
		// // System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
		// System.out.printf("G: %s\n", g1);

		Edge daOk = new Edge("DA");
		daOk.mergeUpperLabelValue(Label.parse("ab¬c"), "D", 0);

		assertEquals("Upper Case values:", daOk.getUpperLabelSet(), g1.findEdge(g1.getNode("D"), g1.getNode("A"))
				.getUpperLabelSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#labelModificationR0R2R4(Graph, boolean))}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testLabelModificationR0() {
		Graph g = new Graph();
		Node P = new Node("P", new Literal('p'));
		Node X = new Node("X");
		Edge px = new Edge("PX");
		px.mergeLabeledValue(Label.parse("ABp"), -10);
		px.mergeLabeledValue(Label.parse("AB¬p"), 0);
		px.mergeLabeledValue(Label.parse("C¬p"), 1);
		px.mergeUpperLabelValue(Label.parse("AB¬p"), "C", -11);
		g.addEdge(px, P, X);

		CSTNU.labelModificationR0R2R4(g, true);

		Edge pxOK = new Edge("XY");
		pxOK.mergeUpperLabelValue(Label.parse("AB"), "C", -11);
		pxOK.mergeLabeledValue(Label.parse("AB"), -10);
		pxOK.mergeLabeledValue(Label.parse("C¬p"), 1);

		assertEquals("R0: p?X labeled values.", pxOK.getLabeledValueMap(), px.getLabeledValueMap());
		assertEquals("R0: px upper case labedled values.", pxOK.getUpperLabelSet(), px.getUpperLabelSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#labelModificationR0R2R4(Graph, boolean))}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testLabelModificationR2R4() {
		Graph g = new Graph();
		Node P = new Node("P?", new Literal('p'));
		Node A = new Node("A?", new Literal('A'));
		Node B = new Node("B?", new Literal('B'));
		Node C = new Node("C?", new Literal('C'));
		Node X = new Node("X");
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(C);

		Edge xp = new Edge("XP");
		xp.mergeLabeledValue(Label.parse("ABp"), 10);
		xp.mergeLabeledValue(Label.parse("AB¬p"), 0);
		xp.mergeLabeledValue(Label.parse("C¬p"), -1);
		xp.mergeUpperLabelValue(Label.parse("ABC¬p"), "C", 9);
		g.addEdge(xp, X, P);

		Edge XC = new Edge("XC");
		XC.mergeLabeledValue(Label.parse("ABCp"), 10);
		XC.mergeLabeledValue(Label.parse("C¬p"), -1);
		g.addEdge(XC, X, C);

		wellDefinition(g);
		// try {
		// CSTNU.checkWellDefinitionProperties(g);
		// }
		// catch (WellDefinitionException e) {
		// fail("Graph not well formed: "+e.getMessage());
		// }

		CSTNU.labelModificationR0R2R4(g, true);

		Edge pxOK = new Edge("XY");
		pxOK.mergeUpperLabelValue(Label.parse("ABC"), "C", 9);
		pxOK.mergeLabeledValue(Label.parse("AB"), 0);
		pxOK.mergeLabeledValue(Label.parse("C¬p"), -1);
		pxOK.mergeLabeledValue(Label.parse("C"), 0);

		Edge XCOK = new Edge("XC");
		XCOK.mergeLabeledValue(Label.parse("ABp"), 10);
		XCOK.mergeLabeledValue(Label.parse("C¬p"), -1);
		XCOK.mergeLabeledValue(Label.parse("¬p"), 0);

		assertEquals("R2R4: XP? labeled values.", pxOK.getLabeledValueMap(), xp.getLabeledValueMap());
		assertEquals("R2R4: XP? upper case labedled values.", pxOK.getUpperLabelSet(), xp.getUpperLabelSet());
		assertEquals("R2R4: XC? labedled values.", XCOK.getLabeledValueMap(), XC.getLabeledValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#labelModificationR0R2R4(it.univr.di.cstnu.Graph,boolean)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testLabelModificationR2R41() {
		Graph g = new Graph();
		Node P = new Node("P?", new Literal('p'));
		Node A = new Node("A");
		Node B = new Node("B");
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);

		Edge AB = new Edge("AB");
		AB.mergeLabeledValue("p", -1);
		g.addEdge(AB, A, B);

		Edge BP = new Edge("BP");
		BP.mergeLabeledValue(Label.emptyLabel, 0);
		g.addEdge(BP, B, P);

		wellDefinition(g);
		Graph g1 = new Graph(g);

		// System.out.print(g);
		// System.out.print(g1);

		CSTNU.noCaseRule(g, g1);
		CSTNU.labelModificationR0R2R4(g1, true);

		Edge AP_OK = new Edge("AP");
		AP_OK.mergeLabeledValue("", 0);
		AP_OK.mergeLabeledValue("p", -1);

		assertEquals("R2R4: AP? labeled values.", AP_OK.getLabeledValueMap(), g1.findEdge(g1.getNode(A.getName()), g1.getNode(P.getName()))
				.getLabeledValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#labelModificationR1R3R5(Graph, Graph, boolean))}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testLabelModificationR1() {
		// System.out.printf("R1 CASE\n");
		Graph g = new Graph();
		Node P = new Node("P", new Literal('p'));
		Node X = new Node("X");
		Node Y = new Node("Y");
		Edge px = new Edge("PX");
		Node A = new Node("A?", new Literal('a'));
		Node B = new Node("B?", new Literal('b'));
		Node C = new Node("C?", new Literal('c'));
		Node G = new Node("G?", new Literal('g'));
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(G);
		g.addVertex(C);
		g.addVertex(X);
		g.addVertex(Y);

		px.mergeLabeledValue(Label.parse("¬b"), 0);
		px.mergeLabeledValue(Label.parse("ab"), -10);

		Edge xy = new Edge("XY");
		xy.mergeLabeledValue(Label.parse("bgp"), 10);
		xy.mergeLabeledValue(Label.parse("cp"), -1);
		xy.mergeUpperLabelValue(Label.parse("bgp"), "C", 8);

		g.addEdge(px, P, X);
		g.addEdge(xy, X, Y);

		wellDefinition(g);

		Graph g1 = new Graph(g);

		CSTNU.labelModificationR1R3R5(g, g1, false);

		// <XY, normal, X, Y, L:{(¬ABGp, 10) (¬ACp, -1) (ABC, -1) (ABG, 10) (¬BC, -1) (BCp, -1) }
		Edge xyOK = new Edge("XY");
		xyOK.mergeLabeledValue(Label.parse("abc"), -1);
		xyOK.mergeLabeledValue(Label.parse("abg"), 10);
		xyOK.mergeLabeledValue(Label.parse("¬bc"), -1);
		xyOK.mergeLabeledValue(Label.parse("bgp"), 10);
		xyOK.mergeLabeledValue(Label.parse("cp"), -1);
//		xyOK.mergeLabeledValue(Label.parse("bcp"), -1);
		xyOK.mergeUpperLabelValue(Label.parse("abg"), "C", 8);
		xyOK.mergeUpperLabelValue(Label.parse("bgp"), "C", 8);

		assertEquals("R1: xy labeled values.", xyOK.getLabeledValueMap(), g1.findEdge(g1.getNode("X"), g1.getNode("Y")).getLabeledValueMap());
		assertEquals("R1: xy upper case labedled values.", xyOK.getUpperLabelSet(), g1.findEdge(g1.getNode("X"), g1.getNode("Y")).getUpperLabelSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#labelModificationR1R3R5(Graph, Graph, boolean))}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testLabelModificationR3R5() {
		// System.out.printf("R1-R3 CASE\n");
		Graph g = new Graph();
		Node P = new Node("P", new Literal('p'));
		Node X = new Node("X");
		Node Y = new Node("Y");
		Node A = new Node("A?", new Literal('a'));
		Node B = new Node("B?", new Literal('b'));
		Node C = new Node("C?", new Literal('c'));
		Node G = new Node("G?", new Literal('g'));
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(G);
		g.addVertex(C);
		g.addVertex(X);
		g.addVertex(Y);

		Edge px = new Edge("PX");
		px.mergeLabeledValue(Label.parse("¬b"), 0);
		px.mergeLabeledValue(Label.parse("ab"), -10);

		Edge yx = new Edge("YX");
		yx.mergeLabeledValue(Label.parse("bgp"), -4);
		yx.mergeLabeledValue(Label.parse("cp"), -10);
		yx.mergeLabeledValue(Label.parse("c¬p"), 11);
		yx.mergeUpperLabelValue(Label.parse("bgp"), "C", -7);

		g.addEdge(px, P, X);
		g.addEdge(yx, Y, X);

		wellDefinition(g);

		Graph g1 = new Graph(g);

		CSTNU.labelModificationR1R3R5(g, g1, true);

		// <YX, normal, Y, X, L:{(¬ABGp, -4) (ABG, -4) }, LL:{}, UL:{(¬ABG¬p,C:-4) (¬ABGp,C:-7) (ABG,C:-7) }>
		Edge yxOK = new Edge("YX");
		yxOK.mergeUpperLabelValue(Label.parse("abg"), "C", -7);
		yxOK.mergeUpperLabelValue(Label.parse("bgp"), "C", -7);
//		yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("abc"), -10);
		yxOK.mergeLabeledValue(Label.parse("bgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("abg"), -4);
		yxOK.mergeLabeledValue(Label.parse("¬bc"), 0);//R5 rule
		yxOK.mergeLabeledValue(Label.parse("c¬p"), 11);
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);

		assertEquals("R3R5: yx labeled values.", yxOK.getLabeledValueMap(), g1.findEdge(g1.getNode("Y"), g1.getNode("X")).getLabeledValueMap());
		assertEquals("R3R5: yx upper case labedled values.", yxOK.getUpperLabelSet(), g1.findEdge(g1.getNode("Y"), g1.getNode("X")).getUpperLabelSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#lowerCaseRule(it.univr.di.cstnu.Graph, it.univr.di.cstnu.Graph)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testLowerCaseRule() {
		// System.out.printf("LOWER CASE\n");
		Graph g = new Graph();
		Edge dc = new Edge("DC");
		Edge ca = new Edge("CA");
		dc.mergeLowerLabelValue(Label.parse("ab"), "c", 3);
		ca.mergeLabeledValue(Label.parse("b"), 1);
		ca.mergeLabeledValue(Label.parse("c"), 0);
		ca.mergeLabeledValue(Label.parse("d"), -11);
		Node C = new Node("C");
		g.addEdge(dc, new Node("D"), C);
		g.addEdge(ca, C, new Node("A"));
		Graph g1 = new Graph(g);
		CSTNU.lowerCaseRule(g, g1);

		Edge daOk = new Edge("DA");
		daOk.mergeLabeledValue(Label.parse("abc"), 3);
		daOk.mergeLabeledValue(Label.parse("abd"), -8);

		assertEquals("Lower Case values:", daOk.getLabeledValueMap(), g1.findEdge(g1.getNode("D"), g1.getNode("A")).getLabeledValueMap());
		// System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
		// System.out.printf("G: %s\n", g1);
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#observationCaseRule(it.univr.di.cstnu.Graph, it.univr.di.cstnu.Graph)}.
	 */
	// @Test
	// public final void testObservationCaseRule() {
	// fail("Not yet implemented"); // TODO
	// }

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#upperCaseRule(it.univr.di.cstnu.Graph, it.univr.di.cstnu.Graph)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testUpperCaseRule() {
		// System.out.printf("UPPER CASE\n");
		Graph g = new Graph();
		Edge dc = new Edge("dc");
		Edge ca = new Edge("ca");
		ca.mergeUpperLabelValue(Label.parse("ab"), "B", 3);
		dc.mergeLabeledValue(Label.parse("b"), -13);
		dc.mergeLabeledValue(Label.parse("c"), 11);
		Node C = new Node("C"), A = new Node("A"), D = new Node("D");
		g.addEdge(dc, D, C);
		g.addEdge(ca, C, A);

		Graph g1 = new Graph(g);
		CSTNU.upperCaseRule(g, g1);

		Edge daOk = new Edge("DA");
		daOk.mergeUpperLabelValue(Label.parse("abc"), "B", 14);
		daOk.mergeUpperLabelValue(Label.parse("ab"), "B", -10);

		assertEquals("Upper Case values:", daOk.getUpperLabelSet(), g1.findEdge(g1.getNode("D"), g1.getNode("A")).getUpperLabelSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#noCaseRule(Graph, Graph)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testNoCase() {
		// System.out.printf("R1 CASE\n");
		Graph g = new Graph();
		Node P = new Node("P?", new Literal('p'));
		Node X = new Node("X");
		Node Y = new Node("Y");
		Node A = new Node("A?", new Literal('A'));
		Node B = new Node("B?", new Literal('B'));
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(Y);

		Edge XP = new Edge("XP");
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬A"), 8);

		Edge PY = new Edge("PY");
		PY.mergeLabeledValue(Label.parse("¬B"), 9);
		PY.mergeLabeledValue(Label.parse("B"), 10);

		g.addEdge(XP, X, P);
		g.addEdge(PY, P, Y);

		wellDefinition(g);

		Graph g1 = new Graph(g);

		CSTNU.noCaseRule(g, g1);

		Edge xyOK = new Edge("XY");
		xyOK.mergeLabeledValue(Label.parse("¬A¬B"), 17);
		xyOK.mergeLabeledValue(Label.parse("¬AB"), 18);
		xyOK.mergeLabeledValue(Label.parse("¬B"), 19);
		xyOK.mergeLabeledValue(Label.parse("B"), 20);

		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), g1.findEdge(g1.getNode("X"), g1.getNode("Y")).getLabeledValueMap());
		assertEquals("No case: XY upper case labedled values.", xyOK.getUpperLabelSet(), g1.findEdge(g1.getNode("X"), g1.getNode("Y")).getUpperLabelSet());
	}

}
