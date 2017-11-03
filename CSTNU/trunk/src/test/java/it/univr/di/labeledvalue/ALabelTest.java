/**
 * 
 */
package it.univr.di.labeledvalue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;

/**
 * @author posenato
 */
public class ALabelTest {

	@SuppressWarnings("javadoc")
	ALabel a, b, c, e;
	@SuppressWarnings("javadoc")
	ALabelAlphabet alpha;

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
		assertEquals("A∙b", this.a.toString());

		assertEquals("A∙b", this.a.conjunction(this.e).toString());
		assertEquals("A∙b", this.e.conjunction(this.a).toString());
		assertEquals("A∙b", this.a.conjunction(this.a).toString());

		this.a = this.a.conjunction(this.c);
		assertEquals("A∙b∙C", this.a.toString());
	}

	@SuppressWarnings("javadoc")
	@Test
	public void conjunct() {
		this.a.conjunct(new ALetter("x"));
		assertEquals("A∙x", this.a.toString());

		this.a.conjunct(new ALetter("y"));
		assertEquals("A∙x∙y", this.a.toString());
	}

	@SuppressWarnings("javadoc")
	@Test
	public void remove() {
		this.a = this.a.conjunction(this.b).conjunction(this.c);
		this.a.remove(new ALetter("C"));
		assertEquals("A∙b", this.a.toString());
		this.e.remove((ALetter) null);
		assertEquals("◇", this.e.toString());
	}

	@SuppressWarnings("javadoc")
	@Test
	public void contains() {
		this.a = this.a.conjunction(this.b);
		assertTrue(this.a.contains(new ALetter("b")));
		assertTrue(this.a.contains(this.b));
		assertTrue(this.a.contains(ALabel.emptyLabel));
		assertTrue(this.a.contains((ALabel) null));
		assertTrue(this.a.contains((ALetter) null));
		assertFalse(this.a.contains(new ALetter("C")));
		assertFalse(this.a.contains(this.c.conjunction(this.b)));

	}

	@SuppressWarnings("javadoc")
	@Test
	public void compare() {
		assertTrue(this.a.compareTo(this.c) < 0);
		assertTrue(this.a.compareTo(this.a) == 0);
		assertTrue(this.c.compareTo(this.a) > 0);
	}

	@SuppressWarnings("javadoc")
	@Test
	public void array() {
		this.a = this.a.conjunction(this.c);
		System.out.println("ALabel A: " + this.a);
		ALetter[] a1 = new ALetter[3];
		int i = 0;
		for (ALetter l : this.a) {
			a1[i++] = l;
		}
		assertEquals("C", a1[1].toString());
	}

	@SuppressWarnings("javadoc")
	@Test
	public void intersect() {
		this.a = this.a.conjunction(this.c);
		assertEquals("◇", this.a.intersect(this.e).toString());
		assertEquals(this.a, this.a.intersect(this.a));
		assertNotEquals(this.b, this.a.intersect(this.b));
		assertEquals(this.c, this.a.intersect(this.c));
		assertEquals("A", this.a.intersect(new ALabel("A", this.a.getAlphabet())).toString());
	}

	/**
	 *
	 */
	@Test
	public final void parse() {
		this.alpha.clear();

		ALabel n9 = ALabel.parse("N9" + ALabel.ALABEL_SEPARATORstring + "N12" + ALabel.ALABEL_SEPARATORstring + "N13", this.alpha);

		// System.out.printf("Map da parse: %s\n", map);

		ALabel n9ok = new ALabel("N9", this.alpha);
		n9ok.conjunct(new ALetter("N12"));
		n9ok.conjunct(new ALetter("N13"));

		Assert.assertEquals("Check of parse method", n9ok, n9);

		n9 = ALabel.parse("", this.alpha);
		n9ok = ALabel.emptyLabel;
		Assert.assertEquals("Check of parse method", n9ok, n9);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ALabelAlphabet alpha = new ALabelAlphabet();
		ALabel a = new ALabel(new ALetter("A"), alpha);
		System.out.println("ALabel A: " + a);

		ALabel b = new ALabel(new ALetter("b"), a.getAlphabet());
		System.out.println("ALabel b: " + b);

		ALabel c = new ALabel(new ALetter("C"), b.getAlphabet());
		System.out.println("ALabel C: " + c);

		System.out.println("ALabel b: " + b);

		a = a.conjunction(b);
		System.out.println("ALabel dopo conjunction con b [Ab]: " + a);

		ALabel e = ALabel.emptyLabel;
		System.out.println("Empty label: " + e);

		a = a.conjunction(e);
		System.out.println("ALabel dopo conjunction con empty [Ab]: " + a);

		System.out.println("Empty conjunction con Ab [Ab]: " + e.conjunction(a));

		a = a.conjunction(a);
		System.out.println("ALabel dopo conjunction con a [Ab]: " + a);

		a = a.conjunction(c);
		System.out.println("ALabel dopo conjunction con C [AbC]: " + a);

		a.remove(new ALetter("C"));
		System.out.println("ALabel dopo rimozione C [Ab]: " + a);

		System.out.println("ALabel contiene 'b' [true]: " + a.contains(new ALetter("b")));
		System.out.println("ALabel contiene 'C' [false]: " + a.contains(new ALetter("C")));

		System.out.println("ALabel confronto con 'C' [<0]: " + a.compareTo(c));
		System.out.println("ALabel confronto a [0]: " + a.compareTo(a));
		System.out.println("C confronto con 'A∙b' [>0]: " + c.compareTo(a));
		a.conjunct(new ALetter("C"));
		System.out.println("ALabel A: " + a);
		for (ALetter l : a) {
			System.out.println("Letter: " + l);
		}

		System.out.println("A intersect with empty: " + a.intersect(ALabel.emptyLabel));
		System.out.println("A intersect with a: " + a.intersect(a));
		System.out.println("A intersect with b: " + a.intersect(b));
		System.out.println("A intersect with c: " + a.intersect(c));
		System.out.println("A intersect with 'A': " + a.intersect(new ALabel("A", a.getAlphabet())));

	}

}
