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
import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledContingentIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * @author posenato
 *
 */
public class LabeledIntEdgeSimpleTest {

	@SuppressWarnings("javadoc")
	static LabeledIntEdgeFactory<LabeledIntTreeMap> edgeFactory = new LabeledIntEdgeFactory<>(LabeledIntTreeMap.class);
	@SuppressWarnings("javadoc")
	static Class<? extends LabeledIntMap> labeledValueClass = edgeFactory.create().labeledValueMapFactory.create().getClass();
	
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
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdgePluggable#toString()}.
	 */
	@Test
	public final void testToString() {
		LabeledIntEdgePluggable e = edgeFactory.create();
		e.setName("e");
		e.setConstraintType(ConstraintType.contingent);
		e.mergeLabeledValue(Label.emptyLabel, 1);
		assertEquals("❮e; contingent; {(1, ⊡) }; ❯", e.toString());
		e.mergeUpperLabelValue(Label.emptyLabel, new ALabel("A", this.alpha), 0);
		assertEquals("❮e; contingent; {(1, ⊡) }; UL: {(A, 0, ⊡) }; ❯", e.toString());

	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdgePluggable#getAllUpperCaseAndOrdinaryLabeledValuesSet()}.
	 */
	@Test
	public final void testGetAllUpperCaseAndOrdinaryLabeledValuesSet() {
		LabeledIntEdgePluggable e = edgeFactory.create();
		e.setName("e");
		e.mergeLabeledValue(Label.emptyLabel, 1);
		e.mergeUpperLabelValue(Label.emptyLabel, new ALabel("A", this.alpha), 0);
		e.mergeLowerLabelValue(Label.emptyLabel, new ALabel("B", this.alpha), -1);
		
		assertEquals("{⊡=A->0, ⊡->◇->1}", e.getAllUpperCaseAndOrdinaryLabeledValuesSet().toString());

	}

	/**
	 */
	@Test
	public final void testGetUpperLabelValueLabelString() {
		LabeledIntEdgePluggable e = edgeFactory.create();
		e.setName("e");
		e.mergeLabeledValue(Label.emptyLabel, 1);
		e.mergeUpperLabelValue(Label.emptyLabel, new ALabel("A", this.alpha), 0);
		e.mergeUpperLabelValue(Label.emptyLabel, new ALabel("B", this.alpha), -1);
		
		assertEquals(0, e.getUpperLabelValue(Label.emptyLabel, new ALabel("A", this.alpha)));
	}

	/**
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testMergeUpperLabeledValues() {
		LabeledIntEdgePluggable e = edgeFactory.create();
		e.setName("e");
		ALabel aleph = new ALabel("C1_E", new ALabelAlphabet());
		aleph.conjunct(new ALetter("C3_E"));
		e.mergeUpperLabelValue(Label.parse("a¿b"), aleph, -2);
		aleph.conjunct(new ALetter("C2_E"));
		e.putUpperLabeledValueToRemovedList(Label.parse("a¿b"), aleph, -2);
		e.mergeUpperLabelValue(Label.parse("a¿b"), aleph, -2);
				
		assertEquals("{a¿b=C1_E∙C3_E->-2}", e.getUpperLabelSet().toString());
	}

	@SuppressWarnings("javadoc")
	@Test
	public final void testMergeUpperLabeledValues1() {
		this.alpha.clear();
		LabeledIntEdgePluggable e = edgeFactory.create();
		e.setName("e");
		LabeledIntMap map = AbstractLabeledIntMap.parse("{(-20, ab) }");
		assertEquals(map.toString(), "{(-20, ab) }");
		map = AbstractLabeledIntMap.parse("{(-20, ab) (-∞, ¿ab) }");
		assertEquals(map.toString(), "{(-20, ab) (-∞, ¿ab) }");
		map = AbstractLabeledIntMap.parse("{(-20, ab) (-∞, ¿ab) (-8, ¬b)  }");
		assertEquals(map.toString(), "{(-20, ab) (-∞, ¿ab) (-8, ¬b) }");
		map = AbstractLabeledIntMap.parse("{(-20, ab) (-∞, ¿ab) (-8, ¬b) (-17, b) }");
		assertEquals(map.toString(), "{(-20, ab) (-∞, ¿ab) (-8, ¬b) (-17, b) }");
		e.setLabeledValue(map);
		LabeledContingentIntTreeMap map1 = LabeledContingentIntTreeMap.parse("{(D, -∞, ¿ab) (D, -30, ab) (D, -∞, a¿b) (D, -4, ¿b) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -19, ¬ab) (F, -∞, ¿ab) }", this.alpha);
		e.setLabeledUpperCaseValue(map1);
		
		assertEquals(e.toString(), "❮e; normal; {(-20, ab) (-∞, ¿ab) (-8, ¬b) (-17, b) }; UL: {(D, -∞, ¿ab) (D, -30, ab) (D, -∞, a¿b) (D, -4, ¿b) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -19, ¬ab) (F, -∞, ¿ab) }; ❯");
		
		LabeledIntEdgePluggable e1 = edgeFactory.create(e);
		
		assertTrue(e.equalsLabeledValues(e1));
	}


	
	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdgePluggable#mergeLabeledValue(it.univr.di.labeledvalue.Label, int)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testMergeLabeledValueLabelInt() {
		LabeledIntEdgePluggable e = edgeFactory.create();
		e.setName("e");
		e.setConstraintType(ConstraintType.contingent);
		e.mergeLabeledValue(Label.emptyLabel, 1);

		assertEquals("❮e; contingent; {(1, ⊡) }; ❯", e.toString());
		e.mergeLabeledValue(Label.parse("a"), 1);
		assertEquals("❮e; contingent; {(1, ⊡) }; ❯", e.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdgePluggable#mergeLabeledValue(it.univr.di.labeledvalue.Label, int)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testMergeLabeledValueLabelInt1() {
		LabeledIntEdgePluggable e = edgeFactory.create();
		e.setName("e");
		
		e.setConstraintType(ConstraintType.contingent);
		e.mergeLabeledValue(Label.emptyLabel, 1);

		assertEquals("❮e; contingent; {(1, ⊡) }; ❯", e.toString());
		e.mergeLabeledValue(Label.parse("a"), 1);
		assertEquals("❮e; contingent; {(1, ⊡) }; ❯", e.toString());
	}

	
	
	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdgePluggable#putLabeledValue(it.univr.di.labeledvalue.Label, int)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testPutLabeledValue() {
		LabeledIntEdgePluggable e = edgeFactory.create();
		e.setName("e");
		e.setConstraintType(ConstraintType.contingent);
		e.mergeLabeledValue(Label.emptyLabel, 1);

		assertEquals("❮e; contingent; {(1, ⊡) }; ❯", e.toString());
		e.putLabeledValue(Label.parse("a"), 1);
		assertEquals("❮e; contingent; {(1, ⊡) }; ❯", e.toString());

	}


}
