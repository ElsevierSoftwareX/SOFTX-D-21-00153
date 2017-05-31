package it.univr.di.labeledvalue;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Roberto Posenato
 */
@RunWith(Suite.class)
@SuiteClasses({ ALabelTest.class, LabeledContingentIntTreeMapTest.class, LabeledIntTreeMapTest.class, LabeledIntHierarchyMapTest.class, LabelTest.class, LiteralTest.class
})
public class AllTests {
	//annotation is sufficient!
}
