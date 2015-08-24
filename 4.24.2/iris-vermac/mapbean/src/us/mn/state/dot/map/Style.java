/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

import java.awt.Color;

/**
 * A style for stroke/fill of map objects.
 *
 * @author Douglas Lau
 */
public class Style {

	/** Style label */
	protected final String label;

	/** Outline style */
	public final Outline outline;

	/** Fill color */
	public final Color fill_color;

	/** Create a style with given label, outline and fill color */
	public Style(String l, Outline o, Color f) {
		label = l;
		outline = o;
		fill_color = f;
	}

	/** Create a new style with no outline */
	public Style(String l, Color f) {
		this(l, null, f);
	}

	/** Get the style label */
	public String getLabel() {
		return label;
	}
}
