/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2012  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * A theme is a collection of symbols for one layer of a map.
 *
 * @author Douglas Lau
 */
abstract public class Theme {

	/** Name of theme */
	protected final String name;

	/** Default symbol */
	protected Symbol dsymbol;

	/** Mapping of symbols */
	protected final Map<String, Symbol> sym_map =
		new HashMap<String, Symbol>();

	/** Create a new theme */
	protected Theme(String n) {
		name = n;
	}

	/** Get a string representation of the theme */
	public String toString() {
		return name;
	}

	/** Add a symbol to the theme */
	protected void addSymbol(Symbol sym) {
		sym_map.put(sym.getLabel(), sym);
		if(dsymbol == null)
			dsymbol = sym;
	}

	/** Get a symbol by label */
	public Symbol getSymbol(String label) {
		Symbol sym = sym_map.get(label);
		if(sym != null)
			return sym;
		else
			return dsymbol;
	}

	/** Get a list of all symbols */
	public List<Symbol> getSymbols() {
		LinkedList<Symbol> symbols = new LinkedList<Symbol>();
		TreeSet<String> keys = new TreeSet<String>(sym_map.keySet());
		for(String key: keys)
			symbols.add(sym_map.get(key));
		return symbols;
	}

	/** Get the symbol to draw a given map object */
	abstract public Symbol getSymbol(MapObject mo);

	/** Draw the specified map object */
	public void draw(Graphics2D g, MapObject mo, float scale) {
		Symbol s = getSymbol(mo);
		if(s != null) {
			g.transform(mo.getTransform());
			s.draw(g, mo.getShape(), mo.getOutlineShape(), scale);
		}
	}

	/** Draw a selected map object */
	abstract public void drawSelected(Graphics2D g, MapObject mo,
		float scale);

	/** Get tooltip text for the given map object */
	public String getTip(MapObject mo) {
		return mo.toString();
	}
}
