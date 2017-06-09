/**
 * 
 */
package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.cstnu.graph.LabeledIntEdgeFactory;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntEdge.ConstraintType;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntNodeSetTreeMap;

/**
 * @author posenato
 *
 */
public class LabeledIntEdge_NodeSetTest {

	@SuppressWarnings("javadoc")
	static LabeledIntEdgeFactory<LabeledIntNodeSetTreeMap> edgeWithNodeSetFactory = new LabeledIntEdgeFactory<>(LabeledIntNodeSetTreeMap.class);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdge_NodeSet#toString()}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testToString() {
		LabeledIntEdgePluggable e = edgeWithNodeSetFactory.create("e");
		e.setConstraintType(ConstraintType.contingent);
		e.putLabeledValue(Label.emptyLabel, 1);

		assertEquals("Valore di e:" + e, "❮e; contingent; {(⊡, 1) }; ❯", e.toString());
		e.mergeUpperLabelValue(Label.emptyLabel, "A", 0);
		assertEquals("❮e; contingent; {(⊡, 1) }; UL: {(0, A, ⊡) }; ❯", e.toString());

	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdge_NodeSet#getAllUpperCaseAndOrdinaryLabeledValuesSet()}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testGetAllUpperCaseAndOrdinaryLabeledValuesSet() {
		LabeledIntEdgePluggable e = edgeWithNodeSetFactory.create("e");
		e.setConstraintType(ConstraintType.contingent);
		e.putLabeledValue(Label.emptyLabel, 1);

		e.mergeUpperLabelValue(Label.emptyLabel, "A", 0);
		e.mergeLowerLabelValue(Label.emptyLabel, "B", -1);

		assertEquals("{⊡=A->0, ⊡->◇->1}", e.getAllUpperCaseAndOrdinaryLabeledValuesSet().toString());

	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdge_NodeSet#getUpperLabelValue(it.univr.di.labeledvalue.Label, java.lang.String)}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testGetUpperLabelValueLabelString() {
		LabeledIntEdgePluggable e = edgeWithNodeSetFactory.create("e");
		e.setConstraintType(ConstraintType.contingent);
		e.putLabeledValue(Label.emptyLabel, 1);

		e.mergeUpperLabelValue(Label.emptyLabel, "A", 0);
		e.mergeUpperLabelValue(Label.emptyLabel, "B", -1);

		assertEquals(0, e.getUpperLabelValue(Label.emptyLabel, "A"));
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdge_NodeSet#mergeLabeledValue(it.univr.di.labeledvalue.Label, int)}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testMergeLabeledValueLabelInt() {
		LabeledIntEdgePluggable e = edgeWithNodeSetFactory.create("e");
		e.setConstraintType(ConstraintType.contingent);
		e.putLabeledValue(Label.emptyLabel, 1);

		assertEquals("❮e; contingent; {(⊡, 1) }; ❯", e.toString());
		e.mergeLabeledValue(Label.parse("a"), 1);
		assertEquals("❮e; contingent; {(⊡, 1) }; ❯", e.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdge_NodeSet#mergeLabeledValue(it.univr.di.labeledvalue.Label, int)}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testMergeLabeledValueLabelInt1() {
		LabeledIntEdgePluggable e = edgeWithNodeSetFactory.create("e");
		e.setConstraintType(ConstraintType.contingent);
		e.putLabeledValue(Label.emptyLabel, 1);

		assertEquals("❮e; contingent; {(⊡, 1) }; ❯", e.toString());
		ObjectSet<String> s = new ObjectAVLTreeSet<>();
		s.add("B");
		s.add("A");
		e.mergeLabeledValue(Label.parse("a"), 1, s);
		assertEquals("❮e; contingent; {(⊡, 1) (a, 1, {A, B}) }; ❯", e.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdge_NodeSet#putLabeledValue(it.univr.di.labeledvalue.Label, int)}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testPutLabeledValue() {
		LabeledIntEdgePluggable e = edgeWithNodeSetFactory.create("e");
		e.setConstraintType(ConstraintType.contingent);
		e.putLabeledValue(Label.emptyLabel, 1);

		assertEquals("❮e; contingent; {(⊡, 1) }; ❯", e.toString());
		e.putLabeledValue(Label.parse("a"), 1);
		assertNotEquals("❮e; contingent; {(⊡, 1) (a, 1) }; ❯", e.toString());

	}

	@SuppressWarnings({ "static-method", "javadoc", "unchecked" })
	@Test
	public final void testCopyEdgeWithNodeSet() {
		SortedSet<String> XU = new ObjectAVLTreeSet<>();
		XU.add("X");
		XU.add("U");
		SortedSet<String> XUY = new ObjectAVLTreeSet<>();
		XUY.add("X");
		XUY.add("U");
		XUY.add("Y");

		SortedSet<String> YXW = new ObjectAVLTreeSet<>();
		YXW.add("Y");
		YXW.add("X");
		YXW.add("W");

		LabeledIntEdgePluggable e = edgeWithNodeSetFactory.create("eUZ");
		e.setConstraintType(ConstraintType.derived);

		e.mergeLabeledValue(Label.parse("¬p¿q"), -13, (ObjectSet<String>) XU);
		e.mergeLabeledValue(Label.parse("¬p¬q"), -13, (ObjectSet<String>) XU);
		e.mergeLabeledValue(Label.parse("¿p"), -15, null);
		e.mergeLabeledValue(Label.parse("¿p¬q"), -22, (ObjectSet<String>) XUY);
		e.mergeLabeledValue(Label.emptyLabel, -10, null);
		e.mergeLabeledValue(Label.parse("¿p¿q"), Constants.INT_NEG_INFINITE, (ObjectSet<String>) YXW);

		LabeledIntEdgePluggable e1 = new LabeledIntEdgePluggable(e);
		assertEquals(e1.toString(), e.toString());

	}

	@SuppressWarnings({ "static-method", "javadoc", "unchecked" })
	@Test
	public final void testEqualEdgeWithNodeSet() {
		SortedSet<String> XU = new ObjectAVLTreeSet<>();
		XU.add("X");
		XU.add("U");
		SortedSet<String> XUY = new ObjectAVLTreeSet<>();
		XUY.add("X");
		XUY.add("U");
		XUY.add("Y");

		SortedSet<String> YXW = new ObjectAVLTreeSet<>();
		YXW.add("Y");
		YXW.add("X");
		YXW.add("W");

		LabeledIntEdgePluggable e = edgeWithNodeSetFactory.create("eUZ");
		e.setConstraintType(ConstraintType.derived);
		e.mergeLabeledValue(Label.parse("¬p¿q"), -13, (ObjectSet<String>) XU);
		e.mergeLabeledValue(Label.parse("¬p¬q"), -13, (ObjectSet<String>) XU);
		e.mergeLabeledValue(Label.parse("¿p"), -15, (ObjectSet<String>) null);
		e.mergeLabeledValue(Label.parse("¿p¬q"), -22, (ObjectSet<String>) XUY);
		e.mergeLabeledValue(Label.emptyLabel, -10, null);
		e.mergeLabeledValue(Label.parse("¿p¿q"), Constants.INT_NEG_INFINITE, (ObjectSet<String>) YXW);

		LabeledIntEdgePluggable e1 = new LabeledIntEdgePluggable(e);
		assertNotEquals(e1, e);//equals is necessarily based on address because internal structure of graphs requires an equals that does not change for the internal 
		//edge changes

	}

}
