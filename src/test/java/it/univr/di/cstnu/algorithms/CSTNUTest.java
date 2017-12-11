/**
 * 
 */
package it.univr.di.cstnu.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import it.univr.di.cstnu.algorithms.CSTNU.CSTNUCheckStatus;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.ALabelAlphabet;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntMapFactory;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * @author posenato
 */
public class CSTNUTest {

	/**
	 * 
	 */
	final Class<? extends LabeledIntMap> labeledIntValueMapClass = LabeledIntTreeMap.class;

	/**
	 * 
	 */
	final LabeledIntMapFactory<? extends LabeledIntMap> labeledIntMapFactory = new LabeledIntMapFactory<>(this.labeledIntValueMapClass);

	/**
	 * 
	 */
	CSTNU cstnu;

	/**
	 * 
	 */
	LabeledNode Z;
	
	/**
	 * 
	 */
	ALabelAlphabet alpha;
	

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.cstnu = new CSTNU(new LabeledIntGraph(LabeledIntTreeMap.class));
		this.cstnu.Z = this.Z;
		this.Z = new LabeledNode("Z");
		this.alpha = new ALabelAlphabet();
	}

	/**
	 * @param g
	 */
	private final void wellDefinition(LabeledIntGraph g) {
		this.cstnu.setG(g);
		try {
			this.cstnu.checkWellDefinitionProperties();
		} catch (WellDefinitionException e) {
			fail("LabeledIntGraph not well formed: " + e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link it.univr.di.attic.CSTNU_NodeSet#caseLabelRemovalRule(it.univr.di.cstnu.graph.LabeledIntGraph, it.univr.di.cstnu.graph.LabeledIntGraph, CSTNUCheckStatus)}
	 * 
	 * <pre>
	 * B &lt;--- 13,b,b--- A &lt;-----4,B,ab---- C
	 * </pre>
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testCaseLabelRemovalRule() {
		// System.out.printf("LABEL REMOVAL CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		final LabeledIntEdgePluggable ab = new LabeledIntEdgePluggable("AB", this.labeledIntValueMapClass);
		final LabeledNode A = new LabeledNode("A");
		final LabeledNode B = new LabeledNode("B");
		ALabel aLabel4B = new ALabel(B.getName(), this.alpha);
		B.setAlabel(aLabel4B);
		final LabeledNode C = new LabeledNode("C");

		ab.setLowerCaseValue(Label.parse("b"), aLabel4B, 13);

		final LabeledIntEdgePluggable ba = new LabeledIntEdgePluggable("BA", this.labeledIntValueMapClass);// now CaseLabelRemoval checks the lower bound, no the
																										// guards.
		final LabeledIntEdgePluggable ca = new LabeledIntEdgePluggable("CA", this.labeledIntValueMapClass);
		ca.mergeUpperCaseValue(Label.parse("ab"), aLabel4B, -4);
		ca.mergeUpperCaseValue(Label.parse("a"), aLabel4B, -3);
		ca.mergeUpperCaseValue(Label.parse("¬b"), aLabel4B, -3);
		ca.mergeUpperCaseValue(Label.parse("¬ab"), aLabel4B, -15);

		g.addEdge(ab, A, B);
		g.addEdge(ba, B, A);
		g.addEdge(ca, C, A);
		
		this.cstnu.setG(g);
		
		this.cstnu.labeledLetterRemovalRule(C, A, ca);

		LabeledIntEdgePluggable abOk = new LabeledIntEdgePluggable("CA", this.labeledIntValueMapClass);
//		abOk.mergeLabeledValue(Label.parse("a"), -3);
		abOk.mergeLabeledValue(Label.parse("ab"), -4);

		assertEquals("Upper Case values:", abOk.getLabeledValueMap(), ca.getLabeledValueMap());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.attic.CSTNU_NodeSet#crossCaseRule(it.univr.di.cstnu.graph.LabeledIntGraph, it.univr.di.cstnu.graph.LabeledIntGraph, CSTNUCheckStatus)}
	 * 
	 * <pre>
	 * A &lt;--- -3,D,b¬c--- C &lt;-----3,c,ab---- D
	 * </pre>
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testCrossCaseRule() {
		// System.out.printf("CROSS CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode A = new LabeledNode("A");
		LabeledNode C = new LabeledNode("C");
		LabeledNode D = new LabeledNode("D");
		LabeledIntEdgePluggable dc = new LabeledIntEdgePluggable("DC", this.labeledIntValueMapClass);
		LabeledIntEdgePluggable ca = new LabeledIntEdgePluggable("CA", this.labeledIntValueMapClass);
		LabeledIntEdgePluggable da = new LabeledIntEdgePluggable("DA", this.labeledIntValueMapClass);
		g.addEdge(dc, D, C);
		g.addEdge(ca, C, A);
		g.addEdge(da, D, A);
		dc.setLowerCaseValue(Label.parse("ab"), new ALabel(C.getName(), this.alpha), 3);

		ca.mergeUpperCaseValue(Label.parse("b¬c"), new ALabel(D.getName(), this.alpha), -3);
		ca.mergeUpperCaseValue(Label.parse("b¬f"), new ALabel(D.getName(), this.alpha), 3);
		ca.mergeUpperCaseValue(Label.parse("¬b"), new ALabel(D.getName(), this.alpha), -4);
		ca.mergeUpperCaseValue(Label.parse("ab"), new ALabel(C.getName(), this.alpha), -4);

		this.cstnu.setG(g);
		this.cstnu.labeledCrossLowerCaseRule(D, C, A, dc, ca, da, null);

		LabeledIntEdgePluggable daOk = new LabeledIntEdgePluggable("DA", this.labeledIntValueMapClass);
		daOk.mergeUpperCaseValue(Label.parse("ab¬c"), new ALabel("D", this.alpha), 0);

		assertEquals("Upper Case values:", daOk.getUpperCaseValueMap(), da.getUpperCaseValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU_NodeSet#labelModificationR0R2R4(LabeledIntGraph, boolean))}.
	 * 
	 * @throws WellDefinitionException
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabelModificationR0() throws WellDefinitionException {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode P = new LabeledNode("P", 'p');
//		LabeledNode X = new LabeledNode("X");
		LabeledNode Q = new LabeledNode("Q?", 'q');
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		LabeledNode C = new LabeledNode("C?", 'c');
		Q.setLabel(Label.parse("p"));
		C.setLabel(Label.parse("¬p"));
		LabeledIntEdgePluggable pz = new LabeledIntEdgePluggable("PX", this.labeledIntValueMapClass);
		pz.mergeLabeledValue(Label.parse("abpq"), -10);// ok but p has to be removed
		pz.mergeLabeledValue(Label.parse("ab¬p"), 0);// it is removed during init by (0, ⊡)
		pz.mergeLabeledValue(Label.parse("c¬p"), 1);// verrà cancellato in fase di init!
		pz.mergeLabeledValue(Label.parse("¬c¬pa"), -1);// ok but ¬c¬p has to be removed
		pz.mergeUpperCaseValue(Label.parse("ab¬p"), new ALabel("C", this.alpha), -11);// ok
		g.addEdge(pz, P, this.Z);
		g.addVertex(Q);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(C);

		this.cstnu.setG(g);
		this.cstnu.initAndCheck();// from nov 2017, it doesn't clear odd values
		this.cstnu.labelModificationqR0(P, pz);

		LabeledIntEdgePluggable pxOK = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		//if R0 is applied!
//		pxOK.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alpha), -11);
//		pxOK.mergeLabeledValue(Label.parse("ab"), -10);
//		pxOK.mergeLabeledValue(Label.parse("a"), -1);
//		pxOK.mergeLabeledValue(Label.parse("c¬p"), 1);
		//if only qR0 is applied!
		pxOK.mergeLabeledValue(Label.emptyLabel, 0);// ok
		pxOK.mergeLabeledValue(Label.parse("abq"), -10);// ok if it is streamlined
		pxOK.mergeLabeledValue(Label.parse("ab"), -10);// ok if it is NOT streamlined
		pxOK.mergeLabeledValue(Label.parse("ab¬p"), 0); // quindi non è memorizzato
		pxOK.mergeLabeledValue(Label.parse("c¬p"), 1);// viene cancellato dallo 0
		pxOK.mergeLabeledValue(Label.parse("a¬c"), -1);// ok if it is streamlined
		pxOK.mergeLabeledValue(Label.parse("a"), -1);// ok if it is NOT streamlined
		pxOK.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alpha), -11);

		assertEquals("R0: p?X labeled values.", pxOK.getLabeledValueMap(), pz.getLabeledValueMap());
		assertEquals("R0: px upper case labedled values.", pxOK.getUpperCaseValueMap(), pz.getUpperCaseValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU_NodeSet#labelModificationR0R2R4(LabeledIntGraph, boolean))}.
	 * 
	 * @throws WellDefinitionException
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabelModificationQR0() throws WellDefinitionException {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode P = new LabeledNode("P", 'p');
		LabeledNode Q = new LabeledNode("Q?", 'q');
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		LabeledNode C = new LabeledNode("C?", 'c');
		Q.setLabel(Label.parse("p"));
//		C.setLabel(Label.parse("p"));
		LabeledIntEdgePluggable pz = new LabeledIntEdgePluggable("PZ", this.labeledIntValueMapClass);
		pz.mergeLabeledValue(Label.parse("abp¿q"), -10);
		pz.mergeLabeledValue(Label.parse("ab¬p"), -1);
		pz.mergeLabeledValue(Label.parse("¬a¬c¬p"), -2);
		pz.mergeLabeledValue(Label.parse("¬acp"), -2);
		pz.mergeLabeledValue(Label.parse("¿c¿p"), 1);
		pz.mergeUpperCaseValue(Label.parse("¬a¬p"), new ALabel("C", this.alpha), -2);
		g.addEdge(pz, P, this.Z);
		g.addVertex(Q);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(C);
		g.setZ(this.Z);
		this.cstnu.setG(g);
		this.cstnu.initAndCheck();
		this.cstnu.labelModificationqR0(P, pz);

		LabeledIntEdgePluggable pxOK = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		pxOK.mergeLabeledValue(Label.emptyLabel, 0);
		pxOK.mergeLabeledValue(Label.parse("ab"), -1);
		// pxOK.mergeLabeledValue(Label.parse("ab¿q"), -10);if it were streamlined
		pxOK.mergeLabeledValue(Label.parse("ab"), -10);// if it is not streamlined

		pxOK.mergeLabeledValue(Label.parse("¬a"), -2);
		// pxOK.mergeLabeledValue(Label.parse("¿c¿p"), 1);// NO!
//		pxOK.mergeUpperCaseValue(Label.parse("a¿b"), new ALabel("C", this.alpha), -11);

		assertEquals("R0: P?Z labeled values.", pxOK.getLabeledValueMap(), pz.getLabeledValueMap());
		assertEquals("R0: PZ upper case labedled values.", pxOK.getUpperCaseValueMap().toString(), pz.getUpperCaseValueMap().toString());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU_NodeSet#labelModificationR1R3R5(LabeledIntGraph, LabeledIntGraph, CSTNUCheckStatus)))}.
	 * 
	 * <pre>
	 * P ----[0, ,¬b][-10, ,ab][-11,C,ab]--&gt; X &lt;--------- Y
	 */
	@SuppressWarnings({ "javadoc" })
	@Test
	public final void testLabelModificationR3() {
		// System.out.printf("R1-R3 CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode P = new LabeledNode("P", 'p');
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		LabeledNode C = new LabeledNode("C?", 'c');
		LabeledNode G = new LabeledNode("G?", 'g');
		G.setLabel(Label.parse("p"));
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(G);
		g.addVertex(C);
		g.addVertex(X);
		g.addVertex(Y);
		g.setZ(this.Z);
		LabeledIntEdgePluggable gp = new LabeledIntEdgePluggable("GP", this.labeledIntValueMapClass);
		gp.mergeLabeledValue(Label.parse("p"), -4);
		g.addEdge(gp, G, P);

		LabeledIntEdgePluggable px = new LabeledIntEdgePluggable("PX", this.labeledIntValueMapClass);
		px.mergeLabeledValue(Label.parse("¬b"), 0);
		px.mergeLabeledValue(Label.parse("ab"), -10);
		px.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alpha), -11);

		LabeledIntEdgePluggable yx = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		yx.mergeLabeledValue(Label.parse("bgp"), -4);
		yx.mergeLabeledValue(Label.parse("cp"), -10);
		yx.mergeLabeledValue(Label.parse("c¬p"), 11);
		yx.mergeUpperCaseValue(Label.parse("bgp"), new ALabel("C", this.alpha), -7);

		g.addEdge(px, P, X);
		g.addEdge(yx, Y, X);

		wellDefinition(g);

		this.cstnu.labelModificationqR3(Y, yx);

		// <YX, normal, Y, X, L:{(¬ABGp, -4) (ABG, -4) }, LL:{}, UL:{(¬ABG¬p,C:-4) (¬ABGp,C:-7) (ABG,C:-7) }>
		LabeledIntEdgePluggable yxOK = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		// yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
//		yxOK.mergeLabeledValue(Label.parse("ab"), -4); from 20171017 R3 is only qR3*
//		yxOK.mergeLabeledValue(Label.parse("abc"), -10); from 20171017 R3 is only qR3*
//		yxOK.mergeLabeledValue(Label.parse("¬bc"), 0);// R5 rule. from 20171017 R3 is only qR3*
		yxOK.mergeLabeledValue(Label.parse("bgp"), -4);//original
		yxOK.mergeLabeledValue(Label.parse("c¬p"), 11);//original
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);//original

//		yxOK.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alpha), -7);from 20171017 R3 is only qR3*
		yxOK.mergeUpperCaseValue(Label.parse("bgp"), new ALabel("C", this.alpha), -7);//original

		assertEquals("R3: yx labeled values.", yxOK.getLabeledValueMap(), yx.getLabeledValueMap());
		assertEquals("R3: yx upper case labedled values.", yxOK.getUpperCaseValueMap(), yx.getUpperCaseValueMap());
	}

	
	
	/**
	 * 
	 */
	@Test
	public final void testLabelModificationR3bis() {
		this.alpha.clear();
		
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode B = new LabeledNode("B?", 'b');
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode N8 = new LabeledNode("n8");
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(N8);
		g.setZ(this.Z);
		
		LabeledIntEdgePluggable eN8Z = new LabeledIntEdgePluggable("n8_Z", this.labeledIntValueMapClass);
		LabeledIntMap map = AbstractLabeledIntMap.parse("{(-20, ab) (-∞, ¿ab) (-8, ¬b) (-17, b) }");
		eN8Z.setLabeledValueMap(map);
		LabeledALabelIntTreeMap map1 = LabeledALabelIntTreeMap.parse("{(D, -∞, ¿ab) (D, -30, ab) (D, -∞, a¿b) (D, -4, ¿b) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -19, ¬ab) (F, -∞, ¿ab) }", this.alpha);
		eN8Z.setUpperCaseValueMap(map1);
		g.addEdge(eN8Z, N8, this.Z);
		
		//{❮B?_Z; derived; {(0, ⊡) }; ❯, ❮A?_Z; derived; {(-2, ⊡) (-4, b) }; ❯}
		LabeledIntEdgePluggable eAZ = new LabeledIntEdgePluggable("A?_Z", this.labeledIntValueMapClass);
		map = AbstractLabeledIntMap.parse("{(-2, ⊡) (-4, b) }");
		eAZ.setLabeledValueMap(map);
		eAZ.setUpperCaseValueMap(map1);
		g.addEdge(eAZ, A, this.Z);

		// System.out.println(eN8Z);
		this.cstnu.labelModificationqR3(N8, eN8Z);
		
		LabeledIntEdgePluggable eN8ZOK = new LabeledIntEdgePluggable("n8_Z", this.labeledIntValueMapClass);
		map = AbstractLabeledIntMap.parse("{(-20, ab) (-∞, ¿ab) (-8, ¬b) (-17, b) }");
		eN8ZOK.setLabeledValueMap(map);
		map1 = LabeledALabelIntTreeMap.parse("{(D, -∞, ¿ab) (D, -30, ab) (D, -∞, a¿b) (D, -4, ¿b) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -19, ¬ab) (F, -∞, ¿ab) }", this.alpha);
		eN8ZOK.setUpperCaseValueMap(map1);
		
		assertEquals("R3: eN8Z labeled values.", eN8ZOK.getLabeledValueMap(), eN8Z.getLabeledValueMap());
		assertEquals("R3: eN8ZOK upper case labedled values.", eN8ZOK.getUpperCaseValueMap(), eN8Z.getUpperCaseValueMap());

		this.cstnu.labelModificationqR3(N8, eN8Z);

		assertEquals("R3: eN8Z labeled values.", eN8ZOK.getLabeledValueMap(), eN8Z.getLabeledValueMap());
		assertEquals("R3: eN8ZOK upper case labedled values.", eN8ZOK.getUpperCaseValueMap(), eN8Z.getUpperCaseValueMap());

	}
	
	
	
	/**
	 * Test method for {@link it.univr.di.cstnu.CSTNU_NodeSet#labelModificationR1R3R5(LabeledIntGraph, LabeledIntGraph, CSTNUCheckStatus)))}.
	 * 
	 * <pre>
	 * P ----[0, ,¬b][-10, ,ab][-11,C,ab]--&gt; Z &lt;--------- Y
	 */
	@SuppressWarnings({ "javadoc" })
	@Test
	public final void testLabelModificationQR3() {
		// System.out.printf("R1-R3 CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode P = new LabeledNode("P", 'p');
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		LabeledNode C = new LabeledNode("C?", 'c');
		LabeledNode G = new LabeledNode("G?", 'g');
		G.setLabel(Label.parse("p"));
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(G);
		g.addVertex(C);
		g.addVertex(Y);
		g.setZ(this.Z);
		LabeledIntEdgePluggable gp = new LabeledIntEdgePluggable("GP", this.labeledIntValueMapClass);
		gp.mergeLabeledValue(Label.parse("p"), -4);
		g.addEdge(gp, G, P);

		LabeledIntEdgePluggable pz = new LabeledIntEdgePluggable("PZ", this.labeledIntValueMapClass);
		pz.mergeLabeledValue(Label.parse("¬b"), 0);
		pz.mergeLabeledValue(Label.parse("ab"), -10);
		pz.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alpha), -11);

		LabeledIntEdgePluggable yz = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
		yz.mergeLabeledValue(Label.parse("bg¿p"), -4);
		yz.mergeLabeledValue(Label.parse("cp"), -10);
		yz.mergeLabeledValue(Label.parse("¿cp"), -11);
		yz.mergeUpperCaseValue(Label.parse("bgp"), new ALabel("C", this.alpha), -7);

		g.addEdge(pz, P, this.Z);
		g.addEdge(yz, Y, this.Z);

		wellDefinition(g);

		this.cstnu.labelModificationqR3(Y, yz);

		// <YX, normal, Y, X, L:{(¬ABGp, -4) (ABG, -4) }, LL:{}, UL:{(¬ABG¬p,C:-4) (¬ABGp,C:-7) (ABG,C:-7) }>
		LabeledIntEdgePluggable yxOK = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		// yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
		yxOK.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alpha), -7);
		yxOK.mergeUpperCaseValue(Label.parse("abc"), new ALabel("C", this.alpha), -10);// it could not be present because there is the labeled value (-10,abc)... it depends in which
																// order it is inserted.
		yxOK.mergeUpperCaseValue(Label.parse("bgp"), new ALabel("C", this.alpha), -7);//
		yxOK.mergeUpperCaseValue(Label.parse("ab¿c"), new ALabel("C", this.alpha), -11);//

		yxOK.mergeLabeledValue(Label.parse("ab"), -4);
		yxOK.mergeLabeledValue(Label.parse("abc"), -10);
		yxOK.mergeLabeledValue(Label.parse("bg¿p"), -4);
		// yxOK.mergeLabeledValue(Label.parse("¬bc"), 0);// R5 rule
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);
		yxOK.mergeLabeledValue(Label.parse("¿cp"), -11);

		assertEquals("R3: yx labeled values.", yxOK.getLabeledValueMap(), yz.getLabeledValueMap());
		assertEquals("R3: yx upper case labedled values.", yxOK.getUpperCaseValueMap(), yz.getUpperCaseValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.attic.CSTNU_NodeSet#lowerCaseRule(LabeledIntGraph, LabeledIntGraph, CSTNUCheckStatus)}.
	 * 
	 * <pre>
	 * A &lt;----[1,,b][0,,c][-11,,d]--- C &lt;---[3,c,ab]--- D
	 * </pre>
	@SuppressWarnings("javadoc")
	@Test
	public final void testLowerCaseRule() {
		// System.out.printf("LOWER CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledIntEdgePluggable dc = new LabeledIntEdgePluggable("DC", this.labeledIntValueMapClass);
		LabeledIntEdgePluggable ca = new LabeledIntEdgePluggable("CA", this.labeledIntValueMapClass);
		LabeledIntEdgePluggable da = new LabeledIntEdgePluggable("DA", this.labeledIntValueMapClass);
		dc.setLowerCaseValue(Label.parse("ab"), new ALabel("c", this.alpha), 3);
		ca.mergeLabeledValue(Label.parse("b"), 1);
		ca.mergeLabeledValue(Label.parse("c"), 0);
		ca.mergeLabeledValue(Label.parse("d"), -11);
		LabeledNode A = new LabeledNode("A");
		LabeledNode C = new LabeledNode("C");
		LabeledNode D = new LabeledNode("D");
		LabeledNode OA = new LabeledNode("A?", 'a');
		LabeledNode OB = new LabeledNode("B?", 'b');
		LabeledNode OC = new LabeledNode("C?", 'c');
		LabeledNode OD = new LabeledNode("D?", 'd');
		g.addVertex(OA);
		g.addVertex(OB);
		g.addVertex(OC);
		g.addVertex(OD);
		g.addEdge(dc, D, C);
		g.addEdge(ca, C, A);
		g.addEdge(da, D, A);
		g.setZ(this.Z);
		
		wellDefinition(g);
		this.cstnu.labeledLowerCaseRule(D, C, A, this.Z, dc, ca, da);

		LabeledIntEdgePluggable daOk = new LabeledIntEdgePluggable("DA", this.labeledIntValueMapClass);
		daOk.mergeLabeledValue(Label.parse("abc"), 3);
		daOk.mergeLabeledValue(Label.parse("abd"), -8);

		assertEquals("Lower Case values:", daOk.getLabeledValueMap(), da.getLabeledValueMap());
		// System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
		// System.out.printf("G: %s\n", g1);
	}
	 */

	/**
	 * Test method for {@link it.univr.di.attic.CSTNU_NodeSet#lowerCaseRule(LabeledIntGraph, LabeledIntGraph, CSTNUCheckStatus)}.
	 * 
	 * <pre>
	 * Z &lt;----[1,,b][0,,c][-11,,¬b]--- C &lt;---[3,c,ab]--- D
	 * </pre>
	@SuppressWarnings("javadoc")
	@Test
	public final void testQLowerCaseRule() {
		// System.out.printf("LOWER CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledIntEdgePluggable dc = new LabeledIntEdgePluggable("DC", this.labeledIntValueMapClass);
		LabeledIntEdgePluggable ca = new LabeledIntEdgePluggable("CA", this.labeledIntValueMapClass);
		LabeledIntEdgePluggable da = new LabeledIntEdgePluggable("DA", this.labeledIntValueMapClass);
		dc.setLowerCaseValue(Label.parse("ab"), new ALabel("c", this.alpha), 3);
		ca.mergeLabeledValue(Label.parse("b"), 1);
		ca.mergeLabeledValue(Label.parse("c"), 0);
		ca.mergeLabeledValue(Label.parse("¬b"), -11);
		LabeledNode A = new LabeledNode("A");
		LabeledNode C = new LabeledNode("C");
		LabeledNode D = new LabeledNode("D");
		LabeledNode OA = new LabeledNode("A?", 'a');
		LabeledNode OB = new LabeledNode("B?", 'b');
		LabeledNode OC = new LabeledNode("C?", 'c');
		LabeledNode OD = new LabeledNode("D?", 'd');
		g.addVertex(OA);
		g.addVertex(OB);
		g.addVertex(OC);
		g.addVertex(OD);
		g.addEdge(dc, D, C);
		g.addEdge(ca, C, A);
		g.addEdge(da, D, A);
		g.setZ(A);
		wellDefinition(g);
		this.cstnu.labeledLowerCaseRule(D, C, A, A, dc, ca, da);

		LabeledIntEdgePluggable daOk = new LabeledIntEdgePluggable("DA", this.labeledIntValueMapClass);
		daOk.mergeLabeledValue(Label.parse("abc"), 3);
		daOk.mergeLabeledValue(Label.parse("a¿b"), -8);

		assertEquals("Lower Case values:", daOk.getLabeledValueMap(), da.getLabeledValueMap());
		// System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
		// System.out.printf("G: %s\n", g1);
	}
	 */

	/**
	 * Test method for {@link it.univr.di.attic.CSTNU_NodeSet#upperCaseRule(LabeledIntGraph, LabeledIntGraph, CSTNUCheckStatus)}.
	 * A &lt;---[3,B,ab]--- C &lt;----[-13,,b][11,,c]----D
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testUpperCaseRule() {
		// System.out.printf("UPPER CASE\n");
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledIntEdgePluggable dc = new LabeledIntEdgePluggable("dc", this.labeledIntValueMapClass);
		LabeledIntEdgePluggable ca = new LabeledIntEdgePluggable("ca", this.labeledIntValueMapClass);
		LabeledIntEdgePluggable da = new LabeledIntEdgePluggable("da", this.labeledIntValueMapClass);
		ca.mergeUpperCaseValue(Label.parse("ab"), new ALabel("B", this.alpha), 3);
		dc.mergeLabeledValue(Label.parse("b"), -13);
		dc.mergeLabeledValue(Label.parse("c"), 11);
		LabeledNode C = new LabeledNode("C"), A = new LabeledNode("A"), D = new LabeledNode("D");
		g.addEdge(dc, D, C);
		g.addEdge(ca, C, A);
		g.addEdge(da, D, A);
		this.cstnu.setG(g);
		this.cstnu.labeledPropagationqLP(D, C, A, dc, ca, da);

		LabeledIntEdgePluggable daOk = new LabeledIntEdgePluggable("DA", this.labeledIntValueMapClass);
		daOk.mergeUpperCaseValue(Label.parse("abc"), new ALabel("B", this.alpha), 14);
		daOk.mergeUpperCaseValue(Label.parse("ab"), new ALabel("B", this.alpha), -10);

		assertEquals("Upper Case values:", daOk.getUpperCaseValueMap(), da.getUpperCaseValueMap());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTNU#labeledPropagationqLP(LabeledNode, LabeledNode, LabeledNode, it.univr.di.cstnu.graph.LabeledIntEdge, it.univr.di.cstnu.graph.LabeledIntEdge, it.univr.di.cstnu.graph.LabeledIntEdge)}
	 */
	@Test
	public final void test_lncRule() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode W = new LabeledNode("W", 'w');
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		W.setLabel(Label.parse("a"));
		g.addVertex(W);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(Y);
		g.setZ(this.Z);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		XY.mergeLabeledValue(Label.emptyLabel, 2);
		XY.mergeLabeledValue(Label.parse("¬a"), -2);
		XY.mergeLabeledValue(Label.parse("b"), 1);
		XY.mergeLabeledValue(Label.parse("a¬b"), -1);
//		System.out.println(XY);

		LabeledIntEdgePluggable YW = new LabeledIntEdgePluggable("YW", this.labeledIntValueMapClass);
		YW.mergeLabeledValue(Label.parse("a"), 2);
		YW.mergeLabeledValue(Label.parse("ab"), 1);
		YW.mergeLabeledValue(Label.parse("a¬b"), -1);
//		System.out.println(YW);

		LabeledIntEdgePluggable XW = new LabeledIntEdgePluggable("XW", this.labeledIntValueMapClass);
		
		g.addEdge(XY, X, Y);
		g.addEdge(YW, Y, W);
		g.addEdge(XW, X, W);

		wellDefinition(g);
//		System.out.println(XW);

		this.cstnu.labeledPropagationqLP(X, Y, W, XY, YW, XW);
//		System.out.println(XW);
		
		LabeledIntEdgePluggable xwOK = new LabeledIntEdgePluggable("XW", this.labeledIntValueMapClass);
		// xwOK.mergeLabeledValue(Label.parse("a"), 4);if no positive edge are propagated
		xwOK.mergeLabeledValue(Label.parse("a¬b"), -2);
		// xwOK.mergeLabeledValue(Label.parse("ab"), 2);
//		xwOK.mergeLabeledValue(Label.parse("¿a"), 0); From 20171010 unknown literal are not more propagated
//		xwOK.mergeLabeledValue(Label.parse("¿a¬b"), -3);
//		xwOK.mergeLabeledValue(Label.parse("¿ab"), -1);
	
		assertEquals("No case: XW labeled values.", xwOK.getLabeledValueMap(), XW.getLabeledValueMap());
	}
	
	
	
	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTNU#labeledPropagationqLP(LabeledNode, LabeledNode, LabeledNode, it.univr.di.cstnu.graph.LabeledIntEdge, it.univr.di.cstnu.graph.LabeledIntEdge, it.univr.di.cstnu.graph.LabeledIntEdge)}
	 */
	@Test
	public final void test_lucRule() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode W = new LabeledNode("W", 'w');
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		W.setLabel(Label.parse("a"));
		g.addVertex(W);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(Y);
		g.setZ(this.Z);

		ALabel aLabel = new ALabel(A.getName(), this.alpha);
		ALabel bLabel = new ALabel(B.getName(), this.alpha);
		
		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		XY.mergeLabeledValue(Label.emptyLabel, 2);
		XY.mergeLabeledValue(Label.parse("¬a"), 1);
		XY.mergeLabeledValue(Label.parse("b"), 1);
		XY.mergeLabeledValue(Label.parse("a¬b"), -1);
