/**
 * 
 */
package it.univr.di.cstnu.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.STNUEdge;
import it.univr.di.cstnu.graph.STNUEdgeInt;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.cstnu.graph.TNGraphMLReader;

/**
 * @author posenato
 */
public class STNUTest {

	/**
	 * 
	 */
	String fileName = "src/test/resources/testGraphML.stnu";

	/**
	 * 
	 */
	TNGraphMLReader<STNUEdge> readerSTNU;

	/**
	 * 
	 */
	TNGraph<STNUEdge> stnuGraph;

	/**
	 * 
	 */
	STNU stnu;

	private LabeledNode X, Ω, Y, Z;

	/**
	 * @throws java.lang.Exception  nope
	 */
	@Before
	public void setUp() throws Exception {
		this.readerSTNU = new TNGraphMLReader<>();
		this.stnuGraph = this.readerSTNU.readGraph(new File(this.fileName), STNUEdgeInt.class);
		this.X = this.stnuGraph.getNode("X");
		this.Ω = this.stnuGraph.getNode("Ω");
		this.Y = this.stnuGraph.getNode("Y");
		this.Z = this.stnuGraph.getZ();
		this.stnuGraph.addEdge(new STNUEdgeInt("ΩY", -4), this.Ω, this.Y);
		this.stnuGraph.addEdge(new STNUEdgeInt("XZ", -4), this.X, this.Z);
		this.stnuGraph.addEdge(new STNUEdgeInt("XΩ", 10), this.X, this.Ω);
		this.stnuGraph.addEdge(new STNUEdgeInt("ZΩ", 11), this.Z, this.Ω);
		this.stnu = new STNU(this.stnuGraph);
		try {
			this.stnu.initAndCheck();
		} catch (WellDefinitionException e) {
			e.printStackTrace();
		}
	}

	/**
	 */
	@Test
	public void testBellmanFordOL() {
		this.testGraph();
		Object2IntMap<LabeledNode> potential = this.stnu.bellmanFordOL();
		Object2IntMap<LabeledNode> expected = new Object2IntOpenHashMap<>();

		expected.put(this.Y, 0);
		expected.put(this.Ω, 4);
		expected.put(this.Z, 0);
		expected.put(this.X, 4);
		assertEquals("BellmanFord", 4, expected.size());
		assertEquals("BellmanFord", expected, potential);
		this.stnuGraph.addEdge(new STNUEdgeInt("ΩX", -8), this.Ω, this.X);
		potential = this.stnu.bellmanFordOL();
		assertNull("BellmanFord", potential);
	}

	/**
	 */
	@Test
	public void testNormalForm() {
		this.testGraph();
		this.stnuGraph.addEdge(new STNUEdgeInt("ΩX", -1), this.Ω, this.X);
		this.stnu.makeNormalForm();
		String graph = "%TNGraph: src/test/resources/testGraphML.stnu\n" +
				"%Nodes:\n" +
				"❮Z❯\n" +
				"❮Ω❯\n" +
				"❮____X❯\n" +
				"❮X❯\n" +
				"❮Y❯\n" +
				"%Edges:\n" +
				"❮X❯--❮XY; contingent; LC(Y):0❯-->❮Y❯\n" +
				"❮X❯--❮X_-____X; internal; -2; ❯-->❮____X❯\n" +
				"❮Y❯--❮YX; contingent; UC(Y):-3❯-->❮X❯\n" +
				"❮Z❯--❮ZΩ; normal; 11; ❯-->❮Ω❯\n" +
				"❮____X❯--❮____X--Z; normal; -4; ❯-->❮Z❯\n" +
				"❮____X❯--❮____X--Ω; normal; 10; ❯-->❮Ω❯\n" +
				"❮____X❯--❮____X_-X; internal; 2; ❯-->❮X❯\n" +
				"❮Ω❯--❮Ω--____X; normal; -1; ❯-->❮____X❯\n" +
				"❮Ω❯--❮ΩY; normal; -4; ❯-->❮Y❯\n";
		assertEquals(graph, this.stnuGraph.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.STNU#getActivationNode()}.
	 */
	@Test
	public void testGetActivationNode() {
		this.testGraph();
		LabeledNode x = this.stnu.getActivationNode().get(this.Y);
		assertEquals("Activation  node", this.X, x);
		assertNull("Activation  node", this.stnu.getActivationNode().get(this.X));
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.STNU#getLowerContingentEdge()}.
	 */
	@Test
	public void testGetLowerContingentEdge() {
		this.testGraph();
		STNUEdge lce = this.stnu.getLowerContingentEdge().get(this.Y);
		assertEquals("Lower case edge", "❮XY; contingent; LC(Y):2❯", lce.toString());
		assertEquals("Lower case edge", 1, this.stnu.getLowerContingentEdge().size());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.STNU#getUpperContingentEdge()}.
	 */
	@Test
	public void testGetUpperContingentEdge() {
		this.testGraph();
		STNUEdge lce = this.stnu.getUpperContingentEdge().get(this.Y);
		assertEquals("Upper case edge", "❮YX; contingent; UC(Y):-5❯", lce.toString());
		assertEquals("Upper case edge", 1, this.stnu.getLowerContingentEdge().size());
	}

	/**
	 * 
	 */
	@Test
	public void testGraph() {
		String graph = "%TNGraph: src/test/resources/testGraphML.stnu\n" +
				"%Nodes:\n" +
				"❮Z❯\n" +
				"❮Ω❯\n" +
				"❮X❯\n" +
				"❮Y❯\n" +
				"%Edges:\n" +
				"❮X❯--❮XY; contingent; LC(Y):2❯-->❮Y❯\n" +
				"❮X❯--❮XZ; normal; -4; ❯-->❮Z❯\n" +
				"❮X❯--❮XΩ; normal; 10; ❯-->❮Ω❯\n" +
				"❮Y❯--❮YX; contingent; UC(Y):-5❯-->❮X❯\n" +
				"❮Z❯--❮ZΩ; normal; 11; ❯-->❮Ω❯\n" +
				"❮Ω❯--❮ΩY; normal; -4; ❯-->❮Y❯\n";
		assertEquals(graph, this.stnuGraph.toString());
	}

}
