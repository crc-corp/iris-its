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

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import us.mn.state.dot.video.EncoderFactory;

/**
 * The DataSourceFactory creates and maintains DataSources.
 * It is responsible for making sure that only one DataSource
 * object is created for each stream regardless of the number 
 * of clients requesting the stream.
 *
 * @author Timothy Johnson
 */
public class DataSourceFactory {

	private ThreadMonitor monitor = null;
	
	/** Hash of video streams that are active. */
	static final private HashMap<String, AbstractDataSource> sources =
		new HashMap<String, AbstractDataSource>();

	private final Logger logger;
	
	/**Flag that controls whether this instance is acting as a proxy 
	 * or a direct video server */
	private boolean proxy = false;

	protected static final HashMap<District, URL> districtVideoURLs =
		new HashMap<District, URL>();

	protected EncoderFactory encoderFactory;
	
	private static DataSourceFactory factory = null;

	public static synchronized DataSourceFactory create(Properties p, ThreadMonitor m){
		if(factory == null){
			factory = new DataSourceFactory(p, m);
		}
		return factory;
	}
	
	/** Constructor for the DataSourceFactory. */
	private DataSourceFactory(Properties p, ThreadMonitor m) {
		logger = Logger.getLogger(Constants.LOGGER_NAME);
		monitor = m;
		proxy = new Boolean(p.getProperty("proxy", "false")).booleanValue();
		if(proxy) {
			for(District d : District.values()){
				try{
					districtVideoURLs.put(d, new URL(p.getProperty(d.name().toLowerCase() + ".video.url")));
				}catch(Exception e){
					//do nothing, it's a misconfigured url.
				}
			}
		}else{
			encoderFactory = EncoderFactory.getInstance(p);
		}
	}

	private static String createSourceKey(Client c){
		return c.getCameraName() + ":" + c.getSize();
	}
	
	private static void cleanupSources(){
		Iterator<Entry<String, AbstractDataSource>> it = sources.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, AbstractDataSource> entry = it.next();
			if(!entry.getValue().isAlive()){
				it.remove();
			}
		}
	}

	private DataSource createDataSource(Client c) throws VideoException {
		try{
			if(proxy){
				URL url = new URL(districtVideoURLs.get(c.getDistrict()), createRelativeURL(c));
				return new HttpDataSource(c, logger, monitor, url, null, null);
			}else{
				Encoder encoder = encoderFactory.getEncoder(c.getCameraName());
				if(encoder == null){
					throw new VideoException("No encoder for " + c.getCameraName());
				}
				return encoder.getDataSource(c);
			}
		}catch(Exception e){
			throw new VideoException(e.getMessage());
		}
	}

	private static String createRelativeURL(Client c) {
		return RequestType.STREAM.name().toLowerCase() +
			"/" + c.getDistrict().name().toLowerCase() +
			"/" + c.getCameraName() +
			"?size=" + c.getSize().name().toLowerCase().charAt(0) +
			"&rate=" + c.getRate() +
			"&duration=" + c.getDuration() +
			"&ssid=" + c.getSonarSessionId();
	}

	public synchronized DataSource getDataSource(Client c)
			throws VideoException {
		if(c.getCameraName()==null){
			return null;
		}
		cleanupSources();
		String key = createSourceKey(c);
		logger.info("There are currently " + sources.size() + " datasources.");
		AbstractDataSource src = sources.get(key);
		if(src != null && src.isAlive()){
			return src;
		}
		src = (AbstractDataSource)createDataSource(c);
		sources.put(key, src);
		return src;
	}
}
