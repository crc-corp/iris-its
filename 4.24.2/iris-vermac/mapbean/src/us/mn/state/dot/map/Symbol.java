/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.map;

import java.awt.Graphics2D;
import java.awt.Shape;
import javax.swing.Icon;

/**
 * A symbol is a graphical representaion of a map object.  A theme is
 * responsible for selecting which symbol to use for a particular map object.
 *
 * @author Douglas Lau
 * @author Erik Engstrom
 */
public interface Symbol {

	/** Get the symbol label */
	String getLabel();

	/** Get the legend icon */
	Icon getLegend();

	/** Draw the symbol */
	void draw(Graphics2D g, Shape shp, Shape out, float scale);
}
