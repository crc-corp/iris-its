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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Vector;

import us.mn.state.dot.data.Constants;


/**
 * A fileformat in which all of the data is presented in one file.
 * This format is used for producing the lane closure manual
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @version   $Revision: 1.13 $ $Date: 2005/08/17 20:28:18 $
 */
public class LaneClosureFile extends FileFormat {

	/** Create a new lane closure file */
	public LaneClosureFile() {
		super( "Lane Closure File" );
		Collection<String> c = new Vector<String>();
		c.add(OutputSelector.VALUES);
		outputOptions = c;
	}


	/**
	 * Write the data to the disk.  The file is always written
	 * with the time ranges in rows and can be transformed
	 * after the fact.
	 *
	 * @param writer     The printwriter used to write the data
	 */
	public void writeFile( String fileName, Calendar c, PrintWriter writer )
	{
		validateRequest();
//		id = id.substring( 2 );
		writer.println();
		writeColumnHeader( writer );
		writeRows( getId( fileName ), writer );
	}


	private void validateRequest()
			throws IllegalStateException
	{
		if( request.getTimeRanges().size() != 1 )
			throw new IllegalStateException(
				"You must select exactly one time range." );
		if( request.getDataSets().size() != 1 )
			throw new IllegalStateException(
				"You must select exactly one data set." );
	}


	/** Not used */
	protected void writeRows( Object values,
			PrintWriter writer )
	{}

	/**
	 * Write the row data to file.
	 *
	 * @param values  The days of the week in the order
	 * that the
	 * @param writer    The PrintWriter used to write the data.
	 */
	protected void writeRows( String detId,
			PrintWriter writer )
	{
		Collection ranges = request.getTimeRanges();
		TimeRange range = (TimeRange)( ( ranges.toArray() )[ 0 ] );
		int columnCount = range.getExtent() / range.getSmoothing();
		int[] days = {Calendar.MONDAY, Calendar.TUESDAY,
			Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY,
			Calendar.SATURDAY, Calendar.SUNDAY };
		for( int i=0; i<days.length; i++ ) {
			float[] data = getDayOfWeekMedians( days[i], detId );
			writer.print( " , ," );
			writer.print( getDayString( days[i] ) );
			if( data == null ) {
				for( int x=0; x<columnCount; x++ ) {
					writer.print( "," + Constants.NULL );
				}
			} else {
				for( int x=0; x<data.length; x++ ) {
					writer.print( "," + data[ x ] );
				}
			}
			writer.println();
		}
	}

	private float[] getDayOfWeekMedians( int day, String detectorId )
	{
		TimeRange range = (TimeRange)( request.getTimeRanges().toArray() )[ 0 ];
		float[] medianValues = new float[range.getExtent()/range.getSmoothing() ];
		for( int i=0; i<medianValues.length; i++ ){
			medianValues[ i ] = Constants.NULL;
		}
		DataSet[] sets = getDataSets( day, detectorId );
		float[][] smoothedArrays = getSmoothedArrays( sets );
		if( smoothedArrays.length == 0 ){
			return medianValues;
		}
		float[] rawValues = new float[ smoothedArrays.length ];
		for( int timeIndex=0; timeIndex<smoothedArrays[0].length; timeIndex++ ){
			for( int set=0; set<smoothedArrays.length; set++ ){
				rawValues[ set ] = smoothedArrays[ set ][ timeIndex ];
			}
			medianValues[ timeIndex ] = DataSet.median( rawValues );
			rawValues = new float[ smoothedArrays.length ];
		}
		return medianValues;
	}

	private float[][] getSmoothedArrays( DataSet[] sets ){
		TimeRange range = (TimeRange)( request.getTimeRanges().toArray() )[ 0 ];
		float[][] result = new float[ sets.length ][range.getExtent()/range.getSmoothing() ];
		for( int set=0; set<sets.length; set++ ){
			result[ set ] = sets[ set ].getSmoothedArray(
				range.getStart(), range.getExtent(),
				range.getSmoothing() );
		}
		return result;
	}
	
	private DataSet[] getDataSets( int day, String detId ){
		ArrayList<DataSet> dataSets = new ArrayList<DataSet>();
		String dataSet = ( (String)( request.getDataSets().toArray() )[ 0 ] );
		Collection<Calendar> dates = filterCalendars( day, request.getDates() );
		for(Calendar c : dates) {
			if( c.get( Calendar.DAY_OF_WEEK ) == day ){
				dataSets.add( request.getDataSet(
					c, detId, dataSet ) );
			}
		}
		return (DataSet[])dataSets.toArray( new DataSet[ 0 ] );
	}
	
	/** Get a subset of calendars where the day of week is
	* the same as the filterDay
	*/
	private Collection<Calendar> filterCalendars( int filterDay, Collection<Calendar> cals ) {
		Object[] calendars = cals.toArray();
		Collection<Calendar> filtered = new Vector<Calendar>();
		for( int i=0; i<cals.size(); i++ ) {
			if( ( (Calendar)calendars[ i ] ).get( Calendar.DAY_OF_WEEK ) == filterDay )
				filtered.add( (Calendar)calendars[ i ] );
		}
		return filtered;
	}

	private String getDayString( int day ) {
		String dayString = null;
		switch( day ) {
			case Calendar.SUNDAY:
				dayString = "Sunday";
				break;
			case Calendar.MONDAY:
				dayString = "Monday";
				break;
			case Calendar.TUESDAY:
				dayString = "Tuesday";
				break;
			case Calendar.WEDNESDAY:
				dayString = "Wednesday";
				break;
			case Calendar.THURSDAY:
				dayString = "Thursday";
				break;
			case Calendar.FRIDAY:
				dayString = "Friday";
				break;
			case Calendar.SATURDAY:
				dayString = "Saturday";
				break;
		}
		return dayString;
	}
}
