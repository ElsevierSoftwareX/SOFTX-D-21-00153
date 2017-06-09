/**
 * 
 */
package it.univr.di.test;
//
//import orbital.logic.imp.Formula;
//import orbital.logic.imp.Inference;
//import orbital.logic.imp.Logic;
//import orbital.logic.sign.Signature;
//import orbital.moon.logic.ClassicalLogic;
//import orbital.moon.logic.resolution.ClausalSet;
//import orbital.moon.logic.resolution.DefaultClausalFactory;
//
/**
 * A simple example for logic evaluations using classical logic implementation.
 * 
 * @version $Id: SimpleLogic.java 1683 2005-09-13 14:58:42Z andre $
 * @author Andr&eacute; Platzer
 * @author posenato
 */
public class DNFEncoder {
//
//	/**
//	 * The logic to use.
//	 */
//	protected final ClassicalLogic logic;
//
//	/**
//	 * the formula and its signature.
//	 */
//	private Formula formula;
//	@SuppressWarnings("javadoc")
//	private Signature sigma;
//
//	/**
//	 */
//	public DNFEncoder() {
//		logic = new ClassicalLogic();//ClassicalLogic.PROPOSITIONAL_INFERENCE);
//	}
//
///**
//	 * It seems that the 
//	 * 	/**
//	 * Grammar
//	 * «EQUIV» ::= "↔" | "⇔" | "<->" | "<=>"
//	 * «IMPLY» ::= "→" | "⇒" | "⊃" | "->" | "=>"
//	 * «OR» ::= "∨" | "|" | "||"
//	 * «XOR» ::= "∨̇" | "xor" | "^"
//	 * «AND» ::= "∧" | "&" | "&&"
//	 * «NOT» ::= "¬" | "~" | "!"
//	 * «EXISTS» ::= "∃" | "?" | "some"
//	 * «FORALL» ::= "∀" | "Â°" | "all"
//	 * «BOX» ::= "□" | "[]"
//	 * «DIAMOND» ::= "◇" | "<>"
//	 * «EQUAL» ::= "=" | "=="
//	 * UNEQUAL» ::= "≠" | "!="
//	 * «LESS>	::=	"<"
//	* «GREATER>	::=	">"
//	* «LESS_EQUAL>	::=	"≤" | "=<"
//	* «GREATER_EQUAL>	::=	"≥" | ">="
//	 */
//	public void run() {
//		try {
//			// String formulaText = "((a ∧ b) → ( ( (16 ≤ (D-B)) ∧ ((D-B) >= 30)) ∧ ((5 =< (C-A)) ∧ ((C-A) >= 5) ))) ∧ (!b → ((1 =< (H-A)) ∧ ((H-A) >= 10)))";
//			String formulaText;
////			formulaText = "( (a ∧ b) → ( ((16 ≤ (D-B)) ∧ ((D-B) ≤ 30)) ∧ ((5 ≤ (C-A)) ∧ ((C-A) ≤ 5)) ) ) ∧ ( !b → ((1 ≤ (H-A)) ∧ ((H-A) ≤ 10)) )";
////			formulaText = "( (a ∧ b) → ( ((DBg16) ∧ (DBl30)) ∧ ((CAg5) ∧ (CAg5)) ) ) ∧ ( !b → ((HAg1) ∧ (HAl10)) )";
//			formulaText = " (¬a=>((tn1 - tA) =< 4)) ∧  (¬a=>((tA - tn1) =< -3)) ∧  (¬a=>((tn3 - tn1) =< 4)) ∧  (¬a=>((tn1 - tn3) =< -3)) ∧ "
//					+" (a=>((tn2 - tA) =< 10)) ∧  (a=>((tA - tn2) =< -5)) ∧  (a=>((tn3 - tn2) =< 10)) ∧  (a=>((tn2 - tn3) =< -5))";
//			
////			formulaText = " ((¬a  ∧ b)=> tn1tA4) ∧  (¬a=>tAtn1_3) ∧  (¬a=>tn3tn14) ∧  (¬a=>tn1tn3_3) ∧  (a=>tn2tA10) ∧  (a=>tAtn2_5) ∧  (a=>tn3tn210) ∧  (a=>tn2tn3_5)";
////			formulaText = "(a|(B & C)) & (!a|(C&D))";
//			Formula dnf;
//			DefaultClausalFactory dcf = new DefaultClausalFactory();
////			sigma = logic.scanSignature(formulaText);
////			System.out.println("Signature: "+sigma);
//			formula = (Formula) logic.createExpression(formulaText);
//			Formula cnf = ClassicalLogic.Utilities.conjunctiveForm(formula);
////			Formula dnf = ClassicalLogic.Utilities.disjunctiveForm(formula);
//			Formula dnfO = ClassicalLogic.Utilities.disjunctiveForm(formula, false);
//			System.out.println("Input expression: " + formulaText);
//			System.out.println("Formula after parsing: " + formula.toString());
//			System.out.println("CNF: " + cnf.toString());
////			System.out.println("Formula in Clausal form: " + dcf.asClausalSet(cnf));
//			
////			System.out.println("DNF: " + dnf.toString().replace('|', '\n'));
////			System.out.println("DNF simplified: " + dnfO);
//			System.out.println("DNF tokenized:\n" + dnfO.toString().replace('|', '\n'));
////			System.out.println("DNF tokenized converted:\n" + Arrays.toString(toTigaExpressionRepresentation(dnfO.toString()).split("\\|")).replace(" , ", "\n"));
////			String[] formulas = toTigaExpressionRepresentation(dnfO.toString()).split("\\|");
//
//			int i=0;
////			Inference inf = logic.inference();
////			String[] fa = new Formula[1];
////			for(String f: formulas) {
////				String fC =  dcf.asClausalSet((Formula) logic.createExpression(f)).toFormula().toString();
////				fa[0] = fC;
////				boolean sat = false;
////				try {
////					sat = logic.infer(fC, fC);
////				} 
////				catch (java.lang.IllegalStateException e) {
////					continue;
////				}
////				String fClean = fC.toString();
////				if (sat) {
////					System.out.println(++i //+ " Implicant\t\t: " +f 
////							+"\nImplicant cleaned\t: " + fClean);
////				}
////			}
////			System.out.println("ClausalSet: " + cs);
//			
//			
////			formulaText = "(a → (A ∧ nB)) ∧ (a → (C ∧ D)) ∧ (¬a → (C∧nB))∧ (¬a → (E∧F))";
////			formula = (Formula) logic.createExpression(formulaText);
////			dnf = ClassicalLogic.Utilities.disjunctiveForm(formula, true);
////			System.out.println("\n\nFormula: " + formula.toString());
////			System.out.println("DNF: " + dnf.toString().replace('|', '\n'));
////			
////			
////			Symbol sa = new SymbolBase("a", Types.TRUTH, null, true );
////			Symbol sA = new SymbolBase("A", Types.TRUTH, null, true );
////			Symbol snB = new SymbolBase("B", Types.TRUTH, null, true );
////			Symbol sC = new SymbolBase("C", Types.TRUTH, null, true );
////			Symbol sD = new SymbolBase("D", Types.TRUTH, null, true );
////			Symbol sE = new SymbolBase("E", Types.TRUTH, null, true );
////			Symbol sF = new SymbolBase("F", Types.TRUTH, null, true );
////			Formula fa = (Formula) logic.createAtomic(sa);
////			Formula fA = (Formula) logic.createAtomic(sA);
////			Formula fnB = (Formula) logic.createAtomic(snB);
////			Formula fC = (Formula) logic.createAtomic(sC);
////			Formula fD = (Formula) logic.createAtomic(sD);
////			Formula fE = (Formula) logic.createAtomic(sE);
////			Formula fF = (Formula) logic.createAtomic(sF);
//
////			Formula newForm =  fa.impl(fA.and(fnB)).and(fa.impl(fC.and(fD))).and(fa.not().impl(fC.and(fnB))).and(fa.not().impl(fE.and(fF)));
////			dnf = ClassicalLogic.Utilities.disjunctiveForm(newForm, false);
////			System.out.println("\n\nFormula: " + newForm);
////			System.out.println("new form Signature: "+newForm.getSignature());
////			System.out.println("DNF: " + dnf.toString().replace('|', '\n'));
////			String[] formulas = dnf.toString().split("\\|");
////			Formula f1 = (Formula) logic.createExpression(formulas[0]);
////			System.out.println("Formula: " + formulas[0]);
//
//			//			String[] formulas = dnf.toString().split("\\|");
////			DefaultClausalFactory dcf = new DefaultClausalFactory();
////			ClausalSet cs = dcf.asClausalSet((Formula) logic.createExpression(formulas[0]));
////			System.out.println("ClausalSet: " + cs);
////			System.out.println("Formula da clausal set: " + cs.toFormula());
//		}
//		catch (Exception x) {
//			x.printStackTrace();
//		}
//	}
//
//	
//	
//	/**
//	 * Orbital represents Formula as, for example, "~a & =<(7,D - A) | a & =<(3,B - A)"
//	 * Infix representation of integer relation and
//	 * less or equal as =< instead of <=
//	 * logical not as ~ instead of !
//	 * 
//	 * This method convert such representation into Tiga format.
//	 * It is limited to the above operator!
//	 * 
//	 * @param orbitalFormulaText 
//	 * @return Tiga representation of orbitalFormulaText.
//	 */
//	private static String toTigaExpressionRepresentation(String orbitalFormulaText) {
//		if (orbitalFormulaText == null || orbitalFormulaText.isEmpty()) return "";
//		
//		//I use regex!
//		String allowedTokenRE = "-\\w\\s\\.";
//		orbitalFormulaText = orbitalFormulaText.replaceAll("~", "!");
//		orbitalFormulaText = orbitalFormulaText.replaceAll("&", "&&");
//		orbitalFormulaText = orbitalFormulaText.replaceAll("=<\\((["+allowedTokenRE+"]+),(["+allowedTokenRE+"]+)\\)", "( ($1) <= ($2) )");
//		
//		return orbitalFormulaText;
//	}
//
//	/**
//	 * @param arg
//	 * @throws Exception
//	 */
//	public static void main(String arg[]) throws Exception {
//		new DNFEncoder().run();
//	}
//
}
