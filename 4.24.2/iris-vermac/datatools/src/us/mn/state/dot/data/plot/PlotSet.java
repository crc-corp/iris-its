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
package us.mn.state.dot.data.plot;

import java.awt.Color;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;

import us.mn.state.dot.data.DateSelection;
import us.mn.state.dot.data.PlotData;
import us.mn.state.dot.data.PlotDetector;

/**
 * A set of detector/date pairs to plot
 *
 * @author Douglas Lau
 */
public final class PlotSet implements DateSelection.Listener {

	/** Maximum number of data sets to plot */
	static protected final int MAX_PLOTS = 10;

	/** Full date formatter for the date of a plot */
	static protected final DateFormat FORMAT_FULL =
		DateFormat.getDateInstance( DateFormat.FULL );

	/** Shorted date formatter for the date of a plot */
	static protected final DateFormat FORMAT_SHORTER =
		new SimpleDateFormat( "EE, MMM d, yyyy" );

	/** Get a generic number formatter */
	static protected final NumberFormat NUMBER =
		NumberFormat.getNumberInstance();
	static {
		NUMBER.setMaximumFractionDigits( 1 );
	}

	/** Graph plotting colors */
	static protected final Color COLOR[] = new Color[] {
		new Color( 0.65f, 0.0f, 0.0f ),
		new Color( 0.0f, 0.5f, 0.0f ),
		new Color( 0.0f, 0.0f, 0.75f ),
		new Color( 0.0f, 0.5f, 0.5f ),
		new Color( 0.7f, 0.0f, 0.7f ),
		new Color( 0.5f, 0.5f, 0.1f ),
		new Color( 0.1f, 0.1f, 0.1f ),
		new Color( 0.7f, 0.5f, 0.0f ),
		new Color( 0.5f, 0.3f, 0.7f ),
		new Color( 0.0f, 0.4f, 0.7f )
	};

	/** Selection changed event type */
	static public final int SELECTION_CHANGED = 1;

	/** Plot changed event type */
	static public final int PLOT_CHANGED = 2;


	/** Plot changed event */
	public class Event extends EventObject {

		/** Event type (SELECTION_CHANGED or PLOT_CHANGED) */
		protected final int type;

		/** Get the event type (SELECTION_CHANGED or PLOT_CHANGED) */
		public int getType() { return type; }

		/** Create a new plot change event
		  * @param source PlotSet which is the source of this event
		  * @param t Event type (SELECTION_CHANGED or PLOT_CHANGED)
		  */
		protected Event( PlotSet source, int t ) {
			super( source );
			type = t;
		}
	}


	/** PlotSet change event listener interface */
	public interface Listener {

		/** Called when the plot selection is changed */
		public void selectionChanged( Event e );

		/** Called when the plot set is changed */
		public void plotChanged( Event e );
	}


	/** EventMulticaster */
	protected class EventMulticaster implements Listener {

		/** Date selection listener a */
		protected final Listener a;

		/** Date selection listener b */
		protected final Listener b;

		/** Create a new EventMulticaster, composed of two other Listeners */
		protected EventMulticaster( Listener a, Listener b ) {
			this.a = a;
			this.b = b;
		}

		/** Remove a listener from this multicaster, returning the new multicaster */
		protected Listener remove( Listener l ) {
			if( l == a ) return b;
			if( l == b ) return a;
			Listener newA = PlotSet.remove( a, l );
			Listener newB = PlotSet.remove( b, l );
			if( newA == a && newB == b ) return this;
			return add( newA, newB );
		}

		/** Handle the selectionChanged event by calling the
		  * selectionChangedmethods on both listeners contained within
		  * this multicaster.
		  * @param e the selection changed event */
		public void selectionChanged( Event e ) {
			a.selectionChanged( e );
			b.selectionChanged( e );
		}

		/** Handle the plotChanged event by calling the plotChanged
		  * methods on both listeners contained within this multicaster.
		  * @param e the plot changed event */
		public void plotChanged( Event e ) {
			a.plotChanged( e );
			b.plotChanged( e );
		}
	}


	/** Add a listener to a set of listeners (should be static) */
	protected Listener add( Listener a, Listener b ){
		if( a == null ) return b;
		if( b == null ) return a;
		return new EventMulticaster( a, b );
	}

	/** Remove a listener from a set of listeners */
	protected static Listener remove( Listener l, Listener old ) {
		if( l == null || l == old )
			return null;
		if( l instanceof EventMulticaster )
			return ((EventMulticaster)l).remove( old );
		return l;
	}

	/** Plot set event listeners */
	protected Listener listeners;

	/** Process a "selection changed" event */
	protected void processSelectionChangedEvent() {
		if( listeners != null ) {
			Event e = new Event( this, SELECTION_CHANGED );
			listeners.selectionChanged( e );
		}
	}

	/** Process a "plot changed" event */
	protected void processPlotChangedEvent() {
		if( listeners != null ) {
			Event e = new Event( this, PLOT_CHANGED );
			listeners.plotChanged( e );
		}
	}

