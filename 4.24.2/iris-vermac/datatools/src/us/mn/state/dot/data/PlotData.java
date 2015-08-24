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

import java.util.Calendar;
import java.util.Date;

/**
 * PlotData
 *
 * @author Douglas Lau
 */
public class PlotData implements Constants {

	/** Date to plot */
	private final Date date;

	/** Get the calendar date to plot */
	public Calendar getCalendar() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime( date );
		return calendar;
	}

	/** Detector to plot */
	private final PlotDetector detector;

	/** Get the detector to plot */
	public PlotDetector getDetector() { return detector; }

	/** Create a new plot data */
	public PlotData( Date day, PlotDetector det ) {
		date = day;
		detector = det;
	}

	/** Get the total volume for this plot data */
	public int getTotalVolume() {
		return detector.getTotalVolume( getCalendar() );
	}

	/** Get the percentage of data sampled in the whole day */
	public float getSample() {
		float[] volume = detector.getVolumeSet( getCalendar() );
		int sample = 0;
		for( int i = 0; i < volume.length; i++ )
			if( volume[ i ] != MISSING_DATA ) sample++;
		return (float)sample / volume.length;
	}
}
