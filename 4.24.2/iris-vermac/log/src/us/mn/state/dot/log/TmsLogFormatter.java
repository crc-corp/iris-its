/*
 * Project: TMS Log
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * This is the default formatter that can be used in any TMS application.
 * @author john3tim
 *
 */
public class TmsLogFormatter extends SimpleFormatter {

	SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
	
	public String format(LogRecord r){
		Date d = Calendar.getInstance().getTime();
		String level = r.getLevel().toString();
		if(level.length() > 4) level = level.substring(0,4);
		return "[" + f.format(d) + "] " + level + " " +
			r.getMessage() + "\n";
	}
}
