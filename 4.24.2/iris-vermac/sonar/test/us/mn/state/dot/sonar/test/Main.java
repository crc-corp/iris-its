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
package us.mn.state.dot.sonar.test;

import us.mn.state.dot.sonar.SonarException;

public class Main {

	static protected boolean checkClient(String[] args) {
		for(String a: args) {
			if(a.equals("-c") || a.equals("--client"))
				return true;
		}
		return false;
	}

	static public void main(String[] args) {
		try {
			if(checkClient(args)) {
				TestClient c = new TestClient();
				c.quit();
				c.join();
			} else {
				TestServer s = new TestServer();
				s.server.join();
			}
		}
		catch(SonarException e) {
			System.err.println("SONAR " + e.getMessage());
			System.exit(1);
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
