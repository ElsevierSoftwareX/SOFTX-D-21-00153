/**
 * 
 */
package it.univr.di.labeledvalue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.univr.di.labeledvalue.Literal.State;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * @author posenato
 */
public class LabeledIntTreeMapTest {

	/**
	 * stub class just to have a different implementation of the interface.
	 * 
	 * @author posenato
	 *
	 */
	private static class LabeledIntMapStub implements LabeledIntMap {

		/**
		 * 
		 */
		Map<Label, Integer> map;

		/**
		 * 
		 */
		public LabeledIntMapStub() {
			map = new HashMap<>();
		}

		@Override
		public boolean equals(final Object o) {
			if ((o == null) || !(o instanceof LabeledIntMap)) return false;
			final LabeledIntMap lvm = ((LabeledIntMap) o);
			if (this.size() != lvm.size()) return false;
			return this.entrySet().equals(lvm.entrySet());// Two maps are equals if they contain the same set of values. The internal representation of
			// optimization is not important!.
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
		public Set<Entry<Label>> entrySet() {
			Set<Entry<Label>> set = new ObjectArraySet<>();
			for (java.util.Map.Entry<Label, Integer> e : map.entrySet()) {
				set.add(new AbstractObject2IntMap.BasicEntry<>(e.getKey(), e.getValue()));
			}
			return set;
		}

		@Override
		public void clear() {
			map.clear();
		}

		@Override
		public IntCollection values() {
			return null;
		}

		@Override
		public int remove(Label l) {
			return 0;
		}

		@Override
		public int putForcibly(Label l, int i) {
			return 0;
		}

		@Override
		public void putAll(LabeledIntMap inputMap) {}

		@Override
		public int getValue(Label l) {
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
			// TODO Auto-generated method stub
			return 0;
		}
	};

	/**
	 * 
	 */
	LabeledIntTreeMap map = new LabeledIntTreeMap(true);
	/**
	 * 
	 */
	final LabeledIntTreeMap map1 = new LabeledIntTreeMap(true);

