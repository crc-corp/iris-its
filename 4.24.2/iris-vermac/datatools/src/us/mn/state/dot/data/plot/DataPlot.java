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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Vector;

import javax.jnlp.PrintService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import us.mn.state.dot.data.Axis;
import us.mn.state.dot.data.CSVDataFactory;
import us.mn.state.dot.data.CompositeDetector;
import us.mn.state.dot.data.DataFactory;
import us.mn.state.dot.data.DataTool;
import us.mn.state.dot.data.DateSelection;
import us.mn.state.dot.data.PlotData;
import us.mn.state.dot.data.PlotDetector;
import us.mn.state.dot.data.SingleDetector;

/**
 * DataPlot
 *
 * @author    Douglas Lau
 */
public class DataPlot extends DataTool {

	/** Font to draw labels */
	protected final static Font FONT =
			new Font( "SansSerif", Font.PLAIN, 18 );

	/** Font to draw sample smoothing */
	protected final static Font SMALL_FONT =
			new Font( "SansSerif", Font.PLAIN, 10 );

	/** Number of open data plot frames */
	protected static int frames = 0;

	/** Axis count */
	private final static int AXIS_COUNT = 6;// 7 if capacity is included

	/** Data factory */
	protected final DataFactory factory;

	/** Date selection component */
	protected final DateSelection selection;

	/** Plot set */
	protected final PlotSet plot;

	/** Time axis */
	protected final Axis.Time time;

	/** Axis object array */
	protected final Axis[] axis = new Axis[AXIS_COUNT];

	/** Main window container pane */
	protected final Container pane;

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
	public DataPlot(DataFactory f) {
		super("DataPlot", f);
		pane = getContentPane();
		factory = f;
		selection = new DateSelection( factory );
		plot = new PlotSet( selection );
		if ( factory instanceof CSVDataFactory ) {
			PlotDetector[] detectors = ( ( CSVDataFactory ) factory ).getDetectors();
			for ( int i = 0; i < detectors.length; i++ ) {
				plot.detectorAdded( detectors[i] );
			}
			Calendar[] calendars = ( ( CSVDataFactory ) factory ).getCalendars();
			for ( int i = 0; i < calendars.length; i++ ) {
				selection.preselectDate( calendars[i] );
			}
		}
		time = new Axis.Time();
		axis[0] = time;
		axis[1] = new Axis.Flow( plot );
		axis[2] = new Axis.Headway( plot );
		axis[3] = new Axis.Occupancy( plot );
		axis[4] = new Axis.Density( plot );
		axis[5] = new Axis.Speed( plot );
//		axis[6] = new Axis.Capacity( plot );
		pane.setLayout( new BorderLayout() );
		Box c = createPlotBox();
		pane.add( c, BorderLayout.SOUTH );
		JMenuBar bar = new JMenuBar();
		bar.add( createFileMenu() );
		bar.add( createViewMenu() );
		bar.add( createHelpMenu() );
		setJMenuBar( bar );
		addWindowListener(
			new WindowAdapter() {
				public void windowClosing( WindowEvent e ) {
					closeFrame();
				}
			} );
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds( 50, 25, screenSize.width - 100, screenSize.height - 100 );
		setVisible( true );
		frames++;
	}

	/** Close one data plot frame */
	protected static void closeFrame() {
		frames--;
		if ( frames < 1 ) {
			System.exit( 0 );
		}
	}


	/**
	 * Allows DataPlot to handle WindowEvents
	 *
	 * @param e  WindowEvent
	 */
	public void doWindowEvent( WindowEvent e ) {
		this.processWindowEvent( e );
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
		pane.validate();
		scroll.repaint();
	}


	/**
	 * Report which x axis has been selected to be plotted
	 *
	 * @return   The xAxis value
	 */
	protected Axis getXAxis() {
		Axis xAxis = null;
		for ( int i = 0; i < AXIS_COUNT; i++ ) {
			if ( buttonGrid[i][0].isSelected() ) {
				xAxis = axis[i];
			}
		}
		return xAxis;
	}


