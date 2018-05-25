/**
 * 
 */
package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import it.univr.di.cstnu.graph.LabeledIntEdge.ConstraintType;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.ALabelAlphabet;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * @author posenato
 *
 */
public class AbstractLabeledIntEdgeTest {

	@SuppressWarnings("javadoc")
	static LabeledIntEdgeSupplier<LabeledIntTreeMap> edgeFactory = new LabeledIntEdgeSupplier<>(LabeledIntTreeMap.class);
	@SuppressWarnings("javadoc")
	static Class<? extends LabeledIntMap> labeledValueClass = edgeFactory.get().labeledValueMapFactory.get().getClass();

	/**
	 * 
	 */
	ALabelAlphabet alpha;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.alpha = new ALabelAlphabet();
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.AbstractLabeledIntEdge#clear()}.
	 */
	@Test
	public void testClear() {
		LabeledIntEdgePluggable e = edgeFactory.get();
		e.setName("e");
		e.setConstraintType(ConstraintType.contingent);
		e.mergeLabeledValue(Label.emptyLabel, 1);
		e.mergeUpperCaseValue(Label.emptyLabel, new ALabel("A", this.alpha), 0);
		assertEquals("❮e; contingent; {(1, ⊡) }; UL: {(A, 0, ⊡) }; ❯", e.toString());

		e.clear();
		assertEquals("❮e; contingent; ❯", e.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.AbstractLabeledIntEdge#clearLowerCaseValue()}.
	 */
	@Test
	public void testClearLowerCaseValue() {
		LabeledIntEdgePluggable e = edgeFactory.get();
		e.setName("e");
		e.setConstraintType(ConstraintType.contingent);
		e.mergeLabeledValue(Label.emptyLabel, 1);
		e.setLowerCaseValue(Label.emptyLabel, new ALabel("A", this.alpha), 1);

		assertEquals("❮e; contingent; {(1, ⊡) }; LL: {(A, 1, ⊡) };❯", e.toString());

		e.clearLowerCaseValue();
		assertEquals("❮e; contingent; {(1, ⊡) }; ❯", e.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.AbstractLabeledIntEdge#equalsAllLabeledValues(it.univr.di.cstnu.graph.LabeledIntEdge)}.
	 */
	@Test
	public void testEqualsAllLabeledValues() {
		LabeledIntEdgePluggable e = edgeFactory.get();
		e.setName("e");
		e.setConstraintType(ConstraintType.contingent);
		e.mergeLabeledValue(Label.emptyLabel, 1);
		e.setLowerCaseValue(Label.emptyLabel, new ALabel("A", this.alpha), 1);
		e.mergeUpperCaseValue(Label.emptyLabel, new ALabel("A", this.alpha), -1);
		
		LabeledIntEdgePluggable e1 = edgeFactory.get();
		e1.setName("e1");
		e1.setConstraintType(ConstraintType.contingent);
		e1.mergeLabeledValue(Label.emptyLabel, 1);
		e1.setLowerCaseValue(Label.emptyLabel, new ALabel("A", this.alpha), 1);
		e1.mergeUpperCaseValue(Label.emptyLabel, new ALabel("A", this.alpha), -1);
		
		assertTrue(e.equalsAllLabeledValues(e1));
	}


}
