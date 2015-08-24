/*
 * Project: Trafdat
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.trafdat;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet for serving IRIS traffic sample data.  There are several valid
 * request types.
 *
 * @author john3tim
 * @author Douglas Lau
 */
public class TrafdatServlet extends HttpServlet {

	/** Maximum length of a data filename */
	static private final int MAX_FILENAME_LENGTH = 24;

	/** Default district ID */
	static private final String DEFAULT_DIST = "tms";

	/** Split a path into component parts.
	 * @param path Request path
	 * @return Array of path components. */
	static private String[] splitPath(String path) {
		if (path != null) {
			while (path.startsWith("/"))
				path = path.substring(1);
			return path.split("/");
		} else
			return "".split("/");
	}

	/** Check if the given file name is valid.
	 * @param name Name of sample file.
	 * @return true if name is valid, otherwise false */
	static private boolean isFileNameValid(String name) {
		return name.length() <= MAX_FILENAME_LENGTH;
	}

	/** Check if the given file name is a JSON file.
	 * @param name Name of sample file.
	 * @return true if name is valid, otherwise false */
	static private boolean isJsonFile(String name) {
		return name.endsWith(".json");
	}

	/** Strip the .json extension from a file name */
	static private String stripJsonExt(String name) {
		assert name.endsWith(".json");
		return name.substring(0, name.length() - 5);
	}

