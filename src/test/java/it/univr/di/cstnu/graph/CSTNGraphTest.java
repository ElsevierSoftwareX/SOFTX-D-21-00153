/**
 * 
 */
package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;

/**
 * @author posenato
 */
public class CSTNGraphTest {

	/**
	 * 
	 */
	static Class<? extends CSTNEdge> edgeImplClass = CSTNEdgePluggable.class;

	static EdgeSupplier<CSTNEdge> edgeFactory = new EdgeSupplier<>(edgeImplClass);

	@SuppressWarnings({ "static-method" })
	@Test
	public void enlarge() {
		int order = 4;
		int[][] adjacency = new int[order][order];
		int currentSize = adjacency.length;
		for (int i = 0; i < currentSize; i++) {
			for (int j = 0; j < currentSize; j++) {
				adjacency[i][j] = (i + 1) * (j + 1);
			}
		}

		// System.out.println("Adjacency prima di enlarge: " + Arrays.deepToString(adjacency) + "\nlenght:" + adjacency.length);
		assertEquals("[[1, 2, 3, 4], [2, 4, 6, 8], [3, 6, 9, 12], [4, 8, 12, 16]]", Arrays.deepToString(adjacency));

		currentSize *= 1.8f;
		// System.out.println("currentSize:" + currentSize);
		assertEquals(7, currentSize);
		adjacency = Arrays.copyOf(adjacency, currentSize);
		for (int j = 0; j < order; j++) {
			adjacency[j] = Arrays.copyOf(adjacency[j], currentSize);
		}
		for (int j = order; j < currentSize; j++) {
			adjacency[j] = new int[currentSize];
		}
		// System.out.println("Adjacency dopo di enlarge: " + Arrays.deepToString(adjacency) + "\nlenght:" + adjacency.length);
		assertEquals(
				"[[1, 2, 3, 4, 0, 0, 0], [2, 4, 6, 8, 0, 0, 0], [3, 6, 9, 12, 0, 0, 0], [4, 8, 12, 16, 0, 0, 0], [0, 0, 0, 0, 0, 0, 0], [0, 0, 0, 0, 0, 0, 0], [0, 0, 0, 0, 0, 0, 0]]",
				Arrays.deepToString(adjacency));
		int nodeIndexToRem = 1;
		int last = order - 1;
		for (int i = 0; i < last; i++) {
			if (i == nodeIndexToRem) {
				adjacency[i][i] = adjacency[last][last];
				adjacency[last][last] = adjacency[i][last] = adjacency[last][i] = 0;
				continue;
			}
			adjacency[i][nodeIndexToRem] = adjacency[i][last];
			adjacency[i][last] = 0;
			adjacency[nodeIndexToRem][i] = adjacency[last][i];
			adjacency[last][i] = 0;
		}
		// System.out.println("Adjacency dopo cancellazione indice 1: " + Arrays.deepToString(adjacency) + "\nlenght:" + adjacency.length);
		assertEquals(
				"[[1, 4, 3, 0, 0, 0, 0], [4, 16, 12, 0, 0, 0, 0], [3, 12, 9, 0, 0, 0, 0], [0, 0, 0, 0, 0, 0, 0], [0, 0, 0, 0, 0, 0, 0], [0, 0, 0, 0, 0, 0, 0], [0, 0, 0, 0, 0, 0, 0]]",
				Arrays.deepToString(adjacency));
	}

	@SuppressWarnings({ "static-method" })
	@Test
	public void create() {
		TNGraph<CSTNEdge> g = new TNGraph<>("prova", edgeImplClass);

		g.addVertex(g.getNodeFactory().get("Z"));
		g.addVertex(g.getNodeFactory().get("X"));
		g.addEdge(edgeFactory.get("ZX"), "Z", "X");
		g.addEdge(edgeFactory.get("ZX"), "X", "Z");
		g.addEdge(edgeFactory.get("XZ"), "X", "Z");

		assertEquals(g.getVertexCount(), 2);
		assertEquals(g.getEdgeCount(), 2);
	}

	@SuppressWarnings({ "static-method" })
	@Test
	public void removeNode() {
		TNGraph<CSTNEdge> g = new TNGraph<>("prova", edgeImplClass);

		LabeledNode X = g.getNodeFactory().get("X");
		LabeledNode Z = g.getNodeFactory().get("Z");
		g.addVertex(Z);
		g.addVertex(X);
		g.addEdge(edgeFactory.get("ZX"), Z, X);
		g.addEdge(edgeFactory.get("ZX"), "X", "Z");
		g.addEdge(edgeFactory.get("XZ"), X, Z);

		g.removeVertex(X);
		assertEquals(g.getVertexCount(), 1);
		assertEquals(g.getEdgeCount(), 0);

	}

