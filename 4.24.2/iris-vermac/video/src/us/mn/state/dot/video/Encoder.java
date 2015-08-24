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
package us.mn.state.dot.video;

import java.net.URL;

/**
 * The <code>Encoder</code> interface defines the attributes of a digital video encoder.
 * @author Timothy Johnson
 *
 */
public interface Encoder {

	/**
	 * Get the IP port for video requests.
	 * @return port The port on which to connect.
	 */
	public int getPort();

	/**
	 * Set the IP port for video requests.
	 * @param port
	 */
	public void setPort(int port);

	/** Get the number of video channels. */
	public int getChannels();
	
	/**
	 * Set the number of available video channels.
	 * @param channels
	 */
	public void setChannels(int channels);

	/**
	 * Get a VideoStream for the Client, c
	 * @param c
	 * @return A VideoStream
	 * @throws VideoException
	 */
	public DataSource getDataSource(Client c) throws VideoException;

	public String getPassword();

	public String getUsername();

	public void setCamera(String id, int channel);

	public URL getStreamURL(Client c);

	public URL getImageURL(Client c);
}
