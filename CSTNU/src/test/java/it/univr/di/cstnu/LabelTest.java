/**
 * 
 */
package it.univr.di.cstnu;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author posenato
 *
 */
public class LabelTest {

	/**
	 * 
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void  parse() {
		Label a = new Label(new Literal('a'));
		a.conjunct((new Literal('b')).negation());
		a.conjunct((new Literal('b')).negation());
		a.conjunct((new Literal('c')).negation());
		Label aParse = Label.parse("a¬b¬c");
		
		assertEquals("Test confronto label creata con costrutture e creata con parse: ", a, aParse);
	}

}
