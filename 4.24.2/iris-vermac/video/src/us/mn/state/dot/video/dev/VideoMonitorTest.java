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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.JFrame;

import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.DataSourceFactory;
import us.mn.state.dot.video.HttpDataSource;
import us.mn.state.dot.video.client.VideoMonitor;

public class VideoMonitorTest extends JFrame {

	public VideoMonitorTest(String streamUri){
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we){
				System.exit(0);
			}
		});
		setVisible(true);
		setSize(400, 400);
		VideoMonitor mon = new VideoMonitor();
		mon.setProgressVisible(true);
		mon.setStatusVisible(false);
		mon.setLabelVisible(false);
		this.getContentPane().add(mon);
		Client c = new Client();
		c.setCameraId("C814");
		c.setRate(30);
		try{
//			URL url = DataSourceFactory.createURL(c, streamUri);
			URL url = new URL("http://10.69.9.18/axis-cgi/mjpg/video.cgi?camera=1");
			mon.setDataSource(
					new HttpDataSource(c, null, null, url),
					800);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new VideoMonitorTest(args[0]);
	}

}
