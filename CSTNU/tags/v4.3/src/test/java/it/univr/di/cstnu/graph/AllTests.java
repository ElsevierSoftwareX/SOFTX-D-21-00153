package it.univr.di.cstnu.graph;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Roberto Posenato
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ CSTNEdgeTest.class, CSTNGraphTest.class, CSTNUEdgeTest.class, CSTNUGraphMLReaderTest.class, CSTNUGraphMLWriterTest.class, STNUEdgeTest.class,
		EdgeSupplierTest.class, LabeledNodeTest.class, STNUGraphMLWriterTest.class, STNUGraphMLReaderTest.class})
public class AllTests {
	//annotations is sufficient!
}
