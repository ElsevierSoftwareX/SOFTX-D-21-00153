/**
 * 
 */
package it.univr.di.labeledvalue;

import static org.junit.Assert.*;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import org.junit.Test;

/**
 * @author posenato
 *
 */
public class ValueNodeSetPairTest {

	/**
	 * Test method for {@link it.univr.di.labeledvalue.ValueNodeSetPair#ValueNodeSetPair(int, java.util.Set)}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testEqualsObject() {
		ValueNodeSetPair a = new ValueNodeSetPair(12, new ObjectArraySet<String>(new String[] { "A", "B", "C"}));
		
		ValueNodeSetPair b = new ValueNodeSetPair(12, a.getNodeSet());
		
		assertEquals(a, b);
		
		ValueNodeSetPair c = new ValueNodeSetPair(12,null);
		
		assertNotEquals(a, c);
		
		ValueNodeSetPair d = new ValueNodeSetPair(12,null);
		assertEquals(c, d);
		
		
	}
}
