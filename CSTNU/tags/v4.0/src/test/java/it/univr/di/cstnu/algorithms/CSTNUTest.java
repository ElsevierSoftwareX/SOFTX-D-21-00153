package it.univr.di.cstnu.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import it.univr.di.cstnu.algorithms.CSTNU.CSTNUCheckStatus;
import it.univr.di.cstnu.graph.CSTNUEdge;
import it.univr.di.cstnu.graph.CSTNUEdgePluggable;
import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.ALabelAlphabet;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;

/**
 * @author posenato
 */
public class CSTNUTest {

	/**
	 * Default implementation class for CSTNEdge
	 */
	static final Class<? extends CSTNUEdge> EDGE_IMPL_CLASS = CSTNUEdgePluggable.class;

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
	 * 
	 */
	TNGraph<CSTNUEdge> g;

	/**
	 * @throws java.lang.Exception nope
	 */
	@Before
	public void setUp() throws Exception {
		this.alpha = new ALabelAlphabet();
		this.g = new TNGraph<>(EDGE_IMPL_CLASS, this.alpha);
		this.Z = this.g.getNodeFactory().get("Z");
		this.g.setZ(this.Z);
		this.cstnu = new CSTNU(this.g);
	}

	/**
	 * @param g1
	 */
	private final void wellDefinition(TNGraph<CSTNUEdge> g1) {
		this.cstnu.setG(g1);
		try {
			this.cstnu.checkWellDefinitionProperties();
		} catch (WellDefinitionException e) {
			fail("TNGraph not well formed: " + e.getMessage());
		}
	}

	/**
	 * @throws WellDefinitionException nope
	 */
	@Test
	public final void testCaseLabelRemovalRule() throws WellDefinitionException {
		final LabeledNode A = this.g.getNodeFactory().get("A");
		final LabeledNode B = this.g.getNodeFactory().get("B");
		ALabel aLabel4B = new ALabel(B.getName(), this.alpha);
		B.setALabel(aLabel4B);
		final LabeledNode C = this.g.getNodeFactory().get("C");

		final LabeledNode a = this.g.getNodeFactory().get("A?");
		a.setObservable('a');
		this.g.addVertex(a);
		final LabeledNode b = this.g.getNodeFactory().get("B?");
		b.setObservable('b');
		this.g.addVertex(b);

		final CSTNUEdge AB = this.g.getEdgeFactory().get("AB");
		AB.setLowerCaseValue(Label.emptyLabel, aLabel4B, 13);
		AB.setConstraintType(ConstraintType.contingent);

		final CSTNUEdge BA = this.g.getEdgeFactory().get("BA");
		BA.setConstraintType(ConstraintType.contingent);
		BA.mergeUpperCaseValue(Label.emptyLabel, aLabel4B, -20);

		final CSTNUEdge cz = this.g.getEdgeFactory().get("CZ");
		cz.mergeUpperCaseValue(Label.parse("ab"), aLabel4B, -4);
		cz.mergeUpperCaseValue(Label.parse("a"), aLabel4B, -3);
		cz.mergeUpperCaseValue(Label.parse("¬b"), aLabel4B, -3);
		cz.mergeUpperCaseValue(Label.parse("¬ab"), aLabel4B, -15);

		final CSTNUEdge az = this.g.getEdgeFactory().get("AZ");
		az.mergeLabeledValue(Label.parse("ab"), -1);

		this.g.addEdge(AB, A, B);
		this.g.addEdge(BA, B, A);
		this.g.addEdge(cz, C, this.Z);
		this.g.addEdge(az, A, this.Z);

		this.cstnu.initAndCheck();

		this.cstnu.zLabeledLetterRemovalRule(C, cz);

		CSTNUEdge CZOk = this.g.getEdgeFactory().get("CZ");
		// remember that CZ contains also (0,emptyLabel)
		CZOk.mergeLabeledValue(Label.emptyLabel, 0);
		CZOk.mergeLabeledValue(Label.parse("a"), -3);
		CZOk.mergeLabeledValue(Label.parse("¬b"), -3);
		CZOk.mergeLabeledValue(Label.parse("ab"), -4);
		CZOk.mergeLabeledValue(Label.parse("¬ab"), -13);
		CZOk.mergeLabeledValue(Label.parse("¿ab"), -14);

		assertEquals("Upper Case values:", CZOk.getLabeledValueMap(), cz.getLabeledValueMap());
	}

