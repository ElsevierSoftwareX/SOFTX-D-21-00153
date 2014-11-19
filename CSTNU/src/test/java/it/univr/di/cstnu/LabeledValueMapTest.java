/**
 * 
 */
package it.univr.di.cstnu;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author posenato
 */
public class LabeledValueMapTest {

	/**
	 * 
	 */
	final LabeledValueMap<Integer> map = new LabeledValueMap<>();
	/**
	 * 
	 */
	final LabeledValueMap<Integer> map1 = new LabeledValueMap<>();

	/**
	 * 
	 */
	final LabeledValueMap<Integer> result = new LabeledValueMap<>();
	
	
	
	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void eliminazione() {
		Label l1 = Label.parse("¬a¬b");
		map.clear();
		map.mergeLabeledValue(Label.emptyLabel, 109);
		map.mergeLabeledValue(l1, 21);

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
		map.mergeLabeledValue(Label.emptyLabel, 109);
		map.mergeLabeledValue(Label.parse("¬a¬b"), 21);
		map.mergeLabeledValue(Label.parse("¬ab"), 21);

		result.clear();
		result.putLabeledValue(Label.emptyLabel, 109);
		result.putLabeledValue(Label.parse("¬a"), 21);
		
		assertEquals("Test inserimento due label di pari valore con un letterale opposto:\n", result, map);
	}

	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneRicorsivaTest() {
		map.clear();
		map.mergeLabeledValue(Label.emptyLabel, 109);
		map.putLabeledValue(Label.parse("a"), 21);
		map.mergeLabeledValue(Label.parse("¬a¬b"), 21);
		map.mergeLabeledValue(Label.parse("¬ab"), 21);

		result.clear();
		result.putLabeledValue(Label.emptyLabel, 21);
		
		assertEquals("Test inserimento due label di pari valore con un letterale opposto con ricorsione:\n", result, map);
	}


	
	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase1Test() {
		map.clear();
		map.mergeLabeledValue(Label.emptyLabel, 109);
		map.mergeLabeledValue(Label.parse("ab"), 10);
		map.mergeLabeledValue(Label.parse("a"), 25);
		map.mergeLabeledValue(Label.parse("¬b"), 23);
		map.mergeLabeledValue(Label.parse("b"), 22);
		map.mergeLabeledValue(Label.parse("a¬b"), 23);
		map.mergeLabeledValue(Label.parse("ab"), 20);
		
		result.clear();
		result.putLabeledValue(Label.parse("¬b"), 23);
		result.putLabeledValue(Label.parse("b"), 22);
		result.putLabeledValue(Label.parse("ab"), 10);
		
		assertEquals("Test su creazione base A INIZIO e gestione semplificazioni:\n", result, map);
	}
	
	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase2Test() {
		map.clear();
		map.mergeLabeledValue(Label.emptyLabel, 109);
		map.mergeLabeledValue(Label.parse("ab"), 10);
		map.mergeLabeledValue(Label.parse("a¬b"), 23);
		map.mergeLabeledValue(Label.parse("¬b"), 23);
		map.mergeLabeledValue(Label.parse("b"), 22);
		map.mergeLabeledValue(Label.parse("ab"), 20);
		
		result.clear();
		result.putLabeledValue(Label.parse("¬b"), 23);
		result.putLabeledValue(Label.parse("b"), 22);
		result.putLabeledValue(Label.parse("ab"), 10);
		
		assertEquals("Test su creazione base IN MEZZO e gestione semplificazioni:\n", result, map);
	}
	
	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase3Test() {
		map.clear();
		map.mergeLabeledValue(Label.emptyLabel, 109);
		map.mergeLabeledValue(Label.parse("A¬b"), 23);
		map.mergeLabeledValue(Label.parse("ab"), 20);
		map.mergeLabeledValue(Label.parse("¬b"), 23);
		map.mergeLabeledValue(Label.parse("b"), 23);
		
		result.clear();
		result.putLabeledValue(Label.emptyLabel, 23);
		result.putLabeledValue(Label.parse("ab"), 20);
		
		assertEquals("Test su creazione base IN FINE e gestione semplificazioni:\n", result, map);
	}

	
	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void semplificazioneBase4Test() {
		map.clear();
		map.mergeLabeledValue(Label.emptyLabel, 109);
		map.mergeLabeledValue(Label.parse("ab"), 10);
		map.mergeLabeledValue(Label.parse("a¬b"), 19);
		map.mergeLabeledValue(Label.parse("¬ab"), 10);
		map.mergeLabeledValue(Label.parse("¬a¬b"), 19);
		//Map: {(¬a¬b, 19) (¬aB, 10) (A¬b, 19) (AB, 10) }
		
		result.clear();
		result.putLabeledValue(Label.parse("b"), 10);
		result.putLabeledValue(Label.parse("¬b"), 19);
		
		assertEquals("Test su creazione e gestione semplificazioni:\n", result, map);

		map.mergeLabeledValue(Label.parse("¬b"), 10);
		result.clear();
		result.putLabeledValue(Label.emptyLabel, 10);
		
		assertEquals("Test su creazione e gestione semplificazioni:\n", result, map);
		
	}
	
	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void insert0() {
		map.clear();
		map.mergeLabeledValue(Label.emptyLabel, 0);
		
		result.clear();
		result.putLabeledValue(Label.emptyLabel, 0);
		assertEquals("Test su aggiunta di (empty,0):\n", result, map);
	}
	
	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void distruzioneBase() {
		map.clear();
		map.mergeLabeledValue(Label.emptyLabel, 100);
		map.mergeLabeledValue(Label.parse("ab"), 50);
		map.mergeLabeledValue(Label.parse("a¬b"), 59);
		map.mergeLabeledValue(Label.parse("¬aB"), 60);
		map.mergeLabeledValue(Label.parse("¬a¬b"), 69);
		map.mergeLabeledValue(Label.parse("¬a"), 30);
		map.mergeLabeledValue(Label.parse("a"), 30);
		
		
		result.clear();
		result.putLabeledValue(Label.emptyLabel, 30);
		assertEquals("Test di distruzione base:\n", result, map);
	}
	
	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void distruzioneBase2() {
		map.clear();
		map.mergeLabeledValue(Label.parse("b"), -12);
		map.mergeLabeledValue(Label.parse("¬b"), -1);
		map.mergeLabeledValue(Label.parse("¬a"), -10);
		
		map.mergeLabeledValue(Label.emptyLabel, -10);
		
		
		result.clear();
		result.putLabeledValue(Label.emptyLabel, -10);
		result.putLabeledValue(Label.parse("b"), -12);
		assertEquals("Test di distruzione base 2:\n", result, map);
	}
	
	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void caso20141021() {
		map.clear();
		map.mergeLabeledValue(Label.parse("¬b"), 29);
		map.mergeLabeledValue(Label.parse("a"), 26);
		map.mergeLabeledValue(Label.parse("ab"), 25);
		map.mergeLabeledValue(Label.emptyLabel, 30);
		map.mergeLabeledValue(Label.parse("¬ab"), 28);
		map.mergeLabeledValue(Label.parse("¬a"), 25);
		map.mergeLabeledValue(Label.parse("¬aB"), 23);
		map.mergeLabeledValue(Label.parse("a¬b"), 24);
		
		result.clear();
		result.putLabeledValue(Label.parse("a"), 26);
		result.putLabeledValue(Label.parse("ab"), 25);
		result.putLabeledValue(Label.parse("¬a"), 25);
		result.putLabeledValue(Label.parse("¬aB"), 23);
		result.putLabeledValue(Label.parse("a¬b"), 24);
		
		System.out.printf("map:"+map);
		assertEquals("Rimuovo componenti maggiori con elementi consistenti della base:\n", result, map);
	}
	
	/**
	 * Check if the management of the base is correct.
	 */
	@Test
	public final void casoBaseConValoriDistinti() {
		map.clear();
		map.mergeLabeledValue(Label.parse("a"), 30);
		map.mergeLabeledValue(Label.parse("¬a"), 25);
		map.mergeLabeledValue(Label.emptyLabel, 31);
		map.mergeLabeledValue(Label.parse("¬ab"), 24);
		map.mergeLabeledValue(Label.parse("¬a¬b"), 30);
		map.mergeLabeledValue(Label.parse("ab"), 23);
		
		result.clear();
		result.putLabeledValue(Label.parse("a"), 30);
		result.putLabeledValue(Label.parse("¬a"), 25);
		result.putLabeledValue(Label.parse("ab"), 23);
		result.putLabeledValue(Label.parse("¬ab"), 24);
		
		System.out.printf("map:"+map);
		assertEquals("Base con valori differenti:\n", result, map);
	}
}
