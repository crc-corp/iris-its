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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import us.mn.state.dot.data.Constants;
import us.mn.state.dot.data.DataFactory;

/**
 * Selector
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @version   $Revision: 1.13 $ $Date: 2005/08/15 16:33:04 $
 */
public abstract class Selector extends JPanel
		 implements Constants {

	/** The factory for all data */
	protected final DataFactory factory;

	/** The DataExtract frame that contains this selector */
	protected DataExtract extractor = null;

	protected final Dimension MIN_FIELD_DIM = new Dimension( 100, 20 );

	/**
	 * Constructor for the Selector object
	 *
	 * @param f        Description of Parameter
	 * @param e        Description of Parameter
	 */
	public Selector( DataFactory f, DataExtract e ) {
		factory = f;
		extractor = e;
		setBorder( BorderFactory.createLineBorder( Color.black ) );
	}


	/** Description of the Method */
	public abstract void clear();


	/**
	 * Description of the Method
	 *
	 * @return   Description of the Returned Value
	 */
	public GridBagConstraints createConstraints() {
		GridBagConstraints c = new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets( 2, 2, 2, 2 ), 0, 0 );
		return c;
	}


	/** Description of the Method */
	protected abstract void init();

}

