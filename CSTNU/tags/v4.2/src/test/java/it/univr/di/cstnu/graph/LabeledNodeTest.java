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
 */
public class LabeledNodeTest {

	/**
	 * 
	 */
	static LabeledNodeSupplier nodeFactory = new LabeledNodeSupplier();

	/**
	 * 
	 */
	ALabelAlphabet alphabet;

	/**
	 * 
	 */
	LabeledNode a;

	/**
	 * @throws java.lang.Exception  none
	 */
	@Before
	public void setUp() throws Exception {
//		this.alphabet = new ALabelAlphabet();
		this.a = nodeFactory.get("A");
		this.a.setLabel(Label.emptyLabel);
	}

	/**
	 */
	@SuppressWarnings("deprecation")
	@Test
	public final void testEquals() {
		LabeledNode aa = nodeFactory.get("A");
		aa.setLabel(Label.emptyLabel);

		assertTrue(this.a.equalsByName(aa));
		assertFalse(this.a.equals(aa));
	}

	/**
	 */
	@Test
	public final void potentialPut1() {
		this.a.putLabeledPotential(Label.parse("b"), -1);
		this.a.putLabeledPotential(Label.parse("b"), -1);// ignored
		this.a.putLabeledPotential(Label.parse("a"), -1);

		assertEquals("{b->-1, a->-1}", this.a.getLabeledPotential().entrySet().toString());

		this.a.putLabeledPotential(Label.parse("¬b"), -1);
		assertEquals("{⊡->-1}", this.a.getLabeledPotential().entrySet().toString());
	}

}
