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
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.Collection;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;

import us.mn.state.dot.data.DataFactory;

/**
 * Description of the Class
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @version   $Revision: 1.29 $ $Date: 2005/08/09 18:52:42 $
 */
public class OutputSelector extends Selector {

	/** Constant for the VALUES element */
	public static String VALUES = "Values";

	/** Constant for the SUM element */
	public static String SUM = "Sum";

	/** Constant for the AVERAGE element */
	public static String AVERAGE = "Average";

	/** Constant for the TIME_MEDIAN element */
	public static String TIME_MEDIAN = "Time Median";

	/** Constant for the DAY_MEDIAN element */
	public static String DAY_MEDIAN = "Day Median";

	/** Constant for the SAMPLE element */
	public static String SAMPLE = "Sample";

	public static JCheckBox[] options = {
			new JCheckBox(VALUES),
			new JCheckBox(SUM),
			new JCheckBox(AVERAGE),
			new JCheckBox(TIME_MEDIAN),
			new JCheckBox(DAY_MEDIAN),
			new JCheckBox(SAMPLE) };

	/** The file format of the output */
	protected FileFormat format;

	/** Description of the Field */
	protected JTextField name;

	/** Description of the Field */
	protected DefaultComboBoxModel typeModel;

	/** Description of the Field */
	protected JComboBox detectors, ranges, dates, sets;

	private String[] headers = {
			new String( "Columns" ),
			new String( "Rows" )};

	/** Description of the Field */
	protected JLabel location;

	private FileFormat[] formatOptions = {
			new CompositeFile(),
			new DetectorFile(),
			new DateFile(),
			new DataFile(),
			new CongestionFile(),
			new LaneClosureFile(),
			new DBFile()
	};
	
	private JComboBox formats = new JComboBox(formatOptions);
	
	/**
	 * Constructor for the OutputSelector object
	 *
	 * @param f    Description of Parameter
	 * @param e    Description of Parameter
	 */
	public OutputSelector( DataFactory f, DataExtract e ) {
		super( f, e );
		format = null;
		init();
		updateFileFormat();
	}


	/**
	 * Sets the orientation attribute of the OutputSelector object
	 *
	 * @param dataType     The new orientation value
	 * @param orientation  The new orientation value
	 */
	public void setOrientation( int dataType, String orientation ) {
		if ( !orientation.equals( "Rows" ) && !orientation.equals( "Columns" ) ) {
			return;
		}
		if ( dataType == DETECTORS ) {
			detectors.setSelectedItem( orientation );
		} else if ( dataType == TIMES ) {
			ranges.setSelectedItem( orientation );
		} else if ( dataType == DATA ) {
			sets.setSelectedItem( orientation );
		} else if ( dataType == DATES ) {
			dates.setSelectedItem( orientation );
		}
		updateFileFormat();
	}


	/**
	 * Sets the filePath attribute of the OutputSelector object
	 *
	 * @param p  The new filePath value
	 */
	public void setFilePath( String p ) {
		location.setText( p );
	}


	/**
	 * Sets the fileFormat attribute of the OutputSelector object
	 *
	 * @param f  The new fileFormat value
	 */
	public void setFileFormat( FileFormat f ) {
		if( f == null ) return;
		ComboBoxModel model = formats.getModel();
		if ( f != null ) {
			if ( f.getRows() != null && f.getRows().contains( "Times" ) ) {
				setOrientation( TIMES, "Rows" );
			} else {
				setOrientation( TIMES, "Columns" );
			}
		}
		for ( int i = 0; i < model.getSize(); i++ ) {
			String s = model.getElementAt( i ).toString();
			if ( s.equals( f.toString() ) ) {
				formats.setSelectedIndex( i );
			}
		}
	}


	/**
	 * Sets the fileName attribute of the OutputSelector object
	 *
	 * @param n  The new fileName value
	 */
	public void setFileName( String n ) {
		name.setText( n );
		validateFileName();
	}


	/**
	 * Gets the rows attribute of the OutputSelector object
	 *
	 * @return   The rows value
	 */
	public Collection getRows() {
		Vector<String> v = new Vector<String>();
		if ( ranges.getSelectedItem().toString().equals( "Rows" ) ) {
			v.addElement( "Times" );
		}
		return v;
	}


	/**
	 * Gets the columns attribute of the OutputSelector object
	 *
	 * @return   The columns value
	 */
	public Collection<String> getColumns() {
		Vector<String> v = new Vector<String>();
		v.addElement( "Detectors" );
		if ( ranges.getSelectedItem().toString().equals( "Columns" ) ) {
			v.addElement( "Times" );
		}
		v.addElement( "Dates" );
		v.addElement( "Data Sets" );
		return v;
	}


	/**
	 * Description of the Method
	 *
	 * @param option  Description of Parameter
	 */
	public void selectOutputOption( String option ) {
		for(int i=0; i<options.length; i++){
			if(options[i].getText().equals(option)){
				options[i].setSelected(true);
				break;
			}
		}
		updateFileFormat();
	}


