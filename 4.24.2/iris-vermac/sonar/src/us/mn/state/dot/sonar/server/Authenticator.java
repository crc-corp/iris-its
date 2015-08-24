/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2012  Minnesota Department of Transportation
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

import java.util.LinkedList;
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;

/**
 * Simple class to authenticate a user with an LDAP server.
 *
 * @author Douglas Lau
 */
public class Authenticator {

	/** Check that a user is enabled */
	static private boolean isUserEnabled(UserImpl u) {
		return u != null && u.getEnabled();
	}

	/** Check that a password is sane */
	static protected boolean isPasswordSane(char[] pwd) {
		return pwd != null && pwd.length > 0;
	}

	/** Clear a password in memory */
	static private void clearPassword(char[] pwd) {
		for(int i = 0; i < pwd.length; i++)
			pwd[i] = '\0';
	}

	/** Authentication thread */
	private final Scheduler auth_sched = new Scheduler("sonar_auth",
		new ExceptionHandler()
	{
		public boolean handle(Exception e) {
			System.err.println("SONAR: auth_sched error " +
				e.getMessage());
			e.printStackTrace();
			return true;
		}
	});

	/** Task processor */
	private final TaskProcessor processor;

	/** List of authentication providers */
	private final LinkedList<AuthProvider> providers =
		new LinkedList<AuthProvider>();

	/** Add an authentication provider */
	public void addProvider(final AuthProvider ap) {
		auth_sched.addJob(new Job() {
			public void perform() {
				// Add to beginning of list, so that LDAP
				// providers will be checked last
				providers.addFirst(ap);
			}
		});
	}

	/** Create a new user authenticator */
	public Authenticator(TaskProcessor tp) {
		processor = tp;
	}

	/** Authenticate a user connection */
	void authenticate(final ConnectionImpl c, final UserImpl u,
		final String name, final char[] password)
	{
		auth_sched.addJob(new Job() {
			public void perform() {
				doAuthenticate(c, u, name, password);
			}
		});
	}

	/** Perform a user authentication */
	private void doAuthenticate(ConnectionImpl c, UserImpl user,
		String name, char[] pwd)
	{
		try {
			if(authenticate(user, pwd))
				processor.finishLogin(c, user);
			else
				processor.failLogin(c, name);
		}
		finally {
			clearPassword(pwd);
		}
	}

	/** Authenticate a user's credentials */
	private boolean authenticate(UserImpl user, char[] pwd) {
		if(isUserEnabled(user) && isPasswordSane(pwd)) {
			for(AuthProvider p: providers) {
				if(p.authenticate(user, pwd))
					return true;
			}
		}
		return false;
	}

	/** Change a user password */
	void changePassword(final ConnectionImpl c, final UserImpl u,
		final char[] pwd_current, final char[] pwd_new)
	{
		auth_sched.addJob(new Job() {
			public void perform() {
				doChangePassword(c, u, pwd_current, pwd_new);
			}
		});
	}

	/** Perform a user password change */
	private void doChangePassword(ConnectionImpl c, UserImpl user,
		char[] pwd_current, char[] pwd_new)
	{
		try {
			if(authenticate(user, pwd_current))
				processor.finishPassword(c, user, pwd_new);
			else {
				processor.failPassword(c, PermissionDenied.
					AUTHENTICATION_FAILED.getMessage());
			}
		}
		finally {
			clearPassword(pwd_current);
			clearPassword(pwd_new);
		}
	}
}
