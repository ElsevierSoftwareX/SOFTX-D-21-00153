/**
 * 
 */
package it.univr.di.cstnu.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import it.univr.di.cstnu.algorithms.CSTNU;
import it.univr.di.cstnu.algorithms.WellDefinitionException;
import it.univr.di.cstnu.algorithms.CSTNU.CSTNUCheckStatus;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.ALabelAlphabet;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledContingentIntTreeMap;
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
		ab.mergeLowerLabelValue(Label.parse("b"), new ALabel("b", this.alpha), 13);

		final LabeledIntEdgePluggable ba = new LabeledIntEdgePluggable("BA", this.labeledIntValueMapClass);// now CaseLabelRemoval checks the lower bound, no the
																										// guards.
		final LabeledIntEdgePluggable ca = new LabeledIntEdgePluggable("CA", this.labeledIntValueMapClass);
		ca.mergeUpperLabelValue(Label.parse("ab"), new ALabel("B", this.alpha), -4);
		ca.mergeUpperLabelValue(Label.parse("a"), new ALabel("B", this.alpha), -3);
		ca.mergeUpperLabelValue(Label.parse("¬b"), new ALabel("B", this.alpha), -3);
		ca.mergeUpperLabelValue(Label.parse("¬ab"), new ALabel("B", this.alpha), -15);

		final LabeledNode A = new LabeledNode("A");
		final LabeledNode B = new LabeledNode("B");
		final LabeledNode C = new LabeledNode("C");
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
		dc.mergeLowerLabelValue(Label.parse("ab"), new ALabel("c", this.alpha), 3);

		ca.mergeUpperLabelValue(Label.parse("b¬c"), new ALabel("D", this.alpha), -3);
		ca.mergeUpperLabelValue(Label.parse("b¬f"), new ALabel("D", this.alpha), 3);
		ca.mergeUpperLabelValue(Label.parse("¬b"), new ALabel("D", this.alpha), -4);
		ca.mergeUpperLabelValue(Label.parse("ab"), new ALabel("C", this.alpha), -4);

		this.cstnu.setG(g);
		this.cstnu.labeledCrossLowerCaseRule(D, C, A, dc, ca, da);

		LabeledIntEdgePluggable daOk = new LabeledIntEdgePluggable("DA", this.labeledIntValueMapClass);
		daOk.mergeUpperLabelValue(Label.parse("ab¬c"), new ALabel("D", this.alpha), 0);

		assertEquals("Upper Case values:", daOk.getUpperLabelSet(), da.getUpperLabelSet());
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
		LabeledNode X = new LabeledNode("X");
		LabeledNode Q = new LabeledNode("Q?", 'q');
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		LabeledNode C = new LabeledNode("C?", 'c');
		Q.setLabel(Label.parse("p"));
		C.setLabel(Label.parse("¬p"));
		LabeledIntEdgePluggable px = new LabeledIntEdgePluggable("PX", this.labeledIntValueMapClass);
		px.mergeLabeledValue(Label.parse("abpq"), -10);
		px.mergeLabeledValue(Label.parse("ab¬p"), 0);
		px.mergeLabeledValue(Label.parse("c¬p"), 1);
		px.mergeLabeledValue(Label.parse("¬c¬pa"), -1);
		px.mergeUpperLabelValue(Label.parse("ab¬p"), new ALabel("C", this.alpha), -11);
		g.addEdge(px, P, X);
		g.addVertex(Q);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(C);

		this.cstnu.setG(g);
		this.cstnu.initUpperLowerLabelDataStructure();
		this.cstnu.labelModificationR0(P, X, this.Z, px);

		LabeledIntEdgePluggable pxOK = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		pxOK.mergeUpperLabelValue(Label.parse("ab"), new ALabel("C", this.alpha), -11);
		pxOK.mergeLabeledValue(Label.parse("ab"), -10);
		pxOK.mergeLabeledValue(Label.parse("a"), -1);
		pxOK.mergeLabeledValue(Label.parse("c¬p"), 1);

		assertEquals("R0: p?X labeled values.", pxOK.getLabeledValueMap(), px.getLabeledValueMap());
		assertEquals("R0: px upper case labedled values.", pxOK.getUpperLabelSet(), px.getUpperLabelSet());
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
		C.setLabel(Label.parse("¬p"));
		LabeledIntEdgePluggable pz = new LabeledIntEdgePluggable("PZ", this.labeledIntValueMapClass);
		pz.mergeLabeledValue(Label.parse("abp¿q"), -10);
		pz.mergeLabeledValue(Label.parse("ab¬p"), -1);
		pz.mergeLabeledValue(Label.parse("a¬c¬p"), -1);
		pz.mergeLabeledValue(Label.parse("¿c¿p"), -1);
		pz.mergeUpperLabelValue(Label.parse("a¿b¬p"), new ALabel("C", this.alpha), -11);
		g.addEdge(pz, P, this.Z);
		g.addVertex(Q);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(C);
		g.setZ(this.Z);
		this.cstnu.setG(g);
		this.cstnu.initUpperLowerLabelDataStructure();
		this.cstnu.labelModificationR0(P, this.Z, this.Z, pz);

		LabeledIntEdgePluggable pxOK = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		pxOK.mergeUpperLabelValue(Label.parse("a¿b"), new ALabel("C", this.alpha), -11);
		pxOK.mergeLabeledValue(Label.parse("ab"), -10);
		pxOK.mergeLabeledValue(Label.emptyLabel, -1);

		assertEquals("R0: p?X labeled values.", pxOK.getLabeledValueMap(), pz.getLabeledValueMap());
		assertEquals("R0: px upper case labedled values.", pxOK.getUpperLabelSet(), pz.getUpperLabelSet());
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
		px.mergeUpperLabelValue(Label.parse("ab"), new ALabel("C", this.alpha), -11);

		LabeledIntEdgePluggable yx = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		yx.mergeLabeledValue(Label.parse("bgp"), -4);
		yx.mergeLabeledValue(Label.parse("cp"), -10);
		yx.mergeLabeledValue(Label.parse("c¬p"), 11);
		yx.mergeUpperLabelValue(Label.parse("bgp"), new ALabel("C", this.alpha), -7);

		g.addEdge(px, P, X);
		g.addEdge(yx, Y, X);

		wellDefinition(g);

		this.cstnu.labelModificationR3(Y, X, this.Z, yx);

		// <YX, normal, Y, X, L:{(¬ABGp, -4) (ABG, -4) }, LL:{}, UL:{(¬ABG¬p,C:-4) (¬ABGp,C:-7) (ABG,C:-7) }>
		LabeledIntEdgePluggable yxOK = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		// yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("ab"), -4);
		yxOK.mergeLabeledValue(Label.parse("abc"), -10);
		yxOK.mergeLabeledValue(Label.parse("bgp"), -4);
		yxOK.mergeLabeledValue(Label.parse("¬bc"), 0);// R5 rule
		yxOK.mergeLabeledValue(Label.parse("c¬p"), 11);
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);

		yxOK.mergeUpperLabelValue(Label.parse("ab"), new ALabel("C", this.alpha), -7);
		yxOK.mergeUpperLabelValue(Label.parse("bgp"), new ALabel("C", this.alpha), -7);

		assertEquals("R3: yx labeled values.", yxOK.getLabeledValueMap(), yx.getLabeledValueMap());
		assertEquals("R3: yx upper case labedled values.", yxOK.getUpperLabelSet(), yx.getUpperLabelSet());
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
		eN8Z.setLabeledValue(map);
		LabeledContingentIntTreeMap map1 = LabeledContingentIntTreeMap.parse("{(D, -∞, ¿ab) (D, -30, ab) (D, -∞, a¿b) (D, -4, ¿b) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -19, ¬ab) (F, -∞, ¿ab) }", this.alpha);
		eN8Z.setLabeledUpperCaseValue(map1);
		g.addEdge(eN8Z, N8, this.Z);
		
		//{❮B?_Z; derived; {(0, ⊡) }; ❯, ❮A?_Z; derived; {(-2, ⊡) (-4, b) }; ❯}
		LabeledIntEdgePluggable eAZ = new LabeledIntEdgePluggable("A?_Z", this.labeledIntValueMapClass);
		map = AbstractLabeledIntMap.parse("{(-2, ⊡) (-4, b) }");
		eAZ.setLabeledValue(map);
		eAZ.setLabeledUpperCaseValue(map1);
		g.addEdge(eAZ, A, this.Z);

		System.out.println(eN8Z);
		this.cstnu.labelModificationR3(N8, this.Z, this.Z, eN8Z);
		
		LabeledIntEdgePluggable eN8ZOK = new LabeledIntEdgePluggable("n8_Z", this.labeledIntValueMapClass);
		map = AbstractLabeledIntMap.parse("{(-20, ab) (-∞, ¿ab) (-8, ¬b) (-17, b) }");
		eN8ZOK.setLabeledValue(map);
		map1 = LabeledContingentIntTreeMap.parse("{(D, -∞, ¿ab) (D, -30, ab) (D, -∞, a¿b) (D, -4, ¿b) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -19, ¬ab) (F, -∞, ¿ab) }", this.alpha);
		eN8ZOK.setLabeledUpperCaseValue(map1);
		
		assertEquals("R3: eN8Z labeled values.", eN8ZOK.getLabeledValueMap(), eN8Z.getLabeledValueMap());
		assertEquals("R3: eN8ZOK upper case labedled values.", eN8ZOK.getUpperLabelSet(), eN8Z.getUpperLabelSet());

		this.cstnu.labelModificationR3(N8, this.Z, this.Z, eN8Z);

		assertEquals("R3: eN8Z labeled values.", eN8ZOK.getLabeledValueMap(), eN8Z.getLabeledValueMap());
		assertEquals("R3: eN8ZOK upper case labedled values.", eN8ZOK.getUpperLabelSet(), eN8Z.getUpperLabelSet());

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
		pz.mergeUpperLabelValue(Label.parse("ab"), new ALabel("C", this.alpha), -11);

		LabeledIntEdgePluggable yz = new LabeledIntEdgePluggable("YZ", this.labeledIntValueMapClass);
		yz.mergeLabeledValue(Label.parse("bg¿p"), -4);
		yz.mergeLabeledValue(Label.parse("cp"), -10);
		yz.mergeLabeledValue(Label.parse("¿cp"), -11);
		yz.mergeUpperLabelValue(Label.parse("bgp"), new ALabel("C", this.alpha), -7);

		g.addEdge(pz, P, this.Z);
		g.addEdge(yz, Y, this.Z);

		wellDefinition(g);

		this.cstnu.labelModificationR3(Y, this.Z, this.Z, yz);

		// <YX, normal, Y, X, L:{(¬ABGp, -4) (ABG, -4) }, LL:{}, UL:{(¬ABG¬p,C:-4) (¬ABGp,C:-7) (ABG,C:-7) }>
		LabeledIntEdgePluggable yxOK = new LabeledIntEdgePluggable("YX", this.labeledIntValueMapClass);
		// yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
		yxOK.mergeUpperLabelValue(Label.parse("ab"), new ALabel("C", this.alpha), -7);
		yxOK.mergeUpperLabelValue(Label.parse("abc"), new ALabel("C", this.alpha), -10);// it could not be present because there is the labeled value (-10,abc)... it depends in which
																// order it is inserted.
		yxOK.mergeUpperLabelValue(Label.parse("bgp"), new ALabel("C", this.alpha), -7);//
		yxOK.mergeUpperLabelValue(Label.parse("ab¿c"), new ALabel("C", this.alpha), -11);//

		yxOK.mergeLabeledValue(Label.parse("ab"), -4);
		yxOK.mergeLabeledValue(Label.parse("abc"), -10);
		yxOK.mergeLabeledValue(Label.parse("bg¿p"), -4);
		// yxOK.mergeLabeledValue(Label.parse("¬bc"), 0);// R5 rule
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);
		yxOK.mergeLabeledValue(Label.parse("¿cp"), -11);

		assertEquals("R3: yx labeled values.", yxOK.getLabeledValueMap(), yz.getLabeledValueMap());
		assertEquals("R3: yx upper case labedled values.", yxOK.getUpperLabelSet(), yz.getUpperLabelSet());
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
		dc.mergeLowerLabelValue(Label.parse("ab"), new ALabel("c", this.alpha), 3);
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
		dc.mergeLowerLabelValue(Label.parse("ab"), new ALabel("c", this.alpha), 3);
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
		ca.mergeUpperLabelValue(Label.parse("ab"), new ALabel("B", this.alpha), 3);
		dc.mergeLabeledValue(Label.parse("b"), -13);
		dc.mergeLabeledValue(Label.parse("c"), 11);
		LabeledNode C = new LabeledNode("C"), A = new LabeledNode("A"), D = new LabeledNode("D");
		g.addEdge(dc, D, C);
		g.addEdge(ca, C, A);
		g.addEdge(da, D, A);
		this.cstnu.setG(g);
		this.cstnu.labeledPropagationRule(D, C, A, dc, ca, da);

		LabeledIntEdgePluggable daOk = new LabeledIntEdgePluggable("DA", this.labeledIntValueMapClass);
		daOk.mergeUpperLabelValue(Label.parse("abc"), new ALabel("B", this.alpha), 14);
		daOk.mergeUpperLabelValue(Label.parse("ab"), new ALabel("B", this.alpha), -10);

		assertEquals("Upper Case values:", daOk.getUpperLabelSet(), da.getUpperLabelSet());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.algorithms.CSTNU#labeledPropagationRule()}.
	 * 
	 * <pre>
	 * Y &lt;---[9,,¬b][10,,b]-- P &lt;---[10,,][8,,¬a]--- X
	 * </pre>
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testNoCase() {
		LabeledIntGraph g = new LabeledIntGraph(this.labeledIntValueMapClass);
		LabeledNode P = new LabeledNode("P?", 'p');
		LabeledNode X = new LabeledNode("X");
		LabeledNode Y = new LabeledNode("Y");
		LabeledNode A = new LabeledNode("A?", 'a');
		LabeledNode B = new LabeledNode("B?", 'b');
		g.addVertex(P);
		g.addVertex(A);
		g.addVertex(B);
		g.addVertex(X);
		g.addVertex(Y);
		g.setZ(this.Z);

		LabeledIntEdgePluggable XP = new LabeledIntEdgePluggable("XP", this.labeledIntValueMapClass);
		XP.mergeLabeledValue(Label.emptyLabel, -10);
		XP.mergeLabeledValue(Label.parse("¬a"), 8);

		LabeledIntEdgePluggable PY = new LabeledIntEdgePluggable("PY", this.labeledIntValueMapClass);
		PY.mergeLabeledValue(Label.parse("¬b"), -9);
		PY.mergeLabeledValue(Label.parse("b"), 10);

		LabeledIntEdgePluggable XY = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
		g.addEdge(XP, X, P);
		g.addEdge(PY, P, Y);
		g.addEdge(XY, X, Y);

		wellDefinition(g);

		this.cstnu.labeledPropagationRule(X, P, Y, XP, PY,  XY);//si propagano solo valori negativi
		
		LabeledIntEdgePluggable xyOK = new LabeledIntEdgePluggable("XY", this.labeledIntValueMapClass);
//		xyOK.mergeLabeledValue(Label.parse("¬a¬b"), 17);
//		xyOK.mergeLabeledValue(Label.parse("¬ab"), 18);
		xyOK.mergeLabeledValue(Label.parse("¬b"), -19);
		xyOK.mergeLabeledValue(Label.parse("b"), 0);

		assertEquals("No case: XY labeled values.", xyOK.getLabeledValueMap(), XY.getLabeledValueMap());
		assertEquals("No case: XY upper case labedled values.", xyOK.getUpperLabelSet(), XY.getUpperLabelSet());
	}

}
