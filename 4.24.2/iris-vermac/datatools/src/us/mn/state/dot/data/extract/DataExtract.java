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
package us.mn.state.dot.data.extract;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;

import us.mn.state.dot.data.DataFactory;
import us.mn.state.dot.data.DataTool;

/**
 * DataSource
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class DataExtract extends DataTool {

	/** Property key for output file directory */
	protected final String PROP_OUT_DIR = "output_directory";

	/** Directory to find import files. */
	protected String PROP_IN_DIR = "";

	/** Directory for storing settings files */
	protected String PROP_SETTINGS_FILE_DIR = "settings_directory";

	/** FileSystemView */
	protected FileSystemView system;

	/** Properties used for defaulting selections */
	protected Properties properties;

	/** Property file which resides on the client machine */
	protected File propertyFile;

	/** Date selection component */
	protected DateSelector dateSelector;

	/** Detector selection component */
	protected DetectorSelector detectorSelector;

	/** Data set selection component */
	protected DataSetSelector dataSetSelector;

	/** Time frame selection component */
	protected TimeSelector timeSelector;

	/** Output format selection component */
	protected OutputSelector outputSelector;

	/** Data factory */
	protected final DataFactory factory;

	/** Main window container */
	protected final Container pane;

	/**
	 * Create a data extract application
	 *
	 * @param f             The dataFactory used for all data
	 */
	public DataExtract(DataFactory f) {
		super("DataExtract", f);
		pane = this.getContentPane();
		factory = f;
		JMenuBar bar = new JMenuBar();
		bar.add( createFileMenu() );
		bar.add( createHelpMenu() );
		setJMenuBar( bar );
		addWindowListener(
			new WindowAdapter() {
				public void windowClosing( WindowEvent e ) {
					System.exit( 0 );
				}
			} );
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds( 50, 25, screenSize.width - 100, screenSize.height - 100 );
		init();
		setVisible( true );
	}

	/**
	 * Set a property in the user's DataExtract properties file.
	 *
	 * @param key    The property to set.
	 * @param value  The property value.
	 */
	public void setProperty( String key, String value ) {
		properties.setProperty( key, value );
		writeProperties();
	}


	/**
	 * Gets a FileSystemView object
	 *
	 * @return   The fileSystemView value
	 */
	public FileSystemView getFileSystemView() {
		return system;
	}


	/**
	 * Get a DataExtract property value.
	 *
	 * @param key  The property.
	 * @return     The property value
	 */
	public String getProperty( String key ) {
		return properties.getProperty( key );
	}


	/** Open a file that contains a datarequest object. */
	public void openSettingsFile() {
		try {
			JFileChooser chooser = new JFileChooser(
					properties.getProperty( PROP_SETTINGS_FILE_DIR ), system );
			chooser.setDialogTitle( "Open Settings File..." );
			chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
			chooser.showDialog( this, "OK" );
			if ( chooser.getSelectedFile() != null ) {
				File f = chooser.getSelectedFile();
				DataRequestFile drf = new DataRequestFile( f.getAbsolutePath() );
				DataRequest r = drf.readData();
				setProperty(
						PROP_SETTINGS_FILE_DIR, f.getCanonicalPath() );
				updateSelectors( r );
			}
		} catch ( StreamCorruptedException sce ) {
			JOptionPane.showMessageDialog( this, "Invalid settings file." );
		} catch ( Exception e ) {
			e.printStackTrace();
			JOptionPane.showMessageDialog( this, "Incompatible settings file." );
		}
	}


	/** Save the current <code>DataRequest</code> object to disk. */
	public void saveAs() {
		try {
			JFileChooser chooser = new JFileChooser(
					properties.getProperty( PROP_SETTINGS_FILE_DIR ), system );
			chooser.setDialogTitle( "Save Settings As..." );
			chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
			chooser.showDialog( this, "OK" );
			if ( chooser.getSelectedFile() != null ) {
				File settingsFile = chooser.getSelectedFile();
				DataRequestFile drf = new DataRequestFile( settingsFile.getAbsolutePath() );
				DataRequest dr = createDataRequest();
				drf.writeData( dr );
				setProperty(
						PROP_SETTINGS_FILE_DIR, settingsFile.getCanonicalPath() );
			}
		} catch ( Exception e ) {
			new ExtractExceptionDialog( e ).setVisible( true );
		}
	}


	/**
	 * Create the file menu
	 *
	 * @return   The file menu.
	 */
	public JMenu createFileMenu() {
		JMenu file = new JMenu( "File" );
		Action extract =
			new AbstractAction() {
				public void actionPerformed( ActionEvent e ) {
					Thread extraction =
						new Thread() {
							public void run() {
								extractData();
							}
						};
					extraction.start();
				}
			};
		extract.putValue( Action.NAME, "Extract Data" );
		file.add( new JMenuItem( extract ) );
		Action saveAs =
			new AbstractAction() {
				public void actionPerformed( ActionEvent e ) {
					saveAs();
				}
			};
		saveAs.putValue( Action.NAME, "Save Settings As..." );
		file.add( new JMenuItem( saveAs ) );
		Action openSettingsFile =
			new AbstractAction() {
				public void actionPerformed( ActionEvent e ) {
					openSettingsFile();
				}
			};
		openSettingsFile.putValue( Action.NAME, "Open Settings File..." );
		file.add( new JMenuItem( openSettingsFile ) );
		Action importTraf =
			new AbstractAction() {
				public void actionPerformed( ActionEvent e ) {
					importTraf();
				}
			};
		importTraf.putValue( Action.NAME, "Import .traf file..." );
		file.add( new JMenuItem( importTraf ) );
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
	 * Get the index ( from 1 to 2880 ) that is represented by the time, t which is
	 * in the form hh:mm:ss
	 *
	 * @param t         The time, in the format, hh:mm:ss
	 * @return             The index of the sample
	 * @exception Exception  Throws a <code>NumberFormatException</code>
	 * if the string, t, is not properly formatted.
	 */
	protected int getSampleIndex( String t )
			 throws Exception {
		try {
			StringTokenizer tokenizer = new StringTokenizer( t, ":" );
			if ( tokenizer.countTokens() != 3 ) {
				throw new NumberFormatException( "hh:mm:ss format exception" );
			}
			int hours = Integer.parseInt( ( String ) ( tokenizer.nextElement() ) );
			int minutes = Integer.parseInt( ( String ) ( tokenizer.nextElement() ) );
			return ( hours * 120 + minutes * 2 );
		} catch ( Exception e ) {
			throw new NumberFormatException( "Invalid time format." );
		}
	}


	/** Initialize the widgets. */
	protected void init() {
		pane.setLayout( new GridBagLayout() );
		initProperties();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.BOTH;
		pane.add(createTopPanel(), c);
		pane.add(createBottomPanel(), c);
		validate();
	}
	
	protected JPanel createTopPanel(){
		JPanel p = new JPanel( new GridBagLayout() );
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		dateSelector = new DateSelector( factory, this );
		detectorSelector = new DetectorSelector(factory, this);
		dataSetSelector = new DataSetSelector( factory, this );
		c.weightx = 1;
		c.weighty = 1;
		p.add(detectorSelector, c);
		c.weightx = 0;
		p.add(dateSelector, c);
		p.add(dataSetSelector, c);
		return p;
	}	

	protected JPanel createBottomPanel(){
		JPanel p = new JPanel( new GridBagLayout() );
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		timeSelector = new TimeSelector( factory, this );
		outputSelector = new OutputSelector( factory, this );
		p.add(timeSelector, c);
		p.add(outputSelector, c);
		return p;
	}

	/**
	 * Update each of the selectors so that they are
	 * in sync with the current <code>DataRequest</code>
	 * object.
	 *
	 * @param r  The <code>DataRequest</code> object to
	 * synchronize the selectors with.
	 */
	protected void updateSelectors( DataRequest r ) {
		clearSelectors();
		for(String id : r.getSensorIds()){
			detectorSelector.select(id);
		}
		for(Calendar c : r.getDates()) {
			dateSelector.select(c);
		}
		for(String dataSet : r.getDataSets()) {
			dataSetSelector.select(dataSet);
		}
		for(TimeRange range : r.getTimeRanges()) {
			timeSelector.select(range);
		}
		outputSelector.setOutputDir(r.getOutputDir());
		FileFormat f = r.getFileFormat();
		if( f != null ) {
			outputSelector.setFileFormat( f );
			if ( f instanceof CompositeFile ||
					f instanceof CongestionFile ||
					f instanceof LaneClosureFile ) {
				if ( r.getFileNames() != null ) {
					outputSelector.setFileName( ( r.getFileNames() )[0] );
				}
			}
			for(String dataElement : f.getDataElements()) {
				outputSelector.selectOutputOption(dataElement);
			}
		}
	}


	/** Initialize the property values. */
	protected void initProperties() {
		system = FileSystemView.getFileSystemView();
		File homeDir = system.getHomeDirectory();
		if ( homeDir.getAbsolutePath().endsWith( "\\Desktop" ) ) {
			String path = homeDir.getAbsolutePath();
			path = path.substring( 0, path.indexOf( "\\Desktop" ) );
			homeDir = new File( path );
		}
		homeDir = new File( homeDir.getAbsolutePath() + File.separator +
				"DataExtract" );
		homeDir.mkdir();
		URL url = null;
		properties = new Properties();
		try {
			propertyFile = new File(
					homeDir.getAbsolutePath() + File.separator + "DataExtract.properties" );
			if ( propertyFile.exists() ) {
				url = propertyFile.toURI().toURL();
				properties.load( url.openStream() );
			}
		} catch ( Exception e ) {
			new ExtractExceptionDialog( e ).setVisible( true );
		}
		writeProperties();
	}


	/** Write a property to the users property file. */
	protected void writeProperties() {
		try {
			FileOutputStream oStream = new FileOutputStream( propertyFile );
			properties.store( oStream, "DataExtract Properties" );
			oStream.flush();
			oStream.close();
		} catch ( Exception e ) {
			new ExtractExceptionDialog( e ).setVisible( true );
		}
	}

	private DataRequest createDataRequest(){
		DataRequest dr = new DataRequest(factory);
		dr.setSensorIds(detectorSelector.getSensorIds());
		dr.setDates(dateSelector.getDates());
		dr.setDataSets(dataSetSelector.getDataSets());
		dr.setTimeRanges(timeSelector.getTimeRanges());
		dr.setOutputDir(outputSelector.getOutputDir());
		dr.setFileFormat(outputSelector.getFileFormat());
		dr.setFileName(outputSelector.getFileName());
		return dr;
	}

	/** Extract the data from the data server. */
	protected void extractData() {
		DataRequest dr = createDataRequest();
//		RequestProcessor p =
//			new RequestProcessor(configs, dr, factory, this);
		try {
			dr.process( this );
		} catch ( IllegalStateException ise ) {
			JOptionPane.showMessageDialog( this, ise.getMessage() );
		} catch ( Exception e ) {
			new ExtractExceptionDialog( e ).setVisible( true );
		}
	}


	/** Import the parameters of a *.traf file */
	protected void importTraf() {
		try {
			JFileChooser chooser = new JFileChooser(
					properties.getProperty( PROP_IN_DIR ) );
			chooser.setDialogTitle( "Import .traf file..." );
			chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
			chooser.showDialog( this, "OK" );
			if ( chooser.getSelectedFile() != null ) {
				DataRequest r = new DataRequest();
				updateSelectors( r );
				File file = chooser.getSelectedFile();
				FileReader fileReader = new FileReader( file );
				BufferedReader reader = new BufferedReader( fileReader );
				String line = reader.readLine();
				int start = 0;
				int end = 0;
				int smoothing = 0;
				while ( line != null ) {
					line = line.toLowerCase();
					if ( line.startsWith( "archive:" ) ) {
					} else if ( line.startsWith( "format:" ) ) {
						String format = line.substring( 7 ).trim();
						if ( format.equals( "wide" ) ) {
							outputSelector.setOrientation( TIMES, "Columns" );
						} else if ( format.equals( "tall" ) ) {
							outputSelector.setOrientation( TIMES, "Columns" );
						} else if ( format.equals( "timing" ) ) {
							throw new Exception( "Timing format not supported for import." );
						}
					} else if ( line.startsWith( "dates:" ) ) {
						SimpleDateFormat formatter =
								new SimpleDateFormat( "yyyyMMdd" );
						String s = line.substring( 6 ).trim();
						StringTokenizer tokenizer = new StringTokenizer( s );
						while ( tokenizer.hasMoreTokens() ) {
							Calendar c = Calendar.getInstance();
							Date d = formatter.parse( ( String ) ( tokenizer.nextElement() ) );
							if ( d != null ) {
								c.setTime( d );
								r.addDate( c );
							}
						}
					} else if ( line.startsWith( "start:" ) ) {
						start = getSampleIndex( line.substring( 6 ).trim() );
					} else if ( line.startsWith( "end:" ) ) {
						end = getSampleIndex( line.substring( 4 ).trim() );
					} else if ( line.startsWith( "smoothing:" ) ) {
						line = line.substring( 10 ).trim();
						smoothing = timeSelector.parseSmoothing( line );
					} else if ( line.startsWith( "detectors:" ) ) {
						line = line.substring( 10 ).trim();
						StringTokenizer t = new StringTokenizer( line );
						while ( t.hasMoreTokens() ) {
							r.addSensorId((String)(t.nextElement()));
						}
					} else if ( line.startsWith( "sets:" ) ) {
						line = line.substring( 5 ).trim();
						StringTokenizer t = new StringTokenizer( line );
						while ( t.hasMoreTokens() ) {
							String s = ( String ) ( t.nextElement() );
							r.addDataSet( s );
						}
					}
					line = reader.readLine();
				}
				r.addTimeRange( TimeRange.createTimeRange( start, end, smoothing ) );
				reader.close();
				updateSelectors( r );
			}
		} catch ( Exception e ) {
			new ExtractExceptionDialog( e ).setVisible( true );
		}
	}


	/** Clear all of the selectors. */
	private void clearSelectors() {
		detectorSelector.clear();
		dateSelector.clear();
		dataSetSelector.clear();
		timeSelector.clear();
		outputSelector.clear();
	}

}