//		System.out.println(XY);

		LabeledIntEdgePluggable YW = new LabeledIntEdgePluggable("YW", this.labeledIntValueMapClass);
		YW.mergeLabeledValue(Label.parse("a"), -1);
		YW.mergeLabeledValue(Label.parse("a¬b"), -2);
		YW.mergeUpperCaseValue(Label.parse("a"), aLabel, -2);
		YW.mergeUpperCaseValue(Label.parse("ab"), aLabel, -3);
		YW.mergeUpperCaseValue(Label.parse("a¬b"), aLabel.conjunction(bLabel), -4);
//		System.out.println(YW);

		LabeledIntEdgePluggable XW = new LabeledIntEdgePluggable("XW", this.labeledIntValueMapClass);
		
		g.addEdge(XY, X, Y);
		g.addEdge(YW, Y, W);
		g.addEdge(XW, X, W);

		wellDefinition(g);
//		System.out.println(XW);

		this.cstnu.labeledPropagationqLP(X, Y, W, XY, YW, XW);
//		System.out.println(XW);
		
		LabeledIntEdgePluggable xwOK = new LabeledIntEdgePluggable("XW", this.labeledIntValueMapClass);
		xwOK.mergeLabeledValue(Label.parse("a"), 0);
//		xwOK.mergeLabeledValue(Label.parse("¿a¬b"), -3);From 20171010 unknown literal are not more propagated
		xwOK.mergeLabeledValue(Label.parse("ab"), 2);
		xwOK.mergeLabeledValue(Label.parse("a¬b"), -3);
		xwOK.mergeUpperCaseValue(Label.parse("a"), aLabel, 0);//not stored because is 0
		xwOK.mergeUpperCaseValue(Label.parse("ab"), aLabel, -2);
