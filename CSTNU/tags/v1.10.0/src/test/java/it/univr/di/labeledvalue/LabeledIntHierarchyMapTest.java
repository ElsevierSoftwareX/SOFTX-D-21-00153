/**
 * 
 */
package it.univr.di.labeledvalue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;

/**
 * @author posenato
 */
public class LabeledIntHierarchyMapTest {

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
			//not implemented
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
		public ObjectSortedSet<Entry<Label>> entrySet() {
			return this.map.object2IntEntrySet();
		}

		@Override
		public ObjectSet<Label> keySet() {
			return this.map.keySet();
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

	@SuppressWarnings("javadoc")
	LabeledIntMapFactory<LabeledIntHierarchyMap> factory = new LabeledIntMapFactory<>(LabeledIntHierarchyMap.class);
	/**
	 * 
	 */
	LabeledIntHierarchyMap actual = this.factory.create();
	
	/**
	 * 
	 */
	LabeledIntHierarchyMap actual1 = new LabeledIntHierarchyMap();

	/**
	 * 
	 */
	LabeledIntMap expected = this.factory.create();

	/**
	 * 
	 */
	@Before
	public void init() {
		this.actual.wellFormatCheck = true;
		this.actual1.wellFormatCheck = true;
	}
	
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

//		assertEquals("Test cancellazione ripetuta, di empty label e di una label inesistente:\n", expected, actual);
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

//		assertEquals("Test inserimento due label di pari valore con un letterale opposto:\n", expected, actual);
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

//		assertEquals("Test inserimento due label di pari valore con un letterale opposto con ricorsione:\n", expected, actual);
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
		this.expected.put(Label.emptyLabel, 23);
		this.expected.put(Label.parse("b"), 22);
		this.expected.put(Label.parse("ab"), 10);

//		assertEquals("Test su creazione base A INIZIO e gestione semplificazioni:\n", expected, actual);
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
		this.expected.put(Label.emptyLabel, 23);
		this.expected.put(Label.parse("b"), 22);
		this.expected.put(Label.parse("ab"), 10);

//		assertEquals("Test su creazione base IN MEZZO e gestione semplificazioni:\n", expected, actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase3Test() {
		this.actual.clear();
		this.actual.put(Label.emptyLabel, 109);
		this.actual.put(Label.parse("a¬b"), 23);
		this.actual.put(Label.parse("ab"), 20);
		this.actual.put(Label.parse("¬b"), 23);
		this.actual.put(Label.parse("b"), 23);

		this.expected.clear();
		this.expected.put(Label.emptyLabel, 23);
		this.expected.put(Label.parse("ab"), 20);

//		assertEquals("Test su creazione base IN FINE e gestione semplificazioni:\n", expected, actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase4Test() {
		this.actual.clear();
		this.actual.put(Label.emptyLabel, 109);
		this.actual.put(Label.parse("ab"), 10);
		this.actual.put(Label.parse("a¬b"), 19);
		this.actual.put(Label.parse("¬ab"), 10);
		this.actual.put(Label.parse("¬a¬b"), 19);
		// { E, 109 a,19 b,10 –a–b,19 }

		this.expected.clear();
		this.expected.put(Label.emptyLabel, 109);
		this.expected.put(Label.parse("¬b¬a"), 19);
		this.expected.put(Label.parse("b"), 10);
		this.expected.put(Label.parse("a"), 19);
//		assertEquals("Test su creazione e gestione semplificazioni:\n", expected, actual);

		this.actual.put(Label.parse("¬b"), 10);
		this.expected.clear();
		this.expected.put(Label.emptyLabel, 10);

//		assertEquals("Test su creazione e gestione semplificazioni:\n", expected, actual);

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
//		assertEquals("Test di distruzione base:\nexpect.size" + expected.size() + "\nactual.size" + actual.size(), expected, actual);//se ottimizzato
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
		// E,25 a,24 ¬ab,23

		this.expected.clear();
		this.expected.put(Label.emptyLabel, 25);
		this.expected.put(Label.parse("a"), 24);
		this.expected.put(Label.parse("¬ab"), 23);

		// System.out.println("map:"+map);
		// System.out.println("result:"+result);
//		assertEquals(
//				"Rimuovo componenti maggiori con elementi consistenti della base:\nexpected:" + expected + ".size: " + expected.size() + "\nactual" + actual
//						+ ".size: " + actual.size()
//						+ "\n",
//				expected, actual);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void caso20160108() {
		this.actual.clear();
		// SEVERE: Put history: (A, -40) (A, -56) (¬a¬b¬c¬DE, -46) (¬a¬c¬DE, -4) (-20, ¬a¬b) (⊡, -16)
		Label a = Label.parse("a");
		Label NaNbNcNde = Label.parse("¬a¬b¬c¬de");
		Label NaNcNde = Label.parse("¬a¬c¬de");
		Label NaNb = Label.parse("¬a¬b");

		this.actual.put(a, -40);
		this.actual.put(a, -56);
		this.actual.put(NaNbNcNde, -46);
		this.actual.put(NaNcNde, -4);
		this.actual.put(NaNb, -20);
		this.actual.put(Label.emptyLabel, -16);

		assertEquals("{(-16, ⊡) (-20, ¬a¬b) (-46, ¬a¬b¬c¬de) (-56, a) }", this.actual.toString());
		// System.out.println("actual: " + actual);

	}

