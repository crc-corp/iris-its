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
package us.mn.state.dot.video;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

/**
 * VideoServer is the main thread for still video server.
 *
 * @author    dinh1san
 * @author    Tim Johnson
 * @created   December 27, 2001
 */
public final class ImageServer extends VideoServlet{

	private static ImageCache imageCache = null;
	
	/** Constructor for the ImageServer */
    public void init(ServletConfig config) throws ServletException {
		super.init( config );
		try{
			ServletContext ctx = config.getServletContext();
			Properties p = (Properties)ctx.getAttribute("properties");
			imageCache = ImageCache.create(p);
		}catch(Exception e){
			logger.severe(e.getMessage() + " --see error log for details.");
			e.printStackTrace();
		}
    }

	public void processRequest(HttpServletResponse response, Client c)
		throws VideoException
	{
		URL imageURL = null;
		String user = null;
		String pass = null;
		if(proxy){
			imageURL = getDistrictImageURL(c);
		}else{
			Encoder e = encoderFactory.getEncoder(c.getCameraName());
			if(e != null){
				imageURL = e.getImageURL(c);
				user = e.getUsername();
				pass = e.getPassword();
			}
		}
		if(imageURL == null){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if(!isPublished(c.getCameraName())){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		byte[] image = null;
		try{
			image = imageCache.getImage(c, imageURL, user, pass);
			if(image == null){
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("image/jpeg\r\n");
			response.setContentLength(image.length);
			Calendar cal = Calendar.getInstance();
			Constants.LAST_MODIFIED_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
			response.setHeader("Last-Modified", Constants.LAST_MODIFIED_FORMAT.format(cal.getTime()));
			response.getOutputStream().write(image);
		}catch(HTTPException httpEx){
			response.setStatus(httpEx.getStatusCode());
		}catch(Throwable t){
			t.printStackTrace();
			logger.warning("Exception serving image " + c.getCameraName() +
					" to client " + c.getHost());
		}
	}

	/** Is this district server responsible for handling
	 * the request from the client?
	 */
	private boolean isDistrictServer(HttpServletRequest request, Client c){
		String localIp = request.getLocalAddr();
		URL districtURL = districtVideoURLs.get(c.getDistrict());
		if(districtURL == null){
			return false;
		}
		String districtIp = districtURL.getHost();
		logger.warning("Local: " + localIp);
		logger.warning("District: " + districtIp);
		return (districtIp.equals(localIp));
	}
	
	private URL getImageURL(Client c) throws VideoException {
		if(proxy){
			return getDistrictImageURL(c);
		}else{
			Encoder encoder = encoderFactory.getEncoder(c.getCameraName());
			if(encoder != null){
				return encoder.getImageURL(c);
			}
		}
		return null;
	}
	
	/** Get the URL used to retrieve a new image from a district server */
	private URL getDistrictImageURL(Client c) throws VideoException {
		String relativeURL = "";
		try{
			relativeURL = "/video/" +
				RequestType.IMAGE.name().toLowerCase() +
				"/" + c.getDistrict().name().toLowerCase() +
				"/" + c.getCameraName() + ".jpg" +
				"?size=" + c.getSize().name().toLowerCase().charAt(0);
			return new URL(districtVideoURLs.get(c.getDistrict()), relativeURL);
		}catch(MalformedURLException mue){
			throw new VideoException(mue.getMessage());
		}
	}
}
