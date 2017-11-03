package it.univr.di.cstnu.graph;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Roberto Posenato
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ LabeledIntEdgeSimpleTest.class, LabeledIntGraphTest.class, LabeledNodeTest.class, CSTNUGraphMLWriterTest.class })
public class AllTests {
	//annotations is sufficient!
}
