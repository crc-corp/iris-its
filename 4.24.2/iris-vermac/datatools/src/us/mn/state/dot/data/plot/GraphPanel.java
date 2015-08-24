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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import us.mn.state.dot.data.Axis;
import us.mn.state.dot.data.AxisLabel;
import us.mn.state.dot.data.AxisScrollBar;

/**
  * GraphPanel: A class for drawing data graphs.
  *
  * @author Douglas Lau
  */
public final class GraphPanel extends JPanel implements Cloneable{

	/** Minimum size (one dimension) of a graph */
	static protected final int MINIMUM_SIZE = 100;

	/** Spacing between graphs on a print */
	static protected final int GRAPH_SPACING = 10;

	/** Small gap between components */
	static protected final int SMALL_GAP = 2;

	/** Icon for minus button. */
	protected final Icon minusIcon;

	/** Icon for plus button. */
	protected final Icon plusIcon;

	/** Time axis */
	protected final Axis.Time time;

	/** X-axis to plot */
	public final Axis xAxis;

	/** Label for X-axis */
	protected final AxisLabel label;

	/** Horizontal scale component */
	protected final Scale.Horizontal scale;

	/** Horizontal scale viewport */
	protected final JViewport scaleView;

	/** Axis scroll bar */
	protected final JScrollBar scrollBar;

	/** Bounded range model for this axis */
	protected final BoundedRangeModel model;

	/** Change listener for the axis bounded range model */
	protected final ChangeListener modelChangeListener;

	/** Minus button for X-axis */
	//protected final JButton minus = new JButton( "-" );
	protected final JButton minus = new JButton();

	/** Plus button for X-axis */
	//protected final JButton plus = new JButton( "+" );
	protected final JButton plus = new JButton();

	/** Y-axis to plot */
	protected final AxisComponents[] yAxis;

	/** Plotset used for cloning */
	protected final PlotSet ps;

	/** Grouping of all components associated with one y-axis */
	protected final class AxisComponents {

		/** Y-axis to plot */
		protected final Axis axis;

		/** Label for the axis */
		protected final AxisLabel label;

		/** Axis scale component */
		protected final Scale.Vertical scale;

		/** Axis scale viewport */
		protected final JViewport scaleView;

		/** Axis scroll bar */
		protected final JScrollBar scrollBar;

		/** Bounded range model for this axis */
		protected final BoundedRangeModel model;

		/** Change listener for the axis bounded range model */
		protected final ChangeListener modelChangeListener;

		/** Minus button */
		//protected final JButton minus = new JButton( "-" );
		protected final JButton minus = new JButton();

		/** Plus button */
		//protected final JButton plus = new JButton( "+" );
		protected final JButton plus = new JButton();

		/** Graph plot component */
		protected final GraphPlot graph;

		/** Graph plot viewport */
		protected final JViewport graphView;

		/** View position point */
		protected final Point point = new Point();

		/** Graph separator */
		protected final JSeparator separator = new JSeparator();

