/**
 * 
 */
package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import it.univr.di.labeledvalue.ALabelAlphabet;
import it.univr.di.labeledvalue.Label;


/**
 * @author posenato
 *
 */
public class LabeledNodeTest {

	/**
	 * 
	 */
	ALabelAlphabet alphabet;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.alphabet = new ALabelAlphabet();
	}


	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledNode#LabeledNode(java.lang.String)}.
	 */
	@SuppressWarnings({ "deprecation", "static-method" })
	@Test
	public final void testEquals() {
		LabeledNode a = new LabeledNode("A");
		LabeledNode aa = new LabeledNode("A", Label.emptyLabel);
		
		assertTrue(a.equalsByName(aa));
		assertFalse(a.equals(aa));
	}
	
	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledNode#LabeledNode(java.lang.String)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void potentialPut1() {
		LabeledNode a = new LabeledNode("A", Label.emptyLabel);
		a.putPotential(Label.parse("b"), -1);
		a.putPotential(Label.parse("b"), -1);// ignored
		a.putPotential(Label.parse("a"), -1);

		assertEquals("{b->-1, a->-1}", a.getPotential().entrySet().toString());

		a.putPotential(Label.parse("¬b"), -1);
		assertEquals("{⊡->-1}", a.getPotential().entrySet().toString());
	}

}
