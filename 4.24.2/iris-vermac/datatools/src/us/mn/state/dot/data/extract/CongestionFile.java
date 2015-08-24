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
import java.util.Vector;

import us.mn.state.dot.data.Axis;

/**
 * A fileformat in which all of the data is presented in one file.
 * Detectors and time samples on one axis and Dates and
 * Data Sets on the opposing axis.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @version   $Revision: 1.13 $ $Date: 2005/08/17 20:28:17 $
 */
public class CongestionFile extends FileFormat {

	/** Create a new congestion file */
	public CongestionFile() {
		super( "Congestion File" );
		Collection<String> c = new Vector<String>();
		c.add(OutputSelector.DAY_MEDIAN);
		c.add(OutputSelector.VALUES);
		outputOptions = c;
	}


	/**
	 * Write the data to the disk.  The file is always written
	 * with the time ranges in rows.
	 *
	 * @param writer     The printwriter used to write the data
	 */
	public void writeFile( String fn, Calendar c, PrintWriter writer ) {
		writer.println();
		writeColumnHeader( writer );
		writeRows(request.getSensorIds(), writer );
	}


	/**
	 * Write the column headers to file.
	 *
	 * @param writer     The PrintWriter used to write the data.
	 */
	protected void writeColumnHeader( PrintWriter writer ) {
		Axis.Time time = new Axis.Time();
		writer.print( " , , ," );
		int currentTime = NULL;
		int endTime = NULL;
		int smoothing = NULL;
		int columnCount = 0;
		for(TimeRange range : request.getTimeRanges()) {
			int e = range.getExtent();
			int s = range.getSmoothing();
			columnCount += e / s;
			if ( dataElements.contains( OutputSelector.SUM ) ) {
				columnCount += 1;
			}
			if ( dataElements.contains( OutputSelector.AVERAGE ) ) {
				columnCount += 1;
			}
			if ( dataElements.contains( OutputSelector.TIME_MEDIAN ) ) {
				columnCount += 1;
			}
			if ( dataElements.contains( OutputSelector.SAMPLE ) ) {
				columnCount += 1;
			}
		}
		for(String id : request.getSensorIds()) {
			for ( int i = 0; i < columnCount; i++ ) {
				writer.print( id + "," );
			}
		}
		writer.println();
		writer.print( " , , ," );
		for(String id : request.getSensorIds()) {
			for(TimeRange range : request.getTimeRanges()) {
				currentTime = range.getStart();
				endTime = currentTime + range.getExtent();
				smoothing = range.getSmoothing();
				while ( currentTime < endTime ) {
					writer.print( time.formatTime( currentTime + smoothing ) + "," );
					currentTime += smoothing;
				}
				if ( dataElements.contains( OutputSelector.SUM ) ) {
					writer.print( OutputSelector.SUM + " (" + range.toString() + ")," );
				}
				if ( dataElements.contains( OutputSelector.AVERAGE ) ) {
					writer.print( OutputSelector.AVERAGE + " (" + range.toString() + ")," );
				}
				if ( dataElements.contains( OutputSelector.TIME_MEDIAN ) ) {
					writer.print( OutputSelector.TIME_MEDIAN + " (" + range.toString() + ")," );
				}
				if ( dataElements.contains( OutputSelector.SAMPLE ) ) {
					writer.print( OutputSelector.SAMPLE + " (" + range.toString() + ")," );
				}
			}
		}
		writer.println();
	}


	/**
	 * Write the row data to file.
	 *
	 * @param values  The collection of values
	 * @param writer    The PrintWriter used to write the data.
	 */
	protected void writeRows( Object values,
			PrintWriter writer ) {
		Collection<String> ids = ( Collection ) values;
		Collection<Calendar> dates = request.getDates();
		Collection<TimeRange> times = request.getTimeRanges();
		Collection<String> dataSets = request.getDataSets();
//		ProgressMonitor monitor = processor.getProgressMonitor();
//		monitor.setMaximum( ids.size() * dates.size() *
//				times.size() * dataSets.size() );
//		int progress = -1;
		for(String dataSet : dataSets) {
//			monitor.setNote( "Processing " + dataSet + "..." );
			for(Calendar c : dates) {
				String d = formatter.format( c.getTime() );
				if ( dataElements.contains( OutputSelector.VALUES ) ) {
					writer.print( " ," + dataSet + "," + d + "," );
					for(String id : ids) {
						id = id.toUpperCase();
						for(TimeRange range : times) {
//							monitor.setProgress( ++progress );
							writeRowData(
									writer, c, id, dataSet, range );
						}
					}
					writer.println();
				}
			}
			if ( dataElements.contains( OutputSelector.DAY_MEDIAN ) ) {
				writer.print( " ," + dataSet + "," +
						OutputSelector.DAY_MEDIAN + "," );
				for(String id : ids) {
					id = id.toUpperCase();
					writeDayAggregationData(
							writer, id, dataSet );
				}
				writer.println();
			}
		}
	}
}
