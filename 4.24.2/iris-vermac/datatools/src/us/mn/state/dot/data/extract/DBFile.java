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
package us.mn.state.dot.data.extract;

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;

import us.mn.state.dot.data.Axis;

/**
 * A file format in which all data is presented in one file.
 * The layout is like records in a database table.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @version   $Revision: 1.18 $ $Date: 2005/08/17 20:28:17 $
 */
public class DBFile extends FileFormat {

	protected PrintWriter writer = null;
	
	/** Constructor for the CompositeFile object */
	public DBFile() {
		super( "DB File" );
		Collection<String> c = new Vector<String>();
		c.add(OutputSelector.VALUES);
		outputOptions = c;
	}


	/**
	 * Description of the Method
	 *
	 * @param writer     Description of Parameter
	 */
	public void writeFile( String fn, Calendar c, PrintWriter writer ) {
		this.writer = writer;
		if(!dataElements.contains(OutputSelector.VALUES)) return;
		writer.println();
		writeColumnHeader( writer );
		writeRows( null, writer );
	}


	/**
	 * Description of the Method
	 *
	 * @param fieldValue  Description of Parameter
	 * @param writer      Description of Parameter
	 */
	public void writeRows( Object fieldValue,
			PrintWriter writer ) {
		for(String id : request.getSensorIds()) {
			writeSensorData(id = id.toUpperCase());
		}
	}
	
	private void writeSensorData(String id){
		for(Calendar c : request.getDates()) {
			writeDailyData(id, c);
		}
	}

	private void writeDailyData(String id, Calendar c){
		for(TimeRange range : request.getTimeRanges()) {
			writeRowData( writer, c, id, null, range );
		}
	}

	/**
	 * Description of the Method
	 *
	 * @param writer     Description of Parameter
	 */
	protected void writeColumnHeader( PrintWriter writer ) {
		writer.print("sensor,date,time,");
		for(String dataSet : request.getDataSets()) {
			writer.print(dataSet + ",");
		}
		writer.print("\n");
	}

	public boolean transform(){
		return false;
	}

	/**
	 * Description of the Method
	 *
	 * @param writer      The PrintWriter to write to
	 * @param c           The calendar representing the day to write
	 * @param detectorId  The ID of the detector
	 * @param dataSet     The name of the <code>DataSet</code>
	 * @param range       The <code>TimeRange</code> for which to write data
	 */
	protected void writeRowData( PrintWriter writer, Calendar c,
			String detectorId, String dataSet, TimeRange range ) {
		String d = formatter.format( c.getTime() );
		LinkedList<String> setNames = new LinkedList<String>(request.getDataSets());
		HashMap<String, DataSet> setsMap = new LinkedHashMap<String, DataSet>();
		HashMap<String, float[]> valuesMap = new LinkedHashMap<String, float[]>();
		int start = range.getStart();
		int extent = range.getExtent();
		int smoothing = range.getSmoothing();
		int numberSamples = NULL;
		for(String dataSetName : setNames){
			DataSet ds = request.getDataSet(c, detectorId, dataSetName);
			setsMap.put(dataSetName, ds);
			float[] values = ds.getSmoothedArray(start, extent, smoothing);
			valuesMap.put(dataSetName, values);
			numberSamples = values.length;
		}
		Axis.Time time = new Axis.Time();
		int currentTime = range.getStart();
		for(int i=0; i<numberSamples; i++){
			writer.print( detectorId + "," + d + "," );
			writer.print(time.formatTime(currentTime + smoothing) + "," );
			currentTime += smoothing;
			for(String dataSetName : setNames){
				writer.print(valuesMap.get(dataSetName)[i] + ",");
			}
			writer.print("\n");
		}
	}
}

