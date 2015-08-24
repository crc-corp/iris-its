/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.data.convert;

import java.io.IOException;
import java.util.Vector;

/**
  * DataFile5Minute
  *
  * This is the class for reading 5-minute detector data from
  * the standard file format used by MN/DOT.  It is a subclass
  * of DataFile, which takes care of the header and other things
  * which are common to all the data files.
  */
public final class DataFile5Minute extends DataFile {


	/** Vector to hold all the DetectorDataSet objects to fill */
	private Vector<DetectorDataSet> detectorVector = null;


	/** Getter methods */
	public DetectorDataSet getDetectorDataSet( int index ) {
		return detectorVector.elementAt( index );
	}
	public int getDetectors() { return detectorVector.size(); };


	/** Open a 5-minute traffic data file read-only */
	public DataFile5Minute( String fileName ) throws IOException {
		super( fileName );
		if( size != 3 ) throw new IOException( "Structure size mismatch" );
		if( intervals != 288 )
			throw new IOException( "Incorrect interval count" );
		if( type != 5 ) throw new IOException( "Incorrect data file type" );
		detectorVector = new Vector<DetectorDataSet>( 6, 6 );
	}


	/** Set the record range for a new data set */
	public void setRecordRange( short firstRecord, short lastRecord ) {
		startRecord = firstRecord;
		endRecord = lastRecord;
		detectorVector.removeAllElements();
	}


	/** Add a detector to the data set */
	public void addDetector( short newDetector ) {
		DetectorDataSet data =
			new DetectorDataSet( newDetector, startRecord, endRecord );
		detectorVector.addElement( data );
	}


	/** Seek to the specified detector in the current record */
	private void seekDetector( short detector ) throws IOException {
		if( detector < 1 || detector > count )
			throw new IOException( "Invalid detector index" );
		seek( recordPos + ( detector - 1 ) * size );
	}


	/** Read the whole detector data structure */
	private void readDetectorData( short detector, DetectorData data )
		throws IOException
	{
		seekDetector( detector );
		data.setVolume( (short)readUnsignedByte() );
		int temp = readByteSwappedUnsignedShort();
		data.setOccupancy( (short)( temp & 0x03ff ) );
		data.setStatus( (short)( ( temp >> 10 ) & 0x07 ) );
		data.setFlag( (short)( ( temp >> 13 ) & 0x07 ) );
	}


	/** Read an entire data set */
	public void readDataSet() throws IOException {
		DetectorDataSet dataSet = null;
		DetectorData data = null;
		short detector = 0;
		int detectorSets = detectorVector.size();
		for( short rec = startRecord; rec <= endRecord; rec++ ) {
			setRecord( rec );
			for( int index = 0; index < detectorSets; index++ ) {
				dataSet = getDetectorDataSet( index );
				detector = dataSet.getDetector();
				data = new DetectorData();
				readDetectorData( detector, data );
				dataSet.addData( data );
			}
		}
	}
}
