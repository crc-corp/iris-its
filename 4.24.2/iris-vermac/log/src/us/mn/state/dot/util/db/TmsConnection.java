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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;


public class TmsConnection extends DatabaseConnection {

	protected static final String CAMERA_ID = "name";
	protected static final String CAMERA_ENCODER = "encoder";
	protected static final String CAMERA_ENCODER_CHANNEL = "encoder_channel";
	protected static final String CAMERA_PUBLISH = "publish";
	protected static final String CAMERA_NVR = "nvr";
	
	public static final int TYPE_CONTROLLER = 1;
	public static final int TYPE_COMM_LINK = 2;
	public static final int TYPE_CAMERA = 3;
	public static final int TYPE_DETECTOR = 4;
	public static final int TYPE_LCS = 5;
	public static final int TYPE_DMS = 6;
	public static final int TYPE_METER = 7;
	public static final int TYPE_ROADWAY = 8;
	
	protected static final String TABLE_CAMERA = "camera_view";
	protected static final String TABLE_DMS = "dms_view";
	protected static final String TABLE_METER = "ramp_meter_view";
	protected static final String TABLE_DETECTOR = "detector_view";
	protected static final String TABLE_COMMLINK = "comm_link_view";
	protected static final String TABLE_CONTROLLER = "controller_loc_view";

	protected static final String F_CROSS_STREET = "cross_street";
	protected static final String F_CROSS_DIR = "cross_dir";
	protected static final String F_CROSS_MOD = "cross_mod";
	protected static final String F_ROADWAY = "roadway";
	protected static final String F_ROADWAY_DIR = "road_dir";

	protected static final String F_DMS_ID = "name";
	protected static final String F_CAMERA_ID = "name";
	protected static final String F_METER_ID = "name";
	protected static final String F_DETECTOR_ID = "det_id";
	protected static final String F_COMMLINK_ID = "name";
	protected static final String F_COMMLINK_URL = "url";
	protected static final String F_CONTROLLER_ID = "name";
	
	public TmsConnection(Properties p){
		super(
			DatabaseConnection.TYPE_POSTGRES,
			p.getProperty("tms.db.user"),
			p.getProperty("tms.db.pwd"),
			p.getProperty("tms.db.host"),
			Integer.parseInt(p.getProperty("tms.db.port")), "tms");
	}

	protected String createId(int camNumber){
		String id = Integer.toString(camNumber);
		while(id.length()<4) id = "0" + id;
		return "C" + id;
	}
	