	/**
	 * Report which y axes have been selected to be plotted
	 *
	 * @return   The yAxis value
	 */
	protected Axis[] getYAxis() {
		Axis[] yAxis = null;
		int y = 0;
		for ( int i = 0; i < AXIS_COUNT; i++ ) {
			if ( buttonGrid[i][1].isSelected() ) {
				y++;
			}
		}
		if ( y > 0 ) {
			yAxis = new Axis[y];
			y = 0;
			for ( int i = 0; i < AXIS_COUNT; i++ ) {
				if ( buttonGrid[i][1].isSelected() ) {
					yAxis[y++] = axis[i];
				}
			}
		}
		return yAxis;
	}


	/**
	 * Create the file menu
	 *
	 * @return   The file menu for DataPlot
	 */
	protected JMenu createFileMenu() {
		JMenu file = new JMenu( "File" );
		Action newWindow =
			new AbstractAction() {
				public void actionPerformed( ActionEvent e ) {
					new DataPlot(factory);
				}
			};
		newWindow.putValue( Action.NAME, "New" );
		file.add( new JMenuItem( newWindow ) );
		Action importData =
			new AbstractAction() {
				public void actionPerformed( ActionEvent e ) {
					String[] fileTypes = {"csv", "txt"};
					importFile( "C:\\temp", fileTypes );
				}
			};
		importData.putValue( Action.NAME, "Import" );
		file.add( new JMenuItem( importData ) );
		Action exportData =
			new AbstractAction() {
				public void actionPerformed( ActionEvent e ) {
					String[] fileTypes = {"csv"};
					exportFile( "C:\\temp", fileTypes );
				}
			};
		exportData.putValue( Action.NAME, "Export" );
		file.add( new JMenuItem( exportData ) );
		file.addSeparator();
		Action print =
			new AbstractAction() {
				public void actionPerformed( ActionEvent e ) {
					doLocalPrinterJob();
				}
			};
		print.putValue( Action.NAME, "Print" );
		file.add( new JMenuItem( print ) );
		file.addSeparator();
		final JFrame frame = this;
		Action closeWindow =
			new AbstractAction() {
				public void actionPerformed( ActionEvent e ) {
					doWindowEvent( new WindowEvent( frame,
							WindowEvent.WINDOW_CLOSING ) );
				}
			};
		closeWindow.putValue( Action.NAME, "Close" );
		file.add( new JMenuItem( closeWindow ) );
		Action quit =
			new AbstractAction() {
				public void actionPerformed( ActionEvent e ) {
					System.exit( 0 );
				}
			};
		quit.putValue( Action.NAME, "Quit" );
		file.add( new JMenuItem( quit ) );
		return file;
	}