//		xwOK.mergeUpperCaseValue(Label.parse("a¿b"), aLabel, -4);
//		xwOK.mergeUpperCaseValue(Label.parse("a¬b"), aLabel.conjunction(bLabel), -5);//not allowed because rule does not back propagate conjuncted UC from node != Z

//		assertEquals("No case: XW ", xwOK.toString(), XW.toString());
	
		assertEquals("No case: XW labeled values.", xwOK.getLabeledValueMap(), XW.getLabeledValueMap());
		assertEquals("No case: XW upper case labedled values.", xwOK.getUpperCaseValueMap(), XW.getUpperCaseValueMap());
	}
	
	
	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTNU#labeledPropagationqLP(LabeledNode, LabeledNode, LabeledNode, it.univr.di.cstnu.graph.LabeledIntEdge, it.univr.di.cstnu.graph.LabeledIntEdge, it.univr.di.cstnu.graph.LabeledIntEdge)}
	 */
	@Test
	public final void test_flucRule() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(Y);
		g.setZ(this.Z);

		ALabel aLabel = new ALabel(A.getName(), this.alpha);
		ALabel bLabel = new ALabel(B.getName(), this.alpha);
		
		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		XY.mergeUpperCaseValue(Label.emptyLabel, aLabel, 1);
		XY.mergeUpperCaseValue(Label.parse("¬a"), aLabel,-3);
		XY.mergeUpperCaseValue(Label.parse("b"), aLabel, -4);
		XY.mergeUpperCaseValue(Label.parse("a¬b"), aLabel.conjunction(bLabel), -2);
