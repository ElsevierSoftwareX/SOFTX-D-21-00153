package it.univr.di.cstnu.visualization;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.JComboBox;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.EditingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import it.univr.di.cstnu.graph.Edge;
import it.univr.di.cstnu.graph.LabeledNode;

/**
 * I modified the source to use a local EditingGraphMousePlugin and to remove some extra useless features.
 *
 * @param <V> vertex type
 * @param <E> edge type
 * @version $Id: $Id
 * @see edu.uci.ics.jung.visualization.control.EditingModalGraphMouse
 * @author posenato
 */
@SuppressWarnings("javadoc")
public class EditingModalGraphMouse<V extends LabeledNode, E extends Edge> extends edu.uci.ics.jung.visualization.control.EditingModalGraphMouse<V, E> {

	/**
	 * logger
	 */
	static Logger LOG = Logger.getLogger(EditingModalGraphMouse.class.getName());

	/**
	 * Internal utility class to set the mode by keyboard.
	 * I removed the annotation plugin.
	 *
	 * @author posenato
	 */
	public static class ModeKeyAdapter extends KeyAdapter {
		private char t = 't';
		private char p = 'p';
		private char e = 'e';
		protected ModalGraphMouse graphMouse;

		/**
		 * @param t t key
		 * @param p p key
		 * @param e e key
		 * @param a a key, not used.
		 * @param graphMouse a ModalGraphMouse object.
		 */
		@SuppressWarnings("hiding")
		public ModeKeyAdapter(final char t, final char p, final char e, final char a, final ModalGraphMouse graphMouse) {
			this.t = t;
			this.p = p;
			this.e = e;
			this.graphMouse = graphMouse;
		}

		/**
		 * @param graphMouse1 a ModalGraphMouse object.
		 */
		public ModeKeyAdapter(final ModalGraphMouse graphMouse1) {
			this.graphMouse = graphMouse1;
		}

