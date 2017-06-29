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
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
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
			this.map = new Object2IntRBTreeMap<>();
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
			return this.map.size();
		}

		@Override
		public boolean put(Label l, int i) {
			this.map.put(l, i);
			return false;
		}

		@Override
		public void clear() {
			this.map.clear();
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
			// not implemented
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
				final int value = e.getIntValue();
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
			return null;// this.map.keySet();
		}

		@Override
		public LabeledIntMap createLabeledIntMap() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public LabeledIntMap createLabeledIntMap(LabeledIntMap lim) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ObjectSet<Entry<Label>> entrySet(ObjectSet<Entry<Label>> setToReuse) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ObjectSet<Label> keySet(ObjectSet<Label> setToReuse) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	/**
	 * 
	 */
	LabeledIntMapFactory<LabeledIntTreeMap> factory = new LabeledIntMapFactory<>(LabeledIntTreeMap.class);

	/**
	 * 
	 */
	LabeledIntMap actual = this.factory.get();
	/**
	 * 
	 */
	LabeledIntMap actual1 = this.factory.get();

	/**
	 * 
	 */
	LabeledIntMap expected = this.factory.get();

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void eliminazione() {
		Label l1 = Label.parse("¬a¬b");
		this.actual.clear();
		this.actual.put(Label.emptyLabel, 109);
		this.actual.put(l1, 21);

		this.actual.remove(l1);
		this.actual.remove(l1);
		this.actual.remove(Label.emptyLabel);
		this.actual.remove(Label.parse("a"));

		this.expected.clear();

		assertEquals("Test cancellazione ripetuta, di empty label e di una label inesistente:\n", this.expected, this.actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazione1Test() {
		this.actual.clear();
		this.actual.put(Label.emptyLabel, 109);
		this.actual.put(Label.parse("¬a¬b"), 21);
		this.actual.put(Label.parse("¬ab"), 21);

		this.expected.clear();
		this.expected.put(Label.emptyLabel, 109);
		this.expected.put(Label.parse("¬a"), 21);

		assertEquals("Test inserimento due label di pari valore con un letterale opposto:\n", this.expected, this.actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneRicorsivaTest() {
		this.actual.clear();
		this.actual.put(Label.emptyLabel, 109);
		this.actual.put(Label.parse("a"), 21);
		this.actual.put(Label.parse("¬a¬b"), 21);
		this.actual.put(Label.parse("¬ab"), 21);

		this.expected.clear();
		this.expected.put(Label.emptyLabel, 21);

		assertEquals("Test inserimento due label di pari valore con un letterale opposto con ricorsione:\n", this.expected, this.actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase1Test() {
		this.actual.clear();
		this.actual.put(Label.emptyLabel, 109);
		this.actual.put(Label.parse("ab"), 10);
		this.actual.put(Label.parse("a"), 25);
		this.actual.put(Label.parse("¬b"), 23);
		this.actual.put(Label.parse("b"), 22);
		this.actual.put(Label.parse("a¬b"), 23);
		this.actual.put(Label.parse("ab"), 20);

		this.expected.clear();
		// expected.put(Label.emptyLabel, 23);
		this.expected.put(Label.parse("¬b"), 23);
		this.expected.put(Label.parse("a"), 25);
		this.expected.put(Label.parse("b"), 22);
		this.expected.put(Label.parse("ab"), 10);

		assertEquals("Test su creazione base A INIZIO e gestione semplificazioni:\n", this.expected, this.actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase2Test() {
		this.actual.clear();
		this.actual.put(Label.emptyLabel, 109);
		this.actual.put(Label.parse("ab"), 10);
		this.actual.put(Label.parse("a¬b"), 23);
		this.actual.put(Label.parse("¬b"), 23);
		this.actual.put(Label.parse("b"), 22);
		this.actual.put(Label.parse("ab"), 20);

		this.expected.clear();
		this.expected.put(Label.emptyLabel, 109);
		this.expected.put(Label.parse("¬b"), 23);
		this.expected.put(Label.parse("b"), 22);
		this.expected.put(Label.parse("ab"), 10);

		assertEquals("Test su creazione base IN MEZZO e gestione semplificazioni:\n", this.expected, this.actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase3Test() {
		this.actual.clear();
		this.actual.put(Label.emptyLabel, 109);
		this.actual.put(Label.parse("A¬b"), 23);
		this.actual.put(Label.parse("ab"), 20);
		this.actual.put(Label.parse("¬b"), 23);
		this.actual.put(Label.parse("b"), 23);

		this.expected.clear();
		this.expected.put(Label.emptyLabel, 23);
		this.expected.put(Label.parse("ab"), 20);

		assertEquals("Test su creazione base IN FINE e gestione semplificazioni:\n", this.expected, this.actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase4Test() {
		this.actual.clear();
		this.actual.put(Label.emptyLabel, 109);
		this.actual.put(Label.parse("a¬b"), 19);
		this.actual.put(Label.parse("¬a¬b"), 19);
		this.actual.put(Label.parse("¬ab"), 10);
		this.actual.put(Label.parse("ab"), 10);
		// Map: {⊡, 109 (19, a) (10, b) (19, ¬a¬b) }

		this.expected.clear();
		this.expected.put(Label.emptyLabel, 109);
		this.expected.put(Label.parse("b"), 10);
		this.expected.put(Label.parse("¬b"), 19);

		assertEquals("Test su creazione e gestione semplificazioni:\n", this.expected, this.actual);

		this.actual.put(Label.parse("¬b"), 10);
		this.expected.clear();
		this.expected.put(Label.emptyLabel, 10);

		assertEquals("Test su creazione e gestione semplificazioni:\n", this.expected, this.actual);

	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void insert0() {
		this.actual.clear();
		this.actual.put(Label.emptyLabel, 0);

		this.expected.clear();
		this.expected.put(Label.emptyLabel, 0);
		assertEquals("Test su aggiunta di (empty,0):\n", this.expected, this.actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void distruzioneBase() {
		this.actual.clear();
		this.actual.put(Label.emptyLabel, 100);
		this.actual.put(Label.parse("ab"), 50);
		this.actual.put(Label.parse("a¬b"), 59);
		this.actual.put(Label.parse("¬ab"), 60);
		this.actual.put(Label.parse("¬a¬b"), 69);
		this.actual.put(Label.parse("¬a"), 30);
		this.actual.put(Label.parse("a"), 30);

		this.expected.clear();
		this.expected.put(Label.emptyLabel, 30);
		assertEquals("Test di distruzione base:\n", this.expected, this.actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void distruzioneBase2() {
		this.actual.clear();
		this.actual.put(Label.parse("b"), -12);
		this.actual.put(Label.parse("¬b"), -1);
		this.actual.put(Label.parse("¬a"), -10);

		this.actual.put(Label.emptyLabel, -10);

		this.expected.clear();
		this.expected.put(Label.emptyLabel, -10);
		this.expected.put(Label.parse("b"), -12);
		assertEquals("Test di distruzione base 2:\n", this.expected, this.actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void caso20141021() {
		this.actual.clear();
		this.actual.put(Label.parse("¬b"), 29);
		this.actual.put(Label.parse("a"), 26);
		this.actual.put(Label.parse("ab"), 24);
		this.actual.put(Label.emptyLabel, 30);
		this.actual.put(Label.parse("¬ab"), 28);
		this.actual.put(Label.parse("¬a"), 25);
		this.actual.put(Label.parse("¬ab"), 23);
		this.actual.put(Label.parse("a¬b"), 24);

		this.expected.clear();
		// expected.put(Label.emptyLabel, 26);
		// expected.put(Label.parse("¬a"), 25);
		// expected.put(Label.parse("¬ab"), 23);
		// expected.put(Label.parse("a¬b"), 24);
		// expected.put(Label.parse("b"), 24);
		this.expected.put(Label.parse("ab"), 24);
		this.expected.put(Label.parse("¬ab"), 23);
		this.expected.put(Label.parse("a¬b"), 24);
		this.expected.put(Label.parse("a"), 26);
		this.expected.put(Label.parse("¬a"), 25);

		// System.out.println("map:"+map);
		// System.out.println("result:"+result);
		assertEquals("Rimuovo componenti maggiori con elementi consistenti della base:\n", this.expected, this.actual);
	}

	/**
	 * 
	 */
	@Test
	public final void caso20141128() {
		this.actual.clear();
		this.actual.put(Label.parse("bgp"), 10);
		this.actual.put(Label.parse("cp"), -1);
		// System.out.println(map);

		assertEquals("{(10, bgp) (-1, cp) }", this.actual.toString());
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void casoBaseConValoriDistinti() {
		this.actual.clear();
		this.actual.put(Label.parse("a"), 30);
		this.actual.put(Label.parse("¬a"), 25);
		this.actual.put(Label.emptyLabel, 31);
		this.actual.put(Label.parse("¬ab"), 24);
		this.actual.put(Label.parse("¬a¬b"), 30);
		this.actual.put(Label.parse("ab"), 23);

		this.expected.clear();
		this.expected.put(Label.parse("a"), 30);
		this.expected.put(Label.parse("¬a"), 25);
		this.expected.put(Label.parse("ab"), 23);
		this.expected.put(Label.parse("¬ab"), 24);
		this.expected.put(Label.emptyLabel, 31);

		// System.out.printf("map:"+map);
		assertEquals("Base con valori differenti:\n", this.expected, this.actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void equals() {
		this.actual.clear();
		this.actual.put(Label.parse("¬a¬b"), 30);
		this.actual.put(Label.parse("¬ab"), 24);
		this.actual.put(Label.parse("ab"), 23);
		this.actual.put(Label.parse("¬a"), 25);
		this.actual.put(Label.parse("a"), 30);
		this.actual.put(Label.emptyLabel, 31);

		this.expected = new LabeledIntMapStub();
		// in case of algorithm simplifies always two labels that differ for only one opposite literal, then the expected is ⊡, 30, ¬a,25 b,24 ab,23
		// in case of algorithm simplifies two labels that differ for only one opposite literal only when they have same value, then the expected is ¬a,25 ¬ab,
		// 24 a,30 ab,23
		this.expected.put(Label.emptyLabel, 30);
		// expected.put(Label.parse("a"), 30);
		this.expected.put(Label.parse("¬a"), 25);
		this.expected.put(Label.parse("ab"), 23);
		this.expected.put(Label.parse("b"), 24);
		// expected.put(Label.parse("¬ab"), 24);

		assertEquals("Base con valori differenti:\n", "{(25, ¬a) (24, ¬ab) (30, a) (23, ab) }", this.actual.toString());
		// assertTrue("Test di equals con un'altra classe che implementa l'interfaccia:\nexpected: " + expected + "\nactual: " + actual,
		// expected.equals(actual));
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void unknown() {
		this.actual.clear();
		this.actual.put(Label.parse("¬a¬b"), 30);
		this.actual.put(Label.parse("¬ab"), 24);
		this.actual.put(Label.parse("¿ab"), 24);
		this.actual.put(Label.parse("¿a¬b"), 21);
		this.actual.put(Label.parse("ab"), 23);
		this.actual.put(Label.parse("¬a"), 25);
		this.actual.put(Label.parse("a"), 24);
		this.actual.put(Label.emptyLabel, 31);

		assertEquals("Base con valori differenti:\n", "{(25, ¬a) (24, ¬ab) (24, a) (23, ab) (21, ¿a¬b) }", this.actual.toString());
		// assertTrue("Test di equals con un'altra classe che implementa l'interfaccia:\nexpected: " + expected + "\nactual: " + actual,
		// expected.equals(actual));
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void equals1() {
		this.actual.clear();
		this.actual.put(Label.parse("¬a¬b"), 30);
		this.actual.put(Label.parse("¬ab"), 24);
		this.actual.put(Label.parse("ab"), 23);
		this.actual.put(Label.parse("¬a"), 25);
		this.actual.put(Label.parse("a"), 30);
		this.actual.put(Label.parse("c"), 32);
		this.actual.put(Label.emptyLabel, 31);

		assertEquals("{(25, ¬a) (24, ¬ab) (30, a) (23, ab) }", this.actual.toString());
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void differentValuesWithBase() {
		this.actual.clear();
		this.actual.put(Label.parse("¬a"), 25);
		this.actual.put(Label.parse("a"), 30);
		this.actual.put(Label.parse("b"), 2);
		this.actual.put(Label.parse("c"), 25);
		this.actual.put(Label.parse("¬ae"), 4);
		this.actual.put(Label.parse("¿ac"), 4);
		this.actual.put(Label.parse("d"), 31);

		assertEquals("{(25, ¬a) (4, ¬ae) (30, a) (4, ¿ac) (2, b) (25, c) }", this.actual.toString());
	}

	/**
	 * Check the kind of bundle result returned.
	 */
	@Test
	public final void checkIntSet() {
		this.actual.clear();
		this.actual.put(Label.parse("a"), 30);
		this.actual.put(Label.parse("¬a"), 25);
		this.actual.put(Label.emptyLabel, 31);
		this.actual.put(Label.parse("¬ab"), 24);
		this.actual.put(Label.parse("¬a¬b"), 30);
		this.actual.put(Label.parse("ab"), 23);

		ObjectArraySet<Entry<Label>> set = (ObjectArraySet<Entry<Label>>) this.actual.entrySet();
		// System.out.println(set);
		try {
			for (Iterator<Entry<Label>> ite = set.iterator(); ite.hasNext();) {
				Entry<Label> e = ite.next();
				e.setValue(1);
			}
		} catch (java.lang.UnsupportedOperationException e) {
			assertTrue(e != null);
			return;
		}
		assertEquals(set, this.actual.entrySet());
		// System.out.println("Map: "+map);
		// System.out.println("Set: "+set);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void putAllTest() {
		this.actual.clear();
		this.actual.put(Label.parse("a"), 30);
		this.actual.put(Label.parse("¬a"), 25);

		LabeledIntMapStub result = new LabeledIntMapStub();
		result.clear();
		result.put(Label.parse("b"), 30);
		result.put(Label.parse("¬b"), 25);

		this.actual1.clear();
		this.actual1.put(Label.parse("c"), 30);
		this.actual1.put(Label.parse("¬c"), 25);

		this.actual.putAll(result);
		assertEquals("Put all da un oggetto di una classe diversa che implementa la stessa interfaccia",
				AbstractLabeledIntMap.parse("{(30, a) (25, ¬a) (30, b) (25, ¬b) }"), this.actual);
		this.actual.putAll(this.actual1);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void putACleaningValueTest() {
		this.actual.clear();
		this.actual.put(Label.parse("a"), 30);
		this.actual.put(Label.parse("¬a"), 25);
		this.actual.put(Label.emptyLabel, 0);

		this.actual1 = new LabeledIntTreeMap(this.actual);
		this.actual1.put(Label.emptyLabel, 0);

		assertEquals("Put forcibly with a base", this.actual, this.actual1);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void parseTest() {
		this.actual = AbstractLabeledIntMap.parse("{(30, a) (25, ¬a) (30, b) (25, ¬b) }");

		this.expected.clear();
		this.expected.put(Label.parse("a"), 30);
		this.expected.put(Label.parse("¬a"), 25);
		this.expected.put(Label.parse("b"), 30);
		this.expected.put(Label.parse("¬b"), 25);
		assertEquals("Parse test", this.expected, this.actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void minValue() {
		this.actual.clear();
		this.actual.put(Label.parse("a"), 0);
		this.actual.put(Label.parse("¬a"), 1);

		// System.out.println(map.toString());
		assertTrue("Test min value", this.actual.getMinValue() == 0);
		this.actual1.clear();

		assertTrue(this.actual1.getMinValue() == Constants.INT_NULL);

		this.actual.clear();
		this.actual.put(Label.emptyLabel, 109);
		this.actual.put(Label.parse("ab"), 10);
		this.actual.put(Label.parse("a"), Constants.INT_POS_INFINITE);
		this.actual.put(Label.parse("¬b"), 23);
		this.actual.put(Label.parse("b"), 22);
		this.actual.put(Label.parse("a¬b"), Constants.INT_NULL);

		assertEquals("Min value: ", 10, this.actual.getMinValue());

		this.actual.put(Label.parse("ab"), Constants.INT_NEG_INFINITE);

		assertEquals("Min value: ", Constants.INT_NEG_INFINITE, this.actual.getMinValue());
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void minValueConsistentWithTest() {
		this.actual.clear();
		this.actual.put(Label.parse("a"), 0);
		this.actual.put(Label.parse("¬a"), 1);

		assertTrue(this.actual.getMinValueConsistentWith(Label.parse("¬a")) == 1);
	}

	/**
	 * Check the sum of labeled value.
	 */
	@Test
	public final void simplificationWithInfinite() {
		this.actual.clear();
		this.actual.put(Label.emptyLabel, 109);
		this.actual.put(Label.parse("ab"), 10);
		this.actual.put(Label.parse("a"), Constants.INT_POS_INFINITE);
		this.actual.put(Label.parse("¬b"), 23);
		this.actual.put(Label.parse("b"), 22);
		this.actual.put(Label.parse("a¬b"), Constants.INT_NULL);
		this.actual.put(Label.parse("ab"), Constants.INT_NEG_INFINITE);

		// System.out.println(map);
		this.expected = AbstractLabeledIntMap.parse("{(23, ¬b) (22, b) }");
		this.expected.put(Label.parse("ab"), Constants.INT_NEG_INFINITE);
		this.expected.put(Label.emptyLabel, 109);
		assertEquals("Test about simplification with infinity numbers:\n", this.expected, this.actual);
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

		this.actual.clear();
		this.actual.put(ab, 10);
		this.actual.put(abUp, 10);
		assertEquals("Test about simplification with unknown labeled numbers:\n", AbstractLabeledIntMap.parse("{(10, ab) }"), this.actual);

		this.actual.clear();
		this.actual.put(abp, 10);
		this.actual.put(abUp, 10);
		assertEquals("Test about simplification with unknown labeled numbers:\n", AbstractLabeledIntMap.parse("{(10, abp) }"), this.actual);

		this.actual.clear();
		this.actual.put(abp, Constants.INT_NEG_INFINITE);
		this.actual.put(abUp, Constants.INT_NEG_INFINITE);
		this.actual1.clear();
		this.actual1.put(abp, Constants.INT_NEG_INFINITE);
		assertEquals("Test about simplification with unknown labeled numbers:\n", this.actual1, this.actual);

		this.actual.clear();
		this.actual.put(abp, 10);
		this.actual.put(abNp, 9);
		this.actual.put(abUp, 9);
		this.expected = AbstractLabeledIntMap.parse("{(10, abp) (9, ab¬p) }");
		assertEquals("Test about simplification with unknown labeled numbers:\n", this.expected, this.actual);

		this.actual.clear();
		this.actual.put(abUp, 9);
		this.actual.put(abp, 10);
		this.actual.put(abNp, 9);
		assertEquals("Test about simplification with unknown labeled numbers:\n", AbstractLabeledIntMap.parse("{(10, abp) (9, ab¬p) }"), this.actual);

		this.actual.clear();
		this.actual.put(abp, 10);
		this.actual.put(abNp, 9);
		this.actual.put(abUp, 8);
		assertEquals("Test about simplification with unknown labeled numbers:\n", AbstractLabeledIntMap.parse("{(10, abp) (9, ab¬p) (8, ab¿p) }"),
				this.actual);
	}

	/**
	 * Check the sum of labeled value.
	 */
	@Test
	public final void simplificationWithUnknown1() {
		Label Upab = Label.parse("¿p¿ab");
		Label Upr = Label.parse("¿pr");
		Label UpNr = Label.parse("¿p¬r");

		this.actual.clear();
		this.actual.put(Upab, -24);
		this.actual.put(Upr, Constants.INT_NEG_INFINITE);
		// System.out.println("Before introduction UpNr: "+ map);
		this.actual.put(UpNr, Constants.INT_NEG_INFINITE);
		assertEquals("Test about simplification with unknown labeled infinity:\n", "{(-∞, ¿p) }", this.actual.toString());
	}

	/**
	 * Test merge 20160320
	 */
	@Test
	public final void testMerge20160320() {
		this.actual.clear();
		this.actual.put(Label.parse("¬b"), 8);
		this.actual.put(Label.parse("¬ab"), -2);
		this.actual.put(Label.parse("b"), -1);
		this.actual.put(Label.parse("¿b"), -11);

		this.expected.clear();
		// assertEquals("{(8, ⊡) (-2, ¬ab) (-1, b) (-11, ¿b) }", actual.toString());
		assertEquals("{(-2, ¬ab) (8, ¬b) (-1, b) (-11, ¿b) }", this.actual.toString());
	}

	/**
	 * Test merge 20160408
	 */
	@Test
	public final void testMerge20160408() {

		this.actual.clear();
		this.actual.put(Label.emptyLabel, 109);
		this.actual.put(Label.parse("abc¬f"), 10);
		this.actual.put(Label.parse("abcdef"), 20);
		this.actual.put(Label.parse("a¬bc¬de¬f"), 25);
		assertEquals("{(109, ⊡) (25, a¬bc¬de¬f) (20, abcdef) (10, abc¬f) }", this.actual.toString());

		this.actual.put(Label.parse("¬b¬d¬f"), 23);// toglie (25, a¬bc¬de¬f)
		this.actual.put(Label.parse("ec"), 22);
		this.actual.put(Label.parse("¬fedcba"), 23);// non serve a nulla
		assertEquals("{(109, ⊡) (20, abcdef) (10, abc¬f) (23, ¬b¬d¬f) (22, ce) }", this.actual.toString());

		this.actual.put(Label.parse("ae¬f"), 20);
		this.actual.put(Label.parse("¬af¿b"), 20);
		this.actual.put(Label.parse("¬af¿b"), 21);
		assertEquals("{(109, ⊡) (20, ¬a¿bf) (20, abcdef) (10, abc¬f) (20, ae¬f) (23, ¬b¬d¬f) (22, ce) }", this.actual.toString());

		this.actual.put(Label.parse("¬ec"), 11);
		this.actual.put(Label.parse("abd¿f"), 11);
		this.actual.put(Label.parse("a¿d¬f"), 11);
		assertEquals("{(109, ⊡) (20, ¬a¿bf) (20, abcdef) (10, abc¬f) (11, abd¿f) (11, a¿d¬f) (20, ae¬f) (23, ¬b¬d¬f) (11, c¬e) (22, ce) }",
				this.actual.toString());

		this.actual.put(Label.parse("¬b¿d¿f"), 24);// non inserito perché c'è (23, ¬b¬d¬f)
		this.actual.put(Label.parse("b¬df¿e"), 22);
		this.actual.put(Label.parse("e¬c"), 23);
		assertEquals(
				"{(109, ⊡) (20, ¬a¿bf) (20, abcdef) (10, abc¬f) (11, abd¿f) (11, a¿d¬f) (20, ae¬f) (23, ¬b¬d¬f) (22, b¬d¿ef) (23, ¬ce) (11, c¬e) (22, ce) }",
				this.actual.toString());

		this.actual.put(Label.parse("ab¿d¿f"), 20);// non iserito perché c'è (11, abd¿f)
		this.actual.put(Label.parse("ad¬f"), 23);
		this.actual.put(Label.parse("b¿d¿f"), 23);
		assertEquals(
				"{(109, ⊡) (20, ¬a¿bf) (20, abcdef) (10, abc¬f) (11, abd¿f) (23, ad¬f) (11, a¿d¬f) (20, ae¬f) (23, ¬b¬d¬f) (22, b¬d¿ef) (23, b¿d¿f) (23, ¬ce) (11, c¬e) (22, ce) }",
				this.actual.toString());

		this.actual.put(Label.parse("¬b¬d¿ef"), 23);
		assertEquals(
				"{(109, ⊡) (20, ¬a¿bf) (20, abcdef) (10, abc¬f) (11, abd¿f) (23, ad¬f) (11, a¿d¬f) (20, ae¬f) (23, ¬b¬d¿ef) (23, ¬b¬d¬f) (22, b¬d¿ef) (23, b¿d¿f) (23, ¬ce) (11, c¬e) (22, ce) }",
				this.actual.toString());

		this.actual.put(Label.parse("¬e¬c"), 19);

		assertEquals("{(20, ¬a¿bf) (20, abcdef) (10, abc¬f) (11, abd¿f) (11, a¿d¬f) (20, ae¬f) (22, b¬d¿ef) (19, ¬c¬e) (23, ¬ce) (11, c¬e) (22, ce) }",
				this.actual.toString());
	}
}
