package it.univr.di.cstnu.graph;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.plaf.basic.BasicIconFactory;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.annotations.AnnotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
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

/**
 * It is a PluggableGraphMouse class that manages a collection of plugins for picking and transforming the graph. Additionally, it carries the notion of a Mode:
 * Picking, Translating, Editing. Switching between modes allows for a more natural choice of mouse modifiers to be used for the various plugins. The default
 * modifiers are intended to mimick those of mainstream software applications in order to be intuitive to users. To change between modes, two different controls
 * are offered, a combo box and a menu system. These controls are lazily created in their respective 'getter' methods so they don't impact code that does not
 * intend to use them. The menu control can be placed in an unused corner of the GraphZoomScrollPane, which is a common location for mouse mode selection menus
 * in mainstream applications. The order of the plugins is important, as they are evaluated against the mask parameters in the order that they are added.<br>
 * <br>
 * Posenato: I modified the source to use a local EditingGraphMousePlugin.
 *
 * @author posenato
 * @param <V>
 *                vertex type
 * @param <E>
 *                edge type
 * @version $Id: $Id
 */
@SuppressWarnings("javadoc")
public class EditingModalGraphMouse<V, E> extends AbstractModalGraphMouse {

	/**
	 * Internal utility class to set the mode by keyboard.
	 *
	 * @author posenato
	 */
	public static class ModeKeyAdapter extends KeyAdapter {
		private char t = 't';
		private char p = 'p';
		private char e = 'e';
		protected ModalGraphMouse graphMouse;

		/**
		 * @param t
		 *                t key
		 * @param p
		 *                p key
		 * @param e
		 *                e key
		 * @param a
		 *                a key, not used.
		 * @param graphMouse
		 *                a ModalGraphMouse object.
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
	 * Factory used to create a vertex.
	 */
	protected Factory<V> vertexFactory;
	/**
	 * Factory used to create an edge.
	 */
	protected Factory<E> edgeFactory;
	/**
	 * @see edu.uci.ics.jung.visualization.control.EditingGraphMousePlugin
	 */
	protected EditingGraphMousePlugin<V, E> editingPlugin;
	protected LabelEditingGraphMousePlugin<? extends LabeledNode, ? extends LabeledIntEdge> labelEditingPlugin;
	protected EditingPopupGraphMousePlugin<V, E> popupEditingPlugin;
	protected AnnotatingGraphMousePlugin<V, E> annotatingPlugin;
	protected MultiLayerTransformer basicTransformer;

	protected RenderContext<V, E> rc;

	/**
	 * Creates an instance with default values.
	 *
	 * @param rc
	 *                a {@link edu.uci.ics.jung.visualization.RenderContext} object.
	 * @param vertexFactory
	 *                a {@link org.apache.commons.collections15.Factory} object.
	 * @param edgeFactory
	 *                a {@link org.apache.commons.collections15.Factory} object.
	 */
	public EditingModalGraphMouse(final RenderContext<V, E> rc, final Factory<V> vertexFactory, final Factory<E> edgeFactory) {
		this(rc, vertexFactory, edgeFactory, 1.1f, 1 / 1.1f);
	}

	/**
	 * Creates an instance with passed values.
	 *
	 * @param rc
	 *                a {@link edu.uci.ics.jung.visualization.RenderContext} object.
	 * @param vertexFactory
	 *                a {@link org.apache.commons.collections15.Factory} object.
	 * @param edgeFactory
	 *                a {@link org.apache.commons.collections15.Factory} object.
	 * @param in
	 *                override value for scale in
	 * @param out
	 *                override value for scale out
	 */
	public EditingModalGraphMouse(final RenderContext<V, E> rc, final Factory<V> vertexFactory, final Factory<E> edgeFactory, final float in,
			final float out) {
		super(in, out);
		this.vertexFactory = vertexFactory;
		this.edgeFactory = edgeFactory;
		this.rc = rc;
		this.basicTransformer = rc.getMultiLayerTransformer();
		this.loadPlugins();
		this.setModeKeyListener(new ModeKeyAdapter(this));
	}

	/**
	 * <p>
	 * Getter for the field <code>annotatingPlugin</code>.
	 * </p>
	 *
	 * @return the annotatingPlugin
	 */
	public AnnotatingGraphMousePlugin<V, E> getAnnotatingPlugin() {
		return this.annotatingPlugin;
	}

	/**
	 * <p>
	 * Getter for the field <code>editingPlugin</code>.
	 * </p>
	 *
	 * @return the editingPlugin
	 */
	public EditingGraphMousePlugin<V, E> getEditingPlugin() {
		return this.editingPlugin;
	}

	/**
	 * <p>
	 * Getter for the field <code>labelEditingPlugin</code>.
	 * </p>
	 *
	 * @return the labelEditingPlugin
	 */
	public LabelEditingGraphMousePlugin<? extends LabeledNode, ? extends LabeledIntEdge> getLabelEditingPlugin() {
		return this.labelEditingPlugin;
	}

