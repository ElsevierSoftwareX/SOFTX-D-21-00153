/**
 * 
 */
package it.univr.di.cstnu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import it.univr.di.attic.CSTNU_NodeSet;
import it.univr.di.attic.CSTNU_NodeSet.CSTNUCheckStatus;
import it.univr.di.cstnu.algorithms.WellDefinitionException;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntMapFactory;
import it.univr.di.labeledvalue.LabeledIntNodeSetTreeMap;

/**
 * @author posenato
 */
@SuppressWarnings("deprecation")
public class CSTNU_NodeSetTest {

	@SuppressWarnings("javadoc")
	final Class<? extends LabeledIntMap> labeledIntValueMapClass = (new LabeledIntNodeSetTreeMap()).getClass();

	@SuppressWarnings("javadoc")
	final LabeledIntMapFactory<? extends LabeledIntMap> labeledIntMapFactory = new LabeledIntMapFactory<>(labeledIntValueMapClass);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @param g
	 */
	private final static void wellDefinition(LabeledIntGraph g) {
		try {
			CSTNU_NodeSet.checkWellDefinitionProperties(g);
		} catch (WellDefinitionException e) {
			fail("LabeledIntGraph not well formed: " + e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link it.univr.di.attic.CSTNU_NodeSet#caseLabelRemovalRule(it.univr.di.cstnu.graph.LabeledIntGraph, it.univr.di.cstnu.graph.LabeledIntGraph, CSTNUCheckStatus)}.
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testCaseLabelRemovalRule() {
		// System.out.printf("LABEL REMOVAL CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMapClass);

		final LabeledIntEdgePluggable ab = new LabeledIntEdgePluggable("AB",labeledIntValueMapClass);
		ab.mergeLowerLabelValue(Label.parse("b"), "b", 13);

		final LabeledIntEdgePluggable ba = new LabeledIntEdgePluggable("ba",labeledIntValueMapClass);// now CaseLabelRemoval checks the lower bound, no the guards.
		ba.putLabeledValue(Label.parse("b"), -10);

		final LabeledIntEdgePluggable ca = new LabeledIntEdgePluggable("CA",labeledIntValueMapClass);
		ca.mergeUpperLabelValue(Label.parse("ab"), "B", 3);
		ca.mergeUpperLabelValue(Label.parse("a"), "B", 4);

		final LabeledNode A = new LabeledNode("A");
		final LabeledNode B = new LabeledNode("B");
		g.addEdge(ab, A, B);
		g.addEdge(ba, B, A);
		g.addEdge(ca, new LabeledNode("C"), A);

		LabeledIntGraph g1 = new LabeledIntGraph(g, labeledIntValueMapClass);
		//
		// System.out.printf("G: %s\n", g);
		// System.out.printf("G1.hasSameEdge(G): %s\n", g1.hasSameEdgesOf(g));
		CSTNU_NodeSet cstnu = new CSTNU_NodeSet();
		CSTNUCheckStatus status = new CSTNUCheckStatus();
		cstnu.caseLabelRemovalRule(g, g1, status);
		// // System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
		// System.out.printf("G: %s\n", g1);
		//
		LabeledIntEdgePluggable abOk = new LabeledIntEdgePluggable("CA",labeledIntValueMapClass);
		abOk.mergeLabeledValue(Label.parse("ab"), 3);

		assertEquals("Upper Case values:", abOk.getLabeledValueMap(), g1.findEdge(g1.getNode("C"), g1.getNode("A")).getLabeledValueMap());

	}

	/**
	 * Test method for
	 * {@link it.univr.di.attic.CSTNU_NodeSet#crossCaseRule(it.univr.di.cstnu.graph.LabeledIntGraph, it.univr.di.cstnu.graph.LabeledIntGraph, CSTNUCheckStatus)}.
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testCrossCaseRule() {
		// System.out.printf("CROSS CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMapClass);
		LabeledNode A = new LabeledNode("A");
		LabeledNode C = new LabeledNode("C");
		LabeledNode D = new LabeledNode("D");
		LabeledIntEdgePluggable dc = new LabeledIntEdgePluggable("DC",labeledIntValueMapClass);
		LabeledIntEdgePluggable ca = new LabeledIntEdgePluggable("CA",labeledIntValueMapClass);
		g.addEdge(dc, D, C);
		g.addEdge(ca, C, A);
		dc.mergeLowerLabelValue(Label.parse("ab"), "c", 3);
		ca.mergeUpperLabelValue(Label.parse("b¬c"), "D", -3);
		ca.mergeUpperLabelValue(Label.parse("b¬f"), "D", 3);
		LabeledIntGraph g1 = new LabeledIntGraph(g, labeledIntValueMapClass);
		// // System.out.printf("G: %s\n", g);
		// // System.out.printf("G1.hasSameEdge(G): %s\n", g1.hasAllEdgesOf(g));
		CSTNU_NodeSet cstnu = new CSTNU_NodeSet();
		CSTNUCheckStatus status = new CSTNUCheckStatus();
		cstnu.crossCaseRule(g, g1, status);
		// // System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
		// System.out.printf("G: %s\n", g1);

		LabeledIntEdgePluggable daOk = new LabeledIntEdgePluggable("DA",labeledIntValueMapClass);
		daOk.mergeUpperLabelValue(Label.parse("ab¬c"), "D", 0);

		assertEquals("Upper Case values:", daOk.getUpperLabelSet(), g1.findEdge(g1.getNode("D"), g1.getNode("A")).getUpperLabelSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU_NodeSet#labelModificationR0R2R4(LabeledIntGraph, boolean))}.
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabelModificationR0() {
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMapClass);
		LabeledNode P = new LabeledNode("P", 'p');
		LabeledNode X = new LabeledNode("X");
		LabeledIntEdgePluggable px = new LabeledIntEdgePluggable("PX",labeledIntValueMapClass);
		px.mergeLabeledValue(Label.parse("abp"), -10);
		px.mergeLabeledValue(Label.parse("ab¬p"), 0);
		px.mergeLabeledValue(Label.parse("c¬p"), 1);
		px.mergeUpperLabelValue(Label.parse("ab¬p"), "C", -11);
		g.addEdge(px, P, X);

		CSTNU_NodeSet cstnu = new CSTNU_NodeSet();
		CSTNUCheckStatus status = new CSTNUCheckStatus();
		cstnu.labelModificationR0R2(g, status);

		LabeledIntEdgePluggable pxOK = new LabeledIntEdgePluggable("XY",labeledIntValueMapClass);
		pxOK.mergeUpperLabelValue(Label.parse("ab"), "C", -11);
		pxOK.mergeLabeledValue(Label.parse("ab"), -10);
		pxOK.mergeLabeledValue(Label.parse("c¬p"), 1);

		assertEquals("R0: p?X labeled values.", pxOK.getLabeledValueMap(), px.getLabeledValueMap());
		assertEquals("R0: px upper case labedled values.", pxOK.getUpperLabelSet(), px.getUpperLabelSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU_NodeSet#labelModificationR0R2R4(LabeledIntGraph, boolean))}.
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabelModificationR2R4() {
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMapClass);
		LabeledNode P = new LabeledNode("P?", 'p');
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		LabeledNode C = new LabeledNode("C?", 'c');
		LabeledNode X = new LabeledNode("X");
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(C);

		LabeledIntEdgePluggable xp = new LabeledIntEdgePluggable("XP",labeledIntValueMapClass);
		xp.mergeLabeledValue(Label.parse("abp"), 10);
		xp.mergeLabeledValue(Label.parse("ab¬p"), 0);
		xp.mergeLabeledValue(Label.parse("c¬p"), -1);
		xp.mergeUpperLabelValue(Label.parse("abc¬p"), "C", 9);
		g.addEdge(xp, X, P);

		LabeledIntEdgePluggable XC = new LabeledIntEdgePluggable("XC",labeledIntValueMapClass);
		XC.mergeLabeledValue(Label.parse("abcp"), 10);
		XC.mergeLabeledValue(Label.parse("c¬p"), -1);
		g.addEdge(XC, X, C);

		wellDefinition(g);
		// try {
		// CSTNU_NodeSet.checkWellDefinitionProperties(g);
		// }
		// catch (WellDefinitionException e) {
		// fail("LabeledIntGraph not well formed: "+e.getMessage());
		// }

		CSTNU_NodeSet cstnu = new CSTNU_NodeSet();
		CSTNUCheckStatus status = new CSTNUCheckStatus();
		cstnu.labelModificationR0R2(g, status);

		LabeledIntEdgePluggable pxOK = new LabeledIntEdgePluggable("XY",labeledIntValueMapClass);
		pxOK.mergeUpperLabelValue(Label.parse("abc"), "C", 9);
		pxOK.mergeLabeledValue(Label.parse("ab"), 0);
		pxOK.mergeLabeledValue(Label.parse("c¬p"), -1);
		pxOK.mergeLabeledValue(Label.parse("c"), 0);

		LabeledIntEdgePluggable XCOK = new LabeledIntEdgePluggable("XC",labeledIntValueMapClass);
		XCOK.mergeLabeledValue(Label.parse("abp"), 10);
		XCOK.mergeLabeledValue(Label.parse("c¬p"), -1);
		XCOK.mergeLabeledValue(Label.parse("¬p"), 0);

		assertEquals("R2R4: XP? labeled values.", pxOK.getLabeledValueMap(), xp.getLabeledValueMap());
		assertEquals("R2R4: XP? upper case labedled values.", pxOK.getUpperLabelSet(), xp.getUpperLabelSet());
		assertEquals("R2R4: XC? labedled values.", XCOK.getLabeledValueMap(), XC.getLabeledValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.attic.CSTNU_NodeSet#labelModificationR0R2(LabeledIntGraph, CSTNUCheckStatus)}.
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabelModificationR2R41() {
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMapClass);
		LabeledNode P = new LabeledNode("P?", 'p');
		LabeledNode A = new LabeledNode("A");
		LabeledNode B = new LabeledNode("B");
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);

		LabeledIntEdgePluggable AB = new LabeledIntEdgePluggable("AB",labeledIntValueMapClass);
		AB.mergeLabeledValue("p", -1);
		g.addEdge(AB, A, B);

		LabeledIntEdgePluggable BP = new LabeledIntEdgePluggable("BP",labeledIntValueMapClass);
		BP.mergeLabeledValue(Label.emptyLabel, 0);
		g.addEdge(BP, B, P);

		wellDefinition(g);
		LabeledIntGraph g1 = new LabeledIntGraph(g, labeledIntValueMapClass);

		// System.out.print(g);
		// System.out.print(g1);

		CSTNU_NodeSet cstnu = new CSTNU_NodeSet();
		CSTNUCheckStatus status = new CSTNUCheckStatus();
		cstnu.noCaseRule(g, g1, status);
		cstnu.labelModificationR0R2(g1, status);

		LabeledIntEdgePluggable AP_OK = new LabeledIntEdgePluggable("AP",labeledIntValueMapClass);
		AP_OK.mergeLabeledValue("", 0);
		AP_OK.mergeLabeledValue("p", -1);

		assertEquals("R2R4: AP? labeled values.", AP_OK.getLabeledValueMap(), g1.findEdge(g1.getNode(A.getName()), g1.getNode(P.getName()))
				.getLabeledValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU_NodeSet#labelModificationR1R3R5(LabeledIntGraph, LabeledIntGraph, boolean))}.
	 */
	@SuppressWarnings({ "javadoc" })
	@Test
	public final void testLabelModificationR1() {
		// System.out.printf("R1 CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMapClass);
		LabeledNode P = new LabeledNode("P", 'p');
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledIntEdgePluggable px = new LabeledIntEdgePluggable("PX",labeledIntValueMapClass);
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		LabeledNode C = new LabeledNode("C?", 'c');
		LabeledNode G = new LabeledNode("G?", 'g');
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(G);
		g.addVertex(C);
		g.addVertex(X);
		g.addVertex(Y);

		px.mergeLabeledValue(Label.parse("¬b"), 0);
		px.mergeLabeledValue(Label.parse("ab"), -10);

		LabeledIntEdgePluggable xy = new LabeledIntEdgePluggable("XY",labeledIntValueMapClass);
		xy.mergeLabeledValue(Label.parse("bgp"), 10);
		xy.mergeLabeledValue(Label.parse("cp"), -1);
		xy.mergeUpperLabelValue(Label.parse("bgp"), "C", 8);

		g.addEdge(px, P, X);
		g.addEdge(xy, X, Y);

		wellDefinition(g);

		LabeledIntGraph g1 = new LabeledIntGraph(g, labeledIntValueMapClass);

		boolean instantaneousReaction = false;
		CSTNU_NodeSet cstnu = new CSTNU_NodeSet();
		CSTNUCheckStatus status = new CSTNUCheckStatus();
		cstnu.labelModificationR1R3(g, g1, status);

		// <XY, normal, X, Y, L:{(¬ABGp, 10) (¬ACp, -1) (ABC, -1) (ABG, 10) (¬BC, -1) (BCp, -1) }
		LabeledIntEdgePluggable xyOK = new LabeledIntEdgePluggable("XY",labeledIntValueMapClass);
		xyOK.mergeLabeledValue(Label.parse("abc"), -1);
		if (!instantaneousReaction) 
			xyOK.mergeLabeledValue(Label.parse("abg"), 10);
		xyOK.mergeLabeledValue(Label.parse("¬bc"), -1);
		xyOK.mergeLabeledValue(Label.parse("bgp"), 10);
		xyOK.mergeLabeledValue(Label.parse("cp"), -1);
		// xyOK.mergeLabeledValue(Label.parse("bcp"), -1);
		xyOK.mergeUpperLabelValue(Label.parse("abg"), "C", 8);
		xyOK.mergeUpperLabelValue(Label.parse("bgp"), "C", 8);

		assertEquals("R1: xy labeled values.", xyOK.getLabeledValueMap(), g1.findEdge(g1.getNode("X"), g1.getNode("Y")).getLabeledValueMap());
		assertEquals("R1: xy upper case labedled values.", xyOK.getUpperLabelSet(), g1.findEdge(g1.getNode("X"), g1.getNode("Y")).getUpperLabelSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU_NodeSet#labelModificationR1R3R5(LabeledIntGraph, LabeledIntGraph, CSTNUCheckStatus)))}.
	 */
	@SuppressWarnings({ "javadoc" })
	@Test
	public final void testLabelModificationR3R5() {
		// System.out.printf("R1-R3 CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMapClass);
		LabeledNode P = new LabeledNode("P", 'p');
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		LabeledNode C = new LabeledNode("C?", 'c');
		LabeledNode G = new LabeledNode("G?", 'g');
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(G);
		g.addVertex(C);
		g.addVertex(X);
		g.addVertex(Y);

		LabeledIntEdgePluggable px = new LabeledIntEdgePluggable("PX",labeledIntValueMapClass);
		px.mergeLabeledValue(Label.parse("¬b"), 0);
		px.mergeLabeledValue(Label.parse("ab"), -10);

		LabeledIntEdgePluggable yx = new LabeledIntEdgePluggable("YX",labeledIntValueMapClass);
		yx.mergeLabeledValue(Label.parse("bgp"), -4);
		yx.mergeLabeledValue(Label.parse("cp"), -10);
		yx.mergeLabeledValue(Label.parse("c¬p"), 11);
		yx.mergeUpperLabelValue(Label.parse("bgp"), "C", -7);

		g.addEdge(px, P, X);
		g.addEdge(yx, Y, X);

		wellDefinition(g);

		LabeledIntGraph g1 = new LabeledIntGraph(g, labeledIntValueMapClass);

		CSTNU_NodeSet cstnu = new CSTNU_NodeSet();
		CSTNUCheckStatus status = new CSTNUCheckStatus();
		cstnu.labelModificationR1R3(g, g1, status);

		// <YX, normal, Y, X, L:{(¬ABGp, -4) (ABG, -4) }, LL:{}, UL:{(¬ABG¬p,C:-4) (¬ABGp,C:-7) (ABG,C:-7) }>
		LabeledIntEdgePluggable yxOK = new LabeledIntEdgePluggable("YX",labeledIntValueMapClass);
		yxOK.mergeUpperLabelValue(Label.parse("abg"), "C", -7);
		yxOK.mergeUpperLabelValue(Label.parse("bgp"), "C", -7);
		// yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("abc"), -10);
		yxOK.mergeLabeledValue(Label.parse("bgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("abg"), -4);
		yxOK.mergeLabeledValue(Label.parse("¬bc"), 0);// R5 rule
		yxOK.mergeLabeledValue(Label.parse("c¬p"), 11);
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);

		assertEquals("R3R5: yx labeled values.", yxOK.getLabeledValueMap(), g1.findEdge(g1.getNode("Y"), g1.getNode("X")).getLabeledValueMap());
		assertEquals("R3R5: yx upper case labedled values.", yxOK.getUpperLabelSet(), g1.findEdge(g1.getNode("Y"), g1.getNode("X")).getUpperLabelSet());
	}

	/**
	 * Test method for {@link it.univr.di.attic.CSTNU_NodeSet#lowerCaseRule(LabeledIntGraph, LabeledIntGraph, CSTNUCheckStatus)}.
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLowerCaseRule() {
		// System.out.printf("LOWER CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMapClass);
		LabeledIntEdgePluggable dc = new LabeledIntEdgePluggable("DC",labeledIntValueMapClass);
		LabeledIntEdgePluggable ca = new LabeledIntEdgePluggable("CA",labeledIntValueMapClass);
		dc.mergeLowerLabelValue(Label.parse("ab"), "c", 3);
		ca.mergeLabeledValue(Label.parse("b"), 1);
		ca.mergeLabeledValue(Label.parse("c"), 0);
		ca.mergeLabeledValue(Label.parse("d"), -11);
		LabeledNode C = new LabeledNode("C");
		g.addEdge(dc, new LabeledNode("D"), C);
		g.addEdge(ca, C, new LabeledNode("A"));
		LabeledIntGraph g1 = new LabeledIntGraph(g, labeledIntValueMapClass);
		CSTNU_NodeSet cstnu = new CSTNU_NodeSet();
		CSTNUCheckStatus status = new CSTNUCheckStatus();
		cstnu.lowerCaseRule(g, g1,status);

		LabeledIntEdgePluggable daOk = new LabeledIntEdgePluggable("DA",labeledIntValueMapClass);
		daOk.mergeLabeledValue(Label.parse("abc"), 3);
		daOk.mergeLabeledValue(Label.parse("abd"), -8);

		assertEquals("Lower Case values:", daOk.getLabeledValueMap(), g1.findEdge(g1.getNode("D"), g1.getNode("A")).getLabeledValueMap());
		// System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
		// System.out.printf("G: %s\n", g1);
	}

	/**
	 * Test method for {@link it.univr.di.attic.CSTNU_NodeSet#upperCaseRule(LabeledIntGraph, LabeledIntGraph, CSTNUCheckStatus)}.
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testUpperCaseRule() {
		// System.out.printf("UPPER CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMapClass);
		LabeledIntEdgePluggable dc = new LabeledIntEdgePluggable("dc",labeledIntValueMapClass);
		LabeledIntEdgePluggable ca = new LabeledIntEdgePluggable("ca",labeledIntValueMapClass);
		ca.mergeUpperLabelValue(Label.parse("ab"), "B", 3);
		dc.mergeLabeledValue(Label.parse("b"), -13);
		dc.mergeLabeledValue(Label.parse("c"), 11);
		LabeledNode C = new LabeledNode("C"), A = new LabeledNode("A"), D = new LabeledNode("D");
		g.addEdge(dc, D, C);
		g.addEdge(ca, C, A);

		LabeledIntGraph g1 = new LabeledIntGraph(g, labeledIntValueMapClass);
		CSTNU_NodeSet cstnu = new CSTNU_NodeSet();
		CSTNUCheckStatus status = new CSTNUCheckStatus();
		cstnu.upperCaseRule(g, g1,status);

		LabeledIntEdgePluggable daOk = new LabeledIntEdgePluggable("DA",labeledIntValueMapClass);
		daOk.mergeUpperLabelValue(Label.parse("abc"), "B", 14);
		daOk.mergeUpperLabelValue(Label.parse("ab"), "B", -10);

		assertEquals("Upper Case values:", daOk.getUpperLabelSet(), g1.findEdge(g1.getNode("D"), g1.getNode("A")).getUpperLabelSet());
	}

	/**
	 * Test method for {@link it.univr.di.attic.CSTNU_NodeSet#noCaseRule(LabeledIntGraph, LabeledIntGraph, CSTNUCheckStatus)}.
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testNoCase() {
		// System.out.printf("R1 CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMapClass);
		LabeledNode P = new LabeledNode("P?", 'p');
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(Y);

		LabeledIntEdgePluggable XP = new LabeledIntEdgePluggable("XP",labeledIntValueMapClass);
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬a"), 8);

		LabeledIntEdgePluggable PY = new LabeledIntEdgePluggable("PY",labeledIntValueMapClass);
		PY.mergeLabeledValue(Label.parse("¬b"), 9);
		PY.mergeLabeledValue(Label.parse("b"), 10);

		g.addEdge(XP, X, P);
		g.addEdge(PY, P, Y);

		wellDefinition(g);

		LabeledIntGraph g1 = new LabeledIntGraph(g, labeledIntValueMapClass);

		CSTNU_NodeSet cstnu = new CSTNU_NodeSet();
		CSTNUCheckStatus status = new CSTNUCheckStatus();
		cstnu.noCaseRule(g, g1,status);

		LabeledIntEdgePluggable xyOK = new LabeledIntEdgePluggable("XY",labeledIntValueMapClass);
		xyOK.mergeLabeledValue(Label.parse("¬a¬b"), 17);
		xyOK.mergeLabeledValue(Label.parse("¬ab"), 18);
		xyOK.mergeLabeledValue(Label.parse("¬b"), 19);
		xyOK.mergeLabeledValue(Label.parse("b"), 20);

		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), g1.findEdge(g1.getNode("X"), g1.getNode("Y")).getLabeledValueMap());
		assertEquals("No case: XY upper case labedled values.", xyOK.getUpperLabelSet(), g1.findEdge(g1.getNode("X"), g1.getNode("Y")).getUpperLabelSet());
	}

}
