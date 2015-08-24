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

import javax.swing.BoundedRangeModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultComboBoxModel;

import us.mn.state.dot.data.plot.PlotSet;

/**
 * Axis is the base class for any data plotting axis
 *
 * @author Douglas Lau
 */
abstract public class Axis implements Constants {

	/** Zoom level class */
	protected final class Zoom {

		/** Number of lines to draw at this zoom level */
		protected final int lines;

		/** Viewable extent of this zoom level */
		protected final int extent;

		/** Strong line spacing */
		protected final int strong;

		/** Scale of this zoom level */
		protected final float scale;

		/** Create a new zoom level */
		protected Zoom( int e, float l, int s ) {
			extent = e;
			lines = (int)( maxValue / l );
			strong = s;
			scale = (float)extent / (float)maxValue;
		}

		/** Create a label for a specified line */
		public String createLineLabel( int line ) {
			return String.valueOf( maxValue * line / lines );
		}

		/** Is the specified line "strong"? */
		public boolean isLineStrong( int line ) {
			return ( maxValue * line / lines ) % strong == 0;
		}
	}


	/** Create a new axis */
	protected Axis( String n, String u, int m, PlotSet p ) {
		name = n;
		units = "(" + u + ")";
		maxValue = m;
		plot = p;
		model.setMinimum( 0 );
		model.setMaximum( maxValue );
		model.setValue( 0 );
	}

	/** Plot set */
	protected final PlotSet plot;

	/** Axis name */
	protected final String name;

	/** Get the axis name */
	public String getName() { return name; }

	/** Axis units */
	protected final String units;

	/** Get the axis units */
	public String getUnits() { return units; }

	/** Maximum value to plot */
	protected final int maxValue;

	/** List of available zoom levels */
	protected final ArrayList<Zoom> zooms = new ArrayList<Zoom>();

	/** Current zoom level */
	protected int zoom = 0;

	/** Zoom in to the next zoom level */
	public void zoomIn() {
		if( zoom < zooms.size() - 1 ) {
			zoom++;
			setExtent();
		}
	}

	/** Zoom out to the previous zoom level */
	public void zoomOut() {
		if( zoom > 0 ) {
			zoom--;
			setExtent();
		}
	}

	/** Set the extent of the model based on the current zoom level */
	protected void setExtent() {
		int extent = ( (Zoom)zooms.get( zoom ) ).extent;
		int value = model.getValue() + model.getExtent() - extent;
		if( value + extent > maxValue ) value = maxValue - extent;
		if( value < 0 ) value = 0;
		model.setRangeProperties( value, extent, 0, maxValue, false );
	}

	/** Get the graph scale at the current zoom level */
	public float getScale() {
		return ( (Zoom)zooms.get( zoom ) ).scale;
	}

	/** Get the number of lines at the current zoom level */
	public int getLines() {
		return ( (Zoom)zooms.get( zoom ) ).lines;
	}

	/** Check if a particular line should be strong (darker) */
	public boolean isLineStrong( int line ) {
		return ( (Zoom)zooms.get( zoom ) ).isLineStrong( line );
	}

	/** Get the label for a specified line */
	public String getLineLabel( int line ) {
		return ( (Zoom)zooms.get( zoom ) ).createLineLabel( line );
	}

	/** Get a specific data set */
	abstract protected float[] getDataSet( int set );