//		System.out.println(XY);

		LabeledIntEdgePluggable YZ = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
		YZ.mergeLabeledValue(Label.parse("a"), 2);
		YZ.mergeLabeledValue(Label.parse("ab"), 1);
		YZ.mergeLabeledValue(Label.parse("a¬b"), -1);
	
		LabeledIntEdgePluggable XZ = new LabeledIntEdgePluggable("XZ", this.labeledIntValueMapClass);

		g.addEdge(XY, X, Y);
		g.addEdge(YZ, Y, this.Z);
		g.addEdge(XZ, X, this.Z);

		wellDefinition(g);
//		System.out.println(XW);

		this.cstnu.labeledPropagationqLP(X, Y, this.Z, XY, YZ, XZ);
//		System.out.println(XW);
		
		LabeledIntEdgePluggable xzOK = new LabeledIntEdgePluggable("XW", this.labeledIntValueMapClass);
		//FLUC and LCUC change upper case values!
//		xzOK.mergeUpperCaseValue(Label.parse("¿a"), aLabel, -1); From 20171010 unknown literal are not more propagated
//		xzOK.mergeUpperCaseValue(Label.parse("¿a¬b"), aLabel, -4);
//		xzOK.mergeUpperCaseValue(Label.parse("ab"), aLabel, -3);// X<>A?, so rule cannot be applied
//		xzOK.mergeUpperCaseValue(Label.parse("a¿b"), aLabel, -5);
		
		assertEquals("No case: XZ labeled values.", xzOK.getLabeledValueMap(), XZ.getLabeledValueMap());
		assertEquals("No case: XZ upper case labedled values.", xzOK.getUpperCaseValueMap(), XZ.getUpperCaseValueMap());
	}

	
	/**
	 * Test method for
	 * {@link it.univr.di.cstnu.algorithms.CSTNU#labeledPropagationqLP(LabeledNode, LabeledNode, LabeledNode, it.univr.di.cstnu.graph.LabeledIntEdge, it.univr.di.cstnu.graph.LabeledIntEdge, it.univr.di.cstnu.graph.LabeledIntEdge)}
	 */
	@Test
	public final void test_lcucRule() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(Y);
		g.setZ(this.Z);

		ALabel aLabel = new ALabel(A.getName(), this.alpha);
		ALabel bLabel = new ALabel(B.getName(), this.alpha);
		
		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		XY.mergeUpperCaseValue(Label.emptyLabel, aLabel, 1);
		XY.mergeUpperCaseValue(Label.parse("¬a"), aLabel,-3);
		XY.mergeUpperCaseValue(Label.parse("b"), aLabel, -4);
		XY.mergeUpperCaseValue(Label.parse("a¬b"), aLabel.conjunction(bLabel), -2);
