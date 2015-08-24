/*
 * Project: Video
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.video;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author john3tim
 * @author Travis Swanston
 *
 * The EncoderFactory is responsible for creating Encoder objects and
 * making sure that they are in sync with the database.
 */
public class EncoderFactory {

	protected static final String INFINOVA = "Infinova";

	protected static final String AXIS     = "Axis";
	
	protected DatabaseConnection tms = null;

	/** The expiration time of database information, in milliseconds */
	protected long dbExpire = 10 * 1000;

	/** The time, in milliseconds, of the last database update */
	protected long dbTime = 0;
	
	protected Properties properties = null;

	protected Logger logger = null;
	
	/** Hashtable of encoders indexed by camera name */
	protected final Hashtable<String, Encoder> encoders =
		new Hashtable<String, Encoder>();

	private static EncoderFactory factory = null;
	
	public synchronized static EncoderFactory getInstance(Properties p){
		if( factory != null ) return factory;
		factory = new EncoderFactory(p);
		return factory;
	}
	
	private EncoderFactory(Properties props){
		this.logger = Logger.getLogger(Constants.LOGGER_NAME);
		this.properties = props;
		tms = DatabaseConnection.create(props);
		try{
			dbExpire = Long.parseLong(props.getProperty("db.expire"));
		}catch(Exception e){
			//do nothing, use the default database expiration
		}
	}

	private boolean dbExpired(){
		logger.info("Checking if database is expired.");
		long now = Calendar.getInstance().getTimeInMillis();
		return (now - dbTime) > dbExpire;
	}

	private synchronized void updateEncoders(){
		logger.info("Updating encoder information.");
		for(String key : encoders.keySet()){
			logger.info("Updating encoder for " + key);
			createEncoder(key);
		}
		dbTime = Calendar.getInstance().getTimeInMillis();
	}
	
	public Encoder getEncoder(String cameraId){
		if(dbExpired()) updateEncoders();
		Encoder e = encoders.get(cameraId);
		if(e != null) return e;
		try{
			return createEncoder(cameraId);
		}catch(Throwable th){
			if(logger != null){
				logger.warning("Error creating encoder for camera " +
						cameraId + ": " + th.getMessage());
			}
			tms = DatabaseConnection.create(properties);
			return null;
		}
	}
	
	protected Encoder createEncoder(String name){
		if(name == null) return null;
		String mfr = tms.getEncoderType(name);
		String host = tms.getEncoderHost(name);
		if(mfr == null || host == null) return null;
		logger.info("Creating new encoder for camera " + name);
		Encoder e = null;
		if(mfr.indexOf(INFINOVA) > -1){
			String u = properties.getProperty("video.encoder.infinova.user");
			String p = properties.getProperty("video.encoder.infinova.pwd");
			e = new Infinova(host,u,p);
		}else{
			String u = properties.getProperty("video.encoder.axis.user");
			String p = properties.getProperty("video.encoder.axis.pwd");
			e = new Axis(host,u,p,properties);
		}
		int ch = tms.getEncoderChannel(name);
		e.setCamera(name, ch);
		encoders.put(name, e);
		logger.info(name + " " + e);
		logger.info(encoders.size() + " encoders.");
		return e;
	}
	
	public boolean isPublished(String cameraId){
		return tms.isPublished(cameraId);
	}
}
