/*
 * DataExtract
 * Copyright (C) 2005-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.data;

/**
 * @author John3Tim
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Sensor {

	public final static int MAINLINE = 1;
	public final static int HOV = 2;
	public final static int MERGE = 3;
	public final static int AUXILIARY = 4;
	public final static int EXIT = 5;
	public final static int QUEUE = 6;
	public final static int BYPASS = 7;
	public final static int PASSAGE = 8;
	public final static int REVERSIBLE = 9;
	public final static int UNKNOWN = 10;
	protected static final float DEFAULT_FIELD_LENGTH = 22;
	protected String id;
	protected float field = DEFAULT_FIELD_LENGTH;
	protected String label = null;
	protected int category = UNKNOWN;
	
	public Sensor(String name, SystemConfig cfg){
		id = name;
		if(!cfg.getDetectorPrefix().equals("")){
			if(Character.isLetter(id.charAt(0))){
				id = cfg.getDetectorPrefix() + id.substring(1);
			}else{
				id = cfg.getDetectorPrefix() + id;
			}
		}
	}

	public float getField() {
		return field;
	}
	public void setField(float field) {
		this.field = field;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getId() {
		return id;
	}
	
	public String toString(){
		return id + ": " + label;
	}
	
	public int getCategory() {
		return category;
	}
	
	public void setCategory( char cat ) {
		cat = Character.toLowerCase( cat );
		switch( cat ){
			case( 'm' ):
				category = MERGE;
				break;
			case( 'x' ):
				category = EXIT;
				break;
			case( 'p' ):
				category = PASSAGE;
				break;
			case( 'q' ):
				category = QUEUE;
				break;
			case( 'b' ):
				category = BYPASS;
				break;
			case( 'h' ):
				category = HOV;
				break;
			case( 'a' ):
				category = AUXILIARY;
				break;
			case( 'r' ):
				category = REVERSIBLE;
				break;
			case( ' ' ):
				category = MAINLINE;
				break;
			default:
				category = UNKNOWN;
				break;
		}
	}
}
