/*
 * DataExtract
 * Copyright (C) 2005-2008  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * @author John3Tim
 */
public class DataTool extends JFrame implements Constants {

	/** About dialog box */
	protected final About about;

	/** Create a new data tool */
	public DataTool(String title, DataFactory f) {
		super(title);
		about = new About(this, f);
	}

	/**
	 * Create the Help menu
	 *
	 * @return   The menu
	 */
	public JMenu createHelpMenu() {
		JMenu file = new JMenu( "Help" );
		Action popAbout = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				about.setVisible(true);
			}
		};
		popAbout.putValue( Action.NAME, "About..." );
		file.add( new JMenuItem( popAbout ) );
		return file;
	}
}
