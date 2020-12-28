package it.univr.di.cstnu.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import it.univr.di.cstnu.algorithms.CSTNU.CSTNUCheckStatus;
import it.univr.di.cstnu.graph.CSTNPSUEdge;
import it.univr.di.cstnu.graph.CSTNPSUEdgePluggable;
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
public class CSTNPSUTest {

	/**
	 * Default implementation class for CSTNEdge
	 */
	static final Class<? extends CSTNPSUEdge> EDGE_IMPL_CLASS = CSTNPSUEdgePluggable.class;

	/**
	 * 
	 */
	CSTNPSU cstnpsu;

	/**
	 * 
	 */
	LabeledNode Z;

	/**
	 * 
	 */
	ALabelAlphabet alphabeth;

	/**
	 * 
	 */
	TNGraph<CSTNPSUEdge> g;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.alphabeth = new ALabelAlphabet();
		this.g = new TNGraph<>(EDGE_IMPL_CLASS, this.alphabeth);
		this.Z = this.g.getNodeFactory().get("Z");
		this.g.setZ(this.Z);
		this.cstnpsu = new CSTNPSU(this.g);
	}

	/**
	 * @param g1
	 */
	private final void wellDefinition(TNGraph<CSTNPSUEdge> g1) {
		this.cstnpsu.setG(g1);
		try {
			this.cstnpsu.checkWellDefinitionProperties();
		} catch (WellDefinitionException e) {
			fail("TNGraph not well formed: " + e.getMessage());
		}
	}

	/**
	 * @throws WellDefinitionException
	 */
	@Test
	public final void testCaseLabelRemovalRule() throws WellDefinitionException {
		final LabeledNode A = this.g.getNodeFactory().get("A");
		final LabeledNode B = this.g.getNodeFactory().get("B");
		ALabel aLabel4B = new ALabel(B.getName(), this.alphabeth);
		B.setALabel(aLabel4B);
		final LabeledNode C = this.g.getNodeFactory().get("C");

		final LabeledNode a = this.g.getNodeFactory().get("A?");
		a.setObservable('a');
		this.g.addVertex(a);
		final LabeledNode b = this.g.getNodeFactory().get("B?");
		b.setObservable('b');
		this.g.addVertex(b);

		final CSTNPSUEdge AB = this.g.getEdgeFactory().get("AB");
		AB.mergeLowerCaseValue(Label.emptyLabel, aLabel4B, 13);
		AB.mergeLabeledValue(Label.emptyLabel, 30);
		AB.setConstraintType(ConstraintType.contingent);

		final CSTNPSUEdge BA = this.g.getEdgeFactory().get("BA");
		BA.setConstraintType(ConstraintType.contingent);
		BA.mergeLabeledValue(Label.emptyLabel, -2);
		BA.mergeUpperCaseValue(Label.emptyLabel, aLabel4B, -20);

		final CSTNPSUEdge cz = this.g.getEdgeFactory().get("CZ");
		cz.mergeUpperCaseValue(Label.parse("ab"), aLabel4B, -4);
		cz.mergeUpperCaseValue(Label.parse("a"), aLabel4B, -3);
		cz.mergeUpperCaseValue(Label.parse("¬b"), aLabel4B, -3);
		cz.mergeUpperCaseValue(Label.parse("¬ab"), aLabel4B, -15);

		final CSTNPSUEdge az = this.g.getEdgeFactory().get("AZ");
		az.mergeLabeledValue(Label.parse("ab"), -1);

		this.g.addEdge(AB, A, B);
		this.g.addEdge(BA, B, A);
		this.g.addEdge(cz, C, this.Z);
		this.g.addEdge(az, A, this.Z);

		this.cstnpsu.initAndCheck();

		this.cstnpsu.rG4(C, cz);

		CSTNPSUEdge CZOk = this.g.getEdgeFactory().get("CZ");
		// remember that CZ contains also (0,emptyLabel)
		CZOk.mergeLabeledValue(Label.emptyLabel, -2);
		CZOk.mergeLabeledValue(Label.parse("ab"), -3);
		CZOk.mergeUpperCaseValue(Label.parse("ab"), aLabel4B, -4);
		CZOk.mergeUpperCaseValue(Label.parse("a"), aLabel4B, -3);
		CZOk.mergeUpperCaseValue(Label.parse("¬b"), aLabel4B, -3);
		CZOk.mergeUpperCaseValue(Label.parse("¬ab"), aLabel4B, -15);

		assertEquals("Upper Case values:", CZOk.toString(), cz.toString());
	}

	/**
	 * Test method for
	 * {@link it.univr.di.attic.CSTPSU_NodeSet#crossCaseRule(it.univr.di.cstnu.graph.TNGraph, it.univr.di.cstnu.graph.TNGraph, CSTPSUCheckStatus)}
	 * 
	 * <pre>
	 * Z &lt;--- -3,D,b¬c--- C &lt;-----3,c,ab---- A
	 * </pre>
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testCrossCaseRule() {
		// System.out.printf("CROSS CASE\n");
		LabeledNode C = this.g.getNodeFactory().get("C");
		LabeledNode A = this.g.getNodeFactory().get("A");
		LabeledNode D = this.g.getNodeFactory().get("D");
		CSTNPSUEdge ac = this.g.getEdgeFactory().get("AC");
		ac.setConstraintType(ConstraintType.contingent);
		CSTNPSUEdge ca = this.g.getEdgeFactory().get("CA");
		ca.setConstraintType(ConstraintType.contingent);
		CSTNPSUEdge cz = this.g.getEdgeFactory().get("CZ");
		CSTNPSUEdge az = this.g.getEdgeFactory().get("AZ");
		this.g.addEdge(ac, A, C);
		this.g.addEdge(ca, C, A);
		this.g.addEdge(cz, C, this.Z);
		this.g.addEdge(az, A, this.Z);
		this.g.addVertex(D);
		ac.mergeLowerCaseValue(Label.parse("ab"), new ALabel(C.getName(), this.alphabeth), 3);
		ca.mergeUpperCaseValue(Label.parse("ab"), new ALabel(C.getName(), this.alphabeth), -10);

		cz.mergeUpperCaseValue(Label.parse("b¬c"), new ALabel(D.getName(), this.alphabeth), -4);
		cz.mergeUpperCaseValue(Label.parse("b¬f"), new ALabel(D.getName(), this.alphabeth), 3);
		cz.mergeUpperCaseValue(Label.parse("¬b"), new ALabel(D.getName(), this.alphabeth), -4);
		cz.mergeUpperCaseValue(Label.parse("ab"), new ALabel(C.getName(), this.alphabeth), -4);

		try {
			this.cstnpsu.initAndCheck();
		} catch (WellDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.cstnpsu.rG2(A, C, this.Z, ac, cz, az);

		CSTNPSUEdge daOk = this.g.getEdgeFactory().get("AZ");
		daOk.mergeUpperCaseValue(Label.parse("ab¬c"), new ALabel(D.getName(), this.alphabeth), -1);
		daOk.mergeUpperCaseValue(Label.parse("a¿b"), new ALabel(D.getName(), this.alphabeth), -1);

		assertEquals("Upper Case values:", daOk.getUpperCaseValueMap(), az.getUpperCaseValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTPSU_NodeSet#labelModificationR0R2R4(TNGraph, boolean))}.
	 * 
	 * @throws WellDefinitionException
	 */
	@SuppressWarnings("javadoc")
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
		CSTNPSUEdge pz = this.g.getEdgeFactory().get("PX");
		pz.mergeLabeledValue(Label.parse("abpq"), -10);// ok but p has to be removed
		pz.mergeLabeledValue(Label.parse("ab¬p"), 0);// it is removed during init by (0, ⊡)
		pz.mergeLabeledValue(Label.parse("c¬p"), 1);// verrà cancellato in fase di init!
		pz.mergeLabeledValue(Label.parse("¬c¬pa"), -1);// ok but ¬c¬p has to be removed
		pz.mergeUpperCaseValue(Label.parse("ab¬p"), new ALabel("C", this.alphabeth), -11);// ok
		this.g.addEdge(pz, P, this.Z);
		this.g.addVertex(Q);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(C);

		this.cstnpsu.initAndCheck();// from nov 2017, it doesn't clear odd values
		this.cstnpsu.rM1(P, pz);

		CSTNPSUEdge pxOK = this.g.getEdgeFactory().get("XY");
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
		pxOK.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alphabeth), -11);

		assertEquals("R0: p?X labeled values.", pxOK.getLabeledValueMap(), pz.getLabeledValueMap());
		assertEquals("R0: px upper case labedled values.", pxOK.getUpperCaseValueMap(), pz.getUpperCaseValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTPSU_NodeSet#labelModificationR0R2R4(TNGraph, boolean))}.
	 * 
	 * @throws WellDefinitionException
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testLabelModificationQR0() throws WellDefinitionException {
		LabeledNode P = this.g.getNodeFactory().get("P?", 'p');
		LabeledNode Q = this.g.getNodeFactory().get("Q?", 'q');
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		LabeledNode C = this.g.getNodeFactory().get("C?", 'c');
		Q.setLabel(Label.parse("p"));
		// C.setLabel(Label.parse("p"));
		CSTNPSUEdge pz = this.g.getEdgeFactory().get("PZ");
		pz.mergeLabeledValue(Label.parse("abp¿q"), -10);
		pz.mergeLabeledValue(Label.parse("ab¬p"), -1);
		pz.mergeLabeledValue(Label.parse("¬a¬c¬p"), -2);
		pz.mergeLabeledValue(Label.parse("¬acp"), -2);
		pz.mergeLabeledValue(Label.parse("¿c¿p"), 1);
		pz.mergeUpperCaseValue(Label.parse("¬a¬p"), new ALabel("C", this.alphabeth), -2);
		this.g.addEdge(pz, P, this.Z);
		this.g.addVertex(Q);
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(C);
		this.g.setZ(this.Z);
		this.cstnpsu.initAndCheck();
		this.cstnpsu.rM1(P, pz);

		CSTNPSUEdge pxOK = this.g.getEdgeFactory().get("XY");
		pxOK.mergeLabeledValue(Label.emptyLabel, 0);
		pxOK.mergeLabeledValue(Label.parse("ab"), -1);
		// pxOK.mergeLabeledValue(Label.parse("ab¿q"), -10);// if it were streamlined
		pxOK.mergeLabeledValue(Label.parse("ab"), -10);// if it is not streamlined
		pxOK.mergeLabeledValue(Label.parse("¬a"), -2);
		// pxOK.mergeLabeledValue(Label.parse("¿c¿p"), 1);// NO!
		pxOK.mergeUpperCaseValue(Label.parse("¬a¬p"), new ALabel("C", this.alphabeth), -2);

		assertEquals("R0: P?Z labeled values.", pxOK.getLabeledValueMap(), pz.getLabeledValueMap());
		// 2018-12-18 Trying to make a-label simplification faster... loosing some optimization
		// asserEquals when full optimization is activated.
		assertNotEquals("R0: PZ upper case labedled values.", pxOK.getUpperCaseValueMap(), pz.getUpperCaseValueMap());
	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTPSU_NodeSet#labelModificationR1R3R5(TNGraph, TNGraph, CSTPSUCheckStatus)))}.
	 * 
	 * <pre>
	 * P ----[0, ,¬b][-10, ,ab][-11,C,ab]--&gt; X &lt;--------- Y
	 */
	@SuppressWarnings({ "javadoc" })
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
		CSTNPSUEdge gp = this.g.getEdgeFactory().get("GP");
		gp.mergeLabeledValue(Label.parse("p"), -4);
		this.g.addEdge(gp, G, P);

		CSTNPSUEdge px = this.g.getEdgeFactory().get("PX");
		px.mergeLabeledValue(Label.parse("¬b"), 0);
		px.mergeLabeledValue(Label.parse("ab"), -10);
		px.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alphabeth), -11);

		CSTNPSUEdge yx = this.g.getEdgeFactory().get("YX");
		yx.mergeLabeledValue(Label.parse("bgp"), -4);
		yx.mergeLabeledValue(Label.parse("cp"), -10);
		yx.mergeLabeledValue(Label.parse("c¬p"), 11);
		yx.mergeUpperCaseValue(Label.parse("bgp"), new ALabel("C", this.alphabeth), -7);

		this.g.addEdge(px, P, X);
		this.g.addEdge(yx, Y, X);

		wellDefinition(this.g);

		this.cstnpsu.rM2(Y, yx);

		// <YX, normal, Y, X, L:{(¬ABGp, -4) (ABG, -4) }, LL:{}, UL:{(¬ABG¬p,C:-4) (¬ABGp,C:-7) (ABG,C:-7) }>
		CSTNPSUEdge yxOK = this.g.getEdgeFactory().get("YX");
		// yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
		// yxOK.mergeLabeledValue(Label.parse("ab"), -4); from 20171017 R3 is only qR3*
		// yxOK.mergeLabeledValue(Label.parse("abc"), -10); from 20171017 R3 is only qR3*
		// yxOK.mergeLabeledValue(Label.parse("¬bc"), 0);// R5 rule. from 20171017 R3 is only qR3*
		yxOK.mergeLabeledValue(Label.parse("bgp"), -4);// original
		yxOK.mergeLabeledValue(Label.parse("c¬p"), 11);// original
		yxOK.mergeLabeledValue(Label.parse("cp"), -10);// original

		// yxOK.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alpha), -7);from 20171017 R3 is only qR3*
		yxOK.mergeUpperCaseValue(Label.parse("bgp"), new ALabel("C", this.alphabeth), -7);// original

		assertEquals("R3: yx labeled values.", yxOK.getLabeledValueMap(), yx.getLabeledValueMap());
		assertEquals("R3: yx upper case labedled values.", yxOK.getUpperCaseValueMap(), yx.getUpperCaseValueMap());
	}

	/**
	 * 
	 */
	@Test
	public final void testLabelModificationR3bis() {
		this.alphabeth.clear();

		LabeledNode B = this.g.getNodeFactory().get("B?", 'b');
		LabeledNode A = this.g.getNodeFactory().get("A?", 'a');
		LabeledNode N8 = this.g.getNodeFactory().get("n8");
		this.g.addVertex(A);
		this.g.addVertex(B);
		this.g.addVertex(N8);
		this.g.setZ(this.Z);

		CSTNPSUEdge eN8Z = this.g.getEdgeFactory().get("n8_Z");
		LabeledIntMap map = AbstractLabeledIntMap.parse("{(-20, ab) (-∞, ¿ab) (-8, ¬b) (-17, b) }");
		eN8Z.setLabeledValueMap(map);
		LabeledALabelIntTreeMap map1 = LabeledALabelIntTreeMap
				.parse("{(D, -∞, ¿ab) (D, -30, ab) (D, -∞, a¿b) (D, -4, ¿b) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -19, ¬ab) (F, -∞, ¿ab) }",
						this.alphabeth);
		eN8Z.setUpperCaseValueMap(map1);
		this.g.addEdge(eN8Z, N8, this.Z);

		// {❮B?_Z; derived; {(0, ⊡) }; ❯, ❮A?_Z; derived; {(-2, ⊡) (-4, b) }; ❯}
		CSTNPSUEdge eAZ = this.g.getEdgeFactory().get("A?_Z");
		map = AbstractLabeledIntMap.parse("{(-2, ⊡) (-4, b) }");
		eAZ.setLabeledValueMap(map);
		eAZ.setUpperCaseValueMap(map1);
		this.g.addEdge(eAZ, A, this.Z);

		// System.out.println(eN8Z);
		this.cstnpsu.rM2(N8, eN8Z);

		CSTNPSUEdge eN8ZOK = this.g.getEdgeFactory().get("n8_Z");
		map = AbstractLabeledIntMap.parse("{(-20, ab) (-∞, ¿ab) (-8, ¬b) (-17, b) }");
		eN8ZOK.setLabeledValueMap(map);
		map1 = LabeledALabelIntTreeMap
				.parse("{(D, -∞, ¿ab) (D, -30, ab) (D, -∞, a¿b) (D, -4, ¿b) (I, -9, ¬b) (I, -11, ¿b) (I, -∞, ¿a¿b) (F, -19, ¬ab) (F, -∞, ¿ab) }",
						this.alphabeth);
		eN8ZOK.setUpperCaseValueMap(map1);

		assertEquals("R3: eN8Z labeled values.", eN8ZOK.getLabeledValueMap(), eN8Z.getLabeledValueMap());
		assertEquals("R3: eN8ZOK upper case labedled values.", eN8ZOK.getUpperCaseValueMap(), eN8Z.getUpperCaseValueMap());

		this.cstnpsu.rM2(N8, eN8Z);

		assertEquals("R3: eN8Z labeled values.", eN8ZOK.getLabeledValueMap(), eN8Z.getLabeledValueMap());
		assertEquals("R3: eN8ZOK upper case labedled values.", eN8ZOK.getUpperCaseValueMap(), eN8Z.getUpperCaseValueMap());

	}

	/**
	 * Test method for {@link it.univr.di.cstnu.CSTPSU_NodeSet#labelModificationR1R3R5(TNGraph, TNGraph, CSTPSUCheckStatus)))}.
	 * 
	 * <pre>
	 * P ----[0, ,¬b][-10, ,ab][-11,C,ab]--&gt; Z &lt;--------- Y
	 */
	@SuppressWarnings({ "javadoc" })
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
		CSTNPSUEdge gp = this.g.getEdgeFactory().get("GP");
		gp.mergeLabeledValue(Label.parse("p"), -4);
		this.g.addEdge(gp, G, P);

		CSTNPSUEdge pz = this.g.getEdgeFactory().get("PZ");
		pz.mergeLabeledValue(Label.parse("¬b"), 0);
		pz.mergeLabeledValue(Label.parse("ab"), -10);
		pz.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alphabeth), -11);

		CSTNPSUEdge yz = this.g.getEdgeFactory().get("YZ");
		yz.mergeLabeledValue(Label.parse("bg¿p"), -4);
		yz.mergeLabeledValue(Label.parse("cp"), -10);
		yz.mergeLabeledValue(Label.parse("¿cp"), -11);
		yz.mergeUpperCaseValue(Label.parse("bgp"), new ALabel("C", this.alphabeth), -7);

		this.g.addEdge(pz, P, this.Z);
		this.g.addEdge(yz, Y, this.Z);

		wellDefinition(this.g);

		this.cstnpsu.rM2(Y, yz);

		// <YX, normal, Y, X, L:{(¬ABGp, -4) (ABG, -4) }, LL:{}, UL:{(¬ABG¬p,C:-4) (¬ABGp,C:-7) (ABG,C:-7) }>
		CSTNPSUEdge yxOK = this.g.getEdgeFactory().get("YX");
		// yxOK.mergeLabeledValue(Label.parse("¬abgp"), -4);
		// yxOK.mergeUpperCaseValue(Label.parse("abg"), new ALabel("C", this.alpha), -7);// if streamlined
		yxOK.mergeUpperCaseValue(Label.parse("ab"), new ALabel("C", this.alphabeth), -7);// if not streamlined
		yxOK.mergeUpperCaseValue(Label.parse("abc"), new ALabel("C", this.alphabeth), -10);// it could not be present because there is the labeled value
																						// (-10,abc)... it depends in which
		// order it is inserted.
		yxOK.mergeUpperCaseValue(Label.parse("bgp"), new ALabel("C", this.alphabeth), -7);//
		yxOK.mergeUpperCaseValue(Label.parse("ab¿c"), new ALabel("C", this.alphabeth), -11);//

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
	 * Test method for {@link it.univr.di.attic.CSTPSU_NodeSet#lowerCaseRule(TNGraph, TNGraph, CSTPSUCheckStatus)}.
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
	 *       CSTNPSUEdge ca = this.g.getEdgeFactory().get("CA");
	 *       CSTNPSUEdge da = this.g.getEdgeFactory().get("DA");
	 *       dc.mergeLowerCaseValue(Label.parse("ab"), new ALabel("c", this.alpha), 3);
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
	 *       CSTNPSUEdge daOk = this.g.getEdgeFactory().get("DA");
	 *       daOk.mergeLabeledValue(Label.parse("abc"), 3);
	 *       daOk.mergeLabeledValue(Label.parse("abd"), -8);
	 *       assertEquals("Lower Case values:", daOk.getLabeledValueMap(), da.getLabeledValueMap());
	 *       // System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
	 *       // System.out.printf("G: %s\n", g1);
	 *       }
	 */

	/**
	 * Test method for {@link it.univr.di.attic.CSTPSU_NodeSet#lowerCaseRule(TNGraph, TNGraph, CSTPSUCheckStatus)}.
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
	 *       CSTNPSUEdge dc = this.g.getEdgeFactory().get("DC");
	 *       CSTNPSUEdge ca = this.g.getEdgeFactory().get("CA");
	 *       CSTNPSUEdge da = this.g.getEdgeFactory().get("DA");
	 *       dc.mergeLowerCaseValue(Label.parse("ab"), new ALabel("c", this.alpha), 3);
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
	 *       CSTNPSUEdge daOk = this.g.getEdgeFactory().get("DA");
	 *       daOk.mergeLabeledValue(Label.parse("abc"), 3);
	 *       daOk.mergeLabeledValue(Label.parse("a¿b"), -8);
	 *       assertEquals("Lower Case values:", daOk.getLabeledValueMap(), da.getLabeledValueMap());
	 *       // System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
	 *       // System.out.printf("G: %s\n", g1);
	 *       }
	 */

	/**
	 * Test method for {@link it.univr.di.attic.CSTPSU_NodeSet#upperCaseRule(TNGraph, TNGraph, CSTPSUCheckStatus)}.
	 * Z &lt;---[3,B,ab]--- C &lt;----[-13,,b][11,,c]----D
	 */
	@SuppressWarnings("javadoc")
	@Test
	public final void testUpperCaseRule() {
		// System.out.printf("UPPER CASE\n");
		CSTNPSUEdge dc = this.g.getEdgeFactory().get("dc");
		CSTNPSUEdge cz = this.g.getEdgeFactory().get("cz");
		CSTNPSUEdge dz = this.g.getEdgeFactory().get("dz");
		cz.mergeUpperCaseValue(Label.parse("ab"), new ALabel("B", this.alphabeth), 3);
		dc.mergeLabeledValue(Label.parse("b"), -13);
		dc.mergeLabeledValue(Label.parse("c"), 11);
		LabeledNode C = this.g.getNodeFactory().get("C"), D = this.g.getNodeFactory().get("D");
		this.g.addEdge(dc, D, C);
		this.g.addEdge(cz, C, this.Z);
		this.g.addEdge(dz, D, this.Z);
		this.cstnpsu.rG1G3(D, C, this.Z, dc, cz, dz);

		CSTNPSUEdge daOk = this.g.getEdgeFactory().get("DZ");
		daOk.mergeUpperCaseValue(Label.parse("abc"), new ALabel("B", this.alphabeth), 14);
		daOk.mergeUpperCaseValue(Label.parse("ab"), new ALabel("B", this.alphabeth), -10);

		assertEquals("Upper Case values:", daOk.getUpperCaseValueMap(), dz.getUpperCaseValueMap());
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

		CSTNPSUEdge XY = this.g.getEdgeFactory().get("XY");
		XY.mergeLabeledValue(Label.emptyLabel, 2);
		XY.mergeLabeledValue(Label.parse("¬a"), -2);
		XY.mergeLabeledValue(Label.parse("b"), 1);
		XY.mergeLabeledValue(Label.parse("a¬b"), -1);
		// System.out.println(XY);

		CSTNPSUEdge YZ = this.g.getEdgeFactory().get("YZ");
		YZ.mergeLabeledValue(Label.parse("a"), 2);
		YZ.mergeLabeledValue(Label.parse("ab"), 1);
		YZ.mergeLabeledValue(Label.parse("a¬b"), -1);
		// System.out.println(YW);

		CSTNPSUEdge XZ = this.g.getEdgeFactory().get("XZ");

		this.g.addEdge(XY, X, Y);
		this.g.addEdge(YZ, Y, this.Z);
		this.g.addEdge(XZ, X, this.Z);

		wellDefinition(this.g);
		// System.out.println(XW);

		this.cstnpsu.rG1G3(X, Y, this.Z, XY, YZ, XZ);
		// System.out.println(XW);

		CSTNPSUEdge xwOK = this.g.getEdgeFactory().get("XZ");
		// xwOK.mergeLabeledValue(Label.parse("a"), 4);if no positive edge are propagated
		xwOK.mergeLabeledValue(Label.parse("a¬b"), -2);// if not streamlined
		xwOK.mergeLabeledValue(Label.parse("¿a¬b"), -3);
		xwOK.mergeLabeledValue(Label.parse("¿a"), -1);

		assertEquals("No case test.", xwOK.getLabeledValueMap(), XZ.getLabeledValueMap());
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

		ALabel aLabel = new ALabel(A.getName(), this.alphabeth);
		ALabel bLabel = new ALabel(B.getName(), this.alphabeth);

		CSTNPSUEdge XY = this.g.getEdgeFactory().get("XY");
		XY.mergeLabeledValue(Label.emptyLabel, 2);
		XY.mergeLabeledValue(Label.parse("¬a"), 1);
		XY.mergeLabeledValue(Label.parse("b"), 1);
		XY.mergeLabeledValue(Label.parse("a¬b"), -1);
		// System.out.println(XY);

		CSTNPSUEdge YZ = this.g.getEdgeFactory().get("YZ");
		YZ.mergeLabeledValue(Label.parse("a"), -1);
		YZ.mergeLabeledValue(Label.parse("a¬b"), -2);
		YZ.mergeUpperCaseValue(Label.parse("a"), aLabel, -2);
		YZ.mergeUpperCaseValue(Label.parse("ab"), aLabel, -3);
		YZ.mergeUpperCaseValue(Label.parse("a¬b"), aLabel.conjunction(bLabel), -4);
		// System.out.println(YW);

		CSTNPSUEdge XZ = this.g.getEdgeFactory().get("XZ");

		this.g.addEdge(XY, X, Y);
		this.g.addEdge(YZ, Y, this.Z);
		this.g.addEdge(XZ, X, this.Z);

		wellDefinition(this.g);
		// System.out.println(XW);

		this.cstnpsu.rG1G3(X, Y, this.Z, XY, YZ, XZ);
		// System.out.println(XW);

		CSTNPSUEdge xwOK = this.g.getEdgeFactory().get("XW");
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

		assertEquals("No case: XZ labeled values.", xwOK.getLabeledValueMap(), XZ.getLabeledValueMap());
		// 2018-12-18 Trying to make a-label simplification faster... loosing some optimization
		// asserEquals when full optimization is activated.
		assertNotEquals("No case: XZ upper case labedled values.", xwOK.getUpperCaseValueMap(), XZ.getUpperCaseValueMap());
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

		ALabel aLabel = new ALabel(A.getName(), this.alphabeth);
		ALabel bLabel = new ALabel(B.getName(), this.alphabeth);

		CSTNPSUEdge XY = this.g.getEdgeFactory().get("XY");
		XY.mergeUpperCaseValue(Label.emptyLabel, aLabel, 1);
		XY.mergeUpperCaseValue(Label.parse("¬a"), aLabel, -3);
		XY.mergeUpperCaseValue(Label.parse("b"), aLabel, -4);
		XY.mergeUpperCaseValue(Label.parse("a¬b"), aLabel.conjunction(bLabel), -2);
		// System.out.println(XY);

		CSTNPSUEdge YZ = this.g.getEdgeFactory().get("YZ");
		YZ.mergeLabeledValue(Label.parse("a"), 2);
		YZ.mergeLabeledValue(Label.parse("ab"), 1);
		YZ.mergeLabeledValue(Label.parse("a¬b"), -1);

		CSTNPSUEdge XZ = this.g.getEdgeFactory().get("XZ");

		this.g.addEdge(XY, X, Y);
		this.g.addEdge(YZ, Y, this.Z);
		this.g.addEdge(XZ, X, this.Z);

		wellDefinition(this.g);
		// System.out.println(XW);

		this.cstnpsu.rG1G3(X, Y, this.Z, XY, YZ, XZ);
		// System.out.println(XW);

		CSTNPSUEdge xzOK = this.g.getEdgeFactory().get("XW");
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

		ALabel aLabel = new ALabel(A.getName(), this.alphabeth);
		ALabel bLabel = new ALabel(B.getName(), this.alphabeth);

		CSTNPSUEdge XY = this.g.getEdgeFactory().get("XY");
		XY.mergeUpperCaseValue(Label.emptyLabel, aLabel, 1);
		XY.mergeUpperCaseValue(Label.parse("¬a"), aLabel, -3);
		XY.mergeUpperCaseValue(Label.parse("b"), aLabel, -4);
		XY.mergeUpperCaseValue(Label.parse("a¬b"), aLabel.conjunction(bLabel), -2);
		// System.out.println(XY);

		CSTNPSUEdge YZ = this.g.getEdgeFactory().get("YZ");
		YZ.mergeUpperCaseValue(Label.parse("a"), aLabel, 1);
		YZ.mergeUpperCaseValue(Label.parse("ab"), aLabel, 0);
		YZ.mergeUpperCaseValue(Label.parse("a¬b"), aLabel.conjunction(bLabel), -2);

		CSTNPSUEdge XZ = this.g.getEdgeFactory().get("XZ");

		this.g.addEdge(XY, X, Y);
		this.g.addEdge(YZ, Y, this.Z);
		this.g.addEdge(XZ, X, this.Z);

		wellDefinition(this.g);
		// System.out.println(XW);

		this.cstnpsu.rG1G3(X, Y, this.Z, XY, YZ, XZ);
		// System.out.println(XW);

		CSTNPSUEdge xzOK = this.g.getEdgeFactory().get("XZ");
		// <{¿a=A?->-2, ab=A?->-4, ¿a¬b=A?∙B?->-5, a¿b=A?∙B?->-6}>
		// xwOK.mergeUpperCaseValue(Label.parse("¿a"), aLabel, -2);From 20171010 unknown literal are not more propagated
		// xwOK.mergeUpperCaseValue(Label.parse("ab"), aLabel, -4);
		// xwOK.mergeUpperCaseValue(Label.parse("¿a¬b"), aLabel.conjunction(bLabel), -5);
		// xwOK.mergeUpperCaseValue(Label.parse("a¿b"), aLabel.conjunction(bLabel), -6);

		assertEquals("No case: XZ labeled values.", xzOK.getLabeledValueMap(), XZ.getLabeledValueMap());
		assertEquals("No case: XZ upper case labedled values.", xzOK.getUpperCaseValueMap(), XZ.getUpperCaseValueMap());
	}

	/**
	 * @throws WellDefinitionException
	 */
	@Test
	public final void test_llcRule() throws WellDefinitionException {
		LabeledNode A = this.g.getNodeFactory().get("A");
		LabeledNode C = this.g.getNodeFactory().get("C");
		LabeledNode D = this.g.getNodeFactory().get("D");
		LabeledNode a = this.g.getNodeFactory().get("a?", 'a');
		LabeledNode b = this.g.getNodeFactory().get("b?", 'b');

		this.g.addVertex(A);
		this.g.addVertex(a);
		this.g.addVertex(b);
		this.g.addVertex(C);
		this.g.addVertex(D);
		this.g.setZ(this.Z);

		ALabel CaLabel = new ALabel(C.getName(), this.alphabeth);

		CSTNPSUEdge CA = this.g.getEdgeFactory().get("CA");
		CA.mergeUpperCaseValue(Label.parse("a"), CaLabel, -6);
		CA.mergeLabeledValue(Label.parse("a"), -1);
		CA.setConstraintType(ConstraintType.contingent);
		CSTNPSUEdge AC = this.g.getEdgeFactory().get("AC");
		AC.mergeLowerCaseValue(Label.parse("a"), CaLabel, 3);
		AC.mergeLabeledValue(Label.parse("a"), 10);
		AC.setConstraintType(ConstraintType.contingent);
		CSTNPSUEdge CD = this.g.getEdgeFactory().get("CD");
		CD.mergeLabeledValue(Label.parse("b"), 0);

		CSTNPSUEdge DA = this.g.getEdgeFactory().get("DA");
		DA.mergeLabeledValue(Label.parse("b"), -4);

		this.g.addEdge(AC, A, C);
		this.g.addEdge(CA, C, A);
		this.g.addEdge(CD, C, D);
		this.g.addEdge(DA, D, A);

		this.cstnpsu.setG(this.g);

		CSTNUCheckStatus status = this.cstnpsu.dynamicControllabilityCheck();

		assertFalse(status.consistency);
	}

}
