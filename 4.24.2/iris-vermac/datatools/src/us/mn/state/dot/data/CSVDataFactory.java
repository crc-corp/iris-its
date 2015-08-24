/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;
import javax.jnlp.ServiceManager;

import us.mn.state.dot.data.plot.PlotSet;

/**
 * CSVDataFactory is a class for retrieving volume and occupancy data from a
 * comma separated value file.
 *
 * @author    Timothy A. Johnson
 */
public class CSVDataFactory extends DataFactory {

	/** An internal representation of a data type */
	private static int VOLUME = 1;

	/** An internal representation of a data type */
	static int OCCUPANCY = 2;

	/** State of CSV file acccess */
	public boolean FILE_OPENED = false;

	/** Holds each line of the file in a string */
	Vector<String> file = new Vector<String>();

	/** The file to read */
	private BufferedReader inFile = null;

	/** Contains float arrays of data */
	private Vector<float[]> rawVolumeData = new Vector<float[]>();

	/** Contains float arrays of data */
	private Vector<float[]> rawOccupancyData = new Vector<float[]>();

	/** The detectors in the plotset */
	private PlotDetector[] detectors;

	/** The detector indices (as strings) */
	private String[] detectorNames;

	/** The number of detectors found in the file */
	private int numberOfDetectors = 0;

	/** The yAxis's for the plots */
	private Axis[] yAxis;

	/** The xAxis for the plots */
	private Axis xAxis;

	/** The dates of each set in the plotset */
	private Calendar[] calendars;

	/** A suggested path to the file */
	private final String location;

	/** Get the location of the data factory */
	public String getLocation() {
		return location;
	}

	/** A suggested set of file types to display in open dialog */
	private String[] fileTypes = null;

	/** The field lengths of the detectors */
	private float[] fields;


	/**
	 * Constructor for the CSVDataFactory object
	 *
	 * @param loc            Default file location
	 * @param ft             Default filetype filter
	 * @exception Exception  Any unexpected exception
	 */
	public CSVDataFactory( String loc, String[] ft, SystemConfig[] cfgs ) throws Exception {
		super(cfgs);
		location = loc;
		fileTypes = ft;
		FileOpenService fileOpenService = ( FileOpenService ) ServiceManager.lookup( "javax.jnlp.FileOpenService" );
		FileContents dataFile = fileOpenService.openFileDialog( location, fileTypes );
		if ( dataFile != null ) {
			FILE_OPENED = true;
			InputStreamReader isReader = new InputStreamReader( dataFile.getInputStream() );
			inFile = new BufferedReader( isReader );
			processFile();
		}
	}