		/** Create a new set of components for an axis */
		protected AxisComponents( PlotSet plot, Axis y ) {
			axis = y;
			label = new AxisLabel.Vertical( axis.getName(),
				axis.getUnits() );
			add( label );
			scale = new Scale.Vertical( axis );
			scaleView = new AxisViewport();//JViewport();
//			scaleView.setScrollMode( JViewport.BLIT_SCROLL_MODE );
			scaleView.setView( scale );
			scaleView.addChangeListener( new ChangeListener() {
				public void stateChanged( ChangeEvent e ) {
					fixScaleView();
				}
			} );
			add( scaleView );
			graph = new GraphPlot( plot, xAxis, axis, time );
			graphView = new JViewport();
			graphView.setView( graph );
			add( graphView );
			graphView.addComponentListener( new ComponentAdapter() {
				public void componentResized( ComponentEvent e ) {
					recalculateAxis();
				}
			} );
			scrollBar = new AxisScrollBar( axis, JScrollBar.VERTICAL );
			model = scrollBar.getModel();
			modelChangeListener = new ChangeListener() {
				public void stateChanged( ChangeEvent e ) {
					recalculateAxis();
				}
			};
			model.addChangeListener( modelChangeListener );
			add( scrollBar );
			minus.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					axis.zoomOut();
				}
			} );
			minus.setIcon( minusIcon );
			minus.setMinimumSize( new Dimension( 25, 25 ) );
			add( minus );
			plus.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					axis.zoomIn();
				}
			} );
			plus.setIcon( plusIcon );
			plus.setMinimumSize( new Dimension( 25, 25 ) );
			add( plus );
			add( separator );
		}

		/** Recalculate the size of the graph and the scale */
		public void recalculateAxis() {
			Dimension extent = graphView.getExtentSize();
			extent.height = (int)( extent.height / axis.getScale() );
			extent.width = (int)( extent.width / xAxis.getScale() );
			if( extent.height < 1 || extent.width < 1 ) return;
			graphView.setViewSize( extent );
			extent.width = scale.getPreferredSize().width;
			scaleView.getView().setSize(extent);
			setYPosition();
		}

		/** Set the y-position of the graph and the scale */
		protected void setYPosition() {
			int height = graph.getHeight();
			point.y = height * model.getValue() / model.getMaximum();
			scaleView.setViewPosition( new Point( 0, point.y ) );
			graphView.setViewPosition( point );
		}

		/** Fix the size of the scale after it gets screwed up somehow */
		protected void fixScaleView() {
			if( scale.getHeight() != graph.getHeight() ) {
				Dimension extent = graphView.getViewSize();
				extent.width = scale.getPreferredSize().width;
				scaleView.getView().setSize( extent );
				setYPosition();
			}
		}

		/** Set the x-position of the view port */
		public void setPosition( int x ) {
			point.x = x;
			graphView.setViewPosition( point );
		}

		/** Destroy the axis components */
		protected void destroy() {
			graph.destroy();
			model.removeChangeListener( modelChangeListener );
		}
	}


	/** Create a new graph panel */
	public GraphPanel( PlotSet plot, Axis x, Axis[] y, Axis.Time t ) {
		super( false );
		minusIcon = new ImageIcon(
			getClass().getResource( "/images/ZoomOut24.gif" ) );
		plusIcon = new ImageIcon(
			getClass().getResource( "/images/ZoomIn24.gif" ) );
		ps = plot;
		setLayout( new Layout() );
		time = t;
		xAxis = x;
		label = new AxisLabel.Horizontal( xAxis.getName(),
			xAxis.getUnits() );
		add( label );
		scale = new Scale.Horizontal( xAxis );
		scaleView = new AxisViewport();//JViewport();
		scaleView.setView( scale );
		scaleView.addChangeListener( new ChangeListener() {
			public void stateChanged( ChangeEvent e ) {
				recalculate();
			}
		} );
		add( scaleView );
		minus.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				xAxis.zoomOut();
			}
		} );
		minus.setMinimumSize( new Dimension( 25, 25 ) );
		minus.setIcon( minusIcon );
		add( minus );
		plus.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				xAxis.zoomIn();
			}
		} );
		plus.setMinimumSize( new Dimension( 25, 25 ) );
		plus.setIcon( plusIcon );
		add( plus );
		scrollBar = new AxisScrollBar( xAxis, JScrollBar.HORIZONTAL );
		model = scrollBar.getModel();
		modelChangeListener = new ChangeListener() {
			public void stateChanged( ChangeEvent e ) {
				recalculate();
			}
		};
		model.addChangeListener( modelChangeListener );
		add( scrollBar );
		yAxis = new AxisComponents[ y.length ];
		for( int i = 0; i < y.length; i++ )
			yAxis[ i ] = new AxisComponents( plot, y[ i ] );
	}

	/** Redraw all the graphs */
	public void redraw() {
		for( int i = 0; i < yAxis.length; i++ ) {
			yAxis[ i ].graph.redraw();
		}
	}

	/** Destroy this component */
	public void destroy() {
		for( int y = 0; y < yAxis.length; y++ )
			yAxis[ y ].destroy();
		model.removeChangeListener( modelChangeListener );
		removeAll();
	}

	/** Set the position of the view port */
	protected void setPosition() {
		int x = scale.getWidth() * model.getValue() / model.getMaximum();
		scaleView.setViewPosition( new Point( x, 0 ) );
		for( int i = 0; i < yAxis.length; i++ )
			yAxis[ i ].setPosition( x );
	}

	/** Recalculate the size of the graph(s) */
	protected void recalculate() {
		Dimension extent = scaleView.getExtentSize();
		extent.height = scale.getPreferredSize().height;
		extent.width = (int)( extent.width / xAxis.getScale() );
		scaleView.getView().setSize(extent);
		for( int i = 0; i < yAxis.length; i++ )
			yAxis[ i ].recalculateAxis();
		setPosition();
	}

	/** Print the graph panel */
	public void print( Graphics g, Rectangle bounds ) {
		int[] width = computeWidths( bounds.width, true );
		int[] x = computeXPositions( bounds.x, width );
		int[] height = computeHeights( bounds.height, true );
		int[] y = computeYPositions( bounds.y, height );
		label.printAt( g, x[ 2 ], y[ 0 ], width[ 2 ], height[ 0 ] );
		scale.printAt( g, x[ 2 ], y[ 1 ], width[ 2 ], height[ 1 ] );
		int high = height[ 2 ] / yAxis.length - GRAPH_SPACING;
		for( int i = 0; i < yAxis.length; i++ ) {
			int top = y[ 2 ] + i * ( high + GRAPH_SPACING );
			yAxis[ i ].label.printAt( g, x[ 0 ], top, width[ 0 ], high );
			yAxis[ i ].scale.printAt( g, x[ 1 ], top, width[ 1 ], high );
			yAxis[ i ].graph.printAt( g, x[ 2 ], top, width[ 2 ], high );
			g.setColor( Color.black );
			g.drawRect( x[ 2 ], top, width[ 2 ], high );
		}
	}

	/** Compute the widths for all the components */
	protected int[] computeWidths( int avail, boolean print ) {
		int[] width = new int[ 5 ];
		for( int i = 0; i < yAxis.length; i++ ) {
			AxisComponents yac = yAxis[ i ];
			width[ 0 ] = Math.max( width[ 0 ],
				yac.label.getPreferredSize().width );
			width[ 1 ] = Math.max( width[ 1 ],
				yac.scale.getPreferredSize().width );
			if( !print ) {
				width[ 3 ] = Math.max( width[ 3 ],
					yac.scrollBar.getPreferredSize().width );
				width[ 4 ] = Math.max( width[ 4 ],
					yac.minus.getMinimumSize().width );
				width[ 4 ] = Math.max( width[ 4 ],
					yac.plus.getMinimumSize().width );
			}
		}
		for( int i = 0; i < 5; i++ )
			avail -= width[ i ];
		if( !print ) avail -= SMALL_GAP * 2;
		width[ 2 ] = Math.max( avail, MINIMUM_SIZE );
		return width;
	}

	/** Compute the x-positions of the components */
	static protected int[] computeXPositions( int margin, int[] width ) {
		int[] x = new int[ 5 ];
		x[ 0 ] = margin;
		x[ 1 ] = x[ 0 ] + width[ 0 ];
		x[ 2 ] = x[ 1 ] + width[ 1 ];
		x[ 3 ] = x[ 2 ] + width[ 2 ] + SMALL_GAP;
		x[ 4 ] = x[ 3 ] + width[ 3 ] + SMALL_GAP;
		return x;
	}

	/** Compute the heights for all the components */
	protected int[] computeHeights( int avail, boolean print ) {
		int[] height = new int[ 5 ];
		height[ 0 ] = label.getPreferredSize().height;
		height[ 1 ] = scale.getPreferredSize().height;
		if( !print ) {
			height[ 3 ] = scrollBar.getPreferredSize().height;
			height[ 4 ] = Math.max( minus.getMinimumSize().height,
				plus.getMinimumSize().height );
		}
		for( int i = 0; i < 5; i++ )
			avail -= height[ i ];
		if( !print ) avail -= SMALL_GAP;
		height[ 2 ] = Math.max( avail, MINIMUM_SIZE );
		return height;
	}

	/** Compute the y-positions of the components */
	static protected int[] computeYPositions( int margin, int[] height ) {
		int[] y = new int[ 5 ];
		y[ 0 ] = margin;
		y[ 1 ] = y[ 0 ] + height[ 0 ];
		y[ 2 ] = y[ 1 ] + height[ 1 ];
		y[ 3 ] = y[ 2 ] + height[ 2 ];
		y[ 4 ] = y[ 3 ] + height[ 3 ] + SMALL_GAP;
		return y;
	}

	/** Graph panel layout */
	protected final class Layout implements LayoutManager {

		/** Not used, since this is an inner class */
		public void addLayoutComponent( String name, Component comp ) {
		}

		/** Lay out the components on the graph panel */
		public void layoutContainer( Container parent ) {
			Dimension avail = getSize();
			int[] width = computeWidths( avail.width, false );
			int[] x = computeXPositions( 0, width );
			int[] height = computeHeights( avail.height, false );
			int[] y = computeYPositions( 0, height );

			// Place the components
			label.setBounds( x[ 2 ], y[ 0 ], width[ 2 ], height[ 0 ] );
			scaleView.setBounds( x[ 2 ], y[ 1 ], width[ 2 ], height[ 1 ] );
			scrollBar.setBounds( x[ 2 ], y[ 3 ], width[ 2 ], height[ 3 ] );
			minus.setBounds( x[ 2 ] + width[ 2 ] / 3, y[ 4 ],
				minus.getMinimumSize().width,
				minus.getMinimumSize().height );
			plus.setBounds( x[ 2 ] + width[ 2 ] * 2 / 3, y[ 4 ],
				plus.getMinimumSize().width,
				plus.getMinimumSize().height );
			int high = ( height[ 2 ] - 8 * ( yAxis.length - 1 ) ) /
				yAxis.length;
			for( int i = 0; i < yAxis.length; i++ ) {
				int top = y[ 2 ] + ( high + 8 ) * i;
				AxisComponents yac = yAxis[ i ];
				yac.label.setBounds( x[ 0 ], top, width[ 0 ], high );
				yac.scaleView.setBounds( x[ 1 ], top, width[ 1 ], high );
				yac.graphView.setBounds( x[ 2 ], top, width[ 2 ], high );
				yac.scrollBar.setBounds( x[ 3 ], top, width[ 3 ], high );
				yac.minus.setBounds( x[ 4 ],
					top + high / 3 - yac.minus.getMinimumSize().height / 2,
					yac.minus.getMinimumSize().width,
					yac.minus.getMinimumSize().height );
				yac.plus.setBounds( x[ 4 ], top + high * 2 / 3,
					yac.plus.getMinimumSize().width,
					yac.plus.getMinimumSize().height );
				int s = 4;
				if( i + 1 == yAxis.length ) s = 0;
				yac.separator.setBounds( x[ 0 ], top + high, getWidth(), s );
			}
		}

		/** Not used, since this is an inner class */
		public Dimension minimumLayoutSize( Container parent ) {
			return new Dimension( MINIMUM_SIZE, MINIMUM_SIZE );
		}

		/** Not used, since this is an inner class */
		public Dimension preferredLayoutSize( Container parent ) {
			return new Dimension( MINIMUM_SIZE, MINIMUM_SIZE );
		}

		/** Not used, since this is an inner class */
		public void removeLayoutComponent( Component comp ) {
		}
	}

	/** */
	public Object clone () {
		GraphPanel clonePanel;
		clonePanel = this;
		try {
			clonePanel = (GraphPanel)super.clone();
		}
		catch (Exception e) {
			clonePanel = null;
		}
		return clonePanel;
	}

	private final class AxisViewport extends JViewport {
		protected LayoutManager createLayoutManager() {
			return null;
		}
	}

}
