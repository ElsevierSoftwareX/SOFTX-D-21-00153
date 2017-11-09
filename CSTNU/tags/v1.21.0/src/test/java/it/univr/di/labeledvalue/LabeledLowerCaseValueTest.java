package it.univr.di.labeledvalue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author posenato
 */
public class LabeledLowerCaseValueTest {

	@SuppressWarnings("javadoc")
	ALabelAlphabet alpha;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.alpha = new ALabelAlphabet();
	}

	/**
	 * 
	 */
	@Test
	public final void parse() {
		LabeledLowerCaseValue lowerValue = LabeledLowerCaseValue.parse("{(¬a, N9, -12) }", this.alpha);
		Assert.assertEquals(LabeledLowerCaseValue.create(new ALabel("N9", this.alpha), -12, Label.parse("¬a")), lowerValue);

		lowerValue = LabeledLowerCaseValue.parse("{ }", this.alpha);
		Assert.assertEquals(LabeledLowerCaseValue.emptyLabeledLowerCaseValue, lowerValue);

		Assert.assertNotEquals(LabeledLowerCaseValue.emptyLabeledLowerCaseValue, null);
		Assert.assertEquals(LabeledLowerCaseValue.emptyLabeledLowerCaseValue, LabeledLowerCaseValue.emptyLabeledLowerCaseValue);

		Assert.assertFalse(LabeledLowerCaseValue.emptyLabeledLowerCaseValue.equals(null));
		Assert.assertTrue(LabeledLowerCaseValue.emptyLabeledLowerCaseValue.equals(LabeledLowerCaseValue.parse("{ }", this.alpha)));
		Assert.assertTrue(LabeledLowerCaseValue.emptyLabeledLowerCaseValue.equals(LabeledLowerCaseValue.emptyLabeledLowerCaseValue));

	}


}