	/** Create a smoothed array of points
	 * @param start Starting sample index
	 * @param extent Extent (in 30-second samples)
	 * @param smooth Number of 30-second samples to smooth together
	 * @param set Data set index
	 * @param max Maximum value to scale the samples
	 */
	public float[] getSmoothedArray( int start, int extent, int smooth,
		int set, float max )
	{
		int points = extent / smooth;
		float[] array = new float[ points ];
		float[] raw_data = getDataSet( set );
		float ratio = max / maxValue;
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
				array[ p ] = ratio * total / good;
			else array[ p ] = MISSING_DATA;
		}
		return array;
	}

	/** Bounded range model for associated scrollbars */
	protected final BoundedRangeModel model =
		new DefaultBoundedRangeModel();

	/** Get the bounded range model for associated scrollbars */
	public BoundedRangeModel getModel() { return model; }


	/** Time axis */
	static public class Time extends Axis {

		/** Combo box model for smoothing selections */
		protected final DefaultComboBoxModel smoothingModel =
			new DefaultComboBoxModel();

		/** Get the smoothing combo box model */
		public ComboBoxModel getSmoothingModel() {
			return smoothingModel;
		}

		/** Flag to prevent spurious repaints */
		protected boolean changing = false;

		/** Is the time axis changing during setExtent? */
		public boolean isChanging() { return changing; }

		/** Smoothing class for putting in a JComboBox */
		public class Smoothing {
			public final int samples;
			protected Smoothing( int s ) {
				samples = s;
			}
			public String toString() {
				int s = samples;
				int h = s / 120;
				s %= 120;
				int m = s / 2;
				s = 30 * ( s % 2 );
				StringBuffer b = new StringBuffer();
				if( h > 0 ) {
					b.append( h ).append( " Hour" );
					if( h > 1 ) b.append( "s" );
				}
				if( m > 0 ) {
					if( b.length() > 0 ) b.append( ", " );
					b.append( m ).append( " Minute" );
					if( m > 1 ) b.append( "s" );
				}
				if( s > 0 ) {
					if( b.length() > 0 ) b.append( ", " );
					b.append( s ).append( " Second" );
					if( s > 1 ) b.append( "s" );
				}
				return b.toString();
			}
		}

		/** Sample smoothing values (# of 30-second samples) */
		static protected final int[] SAMPLES = {
			1, 2, 3, 4, 5, 6, 8, 10, 12, 15, 20, 24, 30, 40, 60, 80, 120
		};

		/** Number of hours per day */
		static protected final int HOURS_PER_DAY = 24;

		/** Number of selectable values per hour */
		static protected final int VALUES_PER_HOUR = 120;

		/** Create a new time axis */
		public Time() {
			super( "Time", "hour of day",
				HOURS_PER_DAY * VALUES_PER_HOUR, null );
			zooms.add( new Zoom( 24 * VALUES_PER_HOUR,
				2 * VALUES_PER_HOUR, 1 ) );
			zooms.add( new Zoom( 16 * VALUES_PER_HOUR,
				VALUES_PER_HOUR, 1 ) );
			zooms.add( new Zoom( 12 * VALUES_PER_HOUR,
				VALUES_PER_HOUR, 1 ) );
			zooms.add( new Zoom( 6 * VALUES_PER_HOUR,
				VALUES_PER_HOUR * 30.0f / 60, 1 ) );
			zooms.add( new Zoom( 3 * VALUES_PER_HOUR,
				VALUES_PER_HOUR * 15.0f / 60, 1 ) );
			zooms.add( new Zoom( VALUES_PER_HOUR,
				VALUES_PER_HOUR * 5.0f / 60, 1 ) );
			setExtent();
		}

		/** Number of lines per hour */
		protected int lines_per_hour = 0;

		/** Set the extent of the model based on the current zoom level */
		protected void setExtent() {
			changing = true;
			int extent = ( (Zoom)zooms.get( zoom ) ).extent;
			smoothingModel.removeAllElements();
			for( int i = 0; i < SAMPLES.length; i++ ) {
				int p = extent / SAMPLES[ i ];
				if( p >= 20 && p < 200 )
					smoothingModel.addElement( new Smoothing( SAMPLES[ i ] ) );
			}
			smoothingModel.setSelectedItem( smoothingModel.getElementAt(
				smoothingModel.getSize() / 2 ) );
			changing = false;
			int value = model.getValue() +
				model.getExtent() / 2 - extent / 2;
			if( value + extent > maxValue ) value = maxValue - extent;
			if( value < 0 ) value = 0;
			model.setRangeProperties( value, extent, 0, maxValue, false );
			lines_per_hour = ( (Zoom)zooms.get( zoom ) ).lines /
				HOURS_PER_DAY;
		}

		/** Check if a particular line should be strong (darker) */
		public boolean isLineStrong( int line ) {
			if( lines_per_hour == 0 ) return true;
			if( line % lines_per_hour == 0 ) return true;
			else return false;
		}

		/** Get the label for a specified line */
		public String getLineLabel( int line ) {
			if( lines_per_hour < 1 ) return String.valueOf( line * 2 );
			int mod = line % lines_per_hour;
			if( mod == 0 ) return String.valueOf( line / lines_per_hour );
			StringBuffer minute = new StringBuffer().append( ":" );
			minute.append( mod * 60 / lines_per_hour );
			while( minute.length() < 3 ) minute.insert( 1, "0" );
			return minute.toString();
		}

		/** Create a smoothed array of points
	 	 * @param start Starting sample index
		 * @param extent Extent (in 30-second samples)
		 * @param smooth Number of 30-second samples to smooth together
		 * @param set Data set index
		 * @param max Maximum value to scale the samples
		 */
		public float[] getSmoothedArray( int start, int extent, int smooth,
			int set, float max )
		{
			int points = SAMPLES_PER_DAY / smooth;
			float[] array = new float[ points ];
			float delta = max / points;
			for( int r = 0; r < points; r++ )
				array[ r ] = ( r + 0.5f ) * delta;
			return array;
		}

		/** Get the number of points to plot */
		public int getSmoothing() {
			int smoothing = 40;
			Smoothing smooth = (Smoothing)smoothingModel.getSelectedItem();
			if( smooth != null ) smoothing = smooth.samples;
			return smoothing;
		}

		/** Get a specific data set (not used by time axis) */
		protected float[] getDataSet( int set ) {
			return null;
		}

		/** Get a string description of the time axis */
		public String toString() {
			int start = model.getValue();
			int stop = start + model.getExtent();
			return formatTime( start ) + " to " + formatTime( stop );
		}

		/** Format a record time into a string */
		public String formatTime( int record ) {
			record %= SAMPLES_PER_DAY;
			if( record == 0 ) return "Midnight";
			if( record == 1440 ) return "Noon";
			StringBuffer b = new StringBuffer();
			int hour = record / 120;
			int pm = hour / 12;
			hour %= 12;
			if( hour == 0 ) hour = 12;
			b.append( hour );
			record %= 120;
			if( record != 0 ) {
				b.append( ":" );
				int minute = record / 2;
				if( minute < 10 ) b.append( "0" );
				b.append( minute );
				int second = ( record % 2 ) * 30;
				if( second != 0 ) {
					b.append( ":" );
					if( second < 10 ) b.append( "0" );
					b.append( second );
				}
			}
			if( pm == 0 ) b.append( " AM" );
			else b.append( " PM" );
			return b.toString();
		}
	}


	/** Flow axis */
	static public class Flow extends Axis {

		/** Create a new flow axis */
		public Flow( PlotSet plot ) {
			super( "Flow", "vehicles / hour", 4000, plot );
			zooms.add( new Zoom( 4000, 1000, 2000 ) );
			zooms.add( new Zoom( 3000, 500, 2000 ) );
			zooms.add( new Zoom( 2500, 500, 2000 ) );
			zooms.add( new Zoom( 2000, 250, 1000 ) );
			zooms.add( new Zoom( 1000, 200, 500 ) );
			zooms.add( new Zoom( 500, 100, 500 ) );
			setExtent();
		}

		/** Get a specific data set */
		protected float[] getDataSet( int set ) {
			PlotData p = plot.getPlotData( set );
			PlotDetector d = p.getDetector();
			Calendar c = p.getCalendar();
			return d.getFlowSet( c );
		}
	}


	/** Headway axis */
	static public class Headway extends Axis {

		/** Create a new headway axis */
		public Headway( PlotSet plot ) {
			super( "Headway", "seconds / vehicle", 30, plot );
			zooms.add( new Zoom( 30, 10, 30 ) );
			zooms.add( new Zoom( 15, 5, 10 ) );
			zooms.add( new Zoom( 10, 2, 10 ) );
			zooms.add( new Zoom( 5, 1, 5 ) );
			setExtent();
		}

		/** Get a specific data set */
		protected float[] getDataSet( int set ) {
			System.out.println("getDataSet()");
			PlotData p = plot.getPlotData( set );
			PlotDetector d = p.getDetector();
			Calendar c = p.getCalendar();
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
		public float[] getSmoothedArray( int start, int extent, int smooth,
			int set, float max )
		{
			int points = extent / smooth;
			float[] array = new float[ points ];
			PlotData pData = plot.getPlotData( set );
			PlotDetector pDet = pData.getDetector();
			Calendar c = pData.getCalendar();
			float[] raw_data = pDet.getFlowSet( c );
			float ratio = max / maxValue;
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
			float[] result = DataFactory.createHeadwaySet(array);
			for(int i=0; i<result.length; i++){
				result[i] = ratio * result[i];
			}
			return result;
		}
	}


	/** Occupancy axis */
	static public class Occupancy extends Axis {

		/** Create a new occupancy axis */
		public Occupancy( PlotSet plot ) {
			super( "Occupancy", "percent", 100, plot );
			zooms.add( new Zoom( 100, 25, 50 ) );
			zooms.add( new Zoom( 60, 10, 50 ) );
			zooms.add( new Zoom( 40, 5, 25 ) );
			zooms.add( new Zoom( 20, 2, 10 ) );
			zooms.add( new Zoom( 10, 1, 5 ) );
			setExtent();
		}

		/** Get a specific data set */
		protected float[] getDataSet( int set ) {
			PlotData p = plot.getPlotData( set );
			PlotDetector d = p.getDetector();
			Calendar c = p.getCalendar();
			return d.getOccupancySet( c );
		}
	}


	/** Density axis */
	static public class Density extends Axis {

		/** Create a new density axis */
		public Density( PlotSet plot ) {
			super( "Density", "vehicles / mile", 200, plot );
			zooms.add( new Zoom( 200, 50, 100 ) );
			zooms.add( new Zoom( 150, 25, 100 ) );
			zooms.add( new Zoom( 100, 25, 50 ) );
			zooms.add( new Zoom( 50, 10, 50 ) );
			zooms.add( new Zoom( 25, 5, 25 ) );
			setExtent();
		}

		/** Get a specific data set */
		protected float[] getDataSet( int set ) {
			PlotData p = plot.getPlotData( set );
			PlotDetector d = p.getDetector();
			Calendar c = p.getCalendar();
			return d.getDensitySet( c );
		}
	}


	/** Speed axis */
	static public class Speed extends Axis {

		/** Create a new speed axis */
		public Speed( PlotSet plot ) {
			super( "Speed", "miles / hour", 100, plot );
			zooms.add( new Zoom( 100, 20, 50 ) );
			zooms.add( new Zoom( 80, 10, 50 ) );
			zooms.add( new Zoom( 60, 10, 50 ) );
			zooms.add( new Zoom( 40, 5, 25 ) );
			zooms.add( new Zoom( 20, 5, 10 ) );
			zooms.add( new Zoom( 10, 1, 5 ) );
			setExtent();
		}

		/** Get a specific data set */
		protected float[] getDataSet( int set ) {
			PlotData p = plot.getPlotData( set );
			PlotDetector d = p.getDetector();
			Calendar c = p.getCalendar();
			return d.getSpeedSet( c );
		}

		/** Create a smoothed array of points
		 * @param start Starting sample index
		 * @param extent Extent (in 30-second samples)
		 * @param smooth Number of 30-second samples to smooth together
		 * @param set Data set index
		 * @param max Maximum value to scale the samples
		 */
		public float[] getSmoothedArray( int start, int extent, int smooth,
			int set, float max )
		{
			try{
				PlotData p = plot.getPlotData(set);
				p.getDetector().getRawSpeedSet(p.getCalendar());
				return super.getSmoothedArray(start, extent, smooth, set, max);
			}catch(IOException ioe){}
			int points = extent / smooth;
			float[] smoothedArray = new float[ points ];
			Axis.Density dAxis = new Axis.Density( plot );
			float[] volumes = getVolumeSet( set );
			float[] densities = dAxis.getDataSet( set );
			float[] speedSet = getDataSet( set );
			float ratio = max / maxValue;
			for( int p = 0; p < points; p++ ) {
				int t = start + p * smooth;
				float volTotal = 0;
				float samples = 0;
				float densTotal = 0;
				for( int r = t; r < t + smooth; r++ ) {
					if( speedSet[ r ] < 0 ) continue;
					volTotal += volumes[r];
					densTotal += densities[r];
					samples++;
				}
				if( samples > 0 ) {
					float density = densTotal / samples;
					float flow =
						volTotal / samples * SAMPLES_PER_HOUR;
					smoothedArray[ p ] =  ratio * flow / density;
				}
				else smoothedArray[ p ] = MISSING_DATA;
			}
			return smoothedArray;
		}

		/** Get a set of data used for calculating weighted averages */
		protected float[] getVolumeSet( int set ) {
			PlotData p = plot.getPlotData( set );
			PlotDetector d = p.getDetector();
			Calendar c = p.getCalendar();
			return d.getVolumeSet( c );
		}

	}

}
