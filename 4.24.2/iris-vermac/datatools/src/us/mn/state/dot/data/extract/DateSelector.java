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
import java.util.Calendar;

import javax.swing.JButton;

import us.mn.state.dot.data.DataFactory;
import us.mn.state.dot.data.DateSelection;

/**
 * DateSelector
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @version   $Revision: 1.17 $ $Date: 2005/08/09 18:52:42 $
 */
public class DateSelector extends Selector implements DateSelection.Listener {

	/** Description of the Field */
	protected DateSelection calendar;


	/**
	 * Create a date selector
	 *
	 * @param f  Description of Parameter
	 * @param e  Description of Parameter
	 */
	public DateSelector( DataFactory f, DataExtract e ) {
		super( f, e );
		init();
	}


	/**
	 * Description of the Method
	 *
	 * @param c  Description of Parameter
	 */
	public void select( Calendar c ) {
		calendar.preselectDate( c );
	}


	/** Clear all the date selections */
	public void clear() {
		calendar.clearAll();
	}


	/**
	 * Called by DateSelection object when a date has been selected
	 *
	 * @param e  Description of Parameter
	 */
	public void dateAdded( DateSelection.Event e ) {
		Calendar c = Calendar.getInstance();
		c.setTime( e.getDate() );
	}


	/**
	 * Called by DateSelection object when a date has been deselected
	 *
	 * @param e  Description of Parameter
	 */
	public void dateRemoved( DateSelection.Event e ) {
		Calendar c = Calendar.getInstance();
		c.setTime( e.getDate() );
	}


	/** Description of the Method */
	protected void init() {
		setLayout( new GridBagLayout() );
		GridBagConstraints c = createConstraints();
		c.gridy = GridBagConstraints.RELATIVE;
		calendar = new DateSelection( factory );
		calendar.addListener( this );
		calendar.setMaxSelectedDays( 1500 );
		add( calendar, c );
		JButton b = new JButton( "Clear" );
		b.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					clear();
				}
			} );
		c.anchor = GridBagConstraints.SOUTHWEST;
		add( b, c );
	}

	public Calendar[] getDates(){
		return calendar.getSelectedDates();
	}
}
