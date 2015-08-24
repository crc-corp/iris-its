/*
 * DataExtract
 * Copyright (C) 2002-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.data.plot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import us.mn.state.dot.data.Axis;
import us.mn.state.dot.data.CSVDataFactory;
import us.mn.state.dot.data.DataFactory;
import us.mn.state.dot.data.DateSelection;
import us.mn.state.dot.data.PlotDetector;
import us.mn.state.dot.data.SingleDetector;

/**
 * DataPlot
 *
 * @author    Douglas Lau
 */
public class Plotlet extends JPanel {

	/** Font to draw labels */
	protected final static Font FONT =
			new Font( "SansSerif", Font.PLAIN, 18 );

	/** Font to draw sample smoothing */
	protected final static Font SMALL_FONT =
			new Font( "SansSerif", Font.PLAIN, 10 );

	/** Axis count */
	private final static int AXIS_COUNT = 7;

	/** Data factory */
	protected final DataFactory factory;

	/** Date selection component */
	protected final DateSelection selection;

	/** Plot set */
	protected final PlotSet plot;

	/** Time axis */
	protected final Axis.Time time = new Axis.Time();

	/** Axis object array */
	protected final Axis[] axis = new Axis[AXIS_COUNT];

	private final Axis xAxis = time;

	private final Axis yAxis;

	/** Button grid for selecting graph types */
	protected final AbstractButton[][] buttonGrid =
			new AbstractButton[AXIS_COUNT][2];

	/** Graph panel */
	protected GraphPanel graph;

	/** Legend scroll pane */
	protected JScrollPane scroll;


	/**
	 * Create a data plot window in a container
	 *
	 * @param f  The dataFactory used for all data
	 */
	public Plotlet(DataFactory f) {
		factory = f;
		selection = new DateSelection( factory );
		plot = new PlotSet( selection );
		if ( factory instanceof CSVDataFactory ) {
			PlotDetector[] detectors =
				( ( CSVDataFactory ) factory ).getDetectors();
			for ( int i = 0; i < detectors.length; i++ ) {
				plot.detectorAdded( detectors[i] );
			}
			Calendar[] calendars =
				( ( CSVDataFactory ) factory ).getCalendars();
			for ( int i = 0; i < calendars.length; i++ ) {
				selection.preselectDate( calendars[i] );
			}
		} else {
			selection.preselectDate( Calendar.getInstance() );
		}
		yAxis = new Axis.Headway( plot );
		axis[0] = time;
		axis[1] = new Axis.Flow( plot );
		axis[2] = new Axis.Headway( plot );
		axis[3] = new Axis.Occupancy( plot );
		axis[4] = new Axis.Density( plot );
		axis[5] = new Axis.Speed( plot );
		setLayout( new BorderLayout() );
		Box c = createPlotBox();
		add( c, BorderLayout.SOUTH );
		createNewGraph();
	}

	/** Add a detector to the plot */
	public void addDetector(String id) {
		try {
			PlotDetector det = new SingleDetector(factory, id);
			plot.detectorAdded(det);
		} catch(InstantiationException ie) {
			// Ignore this?
		}
	}

	/**
	 * Set the size of the legend table
	 *
	 * @param table  The new legendSize value
	 */
	protected void setLegendSize( JTable table ) {
		Dimension dim = table.getPreferredSize();
		dim.height += 16;
		scroll.setPreferredSize( dim );
		revalidate();
		scroll.repaint();
	}


	/**
	 * Report which x axis has been selected to be plotted
	 *
	 * @return   The xAxis value
	 */
	protected Axis getXAxis() {
		return xAxis;
	}


	/**
	 * Report which y axes have been selected to be plotted
	 *
	 * @return   The yAxis value
	 */
	protected Axis[] getYAxis() {
		return new Axis[]{ yAxis };
	}


	/**
	 * Create a panel which contains all of the plot controls
	 *
	 * @return   A box with all GUI tools
	 */
	protected Box createPlotBox() {
		Box box = Box.createHorizontalBox();
		box.add( Box.createHorizontalStrut( 4 ) );
		box.add( selection );
		box.add( Box.createHorizontalGlue() );
		box.add( createLegendBox() );
		box.add( Box.createHorizontalGlue() );
		return box;
	}


