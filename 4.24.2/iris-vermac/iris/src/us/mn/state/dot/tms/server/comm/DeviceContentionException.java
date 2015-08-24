/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

/**
 * A device contention exception happens when one operation has a lock on a
 * device while another tries to acquire the lock.
 *
 * @author Douglas Lau
 */
public class DeviceContentionException extends Exception {

	/** Operation which owns the device lock */
	public final Operation operation;

	/** Create a new device contention exception */
	public DeviceContentionException(Operation o) {
		super("DEVICE CONTENTION ERROR");
		operation = o;
	}
}
