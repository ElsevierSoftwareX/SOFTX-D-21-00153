/**
 * 
 */
package it.univr.di.cstnu.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/**
 * @author posenato
 *
 */
public class ObjectArrayFifoSetQueueTest {

	/**
	 * 
	 */
	public ObjectArrayFIFOSetQueue<String> queue;

	/**
	 * @throws java.lang.Exception  none
	 */
	@Before
	public void setUp() throws Exception {
		this.queue = new ObjectArrayFIFOSetQueue<>();
		this.queue.enqueue("A");
		this.queue.enqueue("B");
		this.queue.enqueue("C");
		this.queue.enqueue("D");
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.ObjectArrayFIFOSetQueue#comparator()}.
	 */
	@Test
	public void testComparator() {
		assertTrue("Compararotor", this.queue.comparator() == null);

	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.ObjectArrayFIFOSetQueue#dequeue()}.
	 */
	@Test
	public void testDequeue() {
		this.queue.dequeue();
		assertEquals("[B, C, D]", this.queue.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.ObjectArrayFIFOSetQueue#dequeueLast()}.
	 */
	@Test
	public void testDequeueLast() {
		this.queue.dequeueLast();
		assertEquals("[A, B, C]", this.queue.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.ObjectArrayFIFOSetQueue#enqueue(java.lang.Object)}.
	 */
	@Test
	public void testEnqueue() {
		this.queue.enqueue("E");
		assertEquals("[A, B, C, D, E]", this.queue.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.ObjectArrayFIFOSetQueue#enqueueFirst(java.lang.Object)}.
	 */
	@Test
	public void testEnqueueFirst() {
		this.queue.enqueueFirst("E");
		assertEquals("[E, A, B, C, D]", this.queue.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.ObjectArrayFIFOSetQueue#first()}.
	 */
	@Test
	public void testFirst() {
		assertEquals("A", this.queue.first());
		assertEquals("[A, B, C, D]", this.queue.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.ObjectArrayFIFOSetQueue#last()}.
	 */
	@Test
	public void testLast() {
		assertEquals("D", this.queue.last());
		assertEquals("[A, B, C, D]", this.queue.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.ObjectArrayFIFOSetQueue#clear()}.
	 */
	@Test
	public void testClear() {
		this.queue.clear();
		assertEquals(0, this.queue.size());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.ObjectArrayFIFOSetQueue#size()}.
	 */
	@Test
	public void testSize() {
		assertEquals(4, this.queue.size());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.ObjectArrayFIFOSetQueue#contains(java.lang.Object)}.
	 */
	@Test
	public void testContains() {
		assertTrue(this.queue.contains("C"));
		assertFalse(this.queue.contains("c"));
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.ObjectArrayFIFOSetQueue#toArray()}.
	 */
	@Test
	public void testToArray() {
		String[] s = this.queue.toArray(new String[0]);
		assertEquals("[A, B, C, D]", Arrays.toString(s));
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.ObjectArrayFIFOSetQueue#toString()}.
	 */
	@Test
	public void testToString() {
		assertEquals("[A, B, C, D]", this.queue.toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.ObjectArrayFIFOSetQueue#isEmpty()}.
	 */
	@Test
	public void testIsEmpty() {
		assertFalse(this.queue.isEmpty());
		this.queue.remove("D");
		this.queue.remove("C");
		this.queue.remove("B");
		this.queue.remove("A");
		assertTrue(this.queue.isEmpty());
	}

}
