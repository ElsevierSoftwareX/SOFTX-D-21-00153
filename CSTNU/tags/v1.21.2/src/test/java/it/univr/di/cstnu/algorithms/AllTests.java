package it.univr.di.cstnu.algorithms;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author posenato
 */
@RunWith(Suite.class)
@SuiteClasses({ CSTNepsilonTest.class, CSTNirTest.class, CSTNirR3Test.class, CSTNTest.class, CSTNUTest.class, CSTNwoNodeLabelTest.class,
		CSTNirwoNodeLabelTest.class,
		CSTNir3RwoNodeLabelTest.class,
		CSTNepsilonwoNodeLabelTest.class
})
public class AllTests {
	// annotation is sufficient!
}
