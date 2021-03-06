/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
 * DmsLightSensorStatus contains the current status of a light sensor.  This
 * object was added in NTCIP 1203 v2.
 *
 * @author Douglas Lau
 */
public class DmsLightSensorStatus extends ASN1Integer {

	/** Enumeration of light sensor status values */
	static public enum Enum {
		undefined, other, noError, fail;

		/** Get light sensor status from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for(Enum e: Enum.values()) {
				if(e.ordinal() == o)
					return e;
			}
			return undefined;
		}
	}

	/** Create a new DmsLightSensorStatus object */
	public DmsLightSensorStatus(int row) {
		super(MIB1203.dmsLightSensorStatusEntry.create(
			new int[] {4, row}));
	}

	/** Set the enum value */
	public void setEnum(Enum v) {
		value = v.ordinal();
	}

	/** Get the enum value */
	public Enum getEnum() {
		return Enum.fromOrdinal(value);
	}

	/** Get the object value */
	public String getValue() {
		return Enum.fromOrdinal(value).toString();
	}
}
