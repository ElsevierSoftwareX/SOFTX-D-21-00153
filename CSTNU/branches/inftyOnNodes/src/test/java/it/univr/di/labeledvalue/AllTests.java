package it.univr.di.labeledvalue;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Roberto Posenato
 */
@RunWith(Suite.class)
@SuiteClasses({
		ALabelTest.class,
		LabeledALabelIntTreeMapTest.class,
		LabeledIntHierarchyMapTest.class,
		LabeledIntSimpleMapTest.class,
		LabeledIntTreeMapTest.class,
		LabeledLowerCaseValueTest.class,
		LabelTest.class,
		LiteralTest.class
})
public class AllTests {
	//annotation is sufficient!
}
