/*
 * Project: TMS Log
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating loggers.
 */
public abstract class TmsLogFactory {

	/** Create a standard logger */
	static public Logger createLogger(String name) {
		return createLogger(name, null, null);
	}

	/**
	 * Create a standard logger.
	 * @param name The base name for the log files
	 * @param level The level of logging
	 * @param dir The location for the log files
	 * @return
	 */
	static public Logger createLogger(String name, Level level, File dir) {
		assert name != null;
		Logger l = Logger.getLogger(name);
		l.setLevel(level != null ? level: Level.FINE);
		setDirectory(l, dir, name);
		return l;
	}

	/** Set the directory for a logger */
	static protected void setDirectory(Logger l, File dir, String name) {
		removeParentHandlers(l);
		Handler h = createHandler(dir, name);
		h.setFormatter(new TmsLogFormatter());
		l.addHandler(h);
	}

	/** Remove existing handlers from a logger */
	static protected void removeParentHandlers(Logger l) {
		Logger parent = l.getParent();
		while(parent != null) {
			Handler[] pHandlers = parent.getHandlers();
			for(int i = 0; i < pHandlers.length; i++)
				parent.removeHandler(pHandlers[i]);
			parent = parent.getParent();
		}
	}

	/** Create a file handler */
	static protected Handler createHandler(File dir, String name) {
		if(dir == null)
			return new ConsoleHandler();
		String fn = new File(dir, name + "_%g.log").getAbsolutePath();
		try {
			return new FileHandler(fn, 1024 * 1024 * 5, 4);
		}
		catch(IOException e) {
			printError("Filename=" + fn + ", " + e.getMessage());
			return new ConsoleHandler();
		}
	}

	/** Print an error message */
	static protected void printError(String e) {
		System.out.println(e + "... using standard out.");
	}

	/** Redirect the standard output and error streams to log files. */
	static public void redirectStdStreams(String appName, File dir)
		throws FileNotFoundException
	{
		String fileName = dir.getAbsolutePath() +
			File.separator + appName;
		FileOutputStream fos =
			new FileOutputStream(fileName + ".out", true);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		System.setOut(new PrintStream(bos, true));
		fos = new FileOutputStream(fileName + ".err", true);
		bos = new BufferedOutputStream(fos);
		System.setErr(new PrintStream(bos, true));
	}
}
