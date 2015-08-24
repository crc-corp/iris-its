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
package us.mn.state.dot.sonar.test;

import java.util.Properties;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.TestObjImpl;
import us.mn.state.dot.sonar.server.AccessMonitor;
import us.mn.state.dot.sonar.server.AuthProvider;
import us.mn.state.dot.sonar.server.CapabilityImpl;
import us.mn.state.dot.sonar.server.PrivilegeImpl;
import us.mn.state.dot.sonar.server.RoleImpl;
import us.mn.state.dot.sonar.server.Server;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.sonar.server.UserImpl;

public class TestServer {

	static protected Properties createProperties() {
		Properties p = new Properties();
		p.setProperty("keystore.file", "sonar-test.keystore");
		p.setProperty("keystore.password", "sonar-test");
		p.setProperty("sonar.port", "1037");
		return p;
	}

	static protected ServerNamespace createNamespace()
		throws SonarException
	{
		ServerNamespace n = new ServerNamespace();
		CapabilityImpl c = new CapabilityImpl("admin");
		c.setEnabled(true);
		n.addObject(c);
		PrivilegeImpl p = new PrivilegeImpl("admin", c);
		p.setPattern(".*");
		p.setPrivR(true);
		p.setPrivW(true);
		p.setPrivC(true);
		p.setPrivD(true);
		n.addObject(p);
		RoleImpl r = new RoleImpl("admin");
		r.setCapabilities(new CapabilityImpl[] { c });
		r.setEnabled(true);
		n.addObject(r);
		UserImpl u = new UserImpl("username");
		u.setDn("cn=username,dc=sonar");
		u.setRole(r);
		u.setFullName("Test user");
		u.setEnabled(true);
		n.addObject(u);
		n.addObject(new TestObjImpl("name_A", 10));
		n.addObject(new TestObjImpl("name_B", 20));
		for(int i = 0; i < 5000; i++)
			n.addObject(new TestObjImpl("name_" + i, i));
		return n;
	}

	public final Server server;

	public TestServer() throws Exception {
		server = new Server(createNamespace(), createProperties(),
			new AccessMonitor()
		{
			public void connect(String hostport) {
				System.err.println("CONNECT: " + hostport);
			}
			public void authenticate(String hostport, String user) {
				System.err.println("AUTH: " + hostport +
					", USER: " + user);
			}
			public void failAuthentication(String hostport,
				String user)
			{
				System.err.println("FAIL AUTH: " + hostport +
					", USER: " + user);
			}
			public void disconnect(String hostport, String user) {
				System.err.println("DISCONNECT: " + hostport +
					", USER: " + user);
			}
		});
		server.addProvider(new AuthProvider() {
			public boolean authenticate(UserImpl u, char[] pwd) {
				return true;
			}
		});
	}
}
