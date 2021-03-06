/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;
import java.net.SocketTimeoutException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * Operatoin to reset a dynamic message sign.
 *
 * @author Douglas Lau
 */
public class OpResetDMS extends OpDMS {

	/** Timeout (ms) to wait for a controller reset */
	static protected final long RESET_TIMEOUT = 45 * 1000;

	/** Create a new DMS reset object */
	public OpResetDMS(DMSImpl d) {
		super(PriorityLevel.COMMAND, d);
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		return new ExecuteReset();
	}

	/** Phase to execute the DMS reset */
	protected class ExecuteReset extends Phase {

		/** Execute the DMS reset */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsSWReset reset = new DmsSWReset();
			mess.add(reset);
			logStore(reset);
			mess.storeProps();
			return new CheckResetCompletion();
		}
	}

	/** Phase to check for completion of the DMS reset */
	protected class CheckResetCompletion extends Phase {

		/** Time to stop checking if the test has completed */
		protected final long expire = TimeSteward.currentTimeMillis() + 
			RESET_TIMEOUT;

		/** Check for reset completion */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsSWReset reset = new DmsSWReset();
			mess.add(reset);
			try {
				mess.queryProps();
				logQuery(reset);
				if(reset.getInteger() == 0)
					return null;
			}
			catch(SocketTimeoutException e) {
				// Controller must still be offline
			}
			if(TimeSteward.currentTimeMillis() > expire) {
				logError("reset timeout expired -- giving up");
				return null;
			} else
				return this;
		}
	}
}
