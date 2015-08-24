/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006  Minnesota Department of Transportation
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
 * This exception indicates that a client has made a protocol error
 *
 * @author Douglas Lau
 */
public class ProtocolError extends SonarException {

	/** Create a new protocol error */
	private ProtocolError(String m) {
		super("Protocol error: " + m);
	}

	/** Error thrown when authentication is required */
	static public final ProtocolError AUTHENTICATION_REQUIRED =
		new ProtocolError("Authentication required");

	/** Error thrown when a second authentication attempt is made */
	static public final ProtocolError ALREADY_LOGGED_IN =
		new ProtocolError("Already logged in");

	/** Error thrown when an invalid message code is received */
	static public final ProtocolError INVALID_MESSAGE_CODE =
		new ProtocolError("Invalid message code");

	/** Error thrown when the wrong number of parameters are received */
	static public final ProtocolError WRONG_PARAMETER_COUNT =
		new ProtocolError("Wrong number of parameters");

	/** Error thrown when an invalid parameter is received */
	static public final ProtocolError INVALID_PARAMETER =
		new ProtocolError("Invalid parameter");

	/** Error thrown when trying to IGNORE an object not being watched */
	static public final ProtocolError NOT_WATCHING =
		new ProtocolError("Not watching name");
}
