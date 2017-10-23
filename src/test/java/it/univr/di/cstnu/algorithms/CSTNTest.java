/**
 * 
 */
package it.univr.di.cstnu.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.univr.di.cstnu.algorithms.CSTN;
import it.univr.di.cstnu.algorithms.WellDefinitionException;
import it.univr.di.cstnu.algorithms.CSTN.CSTNCheckStatus;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntMapFactory;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * @author posenato
 */
public class CSTNTest {

	@SuppressWarnings("javadoc")
	final Class<? extends LabeledIntMap> labeledIntValueMapClass = (new LabeledIntTreeMap()).getClass();

	/**
	 * 
	 */
	final LabeledIntMapFactory<? extends LabeledIntMap> labeledIntMapFactory = new LabeledIntMapFactory<>(this.labeledIntValueMapClass);

	/**
	 * 
	 */
	CSTN cstn;
	/**
	 * 
	 */
	LabeledNode Z = new LabeledNode("Z");
	/**
	 * 
	 */
	LabeledNode P = new LabeledNode("P", 'p');
	@SuppressWarnings("javadoc")
	LabeledNode X = new LabeledNode("X");

	/**
	 * 
	 */
	LabeledIntEdgePluggable ok = new LabeledIntEdgePluggable("OK", this.labeledIntValueMapClass);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.cstn = new CSTN(new LabeledIntGraph(LabeledIntTreeMap.class));
	}

	/**
	 * @param g
	 */
	private final void wellDefinition(LabeledIntGraph g) {
		this.cstn.setG(g);
		try {
			this.cstn.checkWellDefinitionProperties();
		} catch (WellDefinitionException e) {
			fail("LabeledIntGraph<LabeledIntEdgePluggable> not well formed: " + e.getMessage());
		}
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.CSTN#labelModificationR0(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdgePluggable, CSTNCheckStatus)}.
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabelModificationR0() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledIntEdgePluggable px = new LabeledIntEdgePluggable("PX", this.labeledIntValueMapClass);
		px.mergeLabeledValue(Label.parse("ABp"), -10);
		px.mergeLabeledValue(Label.parse("AB¬p"), 0);
		px.mergeLabeledValue(Label.parse("¬A¬B¬p"), 0);
		px.mergeLabeledValue(Label.parse("C¬p"), 1);
		g.addEdge(px, this.P, this.X);
		g.setZ(this.Z);
		// wellDefinition(g);

		this.cstn.setG(g);
		this.cstn.labelModificationR0(this.P, this.X, this.Z, px);

		LabeledIntEdgePluggable pxOK = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		//if R0 is applied
		pxOK.mergeLabeledValue(Label.parse("AB"), -10);
		pxOK.mergeLabeledValue(Label.parse("¬A¬B"), 0);// std semantics
		pxOK.mergeLabeledValue(Label.parse("C¬p"), 1);
		//if only qR0 is applied
//		pxOK.mergeLabeledValue(Label.parse("ABp"), -10);
//		pxOK.mergeLabeledValue(Label.parse("AB¬p"), 0);
//		pxOK.mergeLabeledValue(Label.parse("¬A¬B¬p"), 0);
//		pxOK.mergeLabeledValue(Label.parse("C¬p"), 1);
		
		assertEquals("R0: p?X labeled values.", pxOK.getLabeledValueMap(), px.getLabeledValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.CSTN#labelModificationR0(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdgePluggable, CSTNCheckStatus)}.
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabelModificationR0Z() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledIntEdgePluggable px = new LabeledIntEdgePluggable("PX", this.labeledIntValueMapClass);
		px.mergeLabeledValue(Label.parse("ABp"), -10);
		px.mergeLabeledValue(Label.parse("AB¬p"), 0);
		px.mergeLabeledValue(Label.parse("¬A¬B¬p"), 0);
		g.addEdge(px, this.P, this.X);
		g.setZ(this.X);
		g.addVertex(new LabeledNode("A", 'A'));
		g.addVertex(new LabeledNode("B", 'B'));

		this.cstn.setG(g);
		try {
			this.cstn.initAndCheck();
		} catch (WellDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.cstn.labelModificationR0(this.P, this.X, this.X, px);

		LabeledIntEdgePluggable pxOK = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		pxOK.mergeLabeledValue(Label.parse("AB"), -10);
		pxOK.mergeLabeledValue(Label.emptyLabel, 0);

		assertEquals("R0: P?Z labeled values.", pxOK.getLabeledValueMap(), px.getLabeledValueMap());
	}

	// /**
	// * Test method for {@link it.univr.di.cstnu.CSTN#labelModificationR2(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdgePluggable,
	// CSTNCheckStatus)}.
	// */
	// @SuppressWarnings({ "deprecation", "javadoc" })
	// @Test
	// public final void testLabelModificationR2() {
	// LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMapClass);
	// LabeledNode P = new LabeledNode("P?", 'p');
	// LabeledNode A = new LabeledNode("A?", 'a');
	// LabeledNode B = new LabeledNode("B?", 'b');
	// LabeledNode C = new LabeledNode("C?", 'c');
	// LabeledNode X = new LabeledNode("X");
	// g.addVertex(P);
	// g.addVertex(A);
	// g.addVertex(B);
	// g.addVertex(X);
	// g.addVertex(C);
	//
	// LabeledIntEdgePluggable xp = new LabeledIntEdgePluggable("XP", labeledIntValueMapClass);
	// xp.mergeLabeledValue(Label.parse("ABp"), 10);
	// xp.mergeLabeledValue(Label.parse("AB¬p"), 0);
	// xp.mergeLabeledValue(Label.parse("C¬p"), -1);
	// g.addEdge(xp, X, P);
	//
	// wellDefinition(g);
	// CSTNCheckStatus status = new CSTNCheckStatus();
	//
	// cstn.labelModificationR2(g, P, X, xp, status);
	//
	// LabeledIntEdgePluggable pxOK = new LabeledIntEdgePluggable("XY", labeledIntValueMapClass);
	// //Reaction time = 0
	// pxOK.mergeLabeledValue(Label.parse("AB"), 0);
	// pxOK.mergeLabeledValue(Label.parse("C¬p"), -1);
	// pxOK.mergeLabeledValue(Label.parse("C"), 0);
	// //Reaction time = 1
	// pxOK = new LabeledIntEdgePluggable("XY", labeledIntValueMapClass);
	// pxOK.mergeLabeledValue(Label.parse("AB"), 1);
	// pxOK.mergeLabeledValue(Label.parse("AB¬p"), 0);
	// pxOK.mergeLabeledValue(Label.parse("C"), 1);
	// pxOK.mergeLabeledValue(Label.parse("C¬p"), -1);
	//
	// assertEquals("R2: XP? labeled values.", pxOK.getLabeledValueMap(), xp.getLabeledValueMap());
	// }

	// /**
	// * Test method for {@link it.univr.di.cstnu.CSTN#labelModificationR1(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdgePluggable,
	// CSTNCheckStatus)}.
	// */
	// @SuppressWarnings({ "deprecation", "javadoc" })
	// @Test
	// public final void testLabelModificationR1() {
	// // System.out.printf("R1 CASE\n");
	// LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMapClass);
	// LabeledNode Z = new LabeledNode("Z");
	// LabeledNode P = new LabeledNode("P", 'p');
	// LabeledNode X = new LabeledNode("X");
	// LabeledNode Y = new LabeledNode("Y");
	// LabeledIntEdgePluggable px = new LabeledIntEdgePluggable("PX", labeledIntValueMapClass);
	// LabeledNode A = new LabeledNode("A?", 'a');
	// LabeledNode B = new LabeledNode("B?", 'b');
	// LabeledNode C = new LabeledNode("C?", 'c');
	// LabeledNode G = new LabeledNode("G?", 'g');
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
	// LabeledIntEdgePluggable xy = new LabeledIntEdgePluggable("XY", labeledIntValueMapClass);
	// xy.mergeLabeledValue(Label.parse("bgp"), 10);
	// xy.mergeLabeledValue(Label.parse("cp"), -1);
	//
	// g.addEdge(px, P, X);
	// g.addEdge(xy, X, Y);
	//
	// wellDefinition(g);
	//
	// CSTN cstn1 = new CSTN(1, true, labeledIntValueMapClass);
	// cstn1.labelModificationR1(g, X, Y, Z, xy, new CSTNCheckStatus());
	//
	// LabeledIntEdgePluggable xyOK = new LabeledIntEdgePluggable("XY", labeledIntValueMapClass);
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

	@SuppressWarnings("javadoc")
	/**
	 * Test method for {@link it.univr.di.cstnu.CSTN#labelModificationR3(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdgePluggable, CSTNCheckStatus)}.
	 */
	@Test
	public final void testLabelModificationR3() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		LabeledNode C = new LabeledNode("C?", 'c');
		LabeledNode G = new LabeledNode("G?", 'g');
		g.addVertex(this.P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(G);
		g.addVertex(C);
		g.addVertex(this.X);
		g.addVertex(Y);
		g.setZ(this.Z);

		LabeledIntEdgePluggable px = new LabeledIntEdgePluggable("PX", this.labeledIntValueMapClass);
		px.mergeLabeledValue(Label.parse("¬b"), 0);
		px.mergeLabeledValue(Label.parse("ab"), -10);

		LabeledIntEdgePluggable yx = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		yx.mergeLabeledValue(Label.parse("bgp"), -4);
		yx.mergeLabeledValue(Label.parse("cp"), -10);
		yx.mergeLabeledValue(Label.parse("c¬p"), 11);

		g.addEdge(px, this.P, this.X);
		g.addEdge(yx, Y, this.X);

		wellDefinition(g);

		// System.out.println(g);

		this.cstn.labelModificationR3(Y, this.X, this.Z, yx);

		LabeledIntEdgePluggable yxOK = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		// std semantics
		// yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
		// if R3* is applied
		yxOK.mergeLabeledValue(Label.parse("abc"), -10);
		yxOK.mergeLabeledValue(Label.parse("abg"), -4);
		yxOK.mergeLabeledValue(Label.parse("¬bc"), 0);// std semantics R3 rule with reaction time
		yxOK.mergeLabeledValue(Label.parse("bgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("c¬p"), 11);
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);
		//if only qR3* is applied
//		yxOK.mergeLabeledValue(Label.parse("bgp"), -4);
//		yxOK.mergeLabeledValue(Label.parse("cp"), -10);
//		yxOK.mergeLabeledValue(Label.parse("c¬p"), 11);

		
		
		assertEquals("R3: yx labeled values.", yxOK.getLabeledValueSet(), yx.getLabeledValueSet());
	}

	/**
	 * Test method
	 */
	@Test
	public final void testLabelModificationR3withUnkown() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		LabeledNode C = new LabeledNode("C?", 'c');
		LabeledNode G = new LabeledNode("G?", 'g');
		g.addVertex(this.P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(G);
		g.addVertex(C);
		g.addVertex(this.X);
		g.addVertex(Y);
		g.setZ(this.Z);

		LabeledIntEdgePluggable pz = new LabeledIntEdgePluggable("PZ", this.labeledIntValueMapClass);
		pz.mergeLabeledValue(Label.parse("¬b"), -1);
		pz.mergeLabeledValue(Label.parse("ab"), -10);
		pz.mergeLabeledValue(Label.parse("¿c"), -11);

		LabeledIntEdgePluggable xz = new LabeledIntEdgePluggable("XZ", this.labeledIntValueMapClass);
		xz.mergeLabeledValue(Label.parse("abgp"), -4);
		xz.mergeLabeledValue(Label.parse("abcp"), -10);
		xz.mergeLabeledValue(Label.parse("abc¬p"), -11);
		xz.mergeLabeledValue(Label.parse("ab¿p"), -15);

		g.addEdge(pz, this.P, this.Z);
		g.addEdge(xz, this.X, this.Z);
		// System.out.println(g);
		this.cstn.setG(g);

		this.cstn.labelModificationR3(this.X, this.Z, this.Z, xz);

		assertEquals("R3: yx labeled values.", "{abc¬p->-11, ab¿p->-15, ab¿c->-11, ab->-10}", xz.getLabeledValueSet().toString());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labeledPropagationRule(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagation() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		g.addVertex(this.P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(this.X);
		g.addVertex(Y);

		LabeledIntEdgePluggable XP = new LabeledIntEdgePluggable("XP", this.labeledIntValueMapClass);
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬a"), 8);
		XP.mergeLabeledValue(Label.parse("¬b"), -1);
		XP.mergeLabeledValue(Label.parse("b"), 9);

		LabeledIntEdgePluggable PY = new LabeledIntEdgePluggable("PY", this.labeledIntValueMapClass);
		PY.mergeLabeledValue(Label.parse("¬b"), 9);
		PY.mergeLabeledValue(Label.parse("b"), -10);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		
		g.addEdge(XP, this.X, this.P);
		g.addEdge(XY, this.X, Y);
		g.addEdge(PY, this.P, Y);

		wellDefinition(g);

		this.cstn.labeledPropagationRule(this.X, this.P, Y, XP, PY, XY);

		// System.out.println(XP);
		// System.out.println(PY);

		LabeledIntEdgePluggable XYok = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		// xyOK.mergeLabeledValue(Label.parse("¬A¬B"), 17);
		// EqLP+
		XYok.mergeLabeledValue(Label.parse("¬b"), 8);
		XYok.mergeLabeledValue(Label.parse("¬ab"), -2);
		XYok.mergeLabeledValue(Label.parse("b"), -1);
//		XYok.mergeLabeledValue(Label.parse("¿b"), -11);//2017-10-10: qLabels are not more generated.


		assertEquals("No case: XY labeled values.", XYok.getLabeledValueMap(), XY.getLabeledValueMap());

		XP.clearLabels();
		XP.mergeLabeledValue(Label.parse("b"), -1);
		XP.mergeLabeledValue(Label.parse("¬b"), 1);
		XY.clear();
		this.cstn.labeledPropagationRule(this.X, this.P, Y, XP, PY, XY);// Y is Z!!!

		XYok.clearLabels();
		XYok.mergeLabeledValue(Label.parse("¬b"), 10);
		XYok.mergeLabeledValue(Label.parse("b"), -11);
		assertEquals("No case: XY labeled values.", XYok.getLabeledValueMap(), XY.getLabeledValueMap());

		XP.clearLabels();
		// System.out.println("xp: " +XP);
		XP.mergeLabeledValue(Label.parse("b"), -1);
		// System.out.println("xp: " +XP);
		XP.mergeLabeledValue(Label.parse("¬b"), -10);
		XY.clear();

		// System.out.println("xp: " +XP);
		// System.out.println("py: " +PY);
		// System.out.println("xy: " +xy);

		this.cstn.labeledPropagationRule(this.X, this.P, Y, XP, PY, XY);// Y is Z!!!

		// System.out.println("xy: " +xy);

//		XYok.mergeLabeledValue(Label.parse("¿b"), -20);//2017-10-10: qLabels are not more generated.
		XYok.mergeLabeledValue(Label.parse("¬b"), -1);

		assertEquals("No case: XY labeled values.", XYok.getLabeledValueMap(), XY.getLabeledValueMap());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labeledPropagationRule(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagation1() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode Y = new LabeledNode("Y");
		g.addVertex(this.P);
		g.addVertex(this.X);
		g.addVertex(Y);
		g.addVertex(this.Z);
		g.setZ(this.Z);

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

		g.addEdge(XY, this.X, Y);
		g.addEdge(XZ, this.X, this.Z);
		g.addEdge(YZ, Y, this.Z);
		// wellDefinition(g);

		// System.out.println(g);
		// System.out.println(g1);
		this.cstn.setG(g);

		this.cstn.labeledPropagationRule(this.X, Y, this.Z, XY, YZ, XZ);
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
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labeledPropagationRule(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagation3() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode Y = new LabeledNode("Y");
		g.addVertex(this.P);
		g.addVertex(this.X);
		g.addVertex(Y);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		XY.mergeLabeledValue(Label.parse("p"), -2);
		XY.mergeLabeledValue(Label.parse("¿p"), -5);
		XY.mergeLabeledValue(Label.parse("¬p"), 3);
		XY.mergeLabeledValue(Label.parse("¿p"), -5);

		assertEquals("XY: ", "{(3, ¬p) (-2, p) (-5, ¿p) }", XY.getLabeledValueMap().toString());

		LabeledIntEdgePluggable YX = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		YX.mergeLabeledValue(Label.parse("p"), 3);
		YX.mergeLabeledValue(Label.parse("¬p"), -1);

		LabeledIntEdgePluggable XX = new LabeledIntEdgePluggable("XX", this.labeledIntValueMapClass);

		g.addEdge(XX, this.X, this.X);
		g.addEdge(XY, this.X, Y);
		g.addEdge(YX, Y, this.X);
		// System.out.println(g);

		this.cstn.setG(g);
		try {
			this.cstn.initAndCheck();
		} catch (WellDefinitionException e) {
			e.printStackTrace();
		}

		this.cstn.labeledPropagationRule(this.X, Y, this.X, XY, YX, XX);
		// Remember that not negative value on self loop are never stored!
		assertEquals("XX: ", "{}", XX.getLabeledValueMap().toString());//2017-10-10: qLabels are not more generated.

		XY.mergeLabeledValue(Label.parse("¬p"), 1);
		// reaction time is 1
		this.cstn.labeledPropagationRule(this.X, Y, this.X, XY, YX, XX);
		assertEquals("XX: ", "{}", XX.getLabeledValueMap().toString());//2017-10-10: qLabels are not more generated.
	}

	/**
	 * Test method for checking that all propagations are done
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labeledPropagationRule(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagation4() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode Q = new LabeledNode("Q?", 'q');
		LabeledNode X2 = new LabeledNode("X2");
		LabeledNode X3 = new LabeledNode("X3");
		LabeledNode X4 = new LabeledNode("X4");
		LabeledNode X5 = new LabeledNode("X5");
		LabeledNode X6 = new LabeledNode("X6");
		g.addVertex(this.Z);
		g.addVertex(this.P);
		g.addVertex(Q);
		g.addVertex(X2);
		g.addVertex(X3);
		g.addVertex(X4);
		g.addVertex(X5);
		g.addVertex(X6);

		LabeledIntEdgePluggable e = new LabeledIntEdgePluggable("X2X4", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("p"), -2);
		g.addEdge(e, X2, X4);

		e = new LabeledIntEdgePluggable("X4X3", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("¬p"), -1);
		g.addEdge(e, X4, X3);

		e = new LabeledIntEdgePluggable("X3Q", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("p"), -2);
		g.addEdge(e, X3, Q);

		e = new LabeledIntEdgePluggable("QX2", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("¬p"), -3);
		g.addEdge(e, Q, X2);

		e = new LabeledIntEdgePluggable("X4X5", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("¬q"), -3);
		g.addEdge(e, X4, X5);

		e = new LabeledIntEdgePluggable("X5P", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("q"), -2);
		g.addEdge(e, X5, this.P);

		e = new LabeledIntEdgePluggable("PX6", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("¬q"), -1);
		g.addEdge(e, this.P, X6);

		e = new LabeledIntEdgePluggable("X6X4", this.labeledIntValueMapClass);
		e.mergeLabeledValue(Label.parse("q"), -2);
		g.addEdge(e, X6, X4);

		wellDefinition(g);

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
	@SuppressWarnings({ "javadoc" })
	@Test
	public final void testAlphaBetaGamaPrime() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		A.setLabel(Label.parse("¬p"));
		LabeledNode B = new LabeledNode("B?", 'b');
		B.setLabel(Label.parse("a¬p"));
		LabeledNode G = new LabeledNode("G?", 'g');

		g.addVertex(this.P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(G);
		g.addVertex(this.X);
		g.addVertex(Y);
		g.setZ(this.Z);

		LabeledIntEdgePluggable XP = new LabeledIntEdgePluggable("XP", this.labeledIntValueMapClass);
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬a¬p"), 8);
		XP.mergeLabeledValue(Label.parse("¬b"), -1);
		XP.mergeLabeledValue(Label.parse("ba¬p"), 9);

		LabeledIntEdgePluggable YX = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		YX.mergeLabeledValue(Label.parse("¬b"), 9);
		YX.mergeLabeledValue(Label.parse("b"), -10);

		LabeledIntEdgePluggable AP = new LabeledIntEdgePluggable("AP", this.labeledIntValueMapClass);
		AP.mergeLabeledValue(Label.parse("¬p"), -1);

		g.addEdge(XP, this.X, this.P);
		g.addEdge(YX, Y, this.X);
		g.addEdge(AP, A, this.P);

		wellDefinition(g);


		assertEquals("¬abg",
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("¬ab"), Label.parse("pbg")).toString());
		assertEquals("¬abg",
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("¬ab"), Label.parse("¬pbg")).toString());

		assertEquals("bg",
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("b"), Label.parse("bp¬ag")).toString());
		assertEquals("bg",
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("b"), Label.parse("b¬pg¬a")).toString());
		/*
		 * 'a' in 'ba' is a children supposed not to be present.
		 */
		B.setLabel(Label.parse("a"));
		assertEquals("¿bg",
				this.cstn.makeBetaGammaDagger4qR3(Y, this.Z, this.P, this.P.getPropositionObserved(), Label.parse("b"), Label.parse("p¬b¬ag")).toString());
		assertEquals("bg",
				this.cstn.makeAlphaBetaGammaPrime4R3(Y, this.X, this.P, this.P.getPropositionObserved(), Label.parse("b"), Label.parse("¬pbg¬a")).toString());
		assertEquals("¿bg",
				this.cstn.makeBetaGammaDagger4qR3(Y, this.Z, this.P, this.P.getPropositionObserved(), Label.parse("¬b"), Label.parse("b¬apg")).toString());
		// assertEquals("bg", CSTN.makeAlphaBetaGammaPrime(g, Y, X, P, Z, P.getPropositionObserved(), Label.parse("b"), Label.parse("bg¬a")).toString());
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
	@SuppressWarnings({ "javadoc" })
	@Test
	public final void testRemoveChildren() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		A.setLabel(Label.parse("¬p"));
		LabeledNode B = new LabeledNode("B?", 'b');
		B.setLabel(Label.parse("a¬p"));
		LabeledNode G = new LabeledNode("G?", 'g');

		g.addVertex(this.P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(G);
		g.addVertex(this.X);
		g.addVertex(Y);
		g.setZ(this.Z);

		LabeledIntEdgePluggable XP = new LabeledIntEdgePluggable("XP", this.labeledIntValueMapClass);
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬a¬p"), 8);
		XP.mergeLabeledValue(Label.parse("¬b"), -1);
		XP.mergeLabeledValue(Label.parse("ba¬p"), 9);

		LabeledIntEdgePluggable YX = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		YX.mergeLabeledValue(Label.parse("¬b"), 9);
		YX.mergeLabeledValue(Label.parse("b"), -10);

		LabeledIntEdgePluggable AP = new LabeledIntEdgePluggable("AP", this.labeledIntValueMapClass);
		AP.mergeLabeledValue(Label.parse("¬p"), -1);

		g.addEdge(XP, this.X, this.P);
		g.addEdge(YX, Y, this.X);
		g.addEdge(AP, A, this.P);

		wellDefinition(g);

		assertEquals("a¿b", this.cstn.removeChildrenOfUnknown(Label.parse("a¿b")).toString());
		assertEquals("¿p", this.cstn.removeChildrenOfUnknown(Label.parse("a¿p")).toString());
		assertEquals("¿p", this.cstn.removeChildrenOfUnknown(Label.parse("ab¿p")).toString());
		assertEquals("¿ap", this.cstn.removeChildrenOfUnknown(Label.parse("bp¿a")).toString());
		Label a = Label.parse("a");
		a.remove(Label.parse("a"));
		assertEquals(Label.emptyLabel, this.cstn.removeChildrenOfUnknown(a));

	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labeledPropagationRule(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagation2() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode Y = new LabeledNode("Y");
		g.addVertex(this.P);
		g.addVertex(this.X);
		g.addVertex(Y);
		g.addVertex(this.Z);
		g.setZ(this.Z);

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

		g.addEdge(XY, this.X, Y);
		g.addEdge(YZ, Y, this.Z);
		g.addEdge(XZ, this.X, this.Z);

		this.cstn.setG(g);
		this.cstn.labeledPropagationRule(this.X, Y, this.Z, XY, YZ, XZ);

		assertEquals("Label propagation rule with particular values", "{(-1, ¬p) (-2, p) }", XZ.getLabeledValueMap().toString());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labeledPropagationRule(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagationForwardOfInfty() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode Y = new LabeledNode("Y");
		g.addVertex(this.P);
		g.addVertex(this.X);
		g.addVertex(Y);
		g.addVertex(this.Z);
		g.setZ(this.Z);

		LabeledIntEdgePluggable YZ = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
		YZ.mergeLabeledValue(Label.emptyLabel, 0);
		LabeledIntEdgePluggable XZ = new LabeledIntEdgePluggable("XZ", this.labeledIntValueMapClass);
		XZ.mergeLabeledValue(Label.emptyLabel, 0);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		XY.mergeLabeledValue(Label.parse("p"), -2);

		LabeledIntEdgePluggable YX = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		YX.mergeLabeledValue(Label.parse("¬p"), -2);

		LabeledIntEdgePluggable XX = new LabeledIntEdgePluggable("XX", this.labeledIntValueMapClass);
		LabeledIntEdgePluggable YY = new LabeledIntEdgePluggable("YY", this.labeledIntValueMapClass);

		g.addEdge(XY, this.X, Y);
		g.addEdge(YX, Y, this.X);
		g.addEdge(YZ, Y, this.Z);
		g.addEdge(XZ, this.X, this.Z);
		g.addEdge(XX, this.X, this.X);
		g.addEdge(YY, Y, Y);

		this.cstn.setG(g);
		this.cstn.labeledPropagationRule(this.X, Y, this.Z, XY, YZ, XZ);
		assertEquals("XZ", "{(0, ⊡) (-2, p) }", XZ.getLabeledValueMap().toString());

		this.cstn.labeledPropagationRule(this.X, Y, this.X, XY, YX, XX);
		assertEquals("XX", "{}", XX.getLabeledValueMap().toString());//2017-10-10: qLabels are not more generated.

		this.cstn.labeledPropagationRule(this.X, this.X, Y, XX, XY, XY);
		assertEquals("XY", "{(-2, p) }", XY.getLabeledValueMap().toString());//2017-10-10: qLabels are not more generated.

		this.cstn.labeledPropagationRule(Y, this.X, Y, YX, XY, YY);
		assertEquals("", "{}", YY.getLabeledValueMap().toString());//2017-10-10: qLabels are not more generated.
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labeledPropagationRule(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagationForwardOfInfty1() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode Y = new LabeledNode("Y");
		g.addVertex(this.P);
		g.addVertex(this.X);
		g.addVertex(Y);
		g.addVertex(this.Z);
		g.setZ(this.Z);

		LabeledIntEdgePluggable YZ = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
		YZ.mergeLabeledValue(Label.emptyLabel, 0);
		LabeledIntEdgePluggable XZ = new LabeledIntEdgePluggable("XZ", this.labeledIntValueMapClass);
		XZ.mergeLabeledValue(Label.emptyLabel, 0);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		XY.mergeLabeledValue(Label.parse("¿p"), -2);

		LabeledIntEdgePluggable YX = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		YX.mergeLabeledValue(Label.parse("¬p"), -2);

		LabeledIntEdgePluggable XX = new LabeledIntEdgePluggable("XX", this.labeledIntValueMapClass);
		LabeledIntEdgePluggable YY = new LabeledIntEdgePluggable("YY", this.labeledIntValueMapClass);

		g.addEdge(XY, this.X, Y);
		g.addEdge(YX, Y, this.X);
		g.addEdge(YZ, Y, this.Z);
		g.addEdge(XZ, this.X, this.Z);
		g.addEdge(XX, this.X, this.X);
		g.addEdge(YY, Y, Y);

		this.cstn.setG(g);
		this.cstn.labeledPropagationRule(this.X, Y, this.Z, XY, YZ, XZ);
		// assertEquals("XZ", "{(0, ⊡) }", eNew.getLabeledValueMap().toString());//if only negative value are q-propagate
		assertEquals("XZ", "{(0, ⊡) }", XZ.getLabeledValueMap().toString());// if negative sum value are q-propagate///2017-10-10: qlabels are not more generated.

		this.cstn.labeledPropagationRule(this.X, Y, this.X, XY, YX, XX);
		// assertTrue(eNew == null);//if only negative value are q-propagate

		// g.addEdge(XX, X, X);
		this.cstn.labeledPropagationRule(this.X, this.X, Y, XX, XY, XY);
		// assertEquals("XY", "{(-2, ¿p) }", eNew.getLabeledValueMap().toString());//if only negative value are q-propagate
		assertEquals("XY", "{(-2, ¿p) }", XY.getLabeledValueMap().toString());// if negative sum value are q-propagate
		
		this.cstn.labeledPropagationRule(Y, this.X, Y, YX, XY, YY);
		assertEquals("", "{}", YY.getLabeledValueMap().toString());//2017-10-10: qlabels are not more generated.

		this.cstn.labeledPropagationRule(Y, Y, this.X, YY, YX, YX);
		assertEquals("", "{(-2, ¬p) }", YX.getLabeledValueMap().toString());//2017-10-10: qlabels are not more generated.
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTN#labeledPropagationRule(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledIntEdge)}
	 * .
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabeledPropagationBackwardOfInfty() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode Y = new LabeledNode("Y");
		g.addVertex(this.P);
		g.addVertex(this.X);
		g.addVertex(Y);
		g.addVertex(this.Z);
		g.setZ(this.Z);

		LabeledIntEdgePluggable YZ = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
		YZ.mergeLabeledValue(Label.emptyLabel, 0);
		LabeledIntEdgePluggable XZ = new LabeledIntEdgePluggable("XZ", this.labeledIntValueMapClass);
		XZ.mergeLabeledValue(Label.emptyLabel, 0);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		XY.mergeLabeledValue(Label.parse("p"), -2);

		LabeledIntEdgePluggable YX = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		YX.mergeLabeledValue(Label.parse("¬p"), -2);

		LabeledIntEdgePluggable XX = new LabeledIntEdgePluggable("XX", this.labeledIntValueMapClass);

		g.addEdge(XY, this.X, Y);
		g.addEdge(YX, Y, this.X);
		g.addEdge(YZ, Y, this.Z);
		g.addEdge(XZ, this.X, this.Z);
		g.addEdge(XX, this.X, this.X);
		this.cstn.setG(g);
		this.cstn.labeledPropagationRule(this.X, Y, this.Z, XY, YZ, XZ);
		assertEquals("XZ", "{(0, ⊡) (-2, p) }", XZ.getLabeledValueMap().toString());

		this.cstn.labeledPropagationRule(this.X, Y, this.X, XY, YX, XX);
//		assertEquals("XX", "{(-∞, ¿p) }", XX.getLabeledValueMap().toString());//2017-10-10: qLabels are not more generated.
		assertEquals("XX", "{}", XX.getLabeledValueMap().toString());//2017-10-10: qLabels are not more generated.

		this.cstn.labeledPropagationRule(Y, this.X, this.X, YX, XX, YX);
		assertEquals("", "{(-2, ¬p) }", YX.getLabeledValueMap().toString());//2017-10-10: qLabels are not more generated.
	}

	/**
	 * Test method to check if a graph requiring only R0-R3 application is checked well. .
	 * 
	 * @throws WellDefinitionException
	 */
	@Test
	public final void testQstar() throws WellDefinitionException {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode Q = new LabeledNode("Q?", 'q');
		LabeledNode R = new LabeledNode("R?", 'r');
		g.addVertex(this.Z);
		g.addVertex(this.P);
		g.addVertex(Q);
		g.addVertex(R);
		g.setZ(this.Z);

		// Ricostruisco i passi di un caso di errore
		LabeledIntEdgePluggable RZ = new LabeledIntEdgePluggable("RZ", this.labeledIntValueMapClass);
		RZ.mergeLabeledValue(Label.parse("p¬q"), -16);

		LabeledIntEdgePluggable QZ = new LabeledIntEdgePluggable("QZ", this.labeledIntValueMapClass);
		QZ.mergeLabeledValue(Label.parse("p¬r"), -14);

		LabeledIntEdgePluggable PZ = new LabeledIntEdgePluggable("PZ", this.labeledIntValueMapClass);
		PZ.mergeLabeledValue(Label.parse("¬q¬r"), -12);

		g.addEdge(RZ, R, this.Z);
		g.addEdge(QZ, Q, this.Z);
		g.addEdge(PZ, this.P, this.Z);
		this.cstn.setG(g);
		this.cstn.dynamicConsistencyCheck();

		LabeledIntEdgePluggable RZnew = (LabeledIntEdgePluggable) g.findEdge(R.getName(), this.Z.getName());
		// Std semantics
		this.ok.clear();
		this.ok.mergeLabeledValue("⊡", -12);
		this.ok.mergeLabeledValue("p", -14);
		this.ok.mergeLabeledValue("p¬q", -16);
		// assertEquals("Qstar check"okRZ; normal; {(⊡, -12) (p, -14) (p¬q -16) }; ❯", RZnew.toString()); std semantics
		// assertEquals("Qstar check"okRZ; normal; {(⊡, -13) (p, -15) (p¬q -16) }; ❯", RZnew.toString()); epsilon semantics
		assertEquals("Qstar check", this.ok.getLabeledValueMap(), RZnew.getLabeledValueMap());

		LabeledIntEdgePluggable QZnew = (LabeledIntEdgePluggable) g.findEdge(Q.getName(), this.Z.getName());
		this.ok.clear();
		this.ok.mergeLabeledValue("⊡", -12);
		this.ok.mergeLabeledValue("p", -14);
		assertEquals("Qstar check", this.ok.getLabeledValueMap(), QZnew.getLabeledValueMap());

		LabeledIntEdgePluggable PZnew = (LabeledIntEdgePluggable) g.findEdge(this.P.getName(), this.Z.getName());
		this.ok.clear();
		this.ok.mergeLabeledValue("⊡", -12);
		assertEquals("Qstar check", this.ok.getLabeledValueMap(), PZnew.getLabeledValueMap());

	}
}
