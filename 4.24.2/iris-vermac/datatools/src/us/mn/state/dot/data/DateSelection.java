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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
  * DateSelection: A generic component for selecting one or more days,
  * using a visual calendar.
  *
  * @author Douglas Lau
  */
public final class DateSelection extends JPanel {

	/** Create a date formatter for the date selection title */
	static protected final SimpleDateFormat formatter =
		new SimpleDateFormat( "MMMM, yyyy" );

	/** Date added event type */
	static public final int DATE_ADDED = 1;

	/** Date removed event type */
	static public final int DATE_REMOVED = 2;


	/** Date selection event */
	public class Event extends EventObject {

		/** Event type (DATE_ADDED or DATE_REMOVED) */
		protected final int type;

		/** Get the event type (DATE_ADDED or DATE_REMOVED) */
		public int getType() { return type; }

		/** Date being added or removed */
		protected final Calendar calendar;

		/** Get the date */
		public Date getDate() { return calendar.getTime(); }

		/** Create a new date selection event
		  * @param source DateSelection which is the source of this event
		  * @param t Event type (DATE_ADDED or DATE_REMOVED)
		  */
		protected Event( DateSelection source, int t, Calendar c ) {
			super( source );
			type = t;
			calendar = c;
		}
	}


	/** Date selection event listener interface */
	public interface Listener {

		/** Called when a date is added to the selection */
		public void dateAdded( Event e );

		/** Called when a date is removed from the selection */
		public void dateRemoved( Event e );
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
			Listener newA = DateSelection.remove( a, l );
			Listener newB = DateSelection.remove( b, l );
			if( newA == a && newB == b ) return this;
			return add( newA, newB );
		}

		/** Handle the dateAdded event by calling the dateAdded methods on
		  * both listeners contained within this multicaster.
		  * @param e the date added event */
		public void dateAdded( Event e ) {
			a.dateAdded( e );
			b.dateAdded( e );
		}

