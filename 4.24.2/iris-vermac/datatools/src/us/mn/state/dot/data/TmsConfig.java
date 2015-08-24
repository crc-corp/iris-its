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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author john3tim
 *
 */
public class TmsConfig extends SystemConfig {

	public static final String ATT_STATION_ID = "station_id";

	public static final String ATT_DETECTOR_ID = "index";

	public static final String ATT_ROUTE = "route";

	public static final String ATT_DIR = "dir";

	public static final String TAG_CORRIDOR = "corridor";

	public static final String TAG_DETECTOR = "detector";

	public static final String TAG_STATION = "r_node";

	protected final HashMap<String, Element> corridorElements =
		new HashMap<String, Element>();

	protected final HashMap<String, Element> stationElements =
		new HashMap<String, Element>();

	/** A map of station elements not associated with a station. */
	protected final HashMap<String, Element> detectorElements =
		new HashMap<String, Element>();

	/** Create a new TMS config */
	public TmsConfig(URL url, Document doc) {
		super(url, doc);
		loadDetectorElements();
		loadCorridorElements();
		loadStationElements();
	}

	protected Sensor createSensor(Element e) {
		Sensor s = new Sensor(e.getAttribute(ATT_DETECTOR_ID), this);
		String cat = e.getAttribute("category");
		if(cat != null && cat.length() > 0)
			s.setCategory(cat.charAt(0));
		else
			s.setCategory(' ');
		s.setField(Float.parseFloat(e.getAttribute("field")));
		s.setLabel(e.getAttribute("label"));
		return s;
	}

	protected void loadCorridorElements() {
		NodeList cList = document.getElementsByTagName(TAG_CORRIDOR);
		for(int i = 0; i < cList.getLength(); i++) {
			Element c = (Element)cList.item(i);
			corridorElements.put(createCorridorName(c), c);
		}
	}

	protected String createCorridorName(Element c) {
		return c.getAttribute(ATT_ROUTE) + c.getAttribute(ATT_DIR);
	}

	protected void loadStationElements() {
		NodeList cList = document.getElementsByTagName(TAG_CORRIDOR);
		for(int cIndex = 0; cIndex < cList.getLength(); cIndex++) {
			NodeList r_nodeList = cList.item(cIndex).getChildNodes();
			for(int i = 0; i < r_nodeList.getLength(); i++) {
				Node n = r_nodeList.item(i);
				if(n instanceof Element) {
					Element s = (Element)n;
					String id = s.getAttribute("station_id");
					if(id != null && id.length() > 0)
						stationElements.put(id, s);
				}
			}
		}
	}

	protected void loadDetectorElements() {
		NodeList dets = document.getElementsByTagName(TAG_DETECTOR);
		for(int i = 0; i < dets.getLength(); i++) {
			Element d = (Element)dets.item(i);
			detectorElements.put(d.getAttribute(ATT_DETECTOR_ID), d);
			sensors.put(d.getAttribute(ATT_DETECTOR_ID), createSensor(d));
		}
	}

	public Set<SystemNode> getCorridorNodes(SystemTree tree) {
		Set<SystemNode> cNodes =
			new TreeSet<SystemNode>(new NodeComparator());
		for(Element e: corridorElements.values())
			cNodes.add(createCorridorNode(e, tree));
		return cNodes;
	}

	protected SystemNode createCorridorNode(Element e, SystemTree tree) {
		SystemNode c = new SystemNode(e, tree);
		NodeList r_nodeList = e.getChildNodes();
		for(int i = 0; i < r_nodeList.getLength(); i++) {
			Node n = r_nodeList.item(i);
			if(n instanceof Element) {
				Element s = (Element)n;
				String id = s.getAttribute("station_id");
				if(id != null && id.length() > 0)
					c.add(createStation(s, tree));
			}
		}
		return c;
	}

	protected SystemNode createStation(Element e, SystemTree tree) {
		SystemNode s = new SystemNode(e, tree);
		String dets = e.getAttribute("dets");
		StringTokenizer tok = new StringTokenizer(dets, " ", false);
		while(tok.hasMoreTokens()) {
			Element d = detectorElements.remove(tok.nextToken());
			if(d != null)
				s.add(new SystemNode(d, tree));
		}
		return s;
	}

	/** Get a set of SystemNodes which represent detectors which
	 * are not members of any station.  */
	public Set<SystemNode> getNonStationDetectors(SystemTree tree) {
		Set<SystemNode> dets =
			new TreeSet<SystemNode>(new NodeComparator());
		for(Element e: detectorElements.values())
			dets.add(new SystemNode(e, tree));
		return dets;
	}

	/** Get the sensors that make up the station. */
	public Sensor[] getStationSensors(String id) {
		List<Sensor> sList = new ArrayList<Sensor>();
		Element s = stationElements.get(id);
		String dets = s.getAttribute("dets");
		StringTokenizer tok = new StringTokenizer(dets, " ", false);
		while(tok.hasMoreTokens()) {
			Sensor d = sensors.get(tok.nextElement());
			sList.add(d);
		}
		return (Sensor[])sList.toArray(new Sensor[0]);
	}
}
