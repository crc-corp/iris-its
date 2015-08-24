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
 * Bin for storing volume sample data
 *
 * @author Douglas Lau
 */
public class VolumeSampleBin implements SampleBin {

	/** Binned 30-second volume data */
	protected final byte[] vol = new byte[SAMPLES_PER_DAY];

	/** Create a new volume sample bin */
	public VolumeSampleBin() {
		for(int i = 0; i < SAMPLES_PER_DAY; i++)
			vol[i] = SampleData.MISSING_DATA;
	}

	/** Add one data sample to the bin */
	public void addSample(SampleData sam) {
		byte v = (byte)sam.getVolume();
		if(v >= 0) {
			int p = sam.getPeriod();
			if(p >= 0 && p < SAMPLES_PER_DAY)
				vol[p] = v;
		}
	}

	/** Get the binned data */
	public byte[] getData() {
		return vol;
	}
}
