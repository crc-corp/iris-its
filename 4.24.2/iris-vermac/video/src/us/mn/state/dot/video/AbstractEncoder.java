/*
 * Project: Video
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
package us.mn.state.dot.video;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Hashtable;

/**
 * 
 * @author Timothy Johnson
 *
 */
public abstract class AbstractEncoder implements Encoder {

	/** The size request parameter */
	private static final String PARAM_SIZE = "resolution";
	
	/** The username used to connect to this server.  Only required when
	 * the encoder does not allow anonymous connections.
	 */
	protected String username = null;
	
	/** The password used to connect to this server.  Only required when
	 * the encoder does not allow anonymous connections.
	 */
	protected String password = null;

	/** Constant string for no camera connected */
	public static final int NO_CAMERA_CONNECTED = -1;

	/** The Encoder host name (or IP). */
	protected final String host;
	
	/** The tcp port of this encoder. */
	protected int port = 80;

	/** The number of video channels. */
	protected int channels = 1;
	
	/** Get the tcp port of the encoder */
	public final int getPort() { return  port; }

	/** Set the tcp port of the encoder */
	public final void setPort(int p){ this.port = p; }

	/** Get the number of video channels. */
	public final int getChannels(){ return channels; }

	public abstract URL getImageURL(Client c);
	
	/** The ids of the cameras that are connected. */
	private Hashtable<String, Integer> ids =
		new Hashtable<String, Integer>();

	/** Set the number of available video channels. */
	public final void setChannels(int channels){ this.channels = channels; }

	public final String getHost(){ return host; }
	
	protected String getIp() throws UnknownHostException{
		return InetAddress.getByName(host).getHostAddress();
	}

	public AbstractEncoder(String host_port, String user, String pass){
		if(host_port.indexOf(":")>-1){
			host = host_port.substring(0,host_port.indexOf(":"));
			try{
				port = Integer.parseInt(host_port.substring(host_port.indexOf(":")+1));
			}catch(NumberFormatException ex){
				//host port parsing error... use default http port
			}
		}else{
			this.host = host_port;
		}
		this.username = user;
		this.password = pass;
	}
	
	public String toString(){
		String ip = "";
		try{
			ip = getIp();
		}catch(Exception e){}
		return this.getClass().getSimpleName() + " " + ip + " ";
	}

	/** Get the id of the camera connected to the given channel */
	public String getCamera(int channel) {
		for(String id : ids.keySet()){
			if(ids.get(id).intValue() == channel) return id;
		}
		return null;
	}

	/** Get the channel number for the given camera id. */
	public int getChannel(String id){
		if(ids.get(id) != null){
			return ids.get(id);
		}
		return NO_CAMERA_CONNECTED;
	}

	/** Set the camera id for the given channel */
	public final void setCamera(String id, int channel) {
		ids.put(id, new Integer(channel));
	}

	public final String getUsername() {
		return username;
	}

	public final String getPassword() {
		return password;
	}

	/** Get the next image in the mjpeg stream 
	 *  in which the Content-Length header is present
	 * @return
	 */
	public static byte[] readImage(InputStream in, int imageSize)
			throws IOException{
		byte[] image = new byte[imageSize];
		int bytesRead = 0;
		int currentRead = 0;
		while(bytesRead < imageSize){
			currentRead = in.read(image, bytesRead,
					imageSize - bytesRead);
			if(currentRead==-1){
				break;
			}else{
				bytesRead = bytesRead + currentRead;
			}
		}
		return image;
	}

	public String createSizeParam(ImageSize size){
		Dimension d = size.getDimension();
		return PARAM_SIZE + "=" + d.width + "x" + d.height;
	}
}
