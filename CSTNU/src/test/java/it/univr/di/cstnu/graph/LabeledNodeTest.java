/**
 * 
 */
package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import it.univr.di.labeledvalue.Label;

import org.junit.Before;
import org.junit.Test;


/**
 * @author posenato
 *
 */
public class LabeledNodeTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
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
}