//		System.out.println(XY);

		LabeledIntEdgePluggable YZ = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
		YZ.mergeUpperCaseValue(Label.parse("a"), aLabel, 1);
		YZ.mergeUpperCaseValue(Label.parse("ab"), aLabel, 0);
		YZ.mergeUpperCaseValue(Label.parse("a¬b"), aLabel.conjunction(bLabel), -2);
	
		LabeledIntEdgePluggable XZ = new LabeledIntEdgePluggable("XZ", this.labeledIntValueMapClass);
		
		g.addEdge(XY, X, Y);
		g.addEdge(YZ, Y, this.Z);
		g.addEdge(XZ, X, this.Z);

		wellDefinition(g);
//		System.out.println(XW);

		this.cstnu.labeledPropagationqLP(X, Y, this.Z, XY, YZ, XZ);
//		System.out.println(XW);
		
		LabeledIntEdgePluggable xzOK = new LabeledIntEdgePluggable("XZ", this.labeledIntValueMapClass);
		//<{¿a=A?->-2, ab=A?->-4, ¿a¬b=A?∙B?->-5, a¿b=A?∙B?->-6}>
//		xwOK.mergeUpperCaseValue(Label.parse("¿a"), aLabel, -2);From 20171010 unknown literal are not more propagated
//		xwOK.mergeUpperCaseValue(Label.parse("ab"), aLabel, -4);
//		xwOK.mergeUpperCaseValue(Label.parse("¿a¬b"), aLabel.conjunction(bLabel), -5);
//		xwOK.mergeUpperCaseValue(Label.parse("a¿b"), aLabel.conjunction(bLabel), -6);
			

		assertEquals("No case: XZ labeled values.", xzOK.getLabeledValueMap(), XZ.getLabeledValueMap());
		assertEquals("No case: XZ upper case labedled values.", xzOK.getUpperCaseValueMap(), XZ.getUpperCaseValueMap());
	}
}
