/**
 * 
 */
package it.univr.di.labeledvalue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import it.univr.di.labeledvalue.Literal.State;

import org.junit.Before;
import org.junit.Test;

/**
 * @author posenato
 *
 */
public class LabelTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#allComponentsOfBaseGenerator(Literal[])}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testAllComponentsOfBaseGenerator() {
		Label ab = Label.parse("a¬b");
		Literal uC = new Literal('c', State.unknown);
		Label[] abV = Label.allComponentsOfBaseGenerator(ab.toArray());
		assertArrayEquals(new Label[] { Label.parse("a¬b"), Label.parse("¬a¬b"), Label.parse("ab"), Label.parse("¬ab") }, abV);

		ab.conjunct(uC);
		abV = Label.allComponentsOfBaseGenerator(ab.toArray());
		assertArrayEquals(new Label[] { Label.parse("a¬b"), Label.parse("¬a¬b"), Label.parse("ab"), Label.parse("¬ab") }, abV);

		ab.conjunctExtended(uC);
		abV = Label.allComponentsOfBaseGenerator(ab.toArray());
		assertNull(abV);
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#compareTo(it.univr.di.labeledvalue.Label)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testCompareTo() {
		Label ab = Label.parse("a¬b");
		Literal uC = new Literal('c', State.unknown);
		Label ab1 = new Label(ab);
		assertTrue(ab.compareTo(ab1) == 0);

		ab.conjunctExtended(uC.getNegated());
		ab1.conjunctExtended(uC);
		assertTrue(ab.compareTo(ab1) < 0);

		ab1.remove(uC);
		ab1.conjunctExtended(uC.getStraight());
		assertTrue(ab.compareTo(ab1) < 0);

		ab.remove(uC.getNegated());
		ab.conjunctExtended(uC);
		assertTrue(ab.compareTo(ab1) > 0);
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#conjunct(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testConjunct() {
		Label ab = Label.parse("a¬b");
		ab.conjunct(new Literal('c'));
		assertEquals(Label.parse("a¬bc"), ab);

		ab.conjunct(new Literal('d', State.unknown));
		assertEquals(Label.parse("a¬bc"), ab);
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#conjunctExtended(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testConjunctExtended() {
		Label ab = Label.parse("a¬b");
		ab.conjunctExtended(new Literal('b', State.unknown));
		// System.out.println(ab);
		assertTrue(ab.getLiteralWithSameName(new Literal('b')).isUnknown());

		ab.conjunctExtended(new Literal('d', State.unknown));
		assertTrue(ab.getLiteralWithSameName(new Literal('d')).isUnknown());
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#conjunction(it.univr.di.labeledvalue.Label)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testConjunction() {
		Label ab = Label.parse("ab");
		Label aNb = Label.parse("a¬b");
		assertNull(ab.conjunction(aNb));

		Label uC = new Label(new Literal('b', State.unknown));
		assertNull(ab.conjunction(uC));
		
		Label b = Label.parse("¬b");
		Label b1 = Label.parse("¬b");
		assertEquals("¬b",b.conjunction(b1).toString());

	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#conjunctionExtended(it.univr.di.labeledvalue.Label)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testConjunctionExtended() {
		Label ab = Label.parse("ab");
		Label aNb = Label.parse("a¬b");
		// System.out.println(ab.conjunctionExtended(aNb));
		assertTrue(ab.conjunctionExtended(aNb).contains(new Literal('b', State.unknown)));
		assertFalse(ab.conjunctionExtended(aNb).contains(new Literal('b', State.straight)));
		assertFalse(ab.conjunctionExtended(aNb).contains(new Literal('b', State.negated)));
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#contains(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testContains() {
		Label ab = Label.parse("ab");
		Label aNb = Label.parse("a¬b");
		assertTrue(ab.contains(new Literal('a')));
		assertTrue(aNb.contains(Literal.parse("¬b")));
		//¿literals
		assertFalse(ab.contains(new Literal('a',State.unknown)));
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#equals(java.lang.Object)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testEqualsObject() {
		Label ab = Label.parse("a¬b");
		assertEquals(ab, Label.parse("a¬b"));
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#getAllAsStraight()}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testGetAllStraight() {
		Label ab = Label.parse("a¬b");
		ab.conjunctExtended(new Literal('c',State.unknown));
		assertArrayEquals(Label.parse("abc").toArray(),ab.getAllAsStraight());
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#getLiteralWithSameName(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testGetLiteralWithSameName() {
		Label ab = Label.parse("a¬b");
		ab.conjunctExtended(new Literal('c', State.unknown));

		Literal a = new Literal('a');
		assertEquals(a, ab.getLiteralWithSameName(new Literal('a')));

		a = new Literal('c', State.unknown);
		assertEquals(a, ab.getLiteralWithSameName(new Literal('c')));
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#getUniqueDifferentLiteral(it.univr.di.labeledvalue.Label)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testGetUniqueDifferentLiteral() {
		Label ab = Label.parse("a¬b");

		assertEquals(Literal.parse("a"), ab.getUniqueDifferentLiteral(Label.parse("¬a¬b")));
		assertNull(ab.getUniqueDifferentLiteral(Label.parse("¬ab")));
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#isConsistentWith(it.univr.di.labeledvalue.Label)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testIsConsistentWithLabel() {
		Label ab = Label.parse("a¬b");

		assertTrue(ab.isConsistentWith(Label.parse("a¬b")));
		assertFalse(ab.isConsistentWith(Label.parse("ab")));
		ab.conjunctExtended(new Literal('a', State.unknown));
		assertTrue(ab.isConsistentWith(Label.parse("a¬b")));
		assertTrue(ab.isConsistentWith(Label.parse("¬a¬b")));
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#isConsistentWith(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testIsConsistentWithLiteral() {
		Label ab = Label.parse("a¬b");

		assertTrue(ab.isConsistentWith(Literal.parse("¬b")));
		assertFalse(ab.isConsistentWith(Literal.parse("b")));
		ab.conjunctExtended(new Literal('a', State.unknown));
		assertTrue(ab.isConsistentWith(Literal.parse("a")));
		assertTrue(ab.isConsistentWith(Literal.parse("¬a")));
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#Label(it.univr.di.labeledvalue.Label)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testLabelLabel() {
		Label ab = Label.parse("a¬b");

		Label ab1 = new Label(ab);

		assertEquals(ab, ab1);
		assertFalse(ab == ab1);
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#negation()}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testNegation() {
		Label ab = Label.parse("a¬b");
		Literal[] litA = ab.negation();

		assertArrayEquals(litA, new Literal[] { Literal.parse("¬a"), new Literal('b') });

		ab.conjunctExtended(new Literal('c', State.unknown));
		litA = ab.negation();
		assertArrayEquals(litA, new Literal[] { Literal.parse("¬a"), new Literal('b'), null });

	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#parse(java.lang.String)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testParse() {
		Label ab = Label.parse("a¬b");

		Label ab1 = Label.parse(" a ¬b");
		assertEquals(ab, ab1);

		ab1 = Label.parse(" a¬ b");
		assertEquals(ab, ab1);

		ab1 = Label.parse(" a! b");
		assertNull(ab1);

		ab1 = Label.parse(" a¿ 		b");
		assertEquals("a¿b",ab1.toString());

		ab1 = Label.parse(String.valueOf(Constants.EMPTY_LABEL));
		assertEquals(0, ab1.size());

	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#removeAllLiteralsWithSameName(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testRemoveLiteralBoolean() {
		Label abc = Label.parse("a¬bc");

		assertTrue(abc.removeAllLiteralsWithSameName(new Literal('b')));
		assertEquals(Label.parse("ac"), abc);
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#getSubLabelIn(it.univr.di.labeledvalue.Label, boolean, boolean)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testSubLabelIn() {
		Label abc = Label.parse("a¬bc");
		Label abcUd = Label.parse("a¬bc¿d");
		
		Label sub1 = abc.getSubLabelIn(Label.parse("ac"), true, true);
		assertEquals(Label.parse("ac"), sub1);
		
		sub1 = abc.getSubLabelIn(Label.parse("ac"), false, true);
		assertEquals(Label.parse("¬b"), sub1);

		sub1 = abcUd.getSubLabelIn(Label.parse("ab"), true, true);
		assertEquals(Label.parse("a"), sub1);

		sub1 = abcUd.getSubLabelIn(Label.parse("ab"), true, false);
		assertEquals(Label.parse("a¿b"), sub1);

		Label result = Label.parse("¬b¿d");//¬b¿d
		// System.out.println(abc);
		sub1 = abcUd.getSubLabelIn(Label.parse("ac"), false, true);
		assertEquals(result, sub1);
		
		result = Label.parse("¬b");
		sub1 = abcUd.getSubLabelIn(Label.parse("¬bd"), true, true);
		assertEquals(result, sub1);
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#subsumes(it.univr.di.labeledvalue.Label)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testSubsumes() {
		Label ab = Label.parse("a¬b");
		Label abc = Label.parse("a¬bc");
		
		assertTrue(ab.subsumes(ab));
		assertTrue(abc.subsumes(ab));

		//¿literals
		Label abUc = Label.parse("a¬b¿c");
		assertEquals("a¬b¿c", abUc.toString());
		assertTrue(abUc.subsumes(Label.parse("a¬bc")));
		assertFalse(Label.parse("a¬bc").subsumes(abUc));
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#toString()}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testToString() {
		Label ab = Label.parse("¬b¬a¬d¬c");

		assertEquals("¬a¬b¬c¬d", ab.toString());
		ab.conjunctExtended(new Literal('c', State.unknown));
		assertEquals("¬a¬b" + Constants.UNKNOWN + "c¬d", ab.toString());

	}
	
	
	/**
	 * Test conjunction of empty label
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testUnmodifiable() {
		Label a = Label.parse("");
		Label b = Label.emptyLabel;
		
		Label c = a.conjunction(b);

		assertEquals(Label.emptyLabel,a);
		assertEquals(Label.emptyLabel,c);
	}


	/**
	 * 
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void caso20141128() {
		Label bgp = Label.parse("bgp");
		Label cp = Label.parse("cp");

		assertFalse(bgp.subsumes(cp));
	}

	
	
	/**
	 * Test unmodifiable Label
	 */
//	@SuppressWarnings("static-method")
//	@Test
//	public final void testUnmodifiable() {
//		Label ab = Label.parse("¬bad¬c");
//
//		UnmodifiableLabel abU = new UnmodifiableLabel(ab);
//		
//		Label ab1 = abU;
//		assertEquals(ab, ab1);
//		assertEquals(ab, abU);
//		assertNotSame(ab, abU);
//	}
//	/**
//	 * Test unmodifiable Label
//	 */
//	@SuppressWarnings("static-method")
//	@Test(expected = UnsupportedOperationException.class)
//	public final void testUnmodifiableException() {
//		Label ab = Label.parse("¬bad¬c");
//		UnmodifiableLabel abU = new UnmodifiableLabel(ab);
//		Label ab1 = abU;
//		ab1.conjunct(new Literal('x'));
//	}
}
