package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.univr.di.cstnu.CSTN;
import it.univr.di.cstnu.WellDefinitionException;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.Literal;

import java.util.SortedSet;

import org.junit.Test;

/**
 * @author posenato
 *
 */
public class LabeledIntGraphTest {
	
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
	 * 
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testGetChildrenOf() {
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

//		System.out.println("Children of A: "+g.getChildrenOf(A));
		assertNull(g.getChildrenOf(A));
		
//		System.out.println("Children of P: "+g.getChildrenOf(P));
		assertEquals("{a}", g.getChildrenOf(P).toString());
		
	}
	
	
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void cloneTest() {
		LabeledIntGraph g = new LabeledIntGraph(true);
		LabeledNode Z = new LabeledNode("Z");
		LabeledNode U = new LabeledNode("U");
		g.addVertex(Z);
		g.addVertex(U);
		g.setZ(Z);
		
		SortedSet<String> XU = new ObjectAVLTreeSet<>();
		XU.add("X");
		XU.add("U");
		SortedSet<String> XUY = new ObjectAVLTreeSet<>();
		XUY.add("X");
		XUY.add("U");
		XUY.add("Y");
		
		SortedSet<String> YXW = new ObjectAVLTreeSet<>();
		YXW.add("Y");
		YXW.add("X");
		YXW.add("W");

		LabeledIntEdge eUZ = new LabeledIntEdge("eUZ", LabeledIntEdge.Type.derived, true);
		eUZ.mergeLabeledValue(Label.parse("¬p¿q"), -13, XU);
		eUZ.mergeLabeledValue(Label.parse("¬p¬q"), -13, XU);
		eUZ.mergeLabeledValue(Label.parse("¿p"), -15, null);
		eUZ.mergeLabeledValue(Label.parse("¿p¬q"), -22, XUY);
		eUZ.mergeLabeledValue(Label.emptyLabel, -10, null);
		eUZ.mergeLabeledValue(Label.parse("¿p¿q"), Constants.INT_NEG_INFINITE, YXW);
		
		
		g.addEdge(eUZ, U, Z);
		
		assertEquals(g, g);
		
		LabeledIntGraph g1 = new LabeledIntGraph(true);
		g1.copy(g);
		 
		
		assertEquals(g.toString(), g1.toString());
		assertTrue(g1.hasSameEdgesOf(g));
		
		g1 = new LabeledIntGraph(g, false);
		assertEquals(g.toString(), g1.toString());
		assertTrue(g1.hasSameEdgesOf(g));
		
	}

	

}
