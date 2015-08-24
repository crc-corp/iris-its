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
package us.mn.state.dot.video.dev;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

import us.mn.state.dot.video.AxisServer;
import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.ConnectionFactory;
import us.mn.state.dot.video.MJPEGReader;
import us.mn.state.dot.video.VideoStream;

/**
 * @author john3tim
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class StreamTest {

	public StreamTest(String location){
//		testAxisStream();
		try{
			URL url = new URL(location);
			testRepeaterStream(url);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	protected void testAxisStream(String host){
		try{
			AxisServer server = AxisServer.getServer(host);
			server.setCamera("C001",1);
			Client c = new Client();
			c.setCameraId("C001");
			c.setSize(Client.MEDIUM);
			VideoStream stream = server.getStream(c);
			for(int i=0; i<5; i++){
				byte[] image = stream.getImage();
				System.out.println(image.length);
				File f = new File("/tmp/image_" + i + ".jpg");
				FileOutputStream out = new FileOutputStream(f);
				out.write(image);
				out.flush();
				out.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	protected void testRepeaterStream(URL url){
		System.out.println("Testing Repeater Stream...");
		try{
			URLConnection con = ConnectionFactory.createConnection(url);
			System.out.println("  creating mjpegstream...");
			MJPEGReader stream = new MJPEGReader(con.getInputStream());
			System.out.println("  mjpegstream created.");
			for(int i=0; i<5; i++){
				byte[] image = stream.getImage();
				System.out.println(image.length);
				File f = new File("/tmp/image_" + i + ".jpg");
				FileOutputStream out = new FileOutputStream(f);
				out.write(image);
				out.flush();
				out.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new StreamTest(args[0]);
	}
}
