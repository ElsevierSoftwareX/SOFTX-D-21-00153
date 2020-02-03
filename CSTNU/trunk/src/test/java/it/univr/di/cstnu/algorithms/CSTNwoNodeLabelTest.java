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
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Label;

/**
 * @author posenato
 */
public class CSTNwoNodeLabelTest extends CSTNTest {

	/**
	 * 
	 */
	public CSTNwoNodeLabelTest() {
		super();
		this.cstn = new CSTNwoNodeLabel(this.g);
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
	 * Test method for {@link it.univr.di.cstnu.algorithms.CSTN#labelModificationR0(TNGraph, LabeledNode, LabeledNode, CSTNEdge, CSTNCheckStatus)}.
	 */
	@Override
	@SuppressWarnings("javadoc")
	@Test
	public void testLabelModificationR0() {
		CSTNEdge px = this.g.getEdgeFactory().get("PX");
		px.mergeLabeledValue(Label.parse("ABp"), -10);
		px.mergeLabeledValue(Label.parse("AB¬p"), 0);
		px.mergeLabeledValue(Label.parse("¬A¬B¬p"), 0);
		px.mergeLabeledValue(Label.parse("C¬p"), 1);
		this.g.addEdge(px, this.P, this.X);
		// wellDefinition(g);

		this.cstn.labelModificationR0qR0(this.P, this.X, px);

		CSTNEdge pxOK = this.g.getEdgeFactory().get("XY");
		pxOK.mergeLabeledValue(Label.parse("AB"), -10);
		pxOK.mergeLabeledValue(Label.parse("¬A¬B"), 0);// std semantics
		pxOK.mergeLabeledValue(Label.parse("C¬p"), 1);

		assertEquals("R0: p?X labeled values.", pxOK.getLabeledValueMap(), px.getLabeledValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.CSTN#labelModificationR0(TNGraph, LabeledNode, LabeledNode, CSTNEdge, CSTNCheckStatus)}.
	 */
	@Override
	@SuppressWarnings("javadoc")
	@Test
	public void testLabelModificationR0Z() {
		CSTNEdge pz = this.g.getEdgeFactory().get("PZ");
		pz.mergeLabeledValue(Label.parse("ABp"), -10);
		pz.mergeLabeledValue(Label.parse("AB¬p"), 0);
		pz.mergeLabeledValue(Label.parse("¬A¬B¬p"), 0);
		this.g.addEdge(pz, this.P, this.Z);
		this.g.addVertex(this.g.getNodeFactory().get("A", 'A'));
		this.g.addVertex(this.g.getNodeFactory().get("B", 'B'));

		try {
			this.cstn.initAndCheck();
		} catch (WellDefinitionException e) {
			e.printStackTrace();
		}
		this.cstn.labelModificationR0qR0(this.P, this.Z, pz);

		CSTNEdge pzOK = this.g.getEdgeFactory().get("PZ");
		pzOK.mergeLabeledValue(Label.parse("AB"), -10);
		pzOK.mergeLabeledValue(Label.emptyLabel, 0);

		assertEquals("R0: P?Z labeled values.", pzOK.getLabeledValueMap(), pz.getLabeledValueMap());
	}

	/**
	 */
	@Override
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
		this.g.setZ(this.Z);

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
		yxOK.mergeLabeledValue(Label.parse("abc"), -10);
		yxOK.mergeLabeledValue(Label.parse("abg"), -4);
		yxOK.mergeLabeledValue(Label.parse("¬bc"), 0);// std semantics R3 rule with reaction time
		yxOK.mergeLabeledValue(Label.parse("bgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("c¬p"), 11);
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);

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
		this.g.setZ(this.Z);

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
		this.cstn.labelModificationR3qR3(this.X, this.Z, xz);

		assertEquals("R3: yx labeled values.", AbstractLabeledIntMap.parse("{(-11, abc¬p) (-11, ab¿c) (-15, ab¿p) (-10, ab) }"), xz.getLabeledValueMap());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, CSTNEdge, CSTNEdge, LabeledIntEdge)}
	 * .
	 */
	@Override
	@SuppressWarnings("javadoc")
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

		CSTNEdge XYok = this.g.getEdgeFactory().get("XY");
		// xyOK.mergeLabeledValue(Label.parse("¬A¬B"), 17);
		// EqLP+
		// XYok.mergeLabeledValue(Label.parse("¬b"), 8);if positive value are not admitted.
		XYok.mergeLabeledValue(Label.parse("¬ab"), -2);
		XYok.mergeLabeledValue(Label.parse("b"), -1);
		XYok.mergeLabeledValue(Label.parse("¿b"), -11);


		assertEquals("No case: XY labeled values.", XYok.getLabeledValueMap(), XY.getLabeledValueMap());

		XP.clear();
		XP.mergeLabeledValue(Label.parse("b"), -1);
		XP.mergeLabeledValue(Label.parse("¬b"), 1);
		XY.clear();
		this.cstn.labelPropagation(this.X, this.P, Y, XP, PY, XY);// Y is Z!!!

		XYok.clear();
		// XYok.mergeLabeledValue(Label.parse("¬b"), 10);if positive value are not admitted.
		XYok.mergeLabeledValue(Label.parse("b"), -11);
		assertEquals("No case: XY labeled values.", XYok.getLabeledValueMap(), XY.getLabeledValueMap());

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

		XYok.mergeLabeledValue(Label.parse("¿b"), -20);
		XYok.mergeLabeledValue(Label.parse("¬b"), -1);

		assertEquals("No case: XY labeled values.", XYok.getLabeledValueMap(), XY.getLabeledValueMap());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, CSTNEdge, CSTNEdge, LabeledIntEdge)}
	 * .
	 */
	@Override
	@SuppressWarnings("javadoc")
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
		// wellDefinition(g);

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
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, CSTNEdge, CSTNEdge, LabeledIntEdge)}
	 * .
	 */
	@Override
	@SuppressWarnings("javadoc")
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

		assertEquals("YX: ", AbstractLabeledIntMap.parse("{(-1, ¬p) (3, p) }"), YX.getLabeledValueMap());

		CSTNEdge XX = this.g.getEdgeFactory().get("XX");

		this.g.addEdge(XX, this.X, this.X);
		this.g.addEdge(XY, this.X, Y);
		this.g.addEdge(YX, Y, this.X);
		// System.out.println(g);

		try {
			this.cstn.initAndCheck();
		} catch (WellDefinitionException e) {
			e.printStackTrace();
		}
		assertEquals("XY: ", AbstractLabeledIntMap.parse("{(3, ¬p) (-2, p) (-5, ¿p) }"), XY.getLabeledValueMap());
		assertEquals("YX: ", AbstractLabeledIntMap.parse("{(-1, ¬p) (3, p) }"), YX.getLabeledValueMap());
		assertEquals("XX: ", AbstractLabeledIntMap.parse("{}"), XX.getLabeledValueMap());
		
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
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, CSTNEdge, CSTNEdge, LabeledIntEdge)}
	 * .
	 */
	@Override
	@SuppressWarnings("javadoc")
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
	 * {@link it.univr.di.cstnu.algorithms.CSTN#makeAlphaBetaGammaPrime(TNGraph, LabeledNode, LabeledNode, LabeledNode, LabeledNode, char, Label, Label)
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
	@SuppressWarnings({ "javadoc" })
	@Test
	public final void testAlphaBetaGamaPrime() {
		LabeledNode Y = this.g.getNodeFactory().get("Y");
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		A.setLabel(Label.parse("¬p"));
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		B.setLabel(Label.parse("a¬p"));
		LabeledNode G = this.g.getNodeFactory().get("G?", 'g');

		this.g.addVertex(this.P);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(G);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		CSTNEdge XP = this.g.getEdgeFactory().get("XP");
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬a¬p"), 8);
		XP.mergeLabeledValue(Label.parse("¬b"), -1);
		XP.mergeLabeledValue(Label.parse("ba¬p"), 9);

		CSTNEdge YX = this.g.getEdgeFactory().get("YX");
		YX.mergeLabeledValue(Label.parse("¬b"), 9);
		YX.mergeLabeledValue(Label.parse("b"), -10);

		CSTNEdge AP = this.g.getEdgeFactory().get("AP");
		AP.mergeLabeledValue(Label.parse("¬p"), -1);

		this.g.addEdge(XP, this.X, this.P);
		this.g.addEdge(YX, Y, this.X);
		this.g.addEdge(AP, A, this.P);

		wellDefinition();


		// CHILDREN ARE NOT CONSIDERED because it is assumed a streamlined CSTN
		assertEquals("¬abg",
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("¬ab"), Label.parse("pbg")).toString());

		assertEquals("¬abg",
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("¬ab"), Label.parse("¬pbg")).toString());

		assertEquals("¬abg",
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("b"), Label.parse("bp¬ag")).toString());

		assertEquals("¬abg",
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("b"), Label.parse("b¬pg¬a")).toString());

		B.setLabel(Label.parse("a"));
		// now P has only 'a' as child but it doesn't matter!
		assertEquals("¬a¿bg", // if children are not considered
				this.cstn.makeBetaGammaDagger4qR3(Y, this.P, this.P.getPropositionObserved(), Label.parse("b"), Label.parse("p¬b¬ag")).toString());

		assertEquals("¬abg", // if children are not considered
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("b"), Label.parse("¬pbg¬a")).toString());

		assertEquals("¬a¿bg",//if children are not considered
				this.cstn.makeBetaGammaDagger4qR3(Y, this.P, this.P.getPropositionObserved(), Label.parse("¬b"), Label.parse("b¬apg")).toString());
	}


	
	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#removeChildrenOfUnknown(Label)
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
	@SuppressWarnings({ "javadoc" })
	@Test
	public final void testRemoveChildren() {
		LabeledNode Y = this.g.getNodeFactory().get("Y");
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		A.setLabel(Label.parse("¬p"));
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		B.setLabel(Label.parse("a¬p"));
		LabeledNode G = this.g.getNodeFactory().get("G?", 'g');

		this.g.addVertex(this.P);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(G);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		CSTNEdge XP = this.g.getEdgeFactory().get("XP");
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬a¬p"), 8);
		XP.mergeLabeledValue(Label.parse("¬b"), -1);
		XP.mergeLabeledValue(Label.parse("ba¬p"), 9);

		CSTNEdge YX = this.g.getEdgeFactory().get("YX");
		YX.mergeLabeledValue(Label.parse("¬b"), 9);
		YX.mergeLabeledValue(Label.parse("b"), -10);

		CSTNEdge AP = this.g.getEdgeFactory().get("AP");
		AP.mergeLabeledValue(Label.parse("¬p"), -1);

		this.g.addEdge(XP, this.X, this.P);
		this.g.addEdge(YX, Y, this.X);
		this.g.addEdge(AP, A, this.P);

		wellDefinition();

		assertEquals("a¿b", this.cstn.removeChildrenOfUnknown(Label.parse("a¿b")).toString());
		assertEquals("¿p", this.cstn.removeChildrenOfUnknown(Label.parse("a¿p")).toString());
		assertEquals("¿p", this.cstn.removeChildrenOfUnknown(Label.parse("ab¿p")).toString());
		assertEquals("¿ap", this.cstn.removeChildrenOfUnknown(Label.parse("bp¿a")).toString());
		Label a = Label.parse("a");
		a = a.remove(Label.parse("a"));
		assertEquals(Label.emptyLabel, this.cstn.removeChildrenOfUnknown(a));
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, CSTNEdge, CSTNEdge, LabeledIntEdge)}
	 * .
	 */
	@Override
	@SuppressWarnings("javadoc")
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

		this.cstn.setG(this.g);
		this.cstn.labelPropagation(this.X, Y, this.Z, XY, YZ, XZ);

		assertEquals("Label propagation rule with particular values", AbstractLabeledIntMap.parse("{(-1, ¬p) (-2, p) }"), XZ.getLabeledValueMap());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labeledPropagationRule(LabeledNode, LabeledNode, LabeledNode, CSTNEdge, CSTNEdge, LabeledIntEdge)}
	 * .
	 * * Since in 2017-10-10 -infty qlabeled value have been suppressed, this test is not more necessary!
	 */
