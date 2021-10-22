// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 * 
 */
package it.univr.di.cstnu.visualization;

import java.awt.Paint;

import com.google.common.base.Function;

import edu.uci.ics.jung.visualization.picking.PickedInfo;
import it.univr.di.cstnu.graph.Edge;
import it.univr.di.cstnu.graph.LabeledNode;

/**
 * Provides method for LabeledNode rendering in TNEditor GUI application.
 *
 * @author posenato
 * @version $Id: $Id
 */
public class NodeRendering {

	/**
	 * Used to show the node name.
	 */
	public final static Function<LabeledNode, String> vertexLabelFunction = new Function<>() {
		/**
		 * Returns a label for the node
		 */
		@Override
		public String apply(final LabeledNode v) {
			return v.getName() + (v.getLabel().isEmpty() ? "" : "_[" + v.getLabel() + "]");
		}
	};

	/**
	 * Transformer object to show the tooltip of node: the label is print.
	 */
	public static final Function<LabeledNode, String> vertexToolTipFunction = new Function<>() {
		@Override
		public String apply(final LabeledNode v) {
			return "Propositional label: " + v.getLabel().toString();
		}
	};

	/**
	 * Returns a transformer to select the color that is used to draw the edge. This transformer uses the type and the state of the edge to select the color.
	 *
	 * @param pi a {@link edu.uci.ics.jung.visualization.picking.PickedInfo} object.
	 * @param pickedPaint color when the node is selected 
	 * @param fillPaint color for non-selected node
	 * @param negativeCyclePaint color when node is in a negative cycle.
	 * @return a transformer object to draw an edge with a different color when it is picked.
	 * @param <K> a K object.
	 */
	public static final <K extends LabeledNode> Function<K, Paint> nodeDrawPaintTransformer(final PickedInfo<K> pi,
			final Paint pickedPaint,
			final Paint fillPaint,
			final Paint negativeCyclePaint) {

		
		return new Function<>() {
			@Override
			public Paint apply(final K node) {
				if (node == null)
					return fillPaint;
				if (pi.isPicked(node))
					return pickedPaint;
				if (node.inNegativeCycle())
					return negativeCyclePaint;
				return fillPaint;
			}
		};
	}

	/**
	 * An edge is usually draw as an arc between two points.<br>
	 * The area delimited by the arc and the straight line connecting the two edge points can be filled by
	 * a color.
	 *
	 * @param normalPaint for normale edge
	 * @param contingentPaint for contingent edge
	 * @param derivedPaint for derived edge
	 * @return a transformer object to fill an edge 'area' with a color depending on edge type.
	 */
	static final Function<Edge, Paint> edgeFillPaintTransformer(final Paint normalPaint, final Paint contingentPaint, final Paint derivedPaint) {
		return new Function<>() {
			final Paint[] paintMap = { normalPaint, contingentPaint, derivedPaint, normalPaint, normalPaint };

			@Override
			public Paint apply(final Edge e) {
				if (e == null)
					return normalPaint;
				return this.paintMap[e.getConstraintType().ordinal()];
			}
		};
	}

}
