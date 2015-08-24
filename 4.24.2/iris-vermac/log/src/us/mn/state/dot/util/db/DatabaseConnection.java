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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * DatabaseConnection is a convenience class for making a connection to
 * a database.  It handles all of the queries and sql exceptions as
 * well as re-establishing the connection if it is lost.
 * 
 * @author Timothy Johnson
 *
 */
public class DatabaseConnection {

	protected static final String ASCENDING = "asc";
	
	protected static final String DESCENDING = "desc";
	
	public static final SimpleDateFormat DATE_FORMAT =
		new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
	
	/** Username for authentication to the oracle db server */
	private String user = null;

	/** The name of the database to connect to */
	private String dbName = null;

	/** Password for authentication to the oracle db server */
	private String password = null;

	/** Database URL */
	private String url = null;

	/** The connection object used for executing queries */
	protected Connection connection = null;
	
	protected Statement statement = null;
	
	/** The fully qualified name of the database driver for postgres */
	private static final String DRIVER_POSTGRES = "org.postgresql.Driver";

	/** The fully qualified name of the database driver for oracle */
	private static final String DRIVER_ORACLE = "oracle.jdbc.driver.OracleDriver";

	private final String driver;

	private static final String PROTOCOL_POSTGRES = "jdbc:postgresql://";
	private static final String PROTOCOL_ORACLE = "jdbc:oracle:thin:@";

	private final String protocol;
	
	public static final int TYPE_POSTGRES = 1;
	public static final int TYPE_ORACLE = 2;
	
	/** Constructor for the DatabaseConnection class.
	 * 
	 * @param dbType The database vendor.
	 * @param user The username for connections.
	 * @param pwd The user password.
	 * @param host Host name or ip.
	 * @param port Port on which to connect.
	 * @param dbName The name of the database.
	 */
	public DatabaseConnection(
			int dbType, String user, String pwd, String host, int port, String dbName) {
		this.user = user;
		this.dbName = dbName;
		this.password = pwd;
		String port_name_separator = "/";
		switch(dbType){
			case(TYPE_ORACLE):
				driver = DRIVER_ORACLE;
				protocol = PROTOCOL_ORACLE;
				port_name_separator = ":";
				break;
			case(TYPE_POSTGRES):
				driver = DRIVER_POSTGRES;
				protocol = PROTOCOL_POSTGRES;
				break;
			default:
				driver = DRIVER_POSTGRES;
				protocol = PROTOCOL_POSTGRES;
				break;
		}
		url = protocol + host + ":" + port + port_name_separator + dbName;
		try {
			openConnection();
			statement = connection.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	private void openConnection(){
		try {
			Class.forName( driver );
			System.out.println( "Openning connection to " + dbName + " database." );
			System.out.println( "\tURL: " + url + " USER: " + user + " PWD: " + password );
			connection = DriverManager.getConnection( url, user, password );
			System.out.println( "Opened connection to " + dbName + " database." );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	public ResultSet query( String sql ){
		try {
			if ( connection.isClosed() ) {
				openConnection();
			}
			return statement.executeQuery( sql );
		} catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}

	/** Create a Calendar object from a string representation */
	public static Calendar getCalendar(String s) {
		try{
			Calendar c = Calendar.getInstance();
			s = s.replace(" ", "_");
			c.setTime(DATE_FORMAT.parse(s));
			return c;
		}catch(ParseException pe){
			pe.printStackTrace();
			return null;
		}
	}
	
	public static String calendar2String(Calendar c){
		String s = DATE_FORMAT.format(c.getTime());
		return s.replace("_", " ");
	}

	protected String[] getColumn(ResultSet rs, String columnName){
		List<String> l = new ArrayList<String>();
		try{
			while(rs.next()){
				l.add(rs.getString(columnName));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return l.toArray(new String[0]);
	}
}