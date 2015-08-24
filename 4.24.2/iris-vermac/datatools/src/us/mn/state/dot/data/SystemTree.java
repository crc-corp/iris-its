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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

abstract public class SystemTree extends DefaultMutableTreeNode {

	/** The SystemConfig that backs this Tree*/
	protected SystemConfig system = null;
	
	/** List of registered listeners to this stream. */
	private ArrayList<SystemNodeSelectionListener> listeners =
		new ArrayList<SystemNodeSelectionListener>();

	protected final Hashtable<String, SystemNode> nodes =
		new Hashtable<String, SystemNode>();
	
	protected SystemTree(SystemConfig cfg) {
		system = cfg;
	}

	public String toString(){
		return system.getName();
	}
	
	public void addSystemNodeSelectionListener(SystemNodeSelectionListener l){
		listeners.add(l);
	}

	public final SystemNode find(String id){
		return (SystemNode)nodes.get(id);
	}

	public static SystemTree createTree(SystemConfig cfg){
		if(cfg instanceof TmsConfig){
			return new TmsTree((TmsConfig)cfg);
		}else if(cfg instanceof ArterialConfig){
			return new ArterialsTree((ArterialConfig)cfg);
		}else return null;
	}

	public String getDetectorPrefix() {
		return system.getDetectorPrefix();
	}

	public final void registerNode(SystemNode n){
		nodes.put(n.getId(), n);
		for(int i=0; i<n.getChildCount(); i++){
			registerNode((SystemNode)n.getChildAt(i));
		}
	}
	
	protected final void addNodes(Set<SystemNode> set){
		for(SystemNode n : set){
			add(n);
			registerNode(n);
		}
	}
}