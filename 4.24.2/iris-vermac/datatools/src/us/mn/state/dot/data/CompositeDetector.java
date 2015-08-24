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
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * CompositeDetector
 *
 * @author Douglas Lau
 */
public class CompositeDetector extends PlotDetector implements Constants {

	/** Average field length */
	protected final float field;

	/** Get the average field length */
	public float getField() { return field; }

	/** Average composite type */
	static private final int AVERAGE = 0;

	/** Plus composite type */
	static private final int PLUS = 1;

	/** Minus composite type */
	static private final int MINUS = -1;

	/** Array of detectors which make up this composite */
	private final DetectorData[] average;

	/** Array of detectors which add to this composite */
	private final DetectorData[] plus;

	/** Array of detectors which subtract from this composite */
	private final DetectorData[] minus;

	/** String representation of the composite detector */
	private String comp;

	/** Get a string representation of the composite detector */
	public String toString() {
		return comp;
	}
	
	/** Create a new composite detector */
	public CompositeDetector( DataFactory factory,
		String d ) throws NumberFormatException, IOException
	{
		comp = "";
		field = 24.0f;
		Collection<DetectorData> a = new Vector<DetectorData>();
		Collection<DetectorData> p = new Vector<DetectorData>();
		Collection<DetectorData> m = new Vector<DetectorData>();
		int type = AVERAGE;
		StringTokenizer tok = new StringTokenizer( d, " +-", true );
		while( tok.hasMoreTokens() ) {
			String t = tok.nextToken();
			if( t.equals( "+" ) ) {
				type = PLUS;
				comp = comp + "+";
				continue;
			}
			if( t.equals( "-" ) ) {
				type = MINUS;
				comp = comp + "-";
				continue;
			}
			if( t.equals( " " ) ) continue;
			String id = t.toUpperCase();
			try{
				Integer.parseInt(id);
				// if the ID is an int, prepend a 'D'
				id = "D" + id;
			}catch(NumberFormatException nfe){
				//do nothing
			}
			comp = comp + id;
			if( type == AVERAGE ) a.add(factory.createDetectorData(id));
			if( type == PLUS ) p.add(factory.createDetectorData(id));
			if( type == MINUS ) m.add(factory.createDetectorData(id));
		}
		if( a.size() == 0 ){
			throw new NumberFormatException( "Invalid composite" );
		}
		average = (DetectorData[])(a.toArray(new DetectorData[0]));
		plus = (DetectorData[])(p.toArray(new DetectorData[0]));
		minus = (DetectorData[])(m.toArray(new DetectorData[0]));
	}

	/** Get 24 hours of volume data from the data source */
	public float[] getRawVolumeSet( Calendar calendar )
		throws IOException
	{
		int total = 0;
		float[] volume = new float[ SAMPLES_PER_DAY ];
		for( int i = 0; i < average.length; i++ ) {
			total += addDataSet( volume,
				average[ i ].getVolumeSet( calendar ) );
		}
		for( int i = 0; i < plus.length; i++ ) {
			total += addDataSet( volume,
				plus[ i ].getVolumeSet( calendar ) );
		}
		for( int i = 0; i < minus.length; i++ ) {
			total -= subtractDataSet( volume,
				minus[ i ].getVolumeSet( calendar ) );
		}
		totalCache.put( calendar, new Integer( total ) );
		float div = average.length;
		for( int i = 0; i < SAMPLES_PER_DAY; i++ ) {
			if( volume[ i ] != MISSING_DATA )
				volume[ i ] = volume[ i ] / div;
		}
		return volume;
	}

	/** Add one data set to another */
	static protected int addDataSet( float[] set, float[] add ) {
		int total = 0;
		for( int i = 0; i < SAMPLES_PER_DAY; i++ ) {
			if( add[ i ] != MISSING_DATA )
				total += (int)add[ i ];
			if( set[ i ] == MISSING_DATA ) continue;
			if( add[ i ] == MISSING_DATA )
				set[ i ] = MISSING_DATA;
			else set[ i ] += add[ i ];
		}
		return total;
	}

	/** Subtract one data set from another */
	static protected int subtractDataSet( float[] set,
		float[] subtract )
	{
		int total = 0;
		for( int i = 0; i < SAMPLES_PER_DAY; i++ ) {
			if( subtract[ i ] != MISSING_DATA )
				total += (int)subtract[ i ];
			if( set[ i ] == MISSING_DATA ) continue;
			if( subtract[ i ] == MISSING_DATA )
				set[ i ] = MISSING_DATA;
			else {
				set[ i ] -= subtract[ i ];
				if( set[ i ] < 0 ) set[ i ] = MISSING_DATA;
			}
		}
		return total;
	}
	
	/** Get 24 hours of occupancy data from the data source */
	public float[] getRawOccupancySet( Calendar calendar )
		throws IOException
	{
		float[] occupancy = new float[ SAMPLES_PER_DAY ];
		for( int i = 0; i < average.length; i++ ) addDataSet( occupancy,
			average[ i ].getOccupancySet( calendar ) );
		float div = average.length;
		for( int i = 0; i < SAMPLES_PER_DAY; i++ ) {
			if( occupancy[ i ] != MISSING_DATA )
				occupancy[ i ] = occupancy[ i ] / div;
		}
		return occupancy;
	}
	
	/** Get 24 hours of speed data from the data source */
	public float[] getRawSpeedSet( Calendar calendar )
		throws IOException
	{
		//FIXME: implement this method
		return DataFactory.createEmptyDataSet();
	}

	/** Get 24 hours of density data */
	public float[] getDensitySet( Calendar calendar ) {
		// FIXME: the field stuff is not correct
		return DataFactory.createDensitySet( getOccupancySet( calendar ),
			field );
	}
}
