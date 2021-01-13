/**
 * 
 */
package it.univr.di.cstnu.algorithms;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import it.univr.di.cstnu.graph.CSTNUEdge;
import it.univr.di.cstnu.graph.CSTNUEdgePluggable;
import it.univr.di.cstnu.graph.TNGraph;

/**
 * @author posenato
 */
public class CSTNU2CSTNTest {
	/**
	 * 
	 */
	static final Class<? extends CSTNUEdge> edgeImplClass = CSTNUEdgePluggable.class;
	/**
	 * 
	 */
	TNGraph<CSTNUEdge> g;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.g = new TNGraph<>(edgeImplClass);

	}

	/**
	 */
	@Test
	public void testCSTNU2CSTNLabeledIntGraphInt() {

		CSTNU2CSTN checker = new CSTNU2CSTN(this.g, 100);

		assertEquals(checker.timeOut, 100);
	}

}