	/**
	 * Create the view menu
	 *
	 * @return   The view menu for DataPlot
	 */
	protected JMenu createViewMenu() {
		JMenu view = new JMenu( "View" );
		JMenu xView = new JMenu( "X-Axis" );
		view.add( xView );
		JMenu yView = new JMenu( "Y-Axis" );
		view.add( yView );
		/*
		 *  final JFrame snapShot = new JFrame();
		 *  Action capturePlot = new AbstractAction() {
		 *  public void actionPerformed( ActionEvent e ) {
		 *  snapShot.setSize(graph.getSize());
		 *  snapShot.setResizable(false);
		 *  JScrollPane captureScroll = new JScrollPane((GraphPanel)graph.clone());
		 *  snapShot.getContentPane().add(captureScroll);
		 *  snapShot.show();
		 *  }
		 *  };
		 *  capturePlot.putValue( Action.NAME, "Capture" );
		 *  view.add( new JMenuItem( capturePlot ) );
		 */
		ButtonGroup group = new ButtonGroup();
		for ( int i = 0; i < AXIS_COUNT; i++ ) {
			final int b = i;
			buttonGrid[b][0] =
					new JRadioButtonMenuItem( axis[i].getName() );
			group.add( buttonGrid[b][0] );
			xView.add( buttonGrid[b][0] );
			buttonGrid[b][1] =
					new JCheckBoxMenuItem( axis[i].getName() );
			yView.add( buttonGrid[b][1] );
			buttonGrid[b][0].addItemListener(
				new ItemListener() {
					public void itemStateChanged( ItemEvent e ) {
						if ( e.getStateChange() == ItemEvent.SELECTED ) {
							buttonGrid[b][1].setEnabled( false );
							createNewGraph();
						} else {
							buttonGrid[b][1].setEnabled( true );
						}
					}
				} );
			buttonGrid[b][1].addItemListener(
				new ItemListener() {
					public void itemStateChanged( ItemEvent e ) {
						if ( e.getStateChange() == ItemEvent.SELECTED ) {
							buttonGrid[b][0].setEnabled( false );
						} else {
							buttonGrid[b][0].setEnabled( true );
						}
						createNewGraph();
					}
				} );
		}
		buttonGrid[1][1].setSelected( true );
		buttonGrid[0][0].setSelected( true );
		return view;
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
		Box vBox = Box.createVerticalBox();
		vBox.add( Box.createVerticalStrut( 6 ) );
		vBox.add( box );
		vBox.add( Box.createVerticalStrut( 6 ) );
		vBox.add( createDetectorBox() );
		return vBox;
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
		return box;
	}


