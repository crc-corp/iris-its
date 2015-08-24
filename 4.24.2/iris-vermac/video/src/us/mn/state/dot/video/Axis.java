/*
* VideoServer
* Copyright (C) 2003-2007  Minnesota Department of Transportation
* Copyright (C) 2014-2015  AHMCT, University of California
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
import java.util.Properties;

/**
 * The AxisEncoder class encapsulates information about an axis video
 * capture device
 *
 * @author    Timothy Johnson
 * @author    Travis Swanston
 * @created   July 2, 2003
 */

public final class Axis extends AbstractEncoder {

	/** The base URI for a request for an image */
	private final String BASE_IMAGE_URI = "/axis-cgi/jpg/image.cgi";

	/** The base URI for a request for a stream */
	private final String BASE_STREAM_URI = "/axis-cgi/mjpg/video.cgi";

	/** The camera request parameter */
	private static final String PARAM_CAMERA = "camera";

	/** The clock request parameter */
	private static final String PARAM_CLOCK = "clock";

	/** The compression request parameter */
	private static final String PARAM_COMPRESSION = "compression";

	/** The date request parameter */
	private static final String PARAM_DATE = "date";

	/** The FPS request parameter */
	private static final String PARAM_FPS = "fps";

	/** The resolution request parameter */
	private static final String PARAM_RESOLUTION = "resolution";

	/** The square-pixel request parameter */
	private static final String PARAM_SQUAREPIXEL = "squarepixel";

	/** The text request parameter */
	private static final String PARAM_TEXT = "text";

	/** The showlength request parameter */
	private static final String PARAM_SHOWLENGTH = "showlength";

	/** Optional value for the clock request parameter */
	private String opt_clock = null;

	/** Optional value for the compression request parameter */
	private String opt_compression = null;

	/** Optional value for the date request parameter */
	private String opt_date = null;

	/** Optional value for the FPS request parameter */
	private String opt_fps = null;

	/** Optional value for the resolution request parameter */
	private String opt_resolution = null;

	/** Optional value for the square-pixel request parameter */
	private String opt_squarepixel = null;

	/** Optional value for the text request parameter */
	private String opt_text = null;

	/** Optional value for the showlength request parameter */
	private String opt_showlength = null;

	/** Constructor for the axis encoder object */
	public Axis(String host, String user, String pass, Properties props) {
		super(host, user, pass);
		loadProps(props);
	}

	private void loadProps(Properties p) {
		opt_clock = p.getProperty("axis.opt.clock");
		opt_compression = p.getProperty("axis.opt.compression");
		opt_date = p.getProperty("axis.opt.date");
		opt_fps = p.getProperty("axis.opt.fps");
		opt_resolution = p.getProperty("axis.opt.resolution");
		opt_squarepixel = p.getProperty("axis.opt.squarepixel");
		opt_text = p.getProperty("axis.opt.text");
		opt_showlength = p.getProperty("axis.opt.showlength");
	}

	/**
	 * Get a URL for requesting an MJPEG stream from an Axis encoder.
	 * @param c The client object containing request parameters.
	 * @return The URL
	 */
	public URL getStreamURL(Client c) {
		return getURL(c, true);
	}

	/**
	 * Get a URL for requesting a JPEG image from an Axis encoder.
	 * @param c The client object containing request parameters.
	 * @return The URL
	 */
	public URL getImageURL(Client c) {
		return getURL(c, false);
	}

	/**
	 * Get a URL for requesting a JPEG image or an MJPEG stream from an
	 * Axis encoder.
	 * @param c The client object containing request parameters.
	 * @param stream True for a stream request, false for an image request.
	 * @return The URL
	 */
	public URL getURL(Client c, boolean stream) {
		int channel = getChannel(c.getCameraName());
		if (channel == NO_CAMERA_CONNECTED)
			return null;

		String url =
			"http://" + host +
			":" + getPort() +
			(stream ? BASE_STREAM_URI : BASE_IMAGE_URI) +
			keyVal("?", PARAM_CAMERA,
				String.valueOf(getChannel(c.getCameraName()))) +
			keyVal("&", PARAM_CLOCK, opt_clock) +
			keyVal("&", PARAM_DATE, opt_date) +
			keyVal("&", PARAM_SQUAREPIXEL, opt_squarepixel) +
			keyVal("&", PARAM_TEXT, opt_text) +
			keyVal("&", PARAM_SHOWLENGTH, opt_showlength);

		// only specify FPS for stream requests
		if (stream)
			url += keyVal("&", PARAM_FPS, opt_fps);

		// use compression 30 for images if opt_compression null
		if ((!stream) && (opt_compression == null))
			url += keyVal("&", PARAM_COMPRESSION, "30");
		else
			url += keyVal("&", PARAM_COMPRESSION, opt_compression);

		// use resolution from ImageSize if opt_resolution null
		if (opt_resolution == null)
			url += "&" + createSizeParam(c.getSize());
		else
			url += keyVal("&", PARAM_RESOLUTION, opt_resolution);

		try {
			return new URL(url);
		}catch(Exception e){
			return null;
		}
	}

	/**
	 * Create a key-value style string.
	 *
	 * @param pre The prefix string; null implies no prefix.
	 * @param key The key string; if null, "" is returned.
	 * @param val The value string; if null, "" is returned.
	 * @return The key-value string (e.g. "?load=true"),
	 *         or "" if key or val is null.
	 */
	private String keyVal(String pre, String key, String val) {
		if ((key == null) || (val == null))
			return "";
		String kv = ((pre != null) ? pre : "") +
			key + "=" + val;
		return kv;
	}

	public DataSource getDataSource(Client c) throws VideoException{
		URL url = getStreamURL(c);
		if(url == null) return null;
		try{
			return new HttpDataSource(c, url, username, password);
		}catch(Exception e){
			throw new VideoException(e.getMessage());
		}
	}
}
