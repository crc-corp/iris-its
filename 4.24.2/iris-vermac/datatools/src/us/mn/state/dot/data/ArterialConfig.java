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

import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author john3tim
 *
 */
public class ArterialConfig extends SystemConfig {

	public ArterialConfig(URL url, Document doc) {
		super(url, doc);
	}

	public Set<SystemNode> getZones(SystemTree tree) {
		NodeList zList = document.getElementsByTagName("zone");
		Set zones = new TreeSet<SystemNode>(new NodeComparator());
		for(int i = 0; i < zList.getLength(); i++)
			zones.add(createZone((Element)zList.item(i), tree));
		return zones;
	}

	protected SystemNode createZone(Element e, SystemTree tree) {
		SystemNode z = new SystemNode(e, tree);
		NodeList dList = e.getChildNodes();
		for(int i = 0; i < dList.getLength(); i++) {
			Node n = dList.item(i);
			if(n instanceof Element) {
				SystemNode d = new SystemNode((Element)n, tree);
				z.add(d);
			}
		}
		return z;
	}
}