		/** Handle the dateRemoved event by calling the dateRemoved methods on
		  * both listeners contained within this multicaster.
		  * @param e the date removed event */
		public void dateRemoved( Event e ) {
			a.dateRemoved( e );
			b.dateRemoved( e );
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


	/** Calendar object for the current month */
   protected final Calendar month = Calendar.getInstance();

	/** Previous year button */
	protected final JButton previousYear = new JButton( "<<" );

	/** Previous month button */
	protected final JButton previousMonth = new JButton( "<" );

	/** Date selection label (month, year) */
	protected final JLabel label = new JLabel( "", JLabel.CENTER );

	/** Next month button */
	protected final JButton nextMonth = new JButton( ">" );

	/** Next year button */
	protected final JButton nextYear = new JButton( ">>" );

	/** Date button grid */
	protected final JPanel buttonGrid;

	/** Date button grid spacers */
	protected final JLabel[] spacer = new JLabel[ 8 ];

	/** Date button array */
	protected final JToggleButton dayButton[] = new JToggleButton[ 31 ];

	/** Clear all dates button */
	protected final JButton clearAll = new JButton( "Clear All" );

	/** Date selection event listeners */
	protected Listener listeners;

	/** Set of selected days */
	protected final TreeSet<Date> selected = new TreeSet<Date>();

	/** Maximum number of selected days */
	protected int maxSelectedDays = 10;

	/** Set the maximum number of selected days */
	public void setMaxSelectedDays( int m ) {
		maxSelectedDays = m;
		if( selected.size() >= maxSelectedDays )
			disableMoreSelections();
		else enableMoreSelections();
	}

	/** Date selectable checker */
	protected final DateChecker checker;


	/** Create a new date selection component */
	public DateSelection( DateChecker ch ) {
		checker = ch;
		month.set( Calendar.DAY_OF_MONTH, 1 );
		Box vBox = Box.createVerticalBox();
		Box box = Box.createHorizontalBox();
		previousYear.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				month.add( Calendar.YEAR, -1 );
				initialize();
			}
		} );
		box.add( previousYear );
		box.add( Box.createHorizontalStrut( 4 ) );
		previousMonth.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				month.add( Calendar.MONTH, -1 );
				initialize();
			}
		} );
		box.add( previousMonth );
		box.add( Box.createHorizontalGlue() );
		box.add( label );
		box.add( Box.createHorizontalGlue() );
		nextMonth.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				month.add( Calendar.MONTH, 1 );
				initialize();
			}
		} );
		box.add( nextMonth );
		box.add( Box.createHorizontalStrut( 4 ) );
		nextYear.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				month.add( Calendar.YEAR, 1 );
				initialize();
			}
		} );
		box.add( nextYear );
		vBox.add( box );
		buttonGrid = createButtonGrid();
		vBox.add( buttonGrid );
		add( vBox );
		initialize();
	}

	/** Deselect all dates */
	public void clearAll() {
		Calendar c = Calendar.getInstance();
		for(Date d : selected) {
			c.setTime(d);
			processDateRemovedEvent(c);
		}
		selected.clear();
		initialize();
	}

	/** Get the current selection set */
	public Calendar[] getSelectedDates() {
		Calendar[] result = new Calendar[selected.size()];
		int i=0;
		for(Date d : selected) {
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			result[i++] = c;
		}
		return result;
	}

	/** Select calendar dates at startup */
	public void preselectDate(Calendar c) {
		while (month.get(Calendar.YEAR)!=c.get(Calendar.YEAR)) {
			if (month.get(Calendar.YEAR)>c.get(Calendar.YEAR)) {
				previousYear.doClick();
			}
			else {
				nextYear.doClick();
			}
		}
		while (month.get(Calendar.MONTH)!=c.get(Calendar.MONTH)) {
			if (month.get(Calendar.MONTH)>c.get(Calendar.MONTH)) {
				previousMonth.doClick();
			}
			else {
				nextMonth.doClick();
			}
		}
		JToggleButton tempButton = dayButton[c.get(Calendar.DAY_OF_MONTH)-1];
		//tempButton.setEnabled(true);
		tempButton.doClick();
	}

	/** Create a grid of buttons (one for each day of the month) */
	protected JPanel createButtonGrid() {
		GridLayout grid = new GridLayout( 0, 7 );
		JPanel panel = new JPanel( grid );
		panel.add( new JLabel( "S", JLabel.CENTER ) );
		panel.add( new JLabel( "M", JLabel.CENTER ) );
		panel.add( new JLabel( "Tu", JLabel.CENTER ) );
		panel.add( new JLabel( "W", JLabel.CENTER ) );
		panel.add( new JLabel( "Th", JLabel.CENTER ) );
		panel.add( new JLabel( "F", JLabel.CENTER ) );
		panel.add( new JLabel( "S", JLabel.CENTER ) );
		for( int s = 0; s < 8; s++ )
			spacer[ s ] = new JLabel();
		for( int d = 0; d < 31; d++ ) {
			final int day = d + 1;
			final JToggleButton b = new JToggleButton( String.valueOf( day ) );
			dayButton[ d ] = b;
			dayButton[ d ].addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					dayButtonToggled( b, day );
				}
			} );
		}
		return panel;
	}

	/** Performed whenever a day button is toggled */
	protected void dayButtonToggled( JToggleButton button, int day ) {
		int y = month.get( Calendar.YEAR );
		int m = month.get( Calendar.MONTH );
		GregorianCalendar calendar =
			new GregorianCalendar( y, m, day, 0, 0, 0 );
		if( button.isSelected() ) {
			selected.add( calendar.getTime() );
			if( selected.size() >= maxSelectedDays )
				disableMoreSelections();
			processDateAddedEvent( calendar );
		}
		else {
			selected.remove( calendar.getTime() );
			if( selected.size() < maxSelectedDays )
				enableMoreSelections();
			processDateRemovedEvent( calendar );
		}
	}

	/** Enable more date selections */
	protected void enableMoreSelections() {
		int y = month.get( Calendar.YEAR );
		int m = month.get( Calendar.MONTH );
		GregorianCalendar c =
			new GregorianCalendar( y, m, 1, 0, 0, 0 );
		for( int d = 0; d < 31; d++ ) {
			c.set( Calendar.DAY_OF_MONTH, d + 1 );
			dayButton[ d ].setEnabled( checker.isAvailable( c ) );
		}
	}

	/** Disable any more date selections */
	protected void disableMoreSelections() {
		int y = month.get( Calendar.YEAR );
		int m = month.get( Calendar.MONTH );
		GregorianCalendar c =
			new GregorianCalendar( y, m, 1, 0, 0, 0 );
		for( int d = 0; d < 31; d++ ) {
			if( dayButton[ d ].isSelected() ) {
				c.set(Calendar.DAY_OF_MONTH, d + 1);
				dayButton[ d ].setEnabled( checker.isAvailable( c ) );
			}
			else {
				dayButton[ d ].setEnabled( false );
			}
		}
	}

	/** Initialize the date selection component */
	protected void initialize() {
		label.setText( formatter.format( month.getTime() ) );
		for( int s = 0; s < 8; s++ )
			buttonGrid.remove( spacer[ s ] );
		for( int day = 0; day < 31; day++ ) {
			buttonGrid.remove( dayButton[ day ] );
			dayButton[ day ].setSelected( false );
		}
		int y = month.get( Calendar.YEAR );
		int m = month.get( Calendar.MONTH );
		Calendar c = Calendar.getInstance();
		for(Date d : selected) {
			c.setTime(d);
			if( c.get( Calendar.YEAR ) == y ) {
				if( c.get( Calendar.MONTH ) == m ) {
					int day = c.get( Calendar.DATE ) - 1;
					dayButton[ day ].setSelected( true );
				}
			}
		}
		if( selected.size() < maxSelectedDays )
			enableMoreSelections();
		else
			disableMoreSelections();
		int skip = month.get( Calendar.DAY_OF_WEEK ) - 1;
		for( int s = 0; s < skip; s++ )
			buttonGrid.add( spacer[ s ] );
		for( int day = 0; day < month.getActualMaximum( Calendar.DATE ); day++ )
			buttonGrid.add( dayButton[ day ] );
		for( int s = skip; s < 8; s++ )
			buttonGrid.add( spacer[ s ] );
		buttonGrid.repaint();
	}

	/** Process a "date added" event */
	protected void processDateAddedEvent( Calendar c ) {
		if( listeners != null ) {
			Event e = new Event( this, DATE_ADDED, c );
			listeners.dateAdded( e );
		}
	}

	/** Process a "date removed" event */
	protected void processDateRemovedEvent( Calendar c ) {
		if( listeners != null ) {
			Event e = new Event( this, DATE_REMOVED, c );
			listeners.dateRemoved( e );
		}
	}

	/** Add a DateSelection.Listener */
	public void addListener( Listener l ) {
		listeners = add( listeners, l );
	}

	/** Remove a DateSelection.Listener */
	public void removeListener( Listener l ) {
		listeners = remove( listeners, l );
	}
}
