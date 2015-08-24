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

import javax.swing.JScrollBar;

/**
 * Axis scroll bar to handle unit increment stuff
 *
 * @author Douglas Lau
 */
public class AxisScrollBar extends JScrollBar {

	/** Underlying axis object */
	protected final Axis axis;

	/** Create a new axis scroll bar */
	public AxisScrollBar( Axis a, int orientation ) {
		super( orientation );
		axis = a;
		setModel( axis.getModel() );
	}

	/** Get the amount of change needed to scroll to the next line */
	public int getUnitIncrement( int direction ) {
		int value = getValue();
		int lines = axis.getLines();
		int max = getMaximum();
		int line;
		if( direction > 0 ) line = ( value + 1 ) * lines / max + 1;
		else line = ( value - 1 ) * lines / max;
		return Math.abs( value - max * line / lines );
	}

	/** Get the amount of change needed to scroll to the next page */
	public int getBlockIncrement( int direction ) {
		return getVisibleAmount();
	}
}
