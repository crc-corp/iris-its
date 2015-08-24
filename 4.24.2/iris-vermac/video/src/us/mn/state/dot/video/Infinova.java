/*
* VideoServer
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
* Foundation, Inc., 59 temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package us.mn.state.dot.video;

import java.net.URL;


/**
 * The InfinovaEncoder class encapsulates information about an Infinova video
 * capture device
 *
 * @author    Timothy Johnson
 */

public final class Infinova extends AbstractEncoder {

	/** The base URI for a request for an image */
	private final String BASE_IMAGE_URI = "/jpgimage/1/image.jpg";
	
	private final String BASE_STREAM_URI = "/jpgimage/1/image.jpg";
	
	/** Constructor for the Infinova encoder object */
	public Infinova(String host, String user, String pass) {
		super(host, user, pass);
	}
	
	/**
	 * Get a URL for connecting to the MJPEG stream of an Infinova Server.
	 * @param c The client object containing request parameters.
	 * @return
	 */
	public URL getStreamURL(Client c){
		int channel = getChannel(c.getCameraName());
		if(channel == NO_CAMERA_CONNECTED) return null;
		try{
			return new URL( "http://" + host + ":" +
					getPort() + BASE_STREAM_URI
//					createCameraParam(c) + "&" +
//					createSizeParam(c.getSize()) + "&" +
//					createCompressionParam(c.getCompression())
					);
		}catch(Exception e){
		}
		return null;
	}

	public URL getImageURL(Client c) {
		int channel = getChannel(c.getCameraName());
		if(channel == NO_CAMERA_CONNECTED) return null;
		try{
			String url = 
				"http://" + host + ":" +
				getPort() + BASE_IMAGE_URI ;
				//createCameraParam(c) + "&" +
				//createSizeParam(c.getSize()) + "&" +
				//createCompressionParam(c.getCompression());
/*			if(size==SMALL){
				url = url +
					"&" + PARAM_CLOCK + "=" + VALUE_OFF +
					"&" + PARAM_DATE + "=" + VALUE_OFF;
			}*/
			return new URL(url);
		}catch(Exception e){
			return null;
		}
	}

	public DataSource getDataSource(Client c) throws VideoException{
		URL url = getStreamURL(c);
		if(url == null) return null;
		try{
			return new MultiRequestDataSource(c, url, username, password);
		}catch(Exception e){
			throw new VideoException(e.getMessage());
		}
	}
}
