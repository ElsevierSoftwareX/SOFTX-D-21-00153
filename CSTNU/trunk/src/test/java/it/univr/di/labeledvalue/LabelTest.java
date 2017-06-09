/**
 * 
 */
package it.univr.di.labeledvalue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.univr.di.labeledvalue.Literal.State;

/**
 * @author posenato
 */
public class LabelTest {

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#allComponentsOfBaseGenerator(Literal[])}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testAllComponentsOfBaseGenerator() {
		Label ab = Label.parse("a¬b");
		Literal uC = Literal.create('c', State.unknown);
		Label[] abV = Label.allComponentsOfBaseGenerator(ab.getPropositions());
		assertArrayEquals(new Label[] { Label.parse("ab"), Label.parse("¬ab"), Label.parse("a¬b"), Label.parse("¬a¬b") }, abV);

		ab.conjunct(uC);
		abV = Label.allComponentsOfBaseGenerator(ab.getPropositions());
		assertArrayEquals(new Label[] { Label.parse("ab"), Label.parse("¬ab"), Label.parse("a¬b"), Label.parse("¬a¬b") }, abV);
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#compareTo(it.univr.di.labeledvalue.Label)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testCompareTo() {
		Label ab = Label.parse("a¬b");
		Literal uC = Literal.create('c', State.unknown);
		Label ab1 = new Label(ab);
		assertTrue(ab.compareTo(ab1) == 0);

		ab.conjunctExtended(uC.getNegated());
		ab1.conjunctExtended(uC);
		assertTrue(ab.compareTo(ab1) < 0);
		assertTrue("ab1.size:" + ab1.size(), 3 == ab1.size());

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
		assertTrue("ab.size=" + ab.size(), 2 == ab.size());
		ab.conjunct(Literal.create('c'));
		assertEquals(Label.parse("a¬bc"), ab);
		assertTrue("ab.size=" + ab.size(), 3 == ab.size());
		ab.conjunct(Literal.create('d', State.unknown));
		assertEquals(Label.parse("a¬bc"), ab);
		assertTrue(3 == ab.size());
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#conjunctExtended(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testConjunctExtended() {
		Label ab = Label.parse("a¬b");
		ab.conjunctExtended(Literal.create('b', State.unknown));
		// System.out.println(ab);
		assertTrue(ab.getStateLiteralWithSameName('b') == State.unknown);

		ab.conjunctExtended(Literal.create('d', State.unknown));
		assertTrue(ab.getStateLiteralWithSameName('d') == State.unknown);
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

		Label uC = new Label('b', State.unknown);
		assertNull(ab.conjunction(uC));