//	@SuppressWarnings("javadoc")
//	@Test
//	public final void testLabeledPropagationForwardOfInfty() {
//		TNGraph g = new TNGraph(this.labeledIntValueMapClass);
	// LabeledNode Y = g.getNodeFactory().get("Y");
//		g.addVertex(this.P);
//		g.addVertex(this.X);
//		g.addVertex(Y);
//		g.addVertex(this.Z);
//		g.setZ(this.Z);
//
	// CSTNEdge YZ = this.g.getEdgeFactory().get("YZ");
//		YZ.mergeLabeledValue(Label.emptyLabel, 0);
	// CSTNEdge XZ = this.g.getEdgeFactory().get("XZ");
//		XZ.mergeLabeledValue(Label.emptyLabel, 0);
//
	// CSTNEdge XY = this.g.getEdgeFactory().get("XY");
//		XY.mergeLabeledValue(Label.parse("p"), -2);
//
	// CSTNEdge YX = this.g.getEdgeFactory().get("YX");
//		YX.mergeLabeledValue(Label.parse("¬p"), -2);
//
	// CSTNEdge XX = this.g.getEdgeFactory().get("XX");
	// CSTNEdge YY = this.g.getEdgeFactory().get("YY");
//
//		g.addEdge(XY, this.X, Y);
//		g.addEdge(YX, Y, this.X);
//		g.addEdge(YZ, Y, this.Z);
//		g.addEdge(XZ, this.X, this.Z);
//		g.addEdge(XX, this.X, this.X);
//		g.addEdge(YY, Y, Y);
//
//		this.cstn.setG(g);
//		this.cstn.labeledPropagationRule(this.X, Y, this.Z, XY, YZ, XZ);
//		assertEquals("XZ", "{(0, ⊡) (-2, p) }", XZ.getLabeledValueMap().toString());
//
//		this.cstn.labeledPropagationRule(this.X, Y, this.X, XY, YX, XX);
//		assertEquals("XX", "{(-∞, ¿p) }", XX.getLabeledValueMap().toString());
//
//		this.cstn.labeledPropagationRule(this.X, this.X, Y, XX, XY, XY);
//		assertEquals("XY", "{(-2, p) (-∞, ¿p) }", XY.getLabeledValueMap().toString());
//
//		this.cstn.labeledPropagationRule(Y, this.X, Y, YX, XY, YY);
//		assertEquals("", "{(-∞, ¿p) }", YY.getLabeledValueMap().toString());
//	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labeledPropagationRule(LabeledNode, LabeledNode, LabeledNode, CSTNEdge, CSTNEdge, LabeledIntEdge)}
	 * .
	 * * Since in 2017-10-10 -infty qlabeled value have been suppressed, this test is not more necessary!
	 */
