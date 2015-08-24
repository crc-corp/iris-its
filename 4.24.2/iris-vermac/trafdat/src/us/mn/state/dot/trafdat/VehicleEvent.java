/*
 * Project: Trafdat
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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
 */
package us.mn.state.dot.trafdat;

/**
 * Vehicle event class
 *
 * @author Douglas Lau
 */
public class VehicleEvent {

	/** Parse an integer from a vehicle log */
	static protected Integer parseInt(String v) {
		try {
			return Integer.valueOf(v);
		}
		catch(NumberFormatException e) {
			return null;
		}
	}

	/** Parse a time stamp from a vehicle log */
	static protected Integer parseStamp(String v) {
		String[] t = v.split(":");
		if(t.length != 3)
			return null;
		try {
			int hour = Integer.parseInt(t[0]);
			int minute = Integer.parseInt(t[1]);
			int second = Integer.parseInt(t[2]);
			if(hour < 0 || hour > 23)
				return null;
			if(minute < 0 || minute > 59)
				return null;
			if(second < 0 || second > 59)
				return null;
			int ms = hour * 3600 + minute * 60 + second;
			return ms * 1000;
		}
		catch(NumberFormatException e) {
			return null;
		}
	}

	/** Is this a reset event? */
	protected boolean reset = false;

	/** Duration vehicle was over detector (ms) */
	protected Integer duration = null;

	/** Headway from start of previous vehicle to this one (ms) */
	protected Integer headway = null;

	/** Time stamp of this event (ms of day 0 - 86.4 million) */
	protected Integer stamp = null;

	/** Vehicle speed (mph) */
	protected Integer speed = null;

	/** Create a new vehicle event */
	public VehicleEvent(String line) {
		String[] f = line.trim().split(",");
		if(f.length == 1 && f[0].equals("*"))
			reset = true;
		if(f.length > 0)
			duration = parseInt(f[0]);
		if(f.length > 1)
			headway = parseInt(f[1]);
		if(f.length > 2)
			stamp = parseStamp(f[2]);
		if(f.length > 3)
			speed = parseInt(f[3]);
	}

	/** Get a string representation of the vehicle event */
	public String toString() {
		if(reset)
			return "*";
		StringBuilder b = new StringBuilder();
		b.append(duration);
		b.append(',');
		b.append(headway);
		b.append(',');
		b.append(stamp);
		b.append(',');
		b.append(speed);
		return b.toString();
	}

	/** Is this a reset event? */
	public boolean isReset() {
		return reset;
	}

	/** Get a timestamp for the previous vehicle event */
	public Integer getPreviousStamp() {
		if(stamp == null || headway == null)
			return null;
		return stamp - headway + 999;
	}

	/** Set headway/timestamp based on previous vehicle stamp */
	public void setPreviousStamp(int pstamp) {
		if(headway != null)
			setStamp(pstamp + headway);
		if(stamp != null)
			setHeadway(stamp - pstamp);
	}

	/** Set the timestamp if not already set */
	public void setStamp(Integer s) {
		if(stamp == null)
			stamp = s;
	}

	/** Get the vehicle event timestamp */
	public Integer getStamp() {
		return stamp;
	}

	/** Set the headway if not already set */
	public void setHeadway(int h) {
		if (headway == null)
			headway = h;
	}

	/** Get the vehicle speed */
	public Integer getSpeed() {
		return speed;
	}
}
