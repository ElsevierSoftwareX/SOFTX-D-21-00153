/**
 * 
 */
package it.univr.di.labeledvalue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;

/**
 * @author posenato
 */
public class LabeledIntTreeMapTest {

	/**
	 * stub class just to have a different implementation of the interface.
	 * 
	 * @author posenato
	 */
	private static class LabeledIntMapStub implements LabeledIntMap {

		/**
		 * 
		 */
		Object2IntRBTreeMap<Label> map;

		/**
		 * 
		 */
		public LabeledIntMapStub() {
			map = new Object2IntRBTreeMap<>();
		}

		@Override
		public boolean equals(final Object o) {
			if ((o == null) || !(o instanceof LabeledIntMap))
				return false;
			final LabeledIntMap lvm = ((LabeledIntMap) o);
			return this.entrySet().equals(lvm.entrySet());
		}

		@Override
		public int hashCode() {
			return this.map.hashCode();
		}

		@Override
		public int size() {
			return map.size();
		}

		@Override
		public boolean put(Label l, int i) {
			map.put(l, i);
			return false;
		}

		@Override
		public void clear() {
			map.clear();
		}

		@Override
		public IntSet values() {
			return null;
		}

		@Override
		public int remove(Label l) {
			return 0;
		}

		@Override
		public void putAll(LabeledIntMap inputMap) {
		}

		@Override
		public int get(Label l) {
			return 0;
		}

		@Override
		public int getMinValueConsistentWith(Label l) {
			return 0;
		}

		@Override
		public int getMinValue() {
			return 0;
		}

		@Override
		public int getMinValueAmongLabelsWOUnknown() {
			return 0;
		}

		@Override
		public String toString() {
			final StringBuffer sb = new StringBuffer("{");
			for (final Entry<Label> e : this.entrySet()) {// I wanted a sorted print!
				sb.append("(");
				sb.append(e.getKey().toString());
				sb.append(", ");
				final int value = e.getValue();
				if ((value == Constants.INT_NEG_INFINITE) || (value == Constants.INT_POS_INFINITE)) {
					if (value < 0) {
						sb.append('-');
					}
					sb.append(Constants.INFINITY_SYMBOL);
				} else {
					sb.append(value);
				}
				sb.append(") ");
			}
			sb.append("}");
			return sb.toString();
		}

		@Override
		public ObjectSet<Entry<Label>> entrySet() {
			return this.map.object2IntEntrySet();
		}

		@Override
		public ObjectSet<Label> keySet() {
			return null;//this.map.keySet();
		}
	};

	/**
	 * 
	 */
	LabeledIntMap actual = new LabeledIntTreeMap(true);
	/**
	 * 
	 */
	LabeledIntMap actual1 = new LabeledIntTreeMap(true);

