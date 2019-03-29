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
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * @author posenato
 */
public class LabeledIntEdgeSimpleTest {

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
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdgePluggable#toString()}.
	 */
	@Test
	public final void testToString() {
		LabeledIntEdgePluggable e = edgeFactory.get();
		e.setName("e");
		e.setConstraintType(ConstraintType.contingent);
		e.mergeLabeledValue(Label.emptyLabel, 1);
		assertEquals("❮e; contingent; {(1, ⊡) }; ❯", e.toString());
		e.mergeUpperCaseValue(Label.emptyLabel, new ALabel("A", this.alpha), 0);
		assertEquals("❮e; contingent; {(1, ⊡) }; UL: {(A, 0, ⊡) }; ❯", e.toString());

	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdgePluggable#getAllUpperCaseAndOrdinaryLabeledValuesSet()}.
	 */
	@SuppressWarnings({ "javadoc" })
	@Test()
	public final void testGetAllUpperCaseAndOrdinaryLabeledValuesSet() {
		LabeledIntEdgePluggable e = edgeFactory.get();
		e.setName("e");
		e.mergeLabeledValue(Label.emptyLabel, 1);
		e.mergeUpperCaseValue(Label.emptyLabel, new ALabel("A", this.alpha), 0);
		e.setLowerCaseValue(Label.emptyLabel, new ALabel("B", this.alpha), -1);
		// System.out.println(e.getAllUpperCaseAndLabeledValuesMaps().toString());
		assertEquals("{(◇, 1, ⊡) (A, 0, ⊡) }", e.getAllUpperCaseAndLabeledValuesMaps().toString());
	}

	/**
	 */
	@Test
	public final void testGetUpperLabelValueLabelString() {
		LabeledIntEdgePluggable e = edgeFactory.get();
		e.setName("e");
		e.mergeLabeledValue(Label.emptyLabel, 1);
		e.mergeUpperCaseValue(Label.emptyLabel, new ALabel("A", this.alpha), 0);
		e.mergeUpperCaseValue(Label.emptyLabel, new ALabel("B", this.alpha), -1);

		assertEquals(0, e.getUpperCaseValue(Label.emptyLabel, new ALabel("A", this.alpha)));
	}

	/**
	 */
	@Test
	public final void testMergeUpperLabeledValues() {
		LabeledIntEdgePluggable e = edgeFactory.get();
		e.setName("e");
		ALabel aleph = new ALabel("C1_E", this.alpha);
		aleph.conjunct(new ALetter("C3_E"));
		e.mergeUpperCaseValue(Label.parse("a¿b"), aleph, -2);
		aleph.conjunct(new ALetter("C2_E"));
		e.putUpperCaseValueToRemovedList(Label.parse("a¿b"), aleph, -2);
		e.mergeUpperCaseValue(Label.parse("a¿b"), aleph, -2);

		assertEquals("{(C1_E∙C3_E, -2, a¿b) }", e.getUpperCaseValueMap().toString());
	}

	@SuppressWarnings("javadoc")
	@Test
	public final void testMergeUpperLabeledValues1() {
		this.alpha.clear();
		LabeledIntEdgePluggable e = edgeFactory.get();
		e.setName("e");
		LabeledIntMap map = AbstractLabeledIntMap.parse("{(-20, ab) }");
		assertEquals(map.toString(), "{(-20, ab) }");

		map = AbstractLabeledIntMap.parse("{(-20, ab) (-∞, ¿ab) }");
		assertEquals(map.toString(), "{(-20, ab) (-∞, ¿ab) }");

		map = AbstractLabeledIntMap.parse("{(-20, ab) (-∞, ¿ab) (-8, ¬b)  }");
		assertEquals("{(-8, ¬b) (-20, ab) (-∞, ¿ab) }", map.toString());

		map = AbstractLabeledIntMap.parse("{(-20, ab) (-∞, ¿ab) (-8, ¬b) (-17, b) }");
		// assertEquals("{(-17, b) (-8, ¬b) (-20, ab) (-∞, ¿ab) }", map.toString());
		assertEquals("{(-8, ⊡) (-17, b) (-20, ab) (-∞, ¿ab) }", map.toString());

		e.setLabeledValueMap(map);
		LabeledALabelIntTreeMap map1 = LabeledALabelIntTreeMap
				.parse("{(D, -∞, ¿ab) (D, -30, ab) (D, -∞, a¿b) (D, -4, ¿b) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -19, ¬ab) (F, -∞, ¿ab) }", this.alpha);
		e.setUpperCaseValueMap(map1);

		// assertEquals(
		// "❮e; normal; {(-17, b) (-8, ¬b) (-20, ab) (-∞, ¿ab) }; UL: {(D, -4, ¿b) (D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b)
		// (F, -19, ¬ab) (F, -∞, ¿ab) }; ❯",
		// e.toString());
		assertEquals(
				"❮e; normal; {(-8, ⊡) (-17, b) (-20, ab) (-∞, ¿ab) }; UL: {(D, -4, ¿b) (D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -19, ¬ab) (F, -∞, ¿ab) }; ❯",
				e.toString());

		LabeledIntEdgePluggable e1 = edgeFactory.get(e);

		// System.out.println(e);
		// System.out.println(e1);

		assertTrue(e.equalsAllLabeledValues(e1));
	}

	@SuppressWarnings("javadoc")
	@Test
	public final void testMergeUpperLabeledValues2() {
		this.alpha.clear();

		LabeledIntEdgePluggable e = edgeFactory.get();
		e.setName("e");
		e.mergeLabeledValue(Label.parse("a"), -20);
		e.mergeLabeledValue(Label.parse("¬a"), -20);

		e.mergeUpperCaseValue(Label.parse("ab"), ALabel.parse("D", this.alpha), -30);
		assertEquals("❮e; normal; {(-20, ⊡) }; UL: {(D, -30, ab) }; ❯", e.toString());

		e.mergeUpperCaseValue(Label.parse("a¿b"), ALabel.parse("D", this.alpha), Constants.INT_NEG_INFINITE);
		assertEquals("❮e; normal; {(-20, ⊡) }; UL: {(D, -30, ab) (D, -∞, a¿b) }; ❯", e.toString());

		e.mergeUpperCaseValue(Label.parse("¿ab"), ALabel.parse("D", this.alpha), Constants.INT_NEG_INFINITE);
		assertEquals("❮e; normal; {(-20, ⊡) }; UL: {(D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) }; ❯", e.toString());

		e.mergeUpperCaseValue(Label.parse("¿b"), ALabel.parse("I", this.alpha), -9);
		assertEquals("❮e; normal; {(-20, ⊡) }; UL: {(D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) }; ❯", e.toString());

		e.mergeUpperCaseValue(Label.parse("¬b"), ALabel.parse("I", this.alpha), -30);
		assertEquals("❮e; normal; {(-20, ⊡) }; UL: {(D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) (I, -30, ¬b) }; ❯", e.toString());

		e.mergeUpperCaseValue(Label.parse("¬b"), ALabel.parse("I" + ALabel.ALABEL_SEPARATORstring + "D", this.alpha), -60);
		assertEquals(
				"❮e; normal; {(-20, ⊡) }; UL: {(D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) (I, -30, ¬b) (D" + ALabel.ALABEL_SEPARATORstring + "I, -60, ¬b) }; ❯",
				e.toString());

		e.mergeUpperCaseValue(Label.parse("¬b"), ALabel.parse("I" + ALabel.ALABEL_SEPARATORstring + "D" + ALabel.ALABEL_SEPARATORstring + "F", this.alpha),
				-60);
		assertEquals(
				"❮e; normal; {(-20, ⊡) }; UL: {(D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) (I, -30, ¬b) (D" + ALabel.ALABEL_SEPARATORstring + "I, -60, ¬b) }; ❯",
				e.toString());

		e.mergeUpperCaseValue(Label.parse("¬ba"), ALabel.parse("I" + ALabel.ALABEL_SEPARATORstring + "D" + ALabel.ALABEL_SEPARATORstring + "F", this.alpha),
				-60);
		assertEquals(
				"❮e; normal; {(-20, ⊡) }; UL: {(D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) (I, -30, ¬b) (D" + ALabel.ALABEL_SEPARATORstring + "I, -60, ¬b) }; ❯",
				e.toString());

		e.mergeUpperCaseValue(Label.parse("b"), ALabel.parse("I", this.alpha), -60);
		// assertEquals(
		// "❮e; normal; {(-20, ⊡) }; UL: {(D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) (I, -60, b) (I, -30, ¬b) (D" + ALabel.ALABEL_SEPARATORstring
		// + "I, -60, ¬b) }; ❯",
		// e.toString());
		assertEquals(
				"❮e; normal; {(-20, ⊡) }; UL: {(D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) (I, -30, ⊡) (I, -60, b) (D" + ALabel.ALABEL_SEPARATORstring
						+ "I, -60, ¬b) }; ❯",
				e.toString());

		e.mergeUpperCaseValue(Label.parse("¬b"), ALabel.parse("I", this.alpha), -60);
		assertEquals(
				"❮e; normal; {(-20, ⊡) }; UL: {(D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) (I, -60, ⊡) }; ❯",
				e.toString());

	}

	@SuppressWarnings("javadoc")
	@Test
	public final void testSimlificationUpperLabeledValues() {
		this.alpha.clear();

		LabeledALabelIntTreeMap mapUC = LabeledALabelIntTreeMap
				.parse("{(D, -∞, ¿ab) (D, -30, ab) (D, -∞, a¿b) (D, -4, ¿b) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -19, ¬ab) (F, -∞, ¿ab) }", this.alpha);
		LabeledIntMap map = AbstractLabeledIntMap.parse("{(0, a) (0, ¬a) }");

		LabeledIntEdgePluggable e = edgeFactory.get();
		e.setName("e");
		e.setLabeledValueMap(map);
		e.setUpperCaseValueMap(mapUC);

		assertEquals(
				"❮e; normal; {(0, ⊡) }; UL: {(D, -4, ¿b) (D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -19, ¬ab) (F, -∞, ¿ab) }; ❯",
				e.toString());

		e.mergeLabeledValue(Label.parse("¬ab"), -20);
		assertEquals(
				"❮e; normal; {(0, ⊡) (-20, ¬ab) }; UL: {(D, -4, ¿b) (D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -∞, ¿ab) }; ❯",
				e.toString());

		e.mergeLabeledValue(Label.parse("ab"), -20);

		assertEquals(
				"❮e; normal; {(0, ⊡) (-20, b) }; UL: {(D, -4, ¿b) (D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -∞, ¿ab) }; ❯",
				e.toString());

		e.mergeLabeledValue(Label.parse("¬b"), -20);

		assertEquals(
				"❮e; normal; {(-20, ⊡) }; UL: {(D, -30, ab) (D, -∞, a¿b) (D, -∞, ¿ab) (I, -∞, ¿a¿b) (F, -∞, ¿ab) }; ❯",
				e.toString());

		e.mergeLabeledValue(Label.parse("a"), -30);

		assertEquals(
				"❮e; normal; {(-20, ⊡) (-30, a) }; UL: {(D, -∞, a¿b) (D, -∞, ¿ab) (I, -∞, ¿a¿b) (F, -∞, ¿ab) }; ❯",
				e.toString());

	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.LabeledIntEdgePluggable#mergeLabeledValue(it.univr.di.labeledvalue.Label, int)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testMergeLabeledValueLabelInt() {
		LabeledIntEdgePluggable e = edgeFactory.get();
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
		LabeledIntEdgePluggable e = edgeFactory.get();
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
		LabeledIntEdgePluggable e = edgeFactory.get();
		e.setName("e");
		e.setConstraintType(ConstraintType.contingent);
		e.mergeLabeledValue(Label.emptyLabel, 1);

		assertEquals("❮e; contingent; {(1, ⊡) }; ❯", e.toString());
		e.putLabeledValue(Label.parse("a"), 1);
		assertEquals("❮e; contingent; {(1, ⊡) }; ❯", e.toString());

	}

}
