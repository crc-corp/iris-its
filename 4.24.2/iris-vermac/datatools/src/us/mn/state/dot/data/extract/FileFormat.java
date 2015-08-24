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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Vector;

import us.mn.state.dot.data.Axis;
import us.mn.state.dot.data.Constants;

/**
 * Description of the Class
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @version   $Revision: 1.20 $ $Date: 2005/08/17 20:28:18 $
 */
public abstract class FileFormat implements Constants, Serializable {

	protected Collection<String> outputOptions =
		new Vector<String>();
	
	protected DataRequest request = null;
	
	/** Description of the Field */
	protected String filePath = null;

	/** Description of the Field */
	protected String fileName = null;

	/** Description of the Field */
	protected Collection rows = null;

	/** Description of the Field */
	protected Collection columns = null;

	/** Description of the Field */
	protected Collection<String> dataElements = null;

	/** Description of the Field */
	protected final String name;

	protected final SimpleDateFormat formatter = new SimpleDateFormat( "yyyy/MM/dd" );

	/**
	 * Constructor for the FileFormat object
	 *
	 * @param name  Description of Parameter
	 */
	public FileFormat( String name ) {
		this.name = name;
	}

	public final Collection getValidOptions(){
		return new Vector();
	}

	/**
	 * Sets the rows attribute of the FileFormat object
	 *
	 * @param rows  The new rows value
	 */
	public void setRows( Collection rows ) {
		this.rows = rows;
	}


	/**
	 * Sets the columns attribute of the FileFormat object
	 *
	 * @param columns  The new columns value
	 */
	public void setColumns( Collection columns ) {
		this.columns = columns;
	}


	/**
	 * Sets the dataElements attribute of the FileFormat object
	 *
	 * @param elements  The new dataElements value
	 */
	public void setDataElements( Collection<String> elements ) {
		dataElements = elements;
	}


	/**
	 * Gets the rows attribute of the FileFormat object
	 *
	 * @return   The rows value
	 */
	public Collection getRows() {
		return rows;
	}


	/**
	 * Gets the columns attribute of the FileFormat object
	 *
	 * @return   The columns value
	 */
	public Collection getColumns() {
		return columns;
	}


	/**
	 * Gets the dataElements attribute of the FileFormat object
	 *
	 * @return   The dataElements value
	 */
	public Collection<String> getDataElements() {
		return dataElements;
	}


	/**
	 * Description of the Method
	 *
	 * @return   Description of the Returned Value
	 */
	public String toString() {
		return name;
	}

