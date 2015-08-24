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
package us.mn.state.dot.sonar;

/**
 * This exception indicates an error accessing the SONAR namespace
 *
 * @author Douglas Lau
 */
public class NamespaceError extends SonarException {

	/** Create a new namespace error */
	private NamespaceError(String m) {
		super("Namespace error: " + m);
	}

	/** Error thrown when a name is invalid */
	static public final NamespaceError NAME_INVALID =
		new NamespaceError("Invalid name");

	/** Error thrown when a name already exists */
	static public final NamespaceError NAME_EXISTS =
		new NamespaceError("Name already exists");

	/** Error thrown when a name pattern is invalid */
	static public final NamespaceError PATTERN_INVALID =
		new NamespaceError("Invalid name pattern");

	/** Create a "Name unknown" exception */
	static public NamespaceError nameUnknown(String name) {
		return new NamespaceError("Name unknown (" + name + ")");
	}
}
