/**
 * 
 */
package it.univr.di.cstnu.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap.BasicEntry;
import it.univr.di.cstnu.graph.LabeledNode;

/**
 * @author posenato
 */
public class NodePriorityHeapTest {

	/**
	 * 
	 */
	NodePriorityHeap heap;
	@SuppressWarnings("javadoc")
	LabeledNode A, B, C;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.heap = new NodePriorityHeap();
		this.A = new LabeledNode("A");
		this.B = new LabeledNode("B");
		this.C = new LabeledNode("C");
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.NodePriorityHeap#insertOrDecrease(it.univr.di.cstnu.graph.LabeledNode, int)}.
	 */
	@Test
	public void testAdd() {
		this.heap.insertOrDecrease(this.A, 0);
		// this.heap.add(this.A, 1);
		// this.heap.add(this.A, 2);
		this.heap.insertOrDecrease(this.B, 1);
		this.heap.insertOrDecrease(this.C, -1);
		assertEquals("Aggiunta elementi:", "[-1->❮C❯, 0->❮A❯, 1->❮B❯]", this.heap.toString());
		assertEquals("Aggiunta elementi:", 0, this.heap.getAllDeterminedPriorities().getInt(this.A));
		assertEquals("Aggiunta elementi:", "isPresent", this.heap.getStatus(this.A).toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.NodePriorityHeap#clear()}.
	 */
	@Test
	public void testClear() {
		this.testAdd();
		this.heap.clear();
		assertEquals("Cancellazione elementi:", "[]", this.heap.toString());
		assertEquals("Aggiunta elementi:", "{}", this.heap.getAllDeterminedPriorities().toString());
		assertEquals("Aggiunta elementi:", "notPresent", this.heap.getStatus(this.A).toString());
	}

	/**
	 */
	@Test
	public void testDecreasePriority() {
		this.testAdd();
		this.heap.insertOrDecrease(this.B, -10);
		assertEquals("Decrease priority:", "[-10->❮B❯, -1->❮C❯, 0->❮A❯]", this.heap.toString());
		assertEquals("Aggiunta elementi:", "isPresent", this.heap.getStatus(this.B).toString());
		assertEquals("Decrease priority:", -10, this.heap.getPriority(this.B));
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.NodePriorityHeap#extractMin()}.
	 */
	@Test
	public void testExtractMin() {
		this.testDecreasePriority();
		LabeledNode min = this.heap.extractMin();
		assertEquals("testExtractMin:", "[-1->❮C❯, 0->❮A❯]", this.heap.toString());
		assertEquals("min: ", "❮B❯", min.toString());
		assertEquals("Aggiunta elementi:", "wasPresent", this.heap.getStatus(this.B).toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.NodePriorityHeap#extractMinEntry()}.
	 */
	@Test
	public void testExtractMinEntry() {
		this.testDecreasePriority();
		BasicEntry<LabeledNode> min = this.heap.extractMinEntry();
		assertEquals("testExtractMin:", "[-1->❮C❯, 0->❮A❯]", this.heap.toString());
		assertEquals("min: ", "❮B❯", min.getKey().toString());
		assertEquals("min: ", -10, min.getIntValue());
		assertEquals("Aggiunta elementi:", "wasPresent", this.heap.getStatus(this.B).toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.NodePriorityHeap#getPriority(it.univr.di.cstnu.graph.LabeledNode)}.
	 */
	@Test
	public void testGetPriority() {
		this.testDecreasePriority();
		int p = this.heap.getPriority(this.B);
		assertEquals("priority: ", -10, p);
		assertEquals("Aggiunta elementi:", "isPresent", this.heap.getStatus(this.B).toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.NodePriorityHeap#getAllDeterminedPriorities()}.
	 */
	@Test
	public void testGetAllDeterminedPriorities() {
		this.testAdd();
		assertEquals("testGetPriorities", 0, this.heap.getAllDeterminedPriorities().getInt(this.A));
		this.heap.insertOrDecrease(this.B, -1);
		this.heap.extractMin();
		this.heap.insertOrDecrease(this.B, -2);
		this.heap.extractMin();
		this.heap.insertOrDecrease(this.B, -3);
		assertEquals("testGetPriorities", "{❮A❯=>0, ❮B❯=>-2, ❮C❯=>-1}", this.heap.getAllDeterminedPriorities().toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.NodePriorityHeap#getStatus(it.univr.di.cstnu.graph.LabeledNode)}.
	 */
	@Test
	public void testGetStatus() {
		this.testAdd();
		assertEquals("status", "isPresent", this.heap.getStatus(this.A).toString());
		assertEquals("status", "isPresent", this.heap.getStatus(this.C).toString());
		this.heap.extractMinEntry();
		this.heap.extractMinEntry();
		assertEquals("status", "wasPresent", this.heap.getStatus(this.A).toString());
		assertEquals("status", "wasPresent", this.heap.getStatus(this.C).toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.NodePriorityHeap#isEmpty()}.
	 */
	@Test
	public void testIsEmpty() {
		this.testAdd();
		assertFalse(this.heap.isEmpty());
		this.heap.clear();
		assertTrue(this.heap.isEmpty());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.NodePriorityHeap#size()}.
	 */
	@Test
	public void testSize() {
		this.testAdd();
		assertEquals("status", 3, this.heap.size());
		this.heap.clear();
		assertEquals("status", 0, this.heap.size());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.NodePriorityHeap#value(it.univr.di.cstnu.graph.LabeledNode)}.
	 */
	@Test
	public void testValue() {
		this.testAdd();
		assertEquals("testValue", 0, this.heap.value(this.A));
	}

}
