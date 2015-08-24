/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2010  Minnesota Department of Transportation
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
 * A privilege controls access to the SONAR namespace.
 *
 * @author Douglas Lau
 */
public interface Privilege extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "privilege";

	/** Get the capability */
	Capability getCapability();

	/** Get the namespace pattern */
	String getPattern();

	/** Set the namespace pattern */
	void setPattern(String p);

	/** Set the read privilege */
	void setPrivR(boolean p);

	/** Get the read privilege */
	boolean getPrivR();

	/** Set the write privilege */
	void setPrivW(boolean p);

	/** Get the write privilege */
	boolean getPrivW();

	/** Set the create privilege */
	void setPrivC(boolean p);

	/** Get the create privilege */
	boolean getPrivC();

	/** Set the delete privilege */
	void setPrivD(boolean p);

	/** Get the delete privilege */
	boolean getPrivD();
}
