/**
 * 
 */
package it.univr.di.cstnu;

import static org.junit.Assert.*;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.Literal;

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
	private final static void wellDefinition(LabeledIntGraph g) {
		try {
			CSTNU.checkWellDefinitionProperties(g);
		}
		catch (WellDefinitionException e) {
			fail("LabeledIntGraph not well formed: " + e.getMessage());
		}
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#caseLabelRemovalRule(it.univr.di.cstnu.graph.LabeledIntGraph, it.univr.di.cstnu.graph.LabeledIntGraph)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testCaseLabelRemovalRule() {
		// System.out.printf("LABEL REMOVAL CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(true);
		final LabeledIntEdge ab = new LabeledIntEdge("AB",true);
		ab.mergeLowerLabelValue(Label.parse("B"), "b", 13);
		final LabeledIntEdge ca = new LabeledIntEdge("CA",true);
		ca.mergeUpperLabelValue(Label.parse("AB"), "B", 3);
		ca.mergeUpperLabelValue(Label.parse("A"), "B", 4);
		final LabeledNode A = new LabeledNode("A");
		g.addEdge(ab, A, new LabeledNode("B"));
		g.addEdge(ca, new LabeledNode("C"), A);
		LabeledIntGraph g1 = new LabeledIntGraph(g, true);
		//
		// System.out.printf("G: %s\n", g);
		// // System.out.printf("G1.hasSameEdge(G): %s\n", g1.hasAllEdgesOf(g));
		CSTNU.caseLabelRemovalRule(g, g1);
		// // System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
		// System.out.printf("G: %s\n", g1);
		//
		LabeledIntEdge abOk = new LabeledIntEdge("CA",true);
		abOk.mergeLabeledValue(Label.parse("AB"), 3);

		assertEquals("Upper Case values:", abOk.getLabeledValueMap(), g1.findEdge(g1.getNode("C"), g1.getNode("A"))
				.getLabeledValueMap());

	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#crossCaseRule(it.univr.di.cstnu.graph.LabeledIntGraph, it.univr.di.cstnu.graph.LabeledIntGraph)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testCrossCaseRule() {
		// System.out.printf("CROSS CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledNode A = new LabeledNode("A");
		LabeledNode C = new LabeledNode("C");
		LabeledNode D = new LabeledNode("D");
		LabeledIntEdge dc = new LabeledIntEdge("DC",true);
		LabeledIntEdge ca = new LabeledIntEdge("CA",true);
		g.addEdge(dc, D, C);
		g.addEdge(ca, C, A);
		dc.mergeLowerLabelValue(Label.parse("ab"), "c", 3);
		ca.mergeUpperLabelValue(Label.parse("b¬c"), "D", -3);
		ca.mergeUpperLabelValue(Label.parse("b¬f"), "D", 3);
		LabeledIntGraph g1 = new LabeledIntGraph(g, true);
		// // System.out.printf("G: %s\n", g);
		// // System.out.printf("G1.hasSameEdge(G): %s\n", g1.hasAllEdgesOf(g));
		CSTNU.crossCaseRule(g, g1);
		// // System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
		// System.out.printf("G: %s\n", g1);

		LabeledIntEdge daOk = new LabeledIntEdge("DA",true);
		daOk.mergeUpperLabelValue(Label.parse("ab¬c"), "D", 0);

		assertEquals("Upper Case values:", daOk.getUpperLabelSet(), g1.findEdge(g1.getNode("D"), g1.getNode("A")).getUpperLabelSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#labelModificationR0R2R4(LabeledIntGraph, boolean))}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testLabelModificationR0() {
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledNode P = new LabeledNode("P", new Literal('p'));
		LabeledNode X = new LabeledNode("X");
		LabeledIntEdge px = new LabeledIntEdge("PX",true);
		px.mergeLabeledValue(Label.parse("ABp"), -10);
		px.mergeLabeledValue(Label.parse("AB¬p"), 0);
		px.mergeLabeledValue(Label.parse("C¬p"), 1);
		px.mergeUpperLabelValue(Label.parse("AB¬p"), "C", -11);
		g.addEdge(px, P, X);

		CSTNU.labelModificationR0R2R4(g, true);

		LabeledIntEdge pxOK = new LabeledIntEdge("XY",true);
		pxOK.mergeUpperLabelValue(Label.parse("AB"), "C", -11);
		pxOK.mergeLabeledValue(Label.parse("AB"), -10);
		pxOK.mergeLabeledValue(Label.parse("C¬p"), 1);

		assertEquals("R0: p?X labeled values.", pxOK.getLabeledValueMap(), px.getLabeledValueMap());
		assertEquals("R0: px upper case labedled values.", pxOK.getUpperLabelSet(), px.getUpperLabelSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#labelModificationR0R2R4(LabeledIntGraph, boolean))}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testLabelModificationR2R4() {
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledNode P = new LabeledNode("P?", new Literal('p'));
		LabeledNode A = new LabeledNode("A?", new Literal('A'));
		LabeledNode B = new LabeledNode("B?", new Literal('B'));
		LabeledNode C = new LabeledNode("C?", new Literal('C'));
		LabeledNode X = new LabeledNode("X");
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(C);

		LabeledIntEdge xp = new LabeledIntEdge("XP",true);
		xp.mergeLabeledValue(Label.parse("ABp"), 10);
		xp.mergeLabeledValue(Label.parse("AB¬p"), 0);
		xp.mergeLabeledValue(Label.parse("C¬p"), -1);
		xp.mergeUpperLabelValue(Label.parse("ABC¬p"), "C", 9);
		g.addEdge(xp, X, P);

		LabeledIntEdge XC = new LabeledIntEdge("XC",true);
		XC.mergeLabeledValue(Label.parse("ABCp"), 10);
		XC.mergeLabeledValue(Label.parse("C¬p"), -1);
		g.addEdge(XC, X, C);

		wellDefinition(g);
		// try {
		// CSTNU.checkWellDefinitionProperties(g);
		// }
		// catch (WellDefinitionException e) {
		// fail("LabeledIntGraph not well formed: "+e.getMessage());
		// }

		CSTNU.labelModificationR0R2R4(g, true);

		LabeledIntEdge pxOK = new LabeledIntEdge("XY",true);
		pxOK.mergeUpperLabelValue(Label.parse("ABC"), "C", 9);
		pxOK.mergeLabeledValue(Label.parse("AB"), 0);
		pxOK.mergeLabeledValue(Label.parse("C¬p"), -1);
		pxOK.mergeLabeledValue(Label.parse("C"), 0);

		LabeledIntEdge XCOK = new LabeledIntEdge("XC",true);
		XCOK.mergeLabeledValue(Label.parse("ABp"), 10);
		XCOK.mergeLabeledValue(Label.parse("C¬p"), -1);
		XCOK.mergeLabeledValue(Label.parse("¬p"), 0);

		assertEquals("R2R4: XP? labeled values.", pxOK.getLabeledValueMap(), xp.getLabeledValueMap());
		assertEquals("R2R4: XP? upper case labedled values.", pxOK.getUpperLabelSet(), xp.getUpperLabelSet());
		assertEquals("R2R4: XC? labedled values.", XCOK.getLabeledValueMap(), XC.getLabeledValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#labelModificationR0R2R4(it.univr.di.cstnu.graph.LabeledIntGraph,boolean)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testLabelModificationR2R41() {
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledNode P = new LabeledNode("P?", new Literal('p'));
		LabeledNode A = new LabeledNode("A");
		LabeledNode B = new LabeledNode("B");
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);

		LabeledIntEdge AB = new LabeledIntEdge("AB",true);
		AB.mergeLabeledValue("p", -1);
		g.addEdge(AB, A, B);

		LabeledIntEdge BP = new LabeledIntEdge("BP",true);
		BP.mergeLabeledValue(Label.emptyLabel, 0);
		g.addEdge(BP, B, P);

		wellDefinition(g);
		LabeledIntGraph g1 = new LabeledIntGraph(g, true);

		// System.out.print(g);
		// System.out.print(g1);

		CSTNU.noCaseRule(g, g1);
		CSTNU.labelModificationR0R2R4(g1, true);

		LabeledIntEdge AP_OK = new LabeledIntEdge("AP",true);
		AP_OK.mergeLabeledValue("", 0);
		AP_OK.mergeLabeledValue("p", -1);

		assertEquals("R2R4: AP? labeled values.", AP_OK.getLabeledValueMap(), g1.findEdge(g1.getNode(A.getName()), g1.getNode(P.getName()))
				.getLabeledValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#labelModificationR1R3R5(LabeledIntGraph, LabeledIntGraph, boolean))}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testLabelModificationR1() {
		// System.out.printf("R1 CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledNode P = new LabeledNode("P", new Literal('p'));
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledIntEdge px = new LabeledIntEdge("PX",true);
		LabeledNode A = new LabeledNode("A?", new Literal('a'));
		LabeledNode B = new LabeledNode("B?", new Literal('b'));
		LabeledNode C = new LabeledNode("C?", new Literal('c'));
		LabeledNode G = new LabeledNode("G?", new Literal('g'));
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(G);
		g.addVertex(C);
		g.addVertex(X);
		g.addVertex(Y);

		px.mergeLabeledValue(Label.parse("¬b"), 0);
		px.mergeLabeledValue(Label.parse("ab"), -10);

		LabeledIntEdge xy = new LabeledIntEdge("XY",true);
		xy.mergeLabeledValue(Label.parse("bgp"), 10);
		xy.mergeLabeledValue(Label.parse("cp"), -1);
		xy.mergeUpperLabelValue(Label.parse("bgp"), "C", 8);

		g.addEdge(px, P, X);
		g.addEdge(xy, X, Y);

		wellDefinition(g);

		LabeledIntGraph g1 = new LabeledIntGraph(g, true);

		CSTNU.labelModificationR1R3R5(g, g1, true);

		// <XY, normal, X, Y, L:{(¬ABGp, 10) (¬ACp, -1) (ABC, -1) (ABG, 10) (¬BC, -1) (BCp, -1) }
		LabeledIntEdge xyOK = new LabeledIntEdge("XY",true);
		xyOK.mergeLabeledValue(Label.parse("abc"), -1);
//		xyOK.mergeLabeledValue(Label.parse("abg"), 10);no because the new version of rule excludes w==v
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
	 * Test method for {@link it.univr.di.cstnu.CSTNU#labelModificationR1R3R5(LabeledIntGraph, LabeledIntGraph, boolean))}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testLabelModificationR3R5() {
		// System.out.printf("R1-R3 CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledNode P = new LabeledNode("P", new Literal('p'));
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", new Literal('a'));
		LabeledNode B = new LabeledNode("B?", new Literal('b'));
		LabeledNode C = new LabeledNode("C?", new Literal('c'));
		LabeledNode G = new LabeledNode("G?", new Literal('g'));
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(G);
		g.addVertex(C);
		g.addVertex(X);
		g.addVertex(Y);

		LabeledIntEdge px = new LabeledIntEdge("PX",true);
		px.mergeLabeledValue(Label.parse("¬b"), 0);
		px.mergeLabeledValue(Label.parse("ab"), -10);

		LabeledIntEdge yx = new LabeledIntEdge("YX",true);
		yx.mergeLabeledValue(Label.parse("bgp"), -4);
		yx.mergeLabeledValue(Label.parse("cp"), -10);
		yx.mergeLabeledValue(Label.parse("c¬p"), 11);
		yx.mergeUpperLabelValue(Label.parse("bgp"), "C", -7);

		g.addEdge(px, P, X);
		g.addEdge(yx, Y, X);

		wellDefinition(g);

		LabeledIntGraph g1 = new LabeledIntGraph(g, true);

		CSTNU.labelModificationR1R3R5(g, g1, true);

		// <YX, normal, Y, X, L:{(¬ABGp, -4) (ABG, -4) }, LL:{}, UL:{(¬ABG¬p,C:-4) (¬ABGp,C:-7) (ABG,C:-7) }>
		LabeledIntEdge yxOK = new LabeledIntEdge("YX",true);
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
	 * Test method for {@link it.univr.di.cstnu.CSTNU#lowerCaseRule(it.univr.di.cstnu.graph.LabeledIntGraph, it.univr.di.cstnu.graph.LabeledIntGraph)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testLowerCaseRule() {
		// System.out.printf("LOWER CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledIntEdge dc = new LabeledIntEdge("DC",true);
		LabeledIntEdge ca = new LabeledIntEdge("CA",true);
		dc.mergeLowerLabelValue(Label.parse("ab"), "c", 3);
		ca.mergeLabeledValue(Label.parse("b"), 1);
		ca.mergeLabeledValue(Label.parse("c"), 0);
		ca.mergeLabeledValue(Label.parse("d"), -11);
		LabeledNode C = new LabeledNode("C");
		g.addEdge(dc, new LabeledNode("D"), C);
		g.addEdge(ca, C, new LabeledNode("A"));
		LabeledIntGraph g1 = new LabeledIntGraph(g, true);
		CSTNU.lowerCaseRule(g, g1);

		LabeledIntEdge daOk = new LabeledIntEdge("DA",true);
		daOk.mergeLabeledValue(Label.parse("abc"), 3);
		daOk.mergeLabeledValue(Label.parse("abd"), -8);

		assertEquals("Lower Case values:", daOk.getLabeledValueMap(), g1.findEdge(g1.getNode("D"), g1.getNode("A")).getLabeledValueMap());
		// System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
		// System.out.printf("G: %s\n", g1);
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#upperCaseRule(it.univr.di.cstnu.graph.LabeledIntGraph, it.univr.di.cstnu.graph.LabeledIntGraph)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testUpperCaseRule() {
		// System.out.printf("UPPER CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledIntEdge dc = new LabeledIntEdge("dc",true);
		LabeledIntEdge ca = new LabeledIntEdge("ca",true);
		ca.mergeUpperLabelValue(Label.parse("ab"), "B", 3);
		dc.mergeLabeledValue(Label.parse("b"), -13);
		dc.mergeLabeledValue(Label.parse("c"), 11);
		LabeledNode C = new LabeledNode("C"), A = new LabeledNode("A"), D = new LabeledNode("D");
		g.addEdge(dc, D, C);
		g.addEdge(ca, C, A);

		LabeledIntGraph g1 = new LabeledIntGraph(g, true);
		CSTNU.upperCaseRule(g, g1);

		LabeledIntEdge daOk = new LabeledIntEdge("DA",true);
		daOk.mergeUpperLabelValue(Label.parse("abc"), "B", 14);
		daOk.mergeUpperLabelValue(Label.parse("ab"), "B", -10);

		assertEquals("Upper Case values:", daOk.getUpperLabelSet(), g1.findEdge(g1.getNode("D"), g1.getNode("A")).getUpperLabelSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#noCaseRule(LabeledIntGraph, LabeledIntGraph)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testNoCase() {
		// System.out.printf("R1 CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledNode P = new LabeledNode("P?", new Literal('p'));
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", new Literal('A'));
		LabeledNode B = new LabeledNode("B?", new Literal('B'));
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(Y);

		LabeledIntEdge XP = new LabeledIntEdge("XP",true);
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬A"), 8);

		LabeledIntEdge PY = new LabeledIntEdge("PY",true);
		PY.mergeLabeledValue(Label.parse("¬B"), 9);
		PY.mergeLabeledValue(Label.parse("B"), 10);

		g.addEdge(XP, X, P);
		g.addEdge(PY, P, Y);

		wellDefinition(g);

		LabeledIntGraph g1 = new LabeledIntGraph(g, true);

		CSTNU.noCaseRule(g, g1);

		LabeledIntEdge xyOK = new LabeledIntEdge("XY",true);
		xyOK.mergeLabeledValue(Label.parse("¬A¬B"), 17);
		xyOK.mergeLabeledValue(Label.parse("¬AB"), 18);
		xyOK.mergeLabeledValue(Label.parse("¬B"), 19);
		xyOK.mergeLabeledValue(Label.parse("B"), 20);

		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), g1.findEdge(g1.getNode("X"), g1.getNode("Y")).getLabeledValueMap());
		assertEquals("No case: XY upper case labedled values.", xyOK.getUpperLabelSet(), g1.findEdge(g1.getNode("X"), g1.getNode("Y")).getUpperLabelSet());
	}

}