	/**
	 * Test method for
	 * 
	 * <pre>
	 * A &lt;--- -3,D,b¬c--- C &lt;-----3,c,ab---- D
	 * </pre>
	 */
	@Test
	public final void testCrossCaseRule() {
		// System.out.printf("CROSS CASE\n");
		LabeledNode A = this.g.getNodeFactory().get("A");
		LabeledNode C = this.g.getNodeFactory().get("C");
		LabeledNode D = this.g.getNodeFactory().get("D");
		CSTNUEdge dc = this.g.getEdgeFactory().get("DC");
		CSTNUEdge ca = this.g.getEdgeFactory().get("CA");
		CSTNUEdge da = this.g.getEdgeFactory().get("DA");
		this.g.addEdge(dc, D, C);
		this.g.addEdge(ca, C, A);
		this.g.addEdge(da, D, A);
		dc.setLowerCaseValue(Label.parse("ab"), new ALabel(C.getName(), this.alpha), 3);

		ca.mergeUpperCaseValue(Label.parse("b¬c"), new ALabel(D.getName(), this.alpha), -3);
		ca.mergeUpperCaseValue(Label.parse("b¬f"), new ALabel(D.getName(), this.alpha), 3);
		ca.mergeUpperCaseValue(Label.parse("¬b"), new ALabel(D.getName(), this.alpha), -4);
		ca.mergeUpperCaseValue(Label.parse("ab"), new ALabel(C.getName(), this.alpha), -4);

		this.cstnu.labeledCrossLowerCaseRule(D, C, A, dc, ca, da);

		CSTNUEdge daOk = this.g.getEdgeFactory().get("DA");
		daOk.mergeUpperCaseValue(Label.parse("ab¬c"), new ALabel("D", this.alpha), 0);

		assertEquals("Upper Case values:", daOk.getUpperCaseValueMap(), da.getUpperCaseValueMap());
	}

