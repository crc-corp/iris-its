/*
 * DataExtract
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.data.extract;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import us.mn.state.dot.data.DataFactory;

/**
 * DataSetSelector
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @version   $Revision: 1.20 $ $Date: 2005/08/09 19:04:30 $
 */
public class DataSetSelector extends Selector {

	/** Check boxes for the various data sets available. */
	protected final JCheckBox[] sets = {
		new JCheckBox(VOLUME),
		new JCheckBox(OCCUPANCY),
		new JCheckBox(FLOW),
		new JCheckBox(HEADWAY),
		new JCheckBox(DENSITY),
		new JCheckBox(SPEED)};

	protected JButton clearButton;

	/**
	 * Create a data set selector
	 *
	 * @param f  The <code>DataFactory</code> from which to retrieve
	 * traffic data
	 * @param e  Description of Parameter
	 */
	public DataSetSelector( DataFactory f, DataExtract e ) {
		super( f, e );
		init();
	}


	/** Description of the Method */
	public void clear() {
		for(int i=0; i<sets.length; i++){
			sets[i].setSelected(false);
		}
	}

	public void setEnabled( boolean enabled ) {
		for(int i=0; i<sets.length; i++){
			sets[i].setEnabled(enabled);
		}
		clearButton.setEnabled(enabled);
	}

	public void select(String dataSet){
		for(int i=0; i<sets.length; i++){
			if(sets[i].getText().equals(dataSet)){
				sets[i].setSelected(true);
				break;
			}
		}
	}
	
	/** Description of the Method */
	protected void init() {
		setLayout( new GridBagLayout() );
		GridBagConstraints c = createConstraints();
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		add(createSetsPanel(), c);
		c.weighty = 0;
		add(createButtonsPanel(), c);
	}
	
	private JPanel createSetsPanel(){
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = createConstraints();
		c.gridy = GridBagConstraints.RELATIVE;
		for(int i=0; i<sets.length; i++){
			add(sets[i], c);
		}
		return p;
	}
	
	private JPanel createButtonsPanel(){
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = createConstraints();
		c.gridy = GridBagConstraints.RELATIVE;
		JButton b = new JButton("Clear");
		b.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent ae ) {
					clear();
				}
			} );
		c.anchor = GridBagConstraints.SOUTHWEST;
		add(b, c);
		return p;
	}

	public String[] getDataSets(){
		Vector<String> v = new Vector<String>();
		for(int i=0; i<sets.length; i++){
			if(sets[i].isSelected()){
				v.add(sets[i].getText());
			}
		}
		return (String[])(v.toArray(new String[0]));
	}

	public void setDataSets(String[] array){
		clear();
		for(int i=0; i<sets.length; i++){
			if(sets[i].getText().equals(array[i])){
				sets[i].setSelected(true);
			}
		}
	}
}