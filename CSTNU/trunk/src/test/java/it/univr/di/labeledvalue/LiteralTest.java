/**
 * 
 */
package it.univr.di.labeledvalue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
		Literal unkA = Literal.parse("¿a");
		
		assertArrayEquals("Verifica del parser: ", new String[] { "a", "¬a", "¿a" }, new String[] { a.toString(), notA.toString(), unkA.toString() });
	}


	/**
	 * Test method for {@link it.univr.di.labeledvalue.Literal#compareTo(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testCompareTo() {
		Literal a = Literal.parse("a");
		Literal notA = Literal.parse("¬a");
		Literal unkA = Literal.parse("¿a");
		
		assertTrue(a.compareTo(notA) < 0);
		assertTrue(a.compareTo(a)==0);
		assertTrue(notA.compareTo(a) > 0);
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

		Literal unA = Literal.valueOf('a', Literal.UNKNONW);
		
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
		Literal unkA = Literal.valueOf('a', Literal.UNKNONW);

		assertEquals("a", a.toString());
		assertEquals("¬a", notA.toString());
		assertEquals("¿a", unkA.toString());
		// assertEquals(1, State.negated.ordinal());
		// assertEquals(2, State.straight.ordinal());
		// assertEquals(3, Literal.UNKNONW);
		
	}

	/**
	 * Proposes only some execution time estimates about some class methods.
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		// final int nTest = 1000;
		// final double msNorm = 1.0 / (1000000.0 * nTest);

		int j = 0;
		for (char i = 'a'; i <= 'z'; i++)
			System.out.println((j++) + ": '" + (i) + "', ");
		for (char i = 'A'; i <= 'Z'; i++)
			System.out.println((j++) + ": '" + (i) + "', ");
		for (char i = 'α'; i <= 'μ'; i++)
			System.out.println((j++) + ": '" + (i) + "', ");

		final Literal A = Literal.valueOf('A'), Z = Literal.valueOf('Z'), B = Literal.valueOf('B', Literal.STRAIGHT), b = Literal.valueOf('b'),
				a = Literal.valueOf('a'), z = Literal.valueOf('z');
		final Literal α = Literal.valueOf('α');
		final Literal μ = Literal.valueOf('μ');

		System.out.println("A: " + A);
		System.out.println("Z: " + Z);
		System.out.println("a: " + a);
		System.out.println("z: " + z);
		System.out.println("α: " + α);
		System.out.println("μ; " + μ);

		System.out.println("Hashcode A: " + A.hashCode());
		System.out.println("Hashcode B: " + B.hashCode());
		System.out.println("Hashcode Z: " + Z.hashCode());
		System.out.println("Hashcode a: " + a.hashCode());
		System.out.println("Hashcode b: " + b.hashCode());
		System.out.println("Hashcode z: " + z.hashCode());
		System.out.println("Hashcode α: " + α.hashCode());
		System.out.println("Hashcode μ; " + μ.hashCode());

		System.out.println("Index of 'A': " + Literal.index('A'));
		System.out.println("Index of 'B': " + Literal.index('B'));
		System.out.println("Index of 'Z': " + Literal.index('Z'));
		System.out.println("Index of 'a': " + Literal.index('a'));
		System.out.println("Index of 'b': " + Literal.index('b'));
		System.out.println("Index of 'z': " + Literal.index('z'));
		System.out.println("Index of 'α': " + Literal.index('α'));
		System.out.println("Index of 'μ': " + Literal.index('μ'));

		System.out.println("CharValue of 0: " + Literal.charValue(0));
		System.out.println("CharValue of 1: " + Literal.charValue(1));
		System.out.println("CharValue of 25: " + Literal.charValue(25));
		System.out.println("CharValue of 26: " + Literal.charValue(26));
		System.out.println("CharValue of 27: " + Literal.charValue(27));
		System.out.println("CharValue of 51: " + Literal.charValue(51));
		System.out.println("CharValue of 52: " + Literal.charValue(52));
		System.out.println("CharValue of 63: " + Literal.charValue(63));
	}

}
