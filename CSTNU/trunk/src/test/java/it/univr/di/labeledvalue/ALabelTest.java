/**
 * 
 */
package it.univr.di.labeledvalue;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;

/**
 * @author posenato
 *
 */
public class ALabelTest {
	
	@SuppressWarnings("javadoc")
	ALabel a,b,c,e;
	@SuppressWarnings("javadoc")
	ALabelAlphabet alpha ;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.alpha = new ALabelAlphabet();
		this.a = new ALabel(new ALetter("A"), this.alpha);
		this.b = new ALabel(new ALetter("b"), this.alpha);
		this.c = new ALabel(new ALetter("C"), this.alpha);
		this.e = ALabel.emptyLabel;
	}

	@SuppressWarnings("javadoc")
	@Test
	public void creation() {
		assertEquals("A", this.a.toString());
		assertEquals("b", this.b.toString());
		assertEquals("C", this.c.toString());
		assertEquals("◇", this.e.toString());
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public void conjunction() {
		this.a = this.a.conjunction(this.b);
		assertEquals("A∙b",  this.a.toString());

		this.a = this.a.conjunction(this.e);
		assertEquals("A∙b",  this.a.toString());
		assertEquals("A∙b",  this.e.conjunction(this.a).toString());
		assertEquals("A∙b",  this.a.conjunction(this.a).toString());

		this.a = this.a.conjunction(this.c);
		assertEquals("A∙b∙C",  this.a.toString());
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public void remove() {
		this.a = this.a.conjunction(this.b).conjunction(this.c);
		this.a.remove(new ALetter("C"));
		assertEquals("A∙b",  this.a.toString());
		this.e.remove((ALetter) null);
		assertEquals("◇", this.e.toString());
	}
	@SuppressWarnings("javadoc")
	@Test
	public void contains() {
		this.a = this.a.conjunction(this.b);
		assertTrue(this.a.contains(new ALetter("b")));
		assertTrue(this.a.contains(this.b));
		assertFalse(this.a.contains(new ALetter("C")));
		assertFalse(this.a.contains(this.c));
		
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public void compare(){
		assertTrue(this.a.compareTo(this.c)<0);
		assertTrue(this.a.compareTo(this.a)==0);
		assertTrue(this.c.compareTo(this.a)>0);
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public void array() {
		this.a = this.a.conjunction(this.c);
		System.out.println("ALabel A: " + this.a);
		ALetter[] a1 =new ALetter[3];
		int i=0;
		for(ALetter l: this.a) {
			a1[i++]=l;
		}
		assertEquals("C", a1[1].toString());
	}

	@SuppressWarnings("javadoc")
	@Test
	public void intersect(){
		this.a = this.a.conjunction(this.c);
		assertEquals("◇", this.a.intersect(this.e).toString());
		assertEquals(this.a, this.a.intersect(this.a));
		assertNotEquals(this.b, this.a.intersect(this.b));
		assertEquals(this.c, this.a.intersect(this.c));
		assertEquals("A", this.a.intersect(new ALabel("A", this.a.getAlphabet())).toString());
	}
}