	/**
	 * @throws WellDefinitionException nope
	 */
	@Test
	public final void testLabelModificationR0() throws WellDefinitionException {
		LabeledNode P = this.g.getNodeFactory().get("P", 'p');
		// LabeledNode X = this.g.getNodeFactory().get("X");
		LabeledNode Q = this.g.getNodeFactory().get("Q?", 'q');
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		LabeledNode C = this.g.getNodeFactory().get("C?", 'c');
		Q.setLabel(Label.parse("p"));
		C.setLabel(Label.parse("¬p"));
		CSTNUEdge pz = this.g.getEdgeFactory().get("PX");
		pz.mergeLabeledValue(Label.parse("abpq"), -10);// ok but p has to be removed
		pz.mergeLabeledValue(Label.parse("ab¬p"), 0);// it is removed during init by (0, ⊡)
		pz.mergeLabeledValue(Label.parse("c¬p"), 1);// verrà cancellato in fase di init!
		pz.mergeLabeledValue(Label.parse("¬c¬pa"), -1);// ok but ¬c¬p has to be removed
		pz.mergeUpperCaseValue(Label.parse("ab¬p"), new ALabel("C", this.alpha), -11);// ok
		this.g.addEdge(pz, P, this.Z);
		this.g.addVertex(Q);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(C);

		this.cstnu.initAndCheck();// from nov 2017, it doesn't clear odd values
		this.cstnu.labelModificationqR0(P, pz);

		CSTNUEdge pxOK = this.g.getEdgeFactory().get("XY");
		// if R0 is applied!
		// pxOK.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alpha), -11);
		// pxOK.mergeLabeledValue(Label.parse("ab"), -10);
		// pxOK.mergeLabeledValue(Label.parse("a"), -1);
		// pxOK.mergeLabeledValue(Label.parse("c¬p"), 1);
		// if only qR0 is applied!
		pxOK.mergeLabeledValue(Label.emptyLabel, 0);// ok
		// pxOK.mergeLabeledValue(Label.parse("abq"), -10);// ok if it is streamlined
		pxOK.mergeLabeledValue(Label.parse("ab"), -10);// ok if it is NOT streamlined
		pxOK.mergeLabeledValue(Label.parse("ab¬p"), 0); // quindi non è memorizzato
		pxOK.mergeLabeledValue(Label.parse("c¬p"), 1);// viene cancellato dallo 0
		// pxOK.mergeLabeledValue(Label.parse("a¬c"), -1);// ok if it is streamlined
		pxOK.mergeLabeledValue(Label.parse("a"), -1);// ok if it is NOT streamlined
		pxOK.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alpha), -11);

		assertEquals("R0: p?X labeled values.", pxOK.getLabeledValueMap(), pz.getLabeledValueMap());
		assertEquals("R0: px upper case labedled values.", pxOK.getUpperCaseValueMap(), pz.getUpperCaseValueMap());
	}

	/**
	 * @throws WellDefinitionException nope
	 */
	@Test
	public final void testLabelModificationQR0() throws WellDefinitionException {
		LabeledNode P = this.g.getNodeFactory().get("P?", 'p');
		LabeledNode Q = this.g.getNodeFactory().get("Q?", 'q');
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		LabeledNode C = this.g.getNodeFactory().get("C?", 'c');
		Q.setLabel(Label.parse("p"));
		// C.setLabel(Label.parse("p"));
		CSTNUEdge pz = this.g.getEdgeFactory().get("PZ");
		pz.mergeLabeledValue(Label.parse("abp¿q"), -10);
		pz.mergeLabeledValue(Label.parse("ab¬p"), -1);
		pz.mergeLabeledValue(Label.parse("¬a¬c¬p"), -2);
		pz.mergeLabeledValue(Label.parse("¬acp"), -2);
		pz.mergeLabeledValue(Label.parse("¿c¿p"), 1);
		pz.mergeUpperCaseValue(Label.parse("¬a¬p"), new ALabel("C", this.alpha), -2);
		this.g.addEdge(pz, P, this.Z);
		this.g.addVertex(Q);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(C);
		this.g.setZ(this.Z);
		this.cstnu.initAndCheck();
		this.cstnu.labelModificationqR0(P, pz);

		CSTNUEdge pxOK = this.g.getEdgeFactory().get("XY");
		pxOK.mergeLabeledValue(Label.emptyLabel, 0);
		pxOK.mergeLabeledValue(Label.parse("ab"), -1);
		// pxOK.mergeLabeledValue(Label.parse("ab¿q"), -10);// if it were streamlined
		pxOK.mergeLabeledValue(Label.parse("ab"), -10);// if it is not streamlined
		pxOK.mergeLabeledValue(Label.parse("¬a"), -2);
		// pxOK.mergeLabeledValue(Label.parse("¿c¿p"), 1);// NO!
		pxOK.mergeUpperCaseValue(Label.parse("¬a¬p"), new ALabel("C", this.alpha), -2);

		assertEquals("R0: P?Z labeled values.", pxOK.getLabeledValueMap(), pz.getLabeledValueMap());
		// 2018-12-18 Trying to make a-label simplification faster... loosing some optimization
		// asserEquals when full optimization is activated.
		assertNotEquals("R0: PZ upper case labedled values.", pxOK.getUpperCaseValueMap(), pz.getUpperCaseValueMap());
	}

	/**
	 * <pre>
	 * P ----[0, ,¬b][-10, ,ab][-11,C,ab]--&gt; X &lt;--------- Y
	 */
	@Test
	public final void testLabelModificationR3() {
		// System.out.printf("R1-R3 CASE\n");
		LabeledNode P = this.g.getNodeFactory().get("P", 'p');
		LabeledNode X = this.g.getNodeFactory().get("X");
		LabeledNode Y = this.g.getNodeFactory().get("Y");
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		LabeledNode C = this.g.getNodeFactory().get("C?", 'c');
		LabeledNode G = this.g.getNodeFactory().get("G?", 'g');
		G.setLabel(Label.parse("p"));
		this.g.addVertex(P);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(G);
		this.g.addVertex(C);
		this.g.addVertex(X);
		this.g.addVertex(Y);
		this.g.setZ(this.Z);
		CSTNUEdge gp = this.g.getEdgeFactory().get("GP");
		gp.mergeLabeledValue(Label.parse("p"), -4);
		this.g.addEdge(gp, G, P);

		CSTNUEdge px = this.g.getEdgeFactory().get("PX");
		px.mergeLabeledValue(Label.parse("¬b"), 0);
		px.mergeLabeledValue(Label.parse("ab"), -10);
		px.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alpha), -11);

		CSTNUEdge yx = this.g.getEdgeFactory().get("YX");
		yx.mergeLabeledValue(Label.parse("bgp"), -4);
		yx.mergeLabeledValue(Label.parse("cp"), -10);
		yx.mergeLabeledValue(Label.parse("c¬p"), 11);
		yx.mergeUpperCaseValue(Label.parse("bgp"), new ALabel("C", this.alpha), -7);

		this.g.addEdge(px, P, X);
		this.g.addEdge(yx, Y, X);

		wellDefinition(this.g);

		this.cstnu.labelModificationqR3(Y, yx);

		// <YX, normal, Y, X, L:{(¬ABGp, -4) (ABG, -4) }, LL:{}, UL:{(¬ABG¬p,C:-4) (¬ABGp,C:-7) (ABG,C:-7) }>
		CSTNUEdge yxOK = this.g.getEdgeFactory().get("YX");
		// yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
		// yxOK.mergeLabeledValue(Label.parse("ab"), -4); from 20171017 R3 is only qR3*
		// yxOK.mergeLabeledValue(Label.parse("abc"), -10); from 20171017 R3 is only qR3*
		// yxOK.mergeLabeledValue(Label.parse("¬bc"), 0);// R5 rule. from 20171017 R3 is only qR3*
		yxOK.mergeLabeledValue(Label.parse("bgp"), -4);// original
		yxOK.mergeLabeledValue(Label.parse("c¬p"), 11);// original
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);// original

		// yxOK.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alpha), -7);from 20171017 R3 is only qR3*
		yxOK.mergeUpperCaseValue(Label.parse("bgp"), new ALabel("C", this.alpha), -7);// original

		assertEquals("R3: yx labeled values.", yxOK.getLabeledValueMap(), yx.getLabeledValueMap());
		assertEquals("R3: yx upper case labedled values.", yxOK.getUpperCaseValueMap(), yx.getUpperCaseValueMap());
	}

	/**
	 * 
	 */
	@Test
	public final void testLabelModificationR3bis() {
		this.alpha.clear();

		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode N8 = this.g.getNodeFactory().get("n8");
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(N8);
		this.g.setZ(this.Z);

		CSTNUEdge eN8Z = this.g.getEdgeFactory().get("n8_Z");
		LabeledIntMap map = AbstractLabeledIntMap.parse("{(-20, ab) (-∞, ¿ab) (-8, ¬b) (-17, b) }");
		eN8Z.setLabeledValueMap(map);
		LabeledALabelIntTreeMap map1 = LabeledALabelIntTreeMap
				.parse("{(D, -∞, ¿ab) (D, -30, ab) (D, -∞, a¿b) (D, -4, ¿b) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -19, ¬ab) (F, -∞, ¿ab) }", this.alpha);
		eN8Z.setUpperCaseValueMap(map1);
		this.g.addEdge(eN8Z, N8, this.Z);

		// {❮B?_Z; derived; {(0, ⊡) }; ❯, ❮A?_Z; derived; {(-2, ⊡) (-4, b) }; ❯}
		CSTNUEdge eAZ = this.g.getEdgeFactory().get("A?_Z");
		map = AbstractLabeledIntMap.parse("{(-2, ⊡) (-4, b) }");
		eAZ.setLabeledValueMap(map);
		eAZ.setUpperCaseValueMap(map1);
		this.g.addEdge(eAZ, A, this.Z);

		// System.out.println(eN8Z);
		this.cstnu.labelModificationqR3(N8, eN8Z);

		CSTNUEdge eN8ZOK = this.g.getEdgeFactory().get("n8_Z");
		map = AbstractLabeledIntMap.parse("{(-20, ab) (-∞, ¿ab) (-8, ¬b) (-17, b) }");
		eN8ZOK.setLabeledValueMap(map);
		map1 = LabeledALabelIntTreeMap
				.parse("{(D, -∞, ¿ab) (D, -30, ab) (D, -∞, a¿b) (D, -4, ¿b) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -19, ¬ab) (F, -∞, ¿ab) }", this.alpha);
		eN8ZOK.setUpperCaseValueMap(map1);

		assertEquals("R3: eN8Z labeled values.", eN8ZOK.getLabeledValueMap(), eN8Z.getLabeledValueMap());
		assertEquals("R3: eN8ZOK upper case labedled values.", eN8ZOK.getUpperCaseValueMap(), eN8Z.getUpperCaseValueMap());

		this.cstnu.labelModificationqR3(N8, eN8Z);

		assertEquals("R3: eN8Z labeled values.", eN8ZOK.getLabeledValueMap(), eN8Z.getLabeledValueMap());
		assertEquals("R3: eN8ZOK upper case labedled values.", eN8ZOK.getUpperCaseValueMap(), eN8Z.getUpperCaseValueMap());

	}

	/**
	 * <pre>
	 * P ----[0, ,¬b][-10, ,ab][-11,C,ab]--&gt; Z &lt;--------- Y
	 */
	@Test
	public final void testLabelModificationQR3() {
		// System.out.printf("R1-R3 CASE\n");
		LabeledNode P = this.g.getNodeFactory().get("P", 'p');
		LabeledNode Y = this.g.getNodeFactory().get("Y");
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		LabeledNode C = this.g.getNodeFactory().get("C?", 'c');
		LabeledNode G = this.g.getNodeFactory().get("G?", 'g');
		G.setLabel(Label.parse("p"));
		this.g.addVertex(P);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(G);
		this.g.addVertex(C);
		this.g.addVertex(Y);
		this.g.setZ(this.Z);
		CSTNUEdge gp = this.g.getEdgeFactory().get("GP");
		gp.mergeLabeledValue(Label.parse("p"), -4);
		this.g.addEdge(gp, G, P);

		CSTNUEdge pz = this.g.getEdgeFactory().get("PZ");
		pz.mergeLabeledValue(Label.parse("¬b"), 0);
		pz.mergeLabeledValue(Label.parse("ab"), -10);
		pz.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alpha), -11);

		CSTNUEdge yz = this.g.getEdgeFactory().get("YZ");
		yz.mergeLabeledValue(Label.parse("bg¿p"), -4);
		yz.mergeLabeledValue(Label.parse("cp"), -10);
		yz.mergeLabeledValue(Label.parse("¿cp"), -11);
		yz.mergeUpperCaseValue(Label.parse("bgp"), new ALabel("C", this.alpha), -7);

		this.g.addEdge(pz, P, this.Z);
		this.g.addEdge(yz, Y, this.Z);

		wellDefinition(this.g);

		this.cstnu.labelModificationqR3(Y, yz);

		// <YX, normal, Y, X, L:{(¬ABGp, -4) (ABG, -4) }, LL:{}, UL:{(¬ABG¬p,C:-4) (¬ABGp,C:-7) (ABG,C:-7) }>
		CSTNUEdge yxOK = this.g.getEdgeFactory().get("YX");
		// yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
		// yxOK.mergeUpperCaseValue(Label.parse("abg"), new ALabel("C", this.alpha), -7);// if streamlined
		yxOK.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alpha), -7);// if not streamlined
		yxOK.mergeUpperCaseValue(Label.parse("abc"), new ALabel("C", this.alpha), -10);// it could not be present because there is the labeled value
																						// (-10,abc)... it depends in which
		// order it is inserted.
		yxOK.mergeUpperCaseValue(Label.parse("bgp"), new ALabel("C", this.alpha), -7);//
		yxOK.mergeUpperCaseValue(Label.parse("ab¿c"), new ALabel("C", this.alpha), -11);//

		yxOK.mergeLabeledValue(Label.parse("ab"), -4);// if not streamlined
		// yxOK.mergeLabeledValue(Label.parse("abg"), -4);// if streamlined
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);
		yxOK.mergeLabeledValue(Label.parse("¿cp"), -11);
		yxOK.mergeLabeledValue(Label.parse("abc"), -10);
		yxOK.mergeLabeledValue(Label.parse("bg¿p"), -4);

		assertEquals("R3: yx labeled values.", yxOK.getLabeledValueMap(), yz.getLabeledValueMap());
		assertEquals("R3: yx upper case labedled values.", yxOK.getUpperCaseValueMap(), yz.getUpperCaseValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.attic.CSTNU_NodeSet#lowerCaseRule(TNGraph, TNGraph, CSTNUCheckStatus)}.
	 * 
	 * <pre>
	 * A &lt;----[1,,b][0,,c][-11,,d]--- C &lt;---[3,c,ab]--- D
	 * </pre>
	 * 
	 * @SuppressWarnings("javadoc")
	 * 
	 * @Test
	 *       public final void testLowerCaseRule() {
	 *       // System.out.printf("LOWER CASE\n");
	 *       TNGraph g = new TNGraph(this.LABELED_VALUE_MAP_IMPL_CLASS);
	 *       LabeledIntEdgePlugga ble dc = this.g.getEdgeFactory().get("DC");
	 *       CSTNUEdge ca = this.g.getEdgeFactory().get("CA");
	 *       CSTNUEdge da = this.g.getEdgeFactory().get("DA");
	 *       dc.setLowerCaseValue(Label.parse("ab"), new ALabel("c", this.alpha), 3);
	 *       ca.mergeLabeledValue(Label.parse("b"), 1);
	 *       ca.mergeLabeledValue(Label.parse("c"), 0);
	 *       ca.mergeLabeledValue(Label.parse("d"), -11);
	 *       LabeledNode A = this.g.getNodeFactory().get("A");
	 *       LabeledNode C = this.g.getNodeFactory().get("C");
	 *       LabeledNode D = this.g.getNodeFactory().get("D");
	 *       LabeledNode OA = this.g.getNodeFactory().get("A?", 'a');
	 *       LabeledNode OB = this.g.getNodeFactory().get("B?", 'b');
	 *       LabeledNode OC = this.g.getNodeFactory().get("C?", 'c');
	 *       LabeledNode OD = this.g.getNodeFactory().get("D?", 'd');
	 *       g.addVertex(OA);
	 *       g.addVertex(OB);
	 *       g.addVertex(OC);
	 *       g.addVertex(OD);
	 *       g.addEdge(dc, D, C);
	 *       g.addEdge(ca, C, A);
	 *       g.addEdge(da, D, A);
	 *       g.setZ(this.Z);
	 *       wellDefinition(g);
	 *       this.cstnu.labeledLowerCaseRule(D, C, A, this.Z, dc, ca, da);
	 *       CSTNUEdge daOk = this.g.getEdgeFactory().get("DA");
	 *       daOk.mergeLabeledValue(Label.parse("abc"), 3);
	 *       daOk.mergeLabeledValue(Label.parse("abd"), -8);
	 *       assertEquals("Lower Case values:", daOk.getLabeledValueMap(), da.getLabeledValueMap());
	 *       // System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
	 *       // System.out.printf("G: %s\n", g1);
	 *       }
	 */

	/**
	 * Test method for {@link it.univr.di.attic.CSTNU_NodeSet#lowerCaseRule(TNGraph, TNGraph, CSTNUCheckStatus)}.
	 * 
	 * <pre>
	 * Z &lt;----[1,,b][0,,c][-11,,¬b]--- C &lt;---[3,c,ab]--- D
	 * </pre>
	 * 
	 * @SuppressWarnings("javadoc")
	 * 
	 * @Test
	 *       public final void testQLowerCaseRule() {
	 *       // System.out.printf("LOWER CASE\n");
	 *       TNGraph g = new TNGraph(this.LABELED_VALUE_MAP_IMPL_CLASS);
	 *       CSTNUEdge dc = this.g.getEdgeFactory().get("DC");
	 *       CSTNUEdge ca = this.g.getEdgeFactory().get("CA");
	 *       CSTNUEdge da = this.g.getEdgeFactory().get("DA");
	 *       dc.setLowerCaseValue(Label.parse("ab"), new ALabel("c", this.alpha), 3);
	 *       ca.mergeLabeledValue(Label.parse("b"), 1);
	 *       ca.mergeLabeledValue(Label.parse("c"), 0);
	 *       ca.mergeLabeledValue(Label.parse("¬b"), -11);
	 *       LabeledNode A = this.g.getNodeFactory().get("A");
	 *       LabeledNode C = this.g.getNodeFactory().get("C");
	 *       LabeledNode D = this.g.getNodeFactory().get("D");
	 *       LabeledNode OA = this.g.getNodeFactory().get("A?", 'a');
	 *       LabeledNode OB = this.g.getNodeFactory().get("B?", 'b');
	 *       LabeledNode OC = this.g.getNodeFactory().get("C?", 'c');
	 *       LabeledNode OD = this.g.getNodeFactory().get("D?", 'd');
	 *       g.addVertex(OA);
	 *       g.addVertex(OB);
	 *       g.addVertex(OC);
	 *       g.addVertex(OD);
	 *       g.addEdge(dc, D, C);
	 *       g.addEdge(ca, C, A);
	 *       g.addEdge(da, D, A);
	 *       g.setZ(A);
	 *       wellDefinition(g);
	 *       this.cstnu.labeledLowerCaseRule(D, C, A, A, dc, ca, da);
	 *       CSTNUEdge daOk = this.g.getEdgeFactory().get("DA");
	 *       daOk.mergeLabeledValue(Label.parse("abc"), 3);
	 *       daOk.mergeLabeledValue(Label.parse("a¿b"), -8);
	 *       assertEquals("Lower Case values:", daOk.getLabeledValueMap(), da.getLabeledValueMap());
	 *       // System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
	 *       // System.out.printf("G: %s\n", g1);
	 *       }
	 */

	/**
	 * A &lt;---[3,B,ab]--- C &lt;----[-13,,b][11,,c]----D
	 */
	@Test
	public final void testUpperCaseRule() {
		// System.out.printf("UPPER CASE\n");
		CSTNUEdge dc = this.g.getEdgeFactory().get("dc");
		CSTNUEdge ca = this.g.getEdgeFactory().get("ca");
		CSTNUEdge da = this.g.getEdgeFactory().get("da");
		ca.mergeUpperCaseValue(Label.parse("ab"), new ALabel("B", this.alpha), 3);
		dc.mergeLabeledValue(Label.parse("b"), -13);
		dc.mergeLabeledValue(Label.parse("c"), 11);
		LabeledNode C = this.g.getNodeFactory().get("C"), A = this.g.getNodeFactory().get("A"), D = this.g.getNodeFactory().get("D");
		this.g.addEdge(dc, D, C);
		this.g.addEdge(ca, C, A);
		this.g.addEdge(da, D, A);
		this.cstnu.labelPropagation(D, C, A, dc, ca, da);

		CSTNUEdge daOk = this.g.getEdgeFactory().get("DA");
		daOk.mergeUpperCaseValue(Label.parse("abc"), new ALabel("B", this.alpha), 14);
		daOk.mergeUpperCaseValue(Label.parse("ab"), new ALabel("B", this.alpha), -10);

		assertEquals("Upper Case values:", daOk.getUpperCaseValueMap(), da.getUpperCaseValueMap());
	}

	/**
	 */
	@Test
	public final void test_lncRule() {
		LabeledNode W = this.g.getNodeFactory().get("W", 'w');
		LabeledNode X = this.g.getNodeFactory().get("X");
		LabeledNode Y = this.g.getNodeFactory().get("Y");
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		W.setLabel(Label.parse("a"));
		this.g.addVertex(W);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(X);
		this.g.addVertex(Y);
		this.g.setZ(this.Z);

		CSTNUEdge XY = this.g.getEdgeFactory().get("XY");
		XY.mergeLabeledValue(Label.emptyLabel, 2);
		XY.mergeLabeledValue(Label.parse("¬a"), -2);
		XY.mergeLabeledValue(Label.parse("b"), 1);
		XY.mergeLabeledValue(Label.parse("a¬b"), -1);
		// System.out.println(XY);

		CSTNUEdge YW = this.g.getEdgeFactory().get("YW");
		YW.mergeLabeledValue(Label.parse("a"), 2);
		YW.mergeLabeledValue(Label.parse("ab"), 1);
		YW.mergeLabeledValue(Label.parse("a¬b"), -1);
		// System.out.println(YW);

		CSTNUEdge XW = this.g.getEdgeFactory().get("XW");

		this.g.addEdge(XY, X, Y);
		this.g.addEdge(YW, Y, W);
		this.g.addEdge(XW, X, W);

		wellDefinition(this.g);
		// System.out.println(XW);

		this.cstnu.labelPropagation(X, Y, W, XY, YW, XW);
		// System.out.println(XW);

		CSTNUEdge xwOK = this.g.getEdgeFactory().get("XW");
		// xwOK.mergeLabeledValue(Label.parse("a"), 4);if no positive edge are propagated
		xwOK.mergeLabeledValue(Label.parse("a¬b"), -2);// if not streamlined
		xwOK.mergeLabeledValue(Label.parse("ab"), 2);//
		// xwOK.mergeLabeledValue(Label.parse("¿a"), 0); From 20171010 unknown literal are not more propagated
		// xwOK.mergeLabeledValue(Label.parse("¿a¬b"), -3);
		// xwOK.mergeLabeledValue(Label.parse("¿ab"), -1);

		assertEquals("No case: XW labeled values.", xwOK.getLabeledValueMap(), XW.getLabeledValueMap());
	}

	/**
	 * Test method for
	 */
	@Test
	public final void test_lucRule() {
		LabeledNode W = this.g.getNodeFactory().get("W", 'w');
		LabeledNode X = this.g.getNodeFactory().get("X");
		LabeledNode Y = this.g.getNodeFactory().get("Y");
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		W.setLabel(Label.parse("a"));
		this.g.addVertex(W);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(X);
		this.g.addVertex(Y);
		this.g.setZ(this.Z);

		ALabel aLabel = new ALabel(A.getName(), this.alpha);
		ALabel bLabel = new ALabel(B.getName(), this.alpha);

		CSTNUEdge XY = this.g.getEdgeFactory().get("XY");
		XY.mergeLabeledValue(Label.emptyLabel, 2);
		XY.mergeLabeledValue(Label.parse("¬a"), 1);
		XY.mergeLabeledValue(Label.parse("b"), 1);
		XY.mergeLabeledValue(Label.parse("a¬b"), -1);
		// System.out.println(XY);

		CSTNUEdge YW = this.g.getEdgeFactory().get("YW");
		YW.mergeLabeledValue(Label.parse("a"), -1);
		YW.mergeLabeledValue(Label.parse("a¬b"), -2);
		YW.mergeUpperCaseValue(Label.parse("a"), aLabel, -2);
		YW.mergeUpperCaseValue(Label.parse("ab"), aLabel, -3);
		YW.mergeUpperCaseValue(Label.parse("a¬b"), aLabel.conjunction(bLabel), -4);
		// System.out.println(YW);

		CSTNUEdge XW = this.g.getEdgeFactory().get("XW");

		this.g.addEdge(XY, X, Y);
		this.g.addEdge(YW, Y, W);
		this.g.addEdge(XW, X, W);

		wellDefinition(this.g);
		// System.out.println(XW);

		this.cstnu.labelPropagation(X, Y, W, XY, YW, XW);
		// System.out.println(XW);

		CSTNUEdge xwOK = this.g.getEdgeFactory().get("XW");
		xwOK.mergeLabeledValue(Label.parse("a"), 0);
		// xwOK.mergeLabeledValue(Label.parse("¿a¬b"), -3);From 20171010 unknown literal are not more propagated
		xwOK.mergeLabeledValue(Label.parse("ab"), 2);
		xwOK.mergeLabeledValue(Label.parse("a¬b"), -3);
		xwOK.mergeUpperCaseValue(Label.parse("a"), aLabel, 0);// not stored because is 0
		xwOK.mergeUpperCaseValue(Label.parse("ab"), aLabel, -2);
		// xwOK.mergeUpperCaseValue(Label.parse("a¿b"), aLabel, -4);
		// xwOK.mergeUpperCaseValue(Label.parse("a¬b"), aLabel.conjunction(bLabel), -5);//not allowed because rule does not back propagate conjuncted UC from
		// node != Z

		// assertEquals("No case: XW ", xwOK.toString(), XW.toString());

		assertEquals("No case: XW labeled values.", xwOK.getLabeledValueMap(), XW.getLabeledValueMap());
		// 2018-12-18 Trying to make a-label simplification faster... loosing some optimization
		// asserEquals when full optimization is activated.
		assertNotEquals("No case: XW upper case labedled values.", xwOK.getUpperCaseValueMap(), XW.getUpperCaseValueMap());
	}

	/**
	 * Test method for
	 */
	@Test
	public final void test_flucRule() {
		LabeledNode X = this.g.getNodeFactory().get("X");
		LabeledNode Y = this.g.getNodeFactory().get("Y");
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(X);
		this.g.addVertex(Y);
		this.g.setZ(this.Z);

		ALabel aLabel = new ALabel(A.getName(), this.alpha);
		ALabel bLabel = new ALabel(B.getName(), this.alpha);

		CSTNUEdge XY = this.g.getEdgeFactory().get("XY");
		XY.mergeUpperCaseValue(Label.emptyLabel, aLabel, 1);
		XY.mergeUpperCaseValue(Label.parse("¬a"), aLabel, -3);
		XY.mergeUpperCaseValue(Label.parse("b"), aLabel, -4);
		XY.mergeUpperCaseValue(Label.parse("a¬b"), aLabel.conjunction(bLabel), -2);
		// System.out.println(XY);

		CSTNUEdge YZ = this.g.getEdgeFactory().get("YZ");
		YZ.mergeLabeledValue(Label.parse("a"), 2);
		YZ.mergeLabeledValue(Label.parse("ab"), 1);
		YZ.mergeLabeledValue(Label.parse("a¬b"), -1);

		CSTNUEdge XZ = this.g.getEdgeFactory().get("XZ");

		this.g.addEdge(XY, X, Y);
		this.g.addEdge(YZ, Y, this.Z);
		this.g.addEdge(XZ, X, this.Z);

		wellDefinition(this.g);
		// System.out.println(XW);

		this.cstnu.labelPropagation(X, Y, this.Z, XY, YZ, XZ);
		// System.out.println(XW);

		CSTNUEdge xzOK = this.g.getEdgeFactory().get("XW");
		// FLUC and LCUC change upper case values!
		// xzOK.mergeUpperCaseValue(Label.parse("¿a"), aLabel, -1); From 20171010 unknown literal are not more propagated
		// xzOK.mergeUpperCaseValue(Label.parse("¿a¬b"), aLabel, -4);
		// xzOK.mergeUpperCaseValue(Label.parse("ab"), aLabel, -3);// X<>A?, so rule cannot be applied
		// xzOK.mergeUpperCaseValue(Label.parse("a¿b"), aLabel, -5);

		assertEquals("No case: XZ labeled values.", xzOK.getLabeledValueMap(), XZ.getLabeledValueMap());
		assertEquals("No case: XZ upper case labedled values.", xzOK.getUpperCaseValueMap(), XZ.getUpperCaseValueMap());
	}

	/**
	 * Test method for
	 */
	@Test
	public final void test_lcucRule() {
		LabeledNode X = this.g.getNodeFactory().get("X");
		LabeledNode Y = this.g.getNodeFactory().get("Y");
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(X);
		this.g.addVertex(Y);
		this.g.setZ(this.Z);

		ALabel aLabel = new ALabel(A.getName(), this.alpha);
		ALabel bLabel = new ALabel(B.getName(), this.alpha);

		CSTNUEdge XY = this.g.getEdgeFactory().get("XY");
		XY.mergeUpperCaseValue(Label.emptyLabel, aLabel, 1);
		XY.mergeUpperCaseValue(Label.parse("¬a"), aLabel, -3);
		XY.mergeUpperCaseValue(Label.parse("b"), aLabel, -4);
		XY.mergeUpperCaseValue(Label.parse("a¬b"), aLabel.conjunction(bLabel), -2);
		// System.out.println(XY);

		CSTNUEdge YZ = this.g.getEdgeFactory().get("YZ");
		YZ.mergeUpperCaseValue(Label.parse("a"), aLabel, 1);
		YZ.mergeUpperCaseValue(Label.parse("ab"), aLabel, 0);
		YZ.mergeUpperCaseValue(Label.parse("a¬b"), aLabel.conjunction(bLabel), -2);

		CSTNUEdge XZ = this.g.getEdgeFactory().get("XZ");

		this.g.addEdge(XY, X, Y);
		this.g.addEdge(YZ, Y, this.Z);
		this.g.addEdge(XZ, X, this.Z);

		wellDefinition(this.g);
		// System.out.println(XW);

		this.cstnu.labelPropagation(X, Y, this.Z, XY, YZ, XZ);
		// System.out.println(XW);

		CSTNUEdge xzOK = this.g.getEdgeFactory().get("XZ");
		// <{¿a=A?->-2, ab=A?->-4, ¿a¬b=A?∙B?->-5, a¿b=A?∙B?->-6}>
		// xwOK.mergeUpperCaseValue(Label.parse("¿a"), aLabel, -2);From 20171010 unknown literal are not more propagated
		// xwOK.mergeUpperCaseValue(Label.parse("ab"), aLabel, -4);
		// xwOK.mergeUpperCaseValue(Label.parse("¿a¬b"), aLabel.conjunction(bLabel), -5);
		// xwOK.mergeUpperCaseValue(Label.parse("a¿b"), aLabel.conjunction(bLabel), -6);

		assertEquals("No case: XZ labeled values.", xzOK.getLabeledValueMap(), XZ.getLabeledValueMap());
		assertEquals("No case: XZ upper case labedled values.", xzOK.getUpperCaseValueMap(), XZ.getUpperCaseValueMap());
	}

	/**
	 * @throws WellDefinitionException nope
	 */
	@Test
	public final void test_llcRule() throws WellDefinitionException {
		LabeledNode A = this.g.getNodeFactory().get("A");
		LabeledNode C = this.g.getNodeFactory().get("C");
		LabeledNode D = this.g.getNodeFactory().get("D");
		LabeledNode a = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode b = this.g.getNodeFactory().get("B?", 'b');

		this.g.addVertex(A);
		this.g.addVertex(a);
		this.g.addVertex(b);
		this.g.addVertex(C);
		this.g.addVertex(D);
		this.g.setZ(this.Z);

		ALabel CaLabel = new ALabel(C.getName(), this.alpha);

		CSTNUEdge CA = this.g.getEdgeFactory().get("CA");
		CA.mergeUpperCaseValue(Label.parse("a"), CaLabel, -6);
		CA.setConstraintType(ConstraintType.contingent);
		CSTNUEdge AC = this.g.getEdgeFactory().get("AC");
		AC.setLowerCaseValue(Label.parse("a"), CaLabel, 3);
		AC.setConstraintType(ConstraintType.contingent);
		CSTNUEdge CD = this.g.getEdgeFactory().get("CD");
		CD.mergeLabeledValue(Label.parse("b"), 0);

		CSTNUEdge DA = this.g.getEdgeFactory().get("DA");
		DA.mergeLabeledValue(Label.parse("b"), -4);

		this.g.addEdge(AC, A, C);
		this.g.addEdge(CA, C, A);
		this.g.addEdge(CD, C, D);
		this.g.addEdge(DA, D, A);

		this.cstnu.setG(this.g);
		this.cstnu.initAndCheck();

		CSTNUCheckStatus status = this.cstnu.dynamicControllabilityCheck();

		assertFalse(status.consistency);
	}

}
