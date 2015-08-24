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
import java.util.HashMap;

/**
 * PlotDetector
 *
 * @author Douglas Lau
 */
abstract public class PlotDetector {

	/** Get the average field length */
	abstract public float getField();

	/** Volume data cache */
	protected final HashMap volumeCache = new HashMap();

	/** Get 24 hours of volume data */
	public synchronized float[] getVolumeSet( Calendar calendar ) {
		float[] volume = null;
		if( volumeCache.containsKey( calendar ) ) {
			volume = (float [])volumeCache.get( calendar );
		}
		else {
			try { volume = getRawVolumeSet( calendar ); }
			catch( Exception e ) {
				volume = DataFactory.createEmptyDataSet();
			}
			volumeCache.put( calendar, volume );
		}
		return volume;
	}

	/** Get 24 hours of volume data from the data source */
	abstract public float[] getRawVolumeSet( Calendar calendar )
		throws IOException;

	/** 24 hour volume total cache */
	protected final HashMap<Calendar, Integer> totalCache =
		new HashMap<Calendar, Integer>();

	/** Get the total volume for this plot data */
	public synchronized int getTotalVolume( Calendar calendar ) {
		getVolumeSet( calendar );
		Integer total = (Integer)totalCache.get( calendar );
		if( total == null ) return 0;
		else return total.intValue();
	}

	/** Occupancy data cache */
	protected final HashMap occupancyCache = new HashMap();

	/** Get 24 hours of occupancy data */
	public synchronized float[] getOccupancySet( Calendar calendar ) {
		float[] occupancy = null;
		if( occupancyCache.containsKey( calendar ) ) {
			occupancy = (float [])occupancyCache.get( calendar );
		}
		else {
			try { occupancy = getRawOccupancySet( calendar ); }
			catch( Exception e ) {
				occupancy = DataFactory.createEmptyDataSet();
			}
			occupancyCache.put( calendar, occupancy );
		}
		return occupancy;
	}

	/** Get 24 hours of occupancy data from the data source */
	abstract public float[] getRawOccupancySet( Calendar calendar )
		throws IOException;

	/** Refresh the data by clearing the cache */
	public synchronized void refresh() {
		volumeCache.clear();
		totalCache.clear();
		occupancyCache.clear();
	}

	/** Get 24 hours of flow data */
	public float[] getFlowSet( Calendar calendar ) {
		return DataFactory.createFlowSet( getVolumeSet( calendar ) );
	}

	/** Get 24 hours of headway data */
	public float[] getHeadwaySet( Calendar calendar ) {
		return DataFactory.createHeadwaySet( getFlowSet( calendar ) );
	}

	/** Get 24 hours of density data */
	abstract public float[] getDensitySet( Calendar calendar );

	/** Speed data cache */
	protected final HashMap speedCache = new HashMap();

	/** Get 24 hours of speed data */
	public synchronized float[] getSpeedSet( Calendar calendar ) {
		float[] speed = null;
		if( speedCache.containsKey( calendar ) ) {
			speed = (float [])speedCache.get( calendar );
		}
		else {
			try {
				speed = getRawSpeedSet( calendar );
				speedCache.put( calendar, speed );
			}catch( Exception e ) {
				speed = DataFactory.createSpeedSet( getFlowSet( calendar ),
						getDensitySet( calendar ) );
			}
		}
		return speed;
	}
	/** Get 24 hours of speed data from the data source */
	abstract public float[] getRawSpeedSet( Calendar calendar )
		throws IOException;

	/** Create a plot detector */
	static public PlotDetector createPlotDetector( DataFactory factory,
		String d ) throws NumberFormatException, IOException
	{
		d = d.toUpperCase();
		try {
			return new SingleDetector( factory, d );
		}
		catch( InstantiationException e ) {
			return new CompositeDetector( factory, d );
		}
	}

	/** Create a station plot detector */
	static public PlotDetector createStationDetector( DataFactory factory,
			String stationId, Sensor[] sensors ) throws Exception
	{
		return new StationDetector( factory, stationId, sensors );
	}
}
