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
package us.mn.state.dot.data;

import java.io.IOException;
import java.util.Calendar;
import java.util.StringTokenizer;

/**
 * SingleDetector
 *
 * @author Douglas Lau
 */
public class SingleDetector extends PlotDetector implements Constants {

	/** Detector id */
	protected final String id;

	/** Detector label */
	protected final String label;

	/** Get a string description of this detector */
	public String toString() {
		return label;
	}

	/** Average field length */
	protected final float field;

	/** Get the average field length */
	public float getField() { return field; }

	/** Detector data source */
	protected final DetectorData detector;
	
	/** Create a new single detector */
	public SingleDetector( DataFactory factory, String d )
			throws InstantiationException
	{
		this.id = createId(d);
		if(id == null){
			throw new InstantiationException(
				"Invalid ID for single detector");
		}
		detector = factory.createDetectorData( id );
		StringBuffer buf = new StringBuffer();
		buf.append( id );
		String l = detector.getLabel();
		if( l != null ) buf.append( " - " ).append( l );
		label = buf.toString();
		field = detector.getFieldLength();
	}

	/** Create a new single detector */
	public SingleDetector( DataFactory factory, String d, float f )
		throws InstantiationException
	{
		this.id = createId(d);
		if(id == null){
			throw new InstantiationException(
					"Invalid ID for single detector");
		}
		detector = factory.createDetectorData( id );
		StringBuffer buf = new StringBuffer();
		buf.append( id );
		String l = detector.getLabel();
		if( l != null ) buf.append( " - " ).append( l );
		label = buf.toString();
		field = f;
	}

	private String createId(String id){
		StringTokenizer t = new StringTokenizer(id, " +-", false);
		if(t.countTokens() != 1){
			return null;
		}
		try{
			Integer.parseInt(id);
			// if the ID is an int, prepend a "D"
			id = "D" + id;
		}catch(NumberFormatException nfe){
		}
		return id;
	}

	/** Get 24 hours of volume data from the data source */
	public float[] getRawVolumeSet( Calendar calendar )
		throws IOException
	{
		int total = 0;
		float[] volume = detector.getVolumeSet( calendar );
		for( int i = 0; i < volume.length; i++ )
			if( volume[ i ] != MISSING_DATA )
				total += (int)volume[ i ];
		totalCache.put( calendar, new Integer( total ) );
		return volume;
	}

	/** Get 24 hours of occupancy data from the data source */
	public float[] getRawOccupancySet( Calendar calendar )
		throws IOException
	{
		return detector.getOccupancySet( calendar );
	}

	/** Get 24 hours of speed data from the data source */
	public float[] getRawSpeedSet( Calendar calendar )
		throws IOException
	{
		return detector.getSpeedSet( calendar );
	}
	
	/** Get 24 hours of density data */
	public float[] getDensitySet( Calendar calendar ) {
		return DataFactory.createDensitySet( getOccupancySet( calendar ),
			field );
	}
}
