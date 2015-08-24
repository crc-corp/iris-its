/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2014  Minnesota Department of Transportation
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

import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.ConfigurationError;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.Security;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.User;

/**
 * The task processor handles all SONAR tasks.
 *
 * @author Douglas Lau
 */
public class TaskProcessor {

	/** SONAR debug log */
	static public final DebugLog DEBUG = new DebugLog("sonar");

	/** SONAR task debug log */
	static private final DebugLog DEBUG_TASK = new DebugLog("sonar_task");

	/** Debug a task */
	static private void debugTask(String msg, ConnectionImpl c) {
		if (DEBUG_TASK.isOpen()) {
			if (c != null)
				DEBUG_TASK.log(msg + ": " + c.getName());
			else
				DEBUG_TASK.log(msg + ": no connection");
		}
	}

	/** Debug a task */
	static private void debugTask(String msg, String n) {
		if (DEBUG_TASK.isOpen())
			DEBUG_TASK.log(msg + ": " + n);
	}

	/** SONAR namespace being served */
	private final ServerNamespace namespace;

	/** Access monitor */
	private final AccessMonitor access_monitor;

	/** SSL context */
	private final SSLContext context;

	/** Task processor scheduler */
	private final Scheduler processor = new Scheduler("sonar_proc",
 		new ExceptionHandler()
	{
		public boolean handle(Exception e) {
			if(e instanceof CancelledKeyException)
				DEBUG.log("Key already cancelled");
			else if(e instanceof SSLException)
				DEBUG.log("SSL error " + e.getMessage());
			else {
				System.err.println("SONAR: error " +
					e.getMessage());
				e.printStackTrace();
			}
			return true;
		}
	});

	/** Authenticator for user credentials */
	private final Authenticator authenticator;

	/** Map of active client connections */
	private final Map<SelectionKey, ConnectionImpl> clients =
		new HashMap<SelectionKey, ConnectionImpl>();

	/** File to write session list */
	private final String session_file;

	/** Create a task processor */
	public TaskProcessor(ServerNamespace n, Properties props,
		AccessMonitor am) throws IOException, ConfigurationError
	{
		namespace = n;
		access_monitor = am;
		authenticator = new Authenticator(this);
		context = Security.createContext(props);
		LDAPSocketFactory.FACTORY = context.getSocketFactory();
		String ldap_urls = props.getProperty("sonar.ldap.urls");
		if(ldap_urls != null) {
			for(String url: ldap_urls.split("[ \t]+"))
				addProvider(new LDAPProvider(url));
		}
		session_file = props.getProperty("sonar.session.file");
	}

	/** Add an authentication provider */
	public void addProvider(AuthProvider ap) {
		authenticator.addProvider(ap);
	}

	/** Get the SONAR namespace */
	public ServerNamespace getNamespace() {
		return namespace;
	}

	/** Get a list of active connections */
	private List<ConnectionImpl> getConnectionList() {
		LinkedList<ConnectionImpl> clist =
			new LinkedList<ConnectionImpl>();
		synchronized(clients) {
			clist.addAll(clients.values());
		}
		return clist;
	}

	/** Schedule a client connection */
	public void scheduleConnect(final SelectionKey key,
		final SocketChannel sc)
	{
		processor.addJob(new Job() {
			public void perform() throws Exception {
				try {
					doConnect(key, sc);
				}
				catch(Exception e) {
					// Don't leak channels
					key.cancel();
					sc.close();
					throw e;
				}
			}
		});
	}

	/** Create a client connection */
	private void doConnect(SelectionKey key, SocketChannel sc)
		throws IOException, NamespaceError
	{
		ConnectionImpl con = new ConnectionImpl(this, key, sc);
		doAddObject(con);
		access_monitor.connect(con.getName());
		synchronized(clients) {
			clients.put(key, con);
		}
		updateSessionList();
		// Enable OP_READ interest
		con.disableWrite();
	}

