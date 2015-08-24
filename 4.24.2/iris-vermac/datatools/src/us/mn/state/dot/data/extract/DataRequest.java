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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import us.mn.state.dot.data.Constants;
import us.mn.state.dot.data.DataFactory;
import us.mn.state.dot.data.DataTool;
import us.mn.state.dot.data.PlotData;
import us.mn.state.dot.data.PlotDetector;
import us.mn.state.dot.data.Sensor;
import us.mn.state.dot.data.SystemConfig;
import us.mn.state.dot.data.TmsConfig;
/**
 * DataRequest is an encapsulation of all of the parameters necessary
 * to extract data from the traffic data archives.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class DataRequest implements Constants, Serializable {

	protected final DataFactory factory;

	/** Description of the Field */
	protected ProgressMonitor monitor;

	/** The types of data sets requested */
	protected Collection<String> dataSets = new TreeSet<String>();

	/** The dates requested */
	protected Collection<Calendar> dates = new Vector<Calendar>();

	/** The times requested */
	protected Collection<TimeRange> ranges = new Vector<TimeRange>();

	/** The sensors requested */
	protected Collection<String> sensorIds = new TreeSet<String>();

	/** The stations requested */
	protected Collection<String> stationIds = new Vector<String>();

	/** The FileFormat for the output */
	protected FileFormat format = null;

	/** The directory where the output file will be saved */
	protected File outputDir;

	/** The names of the files to be written */
	protected String[] fileNames;

	protected SimpleDateFormat formatter = new SimpleDateFormat( "yyyyMMdd" );

	protected Hashtable<String, DataSet> dataCache =
		new Hashtable<String, DataSet>();

	/** Create a <code>DataRequest</code> object. */
	public DataRequest() {
		this(null);
	}

	/** Create a <code>DataRequest</code> object. */
	public DataRequest(DataFactory fact) {
		factory = fact;
	}

	/**
	 * Sets the fileFormat of the output for this request
	 *
	 * @param f  The FileFormat of the output.
	 */
	public void setFileFormat( FileFormat f ) {
		format = f;
	}


	/**
	 * Sets the directory where the output file will be written.
	 *
	 * @param dir  The dir where the output file will be saved.
	 */
	public void setOutputDir(File dir) {
		if(dir == null || !dir.isDirectory()){
			return;
//			throw new IllegalArgumentException(
//					"Invalid output location.");
		}
		outputDir = dir;
	}


	/**
	 * Sets the name of the output data file.
	 *
	 * @param name  The file name.
	 */
	public void setFileName( String name ) {
		if ( name == null || name.trim().equals( "" ) ) {
			fileNames = null;
		} else {
			String[] names = {name};
			fileNames = names;
		}
	}


	/**
	 * Get a collection of data sets that are being requested.
	 *
	 * @return   The data set collection.
	 */
	public Collection<String> getDataSets() {
		return dataSets;
	}


	/**
	 * Get a collection of detector ids for which data has been requested.
	 *
	 * @return   The collection of detector ids.
	 */
	public Collection<String> getSensorIds() {
		return sensorIds;
	}


	/**
	 * Get a collection of dates for which data has been requested.
	 *
	 * @return   The collection of dates.
	 */
	public Collection<Calendar> getDates() {
		return dates;
	}


	/**
	 * Get a collection of TimeRanges for which data has been requested.
	 *
	 * @return   The collection of TimeRanges.
	 */
	public Collection<TimeRange> getTimeRanges() {
		return ranges;
	}


	/**
	 * Gets the fileFormat used for outputting the data.
	 *
	 * @return   The FileFormat.
	 */
	public FileFormat getFileFormat() {
		return format;
	}


	/**
	 * Gets the directory where the output file gets written.
	 *
	 * @return   The file directory.
	 */
	public File getOutputDir() {
		return outputDir;
	}

	/**
	 * Get the names of the files that the output is written to.
	 *
	 * @return   The file names.
	 */
	public String[] getFileNames() {
		if ( format instanceof CompositeFile ) return fileNames;
		if ( format instanceof CongestionFile ) return fileNames;
		if ( format instanceof DBFile ) return fileNames;
		if ( format instanceof LaneClosureFile ) return createDetFileNames();
		if ( format instanceof DetectorFile ) return createDetFileNames();
		if ( format instanceof DateFile ) return createDateFileNames();
		if ( format instanceof DataFile ) return createDataFileNames();
		return null;
	}


	/**
	 * Create a clone of this <code>DataRequest</code>.
	 *
	 * @return   An object which is an exact clone of this object.
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch ( CloneNotSupportedException cnse ) {
			return null;
		}
	}


	/** Clear the sensor ids requested. */
	public void clearSensorIds() {
		sensorIds.clear();
	}


	/** Clear the dates requested. */
	public void clearDates() {
		dates.clear();
	}


	/** Clear the data sets requested. */
	public void clearDataSets() {
		dataSets.clear();
	}


	/** Clear the time ranges requested. */
	public void clearTimeRanges() {
		ranges.clear();
	}

	/**
	 * Add a detector to the request.
	 *
	 * @param id  The detector id.
	 */
	public void addSensorId(String id) {
		sensorIds.add(id);
	}

	/**
	 * Remove a sensor from the request.
	 *
	 * @param id  The sensor id.
	 */
	public void removeSensorId(String id){
		sensorIds.remove(id);
	}


	/**
	 * Add a date to the request.
	 *
	 * @param c  The calendar representing the date.
	 */
	public void addDate( Calendar c ) {
		dates.add( c );
	}


	/**
	 * Remove a date from the request.
	 *
	 * @param c  The calendar representing the date.
	 */
	public void removeDate( Calendar c ) {
		dates.remove( c );
	}


	/**
	 * Add a data set to the request.
	 *
	 * @param set  The name of the data set.
	 */
	public void addDataSet( String set ) {
		dataSets.add( set );
	}


	/**
	 * Remove a data set from the request.
	 *
	 * @param set  The name of the data set to be removed.
	 */
	public void removeDataSet( String set ) {
		dataSets.remove( set );
	}


	/**
	 * Add a <code>TimeRange</code> to the request.
	 *
	 * @param range  The <code>TimeRange</code> to add.
	 */
	public void addTimeRange( TimeRange range ) {
		ranges.add( range );
	}


	/**
	 * Remove a <code>TimeRange</code> from the request.
	 *
	 * @param range  The <code>TimeRange</code> to be removed.
	 */
	public void removeTimeRange( TimeRange range ) {
		ranges.remove( range );
	}

	public void setSensorIds(String[] ids){
		clearSensorIds();
		for(int i=0; i<ids.length; i++){
			sensorIds.add(ids[i]);
		}
	}

	/**
	 * Create the file names to be used when writing a <code>DataFile</code>
	 *
	 * @return   The file names.
	 */
	private String[] createDataFileNames() {
		String[] names = new String[dataSets.size()];
		int i = 0;
		for(String set : dataSets) {
			if ( set != null ) {
				names[i++] = set;
			}
		}
		return names;
	}


	/**
	 * Create the file names to be used when writing a <code>DateFile</code>
	 *
	 * @return   The file names.
	 */
	private String[] createDateFileNames() {
		String[] names = new String[dates.size()];
		SimpleDateFormat formatter = new SimpleDateFormat( "yyyyMMdd" );
		String dateString = null;
		int i = 0;
		for(Calendar c : dates) {
			dateString = formatter.format( c.getTime() );
			names[i++] = dateString;
		}
		return names;
	}


	/**
	 * Create the file names to be used when writing a <code>DetectorFile</code>
	 *
	 * @return   The file names.
	 */
	private String[] createDetFileNames() {
		String[] names =
				new String[sensorIds.size()];
		int i = 0;
		for(String id : sensorIds){
			id = id.toUpperCase();
			if(!id.startsWith("S")){
				names[i++] = "Detector_" + id;
			}else{
				names[i++] = "Station_" + id;
			}
		}
		return names;
	}

	public void setTimeRanges(TimeRange[] array){
		clearTimeRanges();
		for(int i=0; i<array.length; i++){
			addTimeRange(array[i]);
		}
	}

	public void setDataSets(String[] array){
		dataSets.clear();
		for(int i=0; i<array.length; i++){
			dataSets.add(array[i]);
		}
	}

	public void setDates(Calendar[] array){
		dates.clear();
		for(int i=0; i<array.length; i++){
			dates.add(array[i]);
		}
	}

	/**
	 * Ensure that there is sufficient information to process
	 * the data request.
	 *
	 * @exception IllegalStateException  If the request is missing
	 * information necessary for processing.
	 */
	private void validate() throws IllegalStateException {
		if( getSensorIds().isEmpty()){
			throw new IllegalStateException( "No sensors selected." );
		}
		if ( getDates().isEmpty() ) {
			throw new IllegalStateException( "No dates selected." );
		}
		if ( getDataSets().isEmpty() ) {
			throw new IllegalStateException( "No data sets selected." );
		}
		if ( getTimeRanges().isEmpty() ) {
			throw new IllegalStateException( "No time ranges selected." );
		}
		if ( getOutputDir() == null ) {
			throw new IllegalStateException( "No output directory specified." );
		}
		if ( !getOutputDir().canWrite() ) {
			throw new IllegalStateException( "Unable to write to output directory." );
		}
		if ( format == null ) {
			throw new IllegalStateException( "No file format selected." );
		}
		if ( getFileNames() == null ) {
			throw new IllegalStateException( "No file name provided." );
		}
		if ( format.getDataElements().isEmpty() ) {
			throw new IllegalStateException( "No export options selected." );
		}
	}

	public DataSet getDataSet( Calendar c, String detId, String set ){
		if( dataCache.containsKey(
				detId + set + formatter.format( c.getTime() ) ) ){
			return (DataSet)dataCache.get(
					detId + set + formatter.format( c.getTime() ) );
		}else{
			return createDataSet( c, detId, set );
		}
	}

	public void process( DataTool application ){
		validate();
		monitor = new ProgressMonitor( application, "Data Extract Progress",
				"Writing file(s)...", 0, 1 );
		format.setDataRequest( this );
		String filePath = getOutputDir().getAbsolutePath();
		String[] fileNames = getFileNames();
		File file = null;
		PrintWriter writer = null;
		monitor.setMaximum( fileNames.length );
		Calendar[] calendars = (Calendar[])getDates().toArray( new Calendar[0] );
		for ( int i = 0; i < fileNames.length; i++ ) {
			monitor.setProgress( i );
			monitor.setNote( "Processing " + fileNames[i] + ".csv..." );
			try {
				file = new File( filePath + File.separator + fileNames[i] + ".csv" );
				FileOutputStream stream = new FileOutputStream( file );
				writer = new PrintWriter( stream );
				//FIXME: can't index both fileNames and calendars on i
				format.writeFile( fileNames[i], calendars[i], writer );
				writer.flush();
				writer.close();
				if ( format.transform() ) {
					format.transformFile( file );
				}
			} catch ( FileNotFoundException fnfe ) {
				JOptionPane.showMessageDialog( application,
					"Error writing file.\n" +
					"Check to make sure the file is not being accessed " +
					"by another application.\n Extraction cancelled." );
				return;
			} catch ( IllegalStateException ise ) {
				JOptionPane.showMessageDialog( application, ise.getMessage() );
				return;
			} catch ( Exception e ) {
				new ExtractExceptionDialog( e ).setVisible( true );
				return;
			} finally {
				monitor.close();
			}
		}
		monitor.close();
		javax.swing.JOptionPane.showMessageDialog( application, "Complete!" );
	}

	/**
	 * Description of the Method
	 *
	 * @param c        Description of Parameter
	 * @param detType  Description of Parameter
	 * @param id       Description of Parameter
	 * @param dataSet  Description of Parameter
	 * @return         Description of the Returned Value
	 */
	private DataSet createDataSet( Calendar c,
			String id, String dataSet ){
		DataSet set = null;
		try {
			PlotDetector plotDetector = null;
			id = id.toUpperCase();
			if(!id.startsWith("S")){
				plotDetector =
					PlotDetector.createPlotDetector( factory, id );
			}else{
				Sensor[] sensors = null;
				SystemConfig[] configs = factory.getConfigs();
				for(int i=0; i<configs.length; i++){
					if(configs[i] instanceof TmsConfig){
						TmsConfig tms = (TmsConfig)configs[i];
						if(tms.getName().equals("RTMC")){
							sensors = tms.getStationSensors(id);
							break;
						}
					}
				}
				plotDetector = PlotDetector.createStationDetector(
					factory, id, sensors );
			}
			PlotData plotData = new PlotData( c.getTime(), plotDetector );
			if ( dataSet.equals(VOLUME) ) {
				set = new DataSet.Volume( plotData );
			} else if ( dataSet.equals(OCCUPANCY) ) {
				set = new DataSet.Occupancy( plotData );
			} else if ( dataSet.equals(FLOW) ) {
				set = new DataSet.Flow( plotData );
			} else if ( dataSet.equals(SPEED) ) {
				set = new DataSet.Speed( plotData );
			} else if ( dataSet.equals(HEADWAY) ) {
				set = new DataSet.Headway( plotData );
			} else if ( dataSet.equals(DENSITY) ) {
				set = new DataSet.Density( plotData );
			}
		} catch ( IndexOutOfBoundsException iobe ) {
			// invalid detector id
			return null;
		} catch ( Exception e ) {
			new ExtractExceptionDialog( e ).setVisible( true );
		}
		dataCache.put( id + dataSet + formatter.format( c.getTime() ), set );
		return set;
	}
}

