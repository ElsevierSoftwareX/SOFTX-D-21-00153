/**
 * 
 */
package it.univr.di.test;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

/**
 * @author posenato
 */
public class LogicalExpression {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String exp = " (!(!`a`  ∧ `b`)| `tn1tA4`) &  (`a`|`tAtn1_3`) &  (`a` | `tn3tn14`) &  (`a` |`tn1tn3_3`) & "
				+" (!`a` | `tn2tA10`) &  (`a` | `tAtn2_5`) &  (`a` |`tn3tn210`) &  (`a`|`tn2tn3_5`)";
		
		exp = "(`a`|(B & C)) & (!`a`|(C&D))";
		
		exp = " (`a` | `((tn1 - tA) =< 4)`) &  (`a`|`((tA - tn1) =< -3)`) &  (`a`|`((tn3 - tn1) =< 4)`) &  (`a`|`((tn1 - tn3) =< -3)`) & "
				+ " ( !`a` | `((tn2 - tA) =< 10)`)  &  (!`a`|`((tA - tn2) =< -5)`) &  (!`a`|`((tn3 - tn2) =< 10)`) &  (!`a`|`((tn2 - tn3) =< -5)`)";
		
		Expression<String> parsedExpression = ExprParser.parse(exp);
		System.out.println("Original expression: "+parsedExpression);
		Expression<String> simplified = RuleSet.simplify(parsedExpression);
		
		System.out.println("Simplified expression: "+ simplified);
		System.out.println("DNF expression:\n "+ (RuleSet.toSop(simplified)).toString().replace('|', '\n'));
//		System.out.println("Is it equals to the original one? " + parsedExpression.equals(simplified));

	}

}
