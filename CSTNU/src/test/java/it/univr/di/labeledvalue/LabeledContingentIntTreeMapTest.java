package it.univr.di.labeledvalue;

import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author posenato
 */
public class LabeledContingentIntTreeMapTest {

	/**
	 *
	 */
	@SuppressWarnings("javadoc")
	LabeledContingentIntTreeMap map = new LabeledContingentIntTreeMap(true), result = new LabeledContingentIntTreeMap(true);

	/**
	 *
	 */
	@Test
	public final void generazioneSet() {
		map.clear();
		map.mergeTriple("¬A¬B", "N9", 13);
		map.mergeTriple("¬A", "N9", 13);
		map.mergeTriple(Label.emptyLabel, "N9", 12);
		map.mergeTriple(Label.parse("¬A"), "N9", 14);
		map.mergeTriple(Label.parse("¬A¬B"), "N9", 11);
		map.mergeTriple(Label.parse("¬A¬B"), "N8", 11);
		map.mergeTriple(Label.parse("¬AB"), "N7", 11);
		map.mergeTriple(Label.parse("¬BA"), "N9", 15);
		map.mergeTriple(Label.parse("AB"), "N6", 1);

		final Set<Object2IntMap.Entry<Entry<Label, String>>> set = map.labeledTripleSet();

		final Set<Object2IntMap.Entry<Entry<Label, String>>> set1 = new ObjectArraySet<>();

		set1.add(new AbstractObject2IntMap.BasicEntry<Entry<Label, String>>((new SimpleEntry<>(Label.parse("¬AB"), "N7")), 11));
		set1.add(new AbstractObject2IntMap.BasicEntry<Entry<Label, String>>((new SimpleEntry<>(Label.parse("AB"), "N6")), 1));
		set1.add(new AbstractObject2IntMap.BasicEntry<Entry<Label, String>>((new SimpleEntry<>(Label.parse("¬A¬B"), "N9")), 11));
		set1.add(new AbstractObject2IntMap.BasicEntry<Entry<Label, String>>((new SimpleEntry<>(Label.parse("¬A¬B"), "N8")), 11));
		set1.add(new AbstractObject2IntMap.BasicEntry<Entry<Label, String>>((new SimpleEntry<>(Label.emptyLabel, "N9")), 12));

		Assert.assertEquals("Generation of set of triple\n", set1, set);
	}

	/**
	 *
	 */
	@Test
	public final void mergeConSemplificazione() {
		map.clear();
		map.mergeTriple("¬A¬B", "N9", 13);
		map.mergeTriple("¬A", "N9", 13);

		this.result.clear();
		this.result.mergeTriple("¬A", "N9", 13);

		Assert.assertEquals("Check of merge with simple simplification", this.result, map);
	}

	/**
	 *
	 */
	@Test
	public final void mergeConSemplificazione1() {
		map.clear();
		map.mergeTriple("¬A¬B", "N9", 13);
		map.mergeTriple("¬A", "N9", 13);
		map.mergeTriple(Label.emptyLabel, "N9", 12);

		this.result.clear();
		this.result.mergeTriple(Label.emptyLabel, "N9", 12);

		Assert.assertEquals("Check of merge with double simple simplification", this.result, map);
	}

	/**
	 *
	 */
	@Test
	public final void mergeConSemplificazione2() {
		map.clear();
		map.mergeTriple("¬A¬B", "N9", 13);
		map.mergeTriple("¬A", "N9", 13);
		map.mergeTriple(Label.emptyLabel, "N9", 12);
		map.mergeTriple(Label.parse("¬A"), "N9", 14);

		this.result.clear();
		this.result.mergeTriple(Label.emptyLabel, "N9", 12);

		Assert.assertEquals("Check of merge with double simple simplification and add useless value", this.result, map);
	}

