/**
 * 
 */
package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;

import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;

/**
 * @author posenato
 *
 */
public class LabeledIntEdgeTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdge#toString()}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testToString() {
		LabeledIntEdge e = new LabeledIntEdge("e", LabeledIntEdge.Type.contingent, Label.emptyLabel, 1, true);

		assertEquals("❮e; contingent; {(⊡, 1) }; ❯", e.toString());
		e.mergeUpperLabelValue(Label.emptyLabel, "A", 0);
		assertEquals("❮e; contingent; {(⊡, 1) }; UL: {(⊡, A, 0) }; ❯", e.toString());

	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdge#getAllUpperCaseAndOrdinaryLabeledValuesSet()}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testGetAllUpperCaseAndOrdinaryLabeledValuesSet() {
		LabeledIntEdge e = new LabeledIntEdge("e", LabeledIntEdge.Type.contingent, Label.emptyLabel, 1, true);
		e.mergeUpperLabelValue(Label.emptyLabel, "A", 0);
		e.mergeLowerLabelValue(Label.emptyLabel, "B", -1);
		
		assertEquals("{⊡=A->0, ⊡->◇->1}", e.getAllUpperCaseAndOrdinaryLabeledValuesSet().toString());

	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdge#getUpperLabelValue(it.univr.di.labeledvalue.Label, java.lang.String)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testGetUpperLabelValueLabelString() {
		LabeledIntEdge e = new LabeledIntEdge("e", LabeledIntEdge.Type.contingent, Label.emptyLabel, 1, true);
		e.mergeUpperLabelValue(Label.emptyLabel, "A", 0);
		e.mergeUpperLabelValue(Label.emptyLabel, "B", -1);
		
		assertEquals(0, e.getUpperLabelValue(Label.emptyLabel, "A"));
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdge#mergeLabeledValue(it.univr.di.labeledvalue.Label, int)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testMergeLabeledValueLabelInt() {
		LabeledIntEdge e = new LabeledIntEdge("e", LabeledIntEdge.Type.contingent, Label.emptyLabel, 1, true);

		assertEquals("❮e; contingent; {(⊡, 1) }; ❯", e.toString());
		e.mergeLabeledValue(Label.parse("a"), 1);
		assertEquals("❮e; contingent; {(⊡, 1) }; ❯", e.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdge#putLabeledValue(it.univr.di.labeledvalue.Label, int)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testPutLabeledValue() {
		LabeledIntEdge e = new LabeledIntEdge("e", LabeledIntEdge.Type.contingent, Label.emptyLabel, 1, true);

		assertEquals("❮e; contingent; {(⊡, 1) }; ❯", e.toString());
		e.putLabeledValue(Label.parse("a"), 1);
		assertEquals("❮e; contingent; {(⊡, 1) (a, 1) }; ❯", e.toString());

	}


	@SuppressWarnings({ "static-method", "javadoc" })
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

		LabeledIntEdge e = new LabeledIntEdge("eUZ", LabeledIntEdge.Type.derived, true);
		e.mergeLabeledValue(Label.parse("¬p¿q"), -13, XU);
		e.mergeLabeledValue(Label.parse("¬p¬q"), -13, XU);
		e.mergeLabeledValue(Label.parse("¿p"), -15, null);
		e.mergeLabeledValue(Label.parse("¿p¬q"), -22, XUY);
		e.mergeLabeledValue(Label.emptyLabel, -10, null);
		e.mergeLabeledValue(Label.parse("¿p¿q"), Constants.INT_NEG_INFINITE, YXW);
		

		LabeledIntEdge e1 = new LabeledIntEdge(e, true);
		assertEquals(e1.toString(), e.toString());

	}


	@SuppressWarnings({ "static-method", "javadoc" })
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

		LabeledIntEdge e = new LabeledIntEdge("eUZ", LabeledIntEdge.Type.derived, true);
		e.mergeLabeledValue(Label.parse("¬p¿q"), -13, XU);
		e.mergeLabeledValue(Label.parse("¬p¬q"), -13, XU);
		e.mergeLabeledValue(Label.parse("¿p"), -15, null);
		e.mergeLabeledValue(Label.parse("¿p¬q"), -22, XUY);
		e.mergeLabeledValue(Label.emptyLabel, -10, null);
		e.mergeLabeledValue(Label.parse("¿p¿q"), Constants.INT_NEG_INFINITE, YXW);
		
		LabeledIntEdge e1 = new LabeledIntEdge(e, true);
		assertNotEquals(e1, e);//equals is necessarily based on address because internal structure of graphs requires an equals that does not change for the internal 
		//edge changes

	}

}
