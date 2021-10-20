package it.univr.di.cstnu.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.STNEdge;
import it.univr.di.cstnu.graph.STNEdgeInt;
import it.univr.di.cstnu.graph.TNGraphMLReader;

/**
 * @author posenato, ocampo
 */
public class STNTest {

	/**
	 * 
	 */
	STN stn;
	/**
	 * 
	 */
	TNGraphMLReader<STNEdge> graphMLReader;
	/**
	 * 
	 */
	String fileName = "src/test/resources/testSTNwithNegativeCycle.stn";
	/**
	 * 
	 */
	String fileName2 = "src/test/resources/testSTNwithNegativeCycle8nodes.stn";

	/**
	 */
	@Before
	public void setUp() {
		this.stn = new STN();
		this.graphMLReader = new TNGraphMLReader<>();
	}

	/**
	 * @throws Exception if the input file is not available
	 */
	@Test
	public void testBFCTWithNegativeCycle() throws Exception {

		this.stn.fInput = new File(this.fileName);

		this.stn.setG(this.graphMLReader.readGraph(this.stn.fInput, STNEdgeInt.class));

		boolean consistent = this.stn.BFCT();
		assertFalse(consistent);
		ObjectList<LabeledNode> cycle = this.stn.getCheckStatus().negativeCycle;
		assertNotNull(cycle);
		assertEquals("Negative cycle",
				"[❮1; Potential: 5\u276F, \u276EZ; Potential: -1\u276F, \u276E3; Potential: 2\u276F, \u276E1; Potential: 5❯]", cycle.toString());

	}

	/**
	 * @throws Exception if the input file is not available
	 */
	@Test
	public void testBFCT8Nodes() throws Exception {

		this.stn.fInput = new File(this.fileName2);

		this.stn.setG(this.graphMLReader.readGraph(this.stn.fInput, STNEdgeInt.class));
		try {
			this.stn.initAndCheck();
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"The STN graph has a problem and it cannot be initialize: " + e.getMessage());
		}
		boolean consistent = this.stn.BFCT();
		assertFalse(consistent);
		ObjectList<LabeledNode> cycle = this.stn.getCheckStatus().negativeCycle;
		assertNotNull(cycle);
		assertEquals("Empty cycle",
				"[\u276En9; Potential: -6\u276F, \u276EZ; Potential: -6\u276F, \u276En3; Potential: -5\u276F, \u276En9; Potential: -6\u276F]",
				cycle.toString());
		assertEquals("Node n7 ", -2, this.stn.getG().getNode("n7").getPotential());

	}

}
