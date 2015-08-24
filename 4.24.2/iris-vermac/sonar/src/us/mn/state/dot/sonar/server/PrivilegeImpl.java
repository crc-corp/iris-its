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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.Privilege;
import us.mn.state.dot.sonar.Capability;

/**
 * A privilege controls access to the SONAR namespace.
 *
 * @author Douglas Lau
 */
public class PrivilegeImpl implements Privilege {

	/** Namespace access regex pattern */
	static protected final Pattern NAMESPACE_PATTERN =
		Pattern.compile("[A-Za-z0-9_.*+?()/]*");

	/** Destroy a privilege */
	public void destroy() {
		// Subclasses must remove privilege from backing store
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Privilege name */
	protected final String name;

	/** Get the SONAR object name */
	public String getName() {
		return name;
	}

	/** Create a new privilege */
	public PrivilegeImpl(String n) {
		name = n;
	}

	/** Create a new privilege */
	public PrivilegeImpl(String n, Capability c) {
		name = n;
		capability = c;
	}

	/** Capability */
	protected Capability capability;

	/** Get the capability */
	public Capability getCapability() {
		return capability;
	}

	/** Pattern for matching names */
	protected String pattern = "";

	/** Get the namespace pattern */
	public String getPattern() {
		return pattern;
	}

	/** Check for a valid namespace pattern */
	protected void checkPattern(String p) throws NamespaceError {
		Matcher m = NAMESPACE_PATTERN.matcher(p);
		if(!m.matches())
			throw NamespaceError.PATTERN_INVALID;
	}

	/** Set the namespace pattern */
	public void setPattern(String p) {
		pattern = p;
	}

	/** Set the namespace pattern */
	public void doSetPattern(String p) throws Exception {
		checkPattern(p);
		setPattern(p);
	}

	/** Read access privilege */
	protected boolean priv_r = false;

	/** Set the read privilege */
	public void setPrivR(boolean p) {
		priv_r = p;
	}

	/** Get the read privilege */
	public boolean getPrivR() {
		return priv_r;
	}

	/** Write access privilege */
	protected boolean priv_w = false;

	/** Set the write privilege */
	public void setPrivW(boolean p) {
		priv_w = p;
	}

	/** Get the write privilege */
	public boolean getPrivW() {
		return priv_w;
	}

	/** Create access privilege */
	protected boolean priv_c = false;

	/** Set the create privilege */
	public void setPrivC(boolean p) {
		priv_c = p;
	}

	/** Get the create privilege */
	public boolean getPrivC() {
		return priv_c;
	}

	/** Delete access privilege */
	protected boolean priv_d = false;

	/** Set the delete privilege */
	public void setPrivD(boolean p) {
		priv_d = p;
	}

	/** Get the delete privilege */
	public boolean getPrivD() {
		return priv_d;
	}
}