	@SuppressWarnings({ "static-method" })
	@Test
	public void addManyNodes() {
		TNGraph<CSTNEdge> g = new TNGraph<>("prova", edgeImplClass);

		LabeledNode X = g.getNodeFactory().get("X");
		LabeledNode Z = g.getNodeFactory().get("Z");
		g.addVertex(Z);
		g.addVertex(X);
		g.addVertex(g.getNodeFactory().get("A3"));
		g.addVertex(g.getNodeFactory().get("A4"));
		g.addVertex(g.getNodeFactory().get("A5"));
		g.addVertex(g.getNodeFactory().get("A6"));
		g.addVertex(g.getNodeFactory().get("A7"));
		g.addVertex(g.getNodeFactory().get("A8"));
		g.addVertex(g.getNodeFactory().get("A9"));
		g.addVertex(g.getNodeFactory().get("A10"));
		g.addVertex(g.getNodeFactory().get("A11"));
		g.addEdge(edgeFactory.get("ZX"), Z, X);
		g.addEdge(edgeFactory.get("ZX"), "X", "Z");
		g.addEdge(edgeFactory.get("XZ"), X, Z);
		g.addEdge(edgeFactory.get("Z3"), "Z", "A3");
		g.addEdge(edgeFactory.get("Z4"), "Z", "A4");
		g.addEdge(edgeFactory.get("Z5"), "Z", "A5");
		g.addEdge(edgeFactory.get("Z6"), "Z", "A6");
		g.addEdge(edgeFactory.get("Z7"), "Z", "A7");
		g.addEdge(edgeFactory.get("Z8"), "Z", "A8");
		g.addEdge(edgeFactory.get("Z9"), "Z", "A9");
		g.addEdge(edgeFactory.get("Z10"), "Z", "A10");
		g.addEdge(edgeFactory.get("Z11"), "Z", "A11");

		g.addEdge(edgeFactory.get("A3_11"), "A3", "A11");
		g.addEdge(edgeFactory.get("A11_3"), "A11", "A3");
		g.addEdge(edgeFactory.get("A4_11"), "A4", "A11");
		g.addEdge(edgeFactory.get("A11_4"), "A11", "A4");
		g.addEdge(edgeFactory.get("A5_11"), "A5", "A11");
		g.addEdge(edgeFactory.get("A11_5"), "A11", "A5");
		// System.out.println("G: "+g);
		g.removeVertex(Z);
		@SuppressWarnings("unused")
		String expected = "%TNGraph: prova\n" +
				"%TNGraph Syntax\n" +
				"%LabeledNode: <name, label, proposition observed>\n" +
				"%T: <name, type, source node, dest. node, L:{labeled values}, LL:{lower case labeled values}, UL:{upper case labeled values}>\n" +
				"Nodes:\n" +
				"<A10,	⊡,	null>\n" +
				"<A11,	⊡,	null>\n" +
				"<A3,	⊡,	null>\n" +
				"<A4,	⊡,	null>\n" +
				"<A5,	⊡,	null>\n" +
				"<A6,	⊡,	null>\n" +
				"<A7,	⊡,	null>\n" +
				"<A8,	⊡,	null>\n" +
				"<A9,	⊡,	null>\n" +
				"<X,	⊡,	null>\n" +
				"Edges:\n" +
				"<A11_3,	normal,	A11,	A3,	L:{}, LL:{}, UL:{}>\n" +
				"<A11_4,	normal,	A11,	A4,	L:{}, LL:{}, UL:{}>\n" +
				"<A11_5,	normal,	A11,	A5,	L:{}, LL:{}, UL:{}>\n" +
				"<A3_11,	normal,	A3,	A11,	L:{}, LL:{}, UL:{}>\n" +
				"<A4_11,	normal,	A4,	A11,	L:{}, LL:{}, UL:{}>\n" +
				"<A5_11,	normal,	A5,	A11,	L:{}, LL:{}, UL:{}>\n";
		assertEquals(g.getVertexCount(), 10);
		assertEquals(g.getEdgeCount(), 6);
	}

