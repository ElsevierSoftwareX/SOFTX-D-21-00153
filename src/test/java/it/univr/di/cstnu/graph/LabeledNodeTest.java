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
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * @author posenato
 */
public class LabeledNodeTest {

	@SuppressWarnings("javadoc")
	static LabeledNodeSupplier<LabeledIntTreeMap> nodeFactory = new LabeledNodeSupplier<>(LabeledIntTreeMap.class);

	/**
	 * 
	 */
	ALabelAlphabet alphabet;

	/**
	 * 
	 */
	LabeledNode a;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.alphabet = new ALabelAlphabet();
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
		this.a.putPotential(Label.parse("b"), -1);
		this.a.putPotential(Label.parse("b"), -1);// ignored
		this.a.putPotential(Label.parse("a"), -1);

		assertEquals("{b->-1, a->-1}", this.a.getPotential().entrySet().toString());

		this.a.putPotential(Label.parse("¬b"), -1);
		assertEquals("{⊡->-1}", this.a.getPotential().entrySet().toString());
	}

}