	/**
	 * 
	 */
	LabeledIntTreeMap result = new LabeledIntTreeMap(true);

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void eliminazione() {
		Label l1 = Label.parse("¬a¬b");
		map.clear();
		map.put(Label.emptyLabel, 109);
		map.put(l1, 21);

		map.remove(l1);
		map.remove(l1);
		map.remove(Label.emptyLabel);
		map.remove(Label.parse("a"));

		result.clear();

		assertEquals("Test inserimento due label di pari valore con un letterale opposto:\n", result, map);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazione1Test() {
		map.clear();
		map.put(Label.emptyLabel, 109);
		map.put(Label.parse("¬a¬b"), 21);
		map.put(Label.parse("¬ab"), 21);

		result.clear();
		result.put(Label.emptyLabel, 109);
		result.put(Label.parse("¬a"), 21);

		assertEquals("Test inserimento due label di pari valore con un letterale opposto:\n", result, map);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneRicorsivaTest() {
		map.clear();
		map.put(Label.emptyLabel, 109);
		map.put(Label.parse("a"), 21);
		map.put(Label.parse("¬a¬b"), 21);
		map.put(Label.parse("¬ab"), 21);

		result.clear();
		result.put(Label.emptyLabel, 21);

		assertEquals("Test inserimento due label di pari valore con un letterale opposto con ricorsione:\n", result, map);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase1Test() {
		map.clear();
		map.put(Label.emptyLabel, 109);
		map.put(Label.parse("ab"), 10);
		map.put(Label.parse("a"), 25);
		map.put(Label.parse("¬b"), 23);
		map.put(Label.parse("b"), 22);
		map.put(Label.parse("a¬b"), 23);
		map.put(Label.parse("ab"), 20);

		result.clear();
		result.put(Label.parse("¬b"), 23);
		result.put(Label.parse("b"), 22);
		result.put(Label.parse("ab"), 10);

		assertEquals("Test su creazione base A INIZIO e gestione semplificazioni:\n", result, map);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase2Test() {
		map.clear();
		map.put(Label.emptyLabel, 109);
		map.put(Label.parse("ab"), 10);
		map.put(Label.parse("a¬b"), 23);
		map.put(Label.parse("¬b"), 23);
		map.put(Label.parse("b"), 22);
		map.put(Label.parse("ab"), 20);

		result.clear();
		result.put(Label.parse("¬b"), 23);
		result.put(Label.parse("b"), 22);
		result.put(Label.parse("ab"), 10);

		assertEquals("Test su creazione base IN MEZZO e gestione semplificazioni:\n", result, map);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase3Test() {
		map.clear();
		map.put(Label.emptyLabel, 109);
		map.put(Label.parse("A¬b"), 23);
		map.put(Label.parse("ab"), 20);
		map.put(Label.parse("¬b"), 23);
		map.put(Label.parse("b"), 23);

		result.clear();
		result.put(Label.emptyLabel, 23);
		result.put(Label.parse("ab"), 20);

		assertEquals("Test su creazione base IN FINE e gestione semplificazioni:\n", result, map);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase4Test() {
		map.clear();
		map.put(Label.emptyLabel, 109);
		map.put(Label.parse("ab"), 10);
		map.put(Label.parse("a¬b"), 19);
		map.put(Label.parse("¬ab"), 10);
		map.put(Label.parse("¬a¬b"), 19);
		// Map: {(¬a¬b, 19) (¬aB, 10) (A¬b, 19) (AB, 10) }

		result.clear();
		result.put(Label.parse("b"), 10);
		result.put(Label.parse("¬b"), 19);

		assertEquals("Test su creazione e gestione semplificazioni:\n", result, map);

		map.put(Label.parse("¬b"), 10);
		result.clear();
		result.put(Label.emptyLabel, 10);

		assertEquals("Test su creazione e gestione semplificazioni:\n", result, map);

	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void insert0() {
		map.clear();
		map.put(Label.emptyLabel, 0);

		result.clear();
		result.put(Label.emptyLabel, 0);
		assertEquals("Test su aggiunta di (empty,0):\n", result, map);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void distruzioneBase() {
		map.clear();
		map.put(Label.emptyLabel, 100);
		map.put(Label.parse("ab"), 50);
		map.put(Label.parse("a¬b"), 59);
		map.put(Label.parse("¬aB"), 60);
		map.put(Label.parse("¬a¬b"), 69);
		map.put(Label.parse("¬a"), 30);
		map.put(Label.parse("a"), 30);

		result.clear();
		result.put(Label.emptyLabel, 30);
		assertEquals("Test di distruzione base:\n", result, map);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void distruzioneBase2() {
		map.clear();
		map.put(Label.parse("b"), -12);
		map.put(Label.parse("¬b"), -1);
		map.put(Label.parse("¬a"), -10);

		map.put(Label.emptyLabel, -10);

		result.clear();
		result.put(Label.emptyLabel, -10);
		result.put(Label.parse("b"), -12);
		assertEquals("Test di distruzione base 2:\n", result, map);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void caso20141021() {
		map.clear();
		map.put(Label.parse("¬b"), 29);
		map.put(Label.parse("a"), 26);
		map.put(Label.parse("ab"), 25);
		map.put(Label.emptyLabel, 30);
		map.put(Label.parse("¬ab"), 28);
		map.put(Label.parse("¬a"), 25);
		map.put(Label.parse("¬aB"), 23);
		map.put(Label.parse("a¬b"), 24);

		result.clear();
		result.put(Label.parse("a"), 26);
		result.put(Label.parse("ab"), 25);
		result.put(Label.parse("¬a"), 25);
		result.put(Label.parse("¬aB"), 23);
		result.put(Label.parse("a¬b"), 24);

		// System.out.println("map:"+map);
		// System.out.println("result:"+result);
		assertEquals("Rimuovo componenti maggiori con elementi consistenti della base:\n", result, map);
	}
	
	
	/**
	 * 
	 */
	@Test
	public final void caso20141128() {
		map.clear();
		map.put(Label.parse("bgp"), 10);
		map.put(Label.parse("cp"), -1);
//		System.out.println(map);

		assertEquals("{(bgp, 10) (cp, -1) }", map.toString());
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void casoBaseConValoriDistinti() {
		map.clear();
		map.put(Label.parse("a"), 30);
		map.put(Label.parse("¬a"), 25);
		map.put(Label.emptyLabel, 31);
		map.put(Label.parse("¬ab"), 24);
		map.put(Label.parse("¬a¬b"), 30);
		map.put(Label.parse("ab"), 23);

		result.clear();
		result.put(Label.parse("a"), 30);
		result.put(Label.parse("¬a"), 25);
		result.put(Label.parse("ab"), 23);
		result.put(Label.parse("¬ab"), 24);

		// System.out.printf("map:"+map);
		assertEquals("Base con valori differenti:\n", result, map);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void equals() {
		map.clear();
		map.put(Label.parse("a"), 30);
		map.put(Label.parse("¬a"), 25);
		map.put(Label.emptyLabel, 31);
		map.put(Label.parse("¬ab"), 24);
		map.put(Label.parse("¬a¬b"), 30);
		map.put(Label.parse("ab"), 23);

		LabeledIntMap result = new LabeledIntMapStub();
		result.clear();
		result.put(Label.parse("a"), 30);
		result.put(Label.parse("¬a"), 25);
		result.put(Label.parse("ab"), 23);
		result.put(Label.parse("¬ab"), 24);

		// System.out.printf("map:"+map);
		assertEquals("Test di equals con un'altra classe che implementa l'interfaccia:\n", result, map);
	}

	/**
	 * Check the kind of bundle result returned.
	 */
	@Test
	public final void checkIntSet() {
		map.clear();
		map.put(Label.parse("a"), 30);
		map.put(Label.parse("¬a"), 25);
		map.put(Label.emptyLabel, 31);
		map.put(Label.parse("¬ab"), 24);
		map.put(Label.parse("¬a¬b"), 30);
		map.put(Label.parse("ab"), 23);

		Set<Entry<Label>> set = map.entrySet();
		// System.out.println(set);
		for (Iterator<Entry<Label>> ite = set.iterator(); ite.hasNext();) {
			Entry<Label> e = ite.next();
			e.setValue(1);
		}
		assertNotEquals(map.entrySet(), set);
//		System.out.println("Map: "+map);
//		System.out.println("Set: "+set);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void putAllTest() {
		map.clear();
		map.put(Label.parse("a"), 30);
		map.put(Label.parse("¬a"), 25);

		LabeledIntMap result = new LabeledIntMapStub();
		result.clear();
		result.put(Label.parse("b"), 30);
		result.put(Label.parse("¬b"), 25);

		map1.clear();
		map1.put(Label.parse("c"), 30);
		map1.put(Label.parse("¬c"), 25);

		map.putAll(result);
		assertEquals("Put all da un oggetto di una classe diversa che implementa la stessa interfaccia",
				LabeledIntTreeMap.parse("{(a, 30) (¬a, 25) (b, 30) (¬b, 25) }", true), map);
		map.putAll(map1);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void parseTest() {
		map = LabeledIntTreeMap.parse("{(a, 30) (¬a, 25) (b, 30) (¬b, 25) }", true);

		result.clear();
		result.put(Label.parse("a"), 30);
		result.put(Label.parse("¬a"), 25);
		result.put(Label.parse("b"), 30);
		result.put(Label.parse("¬b"), 25);
		assertEquals(result, map);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void sumTest() {
		map.clear();
		map.put(Label.parse("a"), 0);
		map.put(Label.parse("¬a"), 1);

		map1.clear();
		map1.put(Label.parse("¬b"), LabeledIntMap.INT_NULL);
		map1.put(new Label(new Literal('c', State.unknown)), Constants.INT_POS_INFINITE);

//		System.out.println(map1);
		map = map.summedTo(map1);
		result.clear();
		result.put(new Label(new Literal('c', State.unknown)), Constants.INT_POS_INFINITE);
		assertEquals("Sum of two maps", result, map);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void minValue() {
		map.clear();
		map.put(Label.parse("a"), 0);
		map.put(Label.parse("¬a"), 1);

		assertTrue(map.getMinValue() == 0);
		map1.clear();

		assertTrue(map1.getMinValue() == LabeledIntMap.INT_NULL);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void minValueConsistentWithTest() {
		map.clear();
		map.put(Label.parse("a"), 0);
		map.put(Label.parse("¬a"), 1);

		assertTrue(map.getMinValueConsistentWith(Label.parse("¬a")) == 1);
	}

	/**
	 * Check the sum of labeled value.
	 */
	@Test
	public final void summedTo() {
		map.clear();
		map.put(Label.emptyLabel, 109);
		map.put(Label.parse("ab"), 10);
		map.put(Label.parse("a"), Constants.INT_POS_INFINITE);
		map.put(Label.parse("¬b"), 23);
		map.put(Label.parse("b"), 22);
		map.put(Label.parse("a¬b"), 23);
		map.put(Label.parse("ab"), Constants.INT_NEG_INFINITE);
//		System.out.println(map);  {(ab, -∞) (¬b, 23) (b, 22) }

		map1.clear();
		map1.put(Label.parse("¬b"), -23);
		map1.put(Label.parse("b"), -22);
		map1.put(Label.parse("ab"), -10);
//		System.out.println(map1); {(¬b, -23) (b, -22) }

		result.put(Label.emptyLabel, 0);
		result.put(Label.parse("ab"), Constants.INT_NEG_INFINITE);
		result.put(Label.parse("a").conjunctionExtended(new Label(new Literal('b', State.unknown))), Constants.INT_NEG_INFINITE);
		result.put(new Label(new Literal('b', State.unknown)), -1);

		assertEquals("Test about the sum of two labeled value sets:\n", result, map.summedTo(map1));
	}

	/**
	 * Check the sum of labeled value.
	 */
	@Test
	public final void simplificationWithInfinite() {
		map.clear();
		map.put(Label.emptyLabel, 109);
		map.put(Label.parse("ab"), 10);
		map.put(Label.parse("a"), Constants.INT_POS_INFINITE);
		map.put(Label.parse("¬b"), 23);
		map.put(Label.parse("b"), 22);
		map.put(Label.parse("a¬b"), Constants.INT_NULL);
		map.put(Label.parse("ab"), Constants.INT_NEG_INFINITE);

//		System.out.println(map);
		result = LabeledIntTreeMap.parse("{(¬b, 23) (b, 22) }", false);
		result.put(Label.parse("ab"), Constants.INT_NEG_INFINITE);
		assertEquals("Test about simplification with infinity numbers:\n", result, map);
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

		map.clear();
		map.put(ab, 10);
		map.put(abUp, 10);
		assertEquals("Test about simplification with unknown labeled numbers:\n", LabeledIntTreeMap.parse("{(ab, 10) }", false), map);

		map.clear();
		map.put(abp, 10);
		map.put(abUp, 10);
		assertEquals("Test about simplification with unknown labeled numbers:\n", LabeledIntTreeMap.parse("{(abp, 10) }", false), map);

		map.clear();
		map.put(abp, Constants.INT_NEG_INFINITE);
		map.put(abUp, Constants.INT_NEG_INFINITE);
		map1.clear();
		map1.put(abp, Constants.INT_NEG_INFINITE);
		assertEquals("Test about simplification with unknown labeled numbers:\n", map1, map);

		map.clear();
		map.put(abp, 10);
		map.put(abNp, 9);
		map.put(abUp, 9);
		result = LabeledIntTreeMap.parse("{(abp, 10) (ab¬p, 9) }", false);
		assertEquals("Test about simplification with unknown labeled numbers:\n", result, map);

		map.clear();
		map.put(abUp, 9);
		map.put(abp, 10);
		map.put(abNp, 9);
		assertEquals("Test about simplification with unknown labeled numbers:\n", LabeledIntTreeMap.parse("{(abp, 10) (ab¬p, 9) }", false), map);

		map.clear();
		map.put(abp, 10);
		map.put(abNp, 9);
		map.put(abUp, 8);
		assertEquals("Test about simplification with unknown labeled numbers:\n", LabeledIntTreeMap.parse("{(abp, 10) (ab¬p, 9) (ab¿p, 8) }", false), map);
	}
}
