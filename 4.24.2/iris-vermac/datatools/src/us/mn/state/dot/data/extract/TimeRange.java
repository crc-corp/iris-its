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

import java.io.Serializable;

import us.mn.state.dot.data.Axis;
import us.mn.state.dot.data.Constants;

/**
 * Description of the Class
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @version   $Revision: 1.8 $ $Date: 2005/08/15 16:33:04 $
 */
public class TimeRange implements Constants, Serializable {

	/** Description of the Field */
	protected int start;

	/** Description of the Field */
	protected int extent;

	/** Description of the Field */
	protected int smoothing;


	/**
	 * Constructor for the TimeRange object
	 *
	 * @param start      Description of Parameter
	 * @param end        Description of Parameter
	 * @param smoothing  Description of Parameter
	 */
	private TimeRange( int start, int end, int smoothing ) {
		extent = ( end - start ) / smoothing * smoothing;
		this.smoothing = smoothing;
		this.start = start;
	}


	public static TimeRange createTimeRange( int start, int end, int smoothing ) {
		if( smoothing < 1 ) return null;
		if( end <= start ) return null;
		return new TimeRange( start, end, smoothing );
	}


	/**
	 * Gets the start attribute of the TimeRange object
	 *
	 * @return   The start value
	 */
	public int getStart() {
		return start;
	}


	/**
	 * Gets the end attribute of the TimeRange object
	 *
	 * @return   The end value
	 */
	public int getEnd() {
		return start + extent;
	}


	/**
	 * Gets the extent attribute of the TimeRange object
	 *
	 * @return   The extent value
	 */
	public int getExtent() {
		return extent;
	}


	/**
	 * Gets the smoothing attribute of the TimeRange object
	 *
	 * @return   The smoothing value
	 */
	public int getSmoothing() {
		return smoothing;
	}


	/**
	 * Gets the sampleSize attribute of the TimeRange object
	 *
	 * @return   The sampleSize value
	 */
	public int getSampleSize() {
		return extent / smoothing;
	}


	/**
	 * Description of the Method
	 *
	 * @return   Description of the Returned Value
	 */
	public String toString() {
		Axis.Time time = new Axis.Time();
		String a = time.formatTime( start );
		String b = time.formatTime( start + extent );
		String c = getSmoothingString( smoothing );
		return ( "From " + a + " to " + b + " every " + c );
	}


	/**
	 * Gets the extentString attribute of the TimeRange object
	 *
	 * @param value  Description of Parameter
	 * @return       The extentString value
	 */
	protected String getExtentString( int value ) {
		return getSmoothingString( value );
	}


	/**
	 * Gets the smoothingString attribute of the TimeRange object
	 *
	 * @param s  Description of Parameter
	 * @return   The smoothingString value
	 */
	protected String getSmoothingString( int s ) {
		if ( s == 1 ) {
			return "30 seconds";
		}
		if ( s == 2 ) {
			return "1 minute";
		}
		if ( s == 3 ) {
			return "1 minute 30 seconds";
		}
		if ( s == 4 ) {
			return "2 minutes";
		}
		if ( s == 5 ) {
			return "2 minutes 30 seconds";
		}
		if ( s == 6 ) {
			return "3 minutes";
		}
		if ( s == 8 ) {
			return "4 minutes";
		}
		if ( s == 10 ) {
			return "5 minutes";
		}
		if ( s == 12 ) {
			return "6 minutes";
		}
		if ( s == 15 ) {
			return "7 minutes 30 seconds";
		}
		if ( s == 20 ) {
			return "10 minutes";
		}
		if ( s == 30 ) {
			return "15 minutes";
		}
		if ( s == 60 ) {
			return "30 minutes";
		}
		if ( s == 120 ) {
			return "1 hour";
		}
		return "";
	}
}
