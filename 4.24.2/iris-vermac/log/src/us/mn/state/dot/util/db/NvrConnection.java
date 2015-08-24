/*
 * Utility classes project
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.util.db;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Properties;


public class NvrConnection extends DatabaseConnection {

	protected static final String OFFSET_TABLE = "file_offset";
	protected static final String OFFSET_FIELD = "file_offset";
	protected static final String CAMERA_FIELD = "camera_id";
	protected static final String FILENAME_FIELD = "file_name";
	protected static final String TIME_FIELD = "time";
	
	public NvrConnection(Properties p){
		super(
			DatabaseConnection.TYPE_POSTGRES,
			p.getProperty("nvr.db.user"),
			p.getProperty("nvr.db.pwd"),
			p.getProperty("nvr.db.host"),
			Integer.parseInt(p.getProperty("nvr.db.port")),
			"nvr");
	}

	public long getOffset(Calendar c, String camId){
		try{
			String q = "select * from " + OFFSET_TABLE + " where " +
				CAMERA_FIELD + " = '" + camId + "' and " +
				TIME_FIELD + " >= '" + calendar2String(c) +
				"' order by " + TIME_FIELD + " " + ASCENDING;
			ResultSet rs = query(q);
			if(rs.next()) return rs.getLong(OFFSET_FIELD);
			return -1;
		}catch(Exception e){
			e.printStackTrace();
			return -1; 
		}
	}

	public Calendar getBegin(String camId){
		return getCalendarExtent(camId, ASCENDING);
	}
	
	public Calendar getEnd(String camId){
		return getCalendarExtent(camId, DESCENDING);
	}

	private Calendar getCalendarExtent(String camId, String order){
		try{
			String q = "select * from " + OFFSET_TABLE + " where " +
				CAMERA_FIELD + " = '" + camId + "' " +
				"order by " + TIME_FIELD + " " + order;
			ResultSet rs = query(q);
			if(rs.next()) return getCalendar(rs.getString(TIME_FIELD));
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public String getFilename(String camId){
		try{
			String q = "select * from " + OFFSET_TABLE + " where " +
				CAMERA_FIELD + " = '" + camId + "'";
			ResultSet rs = query(q);
			if(rs.next()) return rs.getString(FILENAME_FIELD);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
