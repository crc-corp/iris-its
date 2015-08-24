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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Sensor data archive.
 *
 * @author Douglas Lau
 */
public class SensorArchive {

	/** Path to directory containing traffic data files */
	static private final String BASE_PATH = "/var/lib/iris/traffic";

	/** Name of trafdat documentation file */
	static private final String DOC_FILE = "index.html";

	/** Traffic file extension */
	static private final String EXT = ".traffic";

	/** Check if a file name is canonical */
	static private boolean isCanonical(File f) throws IOException {
		return f.getPath().equals(f.getCanonicalPath());
	}

	/** Check if sensor data exists for a given date.
	 * @param path Path to year archive.
	 * @param name Name of file in archive.
	 * @return true if file is readable, otherwise false. */
	static private boolean isDateReadable(File path, String name) {
		File file = new File(path, name);
		return file.canRead() &&
		      (isTrafficFile(name) || isDateDirectory(file, name));
	}

	/** Check if a name is a valid .traffic file */
	static private boolean isTrafficFile(String name) {
		return (name.length() == 16) && name.endsWith(EXT);
	}

	/** Check if a date directory exists */
	static private boolean isDateDirectory(File file, String name) {
		return (name.length() == 8) && file.isDirectory();
	}

	/** Parse the date string for the given file.
	 * @param name Name of file in archive.
	 * @return Date represented by file, or null */
	static private String parseDate(String name) {
		if (name.length() >= 8) {
			String date = name.substring(0, 8);
			if (isValidDate(date))
				return date;
		}
		return null;
	}