	/** Schedule a disconnect on a selection key */
	public void scheduleDisconnect(final SelectionKey key) {
		processor.addJob(new Job() {
			public void perform() {
				disconnect(key);
			}
		});
	}

	/** Schedule a connection to be disconnected */
	public void scheduleDisconnect(final ConnectionImpl c,
		final String msg)
	{
		processor.addJob(new Job() {
			public void perform() {
				debugTask("Disconnect", c);
				if(msg != null)
					c.disconnect(msg);
				else
					c.disconnect();
			}
		});
	}

	/** Disconnect the client associated with the selection key. */
	void disconnect(SelectionKey key) {
		key.cancel();
		ConnectionImpl c;
		synchronized(clients) {
			c = clients.remove(key);
		}
		debugTask("Disconnecting", c);
		if(c != null) {
			access_monitor.disconnect(c.getName(), c.getUserName());
			updateSessionList();
			scheduleRemoveObject(c);
		}
	}

	/** Update list of valid session IDs */
	private void updateSessionList() {
		if(session_file == null)
			return;
		List<ConnectionImpl> clist = getConnectionList();
		try {
			FileWriter fw = new FileWriter(session_file);
			try {
				for(ConnectionImpl c: clist) {
					fw.write(String.valueOf(
						c.getSessionId()));
					fw.append('\n');
				}
			}
			finally {
				fw.close();
			}
		}
		catch(IOException e) {
			DEBUG.log("Error writing session file: " +
				session_file + " (" + e.getMessage() + ")");
		}
	}

	/** Process messages on one connection */
	void processMessages(final ConnectionImpl c) {
		processor.addJob(new Job() {
			public void perform() {
				debugTask("Processing messages", c);
				c.processMessages();
			}
		});
	}

	/** Flush outgoing data for one connection */
	void flush(final ConnectionImpl c) {
		processor.addJob(new Job() {
			public void perform() {
				debugTask("Flush", c);
				c.flush();
			}
		});
	}

	/** Authenticate a user connection */
	void authenticate(ConnectionImpl c, String name, char[] password) {
		authenticator.authenticate(c, lookupUser(name), name, password);
	}

	/** Lookup a user by name. */
	private UserImpl lookupUser(String n) {
		return (UserImpl)namespace.lookupObject(User.SONAR_TYPE, n);
	}

	/** Finish a LOGIN */
	void finishLogin(final ConnectionImpl c, final UserImpl u) {
		processor.addJob(new Job() {
			public void perform() {
				debugTask("Finishing LOGIN", c);
				access_monitor.authenticate(c.getName(),
					u.getName());
				scheduleSetAttribute(c, "user");
				c.finishLogin(u);
			}
		});
	}

	/** Fail a LOGIN */
	void failLogin(final ConnectionImpl c, final String name) {
		processor.addJob(new Job() {
			public void perform() {
				debugTask("Failing LOGIN", c);
				access_monitor.failAuthentication(c.getName(),
					name);
				c.failLogin();
			}
		});
	}

	/** Change a user password */
	void changePassword(ConnectionImpl c, UserImpl u, char[] pwd_current,
		char[] pwd_new)
	{
		authenticator.changePassword(c, u, pwd_current, pwd_new);
	}

	/** Finish a PASSWORD */
	void finishPassword(final ConnectionImpl c, final UserImpl u,
		char[] pwd_new)
	{
		// Need to copy password, since authenticator will clear it
		final String pwd = new String(pwd_new);
		processor.addJob(new Job() {
			public void perform() {
				try {
					u.doSetPassword(pwd);
					debugTask("Finishing PASSWORD", c);
				}
				catch(Exception e) {
					failPassword(c, e.getMessage());
					debugTask("Exception PASSWORD", c);
				}
			}
		});
	}

	/** Fail a PASSWORD */
	void failPassword(final ConnectionImpl c, final String msg) {
		processor.addJob(new Job() {
			public void perform() {
				c.failPassword(msg);
				debugTask("Failing PASSWORD", c);
			}
		});
	}

