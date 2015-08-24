/*
 * Video project
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.video;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * DatabaseConnection is a convenience class for making a connection to
 * a database.  It handles all of the queries and sql exceptions as
 * well as re-establishing the connection if it is lost.
 * 
 * @author Timothy Johnson
 *
 */
public class DatabaseConnection {

	protected static final String CAMERA_ID = "name";
	protected static final String CAMERA_ENCODER = "encoder";
	protected static final String CAMERA_ENCODER_CHANNEL = "encoder_channel";
	protected static final String CAMERA_PUBLISH = "publish";
	protected static final String CAMERA_ENCODER_TYPE = "encoder_type";

	protected static final String TABLE_CAMERA = "camera_view";

	protected static final String ASCENDING = "asc";
	
	protected static final String DESCENDING = "desc";
	
	/** Username for authentication to the db server */
	private String user = null;

	/** The name of the database to connect to */
	private String dbName = null;

	/** Password for authentication to the db server */
	private String password = null;

	/** Database URL */
	private String url = null;

	/** The connection object used for executing queries */
	protected Connection connection = null;
	
	protected PreparedStatement isPublishedStatement = null;
	
	protected PreparedStatement encoderHostStatement = null;

	protected PreparedStatement encoderTypeStatement = null;

	protected PreparedStatement encoderChannelStatement = null;

	private static DatabaseConnection db = null;
	
	private Logger logger = null;

	public static synchronized DatabaseConnection create(final Properties p){
		if(db == null){
			try{
				db = new DatabaseConnection(p);
			}catch(Exception e){
				return null;
			}
		}
		return db;
	}

	private DatabaseConnection(Properties p){
		this.logger = Logger.getLogger(Constants.LOGGER_NAME);
		this.user = p.getProperty("tms.db.user");
		this.dbName = p.getProperty("tms.db.name");
		this.password = p.getProperty("tms.db.pwd");
		String port_name_separator = "/";
		url = "jdbc:postgresql://" +
				p.getProperty("tms.db.host") + ":" +
				p.getProperty("tms.db.port") +
				port_name_separator +
				dbName;
		connect();
	}

	private void connect(){
		try {
			Class.forName( "org.postgresql.Driver" );
			logger.info( "Openning connection to " + dbName + " database." );
			connection = DriverManager.getConnection( url, user, password );
			DatabaseMetaData md = connection.getMetaData();
			String dbVersion = md.getDatabaseProductName() + ":" + md.getDatabaseProductVersion();
			logger.info("DB: " + dbVersion);
			isPublishedStatement = connection.prepareStatement(
					"select " + CAMERA_PUBLISH + " from " + TABLE_CAMERA +
					" where " + CAMERA_ID + " = ?");
			encoderHostStatement = connection.prepareStatement(
					"select " + CAMERA_ENCODER + " from " + TABLE_CAMERA +
					" where " + CAMERA_ID + " = ?");
			encoderTypeStatement = connection.prepareStatement(
					"select " + CAMERA_ENCODER_TYPE + " from " + TABLE_CAMERA +
					" where " + CAMERA_ID + " = ?");
			encoderChannelStatement = connection.prepareStatement(
					"select " + CAMERA_ENCODER_CHANNEL + " from " + TABLE_CAMERA +
					" where " + CAMERA_ID + " = ?");
			logger.info( "Opened connection to " + dbName + " database." );
		} catch ( Exception e ) {
			System.err.println("Error connecting to DB: " + url + " USER: " + user + " PWD: " + password );
		}
	}
	
	/** Get the publish attribute of the camera */
	public synchronized boolean isPublished(String camId){
		try{
			isPublishedStatement.setString(1, camId);
			ResultSet rs = isPublishedStatement.executeQuery();
			if(rs != null && rs.next()){
				return rs.getBoolean(CAMERA_PUBLISH);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	public synchronized String getEncoderHost(String camId){
		try{
			encoderHostStatement.setString(1, camId);
			ResultSet rs = encoderHostStatement.executeQuery();
			if(rs != null && rs.next()){
				return rs.getString(CAMERA_ENCODER);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public synchronized String getEncoderType(String camId){
		try{
			encoderTypeStatement.setString(1, camId);
			ResultSet rs = encoderTypeStatement.executeQuery();
			if(rs != null && rs.next()){
				return rs.getString(CAMERA_ENCODER_TYPE);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public synchronized int getEncoderChannel(String camId){
		try{
			encoderChannelStatement.setString(1, camId);
			ResultSet rs = encoderChannelStatement.executeQuery();
			if(rs != null && rs.next()){
				return rs.getInt(CAMERA_ENCODER_CHANNEL);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return -1;
	}
}