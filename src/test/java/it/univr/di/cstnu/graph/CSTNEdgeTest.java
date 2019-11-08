/**
 * 
 */
package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * @author posenato
 */
public class CSTNEdgeTest {

	static EdgeSupplier<CSTNEdgePluggable> edgeFactory = new EdgeSupplier<>(CSTNEdgePluggable.class, LabeledIntTreeMap.class);
	static Class<? extends LabeledIntMap> labeledValueClass = edgeFactory.getLabeledIntValueMapImplClass();

	/**
	 * Simple edge
	 */
	CSTNEdge e;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.e = edgeFactory.get("e");
	}

	/**
	 * Test method for 2 methods.
	 */
	@Test
	public final void testIsEmptyClear() {
		this.e.mergeLabeledValue(Label.emptyLabel, 1);
		assertFalse(this.e.isEmpty());
		this.e.clear();
		assertEquals("❮e; normal; ❯", this.e.toString());
		assertTrue(this.e.isEmpty());
		assertFalse(this.e.isContingentEdge());
		this.e.setConstraintType(ConstraintType.contingent);
		assertTrue(this.e.isContingentEdge());
		assertTrue(this.e.isCSTNEdge());
		assertFalse(this.e.isCSTNUEdge());
		assertFalse(this.e.isRequirementEdge());
		this.e.setConstraintType(ConstraintType.constraint);
		assertTrue(this.e.isRequirementEdge());
		assertFalse(this.e.isSTNEdge());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.CSTNEdge#mergeLabeledValue(it.univr.di.labeledvalue.Label, int)}.
	 */
	@Test
	public final void testMergeLabeledValueLabelInt() {
		this.e.setName("e");
		this.e.setConstraintType(ConstraintType.normal);
		this.e.mergeLabeledValue(Label.emptyLabel, 1);

		assertEquals("❮e; normal; {(1, ⊡) }; ❯", this.e.toString());
		this.e.mergeLabeledValue(Label.parse("a"), 1);
		assertEquals("❮e; normal; {(1, ⊡) }; ❯", this.e.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.CSTNEdge#mergeLabeledValue(it.univr.di.labeledvalue.Label, int)}.
	 */
	@Test
	public final void testMergeLabeledValueLabelInt1() {
		this.e.setName("e");

		this.e.setConstraintType(ConstraintType.normal);
		this.e.mergeLabeledValue(Label.emptyLabel, 1);

		assertEquals("❮e; normal; {(1, ⊡) }; ❯", this.e.toString());
		this.e.mergeLabeledValue(Label.parse("a"), 1);
		assertEquals("❮e; normal; {(1, ⊡) }; ❯", this.e.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.CSTNEdge#putLabeledValue(it.univr.di.labeledvalue.Label, int)}.
	 */
	@Test
	public final void testPutLabeledValue() {
		this.e.setName("e");
		this.e.setConstraintType(ConstraintType.contingent);
		this.e.mergeLabeledValue(Label.emptyLabel, 1);

		assertEquals("❮e; contingent; {(1, ⊡) }; ❯", this.e.toString());
		this.e.putLabeledValue(Label.parse("a"), 1);
		assertEquals("❮e; contingent; {(1, ⊡) }; ❯", this.e.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.CSTNEdge#toString()}.
	 */
	@Test
	public final void testToString() {
		this.e.setName("e");
		this.e.setConstraintType(ConstraintType.constraint);
		this.e.mergeLabeledValue(Label.emptyLabel, 1);
		assertEquals("❮e; constraint; {(1, ⊡) }; ❯", this.e.toString());
	}
}
