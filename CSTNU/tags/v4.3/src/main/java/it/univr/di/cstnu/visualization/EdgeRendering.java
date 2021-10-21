// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 * 
 */
package it.univr.di.cstnu.visualization;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;

import com.google.common.base.Function;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import it.univr.di.cstnu.graph.BasicCSTNUEdge;
import it.univr.di.cstnu.graph.CSTNEdge;
import it.univr.di.cstnu.graph.Edge;
import it.univr.di.cstnu.graph.STNEdge;
import it.univr.di.cstnu.graph.STNUEdge;
import it.univr.di.labeledvalue.Constants;

/**
 * <p>
 * EdgeRendering class.
 * </p>
 *
 * @author posenato
 * @version $Id: $Id
 */
public class EdgeRendering {

	/**
	 * Simple stroke object to draw a 'standard' type edge.
	 */
	static final Stroke constraintEdgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);

	// GRAPHICS & VISUALIZATION STUFF
	// Set up a new stroke Transformer for the edges
	/**
	 * Simple stroke object to draw a 'derived' type edge.
	 */
	static final Stroke derivedEdgeStroke = RenderContext.DOTTED;
	// new BasicStroke(0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);

	/**
	 * Simple stroke object to draw a 'standard' type edge.
	 */
	static final Stroke contingentEdgeStroke = new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);

	/**
	 * Font for edge label rendering
	 */
	public final static Function<Edge, Font> edgeFontFunction = new Function<>() {
		/**
		 * 
		 */
		Font b = new Font("Helvetica", Font.BOLD, 14);
		/**
		 * 
		 */
		protected boolean bold = false;
		/**
		 * 
		 */
		Font f = new Font("Helvetica", Font.PLAIN, 14);

		@Override
		public Font apply(Edge e) {
			if (this.bold)
				return this.b;
			return this.f;
		}

		/**
		 * @param bold1 true if it must be rendered in bold
		 *            @SuppressWarnings("unused")
		 *            public void setBold(boolean bold1) {
		 *            this.bold = bold1;
		 *            }
		 */
	};

	/**
	 * Font for edge label rendering
	 */
	public final static Function<Edge, String> edgeLabelFunction = new Function<>() {
		/**
		 * Returns a label for the edge
		 */
		@Override
		public String apply(final Edge e) {
			final StringBuffer sb = new StringBuffer();
			sb.append((e.getName().length() == 0 ? "''" : e.getName()));
			sb.append("; ");
			if (e.isSTNEdge() || e.isSTNUEdge()) {
				STNEdge e1 = (STNEdge) e;
				sb.append(Constants.formatInt(e1.getValue()));
			}
			if (e.isSTNUEdge()) {
				STNUEdge e1 = (STNUEdge) e;
				String lv = e1.getLabeledValueFormatted();
				if (!lv.isEmpty()) {
					sb.append("; ");
					sb.append(lv);
				}
			}
			if (e.isCSTNEdge()) {
				CSTNEdge e1 = (CSTNEdge) e;
				if (e1.getLabeledValueMap().size() > 0) {
					sb.append(e1.getLabeledValueMap().toString());
				}
			}
			if (BasicCSTNUEdge.class.isAssignableFrom(e.getClass())) {
				BasicCSTNUEdge e1 = (BasicCSTNUEdge) e;

				if (e1.upperCaseValueSize() > 0) {
					sb.append("; UL: ");
					sb.append(e1.upperCaseValuesAsString());
				}
				if (e1.lowerCaseValueSize() > 0) {
					sb.append("; LL:");
					sb.append(e1.lowerCaseValuesAsString());
				}

			}
			return sb.toString();
		}
	};

	/**
	 * Select how to draw an edge given its type.
	 */
	public final static Function<Edge, Stroke> edgeStrokeFunction = new Function<>() {
		@Override
		public Stroke apply(final Edge s) {
			switch (s.getConstraintType()) {
			case requirement:
				return constraintEdgeStroke;
			case derived:
			case internal:
				return derivedEdgeStroke;
			case contingent:
			case qloopFinder:
			default:
				return contingentEdgeStroke;
			}
		}
	};

	/**
	 * Returns a transformer to select the color that is used to draw the edge. This transformer uses the type and the state of the edge to select the color.
	 *
	 * @param pi a {@link edu.uci.ics.jung.visualization.picking.PickedInfo} object.
	 * @param pickedPaint a {@link java.awt.Paint} object.
	 * @param normalPaint a {@link java.awt.Paint} object.
	 * @param contingentPaint a {@link java.awt.Paint} object.
	 * @param derivedPaint a {@link java.awt.Paint} object.
	 * @param inNegativeCycle a {@link java.awt.Paint} object for rendering edge in a negative cycle.
	 * @param <K> a K object.
	 * @return a transformer object to draw an edge with a different color when it is picked.
	 */
	public static final <K extends Edge> Function<K, Paint> edgeDrawPaintFunction(final PickedInfo<K> pi,
			final Paint pickedPaint,
			final Paint normalPaint,
			final Paint contingentPaint,
			final Paint derivedPaint,
			final Paint inNegativeCycle) {

		final Paint[] paintMap = new Paint[] { normalPaint, contingentPaint, derivedPaint, normalPaint, derivedPaint };

		return new Function<>() {
			@Override
			public Paint apply(final K e) {
				if (e == null)
					return normalPaint;
				// LabeledIntEdge.LOG.finer("LabeledIntEdge: " + e + ", picked: " + pi.isPicked(e));
				if (pi.isPicked(e))
					return pickedPaint;
				if (e.inNegativeCycle())
					return inNegativeCycle;
				return paintMap[e.getConstraintType().ordinal()];
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
	static final Function<Edge, Paint> edgeFillPaintFunction(final Paint normalPaint, final Paint contingentPaint, final Paint derivedPaint) {
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
