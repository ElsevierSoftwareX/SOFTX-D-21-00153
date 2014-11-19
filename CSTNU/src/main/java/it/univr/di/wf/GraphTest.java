/**
 * 
 */
package it.univr.di.wf;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roberto Posenato
 */
public class GraphTest {

	/**
	 * 
	 */
	private int[][] m; 
	/**
	 * 
	 */
	private int[][] m1;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		m = new int[][] { 
				{ 0, 2, Integer.MAX_VALUE }, 
				{ -1, 0, Integer.MAX_VALUE },
				{ -2, Integer.MAX_VALUE, 0} };
		m1 = new int[m.length][m.length];
		// System.out.println("@Before - setUp");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		// System.out.println("@After - tearDown");
	}

	/**
	 * Test method for {@link it.univr.di.wf.DistanceGraph#FloydWarshall(int[][])}.
	 */
	@Test
	public final void testFloydWarshall() {
		System.out.print("Prima:\n"+DistanceGraph.print(m));
		DistanceGraph.deepClone(m, m1);
		DistanceGraph.FloydWarshall(m);
		System.out.print("Dopo:\n"+DistanceGraph.print(m));
		DistanceGraph.FloydWarshallI(m1);
		//System.out.print(DistanceGraph.print(m));
		//Assert.assertTrue(!DistanceGraph.isDifferent(m, m1));
		System.out.println("@Test - testFloydWarshall");
	}

	/**
	 * Test method for {@link it.univr.di.wf.DistanceGraph#deepClone(int[][], int[][])}.
	 */
	@Test
	public final void testDeepClone() {
		DistanceGraph.deepClone(m, m1);
		Assert.assertFalse(DistanceGraph.isDifferent(m, m1));
		System.out.println("@Test - testDeepClone");
	}

	/**
	 * Test method for {@link it.univr.di.wf.DistanceGraph#isDifferent(int[][], int[][])}.
	 */
	@Test
	public final void testIsDifferent() {
		int[][] m1 = m.clone();
		Assert.assertTrue(!DistanceGraph.isDifferent(m, m1));
		System.out.println("@Test - testIsDifferent");
	}

	/**
	 * Test method for {@link it.univr.di.wf.DistanceGraph#print(int[][])}.
	 */
	@Test
	public final void testPrint() {
		String s = DistanceGraph.print(m);
//		System.out.print(s);
		String sOk = "    0    2    ∞\n" 
			       + "   -1    0    ∞\n" 
			       + "   -2    ∞    0\n";
		// fail("Not yet implemented"); // TODO
		Assert.assertEquals(s, sOk);
		System.out.println("@Test - testPrint");
	}

}