//	@SuppressWarnings("javadoc")
//	@Test
//	public final void testLabeledPropagationForwardOfInfty1() {
//		TNGraph g = new TNGraph(this.labeledIntValueMapClass);
	// LabeledNode Y = g.getNodeFactory().get("Y");
//		g.addVertex(this.P);
//		g.addVertex(this.X);
//		g.addVertex(Y);
//		g.addVertex(this.Z);
//		g.setZ(this.Z);
//
	// CSTNEdge YZ = this.g.getEdgeFactory().get("YZ");
//		YZ.mergeLabeledValue(Label.emptyLabel, 0);
	// CSTNEdge XZ = this.g.getEdgeFactory().get("XZ");
//		XZ.mergeLabeledValue(Label.emptyLabel, 0);
//
	// CSTNEdge XY = this.g.getEdgeFactory().get("XY");
//		XY.mergeLabeledValue(Label.parse("¿p"), -2);
//
	// CSTNEdge YX = this.g.getEdgeFactory().get("YX");
//		YX.mergeLabeledValue(Label.parse("¬p"), -2);
//
	// CSTNEdge XX = this.g.getEdgeFactory().get("XX");
	// CSTNEdge YY = this.g.getEdgeFactory().get("YY");
//
//		g.addEdge(XY, this.X, Y);
//		g.addEdge(YX, Y, this.X);
//		g.addEdge(YZ, Y, this.Z);
//		g.addEdge(XZ, this.X, this.Z);
//		g.addEdge(XX, this.X, this.X);
//		g.addEdge(YY, Y, Y);
//
//		this.cstn.setG(g);
//		this.cstn.labeledPropagationRule(this.X, Y, this.Z, XY, YZ, XZ);
//		// assertEquals("XZ", "{(0, ⊡) }", eNew.getLabeledValueMap().toString());//if only negative value are q-propagate
//		assertEquals("XZ", "{(0, ⊡) (-2, ¿p) }", XZ.getLabeledValueMap().toString());// if negative sum value are q-propagate
//
//		this.cstn.labeledPropagationRule(this.X, Y, this.X, XY, YX, XX);
//		// assertTrue(eNew == null);//if only negative value are q-propagate
//
//		// g.addEdge(XX, X, X);
//		this.cstn.labeledPropagationRule(this.X, this.X, Y, XX, XY, XY);
//		// assertEquals("XY", "{(-2, ¿p) }", eNew.getLabeledValueMap().toString());//if only negative value are q-propagate
//		assertEquals("XY", "{(-∞, ¿p) }", XY.getLabeledValueMap().toString());// if negative sum value are q-propagate
//
//		this.cstn.labeledPropagationRule(Y, this.X, Y, YX, XY, YY);
//		assertEquals("", "{(-∞, ¿p) }", YY.getLabeledValueMap().toString());
//
//		this.cstn.labeledPropagationRule(Y, Y, this.X, YY, YX, YX);
//		assertEquals("", "{(-2, ¬p) (-∞, ¿p) }", YX.getLabeledValueMap().toString());
//	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labeledPropagationRule(LabeledNode, LabeledNode, LabeledNode, CSTNEdge, CSTNEdge, LabeledIntEdge)}
	 * .
	 * * Since in 2017-10-10 -infty qlabeled value have been suppressed, this test is not more necessary!
	 */
