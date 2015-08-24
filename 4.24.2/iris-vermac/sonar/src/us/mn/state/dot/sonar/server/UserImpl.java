/*
 * SONAR -- Simple Object Notification And Replication
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

import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.User;

/**
 * A user must be authenticated by an LDAP server.
 *
 * @author Douglas Lau
 */
public class UserImpl implements User {

	/** Destroy a user */
	public void destroy() {
		// Subclasses must remove user from backing store
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** User name */
	protected final String name;

	/** Get the SONAR object name */
	public String getName() {
		return name;
	}

	/** Create a new user */
	public UserImpl(String n) {
		name = n;
		dn = "cn=" + name;
	}

	/** Full (display) name */
	protected String fullName;

	/** Set the user's full name */
	public void setFullName(String n) {
		fullName = n;
	}

	/** Get the user's full name */
	public String getFullName() {
		return fullName;
	}

	/** Password hash */
	protected String password;

	/** Set the password */
	public void setPassword(String pwd) {
		password = pwd;
	}

	/** Set the password */
	public void doSetPassword(String pwd) throws Exception {
		// Subclasses should implement hashing
		setPassword(pwd);
	}

	/** LDAP Distinguished Name */
	protected String dn;

	/** Set the LDAP Distinguished Name */
	public void setDn(String d) {
		dn = d;
	}

	/** Get the LDAP Distinguished Name */
	public String getDn() {
		return dn;
	}

	/** Role of the user */
	protected RoleImpl role;

	/** Set the role */
	public void setRole(Role r) {
		if(r instanceof RoleImpl)
			role = (RoleImpl)r;
	}

	/** Get the role */
	public Role getRole() {
		return role;
	}

	/** Enabled flag */
	protected boolean enabled;

	/** Set the enabled flag */
	public void setEnabled(boolean e) {
		enabled = e;
	}

	/** Get the enabled flag */
	public boolean getEnabled() {
		return enabled;
	}
}
