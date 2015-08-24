/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar.server;

import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.SonarException;

/**
 * This exception indicates an attempt to access a name without permission
 *
 * @author Douglas Lau
 */
public class PermissionDenied extends SonarException {

	/** Create a new permission denied exception */
	private PermissionDenied(String m) {
		super("Permission denied: " + m);
	}

	/** Thrown when LOGIN authentication has failed */
	static public final PermissionDenied AUTHENTICATION_FAILED =
		new PermissionDenied("Authentication failed");

	/** Thrown when a name cannot be added */
	static public final PermissionDenied CANNOT_ADD =
		new PermissionDenied("Unable to add object");

	/** Thrown when a name cannot be removed */
	static public final PermissionDenied CANNOT_REMOVE =
		new PermissionDenied("Unable to remove object");

	/** Thrown when an attribute cannot be read */
	static public final PermissionDenied CANNOT_READ =
		new PermissionDenied("Unable to read attribute");

	/** Thrown when an attribute cannot be written */
	static public final PermissionDenied CANNOT_WRITE =
		new PermissionDenied("Unable to write attribute");

	/** Create a new "insufficient privileges" exception */
	static public PermissionDenied create(Name n) {
		return new PermissionDenied("Insufficient privileges: " +
			n.toString());
	}
}