//	@SuppressWarnings("javadoc")
//	@Test
//	public final void testLabeledPropagationBackwardOfInfty() {
//		TNGraph g = new TNGraph(this.labeledIntValueMapClass);
	// LabeledNode Y = g.getNodeFactory().get("Y");
//		g.addVertex(this.P);
//		g.addVertex(this.X);
//		g.addVertex(Y);
//		g.addVertex(this.Z);
//		g.setZ(this.Z);
//
	// CSTNEdge YZ = this.g.getEdgeFactory().get("YZ");
//		YZ.mergeLabeledValue(Label.emptyLabel, 0);
	// CSTNEdge XZ = this.g.getEdgeFactory().get("XZ");
//		XZ.mergeLabeledValue(Label.emptyLabel, 0);
//
	// CSTNEdge XY = this.g.getEdgeFactory().get("XY");
//		XY.mergeLabeledValue(Label.parse("p"), -2);
//
	// CSTNEdge YX = this.g.getEdgeFactory().get("YX");
//		YX.mergeLabeledValue(Label.parse("¬p"), -2);
//
	// CSTNEdge XX = this.g.getEdgeFactory().get("XX");
//
//		g.addEdge(XY, this.X, Y);
//		g.addEdge(YX, Y, this.X);
//		g.addEdge(YZ, Y, this.Z);
//		g.addEdge(XZ, this.X, this.Z);
//		g.addEdge(XX, this.X, this.X);
//		this.cstn.setG(g);
//		this.cstn.labeledPropagationRule(this.X, Y, this.Z, XY, YZ, XZ);
//		assertEquals("XZ", "{(0, ⊡) (-2, p) }", XZ.getLabeledValueMap().toString());
//
//		this.cstn.labeledPropagationRule(this.X, Y, this.X, XY, YX, XX);
//		assertEquals("XX", "{(-∞, ¿p) }", XX.getLabeledValueMap().toString());
//
//		this.cstn.labeledPropagationRule(Y, this.X, this.X, YX, XX, YX);
//		assertEquals("", "{(-2, ¬p) (-∞, ¿p) }", YX.getLabeledValueMap().toString());
//	}

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
