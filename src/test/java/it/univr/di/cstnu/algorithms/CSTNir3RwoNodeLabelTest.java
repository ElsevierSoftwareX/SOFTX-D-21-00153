/**
 * 
 */
package it.univr.di.cstnu.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.univr.di.cstnu.algorithms.CSTN.CSTNCheckStatus;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;

/**
 * @author posenato
 */
public class CSTNir3RwoNodeLabelTest extends CSTNTest {

	/**
	 * 
	 */
	public CSTNir3RwoNodeLabelTest() {
		super();
		this.cstn = new CSTNIR3RwoNodeLabels(this.g);
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
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelModificationR0(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdgePluggable, CSTNCheckStatus)}.
	 */
	@Override
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabelModificationR0() {

		LabeledIntEdgePluggable pz = new LabeledIntEdgePluggable("PZ", this.labeledIntValueMapClass);
		pz.mergeLabeledValue(Label.parse("ABp"), -10);
		pz.mergeLabeledValue(Label.parse("AB¬p"), 0);
		pz.mergeLabeledValue(Label.parse("¬A¬B¬p"), 0);
		pz.mergeLabeledValue(Label.parse("C¬p"), 1);
		this.g.addEdge(pz, this.P, this.Z);

		// wellDefinition(g);

		this.cstn.labelModificationR0qR0(this.P, this.Z, pz);

		LabeledIntEdgePluggable pzOK = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		// if R0 is applied!
		pzOK.mergeLabeledValue(Label.parse("AB"), -10);
		pzOK.mergeLabeledValue(Label.parse("¬A¬B¬p"), 0);// ir semantics
		pzOK.mergeLabeledValue(Label.parse("C¬p"), 1);
		// if only qR0 is applied!
		// pxOK.mergeLabeledValue(Label.parse("ABp"), -10);
		// pxOK.mergeLabeledValue(Label.parse("AB¬p"), 0);
		// pxOK.mergeLabeledValue(Label.parse("¬A¬B¬p"), 0);
		// pxOK.mergeLabeledValue(Label.parse("C¬p"), 1);

		assertEquals("R0: p?X labeled values.", pzOK.getLabeledValueMap(), pz.getLabeledValueMap());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelModificationR0(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdgePluggable, CSTNCheckStatus)}.
	 */
	@Override
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabelModificationR0Z() {

		LabeledIntEdgePluggable px = new LabeledIntEdgePluggable("PX", this.labeledIntValueMapClass);
		px.mergeLabeledValue(Label.parse("ABp"), -10);
		px.mergeLabeledValue(Label.parse("AB¬p"), 0);
		px.mergeLabeledValue(Label.parse("¬A¬B¬p"), 0);
		this.g.addEdge(px, this.P, this.X);
		this.g.setZ(this.X);
		this.g.addVertex(new LabeledNode("A", 'A'));
		this.g.addVertex(new LabeledNode("B", 'B'));

		try {
			this.cstn.initAndCheck();
		} catch (WellDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.cstn.Z = this.X;
		this.cstn.labelModificationR0qR0(this.P, this.X, px);

		LabeledIntEdgePluggable pxOK = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		pxOK.mergeLabeledValue(Label.parse("AB"), -10);
		pxOK.mergeLabeledValue(Label.emptyLabel, 0);

		assertEquals("R0: P?Z labeled values.", pxOK.getLabeledValueMap(), px.getLabeledValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTN#labelModificationqR3(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdgePluggable, CSTNCheckStatus)}.
	 * 
	 * @throws WellDefinitionException
	 */
	@Override
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabelModificationR3() {

		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		LabeledNode C = new LabeledNode("C?", 'c');
		LabeledNode G = new LabeledNode("G?", 'g');
		this.g.addVertex(this.P);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(G);
		this.g.addVertex(C);
		this.g.addVertex(Y);


		LabeledIntEdgePluggable pz = new LabeledIntEdgePluggable("PZ", this.labeledIntValueMapClass);
		pz.mergeLabeledValue(Label.parse("¬b"), -1);
		pz.mergeLabeledValue(Label.parse("¬cab"), -10);

		LabeledIntEdgePluggable yz = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
		yz.mergeLabeledValue(Label.parse("bgp"), -4);
		yz.mergeLabeledValue(Label.parse("cp"), -10);
		yz.mergeLabeledValue(Label.parse("c¬p"), 11);

		this.g.addEdge(pz, this.P, this.Z);
		this.g.addEdge(yz, Y, this.Z);

		wellDefinition();
		try {
			this.cstn.initAndCheck();
		} catch (Exception e) {
			fail("Wrong init: " + e.getMessage());
		}

		this.cstn.labelModificationR3qR3(Y, this.Z, yz);

		LabeledIntEdgePluggable yzOK = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
		yzOK.mergeLabeledValue(Label.emptyLabel, 0);// ok
		yzOK.mergeLabeledValue(Label.parse("bgp"), -4);// ok
		yzOK.mergeLabeledValue(Label.parse("cp"), -10);// ok
		yzOK.mergeLabeledValue(Label.parse("c¬p"), 11);// ok
		yzOK.mergeLabeledValue(Label.parse("¿bg"), -1);
		yzOK.mergeLabeledValue(Label.parse("¬cabg"), -4);
		yzOK.mergeLabeledValue(Label.parse("c¬b"), -1);
		yzOK.mergeLabeledValue(Label.parse("a¿cb"), -10);
		yzOK.mergeLabeledValue(Label.parse("c¬b"), -1);
		yzOK.mergeLabeledValue(Label.parse("a¿cb"), -10);

		Assert.assertEquals("No case: XY labeled values.", yzOK.getLabeledValueMap(), yz.getLabeledValueMap());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@Override
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagation() {

		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		this.g.addVertex(this.P);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		LabeledIntEdgePluggable XP = new LabeledIntEdgePluggable("XP", this.labeledIntValueMapClass);
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬a"), 8);
		XP.mergeLabeledValue(Label.parse("¬b"), -1);
		XP.mergeLabeledValue(Label.parse("b"), 9);

		LabeledIntEdgePluggable PY = new LabeledIntEdgePluggable("PY", this.labeledIntValueMapClass);
		PY.mergeLabeledValue(Label.parse("¬b"), 9);
		PY.mergeLabeledValue(Label.parse("b"), -10);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		this.g.addEdge(XP, this.X, this.P);
		this.g.addEdge(XY, this.X, Y);
		this.g.addEdge(PY, this.P, Y);

		wellDefinition();

		this.cstn.labelPropagation(this.X, this.P, Y, XP, PY, XY);

		// System.out.println(XP);
		// System.out.println(PY);

		LabeledIntEdgePluggable xyOK = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		// xyOK.mergeLabeledValue(Label.parse("¬A¬B"), 17);
		// xyOK.mergeLabeledValue(Label.parse("¬b"), 8);if positive value are not admitted.
		xyOK.mergeLabeledValue(Label.parse("¬ab"), -2);
		xyOK.mergeLabeledValue(Label.parse("b"), -1);
		// xyOK.mergeLabeledValue(Label.parse("¿b"), -11);// Propagations to Z does not generate unknown values.

		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), XY.getLabeledValueMap());

		XP.clearLabels();
		XP.mergeLabeledValue(Label.parse("b"), -1);
		XP.mergeLabeledValue(Label.parse("¬b"), 1);
		XY.clear();
		this.cstn.labelPropagation(this.X, this.P, Y, XP, PY, XY);

		// EqLP+ rule no positive value
		xyOK.clearLabels();
		// xyOK.mergeLabeledValue(Label.parse("¬b"), 10);if positive value are not admitted.
		xyOK.mergeLabeledValue(Label.parse("b"), -11);
		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), XY.getLabeledValueMap());

		XP.clearLabels();
		// System.out.println("xp: " +XP);
		XP.mergeLabeledValue(Label.parse("b"), -1);
		// System.out.println("xp: " +XP);
		XP.mergeLabeledValue(Label.parse("¬b"), -10);
		XY.clear();

		// System.out.println("xp: " +XP);
		// System.out.println("py: " +PY);
		// System.out.println("xy: " +xy);

		this.cstn.labelPropagation(this.X, this.P, Y, XP, PY, XY);

		// System.out.println("xy: " +xy);

		// xyOK.mergeLabeledValue(Label.parse("¿b"), -20);// Propagations to Z does not generate unknown values.
		xyOK.mergeLabeledValue(Label.parse("¬b"), -1);

		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), XY.getLabeledValueMap());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@Override
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagation1() {

		LabeledNode Y = new LabeledNode("Y");
		this.g.addVertex(this.P);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);
		this.g.addVertex(this.Z);


		// Ricostruisco i passi di un caso di errore
		LabeledIntEdgePluggable XZ = new LabeledIntEdgePluggable("XZ", this.labeledIntValueMapClass);
		XZ.mergeLabeledValue(Label.parse("p"), -2);
		XZ.mergeLabeledValue(Label.parse("¿p"), -5);
		XZ.mergeLabeledValue(Label.parse("¬p"), -1);
		XZ.mergeLabeledValue(Label.parse("¿p"), -5);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		XY.mergeLabeledValue(Label.parse("p"), -2);
		XY.mergeLabeledValue(Label.emptyLabel, 0);

		LabeledIntEdgePluggable YZ = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
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
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@Override
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagation3() {

		LabeledNode Y = new LabeledNode("Y");
		this.g.addVertex(this.P);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		XY.mergeLabeledValue(Label.parse("p"), -2);
		XY.mergeLabeledValue(Label.parse("¬p"), 3);

		assertEquals("XY: ", AbstractLabeledIntMap.parse("{(3, ¬p) (-2, p) }"), XY.getLabeledValueMap());

		LabeledIntEdgePluggable YX = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
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

		LabeledIntEdgePluggable XX = new LabeledIntEdgePluggable("XX", this.labeledIntValueMapClass);
		this.g.addEdge(XX, this.X, this.X);
		this.cstn.labelPropagation(this.X, Y, this.X, XY, YX, XX);
		// Remember that not negative value on self loop are never stored!
		assertEquals("XX: ", "{}", XX.getLabeledValueMap().toString());

		XY.mergeLabeledValue(Label.parse("¬p"), 1);
		// reaction time is 1
		this.cstn.labelPropagation(this.X, Y, this.X, XY, YX, XX);
		assertEquals("XX: ", "{}", XX.getLabeledValueMap().toString());

	}

	/**
	 * Test method for checking that all propagations are done
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@Override
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagation4() {

		LabeledNode Q = new LabeledNode("Q?", 'q');
		LabeledNode X2 = new LabeledNode("X2");
		LabeledNode X3 = new LabeledNode("X3");
		LabeledNode X4 = new LabeledNode("X4");
		LabeledNode X5 = new LabeledNode("X5");
		LabeledNode X6 = new LabeledNode("X6");
		this.g.addVertex(this.Z);
		this.g.addVertex(this.P);
		this.g.addVertex(Q);
		this.g.addVertex(X2);
		this.g.addVertex(X3);
		this.g.addVertex(X4);
		this.g.addVertex(X5);
		this.g.addVertex(X6);

		LabeledIntEdgePluggable e = new LabeledIntEdgePluggable("X2X4", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("p"), -2);
		this.g.addEdge(e, X2, X4);

		e = new LabeledIntEdgePluggable("X4X3", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("¬p"), -1);
		this.g.addEdge(e, X4, X3);

		e = new LabeledIntEdgePluggable("X3Q", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("p"), -2);
		this.g.addEdge(e, X3, Q);

		e = new LabeledIntEdgePluggable("QX2", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("¬p"), -3);
		this.g.addEdge(e, Q, X2);

		e = new LabeledIntEdgePluggable("X4X5", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("¬q"), -3);
		this.g.addEdge(e, X4, X5);

		e = new LabeledIntEdgePluggable("X5P", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("q"), -2);
		this.g.addEdge(e, X5, this.P);

		e = new LabeledIntEdgePluggable("PX6", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("¬q"), -1);
		this.g.addEdge(e, this.P, X6);

		e = new LabeledIntEdgePluggable("X6X4", this.labeledIntValueMapClass);
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
	 * {@link it.univr.di.cstnu.algorithms.CSTN#makeAlphaBetaGammaPrime(LabeledIntGraph, LabeledNode, LabeledNode, LabeledNode, LabeledNode, char, Label, Label)
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

		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		A.setLabel(Label.parse("p"));

		this.g.addVertex(this.P);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);


		LabeledIntEdgePluggable XP = new LabeledIntEdgePluggable("XP", this.labeledIntValueMapClass);
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬ap"), 8);
		XP.mergeLabeledValue(Label.parse("¬b"), -1);
		XP.mergeLabeledValue(Label.parse("bap"), 9);

		LabeledIntEdgePluggable YX = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		YX.mergeLabeledValue(Label.parse("¬b"), 9);
		YX.mergeLabeledValue(Label.parse("b"), -10);

		LabeledIntEdgePluggable AP = new LabeledIntEdgePluggable("AP", this.labeledIntValueMapClass);
		AP.mergeLabeledValue(Label.parse("p"), -1);

		this.g.addEdge(XP, this.X, this.P);
		this.g.addEdge(YX, Y, this.X);
		this.g.addEdge(AP, A, this.P);

		wellDefinition();

		/**
		 * 'a' in 'ba' is a child supposed not to be present. Here there is no children control... so ¬a is present!
		 */
		assertEquals("¬a¿bg",
				this.cstn.makeBetaGammaDagger4qR3(Y, this.P, this.P.getPropositionObserved(), Label.parse("b"), Label.parse("p¬b¬ag")).toString());

		assertEquals("¬abg",
				this.cstn.makeBetaGammaDagger4qR3(Y, this.P, this.P.getPropositionObserved(), Label.parse("b"), Label.parse("bg¬a")).toString());

		assertEquals("¬a¿bg",
				this.cstn.makeBetaGammaDagger4qR3(Y, this.P, this.P.getPropositionObserved(), Label.parse("¬b"), Label.parse("b¬ag")).toString());
		// assertEquals("bg", this.cstn.makeAlphaBetaGammaPrime(g, Y, X, P, Z, P.getPropositionObserved(), Label.parse("b"), Label.parse("bg¬a")).toString());
	}

	/**
	 * Test method for
	 */
	@Override
	@Test
	public final void testLabeledPropagation2() {

		LabeledNode Y = new LabeledNode("Y");
		this.g.addVertex(this.P);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);
		this.g.addVertex(this.Z);


		ObjectAVLTreeSet<String> nodeSet = new ObjectAVLTreeSet<>();
		nodeSet.add(this.X.getName());
		nodeSet.add(Y.getName());

		// Ricostruisco i passi di un caso di errore
		LabeledIntEdgePluggable YZ = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
		YZ.mergeLabeledValue(Label.emptyLabel, 0);
		LabeledIntEdgePluggable XZ = new LabeledIntEdgePluggable("XZ", this.labeledIntValueMapClass);
		XZ.mergeLabeledValue(Label.emptyLabel, 0);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
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
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@Override
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagationForwardOfInfty() {

		LabeledNode Y = new LabeledNode("Y");
		this.g.addVertex(this.P);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);
		this.g.addVertex(this.Z);


		LabeledIntEdgePluggable YZ = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
		YZ.mergeLabeledValue(Label.emptyLabel, 0);
		LabeledIntEdgePluggable XZ = new LabeledIntEdgePluggable("XZ", this.labeledIntValueMapClass);
		XZ.mergeLabeledValue(Label.emptyLabel, 0);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		XY.mergeLabeledValue(Label.parse("p"), 2);

		LabeledIntEdgePluggable YX = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		YX.mergeLabeledValue(Label.parse("¬p"), -2);

		LabeledIntEdgePluggable XX = new LabeledIntEdgePluggable("XX", this.labeledIntValueMapClass);
		LabeledIntEdgePluggable YY = new LabeledIntEdgePluggable("YY", this.labeledIntValueMapClass);

		this.g.addEdge(XY, this.X, Y);
		this.g.addEdge(YX, Y, this.X);
		this.g.addEdge(YZ, Y, this.Z);
		this.g.addEdge(XZ, this.X, this.Z);
		this.g.addEdge(XX, this.X, this.X);
		this.g.addEdge(YY, Y, Y);


		this.cstn.labelPropagation(this.X, Y, this.Z, XY, YZ, XZ);
		assertEquals("XZ", "{(0, ⊡) }", XZ.getLabeledValueMap().toString());

		this.cstn.labelPropagation(this.X, Y, this.X, XY, YX, XX);
		assertEquals("XX", "{}", XX.getLabeledValueMap().toString());

		this.cstn.labelPropagation(this.X, this.X, Y, XX, XY, XY);
		assertEquals("XY", "{(2, p) }", XY.getLabeledValueMap().toString());

		this.cstn.labelPropagation(Y, this.X, Y, YX, XY, YY);
		assertEquals("", "{}", YY.getLabeledValueMap().toString());

	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@Override
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagationForwardOfInfty1() {

		LabeledNode Y = new LabeledNode("Y");
		this.g.addVertex(this.P);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);
		this.g.addVertex(this.Z);


		LabeledIntEdgePluggable YZ = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
		YZ.mergeLabeledValue(Label.emptyLabel, 0);
		LabeledIntEdgePluggable XZ = new LabeledIntEdgePluggable("XZ", this.labeledIntValueMapClass);
		XZ.mergeLabeledValue(Label.emptyLabel, 0);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		XY.mergeLabeledValue(Label.parse("p"), 2);

		LabeledIntEdgePluggable YX = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		YX.mergeLabeledValue(Label.parse("¬p"), -2);

		LabeledIntEdgePluggable XX = new LabeledIntEdgePluggable("XX", this.labeledIntValueMapClass);
		LabeledIntEdgePluggable YY = new LabeledIntEdgePluggable("YY", this.labeledIntValueMapClass);

		this.g.addEdge(XY, this.X, Y);
		this.g.addEdge(YX, Y, this.X);
		this.g.addEdge(YZ, Y, this.Z);
		this.g.addEdge(XZ, this.X, this.Z);
		this.g.addEdge(XX, this.X, this.X);
		this.g.addEdge(YY, Y, Y);


		this.cstn.labelPropagation(this.X, Y, this.Z, XY, YZ, XZ);
		assertEquals("XZ", "{(0, ⊡) }", XZ.getLabeledValueMap().toString());

		this.cstn.labelPropagation(this.X, Y, this.X, XY, YX, XX);
		// assertTrue(eNew == null);//if only negative value are q-propagate

		// g.addEdge(XX, X, X);
		this.cstn.labelPropagation(this.X, this.X, Y, XX, XY, XY);
		assertEquals("XY", "{(2, p) }", XY.getLabeledValueMap().toString());
		this.cstn.labelPropagation(Y, this.X, Y, YX, XY, YY);
		assertEquals("", "{}", YY.getLabeledValueMap().toString());// 2017-10-10: qLabels are not more generated.

		this.cstn.labelPropagation(Y, Y, this.X, YY, YX, YX);
		assertEquals("", "{(-2, ¬p) }", YX.getLabeledValueMap().toString());// 2017-10-10: qLabels are not more generated.
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@Override
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagationBackwardOfInfty() {

		LabeledNode Y = new LabeledNode("Y");
		this.g.addVertex(this.P);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);
		this.g.addVertex(this.Z);


		LabeledIntEdgePluggable YZ = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
		YZ.mergeLabeledValue(Label.emptyLabel, 0);
		LabeledIntEdgePluggable XZ = new LabeledIntEdgePluggable("XZ", this.labeledIntValueMapClass);
		XZ.mergeLabeledValue(Label.emptyLabel, 0);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		XY.mergeLabeledValue(Label.parse("p"), 2);

		LabeledIntEdgePluggable YX = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		YX.mergeLabeledValue(Label.parse("¬p"), -2);

		LabeledIntEdgePluggable XX = new LabeledIntEdgePluggable("XX", this.labeledIntValueMapClass);

		this.g.addEdge(XY, this.X, Y);
		this.g.addEdge(YX, Y, this.X);
		this.g.addEdge(YZ, Y, this.Z);
		this.g.addEdge(XZ, this.X, this.Z);
		this.g.addEdge(XX, this.X, this.X);

		this.cstn.labelPropagation(this.X, Y, this.Z, XY, YZ, XZ);
		assertEquals("XZ", "{(0, ⊡) }", XZ.getLabeledValueMap().toString());

		this.cstn.labelPropagation(this.X, Y, this.X, XY, YX, XX);
		assertEquals("XX", "{}", XX.getLabeledValueMap().toString());// 2017-10-10: qLabels are not more generated.


		this.cstn.labelPropagation(Y, this.X, this.X, YX, XX, YX);
		assertEquals("", "{(-2, ¬p) }", YX.getLabeledValueMap().toString());
	}

	/**
	 * Test method to check if a graph requiring only R0-R3 application is checked well. .
	 * 
	 * @throws WellDefinitionException
	 */
	@Override
	@Test
	public final void testQstar() throws WellDefinitionException {

		LabeledNode Q = new LabeledNode("Q?", 'q');
		LabeledNode R = new LabeledNode("R?", 'r');
		this.g.addVertex(this.Z);
		this.g.addVertex(this.P);
		this.g.addVertex(Q);
		this.g.addVertex(R);


		// Ricostruisco i passi di un caso di errore
		LabeledIntEdgePluggable RZ = new LabeledIntEdgePluggable("RZ", this.labeledIntValueMapClass);
		RZ.mergeLabeledValue(Label.parse("p¬q"), -16);

		LabeledIntEdgePluggable QZ = new LabeledIntEdgePluggable("QZ", this.labeledIntValueMapClass);
		QZ.mergeLabeledValue(Label.parse("p¬r"), -14);

		LabeledIntEdgePluggable PZ = new LabeledIntEdgePluggable("PZ", this.labeledIntValueMapClass);
		PZ.mergeLabeledValue(Label.parse("¬q¬r"), -12);

		this.g.addEdge(RZ, R, this.Z);
		this.g.addEdge(QZ, Q, this.Z);
		this.g.addEdge(PZ, this.P, this.Z);

		this.cstn.dynamicConsistencyCheck();

		LabeledIntEdgePluggable RZnew = (LabeledIntEdgePluggable) this.g.findEdge(R.getName(), this.Z.getName());
		// Std semantics
		this.ok.clear();
		this.ok.mergeLabeledValue("⊡", -12);
		this.ok.mergeLabeledValue("p", -14);
		this.ok.mergeLabeledValue("p¬q", -16);
		// assertEquals("Qstar check"okRZ; normal; {(⊡, -12) (p, -14) (p¬q -16) }; ❯", RZnew.toString()); std semantics
		// assertEquals("Qstar check"okRZ; normal; {(⊡, -13) (p, -15) (p¬q -16) }; ❯", RZnew.toString()); epsilon semantics
		assertEquals("Qstar check", this.ok.getLabeledValueMap(), RZnew.getLabeledValueMap());

		LabeledIntEdgePluggable QZnew = (LabeledIntEdgePluggable) this.g.findEdge(Q.getName(), this.Z.getName());
		this.ok.clear();
		this.ok.mergeLabeledValue("⊡", -12);
		this.ok.mergeLabeledValue("p", -14);
		assertEquals("Qstar check", this.ok.getLabeledValueMap(), QZnew.getLabeledValueMap());

		LabeledIntEdgePluggable PZnew = (LabeledIntEdgePluggable) this.g.findEdge(this.P.getName(), this.Z.getName());
		this.ok.clear();
		this.ok.mergeLabeledValue("⊡", -12);
		assertEquals("Qstar check", this.ok.getLabeledValueMap(), PZnew.getLabeledValueMap());

	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 */
	@Override
	@SuppressWarnings("javadoc")
	@Test
	public void testLabeledPropagationBackwardOfInfty1() {
		LabeledNode Y = new LabeledNode("Y");
		this.g.addVertex(this.P);
		this.g.addVertex(this.X);
		this.g.addVertex(Y);

		// this.Z.potentialPut(Label.parse("p"), Constants.INT_NEG_INFINITE);
		this.Z.putPotential(Label.parse("¿p"), Constants.INT_NEG_INFINITE);
		assertEquals("Z", "{(-∞, ¿p) }", this.Z.getPotential().toString());

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		XY.mergeLabeledValue(Label.parse("p"), -2);
		XY.mergeLabeledValue(Label.emptyLabel, -1);
		this.g.addEdge(XY, this.X, Y);
		assertEquals("XY", "{(-1, " + Label.emptyLabel + ") (-2, p) }", XY.getLabeledValueMap().toString());

		LabeledIntEdgePluggable YZ = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
		YZ.mergeLabeledValue(Label.parse("¬p"), -1);
		this.g.addEdge(YZ, Y, this.Z);

		LabeledIntEdgePluggable XZ = new LabeledIntEdgePluggable("XZ", this.labeledIntValueMapClass);
		this.g.addEdge(XZ, this.X, this.Z);


		this.cstn.labelPropagation(this.X, Y, this.Z, XY, YZ, XZ);
		// assertEquals("XZ", "{(-2, ¬p) (-3, ¿p) }", XZ.getLabeledValueMap().toString());// Propagations to Z does not generate unknown values.
		// Z contains a negative loop (forced). At first propagation to X (label p), the
		// method finds the negative loop, stores it, and returns.
		this.cstn.potentialR3(this.X, this.Z, XZ, null);
		assertEquals("X", "{(-∞, ¿p) }", this.X.getPotential().toString());
		assertEquals("Status", true, this.cstn.checkStatus.consistency);
	}

}