	/**
	 * Constructor for use when webstart is not used.
	 *
	 * @param loc            Default file location
	 * @param ft             Default filetype filter
	 * @exception Exception  Any unexpected exception
	 */
	private void processFile() {
		try {
			readEntireFile();
			readDetectorNames();
			numberOfDetectors = detectorNames.length;
			readCalendars();
			readFields();
			readDetectors();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		if ( calendars.length > 1 ) {
			if ( calendars[0] == calendars[1] ) {
				Calendar tempCalendar = calendars[0];
				calendars = new Calendar[1];
				calendars[0] = tempCalendar;
			} else {
				PlotDetector tempDetector = detectors[0];
				detectors = new PlotDetector[1];
				detectors[0] = tempDetector;
			}
		}
		for ( int i = 0; i < numberOfDetectors; i++ ) {
			rawVolumeData.addElement( getRawData( VOLUME, i ) );
			rawOccupancyData.addElement( getRawData( OCCUPANCY, i ) );
		}
	}


	/**
	 * Constructor for use when webstart is not used.
	 *
	 * @param br   The <code>BufferedReader</code> used to input the data
	 * @param cfgs The <code>SystemConfig</code>'s
	 */
	public CSVDataFactory( BufferedReader br, SystemConfig[] cfgs ) {
		super(cfgs);
		location = "Unknown";
		inFile = br;
		processFile();
		FILE_OPENED = true;
	}


	/**
	 * Gets the calendars to be added to the plotset
	 *
	 * @return   The calendars
	 */
	public Calendar[] getCalendars() {
		return calendars;
	}


	/**
	 * Gets the detectors to be added to the plotset
	 *
	 * @return   The detectors
	 */
	public PlotDetector[] getDetectors() {
		return detectors;
	}


	/**
	 * Gets the xAxis to use in plotting
	 *
	 * @return    The xAxis
	 */
	public Axis getXAxis( ) {
		readXAxis( );
		return xAxis;
	}


	/**
	 * Gets the yAxis attribute of the CSVDataFactory object
	 *
	 * @return    The yAxes
	 */
	public Axis[] getYAxis( ) {
		readYAxis( );
		return yAxis;
	}


	/**
	 * Gets the fields lengths of the detectors
	 *
	 * @return   The fields lengths
	 */
	public float[] getFields() {
		return fields;
	}


	/**
	 * Get the detectorNames
	 *
	 * @return   The detectorNames
	 */
	public String[] getDetectorNames() {
		return detectorNames;
	}


	/**
	 * Get 24 hours of volume data for each detector in the file
	 *
	 * @param c                Calendar (Date) of the data
	 * @param det              Detector
	 * @return                 24 hour raw volume data
	 * @exception IOException  Throws IOException if an error is encountered while reading the file
	 */
	public float[] getRawVolumeData( Calendar c, int det ) throws IOException {
		int desiredSet = -1;
		for ( int i = 0; i < numberOfDetectors; i++ ) {
			if ( det == ( Integer.parseInt( detectorNames[i] ) ) &&
					c.getTime().compareTo( calendars[i].getTime() ) == 0 ) {
				desiredSet = i;
				break;
			}
		}
		return ( ( float[] ) rawVolumeData.elementAt( desiredSet ) );
	}


	/**
	 * Checks to see if data exists for a specified date
	 *
	 * @param c  The calendar (date) of the data requested
	 * @return   True if data exists, false otherwise
	 */
	public boolean isAvailable( Calendar c ) {
		boolean available = false;
		for ( int i = 0; i < calendars.length; i++ ) {
			if ( ( calendars[i].getTime() ).compareTo( c.getTime() ) == 0 ) {
				available = true;
				break;
			}
		}
		//return true;
		return available;
	}


	/**
	 * Get 24 hours of occupancy data for one detector
	 *
	 * @param c                Calendar (date) of requested data
	 * @param id               The detector ID
	 * @return                 24 hour raw occupancy data
	 * @exception IOException  Throws IOException if an error is encountered while reading the file
	 */
	public float[] getRawOccupancySet( Calendar c, String id ) throws IOException {
		int detectorIndex = -1;
		for ( int i = 0; i < numberOfDetectors; i++ ) {
			if ( Integer.parseInt(id) == ( Integer.parseInt( detectorNames[i] ) ) &&
					c.getTime().compareTo( calendars[i].getTime() ) == 0 ) {
				detectorIndex = i;
				break;
			}
		}
		return ( float[] ) rawOccupancyData.elementAt( detectorIndex );
	}


	/**
	 * Reads the calendars into memory
	 *
	 * @exception Exception  Any exception
	 */
	private void readCalendars() throws Exception {
		int lineNumber = 2;
		String line = ( String ) file.elementAt( lineNumber );
		StringTokenizer tokenizer = new StringTokenizer( line, ",", false );
		Vector<String> uniqueDates = new Vector<String>();
		int tokenCount = tokenizer.countTokens();
		String token = tokenizer.nextToken();
		for ( int i = 1; i < tokenCount - 1; i++ ) {
			token = tokenizer.nextToken();
			if ( uniqueDates.indexOf( token ) == -1 ) {
				uniqueDates.addElement( token );
			}
		}
		Calendar[] cals = new Calendar[uniqueDates.size()];
		SimpleDateFormat dateFormat = new SimpleDateFormat( "MM/dd/yyyy" );
		for ( int i = 0; i < ( cals.length ); i++ ) {
			Calendar c = Calendar.getInstance();
			c.setTime( dateFormat.parse( ( String ) ( uniqueDates.elementAt( i ) ) ) );
			cals[i] = c;
		}
		calendars = cals;
	}


	/**
	 * Reads detectors into memory
	 *
	 * @exception Exception  Any exception
	 */
	private void readDetectors() throws Exception {
		int singleDetectorCount = detectorNames.length;
		for ( int i = 0; i < singleDetectorCount; i++ ) {
			try {
				new SingleDetector( this, detectorNames[i], fields[i] );
			} catch ( Exception e ) {
				singleDetectorCount -= 1;
			}
		}
		PlotDetector[] dets = new PlotDetector[singleDetectorCount];
		for ( int i = 0; i < detectorNames.length; i++ ) {
			dets[i] = new SingleDetector( this, detectorNames[i], fields[i] );
		}
		detectors = dets;
	}


	/** Reads the fields into memory */
	private void readFields() {
		float[] f = new float[numberOfDetectors];
		int lineNumber = 4;
		String line = ( String ) file.elementAt( lineNumber );
		StringTokenizer tokenizer = new StringTokenizer( line, ",", false );
		String token = tokenizer.nextToken();
		for ( int i = 0; i < numberOfDetectors; i++ ) {
			token = tokenizer.nextToken();
			token = tokenizer.nextToken();
			f[i] = Float.parseFloat( token );
		}
		fields = f;
	}


	/**
	 * Create a detector data object
	 *
	 * @param detId  The detector ID
	 * @return       DetectorData for this detector
	 */
	public DetectorData createDetectorData( String detId ) {
		return new Data( detId );
	}


	/**
	 * Get the raw data for a detector
	 *
	 * @param dataType       Type of data requested
	 * @param detectorIndex  Detector number
	 * @return               24 hours of "dataType" data
	 */
	private float[] getRawData( int dataType, int detectorIndex ) {
		float[] data = new float[2880];
		int lineNumber = 5;
		String line = ( String ) file.elementAt( lineNumber );
		for ( int i = 0; i < data.length; i++ ) {
			StringTokenizer tokenizer = new StringTokenizer( line, ",", false );
			String token = null;
			for ( int y = 0; y <= ( 2 * detectorIndex + dataType ); y++ ) {
				token = tokenizer.nextToken();
			}
			data[i] = Float.parseFloat( token );
			try {
				line = ( String ) file.elementAt( ++lineNumber );
			} catch ( ArrayIndexOutOfBoundsException e ) {
				break;
			}
		}
		return data;
	}


	/**
	 * Get raw volume data
	 *
	 * @param cal  Calendar (date) of data requested
	 * @param det  Detector number
	 * @return     24 hours of volume data
	 */
	private float[] getRawVolumeSet( Calendar cal, String id ) {
		int detectorIndex = -1;
		for ( int i = 0; i < numberOfDetectors; i++ ) {
			if ( Integer.parseInt(id) == ( Integer.parseInt( detectorNames[i] ) ) &&
					cal.getTime().compareTo( calendars[i].getTime() ) == 0 ) {
				detectorIndex = i;
				break;
			}
		}
		return ( float[] ) rawVolumeData.elementAt( detectorIndex );
	}


	/**
	 * Read the entire file into memory
	 *
	 * @exception Exception  An Exception encountered while reading the file into memory
	 */
	private void readEntireFile() throws Exception {
		String line = inFile.readLine();
		while ( line != null ) {
			file.addElement( line );
			line = inFile.readLine();
		}
		inFile.close();
	}


	/**
	 * Read the xAxis into memory
	 */
	private void readXAxis( ) {
		DateSelection s = new DateSelection( this );
		PlotSet ps = new PlotSet( s );
		int lineNumber = 0;
		String line = ( String ) file.elementAt( lineNumber );
		StringTokenizer tokenizer = new StringTokenizer( line, ",", false );
		String token = tokenizer.nextToken();
		token = tokenizer.nextToken();
		if ( token.compareTo( "Time" ) == 0 ) {
			xAxis = new Axis.Time();
		} else if ( token.compareTo( "Flow" ) == 0 ) {
			xAxis = new Axis.Flow( ps );
		} else if ( token.compareTo( "Headway" ) == 0 ) {
			xAxis = new Axis.Headway( ps );
		} else if ( token.compareTo( "Density" ) == 0 ) {
			xAxis = new Axis.Density( ps );
		} else if ( token.compareTo( "Speed" ) == 0 ) {
			xAxis = new Axis.Speed( ps );
		}// else if ( token.compareTo( "Capacity" ) == 0 ) {
		//	xAxis = new Axis.Capacity( ps );
		//}
	}


	/**
	 * Read the yAxes into memory
	 */
	private void readYAxis( ) {
		DateSelection s = new DateSelection( this );
		PlotSet ps = new PlotSet( s );
		int lineNumber = 1;
		String line = ( String ) file.elementAt( lineNumber );
		StringTokenizer tokenizer = new StringTokenizer( line, ",", false );
		String[] yGraphs = new String[tokenizer.countTokens() - 1];
		yAxis = new Axis[yGraphs.length];
		tokenizer.nextToken();
		for ( int i = 0; i < yGraphs.length; i++ ) {
			yGraphs[i] = tokenizer.nextToken();
			if ( ( yGraphs[i].compareTo( "Time" ) == 0 ) || ( yGraphs[i].compareTo( "time" ) == 0 ) ) {
				yAxis[i] = new Axis.Time();
			} else if ( ( yGraphs[i].compareTo( "Flow" ) == 0 ) || ( yGraphs[i].compareTo( "flow" ) == 0 ) ) {
				yAxis[i] = new Axis.Flow( ps );
			} else if ( ( yGraphs[i].compareTo( "Headway" ) == 0 ) || ( yGraphs[i].compareTo( "headway" ) == 0 ) ) {
				yAxis[i] = new Axis.Headway( ps );
			} else if ( ( yGraphs[i].compareTo( "Density" ) == 0 ) || ( yGraphs[i].compareTo( "density" ) == 0 ) ) {
				yAxis[i] = new Axis.Density( ps );
			} else if ( ( yGraphs[i].compareTo( "Speed" ) == 0 ) || ( yGraphs[i].compareTo( "speed" ) == 0 ) ) {
				yAxis[i] = new Axis.Speed( ps );
			} //else if ( ( yGraphs[i].compareTo( "Capacity" ) == 0 ) || ( yGraphs[i].compareTo( "capacity" ) == 0 ) ) {
			//	yAxis[i] = new Axis.Capacity( ps );
			//}
		}
	}


	/** Read the detector names into memory */
	private void readDetectorNames() {
		int detectorCount = -1;
		int lineNumber = 3;
		String line = ( String ) file.elementAt( lineNumber );
		StringTokenizer tokenizer = new StringTokenizer( line, ",", false );
		detectorCount = ( tokenizer.countTokens() - 1 ) / 2;
		String[] names = new String[detectorCount];
		tokenizer.nextToken();
		for ( int i = 0; i < ( names.length ); i++ ) {
			StringBuffer buffer = new StringBuffer( tokenizer.nextToken() );
			buffer.replace( 0, 4, "" );
			names[i] = buffer.toString();
			tokenizer.nextToken();
		}
		detectorNames = names;
	}


	/**
	 * Local detector data class
	 *
	 * @author    Timothy.A.Johnson
	 */
	protected class Data implements DetectorData {

		/** Detector id */
		protected final String id;

		/** Average field length */
		protected float fieldLength;

		/** The detector label */
		protected final String label;

		/**
		 * Constructor for data object
		 *
		 * @param id  The detector ID
		 */
		protected Data( String id ) {
			this.id = id;
			fieldLength = DEFAULT_FIELD_LENGTH;
			label = null;
		}


		/**
		 * Get the data label
		 *
		 * @return          The label value
		 */
		public String getLabel() {
			return label;
		}


		/**
		 * Get the average field length
		 *
		 * @return   The averager fieldLength
		 */
		public float getFieldLength() {
			return fieldLength;
		}


		/**
		 * Get all volume data for the specified day
		 *
		 * @param c                Calendar (date) of requested data
		 * @return                 24 hours of volume data
		 * @exception IOException  An exception
		 */
		public float[] getVolumeSet( Calendar c ) throws IOException {
			return getRawVolumeSet( c, id );
		}


		/**
		 * Get all occupancy data for the specified day
		 *
		 * @param c                Calendar (date) of the requested data
		 * @return                 24 hours of occupancy data
		 * @exception IOException  An exception
		 */
		public float[] getOccupancySet( Calendar c ) throws IOException {
			return getRawOccupancySet( c, id );
		}
		public float[] getSpeedSet( Calendar c ) throws IOException {
			// unless the way a CSV data file is updated, raw speeds
			// are not available.
			return createEmptyDataSet();
		}
	}
}
