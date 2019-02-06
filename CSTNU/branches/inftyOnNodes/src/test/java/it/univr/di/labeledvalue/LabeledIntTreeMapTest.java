/**
 * 
 */
package it.univr.di.labeledvalue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Assert;
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

		@Override
		public int getMinValueSubsumedBy(Label l) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getMaxValue() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean alreadyRepresents(Label newLabel, int newValue) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEmpty() {
			return this.size() == 0;
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
		Assert.assertEquals(0, this.actual.size());
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
		Assert.assertEquals(2, this.actual.size());
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
		Assert.assertEquals(1, this.actual.size());
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
		Assert.assertEquals(3, this.actual.size());
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void cicloConCancellazione() {
		this.actual.clear();
		this.actual.put(Label.parse("a"), 20);
		this.actual.put(Label.parse("¬b"), 21);
		this.actual.put(Label.parse("ab"), 10);
		this.actual.put(Label.parse("a¬b"), 15);
		this.actual.put(Label.parse("¬ab"), 12);

		LabeledIntTreeMap copy = new LabeledIntTreeMap(this.actual);

		ObjectSet<Label> keys = copy.keySet();
		assertEquals("[a, ¬b, ab, a¬b, ¬ab]", Arrays.toString(keys.toArray()));

		for (Label l : copy.keySet()) {
			if (l.toString().equals("¬b") || l.toString().equals("ab")) {
				copy.remove(l);
				continue;
			}
		}
		assertEquals("[a, ¬b, ab, a¬b, ¬ab]", Arrays.toString(keys.toArray()));
		assertEquals("{(20, a) (15, a¬b) (12, ¬ab) }", copy.toString());

		copy = new LabeledIntTreeMap(this.actual);

		StringBuffer sb = new StringBuffer();
		for (Entry<Label> e : copy.entrySet()) {
			if (e.getKey().toString().equals("¬b") || e.getKey().toString().equals("ab")) {
				copy.put(e.getKey(), 15);
			}
			if (e.getKey().toString().equals("¬b") || e.getKey().toString().equals("ab")) {
				sb.append(e.getIntValue() + ", ");
			}
		}
		assertEquals("21, 10, ", sb.toString());
		assertEquals("{(20, a) (15, ¬b) (10, ab) (12, ¬ab) }", copy.toString());
		Assert.assertEquals(4, copy.size());
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
		Assert.assertEquals(3, this.actual.size());
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
		Assert.assertEquals(2, this.actual.size());
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
		Assert.assertEquals(1, this.actual.size());

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
		Assert.assertEquals(1, this.actual.size());
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

		assertEquals("{(-1, cp) (10, bgp) }", this.actual.toString());
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

		assertEquals("Base con valori differenti:\n", "{(30, a) (25, ¬a) (23, ab) (24, ¬ab) }", this.actual.toString());
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

		assertEquals("Base con valori differenti:\n", "{(24, a) (25, ¬a) (23, ab) (24, ¬ab) (21, ¿a¬b) }", this.actual.toString());
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

		assertEquals("{(30, a) (25, ¬a) (23, ab) (24, ¬ab) }", this.actual.toString());
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

		assertEquals("{(30, a) (25, ¬a) (2, b) (25, c) (4, ¬ae) (4, ¿ac) }", this.actual.toString());
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
	public final void parseLongMapTest() {
		String longString = "{(0, ⊡) (-9, ¿a) (-1, ¬a¿b) (-9, ¿ab) (-11, ¿a¿d) (-10, ¿a¿f) (-10, ¿a¬i) (-1, abd) (-4, a¿d¬g) (-6, a¿d¿g) (-5, ¬a¿b¿h) (-13, ¿ab¿d) (-13, ¿ab¿f) (-13, ¿ab¿h) (-13, ¿ab¬i) (-33, ¿a¿b¿d) (-10, ¿a¿b¬g) (-7, ¿a¬ce) (-10, ¿a¬e¬i) (-10, ¿ah¿i) (-3, abd¬g) (-4, abd¿h) (-6, ab¿d¿g) (-6, a¿bd¿g) (-6, a¿c¿d¬g) (-9, a¿d¿g¬i) (-10, a¿d¿g¿i) (-5, ¬a¿b¿fh) (-6, ¬a¿b¿hi) (-10, ¿ab¬c¬e) (-10, ¿ab¬c¬f) (-10, ¿ab¬ch) (-13, ¿abd¬e) (-13, ¿abdf) (-13, ¿abdh) (-20, ¿abd¿h) (-20, ¿ab¿dh) (-20, ¿ab¿d¿i) (-13, ¿ab¬ef) (-13, ¿ab¬e¿h) (-13, ¿abfh) (-13, ¿ab¬f¿h) (-13, ¿ab¬f¿i) (-17, ¿a¬b¿d¿f) (-19, ¿a¿bdf) (-20, ¿a¿bd¿f) (-20, ¿a¿bdh) (-19, ¿a¿bd¿i) (-33, ¿a¿b¿d¬h) (-18, ¿a¿b¿d¬i) (-10, ¿a¿b¬e¬g) (-13, ¿a¿bhi) (-33, ¿a¿c¿d¿e) (-19, ¿a¿c¿di) (-13, ¿a¿c¿e¿f) (-11, ¿a¿c¿e¿i) (-13, ¿a¿c¿f¿i) (-10, ¿adfh) (-19, ¿a¿d¿f¿i) (-1, ¿b¬dei) (-3, abde¬g) (-3, abd¬e¬i) (-4, abd¬f¿h) (-6, abd¿g¿h) (-6, a¿bde¿g) (-7, a¿bd¿e¿g) (-9, a¿bd¿g¬i) (-10, a¿bd¿g¿i) (-3, a¿b¬d¿e¿i) (-6, ac¿d¬gh) (-11, a¬c¿d¿g¿i) (-10, a¿c¿d¬g¬i) (-1, a¬de¬g¿i) (-4, a¬d¿e¬g¿i) (-8, a¬d¿e¿g¿i) (-1, ¬a¬b¬d¿ei) (-8, ¬a¿b¿efi) (-8, ¬a¿b¿e¬hi) (-6, ¬a¿b¿fhi) (-8, ¬a¿bg¿hi) (-10, ¿ab¬ce¬h) (-33, ¿ab¿c¿dh) (-33, ¿ab¿c¿d¿i) (-11, ¿ab¿ce¬h) (-33, ¿ab¿deh) (-33, ¿ab¿de¿i) (-31, ¿ab¿d¿fh) (-31, ¿ab¿d¿f¿i) (-33, ¿ab¿d¿gh) (-33, ¿ab¿d¿g¿i) (-33, ¿ab¿dh¿i) (-10, ¿ab¬e¬f¬h) (-11, ¿ab¿e¬f¬h) (-11, ¿a¬b¬d¿e¿i) (-19, ¿a¬b¿de¿f) (-20, ¿a¬b¿d¿e¿f) (-19, ¿a¬b¿df¿i) (-19, ¿a¬b¿d¿f¬i) (-25, ¿a¿bcd¿f) (-13, ¿a¿bchi) (-20, ¿a¿b¿cdf) (-17, ¿a¿b¿cd¬i) (-20, ¿a¿b¿cd¿i) (-20, ¿a¿bd¿ef) (-25, ¿a¿bd¿e¿f) (-20, ¿a¿bd¿e¿i) (-20, ¿a¿bdf¬g) (-20, ¿a¿bdf¿i) (-25, ¿a¿bd¿f¿i) (-20, ¿a¿bd¬g¿i) (-13, ¿a¿b¬d¿ei) (-14, ¿a¿b¬d¿hi) (-15, ¿a¿b¬d¿h¿i) (-40, ¿a¿b¿de¿f) (-20, ¿a¿b¿d¿ei) (-45, ¿a¿b¿d¿fh) (-45, ¿a¿b¿d¿f¿i) (-20, ¿a¿b¿d¬g¬i) (-16, ¿a¿b¿eh¿i) (-10, ¿a¬cde¿h) (-10, ¿a¬cd¿e¿h) (-11, ¿a¬ce¿f¿i) (-11, ¿a¬ce¿h¿i) (-13, ¿a¬c¿e¿f¿i) (-11, ¿a¬c¿e¿g¿i) (-11, ¿a¬c¿eh¿i) (-11, ¿a¬c¿f¿g¿i) (-11, ¿a¿cd¬f¿i) (-11, ¿a¿cd¿f¬i) (-32, ¿a¿c¿deh) (-33, ¿a¿c¿de¿h) (-33, ¿a¿c¿de¿i) (-19, ¿a¿c¿d¿f¬i) (-33, ¿a¿c¿d¿f¿i) (-16, ¿a¿c¿d¿g¿i) (-33, ¿a¿c¿dh¿i) (-11, ¿a¿ce¬f¿i) (-13, ¿a¿ce¿fh) (-11, ¿a¿ce¿f¬i) (-13, ¿a¿c¿f¬h¿i) (-13, ¿ad¿f¿g¿i) (-13, ¿ad¿fh¿i) (-11, ¿a¬d¿e¬f¿i) (-11, ¿a¬d¿e¿f¬h) (-11, ¿a¬d¿e¿f¬i) (-12, ¿a¬d¿e¿f¿i) (-11, ¿a¬d¿e¿g¿i) (-11, ¿a¬d¿eh¿i) (-33, ¿a¿deh¿i) (-19, ¿a¿d¿e¬f¿i) (-13, ¿a¿d¿e¿f¬i) (-33, ¿a¿d¿e¿f¿i) (-17, ¿a¿d¿e¿gh) (-33, ¿a¿d¿e¿g¿i) (-19, ¿a¿df¿g¿i) (-19, ¿a¿dfh¿i) (-33, ¿a¿d¿gh¿i) (-13, ¿ae¿fh¿i) (-13, ¿a¿e¿f¿g¿i) (-13, ¿a¿f¿gh¿i) (-1, ¬b¬c¬dei) (-8, ¬b¬defi) (-12, abcd¬e¿h) (-12, abcdf¬h) (-6, abcd¬g¿h) (-3, ab¬cd¬e¬i) (-11, ab¿cde¬h) (-7, ab¿c¿d¬f¬g) (-12, ab¿c¿d¬g¬i) (-8, abd¬eg¿h) (-9, abd¬e¿g¿h) (-8, abd¿eg¬h) (-9, abd¿e¿g¬h) (-9, abd¿g¬h¬i) (-12, abd¿g¿h¿i) (-7, ab¿d¬e¿f¿g) (-12, ab¿df¿g¿i) (-13, ab¿d¿f¿g¿i) (-3, a¬b¬c¬d¿e¿i) (-7, a¬b¬c¿de¿g) (-7, a¬b¬c¿d¬g¿i) (-16, a¬b¿c¿d¿g¿i) (-8, a¬b¿df¬g¿i) (-16, a¬b¿df¿g¿i) (-7, a¿b¿cde¿g) (-7, a¿b¿cd¿f¿g) (-10, a¿b¿cd¿g¬i) (-7, a¿bd¬e¿f¿g) (-4, a¿b¬de¿hi) (-8, a¿b¬d¿ehi) (-17, a¿b¿d¿e¿g¿i) (-8, a¿b¿d¬gh¿i) (-16, a¿b¿d¿gh¿i) (-6, ac¿de¬gh) (-8, ac¿d¬gh¿i) (-10, ac¿d¬g¿h¬i) (-8, a¿c¬d¿e¬g¿i) (-10, a¿c¬d¿e¿g¿i) (-17, a¿c¿d¿e¿g¿i) (-16, a¿c¿d¬f¿g¿i) (-4, a¬de¬g¿h¿i) (-17, a¿d¿e¿gh¿i) (-16, a¿d¿f¿gh¿i) (-8, ¬a¬b¬d¿e¬hi) (-8, ¬a¿b¿fghi) (-21, ¿abcdf¬h) (-25, ¿abcdf¿h) (-25, ¿abc¿d¿f¬h) (-33, ¿abc¿dgh) (-33, ¿abc¿dg¿i) (-11, ¿ab¬cde¬h) (-11, ¿ab¬c¬d¿e¬h) (-11, ¿ab¬cef¬h) (-45, ¿ab¿c¿d¿fh) (-45, ¿ab¿c¿d¿f¿i) (-25, ¿abdf¿h¿i) (-45, ¿ab¿d¿e¿fh) (-45, ¿ab¿d¿e¿f¿i) (-16, ¿ab¿d¿e¿g¬h) (-31, ¿ab¿df¬g¿i) (-16, ¿ab¿d¿f¬g¬h) (-35, ¿ab¿d¿f¬g¿i) (-45, ¿ab¿d¿f¿gh) (-45, ¿ab¿d¿f¿g¿i) (-45, ¿ab¿d¿fh¿i) (-15, ¿ab¿d¿f¬h¬i) (-11, ¿ab¿e¬g¬hi) (-21, ¿a¬bc¿d¿e¿f) (-10, ¿a¬b¬cd¿e¬g) (-10, ¿a¬b¬ce¬hi) (-10, ¿a¬b¬c¿e¬g¬h) (-13, ¿a¬b¿cd¿f¬i) (-17, ¿a¬b¿c¿df¬i) (-25, ¿a¬b¿c¿d¿f¬i) (-11, ¿a¬b¿ce¿hi) (-11, ¿a¬b¿c¿e¬hi) (-17, ¿a¬b¬defi) (-18, ¿a¬b¬def¿i) (-19, ¿a¬b¬d¿ef¿i) (-12, ¿a¬b¬d¿e¿f¬i) (-20, ¿a¬b¬d¿e¿f¿i) (-10, ¿a¬b¬d¿e¬g¿h) (-21, ¿a¬b¿de¿f¿i) (-13, ¿a¬b¿d¿e¬f¬i) (-21, ¿a¬b¿d¿e¬f¿i) (-20, ¿a¬b¿d¿e¿gh) (-19, ¿a¬b¿d¿fg¿h) (-21, ¿a¬b¿d¿fh¿i) (-14, ¿a¿bc¬dhi) (-15, ¿a¿bc¬dh¿i) (-20, ¿a¿b¬cd¬f¿i) (-14, ¿a¿b¬c¬d¬f¿i) (-11, ¿a¿b¬ce¬hi) (-14, ¿a¿b¬c¿e¬f¿i) (-14, ¿a¿b¬c¬fh¿i) (-40, ¿a¿b¿c¿df¿i) (-25, ¿a¿bd¬e¿f¬g) (-21, ¿a¿b¬def¿i) (-15, ¿a¿b¬de¿hi) (-17, ¿a¿b¬d¿ehi) (-20, ¿a¿b¬d¿eh¿i) (-15, ¿a¿b¬df¿hi) (-15, ¿a¿b¬d¬g¿hi) (-40, ¿a¿b¿defh) (-41, ¿a¿b¿def¿h) (-40, ¿a¿b¿def¿i) (-41, ¿a¿b¿de¿f¬g) (-41, ¿a¿b¿de¿f¬h) (-20, ¿a¿b¿d¬e¬gi) (-41, ¿a¿b¿df¬g¿i) (-40, ¿a¿b¿dfh¿i) (-41, ¿a¿b¿df¿h¿i) (-33, ¿ac¿dgh¿i) (-13, ¿ac¿fgh¿i) (-13, ¿a¬cde¿f¿i) (-11, ¿a¬cdeh¿i) (-11, ¿a¬cd¿gh¿i) (-12, ¿a¬c¿de¬f¿i) (-12, ¿a¬c¿d¬f¿g¿i) (-13, ¿a¬c¬e¿f¿g¿i) (-11, ¿a¿cdgh¿i) (-34, ¿a¿c¿de¿f¿i) (-33, ¿a¿c¿de¬gh) (-45, ¿a¿c¿d¿e¿f¿i) (-45, ¿a¿c¿d¿fh¿i) (-13, ¿a¿c¿e¬f¿g¿i) (-12, ¿a¬d¬e¿f¿g¿i) (-13, ¿a¿d¬e¬f¿g¿i) (-13, ¿a¿d¬e¬fh¿i) (-45, ¿a¿d¿e¿f¿g¿i) (-45, ¿a¿d¿e¿fh¿i) (-17, ¿a¿d¿f¿gh¬i) (-45, ¿a¿d¿f¿gh¿i) (-8, ¬b¬c¬de¬hi) (-3, abcd¬eh¬i) (-12, abcd¬g¿h¬i) (-7, ab¬c¿d¬e¬f¿g) (-7, ab¬c¿d¬e¿gh) (-12, ab¬c¿d¿e¿g¿i) (-12, ab¬c¿d¬f¿g¿i) (-12, ab¬c¿d¿gh¿i) (-11, ab¿cd¬g¬h¬i) (-7, ab¿c¿d¬e¬gh) (-8, abd¬efg¬h) (-9, abd¬ef¿g¬h) (-9, abd¿eg¬h¿i) (-12, abd¿eg¿hi) (-7, ab¿d¬e¬f¿gh) (-8, a¬bc¿d¬gh¿i) (-7, a¬b¬c¬d¿e¬g¿i) (-11, a¬b¬c¬d¿e¿g¿i) (-8, a¬b¬c¿d¬g¬h¿i) (-16, a¬b¬d¿ef¿g¿i) (-14, a¬b¿def¿gi) (-17, a¬b¿def¿g¿i) (-7, a¿b¬cd¬e¬f¿g) (-7, a¿b¬cd¬e¿gh) (-13, a¿b¬c¿d¬f¿g¿i) (-10, a¿b¿cde¿g¬i) (-13, a¿b¿c¬d¿e¿g¿i) (-7, a¿bd¬e¬f¿gh) (-16, a¿b¬d¿efg¿i) (-16, a¿b¬d¿egh¿i) (-8, a¿b¬d¿eg¬hi) (-17, a¿b¿d¿efg¿i) (-17, a¿b¿d¿egh¿i) (-14, a¿b¿d¿e¿ghi) (-8, ac¬d¿e¬gh¿i) (-10, ac¬d¿e¿gh¿i) (-13, a¬c¿d¿e¿f¿g¿i) (-8, a¿c¬de¬g¿h¿i) (-16, a¿c¿de¿gh¿i) (-4, a¬de¿f¬gh¿i) (-25, ¿abcdf¬h¿i) (-20, ¿abc¿d¿e¿g¬h) (-27, ¿abc¿df¿hi) (-45, ¿abc¿d¿fgh) (-45, ¿abc¿d¿fg¿i) (-31, ¿ab¿c¿d¿f¬g¬i) (-25, ¿abd¿ef¿hi) (-18, ¿abd¿f¬g¬h¿i) (-21, ¿ab¿d¬e¬f¿h¬i) (-15, ¿ab¿d¿e¬f¬h¬i) (-31, ¿ab¿df¬gh¬i) (-16, ¿ab¿df¿g¬h¬i) (-21, ¿a¬bc¬d¿e¿f¿i) (-18, ¿a¬bc¿d¿fgh) (-10, ¿a¬b¬cdef¬g) (-10, ¿a¬b¬cde¬gh) (-10, ¿a¬b¬c¬de¬fi) (-11, ¿a¬b¬c¬de¬f¿i) (-11, ¿a¬b¬c¬de¬hi) (-13, ¿a¬b¿c¬d¿e¬f¿i) (-19, ¿a¬b¿c¿def¬i) (-12, ¿a¬b¿c¿de¬f¬i) (-12, ¿a¬b¬d¿e¬f¿g¿i) (-12, ¿a¬b¬d¿e¬fh¿i) (-11, ¿a¬b¬d¿e¬f¬hi) (-11, ¿a¬b¬d¿e¬g¿hi) (-17, ¿a¬b¿df¿gh¬i) (-19, ¿a¬b¿df¿g¿h¬i) (-15, ¿a¿bc¬dehi) (-27, ¿a¿bc¬d¿e¿f¿i) (-15, ¿a¿bc¬dfhi) (-15, ¿a¿bc¬d¬ghi) (-14, ¿a¿b¬c¬d¬eh¿i) (-41, ¿a¿b¿c¿df¬h¿i) (-23, ¿a¿b¬def¿h¿i) (-14, ¿a¿b¬d¬e¬f¬h¿i) (-25, ¿a¿b¬d¿ef¿h¿i) (-23, ¿a¿b¬d¿e¿f¬h¿i) (-41, ¿a¿b¿def¬gh) (-41, ¿a¿b¿def¬h¿i) (-45, ¿a¿b¿de¿f¬g¿i) (-14, ¿a¿b¿e¬f¬g¬h¿i) (-12, ¿ac¬d¬e¿fg¿i) (-13, ¿ac¿d¬e¬fg¿i) (-45, ¿ac¿d¿fgh¿i) (-13, ¿a¬cde¿f¬h¿i) (-13, ¿a¬c¬e¿f¿g¬h¿i) (-13, ¿a¿ce¬f¿gh¿i) (-12, ¿a¬d¬e¿fgh¿i) (-12, ¿a¬d¿e¿f¿gh¬i) (-12, abc¿d¿e¿g¬h¿i) (-9, ab¬cd¿eg¬hi) (-9, abd¿efg¬hi) (-17, a¬bc¬d¿e¿fg¿i) (-5, a¬b¬c¿de¬f¬gi) (-11, a¬b¬c¿de¿g¬hi) (-13, a¬b¬c¿d¿e¬f¿g¿i) (-11, a¬b¿c¬d¿efg¿i) (-17, a¬b¿c¬d¿ef¿g¿i) (-11, a¬b¿c¬d¿eg¬h¿i) (-7, a¬b¿c¿de¬f¬gi) (-17, a¿bc¬d¿egh¿i) (-11, a¿b¬c¬d¿eg¬h¿i) (-8, ac¬de¿f¬gh¿i) (-17, a¿c¿de¿f¿gh¿i) (-25, ¿abcd¿e¿f¬hi) (-31, ¿abc¿de¿f¬hi) (-23, ¿ab¿cd¿ef¬hi) (-23, ¿ab¿c¿def¬hi) (-25, ¿ab¿c¿df¬g¬h¬i) (-14, ¿abd¬e¿f¬g¬hi) (-25, ¿abd¬e¿f¬g¿hi) (-16, ¿abd¿e¿f¬g¬hi) (-25, ¿ab¿d¬e¬f¬g¿hi) (-22, ¿ab¿d¬e¬f¬g¿h¬i) (-16, ¿ab¿d¿e¬f¬g¬hi) (-18, ¿a¬bc¿df¿gh¬i) (-14, ¿a¬b¬c¿d¿e¬f¿g¬i) (-13, ¿a¬b¿c¬de¬fh¿i) (-18, ¿a¬b¿c¿dfgh¬i) (-19, ¿a¬b¿c¿dfg¿h¬i) (-19, ¿a¬b¿def¿gh¬i) (-22, ¿a¿bcdf¬g¬hi) (-23, ¿a¿bc¬defh¿i) (-24, ¿a¿bc¬d¿efh¿i) (-27, ¿a¿bc¬d¿ef¿h¿i) (-26, ¿a¿b¿c¬d¿efh¿i) (-27, ¿a¿b¬d¿ef¬g¿h¿i) (-24, ¿a¿b¬d¿e¿f¬g¬h¿i) (-12, ¿ac¬d¬e¿fg¬h¿i) (-17, a¬bc¬d¿ef¿gh¿i) (-17, a¬b¿c¬d¿efg¬h¿i) (-23, ¿abcd¿ef¬g¬hi) }";
		// System.out.println("Length: " + longString.length());
		this.actual = AbstractLabeledIntMap.parse(longString);

		assertNotNull(this.actual);
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
		assertEquals("{(-1, b) (8, ¬b) (-11, ¿b) (-2, ¬ab) }", this.actual.toString());
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
		assertEquals("{(109, ⊡) (10, abc¬f) (20, abcdef) (25, a¬bc¬de¬f) }", this.actual.toString());

		this.actual.put(Label.parse("¬b¬d¬f"), 23);// toglie (25, a¬bc¬de¬f)
		this.actual.put(Label.parse("ec"), 22);
		this.actual.put(Label.parse("¬fedcba"), 23);// non serve a nulla
		assertEquals("{(109, ⊡) (22, ce) (23, ¬b¬d¬f) (10, abc¬f) (20, abcdef) }", this.actual.toString());

		this.actual.put(Label.parse("ae¬f"), 20);
		this.actual.put(Label.parse("¬af¿b"), 20);
		this.actual.put(Label.parse("¬af¿b"), 21);
		assertEquals("{(109, ⊡) (22, ce) (20, ae¬f) (20, ¬a¿bf) (23, ¬b¬d¬f) (10, abc¬f) (20, abcdef) }", this.actual.toString());

		this.actual.put(Label.parse("¬ec"), 11);
		this.actual.put(Label.parse("abd¿f"), 11);
		this.actual.put(Label.parse("a¿d¬f"), 11);
		assertEquals("{(109, ⊡) (22, ce) (11, c¬e) (11, a¿d¬f) (20, ae¬f) (20, ¬a¿bf) (23, ¬b¬d¬f) (10, abc¬f) (11, abd¿f) (20, abcdef) }",
				this.actual.toString());

		this.actual.put(Label.parse("¬b¿d¿f"), 24);// non inserito perché c'è (23, ¬b¬d¬f)
		this.actual.put(Label.parse("b¬df¿e"), 22);
		this.actual.put(Label.parse("e¬c"), 23);
		assertEquals(
				"{(109, ⊡) (22, ce) (11, c¬e) (23, ¬ce) (11, a¿d¬f) (20, ae¬f) (20, ¬a¿bf) (23, ¬b¬d¬f) (10, abc¬f) (11, abd¿f) (22, b¬d¿ef) (20, abcdef) }",
				this.actual.toString());

		this.actual.put(Label.parse("ab¿d¿f"), 20);// non iserito perché c'è (11, abd¿f)
		this.actual.put(Label.parse("ad¬f"), 23);
		this.actual.put(Label.parse("b¿d¿f"), 23);
		assertEquals(
				"{(109, ⊡) (22, ce) (11, c¬e) (23, ¬ce) (23, ad¬f) (11, a¿d¬f) (20, ae¬f) (20, ¬a¿bf) (23, b¿d¿f) (23, ¬b¬d¬f) (10, abc¬f) (11, abd¿f) (22, b¬d¿ef) (20, abcdef) }",
				this.actual.toString());

		this.actual.put(Label.parse("¬b¬d¿ef"), 23);
		assertEquals(
				"{(109, ⊡) (22, ce) (11, c¬e) (23, ¬ce) (23, ad¬f) (11, a¿d¬f) (20, ae¬f) (20, ¬a¿bf) (23, b¿d¿f) (23, ¬b¬d¬f) (10, abc¬f) (11, abd¿f) (22, b¬d¿ef) (23, ¬b¬d¿ef) (20, abcdef) }",
				this.actual.toString());

		this.actual.put(Label.parse("¬e¬c"), 19);

		assertEquals("{(22, ce) (11, c¬e) (23, ¬ce) (19, ¬c¬e) (11, a¿d¬f) (20, ae¬f) (20, ¬a¿bf) (10, abc¬f) (11, abd¿f) (22, b¬d¿ef) (20, abcdef) }",
				this.actual.toString());
		Assert.assertEquals(11, this.actual.size());
	}

	/**
	 * Main.
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	static public void main(final String[] args) {
		testEntrySet();
		testConstructor();
	}

	/**
	 * Simple test to verify time cost of entrySet() vs. keySet().
	 * On 2017-10-26 it resulted that there is no appreciable difference between accessing to the map using entrySet() & its get()
	 * and accessing to the map using keySet() and, then, find().
	 */
	private static void testEntrySet() {
		final int nTest = (int) 1E2;
		LabeledIntTreeMap map = new LabeledIntTreeMap();
		SummaryStatistics entrySetStats = new SummaryStatistics(), keySetStats = new SummaryStatistics();
		int[] value = { 5, 10, 25, 50, 100, 1000 };
		int nChar = 'z' - 'a' + 1;
		char[] chars = new char[nChar];
		for (int j = 'a'; j <= 'z'; j++)
			chars[j - 'a'] = (char) j;

		for (int k = 0; k < value.length; k++) {
			int nLabel = value[k];
			for (int i = 0; i < nLabel; i++) {
				Label label = Label.emptyLabel;
				int l = 1;
				for (int j = 0; j < nChar; j++, l = l << 1) {
					label.conjunction(chars[j], ((i & l) != 0 ? Literal.STRAIGHT : Literal.NEGATED));
				}
				map.put(label, i);
			}
			// System.out.println("Map size: " + map);
			testEntrySetTime(map, nTest, entrySetStats, keySetStats);
			System.out.println("Time to retrieve " + map.size() + " elements using entrySet(): " + entrySetStats.getMean() + "ms");
			System.out.println("Time to retrieve " + map.size() + " elements using keySet(): " + keySetStats.getMean() + "ms");
			System.out.println("The difference is " + (entrySetStats.getMean() - keySetStats.getMean()) + " ms. It is better to use: "
					+ ((entrySetStats.getMean() < keySetStats.getMean()) ? "entrySet()" : "keySet()") + " approach.\n");
		}
	}

	/**
	 * @param map
	 * @param nTest
	 * @param entrySetStats
	 * @param keySetStats
	 */
	private static void testEntrySetTime(LabeledIntTreeMap map, int nTest, SummaryStatistics entrySetStats, SummaryStatistics keySetStats) {
		entrySetStats.clear();
		keySetStats.clear();
		Instant startInstant;

		for (int i = 0; i < nTest; i++) {
			startInstant = Instant.now();
			for (Entry<Label> entry : map.entrySet()) {
				entry.getKey();
				entry.getIntValue();
			}
			Instant endInstant = Instant.now();
			entrySetStats.addValue(Duration.between(startInstant, endInstant).toNanos() / 10E6);
		}

		for (int i = 0; i < nTest; i++) {
			startInstant = Instant.now();
			for (Label l : map.keySet()) {
				map.get(l);
			}
			Instant endInstant = Instant.now();
			keySetStats.addValue(Duration.between(startInstant, endInstant).toNanos() / 10E6);
		}

	}

	/**
	 * Simple test to verify time cost of constructor.
	 */
	private static void testConstructor() {

		final int nTest = (int) 1E3;
		final double msNorm = 1.0E6 * nTest;

		final LabeledIntMap map = new LabeledIntTreeMap();

		final Label l1 = Label.parse("abc¬f");
		final Label l2 = Label.parse("abcdef");
		final Label l3 = Label.parse("a¬bc¬de¬f");
		final Label l4 = Label.parse("¬b¬d¬f");
		final Label l5 = Label.parse("ec");
		final Label l6 = Label.parse("¬fedcba");
		final Label l7 = Label.parse("ae¬f");
		final Label l8 = Label.parse("¬af¿b");
		final Label l9 = Label.parse("¬af¿b");
		final Label l10 = Label.parse("¬ec");
		final Label l11 = Label.parse("abd¿f");
		final Label l12 = Label.parse("a¿d¬f");
		final Label l13 = Label.parse("¬b¿d¿f");
		final Label l14 = Label.parse("b¬df¿e");
		final Label l15 = Label.parse("e¬c");
		final Label l16 = Label.parse("ab¿d¿f");
		final Label l17 = Label.parse("ad¬f");
		final Label l18 = Label.parse("b¿d¿f");
		final Label l19 = Label.parse("¬b¬df¿e");
		final Label l20 = Label.parse("¬e¬c");

		final Label ll1 = Label.parse("gabc¬f");
		final Label ll2 = Label.parse("gabcdef");
		final Label ll3 = Label.parse("ga¬bc¬de¬f");
		final Label ll4 = Label.parse("g¬b¬d¬f");
		final Label ll5 = Label.parse("gec");
		final Label ll6 = Label.parse("g¬fedcba");
		final Label ll7 = Label.parse("gae¬f");
		final Label ll8 = Label.parse("g¬af¿b");
		final Label ll9 = Label.parse("g¬af¿b");
		final Label ll0 = Label.parse("g¬ec");
		final Label ll21 = Label.parse("gabd¿f");
		final Label ll22 = Label.parse("ga¿d¬f");
		final Label ll23 = Label.parse("g¬b¿d¿f");
		final Label ll24 = Label.parse("gb¬df¿e");
		final Label ll25 = Label.parse("ge¬c");
		final Label ll26 = Label.parse("gab¿d¿f");
		final Label ll27 = Label.parse("gad¬f");
		final Label ll28 = Label.parse("gb¿d¿f");
		final Label ll29 = Label.parse("g¬b¬df¿e");
		final Label ll20 = Label.parse("g¬e¬c");

		long startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			map.clear();
			map.put(Label.emptyLabel, 109);
			map.put(l1, 10);
			map.put(l2, 20);
			map.put(l3, 25);
			map.put(l4, 23);
			map.put(l5, 22);
			map.put(l6, 23);
			map.put(l7, 20);
			map.put(l8, 20);
			map.put(l9, 21);
			map.put(l10, 11);
			map.put(l11, 11);
			map.put(l12, 11);
			map.put(l13, 24);
			map.put(l14, 22);
			map.put(l15, 23);
			map.put(l16, 20);
			map.put(l17, 23);
			map.put(l18, 23);
			map.put(l19, 23);
			map.put(l20, 23);
			map.put(ll1, 10);
			map.put(ll2, 20);
			map.put(ll3, 25);
			map.put(ll4, 23);
			map.put(ll5, 22);
			map.put(ll6, 23);
			map.put(ll7, 20);
			map.put(ll8, 20);
			map.put(ll9, 21);
			map.put(ll0, 11);
			map.put(ll21, 11);
			map.put(ll22, 11);
			map.put(ll23, 24);
			map.put(ll24, 22);
			map.put(ll25, 23);
			map.put(ll26, 20);
			map.put(ll27, 23);
			map.put(ll28, 23);
			map.put(ll29, 23);
			map.put(ll20, 23);

		}
		long endTime = System.nanoTime();
		System.out.println("LABELED VALUE SET-TREE MANAGED\nExecution time for some merge operations (mean over " + nTest + " tests).\nFirst map: " + map
				+ ".\nTime: (ms): "
				+ ((endTime - startTime) / msNorm));
		String rightAnswer = "{(⊡, 23) (¬a¿bf, 20) (abcdef, 20) (abc¬f, 10) (abd¿f, 11) (a¿d¬f, 11) (ae¬f, 20) (b¬d¿ef, 22) (c, 22) (c¬e, 11) }";
		System.out.println("The right final set is " + rightAnswer + ".");
		System.out.println("Is equal? " + AbstractLabeledIntMap.parse(rightAnswer).equals(map));

		startTime = System.nanoTime();
		int min = 1000;
		for (int i = 0; i < nTest; i++) {
			min = map.getMinValue();
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for determining the min value (" + min + ") (mean over " + nTest + " tests). (ms): "
				+ ((endTime - startTime) / msNorm));

		startTime = System.nanoTime();
		Label l = Label.parse("abd¿f");
		for (int i = 0; i < nTest; i++) {
			min = map.get(l);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for retrieving value of label " + l + " (mean over " + nTest + " tests). (ms): "
				+ ((endTime - startTime) / msNorm));

		startTime = System.nanoTime();
		map.put(Label.parse("c"), 11);
		map.put(Label.parse("¬c"), 11);
		endTime = System.nanoTime();
		System.out.println("After the insertion of (c,11) and (¬c,11) the map becomes: " + map);
		System.out.println("Execution time for simplification (ms): "
				+ ((endTime - startTime) / 1.0E6));
	}

}
