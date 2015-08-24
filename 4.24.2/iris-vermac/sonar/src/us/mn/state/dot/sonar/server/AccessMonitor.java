/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2012  Minnesota Department of Transportation
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

/**
 * An access monitor allows the SONAR server to provide feedback for events.
 *
 * @author Douglas Lau
 */
public interface AccessMonitor {

	/** Callback for a client connect event */
	void connect(String hostport);

	/** Callback for a client authenticate event */
	void authenticate(String hostport, String user);

	/** Callback for a client fail authentication event */
	void failAuthentication(String hostport, String user);

	/** Callback for a client disconnect event */
	void disconnect(String hostport, String user);
}
