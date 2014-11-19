package it.univr.di.cstnu;

import java.util.AbstractMap.SimpleEntry;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * @author posenato
 */
public class LabeledCaseCValueMapTest {

	/**
	 * 
	 */
	@SuppressWarnings("javadoc")
	LabeledCaseCValueMap<Integer> map = new LabeledCaseCValueMap<>(), result = new LabeledCaseCValueMap<>();

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {}

	/**
	 * 
	 */
	@Test
	public final void parse() {

		map = LabeledCaseCValueMap.parse("{(¬A, N9, -12) (A, N10, -11) (" + Label.emptyLabel + ", N9, -12)}");

		// System.out.printf("Map da parse: %s\n", map);

		result.clear();
		result.putTriple(Label.emptyLabel, "N9", -12);
		result.mergeTriple("A", "N10", -11);

		assertEquals("Check of parse method", result, map);
	}

	
	/**
	 * 
	 */
	@Test
	public final void mergeConSemplificazione() {
		map.clear();	
	map.mergeTriple("¬A¬B", "N9", 13);
	map.mergeTriple("¬A", "N9", 13);
	
	result.clear();
	result.mergeTriple("¬A","N9",13);
	

	assertEquals("Check of merge with simple simplification", result, map);
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
	
	result.clear();
	result.mergeTriple(Label.emptyLabel,"N9",12);
	

	assertEquals("Check of merge with double simple simplification", result, map);
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
	
	result.clear();
	result.mergeTriple(Label.emptyLabel,"N9",12);
	

	assertEquals("Check of merge with double simple simplification and add useless value", result, map);
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
	
	result.clear();
	result.mergeTriple(Label.emptyLabel, "N9", 12);
	result.mergeTriple("¬A¬B","N9",11);
	

	assertEquals("Check of merge with a final overwriting value", result, map);
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
	
	result.clear();
	result.mergeTriple(Label.emptyLabel, "N9", 12);
	result.mergeTriple("¬A¬B","N9",11);
	result.mergeTriple("¬A¬B","N8",11);
	

	assertEquals("Check of merge with two different node\n", result, map);
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
	
	result.clear();
	result.mergeTriple(Label.emptyLabel,"N9",12);
	result.mergeTriple("¬A","N9",11);
	result.mergeTriple("¬A¬B","N8",11);
	

	assertEquals("Check of merge with two different node.", result, map);
	}
	
	
	/**
	 * 
	 */
	@SuppressWarnings("cast")
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
	
	Set<Entry<Entry<Label, String>, Integer>> set= map.labeledTripleSet();
	
	Set<Entry<Entry<Label, String>, Integer>> set1 = new HashSet<>();
	
	set1.add((Entry<Entry<Label, String>, Integer>) new SimpleEntry<>( ((Entry<Label, String>) new SimpleEntry<>(Label.parse("¬AB"), "N7")), 11) );
	set1.add((Entry<Entry<Label, String>, Integer>) new SimpleEntry<>( ((Entry<Label, String>) new SimpleEntry<>(Label.parse("¬A¬B"), "N9")), 11) );
	set1.add((Entry<Entry<Label, String>, Integer>) new SimpleEntry<>( ((Entry<Label, String>) new SimpleEntry<>(Label.parse("¬A¬B"), "N8")), 11) );
	set1.add((Entry<Entry<Label, String>, Integer>) new SimpleEntry<>( ((Entry<Label, String>) new SimpleEntry<>(Label.emptyLabel, "N9")), 12) );
	

	assertEquals("Generatio of set of triple\n", set1, set);
	}
}
