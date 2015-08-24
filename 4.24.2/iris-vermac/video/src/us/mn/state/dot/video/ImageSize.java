/*
* VideoServer
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
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package us.mn.state.dot.video;

import java.awt.Dimension;

public enum ImageSize {

	SMALL (new Dimension(176, 144),   5 * 1024),
	MEDIUM(new Dimension(352, 240),  15 * 1024),
	LARGE (new Dimension(740, 480),  40 * 1024);
	
	private final int maxBytes;
	private final Dimension dimension;

	private ImageSize(Dimension d, int mb){
		dimension = d;
		maxBytes = mb;
	}
	
	public int getMaxBytes(){ return maxBytes; }
	
	public Dimension getDimension(){ return dimension; }
}
