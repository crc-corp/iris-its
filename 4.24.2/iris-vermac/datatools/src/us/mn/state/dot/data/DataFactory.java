/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
 */
package us.mn.state.dot.data;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;

/**
 * DataFactory is an abstract class for retrieving volume and occupancy
 * data from a data source.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
abstract public class DataFactory implements Constants, DateChecker {

	/** Create a data factory from a set of properties */
	static public DataFactory create(Properties props) throws IOException,
		ParserConfigurationException
	{
		if(props == null)
			return null;

		// get server url
		String server = getProperty(props, 
			"datatools.trafdat.url", "");

		// get multiple config urls
		SystemConfig[] cfgs = createSystemConfigs(props, 
			"datatools.config.url");
		if(cfgs == null)
			return null;

		// create data factory
		return new HttpDataFactory(server, cfgs);
	}

	/** Create an array of SystemConfig objects. 
	 *  @return Null on error or SystemConfig[], which may contain 
	 *          elements that are null. */
	static protected SystemConfig[] createSystemConfigs(
		Properties props, String propname) 
	{
		String config_multi = getProperty(props, propname, "");
		String[] config = config_multi.split(",");
		if(config == null)
			return null;

		// may contain nulls
		SystemConfig[] cfgs = new SystemConfig[config.length];	
		for(int i = 0; i < config.length; i++)
			cfgs[i] = createSystemConfig(config[i]);
		return cfgs;
	}

	/** return a SystemConfig using the specified URL or null on error */
	static protected SystemConfig createSystemConfig(String url_string) {
		if(url_string == null || url_string.length() <= 0)
			return null;
		SystemConfig ret = null;
		try {
			URL url = new URL(url_string);
			ret = SystemConfig.create(url);
		}catch(MalformedURLException ex) {
			System.err.println(
				"Warning: bad property file URL: " + ex);
		}catch(Exception ex) {
			System.err.println(
				"Warning: unknown exception: " + ex);
		}
		return ret;
	}

	/** System configurations, may contain nulls */
	protected final SystemConfig[] configs;

	/** Get the system configs, may contain nulls */
	public SystemConfig[] getConfigs() {
		return configs;
	}

	/** Get the location of the data factory */
	abstract public String getLocation();

	/** Create a new data factory */
	protected DataFactory(SystemConfig[] cfgs) {
		configs = cfgs;
	}

	/** Create an empty (all MISSING_DATA) data set */
	static public float[] createEmptyDataSet() {
		float[] set = new float[ SAMPLES_PER_DAY ];
		for( int i = 0; i < SAMPLES_PER_DAY; i++ )
			set[ i ] = MISSING_DATA;
		return set;
	}

	/** Create a set of flow data from volume data */
	static public float[] createFlowSet( float[] volume ) {
		float[] flow = new float[ volume.length ];
		for( int i = 0; i < flow.length; i++ ) {
			if( volume[ i ] >= 0 ) flow[ i ] =
				volume[ i ] * SAMPLES_PER_HOUR;
			else flow[ i ] = MISSING_DATA;
		}
		return flow;
	}

	/** Create a set of headway data from flow data */
	static public float[] createHeadwaySet( float[] flow ) {
		float[] headway = new float[ flow.length ];
		for( int i = 0; i < headway.length; i++ ) {
			if( flow[ i ] > 0 ) headway[ i ] =
				SECONDS_PER_HOUR / flow[ i ];
			else headway[ i ] = MISSING_DATA;
		}
		return headway;
	}

	/** Create a set of occupancy data from scan data */
	static public float[] createOccupancySet( float[] scans ) {
		float[] occupancy = new float[ scans.length ];
		for( int i = 0; i < occupancy.length; i++ ) {
			if( scans[ i ] >= 0 ) occupancy[ i ] =
				MAX_OCCUPANCY * scans[ i ] / MAX_SCANS;
			else occupancy[ i ] = MISSING_DATA;
		}
		return occupancy;
	}

	/** Create a set of density data from occupancy data */
	static public float[] createDensitySet( float[] occupancy,
		float field )
	{
		float[] density = new float[ occupancy.length ];
		for( int i = 0; i < density.length; i++ ) {
			if( occupancy[ i ] >= 0 ) density[ i ] =
				occupancy[ i ] * FEET_PER_MILE / field / MAX_OCCUPANCY;
			else density[ i ] = MISSING_DATA;
		}
		return density;
	}

	/** Create a set of speed data from flow and density data */
	static public float[] createSpeedSet( float[] flow,
		float[] density )
	{
		float[] speed = new float[flow.length];
		for(int i = 0; i < speed.length; i++) {
			if(flow[i] > 0 && density[i] > 0)
				speed[i] = flow[i] / density[i];
			else speed[i] = MISSING_DATA;
		}
		return speed;
	}

	/** Create a set of capacity data from flow and density data */
	static public float[] createCapacitySet( float [] flow,
		float[] density, float criticalDensity )
	{
		float[] capacity = new float[ flow.length ];
		for( int i = 0; i < capacity.length; i++ ) {
			if( flow[ i ] > 0 && density[ i ] > DENSITY_THRESHOLD )
				capacity[ i ] = flow[ i ] *
					criticalDensity / density[ i ];
			else capacity[ i ] = MISSING_DATA;
		}
		return capacity;
	}

	/** Create a hash key for the provided Calendar object */
	static protected String createKey( Calendar c ) {
		StringBuffer name = new StringBuffer( 8 );
		name.append( c.get( Calendar.YEAR ) );
		while( name.length() < 4 ) name.insert( 0, '0' );
		name.append( c.get( Calendar.MONTH ) + 1 );
		while( name.length() < 6 ) name.insert( 4, '0' );
		name.append( c.get( Calendar.DAY_OF_MONTH ) );
		while( name.length() < 8 ) name.insert( 6, '0' );
		return name.toString();
	}

	protected final float[] readScanStream(InputStream is)
			throws IOException{
		return readStream(is, short.class);
	}

	protected final float[] readVolumeStream(InputStream is)
			throws IOException{
		return readStream(is, byte.class);
	}

	protected final float[] readSpeedStream(InputStream is)
			throws IOException{
		return readStream(is, byte.class);
	}

	/** Read a stream of values into a float[]
	 * 
	 * @param is The input stream to read
	 * @param type The type of values to be read (short or byte).
	 * @return An array of float values from the stream
	 * @throws IOException
	 */
	private final float[] readStream(InputStream is, Class c)
			throws IOException {
		DataInputStream dis = new DataInputStream(is);
		float[] set = new float[SAMPLES_PER_DAY];
		for(int r = 0; r < SAMPLES_PER_DAY; r++)
			set[r] = MISSING_DATA;
		try {
			for(int r = 0; r < SAMPLES_PER_DAY; r++)
				if(c == byte.class){
					set[r] = dis.readByte();
				}else if(c == short.class){
					set[r] = dis.readShort();
				}
		}
		catch(EOFException e) {}
		return set;
	}

	/** Create a detector data object */
	abstract public DetectorData createDetectorData( String id );

	/** 
	  * Read a property from the property file
	  * @param pf Property file.
	  * @param id Name of property to return.
	  * @param def if id doesn't exist, the default string is returned. 
	  */
	//FIXME: this method is a duplicate of utils/get(). Would be nice have utils access in datatools.
	static public String getProperty(Properties pf,String id,String def) {

		// eliminate nulls
		id=(id==null ? "" : id);
		def=(def==null ? "" : def);

		if (pf==null) {
			System.err.println("Error: the property file is null");
			return "";
		}

		String p=pf.getProperty(id);

		// use default
		if (p==null) {
			p=def;
			if (def.length()==0)
				System.err.println("Warning: a property ("+id+
					") was not found in the property file.");
			else
				System.err.println("Warning: a property ("+id+
					") was not found in the property file, "+
					"assigned the default value("+def+")");
		}
		return p;
	}
}
