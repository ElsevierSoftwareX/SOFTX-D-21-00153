/**
 * 
 */
package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertEquals;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntNodeSetMap;
import it.univr.di.labeledvalue.LabeledIntNodeSetTreeMap;

import java.util.Set;

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
		Set<String> XU = new ObjectArraySet<>();
		XU.add("X");
		XU.add("U");
		Set<String> XUY = new ObjectArraySet<>();
		XUY.add("X");
		XUY.add("U");
		XUY.add("Y");
		
		Set<String> YXW = new ObjectArraySet<>();
		YXW.add("Y");
		YXW.add("X");
		YXW.add("W");

		LabeledIntNodeSetMap map = new LabeledIntNodeSetTreeMap(true);
		map.put(Label.parse("¬p¿q"), -13, XU);
		map.put(Label.parse("¬p¬q"), -13, XU);
		map.put(Label.parse("¿p"), -15, null);
		map.put(Label.parse("¿p¬q"), -22, XUY);
		map.put(Label.emptyLabel, -10, null);
		map.put(Label.parse("¿p¿q"), Constants.INT_NEG_INFINITE, YXW);
		
		LabeledIntEdge e = new LabeledIntEdge("eUZ", LabeledIntEdge.Type.derived, true);
		e.setLabeledValue(map);

		LabeledIntEdge e1 = new LabeledIntEdge(e, true);
		assertEquals(e1.toString(), e.toString());

	}

	
}