	/**
	 * Description of the Method
	 *
	 * @param writer     Description of Parameter
	 */
	protected void writeColumnHeader( PrintWriter writer ) {
		Axis.Time time = new Axis.Time();
		writer.print( " , , ," );
		int currentTime = NULL;
		int endTime = NULL;
		int smoothing = NULL;
		for(TimeRange range : request.getTimeRanges()) {
			if ( dataElements.contains( OutputSelector.VALUES ) ||
					dataElements.contains( OutputSelector.DAY_MEDIAN ) ) {
				currentTime = range.getStart();
				endTime = currentTime + range.getExtent();
				smoothing = range.getSmoothing();
				while ( currentTime < endTime ) {
					writer.print( time.formatTime( currentTime + smoothing ) + "," );
					currentTime += smoothing;
				}
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
		writer.println();
	}

	public boolean transform(){
		return getColumns().contains( "Times" );
	}

	abstract public void writeFile( String s, Calendar c, PrintWriter pw );

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
		DataSet set = request.getDataSet( c, detectorId, dataSet );
		if ( set != null ) {
			float[] values = set.getSmoothedArray(
					range.getStart(), range.getExtent(),
					range.getSmoothing() );
			if ( dataElements.contains( OutputSelector.VALUES ) ) {
				for ( int i = 0; i < values.length; i++ ) {
					writer.print( values[i] + "," );
				}
			} else if ( ( dataElements.contains( OutputSelector.SUM ) ||
					dataElements.contains( OutputSelector.AVERAGE ) ||
					dataElements.contains( OutputSelector.TIME_MEDIAN ) ||
					dataElements.contains( OutputSelector.SAMPLE ) ) &&
					dataElements.contains( OutputSelector.DAY_MEDIAN ) ) {
				for ( int i = 0; i < values.length; i++ ) {
					writer.print( " ," );
				}
			}
			if ( dataElements.contains( OutputSelector.SUM ) ) {
				if ( dataSet.equals(VOLUME) ) {
					writer.print( DataSet.sum( values ) + "," );
				} else {
					writer.print( "N/A," );
				}
			}
			if ( dataElements.contains( OutputSelector.AVERAGE ) ) {
				writer.print( DataSet.average( values ) + "," );
			}
			if ( dataElements.contains( OutputSelector.TIME_MEDIAN ) ) {
				writer.print( DataSet.median( values ) + "," );
			}
			if ( dataElements.contains( OutputSelector.SAMPLE ) ) {
				float s = DataSet.sample( values ) * 100;
				writer.print( s + "%" + "," );
			}
		}
	}


	/**
	 * Description of the Method
	 *
	 * @param writer     The <code>PrinWriter</code> to write the data to
	 * @param id         The detector ID
	 * @param dataSet    The name of the <code>DataSet</code>
	 */
	protected void writeDayAggregationData( PrintWriter writer,
			String id, String dataSet ) {
		if ( getDataElements().contains( OutputSelector.DAY_MEDIAN ) ) {
			for(TimeRange range : request.getTimeRanges()) {
				float[][] smoothedValues =
						new float[request.getDates().size()][range.getSampleSize()];
				int dateIndex = 0;
				for(Calendar c : request.getDates()) {
					DataSet set = request.getDataSet(c, id, dataSet);
					float[] values = set.getSmoothedArray(
							( range.getStart() ), range.getExtent(),
							range.getSmoothing() );
					for ( int i = 0; i < range.getSampleSize(); i++ ) {
						smoothedValues[dateIndex][i] = values[i];
					}
					dateIndex++;
				}
				for ( int sampleNumber = 0;
						sampleNumber < range.getSampleSize(); sampleNumber++ ) {
					float[] dailySamples = new float[request.getDates().size()];
					for ( dateIndex = 0; dateIndex < request.getDates().size(); dateIndex++ ) {
						dailySamples[dateIndex] =
								smoothedValues[dateIndex][sampleNumber];
					}
					writer.print( DataSet.median( dailySamples ) + " ," );
				}
				for ( int i = 0; i < ( getDataElements().size() - 2 ); i++ ) {
					writer.print( " ," );
				}
			}
		}
	}


	/**
	 * Description of the Method
	 *
	 * @param o          Description of Parameter
	 * @param w          Description of Parameter
	 */
	abstract void writeRows( Object o, PrintWriter w );

	public final Collection<String> getOutputOptions(){
		return outputOptions;
	}

	/**
	 * Description of the Method
	 *
	 * @param file  Description of Parameter
	 */
	protected void transformFile( File file ) {
		try {
			int rows = 0;
			int cols = 0;
			FileReader fileReader = new FileReader( file );
			BufferedReader reader = new BufferedReader( fileReader );
			StringTokenizer t = null;
			String line = reader.readLine();
			while ( line != null ) {
				rows++;
				t = new StringTokenizer( line, "," );
				cols = Math.max( cols, t.countTokens() );
				line = reader.readLine();
			}
			reader.close();
			fileReader = new FileReader( file );
			reader = new BufferedReader( fileReader );
			String[][] data = new String[rows][cols];
			int row = 0;
			int col = 0;
			line = reader.readLine();
			while ( line != null ) {
				t = new StringTokenizer( line, "," );
				int tokenCount = t.countTokens();
				for ( col = 0; col < tokenCount; col++ ) {
					data[row][col] = t.nextToken();
				}
				line = reader.readLine();
				row++;
			}
			reader.close();
			FileOutputStream stream = new FileOutputStream( file );
			PrintWriter writer = new PrintWriter( stream );
			for ( col = 0; col < cols; col++ ) {
				for ( row = 0; row < rows; row++ ) {
					if ( data[row][col] == null ) {
						writer.print( "," );
					} else {
						writer.print( data[row][col] + "," );
					}
				}
				writer.println();
			}
			writer.flush();
			writer.close();
		} catch ( Exception e ) {
			new ExtractExceptionDialog( e ).setVisible( true );
		}
	}

	public void setDataRequest( DataRequest dr ){
		request = dr;
	}

	/**
	 * Gets the id attribute of the RequestProcessor object
	 *
	 * @param s  Description of Parameter
	 * @return   The id value
	 */
	protected String getId( String s ) {
		if ( s.startsWith( "Station_" ) ) {
			return "S " + s.substring( 8 );
		} else if ( s.startsWith( "Detector_" ) ) {
			return "D " + s.substring( 9 );
		} else {
			return null;
		}
	}

}
