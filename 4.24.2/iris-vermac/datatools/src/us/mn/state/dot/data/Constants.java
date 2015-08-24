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

/**
 * Constants is an interface with some traffic data related constants.
 *
 * @author Douglas Lau
 */
public interface Constants {

	/** Number of seconds per data sample */
	public int SECONDS_PER_SAMPLE = 30;

	/** Number of seconds in one hour */
	public int SECONDS_PER_HOUR = 3600;

	/** Number of data samples in one day */
	public int SAMPLES_PER_DAY = 86400 / SECONDS_PER_SAMPLE;

	/** Number of data samples in one hour */
	public int SAMPLES_PER_HOUR = SAMPLES_PER_DAY / 24;

	/** Number of feet in one mile */
	public int FEET_PER_MILE = 5280;

	/** Maximum "realistic" volume for a 30-second sample */
	public int MAX_VOLUME = 37;

	/** Maximum number of scans in 30 seconds */
	public int MAX_SCANS = 1800;

	/** Maximum occupancy value (100%) */
	public int MAX_OCCUPANCY = 100;

	/** Valid density threshold for speed calculation */
	public float DENSITY_THRESHOLD = 1.2f;

	/** Maximum (valid) speed (miles per hour) */
	public float MAX_SPEED = 100.0f;

	/** Default average detector field length */
	public float DEFAULT_FIELD_LENGTH = 22.0f;

	/** Special case value for missing data */
	public byte MISSING_DATA = -1;

	/** Constant for NULL */
	public final static int NULL = -1;

	/** Constant for the volume data set */
	public final static String VOLUME = "Volume";
	/** Constant for the occupancy data set */
	public final static String OCCUPANCY = "Occupancy";
	/** Constant for the speed data set */
	public final static String SPEED = "Speed";
	/** Constant for the flow data set */
	public final static String FLOW = "Flow";
	/** Constant for the headway data set */
	public final static String HEADWAY = "Headway";
	/** Constant for the density data set */
	public final static String DENSITY = "Density";

	/** The detectors set */
	public final static int DETECTORS = 20;
	/** The times set */
	public final static int TIMES = 21;
	/** The data set */
	public final static int DATA = 22;
	/** The dates set */
	public final static int DATES = 23;

	/** An output option for data aggregation */
	public final int AGG_SUM = 30;
	/** An output option for data aggregation */
	public final int AGG_AVERAGE = 31;
	/** An output option for data aggregation */
	public final int AGG_DAY_MEDIAN = 32;
	/** An output option for data aggregation */
	public final int AGG_TIME_MEDIAN = 33;
}
