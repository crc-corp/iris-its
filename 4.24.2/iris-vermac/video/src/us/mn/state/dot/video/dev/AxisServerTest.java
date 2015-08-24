/*
* VideoServer
* Copyright (C) 2003-2007  Minnesota Department of Transportation
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
import java.io.FileOutputStream;

import us.mn.state.dot.video.AxisServer;
import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.VideoStream;

public class AxisServerTest {

	File dir = new File("/tmp");
	AxisServer server;
	
	public static void main(String[] args){
		new AxisServerTest(args[0]);
	}

	private void writeImage(){
		try{
			File f = new File(dir.getAbsolutePath() + "/image.jpg");
			System.out.println("Writing " + f.getAbsolutePath() );
			FileOutputStream out = new FileOutputStream(f);
			byte[] image = null;
			Client client = new Client();
			client.setCameraId("C001");
			image = server.getImage(client);
			System.out.println("Image size: " + image.length);
			out.write(image);
			out.flush();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Done.");
	}
	
	private void writeStreamImages(){
		String baseName = dir.getAbsolutePath() + "/image_";
		try{
			byte[] image = null;
			Client c = new Client();
			c.setCameraId("C001");
			VideoStream stream = server.getStream(c);
			for(int i=0; i<5; i++){
				File f = new File(baseName + i + ".jpg");
				System.out.println("Writing " + f.getAbsolutePath());
				FileOutputStream out = new FileOutputStream(f);
				image = stream.getImage();
				System.out.println("Image size: " + image.length);
				out.write(image);
				out.flush();
				out.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Done.");
	}

	public AxisServerTest(String host){
		try{
			server = AxisServer.getServer(host);
			server.setCamera("C001", 1);
			writeStreamImages();
			writeImage();
		}catch(Exception e){}
	}

}
