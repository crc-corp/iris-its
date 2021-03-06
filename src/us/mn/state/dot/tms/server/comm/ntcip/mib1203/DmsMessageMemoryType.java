/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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
 * Ntcip DmsMessageMemoryType object
 *
 * @author Douglas Lau
 */
public class DmsMessageMemoryType extends ASN1Integer {

	/** Enumeration of memory types */
	static public enum Enum {
		undefined, other, permanent, changeable, _volatile,
		currentBuffer, schedule, blank;

		/** Get memory type from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for(Enum e: Enum.values()) {
				if(e.ordinal() == o)
					return e;
			}
			return undefined;
		}

		/** Test if a message memory type is "blank" */
		public boolean isBlank() {
		 	// For some vendors (1203v1), blank messages are
		 	// undefined in dmsMsgTableSource
			return this == blank || this == undefined;
		}

		/** Test if a message memory type is "valid" */
		public boolean isValid() {
			switch(this) {
			case permanent:
			case changeable:
			case _volatile:
			case schedule:
			case blank:
				return true;
			default:
				return false;
			}
		}
	}

	/** Create a new memory type object */
	public DmsMessageMemoryType(Enum m, int number) {
		super(MIB1203.dmsMessageEntry.create(new int[] {
			1, m.ordinal(), number}));
	}
}
