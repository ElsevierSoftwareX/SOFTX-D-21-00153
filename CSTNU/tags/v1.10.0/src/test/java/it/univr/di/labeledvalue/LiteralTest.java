/**
 * 
 */
package it.univr.di.labeledvalue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.univr.di.labeledvalue.Literal.State;

/**
 * @author posenato
 *
 */
public class LiteralTest {

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Literal#parse(java.lang.String)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testParse() {
		Literal a = Literal.parse("a");
		Literal notA = Literal.parse("¬a");
		Literal aOk = Literal.create('a');
		
		assertArrayEquals("Verifica del parser: ", new Literal[] {aOk, aOk.getComplement()}, new Literal[]{a, notA} );
	}


	/**
	 * Test method for {@link it.univr.di.labeledvalue.Literal#compareTo(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testCompareTo() {
		Literal a = Literal.parse("a");
		Literal notA = Literal.parse("¬a");
		Literal unkA = Literal.create('a', State.unknown);
		
		assertTrue(a.compareTo(notA)>0);
		assertTrue(a.compareTo(a)==0);
		assertTrue(notA.compareTo(a)<0);
//		assertEquals(1, unkA.compareTo(a));
//		assertEquals(2, unkA.compareTo(notA));
//		assertEquals(0, unkA.compareTo(unkA));
		assertTrue(unkA.compareTo(a)>0);
		assertTrue(unkA.compareTo(notA)>0);
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Literal#equals(java.lang.Object)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testEqualsObject() {
		Literal a = Literal.parse("a");
		Literal notA = Literal.parse("¬a");

		assertTrue(a.equals(a));
		assertFalse(notA.equals(a));
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Literal#getComplement()}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testGetComplement() {
		Literal a = Literal.parse("a");
		Literal notA = a.getComplement();

		Literal unA = Literal.create('a', State.unknown);
		
		assertFalse(notA.equals(a));
		assertTrue(unA.getComplement()==null);
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Literal#toString()}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testToString() {
		Literal a = Literal.parse("a");
		Literal notA = Literal.parse("¬a");
		Literal unkA = Literal.create('a', State.unknown);

		assertEquals("a", a.toString());
		assertEquals("¬a", notA.toString());
		assertEquals("¿a", unkA.toString());
		assertEquals(1, State.negated.ordinal());
		assertEquals(2, State.straight.ordinal());
		assertEquals(3, State.unknown.ordinal());
		
	}

}
