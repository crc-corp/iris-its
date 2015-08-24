/*
 * DataExtract
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.BoundedRangeModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import us.mn.state.dot.data.Axis;

/**
 * GraphPlot: A class for plotting data graphs.
 *
 * @author Douglas Lau
 */
public final class GraphPlot extends JPanel implements PlotSet.Listener {

	/** Inner class for a static thread the paint graphs */
	static protected final class PaintThread extends Thread {

		/** Queue of jobs for this thread to perform */
		protected final LinkedList<GraphPlot> queue =
			new LinkedList<GraphPlot>();

		/** Add a job to the queue */
		protected synchronized void add( GraphPlot plot ) {
			if( !queue.contains( plot ) )
				queue.addLast( plot );
			notify();
		}

		/** Get the next job from the queue (and remove it) */
		protected synchronized GraphPlot next() {
			while( queue.isEmpty() ) {
				try { wait(); }
				catch( InterruptedException e ) { e.printStackTrace(); }
			}
			return (GraphPlot)queue.removeFirst();
		}

		/** Painter thread's run method */
		public void run() {
			while( true ) {
				GraphPlot plot = next();
				plot.updateImage();
			}
		}

		/** Create a new paint thread */
		protected PaintThread() {
			setDaemon( true );
			start();
		}
	}

	/** Thread which paints the graphs in the background */
	static protected final PaintThread painter = new PaintThread();

	/** Line width for stroke settings */
	static protected final float LINE_WIDTH = 1;

	/** Dot length for stroke dash patterns */
	static protected final float DOT = 0.5f;

	/** Dash length for stroke dash patterns */
	static protected final float DASH = 2.5f;

	/** Spacing between dashes/dots on stroke dash patterns */
	static protected final float SPACE = 3.5f;

	/** Solid line stroke */
	static protected final Stroke SOLID_STROKE =
		new BasicStroke( LINE_WIDTH, BasicStroke.CAP_ROUND,
		BasicStroke.JOIN_ROUND );

	/** Dot line stroke */
	static protected final Stroke DOT_STROKE =
		new BasicStroke( LINE_WIDTH, BasicStroke.CAP_ROUND,
		BasicStroke.JOIN_ROUND, 1.0f, new float[] { DOT, SPACE }, 0.0f );

	/** Dash line stroke */
	static protected final Stroke DASH_STROKE =
		new BasicStroke( LINE_WIDTH, BasicStroke.CAP_ROUND,
		BasicStroke.JOIN_ROUND, 1.0f, new float[] { DASH, SPACE }, 0.0f );

	/** Dash-dot line stroke */
	static protected final Stroke DASH_DOT_STROKE =
		new BasicStroke( LINE_WIDTH, BasicStroke.CAP_ROUND,
		BasicStroke.JOIN_ROUND, 1.0f, new float[] { DASH, SPACE, DOT,
		SPACE }, 0.0f );

	/** Dash-dot-dot line stroke */
	static protected final Stroke DASH_DOT_DOT_STROKE =
		new BasicStroke( LINE_WIDTH, BasicStroke.CAP_ROUND,
		BasicStroke.JOIN_ROUND, 1.0f, new float[] { DASH, SPACE, DOT,
		SPACE, DOT, SPACE }, 0.0f );

	/** Dash-dash-dot line stroke */
	static protected final Stroke DASH_DASH_DOT_STROKE =
		new BasicStroke( LINE_WIDTH, BasicStroke.CAP_ROUND,
		BasicStroke.JOIN_ROUND, 1.0f, new float[] { DASH, SPACE, DASH,
		SPACE, DOT, SPACE }, 0.0f );

	/** Strong stroke for the grid lines */
	static protected final Stroke STRONG_STROKE = new BasicStroke( 0.4f );

	/** Weak stroke for the grid lines */
	static protected final Stroke WEAK_STROKE = new BasicStroke( 0.15f );

	/** Maximum image size (in pixels) */
	static protected final long MAX_IMAGE_SIZE = 6000000L;

	/** Time axis */
	protected final Axis.Time time;

	/** X-axis to plot */
	protected final Axis xAxis;

	/** Y-axis to plot */
	protected final Axis yAxis;

	/** Popup menu */
	protected final JPopupMenu popup = new JPopupMenu();

	/** Image of whole graph */
	protected Image image = null;

	/** Plot set */
	protected final PlotSet plot;

	/** Flag to connect the points */
	protected boolean connect = true;