	/**
	 *
	 */
	@Test
	public final void mergeConSemplificazione3() {
		map.clear();
		map.mergeTriple("¬A¬B", "N9", 13);
		map.mergeTriple("¬A", "N9", 13);
		map.mergeTriple(Label.emptyLabel, "N9", 12);
		map.mergeTriple(Label.parse("¬A"), "N9", 14);
		map.mergeTriple(Label.parse("¬A¬B"), "N9", 11);

		this.result.clear();
		this.result.mergeTriple(Label.emptyLabel, "N9", 12);
		this.result.mergeTriple("¬A¬B", "N9", 11);

		Assert.assertEquals("Check of merge with a final overwriting value", this.result, map);
	}

	/**
	 *
	 */
	@Test
	public final void mergeConSemplificazione4() {
		map.clear();
		map.mergeTriple("¬A¬B", "N9", 13);
		map.mergeTriple("¬A", "N9", 13);
		map.mergeTriple(Label.emptyLabel, "N9", 12);
		map.mergeTriple(Label.parse("¬A"), "N9", 14);
		map.mergeTriple(Label.parse("¬A¬B"), "N9", 11);
		map.mergeTriple(Label.parse("¬A¬B"), "N8", 11);

		this.result.clear();
		this.result.mergeTriple(Label.emptyLabel, "N9", 12);
		this.result.mergeTriple("¬A¬B", "N9", 11);
		this.result.mergeTriple("¬A¬B", "N8", 11);

		Assert.assertEquals("Check of merge with two different node\n", this.result, map);
	}

	/**
	 *
	 */
	@Test
	public final void mergeConSemplificazione5() {
		map.clear();
		map.mergeTriple("¬A¬B", "N9", 13);
		map.mergeTriple("¬A", "N9", 13);
		map.mergeTriple(Label.emptyLabel, "N9", 12);
		map.mergeTriple(Label.parse("¬A"), "N9", 14);
		map.mergeTriple(Label.parse("¬A¬B"), "N9", 11);
		map.mergeTriple(Label.parse("¬AB"), "N9", 11);
		map.mergeTriple(Label.parse("¬BA"), "N9", 12);
		map.mergeTriple(Label.parse("AB"), "N9", 11);
		map.mergeTriple(Label.parse("¬A¬B"), "N8", 11);

		this.result.clear();
		this.result.mergeTriple(Label.emptyLabel, "N9", 12);
		this.result.mergeTriple("¬A", "N9", 11);
		this.result.mergeTriple("AB", "N9", 11);
		this.result.mergeTriple("¬A¬B", "N8", 11);

		Assert.assertEquals("Check of merge with two different node,\n", this.result, map);
	}

	/**
	 *
	 */
	@Test
	public final void parse() {

		map = LabeledContingentIntTreeMap.parse("{(¬A, N9, -12) (A, N10, -11) (" + Label.emptyLabel + ", N9, -12)}", true);

		// System.out.printf("Map da parse: %s\n", map);

		this.result.clear();
		this.result.putTriple(Label.emptyLabel, "N9", -12);
		this.result.mergeTriple("A", "N10", -11);

		Assert.assertEquals("Check of parse method", this.result, map);
	}

	
	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void minValue() {
		map.clear();
		map = LabeledContingentIntTreeMap.parse("{(¬a, N9, -12) (a, N10, -11) (" + Label.emptyLabel + ", N9, -14)}", true);

		assertTrue(map.getMinValue()==-14);
		map.clear();
		
		assertTrue(map.getMinValue()==LabeledIntMap.INT_NULL);
	}
	
	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void minValueConsisntenWithTest() {
		map.clear();
		map = LabeledContingentIntTreeMap.parse("{(¬a, N9, -12) (a, N10, -11) (" + Label.emptyLabel + ", N9, -14)}", true);

		assertTrue(map.getMinValue()==-14);
		map.clear();
		assertTrue(map.getMinValue()==LabeledIntMap.INT_NULL);

		map = LabeledContingentIntTreeMap.parse("{(¬a, N9, -12) (a, N10, -11) (" + Label.emptyLabel + ", N9, -14)}", true);
		assertTrue(map.getMinValueConsistentWith(Label.parse("¬a"),"N9")==-14);
		assertTrue(map.getMinValueConsistentWith(Label.parse("¬a"),"N11")==LabeledIntMap.INT_NULL);
	}
	
	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {}
}