	public String getNvrHost(String camId){
		try{
			String q = "select " + CAMERA_NVR + " from " + TABLE_CAMERA +
				" where " + CAMERA_ID + " = '" + camId + "'";
			ResultSet rs = query(q);
			if(rs.next()) return rs.getString(CAMERA_NVR);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null; 
	}

	public String getEncoderHost(String camId){
		try{
			String q = "select " + CAMERA_ENCODER + " from " + TABLE_CAMERA +
				" where " + CAMERA_ID + " = '" + camId + "'";
			ResultSet rs = query(q);
			if(rs.next()) return rs.getString(CAMERA_ENCODER);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null; 
	}

	/**
	 * Get an array of encoder hostnames for all cameras.
	 */
	public String[] getEncoderHosts(){
		String q = "select distinct " + CAMERA_ENCODER + " from " + TABLE_CAMERA +
			" where " + CAMERA_ENCODER + " is not null";
		return getColumn(query(q), CAMERA_ENCODER);
	}
	
	/**
	 * Get an array of camera ids for the given encoder ip address.
	 * @param host The hostname of the encoder.
	 * @return An array camera ids.
	 */
	public String[] getCameraIdsByEncoder(String ip){
		String q = "select " + CAMERA_ID + " from " + TABLE_CAMERA +
			" where " + CAMERA_ENCODER + " like '" + ip + ":%'";
		return getColumn(query(q), CAMERA_ID);
	}

	/**
	 * Get an array of camera ids for the given nvr ip address.
	 * @param host The IP of the nvr.
	 * @return An array camera ids.
	 */
	public String[] getCameraIdsByNvr(String ip){
		String q = "select " + CAMERA_ID + " from " + TABLE_CAMERA +
			" where " + CAMERA_NVR + " like '" + ip + ":%'";
		return getColumn(query(q), CAMERA_ID);
	}

	public int getEncoderChannel(String camId){
		String q = "select " + CAMERA_ENCODER_CHANNEL + " from " + TABLE_CAMERA +
			" where " + CAMERA_ID + " = '" + camId + "'";
		try{
			ResultSet rs = query(q);
			if(rs.next()) return rs.getInt(CAMERA_ENCODER_CHANNEL);
		}catch(Exception e){
			e.printStackTrace();
		}
		return -1;
	}

	/** Get the publish attribute of the camera */
	public boolean isPublished(String camId){
		String q = "select " + CAMERA_PUBLISH + " from " + TABLE_CAMERA +
			" where " + CAMERA_ID + " = '" + camId + "'";
		try{
			ResultSet rs = query(q);
			if(rs.next()) return rs.getBoolean(CAMERA_PUBLISH);
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	/** Get the location of a device */
	public String getLocation(int type, String deviceName){
		String table = null;
		String idField = null;
		switch (type) {
			case TYPE_DMS:
				table = TABLE_DMS;
				idField = F_DMS_ID;
				break;
			case TYPE_CAMERA:
				table = TABLE_CAMERA;
				idField = F_CAMERA_ID;
				break;
			case TYPE_DETECTOR:
				table = TABLE_DETECTOR;
				idField = F_DETECTOR_ID;
				break;
			case TYPE_METER:
				table = TABLE_METER;
				idField = F_METER_ID;
				break;
			case TYPE_CONTROLLER:
				table = TABLE_CONTROLLER;
				idField = F_CONTROLLER_ID;
				break;
			default:
				break;
		}
		if(table == null || idField == null) return "";
		String q = "select " + F_ROADWAY + ", " + F_ROADWAY_DIR + ", " +
			F_CROSS_STREET + ", " + F_CROSS_DIR + ", " + F_CROSS_MOD +
			" from " + table + " where " + idField + " = '" + deviceName + "'";
		String loc = "";
		try{
			ResultSet rs = query(q);
			if(rs.next()){
				loc = loc.concat(rs.getString(F_ROADWAY));
				loc = loc.concat(" " + rs.getString(F_ROADWAY_DIR));
				loc = loc.concat(" " + rs.getString(F_CROSS_MOD));
				loc = loc.concat(" " + rs.getString(F_CROSS_STREET));
				loc = loc.concat(" " + rs.getString(F_CROSS_DIR));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return loc;
	}

	/** Get a list of names for a given device type
	 */
	public ArrayList<String> getNames(int type){
		switch(type){
			case(TYPE_CONTROLLER):
				return getControllerNames();
			case(TYPE_COMM_LINK):
				return getCommLinkNames();
			case(TYPE_CAMERA):
				return getCameraNames();
			case(TYPE_DETECTOR):
				return getDetectorNames();
			case(TYPE_DMS):
				return getDMSNames();
			case(TYPE_LCS):
				return getLCSNames();
			case(TYPE_METER):
				return getMeterNames();
			case(TYPE_ROADWAY):
				return getRoadwayNames();
		}
		return new ArrayList<String>();
	}
	
	private ArrayList<String> getControllerNames(){
		ArrayList<String> list = new ArrayList<String>();
		String sql = "select comm_link, drop_id " +
				"from controller_loc_view ";
		ResultSet set = query(sql);
		try{
			set.beforeFirst();
			while(set.next()){
				list.add(set.getString("comm_link") + "D" + set.getString("drop_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return list;
	}
	
	/** Get a list of comm_link names. */
	private ArrayList<String> getCommLinkNames(){
		ArrayList<String> list = new ArrayList<String>();
		String sql = "select name from comm_link";
		ResultSet set = query(sql);
		try {
			set.beforeFirst();
			while ( set.next() ) {
				 list.add(set.getString("name"));
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
		return list;
	}

	/** Get a list of meter names. */
	private ArrayList<String> getMeterNames(){
		ArrayList<String> list = new ArrayList<String>();
		String sql = "select id from ramp_meter_view";
		ResultSet set = query(sql);
		try {
			set.beforeFirst();
			while ( set.next() ) {
				list.add(set.getString("id"));
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
		return list;
	}

	/** Get a list of detector names. */
	private ArrayList<String> getDetectorNames(){
		ArrayList<String> list = new ArrayList<String>();
		String sql = "select det_id from detector_view";
		ResultSet set = query(sql);
		try {
			set.beforeFirst();
			while ( set.next() ) {
				list.add(set.getString("det_id"));
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
		return list;
	}

	/** Get a list of DMS names. */
	private ArrayList<String> getDMSNames(){
		ArrayList<String> list = new ArrayList<String>();
		String sql = "select id from dms_view";
		ResultSet set = query(sql);
		try {
			set.beforeFirst();
			while ( set.next() ) {
				list.add(set.getString("id"));
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
		return list;
	}

	/** Get a list of camera names. */
	private ArrayList<String> getCameraNames(){
		ArrayList<String> list = new ArrayList<String>();
		String sql = "select name from camera_view order by name";
		ResultSet set = query(sql);
		try {
			set.beforeFirst();
			while ( set.next() ) {
				list.add(set.getString("name"));
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
		return list;
	}

	/** Get a list of LCS names. */
	private ArrayList<String> getLCSNames(){
		ArrayList<String> list = new ArrayList<String>();
		String sql = "select id from lcs";
		ResultSet set = query(sql);
		try {
			set.beforeFirst();
			while ( set.next() ) {
				list.add(set.getString("id"));
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
		return list;
	}

	/** Get a list of Roadway names. */
	private ArrayList<String> getRoadwayNames(){
		ArrayList<String> list = new ArrayList<String>();
		String sql = "select name from road_view order by name";
		ResultSet set = query(sql);
		try {
			set.beforeFirst();
			while ( set.next() ) {
				list.add(set.getString("name"));
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
		return list;
	}

	/** Get the comm_link name for the given URL */
	public String getCommLink(String url){
		try{
			String q = "select " + F_COMMLINK_ID + " from " + TABLE_COMMLINK +
				" where " + F_COMMLINK_URL + " = '" + url + "'";
			ResultSet rs = query(q);
			if(rs.next()) return rs.getString(F_COMMLINK_ID);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null; 
	}

	/** Get the url for the given comm_link name */
	public String getURL(String name){
		try{
			String q = "select " + F_COMMLINK_URL + " from " + TABLE_COMMLINK +
				" where " + F_COMMLINK_ID + " = '" + name + "'";
			ResultSet rs = query(q);
			if(rs.next()) return rs.getString(F_COMMLINK_URL);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null; 
	}

}
