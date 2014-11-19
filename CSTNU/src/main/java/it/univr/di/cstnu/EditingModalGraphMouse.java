package it.univr.di.cstnu;

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
 * @param <E>
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
		@SuppressWarnings("unused")
		private char a = 'a';
		protected ModalGraphMouse graphMouse;

		/**
		 * @param t
		 * @param p
		 * @param e
		 * @param a
		 * @param graphMouse
		 */
		public ModeKeyAdapter(char t, char p, char e, char a, ModalGraphMouse graphMouse) {
			this.t = t;
			this.p = p;
			this.e = e;
			this.a = a;
			this.graphMouse = graphMouse;
		}

		/**
		 * @param graphMouse
		 */
		public ModeKeyAdapter(ModalGraphMouse graphMouse) {
			this.graphMouse = graphMouse;
		}

		@Override
		public void keyTyped(KeyEvent event) {
			final char keyChar = event.getKeyChar();
			if (keyChar == t) {
				((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				graphMouse.setMode(Mode.TRANSFORMING);
			} else if (keyChar == p) {
				((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				graphMouse.setMode(Mode.PICKING);
			} else if (keyChar == e) {
				((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				graphMouse.setMode(Mode.EDITING);
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
	protected LabelEditingGraphMousePlugin<? extends Node, ? extends Edge> labelEditingPlugin;
	protected EditingPopupGraphMousePlugin<V, E> popupEditingPlugin;
	protected AnnotatingGraphMousePlugin<V, E> annotatingPlugin;
	protected MultiLayerTransformer basicTransformer;

	protected RenderContext<V, E> rc;

	/**
	 * Creates an instance with default values.
	 * 
	 * @param rc
	 * @param vertexFactory
	 * @param edgeFactory
	 */
	public EditingModalGraphMouse(RenderContext<V, E> rc, Factory<V> vertexFactory, Factory<E> edgeFactory) {
		this(rc, vertexFactory, edgeFactory, 1.1f, 1 / 1.1f);
	}

	/**
	 * Creates an instance with passed values.
	 * 
	 * @param rc
	 * @param vertexFactory
	 * @param edgeFactory
	 * @param in override value for scale in
	 * @param out override value for scale out
	 */
	public EditingModalGraphMouse(RenderContext<V, E> rc, Factory<V> vertexFactory, Factory<E> edgeFactory, float in,
			float out) {
		super(in, out);
		this.vertexFactory = vertexFactory;
		this.edgeFactory = edgeFactory;
		this.rc = rc;
		this.basicTransformer = rc.getMultiLayerTransformer();
		loadPlugins();
		setModeKeyListener(new ModeKeyAdapter(this));
	}

	/**
	 * @return the annotatingPlugin
	 */
	public AnnotatingGraphMousePlugin<V, E> getAnnotatingPlugin() {
		return annotatingPlugin;
	}

	/**
	 * @return the editingPlugin
	 */
	public EditingGraphMousePlugin<V, E> getEditingPlugin() {
		return editingPlugin;
	}

	/**
	 * @return the labelEditingPlugin
	 */
	public LabelEditingGraphMousePlugin<? extends Node, ? extends Edge> getLabelEditingPlugin() {
		return labelEditingPlugin;
	}

	/**
	 * @return the modeBox.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public JComboBox getModeComboBox() {
		if (modeBox == null) {
			modeBox = new JComboBox(new Mode[] { Mode.TRANSFORMING, Mode.PICKING, Mode.EDITING });
			modeBox.addItemListener(getModeListener());
		}
		modeBox.setSelectedItem(mode);
		return modeBox;
	}

	/**
	 * create (if necessary) and return a menu that will change the mode
	 * 
	 * @return the menu
	 */
	@Override
	public JMenu getModeMenu() {
		if (modeMenu == null) {
			modeMenu = new JMenu();// {
			final Icon icon = BasicIconFactory.getMenuArrowIcon();
			modeMenu.setIcon(BasicIconFactory.getMenuArrowIcon());
			modeMenu.setPreferredSize(new Dimension(icon.getIconWidth() + 10, icon.getIconHeight() + 10));

			final JRadioButtonMenuItem transformingButton = new JRadioButtonMenuItem("Move graph");
			transformingButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) setMode(Mode.TRANSFORMING);
				}
			});

			final JRadioButtonMenuItem pickingButton = new JRadioButtonMenuItem("Edit attributes");
			pickingButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) setMode(Mode.PICKING);
				}
			});

			final JRadioButtonMenuItem editingButton = new JRadioButtonMenuItem("Create node/edges");
			editingButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) setMode(Mode.EDITING);
				}
			});

			final ButtonGroup radio = new ButtonGroup();
			radio.add(transformingButton);
			radio.add(pickingButton);
			radio.add(editingButton);
			transformingButton.setSelected(true);
			modeMenu.add(transformingButton);
			modeMenu.add(pickingButton);
			modeMenu.add(editingButton);
			modeMenu.setToolTipText("Menu for setting Mouse Mode");
			addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) if (e.getItem() == Mode.TRANSFORMING)
						transformingButton.setSelected(true);
						else if (e.getItem() == Mode.PICKING)
							pickingButton.setSelected(true);
						else if (e.getItem() == Mode.EDITING) editingButton.setSelected(true);
				}
			});
		}
		return modeMenu;
	}

	/**
	 * @return the popupEditingPlugin
	 */
	public EditingPopupGraphMousePlugin<V, E> getPopupEditingPlugin() {
		return popupEditingPlugin;
	}

	/**
	 * Create the plugins, and load the plugins for TRANSFORMING mode
	 */
	@SuppressWarnings("unused")
	@Override
	protected void loadPlugins() {
		pickingPlugin = new PickingGraphMousePlugin<V, E>();
		animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<V, E>();
		translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
		scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
		rotatingPlugin = new RotatingGraphMousePlugin();
		shearingPlugin = new ShearingGraphMousePlugin();
		editingPlugin = new EditingGraphMousePlugin<V, E>(vertexFactory, edgeFactory);
		labelEditingPlugin = new LabelEditingGraphMousePlugin<Node, Edge>();
		annotatingPlugin = new AnnotatingGraphMousePlugin<V, E>(rc);
		popupEditingPlugin = new EditingPopupGraphMousePlugin<V, E>(vertexFactory, edgeFactory);
		add(scalingPlugin);
		setMode(Mode.EDITING);
	}

	/**
	 * Set the annotation mode, where it is possible to set/alter/delete text notes showed as sticky notes over the graph.
	 */
	protected void setAnnotatingMode() {
		remove(pickingPlugin);
		remove(animatedPickingPlugin);
		remove(translatingPlugin);
		remove(rotatingPlugin);
		remove(shearingPlugin);
		remove(labelEditingPlugin);
		remove(editingPlugin);
		remove(popupEditingPlugin);
		add(annotatingPlugin);
	}

	/**
	 * Set the editing mode, where vertex or edge attributes can be modified.
	 */
	protected void setEditingMode() {
		remove(pickingPlugin);
		remove(animatedPickingPlugin);
		remove(translatingPlugin);
		remove(rotatingPlugin);
		remove(shearingPlugin);
		remove(labelEditingPlugin);
		remove(annotatingPlugin);
		add(editingPlugin);
		add(popupEditingPlugin);
	}

	/**
	 * setter for the Mode.
	 */
	@Override
	public void setMode(Mode mode) {
		if (this.mode != mode) {
			fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this.mode, ItemEvent.DESELECTED));
			this.mode = mode;
			if (mode == Mode.TRANSFORMING)
				setTransformingMode();
			else if (mode == Mode.PICKING)
				setPickingMode();
			else if (mode == Mode.EDITING) setEditingMode();
			// else if (mode == Mode.ANNOTATING) {
			// setAnnotatingMode();
			// }
			if (modeBox != null) modeBox.setSelectedItem(mode);
			fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, mode, ItemEvent.SELECTED));
		}
	}

	@Override
	protected void setPickingMode() {
		remove(translatingPlugin);
		remove(rotatingPlugin);
		remove(shearingPlugin);
		remove(editingPlugin);
		remove(annotatingPlugin);
		add(pickingPlugin);
		add(animatedPickingPlugin);
		add(labelEditingPlugin);
		add(popupEditingPlugin);
	}

	@Override
	protected void setTransformingMode() {
		remove(pickingPlugin);
		remove(animatedPickingPlugin);
		remove(editingPlugin);
		remove(annotatingPlugin);
		add(translatingPlugin);
		add(rotatingPlugin);
		add(shearingPlugin);
		add(labelEditingPlugin);
		add(popupEditingPlugin);
	}
}
