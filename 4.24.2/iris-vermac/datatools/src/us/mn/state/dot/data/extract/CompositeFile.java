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

/**
 * A file format in which all data is presented in one file.
 * Detectors, Dates and Data Sets are presented on one
 * axis and time samples on the opposing axis.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @version   $Revision: 1.18 $ $Date: 2005/08/17 20:28:17 $
 */
public class CompositeFile extends FileFormat {

	/** Constructor for the CompositeFile object */
	public CompositeFile() {
		super( "Composite File" );
		Collection<String> c = new Vector<String>();
		c.add(OutputSelector.AVERAGE);
		c.add(OutputSelector.DAY_MEDIAN);
		c.add(OutputSelector.SAMPLE);
		c.add(OutputSelector.SUM);
		c.add(OutputSelector.TIME_MEDIAN);
		c.add(OutputSelector.VALUES);
		outputOptions = c;
	}


	/**
	 * Description of the Method
	 *
	 * @param writer     Description of Parameter
	 */
	public void writeFile( String fn, Calendar c, PrintWriter writer ) {
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
//		ProgressMonitor monitor = processor.getProgressMonitor();
//		monitor.setMaximum(
//				sensors.size() * dates.size() * times.size() * dataSets.size() );
//		int progress = -1;
		for(String id : request.getSensorIds()) {
			id = id.toUpperCase();
//			monitor.setNote( "Processing " + id + "..." );
			for(String dataSet : request.getDataSets()) {
				for(Calendar c : request.getDates()) {
					String d = formatter.format( c.getTime() );
					if ( dataElements.contains( OutputSelector.VALUES ) ||
							dataElements.contains( OutputSelector.SUM ) ||
							dataElements.contains( OutputSelector.AVERAGE ) ||
							dataElements.contains( OutputSelector.TIME_MEDIAN ) ||
							dataElements.contains( OutputSelector.SAMPLE ) ) {
						writer.print( id + "," + dataSet + "," + d + "," );
						for(TimeRange range : request.getTimeRanges()) {
//							monitor.setProgress( ++progress );
							writeRowData( writer, c, id, dataSet, range );
						}
						writer.println();
					}
				}
				if ( dataElements.contains( OutputSelector.DAY_MEDIAN ) ) {
					writer.print( id + " ," + dataSet + "," +
							OutputSelector.DAY_MEDIAN + "," );
					writeDayAggregationData(
							writer, id, dataSet );
					writer.println();
				}
			}
		}
	}
}

