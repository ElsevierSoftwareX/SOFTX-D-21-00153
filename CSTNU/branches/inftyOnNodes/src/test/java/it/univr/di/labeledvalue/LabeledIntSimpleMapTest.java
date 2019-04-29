/**
 * 
 */
package it.univr.di.labeledvalue;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectSet;

/**
 * @author posenato
 */
public class LabeledIntSimpleMapTest {

	/**
	 * 
	 */
	LabeledIntSimpleMap map;

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.map = new LabeledIntSimpleMap();
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.LabeledIntSimpleMap#entrySet()}.
	 */
	@Test
	public void testEntrySet() {
		this.map.put(Label.emptyLabel, 0);
		this.map.put(Label.parse("a"), -1);
		this.map.put(Label.parse("¬a"), -1);
		this.map.put(Label.parse("b"), -1);

		assertEquals("{(0, ⊡) (-1, a) (-1, ¬a) (-1, b) }", this.map.toString());
		ObjectSet<Entry<Label>> entrySet = this.map.entrySet();
		assertEquals("{b=>-1, ¬a=>-1, a=>-1, ⊡=>0}", entrySet.toString());

		this.map.remove(Label.parse("¬a"));
		assertEquals("{b=>-1, null=>-1, a=>-1, ⊡=>0}", entrySet.toString());

		entrySet.remove(new AbstractObject2IntMap.BasicEntry<>(Label.parse("a"), -1));
		assertEquals("{(0, ⊡) (-1, a) (-1, b) }", this.map.toString());
		assertEquals("{b=>-1, null=>-1, ⊡=>0}", entrySet.toString());
	}

	/**
	 * Test method for {@link it.univr.di.labeledvalue.LabeledIntSimpleMap#keySet()}.
	 */
	@Test
	public void testKeySet() {
		this.map.put(Label.emptyLabel, 0);
		this.map.put(Label.parse("a"), -1);
		this.map.put(Label.parse("¬a"), -1);
		this.map.put(Label.parse("b"), -1);

		assertEquals("{(0, ⊡) (-1, a) (-1, ¬a) (-1, b) }", this.map.toString());
		ObjectSet<Label> entrySet = this.map.keySet();
		assertEquals("{b, ¬a, a, ⊡}", entrySet.toString());

		this.map.remove(Label.parse("¬a"));
		assertEquals("{b, ¬a, a, ⊡}", entrySet.toString());
	}

}
