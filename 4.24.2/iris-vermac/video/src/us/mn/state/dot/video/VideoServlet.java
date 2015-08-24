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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The VideoServlet is the base class for all servlets in the video package. 
 * @author Timothy Johnson
 *
 */
public abstract class VideoServlet extends HttpServlet {
	
	protected ImageSize maxImageSize = ImageSize.MEDIUM;
	
	protected ImageSize defaultImageSize = ImageSize.MEDIUM;

	protected static final HashMap<District, URL> districtSessionURLs =
		new HashMap<District, URL>();

	protected static final HashMap<District, URL> districtVideoURLs =
		new HashMap<District, URL>();
	
	/**Flag that controls whether this instance is acting as a proxy 
	 * or a direct video server */
	protected boolean proxy = false;
	
	/** The logger used to log all output for the application */
	protected static Logger logger;

	/** The request parameter name for the SONAR session ID */
	public static final String PARAM_SSID = "ssid";
	
	/** The request parameter name for the IRIS district */
	public static final String PARAM_DISTRICT = "district";

	/** The request parameter name for the frame rate of MJPEG stream */
	public static final String PARAM_RATE = "rate";

	/** The request parameter name for the size of video images */
	public static final String PARAM_SIZE = "size";

	/** The request parameter name for the duration of MJPEG streams */
	public static final String PARAM_DURATION = "duration";

	protected static District defaultDistrict = District.METRO;

	protected EncoderFactory encoderFactory = null;

	protected int maxFrameRate = 3;

	/** Initialize the VideoServlet */
	public void init(ServletConfig config) throws ServletException {
		super.init( config );
		ServletContext ctx = config.getServletContext();
		Properties props =(Properties)ctx.getAttribute("properties");
		proxy = new Boolean(props.getProperty("proxy", "false")).booleanValue();
		if(proxy){
			createDistrictURLs(props);
		}else{
			encoderFactory = EncoderFactory.getInstance(props);
		}
		String max = props.getProperty("max.imagesize", ImageSize.MEDIUM.name());
		for(ImageSize size : ImageSize.values()){
			if(max.equalsIgnoreCase(size.name())){
				maxImageSize = size;
				break;
			}
		}
		if(logger==null) logger = Logger.getLogger(Constants.LOGGER_NAME);
		try{
			maxFrameRate = Integer.parseInt(props.getProperty("max.framerate"));
		}catch(Exception e){
			logger.info("Max frame rate not defined, using default...");
		}
		logger.info("Proxy: " + proxy);
	}

	private void createDistrictURLs(Properties p){
		for(District d : District.values()){
			String s = null;
			try{
				s = p.getProperty(d.name().toLowerCase() + ".video.url");
				districtVideoURLs.put(d, new URL(s));
				s = p.getProperty(d.name().toLowerCase() + ".session.url");
				districtSessionURLs.put(d, new URL(s));
			}catch(MalformedURLException e){
				System.out.println("Malformed URL: " + s);
			}
		}
	}

	/** Get an integer parameter request */
	protected int getIntRequest(HttpServletRequest req, String param) {
		return Integer.parseInt(req.getParameter(param));
	}

	/** Get the requested district. */
	protected District getRequestedDistrict(HttpServletRequest req) {
		String path = req.getPathInfo();
		if(path!=null){
			path = path.toLowerCase();
			for(District d : District.values()){
				if(path.startsWith("/" + d.name().toLowerCase())){
					return d;
				}
			}
		}
		return defaultDistrict;
	}
	
	/** Get the requested camera name. */
	protected String getRequestedCamera(HttpServletRequest req) {
		String name = null;
		String path = req.getPathInfo();
		if(path==null) return null;
		String[] pathParts = path.substring(1).split("/");
		if(pathParts.length!=2) return null;
		name = pathParts[1];
		if(name.indexOf('.')>-1){
			name = name.substring(0,name.indexOf('.'));
		}
		return name;
	}

	/** Get the requested image size.
	 * Valid request values are 1,2,3 or s,m,l 
	 */
	protected ImageSize getRequestedSize(HttpServletRequest req) {
		String value = req.getParameter(PARAM_SIZE);
		if(value == null)
			return defaultImageSize;
		if(value.length()!=1)
			return defaultImageSize;
		value = value.toUpperCase();
		if(Character.isDigit(value.charAt(0))){
			//for backward compatibility, subtract 1 from size
			int i = Integer.parseInt(value) - 1;
			for(ImageSize size : ImageSize.values()){
				if(size.ordinal() == i)
					return size;
			}
		}else{
			for(ImageSize size : ImageSize.values()){
				if(size.name().startsWith(value)){
					return size;
				}
			}
		}
		return defaultImageSize;
	}

