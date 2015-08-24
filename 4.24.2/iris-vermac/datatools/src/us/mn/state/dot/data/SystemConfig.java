/*
 * DataExtract
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.data;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author john3tim
 * @author Douglas Lau
 */
abstract public class SystemConfig {

	protected final Hashtable<String, Sensor> sensors =
		new Hashtable<String, Sensor>();

	protected final URL url;

	protected final Document document;

	protected final Element system;

	protected final String name;

	protected final String detectorPrefix;

	protected final String timeStamp;

	protected String lookupSystemName() {
		String n = system.getAttribute("system");
		if(n != null)
			return n;
		else
			return "";
	}

	protected String lookupDetectorPrefix() {
		String pre = system.getAttribute("detector_prefix");
		if(pre != null && pre.length() > 0)
			return pre;
		else
			return "";
	}

	protected String lookupTimestamp() {
		String t = system.getAttribute("time_stamp");
		if(t == null)
			return "not available";
		else
			return t;
	}

	public SystemConfig(URL url, Document doc) {
		this.url = url;
		document = doc;
		system = document.getDocumentElement();
		name = lookupSystemName();
		detectorPrefix = lookupDetectorPrefix();
		timeStamp = lookupTimestamp();
	}

	public URL getURL() {
		return url;
	}

	public String getName() {
		return name;
	}

	public Sensor getSensor(String id) {
		return sensors.get(id);
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public String getDetectorPrefix() {
		return detectorPrefix;
	}

	/** Create a system config from the specified URL */
	static public SystemConfig create(URL url) throws IOException,
		ParserConfigurationException
	{
		XmlParser parser = new XmlParser(url);
		if(parser == null)
			return null;
		Document doc = parser.getDocument();
		if(doc == null)
			return null;
		Element root = doc.getDocumentElement();
		String tag = root.getTagName();
		if(tag.equals("tms_config"))
			return new TmsConfig(url, doc);
		else if(tag.equals("arterials"))
			return new ArterialConfig(url, doc);
		else
			throw new IOException("Unrecognized XML format");
	}
}
