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

package us.mn.state.dot.video;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * The MultiRequestDataSource gets it's data via the HTTP protocol
 * and a series of requests for single images over a single HTTP connection.
 *
 * @author Timothy Johnson
 */
public class MultiRequestDataSource extends AbstractDataSource {

	/** Constructor for the MultiRequestDataSource. */
	public MultiRequestDataSource(Client c, URL url, String user, String pwd) {
		super(c, null, null, url, user, pwd);
	}

	/** Start the stream. */
	public void run() {
		HttpURLConnection conn = null;
		InputStream in = null;
		if(url != null){
			try{
				logger.fine("Starting: " + this);
				while(!done && this.isAlive()){
					conn = ImageFactory.createConnection(url, user, password);
					int response = conn.getResponseCode();
					if(response != 200){
						logger.info("HTTP response: " + response );
						break;
					}
					in = conn.getInputStream();
					int length = Integer.parseInt(
							conn.getHeaderField("Content-Length"));
					byte[] img = AbstractEncoder.readImage(in, length);
					if(img != null && img.length > 0){
						notifySinks(img);
					}else{
						//FIXME: Continue trying to get images even if null or empty.
						//Pehaps a counter can keep track of contiguous failures and
						//then break.

						//break;
					}
				}
			}catch(Exception e){
				logger.info(e.getMessage());
			}finally{
				logger.fine("Stopping: " + this);
				try{
					conn.disconnect();
				}catch(Exception e2){
				}
				removeSinks();
			}
		}else{
			logger.fine("No encoder defined for this source.");
		}
	}
}
