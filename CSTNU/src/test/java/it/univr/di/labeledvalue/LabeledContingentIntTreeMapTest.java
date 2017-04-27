package it.univr.di.labeledvalue;

import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

/**
 * @author posenato
 */
public class LabeledContingentIntTreeMapTest {

	/**
	 *
	 */
	@SuppressWarnings("javadoc")
	LabeledContingentIntTreeMap map,
			result;

	/**
	 * 
	 */
	ALabelAlphabet alpha;
	
	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.map = new LabeledContingentIntTreeMap();
		this.result = new LabeledContingentIntTreeMap();
		this.alpha = new ALabelAlphabet();
	}

	/**
	 *
	 */
	@Test
	public final void generazioneSet() {
		this.map.clear();
		this.map.mergeTriple("¬a¬b", new ALabel("N9", this.alpha), 13);
		this.map.mergeTriple("¬a", new ALabel("N9", this.alpha), 13);
		this.map.mergeTriple(Label.emptyLabel, new ALabel("N9", this.alpha), 12);
		this.map.mergeTriple(Label.parse("¬a"), new ALabel("N9", this.alpha), 14);
		this.map.mergeTriple(Label.parse("¬a¬b"), new ALabel("N9", this.alpha), 11);
		this.map.mergeTriple(Label.parse("¬a¬b"), new ALabel("N8", this.alpha), 11);
		this.map.mergeTriple(Label.parse("¬ab"), new ALabel("N7", this.alpha), 11);
		this.map.mergeTriple(Label.parse("¬ba"), new ALabel("N9", this.alpha), 15);
		this.map.mergeTriple(Label.parse("ab"), new ALabel("N6", this.alpha), 1);

		final ObjectSet<Object2IntMap.Entry<Entry<Label, ALabel>>> set = this.map.labeledTripleSet();

		final ObjectSet<Object2IntMap.Entry<Entry<Label, ALabel>>> set1 = new ObjectArraySet<>();

		set1.add(new AbstractObject2IntMap.BasicEntry<Entry<Label, ALabel>>((new SimpleEntry<>(Label.parse("¬ab"), new ALabel("N7", this.alpha))), 11));
		set1.add(new AbstractObject2IntMap.BasicEntry<Entry<Label, ALabel>>((new SimpleEntry<>(Label.parse("ab"), new ALabel("N6", this.alpha))), 1));
		set1.add(new AbstractObject2IntMap.BasicEntry<Entry<Label, ALabel>>((new SimpleEntry<>(Label.parse("¬a¬b"), new ALabel("N9", this.alpha))), 11));
		set1.add(new AbstractObject2IntMap.BasicEntry<Entry<Label, ALabel>>((new SimpleEntry<>(Label.parse("¬a¬b"), new ALabel("N8", this.alpha))), 11));
		set1.add(new AbstractObject2IntMap.BasicEntry<Entry<Label, ALabel>>((new SimpleEntry<>(Label.emptyLabel, new ALabel("N9", this.alpha))), 12));

		Assert.assertEquals("Generation of set of triple\n", set1, set);
	}

	/**
	 *
	 */
	@Test
	public final void mergeConSemplificazione() {
		this.map.clear();
		this.map.mergeTriple("¬a¬b", new ALabel("N9", this.alpha), 13);
		this.map.mergeTriple("¬a", new ALabel("N9", this.alpha), 13);

		this.result.clear();
		this.result.mergeTriple("¬a", new ALabel("N9", this.alpha), 13);

		Assert.assertEquals("Check of merge with simple simplification", this.result, this.map);
	}

	/**
	 *
	 */
	@Test
	public final void mergeConSemplificazione1() {
		this.map.clear();
		this.map.mergeTriple("¬a¬b", new ALabel("N9", this.alpha), 13);
		this.map.mergeTriple("¬a", new ALabel("N9", this.alpha), 13);
		this.map.mergeTriple(Label.emptyLabel, new ALabel("N9", this.alpha), 12);

		this.result.clear();
		this.result.mergeTriple(Label.emptyLabel, new ALabel("N9", this.alpha), 12);

		Assert.assertEquals("Check of merge with double simple simplification", this.result, this.map);
	}

	/**
	 *
	 */
	@Test
	public final void mergeConSemplificazione2() {
		this.map.clear();
		this.map.mergeTriple("¬a¬b", new ALabel("N9", this.alpha), 13);
		this.map.mergeTriple("¬a", new ALabel("N9", this.alpha), 13);
		this.map.mergeTriple(Label.emptyLabel, new ALabel("N9", this.alpha), 12);
		this.map.mergeTriple(Label.parse("¬a"), new ALabel("N9", this.alpha), 14);

		this.result.clear();
		this.result.mergeTriple(Label.emptyLabel, new ALabel("N9", this.alpha), 12);

		Assert.assertEquals("Check of merge with double simple simplification and add useless value", this.result, this.map);
	}

	/**
	 *
	 */
	@Test
	public final void mergeConSemplificazione3() {
		this.map.clear();
		this.map.mergeTriple("¬a¬b", new ALabel("N9", this.alpha), 13);
		this.map.mergeTriple("¬a", new ALabel("N9", this.alpha), 13);
		this.map.mergeTriple(Label.emptyLabel, new ALabel("N9", this.alpha), 12);
		this.map.mergeTriple(Label.parse("¬a"), new ALabel("N9", this.alpha), 14);
		this.map.mergeTriple(Label.parse("¬a¬b"), new ALabel("N9", this.alpha), 11);

		this.result.clear();
		this.result.mergeTriple(Label.emptyLabel, new ALabel("N9", this.alpha), 12);
		this.result.mergeTriple("¬a¬b", new ALabel("N9", this.alpha), 11);

		Assert.assertEquals("Check of merge with a final overwriting value", this.result, this.map);
	}

	/**
	 *
	 */
	@Test
	public final void mergeConSemplificazione4() {
		this.map.clear();
		this.map.mergeTriple("¬a¬b", new ALabel("N9", this.alpha), 13);
		this.map.mergeTriple("¬a", new ALabel("N9", this.alpha), 13);
		this.map.mergeTriple(Label.emptyLabel, new ALabel("N9", this.alpha), 12);
		this.map.mergeTriple(Label.parse("¬a"), new ALabel("N9", this.alpha), 14);
		this.map.mergeTriple(Label.parse("¬a¬b"), new ALabel("N9", this.alpha), 11);
		this.map.mergeTriple(Label.parse("¬a¬b"), new ALabel("N8", this.alpha), 11);

		this.result.clear();
		this.result.mergeTriple(Label.emptyLabel, new ALabel("N9", this.alpha), 12);
		this.result.mergeTriple("¬a¬b", new ALabel("N9", this.alpha), 11);
		this.result.mergeTriple("¬a¬b", new ALabel("N8", this.alpha), 11);

		Assert.assertEquals("Check of merge with two different node\n", this.result, this.map);
	}

	/**
	 *
	 */
	@Test
	public final void mergeConSemplificazione5() {
		this.map.clear();
		this.map.mergeTriple("¬a¬b", new ALabel("N9", this.alpha), 13);
		this.map.mergeTriple("¬a", new ALabel("N9", this.alpha), 13);
		this.map.mergeTriple(Label.emptyLabel, new ALabel("N9", this.alpha), 12);
		this.map.mergeTriple(Label.parse("¬a"), new ALabel("N9", this.alpha), 14);
		this.map.mergeTriple(Label.parse("¬a¬b"), new ALabel("N9", this.alpha), 11);
		this.map.mergeTriple(Label.parse("¬ab"), new ALabel("N9", this.alpha), 11);
		this.map.mergeTriple(Label.parse("¬ba"), new ALabel("N9", this.alpha), 12);
		this.map.mergeTriple(Label.parse("ab"), new ALabel("N9", this.alpha), 11);
		this.map.mergeTriple(Label.parse("¬a¬b"), new ALabel("N8", this.alpha), 11);

		this.result.clear();
		this.result.mergeTriple(Label.emptyLabel, new ALabel("N9", this.alpha), 12);
		this.result.mergeTriple("¬a", new ALabel("N9", this.alpha), 11);
		this.result.mergeTriple("ab", new ALabel("N9", this.alpha), 11);
		this.result.mergeTriple("¬a¬b", new ALabel("N8", this.alpha), 11);

		Assert.assertEquals("Check of merge with two different node,\n", this.result, this.map);
	}

	/**
	 *
	 */
	@Test
	public final void parse() {

		this.map = LabeledContingentIntTreeMap.parse("{(¬a, N9, -12) (a, N10, -11) (" + Label.emptyLabel + ", N9, -12)}",this.alpha);

		// System.out.printf("Map da parse: %s\n", map);

		this.result.clear();
		this.result.putTriple(Label.emptyLabel, new ALabel("N9", this.alpha), -12);
		this.result.mergeTriple("a", new ALabel("N10", this.alpha), -11);

		Assert.assertEquals("Check of parse method", this.result, this.map);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void minValue() {
		this.map.clear();
		this.map = LabeledContingentIntTreeMap.parse("{(¬a, N9, -12) (a, N10, -11) (" + Label.emptyLabel + ", N9, -14) }",this.alpha);

		assertTrue(this.map.getMinValue() == -14);
		this.map.clear();

		assertTrue(this.map.getMinValue() == Constants.INT_NULL);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void minValueConsisntenWithTest() {
		this.map.clear();
		this.map = LabeledContingentIntTreeMap.parse("{(¬a, N9, -12) (a, N10, -11) (" + Label.emptyLabel + ", N9, -14) }",this.alpha);
//		System.out.println(this.map);
		assertTrue(this.map.getMinValue() == -14);
		this.map.clear();
		assertTrue(this.map.getMinValue() == Constants.INT_NULL);

		this.map = LabeledContingentIntTreeMap.parse("{(¬a, N9, -12) (a, N10, -11) (" + Label.emptyLabel + ", N9, -14)}",this.alpha);
		// System.out.println(map.getMinValueConsistentWith(Label.parse("¬a"), new ALabel("N9", alpha)) );
		assertTrue(this.map.getMinValueConsistentWith(Label.parse("¬a"), new ALabel("N9", this.alpha)) == -14);
		assertTrue(this.map.getMinValueConsistentWith(Label.parse("¬a"), new ALabel("N11", this.alpha)) == Constants.INT_NULL);
	}

}
