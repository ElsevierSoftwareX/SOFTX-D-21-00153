package it.univr.di.cstnu.algorithms;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author posenato
 */
@RunWith(Suite.class)
@SuiteClasses({
		CSTNepsilonTest.class,
		CSTNepsilonwoNodeLabelTest.class,
		CSTNir3RwoNodeLabelTest.class,
		CSTNirR3Test.class,
		CSTNirTest.class,
		CSTNirwoNodeLabelTest.class,
		CSTNTest.class,
		CSTNUTest.class,
		CSTNwoNodeLabelTest.class,
		ObjectArrayFifoSetQueueTest.class,
		PriorityQueueTest.class,
		STNUTest.class
})
public class AllTests {
	// annotation is sufficient!
}