	/**
	 * Print using the JNLP method of printing
	 */
	protected void doRemotePrinterJob() {
		try {
			final PrintService ps = ( PrintService ) ServiceManager.lookup( "javax.jnlp.PrintService" );
			Thread t =
				new Thread() {
					public void run() {
						Printable printable = new PlotJob();
						PageFormat format = ps.showPageFormatDialog( new PageFormat() );
						Book book = new Book();
						book.append( printable, format );
						ps.print( book );
					}
				};
			t.start();
		} catch ( UnavailableServiceException e ) {
			e.printStackTrace();
		}
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
			pane.remove( graph );
			graph.destroy();
		}
		graph = g;
		pane.add( graph, BorderLayout.CENTER );
		pane.validate();
	}


	/**
	 * Create a detector entry box
	 *
	 * @return   A box for entering detector indices
	 */
	protected Box createDetectorBox() {
		Box box = Box.createHorizontalBox();
		box.add( Box.createHorizontalStrut( 4 ) );
		JLabel label = new JLabel( "Enter detector(s):" );
		box.add( label );
		box.add( Box.createHorizontalStrut( 4 ) );
		final JTextField det = new JTextField( 40 );
		final JTextField field = new JTextField( 10 );
		ActionListener listener =
			new ActionListener() {
				public void actionPerformed( ActionEvent ev ) {
					String s = field.getText();
					PlotDetector pd = null;
					try {
						float f = Float.parseFloat( s );
						try{
							pd = new SingleDetector( factory,
									det.getText(), f );
						}catch(Exception e){
							System.out.println( e.getMessage() );
						}
					} catch ( NumberFormatException e ) {
						try {
							pd = PlotDetector.createPlotDetector(
									factory, det.getText() );
						} catch ( Exception ee ) {
							System.out.println( ee.getMessage() );
						}
					}
					if ( pd != null ) {
						plot.detectorAdded( pd );
					}
					det.setText( "" );
					field.setText( "" );
					det.requestFocus();
				}
			};
		det.addActionListener( listener );
		field.addActionListener( listener );
		box.add( det );
		box.add( Box.createHorizontalStrut( 4 ) );
		box.add( new JLabel( "Average field length:" ) );
		box.add( Box.createHorizontalStrut( 4 ) );
		box.add( field );
		box.add( Box.createHorizontalStrut( 4 ) );
		JButton clearDetectors = new JButton( "Clear Detectors" );
		clearDetectors.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					plot.detectorsCleared();
					det.requestFocus();
				}
			} );
		box.add( clearDetectors );
		box.add( Box.createHorizontalStrut( 4 ) );
		JButton clearDates = new JButton( "Clear Dates" );
		clearDates.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					selection.clearAll();
				}
			} );
		box.add( clearDates );
		box.add( Box.createHorizontalStrut( 4 ) );
		JButton refresh = new JButton( "Refresh" );
		refresh.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					plot.refresh();
				}
			} );
		box.add( refresh );
		box.add( Box.createHorizontalStrut( 4 ) );
		return box;
	}


	/**
	 * Import detector information from a text file
	 *
	 * @param location   Default location of file
	 * @param fileTypes  Default filetype filters
	 */
	private void importFile( String location, String[] fileTypes ) {
		CSVDataFactory fileFactory = null;
		try {
			//FIXME pass a set of SystemConfigs to the CSVFactory
			fileFactory = new CSVDataFactory( location, fileTypes, null );
		} catch ( UnavailableServiceException e ) {
			FileDialog dialog = new FileDialog( this, "File Open", FileDialog.LOAD );
			dialog.setVisible(true);
			String file = dialog.getFile();
			String directory = dialog.getDirectory();
			try {
				FileReader fileReader = new FileReader( directory + file );
				BufferedReader bufRdr = new BufferedReader( fileReader );
				//FIXME pass a set of SystemConfigs to the CSVFactory
				fileFactory = new CSVDataFactory( bufRdr, null );
			} catch ( FileNotFoundException fnf ) {
				fnf.printStackTrace();
			}
		} catch ( Exception e ) {
			JOptionPane.showMessageDialog( this, "File may be corrupt.  Import cannot proceed." );
			return;
		}
		if ( fileFactory.FILE_OPENED ) {
			new DataPlot(fileFactory);
		}
	}


	/**
	 * Export a set of data to a text file
	 *
	 * @param location   Default location of file
	 * @param fileTypes  Default filetype filters
	 */
	private void exportFile( String location, String[] fileTypes ) {

		try {
			ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();
			PrintWriter pr = new PrintWriter( bOutStream );
			printData( pr );
			ServiceManager.lookup( "javax.jnlp.FileSaveService" );
		} catch ( UnavailableServiceException use ) {
			try {
				FileDialog dialog = new FileDialog( this, "Export to file...", FileDialog.SAVE );
				dialog.setVisible(true);
				String fileName = dialog.getFile();
				String directory = dialog.getDirectory();
				FileOutputStream fOutStream = new FileOutputStream( directory + fileName );
				PrintWriter pr = new PrintWriter( fOutStream );
				printData( pr );
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}


	/**
	 * Print the data to the output stream
	 *
	 * @param pr  The printWriter that does the printing
	 */
	private void printData( PrintWriter pr ) {
		int plotCount = plot.getPlotCount();
		int compositeDetectorPlotCount = 0;
		int singleDetectorPlotCount = 0;
		for ( int i = 0; i < plotCount; i++ ) {
			if ( plot.getPlotData( i ).getDetector() instanceof CompositeDetector ) {
				compositeDetectorPlotCount = compositeDetectorPlotCount + 1;
			} else if ( plot.getPlotData( i ).getDetector() instanceof SingleDetector ) {
				singleDetectorPlotCount = singleDetectorPlotCount + 1;
			}
		}
		if ( plotCount == 0 ) {
			// abort export (no plots to export)
			JOptionPane.showMessageDialog( this, "There are no plots.  File export aborted." );
			return;
		} else if ( singleDetectorPlotCount == 0 ) {
			// abort export (no singledetector plots to export)
			JOptionPane.showMessageDialog( this, "Composite detectors cannot be exported.  There are no valid plots to export.  File export aborted." );
			return;
		} else if ( compositeDetectorPlotCount > 0 ) {
			// verify export of partial information
			int answer = JOptionPane.showConfirmDialog( this,
					"Composite detectors cannot be exported.  Continue?", "Alert", JOptionPane.YES_NO_OPTION );
			if ( answer == JOptionPane.NO_OPTION ) {
				return;
			}
		}
		Vector<Calendar> calendars = new Vector<Calendar>();
		Vector<PlotDetector> detectors = new Vector<PlotDetector>();
		Vector<float[]> volumeData = new Vector<float[]>();
		Vector occupancyData = new Vector();
		float[] fields = new float[singleDetectorPlotCount];
		Vector<String> detIndices = new Vector<String>();
		try {
			int exportNumber = -1;
			// array index number of valid detectors
			for ( int i = 0; i < plotCount; i++ ) {
				PlotData plotData = plot.getPlotData( i );
				PlotDetector plotDetector = plotData.getDetector();
				if ( plotDetector instanceof SingleDetector ) {
					exportNumber += 1;
					calendars.addElement( plotData.getCalendar() );
					detectors.addElement( plotDetector );
					fields[exportNumber] = plotDetector.getField();
					StringBuffer sb = new StringBuffer( plotDetector.toString() );
					int index = sb.toString().indexOf( " - " );
					if ( index != -1 ) {
						sb.setLength( index );
					}
					String detNumber = sb.toString();
					detIndices.addElement( detNumber );
					volumeData.addElement( plotDetector.getRawVolumeSet( plotData.getCalendar() ) );
					occupancyData.addElement( plotDetector.getRawOccupancySet( plotData.getCalendar() ) );
				}
			}
			PrintWriter printWriter = pr;
			String xaxis = getXAxis().getName();
			Axis[] yAxis = getYAxis();
			printWriter.println( "xaxis," + xaxis );
			printWriter.print( "yaxis" );
			for ( int i = 0; i < yAxis.length; i++ ) {
				printWriter.print( "," + yAxis[i].getName() );
			}
			printWriter.println();
			printWriter.print( "Date" );
			for ( int i = 0; i < singleDetectorPlotCount; i++ ) {
				Calendar c = ( Calendar ) calendars.elementAt( i );
				String dateString =
					( c.get( Calendar.MONTH ) + 1 ) + "/" +
					  c.get( Calendar.DAY_OF_MONTH ) + "/" +
					  c.get( Calendar.YEAR );
				printWriter.print( "," + dateString );
				printWriter.print( "," + dateString );
			}
			printWriter.println();
			printWriter.print( "Detectors" );
			for ( int i = 0; i < singleDetectorPlotCount; i++ ) {
				printWriter.print( ",Vol " + ( detIndices.elementAt( i ) ) );
				printWriter.print( ",Occ " + ( detIndices.elementAt( i ) ) );
			}
			printWriter.println();
			printWriter.print( "Field" );
			for ( int i = 0; i < singleDetectorPlotCount; i++ ) {
				printWriter.print( "," + fields[i] );
				printWriter.print( "," + fields[i] );
			}
			printWriter.println();
			boolean odd = true;
			for ( int i = 0; i < 2880; i++ ) {
				int hours = ( i + 1 ) / 120;
				int minutes = ( ( i + 1 ) - ( hours * 120 ) ) / 2;
				String seconds;
				if ( odd ) {
					seconds = "30";
				} else {
					seconds = "00";
				}
				odd = !odd;
				printWriter.print( hours + ":" + minutes + ":" + seconds );
				for ( int plotIndex = 0; plotIndex < singleDetectorPlotCount; plotIndex++ ) {
					float[] volume = ( float[] ) ( volumeData.elementAt( plotIndex ) );
					float[] occupancy = ( float[] ) ( occupancyData.elementAt( plotIndex ) );
					printWriter.print( "," + volume[i] );
					printWriter.print( "," + occupancy[i] );
				}
				printWriter.println();
			}
			printWriter.flush();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
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