	/** Add a PlotSet.Listener */
	public void addListener( Listener l ) {
		listeners = add( listeners, l );
	}

	/** Remove a PlotSet.Listener */
	public void removeListener( Listener l ) {
		listeners = remove( listeners, l );
	}

	/** Title of the plot set */	
	protected String title = "No selection";

	/** Get the title of the plot set */
	public String getTitle() { return title; }

	/** Title of the plot set table's column */
	protected String columnName = "N/A";

	/** Get the title of the plot set table's column */
	public String getColumnName() { return columnName; }
		
	/** List of dates in the set */
	protected final ArrayList<Date> dates = new ArrayList<Date>();

	/** List of detectors in the set */
	protected final ArrayList<PlotDetector> detectors = new ArrayList<PlotDetector>();

	/** Get the detector for a specified row */
	public PlotDetector getDetector( int row ) {
		return (PlotDetector)detectors.get( row );
	}

	/** List of plots in the set */
	protected final ArrayList<PlotData> plots = new ArrayList<PlotData>();

	/** List containing the rows in the set */
	protected ArrayList rows = detectors;

	/** Get the number of plots */
	public int getPlotCount() { return plots.size(); }

	/** Get a specified plot */
	public PlotData getPlotData( int set ) {
		return (PlotData)plots.get( set );
	}

	/** Get the number of rows in the plot set */
	public int getRowCount() { return rows.size(); }

	/** Get the name for a specified row */
	public String getRowName( int row ) {
		if( rows == detectors ) return detectors.get( row ).toString();
		else return FORMAT_SHORTER.format( (Date)dates.get( row ) );
	}

	/** Get the field length string for a specified row */
	public String getRowField( int row ) {
		int index = 0;
		if( rows == detectors ) index = row;
		else if( detectors.isEmpty() ) return "N/A";
		float field = ( (PlotDetector)detectors.get( index ) ).getField();
		return NUMBER.format( field ) + "'";
	}

	/** Get the color of a specified set */
	public Color getColor( int set ) {
		return COLOR[ set ];
	}

	/** Date selection component */
	protected final DateSelection sel;

	/** Create a new plot set */
	public PlotSet( DateSelection s ) {
		sel = s;
		sel.addListener( this );
	}

	/** Process a date added event */
	public void dateAdded( DateSelection.Event e ) {
		if( dates.size() > 0 )
			if( detectors.size() > 1 || dates.size() >= MAX_PLOTS ) return;
		Date date = e.getDate();
		dates.add( date );
		doChanges( detectors.size() > 0 );
	}

	/** Process a date removed event */
	public void dateRemoved( DateSelection.Event e ) {
		if( !dates.remove( e.getDate() ) ) return;
		doChanges( plots.size() > 0 );
	}

	/** Process a detector added event */
	public boolean detectorAdded( PlotDetector det ) {
		if( detectors.size() > 0 )
			if( dates.size() > 1 || detectors.size() >= MAX_PLOTS )
				return false;
		detectors.add( det );
		if( detectors.size() > 1 ) sel.setMaxSelectedDays( 1 );
		doChanges( dates.size() > 0 );
		if( detectors.size() >= MAX_PLOTS ) return false;
		return true;
	}

	/** Process a detector removed event */
	public void detectorRemoved( PlotDetector det ) {
		if( !detectors.remove( det ) ) return;
		if( detectors.size() == 1 ) sel.setMaxSelectedDays( MAX_PLOTS );
		doChanges( plots.size() > 0 );
	}

	/** Process a detectors cleared event */
	public void detectorsCleared() {
		detectors.clear();
		sel.setMaxSelectedDays( MAX_PLOTS );
		doChanges( plots.size() > 0 );
	}

	/** Do all necessary changes to plot set */
	protected void doChanges( boolean plotChanged ) {
		doSelectionChanged();
		if( plotChanged ) doPlotChanged();
		processSelectionChangedEvent();
		if( plotChanged ) processPlotChangedEvent();
	}
	
	/** Do a plot changed thingy */
	protected void doPlotChanged() {
		plots.clear();
		for(Date date : dates) {
			for(PlotDetector det : detectors) {
				plots.add( new PlotData( date, det ) );
			}
		}
	}

	/** Do a selection changed thingy */
	protected void doSelectionChanged() {
		title = "No selection";
		rows = detectors;
		columnName = "N/A";
		if( dates.size() == 1 || detectors.size() > 1 ) {
			if( dates.size() == 1 )
				title = "Date: " + FORMAT_FULL.format( dates.get( 0 ) );
			else title = "Date: No selection";
			rows = detectors;
			columnName = "Detector";
		}
		else if( detectors.size() == 1 || dates.size() > 1 ) {
			if( detectors.size() == 1 )
				title = "Detector: " + detectors.get( 0 ).toString();
			else title = "Detector: No selection";
			rows = dates;
			columnName = "Date";
		}
	}

	/** Refresh the data by clearing the caches */
	public void refresh() {
		for(PlotDetector plot : detectors) {
			plot.refresh();
		}
		processSelectionChangedEvent();
		processPlotChangedEvent();
	}
}
