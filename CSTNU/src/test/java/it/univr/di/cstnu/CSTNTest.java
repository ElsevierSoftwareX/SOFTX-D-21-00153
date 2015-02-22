/**
 * 
 */
package it.univr.di.cstnu;

import static org.junit.Assert.*;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.univr.di.cstnu.CSTN.CheckStatus;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.Literal;
import it.univr.di.labeledvalue.Literal.State;

import org.junit.Before;
import org.junit.Test;

/**
 * @author posenato
 */
public class CSTNTest {

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
			CSTN.checkWellDefinitionProperties(g);
		}
		catch (WellDefinitionException e) {
			fail("LabeledIntGraph not well formed: " + e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.CSTN#labelModificationR0(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, CheckStatus, boolean)}.
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
		LabeledIntEdge pxNew = new LabeledIntEdge(px, true);
		// wellDefinition(g);
		CheckStatus status = new CheckStatus();

		CSTN.labelModificationR0(g, P, X, px, pxNew, status, true);

		LabeledIntEdge pxOK = new LabeledIntEdge("XY", true);
		pxOK.mergeLabeledValue(Label.parse("AB"), -10);
		pxOK.mergeLabeledValue(Label.parse("AB¬p"), 0);
		pxOK.mergeLabeledValue(Label.parse("C¬p"), 1);

		assertEquals("R0: p?X labeled values.", pxOK.getLabeledValueMap(), pxNew.getLabeledValueMap());
		assertEquals(status.r0calls, 1);
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.CSTN#labelModificationR2(LabeledIntGraph, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, CheckStatus, boolean)))}.
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

		CheckStatus status = new CheckStatus();
		LabeledIntEdge xpNew = new LabeledIntEdge(xp, true);

		assertEquals(xp.labeledValueSet(), xpNew.labeledValueSet());
		wellDefinition(g);

		CSTN.labelModificationR2(g, P, X, xp, xpNew, status, true);

		LabeledIntEdge pxOK = new LabeledIntEdge("XY", true);
		pxOK.mergeLabeledValue(Label.parse("AB"), 0);
		pxOK.mergeLabeledValue(Label.parse("C¬p"), -1);
		pxOK.mergeLabeledValue(Label.parse("C"), 0);

		assertEquals("R2: XP? labeled values.", pxOK.getLabeledValueMap(), xpNew.getLabeledValueMap());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.CSTN#labelModificationR1(LabeledIntGraph, LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, LabeledIntEdge, CheckStatus, boolean)}
	 * .
	 */
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testLabelModificationR1() {
		// System.out.printf("R1 CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledNode P = new LabeledNode("P", new Literal('p'));
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledIntEdge px = new LabeledIntEdge("PX", true);
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

		LabeledIntEdge xy = new LabeledIntEdge("XY", true);
		xy.mergeLabeledValue(Label.parse("bgp"), 10);
		xy.mergeLabeledValue(Label.parse("cp"), -1);

		g.addEdge(px, P, X);
		g.addEdge(xy, X, Y);

		wellDefinition(g);

		LabeledIntEdge xyNew = new LabeledIntEdge(xy, true);

		CSTN.labelModificationR1(g, P, X, Y, px, xy, xyNew, new CheckStatus(), false);

		LabeledIntEdge xyOK = new LabeledIntEdge("XY", true);
		xyOK.putLabeledValue(Label.parse("bgp"), 10);
		xyOK.putLabeledValue(Label.parse("cp"), -1);
		xyOK.putLabeledValue(Label.parse("¬bc"), -1);
		xyOK.putLabeledValue(Label.parse("abg"), 10);//se non è instantaneous!
		xyOK.putLabeledValue(Label.parse("abc"), -1);
		
		assertEquals("R1: xy labeled values.", xyOK.labeledValueSet(), xyNew.labeledValueSet());

		xyOK.removeLabel(Label.parse("abg"));
		xyNew = new LabeledIntEdge(xy, true);
		CSTN.labelModificationR1(g, P, X, Y, px, xy, xyNew, new CheckStatus(), true);//testo istantaneous
		
		assertEquals("R1: xy labeled values.", xyOK.labeledValueSet(), xyNew.labeledValueSet());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.CSTN#labelModificationR3(LabeledIntGraph, LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, LabeledIntEdge, CheckStatus, boolean)
	 * )}.
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

		LabeledIntEdge yxNew = new LabeledIntEdge(yx, true);

		CSTN.labelModificationR3(g, P, X, Y, px, yx, yxNew, new CheckStatus(), true);

		LabeledIntEdge yxOK = new LabeledIntEdge("YX", true);
		// yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("abc"), -10);
		yxOK.mergeLabeledValue(Label.parse("bgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("abg"), -4);
		yxOK.mergeLabeledValue(Label.parse("¬bc"), 0);// R5 rule
		yxOK.mergeLabeledValue(Label.parse("c¬p"), 11);
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);

		assertEquals("R3: yx labeled values.", yxOK.labeledValueSet(), yxNew.labeledValueSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#noCaseRule(LabeledIntGraph, LabeledIntGraph)}.
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

		LabeledIntGraph g1 = new LabeledIntGraph(g, true);
		CSTN.labelPropagationRule(g, X, P, Y, XP, PY, null, g1, new CheckStatus());

//		System.out.println(XP);
//		System.out.println(PY);
		
		LabeledIntEdge xyOK = new LabeledIntEdge("XY", true);
//		xyOK.mergeLabeledValue(Label.parse("¬A¬B"), 17);
		xyOK.mergeLabeledValue(Label.parse("¬AB"), -2);
		xyOK.mergeLabeledValue(Label.parse("¬B"), 8);
		xyOK.mergeLabeledValue(Label.parse("B"), -1);
		
		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), g1.findEdge(g1.getNode("X"), g1.getNode("Y")).getLabeledValueMap());
		
		XP.clearLabels();
		XP.mergeLabeledValue(Label.parse("B"), -1);
		XP.mergeLabeledValue(Label.parse("¬B"), 1);
		g1.removeEdge(g1.findEdge(g1.getNode("X"), g1.getNode("Y")));
		CSTN.labelPropagationRule(g, X, P, Y, XP, PY, Y, g1, new CheckStatus());//Y is Z!!!
		xyOK.clearLabels();
		ObjectAVLTreeSet<String> ns = new ObjectAVLTreeSet<>();
		ns.add(X.getName());
		xyOK.mergeLabeledValue(Label.parse("¬B"), 10);
		xyOK.mergeLabeledValue(Label.parse("B"), -11, ns);
		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), g1.findEdge(g1.getNode("X"), g1.getNode("Y")).getLabeledValueMap());
		
		XP.clearLabels();
		XP.mergeLabeledValue(Label.parse("B"), -1);
		XP.mergeLabeledValue(Label.parse("¬B"), -10);
		g1.removeEdge(g1.findEdge(g1.getNode("X"), g1.getNode("Y")));
		CSTN.labelPropagationRule(g, X, P, Y, XP, PY, Y, g1, new CheckStatus());//Y is Z!!!
		xyOK.mergeLabeledValue(new Label(new Literal('B', State.unknown)), -20, ns);
		xyOK.mergeLabeledValue(Label.parse("¬B"), -1);
		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), g1.findEdge(g1.getNode("X"), g1.getNode("Y")).getLabeledValueMap());
	}


	
	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#noCaseRule(LabeledIntGraph, LabeledIntGraph)}.
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
		
		//Recostruisco i passi di un caso di errore
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
//		wellDefinition(g);

		LabeledIntGraph g1 = new LabeledIntGraph(g, true);
		System.out.println(g);
		System.out.println(g1);
		LabeledIntEdge XZnew = g1.findEdge(g1.getNode("X"), g1.getNode("Z")); 
		
		CSTN.labelPropagationRule(g, X, Y, Z, XY, YZ, Z, g1, new CheckStatus());

		//❮XZ; normal; {(¬p, -1) (p, -2) (¿p, -5, {X, Y}) }; ❯
		assertEquals("Label propagation rule with particular values", "❮XZ; normal; {(¬p, -1) (p, -2) (¿p, -5, {X, Y}) }; ❯", XZ.toString());
		assertEquals("Label propagation rule with particular values", "❮XZ; normal; {(¬p, -1) (p, -2) (¿p, -5, {X, Y}) }; ❯", XZnew.toString());
	}

	
	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#noCaseRule(LabeledIntGraph, LabeledIntGraph)}.
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

		assertEquals("¬abg",CSTN.makeAlphaBetaGammaPrime(g, P, Label.parse("¬ab"), Label.parse("bg"), false).toString());
		assertEquals("¬abg",CSTN.makeAlphaBetaGammaPrime(g, P, Label.parse("¬ab"), Label.parse("bg"), true).toString());

		assertEquals("bg",CSTN.makeAlphaBetaGammaPrime(g, P, Label.parse("b"), Label.parse("b¬ag"), false).toString());
		assertEquals("bg",CSTN.makeAlphaBetaGammaPrime(g, P, Label.parse("b"), Label.parse("bg¬a"), true).toString());

		assertEquals("¿abg",CSTN.makeAlphaBetaGammaPrime(g, P, Label.parse("ba"), Label.parse("b¬ag"), false).toString());
		assertEquals("bg",CSTN.makeAlphaBetaGammaPrime(g, P, Label.parse("ba"), Label.parse("bg¬a"), true).toString());

		assertEquals("¿bg",CSTN.makeAlphaBetaGammaPrime(g, P, Label.parse("¬b"), Label.parse("b¬ag"), false).toString());
		assertEquals("bg",CSTN.makeAlphaBetaGammaPrime(g, P, Label.parse("b"), Label.parse("bg¬a"), true).toString());
	}
	
	
	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU#noCaseRule(LabeledIntGraph, LabeledIntGraph)}.
	@SuppressWarnings({ "static-method" })
	@Test
	public final void testAlphaBetaGama() {
		assertEquals("¬a",CSTN.getAlphaBetaGamma(Label.parse("¬ab"), Label.parse("bg"), true)[0].toString());
		assertEquals("b",CSTN.getAlphaBetaGamma(Label.parse("¬ab"), Label.parse("bg"), true)[1].toString());
		assertEquals("g",CSTN.getAlphaBetaGamma(Label.parse("¬ab"), Label.parse("bg"), true)[2].toString());

		assertEquals("¬a",CSTN.getAlphaBetaGamma(Label.parse("¬ab"), Label.parse("¬bg"), true)[0].toString());
		assertEquals("⊡",CSTN.getAlphaBetaGamma(Label.parse("¬ab"), Label.parse("¬bg"), true)[1].toString());
		assertEquals("g",CSTN.getAlphaBetaGamma(Label.parse("¬ab"), Label.parse("¬bg"), true)[2].toString());


		assertEquals("¬a",CSTN.getAlphaBetaGamma(Label.parse("¬ab"), Label.parse("¬bg"), false)[0].toString());
		assertEquals("¿b",CSTN.getAlphaBetaGamma(Label.parse("¬ab"), Label.parse("¬bg"), false)[1].toString());
		assertEquals("g",CSTN.getAlphaBetaGamma(Label.parse("¬ab"), Label.parse("¬bg"), false)[2].toString());
	}
	 */

	
}
