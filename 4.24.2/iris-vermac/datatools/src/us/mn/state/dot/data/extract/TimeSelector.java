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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import us.mn.state.dot.data.DataFactory;

/**
 * TimeSelector
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @version   $Revision: 1.27 $ $Date: 2005/08/09 19:04:30 $
 */
public class TimeSelector extends Selector {

	/** Description of the Field */
	protected JComboBox extentBox, smoothingBox;

	/** Description of the Field */
	protected Collection smoothingOptions, extentOptions;

	protected final Collection selectedRanges;
	
	/** Description of the Field */
	protected JTextField startField, endField;

	/** Description of the Field */
	protected JList rangeList;


	/**
	 * Create a time selector
	 *
	 * @param f  Description of Parameter
	 * @param e  Description of Parameter
	 */
	public TimeSelector( DataFactory f, DataExtract e ) {
		super( f, e );
		selectedRanges = new TreeSet(new Comparator(){
			public int compare(Object o1, Object o2){
				TimeRange r1 = (TimeRange)o1;
				TimeRange r2 = (TimeRange)o2;
				return r1.toString().compareTo(r2.toString());
			}
		});
		extentOptions = createTimeFrameOptions();
		smoothingOptions = createTimeFrameOptions();
		rangeList = new JList(selectedRanges.toArray());
		init();
	}


	/**
	 * Get a set of all integers found in the string s
	 *
	 * @param s  Description of Parameter
	 * @return   The ints value
	 */
	public int[] getInts( String s ) {
		Vector tokens = new Vector();
		StringBuffer chars = new StringBuffer( s );
		StringBuffer intBuffer = new StringBuffer();
		for ( int i = 0; i < chars.length(); i++ ) {
			if ( Character.isDigit( chars.charAt( i ) ) ) {
				intBuffer.append( chars.charAt( i ) );
			} else {
				if ( intBuffer.length() > 0 ) {
					tokens.addElement( intBuffer.toString() );
					intBuffer = new StringBuffer();
				}
			}
		}
		if ( intBuffer.length() > 0 ) {
			tokens.addElement( intBuffer.toString() );
		}
		int[] ints = new int[tokens.size()];
		for ( int i = 0; i < tokens.size(); i++ ) {
			ints[i] = Integer.parseInt( ( String ) ( tokens.elementAt( i ) ) );
		}
		return ints;
	}

	private void removeRange(TimeRange r){
		selectedRanges.remove(r);
		updateList();
	}

	private void updateList(){
		DefaultComboBoxModel m =
			new DefaultComboBoxModel(selectedRanges.toArray());
		rangeList.setModel(m);
	}
	
	public void select(TimeRange r){
		selectedRanges.add(r);
		updateList();
	}
	
	public void setTimeRanges(TimeRange[] array){
		selectedRanges.clear();
		for(int i=0; i<array.length; i++){
			selectedRanges.add(array[i]);
		}
		updateList();
	}
	
	public TimeRange[] getTimeRanges(){
		return (TimeRange[])(selectedRanges.toArray(new TimeRange[0]));
	}
	
	/** Description of the Method */
	public void clear() {
		selectedRanges.clear();
		updateList();
	}


	public void setEnabled( boolean enabled ) {
		startField.setEnabled( enabled );
		endField.setEnabled( enabled );
		smoothingBox.setEnabled( enabled );
	}

	/**
	 * Create an integer representing the number of 30 second intervals in the day
	 * before the time represented by s
	 *
	 * @param s  Description of Parameter
	 * @return   Description of the Returned Value
	 */
	public int parseTimeString( String s ) {
		int[] ints = getInts( s );
		int intervals = 0;
		if ( ints == null || ints.length > 2 ) {
			throw new NumberFormatException(
					"Too many integers.  Please enter hours and minutes only." );
		} else if ( ints.length == 2 ) {
			if ( ints[0] > 24 || ints[1] > 60 ) {
				throw new NumberFormatException(
						"The hour and/or minutes is/are not valid." );
			}
			intervals = ( 120 * ints[0] ) + ( 2 * ints[1] );
		} else if ( ints.length == 1 ) {
			if ( ints[0] > 2400 ) {
				throw new NumberFormatException(
						"The hour is not acceptable." );
			}
			if ( ints[0] > 24 && ints[0] < 100 ) {
				throw new NumberFormatException(
						"The hour is not acceptable." );
			}
			if ( ints[0] <= 24 ) {
				intervals = 120 * ints[0];
			} else if ( ints[0] >= 100 ) {
				int minutes = ints[0] - ( ints[0] / 100 * 100 );
				if ( minutes > 59 ) {
					throw new NumberFormatException(
							"The minutes must be between 0 and 59, inclusive." );
				}
				int hours = ints[0] / 100;
				intervals = ( 120 * hours ) + ( 2 * minutes );
			} else {
				throw new NumberFormatException(
						"Check start and end times carefully." +
						" One or both is not acceptable." );
			}
		}
		if ( intervals > 2880 ) {
			throw new NumberFormatException(
					"Check start and end times carefully." +
					" One or both is not acceptable." );
		}
		return intervals;
	}

