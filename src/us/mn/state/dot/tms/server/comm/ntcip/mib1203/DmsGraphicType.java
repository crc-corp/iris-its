/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
 * DmsGraphicType indicates the type (color scheme) of a graphic.
 *
 * @author Douglas Lau
 */
public class DmsGraphicType extends ASN1Integer {

	/** Create a new DmsGraphicType object */
	public DmsGraphicType(int row) {
		super(MIB1203.dmsGraphicEntry.create(new int[] {6, row}));
	}

	/** Set the enum value */
	public void setEnum(DmsColorScheme.Enum v) {
		value = v.ordinal();
	}

	/** Get the object value */
	public String getValue() {
		return DmsColorScheme.Enum.fromOrdinal(value).toString();
	}
}