	/**
	 * Checks if the management of the structure is correct.
	 * It checks only if actual.wellFormatCheck is true! 
	 */
	@Test
	public final void caso20160108b() {
		this.actual.clear();
		this.actual.wellFormatCheck = true;
		// SEVERE: Put history: (841, ¬a¬b¬d¬e) (823, ¬a¬b¬d¬e) (576, ¬a¬b¬d¬e) (¬a¬b¬c¬DE, 517) (552, ¬a¬b) (¬a¬b¬d¬e, 881)
		// (818, ¬ab¬d¬e) (890, ¬a¬d¬e) (¬a¬c¬DE, 896) (916, ¬a) (A¬d¬e, 711) (A¬d¬e, 711) (890, ¬d¬e) (¬c¬DE, 927) (⊡, 964)
		// (890, ¬a¬b¬d¬e) (899, ¬a¬d¬e) (¬a¬b¬c¬DE, 373) (459, ¬a¬b) (¬a¬c¬DE, 752) (775, ¬ab¬d¬e) (A, 642) (A, 642)
		this.actual.put(Label.parse("¬a¬b¬d¬e"), 841);
		this.actual.put(Label.parse("¬a¬b¬d¬e"), 823);
		this.actual.put(Label.parse("¬a¬b¬d¬e"), 576);
		this.actual.put(Label.parse("¬a¬b¬c¬de"), 517);
		this.actual.put(Label.parse("¬a¬b"), 552);
		this.actual.put(Label.parse("¬a¬b¬d¬e"), 881);
		this.actual.put(Label.parse("¬ab¬d¬e"), 818);
		this.actual.put(Label.parse("¬a¬d¬e"), 890);
		this.actual.put(Label.parse("¬a¬c¬de"), 896);
		this.actual.put(Label.parse("¬a"), 916);
		this.actual.put(Label.parse("a¬d¬e"), 711);
		this.actual.put(Label.parse("¬d¬e"), 890);
		this.actual.put(Label.parse("¬c¬de"), 927);
		this.actual.put(Label.emptyLabel, 964);
		this.actual.put(Label.parse("¬a¬b¬d¬e"), 890);
		this.actual.put(Label.parse("¬a¬d¬e"), 899);//!!!
		this.actual.put(Label.parse("¬a¬b¬c¬de"), 373);
		this.actual.put(Label.parse("¬a¬b"), 459);
		this.actual.put(Label.parse("¬a¬c¬de"), 752);
		this.actual.put(Label.parse("¬ab¬d¬e"), 775);
		this.actual.put(Label.parse("a"), 642);
		this.actual.put(Label.parse("a"), 642);
		
		assertEquals("{(916, ⊡) (459, ¬a¬b) (373, ¬a¬b¬c¬de) (775, ¬ab¬d¬e) (752, ¬a¬c¬de) (642, a) (890, ¬d¬e) }", this.actual.toString());
	}