	/** Create a new graph component */
	public GraphPlot( PlotSet p, Axis x, Axis y, Axis.Time t ) {
		setOpaque( true );
		plot = p;
		plot.addListener( this );
		xAxis = x;
		yAxis = y;
		time = t;
		final JCheckBoxMenuItem check =
			new JCheckBoxMenuItem( "Connect points", true );
		check.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				connect = check.getState();
				redraw();
			}
		} );
		popup.add( check );
		addMouseListener( new MouseAdapter() {
			public void mousePressed( MouseEvent e ) { testPopup( e ); }
			public void mouseReleased( MouseEvent e ) { testPopup( e ); }
			private void testPopup( MouseEvent e ) {
				if( e.isPopupTrigger() ) popup.show( e.getComponent(),
					e.getX(), e.getY() );
			}
		} );
		addComponentListener( new ComponentAdapter() {
			public void componentResized( ComponentEvent e ) {
				redraw();
			}
		} );
	}

	/** Destroy this component */
	public synchronized void destroy() {
		removeAll();
		plot.removeListener( this );
	}

	/** Called when the plot selection is changed */
	public void selectionChanged( PlotSet.Event e ) {}

	/** Called when the plot set is changed */
	public void plotChanged( PlotSet.Event e ) {
		redraw();
	}

	/** Update the backing image */
	protected synchronized void updateImage() {
		int width = getWidth();
		int height = getHeight();
		if ( width == 0 || height == 0 ) {
			return;
		}
		while( width * height > MAX_IMAGE_SIZE ) {
			width /= 2;
			if( width * height > MAX_IMAGE_SIZE ) height /= 2;
		}
		BufferedImage im = new BufferedImage( width, height,
			BufferedImage.TYPE_BYTE_INDEXED );
		Rectangle rect = new Rectangle( width, height );
		paintGraphics( im.createGraphics(), rect );
		image = im;
		repaint();
		setCursor( null );
	}

	/** Make the image with the full graph */
	public synchronized void redraw() {
		setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
		repaint();
		painter.add( this );
	}

	/** Print this graph onto a graphics context */
	public void printAt( Graphics g, int x, int y, int width,
		int height )
	{
		Shape clip = g.getClip();
		g.clipRect( x, y, width, height );
		g.translate( x, y );
		width = (int)( width / xAxis.getScale() );
		height = (int)( height / yAxis.getScale() );
		BoundedRangeModel model = xAxis.getModel();
		int xOff = width * model.getValue() / model.getMaximum();
		model = yAxis.getModel();
		int yOff = height * model.getValue() / model.getMaximum();
		g.translate( -xOff, -yOff );
		Rectangle view = new Rectangle( x, y, width, height );
		paintGraphics( (Graphics2D)g, view );
		g.translate( xOff, yOff );
		g.translate( -x, -y );
		g.setClip( clip );
	}

	/** Paint the graphics */
	protected void paintGraphics( Graphics2D g2, Rectangle rect ) {
		AffineTransform at = g2.getTransform();
		g2.setPaint( Color.white );
		g2.fill( rect );
		g2.scale( 1, -1 );
		g2.translate( 0, -rect.height );
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON );
		paintGridLines( g2, rect.width, rect.height );
		int start = 0;
		int extent = 2880;
		if( xAxis != time && yAxis != time ) {
			BoundedRangeModel range = time.getModel();
			start = range.getValue();
			extent = range.getExtent();
		}
		int smoothing = time.getSmoothing();
		for( int s = 0; s < plot.getPlotCount(); s++ ) {
			float x[] = xAxis.getSmoothedArray( start, extent, smoothing,
				s, rect.width );
			float y[] = yAxis.getSmoothedArray( start, extent, smoothing,
				s, rect.height );
			g2.setPaint( plot.getColor( s ) );
			if( connect ) {
				g2.setStroke( SOLID_STROKE );
				paintDataSet( g2, x, y );
				g2.setStroke( DASH_DOT_STROKE );
				paintDataGaps( g2, x, y );
			}
			else paintPointSet( g2, x, y );
		}
		g2.setStroke( SOLID_STROKE );
		g2.setTransform( at );
	}

	/** Paint the grid lines */
	protected void paintGridLines( Graphics2D g2, float width,
		float height )
	{
		g2.setPaint( Color.black );
		Line2D.Float line = new Line2D.Float();
		float delta = width / xAxis.getLines();
		for( int i = 0; i < xAxis.getLines(); i++ ) {
			if( xAxis.isLineStrong( i ) )
				g2.setStroke( STRONG_STROKE );
			else
				g2.setStroke( WEAK_STROKE );
			float x = i * delta;
			line.setLine( x, 0, x, height );
			g2.draw( line );
		}
		delta = height / yAxis.getLines();
		for( int i = 0; i <= yAxis.getLines(); i++ ) {
			if( yAxis.isLineStrong( i ) )
				g2.setStroke( STRONG_STROKE );
			else
				g2.setStroke( WEAK_STROKE );
			float y = i * delta;
			line.setLine( 0, y, width, y );
			g2.draw( line );
		}
	}

	/** Paint the data set as individual points */
	protected void paintPointSet( Graphics2D g2, float[] x, float[] y ) {
		Ellipse2D.Float circle = new Ellipse2D.Float();
		for( int i = 0; i < x.length; i++ ) {
			if( x[ i ] >= 0 && y[ i ] >= 0 ) {
				circle.setFrame( x[ i ], y[ i ], 3, 3 );
				g2.fill( circle );
			}
		}
	}

	/** Paint the data set */
	protected void paintDataSet( Graphics2D g2, float[] x, float[] y ) {
		GeneralPath path = new GeneralPath();
		boolean move = true;
		for( int i = 0; i < x.length; i++ ) {
			if( x[ i ] < 0 || y[ i ] < 0 ) {
				move = true;
				continue;
			}
			if( move ) {
				path.moveTo( x[ i ], y[ i ] );
				move = false;
			}
			else path.lineTo( x[ i ], y[ i ] );
		}
		g2.draw( path );
	}

	/** Paint the gaps in the data set */
	protected void paintDataGaps( Graphics2D g2, float[] x, float[] y ) {
		float x1 = 0;
		float y1 = 0;
		boolean move = false;
		boolean first = true;
		for( int i = 0; i < x.length; i++ ) {
			if( x[ i ] < 0 || y[ i ] < 0 ) {
				if( !first ) move = true;
				continue;
			}
			else if( move ) {
				g2.draw( new Line2D.Float( x1, y1, x[ i ], y[ i ] ) );
			}
			x1 = x[ i ];
			y1 = y[ i ];
			first = false;
		}
	}

	/** Paint the graph component */
	public void paintComponent( Graphics g ) {
		Graphics2D g2 = (Graphics2D)g;
		if( image != null )
			g2.drawImage( image, 0, 0, getWidth(), getHeight(), null );
		else {
			g2.setPaint( Color.white );
			g2.fill( g.getClip() );
		}
	}

	/** Get the preferred size of the graph */
	public Dimension getPreferredSize() {
		return getSize();
	}
}
