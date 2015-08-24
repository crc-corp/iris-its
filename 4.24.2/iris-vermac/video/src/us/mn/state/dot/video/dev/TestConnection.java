/*
* Video
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
* Foundation, Inc., 59 temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package us.mn.state.dot.video.dev;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import us.mn.state.dot.util.db.TmsConnection;
import us.mn.state.dot.video.AxisServer;
import us.mn.state.dot.video.server.ServerFactory;

public class TestConnection extends HttpServlet {

	Properties props = new Properties();
	
	public TestConnection(){
/*		try{
			init(null);
		}catch(Exception e){
			e.printStackTrace();
		}*/
	}

	public void init( ServletConfig config ) throws ServletException {
		File f = new File("/etc/iris/video.properties");
		try{
			props.load(new FileInputStream(f));
		}catch(Exception e){
			e.printStackTrace();
		}
		directConnection();
		//testDatabaseConnection();
	}
	
	protected void testDatabaseConnection(){
		TmsConnection c = new TmsConnection(props);
		ServerFactory f = new ServerFactory(props);
		AxisServer.printServers();
	}
	
	protected void directConnection() {
		try {
			Class.forName("org.postgresql.Driver");
			System.out.println("Connecting to tms db on " +
					props.getProperty("tms.db.host"));
			Connection connection =
				DriverManager.getConnection("jdbc:postgresql://" +
						props.getProperty("tms.db.host") + ":" + 
						props.getProperty("tms.db.port") + "/tms",
						props.getProperty("tms.db.user"),
						props.getProperty("tms.db.pwd"));
			System.out.println("Connected.");
			Statement statement = connection.createStatement();
			String q = "select distinct encoder from camera";
			ResultSet rs = statement.executeQuery(q);
			while(rs.next()) {
				try{
					System.out.println(rs.getString("encoder"));
				}catch(Exception e){
					e.printStackTrace();
					continue;
				}
			}
			System.out.println("Closing connection...");
			statement.close();
			connection.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new TestConnection();

	}

}
