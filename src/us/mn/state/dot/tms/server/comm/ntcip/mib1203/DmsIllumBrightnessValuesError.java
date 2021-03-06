/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Integer;

/**
 * Ntcip DmsIllumBrightnessValuesError object
 *
 * @author Douglas Lau
 */
public class DmsIllumBrightnessValuesError extends ASN1Integer {

	/** Enumeration of brightness values errors */
	static public enum Enum {
		undefined, other, none, photocellGap, negativeSlope,
		tooManyLevels, invalidData;

		/** Get brightness values error from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for(Enum e: Enum.values()) {
				if(e.ordinal() == o)
					return e;
			}
			return undefined;
		}
	}

	/** Create a new DmsIllumBrightnessValuesError object */
	public DmsIllumBrightnessValuesError() {
		super(MIB1203.illum.create(new int[] {8, 0}));
	}

	/** Get the object value */
	public String getValue() {
		return Enum.fromOrdinal(value).toString();
	}
}