	/**
	 * Description of the Method
	 *
	 * @param s  Description of Parameter
	 * @return   Description of the Returned Value
	 */
	public int parseSmoothing( String s ) {
		if ( s.equals( "30 seconds" ) ) {
			return 1;
		}
		if ( s.equals( "1 minute" ) ) {
			return 2;
		}
		if ( s.equals( "1 minute 30 seconds" ) ) {
			return 3;
		}
		if ( s.equals( "2 minutes" ) ) {
			return 4;
		}
		if ( s.equals( "2 minutes 30 seconds" ) ) {
			return 5;
		}
		if ( s.equals( "3 minutes" ) ) {
			return 6;
		}
		if ( s.equals( "4 minutes" ) ) {
			return 8;
		}
		if ( s.equals( "5 minutes" ) ) {
			return 10;
		}
		if ( s.equals( "6 minutes" ) ) {
			return 12;
		}
		if ( s.equals( "7 minutes 30 seconds" ) ) {
			return 15;
		}
		if ( s.equals( "10 minutes" ) ) {
			return 20;
		}
		if ( s.equals( "15 minutes" ) ) {
			return 30;
		}
		if ( s.equals( "30 minutes" ) ) {
			return 60;
		}
		if ( s.equals( "1 hour" ) ) {
			return 120;
		} else {
			return 0;
		}
	}


	/**
	 * Description of the Method
	 *
	 * @return   Description of the Returned Value
	 */
	public Vector createTimeFrameOptions() {
		Vector v = new Vector();
		v.add( "30 seconds" );
		v.add( "1 minute" );
		v.add( "1 minute 30 seconds" );
		v.add( "2 minutes" );
		v.add( "2 minutes 30 seconds" );
		v.add( "3 minutes" );
		v.add( "4 minutes" );
		v.add( "5 minutes" );
		v.add( "6 minutes" );
		v.add( "7 minutes 30 seconds" );
		v.add( "10 minutes" );
		v.add( "15 minutes" );
		v.add( "30 minutes" );
		v.add( "1 hour" );
		return v;
	}


	/** Description of the Method */
	protected void init() {
		setLayout( new GridBagLayout() );
		GridBagConstraints c = createConstraints();
		JLabel label = new JLabel( "Start: " );
		add( label, c );
		c.gridy = GridBagConstraints.RELATIVE;
		label = new JLabel( "End: " );
		add( label, c );
		label = new JLabel( "Smoothing: " );
		add( label, c );
		c.gridx = 1;
		c.gridy = 0;
		startField = new JTextField();
		startField.setMinimumSize(MIN_FIELD_DIM);
		startField.setPreferredSize(MIN_FIELD_DIM);
		add( startField, c );
		c.gridy = GridBagConstraints.RELATIVE;
		endField = new JTextField();
		endField.setMinimumSize(MIN_FIELD_DIM);
		endField.setPreferredSize(MIN_FIELD_DIM);
		add( endField, c );
		smoothingBox = new JComboBox( smoothingOptions.toArray() );
		add( smoothingBox, c );
		c.gridx = 2;
		c.gridy = 0;
		JButton b = new JButton( "Add" );
		b.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent ae ) {
					TimeRange range = createTimeRange();
					select(range);
					startField.setText( "" );
					endField.setText( "" );
					startField.requestFocus();
				}
			} );
		add(b, c);
		c.gridy = GridBagConstraints.RELATIVE;
		b = new JButton( "Remove" );
		b.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent ae ) {
					TimeRange range = ( TimeRange ) ( rangeList.getSelectedValue() );
					removeRange(range);
				}
			} );
		add(b, c);
		c.gridx = 0;
		c.gridy = 3;
		c.gridheight = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		JScrollPane scroll = new JScrollPane( rangeList );
		add( scroll, c );
		b = new JButton( "Clear" );
		b.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent ae ) {
					clear();
				}
			} );
		c.gridy = 4;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.SOUTHWEST;
		add(b, c);
	}


	/**
	 * Description of the Method
	 *
	 * @return   Description of the Returned Value
	 */
	protected TimeRange createTimeRange() {
		TimeRange range = null;
		try {
			int a = parseTimeString( startField.getText() );
			int b = parseTimeString( endField.getText() );
			int c = parseSmoothing( smoothingBox.getSelectedItem().toString() );
			if ( b <= a ) {
				throw new NumberFormatException(
						"End time must be after begin time." );
			}
			if ( ( b - a ) < c ) {
				throw new NumberFormatException(
						"Unacceptable smoothing value for given times." );
			}
			range = TimeRange.createTimeRange( a, b, c );
		} catch ( NumberFormatException nfe ) {
			new ExtractExceptionDialog( nfe ).setVisible( true );
		}
		return range;
	}
}
