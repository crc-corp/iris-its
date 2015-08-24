/*
 * DataExtract
 * Copyright (C) 2005-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.data;

import javax.swing.tree.DefaultMutableTreeNode;
import org.w3c.dom.Element;
/**
 * @author John3Tim
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SystemNode extends DefaultMutableTreeNode {

	public static final String NODE_CORRIDOR = "CORRIDOR";
	public static final String NODE_R_NODE = "R_NODE";
	public static final String NODE_METER = "METER";
	public static final String NODE_DETECTOR = "DETECTOR";
	public static final String NODE_ROOT = "ROOT";
	public static final String NODE_ZONE = "ZONE";
	private String id;
	private final String desc;
	private final String type;
	private String category = "";
	private boolean sensor = false;
	
	public SystemNode(Element e, SystemTree tree){
		if(e == null){
			type = NODE_ROOT;
			id = "All Systems";
			desc = "";
			sensor = false;
			return;
		}
		String t = e.getNodeName().toUpperCase();
		if(t.equals(NODE_CORRIDOR)){
			type = NODE_CORRIDOR;
			sensor = false;
			id = e.getAttribute("route");
			desc = e.getAttribute("dir");
		}else if(t.equals(NODE_R_NODE)){
			type = e.getAttribute("n_type");
			id = e.getAttribute("station_id");
			Element corridor = ((Element)e.getParentNode());
			desc = corridor.getAttribute("route") + " " +
				corridor.getAttribute( "dir" ) + "/" +
				e.getAttribute("label");
			sensor = true;
		}else if(t.equals(NODE_METER)){
			type = NODE_METER;
			id = e.getAttribute("id");
			desc = e.getAttribute("label");
			sensor = false;
		}else if(t.equals(NODE_DETECTOR)){
			type = NODE_DETECTOR;
			id = e.getAttribute("index");
			if(!tree.getDetectorPrefix().equals("")){
				if(Character.isLetter(id.charAt(0))){
					id = tree.getDetectorPrefix() + id.substring(1);
				}else{
					id = tree.getDetectorPrefix() + id;
				}
			}
			desc = e.getAttribute("label");
			category = e.getAttribute("category");
			sensor = true;
		}else if(t.equals(NODE_ZONE)){
			type = NODE_ZONE;
			id = e.getAttribute("index");
			desc = e.getAttribute("label");
			sensor = false;
		}else{
			type = t;
			id = t;
			desc = "";
			sensor = false;
		}
	}
	
	public String toString(){
		StringBuffer buf = new StringBuffer(id);
		while(buf.length() < 6) buf.append(' ');
		return buf.toString() + desc;
	}
	
	public boolean isSensor(){
		return sensor;
	}

	public String getType(){
		return type;
	}
	
	public String getId(){
		return id;
	}
	
	public String getCategory(){
		return category;
	}
}
