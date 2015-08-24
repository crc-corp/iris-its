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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

/**
 * StationDetector
 * This class is responsible for supplying detector data
 * for a station.
 * @author Timothy A. Johnson
 */
public class StationDetector extends PlotDetector
		implements Constants {

	/** Station id */
	protected final String id;

	/** The individual PlotDetectors that make up the station */
	protected Hashtable<String, PlotDetector> dets =
		new Hashtable<String, PlotDetector>();

	/** The sensors that make up this station */
	protected Sensor[] sensors;
	
	/** The average field length of the detectors
	 * that make up this station
	 */
	protected float field;

	/** Station label */
	protected final String label;

	/** Get a string description of this station */
	public String toString() {
		return label;
	}

	/** Detector data source */
	protected final DetectorData[] data;

	/** Create a new station detector */
	public StationDetector( DataFactory factory,
			String stationId, Sensor[] sensors ) throws Exception {
		id = stationId;
		if( sensors == null || sensors.length == 0){
			throw new Exception( "There are no sensors for station " + id );
		}
		this.sensors = sensors;
		data = new DetectorData[ sensors.length ];
		for( int i=0; i<sensors.length; i++ ) {
			data[ i ] = factory.createDetectorData( sensors[i].getId() );
			dets.put( sensors[i].getId(), PlotDetector.createPlotDetector( factory, sensors[i].getId() ) );
		}
		int mainCnt = 0;
		SingleDetector d = null;
		for( int i=0; i<sensors.length; i++ ) {
			if(isMainline(sensors[i])){
				d = (SingleDetector)(dets.get( sensors[i].getId() ) );
				field = field + d.getField();
				mainCnt++;
			}
		}
		field = field / mainCnt;
		label = "Station " + stationId ;
	}

	protected boolean isMainline(Sensor s){
		int cat = s.getCategory();
		return cat==Sensor.MAINLINE || cat==Sensor.REVERSIBLE;
	}
	
	public Sensor[] getMainlineSensors(){
		Collection<Sensor> c = new Vector<Sensor>();
		for( int i=0; i<sensors.length; i++ ){
			if(isMainline(sensors[i])){
				c.add(sensors[ i ]);
			}
		}
		return (Sensor[])c.toArray( new Sensor[0] );
	}
	
	public float getField() {
		return field;
	}

	/** Get 24 hours of volume data from the data source.
	 * The volume respresents total volume across all mainline
	 * lanes at this station
	*/
	public float[] getRawVolumeSet(Calendar cal)
		throws IOException
	{
		int total = 0;
		SingleDetector d = (SingleDetector)dets.get( sensors[0].getId() );
		float[] volumes =
			new float[ d.getVolumeSet(cal).length ];
		Sensor[] mlSensors = getMainlineSensors();
		for( int i = 0; i < mlSensors.length; i++ ) {
			d = (SingleDetector)dets.get( mlSensors[i].getId() );
			float[] vSet = d.getVolumeSet(cal);
			for( int index=0; index<vSet.length; index++ ) {
				if( vSet[ index ] != MISSING_DATA ) {
					total += (int)vSet[ index ];
					volumes[ index ] = volumes[ index ] + vSet[ index ];
				}
				totalCache.put(cal, new Integer( total ));
			}
		}
		return volumes;
	}


	/** Get 24 hours of occupancy data from the data source */
	public float[] getRawOccupancySet( Calendar calendar )
		throws IOException
	{
		SingleDetector d = (SingleDetector)dets.get( sensors[0].getId() );
		float[] occupancies =
			new float[ d.getOccupancySet( calendar ).length ];
		Sensor[] mlSensors = getMainlineSensors();
		for( int index=0; index<occupancies.length; index++ ) {
			int goodDataCount = 0;
			for( int s = 0; s<mlSensors.length; s++ ) {
				d = (SingleDetector)dets.get( mlSensors[s].getId() );
				float value = d.getOccupancySet( calendar )[ index ];
				if( value != MISSING_DATA ) {
					occupancies[ index ] = occupancies[ index ] + value;
					goodDataCount++;
				}
			}
			if( goodDataCount > 0 ) {
				occupancies[ index ] = occupancies[ index ] / goodDataCount;
			}
		}
		return occupancies;
	}

	/** Get 24 hours of speed data from the data source. */
	public float[] getRawSpeedSet(Calendar cal)
		throws IOException
	{
		int total = 0;
		int samples = 0;
		SingleDetector d = (SingleDetector)dets.get( sensors[0].getId() );
		float[] speeds = new float[ d.getSpeedSet(cal).length ];
		Sensor[] mlSensors = getMainlineSensors();
		for(int index=0; index<speeds.length; index++) {
			//compute the average raw speed of the mainline detectors
			total = 0;
			samples = 0;
			for(int i=0; i<mlSensors.length; i++) {
				d = (SingleDetector)dets.get( mlSensors[i].getId() );
				float[] sSet = d.getSpeedSet(cal);
				if(sSet[ index ] != MISSING_DATA ) {
					total += (int)sSet[ index ];
					samples++;
				}
			}
			if(samples>0){
				speeds[index] = total / samples;
			}else{
				speeds[index] = MISSING_DATA;
			}
		}
		return speeds;
	}

	/** Get 24 hours of density data */
	public float[] getDensitySet( Calendar calendar ) {
		PlotDetector[] mlDetectors = getMainlineDetectors();
		ArrayList<float[]> dataSets = new ArrayList<float[]>();
		for(int i=0; i<mlDetectors.length; i++){
			dataSets.add(mlDetectors[i].getDensitySet(calendar));
		}
		return averageData(dataSets);
	}

	/** Get 24 hours of flow data.
	 * Flow, for a station, is the sum of the individual
	 * mainline detector flows.
	 */
	public float[] getFlowSet( Calendar calendar, Sensor s ) {
		return DataFactory.createFlowSet( getVolumeSet( calendar, s ) );
	}

	/** Get 24 hours of headway data */
	public float[] getHeadwaySet( Calendar calendar ) {
		PlotDetector[] mlDetectors = getMainlineDetectors();
		ArrayList<float[]> dataSets = new ArrayList<float[]>();
		for(int i=0; i<mlDetectors.length; i++){
			dataSets.add(mlDetectors[i].getHeadwaySet(calendar));
		}
		return averageData(dataSets);
	}

	/** Get the average flow per mainline lane
	 * 
	 * @param calendar
	 * @return The flow per lane on mainline
	 */
	protected float[] getLaneFlowSet( Calendar calendar ) {
		float[] flow = getFlowSet( calendar );
		for( int i=0; i<flow.length; i++ ) {
			flow[ i ] = flow[ i ] / getMainlineSensors().length;
		}
		return flow;
	}

	private PlotDetector[] getMainlineDetectors(){
		Sensor[] mlSensors = getMainlineSensors();
		PlotDetector[] mlDets = new PlotDetector[ mlSensors.length ];
		for(int i=0; i<mlDets.length; i++) {
			mlDets[i] = (PlotDetector)dets.get(mlSensors[i].getId());
		}
		return mlDets;
	}

	/** Get 24 hours of speed data for mainline sensors */
	public float[] getSpeedSet(Calendar c) {
		PlotDetector[] mlDetectors = getMainlineDetectors();
		ArrayList<float[]> dataSets = new ArrayList<float[]>();
		for(int i=0; i<mlDetectors.length; i++){
			dataSets.add(mlDetectors[i].getSpeedSet(c));
		}
		return averageData(dataSets);
	}

	/** Calculate the average of an array of float values */
	private float average(float[] values){
		float total = 0;
		float value = 0;
		int goodSamples = 0;
		for(int i=0; i<values.length; i++){
			value = values[i];
			if(value != MISSING_DATA) {
				total += value;
				goodSamples += 1;
			}
		}
		if(goodSamples>0) return total / goodSamples;
		return MISSING_DATA;
	}
	
	/** Create an array of values which are the averages of the
	 *  values in the list of dataSets
	 * @param dataSets The list of float arrays to be averaged
	 * @return A float[] of average values
	 */
	private float[] averageData(ArrayList<float[]> dataSets){
		int setCount = dataSets.size();
		int sampleSize = dataSets.get(0).length;
		float[] temp = new float[setCount];
		float[] result = new float[sampleSize];
		for(int s=0; s<sampleSize; s++){
			for(int i=0; i<setCount; i++){
				temp[i] = dataSets.get(i)[s];
			}
			result[s] = average(temp);
		}
		return result;
	}

	/** Get 24 hours of volume data */
	public synchronized float[] getVolumeSet( Calendar calendar, Sensor s ) {
		SingleDetector d = (SingleDetector)dets.get( s.getId() );
		return d.getVolumeSet( calendar );
	}

	/** Get 24 hours of occupancy data for the given sensor */
	public synchronized float[] getOccupancySet( Calendar calendar, Sensor s ) {
		SingleDetector d = (SingleDetector)dets.get( s.getId() );
		return d.getOccupancySet( calendar );
	}
}
