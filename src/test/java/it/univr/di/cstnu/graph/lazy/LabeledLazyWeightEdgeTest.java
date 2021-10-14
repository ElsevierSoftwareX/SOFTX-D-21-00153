/**
 * 
 */
package it.univr.di.cstnu.graph.lazy;

import static org.junit.Assert.assertEquals;

import org.apache.commons.math3.fraction.Fraction;
import org.junit.Before;
import org.junit.Test;

import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.labeledvalue.ALabelAlphabet;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.lazy.LazyNumber;
import it.univr.di.labeledvalue.lazy.LazyPiece;

/**
 * @author posenato
 */
public class LabeledLazyWeightEdgeTest {

	/**
	 * 
	 */
	ALabelAlphabet alpha;
	/**
	 * 
	 */
	LabeledLazyWeightEdge e;

	/**
	 * @throws java.lang.Exception  none
	 */
	@Before
	public void setUp() throws Exception {
		this.alpha = new ALabelAlphabet();
		this.e = new LabeledLazyWeightEdge();
		this.e.setName("e");
		this.e.setConstraintType(ConstraintType.internal);
	}

	/**
	 */
	@Test
	public final void testToString() {
		this.e.mergeLabeledValue(Label.emptyLabel, LazyNumber.get(1));
		assertEquals("❮e; internal; {(1.0, ⊡) }; ❯", this.e.toString());
	}

	/**
	 */
	@Test
	public final void testMergeLabeledValueLabelInt() {
		this.e.mergeLabeledValue(Label.emptyLabel, LazyNumber.get(1));

		assertEquals("❮e; internal; {(1.0, ⊡) }; ❯", this.e.toString());
		this.e.mergeLabeledValue(Label.parse("a"), LazyNumber.get(1));
		assertEquals("❮e; internal; {(1.0, ⊡) }; ❯", this.e.toString());
	}

	/**
	 */
	@Test
	public final void testPutLabeledValue() {
		this.e.mergeLabeledValue(Label.emptyLabel, new LazyPiece(Fraction.ZERO, 1, -1, false));

		assertEquals("❮e; internal; {(-1.0[Piece 1.0 * ∂ + -1.0], ⊡) }; ❯", this.e.toString());
		this.e.putLabeledValue(Label.parse("a"), LazyNumber.get(1));
		assertEquals("❮e; internal; {(-1.0[Piece 1.0 * ∂ + -1.0], ⊡) }; ❯", this.e.toString());
	}
}