	/**
	 * 
	 */
	LabeledIntMap expected = new LabeledIntTreeMap(true);

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void eliminazione() {
		Label l1 = Label.parse("¬a¬b");
		actual.clear();
		actual.put(Label.emptyLabel, 109);
		actual.put(l1, 21);

		actual.remove(l1);
		actual.remove(l1);
		actual.remove(Label.emptyLabel);
		actual.remove(Label.parse("a"));

		expected.clear();

		assertEquals("Test cancellazione ripetuta, di empty label e di una label inesistente:\n", expected, actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazione1Test() {
		actual.clear();
		actual.put(Label.emptyLabel, 109);
		actual.put(Label.parse("¬a¬b"), 21);
		actual.put(Label.parse("¬ab"), 21);

		expected.clear();
		expected.put(Label.emptyLabel, 109);
		expected.put(Label.parse("¬a"), 21);

		assertEquals("Test inserimento due label di pari valore con un letterale opposto:\n", expected, actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneRicorsivaTest() {
		actual.clear();
		actual.put(Label.emptyLabel, 109);
		actual.put(Label.parse("a"), 21);
		actual.put(Label.parse("¬a¬b"), 21);
		actual.put(Label.parse("¬ab"), 21);

		expected.clear();
		expected.put(Label.emptyLabel, 21);

		assertEquals("Test inserimento due label di pari valore con un letterale opposto con ricorsione:\n", expected, actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase1Test() {
		actual.clear();
		actual.put(Label.emptyLabel, 109);
		actual.put(Label.parse("ab"), 10);
		actual.put(Label.parse("a"), 25);
		actual.put(Label.parse("¬b"), 23);
		actual.put(Label.parse("b"), 22);
		actual.put(Label.parse("a¬b"), 23);
		actual.put(Label.parse("ab"), 20);

		expected.clear();
		expected.put(Label.emptyLabel, 23);
		expected.put(Label.parse("a"), 25);
		expected.put(Label.parse("b"), 22);
		expected.put(Label.parse("ab"), 10);

		assertEquals("Test su creazione base A INIZIO e gestione semplificazioni:\n", expected, actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase2Test() {
		actual.clear();
		actual.put(Label.emptyLabel, 109);
		actual.put(Label.parse("ab"), 10);
		actual.put(Label.parse("a¬b"), 23);
		actual.put(Label.parse("¬b"), 23);
		actual.put(Label.parse("b"), 22);
		actual.put(Label.parse("ab"), 20);

		expected.clear();
		expected.put(Label.emptyLabel, 109);
		expected.put(Label.parse("¬b"), 23);
		expected.put(Label.parse("b"), 22);
		expected.put(Label.parse("ab"), 10);

		assertEquals("Test su creazione base IN MEZZO e gestione semplificazioni:\n", expected, actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase3Test() {
		actual.clear();
		actual.put(Label.emptyLabel, 109);
		actual.put(Label.parse("A¬b"), 23);
		actual.put(Label.parse("ab"), 20);
		actual.put(Label.parse("¬b"), 23);
		actual.put(Label.parse("b"), 23);

		expected.clear();
		expected.put(Label.emptyLabel, 23);
		expected.put(Label.parse("ab"), 20);

		assertEquals("Test su creazione base IN FINE e gestione semplificazioni:\n", expected, actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase4Test() {
		actual.clear();
		actual.put(Label.emptyLabel, 109);
		actual.put(Label.parse("a¬b"), 19);
		actual.put(Label.parse("¬a¬b"), 19);
		actual.put(Label.parse("¬ab"), 10);
		actual.put(Label.parse("ab"), 10);
		// Map: {⊡, 109 (a, 19) (b, 10) (¬a¬b, 19) }

		expected.clear();
		expected.put(Label.emptyLabel, 109);
		expected.put(Label.parse("b"), 10);
		expected.put(Label.parse("¬b"), 19);

		assertEquals("Test su creazione e gestione semplificazioni:\n", expected, actual);

		actual.put(Label.parse("¬b"), 10);
		expected.clear();
		expected.put(Label.emptyLabel, 10);

		assertEquals("Test su creazione e gestione semplificazioni:\n", expected, actual);

	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void insert0() {
		actual.clear();
		actual.put(Label.emptyLabel, 0);

		expected.clear();
		expected.put(Label.emptyLabel, 0);
		assertEquals("Test su aggiunta di (empty,0):\n", expected, actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void distruzioneBase() {
		actual.clear();
		actual.put(Label.emptyLabel, 100);
		actual.put(Label.parse("ab"), 50);
		actual.put(Label.parse("a¬b"), 59);
		actual.put(Label.parse("¬aB"), 60);
		actual.put(Label.parse("¬a¬b"), 69);
		actual.put(Label.parse("¬a"), 30);
		actual.put(Label.parse("a"), 30);

		expected.clear();
		expected.put(Label.emptyLabel, 30);
		assertEquals("Test di distruzione base:\n", expected, actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void distruzioneBase2() {
		actual.clear();
		actual.put(Label.parse("b"), -12);
		actual.put(Label.parse("¬b"), -1);
		actual.put(Label.parse("¬a"), -10);

		actual.put(Label.emptyLabel, -10);

		expected.clear();
		expected.put(Label.emptyLabel, -10);
		expected.put(Label.parse("b"), -12);
		assertEquals("Test di distruzione base 2:\n", expected, actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void caso20141021() {
		actual.clear();
		actual.put(Label.parse("¬b"), 29);
		actual.put(Label.parse("a"), 26);
		actual.put(Label.parse("ab"), 24);
		actual.put(Label.emptyLabel, 30);
		actual.put(Label.parse("¬ab"), 28);
		actual.put(Label.parse("¬a"), 25);
		actual.put(Label.parse("¬aB"), 23);
		actual.put(Label.parse("a¬b"), 24);

		expected.clear();
		expected.put(Label.parse("a"), 24);
		expected.put(Label.parse("¬a"), 25);
		expected.put(Label.parse("¬aB"), 23);
		expected.put(Label.emptyLabel, 30);
		expected.put(Label.parse("¬b"), 29);

		// System.out.println("map:"+map);
		// System.out.println("result:"+result);
		assertEquals("Rimuovo componenti maggiori con elementi consistenti della base:\n", expected, actual);
	}

	/**
	 * 
	 */
	@Test
	public final void caso20141128() {
		actual.clear();
		actual.put(Label.parse("bgp"), 10);
		actual.put(Label.parse("cp"), -1);
		// System.out.println(map);

		assertEquals("{(bgp, 10) (cp, -1) }", actual.toString());
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void casoBaseConValoriDistinti() {
		actual.clear();
		actual.put(Label.parse("a"), 30);
		actual.put(Label.parse("¬a"), 25);
		actual.put(Label.emptyLabel, 31);
		actual.put(Label.parse("¬ab"), 24);
		actual.put(Label.parse("¬a¬b"), 30);
		actual.put(Label.parse("ab"), 23);

		expected.clear();
		expected.put(Label.parse("a"), 30);
		expected.put(Label.parse("¬a"), 25);
		expected.put(Label.parse("ab"), 23);
		expected.put(Label.parse("¬ab"), 24);
		expected.put(Label.emptyLabel, 31);

		// System.out.printf("map:"+map);
		assertEquals("Base con valori differenti:\n", expected, actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void equals() {
		actual.clear();
		actual.put(Label.parse("¬a¬b"), 30);
		actual.put(Label.parse("¬ab"), 24);
		actual.put(Label.parse("ab"), 23);
		actual.put(Label.parse("¬a"), 25);
		actual.put(Label.parse("a"), 30);
		actual.put(Label.emptyLabel, 31);

		LabeledIntMap expected = new LabeledIntMapStub();
		expected.clear();
		// ⊡, 30, ¬a,25 b,24 ab,23
		expected.put(Label.emptyLabel, 30);
		expected.put(Label.parse("¬a"), 25);
		expected.put(Label.parse("ab"), 23);
		expected.put(Label.parse("b"), 24);

		assertTrue("Test di equals con un'altra classe che implementa l'interfaccia:\nexpected: " + expected + "\nactual: " + actual, expected.equals(actual));
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void equals1() {
		actual.clear();
		actual.put(Label.parse("¬a¬b"), 30);
		actual.put(Label.parse("¬ab"), 24);
		actual.put(Label.parse("ab"), 23);
		actual.put(Label.parse("¬a"), 25);
		actual.put(Label.parse("a"), 30);
		actual.put(Label.emptyLabel, 31);

		expected.clear();
		// expected.put(Label.emptyLabel, 31);
		expected.put(Label.parse("a"), 30);
		expected.put(Label.parse("¬a"), 25);
		expected.put(Label.parse("ab"), 23);
		expected.put(Label.parse("¬ab"), 24);

		// System.out.printf("expected:"+expected +" actual:"+actual);
		assertTrue("Test di equals:\n", expected.equals(actual));
	}

	/**
	 * Check the kind of bundle result returned.
	 */
	@Test
	public final void checkIntSet() {
		actual.clear();
		actual.put(Label.parse("a"), 30);
		actual.put(Label.parse("¬a"), 25);
		actual.put(Label.emptyLabel, 31);
		actual.put(Label.parse("¬ab"), 24);
		actual.put(Label.parse("¬a¬b"), 30);
		actual.put(Label.parse("ab"), 23);

		ObjectSet<Entry<Label>> set = actual.entrySet();
		// System.out.println(set);
		for (Iterator<Entry<Label>> ite = set.iterator(); ite.hasNext();) {
			Entry<Label> e = ite.next();
			e.setValue(1);
		}
		assertEquals(set, actual.entrySet());
		// System.out.println("Map: "+map);
		// System.out.println("Set: "+set);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void putAllTest() {
		actual.clear();
		actual.put(Label.parse("a"), 30);
		actual.put(Label.parse("¬a"), 25);

		LabeledIntMapStub result = new LabeledIntMapStub();
		result.clear();
		result.put(Label.parse("b"), 30);
		result.put(Label.parse("¬b"), 25);

		actual1.clear();
		actual1.put(Label.parse("c"), 30);
		actual1.put(Label.parse("¬c"), 25);

		actual.putAll(result);
		assertEquals("Put all da un oggetto di una classe diversa che implementa la stessa interfaccia",
				AbstractLabeledIntMap.parse("{(a, 30) (¬a, 25) (b, 30) (¬b, 25) }"), actual);
		actual.putAll(actual1);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void putACleaningValueTest() {
		actual.clear();
		actual.put(Label.parse("a"), 30);
		actual.put(Label.parse("¬a"), 25);
		actual.put(Label.emptyLabel, 0);

		actual1 = new LabeledIntTreeMap(actual, true);
		actual1.put(Label.emptyLabel, 0);

		assertEquals("Put forcibly with a base", actual, actual1);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void parseTest() {
		actual = AbstractLabeledIntMap.parse("{(a, 30) (¬a, 25) (b, 30) (¬b, 25) }");

		expected.clear();
		expected.put(Label.parse("a"), 30);
		expected.put(Label.parse("¬a"), 25);
		expected.put(Label.parse("b"), 30);
		expected.put(Label.parse("¬b"), 25);
		assertEquals("Parse test", expected, actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void minValue() {
		actual.clear();
		actual.put(Label.parse("a"), 0);
		actual.put(Label.parse("¬a"), 1);

		// System.out.println(map.toString());
		assertTrue("Test min value", actual.getMinValue() == 0);
		actual1.clear();

		assertTrue(actual1.getMinValue() == LabeledIntNodeSetMap.INT_NULL);

		actual.clear();
		actual.put(Label.emptyLabel, 109);
		actual.put(Label.parse("ab"), 10);
		actual.put(Label.parse("a"), Constants.INT_POS_INFINITE);
		actual.put(Label.parse("¬b"), 23);
		actual.put(Label.parse("b"), 22);
		actual.put(Label.parse("a¬b"), Constants.INT_NULL);

		assertEquals("Min value: ", 10, actual.getMinValue());

		actual.put(Label.parse("ab"), Constants.INT_NEG_INFINITE);

		assertEquals("Min value: ", Constants.INT_NEG_INFINITE, actual.getMinValue());
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void minValueConsistentWithTest() {
		actual.clear();
		actual.put(Label.parse("a"), 0);
		actual.put(Label.parse("¬a"), 1);

		assertTrue(actual.getMinValueConsistentWith(Label.parse("¬a")) == 1);
	}

	/**
	 * Check the sum of labeled value.
	 */
	@Test
	public final void simplificationWithInfinite() {
		actual.clear();
		actual.put(Label.emptyLabel, 109);
		actual.put(Label.parse("ab"), 10);
		actual.put(Label.parse("a"), Constants.INT_POS_INFINITE);
		actual.put(Label.parse("¬b"), 23);
		actual.put(Label.parse("b"), 22);
		actual.put(Label.parse("a¬b"), Constants.INT_NULL);
		actual.put(Label.parse("ab"), Constants.INT_NEG_INFINITE);

		// System.out.println(map);
		expected = AbstractLabeledIntMap.parse("{(¬b, 23) (b, 22) }");
		expected.put(Label.parse("ab"), Constants.INT_NEG_INFINITE);
		expected.put(Label.emptyLabel, 109);
		assertEquals("Test about simplification with infinity numbers:\n", expected, actual);
	}

	/**
	 * Check the sum of labeled value.
	 */
	@Test
	public final void simplificationWithUnknown() {
		Label ab = Label.parse("ab");
		Label abp = Label.parse("abp");
		Label abNp = Label.parse("ab¬p");
		Label abUp = Label.parse("ab¿p");

		actual.clear();
		actual.put(ab, 10);
		actual.put(abUp, 10);
		assertEquals("Test about simplification with unknown labeled numbers:\n", AbstractLabeledIntMap.parse("{(ab, 10) }"), actual);

		actual.clear();
		actual.put(abp, 10);
		actual.put(abUp, 10);
		assertEquals("Test about simplification with unknown labeled numbers:\n", AbstractLabeledIntMap.parse("{(abp, 10) }"), actual);

		actual.clear();
		actual.put(abp, Constants.INT_NEG_INFINITE);
		actual.put(abUp, Constants.INT_NEG_INFINITE);
		actual1.clear();
		actual1.put(abp, Constants.INT_NEG_INFINITE);
		assertEquals("Test about simplification with unknown labeled numbers:\n", actual1, actual);

		actual.clear();
		actual.put(abp, 10);
		actual.put(abNp, 9);
		actual.put(abUp, 9);
		expected = AbstractLabeledIntMap.parse("{(abp, 10) (ab¬p, 9) }");
		assertEquals("Test about simplification with unknown labeled numbers:\n", expected, actual);

		actual.clear();
		actual.put(abUp, 9);
		actual.put(abp, 10);
		actual.put(abNp, 9);
		assertEquals("Test about simplification with unknown labeled numbers:\n", AbstractLabeledIntMap.parse("{(abp, 10) (ab¬p, 9) }"), actual);

		actual.clear();
		actual.put(abp, 10);
		actual.put(abNp, 9);
		actual.put(abUp, 8);
		assertEquals("Test about simplification with unknown labeled numbers:\n", AbstractLabeledIntMap.parse("{(abp, 10) (ab¬p, 9) (ab¿p, 8) }"),
				actual);
	}

	/**
	 * Check the sum of labeled value.
	 */
	@Test
	public final void simplificationWithUnknown1() {
		Label Upab = Label.parse("¿p¿ab");
		Label Upr = Label.parse("¿pr");
		Label UpNr = Label.parse("¿p¬r");

		actual.clear();
		actual.put(Upab, -24);
		actual.put(Upr, Constants.INT_NEG_INFINITE);
		// System.out.println("Before introduction UpNr: "+ map);
		actual.put(UpNr, Constants.INT_NEG_INFINITE);
		assertEquals("Test about simplification with unknown labeled infinity:\n", "{(¿p, -∞) }", actual.toString());
	}
}