	/** {@inheritDoc} */
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
	 * {@inheritDoc} create (if necessary) and return a menu that will change the mode
	 */
	@Override
	public JMenu getModeMenu() {
		if (this.modeMenu == null) {
			this.modeMenu = new JMenu();// {
			final Icon icon = BasicIconFactory.getMenuArrowIcon();
			this.modeMenu.setIcon(BasicIconFactory.getMenuArrowIcon());
			this.modeMenu.setPreferredSize(new Dimension(icon.getIconWidth() + 10, icon.getIconHeight() + 10));

			final JRadioButtonMenuItem transformingButton = new JRadioButtonMenuItem("Move graph");
			transformingButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						EditingModalGraphMouse.this.setMode(Mode.TRANSFORMING);
					}
				}
			});

			final JRadioButtonMenuItem pickingButton = new JRadioButtonMenuItem("Edit attributes");
			pickingButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						EditingModalGraphMouse.this.setMode(Mode.PICKING);
					}
				}
			});

			final JRadioButtonMenuItem editingButton = new JRadioButtonMenuItem("Create node/edges");
			editingButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						EditingModalGraphMouse.this.setMode(Mode.EDITING);
					}
				}
			});

			final ButtonGroup radio = new ButtonGroup();
			radio.add(transformingButton);
			radio.add(pickingButton);
			radio.add(editingButton);
			transformingButton.setSelected(true);
			this.modeMenu.add(transformingButton);
			this.modeMenu.add(pickingButton);
			this.modeMenu.add(editingButton);
			this.modeMenu.setToolTipText("Menu for setting Mouse Mode");
			this.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED)
						if (e.getItem() == Mode.TRANSFORMING) {
							transformingButton.setSelected(true);
						} else if (e.getItem() == Mode.PICKING) {
							pickingButton.setSelected(true);
						} else if (e.getItem() == Mode.EDITING) {
							editingButton.setSelected(true);
						}
				}
			});
		}
		return this.modeMenu;
	}

	/**
	 * <p>
	 * Getter for the field <code>popupEditingPlugin</code>.
	 * </p>
	 *
	 * @return the popupEditingPlugin
	 */
	public EditingPopupGraphMousePlugin<V, E> getPopupEditingPlugin() {
		return this.popupEditingPlugin;
	}

	/**
	 * {@inheritDoc} setter for the Mode.
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
	@SuppressWarnings("unused")
	@Override
	protected void loadPlugins() {
		this.pickingPlugin = new PickingGraphMousePlugin<V, E>();
		this.animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<V, E>();
		this.translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
		this.scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, this.in, this.out);
		this.rotatingPlugin = new RotatingGraphMousePlugin();
		this.shearingPlugin = new ShearingGraphMousePlugin();
		this.editingPlugin = new EditingGraphMousePlugin<V, E>(this.vertexFactory, this.edgeFactory);
		this.labelEditingPlugin = new LabelEditingGraphMousePlugin<LabeledNode, LabeledIntEdge>();
		this.annotatingPlugin = new AnnotatingGraphMousePlugin<V, E>(this.rc);
		this.popupEditingPlugin = new EditingPopupGraphMousePlugin<V, E>(this.vertexFactory, this.edgeFactory);
		this.add(this.scalingPlugin);
		this.setMode(Mode.EDITING);
	}

	/**
	 * Set the annotation mode, where it is possible to set/alter/delete text notes showed as sticky notes over the graph.
	 */
	protected void setAnnotatingMode() {
		this.remove(this.pickingPlugin);
		this.remove(this.animatedPickingPlugin);
		this.remove(this.translatingPlugin);
		this.remove(this.rotatingPlugin);
		this.remove(this.shearingPlugin);
		this.remove(this.labelEditingPlugin);
		this.remove(this.editingPlugin);
		this.remove(this.popupEditingPlugin);
		this.add(this.annotatingPlugin);
	}

	/**
	 * Set the editing mode, where vertex or edge attributes can be modified.
	 */
	protected void setEditingMode() {
		this.remove(this.pickingPlugin);
		this.remove(this.animatedPickingPlugin);
		this.remove(this.translatingPlugin);
		this.remove(this.rotatingPlugin);
		this.remove(this.shearingPlugin);
		this.remove(this.labelEditingPlugin);
		this.remove(this.annotatingPlugin);
		this.add(this.editingPlugin);
		this.add(this.popupEditingPlugin);
	}

	/** {@inheritDoc} */
	@Override
	protected void setPickingMode() {
		this.remove(this.translatingPlugin);
		this.remove(this.rotatingPlugin);
		this.remove(this.shearingPlugin);
		this.remove(this.editingPlugin);
		this.remove(this.annotatingPlugin);
		this.add(this.pickingPlugin);
		this.add(this.animatedPickingPlugin);
		this.add(this.labelEditingPlugin);
		this.add(this.popupEditingPlugin);
	}

	/** {@inheritDoc} */
	@Override
	protected void setTransformingMode() {
		this.remove(this.pickingPlugin);
		this.remove(this.animatedPickingPlugin);
		this.remove(this.editingPlugin);
		this.remove(this.annotatingPlugin);
		this.add(this.translatingPlugin);
		this.add(this.rotatingPlugin);
		this.add(this.shearingPlugin);
		this.add(this.labelEditingPlugin);
		this.add(this.popupEditingPlugin);
	}
}
