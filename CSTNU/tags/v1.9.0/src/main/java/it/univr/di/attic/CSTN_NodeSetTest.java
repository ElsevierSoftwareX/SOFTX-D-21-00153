/**
 * 
 */
package it.univr.di.cstnu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.univr.di.cstnu.WellDefinitionException;
import it.univr.di.cstnu.CSTN_NodeSet.CSTNCheckStatus;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntMapFactory;
import it.univr.di.labeledvalue.LabeledIntNodeSetTreeMap;
import it.univr.di.labeledvalue.Literal;
import it.univr.di.labeledvalue.Literal.State;



/**
 * @author posenato
 */
public class CSTN_NodeSetTest {

	
	@SuppressWarnings("javadoc")
	final Class<? extends LabeledIntMap> labeledIntValueMap = (new LabeledIntNodeSetTreeMap()).getClass();

	@SuppressWarnings("javadoc")
	final LabeledIntMapFactory<? extends LabeledIntMap> labeledIntMapFactory = new LabeledIntMapFactory<>(labeledIntValueMap);

	/**
	 * 
	 */
	CSTN_NodeSet cstn;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		cstn = new CSTN_NodeSet();
	}

	/**
	 * @param g
	 */
	private final void wellDefinition(LabeledIntGraph g) {
		try {
			cstn.checkWellDefinitionProperties(g);
		} catch (WellDefinitionException e) {
			fail("LabeledIntGraph not well formed: " + e.getMessage());
		}
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTN_NodeSet#labelModificationR0(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdgePluggable, CSTNCheckStatus)}.
	 */
	@Test
	public final void testLabelModificationR0() {
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMap);
		LabeledNode P = new LabeledNode("P", 'p');
		LabeledNode X = new LabeledNode("X");
		LabeledIntEdgePluggable px = new LabeledIntEdgePluggable("PX",labeledIntValueMap);
		px.mergeLabeledValue(Label.parse("ABp"), -10);
		px.mergeLabeledValue(Label.parse("AB¬p"), 0);
		px.mergeLabeledValue(Label.parse("C¬p"), 1);
		g.addEdge(px, P, X);
		// wellDefinition(g);
		CSTNCheckStatus status = new CSTNCheckStatus();

		cstn.labelModificationR0(g, P, X, px, status);

		LabeledIntEdgePluggable pxOK = new LabeledIntEdgePluggable("XY",labeledIntValueMap);
		pxOK.mergeLabeledValue(Label.parse("AB"), -10);
		pxOK.mergeLabeledValue(Label.parse("AB¬p"), 0);
		pxOK.mergeLabeledValue(Label.parse("C¬p"), 1);

		assertEquals("R0: p?X labeled values.", pxOK.getLabeledValueMap(), px.getLabeledValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTN_NodeSet#labelModificationR2(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdgePluggable, CSTNCheckStatus, boolean)))}
	 * .
	 */
	@SuppressWarnings({ "javadoc", "deprecation" })
	@Test
	public final void testLabelModificationR2() {
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMap);
		LabeledNode P = new LabeledNode("P?", 'p');
		LabeledNode A = new LabeledNode("A?", 'A');
		LabeledNode B = new LabeledNode("B?", 'B');
		LabeledNode C = new LabeledNode("C?", 'C');
		LabeledNode X = new LabeledNode("X");
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(C);

		LabeledIntEdgePluggable xp = new LabeledIntEdgePluggable("XP",labeledIntValueMap);
		xp.mergeLabeledValue(Label.parse("ABp"), 10);
		xp.mergeLabeledValue(Label.parse("AB¬p"), 0);
		xp.mergeLabeledValue(Label.parse("C¬p"), -1);
		g.addEdge(xp, X, P);

		wellDefinition(g);
		CSTNCheckStatus status = new CSTNCheckStatus();

		cstn.labelModificationR2(g, P, X, xp, status);

		LabeledIntEdgePluggable pxOK = new LabeledIntEdgePluggable("XY",labeledIntValueMap);
		//Reaction time = 0
		pxOK.mergeLabeledValue(Label.parse("AB"), 0);
		pxOK.mergeLabeledValue(Label.parse("C¬p"), -1);
		pxOK.mergeLabeledValue(Label.parse("C"), 0);
		//Reaction time = 1
		pxOK = new LabeledIntEdgePluggable("XY",labeledIntValueMap);
		pxOK.mergeLabeledValue(Label.parse("AB"), 1);
		pxOK.mergeLabeledValue(Label.parse("AB¬p"), 0);
		pxOK.mergeLabeledValue(Label.parse("C"), 1);
		pxOK.mergeLabeledValue(Label.parse("C¬p"), -1);
		
		assertEquals("R2: XP? labeled values.", pxOK.getLabeledValueMap(), xp.getLabeledValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTN_NodeSet#labelModificationR1(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdgePluggable, CSTNCheckStatus)} .
	 */
	@SuppressWarnings({ "javadoc", "deprecation" })
	@Test
	public final void testLabelModificationR1() {
		// System.out.printf("R1 CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMap);
		LabeledNode Z = new LabeledNode("Z");
		LabeledNode P = new LabeledNode("P", 'p');
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledIntEdgePluggable px = new LabeledIntEdgePluggable("PX",labeledIntValueMap);
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		LabeledNode C = new LabeledNode("C?", 'c');
		LabeledNode G = new LabeledNode("G?", 'g');
		g.addVertex(Z);
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(G);
		g.addVertex(C);
		g.addVertex(X);
		g.addVertex(Y);

		px.mergeLabeledValue(Label.parse("¬b"), 0);
		px.mergeLabeledValue(Label.parse("ab"), -10);

		LabeledIntEdgePluggable xy = new LabeledIntEdgePluggable("XY",labeledIntValueMap);
		xy.mergeLabeledValue(Label.parse("bgp"), 10);
		xy.mergeLabeledValue(Label.parse("cp"), -1);

		g.addEdge(px, P, X);
		g.addEdge(xy, X, Y);

		wellDefinition(g);

		CSTN_NodeSet cstn1 = new CSTN_NodeSet();
		cstn1.labelModificationR1(g, X, Y, xy, new CSTNCheckStatus());

		LabeledIntEdgePluggable xyOK = new LabeledIntEdgePluggable("XY",labeledIntValueMap);
		xyOK.putLabeledValue(Label.parse("abc"), -1);
		xyOK.putLabeledValue(Label.parse("¬bc"), -1);
		xyOK.putLabeledValue(Label.parse("bgp"), 10);
		xyOK.putLabeledValue(Label.parse("cp"), -1);
//		xyOK.putLabeledValue(Label.parse("abg"), 10);// se non è instantaneous!

		assertEquals("R1: xy labeled values.", xyOK.labeledValueSet(), xy.labeledValueSet());

		cstn1 = new CSTN_NodeSet();

		xyOK.removeLabel(Label.parse("abg"));
		xy.clear();
		xy.mergeLabeledValue(Label.parse("bgp"), 10);
		xy.mergeLabeledValue(Label.parse("cp"), -1);
		cstn1.labelModificationR1(g, X, Y, xy, new CSTNCheckStatus());// test istantaneous

//		assertEquals("R1: xy labeled values.", xyOK.labeledValueSet(), xy.labeledValueSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTN_NodeSet#labelModificationR3(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdgePluggable, CSTNCheckStatus)} .
	 */
	@Test
	public final void testLabelModificationR3() {
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMap);
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

		LabeledIntEdgePluggable px = new LabeledIntEdgePluggable("PX",labeledIntValueMap);
		px.mergeLabeledValue(Label.parse("¬b"), 0);
		px.mergeLabeledValue(Label.parse("ab"), -10);

		LabeledIntEdgePluggable yx = new LabeledIntEdgePluggable("YX",labeledIntValueMap);
		yx.mergeLabeledValue(Label.parse("bgp"), -4);
		yx.mergeLabeledValue(Label.parse("cp"), -10);
		yx.mergeLabeledValue(Label.parse("c¬p"), 11);

		g.addEdge(px, P, X);
		g.addEdge(yx, Y, X);

		wellDefinition(g);

		cstn.labelModificationR3(g, Y, X, yx, new CSTNCheckStatus());

		LabeledIntEdgePluggable yxOK = new LabeledIntEdgePluggable("YX",labeledIntValueMap);
		// yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("abc"), -10);
		yxOK.mergeLabeledValue(Label.parse("bgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("abg"), -4);
		yxOK.mergeLabeledValue(Label.parse("¬bc"), -1);// R3 rule with reaction time 
		yxOK.mergeLabeledValue(Label.parse("c¬p"), 11);
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);

		assertEquals("R3: yx labeled values.", yxOK.labeledValueSet(), yx.labeledValueSet());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.CSTN_NodeSet#labelPropagationRule(LabeledIntGraph, LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledNode, CSTNCheckStatus)}
	 * .
	 */
	@Test
	public final void testLabeledPropagation() {
		CSTN_NodeSet cstn = new CSTN_NodeSet();
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMap);
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

		LabeledIntEdgePluggable XP = new LabeledIntEdgePluggable("XP",labeledIntValueMap);
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬a"), 8);
		XP.mergeLabeledValue(Label.parse("¬b"), -1);
		XP.mergeLabeledValue(Label.parse("b"), 9);

		LabeledIntEdgePluggable PY = new LabeledIntEdgePluggable("PY",labeledIntValueMap);
		PY.mergeLabeledValue(Label.parse("¬b"), 9);
		PY.mergeLabeledValue(Label.parse("b"), -10);

		g.addEdge(XP, X, P);
		g.addEdge(PY, P, Y);

		wellDefinition(g);

		cstn.labelPropagationRule(g, X, P, Y, XP, PY, null, new CSTNCheckStatus());

		// System.out.println(XP);
		// System.out.println(PY);

		LabeledIntEdgePluggable xyOK = new LabeledIntEdgePluggable("XY",labeledIntValueMap);
		// xyOK.mergeLabeledValue(Label.parse("¬A¬B"), 17);
		xyOK.mergeLabeledValue(Label.parse("¬ab"), -2);
		xyOK.mergeLabeledValue(Label.parse("¬b"), 8);
		xyOK.mergeLabeledValue(Label.parse("b"), -1);

		LabeledIntEdgePluggable xy = (LabeledIntEdgePluggable) g.findEdge(X, Y);

		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), xy.getLabeledValueMap());

		XP.clearLabels();
		XP.mergeLabeledValue(Label.parse("b"), -1);
		XP.mergeLabeledValue(Label.parse("¬b"), 1);
		xy.clear();
		cstn.labelPropagationRule(g, X, P, Y, XP, PY, Y, new CSTNCheckStatus());// Y is Z!!!

		xyOK.clearLabels();
		ObjectAVLTreeSet<String> ns = new ObjectAVLTreeSet<>();
		ns.add(X.getName());
		ns.add(P.getName());
		xyOK.mergeLabeledValue(Label.parse("¬b"), 10);
		xyOK.mergeLabeledValue(Label.parse("b"), -11, ns);
		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), xy.getLabeledValueMap());

		XP.clearLabels();
		XP.mergeLabeledValue(Label.parse("b"), -1);
		XP.mergeLabeledValue(Label.parse("¬b"), -10);
		xy.clear();

		cstn.labelPropagationRule(g, X, P, Y, XP, PY, Y, new CSTNCheckStatus());// Y is Z!!!
		xyOK.mergeLabeledValue(new Label('b', State.unknown), -20, ns);
		xyOK.mergeLabeledValue(Label.parse("¬b"), -1);
		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), xy.getLabeledValueMap());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.CSTN_NodeSet#labelPropagationRule(LabeledIntGraph, LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledNode, CSTNCheckStatus)}
	 * .
	 */
	@SuppressWarnings("unused")
	@Test
	public final void testLabeledPropagation1() {
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMap);
		LabeledNode P = new LabeledNode("P?", 'p');
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode Z = new LabeledNode("Z");
		g.addVertex(P);
		g.addVertex(X);
		g.addVertex(Y);
		g.addVertex(Z);
		g.setZ(Z);

		ObjectAVLTreeSet<String> nodeSet = new ObjectAVLTreeSet<>();
		nodeSet.add(X.getName());

		// Ricostruisco i passi di un caso di errore
		LabeledIntEdgePluggable XZ = new LabeledIntEdgePluggable("XZ",labeledIntValueMap);
		XZ.mergeLabeledValue(Label.parse("p"), -2);
		XZ.mergeLabeledValue(Label.parse("¿p"), -5, nodeSet);
		XZ.mergeLabeledValue(Label.parse("¬p"), -1);
		nodeSet.add(Y.getName());
		XZ.mergeLabeledValue(Label.parse("¿p"), -5, nodeSet);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY",labeledIntValueMap);
		XY.mergeLabeledValue(Label.parse("p"), -2);
		XY.mergeLabeledValue(Label.emptyLabel, 0);

		LabeledIntEdgePluggable YZ = new LabeledIntEdgePluggable("YZ",labeledIntValueMap);
		YZ.mergeLabeledValue(Label.parse("¬p"), -1);
		YZ.mergeLabeledValue(Label.emptyLabel, 0);

		g.addEdge(XY, X, Y);
		g.addEdge(XZ, X, Z);
		g.addEdge(YZ, Y, Z);
		// wellDefinition(g);

		// System.out.println(g);
		// System.out.println(g1);
		LabeledIntEdgePluggable XZnew = (LabeledIntEdgePluggable) g.findEdge(X, Z);

		CSTN_NodeSet cstn = new CSTN_NodeSet();
		cstn.labelPropagationRule(g, X, Y, Z, XY, YZ, Z, new CSTNCheckStatus());
		
		// (¬p, -1, {X, Y}) nel caso in cui accettiamo la propagazione dei node set con valori di edge positivi
		//assertEquals("Label propagation rule with particular values", "❮XZ; normal; {(¬p, -1, {X, Y}) (p, -2) (¿p, -5, {X, Y}) }; ❯", XZnew.toString());

		// ❮XZ; normal; {(¬p, -1) (p, -2) (¿p, -5, {X, Y}) }; ❯
		assertEquals("Label propagation rule with particular values", "❮XZ; normal; {(¬p, -1) (p, -2) (¿p, -5, {X, Y}) }; ❯", XZ.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTN_NodeSet#makeAlphaBetaGammaPrime(LabeledIntGraph, LabeledNode, Literal, Label, Label, boolean)}.
	 */
	@SuppressWarnings({"javadoc"})
	@Test
	public final void testAlphaBetaGamaPrime() {
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMap);
		LabeledNode P = new LabeledNode("P?", 'p');
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		A.setLabel(Label.parse("p"));

		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(Y);

		LabeledIntEdgePluggable XP = new LabeledIntEdgePluggable("XP",labeledIntValueMap);
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬ap"), 8);
		XP.mergeLabeledValue(Label.parse("¬b"), -1);
		XP.mergeLabeledValue(Label.parse("bap"), 9);

		LabeledIntEdgePluggable PY = new LabeledIntEdgePluggable("PY",labeledIntValueMap);
		PY.mergeLabeledValue(Label.parse("¬b"), 9);
		PY.mergeLabeledValue(Label.parse("b"), -10);

		LabeledIntEdgePluggable AP = new LabeledIntEdgePluggable("AP",labeledIntValueMap);
		AP.mergeLabeledValue(Label.parse("p"), -1);

		g.addEdge(XP, X, P);
		g.addEdge(PY, P, Y);
		g.addEdge(AP, A, P);

		wellDefinition(g);

		assertEquals("¬abg", CSTN_NodeSet.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("¬ab"), Label.parse("bg"), false).toString());
		assertEquals("¬abg", CSTN_NodeSet.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("¬ab"), Label.parse("bg"), true).toString());

		assertEquals("bg", CSTN_NodeSet.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("b"), Label.parse("b¬ag"), false).toString());
		assertEquals("bg", CSTN_NodeSet.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("b"), Label.parse("bg¬a"), true).toString());

		assertEquals("¿abg", CSTN_NodeSet.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("ba"), Label.parse("b¬ag"), false).toString());
		assertEquals("bg", CSTN_NodeSet.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("ba"), Label.parse("bg¬a"), true).toString());

		assertEquals("¿bg", CSTN_NodeSet.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("¬b"), Label.parse("b¬ag"), false).toString());
		assertEquals("bg", CSTN_NodeSet.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("b"), Label.parse("bg¬a"), true).toString());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.CSTN_NodeSet#labelPropagationRule(LabeledIntGraph, LabeledNode, LabeledNode, LabeledNode, LabeledIntEdgePluggable, LabeledIntEdgePluggable, LabeledNode, CSTNCheckStatus)}
	 * .
	 */
	@Test
	public final void testLabeledPropagation2() {
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMap);
		LabeledNode P = new LabeledNode("P?", 'p');
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode Z = new LabeledNode("Z");
		g.addVertex(P);
		g.addVertex(X);
		g.addVertex(Y);
		g.addVertex(Z);
		g.setZ(Z);

		ObjectAVLTreeSet<String> nodeSet = new ObjectAVLTreeSet<>();
		nodeSet.add(X.getName());
		nodeSet.add(Y.getName());

		// Ricostruisco i passi di un caso di errore
		LabeledIntEdgePluggable YZ = new LabeledIntEdgePluggable("YZ",labeledIntValueMap);
		YZ.mergeLabeledValue(Label.emptyLabel, 0);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY",labeledIntValueMap);
		XY.mergeLabeledValue(Label.parse("p"), -2);
		XY.mergeLabeledValue(Label.parse("¬p"), -1);
		XY.mergeLabeledValue(Label.emptyLabel, 0);

		g.addEdge(XY, X, Y);
		g.addEdge(YZ, Y, Z);

		CSTN_NodeSet cstn = new CSTN_NodeSet();
		cstn.labelPropagationRule(g, X, Y, Z, XY, YZ, Z, new CSTNCheckStatus());
		LabeledIntEdgePluggable XZnew = (LabeledIntEdgePluggable) g.findEdge(X, Z);
		assertEquals("Label propagation rule with particular values", "❮X_Z; derived; {(¬p, -1) (p, -2) }; ❯", XZnew.toString());
	}
	
	
	
	/**
	 * Test method to check if a graph requiring only R0-R3 application is checked well.
	 * .
	 * @throws WellDefinitionException 
	 */
	@Test
	public final void testQstar() throws WellDefinitionException {
		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMap);
		LabeledNode Z = new LabeledNode("Z");
		LabeledNode P = new LabeledNode("P?", 'p');
		LabeledNode Q = new LabeledNode("Q?", 'q');
		LabeledNode R = new LabeledNode("R?", 'r');
		g.addVertex(Z);
		g.addVertex(P);
		g.addVertex(Q);
		g.addVertex(R);
		g.setZ(Z);

		// Ricostruisco i passi di un caso di errore
		LabeledIntEdgePluggable RZ = new LabeledIntEdgePluggable("RZ",labeledIntValueMap);
		RZ.mergeLabeledValue(Label.parse("p¬q"), -16);

		LabeledIntEdgePluggable QZ = new LabeledIntEdgePluggable("QZ",labeledIntValueMap);
		QZ.mergeLabeledValue(Label.parse("p¬r"), -14);
		
		LabeledIntEdgePluggable PZ = new LabeledIntEdgePluggable("PZ",labeledIntValueMap);
		PZ.mergeLabeledValue(Label.parse("¬q¬r"), -12);
		
		g.addEdge(RZ, R, Z);
		g.addEdge(QZ, Q, Z);
		g.addEdge(PZ, P, Z);

		CSTN_NodeSet cstn = new CSTN_NodeSet();
		cstn.dynamicConsistencyCheck(g);
		
		LabeledIntEdgePluggable RZnew = (LabeledIntEdgePluggable) g.findEdge(R.getName(), Z.getName());
		assertEquals("Qstar check", "❮RZ; normal; {(⊡, -13) (p, -15) (p¬q, -16) }; ❯", RZnew.toString());
		
		LabeledIntEdgePluggable QZnew = (LabeledIntEdgePluggable) g.findEdge(Q.getName(), Z.getName());
		assertEquals("Qstar check", "❮QZ; normal; {(⊡, -13) (p, -14) }; ❯", QZnew.toString());
		
		LabeledIntEdgePluggable PZnew = (LabeledIntEdgePluggable) g.findEdge(P.getName(), Z.getName());
		assertEquals("Qstar check", "❮PZ; normal; {(⊡, -12) }; ❯", PZnew.toString());
		
		
	}
}
