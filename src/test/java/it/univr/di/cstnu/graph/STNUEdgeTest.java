/**
 * 
 */
package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;

/**
 * @author posenato
 */
public class STNUEdgeTest {

	/**
	 * 
	 */
	static EdgeSupplier<STNUEdgeInt> edgeFactory = new EdgeSupplier<>(STNUEdgeInt.class);

	/**
	 * Simple edge
	 */
	STNUEdge e;

	/**
	 * @throws java.lang.Exception  none
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
		this.e.setValue(1);
		assertFalse(this.e.isEmpty());
		this.e.clear();
		assertTrue(this.e.isEmpty());
		assertEquals("❮e; normal; ❯", this.e.toString());
		assertTrue(this.e.isEmpty());
		assertFalse(this.e.isContingentEdge());
		this.e.setConstraintType(ConstraintType.contingent);
		assertTrue(this.e.isContingentEdge());
		assertTrue(this.e.isSTNUEdge());
		assertFalse(this.e.isCSTNUEdge());
		assertFalse(this.e.isRequirementEdge());
		this.e.setConstraintType(ConstraintType.constraint);
		assertTrue(this.e.isRequirementEdge());
		assertFalse(this.e.isSTNEdge());
		assertFalse(this.e.isUpperCase());
		assertFalse(this.e.isLowerCase());
	}

	/**
	 */
	@Test
	public final void testLowerCase() {
		this.e.setConstraintType(ConstraintType.contingent);
		this.e.setLabeledValue(new ALetter("Caa"), 0, false);
		assertEquals(0, this.e.getLabeledValue());
		assertEquals("Caa", this.e.getNodeLabel().getFirst().name);
		assertEquals("LC(Caa):0", this.e.getLabeledValueFormatted());
		assertEquals("❮e; contingent; LC(Caa):0❯", this.e.toString());
		assertFalse(this.e.isUpperCase());
		assertTrue(this.e.isLowerCase());
	}

	/**
	 */
	@Test
	public final void testLowerCaseNonValid() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> this.e.setLabeledValue(new ALetter("Caa"), -1, false));
		assertEquals("A lower-case value cannot be negative. Details: Caa: -1.", ex.getMessage());
	}

	/**
	 */
	@Test
	public final void testUpperCase() {
		this.e.setConstraintType(ConstraintType.contingent);
		this.e.setValue(-1);
		this.e.setLabeledValue(new ALetter("Caa"), 0, true);
		assertEquals("❮e; contingent; -1; UC(Caa):0❯", this.e.toString());
		assertTrue(this.e.isUpperCase());
		assertFalse(this.e.isLowerCase());
	}

	/**
	 */
	@Test
	public final void testParserUpperCase() {
		this.e.setConstraintType(ConstraintType.contingent);
		// this.e.setValue(-1);
		String uc = "UC(Caa):-1234";
		this.e.setLabeledValue(uc);
		assertEquals("❮e; contingent; UC(Caa):-1234❯", this.e.toString());
		assertTrue(this.e.isUpperCase());
		assertFalse(this.e.isLowerCase());
	}

}
