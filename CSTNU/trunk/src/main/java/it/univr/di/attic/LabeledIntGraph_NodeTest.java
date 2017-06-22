package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.SortedSet;

import org.junit.Test;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.cstnu.CSTN_NodeSet;
import it.univr.di.cstnu.algorithms.WellDefinitionException;
import it.univr.di.cstnu.graph.LabeledIntEdgeFactory;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntNodeSetTreeMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * @author posenato
 *
 */
public class LabeledIntGraph_NodeTest {

	@SuppressWarnings("javadoc")
	static LabeledIntEdgeFactory<LabeledIntTreeMap> edgeWithOUTNodeSetFactory = new LabeledIntEdgeFactory<>(LabeledIntTreeMap.class);
	@SuppressWarnings("javadoc")
	static LabeledIntEdgeFactory<LabeledIntNodeSetTreeMap> edgeWithNodeSetFactory = new LabeledIntEdgeFactory<>(LabeledIntNodeSetTreeMap.class);

	/**
	 * @param g
	 */
	private final static void wellDefinition(LabeledIntGraph g) {
		CSTN_NodeSet cstn = new CSTN_NodeSet();
		try {
			cstn.checkWellDefinitionProperties(g);
		} catch (WellDefinitionException e) {
			fail("LabeledIntGraph not well formed: " + e.getMessage());
		}
	}

	/**
	 * 
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testGetChildrenOf() {
		LabeledIntGraph g = new LabeledIntGraph(LabeledIntNodeSetTreeMap.class);
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

		LabeledIntEdgePluggable XP = new LabeledIntEdgePluggable("XP", LabeledIntNodeSetTreeMap.class);
		XP.mergeLabeledValue(Label.emptyLabel, 10);
		XP.mergeLabeledValue(Label.parse("¬ap"), 8);
		XP.mergeLabeledValue(Label.parse("¬b"), -1);
		XP.mergeLabeledValue(Label.parse("bap"), 9);

		LabeledIntEdgePluggable PY = new LabeledIntEdgePluggable("PY", LabeledIntNodeSetTreeMap.class);
		PY.mergeLabeledValue(Label.parse("¬b"), 9);
		PY.mergeLabeledValue(Label.parse("b"), -10);

		LabeledIntEdgePluggable AP = new LabeledIntEdgePluggable("AP", LabeledIntNodeSetTreeMap.class);
		AP.mergeLabeledValue(Label.parse("p"), -1);

		g.addEdge(XP, X, P);
		g.addEdge(PY, P, Y);
		g.addEdge(AP, A, P);

		wellDefinition(g);

		//		System.out.println("Children of A: "+g.getChildrenOf(A));
		assertNull(g.getChildrenOf(A));

		//		System.out.println("Children of P: "+g.getChildrenOf(P));
		assertEquals("[a]", Arrays.toString(g.getChildrenOf(P)));

	}

	@SuppressWarnings({ "static-method", "javadoc", "unchecked" })
	@Test
	public final void cloneTest() {
		LabeledIntGraph g = new LabeledIntGraph(LabeledIntNodeSetTreeMap.class);
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

		LabeledIntEdgePluggable eUZ = new LabeledIntEdgePluggable("eUZ", LabeledIntNodeSetTreeMap.class);
		eUZ.mergeLabeledValue(Label.parse("¬p¿q"), -13, (ObjectSet<String>) XU);
		eUZ.mergeLabeledValue(Label.parse("¬p¬q"), -13, (ObjectSet<String>) XU);
		eUZ.mergeLabeledValue(Label.parse("¿p"), -15, null);
		eUZ.mergeLabeledValue(Label.parse("¿p¬q"), -22, (ObjectSet<String>) XUY);
		eUZ.mergeLabeledValue(Label.emptyLabel, -10, null);
		eUZ.mergeLabeledValue(Label.parse("¿p¿q"), Constants.INT_NEG_INFINITE, (ObjectSet<String>) YXW);

		g.addEdge(eUZ, U, Z);

		assertEquals(g, g);

		LabeledIntGraph g1 = new LabeledIntGraph(LabeledIntNodeSetTreeMap.class);
		g1.copy(g);

		assertEquals(g.toString(), g1.toString());
		assertTrue(g1.hasSameEdgesOf(g));

		g1 = new LabeledIntGraph(g, LabeledIntNodeSetTreeMap.class);
		assertEquals(g.toString(), g1.toString());
		assertTrue(g1.hasSameEdgesOf(g));

	}

}
