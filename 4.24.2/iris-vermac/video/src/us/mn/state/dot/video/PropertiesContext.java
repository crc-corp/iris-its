/*
 * Project: Video
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.video;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Sets up the properties context for all servlets.
 * @author Timothy Johnson
 * @author Travis Swanston
 */
public class PropertiesContext extends HttpServlet{

	/** Properties file */
	protected final File propsFile = new File("/etc/tms/video.properties");

	/** The default time to live for DNS cache within JRE */
	public static final String DNS_TTL = "3600"; // 1 hour

	public static final String PROP_DNS_TTL = "networkaddress.cache.ttl";

	public static final String PROP_DB_REFRESH = "db.refresh";

	/** The property name for the connection timeout */
	public static final String PROP_TIMEOUT_CONN = "video.timeout.conn";

	/** The property name for the read timeout */
	public static final String PROP_TIMEOUT_READ = "video.timeout.read";

	/** The property name for the image cache duration */
	public static final String PROP_CACHE_DURATION = "video.cache.duration";

	/** The time interval for refreshing database information (seconds)*/
	public static final String DB_REFRESH = "3600"; // 1 hour
	
	/** Properties */
	static protected final Properties props = new Properties();

	/** Constructor for the VideoServer */
	public void init( ServletConfig config ) throws ServletException {
		super.init( config );
		ServletContext ctx = config.getServletContext();
		try{
			FileInputStream stream = new FileInputStream(propsFile);
			props.load(stream);
			stream.close();
		}catch(IOException ioe){
		}
		String dnsTTL = props.getProperty(PROP_DNS_TTL, DNS_TTL);
		//networkaddress.cache.ttl must be set within the java properties files
		// for TOMCAT applications.  This will do nothing here!
		java.security.Security.setProperty(PROP_DNS_TTL, dnsTTL);
		ctx.setAttribute("properties", props);
	}

	/**
	 * Get an integer value from the properties.
	 * @param key The properties key.
	 * @return The value as an Integer, or null if not found or error.
	 */
	public static Integer getIntProp(String key) {
		String s = props.getProperty(key);
		if (s == null)
			return null;
		Integer i = null;
		try {
			i = Integer.valueOf(s);
		}
		catch (NumberFormatException e) {
			i = null;
		}
		return i;
	}

	/**
	 * Get an integer value from the properties.
	 * @param key The properties key.
	 * @param def The default value to return if property cannot be
	 *            found/parsed.
	 * @return The value as an int.
	 */
	public static int getIntProp(String key, int def) {
		Integer i = getIntProp(key);
		if (i == null)
			return def;
		return i.intValue();
	}

}
