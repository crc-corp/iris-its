/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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

/**
 * A tile theme draws tiles on a map.
 *
 * @author Douglas Lau
 */
public class TileTheme extends Theme {

	/** Create a new tile theme */
	protected TileTheme() {
		super("Tile");
	}

	/** Draw the specified map object */
	public void draw(Graphics2D g, MapObject mo, float scale) {
		if(mo instanceof TileMapObject) {
			TileMapObject tmo = (TileMapObject)mo;
			g.setTransform(tmo.getTransform());
			g.drawImage(tmo.getImage(), 0, 0, null);
		}
	}

	/** Get the symbol to draw a given map object */
	public Symbol getSymbol(MapObject mo) {
		// symbols aren't used with tiles
		return null;
	}

	/** Draw a selected map object */
	public void drawSelected(Graphics2D g, MapObject mo, float scale) {
		// tiles cannot be selected
	}
}
