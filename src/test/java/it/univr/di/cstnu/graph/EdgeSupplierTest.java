/**
 * 
 */
package it.univr.di.cstnu.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author posenato
 */
public class EdgeSupplierTest {

	/**
	 * 
	 */
	static EdgeSupplier<STNEdge> stnEdgeFactory = new EdgeSupplier<>(STNEdgeInt.class);
	/**
	 * 
	 */
	static EdgeSupplier<CSTNEdge> cstnEdgeFactory = new EdgeSupplier<>(CSTNEdgePluggable.class);
	/**
	 * 
	 */
	static EdgeSupplier<CSTNUEdge> cstnuEdgeFactory = new EdgeSupplier<>(CSTNUEdgePluggable.class);

	/**
	 * 
	 */
	STNEdge stnEdge;
	/**
	 * 
	 */
	CSTNEdge cstnEdge;
	/**
	 * 
	 */
	CSTNUEdge cstnuEdge;

	/**
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testEdgeSupplier() {
		assertNotNull(stnEdgeFactory.getEdgeImplClass());
		assertNotNull(cstnEdgeFactory.getEdgeImplClass());
		assertNotNull(cstnuEdgeFactory.getEdgeImplClass());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.EdgeSupplier#get()}.
	 */
	@Test
	public void testGet() {
		this.stnEdge = stnEdgeFactory.get();
		this.cstnEdge = cstnEdgeFactory.get();
		this.cstnuEdge = cstnuEdgeFactory.get();

		assertNotNull(this.stnEdge);
		assertTrue(this.stnEdge.isSTNEdge());
		assertFalse(this.stnEdge.isCSTNEdge());

		assertNotNull(this.cstnEdge);
		assertTrue(this.cstnEdge.isCSTNEdge());
		assertFalse(this.cstnEdge.isCSTNUEdge());
		assertNotNull(this.cstnuEdge);
		assertTrue(this.cstnuEdge.isCSTNUEdge());
		assertTrue(this.cstnuEdge.isCSTNUEdge());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.EdgeSupplier#get(java.lang.Class, int)}.
	 */
	@SuppressWarnings({ "static-access", "static-method" })
	@Test
	public void testGetClassOfKInt() {

		STNEdge c[] = stnEdgeFactory.get(STNEdgeInt.class, 4);
		assertEquals(Arrays.toString(c), "[null, null, null, null]");
		c[1] = stnEdgeFactory.get();
		assertTrue(c[1].isSTNEdge());

		CSTNEdge a[] = cstnEdgeFactory.get(CSTNEdgePluggable.class, 4);
		assertEquals(Arrays.toString(a), "[null, null, null, null]");
		a[1] = cstnEdgeFactory.get();
		assertTrue(a[1].isCSTNEdge());
		CSTNUEdge b[] = cstnEdgeFactory.get(CSTNUEdgePluggable.class, 4);
		assertNotNull(b);
		b[3] = cstnuEdgeFactory.get();
		assertTrue(b[3].isCSTNEdge());
		a[2] = b[3];
		assertTrue(a[2].isCSTNEdge());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.EdgeSupplier#get(java.lang.String)}.
	 */
	@Test
	public void testGetString() {
		this.stnEdge = stnEdgeFactory.get("Pippo");
		this.cstnEdge = cstnEdgeFactory.get("Pippo");
		this.cstnuEdge = cstnuEdgeFactory.get("Pippo");

		assertNotNull(this.stnEdge);
		assertNotNull(this.cstnEdge);
		assertNotNull(this.cstnuEdge);
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.graph.EdgeSupplier#get(it.univr.di.cstnu.graph.Edge)}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetT() {
		this.stnEdge = stnEdgeFactory.get("Pippo");
		this.cstnEdge = cstnEdgeFactory.get("Pippo");
		this.cstnuEdge = cstnuEdgeFactory.get("Pippo");

		STNEdge stnE1 = stnEdgeFactory.get(this.stnEdge);
		CSTNEdge cstnE1 = cstnEdgeFactory.get(this.cstnEdge);
		CSTNUEdge cstnuE1 = cstnuEdgeFactory.get(this.cstnuEdge);

		assertFalse(this.stnEdge.equals(stnE1));
		assertFalse(this.cstnEdge.equals(cstnE1));
		assertFalse(this.cstnuEdge.equals(cstnuE1));
	}
}
