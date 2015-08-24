/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2012  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.map;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;

/**
 * A vector symbol is draws a Shape with a specific Style.
 *
 * @author Douglas Lau
 */
public class VectorSymbol implements Symbol {

	/** Style to draw symbol */
	public final Style style;

	/** Size of legend icon */
	private final int lsize;

	/** Shape to draw legend */
	protected final Shape lshape;

	/** Create a new vector symbol */
	public VectorSymbol(Style sty, Shape shp, int sz) {
		style = sty;
		lshape = shp;
		lsize = sz;
	}

	/** Create a new vector symbol */
	public VectorSymbol(Style sty, Shape shp) {
		this(sty, shp, 24);
	}

	/** Get the symbol label */
	public String getLabel() {
		return style.getLabel();
	}

	/** Draw the symbol */
	public void draw(Graphics2D g, Shape shp, Shape out, float scale) {
		if(style.fill_color != null) {
			g.setColor(style.fill_color);
			g.fill(shp);
		}
		if(style.outline != null) {
			g.setColor(style.outline.color);
			g.setStroke(style.outline.getStroke(scale));
			g.draw(out);
		}
	}

	/** Get the legend icon */
	public Icon getLegend() {
		return new LegendIcon();
	}

	/** Inner class for icon displayed on the legend */
	protected class LegendIcon implements Icon {

		/** Transform to draw the legend */
		protected final AffineTransform transform;

		/** Create a new legend icon */
		protected LegendIcon() {
			Rectangle2D b = lshape.getBounds2D();
			double x = b.getX() + b.getWidth() / 2;
			double y = b.getY() + b.getHeight() / 2;
			double scale = (lsize - 2) /
				Math.max(b.getWidth(), b.getHeight());
			transform = new AffineTransform();
			transform.translate(lsize / 2, lsize / 2);
			transform.scale(scale, -scale);
			transform.translate(-x, -y);
		}	

		/** Paint the icon onto the given component */
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D)g;
			AffineTransform t = g2.getTransform();
			g2.translate(x, y);
			g2.transform(transform);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
			draw(g2, lshape, lshape, 1);
			g2.setTransform(t);
		}

		/** Get the icon width */
		public int getIconWidth() {
			return lsize;
		}

		/** Get the icon height */
		public int getIconHeight() {
			return lsize;
		}
	}
}
