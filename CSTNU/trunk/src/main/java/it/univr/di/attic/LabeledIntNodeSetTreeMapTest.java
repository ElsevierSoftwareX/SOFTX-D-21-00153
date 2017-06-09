///**
// * 
// */
//package it.univr.di.labeledvalue;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Set;
//import java.util.SortedSet;
//
//import org.junit.Test;
//
//import it.unimi.dsi.fastutil.ints.IntSet;
//import it.unimi.dsi.fastutil.objects.AbstractObject2ObjectMap;
//import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
//import it.univr.di.labeledvalue.Constants;
//import it.univr.di.labeledvalue.Label;
//import it.univr.di.labeledvalue.LabeledIntMap;
//import it.univr.di.labeledvalue.LabeledIntMapFactory;
//import it.univr.di.labeledvalue.LabeledIntNodeSetMap;
//import it.univr.di.labeledvalue.LabeledIntNodeSetTreeMap;
//import it.univr.di.labeledvalue.ValueNodeSetPair;
//import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
//import it.unimi.dsi.fastutil.objects.ObjectArraySet;
//import it.unimi.dsi.fastutil.objects.ObjectSet;
//
///**
// * @author posenato
// */
//public class LabeledIntNodeSetTreeMapTest {
//
//	/**
//	 * stub class just to have a different implementation of the interface.
//	 * 
//	 * @author posenato
//	 *
//	 */
//	private static class LabeledIntMapStub implements LabeledIntNodeSetMap {
//
//		/**
//		 * 
//		 */
//		Map<Label, ValueNodeSetPair> map;
//
//		/**
//		 * 
//		 */
//		public LabeledIntMapStub() {
//			this.map = new HashMap<>();
//		}
//
//		@Override
//		public boolean equals(final Object o) {
//			if ((o == null) || !(o instanceof LabeledIntNodeSetMap)) return false;
//			final LabeledIntNodeSetMap lvm = ((LabeledIntNodeSetMap) o);
//			if (this.size() != lvm.size()) return false;
//			return this.object2ObjectEntrySet().equals(lvm.object2ObjectEntrySet());// Two maps are equals if they contain the same set of values. The internal
//																					// representation of
//			// optimization is not important!.
//		}
//
//		@Override
//		public int hashCode() {
//			return this.map.hashCode();
//		}
//
//		@Override
//		public int size() {
//			return this.map.size();
//		}
//
//		@Override
//		public boolean put(Label l, int i, Set<String> ns) {
//			this.map.put(l, new ValueNodeSetPair(i, ns));
//			return false;
//		}
//
//		@Override
//		public Set<Object2ObjectMap.Entry<Label, ValueNodeSetPair>> object2ObjectEntrySet() {
//			Set<Object2ObjectMap.Entry<Label, ValueNodeSetPair>> set = new ObjectArraySet<>();
//			for (java.util.Map.Entry<Label, ValueNodeSetPair> e : this.map.entrySet()) {
//				set.add(new AbstractObject2ObjectMap.BasicEntry<>(e.getKey(), e.getValue()));
//			}
//			return set;
//		}
//
//		@Override
//		public void clear() {
//			this.map.clear();
//		}
//
//		@Override
//		public int remove(Label l) {
//			return 0;
//		}
//
//		@Override
//		public int putForcibly(Label l, int i, Set<String> ns) {
//			return 0;
//		}
//
//		@Override
//		public void putAll(LabeledIntNodeSetMap inputMap) {}
//
//		@Override
//		public int getMinValueConsistentWith(Label l) {
//			return 0;
//		}
//
//		@Override
//		public int getMinValue() {
//			return 0;
//		}
//
//		@Override
//		public int getMinValueAmongLabelsWOUnknown() {
//			return 0;
//		}
//
//		@Override
//		public String toString() {
//			final StringBuffer sb = new StringBuffer("{");
//			for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> e : this.object2ObjectEntrySet()) {// I wanted a sorted print!
//				sb.append("(");
//				sb.append(e.getKey().toString());
//				sb.append(", ");
//				final int value = e.getValue().getValue();
//				if ((value == Constants.INT_NEG_INFINITE) || (value == Constants.INT_POS_INFINITE)) {
//					if (value < 0) {
//						sb.append('-');
//					}
//					sb.append(Constants.INFINITY_SYMBOL);
//				} else {
//					sb.append(value);
//				}
//				final Set<String> ns = e.getValue().getNodeSet();
//				if (ns != null && !ns.isEmpty()) {
//					sb.append(", ");
//					sb.append(ns.toString());
//				}
//				sb.append(") ");
//			}
//			sb.append("}");
//			return sb.toString();
//		}
//
//		@Override
//		public boolean put(Label l, int i) {
//			// TODO Auto-generated method stub
//			return false;
//		}
//
//		@Override
//		public int putForcibly(Label l, int i) {
//			// TODO Auto-generated method stub
//			return 0;
//		}
//
//		@Override
//		public SortedSet<String> getNodeSet(Label l) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public LabeledIntMap createLabeledIntMap() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public LabeledIntMap createLabeledIntMap(LabeledIntMap lim) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public ObjectSet<Entry<Label>> entrySet() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public int get(Label l) {
//			// TODO Auto-generated method stub
//			return 0;
//		}
//
//		@Override
//		public ObjectSet<Label> keySet() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public void putAll(LabeledIntMap inputMap) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public IntSet values() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public ValueNodeSetPair getNodeSetPair(Label l) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public ObjectSet<Entry<Label>> entrySet(ObjectSet<Entry<Label>> setToReuse) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public ObjectSet<Label> keySet(ObjectSet<Label> setToReuse) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//	};
//
//	@SuppressWarnings("javadoc")
//	LabeledIntMapFactory<LabeledIntNodeSetTreeMap> factory = new LabeledIntMapFactory<>(LabeledIntNodeSetTreeMap.class);
//
//	/**
//	 * 
//	 */
//	LabeledIntNodeSetTreeMap map = this.factory.create();
//	/**
//	 * 
//	 */
//	LabeledIntNodeSetTreeMap map1 = this.factory.create();
//
//	/**
//	 * 
//	 */
//	LabeledIntNodeSetTreeMap result = this.factory.create();
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void eliminazione() {
//		Label l1 = Label.parse("¬a¬b");
//		this.map.clear();
//		this.map.put(Label.emptyLabel, 109, null);
//		this.map.put(l1, 21, null);
//
//		this.map.remove(l1);
//		this.map.remove(l1);
//		this.map.remove(Label.emptyLabel);
//		this.map.remove(Label.parse("a"));
//
//		this.result.clear();
//
//		assertEquals("Test inserimento due label di pari valore con un letterale opposto:\n", this.result, this.map);
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void semplificazione1Test() {
//		this.map.clear();
//		this.map.put(Label.emptyLabel, 109, null);
//		this.map.put(Label.parse("¬a¬b"), 21, null);
//		this.map.put(Label.parse("¬ab"), 21, null);
//
//		this.result.clear();
//		this.result.put(Label.emptyLabel, 109, null);
//		this.result.put(Label.parse("¬a"), 21, null);
//
//		assertEquals("Test inserimento due label di pari valore con un letterale opposto:\n", this.result, this.map);
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void semplificazioneRicorsivaTest() {
//		this.map.clear();
//		this.map.put(Label.emptyLabel, 109, null);
//		this.map.put(Label.parse("a"), 21, null);
//		this.map.put(Label.parse("¬a¬b"), 21, null);
//		this.map.put(Label.parse("¬ab"), 21, null);
//
//		this.result.clear();
//		this.result.put(Label.emptyLabel, 21, null);
//
//		assertEquals("Test inserimento due label di pari valore con un letterale opposto con ricorsione:\n", this.result, this.map);
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void semplificazioneBase1Test() {
//		this.map.clear();
//		this.map.put(Label.emptyLabel, 109, null);
//		this.map.put(Label.parse("ab"), 10, null);
//		this.map.put(Label.parse("a"), 25, null);
//		this.map.put(Label.parse("¬b"), 23, null);
//		this.map.put(Label.parse("b"), 22, null);
//		this.map.put(Label.parse("a¬b"), 23, null);
//		this.map.put(Label.parse("ab"), 20, null);
//
//		this.result.clear();
//		this.result.put(Label.parse("¬b"), 23, null);
//		this.result.put(Label.parse("b"), 22, null);
//		this.result.put(Label.parse("ab"), 10, null);
//
//		assertEquals("Test su creazione base A INIZIO e gestione semplificazioni:\n", this.result, this.map);
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void semplificazioneBase2Test() {
//		this.map.clear();
//		this.map.put(Label.emptyLabel, 109, null);
//		this.map.put(Label.parse("ab"), 10, null);
//		this.map.put(Label.parse("a¬b"), 23, null);
//		this.map.put(Label.parse("¬b"), 23, null);
//		this.map.put(Label.parse("b"), 22, null);
//		this.map.put(Label.parse("ab"), 20, null);
//
//		this.result.clear();
//		this.result.put(Label.parse("¬b"), 23, null);
//		this.result.put(Label.parse("b"), 22, null);
//		this.result.put(Label.parse("ab"), 10, null);
//
//		assertEquals("Test su creazione base IN MEZZO e gestione semplificazioni:\n", this.result, this.map);
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void semplificazioneBase3Test() {
//		this.map.clear();
//		this.map.put(Label.emptyLabel, 109, null);
//		this.map.put(Label.parse("A¬b"), 23, null);
//		this.map.put(Label.parse("ab"), 20, null);
//		this.map.put(Label.parse("¬b"), 23, null);
//		this.map.put(Label.parse("b"), 23, null);
//
//		this.result.clear();
//		this.result.put(Label.emptyLabel, 23, null);
//		this.result.put(Label.parse("ab"), 20, null);
//
//		assertEquals("Test su creazione base IN FINE e gestione semplificazioni:\n", this.result, this.map);
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void semplificazioneBase4Test() {
//		this.map.clear();
//		this.map.put(Label.emptyLabel, 109, null);
//		this.map.put(Label.parse("ab"), 10, null);
//		this.map.put(Label.parse("a¬b"), 19, null);
//		this.map.put(Label.parse("¬ab"), 10, null);
//		this.map.put(Label.parse("¬a¬b"), 19, null);
//		// Map: {(¬a¬b, 19) (¬aB, 10) (A¬b, 19) (AB, 10) }
//
//		this.result.clear();
//		this.result.put(Label.parse("b"), 10, null);
//		this.result.put(Label.parse("¬b"), 19, null);
//
//		assertEquals("Test su creazione e gestione semplificazioni:\n", this.result, this.map);
//
//		this.map.put(Label.parse("¬b"), 10, null);
//		this.result.clear();
//		this.result.put(Label.emptyLabel, 10, null);
//
//		assertEquals("Test su creazione e gestione semplificazioni:\n", this.result, this.map);
//
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void insert0() {
//		this.map.clear();
//		this.map.put(Label.emptyLabel, 0, null);
//
//		this.result.clear();
//		this.result.put(Label.emptyLabel, 0, null);
//		assertEquals("Test su aggiunta di (empty,0):\n", this.result, this.map);
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void distruzioneBase() {
//		this.map.clear();
//		this.map.put(Label.emptyLabel, 100, null);
//		this.map.put(Label.parse("ab"), 50, null);
//		this.map.put(Label.parse("a¬b"), 59, null);
//		this.map.put(Label.parse("¬aB"), 60, null);
//		this.map.put(Label.parse("¬a¬b"), 69, null);
//		this.map.put(Label.parse("¬a"), 30, null);
//		this.map.put(Label.parse("a"), 30, null);
//
//		this.result.clear();
//		this.result.put(Label.emptyLabel, 30, null);
//		assertEquals("Test di distruzione base:\n", this.result, this.map);
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void distruzioneBase2() {
//		this.map.clear();
//		this.map.put(Label.parse("b"), -12, null);
//		this.map.put(Label.parse("¬b"), -1, null);
//		this.map.put(Label.parse("¬a"), -10, null);
//
//		this.map.put(Label.emptyLabel, -10, null);
//
//		this.result.clear();
//		this.result.put(Label.emptyLabel, -10, null);
//		this.result.put(Label.parse("b"), -12, null);
//		assertEquals("Test di distruzione base 2:\n", this.result, this.map);
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void caso20141021() {
//		this.map.clear();
//		this.map.put(Label.parse("¬b"), 29, null);
//		this.map.put(Label.parse("a"), 26, null);
//		this.map.put(Label.parse("ab"), 25, null);
//		this.map.put(Label.emptyLabel, 30, null);
//		this.map.put(Label.parse("¬ab"), 28, null);
//		this.map.put(Label.parse("¬a"), 25, null);
//		this.map.put(Label.parse("¬aB"), 23, null);
//		this.map.put(Label.parse("a¬b"), 24, null);
//
//		this.result.clear();
//		this.result.put(Label.parse("a"), 26, null);
//		this.result.put(Label.parse("ab"), 25, null);
//		this.result.put(Label.parse("¬a"), 25, null);
//		this.result.put(Label.parse("¬aB"), 23, null);
//		this.result.put(Label.parse("a¬b"), 24, null);
//
//		// System.out.println("map:"+map);
//		// System.out.println("result:"+result);
//		assertEquals("Rimuovo componenti maggiori con elementi consistenti della base:\n", this.result, this.map);
//	}
//
//	/**
//	 * 
//	 */
//	@Test
//	public final void caso20141128() {
//		this.map.clear();
//		this.map.put(Label.parse("bgp"), 10, null);
//		this.map.put(Label.parse("cp"), -1, null);
////		System.out.println(map);
//
//		assertEquals("{(bgp, 10) (cp, -1) }", this.map.toString());
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void casoBaseConValoriDistinti() {
//		this.map.clear();
//		this.map.put(Label.parse("a"), 30, null);
//		this.map.put(Label.parse("¬a"), 25, null);
//		this.map.put(Label.emptyLabel, 31, null);
//		this.map.put(Label.parse("¬ab"), 24, null);
//		this.map.put(Label.parse("¬a¬b"), 30, null);
//		this.map.put(Label.parse("ab"), 23, null);
//
//		this.result.clear();
//		this.result.put(Label.parse("a"), 30, null);
//		this.result.put(Label.parse("¬a"), 25, null);
//		this.result.put(Label.parse("ab"), 23, null);
//		this.result.put(Label.parse("¬ab"), 24, null);
//
//		// System.out.printf("map:"+map);
//		assertEquals("Base con valori differenti:\n", this.result, this.map);
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void equals() {
//		this.map.clear();
//		this.map.put(Label.parse("a"), 30, null);
//		this.map.put(Label.parse("¬a"), 25, null);
//		this.map.put(Label.emptyLabel, 31, null);
//		this.map.put(Label.parse("¬ab"), 24, null);
//		this.map.put(Label.parse("¬a¬b"), 30, null);
//		this.map.put(Label.parse("ab"), 23, null);
//
//		LabeledIntNodeSetMap result = new LabeledIntMapStub();
//		result.clear();
//		result.put(Label.parse("a"), 30, null);
//		result.put(Label.parse("¬a"), 25, null);
//		result.put(Label.parse("ab"), 23, null);
//		result.put(Label.parse("¬ab"), 24, null);
//
//		// System.out.printf("map:"+map);
//		assertEquals("Test di equals con un'altra classe che implementa l'interfaccia:\n", result, this.map);
//	}
//
//	/**
//	 * Check the kind of bundle result returned.
//	 */
//	@Test
//	public final void checkIntSet() {
//		this.map.clear();
//		this.map.put(Label.parse("a"), 30, null);
//		this.map.put(Label.parse("¬a"), 25, null);
//		this.map.put(Label.emptyLabel, 31, null);
//		this.map.put(Label.parse("¬ab"), 24, null);
//		this.map.put(Label.parse("¬a¬b"), 30, null);
//		this.map.put(Label.parse("ab"), 23, null);
//
//		Set<Object2ObjectMap.Entry<Label, ValueNodeSetPair>> set = this.map.object2ObjectEntrySet();
//		// System.out.println(set);
//		for (Iterator<Object2ObjectMap.Entry<Label, ValueNodeSetPair>> ite = set.iterator(); ite.hasNext();) {
//			Object2ObjectMap.Entry<Label, ValueNodeSetPair> e = ite.next();
//			e.getValue().setValue(1);
//		}
//		assertEquals(this.map.object2ObjectEntrySet(), set);
//		// System.out.println("Map: "+map);
//		// System.out.println("Set: "+set);
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void putAllTest() {
//		this.map.clear();
//		this.map.put(Label.parse("a"), 30, null);
//		this.map.put(Label.parse("¬a"), 25, null);
//
//		LabeledIntMapStub result = new LabeledIntMapStub();
//		result.clear();
//		result.put(Label.parse("b"), 30, null);
//		result.put(Label.parse("¬b"), 25, null);
//
//		this.map1.clear();
//		this.map1.put(Label.parse("c"), 30, null);
//		this.map1.put(Label.parse("¬c"), 25, null);
//
//		this.map.putAll(result);
//		assertEquals("Put all da un oggetto di una classe diversa che implementa la stessa interfaccia",
//				LabeledIntNodeSetTreeMap.parse("{(a, 30) (¬a, 25) (b, 30) (¬b, 25) }"), this.map);
//		this.map.putAll(this.map1);
//	}
//	
//	
//	
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void putForciclyTest() {
//		this.map.clear();
//		this.map.put(Label.parse("a"), 30, null);
//		this.map.put(Label.parse("¬a"), 25, null);
//		this.map.put(Label.emptyLabel, 0, null);
//
//		this.map1 = this.factory.create(this.map);
//		this.map1.put(Label.emptyLabel, 0, null);
//		
//		assertEquals("Put forcibly with a base", this.map, this.map1);
//	}
//	
//	
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void parseTest() {
//		this.map = LabeledIntNodeSetTreeMap.parse("{(a, 30) (¬a, 25) (b, 30) (¬b, 25) }");
//
//		this.result.clear();
//		this.result.put(Label.parse("a"), 30, null);
//		this.result.put(Label.parse("¬a"), 25, null);
//		this.result.put(Label.parse("b"), 30, null);
//		this.result.put(Label.parse("¬b"), 25, null);
//		assertEquals(this.result, this.map);
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void minValue() {
//		this.map.clear();
//		this.map.put(Label.parse("a"), 0, null);
//		this.map.put(Label.parse("¬a"), 1, null);
//
//		assertTrue(this.map.getMinValue() == 0);
//		this.map1.clear();
//
//		assertTrue(this.map1.getMinValue() == Constants.INT_NULL);
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void minValueConsistentWithTest() {
//		this.map.clear();
//		this.map.put(Label.parse("a"), 0, null);
//		this.map.put(Label.parse("¬a"), 1, null);
//
//		assertTrue(this.map.getMinValueConsistentWith(Label.parse("¬a")) == 1);
//	}
//
//	/**
//	 * Check the sum of labeled value.
//	 */
//	@Test
//	public final void simplificationWithInfinite() {
//		this.map.clear();
//		this.map.put(Label.emptyLabel, 109, null);
//		this.map.put(Label.parse("ab"), 10, null);
//		this.map.put(Label.parse("a"), Constants.INT_POS_INFINITE, null);
//		this.map.put(Label.parse("¬b"), 23, null);
//		this.map.put(Label.parse("b"), 22, null);
//		this.map.put(Label.parse("a¬b"), Constants.INT_NULL, null);
//		this.map.put(Label.parse("ab"), Constants.INT_NEG_INFINITE, null);
//
////		System.out.println(map);
//		this.result = LabeledIntNodeSetTreeMap.parse("{(¬b, 23) (b, 22) }");
//		this.result.put(Label.parse("ab"), Constants.INT_NEG_INFINITE, null);
//		assertEquals("Test about simplification with infinity numbers:\n", this.result, this.map);
//	}
//
//	/**
//	 * Check the sum of labeled value.
//	 */
//	@Test
//	public final void simplificationWithUnknown() {
//		Label ab = Label.parse("ab");
//		Label abp = Label.parse("abp");
//		Label abNp = Label.parse("ab¬p");
//		Label abUp = Label.parse("ab¿p");
//
//		this.map.clear();
//		this.map.put(ab, 10, null);
//		this.map.put(abUp, 10, null);
//		assertEquals("Test about simplification with unknown labeled numbers:\n", LabeledIntNodeSetTreeMap.parse("{(ab, 10) }"), this.map);
//
//		this.map.clear();
//		this.map.put(abp, 10, null);
//		this.map.put(abUp, 10, null);
//		assertEquals("Test about simplification with unknown labeled numbers:\n", LabeledIntNodeSetTreeMap.parse("{(abp, 10) }"), this.map);
//
//		this.map.clear();
//		this.map.put(abp, Constants.INT_NEG_INFINITE, null);
//		this.map.put(abUp, Constants.INT_NEG_INFINITE, null);
//		this.map1.clear();
//		this.map1.put(abp, Constants.INT_NEG_INFINITE, null);
//		assertEquals("Test about simplification with unknown labeled numbers:\n", this.map1, this.map);
//
//		this.map.clear();
//		this.map.put(abp, 10, null);
//		this.map.put(abNp, 9, null);
//		this.map.put(abUp, 9, null);
//		this.result = LabeledIntNodeSetTreeMap.parse("{(abp, 10) (ab¬p, 9) }");
//		assertEquals("Test about simplification with unknown labeled numbers:\n", this.result, this.map);
//
//		this.map.clear();
//		this.map.put(abUp, 9, null);
//		this.map.put(abp, 10, null);
//		this.map.put(abNp, 9, null);
//		assertEquals("Test about simplification with unknown labeled numbers:\n", LabeledIntNodeSetTreeMap.parse("{(abp, 10) (ab¬p, 9) }"), this.map);
//
//		this.map.clear();
//		this.map.put(abp, 10, null);
//		this.map.put(abNp, 9, null);
//		this.map.put(abUp, 8, null);
//		assertEquals("Test about simplification with unknown labeled numbers:\n", LabeledIntNodeSetTreeMap.parse("{(abp, 10) (ab¬p, 9) (ab¿p, 8) }"),
//				this.map);
//	}
//	
//	
//	
//	/**
//	 * Check the sum of labeled value.
//	 */
//	@Test
//	public final void simplificationWithUnknown1() {
//		Label Upab = Label.parse("¿p¿ab");
//		Label Upr = Label.parse("¿pr");
//		Label UpNr = Label.parse("¿p¬r");
//		Set<String> s1 = new ObjectArraySet<>();
//		s1.add("A");s1.add("B");
//		
//		this.map.clear();
//		this.map.put(Upab, -24, s1);
//		this.map.put(Upr, Constants.INT_NEG_INFINITE, null);
////		System.out.println("Before introduction UpNr: "+ map);
//		this.map.put(UpNr, Constants.INT_NEG_INFINITE, null);
//		assertEquals("Test about simplification with unknown labeled infinity:\n", "{(¿p, -∞) }", this.map.toString());
//	}
//
//	/**
//	 * Check if the management of the base is correct.
//	 */
//	@Test
//	public final void mergeTwoEqualLabelsWithDifferentNodeSet() {
//		Set<String> s1 = new ObjectArraySet<>();
//		s1.add("A");
//
//		Set<String> s2 = new ObjectArraySet<>();
//		s2.add("B");
//
//		this.map.clear();
//		this.map.put(Label.parse("a"), -2, s1);
//		this.map.put(Label.parse("a"), -1, s2);
//
//		assertEquals("{(a, -2, {A, B}) }", this.map.toString());
//	}
//
//	/**
//	 * 
//	 */
//	@Test
//	public final void mergeOneLabelSubsumesOtherWithNodeSetTest() {
//		Set<String> s1 = new ObjectArraySet<>();
//		s1.add("A");
//
//		Set<String> s2 = new ObjectArraySet<>();
//		s2.add("B");
//
//		Set<String> s3 = new ObjectArraySet<>();
//		s3.add("C");
//
//		// case 2
//		this.map.clear();
//		this.map.put(Label.parse("ab"), -2, null);
//		this.map.put(Label.parse("a"), -3, s2);
//		this.map.put(Label.parse("b"), -1, s3);
//		assertEquals("{(a, -3, {B}) (b, -1, {C}) }", this.map.toString());
//
//		// case 3
//		this.map.clear();
//		this.map.put(Label.parse("ab"), -2, s1);
//		this.map.put(Label.parse("a"), -3, null);
//		this.map.put(Label.parse("b"), -1, null);
//		assertEquals("{(a, -3) (ab, -3, {A}) (b, -1) }", this.map.toString());
//
//		// case 4
//		this.map.clear();
//		this.map.put(Label.parse("ab"), -2, s1);
//		this.map.put(Label.parse("a"), -3, s2);
//		this.map.put(Label.parse("b"), -1, s3);
//		assertEquals("{(a, -3, {B}) (ab, -3, {A}) (b, -1, {C}) }", this.map.toString());
//	}
//
//	/**
//	 * 
//	 */
//	@Test
//	public final void mergeOneLabelSubsumesOtherWithNodeSetReverseTest() {
//		Set<String> s1 = new ObjectArraySet<>();
//		s1.add("A");
//
//		Set<String> s2 = new ObjectArraySet<>();
//		s2.add("B");
//
//		Set<String> s3 = new ObjectArraySet<>();
//		s3.add("C");
//
//		// case 2
//		this.map.clear();
//		this.map.put(Label.parse("b"), -1, s3);
//		this.map.put(Label.parse("ab"), -2, null);
//		this.map.put(Label.parse("a"), -3, s2);
//		assertEquals("{(a, -3, {B}) (b, -1, {C}) }", this.map.toString());
//
//		// case 3
//		this.map.clear();
//		this.map.put(Label.parse("b"), -1, null);
//		this.map.put(Label.parse("a"), -3, null);
//		this.map.put(Label.parse("ab"), -2, s1);
//		assertEquals("{(a, -3) (ab, -3, {A}) (b, -1) }", this.map.toString());
//
//		// case 4
//		this.map.clear();
//		this.map.put(Label.parse("b"), -1, s3);
//		this.map.put(Label.parse("a"), -3, s2);
//		this.map.put(Label.parse("ab"), -2, s1);
//		assertEquals("{(a, -3, {B}) (ab, -3, {A}) (b, -1, {C}) }", this.map.toString());
//	}
//
//	/**
//	 * 
//	 */
////	@Test
//	public final void mergeOneLabelSubsumesOtherWithNodeSetReverseTes3() {
//
//		// {(⊡, -10) (¬p¿q, -18) (¬p¿q¿r, -20, {X, U, W}) (p¬qr, -13, {X, U}) (p¬qrs, -13, {X, U, W}) (p¬qr¿s, -18, {X, U, W}) (p¬q¿r, -13, {X, U}) (p¬q¿rs,
//		// -17, {X, U, Y}) (p¬q¿r¿s, -∞, {Y, X, U, W}) (p¬q¿s, -18) (p¿qr, -13, {X, U}) (p¿qrs, -18, {X, U}) (p¿qr¿s, -21, {X, U}) (p¿q¿r, -13, {X, U}) (p¿q¿rs,
//		// -∞, {Y, X, U}) (p¿q¿r¿s, -∞, {W, Y, X}) (prs, -13) (p¿r, -13) (p¿rs, -15) (p¿s, -15) (¿p¬q, -15) (¿p¬qr, -18, {X, U}) (¿p¬qrs, -18, {X, U}) (¿p¬qr¿s,
//		// -21, {X, U}) (¿p¬q¿r, -18, {X, U, W}) (¿p¬q¿rs, -∞, {Y, X, U}) (¿p¬q¿r¿s, -∞, {Y, X, W}) (¿p¬qs, -18) (¿p¿qr, -21, {X}) (¿p¿qrs, -21, {X, U})
//		// (¿p¿q¿r, -23, {X, U}) (¿p¿q¿rs, -∞, {Y, X, U}) (¿p¿r, -15) (¿ps, -15) (¬qr, -13, {X, U}) (¬qrs, -13, {X, U, W}) (¬qr¿s, -17, {X, U}) (¬q¿r, -13, {X,
//		// U}) (¬q¿rs, -13, {X, U, W, Y}) (¬q¿r¿s, -∞, {Y, X, U}) (¬qs, -13) (¿qr, -13, {X, U}) (¿qr¿s, -18, {X, U, W}) (¿q¿r, -13, {X, U}) (¿q¿rs, -∞, {Y, X,
//		// U}) (¿q¿r¿s, -∞, {Y, X, U}) (¿qs, -18) }; ❯
//
//		Set<String> XUW = new ObjectArraySet<>();
//		XUW.add("X");
//		XUW.add("U");
//		XUW.add("W");
//
//		Set<String> XUWY = new ObjectArraySet<>();
//		XUWY.add("X");
//		XUWY.add("U");
//		XUWY.add("W");
//		XUWY.add("Y");
//
//		Set<String> XU = new ObjectArraySet<>();
//		XU.add("X");
//		XU.add("U");
//		Set<String> XUY = new ObjectArraySet<>();
//		XUY.add("X");
//		XUY.add("U");
//		XUY.add("Y");
//		Set<String> XYW = new ObjectArraySet<>();
//		XYW.add("W");
//		XYW.add("Y");
//		XYW.add("X");
//
//		Set<String> YXU = new ObjectArraySet<>();
//		XYW.add("Y");
//		XYW.add("X");
//		XYW.add("U");
//
//		Set<String> WYX = new ObjectArraySet<>();
//		WYX.add("W");
//		WYX.add("Y");
//		WYX.add("X");
//
//		Set<String> YXW = new ObjectArraySet<>();
//		YXW.add("Y");
//		YXW.add("X");
//		YXW.add("W");
//
//		Set<String> X = new ObjectArraySet<>();
//		YXW.add("X");
//
//		Set<String> YXUW = new ObjectArraySet<>();
//		YXUW.add("Y");
//		YXUW.add("X");
//		YXUW.add("U");
//		YXUW.add("W");
//
//		this.map.clear();
//		this.map.put(Label.parse("¿qs"), -18, null);
//		this.map.put(Label.parse("¿q¿r¿s"), Constants.INT_NEG_INFINITE, YXU);
//		this.map.put(Label.parse("¿q¿rs"), Constants.INT_NEG_INFINITE, YXU);
//		this.map.put(Label.parse("¿q¿r"), -13, XU);
//		this.map.put(Label.parse("¿qr¿s"), -18, XUW);
//		this.map.put(Label.parse("¿qr"), -13, XU);
//		this.map.put(Label.parse("¬qs"), -13, null);
//		this.map.put(Label.parse("¬q¿r¿s"), Constants.INT_NEG_INFINITE, YXU);
//		this.map.put(Label.parse("¬q¿rs"), -13, XUWY);
//		this.map.put(Label.parse("¬q¿r"), -13, XU);
//		this.map.put(Label.parse("¬qr¿s"), -17, XU);
//		this.map.put(Label.parse("¬qrs"), -13, XUW);
//		this.map.put(Label.parse("¬qr"), -13, XU);
//		this.map.put(Label.parse("¿ps"), -15, null);
//		this.map.put(Label.parse("¿p¿r"), -15, null);
//		this.map.put(Label.parse("¿p¿q¿rs"), Constants.INT_NEG_INFINITE, YXU);
//		this.map.put(Label.parse("¿p¿q¿r"), -23, XU);
//		this.map.put(Label.parse("¿p¿qrs"), -21, XU);
//		this.map.put(Label.parse("¿p¿qr"), -21, X);
//		this.map.put(Label.parse("¿p¬qs"), -18, null);
//		this.map.put(Label.parse("¿p¬q¿r¿s"), Constants.INT_NEG_INFINITE, YXW);
//		this.map.put(Label.parse("¿p¬q¿rs"), Constants.INT_NEG_INFINITE, YXU);
//		this.map.put(Label.parse("¿p¬q¿r"), -18, XUW);
//		this.map.put(Label.parse("¿p¬qr¿s"), -21, XU);
//		this.map.put(Label.parse("¿p¬qrs"), -18, XU);
//		this.map.put(Label.parse("¿p¬qr"), -18, XU);
//		this.map.put(Label.parse("¿p¬q"), -15, null);
//		this.map.put(Label.parse("p¿s"), -15, null);
//		this.map.put(Label.parse("p¿rs"), -15, null);
//		this.map.put(Label.parse("p¿r"), -13, null);
//		this.map.put(Label.parse("prs"), -13, null);
//		this.map.put(Label.parse("p¿q¿r¿s"), Constants.INT_NEG_INFINITE, WYX);
//		this.map.put(Label.parse("p¿q¿rs"), Constants.INT_NEG_INFINITE, YXU);
//		this.map.put(Label.parse("p¿q¿r"), -13, XU);
//		this.map.put(Label.parse("p¿qr¿s"), -21, XU);
//		this.map.put(Label.parse("p¿qrs"), -18, XU);
//		this.map.put(Label.parse("p¿qr"), -13, XU);
//		this.map.put(Label.parse("p¬q¿s"), -18, null);
//		this.map.put(Label.parse("p¬q¿r¿s"), Constants.INT_NEG_INFINITE, YXUW);
//		this.map.put(Label.parse("p¬q¿rs"), -17, XUY);
//		this.map.put(Label.parse("p¬q¿r"), -13, XU);
//		this.map.put(Label.parse("p¬qr¿s"), -18, XUW);
//		this.map.put(Label.parse("p¬qrs"), -13, XUW);
//		this.map.put(Label.parse("p¬qr"), -13, XU);
//		this.map.put(Label.parse("¬p¿q¿r"), -20, XUW);
//		this.map.put(Label.parse("¬p¿q"), -18, null);
//		this.map.put(Label.emptyLabel, -10, null);
//
////		System.out.println(map);
//		assertEquals(
//				"{(⊡, -10) (¬p¿q, -18) (¬p¿q¿r, -20, {X, U, W}) (p¬qr, -13, {X, U}) (p¬qrs, -13, {X, U, W}) (p¬qr¿s, -18, {X, U, W}) (p¬q¿r, -13, {X, U}) (p¬q¿rs, -17, {X, U, Y}) (p¬q¿r¿s, -∞, {Y, X, U, W}) (p¬q¿s, -18) (p¿qr, -13, {X, U}) (p¿qrs, -18, {X, U}) (p¿qr¿s, -21, {X, U}) (p¿q¿r, -13, {X, U}) (p¿q¿rs, -∞, {Y, X, U}) (p¿q¿r¿s, -∞, {W, Y, X}) (prs, -13) (p¿r, -13) (p¿rs, -15) (p¿s, -15) (¿p¬q, -15) (¿p¬qr, -18, {X, U}) (¿p¬qrs, -18, {X, U}) (¿p¬qr¿s, -21, {X, U}) (¿p¬q¿r, -18, {X, U, W}) (¿p¬q¿rs, -∞, {Y, X, U}) (¿p¬q¿r¿s, -∞, {Y, X, W}) (¿p¬qs, -18) (¿p¿qr, -21, {X}) (¿p¿qrs, -21, {X, U}) (¿p¿q¿r, -23, {X, U}) (¿p¿q¿rs, -∞, {Y, X, U}) (¿p¿r, -15) (¿ps, -15) (¬qr, -13, {X, U}) (¬qrs, -13, {X, U, W}) (¬qr¿s, -17, {X, U}) (¬q¿r, -13, {X, U}) (¬q¿rs, -13, {X, U, W, Y}) (¬q¿r¿s, -∞, {Y, X, U}) (¬qs, -13) (¿qr, -13, {X, U}) (¿qr¿s, -18, {X, U, W}) (¿q¿r, -13, {X, U}) (¿q¿rs, -∞, {Y, X, U}) (¿q¿r¿s, -∞, {Y, X, U}) (¿qs, -18) }"
//				, this.map.toString());
//
//	}
//
//	/**
//	 * 
//	 */
//	@Test
//	public final void mergeOneLabelSubsumesOtherWithNodeSetReverseTest4() {
//
//		// {(⊡, -10) (¬p¬q, -13, {X, U}) (¬p¿q, -13, {X, U}) (¿p, -15) (¿p¬q, -22, {X, U, Y}) (¿p¿q, -∞, {Y, X, W})
//		// trasformato in
//		// {(⊡, -10) (¬p¬q, -13, {X, U}) (¬p¿q, -10, {X, U}) (¿p, -15) (¿p¬q, -22, {X, U, Y}) (¿p¿q, -∞, {Y, X, W})
//
//		Set<String> XU = new ObjectArraySet<>();
//		XU.add("X");
//		XU.add("U");
//		Set<String> XUY = new ObjectArraySet<>();
//		XUY.add("X");
//		XUY.add("U");
//		XUY.add("Y");
//
//		Set<String> YXW = new ObjectArraySet<>();
//		YXW.add("Y");
//		YXW.add("X");
//		YXW.add("W");
//
//		this.map.clear();
//		this.map.put(Label.parse("¬p¿q"), -13, XU);
//		this.map.put(Label.parse("¬p¬q"), -13, XU);
//		this.map.put(Label.parse("¿p"), -15, null);
//		this.map.put(Label.parse("¿p¬q"), -22, XUY);
//		this.map.put(Label.emptyLabel, -10, null);
//		this.map.put(Label.parse("¿p¿q"), Constants.INT_NEG_INFINITE, YXW);
////		System.out.println(map.toString());
//		assertEquals(
//				"{(⊡, -10) (¬p¬q, -13, {U, X}) (¬p¿q, -13, {U, X}) (¿p, -15) (¿p¬q, -22, {U, X, Y}) (¿p¿q, -∞, {W, X, Y}) }"
//				, this.map.toString());
//
//	}
//}