	/** Get an array of cipher suites which should be enabled */
	private String[] getCipherSuites(SSLEngine engine) {
		LinkedList<String> enabled = new LinkedList<String>();
		for(String cs: engine.getEnabledCipherSuites()) {
			if(cs.startsWith("TLS_") && cs.contains("AES_128"))
				enabled.add(cs);
		}
		return enabled.toArray(new String[0]);
	}

	/** Create an SSL engine in the server context */
	public SSLEngine createSSLEngine() {
		SSLEngine engine = context.createSSLEngine();
		engine.setEnabledCipherSuites(getCipherSuites(engine));
		engine.setUseClientMode(false);
		return engine;
	}

	/** Lookup the client connection for a selection key */
	public ConnectionImpl lookupClient(SelectionKey key) {
		synchronized(clients) {
			return clients.get(key);
		}
	}

	/** Notify all connections watching a name of an object add. */
	private void notifyObject(SonarObject o) {
		Name name = new Name(o);
		List<ConnectionImpl> clist = getConnectionList();
		for(ConnectionImpl c: clist)
			c.notifyObject(name, o);
	}

	/** Notify all connections watching a name of an attribute change. */
	void notifyAttribute(Name name, String[] params) {
		debugTask("Notify attribute", name.toString());
		if(namespace.isReadable(name)) {
			List<ConnectionImpl> clist = getConnectionList();
			for(ConnectionImpl c: clist)
				c.notifyAttribute(name, params);
		}
	}

	/** Notify all connections watching a name of an object remove. */
	void notifyRemove(Name name) {
		List<ConnectionImpl> clist = getConnectionList();
		for(ConnectionImpl c: clist)
			c.notifyRemove(name);
	}

	/** Schedule an object to be added to the server's namespace */
	public void scheduleAddObject(final SonarObject o) {
		processor.addJob(new Job() {
			public void perform() throws NamespaceError {
				doAddObject(o);
			}
		});
	}

	/** Perform an add object task. */
	private void doAddObject(SonarObject o) throws NamespaceError {
		debugTask("Adding object", o.getName());
		namespace.addObject(o);
		notifyObject(o);
	}

	/** Create (synchronously) an object in the server's namespace */
	public void storeObject(final SonarObject o) throws SonarException {
		// Calling waitForCompletion will hang if we're
		// running on the task processor thread.
		if(processor.isCurrentThread()) {
			doStoreObject(o);
			return;
		}
		Job job = new Job() {
			public void perform() throws SonarException {
				doStoreObject(o);
			}
		};
		processor.addJob(job);
		try {
			// Only wait for 30 seconds before giving up
			job.waitForCompletion(30000);
		}
		catch(TimeoutException e) {
			throw new SonarException(e);
		}
	}

	/** Store an object in the server's namespace. */
	void doStoreObject(SonarObject o) throws SonarException {
		debugTask("Storing object", o.getName());
		namespace.storeObject(o);
		notifyObject(o);
	}

	/** Remove the specified object from the server's namespace */
	public void scheduleRemoveObject(final SonarObject o) {
		processor.addJob(new Job() {
			public void perform() throws SonarException {
				doRemoveObject(o);
			}
		});
	}

	/** Perform a remove object task. */
	private void doRemoveObject(SonarObject o) throws SonarException {
		debugTask("Removing object", o.getName());
		notifyRemove(new Name(o));
		namespace.removeObject(o);
	}

	/** Set the specified attribute in the server's namespace */
	public void scheduleSetAttribute(final SonarObject o, final String a) {
		processor.addJob(new Job() {
			public void perform() throws SonarException {
				doSetAttribute(o, a);
			}
		});
	}

	/** Perform a "set attribute" task. */
	private void doSetAttribute(SonarObject o, String aname)
		throws SonarException
	{
		Name name = new Name(o, aname);
		String[] v = namespace.getAttribute(name);
		notifyAttribute(name, v);
	}
}
