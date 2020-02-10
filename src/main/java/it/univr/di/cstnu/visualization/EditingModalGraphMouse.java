package it.univr.di.cstnu.visualization;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.plaf.basic.BasicIconFactory;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.annotations.AnnotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import it.univr.di.cstnu.graph.Edge;
import it.univr.di.cstnu.graph.EdgeSupplier;
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
public class EditingModalGraphMouse<V extends LabeledNode, E extends Edge> extends AbstractModalGraphMouse {

	/**
	 * logger
	 */
	static Logger LOG = Logger.getLogger(EditingModalGraphMouse.class.getName());

	/**
	 * Internal reference to the main JFrame.
	 */
	CSTNEditor cstnEditor;

	/**
	 * Internal flag for activating 'editing' functions.
	 */
	boolean editor;

	Supplier<V> vertexFactory;
	Supplier<E> edgeFactory;
	EditingGraphMousePlugin<V, E> editingPlugin;
	LabelEditingGraphMousePlugin<V, E> labelEditingPlugin;
	EditingPopupGraphMousePlugin<V, E> popupEditingPlugin;
	AnnotatingGraphMousePlugin<V, E> annotatingPlugin;
	MultiLayerTransformer basicTransformer;
	RenderContext<V, E> rc;


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
		super(1.1f, 1 / 1.1f);
		this.vertexFactory = vertexFactory1;
		this.edgeFactory = edgeFactory1;
		this.rc = rc1;
		this.basicTransformer = rc1.getMultiLayerTransformer();
		this.editor = editor1;
		this.cstnEditor = cstnEditor1;
		loadPlugins();
		setModeKeyListener(new ModeKeyAdapter(this));
	}

	/**
	 * create the plugins, and load the plugins for TRANSFORMING mode
	 */
	@Override
	protected void loadPlugins() {
		this.pickingPlugin = new PickingGraphMousePlugin<V, E>();
		this.animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<V, E>();
		this.translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
		this.scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, this.in, this.out);
		this.rotatingPlugin = new RotatingGraphMousePlugin();
		this.shearingPlugin = new ShearingGraphMousePlugin();
		this.editingPlugin = new EditingGraphMousePlugin<>(this.vertexFactory, this.edgeFactory);
		this.labelEditingPlugin = new LabelEditingGraphMousePlugin<>(this.cstnEditor);
		// this.annotatingPlugin = new AnnotatingGraphMousePlugin<>(this.rc);
		this.popupEditingPlugin = new EditingPopupGraphMousePlugin<>(this.vertexFactory, this.edgeFactory);
		add(this.scalingPlugin);// for zooming
		setMode(Mode.TRANSFORMING);
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
			if (mode1 == Mode.EDITING) {
				this.setEditingMode();
			} else {
				this.setTransformingMode();
			}
			// if (mode1 == Mode.PICKING) {
			// }
			// this.setPickingMode();
			// } else if (mode1 == Mode.EDITING && this.editor) {
			// this.setOLDEditingMode();
			// }
			// // else if (mode == Mode.ANNOTATING) {
			// setAnnotatingMode();
			// }
			if (this.modeBox != null) {
				this.modeBox.setSelectedItem(mode1);
			}
			this.fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, mode1, ItemEvent.SELECTED));
		}
	}

	public void setEdgeEditingPlugin(EdgeSupplier<E> edgeSupp) {
		this.edgeFactory = edgeSupp;
		this.editingPlugin.setEdgeFactory(edgeSupp);
		this.popupEditingPlugin.setEdgeFactory(edgeSupp);
	}

	public Supplier<E> getEdgeEditingPlugin() {
		return this.edgeFactory;
	}

	protected void setEditingMode() {
		remove(this.translatingPlugin);
		remove(this.rotatingPlugin);
		remove(this.shearingPlugin);
		remove(this.editingPlugin);
		// remove(this.annotatingPlugin);
		add(this.pickingPlugin);
		add(this.animatedPickingPlugin);
		add(this.labelEditingPlugin);
		add(this.popupEditingPlugin);
		add(this.editingPlugin);
	}

	@Override
	protected void setTransformingMode() {
		remove(this.pickingPlugin);
		remove(this.animatedPickingPlugin);
		remove(this.editingPlugin);
		// remove(this.annotatingPlugin);
		add(this.translatingPlugin);
		add(this.rotatingPlugin);
		add(this.shearingPlugin);
		add(this.labelEditingPlugin);
		add(this.popupEditingPlugin);
	}

	// protected void setAnnotatingMode() {
	// remove(this.pickingPlugin);
	// remove(this.animatedPickingPlugin);
	// remove(this.translatingPlugin);
	// remove(this.rotatingPlugin);
	// remove(this.shearingPlugin);
	// remove(this.labelEditingPlugin);
	// remove(this.editingPlugin);
	// remove(this.popupEditingPlugin);
	// add(this.annotatingPlugin);
	// }

	/**
	 * @return the modeBox.
	 */
	@Override
	public JComboBox<Mode> getModeComboBox() {
		if (this.modeBox == null) {
			this.modeBox = new JComboBox<>(new Mode[] { Mode.TRANSFORMING, Mode.EDITING });// , Mode.PICKING, Mode.ANNOTATING
			this.modeBox.addItemListener(getModeListener());
		}
		this.modeBox.setSelectedItem(this.mode);
		return this.modeBox;
	}

	/**
	 * create (if necessary) and return a menu that will change
	 * the mode
	 * 
	 * @return the menu
	 */
	@Override
	public JMenu getModeMenu() {
		if (this.modeMenu == null) {
			this.modeMenu = new JMenu();// {
			Icon icon = BasicIconFactory.getMenuArrowIcon();
			this.modeMenu.setIcon(BasicIconFactory.getMenuArrowIcon());
			this.modeMenu.setPreferredSize(new Dimension(icon.getIconWidth() + 10,
					icon.getIconHeight() + 10));

			final JRadioButtonMenuItem transformingButton = new JRadioButtonMenuItem(Mode.TRANSFORMING.toString());
			transformingButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setMode(Mode.TRANSFORMING);
					}
				}
			});

			// final JRadioButtonMenuItem pickingButton = new JRadioButtonMenuItem(Mode.PICKING.toString());
			// pickingButton.addItemListener(new ItemListener() {
			// @Override
			// public void itemStateChanged(ItemEvent e) {
			// if (e.getStateChange() == ItemEvent.SELECTED) {
			// setMode(Mode.PICKING);
			// }
			// }
			// });

			final JRadioButtonMenuItem editingButton = new JRadioButtonMenuItem(Mode.EDITING.toString());
			editingButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setMode(Mode.EDITING);
					}
				}
			});

			ButtonGroup radio = new ButtonGroup();
			radio.add(transformingButton);
			// radio.add(pickingButton);
			radio.add(editingButton);
			transformingButton.setSelected(true);
			this.modeMenu.add(transformingButton);
			// this.modeMenu.add(pickingButton);
			this.modeMenu.add(editingButton);
			this.modeMenu.setToolTipText("Menu for setting Mouse Mode");
			addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (e.getItem() == Mode.TRANSFORMING) {
							transformingButton.setSelected(true);
							// } else if (e.getItem() == Mode.PICKING) {
							// pickingButton.setSelected(true);
						} else if (e.getItem() == Mode.EDITING) {
							editingButton.setSelected(true);
						}
					}
				}
			});
		}
		return this.modeMenu;
	}

	@SuppressWarnings("hiding")
	public static class ModeKeyAdapter extends KeyAdapter {
		private char t = 't';
		// private char p = 'p';
		private char e = 'e';
		// private char a = 'a';
		protected ModalGraphMouse graphMouse;

		public ModeKeyAdapter(ModalGraphMouse graphMouse) {
			this.graphMouse = graphMouse;
		}

		public ModeKeyAdapter(char t, char p, char e, char a, ModalGraphMouse graphMouse) {
			this.t = t;
			// this.p = p;
			this.e = e;
			// this.a = a;
			this.graphMouse = graphMouse;
		}

		@Override
		public void keyTyped(KeyEvent event) {
			char keyChar = event.getKeyChar();
			if (keyChar == this.t) {
				((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				this.graphMouse.setMode(Mode.TRANSFORMING);
				// } else if (keyChar == this.a) {
				// ((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				// this.graphMouse.setMode(Mode.ANNOTATING);
			} else if (keyChar == this.e) {
				((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				this.graphMouse.setMode(Mode.EDITING);
			}
			// else if (keyChar == this.a) {
			// ((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			// this.graphMouse.setMode(Mode.ANNOTATING);
			// }
		}
	}

	/**
	 * @return the annotatingPlugin
	 */
	// public AnnotatingGraphMousePlugin<V, E> getAnnotatingPlugin() {
	// return this.annotatingPlugin;
	// }
	//
	/**
	 * @return the editingPlugin
	 */
	public EditingGraphMousePlugin<V, E> getEditingPlugin() {
		return this.editingPlugin;
	}

	/**
	 * @return the labelEditingPlugin
	 */
	public LabelEditingGraphMousePlugin<V, E> getLabelEditingPlugin() {
		return this.labelEditingPlugin;
	}

	/**
	 * @return the popupEditingPlugin
	 */
	public EditingPopupGraphMousePlugin<V, E> getPopupEditingPlugin() {
		return this.popupEditingPlugin;
	}

}