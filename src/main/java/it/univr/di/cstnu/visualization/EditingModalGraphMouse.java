package it.univr.di.cstnu.visualization;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComboBox;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.annotations.AnnotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.EditingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.EditingPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import it.univr.di.cstnu.graph.LabeledIntEdge;
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
public class EditingModalGraphMouse<V extends LabeledNode, E extends LabeledIntEdge> extends edu.uci.ics.jung.visualization.control.EditingModalGraphMouse<V, E> {

	/**
	 * Internal utility class to set the mode by keyboard.
	 * 
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
		public ModeKeyAdapter(final char t, final char p, final char e, final char a, final ModalGraphMouse graphMouse) {
			this.t = t;
			this.p = p;
			this.e = e;
			this.graphMouse = graphMouse;
		}

		/**
		 * @param graphMouse a ModalGraphMouse object.
		 */
		public ModeKeyAdapter(final ModalGraphMouse graphMouse) {
			this.graphMouse = graphMouse;
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
			} else if (keyChar == this.e) {
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
	 * create an instance with default values
	 *
	 */
	@SuppressWarnings("unused")
	private EditingModalGraphMouse(RenderContext<V,E> rc, Supplier<V> vertexFactory, Supplier<E> edgeFactory) {
		this(rc, vertexFactory, edgeFactory, 1.1f, 1/1.1f);
	}

	/**
	 * create an instance with passed values
	 * @param in override value for scale in
	 * @param out override value for scale out
	 */
	private EditingModalGraphMouse(RenderContext<V,E> rc, Supplier<V> vertexFactory, Supplier<E> edgeFactory, float in, float out) {
		super(rc,vertexFactory, edgeFactory, in,out);
	}

	
	/**
	 * Creates an instance with default values.
	 *
	 * @param rc a {@link edu.uci.ics.jung.visualization.RenderContext} object.
	 * @param vertexFactory a {@link org.apache.commons.collections15.Factory} object.
	 * @param edgeFactory a {@link org.apache.commons.collections15.Factory} object.
	 */
	public EditingModalGraphMouse(final RenderContext<V, E> rc, final Supplier<V> vertexFactory, final Supplier<E> edgeFactory, CSTNEditor cstnEditor) {
		super(rc, vertexFactory,edgeFactory);//this constructor uses local loadPlugins but LabelEditingGraphMousePlugin cannot be initialized correctly.
		this.cstnEditor = cstnEditor;
		this.labelEditingPlugin = new LabelEditingGraphMousePlugin<V, E>(this.cstnEditor);
	}

	/** {@inheritDoc} 
	 *  Removed annotating mode.
	 * */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public JComboBox getModeComboBox() {
		if (this.modeBox == null) {
			this.modeBox = new JComboBox(new Mode[] { Mode.TRANSFORMING, Mode.PICKING, Mode.EDITING });
			this.modeBox.addItemListener(this.getModeListener());
		}
		this.modeBox.setSelectedItem(this.mode);
		return this.modeBox;
	}

	/**
	 * {@inheritDoc} setter for the Mode.
	 * 
	 * Removed annotating mode.
	 */
	@Override
	public void setMode(final Mode mode) {
		if (this.mode != mode) {
			this.fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this.mode, ItemEvent.DESELECTED));
			this.mode = mode;
			if (mode == Mode.TRANSFORMING) {
				this.setTransformingMode();
			} else if (mode == Mode.PICKING) {
				this.setPickingMode();
			} else if (mode == Mode.EDITING) {
				this.setEditingMode();
			}
			// else if (mode == Mode.ANNOTATING) {
			// setAnnotatingMode();
			// }
			if (this.modeBox != null) {
				this.modeBox.setSelectedItem(mode);
			}
			this.fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, mode, ItemEvent.SELECTED));
		}
	}

	/**
	 * {@inheritDoc} Create the plugins, and load the plugins for TRANSFORMING mode
	 */
	@Override
	protected void loadPlugins() {
		this.pickingPlugin = new PickingGraphMousePlugin<V, E>();
		this.animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<V, E>();
		this.translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
		this.scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, this.in, this.out);
		this.rotatingPlugin = new RotatingGraphMousePlugin();
		this.shearingPlugin = new ShearingGraphMousePlugin();
		this.editingPlugin = new EditingGraphMousePlugin<V, E>(this.vertexFactory, this.edgeFactory);
		this.labelEditingPlugin = new LabelEditingGraphMousePlugin<V, E>(this.cstnEditor);
		this.annotatingPlugin = new AnnotatingGraphMousePlugin<V, E>(this.rc);
		this.popupEditingPlugin = new EditingPopupGraphMousePlugin<V, E>(this.vertexFactory, this.edgeFactory);
		this.add(this.scalingPlugin);
		this.setMode(Mode.EDITING);
	}

}
