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

import java.util.Vector;

/**
  * DetectorDataSet
  *
  * This class defines a detector data set from
  * the standard file format used by MN/DOT.
  */
public class DetectorDataSet implements java.io.Serializable {

	// Private data
	private short detector = 0;
	private short startRecord = 0;
	private Vector<DetectorData> dataVector = null;

	/** Getter methods */
	public final short getDetector() { return detector; };
	public final short getStartRecord() { return startRecord; };
	public final DetectorData getData( int index )
		throws ArrayIndexOutOfBoundsException
	{
		return dataVector.elementAt( index );
	}


	/** Create a new detector data set */
	public DetectorDataSet( short det, short firstRecord, short lastRecord ) {
		detector = det;
		startRecord = firstRecord;
		dataVector = new Vector<DetectorData>( 1 + lastRecord - firstRecord );
	}


	/** Add a piece of detector data to a data set */
	public void addData( DetectorData data ) {
		dataVector.addElement( data );
	}

	public int size() { return dataVector.size(); }
}
