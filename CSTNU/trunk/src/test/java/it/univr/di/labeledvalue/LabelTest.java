/**
 * 
 */
package it.univr.di.labeledvalue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;

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
		Literal uC = Literal.valueOf('c', Literal.UNKNONW);
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
		Label aNotB = Label.parse("a¬b");
		Literal uC = Literal.valueOf('c', Literal.UNKNONW);
		Label aNotB1 = Label.clone(aNotB);
		assertTrue(aNotB.compareTo(aNotB1) == 0);

		aNotB.conjunctExtended(uC.getNegated());
		aNotB1.conjunctExtended(uC);
		assertTrue(aNotB.compareTo(aNotB1) < 0);
		assertTrue("ab1.size:" + aNotB1.size(), 3 == aNotB1.size());

		aNotB1.remove(uC);
		aNotB1.conjunctExtended(uC.getStraight());
		// aNotB=a¬b¬c aNotB1=a¬bc
		assertTrue(aNotB.compareTo(aNotB1) > 0);

		aNotB.remove(uC.getNegated());
		aNotB.conjunctExtended(uC);
		// aNotB=a¬b¿c aNotB1=a¬bc
		assertTrue(aNotB.compareTo(aNotB1) > 0);
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#conjunct(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testConjunct() {
		Label ab = Label.parse("a¬b");
		assertTrue("ab.size=" + ab.size(), 2 == ab.size());
		ab.conjunct(Literal.valueOf('c'));
		assertEquals(Label.parse("a¬bc"), ab);
		assertTrue("ab.size=" + ab.size(), 3 == ab.size());
		ab.conjunct(Literal.valueOf('d', Literal.UNKNONW));
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
		ab.conjunctExtended(Literal.valueOf('b', Literal.UNKNONW));
		// System.out.println(ab);
		assertTrue(ab.getStateLiteralWithSameName('b') == Literal.UNKNONW);

		ab.conjunctExtended(Literal.valueOf('d', Literal.UNKNONW));
		assertTrue(ab.getStateLiteralWithSameName('d') == Literal.UNKNONW);
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

		Label uC = new Label('b', Literal.UNKNONW);
		assertNull(ab.conjunction(uC));

		Label b = Label.parse("¬b");
		Label b1 = Label.parse("¬b");
		assertEquals("¬b", b.conjunction(b1).toString());
		assertTrue(1 == b.conjunction(b1).size());
		
		assertNull("Empty label conunct with an unkwown is null", Label.emptyLabel.conjunction(Label.parse("¿p")));
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
		assertTrue(ab.conjunctionExtended(aNb).contains(Literal.valueOf('b', Literal.UNKNONW)));
		assertFalse(ab.conjunctionExtended(aNb).contains(Literal.valueOf('b', Literal.STRAIGHT)));
		assertFalse(ab.conjunctionExtended(aNb).contains(Literal.valueOf('b', Literal.NEGATED)));
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#contains(it.univr.di.labeledvalue.Literal)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testContains() {
		Label ab = Label.parse("ab");
		Label aNb = Label.parse("a¬b");
		assertTrue(ab.contains(Literal.valueOf('a')));
		assertTrue(aNb.contains(Literal.parse("¬b")));
		// ¿literals
		assertFalse(ab.contains(Literal.valueOf('a', Literal.UNKNONW)));
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
		ab.conjunctExtended(Literal.valueOf('c', Literal.UNKNONW));
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
		ab.conjunctExtended(Literal.valueOf('c', Literal.UNKNONW));

		assertEquals(Literal.STRAIGHT, ab.getStateLiteralWithSameName('a'));

		assertEquals(Literal.UNKNONW, ab.getStateLiteralWithSameName('c'));
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
		ab.conjunctExtended(Literal.valueOf('c', Literal.UNKNONW));
		assertTrue(ab.isConsistentWith(Label.parse("a¬b")));
		assertFalse(ab.isConsistentWith(Label.parse("¬a¬b")));
		ab.conjunctExtended(Literal.valueOf('a', Literal.UNKNONW));
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

		assertFalse(ab.isConsistentWith(Literal.index('b'), Literal.UNKNONW));
		assertFalse(ab.isConsistentWith(Literal.index('b'), Literal.STRAIGHT));
		ab.conjunctExtended(Literal.valueOf('a', Literal.UNKNONW));
		assertFalse(ab.isConsistentWith(Literal.index('a'), Literal.STRAIGHT));
		assertTrue(ab.isConsistentWith(Literal.index('a'), Literal.UNKNONW));
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.Label#clone(Label)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testLabelLabel() {
		Label ab = Label.parse("a¬b");

		Label ab1 = Label.clone(ab);

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

		assertArrayEquals(litA, new Literal[] { Literal.parse("¬a"), Literal.valueOf('b') });

		ab.conjunctExtended(Literal.valueOf('c', Literal.UNKNONW));
		litA = ab.negation();
		assertArrayEquals(litA, new Literal[] { Literal.parse("¬a"), Literal.valueOf('b'), null });

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
		ab.conjunctExtended(Literal.valueOf('c', Literal.UNKNONW));
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
		Label b = Label.clone(a);

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
			empty.conjunct('a', Literal.NEGATED);
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
		Label a = new Label('a', Literal.STRAIGHT);
		assertTrue(empty.compareTo(a) < 0);

		Label notA = new Label('a', Literal.NEGATED);
		assertTrue(notA.compareTo(a) > 0);

		Label b = new Label('b', Literal.NEGATED);
		assertTrue(notA.compareTo(b) < 0);

		a = Label.parse("¬a¬b");
		b = Label.parse("¬d¬e");
		assertTrue(a.compareTo(b) < 0);

		a = Label.parse("¬a¬b");
		b = Label.parse("¬b¬e");
		assertTrue(a.compareTo(b) < 0);

		a = Label.parse("¬a¬b");
		b = Label.parse("¬b");
		assertTrue(a.compareTo(b) > 0);

		a = Label.parse("¬b");
		b = Label.parse("¬bc");
		assertTrue(a.compareTo(b) < 0);

		a = Label.parse("¬b¬c");
		b = Label.parse("¬bc");
		assertTrue(a.compareTo(b) > 0);

		a = Label.parse("¬b¬c");
		b = Label.parse("¬b¬c");
		assertTrue(a.compareTo(b) == 0);

		a = Label.parse("ac");
		b = Label.parse("ab");
		assertTrue(a.compareTo(b) > 0);

	}

	/**
	 * Proposes only some execution time estimates about some class methods.
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		final int nTest = 1000;
		final double msNorm = 1.0 / (1000000.0 * nTest);

		final Literal d = Literal.valueOf('d'), z = Literal.valueOf('z');

		Label empty = Label.emptyLabel;

		System.out.println("Empty: " + empty);
		Label result;
		// System.out.println("Empty: " + result);
		result = new Label('a', Literal.STRAIGHT);
		System.out.println("a: " + result);
		result = new Label('b', Literal.NEGATED);
		System.out.println("¬b: " + result);
		result = new Label('a', Literal.ABSENT);
		System.out.println("Null: " + result);

		Label l1 = Label.parse(Constants.NOT + "abcd");
		Label l2 = Label.parse(Constants.NOT + "aejfsd");
		System.out.println("l1: " + l1 + "\nl2: " + l2);
		long startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			result = l1.conjunction(l2);
		}
		long endTime = System.nanoTime();
		System.out.println(
				"Execution time for a simple conjunction of '" + l1 + "' with '" + l2 + "'='" + result + "' (ms): " + ((endTime - startTime) * msNorm));

		l1 = Label.parse(Constants.NOT + "abcd");
		l2 = Label.parse("a¬d¬cejfs");
		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			result = l1.conjunctionExtended(l2);
		}
		endTime = System.nanoTime();
		System.out.println(
				"Execution time for an extended conjunction of '" + l1 + "' with '" + l2 + "'='" + result + "' (ms): " + ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.isConsistentWith(l2);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for checking if two (inconsistent) labels are consistent. Details '" + l1 + "' with '" + l2 + "' (ms): "
				+ ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.isConsistentWith(l1);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for checking if two (consistent) labels are consistent. Details '" + l1 + "' with '" + l1 + "' (ms): "
				+ ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.contains(d);
		}
		endTime = System.nanoTime();
		System.out.println(
				"Execution time for checking if a literal is present in a label (the literal is the last inserted) (ms): " + ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.contains('d');
		}
		endTime = System.nanoTime();
		System.out.println(
				"Execution time for checking if a literal is present in a label (given the name) (ms): " + ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.contains(z);
		}
		endTime = System.nanoTime();
		System.out.println(
				"Execution time for checking if a literal is present in a label (the literal is not present) (ms): " + ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.contains('d');
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for get the literal in the label with the same proposition letter (the literal is present) (ms): "
				+ ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.contains('z');
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for get the literal in the label with the same proposition letter (the literal is not present) (ms): "
				+ ((endTime - startTime) * msNorm));
	}

	@SuppressWarnings({ "javadoc", "static-method" })
	public final void longTest() {
		System.out.println("-2: " + Long.toBinaryString(1 << 63));
		System.out.println("0: " + Long.toBinaryString(0));
		System.out.println("1: " + Long.toBinaryString(1));
		System.out.println("-1*2+1: " + Long.toUnsignedString(-1 * 2 + 1, 2));
		System.out.println("0: " + Long.toUnsignedString(0, 2));
		System.out.println("1: " + Long.toUnsignedString(1, 2));
		System.out.println(Long.toString(1 << 62) + ": " + Long.toUnsignedString(1 << 62));
		System.out.println(Long.compareUnsigned(-1, 1 << 60));
	}

	@SuppressWarnings({ "javadoc", "static-method" })
	@Test
	public final void comparatorOrder() {
		ObjectSortedSet<Label> order = new ObjectRBTreeSet<>();

		order.add(new Label('a', Literal.STRAIGHT));
		order.add(new Label('a', Literal.NEGATED));
		order.add(new Label('a', Literal.UNKNONW));
		order.add(new Label('b', Literal.STRAIGHT));
		order.add(new Label('b', Literal.NEGATED));
		order.add(new Label('b', Literal.UNKNONW));
		order.add(new Label('c', Literal.STRAIGHT));
		order.add(new Label('c', Literal.NEGATED));
		order.add(new Label('c', Literal.UNKNONW));
		order.add(Label.parse("ab"));
		order.add(Label.parse("a¬b"));
		order.add(Label.parse("¬ab"));
		order.add(Label.parse("¬a¬b"));
		order.add(Label.parse("ac"));
		order.add(Label.parse("¬ac"));
		// System.out.println("Order: " + order);
		Assert.assertEquals("{a, ¬a, ¿a, b, ¬b, ¿b, c, ¬c, ¿c, ab, a¬b, ac, ¬ab, ¬a¬b, ¬ac}", order.toString());
		Assert.assertEquals(15, order.size());
	}
}
