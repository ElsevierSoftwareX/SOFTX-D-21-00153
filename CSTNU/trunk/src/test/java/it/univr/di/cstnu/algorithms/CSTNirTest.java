/**
 * 
 */
package it.univr.di.cstnu.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.univr.di.cstnu.algorithms.AbstractCSTN.CSTNCheckStatus;
import it.univr.di.cstnu.graph.CSTNEdge;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Label;

/**
 * @author posenato
 */
public class CSTNirTest extends CSTNTest {

	/**
	 * 
	 */
	public CSTNirTest() {
		super();
		this.cstn = new CSTNIR(this.g);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * 
	 */
	@Override

	@Test
	public final void testLabelModificationR0() {

		CSTNEdge px = this.g.getEdgeFactory().get("PX");
		px.mergeLabeledValue(Label.parse("ABp"), -10);
		px.mergeLabeledValue(Label.parse("AB¬p"), 0);
		px.mergeLabeledValue(Label.parse("¬A¬B¬p"), 0);
		px.mergeLabeledValue(Label.parse("C¬p"), 1);
		this.g.addEdge(px, this.P, this.X);

		// wellDefinition();

		this.cstn.labelModificationR0qR0(this.P, this.X, px);

		CSTNEdge pxOK = this.g.getEdgeFactory().get("XY");
		// if R0 is applied!
		pxOK.mergeLabeledValue(Label.parse("AB"), -10);
		pxOK.mergeLabeledValue(Label.parse("¬A¬B¬p"), 0);// ir semantics
		pxOK.mergeLabeledValue(Label.parse("C¬p"), 1);
		// if only qR0 is applied!
		// pxOK.mergeLabeledValue(Label.parse("ABp"), -10);
		// pxOK.mergeLabeledValue(Label.parse("AB¬p"), 0);
		// pxOK.mergeLabeledValue(Label.parse("¬A¬B¬p"), 0);
		// pxOK.mergeLabeledValue(Label.parse("C¬p"), 1);

		assertEquals("R0: p?X labeled values.", pxOK.getLabeledValueMap(), px.getLabeledValueMap());
	}

	/**
	 * 
	 */
	@Override

	@Test
	public final void testLabelModificationR0Z() {

		CSTNEdge px = this.g.getEdgeFactory().get("PX");
		px.mergeLabeledValue(Label.parse("ABp"), -10);
		px.mergeLabeledValue(Label.parse("AB¬p"), 0);
		px.mergeLabeledValue(Label.parse("¬A¬B¬p"), 0);
		this.g.addEdge(px, this.P, this.X);
		this.g.setZ(this.X);
		this.g.addVertex(this.g.getNodeFactory().get("A", 'A'));
		this.g.addVertex(this.g.getNodeFactory().get("B", 'B'));

		try {
			this.cstn.initAndCheck();
		} catch (WellDefinitionException e) {
			e.printStackTrace();
		}
		this.cstn.labelModificationR0qR0(this.P, this.X, px);

		CSTNEdge pxOK = this.g.getEdgeFactory().get("XY");
		pxOK.mergeLabeledValue(Label.parse("AB"), -10);
		pxOK.mergeLabeledValue(Label.emptyLabel, 0);

		assertEquals("R0: P?Z labeled values.", pxOK.getLabeledValueMap(), px.getLabeledValueMap());
	}

	// /**
	// *
	// CSTNCheckStatus)}.
	// */
	// @SuppressWarnings({ "deprecation", "javadoc" })
	// @Test
	// public final void testLabelModificationR2() {
	// TNGraph g = new TNGraph(labeledIntValueMapClass);
	// LabeledNode P = g.getNodeFactory().get("P?", 'p');
	// LabeledNode A = g.getNodeFactory().get("A?", 'a');
	// LabeledNode B = g.getNodeFactory().get("B?", 'b');
	// LabeledNode C = g.getNodeFactory().get("C?", 'c');
	// LabeledNode X = g.getNodeFactory().get("X");
	// g.addVertex(P);
	// g.addVertex(A);
	// g.addVertex(B);
	// g.addVertex(X);
	// g.addVertex(C);
	//
	// CSTNEdge xp = new CSTNEdge("XP", labeledIntValueMapClass);
	// xp.mergeLabeledValue(Label.parse("ABp"), 10);
	// xp.mergeLabeledValue(Label.parse("AB¬p"), 0);
	// xp.mergeLabeledValue(Label.parse("C¬p"), -1);
	// g.addEdge(xp, X, P);
	//
	// wellDefinition();
	// CSTNCheckStatus status = new CSTNCheckStatus();
	//
	// cstn.labelModificationR2(g, P, X, xp, status);
	//
	// CSTNEdge pxOK = new CSTNEdge("XY", labeledIntValueMapClass);
	// //Reaction time = 0
	// pxOK.mergeLabeledValue(Label.parse("AB"), 0);
	// pxOK.mergeLabeledValue(Label.parse("C¬p"), -1);
	// pxOK.mergeLabeledValue(Label.parse("C"), 0);
	// //Reaction time = 1
	// pxOK = new CSTNEdge("XY", labeledIntValueMapClass);
	// pxOK.mergeLabeledValue(Label.parse("AB"), 1);
	// pxOK.mergeLabeledValue(Label.parse("AB¬p"), 0);
	// pxOK.mergeLabeledValue(Label.parse("C"), 1);
	// pxOK.mergeLabeledValue(Label.parse("C¬p"), -1);
	//
	// assertEquals("R2: XP? labeled values.", pxOK.getLabeledValueMap(), xp.getLabeledValueMap());
	// }

	// /**
	// *
	// CSTNCheckStatus)}.
	// */
	// @SuppressWarnings({ "deprecation", "javadoc" })
	// @Test
	// public final void testLabelModificationR1() {
	// // System.out.printf("R1 CASE\n");
	// TNGraph g = new TNGraph(labeledIntValueMapClass);
	// LabeledNode Z = g.getNodeFactory().get("Z");
	// LabeledNode P = g.getNodeFactory().get("P", 'p');
	// LabeledNode X = g.getNodeFactory().get("X");
	// LabeledNode Y = g.getNodeFactory().get("Y");
	// CSTNEdge px = new CSTNEdge("PX", labeledIntValueMapClass);
	// LabeledNode A = g.getNodeFactory().get("A?", 'a');
	// LabeledNode B = g.getNodeFactory().get("B?", 'b');
	// LabeledNode C = g.getNodeFactory().get("C?", 'c');
	// LabeledNode G = g.getNodeFactory().get("G?", 'g');
	// g.addVertex(Z);
	// g.addVertex(P);
	// g.addVertex(A);
	// g.addVertex(B);
	// g.addVertex(G);
	// g.addVertex(C);
	// g.addVertex(X);
	// g.addVertex(Y);
	//
	// px.mergeLabeledValue(Label.parse("¬b"), 0);
	// px.mergeLabeledValue(Label.parse("ab"), -10);
	//
	// CSTNEdge xy = new CSTNEdge("XY", labeledIntValueMapClass);
	// xy.mergeLabeledValue(Label.parse("bgp"), 10);
	// xy.mergeLabeledValue(Label.parse("cp"), -1);
	//
	// g.addEdge(px, P, X);
	// g.addEdge(xy, X, Y);
	//
	// wellDefinition();
	//
	// CSTN cstn1 = new CSTN(1, true, labeledIntValueMapClass);
	// cstn1.labelModificationR1(g, X, Y, Z, xy, new CSTNCheckStatus());
	//
	// CSTNEdge xyOK = new CSTNEdge("XY", labeledIntValueMapClass);
	// xyOK.putLabeledValue(Label.parse("abc"), -1);
	// xyOK.putLabeledValue(Label.parse("¬bc"), -1);
	// xyOK.putLabeledValue(Label.parse("bgp"), 10);
	// xyOK.putLabeledValue(Label.parse("cp"), -1);
	// // xyOK.putLabeledValue(Label.parse("abg"), 10);// se non è instantaneous!
	//
	// assertEquals("R1: xy labeled values.", xyOK.labeledValueSet(), xy.labeledValueSet());
	//
	// cstn1 = new CSTN(1, true, labeledIntValueMapClass);
	//
	// xyOK.removeLabel(Label.parse("abg"));
	// xy.clear();
	// xy.mergeLabeledValue(Label.parse("bgp"), 10);
	// xy.mergeLabeledValue(Label.parse("cp"), -1);
	// cstn1.labelModificationR1(g, X, Y, xy, new CSTNCheckStatus());// test istantaneous
	//
	// assertEquals("R1: xy labeled values.", xyOK.labeledValueSet(), xy.labeledValueSet());
	// }

	@Override
	/**
	 * 
	 */
	@Test
	public final void testLabelModificationR3() {

		LabeledNode Y = this.g.getNodeFactory().get("Y");
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		LabeledNode C = this.g.getNodeFactory().get("C?", 'c');
		LabeledNode G = this.g.getNodeFactory().get("G?", 'g');
		this.g.addVertex(this.P);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(G);
		this.g.addVertex(C);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		CSTNEdge px = this.g.getEdgeFactory().get("PX");
		px.mergeLabeledValue(Label.parse("¬b"), 0);
		px.mergeLabeledValue(Label.parse("ab"), -10);

		CSTNEdge yx = this.g.getEdgeFactory().get("YX");
		yx.mergeLabeledValue(Label.parse("bgp"), -4);
		yx.mergeLabeledValue(Label.parse("cp"), -10);
		yx.mergeLabeledValue(Label.parse("c¬p"), 11);

		this.g.addEdge(px, this.P, this.X);
		this.g.addEdge(yx, Y, this.X);

		wellDefinition();

		// System.out.println(g);

		this.cstn.labelModificationR3qR3(Y, this.X, yx);

		CSTNEdge yxOK = this.g.getEdgeFactory().get("YX");
		// std semantics
		// yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
		// if R3 is applied!
		yxOK.mergeLabeledValue(Label.parse("abc"), -10);
		yxOK.mergeLabeledValue(Label.parse("abg"), -4);
		yxOK.mergeLabeledValue(Label.parse("¬bc"), 0);// std semantics R3 rule with reaction time
		yxOK.mergeLabeledValue(Label.parse("bgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("c¬p"), 11);
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);
		// if only qR3* is applied
		// yxOK.mergeLabeledValue(Label.parse("bgp"), -4);
		// yxOK.mergeLabeledValue(Label.parse("cp"), -10);
		// yxOK.mergeLabeledValue(Label.parse("c¬p"), 11);

		assertEquals("R3: yx labeled values.", yxOK.getLabeledValueSet(), yx.getLabeledValueSet());
	}

	/**
	 * Test method
	 */
	@Override
	@Test
	public final void testLabelModificationR3withUnkown() {

		LabeledNode Y = this.g.getNodeFactory().get("Y");
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		LabeledNode C = this.g.getNodeFactory().get("C?", 'c');
		LabeledNode G = this.g.getNodeFactory().get("G?", 'g');
		this.g.addVertex(this.P);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(G);
		this.g.addVertex(C);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		CSTNEdge pz = this.g.getEdgeFactory().get("PZ");
		pz.mergeLabeledValue(Label.parse("¬b"), -1);
		pz.mergeLabeledValue(Label.parse("ab"), -10);
		pz.mergeLabeledValue(Label.parse("¿c"), -11);

		CSTNEdge xz = this.g.getEdgeFactory().get("XZ");
		xz.mergeLabeledValue(Label.parse("abgp"), -4);
		xz.mergeLabeledValue(Label.parse("abcp"), -10);
		xz.mergeLabeledValue(Label.parse("abc¬p"), -11);
		xz.mergeLabeledValue(Label.parse("ab¿p"), -15);

		this.g.addEdge(pz, this.P, this.Z);
		this.g.addEdge(xz, this.X, this.Z);
		// System.out.println(g);

		this.g.setZ(this.Z);
		this.cstn.labelModificationR3qR3(this.X, this.Z, xz);

		assertEquals("R3: yx labeled values.", AbstractLabeledIntMap.parse("{(abc¬p, -11) (ab¿c, -11) (ab¿p, -15) (ab, -10) }"), xz.getLabeledValueMap());
	}

	/**
	 * Test method for
	 */
	@Override

	@Test
	public final void testLabeledPropagation() {

		LabeledNode Y = this.g.getNodeFactory().get("Y");
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		this.g.addVertex(this.P);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		CSTNEdge XP = this.g.getEdgeFactory().get("XP");
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬a"), 8);
		XP.mergeLabeledValue(Label.parse("¬b"), -1);
		XP.mergeLabeledValue(Label.parse("b"), 9);

		CSTNEdge PY = this.g.getEdgeFactory().get("PY");
		PY.mergeLabeledValue(Label.parse("¬b"), 9);
		PY.mergeLabeledValue(Label.parse("b"), -10);

		CSTNEdge XY = this.g.getEdgeFactory().get("XY");
		this.g.addEdge(XP, this.X, this.P);
		this.g.addEdge(XY, this.X, Y);
		this.g.addEdge(PY, this.P, Y);

		wellDefinition();

		this.cstn.labelPropagation(this.X, this.P, Y, XP, PY, XY);

		// System.out.println(XP);
		// System.out.println(PY);

		CSTNEdge xyOK = this.g.getEdgeFactory().get("XY");
		// xyOK.mergeLabeledValue(Label.parse("¬A¬B"), 17);
		// xyOK.mergeLabeledValue(Label.parse("¬b"), 8);if positive value are not admitted.
		xyOK.mergeLabeledValue(Label.parse("¬ab"), -2);
		xyOK.mergeLabeledValue(Label.parse("b"), -1);
		xyOK.mergeLabeledValue(Label.parse("¿b"), -11);

		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), XY.getLabeledValueMap());

		XP.clear();
		XP.mergeLabeledValue(Label.parse("b"), -1);
		XP.mergeLabeledValue(Label.parse("¬b"), 1);
		XY.clear();
		this.cstn.labelPropagation(this.X, this.P, Y, XP, PY, XY);// Y is Z!!!

		// EqLP+ rule no positive value
		xyOK.clear();
		// xyOK.mergeLabeledValue(Label.parse("¬b"), 10);if positive value are not admitted.
		xyOK.mergeLabeledValue(Label.parse("b"), -11);
		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), XY.getLabeledValueMap());

		XP.clear();
		// System.out.println("xp: " +XP);
		XP.mergeLabeledValue(Label.parse("b"), -1);
		// System.out.println("xp: " +XP);
		XP.mergeLabeledValue(Label.parse("¬b"), -10);
		XY.clear();

		// System.out.println("xp: " +XP);
		// System.out.println("py: " +PY);
		// System.out.println("xy: " +xy);

		this.cstn.labelPropagation(this.X, this.P, Y, XP, PY, XY);// Y is Z!!!

		// System.out.println("xy: " +xy);

		xyOK.mergeLabeledValue(Label.parse("¿b"), -20);
		xyOK.mergeLabeledValue(Label.parse("¬b"), -1);

		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), XY.getLabeledValueMap());
	}

	/**
	 * Test method for
	 */
	@Override

	@Test
	public final void testLabeledPropagation1() {

		LabeledNode Y = this.g.getNodeFactory().get("Y");
		this.g.addVertex(this.P);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		// Ricostruisco i passi di un caso di errore
		CSTNEdge XZ = this.g.getEdgeFactory().get("XZ");
		XZ.mergeLabeledValue(Label.parse("p"), -2);
		XZ.mergeLabeledValue(Label.parse("¿p"), -5);
		XZ.mergeLabeledValue(Label.parse("¬p"), -1);
		XZ.mergeLabeledValue(Label.parse("¿p"), -5);

		CSTNEdge XY = this.g.getEdgeFactory().get("XY");
		XY.mergeLabeledValue(Label.parse("p"), -2);
		XY.mergeLabeledValue(Label.emptyLabel, 0);

		CSTNEdge YZ = this.g.getEdgeFactory().get("YZ");
		YZ.mergeLabeledValue(Label.parse("¬p"), -1);
		YZ.mergeLabeledValue(Label.emptyLabel, 0);

		this.g.addEdge(XY, this.X, Y);
		this.g.addEdge(XZ, this.X, this.Z);
		this.g.addEdge(YZ, Y, this.Z);
		// wellDefinition();

		// System.out.println(g);
		// System.out.println(g1);

		this.cstn.labelPropagation(this.X, Y, this.Z, XY, YZ, XZ);
		this.ok.clear();
		this.ok.mergeLabeledValue("¬p", -1);
		this.ok.mergeLabeledValue("p", -2);
		this.ok.mergeLabeledValue("¿p", -5);
		assertEquals("Label propagation rule with particular values", this.ok.getLabeledValueMap(), XZ.getLabeledValueMap());
		// (¬p, -1, {X, Y}) nel caso in cui accettiamo la propagazione dei node set con valori di edge positivi

		// ❮XZ; normal; {(¬p, -1) (p, -2) (¿p, -5, {X, Y}) }; ❯
		// assertEquals("Label propagation rule with particular values", "❮XZ; normal; {(¬p, -1) (p, -2) (¿p, -5, {X, Y}) }; ❯", XZ.toString());
	}

	/**
	 * Test method for creating a -infty loop
	 */
	@Override

	@Test
	public final void testLabeledPropagation3() {

		LabeledNode Y = this.g.getNodeFactory().get("Y");
		this.g.addVertex(this.P);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		CSTNEdge XY = this.g.getEdgeFactory().get("XY");
		XY.mergeLabeledValue(Label.parse("p"), -2);
		XY.mergeLabeledValue(Label.parse("¿p"), -5);
		XY.mergeLabeledValue(Label.parse("¬p"), 3);
		XY.mergeLabeledValue(Label.parse("¿p"), -5);

		assertEquals("XY: ", AbstractLabeledIntMap.parse("{(3, ¬p) (-2, p) (-5, ¿p) }"), XY.getLabeledValueMap());

		CSTNEdge YX = this.g.getEdgeFactory().get("YX");
		YX.mergeLabeledValue(Label.parse("p"), 3);
		YX.mergeLabeledValue(Label.parse("¬p"), -1);

		this.g.addEdge(XY, this.X, Y);
		this.g.addEdge(YX, Y, this.X);
		// System.out.println(g);

		try {
			this.cstn.initAndCheck();
		} catch (WellDefinitionException e) {
			e.printStackTrace();
		}

		CSTNEdge XX = this.g.getEdgeFactory().get("XX");
		this.g.addEdge(XX, this.X, this.X);
		this.cstn.labelPropagation(this.X, Y, this.X, XY, YX, XX);
		// Remember that not negative value on self loop are never stored!
		assertEquals("XX: ", "{(-∞, ¿p) }", XX.getLabeledValueMap().toString());

		XY.mergeLabeledValue(Label.parse("¬p"), 1);
		// reaction time is 1
		this.cstn.labelPropagation(this.X, Y, this.X, XY, YX, XX);
		assertEquals("XX: ", "{(-∞, ¿p) }", XX.getLabeledValueMap().toString());

	}

	/**
	 * Test method for checking that all propagations are done
	 */
	@Override

	@Test
	public final void testLabeledPropagation4() {

		LabeledNode Q = this.g.getNodeFactory().get("Q?", 'q');
		LabeledNode X2 = this.g.getNodeFactory().get("X2");
		LabeledNode X3 = this.g.getNodeFactory().get("X3");
		LabeledNode X4 = this.g.getNodeFactory().get("X4");
		LabeledNode X5 = this.g.getNodeFactory().get("X5");
		LabeledNode X6 = this.g.getNodeFactory().get("X6");

		this.g.addVertex(this.P);
		this.g.addVertex(Q);
		this.g.addVertex(X2);
		this.g.addVertex(X3);
		this.g.addVertex(X4);
		this.g.addVertex(X5);
		this.g.addVertex(X6);

		CSTNEdge e = this.g.getEdgeFactory().get("X2X4");
		e.mergeLabeledValue(Label.parse("p"), -2);
		this.g.addEdge(e, X2, X4);

		e = this.g.getEdgeFactory().get("X4X3");
		e.mergeLabeledValue(Label.parse("¬p"), -1);
		this.g.addEdge(e, X4, X3);

		e = this.g.getEdgeFactory().get("X3Q");
		e.mergeLabeledValue(Label.parse("p"), -2);
		this.g.addEdge(e, X3, Q);

		e = this.g.getEdgeFactory().get("QX2");
		e.mergeLabeledValue(Label.parse("¬p"), -3);
		this.g.addEdge(e, Q, X2);

		e = this.g.getEdgeFactory().get("X4X5");
		e.mergeLabeledValue(Label.parse("¬q"), -3);
		this.g.addEdge(e, X4, X5);

		e = this.g.getEdgeFactory().get("X5P");
		e.mergeLabeledValue(Label.parse("q"), -2);
		this.g.addEdge(e, X5, this.P);

		e = this.g.getEdgeFactory().get("PX6");
		e.mergeLabeledValue(Label.parse("¬q"), -1);
		this.g.addEdge(e, this.P, X6);

		e = this.g.getEdgeFactory().get("X6X4");
		e.mergeLabeledValue(Label.parse("q"), -2);
		this.g.addEdge(e, X6, X4);

		wellDefinition();

		// System.out.println(g);

		CSTNCheckStatus status;
		try {
			status = this.cstn.dynamicConsistencyCheck();
		} catch (WellDefinitionException e1) {
			e1.printStackTrace();
			assertTrue("Check failed.", false);
			return;
		}
		assertFalse("Check the NOT DC qLoop29morning2qL.cstn. If this message appears, then checking wronlgy says CSTN is consistent!", status.consistency);
	}

	/**
	 * Test method for
	 * 
	 * <pre>
	 * P? &lt;--- X &lt;---- Y
	 * ^
	 * |
	 * |
	 * A_p?         B?
	 * </pre>
	 */
	@Override
	@Test
	public final void testAlphaBetaGamaPrime() {

		LabeledNode Y = this.g.getNodeFactory().get("Y");
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		A.setLabel(Label.parse("p"));

		this.g.addVertex(this.P);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		CSTNEdge XP = this.g.getEdgeFactory().get("XP");
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬ap"), 8);
		XP.mergeLabeledValue(Label.parse("¬b"), -1);
		XP.mergeLabeledValue(Label.parse("bap"), 9);

		CSTNEdge YX = this.g.getEdgeFactory().get("YX");
		YX.mergeLabeledValue(Label.parse("¬b"), 9);
		YX.mergeLabeledValue(Label.parse("b"), -10);

		CSTNEdge AP = this.g.getEdgeFactory().get("AP");
		AP.mergeLabeledValue(Label.parse("p"), -1);

		this.g.addEdge(XP, this.X, this.P);
		this.g.addEdge(YX, Y, this.X);
		this.g.addEdge(AP, A, this.P);

		wellDefinition();

		assertEquals("¬abg",
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("¬ab"), Label.parse("pbg")).toString());
		assertEquals("¬abg",
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("¬ab"), Label.parse("¬pbg")).toString());

		assertEquals("bg",
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("b"), Label.parse("bp¬ag")).toString());
		assertEquals("bg",
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("b"), Label.parse("b¬pg¬a")).toString());

		assertEquals("¿bg",
				this.cstn.makeBetaGammaDagger4qR3(Y, this.P, this.P.getPropositionObserved(), Label.parse("b"), Label.parse("p¬b¬ag")).toString());// 'a'
																																					// in
																																					// 'ba'
																																					// is
																																					// a
																																					// children
																																					// supposed
																																					// not
																																					// to
																																					// be
																																					// present.
		assertEquals("bg",
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("b"), Label.parse("bg¬a")).toString());

		assertEquals("¿bg",
				this.cstn.makeBetaGammaDagger4qR3(Y, this.P, this.P.getPropositionObserved(), Label.parse("¬b"), Label.parse("b¬ag")).toString());
		// assertEquals("bg", this.cstn.makeAlphaBetaGammaPrime(g, Y, X, P, Z, P.getPropositionObserved(), Label.parse("b"), Label.parse("bg¬a")).toString());
	}

	/**
	 * Test method for
	 */
	@Override

	@Test
	public final void testLabeledPropagation2() {

		LabeledNode Y = this.g.getNodeFactory().get("Y");
		this.g.addVertex(this.P);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		ObjectAVLTreeSet<String> nodeSet = new ObjectAVLTreeSet<>();
		nodeSet.add(this.X.getName());
		nodeSet.add(Y.getName());

		// Ricostruisco i passi di un caso di errore
		CSTNEdge YZ = this.g.getEdgeFactory().get("YZ");
		YZ.mergeLabeledValue(Label.emptyLabel, 0);
		CSTNEdge XZ = this.g.getEdgeFactory().get("XZ");
		XZ.mergeLabeledValue(Label.emptyLabel, 0);

		CSTNEdge XY = this.g.getEdgeFactory().get("XY");
		XY.mergeLabeledValue(Label.parse("p"), -2);
		XY.mergeLabeledValue(Label.parse("¬p"), -1);
		XY.mergeLabeledValue(Label.emptyLabel, 0);

		this.g.addEdge(XY, this.X, Y);
		this.g.addEdge(YZ, Y, this.Z);
		this.g.addEdge(XZ, this.X, this.Z);

		this.cstn.labelPropagation(this.X, Y, this.Z, XY, YZ, XZ);

		assertEquals("Label propagation rule with particular values", AbstractLabeledIntMap.parse("{(-1, ¬p) (-2, p) }"), XZ.getLabeledValueMap());
	}

	/**
	 * Test method for
	 */
	@Override

	@Test
	public final void testLabeledPropagationForwardOfInfty() {

		LabeledNode Y = this.g.getNodeFactory().get("Y");
		this.g.addVertex(this.P);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		CSTNEdge YZ = this.g.getEdgeFactory().get("YZ");
		YZ.mergeLabeledValue(Label.emptyLabel, 0);
		CSTNEdge XZ = this.g.getEdgeFactory().get("XZ");
		XZ.mergeLabeledValue(Label.emptyLabel, 0);

		CSTNEdge XY = this.g.getEdgeFactory().get("XY");
		XY.mergeLabeledValue(Label.parse("p"), -2);

		CSTNEdge YX = this.g.getEdgeFactory().get("YX");
		YX.mergeLabeledValue(Label.parse("¬p"), -2);

		CSTNEdge XX = this.g.getEdgeFactory().get("XX");
		CSTNEdge YY = this.g.getEdgeFactory().get("YY");

		this.g.addEdge(XY, this.X, Y);
		this.g.addEdge(YX, Y, this.X);
		this.g.addEdge(YZ, Y, this.Z);
		this.g.addEdge(XZ, this.X, this.Z);
		this.g.addEdge(XX, this.X, this.X);
		this.g.addEdge(YY, Y, Y);

		this.cstn.labelPropagation(this.X, Y, this.Z, XY, YZ, XZ);
		assertEquals("XZ", "{(0, ⊡) (-2, p) }", XZ.getLabeledValueMap().toString());

		this.cstn.labelPropagation(this.X, Y, this.X, XY, YX, XX);
		assertEquals("XX", "{(-∞, ¿p) }", XX.getLabeledValueMap().toString());

		this.cstn.labelPropagation(this.X, this.X, Y, XX, XY, XY);
		// 2018-11-28: infinity forward propagation is useless
		// assertEquals("XY", "{(-2, p) }", XY.getLabeledValueMap().toString());

		this.cstn.labelPropagation(Y, this.X, Y, YX, XY, YY);
		assertEquals("", "{(-∞, ¿p) }", YY.getLabeledValueMap().toString());

	}

	/**
	 * Test method for
	 */
	@Override

	@Test
	public final void testLabeledPropagationForwardOfInfty1() {

		LabeledNode Y = this.g.getNodeFactory().get("Y");
		this.g.addVertex(this.P);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		CSTNEdge YZ = this.g.getEdgeFactory().get("YZ");
		YZ.mergeLabeledValue(Label.emptyLabel, 0);
		CSTNEdge XZ = this.g.getEdgeFactory().get("XZ");
		XZ.mergeLabeledValue(Label.emptyLabel, 0);

		CSTNEdge XY = this.g.getEdgeFactory().get("XY");
		XY.mergeLabeledValue(Label.parse("¿p"), -2);

		CSTNEdge YX = this.g.getEdgeFactory().get("YX");
		YX.mergeLabeledValue(Label.parse("¬p"), -2);

		CSTNEdge XX = this.g.getEdgeFactory().get("XX");
		CSTNEdge YY = this.g.getEdgeFactory().get("YY");

		this.g.addEdge(XY, this.X, Y);
		this.g.addEdge(YX, Y, this.X);
		this.g.addEdge(YZ, Y, this.Z);
		this.g.addEdge(XZ, this.X, this.Z);
		this.g.addEdge(XX, this.X, this.X);
		this.g.addEdge(YY, Y, Y);

		this.cstn.labelPropagation(this.X, Y, this.Z, XY, YZ, XZ);
		// assertEquals("XZ", "{(0, ⊡) }", eNew.getLabeledValueMap().toString());//if only negative value are q-propagate
		assertEquals("XZ", "{(0, ⊡) (-2, ¿p) }", XZ.getLabeledValueMap().toString());// if negative sum value are q-propagate

		this.cstn.labelPropagation(this.X, Y, this.X, XY, YX, XX);
		// assertTrue(eNew == null);//if only negative value are q-propagate

		// g.addEdge(XX, X, X);
		this.cstn.labelPropagation(this.X, this.X, Y, XX, XY, XY);
		// assertEquals("XY", "{(-2, ¿p) }", eNew.getLabeledValueMap().toString());//if only negative value are q-propagate
		// 2018-11-28: infinity forward propagation is useless
		// assertEquals("XY", "{(-∞, ¿p) }", XY.getLabeledValueMap().toString());// if negative sum value are q-propagate
		this.cstn.labelPropagation(Y, this.X, Y, YX, XY, YY);
		assertEquals("", "{(-∞, ¿p) }", YY.getLabeledValueMap().toString());// 2017-10-10: qLabels are not more generated.

		this.cstn.labelPropagation(Y, Y, this.X, YY, YX, YX);
		// 2018-11-28: infinity forward propagation is useless
		assertEquals("", "{(-2, ¬p) (-∞, ¿p) }", YX.getLabeledValueMap().toString());// 2017-10-10: qLabels are not more generated.
	}

	/**
	 * Test method for
	 */
	@Override

	@Test
	public final void testLabeledPropagationBackwardOfInfty() {

		LabeledNode Y = this.g.getNodeFactory().get("Y");
		this.g.addVertex(this.P);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		CSTNEdge YZ = this.g.getEdgeFactory().get("YZ");
		YZ.mergeLabeledValue(Label.emptyLabel, 0);
		CSTNEdge XZ = this.g.getEdgeFactory().get("XZ");
		XZ.mergeLabeledValue(Label.emptyLabel, 0);

		CSTNEdge XY = this.g.getEdgeFactory().get("XY");
		XY.mergeLabeledValue(Label.parse("p"), -2);

		CSTNEdge YX = this.g.getEdgeFactory().get("YX");
		YX.mergeLabeledValue(Label.parse("¬p"), -2);

		CSTNEdge XX = this.g.getEdgeFactory().get("XX");

		this.g.addEdge(XY, this.X, Y);
		this.g.addEdge(YX, Y, this.X);
		this.g.addEdge(YZ, Y, this.Z);
		this.g.addEdge(XZ, this.X, this.Z);
		this.g.addEdge(XX, this.X, this.X);

		this.cstn.labelPropagation(this.X, Y, this.Z, XY, YZ, XZ);
		assertEquals("XZ", "{(0, ⊡) (-2, p) }", XZ.getLabeledValueMap().toString());

		this.cstn.labelPropagation(this.X, Y, this.X, XY, YX, XX);
		assertEquals("XX", "{(-∞, ¿p) }", XX.getLabeledValueMap().toString());// 2017-10-10: qLabels are not more generated.

		this.cstn.labelPropagation(Y, this.X, this.X, YX, XX, YX);
		assertEquals("", "{(-2, ¬p) (-∞, ¿p) }", YX.getLabeledValueMap().toString());
	}

	/**
	 * Test method to check if a tNGraph requiring only R0-R3 application is checked well. .
	 * 
	 * @throws WellDefinitionException
	 */
	@Override
	@Test
	public final void testQstar() throws WellDefinitionException {

		LabeledNode Q = this.g.getNodeFactory().get("Q?", 'q');
		LabeledNode R = this.g.getNodeFactory().get("R?", 'r');

		this.g.addVertex(this.P);
		this.g.addVertex(Q);
		this.g.addVertex(R);

		// Ricostruisco i passi di un caso di errore
		CSTNEdge RZ = this.g.getEdgeFactory().get("RZ");
		RZ.mergeLabeledValue(Label.parse("p¬q"), -16);

		CSTNEdge QZ = this.g.getEdgeFactory().get("QZ");
		QZ.mergeLabeledValue(Label.parse("p¬r"), -14);

		CSTNEdge PZ = this.g.getEdgeFactory().get("PZ");
		PZ.mergeLabeledValue(Label.parse("¬q¬r"), -12);

		this.g.addEdge(RZ, R, this.Z);
		this.g.addEdge(QZ, Q, this.Z);
		this.g.addEdge(PZ, this.P, this.Z);

		this.cstn.dynamicConsistencyCheck();

		CSTNEdge RZnew = this.g.findEdge(R.getName(), this.Z.getName());
		// Std semantics
		this.ok.clear();
		this.ok.mergeLabeledValue("⊡", -12);
		this.ok.mergeLabeledValue("p", -14);
		this.ok.mergeLabeledValue("p¬q", -16);
		// assertEquals("Qstar check"okRZ; normal; {(⊡, -12) (p, -14) (p¬q -16) }; ❯", RZnew.toString()); std semantics
		// assertEquals("Qstar check"okRZ; normal; {(⊡, -13) (p, -15) (p¬q -16) }; ❯", RZnew.toString()); epsilon semantics
		assertEquals("Qstar check", this.ok.getLabeledValueMap(), RZnew.getLabeledValueMap());

		CSTNEdge QZnew = this.g.findEdge(Q.getName(), this.Z.getName());
		this.ok.clear();
		this.ok.mergeLabeledValue("⊡", -12);
		this.ok.mergeLabeledValue("p", -14);
		assertEquals("Qstar check", this.ok.getLabeledValueMap(), QZnew.getLabeledValueMap());

		CSTNEdge PZnew = this.g.findEdge(this.P.getName(), this.Z.getName());
		this.ok.clear();
		this.ok.mergeLabeledValue("⊡", -12);
		assertEquals("Qstar check", this.ok.getLabeledValueMap(), PZnew.getLabeledValueMap());

	}
}