	/**
	 * 
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testGetChildrenOf() {
		TNGraph<CSTNEdge> g = new TNGraph<>(edgeImplClass);
		LabeledNode P = g.getNodeFactory().get("P?", 'p');
		LabeledNode X = g.getNodeFactory().get("X");
		LabeledNode Y = g.getNodeFactory().get("Y");
		LabeledNode A = g.getNodeFactory().get("A?", 'a');
		A.setLabel(Label.parse("p"));
		LabeledNode B = g.getNodeFactory().get("B?", 'b');
		B.setLabel(Label.parse("¬pa"));

		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(Y);

		// wellDefinition(g);

		// System.out.println("Children of A: "+g.getChildrenOf(A));
		assertNull(g.getChildrenOf(B));

		assertEquals("b", g.getChildrenOf(A).toString());

		// System.out.println("Children of P: "+g.getChildrenOf(P));
		assertEquals("ab", g.getChildrenOf(P).toString());

		B.setLabel(Label.emptyLabel);
		// System.out.println("Children of P: "+g.getChildrenOf(P));
		assertEquals("a", g.getChildrenOf(P).toString());
		assertNull(g.getChildrenOf(A));

	}

	@SuppressWarnings({ "static-method" })
	@Test
	public final void cloneTest() {
		TNGraph<CSTNEdge> g = new TNGraph<>(edgeImplClass);
		LabeledNode Z = g.getNodeFactory().get("Z");
		LabeledNode U = g.getNodeFactory().get("U");
		g.addVertex(Z);
		g.addVertex(U);
		g.setZ(Z);

		Label l = Label.parse("¬p¬q");
		CSTNEdge eUZ = edgeFactory.get("eUZ");
		eUZ.mergeLabeledValue(Label.parse("¬p¿q"), -13);
		eUZ.mergeLabeledValue(l, -13);
		eUZ.mergeLabeledValue(Label.parse("¿p"), -15);
		eUZ.mergeLabeledValue(Label.parse("¿p¬q"), -22);
		eUZ.mergeLabeledValue(Label.emptyLabel, -10);
		eUZ.mergeLabeledValue(Label.parse("¿p¿q"), Constants.INT_NEG_INFINITE);

		g.addEdge(eUZ, U, Z);

		assertEquals(g, g);

		// System.out.println(g);

		TNGraph<CSTNEdge> g1 = new TNGraph<>(edgeImplClass);
		g1.copy(g);

		// System.out.println(g1);

		assertEquals(g.toString(), g1.toString());
		assertTrue(g1.hasSameEdgesOf(g));

		eUZ.removeLabeledValue(l);
		eUZ.putLabeledValue(Label.emptyLabel, -22);

		// System.out.println(g);
		assertFalse(g1.hasSameEdgesOf(g));

		g1 = new TNGraph<>(g, edgeImplClass);
		assertEquals(g.toString(), g1.toString());
		assertTrue(g1.hasSameEdgesOf(g));
	}

	@SuppressWarnings({ "static-method" })
	@Test
	public void managinDifferentEdges() {
		TNGraph<CSTNEdge> g = new TNGraph<>("prova", edgeImplClass);

		LabeledNode X = g.getNodeFactory().get("X");
		LabeledNode Z = g.getNodeFactory().get("Z");
		g.addVertex(Z);
		g.addVertex(X);
		g.addVertex(g.getNodeFactory().get("A3"));
		g.addVertex(g.getNodeFactory().get("A4"));
		g.addVertex(g.getNodeFactory().get("A5"));
		g.addVertex(g.getNodeFactory().get("A6"));
		g.addVertex(g.getNodeFactory().get("A7"));
		g.addVertex(g.getNodeFactory().get("A8"));
		g.addVertex(g.getNodeFactory().get("A9"));
		g.addVertex(g.getNodeFactory().get("A10"));
		g.addVertex(g.getNodeFactory().get("A11"));
		g.addEdge(edgeFactory.get("ZX"), Z, X);
		g.addEdge(edgeFactory.get("XZ"), X, Z);
		g.addEdge(edgeFactory.get("Z3"), "Z", "A3");
		g.addEdge(edgeFactory.get("Z4"), "Z", "A4");
		g.addEdge(edgeFactory.get("Z5"), "Z", "A5");
		g.addEdge(edgeFactory.get("Z6"), "Z", "A6");
		g.addEdge(edgeFactory.get("Z7"), "Z", "A7");
		g.addEdge(edgeFactory.get("Z8"), "Z", "A8");
		g.addEdge(edgeFactory.get("Z9"), "Z", "A9");
		g.addEdge(edgeFactory.get("Z10"), "Z", "A10");
		g.addEdge(edgeFactory.get("Z11"), "Z", "A11");
		g.addEdge(edgeFactory.get("A3_11"), "A3", "A11");
		g.addEdge(edgeFactory.get("A11_3"), "A11", "A3");
		g.addEdge(edgeFactory.get("A4_11"), "A4", "A11");
		g.addEdge(edgeFactory.get("A11_4"), "A11", "A4");
		g.addEdge(edgeFactory.get("A5_11"), "A5", "A11");
		g.addEdge(edgeFactory.get("A11_5"), "A11", "A5");
		// System.out.println("G: "+g);
		g.removeVertex(Z);

		CSTNEdge e1;
		for (CSTNEdge edge : g.getEdges()) {
			e1 = edgeFactory.get(edge.getName() + "new");
			edge.takeIn(e1);
		}
		@SuppressWarnings("unused")
		String expected = "%TNGraph: prova\n" +
				"%TNGraph Syntax\n" +
				"%LabeledNode: <name, label, proposition observed>\n" +
				"%T: <name, type, source node, dest. node, L:{labeled values}, LL:{lower case labeled values}, UL:{upper case labeled values}>\n" +
				"Nodes:\n" +
				"<A10,	⊡,	null>\n" +
				"<A11,	⊡,	null>\n" +
				"<A3,	⊡,	null>\n" +
				"<A4,	⊡,	null>\n" +
				"<A5,	⊡,	null>\n" +
				"<A6,	⊡,	null>\n" +
				"<A7,	⊡,	null>\n" +
				"<A8,	⊡,	null>\n" +
				"<A9,	⊡,	null>\n" +
				"<X,	⊡,	null>\n" +
				"Edges:\n" +
				"<A11_3,	normal,	A11,	A3,	L:{}, LL:{}, UL:{}>\n" +
				"<A11_4,	normal,	A11,	A4,	L:{}, LL:{}, UL:{}>\n" +
				"<A11_5,	normal,	A11,	A5,	L:{}, LL:{}, UL:{}>\n" +
				"<A3_11,	normal,	A3,	A11,	L:{}, LL:{}, UL:{}>\n" +
				"<A4_11,	normal,	A4,	A11,	L:{}, LL:{}, UL:{}>\n" +
				"<A5_11,	normal,	A5,	A11,	L:{}, LL:{}, UL:{}>\n";
		assertEquals(g.getVertexCount(), 10);
		assertEquals(g.getEdgeCount(), 6);
	}

	@SuppressWarnings({ "static-method" })
	@Test
	public void reverse() {
		TNGraph<CSTNEdge> g = new TNGraph<>("prova", edgeImplClass);

		LabeledNode X = g.getNodeFactory().get("X");
		LabeledNode Z = g.getNodeFactory().get("Z");
		g.addVertex(Z);
		g.addVertex(X);
		g.addVertex(g.getNodeFactory().get("A3"));
		g.addVertex(g.getNodeFactory().get("A4"));
		g.addVertex(g.getNodeFactory().get("A5"));
		g.addVertex(g.getNodeFactory().get("A6"));
		g.addVertex(g.getNodeFactory().get("A7"));
		g.addVertex(g.getNodeFactory().get("A8"));
		g.addVertex(g.getNodeFactory().get("A9"));
		g.addVertex(g.getNodeFactory().get("A10"));
		g.addVertex(g.getNodeFactory().get("A11"));
		g.addEdge(edgeFactory.get("ZX"), Z, X);
		g.addEdge(edgeFactory.get("Z3"), "Z", "A3");
		g.addEdge(edgeFactory.get("Z4"), "Z", "A4");
		g.addEdge(edgeFactory.get("A3_11"), "A3", "A11");
		g.addEdge(edgeFactory.get("A11_3"), "A11", "A3");
		g.addEdge(edgeFactory.get("A4_11"), "A4", "A11");
		g.addEdge(edgeFactory.get("A11_4"), "A11", "A4");
		g.addEdge(edgeFactory.get("A11_5"), "A11", "A5");
		// System.out.println("G: "+g);

		CSTNEdge e1;
		for (CSTNEdge edge : g.getEdges()) {
			e1 = edgeFactory.get(edge.getName() + "new");
			edge.takeIn(e1);
		}
		g.reverse();
		assertEquals(g.getVertexCount(), 11);
		assertEquals(g.getEdgeCount(), 8);
		String expected = "%TNGraph: prova\n" +
				"%Nodes:\n" +
				"❮Z; ⊡❯\n" +
				"❮A3; ⊡❯\n" +
				"❮A7; ⊡❯\n" +
				"❮A5; ⊡❯\n" +
				"❮A9; ⊡❯\n" +
				"❮A10; ⊡❯\n" +
				"❮A11; ⊡❯\n" +
				"❮X; ⊡❯\n" +
				"❮A4; ⊡❯\n" +
				"❮A8; ⊡❯\n" +
				"❮A6; ⊡❯\n" +
				"%Edges:\n" +
				"❮A11_3; normal; ❯\n" +
				"❮A11_4; normal; ❯\n" +
				"❮A11_5; normal; ❯\n" +
				"❮A3_11; normal; ❯\n" +
				"❮A4_11; normal; ❯\n" +
				"❮Z3; normal; ❯\n" +
				"❮Z4; normal; ❯\n" +
				"❮ZX; normal; ❯\n";
		assertEquals("Reversed tNGraph:", expected, g.toString());
	}
}
