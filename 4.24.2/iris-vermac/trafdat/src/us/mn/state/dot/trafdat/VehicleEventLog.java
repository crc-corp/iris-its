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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Vehicle event log reader / processor.
 *
 * @author Douglas Lau
 */
public class VehicleEventLog {

	/** Get the 30-second period for the given timestamp (ms) */
	static private int getPeriod30Second(int ms) {
		return ms / 30000;
	}

	/** List of all vehicle events in the log */
	private final LinkedList<VehicleEvent> events =
		new LinkedList<VehicleEvent>();

	/** Create a new vehicle event log */
	public VehicleEventLog(BufferedReader reader) throws IOException {
		String line = reader.readLine();
		while (line != null) {
			events.add(new VehicleEvent(line));
			line = reader.readLine();
		}
	}

	/** Propogate timestamps forward to following events */
	public void propogateStampsForward() {
		Integer stamp = null;
		for (VehicleEvent e: events) {
			if (stamp != null)
				e.setPreviousStamp(stamp);
			stamp = e.getStamp();
		}
	}

	/** Propogate timestamps backward to previous events */
	public void propogateStampsBackward() {
		Integer stamp = null;
		ListIterator<VehicleEvent> it =
			events.listIterator(events.size());
		while (it.hasPrevious()) {
			VehicleEvent e = it.previous();
			e.setStamp(stamp);
			stamp = e.getPreviousStamp();
		}
	}

	/** Interpolate timestamps in gaps where they are missing */
	public void interpolateMissingStamps() {
		Integer stamp = null;
		LinkedList<VehicleEvent> ev = new LinkedList<VehicleEvent>();
		for (VehicleEvent e: events) {
			Integer s = e.getStamp();
			if (s == null)
				ev.add(e);
			else if (!ev.isEmpty()) {
				if (stamp != null) {
					int gap = s - stamp;
					int t = ev.size() + 1;
					int headway = Math.round(gap / t);
					for (VehicleEvent v: ev) {
						v.setHeadway(headway);
						v.setPreviousStamp(stamp);
						stamp = v.getStamp();
					}
				}
				ev.clear();
			}
			if (s != null)
				stamp = s;
		}
	}

	/** Bin vehicle event data into 30 second samples */
	public void bin30SecondSamples(SampleBin bin) {
		SampleData sam = new SampleData();
		for (VehicleEvent e: events) {
			Integer stamp = e.getStamp();
			if (e.isReset() || stamp == null)
				sam.setReset();
			else {
				int p = getPeriod30Second(stamp);
				int sp = sam.getPeriod();
				if (sam.isReset())
					sam.clear(p + 1);
				else if (p >= sp) {
					while (p > sp) {
						bin.addSample(sam);
						sp++;
						sam.clear(sp);
					}
					sam.addEvent(e);
				}
			}
		}
		bin.addSample(sam);
	}
}
