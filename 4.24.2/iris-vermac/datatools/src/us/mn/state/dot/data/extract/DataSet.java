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

import java.io.IOException;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

import us.mn.state.dot.data.Constants;
import us.mn.state.dot.data.DataFactory;
import us.mn.state.dot.data.PlotData;
import us.mn.state.dot.data.PlotDetector;
import us.mn.state.dot.data.StationDetector;


/**
 * DataSet is the base class for any data manipulation
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @version   $Revision: 1.23 $ $Date: 2005/08/15 16:33:04 $
 */
public abstract class DataSet
		 implements Constants, Comparable {

	/** Plot data */
	protected final PlotData plotData;

	/** Set name */
	protected final String name;

	/** Set units */
	protected final String units;

	/** Bounded range model for associated scrollbars */
	protected final BoundedRangeModel model =
			new DefaultBoundedRangeModel();

	public int compareTo(Object o){
		DataSet set0 = this;
		DataSet set1 = (DataSet)o;
		return set0.getName().compareTo(set1.getName());
	}
	
	/**
	 * Create a new data set
	 *
	 * @param n  Description of Parameter
	 * @param u  Description of Parameter
	 * @param d  Description of Parameter
	 */
	protected DataSet( String n, String u, PlotData d ) {
		name = n;
		units = "(" + u + ")";
		plotData = d;
		model.setMinimum( 0 );
		model.setValue( 0 );
	}


	/**
	 * Get the percentage of data sampled in the sample data
	 *
	 * @param data  The data set
	 * @return      The percent of values that are not MISSING_DATA.
	 */
	public static float sample( float[] data ) {
		int sample = 0;
		for ( int i = 0; i < data.length; i++ ) {
			if ( data[i] != MISSING_DATA ) {
				sample++;
			}
		}
		return ( float ) sample / data.length;
	}


	/**
	 * Get the sum of the data
	 *
	 * @param data  The data set.
	 * @return      The sum of all non-missing values.
	 */
	public static float sum( float[] data ) {
		float total = 0;
		for ( int i = 0; i < data.length; i++ ) {
			if ( data[i] != MISSING_DATA ) {
				total += data[i];
			}
		}
		return total;
	}


	/**
	 * Get the average of the data
	 *
	 * @param data  The data set
	 * @return     The average of all non-missing values.
	 */
	public static float average( float[] data ) {
		float total = sum( data );
		int good = good( data );
		return total / good;
	}


	/**
	 * Get the number of good values in a set
	 *
	 * @param data  The data set.
	 * @return      The number of non-missing values in the set.
	 */
	public static int good( float[] data ) {
		int good = 0;
		for ( int i = 0; i < data.length; i++ ) {
			if ( data[i] != MISSING_DATA ) {
				good++;
			}
		}
		return good;
	}


	/**
	 * Get the median value of the data
	 *
	 * @param data  The data set.
	 * @return      The median of non-missing values.
	 */
	public static float median( float[] data ) {
		data = sort( data );
		int good = good( data );
		int mid = ( data.length - good ) + ( good / 2 );
		float f = 2;
		if ( good < 1 ) {
			return -1;
		}
		// there are no good data samples
		if ( good / 2 != good / f ) {
			return data[mid];
		}
		// odd number of samples
		else {
			return ( data[mid] + data[mid - 1] ) / 2;
		}
		// even number of samples
	}


	/**
	 * Sort an array of float values
	 *
	 * @param data  The data set
	 * @return     The sorted data set.
	 */
	public static float[] sort( float[] data ) {
		Vector<Float> vector = new Vector<Float>();
		vector.addElement( new Float( data[0] ) );
		boolean added = false;
		for ( int dataIndex = 1; dataIndex < data.length; dataIndex++ ) {
			added = false;
			for ( int vectorIndex = 0; vectorIndex < vector.size(); vectorIndex++ ) {
				float currentValue =
						( ( Float ) ( vector.elementAt( vectorIndex ) ) ).floatValue();
				if ( data[dataIndex] < currentValue ) {
					vector.add( vectorIndex, new Float( data[dataIndex] ) );
					added = true;
					break;
				}
			}
			if ( !added ) {
				vector.addElement( new Float( data[dataIndex] ) );
			}
		}
		for ( int i = 0; i < vector.size(); i++ ) {
			data[i] = vector.elementAt(i).floatValue();
		}
		return data;
	}


	/**
	 * Get the name of the set.
	 *
	 * @return   The name of the <code>DataSet</code>
	 */
	public String getName() {
		return name;
	}


	/**
	 * Get the set units
	 *
	 * @return   The units of measure for this data set.
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * Create a smoothed array of points
	 *
	 * @param start   Starting sample index
	 * @param extent  Extent (in 30-second samples)
	 * @param smooth  Number of 30-second samples to smooth together
	 * @return        An array of values that represent the averages of the
	 * original values.
	 */
	public float[] getSmoothedArray( int start, int extent, int smooth ) {
		int points = extent / smooth;
		float[] array = new float[points];
		float[] raw_data = getDataSet();
		for ( int p = 0; p < points; p++ ) {
			int t = start + p * smooth;
			float total = 0;
			int good = 0;
			for ( int r = t; r < t + smooth; r++ ) {
				if ( raw_data[r] < 0 ) {
					continue;
				}
				total += raw_data[r];
				good++;
			}
			if ( good > 0 ) {
				array[p] = total / good;
			} else {
				array[p] = MISSING_DATA;
			}
		}
		return array;
	}


	/**
	 * Get the bounded range model for associated scrollbars
	 *
	 * @return   The model value
	 */
	public BoundedRangeModel getModel() {
		return model;
	}


	/**
	 * Get an aggregate value of a smoothed data set.
	 *
	 * @param start      The index within the data set at which to start
	 * @param extent    The number of samples to include in the aggregation
	 * @param smooth   The number of samples to use when smoothing.
	 * @param aggregation  The aggregation method.
	 * @return             The aggregate value.
	 */
	public float getAggregateValue( int start, int extent,
			int smooth, int aggregation ) {
		float[] data = getSmoothedArray( start, extent, smooth );
		if ( aggregation == AGG_SUM ) {
			return sum( data );
		}
		if ( aggregation == AGG_AVERAGE ) {
			return average( data );
		}
		if ( aggregation == AGG_DAY_MEDIAN ) {
			return median( data );
		}
		if ( aggregation == AGG_TIME_MEDIAN ) {
			return median( data );
		}
		return 0.0f;
	}


	/**
	 * Get the set name
	 *
	 * @return   The name of the <code>DataSet</code>.
	 */
	public String toString() {
		return name;
	}


	/**
	 * Get a specific data set
	 *
	 * @return   The dataSet value
	 */
	protected abstract float[] getDataSet();


	/**
	 * The Flow DataSet is the set of values that repesent the number
	 * of vehicles per hour past a given point.
	 *
	 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
	 * @version   $Revision: 1.23 $ $Date: 2005/08/15 16:33:04 $
	 */
	public static class Flow extends DataSet {

		/**
		 * Create a new flow data set
		 *
		 * @param data  The PlotData object.
		 */
		public Flow( PlotData data ) {
			super( "Flow", "vehicles / hour", data );
		}


		/**
		 * Get a specific data set
		 *
		 * @return   The dataSet value
		 */
		protected float[] getDataSet() {
			PlotDetector d = plotData.getDetector();
			Calendar c = plotData.getCalendar();
			return d.getFlowSet( c );
		}
	}


	/**
	 * The Headway DataSet is the set of values that represent
	 * the number of seconds between vehicles.
	 *
	 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
	 * @version   $Revision: 1.23 $ $Date: 2005/08/15 16:33:04 $
	 */
	public static class Headway extends DataSet {

		/**
		 * Create a new headway data set
		 *
		 * @param data  The PlotData object.
		 */
		public Headway( PlotData data ) {
			super( "Headway", "seconds / vehicle", data );
		}


		/**
		 * Get a specific data set
		 *
		 * @return   The dataSet value
		 */
		protected float[] getDataSet() {
			PlotDetector d = plotData.getDetector();
			Calendar c = plotData.getCalendar();
			return d.getHeadwaySet( c );
		}

	
		/** Create a smoothed array of points.  Headway must
		 * override this method because smoothing needs to
		 * be done to the flow data before computing the headway.
		 * @param start Starting sample index
		 * @param extent Extent (in 30-second samples)
		 * @param smooth Number of 30-second samples to smooth together
		 * @param set Data set index
		 * @param max Maximum value to scale the samples
		 */
		public float[] getSmoothedArray( int start, int extent, int smooth )
		{
			if(plotData.getDetector() instanceof StationDetector){
				//We define station headway as the simple (non-weighted)
				//average of the mainline station headways.
				return super.getSmoothedArray(start, extent, smooth);
			}
			int points = extent / smooth;
			float[] array = new float[ points ];
			PlotDetector det = plotData.getDetector();
			Calendar c = plotData.getCalendar();
			float[] raw_data = det.getFlowSet( c );
			for( int p = 0; p < points; p++ ) {
				int t = start + p * smooth;
				float total = 0;
				int good = 0;
				for( int r = t; r < t + smooth; r++ ) {
					if( raw_data[ r ] < 0 ) continue;
					total += raw_data[ r ];
					good++;
				}
				if( good > 0 )
					array[ p ] = total / good;
				else array[ p ] = MISSING_DATA;
			}
			return DataFactory.createHeadwaySet(array);
		}
	}


	/**
	 * The Volume DataSet is the set of values that represent the
	 * number of cars passing a given point per unit of time.
	 *
	 * @author    john3tim
	 */
	public static class Volume extends DataSet {

		/**
		 * Create a new volume data set
		 *
		 * @param data  Description of Parameter
		 */
		public Volume( PlotData data ) {
			super( "Volume", "number of cars", data );
		}


		/**
		 * Create a smoothed array of points
		 *
		 * @param start   Starting sample index
		 * @param extent  Extent (in 30-second samples)
		 * @param smooth  Number of 30-second samples to smooth together
		 * @return        The smoothedArray value
		 */
		public float[] getSmoothedArray( int start, int extent, int smooth ) {
			int points = extent / smooth;
			float[] array = new float[points];
			float[] raw_data = getDataSet();
			for ( int p = 0; p < points; p++ ) {
				int t = start + p * smooth;
				float total = 0;
				int good = 0;
				for ( int r = t; r < t + smooth; r++ ) {
					if ( raw_data[r] < 0 ) {
						continue;
					}
					total += raw_data[r];
					good++;
				}
				if ( good > 0 ) {
					array[p] = total;
				} else {
					array[p] = MISSING_DATA;
				}
			}
			return array;
		}


		/**
		 * Get a specific data set
		 *
		 * @return   The dataSet value
		 */
		protected float[] getDataSet() {
			PlotDetector d = plotData.getDetector();
			Calendar c = plotData.getCalendar();
			float[] data = d.getVolumeSet( c );
			return data;
		}
	}


	/**
	 * The Occupancy DataSet is the set of values that represent
	 * the percent of time that a detector is sensing a vehicle.
	 *
	 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
	 * @version   $Revision: 1.23 $ $Date: 2005/08/15 16:33:04 $
	 */
	public static class Occupancy extends DataSet {

		/**
		 * Create a new occupancy data set
		 *
		 * @param data  The PlotData object.
		 */
		public Occupancy( PlotData data ) {
			super( "Occupancy", "percent", data );
		}


		/**
		 * Get a specific data set
		 *
		 * @return   The dataSet value
		 */
		protected float[] getDataSet() {
			PlotDetector d = plotData.getDetector();
			Calendar c = plotData.getCalendar();
			return d.getOccupancySet( c );
		}
	}


	/**
	 * The Density DataSet is the set of values that represent
	 * number of vehicles per mile of roadway.
	 *
	 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
	 * @version   $Revision: 1.23 $ $Date: 2005/08/15 16:33:04 $
	 */
	public static class Density extends DataSet {

		/**
		 * Create a new density data set
		 *
		 * @param data  The PlotData object.
		 */
		public Density( PlotData data ) {
			super( "Density", "vehicles / mile", data );
		}


		/**
		 * Get a specific data set
		 *
		 * @return   The dataSet value
		 */
		protected float[] getDataSet() {
			PlotDetector d = plotData.getDetector();
			Calendar c = plotData.getCalendar();
			return d.getDensitySet( c );
		}
	}


	/**
	 * The Speed DataSet is the set of values that represent
	 * the average speed of vehicles, measured in miles per hour.
	 *
	 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
	 * @version   $Revision: 1.23 $ $Date: 2005/08/15 16:33:04 $
	 */
	public static class Speed extends DataSet {

		/**
		 * Create a new speed data set
		 *
		 * @param data  The PlotData object.
		 */
		public Speed( PlotData data ) {
			super( "Speed", "miles / hour", data );
		}


		/**
		 * Create a smoothed array of points
		 *
		 * @param start   Starting sample index
		 * @param extent  Extent (in 30-second samples)
		 * @param smooth  Number of 30-second samples to smooth together
		 * @return        The smoothedArray value
		 */
		public float[] getSmoothedArray( int start, int extent, int smooth ) {
			if(plotData.getDetector() instanceof StationDetector){
				//We define station speed as the simple (non-weighted)
				//average of the mainline station speeds.
				return super.getSmoothedArray(start, extent, smooth);
			}
			try{
				plotData.getDetector().getRawSpeedSet(plotData.getCalendar());
				return super.getSmoothedArray(start, extent, smooth);
			}catch(IOException ioe){}
			int points = extent / smooth;
			float[] smoothedArray = new float[points];
			DataSet.Density densSet = new DataSet.Density( plotData );
			DataSet.Volume volSet = new DataSet.Volume( plotData );
			float[] densities = densSet.getDataSet();
			float[] volumes = volSet.getDataSet();
			float[] speedSet = getDataSet();
			for ( int p = 0; p < points; p++ ) {
				int t = start + p * smooth;
				float volTotal = 0;
				float samples = 0;
				float densTotal = 0;
				for ( int r = t; r < t + smooth; r++ ) {
					if ( speedSet[r] < 0 ) {
						continue;
					}
					volTotal += volumes[r];
					densTotal += densities[r];
					samples++;
				}
				if ( samples > 0 ) {
					float density = densTotal / samples;
					float flow =
							volTotal / samples * SAMPLES_PER_HOUR;
					smoothedArray[p] = flow / density;
				} else {
					smoothedArray[p] = MISSING_DATA;
				}
			}
			return smoothedArray;
		}

		/**
		 * Get a specific data set
		 *
		 * @return   The dataSet value
		 */
		protected float[] getDataSet() {
			PlotDetector d = plotData.getDetector();
			Calendar c = plotData.getCalendar();
			return d.getSpeedSet( c );
		}

	}

}
