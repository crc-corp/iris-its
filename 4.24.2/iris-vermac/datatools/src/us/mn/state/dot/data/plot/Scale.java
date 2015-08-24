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
package us.mn.state.dot.data.plot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;

import us.mn.state.dot.data.Axis;

/**
  * Scale is a component which displays the scale of one graph axis
  *
  * @author Douglas Lau
  */
abstract public class Scale extends JComponent {

	/** Amount to reduce the clipping rectangle */
	static protected final int CLIP_SNIP = 4;

	/** Font to draw labels */
	static protected final Font BIG_FONT =
		new Font( "SansSerif", Font.PLAIN, 16 );

	/** Font to draw labels */
	static protected final Font SMALL_FONT =
		new Font( "SansSerif", Font.PLAIN, 12 );

	/** Font metrics information */
	protected final FontMetrics big_metrics =
		getFontMetrics( BIG_FONT );

	/** Font metrics information */
	protected final FontMetrics small_metrics =
		getFontMetrics( SMALL_FONT );

	/** Axis to plot */
	protected final Axis axis;

	/** Create a new scale component */
	protected Scale( Axis a ) {
		axis = a;
	}

	/** Print this scale onto a graphics context */
	abstract public void printAt( Graphics g, int x, int y, int width,
		int height );

	/** Horizontal scale (static inner class) */
	static public class Horizontal extends Scale {

		/** Create a new horizontal scale */
		public Horizontal( Axis a ) {
			super( a );
		}

		/** Get the preferred size of this component */
		public Dimension getPreferredSize() {
			return new Dimension( 0, big_metrics.getHeight() + 2 );
		}

		/** Paint this component */
		protected void paintComponent( Graphics g ) {
			Graphics2D g2 = (Graphics2D)g;
			paintGraphics( g2, getWidth(), getHeight() );
		}

		/** Paint the graphics */
		protected void paintGraphics( Graphics2D g2, int width,
			int height )
		{
			float space = (float)width / axis.getLines();
			g2.setPaint( Color.black );
			for( int i = 1; i < axis.getLines(); i++ ) {
				if( axis.isLineStrong( i ) ) g2.setFont( BIG_FONT );
				else g2.setFont( SMALL_FONT );
				String l = axis.getLineLabel( i );
				g2.drawString( l, i * space, height - 3 );
			}
		}
		
		/** Print this scale onto a graphics context */
		public void printAt( Graphics g, int x, int y, int width,
			int height )
		{
			Shape clip = g.getClip();
			g.clipRect( x, y, width, height );
			g.translate( x, y );
			width = (int)( width / axis.getScale() );
			BoundedRangeModel model = axis.getModel();
			int xOff = width * model.getValue() / model.getMaximum();
			g.translate( -xOff, 0 );
			paintGraphics( (Graphics2D)g, width, height );
			g.translate( xOff, 0 );
			g.translate( -x, -y );
			g.setClip( clip );
		}
	}

	/** Vertical scale (static inner class) */
	static public class Vertical extends Scale {

		/** Create a new vertical scale */
		public Vertical( Axis a ) {
			super( a );
		}

		/** Get the preferred size of this component */
		public Dimension getPreferredSize() {
			return new Dimension( big_metrics.stringWidth( "88888" ), 0 );
		}

		/** Paint this component */
		protected void paintComponent( Graphics g ) {
			Graphics2D g2 = (Graphics2D)g;
			paintGraphics( g2, getWidth(), getHeight() );
		}

		/** Paint the graphics */
		protected void paintGraphics( Graphics2D g2, int width,
			int height )
		{
			float space = (float)height / axis.getLines();
			g2.setPaint( Color.black );
			for( int i = 1; i < axis.getLines(); i++ ) {
				FontMetrics metrics;
				if( axis.isLineStrong( i ) ) {
					g2.setFont( BIG_FONT );
					metrics = big_metrics;
				}
				else {
					g2.setFont( SMALL_FONT );
					metrics = small_metrics;
				}
				String l = axis.getLineLabel( i );
				int x = width - metrics.stringWidth( l ) - 3;
				int y = height - (int)( i * space ) - 1;
				g2.drawString( l, x, y );
			}
		}

		/** Print this scale onto a graphics context */
		public void printAt( Graphics g, int x, int y, int width,
			int height )
		{
			Shape clip = g.getClip();
			g.clipRect( x, y + CLIP_SNIP, width, height - CLIP_SNIP );
			g.translate( x, y );
			height = (int)( height / axis.getScale() );
			BoundedRangeModel model = axis.getModel();
			int yOff = height * model.getValue() / model.getMaximum();
			g.translate( 0, -yOff );
			paintGraphics( (Graphics2D)g, width, height );
			g.translate( 0, yOff );
			g.translate( -x, -y );
			g.setClip( clip );
		}
	}
}
