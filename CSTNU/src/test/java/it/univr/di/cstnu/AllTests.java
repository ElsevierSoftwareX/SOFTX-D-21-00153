package it.univr.di.cstnu;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author posenato
 */
@RunWith(Suite.class)
@SuiteClasses({ CSTNTest.class, CSTNUTest.class, CSTNirTest.class, CSTNepsilonTest.class })
public class AllTests {
	//annotation is sufficient!
}