	@SuppressWarnings("javadoc")
	@Test
	public final void caso20160109c() {
		this.actual.clear();
		Label l = Label.parse("¬p¬q");
		this.actual.put(Label.parse("¬p¿q"), -13);
		this.actual.put(l, -13);
		this.actual.put(Label.parse("¿p"), -15);
		this.actual.put(Label.parse("¿p¬q"), -22);
		this.actual.put(Label.emptyLabel, -10);
		this.actual.put(Label.parse("¿p¿q"), Constants.INT_NEG_INFINITE);
		
//		System.out.println(actual);
		this.actual.put(Label.emptyLabel, -22);
//		System.out.println(actual);
		assertEquals("{(-22, ⊡) }", this.actual.toString());
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public final void caso20160109d() {
		this.actual.clear();
		Label l = Label.parse("¬a¬b¬d¬e");
		Label l1 = Label.parse("¬a¬d¬e");
		
		this.actual.put(Label.parse("¬c¬de"), 184);
		this.actual.put(Label.parse("¬a¬b"), 608);
		this.actual.put(Label.parse("¬ab¬d¬e"), 577);
		this.actual.put(Label.parse("¬a"), 625);
		this.actual.put(Label.parse("a¬d¬e"), 470);
		this.actual.put(Label.emptyLabel, 672);
		this.actual.put(Label.parse("¬a¬bd"), 288);
		this.actual.put(Label.parse("¬ad"), 295);
		this.actual.put(Label.parse("d"), 297);
		this.actual.put(l, 399);
		this.actual.put(l1, 406);
		this.actual.put(Label.parse("¬d¬e"), 408);
		this.actual.put(Label.parse("¬de"), 415);
		this.actual.put(Label.parse("¬d¬e"), 306);
//		System.out.println(actual);
		this.actual.put(Label.emptyLabel, -22);
//		System.out.println(actual);
		assertEquals("{(-22, ⊡) }", this.actual.toString());
	}

	@SuppressWarnings("javadoc")
	@Test
	public final void caso20160110() {
		this.actual.clear();
		Label l1 = Label.parse("¬d¬e");
		
		this.actual.put(Label.parse("¬ab¬d¬e"), -143);
//		actual.put(Label.parse("a¬c¬d¿e"), -137);
//		actual.put(Label.parse("¬c¬d¿e"), -99);
		this.actual.put(Label.parse("¿ab¬d¬e"), -164);
//		actual.put(Label.parse("¬a¬b¬d¬e"), 454);
		this.actual.put(Label.parse("a¬d¬e"), 275);
		this.actual.put(l1, -130);
		this.actual.put(l1, -189);
//		System.out.println(actual.entrySet());
		assertEquals("{(-189, ¬d¬e) }", this.actual.toString());
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public final void caso20160111() {
		//valori arco 13S_X5E?
//		currentGraph:	❮13S_X5E?; derived; {(-86, ⊡) (-87, ¬c) (C, -87) (CD, -95) (¿C, -93) }; ❯
//		nextGraph:	    ❮13S_X5E?; derived; {(-86, ⊡) (-87, ¬c) (C, -87) (CD, -95) (¿C, -93) (¿C¬D, -71) }; ❯

		this.actual.clear();
		
		this.actual.put(Label.parse("C¬D"), -50);
		this.actual.put(Label.parse("c"), 27);
		this.actual.put(Label.parse("c"), -2);
		this.actual.put(Label.parse("¬c"), -32);
		this.actual.put(Label.parse("c"), -17);
		this.actual.put(Label.parse("¬c"), -68);
		this.actual.put(Label.parse("¬ac"), -23);
		this.actual.put(Label.parse("¬ac"), -25);
		this.actual.put(Label.parse("¬b¬c"), -71);
		this.actual.put(Label.parse("¬bc"), -20);
		this.actual.put(Label.parse("¿c¬d"), -71);
		this.actual.put(Label.parse("c"), -20);
		this.actual.put(Label.parse("¬c"), -71);
		this.actual.put(Label.parse("c"), -28);
		this.actual.put(Label.parse("¬c"), -79);
		this.actual.put(Label.parse("¬c"), -80);
		this.actual.put(Label.emptyLabel, 0);
		this.actual.put(Label.emptyLabel, -10);
		this.actual.put(Label.parse("c¬d"), -51);
		this.actual.put(Label.parse("c¬d"), -53);
		this.actual.put(Label.parse("cd"), -72);
		this.actual.put(Label.parse("cd"), -84);
		this.actual.put(Label.parse("ac"), -43);
		this.actual.put(Label.parse("a"), -36);
		this.actual.put(Label.parse("¬ac"), -29);
		this.actual.put(Label.parse("¬a"), -22);
		this.actual.put(Label.parse("c¬d"), -54);
		this.actual.put(Label.parse("cd"), -85);
//		System.out.println("actual:" + actual);
		this.actual1 = new LabeledIntHierarchyMap(this.actual);
//		System.out.println("actual1:" + actual1);
		this.actual = this.actual1;
	
		this.actual.put(Label.parse("c"), -76);
		this.actual.put(Label.emptyLabel, -75);
		this.actual.put(Label.parse("¿cd"), -95);
		this.actual.put(Label.parse("¿c"), -83);
		this.actual.put(Label.parse("¿c"), -93);
		this.actual.put(Label.parse("c"), -83);
		this.actual.put(Label.parse("c"), -87);
		this.actual.put(Label.parse("¬c"), -87);
		this.actual.put(Label.emptyLabel, -86);
//		System.out.println("actual:" + actual);
		this.actual1 = new LabeledIntHierarchyMap(this.actual);
//		System.out.println("actual1:" + actual1);
		this.actual = this.actual1;
		this.actual.put(Label.parse("cd"), -95);
		
//		System.out.println("actual:" + actual);
		this.actual1 = new LabeledIntHierarchyMap(this.actual);
//		System.out.println("actual1:" + actual1);
		this.actual = this.actual1;

//		System.out.println(actual);
		this.expected =  AbstractLabeledIntMap.parse("{(-93, ¿c) (-95, cd) (-87, c) (-87, ¬c) (-86, ⊡) }");
		assertEquals(this.expected.toString(), this.actual.toString()); //se ottimizzato
//		assertEquals("{(-86, ⊡) (-87, ¬c) (C, -87) (CD, -95) (¿C, -93) }", actual.toString());//se non ottimizzato
	}
	
	
	
	
	@SuppressWarnings("javadoc")
	@Test
	public final void caso20160112() {
		//valori arco 34E_5E
//		currentGraph:	❮34E_5E; derived; {(-98, ⊡) (-121, ¬a¬b) (A¬bC, -140) (-115, ¬b) (BC, -177) (C, -125) }; ❯
//		nextGraph:	❮34E_5E; derived; {(-98, ⊡) (-121, ¬a¬b) (A¬bC, -140) (-115, ¬b) (BC, -177) (¿BC, -145) (C, -125) }; ❯

		this.actual.clear();
		
		this.actual.put(Label.parse("¬bc"), -89);
		this.actual.put(Label.parse("¬bc"), -111);
		this.actual.put(Label.parse("¬a¬b"), 125);
		this.actual.put(Label.parse("¬b"), 131);
		this.actual.put(Label.parse("¬b"), 125);
		this.actual.put(Label.parse("¬a¬b"), -111);
		this.actual.put(Label.parse("¬b"), -111);
		this.actual.put(Label.parse("¿bc"), -145);
		this.actual.put(Label.parse("a¬bc"), -140);
		this.actual.put(Label.parse("¬bc"), -119);
		this.actual.put(Label.parse("¬a¬bc"), -124);
		this.actual.put(Label.parse("¬a¿b"), -121);
		this.actual.put(Label.parse("¿a¬b"), -121);
		this.actual.put(Label.parse("¬a¬b"), -121);
		this.actual.put(Label.parse("¿b"), -115);
		this.actual.put(Label.parse("a¬b"), -115);
		this.actual.put(Label.parse("ac"), -107);
		this.actual.put(Label.parse("bc"), -150);
		this.actual.put(Label.parse("¬a"), -88);
		this.actual.put(Label.emptyLabel, -86);
		this.actual.put(Label.parse("bc"), -177);
		this.actual.put(Label.emptyLabel, -98);
		this.actual.put(Label.parse("¬a¬bc"), -125);
//		System.out.println(actual);

		this.expected =  AbstractLabeledIntMap.parse("{(-98, ⊡) (-121, ¬a¬b) (-140, a¬bc) (-115, ¬b) (-177, bc) (-125, c) }");
		assertEquals(this.expected.toString(), this.actual.toString()); //se ottimizzato
//		assertEquals("{(-86, ⊡) (-87, ¬c) (C, -87) (CD, -95) (¿C, -93) }", actual.toString());//se non ottimizzato
	}
	
	
	
	/**
	 * Check if the management of the base is correct.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void caso20160109() {

		ObjectArraySet<Integer> set = new ObjectArraySet<>();

		set.add(1);
		set.add(2);
		Integer quattro = new Integer(4);
		set.add(quattro);
		set.add(3);
		Integer cinque = new Integer(5);
		set.add(cinque);
		set.add(6);
		
		StringBuilder seq = new StringBuilder();
		for (Integer i : set) {//Questo for salta il valore 6!
			if (i == null) {
				continue;
			}
			if (i == 3) {//emulo cancellazioni fatte da procedure chiamate dentro il for
				set.remove(quattro);
				set.remove(cinque);
			}
			seq.append(i);seq.append(' ');
		}
		assertEquals("1 2 4 3 ", seq.toString());
		
		seq.setLength(0);
		set.clear();
		set.add(1);
		set.add(2);
		set.add(quattro);
		set.add(3);
		set.add(cinque);
		set.add(6);
		for (Iterator<Integer> iT = set.iterator(); iT.hasNext();) {//Questo for salta il valore 6!
			Integer i = iT.next();
			if (i == null) {
				continue;
			}
			if (i == 3) {//emulo cancellazioni fatte da procedure chiamate dentro il for
				set.remove(quattro);
				set.remove(cinque);
			}
			seq.append(i);seq.append(' ');
		}
		assertEquals("1 2 4 3 ", seq.toString());

		
		seq.setLength(0);
		set.clear();
		set.add(1);
		set.add(2);
		set.add(quattro);
		set.add(3);
		set.add(cinque);
		set.add(6);
		Integer[] iA = set.toArray(new Integer[6]);
		for (Integer i : iA) {//Questo for analizza il 5 che non è più presente!
			if (i == null) {
				continue;
			}
			if (i == 3) {//emulo cancellazioni fatte da procedure chiamate dentro il for
				set.remove(quattro);
				set.remove(cinque);
			}
			seq.append(i);seq.append(' ');
		}
		assertEquals("1 2 4 3 5 6 ", seq.toString());

		seq.setLength(0);
		set.clear();
		set.add(1);
		set.add(2);
		set.add(quattro);
		set.add(3);
		set.add(cinque);
		set.add(6);
		iA = set.toArray(new Integer[6]);
		for (Integer i : iA) {//Questo for è ok!
			if (i == null || !set.contains(i)) {
				continue;
			}
			if (i == 3) {//emulo cancellazioni fatte da procedure chiamate dentro il for
				set.remove(quattro);
				set.remove(cinque);
			}
			seq.append(i);seq.append(' ');
		}
		assertEquals("1 2 4 3 6 ", seq.toString());
		
	}

	
	/**
	 * Check if the management of the base is correct.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void caso20160109list() {

		ObjectArrayList<Integer> list = new ObjectArrayList<>();

		list.add(1);
		list.add(2);
		Integer quattro = new Integer(4);
		list.add(quattro);
		list.add(3);
		Integer cinque = new Integer(5);
		list.add(cinque);
		list.add(6);
		
		StringBuilder seq = new StringBuilder();
		for (Integer i : list) {//Questo for salta il valore 6!
			if (i == null) {
				continue;
			}
			if (i == 3) {//emulo cancellazioni fatte da procedure chiamate dentro il for
				list.remove(quattro);
				list.remove(cinque);
			}
			seq.append(i);seq.append(' ');
		}
		assertEquals("1 2 4 3 ", seq.toString());
		
		seq.setLength(0);
		list.clear();
		list.add(1);
		list.add(2);
		list.add(quattro);
		list.add(3);
		list.add(cinque);
		list.add(6);
		for (Iterator<Integer> iT = list.iterator(); iT.hasNext();) {//Questo for salta il valore 6!
			Integer i = iT.next();
			if (i == null) {
				continue;
			}
			if (i == 3) {//emulo cancellazioni fatte da procedure chiamate dentro il for
				list.remove(quattro);
				list.remove(cinque);
			}
			seq.append(i);seq.append(' ');
		}
		assertEquals("1 2 4 3 ", seq.toString());

		
		seq.setLength(0);
		list.clear();
		list.add(1);
		list.add(2);
		list.add(quattro);
		list.add(3);
		list.add(cinque);
		list.add(6);
		Integer[] iA = list.toArray(new Integer[6]);
		for (Integer i : iA) {//Questo for analizza il 5 che non è più presente!
			if (i == null) {
				continue;
			}
			if (i == 3) {//emulo cancellazioni fatte da procedure chiamate dentro il for
				list.remove(quattro);
				list.remove(cinque);
			}
			seq.append(i);seq.append(' ');
		}
		assertEquals("1 2 4 3 5 6 ", seq.toString());

		seq.setLength(0);
		list.clear();
		list.add(1);
		list.add(2);
		list.add(quattro);
		list.add(3);
		list.add(cinque);
		list.add(6);
		iA = list.toArray(new Integer[6]);
		for (Integer i : iA) {//Questo for è ok!
			if (i == null || !list.contains(i)) {
				continue;
			}
			if (i == 3) {//emulo cancellazioni fatte da procedure chiamate dentro il for
				list.remove(quattro);
				list.remove(cinque);
			}
			seq.append(i);seq.append(' ');
		}
		assertEquals("1 2 4 3 6 ", seq.toString());
		
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
		// E,30 ¬a,25 b,24 ab,23
		this.expected.clear();
		this.expected.put(Label.emptyLabel, 30);
		this.expected.put(Label.parse("¬a"), 25);
		this.expected.put(Label.parse("ab"), 23);
		this.expected.put(Label.parse("b"), 24);

		// System.out.printf("map:"+map);
		assertEquals("Base con valori differenti:\n", this.expected, this.actual); //Se ottimizzato
//		assertEquals("{("+Label.emptyLabel+", 31) (25, ¬a) (24, ¬ab) (30, a) (23, ab) }", actual.toString());//Non ottimizzato
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
		this.expected.put(Label.emptyLabel, 30);
		this.expected.put(Label.parse("¬a"), 25);
		this.expected.put(Label.parse("ab"), 23);
		this.expected.put(Label.parse("b"), 24);

		// System.out.printf("map:"+map);
//		assertTrue("Test di equals con un'altra classe che implementa l'interfaccia:\nexpected:" + expected + ".size: " + expected.size() + "\nactual" + actual
//				+ ".size: " + actual.size()
//				+ "\n", expected.equals(actual));
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void manageTwinDebug() {
		this.actual.clear();
		this.actual.put(Label.parse("¬a¬b"), 30);
		this.actual.put(Label.parse("¬ab"), 24);
		// actual.put(Label.parse("ab"), 23);
		// actual.put(Label.parse("¬a"), 25);
		// actual.put(Label.parse("a"), 30);
		// actual.put(Label.emptyLabel, 31);

		this.expected.clear();
		// expected.put(Label.emptyLabel, 31);
		// expected.put(Label.parse("a"), 30);
		this.expected.put(Label.parse("¬a"), 30);
		// expected.put(Label.parse("ab"), 23);
		this.expected.put(Label.parse("¬ab"), 24);

//		assertTrue("Test di equals:\n", expected.equals(actual));
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
		this.actual.put(Label.emptyLabel, 31);

		this.expected.clear();
		this.expected.put(Label.emptyLabel, 31);
		this.expected.put(Label.parse("a"), 30);
		this.expected.put(Label.parse("¬a"), 25);
		this.expected.put(Label.parse("ab"), 23);
		this.expected.put(Label.parse("b"), 24);

		// System.out.printf("map:"+map);
		assertTrue("Test di equals.\nexpected: " + this.expected
				+ "\nactual: " + this.actual, this.expected.equals(this.actual)); //se ottimizzato
//		assertEquals("{("+Label.emptyLabel+", 31) (25, ¬a) (24, ¬ab) (30, a) (23, ab) }", actual.toString());//Non ottimizzato
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

		Set<Entry<Label>> set = this.actual.entrySet();
		// System.out.println(set);
		for (Iterator<Entry<Label>> ite = set.iterator(); ite.hasNext();) {
			Entry<Label> e = ite.next();
			e.setValue(1);
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
//		assertEquals("Put all da un oggetto di una classe diversa che implementa la stessa interfaccia",
//				AbstractLabeledIntMap.parse("{(30, a) (25, ¬a) (30, b) (25, ¬b) }"), actual);
//		actual.putAll(actual1);
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

		this.actual1 = new LabeledIntHierarchyMap(this.actual);
		this.actual1.put(Label.emptyLabel, 0);

		assertEquals("Put forcibly with a base", this.actual, this.actual1);
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
//		assertEquals("Test about simplification with infinity numbers:\n", expected, actual);
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
		this.expected = AbstractLabeledIntMap.parse("{(10, ab) }");
		assertEquals("Test about simplification with unknown labeled numbers:\nexpected:" + this.expected.size() + "\nactual: " + this.actual.size(), this.expected, this.actual);

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
//		expected = AbstractLabeledIntMap.parse("{(10, abp) (9, ab¬p) }");
//		assertEquals("Test about simplification with unknown labeled numbers:\n", expected, actual);//se ottimizzato
		assertEquals("{(10, ab) (9, ab¬p) }", this.actual.toString());//Ottimizzato

		this.actual.clear();
		this.actual.put(abUp, 9);
		this.actual.put(abp, 10);
		this.actual.put(abNp, 9);
		assertEquals("Test about simplification with unknown labeled numbers:\n", "{(10, ab) (9, ab¬p) }", this.actual.toString());// se ottimizzato
//		assertEquals("{(9, ab¬p) (10, abp) }", actual.toString());//Non ottimizzato

		this.actual.clear();
		this.actual.put(abp, 10);
		this.actual.put(abNp, 9);
		this.actual.put(abUp, 8);
		assertEquals("Test about simplification with unknown labeled numbers:\n", "{(10, ab) (9, ab¬p) (8, ab¿p) }", this.actual.toString());//se ottimizzato
//		assertEquals("{(9, ab¬p) (10, abp) (8, ab¿p) }", actual.toString());//Non ottimizzato
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
//		assertEquals("Test about simplification with unknown labeled infinity:\n", "{(¿p, -∞) }", actual.toString());
	}
}