	/** Check if the given year is valid.
	 * @param year String year (4 digits, yyyy).
	 * @return true if year is valid, otherwise false */
	static public boolean isValidYear(String year) {
		try {
			Integer.parseInt(year);
			return year.length() == 4;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	/** Check if the given date is valid.
	 * @param date String date (8 digits yyyyMMdd)
	 * @return true if date is valid, otherwise false */
	static public boolean isValidDate(String date) {
		try {
			Integer.parseInt(date);
			return date.length() == 8;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	/** Check if the given year and date is valid.
	 * @param year String year (4 digits, yyyy).
	 * @param date String date (8 digits yyyyMMdd)
	 * @return true if date is valid, otherwise false */
	static public boolean isValidYearDate(String year, String date) {
		return isValidYear(year) &&
		       isValidDate(date) &&
		       date.startsWith(year);
	}

	/** Check if the given sample file name is valid.
	 * @param name Name of sample file.
	 * @return true if name is valid, otherwise false */
	static public boolean isValidSampleFile(String name) {
		return isBinnedFile(name) || name.endsWith(".vlog");
	}

	/** Check if the given file name is a binned sample file.
	 * @param name Name of sample file.
	 * @return true if name is for a binned sample file, otherwise false */
	static public boolean isBinnedFile(String name) {
		return isBinnedTraffic(name)
		    || isClassification(name)
		    || isBinnedPrecipitation(name);
	}

	/** Check if the given file name is a binned traffic file.
	 * @param name Name of sample file.
	 * @return true if name is for a binned traffic file, otherwise false */
	static private boolean isBinnedTraffic(String name) {
		return name.endsWith(".v30")
		    || name.endsWith(".c30")
		    || name.endsWith(".s30");
	}

	/** Check if the given file name is for vehicle classification data.
	 * @param name Name of sample file.
	 * @return true if name is for vehicle classification data. */
	static private boolean isClassification(String name) {
		return name.endsWith(".vmc30")
		    || name.endsWith(".vs30")
		    || name.endsWith(".vm30")
		    || name.endsWith(".vl30");
	}

	/** Check if the given file name is a binned precipitation file.
	 * @param name Name of sample file.
	 * @return true if name is for a binned precipitation file */
	static private boolean isBinnedPrecipitation(String name) {
		return name.endsWith(".pr60")
		    || name.endsWith(".pt60");
	}

	/** Create a sample bin for the given file name.
	 * @param name Name of sample file.
	 * @return Sample bin for specified file. */
	static private SampleBin createSampleBin(String name) {
		if (name.endsWith(".v30"))
			return new VolumeSampleBin();
		else if (name.endsWith(".s30"))
			return new SpeedSampleBin();
		else
			return null;
	}

	/** Get the sensor ID for a given file name.
	 * @param name Sample file name.
	 * @return Sensor ID. */
	static private String sensor_id(String name) {
		int i = name.indexOf('.');
		return (i > 0) ? name.substring(0, i) : name;
	}

	/** Format a number as a string value.
	 * @param val Number to format.
	 * @return String value. */
	static private String formatInt(int val) {
		return (val >= 0) ? Integer.toString(val) : null;
	}

	/** Get a sample reader for the specified sample file name.
	 * @param name Name of sample file.
	 * @return SampleReader to read samples from the file. */
	static private SampleReader sampleReader(String name) {
		if (name.endsWith(".c30") || name.endsWith(".pr60"))
			return new ShortSampleReader();
		else
			return new ByteSampleReader();
	}

	/** Interface for sample data readers */
	static private interface SampleReader {
		int getSample(DataInputStream dis) throws IOException;
	}

	/** Class to read byte samples from a data input stream */
	static private class ByteSampleReader implements SampleReader {
		public int getSample(DataInputStream dis) throws IOException {
			return dis.readByte();
		}
	}

	/** Class to read short samples from a data input stream */
	static private class ShortSampleReader implements SampleReader {
		public int getSample(DataInputStream dis) throws IOException {
			return dis.readShort();
		}
	}

	/** Data path for district */
	private final File dist_path;

	/** Build a file path to the given archive location.
	 * @param path District archive relative path to file.
	 * @return Path to directory in sample archive. */
	private File buildPath(String path) {
		return new File(dist_path, path);
	}

	/** Get the file path to the given date.
	 * @param date String date (8 digits yyyyMMdd).
	 * @return Path to file in sample archive. */
	private File getDatePath(String date) {
		assert date.length() == 8;
		String year = date.substring(0, 4);
		return new File(buildPath(year), date);
	}

	/** Get the file path to the given date traffic file.
	 * @param date String date (8 digits yyyyMMdd).
	 * @return Path to file in sample archive. */
	private File getTrafficPath(String date) {
		assert date.length() == 8;
		String year = date.substring(0, 4);
		return new File(buildPath(year), date + EXT);
	}

	/** Get an InputStream for the documenataion.
	 * @return InputStream from which sample data can be read. */
	static public InputStream docInputStream() throws IOException {
		return new FileInputStream(new File(BASE_PATH, DOC_FILE));
	}

	/** Sensor data archive.
	 * @param d District ID. */
	public SensorArchive(String d) {
		dist_path = new File(BASE_PATH, d);
	}

	/** Sensor data archive */
	public SensorArchive() {
		dist_path = new File(BASE_PATH);
	}

	/** Check if district is valid */
	public boolean isValid() {
		return dist_path.canRead() && dist_path.isDirectory();
	}

	/** Lookup the available districts.
	 * @return Iterator of available districts. */
	public Iterator<String> lookupDistricts() throws IOException {
		TreeSet<String> dists = new TreeSet<String>();
		for (String n: dist_path.list()) {
			File d = buildPath(n);
			if (d.canRead() && d.isDirectory() && isCanonical(d))
				dists.add(n);
		}
		return dists.iterator();
	}

	/** Lookup the dates available for a given year.
	 * @param year String year (4 digits).
	 * @return Iterator of dates available (8 digits yyyyMMdd). */
	public Iterator<String> lookupDates(String year) throws IOException {
		assert year.length() == 4;
		TreeSet<String> dates = new TreeSet<String>();
		File dir = buildPath(year);
		if (dir.canRead() && dir.isDirectory()) {
			for (String name: dir.list()) {
				if (isDateReadable(dir, name)) {
					String date = parseDate(name);
					if (date != null)
						dates.add(date);
				}
			}
		}
		return dates.iterator();
	}

	/** Lookup the sensors available for the given date.
	 * @param date String date (8 digits yyyyMMdd).
	 * @return Iterator of sensor IDs available for the date. */
	public Iterator<String> lookup(String date) throws IOException {
		assert date.length() == 8;
		TreeSet<String> sensors = new TreeSet<String>();
		File traffic = getTrafficPath(date);
		if (traffic.canRead() && traffic.isFile())
			lookup(traffic, sensors);
		File dir = getDatePath(date);
		if (dir.canRead() && dir.isDirectory()) {
			for (String name: dir.list()) {
				if (isValidSampleFile(name))
					sensors.add(sensor_id(name));
			}
		}
		return sensors.iterator();
	}

	/** Lookup all the sensors in a .traffic file.
	 * @param traffic Traffic file to lookup.
	 * @param sensors Sensor set.
	 * @throws IOException On file I/O error. */
	private void lookup(File traffic, TreeSet<String> sensors)
		throws IOException
	{
		ZipFile zf = new ZipFile(traffic);
		try {
			Enumeration e = zf.entries();
			while (e.hasMoreElements()) {
				ZipEntry ze = (ZipEntry)e.nextElement();
				String name = ze.getName();
				if (isValidSampleFile(name))
					sensors.add(sensor_id(name));
			}
		}
		finally {
			zf.close();
		}
	}

	/** Get an InputStream for the given date and sample file.
	 * @param date String date (8 digits yyyyMMdd).
	 * @param name Sample file name.
	 * @return InputStream from which sample data can be read. */
	public InputStream sampleInputStream(String date, String name)
		throws IOException
	{
		assert date.length() == 8;
		try {
			return getZipInputStream(date, name);
		}
		catch (FileNotFoundException e) {
			try {
				return getFileInputStream(date, name);
			}
			catch (FileNotFoundException ee) {
				return getBinnedVLogInputStream(date, name);
			}
		}
	}

	/** Get a sample InputStream from a zip (traffic) file.
	 * @param date String date (8 digits yyyyMMdd).
	 * @param name Name of sample file within .traffic file.
	 * @return InputStream from which sample data can be read. */
	private InputStream getZipInputStream(String date, String name)
		throws IOException
	{
		File traffic = getTrafficPath(date);
		try {
			ZipFile zip = new ZipFile(traffic);
			ZipEntry entry = zip.getEntry(name);
			if (entry != null)
				return zip.getInputStream(entry);
		}
		catch (ZipException e) {
			// Defer to FileNotFoundException, below
		}
		throw new FileNotFoundException(name);
	}

	/** Get a sample input stream from a regular file.
	 * @param date String date (8 digits yyyyMMdd).
	 * @param name Sample file name.
	 * @return InputStream from which sample data can be read. */
	private InputStream getFileInputStream(String date, String name)
		throws IOException
	{
		return new FileInputStream(new File(getDatePath(date), name));
	}

	/** Get a sample input stream by binning a .vlog file.
	 * @param date String date (8 digits yyyyMMdd).
	 * @param name Sample file name.
	 * @return InputStream from which sample data can be read. */
	private InputStream getBinnedVLogInputStream(String date, String name)
		throws IOException
	{
		assert date.length() == 8;
		SampleBin bin = createSampleBin(name);
		if (bin != null) {
			String vlog = sensor_id(name) + ".vlog";
			VehicleEventLog log = createVLog(
				sampleInputStream(date, vlog));
			log.bin30SecondSamples(bin);
			return new ByteArrayInputStream(bin.getData());
		} else
			throw new FileNotFoundException(name);
	}

	/** Create and process a vehicle event log.
	 * @param in InputStream to read .vlog events.
	 * @return Vehicle event log object. */
	private VehicleEventLog createVLog(InputStream in) throws IOException {
		try {
			InputStreamReader reader = new InputStreamReader(in);
			BufferedReader b = new BufferedReader(reader);
			VehicleEventLog log = new VehicleEventLog(b);
			log.propogateStampsForward();
			log.propogateStampsBackward();
			log.interpolateMissingStamps();
			return log;
		}
		finally {
			in.close();
		}
	}

	/** Get a sample Iterator for the given date and sample file.
	 * @param date String date (8 digits yyyyMMdd).
	 * @param name Sample file name.
	 * @return Iterator of all samples. */
	public Iterator<String> sampleIterator(String date, String name)
		throws IOException
	{
		InputStream in = sampleInputStream(date, name);
		try {
			BufferedInputStream bis = new BufferedInputStream(in);
			DataInputStream dis = new DataInputStream(bis);
			SampleReader sr = sampleReader(name);
			ArrayList<String> al = new ArrayList<String>(2880);
			while (true) {
				try {
					al.add(formatInt(sr.getSample(dis)));
				}
				catch (EOFException e) {
					break;
				}
			}
			return al.iterator();
		}
		finally {
			in.close();
		}
	}
}