	/** Description of the Method */
	public void clear() {
		location.setText( "" );
		for(int i=0; i<options.length; i++){
			options[i].setSelected(false);
		}
	}

	public File getOutputDir(){
		if( location.getText() == null ){
			return null;
		}
		return new File(location.getText());
	}

	public void setOutputDir(File dir){
		if(dir == null) return;
		if(dir.isDirectory()){
			location.setText(dir.getAbsolutePath());
		}
	}
	
	/**
	 * Gets the dataElements attribute of the OutputSelector object
	 *
	 * @return   The dataElements value
	 */
	protected Collection getDataElements() {
		Collection c = new Vector();
		for(int i=0; i<options.length; i++){
			if(options[i].isSelected()){
				c.add(options[i].getText());
			}
		}
		return c;
	}


	/** Description of the Method */
	protected void updateFileFormat() {
		format = ( FileFormat ) ( formats.getSelectedItem() );
		format.setDataElements( getDataElements() );
		format.setRows( getRows() );
		format.setColumns( getColumns() );
	}

	/** Description of the Method */
	public FileFormat getFileFormat() {
		FileFormat f = (FileFormat)(formats.getSelectedItem());
		format.setDataElements( getDataElements() );
		format.setRows( getRows() );
		format.setColumns( getColumns() );
		return f;
	}

	public String getFileName(){
		return name.getText();
	}

	/** Description of the Method */
	protected void init() {
		setLayout( new GridBagLayout() );
		GridBagConstraints c = createConstraints();
		c.gridy = GridBagConstraints.RELATIVE;
		add( new JLabel( "File Location:" ), c );
		add( new JLabel( "File Name:" ), c );
		add( new JLabel( "File Format:" ), c );
		c.gridy = 3;
		add( new JLabel( "Export:" ), c );
		c.gridy = 9;
		add( new JLabel( "Time ranges in:" ), c );
		c.gridx = 1;
		c.gridy = 0;
		location = new JLabel( extractor.getProperty( extractor.PROP_OUT_DIR ) );
		add( location, c );
		c.gridy = GridBagConstraints.RELATIVE;
		name = new JTextField();
		name.setMinimumSize(MIN_FIELD_DIM);
		name.setPreferredSize(MIN_FIELD_DIM);
		name.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				validateFileName();
			}
		} );
		name.addFocusListener( new FocusAdapter() {
			public void focusLost( FocusEvent fe ) {
				validateFileName();
			}
		} );
		add( name, c );
		formats.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				validateOptionSelections( ( FileFormat ) ( formats.getSelectedItem() ) );
				updateFileFormat();
			}
		} );
		add( formats, c );
		for(int i=0; i<options.length; i++){
			options[i].addActionListener( new OptionsListener() );
			add( options[i], c );
		}
		ranges = new JComboBox( headers );
		ranges.addActionListener( new OptionsListener() );
		add( ranges, c );
		c.gridx = 2;
		c.gridy = 0;
		JButton b = new JButton( "Browse..." );
		b.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent ae ) {
					JFileChooser chooser = null;
					FileSystemView system = extractor.getFileSystemView();
					if ( location.getText() == null || location.getText().equals( "" ) ) {
						chooser = new JFileChooser(
								system.getDefaultDirectory(), system );
					} else {
						chooser = new JFileChooser( location.getText() );
					}
					chooser.setDialogTitle( "Output file(s) location" );
					chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
					chooser.showDialog(
							( ( JComponent ) ( ae.getSource() ) ).getTopLevelAncestor(), "OK" );
					if ( chooser.getSelectedFile() != null ) {
						location.setText( chooser.getSelectedFile().getAbsolutePath() );
						extractor.setProperty( extractor.PROP_OUT_DIR, location.getText() );
					}
				}
			} );
		add( b, c );
	}


	/**
	 * Description of the Method
	 *
	 * @param f  Description of Parameter
	 */
	protected void validateOptionSelections( FileFormat f ) {
		Collection validOptions = f.getOutputOptions();
		for(int i=0; i<options.length; i++){
			boolean contained = validOptions.contains(
					options[i].getText());
			options[i].setEnabled(contained);
			if(!contained){
				options[i].setSelected(false);
			}
		}
		if(f instanceof CompositeFile ||
				f instanceof CongestionFile ||
				f instanceof DBFile){
			name.setEnabled(true);
			name.setBackground( Color.WHITE );
		}else{
			name.setEnabled(false);
			name.setText("");
			name.setBackground( Color.LIGHT_GRAY );
		}
	}


	/** Description of the Method */
	private void validateFileName() {
		String s = name.getText();
		int periodIndex = s.lastIndexOf( '.' );
		if ( periodIndex > -1 ) {
			s = s.substring( 0, s.lastIndexOf( '.' ) );
			name.setText( s );
		}
	}


	/**
	 * Description of the Class
	 *
	 * @author    john3tim
	 */
	private class OptionsListener implements ActionListener {

		/**
		 * Description of the Method
		 *
		 * @param ae  Description of Parameter
		 */
		public void actionPerformed( ActionEvent ae ) {
			updateFileFormat();
		}
	}
}
