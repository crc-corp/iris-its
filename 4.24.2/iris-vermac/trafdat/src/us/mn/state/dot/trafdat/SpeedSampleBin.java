/*
 * Project: Trafdat
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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
 * Bin for storing speed sample data
 *
 * @author Douglas Lau
 */
public class SpeedSampleBin implements SampleBin {

	/** Binned 30-second speed data */
	protected final byte[] spd = new byte[SAMPLES_PER_DAY];

	/** Create a new speed sample bin */
	public SpeedSampleBin() {
		for(int i = 0; i < SAMPLES_PER_DAY; i++)
			spd[i] = SampleData.MISSING_DATA;
	}

	/** Add one data sample to the bin */
	public void addSample(SampleData sam) {
		byte s = (byte)sam.getSpeed();
		if(s >= 0) {
			int p = sam.getPeriod();
			if(p >= 0 && p < SAMPLES_PER_DAY)
				spd[p] = s;
		}
	}

	/** Get the binned data */
	public byte[] getData() {
		return spd;
	}
}
