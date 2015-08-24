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
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionListener;


/**
  * A fancy text search list box widget
  *
  * @author Douglas Lau
  */
public final class TextSearchList extends JPanel {


	/** Underlying list model */
	private final ListModel model;

	/** Text field for entering the search string */
	private final JTextField text = new JTextField( 12 );

	/** List component */
	private JList list;

	/** Create a new TextSearchList from an array of objects */
	public TextSearchList( Object [] array, String prototype ) {
		super( false );
		DefaultListModel m = new DefaultListModel();
		for( int i = 0; i < array.length; i++ )
			m.addElement( array[ i ] );
		model = m;
		initializeList( prototype );
	}

	/** Create a new TextSearchList from a ListModel */
	public TextSearchList( ListModel m, String prototype ){
		super( false );
		model = m;
		initializeList( prototype );
	}


	/** Initialize the list */
	private void initializeList( String prototype ) {
		GridBagLayout lay = new GridBagLayout();
		setLayout( lay );
		GridBagConstraints bag = new GridBagConstraints();
		bag.fill = GridBagConstraints.BOTH;
		JLabel label = new JLabel( "Search" );
		lay.setConstraints( label, bag );
		add( label );
		lay.setConstraints( text, bag );
		add( text );
//		text.addKeyListener( new KeyAdapter() {
//			public void keyReleased( KeyEvent e ) {
//				processText( e );
//			}
//		} );
		list = new JList( model );
		list.setPrototypeCellValue( prototype );
		JScrollPane pane = new JScrollPane( list );
		bag.gridx = 0;
		bag.gridy = 1;
		bag.gridwidth = 2;
		bag.weighty = 1.0;
		lay.setConstraints( pane, bag );
		add( pane );
	}


	/** Add a ListSelectionListener (delegate to JList) */
	public void addListSelectionListener( ListSelectionListener listener ) {
		list.addListSelectionListener( listener );
	}


	/** Remove a ListSelectionListener (delegate to JList) */
	public void removeListSelectionListener( ListSelectionListener listener ) {
		list.removeListSelectionListener( listener );
	}


	/** Get the selected index (delegate to JList) */
	public int getSelectedIndex() {
		return list.getSelectedIndex();
	}


	/** Get the selected values (delegate to JList) */
	public Object[] getSelectedValues() {
		return list.getSelectedValues();
	}


	/** Set the selected index */
	public void setSelectedIndex( int index ) {
		list.ensureIndexIsVisible( index );
		if( index < list.getLastVisibleIndex() ) {
			int first = list.getFirstVisibleIndex();
			if( first > 0 ) list.ensureIndexIsVisible( first - 1 );
		}
		list.setSelectedIndex( index );
	}


	/** Is the Selection Empty (delegate to JList)? */
	public boolean isSelectionEmpty() {
		return list.isSelectionEmpty();
	}


	/** Search the list for an item which matches the entered text */
	private void searchList( KeyEvent e, String search ) {
		int len = model.getSize();
		int start = 0;
		if( e.getKeyCode() == 18 ) {
			start = list.getSelectedIndex() + 1;
			if( start >= len ) start = 0;
		}
		if( search.length() > 0 ) {
			for( int i = start; i < len; i++ ) {
				String label = model.getElementAt( i ).toString();
				StringTokenizer tokenizer = new StringTokenizer( label );
				if( tokenizer.nextToken().equals( search ) ) {
					list.ensureIndexIsVisible( i );
					list.setSelectedIndex( i );
					return;
				}
				if( start > 0 ) if( i + 1 == len ) {
					start = 0;
					i = -1;
				}
			}
		}
		list.getSelectionModel().clearSelection();
	}

	protected void processText( KeyEvent event ) {
		String search = text.getText();
		try {
			Integer.parseInt( search );
			searchList( event, search );
		} catch ( NumberFormatException nfe ) {
			StringBuffer b = new StringBuffer();
			for ( int i = 0; i < search.length(); i++ ) {
				if ( Character.isDigit( search.charAt( i ) ) ) {
					b.append( search.charAt( i ) );
				}
			}
			text.setText( b.toString() );
			if ( b.length() > 0 ) {
				searchList( event, b.toString() );
			}
		} catch ( Exception e ) {
		}
	}

	public void clear(){
		text.setText( "" );
		list.getSelectionModel().clearSelection();
	}

	/**
	 * Searches model and sets selected item when a valid key is entered
	 *
	 * @param index  Description of Parameter
	 */
	protected void processEntry( int index ) {
		ListModel m = list.getModel();
		if ( index > 0 && index < m.getSize() ) {
			list.setSelectedIndex( index - 1 );
			list.ensureIndexIsVisible( index - 1 );
		} else {
			text.setText( Integer.toString( index / 10 ) );
			processEntry( index / 10 );
		}
	}

	/** Delegate mouseListeners to the JList .*/
	public void addMouseListener( MouseListener listener ) {
		list.addMouseListener( listener );
	}

	/** This is overriden to make the JTextfield the component with focus. */
	public void requestFocus() {
		text.requestFocus();
	}
}