	/** Get a 'long' parameter request */
	protected long getLongRequest(HttpServletRequest req, String param) {
		return Long.parseLong(req.getParameter(param));
	}

	/** Configure a client from an HTTP request */
	protected void configureClient(Client c, HttpServletRequest req) {
		c.setDistrict(getRequestedDistrict(req));
		c.setCameraName(getRequestedCamera(req));
		c.setSize(getRequestedSize(req));
		if(maxImageSize.ordinal() < c.getSize().ordinal()){
			c.setSize(maxImageSize);
		}
		if(req.getParameter(PARAM_RATE) != null)
			c.setRate(getIntRequest(req, PARAM_RATE));
		if(maxFrameRate < c.getRate()){
			c.setRate(maxFrameRate);
		}
		if(req.getParameter(PARAM_DURATION) != null)
			c.setDuration(getIntRequest(req, PARAM_DURATION));
		if(req.getParameter(PARAM_SSID) != null)
			c.setSonarSessionId(getLongRequest(req, PARAM_SSID));
		String host = req.getHeader("x-forwarded-for");
		if(host == null)
			host = req.getRemoteHost();
		if(req.getHeader("via") != null)
			host = host + " via proxy";
		c.setHost(host);
	}
    
	protected boolean isValidCamera(Client c){
		if(c==null) return false;
		if(c.getCameraName()==null) return false;
		if(proxy) return true;//proxy server can't validate camera id
		Encoder encoder = encoderFactory.getEncoder(c.getCameraName());
		return encoder != null;
	}
	
	protected final boolean isPublished(String cameraId){
		if(proxy){
			return true;
		}
		return encoderFactory.isPublished(cameraId);
	}

	private boolean isDirectoryRequest(HttpServletRequest req){
		boolean nullPath  = req.getPathInfo() == null;
		boolean nullQuery = req.getQueryString() == null;
		boolean isDirectoryReq = nullPath && nullQuery;
		return isDirectoryReq;
	}
	
	private void sendDistrictList(HttpServletResponse response)
			throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text");
		OutputStream os = response.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os);
		for(District d : District.values()){
			osw.write(d.name().toLowerCase() + "\n");
			osw.flush();
		}
	}

	/**
	 * Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	protected final void doGet(HttpServletRequest request,
		HttpServletResponse response)
	{
		Client c = new Client();
		try {
			if(isDirectoryRequest(request)){
				sendDistrictList(response);
				return;
			}
			configureClient(c, request);
			processRequest(response, c);
		}
		catch(Throwable th) {
			logger.warning(c.getCameraName() + ": " + th.getMessage());
			th.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try{
				response.flushBuffer();
			}catch(Exception e){
			}
		}
		finally {
			try {
				response.flushBuffer();
				response.getOutputStream().close();
			}
			catch(Exception e2) {
			}
		}
	}

	public abstract void processRequest(HttpServletResponse response,
			Client c) throws Exception;

	/** Check to see if the client is authenticated through SONAR */
	protected final boolean isAuthenticated(Client c){
		if(!proxy) return true;
		long ssid = c.getSonarSessionId();
		List<Long> validIds = getValidSessionIds(c.getDistrict());
		for(long validId : validIds){
			if(ssid == validId) return true;
		}
		return false;
	}
	
	private List<Long> getValidSessionIds(District d){
		List<Long> ids = new LinkedList<Long>();
		try{
			HttpURLConnection conn = ImageFactory.createConnection(districtSessionURLs.get(d));
			conn.setConnectTimeout(VideoThread.TIMEOUT_DIRECT);
			conn.setReadTimeout(VideoThread.TIMEOUT_DIRECT);
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader reader = new BufferedReader(in);
			String l = reader.readLine();
			while(l != null){
				try{
					ids.add(Long.parseLong(l));
				}catch(NumberFormatException nfe){
					//invalid ssid... ignore it!
				}
				l = reader.readLine();
			}
		}catch(Exception e){
			logger.warning("VideoServlet.isValidSSID: " + e.getMessage());
		}
		return ids;
	}
}
