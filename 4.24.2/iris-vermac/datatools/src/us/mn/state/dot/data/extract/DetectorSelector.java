/*
 * DataExtract
 * Copyright (C) 2002-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.data.extract;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import us.mn.state.dot.data.DataFactory;
import us.mn.state.dot.data.NetworkView;
import us.mn.state.dot.data.SystemConfig;
import us.mn.state.dot.data.SystemNode;
import us.mn.state.dot.data.SystemNodeSelectionListener;
import us.mn.state.dot.data.SystemTree;

/**
 * DateSelector
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class DetectorSelector extends Selector
		implements SystemNodeSelectionListener{

	/** Description of the Field */
	protected JList selectedList;

	protected NetworkView network;

	private final List<SystemNode> selectedNodes;

	/**
	 * Create a detector selector
	 *
	 * @param f  The <code>DataFactory</code>
	 * @param e  The <code>DataExtract</code> application
	 */
	public DetectorSelector(DataFactory f, DataExtract e) {
		super( f, e );
		selectedNodes = new ArrayList<SystemNode>();
		SystemConfig[] configs = f.getConfigs();
		SystemTree[] systems = new SystemTree[configs.length];
		for(int i = 0; i < configs.length; i++)
			systems[i] = SystemTree.createTree(configs[i]);
		network = new NetworkView(systems);
		network.addSystemNodeSelectionListener(this);
		selectedList = new JList();
		selectedList.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent ke){
				if(ke.getKeyCode()==KeyEvent.VK_DELETE){
					Object[] nodes =
						selectedList.getSelectedValues();
					for(int i=0; i<nodes.length; i++){
						removeNode((SystemNode)nodes[i]);
					}
				}
			}
		});
		init();
	}

	public String[] getSensorIds(){
		String[] ids = new String[selectedNodes.size()];
		int i=0;
		for(SystemNode node : selectedNodes){
			ids[i++] = node.getId();
		}
		return ids;
	}

	/** Description of the Method */
	public void clear() {
		selectedNodes.clear();
		DefaultComboBoxModel m =
			new DefaultComboBoxModel(selectedNodes.toArray());
		selectedList.setModel(m);
	}

	private JPanel createAddRemovePanel(){
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		JButton b = new JButton( ">" );
		b.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent ae ) {
					SystemNode[] nodes = network.getSelectedNodes();
					if(nodes != null){
						for(int i=0; i<nodes.length; i++){
							addNode(nodes[i]);
						}
					}
				}
			} );
		p.add( b, c );
		b = new JButton( "<" );
		b.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent ae ) {
					if ( selectedList.getSelectedValues() != null ) {
						Object[] vals = selectedList.getSelectedValues();
						for(int i=0; i<vals.length; i++){
							removeNode((SystemNode)vals[i]);
						}
					}
				}
			} );
		p.add( b, c );
		return p;
	}

	/** Description of the Method */
	protected void init() {
		setLayout( new GridBagLayout() );
		GridBagConstraints c = createConstraints();
		c.gridx = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridy = GridBagConstraints.RELATIVE;
		c.weighty = 0;
		add(createTextEntryPanel(), c);
		c.weighty = 1;
		add(createMainPanel(), c);
		c.weighty = 0;
		add(createClearPanel(), c);
	}

	private JPanel createSelectionPanel(){
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		p.add(new JLabel("Selected Sensors"), c);
		c.weighty = 1;
		JScrollPane s = new JScrollPane( selectedList );
		s.setPreferredSize( new Dimension( 50, 50 ) );
		s.setMinimumSize( new Dimension( 50, 50 ) );
		p.add(s, c);
		return p;
	}

	private JPanel createSystemsPanel(){
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		p.add(new JLabel("Available Sensors"), c);
		c.weighty = 1;
		p.add(network, c);
		return p;
	}

	private JPanel createMainPanel(){
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = createConstraints();
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1;
		p.add(createSystemsPanel(), c);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0;
		p.add(createAddRemovePanel(), c);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		p.add(createSelectionPanel(), c);
		return p;
	}

	private JPanel createTextEntryPanel(){
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		final JTextField tf = new JTextField();
		tf.setPreferredSize(new Dimension(200, 20));
		tf.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent ke){
				if(ke.getKeyCode()==KeyEvent.VK_ENTER){
					SystemNode n = network.find(tf.getText());
					if(n == null){
						showMessage("Invalid sensor id: '" + tf.getText() + "'");
					}else{
						addNode(n);
						tf.setText("");
						tf.requestFocus();
					}
				}
			}
		});
		p.add(tf, c);
		JButton b = new JButton("Add");
		b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				SystemNode n = network.find(tf.getText());
				if(n == null){
					JOptionPane.showMessageDialog(DetectorSelector.this,
							"Unable to find sensor " + tf.getText());
				}else{
					addNode(n);
				}
			}
		});
		p.add(b, c);
		return p;
	}

	private void showMessage(String s){
		JOptionPane.showMessageDialog(DetectorSelector.this, s);
	}
	
	private JPanel createClearPanel(){
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		JButton b = new JButton( "Clear" );
		b.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent ae ) {
					clear();
				}
			} );
		p.add(b, c);
		b = new JButton( "Import Sensors..." );
		b.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent ae ) {
					JFileChooser chooser = new JFileChooser( "c:\\temp" );
					chooser.setDialogTitle( "Import file..." );
					chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
					chooser.showDialog(
							( ( JComponent ) ( ae.getSource() ) ).getTopLevelAncestor(), "OK" );
					if ( chooser.getSelectedFile() != null ) {
						importSelections(
								new File( chooser.getSelectedFile().getAbsolutePath() ) );
					}
				}
			} );
		p.add( b, c );
		b = new JButton( "Export Sensors..." );
		b.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent ae ) {
					JFileChooser chooser = new JFileChooser( "c:\\temp" );
					chooser.setDialogTitle( "Export detector selection..." );
					chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
					chooser.showDialog(
							( ( JComponent ) ( ae.getSource() ) ).getTopLevelAncestor(), "OK" );
					if ( chooser.getSelectedFile() != null ) {
						exportSelections(
								new File( chooser.getSelectedFile().getAbsolutePath() ) );
					}
				}
			} );
		p.add( b, c );
		return p;
	}

	/**
	 * Description of the Method
	 *
	 * @param s  Description of Parameter
	 * @return   Description of the Returned Value
	 */
	protected String parseDetectorString( String s ) {
		String result = null;
		try {
			StringTokenizer tokenizer =
				new StringTokenizer( s, " \t+-", true );
			int tokenCount = tokenizer.countTokens();
			if ( tokenCount > 1 ) {
				String token = tokenizer.nextToken();
				if ( token.toUpperCase().equals( "D" ) ) {
					result = "D";
					while ( tokenizer.hasMoreTokens() ) {
						String tok = tokenizer.nextToken();
						if( !tok.equals( " " ) ) {
							result = result + " " + tok.toUpperCase();
						}
					}
				} else if ( token.toUpperCase().equals( "S" ) ) {
					while ( tokenizer.hasMoreTokens() ) {
						String tok = tokenizer.nextToken();
						if( !tok.equals( " " ) && !tok.equals( "\t" ) ) {
//							int index = Integer.parseInt( tok );
//							if( config.getStationLabel( index ) == null )
//								return null; //FIXME
							result = "S " + tok;
							if( tokenizer.hasMoreElements() ) return null;
						}
					}
				}
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
		return result;
	}


	/**
	 * Description of the Method
	 *
	 * @param file  Description of Parameter
	 */
	protected void importSelections( File file ) {
		try {
			FileReader fileReader = new FileReader( file );
			BufferedReader reader = new BufferedReader( fileReader );
			String sensorId = reader.readLine();
			int good = 0;
			int bad = 0;
			while(sensorId != null) {
				SystemNode n = network.find(sensorId);
				if(n != null){
					addNode(n);
					good++;
				}else{
					bad++;
				}
				sensorId = reader.readLine();
			}
			reader.close();
			JOptionPane.showMessageDialog(DetectorSelector.this,
				"Imported " + good + " sensors.\n" +
				"Unable to import " + bad + " sensors.");
		} catch ( FileNotFoundException fnfe ) {
			JOptionPane.showMessageDialog( extractor,
				"Error reading file.\n" +
				"Check to make sure the file exists and " +
				"that you have read permission on the file.\n" +
				"Selection import has been cancelled." );
		} catch ( Exception e ) {
			new ExtractExceptionDialog( e ).setVisible( true );
		}
	}


	/**
	 * Description of the Method
	 *
	 * @param file  Description of Parameter
	 */
	protected void exportSelections( File file ) {
		try {
			FileWriter fileWriter = new FileWriter( file );
			BufferedWriter writer = new BufferedWriter( fileWriter );
			SystemNode n = null;
			for(SystemNode node : selectedNodes){
				writer.write(node.getId());
				writer.newLine();
			}
			writer.flush();
			writer.close();
		} catch ( Exception e ) {
			new ExtractExceptionDialog( e ).setVisible( true );
		}
	}

	public void addNode(SystemNode node){
		if(!node.isSensor()){
			return;
		}
		if(selectedNodes.contains(node)){
			showMessage("Sensor " + node.getId() + " is already selected.");
			return;
		}
		selectedNodes.add(node);
		DefaultComboBoxModel m =
			new DefaultComboBoxModel(selectedNodes.toArray());
		selectedList.setModel(m);
	}

	public void removeNode(SystemNode node){
		selectedNodes.remove(node);
		DefaultComboBoxModel m =
			new DefaultComboBoxModel(selectedNodes.toArray());
		selectedList.setModel(m);
	}

	public void select(String id){
		SystemNode n = network.find(id);
		if(n != null){
			addNode(n);
		}
	}
}

