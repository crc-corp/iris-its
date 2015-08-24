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

import us.mn.state.dot.sonar.SonarObject;

/** 
 *  A sonar test object interface.
 *  @author Doug Lau
 */
public interface TestObj extends SonarObject {

	String SONAR_TYPE = "testobj";

	int getLocation();

	void setLocation(int l);

	String getNotes();

	void setNotes(String n);
}