	/** Create a buffered writer for the response.
	 * @param resp Servlet response.
	 * @return Buffered writer for the response. */
	static private Writer createWriter(HttpServletResponse resp)
		throws IOException
	{
		OutputStream os = resp.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os);
		return new BufferedWriter(osw);
	}

	/** Send raw data from the given input stream to the response.
	 * @param resp Servlet response object.
	 * @param in Input stream to read data from. */
	static private void sendRawData(HttpServletResponse resp,
		InputStream in) throws IOException
	{
		byte[] buf = new byte[4096];
		OutputStream out = resp.getOutputStream();
		try {
			while (true) {
				int n_bytes = in.read(buf);
				if (n_bytes < 0)
					break;
				out.write(buf, 0, n_bytes);
			}
		}
		finally {
			out.close();
		}
	}

	/** Send text data from the given iterator to the response.
	 * @param resp Servlet response object.
	 * @param it Iterator of values to send. */
	static private void sendTextData(HttpServletResponse resp,
		Iterator<String> it) throws IOException
	{
		resp.setContentType("text/plain");
		Writer w = createWriter(resp);
		try {
			while (it.hasNext()) {
				String val = it.next();
				w.write(val + "\n");
			}
			w.flush();
		}
		finally {
			w.close();
		}
	}

	/** Send data from the given iterator to the response as JSON.
	 * @param resp Servlet response object.
	 * @param it Iterator of values to send. */
	static private void sendJsonData(HttpServletResponse resp,
		Iterator<String> it) throws IOException
	{
		resp.setContentType("application/json");
		Writer w = createWriter(resp);
		try {
			w.write('[');
			boolean first = true;
			while (it.hasNext()) {
				String val = formatJson(it.next());
				if (!first)
					w.write(',');
				w.write(val);
				first = false;
			}
			w.write(']');
			w.flush();
		}
		finally {
			w.close();
		}
	}

	/** Format a number as a JSON value.
	 * @param val Value to format.
	 * @return JSON value. */
	static private String formatJson(String val) {
		return (val != null) ? val : "null";
	}

	/** Initialize the servlet */
	@Override
	public void init(ServletConfig config) throws ServletException {
		// Nothing to initialize
	}

	/** Process an HTTP GET request */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		String path = req.getPathInfo();
		try {
			if (!processReq(path, resp)) {
				sendError(resp,
					HttpServletResponse.SC_BAD_REQUEST);
			}
		}
		catch (FileNotFoundException e) {
			sendError(resp, HttpServletResponse.SC_NOT_FOUND);
		}
		catch (IOException e) {
			e.printStackTrace();
			sendError(resp,
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/** Send an HTTP error code.
	 * @param resp Servlet response object.
	 * @param ec HTTP error code. */
	private void sendError(HttpServletResponse resp, int ec) {
		try {
			resp.sendError(ec);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Process a traffic data request from a client.
	 * @param path Path of requested resource.
	 * @param resp Servlet response object.
	 * @return true if request if valid, otherwise false */
	private boolean processReq(String path, HttpServletResponse resp)
		throws IOException
	{
		String[] p = splitPath(path);
		switch (p.length) {
		case 1:
			return processReq1(p, resp);
		case 2:
			return processReq2(p, resp);
		case 3:
			return processReq3(p, resp);
		case 4:
			return processReq4(p, resp);
		default:
			return false;
		}
	}

	/** Process a request with 1 path part.
	 * @param p Path array.
	 * @param resp Servlet response object.
	 * @return true if request if valid, otherwise false */
	private boolean processReq1(String[] p, HttpServletResponse resp)
		throws IOException
	{
		assert p.length == 1;
		return processDocReq(p[0], resp)
		    || processDistReq(p[0], resp)
		    || processTextDateReq(DEFAULT_DIST, p[0], resp);
	}

	/** Process a request for the documentation.
	 * @param resp Servlet response object.
	 * @return true if request if valid, otherwise false */
	private boolean processDocReq(String d, HttpServletResponse resp)
		throws IOException
	{
		if ("".equals(d)) {
			resp.setContentType("text/html");
			sendRawData(resp, SensorArchive.docInputStream());
			return true;
		} else
			return false;
	}

	/** Process a request for the available districts.
	 * @param districts District path.
	 * @param resp Servlet response object.
	 * @return true if request if valid, otherwise false */
	private boolean processDistReq(String districts,
		HttpServletResponse resp) throws IOException
	{
		if ("districts".equals(districts)) {
			SensorArchive sa = new SensorArchive();
			if (sa.isValid()) {
				sendJsonData(resp, sa.lookupDistricts());
				return true;
			}
		}
		return false;
	}

	/** Process a request with 2 path parts.
	 * @param p Path array.
	 * @param resp Servlet response object.
	 * @return true if request if valid, otherwise false */
	private boolean processReq2(String[] p, HttpServletResponse resp)
		throws IOException
	{
		assert p.length == 2;
		return processSensorReq(p[0], p[1], resp)
		    || processJsonDateReq(p[0], p[1], resp)
		    || processTextDateReq(p[0], p[1], resp)
		    || processSensorReq(DEFAULT_DIST, p[0], p[1], resp);
	}

	/** Process a sensor list request.
	 * @param dist District ID.
	 * @param date String date (8 digits yyyyMMdd).
	 * @param resp Servlet response object.
	 * @return true if request if valid, otherwise false */
	private boolean processSensorReq(String dist, String date,
		HttpServletResponse resp) throws IOException
	{
		if (SensorArchive.isValidDate(date)) {
			SensorArchive sa = new SensorArchive(dist);
			if (sa.isValid()) {
				sendJsonData(resp, sa.lookup(date));
				return true;
			}
		}
		return false;
	}

	/** Process a request for the available dates for a given year as JSON.
	 * @param dist District ID.
	 * @param yj Year + .json extension (yyyy.json).
	 * @param resp Servlet response object.
	 * @return true if request if valid, otherwise false */
	private boolean processJsonDateReq(String dist, String yj,
		HttpServletResponse resp) throws IOException
	{
		if (isJsonFile(yj)) {
			String year = stripJsonExt(yj);
			if (SensorArchive.isValidYear(year)) {
				SensorArchive sa = new SensorArchive(dist);
				if (sa.isValid()) {
					sendJsonData(resp,sa.lookupDates(year));
					return true;
				}
			}
		}
		return false;
	}

	/** Process a request for the available dates for a given year.
	 * @param dist District ID.
	 * @param year String year (4 digits, yyyy).
	 * @param resp Servlet response object.
	 * @return true if request if valid, otherwise false */
	private boolean processTextDateReq(String dist, String year,
		HttpServletResponse resp) throws IOException
	{
		if (SensorArchive.isValidYear(year)) {
			SensorArchive sa = new SensorArchive(dist);
			if (sa.isValid()) {
				sendTextData(resp, sa.lookupDates(year));
				return true;
			}
		}
		return false;
	}

	/** Process a request with 3 path parts.
	 * @param p Path array.
	 * @param resp Servlet response object.
	 * @return true if request if valid, otherwise false */
	private boolean processReq3(String[] p, HttpServletResponse resp)
		throws IOException
	{
		assert p.length == 3;
		return processSensorReq(p[0], p[1], p[2], resp)
		    || processSampleReq(p[0], p[1], p[2], resp)
		    || processSampleReq(DEFAULT_DIST, p[0], p[1], p[2], resp);
	}

	/** Process a sensor list request.
	 * @param dist District ID.
	 * @param year String year (4 digits, yyyy).
	 * @param date String date (8 digits yyyyMMdd).
	 * @param resp Servlet response object.
	 * @return true if request if valid, otherwise false */
	private boolean processSensorReq(String dist, String year,
		String date, HttpServletResponse resp) throws IOException
	{
		return SensorArchive.isValidYearDate(year, date)
		    && processSensorReq(dist, date, resp);
	}

	/** Process a request with 4 path parts.
	 * @param p Path array.
	 * @param resp Servlet response object.
	 * @return true if request if valid, otherwise false */
	private boolean processReq4(String[] p, HttpServletResponse resp)
		throws IOException
	{
		assert p.length == 4;
		return processSampleReq(p[0], p[1], p[2], p[3], resp);
	}

	/** Process a sample data request.
	 * @param dist District ID.
	 * @param year String year (4 digits, yyyy).
	 * @param date String date (8 digits yyyyMMdd).
	 * @param name Sample file name.
	 * @param resp Servlet response object.
	 * @return true if request if valid, otherwise false */
	private boolean processSampleReq(String dist, String year,
		String date, String name, HttpServletResponse resp)
		throws IOException
	{
		return SensorArchive.isValidYearDate(year, date)
		    && processSampleReq(dist, date, name, resp);
	}

	/** Process a sample data request.
	 * @param dist District ID.
	 * @param date String date (8 digits yyyyMMdd).
	 * @param name Sample file name.
	 * @param resp Servlet response object.
	 * @return true if request if valid, otherwise false */
	private boolean processSampleReq(String dist, String date,
		String name, HttpServletResponse resp) throws IOException
	{
		if (!SensorArchive.isValidDate(date))
			return false;
		if (!isFileNameValid(name))
			return false;
		if (isJsonFile(name)) {
			return processJsonReq(dist, date, stripJsonExt(name),
				resp);
		} else if (SensorArchive.isValidSampleFile(name)) {
			resp.setContentType("application/octet-stream");
			SensorArchive sa = new SensorArchive(dist);
			if (sa.isValid()) {
				InputStream in = sa.sampleInputStream(date,
					name);
				try {
					sendRawData(resp, in);
				}
				finally {
					in.close();
				}
				return true;
			}
		}
		return false;
	}

	/** Process a JSON data request.
	 * @param dist District ID.
	 * @param date String date (8 digits yyyyMMdd).
	 * @param name Sample file name.
	 * @param resp Servlet response object.
	 * @return true if request if valid, otherwise false */
	private boolean processJsonReq(String dist, String date,
		String name, HttpServletResponse resp) throws IOException
	{
		if (SensorArchive.isBinnedFile(name)) {
			SensorArchive sa = new SensorArchive(dist);
			if (sa.isValid()) {
				sendJsonData(resp, sa.sampleIterator(date,
					name));
				return true;
			}
		}
		return false;
	}
}
