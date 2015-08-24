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

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;

/**
 * @author John3Tim
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class SystemRenderer extends DefaultTreeCellRenderer {

	protected final Icon det = new ImageIcon(
			this.getClass().getClassLoader().getResource("det.png"));
	protected final Icon detP = new ImageIcon(
			this.getClass().getClassLoader().getResource("det_p.png"));
	protected final Icon detM = new ImageIcon(
			this.getClass().getClassLoader().getResource("det_m.png"));
	protected final Icon detA = new ImageIcon(
			this.getClass().getClassLoader().getResource("det_a.png"));
	protected final Icon detX = new ImageIcon(
			this.getClass().getClassLoader().getResource("det_x.png"));
	protected final Icon detB = new ImageIcon(
			this.getClass().getClassLoader().getResource("det_b.png"));
	protected final Icon detH = new ImageIcon(
			this.getClass().getClassLoader().getResource("det_h.png"));

	public SystemRenderer() {
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		MutableTreeNode n = (MutableTreeNode) value;
		if (n instanceof SystemNode) {
			if (((SystemNode) n).isSensor()) {
				SystemNode sn = (SystemNode)n;
				if(sn.getCategory() == null){
					setIcon(det);
				}else if(sn.getCategory().equals("P")){
					setIcon(detP);
				}else if(sn.getCategory().equals("M")){
					setIcon(detM);
				}else if(sn.getCategory().equals("X")){
					setIcon(detX);
				}else if(sn.getCategory().equals("A")){
					setIcon(detA);
				}else if(sn.getCategory().equals("B")){
					setIcon(detB);
				}else if(sn.getCategory().equals("H")){
					setIcon(detH);
				}else{
					setIcon(det);
				}
			}
			//            setToolTipText("This book is in the Tutorial series.");
		} else {
			//            setToolTipText(null); //no tool tip
		}
		return c;
	}
}