	/**
	 * Create a legend box
	 *
	 * @return   A legend of plots displayed
	 */
	protected Box createLegendBox() {
		JPanel smoothPanel = new JPanel( new FlowLayout() );
		smoothPanel.add( new JLabel( "Smoothing" ) );
		Box box = Box.createVerticalBox();
		final JComboBox smoothing =
				new JComboBox( time.getSmoothingModel() );
		smoothing.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					if ( !time.isChanging() ) {
						graph.redraw();
					}
				}
			} );
		smoothPanel.add( smoothing );
		box.add( smoothPanel );
		box.add( Box.createVerticalStrut( 2 ) );
		Box hBox = Box.createHorizontalBox();
		hBox.add( Box.createHorizontalGlue() );
		final JLabel label = new JLabel( plot.getTitle() );
		hBox.add( label );
		hBox.add( Box.createHorizontalGlue() );
		box.add( hBox );
		final JTable table = new LegendTable( plot );
		Action remove =
			new AbstractAction( "remove" ) {
				public void actionPerformed( ActionEvent e ) {
					if ( table.getSelectedColumn() != 0 ) {
						return;
					}
					int row = table.getSelectedRow();
					if ( row < 0 ) {
						return;
					}
					if ( plot.getColumnName().equals( "Detector" ) ) {
						plot.detectorRemoved( plot.getDetector( row ) );
					}
				}
			};
		table.getActionMap().put( remove.getValue( Action.NAME ),
				remove );
		table.getInputMap().put( KeyStroke.getKeyStroke( "DELETE" ),
				remove.getValue( Action.NAME ) );
		scroll = new JScrollPane( table );
		hBox = Box.createHorizontalBox();
		hBox.add( Box.createHorizontalGlue() );
		hBox.add( scroll );
		hBox.add( Box.createHorizontalGlue() );
		box.add( hBox );
		plot.addListener( ( PlotSet.Listener ) table.getModel() );
		plot.addListener(
			new PlotSet.Listener() {
				public void selectionChanged( PlotSet.Event e ) {
					label.setText( plot.getTitle() );
					setLegendSize( table );
				}
				public void plotChanged( PlotSet.Event e ) { }
			} );
		setLegendSize( table );
		box.add( Box.createVerticalGlue() );
		return box;
	}


	/**
	 * Print using the "PrinterJob" method of printing
	 */
	protected void doLocalPrinterJob() {
		final PrinterJob job = PrinterJob.getPrinterJob();
		job.setJobName( "Data Plot" );
		final Printable printable = new PlotJob();
		Thread t =
			new Thread() {
				public void run() {
					PageFormat format = job.pageDialog( job.defaultPage() );
					Book book = new Book();
					book.append( printable, format );
					job.setPageable( book );
					if ( !job.printDialog() ) {
						return;
					}
					try {
						job.print();
					} catch ( PrinterException e ) {
						e.printStackTrace();
					}
				}
			};
		t.start();
	}


	/**
	 * Print the whole page
	 *
	 * @param g     Graphics object to print
	 * @param page  A rectangle the size of the page
	 */
	protected void printPage( Graphics g, Rectangle page ) {
		g.setColor( Color.white );
		g.fillRect( page.x, page.y, page.width, page.height );
		printTitle( g, page );
		printLegend( g, page );
		graph.print( g, page );
	}


	/**
	 * Print the title at the top of the page
	 *
	 * @param g     Graphics object to print
	 * @param page  A rectangle the size of the page
	 */
	protected void printTitle( Graphics g, Rectangle page ) {
		String title = plot.getTitle();
		if ( graph.xAxis != time ) {
			title = title + "   " + time;
		}
		g.setFont( FONT );
		FontMetrics metrics = g.getFontMetrics();
		int width = metrics.stringWidth( title );
		int height = metrics.getHeight();
		int x = page.x + ( page.width - width ) / 2;
		int y = page.y + metrics.getMaxAscent();
		g.setColor( Color.black );
		g.drawString( title, x, y );
		page.y += height;
		page.height -= height;
	}


	/**
	 * Print the legend at the bottom of the page
	 *
	 * @param g     A graphics object to print
	 * @param page  A rectangle the size of the page
	 */
	protected void printLegend( Graphics g, Rectangle page ) {
		int width = scroll.getWidth() - 2;
		int height = scroll.getPreferredSize().height + 1;
		int x = page.x + page.width - width;
		int y = page.y + page.height - height - 1;
		g.translate( x, y );
		scroll.print( g );
		g.translate( -x, -y );
		g.setColor( Color.black );
		g.setFont( SMALL_FONT );
		FontMetrics metrics = g.getFontMetrics();
		String smoothing = "Smoothing: " +
				time.getSmoothingModel().getSelectedItem();
		width = metrics.stringWidth( smoothing );
		g.drawString( smoothing, x - width - 16, y + height / 2 );
		page.height -= height;
		page.width--;
	}


	/**
	 * Create a new graph
	 */
	protected void createNewGraph() {
		Axis xAxis = getXAxis();
		Axis[] yAxis = getYAxis();
		if ( xAxis == null || yAxis == null ) {
			return;
		}
		GraphPanel g = new GraphPanel( plot, xAxis, yAxis, time );
		if ( graph != null ) {
			remove( graph );
			graph.destroy();
		}
		graph = g;
		add( graph, BorderLayout.CENTER );
		revalidate();
	}

	/**
	 * Plot job for printing
	 *
	 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
	 */
	protected class PlotJob implements Printable {
		/**
		 * Description of the Method
		 *
		 * @param g       Description of Parameter
		 * @param format  Description of Parameter
		 * @param index   Description of Parameter
		 * @return        Description of the Returned Value
		 */
		public int print( Graphics g, PageFormat format, int index ) {
			if ( index != 0 ) {
				return NO_SUCH_PAGE;
			}
			int x = ( int ) format.getImageableX();
			int y = ( int ) format.getImageableY();
			int width = ( int ) format.getImageableWidth();
			int height = ( int ) format.getImageableHeight();
			Rectangle page = new Rectangle( x, y, width, height );
			printPage( g, page );
			return PAGE_EXISTS;
		}
	}
}
