/*
 * DataTools
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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

import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URL;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import us.mn.state.dot.data.extract.DataExtract;
import us.mn.state.dot.data.plot.DataPlot;
import us.mn.state.dot.util.HTTPProxySelector;

/**
 * Main starting point for data tools. Processes command line parameters and
 * forwards them to the appropriate application
 *
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson </a>
 * @author Douglas Lau
 */
public class Main {

	/** Name of default properties file to load */
	static protected final String DEFAULT_PROPERTIES =
		"dataplot.properties";

	static protected final String DATAPLOT = "dataplot";

	static protected final String DATAEXTRACT = "dataextract";

	/** Launch the specified application */
	static protected void launchApp(Properties props) throws IOException,
		ParserConfigurationException
	{
		DataFactory factory = DataFactory.create(props);
		String application = props.getProperty("datatools.application");
		if(application.equals(DATAPLOT))
			new DataPlot(factory);
		else if(application.equals(DATAEXTRACT))
			new DataExtract(factory);
	}

	/** Create a URL for the specified property file */
	static protected URL createURL(String prop_file) throws IOException {
		String workingDir = System.getProperty("user.dir");
		File file = new File(workingDir, prop_file);
		if(file.exists())
			return file.toURI().toURL();
		else
			return new URL(prop_file);
	}

	/** Read the IRIS property file */
	static protected Properties readPropertyFile(URL url)
		throws IOException
	{
		Properties props = new Properties();
		props.load(url.openStream());
		return props;
	}

	/** Get the name of the property file to use */
	static protected String getPropertyFile(String[] args) {
		if(args.length > 0)
			return args[0];
		else
			return DEFAULT_PROPERTIES;
	}

	/**
	 * Main entry point.
	 *
	 * @param args Arguments passed to the application.
	 */
	static protected void execute(final String[] args) throws Exception {
		URL url = createURL(getPropertyFile(args));
		Properties props = readPropertyFile(url);
		ProxySelector.setDefault(new HTTPProxySelector(props));
		launchApp(props);
	}

	/**
	 * Main Data Tools entry point.
	 *
	 * @param args the command line arguments
	 */
	static public void main(String args[]) {
		try {
			execute(args);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
