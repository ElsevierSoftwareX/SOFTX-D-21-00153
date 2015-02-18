package it.univr.di.labeledvalue;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Roberto Posenato
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ LabeledContingentIntTreeMapTest.class, LabeledIntNodeSetTreeMapTest.class, LabelTest.class, LiteralTest.class, ValueNodeSetPairTest.class })
public class AllTests {

}