		@Override
		public void keyTyped(final KeyEvent event) {
			final char keyChar = event.getKeyChar();
			if (keyChar == this.t) {
				((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				this.graphMouse.setMode(Mode.TRANSFORMING);
			} else if (keyChar == this.p) {
				((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				this.graphMouse.setMode(Mode.PICKING);
			} else if (keyChar == this.e && ((EditingModalGraphMouse<?, ?>) this.graphMouse).editor) {
				((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				this.graphMouse.setMode(Mode.EDITING);
			}
		}
	}

	/**
	 * Internal reference to the main JFrame.
	 */
	CSTNEditor cstnEditor;

	/**
	 * Internal flag for activating 'editing' functions.
	 */
	boolean editor;

	/**
	 * create an instance with default values
	 */
	@SuppressWarnings({ "unused", "hiding" })
	private EditingModalGraphMouse(RenderContext<V, E> rc, Supplier<V> vertexFactory, Supplier<E> edgeFactory) {
		this(rc, vertexFactory, edgeFactory, 1.1f, 1 / 1.1f);
	}

	/**
	 * create an instance with passed values
	 * 
	 * @param in override value for scale in
	 * @param out override value for scale out
	 */
	@SuppressWarnings("hiding")
	private EditingModalGraphMouse(RenderContext<V, E> rc, Supplier<V> vertexFactory, Supplier<E> edgeFactory, float in, float out) {
		super(rc, vertexFactory, edgeFactory, in, out);
		initCompletion(null, false);
	}

	/**
	 * Creates an instance with default values.
	 *
	 * @param rc1 a render contest.
	 * @param vertexFactory1 a vertex factory.
	 * @param edgeFactory1 an edge factory.
	 * @param editor1 true for having 'editing' function in modeComboBox.
	 */
	public EditingModalGraphMouse(final RenderContext<V, E> rc1, final Supplier<V> vertexFactory1, final Supplier<E> edgeFactory1, CSTNEditor cstnEditor1,
			boolean editor1) {
		super(rc1, vertexFactory1, edgeFactory1);// this constructor uses local loadPlugins but LabelEditingGraphMousePlugin cannot be initialized correctly.
		initCompletion(cstnEditor1, editor1);
	}

	private void initCompletion(CSTNEditor cstnEditor1, boolean editor1) {
		this.cstnEditor = cstnEditor1;
		// LOG.severe("EditingModalGraphMouse.cstnEditor " + cstnEditor);
		// I set again this.labelEditingPlugin
		((LabelEditingGraphMousePlugin<V, E>) this.labelEditingPlugin).setCstnEditor(this.cstnEditor);
		this.editor = editor1;
	}

	/**
	 * {@inheritDoc}
	 * Removed annotating mode.
	 */
	@Override
	public JComboBox<Mode> getModeComboBox() {
		if (this.modeBox == null) {
			this.modeBox = new JComboBox<>(
					(this.editor) ? (new Mode[] { Mode.TRANSFORMING, Mode.PICKING, Mode.EDITING }) : new Mode[] { Mode.TRANSFORMING, Mode.PICKING });
			this.modeBox.addItemListener(this.getModeListener());
		}
		this.modeBox.setSelectedItem(this.mode);
		return this.modeBox;
	}

	/**
	 * {@inheritDoc} setter for the Mode.
	 * Removed annotating mode.
	 */
	@Override
	public void setMode(final Mode mode1) {
		if (this.mode != mode1) {
			this.fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this.mode, ItemEvent.DESELECTED));
			this.mode = mode1;
			if (mode1 == Mode.TRANSFORMING) {
				this.setTransformingMode();
			} else if (mode1 == Mode.PICKING) {
				this.setPickingMode();
			} else if (mode1 == Mode.EDITING && this.editor) {
				this.setEditingMode();
			}
			// else if (mode == Mode.ANNOTATING) {
			// setAnnotatingMode();
			// }
			if (this.modeBox != null) {
				this.modeBox.setSelectedItem(mode1);
			}
			this.fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, mode1, ItemEvent.SELECTED));
		}
	}

	/**
	 * {@inheritDoc} Create the plugins, and load the plugins for TRANSFORMING mode
	 */
	@Override
	protected void loadPlugins() {
		// this.annotatingPlugin = new AnnotatingGraphMousePlugin<>(this.rc);
		this.pickingPlugin = new PickingGraphMousePlugin<V, E>();
		this.animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<V, E>();
		this.translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
		this.scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, this.in, this.out);
		this.add(this.scalingPlugin);
		this.rotatingPlugin = new RotatingGraphMousePlugin();
		this.shearingPlugin = new ShearingGraphMousePlugin();
		this.editingPlugin = new EditingGraphMousePlugin<>(this.vertexFactory, this.edgeFactory);
		// LOG.severe("EditingModalGraphMouse.cstnEditor " + cstnEditor); loadPlugins is called by super() that has not access to this.cstnEditor
		// so the following is made also in the constructor
		this.popupEditingPlugin = new EditingPopupGraphMousePlugin<>(this.vertexFactory, this.edgeFactory);
		this.labelEditingPlugin = new LabelEditingGraphMousePlugin<>(this.cstnEditor);
		this.setMode(Mode.PICKING);
	}

	public void setEdgeEditingPlugin(Supplier<E> edgeF) {
		this.edgeFactory = edgeF;
		this.loadPlugins();
	}

	@Override
	protected void setPickingMode() {
		remove(this.translatingPlugin);
		remove(this.rotatingPlugin);
		remove(this.shearingPlugin);
		remove(this.annotatingPlugin);
		remove(this.editingPlugin);
		add(this.pickingPlugin);
		add(this.animatedPickingPlugin);
		add(this.labelEditingPlugin);
		add(this.popupEditingPlugin);
	}

	@Override
	protected void setTransformingMode() {
		remove(this.pickingPlugin);
		remove(this.animatedPickingPlugin);
		remove(this.annotatingPlugin);
		remove(this.labelEditingPlugin);
		remove(this.popupEditingPlugin);
		remove(this.editingPlugin);
		add(this.translatingPlugin);
		add(this.rotatingPlugin);
		add(this.shearingPlugin);
		// add(this.labelEditingPlugin);
		// add(this.popupEditingPlugin);
	}

	@Override
	protected void setEditingMode() {
		remove(this.pickingPlugin);
		remove(this.animatedPickingPlugin);
		remove(this.translatingPlugin);
		remove(this.rotatingPlugin);
		remove(this.shearingPlugin);
		remove(this.annotatingPlugin);
		remove(this.labelEditingPlugin);
		remove(this.popupEditingPlugin);
		add(this.editingPlugin);
		// add(this.popupEditingPlugin);
		// add(this.labelEditingPlugin);
	}
}