		Label b = Label.parse("¬b");
		Label b1 = Label.parse("¬b");
		assertEquals("¬b", b.conjunction(b1).toString());
		assertTrue(1 == b.conjunction(b1).size());
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
		assertTrue(ab.conjunctionExtended(aNb).contains(Literal.create('b', State.unknown)));
		assertFalse(ab.conjunctionExtended(aNb).contains(Literal.create('b', State.straight)));
		assertFalse(ab.conjunctionExtended(aNb).contains(Literal.create('b', State.negated)));
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#contains(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testContains() {
		Label ab = Label.parse("ab");
		Label aNb = Label.parse("a¬b");
		assertTrue(ab.contains(Literal.create('a')));
		assertTrue(aNb.contains(Literal.parse("¬b")));
		// ¿literals
		assertFalse(ab.contains(Literal.create('a', State.unknown)));
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
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testGetAllStraight() {
		Label ab = Label.parse("a¬b");
		ab.conjunctExtended(Literal.create('c', State.unknown));
		char[] expected = Label.parse("abc").getPropositions();
		char[] obtained = ab.getPropositions();
		assertArrayEquals(expected, obtained);
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#getLiteralWithSameName(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testGetLiteralWithSameName() {
		Label ab = Label.parse("a¬b");
		ab.conjunctExtended(Literal.create('c', State.unknown));

		assertEquals(State.straight, ab.getStateLiteralWithSameName('a'));

		assertEquals(State.unknown, ab.getStateLiteralWithSameName('c'));
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

		assertTrue(ab.isConsistentWith(Label.parse("a¬bc")));
		assertTrue(ab.isConsistentWith(Label.parse("a¬b¬c")));
		assertTrue(ab.isConsistentWith(Label.parse("a¬b¿c")));
		assertTrue(Label.parse("a¬bc").isConsistentWith(ab));
		assertTrue(Label.parse("a¬b¬c").isConsistentWith(ab));
		assertTrue(Label.parse("a¬b¿c").isConsistentWith(ab));

		assertTrue(ab.isConsistentWith(Label.parse("a¬b")));
		assertFalse(ab.isConsistentWith(Label.parse("ab")));
		ab.conjunctExtended(Literal.create('c', State.unknown));
		assertTrue(ab.isConsistentWith(Label.parse("a¬b")));
		assertFalse(ab.isConsistentWith(Label.parse("¬a¬b")));
		ab.conjunctExtended(Literal.create('a', State.unknown));
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

		assertFalse(ab.isConsistentWith(Literal.index('b'), State.unknown));
		assertFalse(ab.isConsistentWith(Literal.index('b'), State.straight));
		ab.conjunctExtended(Literal.create('a', State.unknown));
		assertFalse(ab.isConsistentWith(Literal.index('a'), State.straight));
		assertTrue(ab.isConsistentWith(Literal.index('a'), State.unknown));
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

		assertArrayEquals(litA, new Literal[] { Literal.parse("¬a"), Literal.create('b') });

		ab.conjunctExtended(Literal.create('c', State.unknown));
		litA = ab.negation();
		assertArrayEquals(litA, new Literal[] { Literal.parse("¬a"), Literal.create('b'), null });

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
		assertEquals("a¿b", ab1.toString());

		ab1 = Label.parse(String.valueOf(Constants.EMPTY_LABEL));
		assertEquals(0, ab1.size());

	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#removeAllLiteralsWithSameName(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testRemoveLiteralBoolean() {
		Label abc = Label.parse("a¬bc");

		abc.remove('b');
		assertEquals(Label.parse("ac"), abc);
		assertTrue(2 == abc.size());

		abc.remove(Literal.parse("¬a"));
		assertEquals(Label.parse("ac"), abc);
		assertTrue(2 == abc.size());

		abc.remove(Literal.parse("a"));
		assertEquals(Label.parse("c"), abc);
		assertTrue(1 == abc.size());

		abc.remove(Literal.parse("c"));
		assertEquals(Label.emptyLabel, abc);
		assertTrue(0 == abc.size());
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#removeAllLiteralsWithSameName(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings({ "static-method", "javadoc" })
	@Test
	public final void testRemoveLabel() {
		Label abc = Label.parse("a¬bc");

		abc.remove(Label.parse("b"));
		assertEquals(Label.parse("ac"), abc);
		assertTrue(2 == abc.size());

		abc.remove(Label.parse("¬a"));
		assertEquals(Label.parse("c"), abc);
		assertTrue(1 == abc.size());

		abc.remove(Label.emptyLabel);
		assertEquals(Label.parse("c"), abc);
		assertTrue(1 == abc.size());

		Label.emptyLabel.remove(abc);
		assertEquals(Label.emptyLabel, Label.emptyLabel);
		assertTrue(0 == Label.emptyLabel.size());

		abc.remove(Label.parse("c"));
		assertEquals(Label.emptyLabel, abc);
		assertTrue(0 == abc.size());

		abc.remove(Label.parse("c"));
		assertEquals(Label.emptyLabel, abc);
		assertTrue(0 == abc.size());
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#removeAllLiteralsWithSameName(it.univr.di.labeledvalue.Literal)}.
	 */
	// @SuppressWarnings("static-method")
	// @Test
	// public final void testRemove() {
	// Label abc = Label.parse("ac");
	//
	// abc.remove('c');
	// assertEquals(0, abc.maxIndex);
	//
	// abc = Label.parse("abc");
	// abc.remove('b');
	// assertEquals(2, abc.maxIndex);
	//
	// abc = Label.parse("abc");
	// abc.remove('c');
	// assertEquals(1, abc.maxIndex);
	//
	// }

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
		assertTrue(2 == sub1.size());

		sub1 = abc.getSubLabelIn(Label.parse("ac"), false, true);
		assertEquals(Label.parse("¬b"), sub1);
		assertTrue(1 == sub1.size());

		sub1 = abcUd.getSubLabelIn(Label.parse("ab"), true, true);
		assertEquals(Label.parse("a"), sub1);
		assertTrue(1 == sub1.size());

		sub1 = abcUd.getSubLabelIn(Label.parse("ab"), true, false);
		assertEquals(Label.parse("a¿b"), sub1);
		assertTrue(2 == sub1.size());

		Label result = Label.parse("¬b¿d");// ¬b¿d
		// System.out.println(abc);
		sub1 = abcUd.getSubLabelIn(Label.parse("ac"), false, true);
		assertEquals(result, sub1);
		assertTrue(2 == sub1.size());

		result = Label.parse("¬b");
		sub1 = abcUd.getSubLabelIn(Label.parse("¬bd"), true, true);
		assertEquals(result, sub1);
		assertTrue(1 == sub1.size());
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

		// ¿literals
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
		ab.conjunctExtended(Literal.create('c', State.unknown));
		assertEquals("¬a¬b" + Constants.UNKNOWN + "c¬d", ab.toString());

	}

	/**
	 * Test conjunction of empty label
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void unmodifiable() {
		Label a = Label.parse("");
		Label b = Label.emptyLabel;

		assertTrue(0 == b.size());

		Label c = a.conjunction(b);
		assertTrue(0 == c.size());

		assertEquals(Label.emptyLabel, a);
		assertEquals(Label.emptyLabel, c);
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
	 * 
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void cloneEmptyLabel() {
		Label a = Label.emptyLabel;
		Label b = new Label(a);

		// for(char c='A'; c< 'ù'; c++)
		// System.out.println("Char "+c+" to int:"+Character.hashCode(c));
		assertTrue(a.equals(b));
		assertTrue(0 == b.size());

	}

	/**
	 * Test unmodifiable Label
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testUnmodifiable() {
		Label empty = Label.emptyLabel;

		try {
			empty.conjunct('a', State.negated);
		} catch (IllegalAccessError e) {
			assertTrue(true);
			return;
		}
		assertFalse("Exception not captured", true);
	}

	/**
	 * 
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void compateTo() {
		Label empty = Label.emptyLabel;
		Label a = new Label('a', State.straight);
		assertTrue(empty.compareTo(a) < 0);

		Label an = new Label('a', State.negated);
		assertTrue(an.compareTo(a) < 0);

		Label b = new Label('b', State.negated);
		assertTrue(an.compareTo(b) < 0);

		a = Label.parse("¬a¬b");
		b = Label.parse("¬d¬e");
		assertTrue(a.compareTo(b) < 0);

		a = Label.parse("¬a¬b");
		b = Label.parse("¬b¬e");
		assertTrue(a.compareTo(b) < 0);

		a = Label.parse("¬a¬b");
		b = Label.parse("¬b");
		assertTrue(a.compareTo(b) < 0);

		a = Label.parse("¬b");
		b = Label.parse("¬bc");
		assertTrue(a.compareTo(b) < 0);

		a = Label.parse("¬b¬c");
		b = Label.parse("¬bc");
		assertTrue(a.compareTo(b) < 0);

		a = Label.parse("¬b¬c");
		b = Label.parse("¬b¬c");
		assertTrue(a.compareTo(b) == 0);

	}
}
