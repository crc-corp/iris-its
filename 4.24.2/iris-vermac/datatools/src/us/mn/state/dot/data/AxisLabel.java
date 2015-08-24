/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;

/**
  * AxisLabel
  *
  * @author Douglas Lau
  */
abstract public class AxisLabel extends JComponent {

	/** Font to draw labels */
	static protected final Font BIG_FONT =
		new Font( "SansSerif", Font.PLAIN, 16 );

	/** Font to draw labels */
	static protected final Font SMALL_FONT =
		new Font( "SansSerif", Font.PLAIN, 12 );

	/** Label string */
	protected final String label;

	/** Font rendering context */
	protected final FontRenderContext context;

	/** Label glyph vector */
	protected final GlyphVector labelGlyph;

	/** Create a new axis label component */
	protected AxisLabel( String l, AffineTransform t ) {
		label = l;
		context = new FontRenderContext( t, true, true );
		labelGlyph = BIG_FONT.createGlyphVector( context, label );
	}

	/** Get the preferred size of the component */
	public Dimension getPreferredSize() {
		Rectangle r = labelGlyph.getVisualBounds().getBounds();
		return new Dimension( r.width, r.height + 4 );
	}

	/** Print the label into a graphics context */
	public void printAt( Graphics g, int x, int y, int width,
		int height )
	{
		g.translate( x, y );
		paintGraphics( (Graphics2D)g, width, height );
		g.translate( -x, -y );
	}

	/** Paint the component */
	abstract protected void paintGraphics( Graphics2D g2,
		int width, int height );

	/** Horizontal axis label */
	static public class Horizontal extends AxisLabel {

		/** Create a horizontal axis label */
		public Horizontal( String l, String u ) {
			super( l + " " + u, new AffineTransform() );
		}

		/** Paint the component */
		protected void paintComponent( Graphics g ) {
			Graphics2D g2 = (Graphics2D)g;
			paintGraphics( g2, getWidth(), getHeight() );
		}

		/** Paint the graphics */
		protected void paintGraphics( Graphics2D g2, int width,
			int height )
		{
			g2.setPaint( Color.black );
			FontMetrics metrics = g2.getFontMetrics( BIG_FONT );
			int w = metrics.stringWidth( label );
			g2.drawGlyphVector( labelGlyph, ( width - w ) / 2,
				metrics.getMaxAscent() );
		}
	}

	/** Vertical axis label */
	static public class Vertical extends AxisLabel {

		/** Units string */
		protected final String units;

		/** Unit glyph vector */
		protected final GlyphVector unitGlyph;

		/** Create a vertical axis label */
		public Vertical( String l, String u ) {
			super( l, new AffineTransform() );
			units = u;
			unitGlyph = SMALL_FONT.createGlyphVector( context, units );
		}

		/** Get the preferred size of the component */
		public Dimension getPreferredSize() {
			Rectangle r1 = labelGlyph.getVisualBounds().getBounds();
			Rectangle r2 = unitGlyph.getVisualBounds().getBounds();
			// the glyphs are not yet rotated so we swap x and y values
			return new Dimension( r1.height + r2.height + 6,
				r1.width + r2.width + 6 );
		}

		/** Paint the component */
		protected void paintComponent( Graphics g ) {
			Graphics2D g2 = (Graphics2D)g;
			paintGraphics( g2, getWidth(), getHeight() );
		}

		/** Paint the graphics */
		protected void paintGraphics( Graphics2D g2, int width,
			int height )
		{
			int containerHeight = height;
			int containerWidth = width;
			g2.setPaint( Color.black );
			FontMetrics m = g2.getFontMetrics();
			int fontHeight = m.getMaxAscent() + m.getMaxDescent();
			int stringLength = m.stringWidth( label );
			g2.rotate( -Math.PI / 2, 0.0f, 0.0f );
			int labelY = containerWidth / 2 - 2;
			int labelX = -( ( containerHeight + stringLength ) / 2 );
			int unitY = labelY + fontHeight;
			stringLength = m.stringWidth( units );
			int unitX = -( ( containerHeight + stringLength ) / 2 );
			g2.drawString( label, labelX, labelY );
			g2.drawString( units, unitX, unitY );
			g2.rotate( Math.PI / 2, 0.0f, 0.0f );
		}
	}
}
