//package it.univr.di.cstnu.graph;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//
//import org.junit.Test;
//
//import it.univr.di.cstnu.algorithms.CSTN;
//import it.univr.di.cstnu.algorithms.WellDefinitionException;
//import it.univr.di.labeledvalue.Constants;
//import it.univr.di.labeledvalue.Label;
//import it.univr.di.labeledvalue.Literal;
//
///**
// * @author posenato
// *
// */
//public class LabeledIntGraphJungTest {
//	
//	/**
//	 * @param g
//	 */
//	@SuppressWarnings("unused")
//	private final static void wellDefinition(LabeledIntGraph g) {
//		CSTN cstn = new CSTN();
//		try {
//			cstn.checkWellDefinitionProperties(g);
//		}
//		catch (WellDefinitionException e) {
//			fail("LabeledIntGraphJung<labeledIntEdgeSimple> not well formed: " + e.getMessage());
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@SuppressWarnings("static-method")
//	@Test
//	public final void testGetChildrenOf() {
//		LabeledIntGraphJung<LabeledIntEdgePluggable> g = new LabeledIntGraphJung<>(LabeledIntEdgePluggable.class);
//		LabeledNode P = new LabeledNode("P?", new Literal('p'));
//		LabeledNode X = new LabeledNode("X");
//		LabeledNode Y = new LabeledNode("Y");
//		LabeledNode A = new LabeledNode("A?", new Literal('a'));
//		LabeledNode B = new LabeledNode("B?", new Literal('b'));
//		A.setLabel(Label.parse("p"));
//		
//		g.addVertex(P);
//		g.addVertex(A);
//		g.addVertex(B);
//		g.addVertex(X);
//		g.addVertex(Y);
//
//		LabeledIntEdgePluggable XP = new LabeledIntEdgePluggable("XP", null);
//		XP.mergeLabeledValue(Label.emptyLabel, 10);
//		XP.mergeLabeledValue(Label.parse("¬ap"), 8);
//		XP.mergeLabeledValue(Label.parse("¬b"), -1);
//		XP.mergeLabeledValue(Label.parse("bap"), 9);
//		
//		LabeledIntEdgePluggable PY = new LabeledIntEdgePluggable("PY", null);
//		PY.mergeLabeledValue(Label.parse("¬b"), 9);
//		PY.mergeLabeledValue(Label.parse("b"), -10);
//
//		LabeledIntEdgePluggable AP = new LabeledIntEdgePluggable("AP", null);
//		AP.mergeLabeledValue(Label.parse("p"), -1);
//
//		g.addEdge(XP, X, P);
//		g.addEdge(PY, P, Y);
//		g.addEdge(AP, A, P);
//
//
////		System.out.println("Children of A: "+g.getChildrenOf(A));
//		assertNull(g.getChildrenOf(A));
//		
////		System.out.println("Children of P: "+g.getChildrenOf(P));
//		assertEquals("{a}", g.getChildrenOf(P).toString());
//		
//	}
//	
//	
//	@SuppressWarnings({ "static-method", "javadoc" })
//	@Test
//	public final void cloneTest() {
//		LabeledIntGraphJung<LabeledIntEdgePluggable> g = new LabeledIntGraphJung<>(LabeledIntEdgePluggable.class);
//		LabeledNode Z = new LabeledNode("Z");
//		LabeledNode U = new LabeledNode("U");
//		g.addVertex(Z);
//		g.addVertex(U);
//		g.setZ(Z);
//		
//		Label l = Label.parse("¬p¬q");
//		LabeledIntEdgePluggable eUZ = new LabeledIntEdgePluggable("eUZ", LabeledIntEdgeSimple.ConstraintType.derived);
//		eUZ.mergeLabeledValue(Label.parse("¬p¿q"), -13);
//		eUZ.mergeLabeledValue(l, -13);
//		eUZ.mergeLabeledValue(Label.parse("¿p"), -15);
//		eUZ.mergeLabeledValue(Label.parse("¿p¬q"), -22);
//		eUZ.mergeLabeledValue(Label.emptyLabel, -10);
//		eUZ.mergeLabeledValue(Label.parse("¿p¿q"), Constants.INT_NEG_INFINITE);
//		
//		
//		g.addEdge(eUZ, U, Z);
//		
//		assertEquals(g, g);
//		
////		System.out.println(g);
//		
//		LabeledIntGraphJung<LabeledIntEdgePluggable> g1 = new LabeledIntGraphJung<>(LabeledIntEdgePluggable.class);
//		g1.copy(g);
//		 
//		
//		assertEquals(g.toString(), g1.toString());
//		assertTrue(g1.hasSameEdgesOf(g));
//
//		eUZ.removeLabel(l);
//		eUZ.putLabeledValue(Label.emptyLabel, -22);
//		
////		System.out.println(g);
//		assertFalse(g1.hasSameEdgesOf(g));
//
//		g1 = new LabeledIntGraphJung<>(g, LabeledIntEdgePluggable.class);
//		assertEquals(g.toString(), g1.toString());
//		assertTrue(g1.hasSameEdgesOf(g));
//	}
//
//	
//
//}
