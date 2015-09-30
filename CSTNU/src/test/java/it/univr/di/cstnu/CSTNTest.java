/**
 * 
 */
package it.univr.di.cstnu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.univr.di.cstnu.CSTN.CSTNCheckStatus;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.Literal;
import it.univr.di.labeledvalue.Literal.State;

/**
 * @author posenato
 */
public class CSTNTest {

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
			CSTN.checkWellDefinitionProperties(g);
		} catch (WellDefinitionException e) {
			fail("LabeledIntGraph not well formed: " + e.getMessage());
		}
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTN#labelModificationR0(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdge, CSTNCheckStatus, boolean)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testLabelModificationR0() {
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledNode P = new LabeledNode("P", new Literal('p'));
		LabeledNode X = new LabeledNode("X");
		LabeledIntEdge px = new LabeledIntEdge("PX", true);
		px.mergeLabeledValue(Label.parse("ABp"), -10);
		px.mergeLabeledValue(Label.parse("AB¬p"), 0);
		px.mergeLabeledValue(Label.parse("C¬p"), 1);
		g.addEdge(px, P, X);
		// wellDefinition(g);
		CSTNCheckStatus status = new CSTNCheckStatus();

		CSTN.labelModificationR0(g, P, X, px, status, true);

		LabeledIntEdge pxOK = new LabeledIntEdge("XY", true);
		pxOK.mergeLabeledValue(Label.parse("AB"), -10);
		pxOK.mergeLabeledValue(Label.parse("AB¬p"), 0);
		pxOK.mergeLabeledValue(Label.parse("C¬p"), 1);

		assertEquals("R0: p?X labeled values.", pxOK.getLabeledValueMap(), px.getLabeledValueMap());
		assertEquals(status.r0calls, 1);
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTN#labelModificationR2(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdge, CSTNCheckStatus, boolean)))}
	 * .
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testLabelModificationR2() {
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

		LabeledIntEdge xp = new LabeledIntEdge("XP", true);
		xp.mergeLabeledValue(Label.parse("ABp"), 10);
		xp.mergeLabeledValue(Label.parse("AB¬p"), 0);
		xp.mergeLabeledValue(Label.parse("C¬p"), -1);
		g.addEdge(xp, X, P);

		wellDefinition(g);
		CSTNCheckStatus status = new CSTNCheckStatus();

		CSTN.labelModificationR2(g, P, X, xp, status, true);

		LabeledIntEdge pxOK = new LabeledIntEdge("XY", true);
		pxOK.mergeLabeledValue(Label.parse("AB"), 0);
		pxOK.mergeLabeledValue(Label.parse("C¬p"), -1);
		pxOK.mergeLabeledValue(Label.parse("C"), 0);

		assertEquals("R2: XP? labeled values.", pxOK.getLabeledValueMap(), xp.getLabeledValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTN#labelModificationR1(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdge, CSTNCheckStatus, boolean)} .
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testLabelModificationR1() {
		// System.out.printf("R1 CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledNode Z = new LabeledNode("Z");
		LabeledNode P = new LabeledNode("P", new Literal('p'));
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledIntEdge px = new LabeledIntEdge("PX", true);
		LabeledNode A = new LabeledNode("A?", new Literal('a'));
		LabeledNode B = new LabeledNode("B?", new Literal('b'));
		LabeledNode C = new LabeledNode("C?", new Literal('c'));
		LabeledNode G = new LabeledNode("G?", new Literal('g'));
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

		LabeledIntEdge xy = new LabeledIntEdge("XY", true);
		xy.mergeLabeledValue(Label.parse("bgp"), 10);
		xy.mergeLabeledValue(Label.parse("cp"), -1);

		g.addEdge(px, P, X);
		g.addEdge(xy, X, Y);

		wellDefinition(g);

		CSTN.labelModificationR1(g, X, Y, xy, new CSTNCheckStatus(), false);

		LabeledIntEdge xyOK = new LabeledIntEdge("XY", true);
		xyOK.putLabeledValue(Label.parse("bgp"), 10);
		xyOK.putLabeledValue(Label.parse("cp"), -1);
		xyOK.putLabeledValue(Label.parse("¬bc"), -1);
		xyOK.putLabeledValue(Label.parse("abg"), 10);// se non è instantaneous!
		xyOK.putLabeledValue(Label.parse("abc"), -1);

		assertEquals("R1: xy labeled values.", xyOK.labeledValueSet(), xy.labeledValueSet());

		xyOK.removeLabel(Label.parse("abg"));
		xy.clear();
		xy.mergeLabeledValue(Label.parse("bgp"), 10);
		xy.mergeLabeledValue(Label.parse("cp"), -1);
		CSTN.labelModificationR1(g, X, Y, xy, new CSTNCheckStatus(), true);// test istantaneous

		assertEquals("R1: xy labeled values.", xyOK.labeledValueSet(), xy.labeledValueSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTN#labelModificationR3(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdge, CSTNCheckStatus, boolean) )}
	 * .
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testLabelModificationR3() {
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

		LabeledIntEdge px = new LabeledIntEdge("PX", true);
		px.mergeLabeledValue(Label.parse("¬b"), 0);
		px.mergeLabeledValue(Label.parse("ab"), -10);

		LabeledIntEdge yx = new LabeledIntEdge("YX", true);
		yx.mergeLabeledValue(Label.parse("bgp"), -4);
		yx.mergeLabeledValue(Label.parse("cp"), -10);
		yx.mergeLabeledValue(Label.parse("c¬p"), 11);

		g.addEdge(px, P, X);
		g.addEdge(yx, Y, X);

		wellDefinition(g);

		CSTN.labelModificationR3(g, X, Y, yx, new CSTNCheckStatus(), true);

		LabeledIntEdge yxOK = new LabeledIntEdge("YX", true);
		// yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("abc"), -10);
		yxOK.mergeLabeledValue(Label.parse("bgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("abg"), -4);
		yxOK.mergeLabeledValue(Label.parse("¬bc"), 0);// R5 rule
		yxOK.mergeLabeledValue(Label.parse("c¬p"), 11);
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);

		assertEquals("R3: yx labeled values.", yxOK.labeledValueSet(), yx.labeledValueSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTN#labelPropagationRule(LabeledIntGraph, LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, LabeledNode, CSTNCheckStatus)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testLabeledPropagation() {
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

		LabeledIntEdge XP = new LabeledIntEdge("XP", true);
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬A"), 8);
		XP.mergeLabeledValue(Label.parse("¬B"), -1);
		XP.mergeLabeledValue(Label.parse("B"), 9);

		LabeledIntEdge PY = new LabeledIntEdge("PY", true);
		PY.mergeLabeledValue(Label.parse("¬B"), 9);
		PY.mergeLabeledValue(Label.parse("B"), -10);

		g.addEdge(XP, X, P);
		g.addEdge(PY, P, Y);

		wellDefinition(g);

		CSTN.labelPropagationRule(g, X, P, Y, XP, PY, null, new CSTNCheckStatus());

		// System.out.println(XP);
		// System.out.println(PY);

		LabeledIntEdge xyOK = new LabeledIntEdge("XY", true);
		// xyOK.mergeLabeledValue(Label.parse("¬A¬B"), 17);
		xyOK.mergeLabeledValue(Label.parse("¬AB"), -2);
		xyOK.mergeLabeledValue(Label.parse("¬B"), 8);
		xyOK.mergeLabeledValue(Label.parse("B"), -1);

		LabeledIntEdge xy = g.findEdge(X, Y);

		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), xy.getLabeledValueMap());

		XP.clearLabels();
		XP.mergeLabeledValue(Label.parse("B"), -1);
		XP.mergeLabeledValue(Label.parse("¬B"), 1);
		xy.clear();
		CSTN.labelPropagationRule(g, X, P, Y, XP, PY, Y, new CSTNCheckStatus());// Y is Z!!!

		xyOK.clearLabels();
		ObjectAVLTreeSet<String> ns = new ObjectAVLTreeSet<>();
		ns.add(X.getName());
		ns.add(P.getName());
		xyOK.mergeLabeledValue(Label.parse("¬B"), 10);
		xyOK.mergeLabeledValue(Label.parse("B"), -11, ns);
		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), xy.getLabeledValueMap());

		XP.clearLabels();
		XP.mergeLabeledValue(Label.parse("B"), -1);
		XP.mergeLabeledValue(Label.parse("¬B"), -10);
		xy.clear();

		CSTN.labelPropagationRule(g, X, P, Y, XP, PY, Y, new CSTNCheckStatus());// Y is Z!!!
		xyOK.mergeLabeledValue(new Label(new Literal('B', State.unknown)), -20, ns);
		xyOK.mergeLabeledValue(Label.parse("¬B"), -1);
		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), xy.getLabeledValueMap());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.CSTN#labelPropagationRule(LabeledIntGraph, LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, LabeledNode, CSTNCheckStatus)}
	 * .
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testLabeledPropagation1() {
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledNode P = new LabeledNode("P?", new Literal('p'));
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
		LabeledIntEdge XZ = new LabeledIntEdge("XZ", true);
		XZ.mergeLabeledValue(Label.parse("p"), -2);
		XZ.mergeLabeledValue(Label.parse("¿p"), -5, nodeSet);
		XZ.mergeLabeledValue(Label.parse("¬p"), -1);
		nodeSet.add(Y.getName());
		XZ.mergeLabeledValue(Label.parse("¿p"), -5, nodeSet);

		LabeledIntEdge XY = new LabeledIntEdge("XY", true);
		XY.mergeLabeledValue(Label.parse("p"), -2);
		XY.mergeLabeledValue(Label.emptyLabel, 0);

		LabeledIntEdge YZ = new LabeledIntEdge("YZ", true);
		YZ.mergeLabeledValue(Label.parse("¬p"), -1);
		YZ.mergeLabeledValue(Label.emptyLabel, 0);

		g.addEdge(XY, X, Y);
		g.addEdge(XZ, X, Z);
		g.addEdge(YZ, Y, Z);
		// wellDefinition(g);

		// System.out.println(g);
		// System.out.println(g1);
		LabeledIntEdge XZnew = g.findEdge(X, Z);

		CSTN.labelPropagationRule(g, X, Y, Z, XY, YZ, Z, new CSTNCheckStatus());
		assertEquals("Label propagation rule with particular values", "❮XZ; normal; {(¬p, -1) (p, -2) (¿p, -5, {X, Y}) }; ❯", XZnew.toString());// (¬p,
																			// -1,
																			// {X,
																			// Y})
																			// nel
																			// caso
																			// in
																			// cui
																			// accettiamo
																			// la
																			// propagazione
																			// dei
																			// node
																			// set
																			// con
																			// valori
																			// di
																			// edge
																			// positivi.

		// ❮XZ; normal; {(¬p, -1) (p, -2) (¿p, -5, {X, Y}) }; ❯
		assertEquals("Label propagation rule with particular values", "❮XZ; normal; {(¬p, -1) (p, -2) (¿p, -5, {X, Y}) }; ❯", XZ.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTN#makeAlphaBetaGammaPrime(LabeledIntGraph, LabeledNode, Literal, Label, Label, boolean)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testAlphaBetaGamaPrime() {
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledNode P = new LabeledNode("P?", new Literal('p'));
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", new Literal('a'));
		LabeledNode B = new LabeledNode("B?", new Literal('b'));
		A.setLabel(Label.parse("p"));

		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(Y);

		LabeledIntEdge XP = new LabeledIntEdge("XP", true);
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬ap"), 8);
		XP.mergeLabeledValue(Label.parse("¬b"), -1);
		XP.mergeLabeledValue(Label.parse("bap"), 9);

		LabeledIntEdge PY = new LabeledIntEdge("PY", true);
		PY.mergeLabeledValue(Label.parse("¬b"), 9);
		PY.mergeLabeledValue(Label.parse("b"), -10);

		LabeledIntEdge AP = new LabeledIntEdge("AP", true);
		AP.mergeLabeledValue(Label.parse("p"), 0);

		g.addEdge(XP, X, P);
		g.addEdge(PY, P, Y);
		g.addEdge(AP, A, P);

		wellDefinition(g);

		assertEquals("¬abg", CSTN.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("¬ab"), Label.parse("bg"), false).toString());
		assertEquals("¬abg", CSTN.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("¬ab"), Label.parse("bg"), true).toString());

		assertEquals("bg", CSTN.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("b"), Label.parse("b¬ag"), false).toString());
		assertEquals("bg", CSTN.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("b"), Label.parse("bg¬a"), true).toString());

		assertEquals("¿abg", CSTN.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("ba"), Label.parse("b¬ag"), false).toString());
		assertEquals("bg", CSTN.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("ba"), Label.parse("bg¬a"), true).toString());

		assertEquals("¿bg", CSTN.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("¬b"), Label.parse("b¬ag"), false).toString());
		assertEquals("bg", CSTN.makeAlphaBetaGammaPrime(g, P, P.getPropositionObserved(), Label.parse("b"), Label.parse("bg¬a"), true).toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTN#labelPropagationRule(LabeledIntGraph, LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, LabeledNode, CSTNCheckStatus)}.
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testLabeledPropagation2() {
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledNode P = new LabeledNode("P?", new Literal('p'));
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
		LabeledIntEdge YZ = new LabeledIntEdge("YZ", true);
		YZ.mergeLabeledValue(Label.emptyLabel, 0);

		LabeledIntEdge XY = new LabeledIntEdge("XY", true);
		XY.mergeLabeledValue(Label.parse("p"), -2);
		XY.mergeLabeledValue(Label.parse("¬p"), -1);
		XY.mergeLabeledValue(Label.emptyLabel, 0);

		g.addEdge(XY, X, Y);
		g.addEdge(YZ, Y, Z);

		CSTN.labelPropagationRule(g, X, Y, Z, XY, YZ, Z, new CSTNCheckStatus());
		LabeledIntEdge XZnew = g.findEdge(X, Z);
		assertEquals("Label propagation rule with particular values", "❮X_Z; derived; {(¬p, -1) (p, -2) }; ❯", XZnew.toString());
	}
}
