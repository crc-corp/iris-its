/*
 * Project: Video
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
package us.mn.state.dot.video;

import java.text.SimpleDateFormat;

/**
 * 
 * @author Timothy Johnson
 *
 */
public class Constants {

	public static final SimpleDateFormat DATE_FORMAT =
		new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

	public static final SimpleDateFormat LAST_MODIFIED_FORMAT =
			new SimpleDateFormat("EEE, d MMM y HH:mm:ss z");
			//Tue, 15 Nov 1994 12:45:26 GMT

	//public static final String LOGGER_NAME = "us.mn.state.dot.video";

	public static final String LOGGER_NAME = "video";
}
