/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dinrelay;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.BeaconPoller;
import us.mn.state.dot.tms.server.comm.LCSPoller;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;

/**
 * Poller to control Digital Loggers Inc DIN Relay devices.
 *
 * @author Douglas Lau
 */
public class DinRelayPoller extends MessagePoller<DinRelayProperty>
	implements LCSPoller, BeaconPoller
{
	/** DIN relay debug log */
	static final DebugLog DIN_LOG = new DebugLog("dinrelay");

	/** Create a new DIN relay poller */
	public DinRelayPoller(String n, Messenger m) {
		super(n, m);
	}

	/** Check if a drop address is valid */
	@Override
	public boolean isAddressValid(int drop) {
		return true;
	}

	/** Query the outlet status */
	public void queryOutlets(ControllerImpl c, OutletProperty op) {
		addOperation(new OpQueryOutlets(c, op));
	}

	/** Command the outlet status */
	public void commandOutlets(ControllerImpl c, boolean[] outlets,
		OutletProperty op)
	{
		addOperation(new OpCommandOutlets(c, outlets, op));
	}

	/** Send a device request */
	@Override
	public void sendRequest(LCSArrayImpl lcs_array, DeviceRequest r) {
		switch (r) {
		case SEND_SETTINGS:
			addOperation(new OpSendLCSSettings(lcs_array));
			break;
		case QUERY_MESSAGE:
			addOperation(new OpQueryLCSIndications(lcs_array));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send new indications to an LCS array.
	 * @param lcs_array LCS array.
	 * @param ind New lane use indications.
	 * @param o User who deployed the indications. */
	@Override
	public void sendIndications(LCSArrayImpl lcs_array, Integer[] ind,
		User o)
	{
		addOperation(new OpSendLCSIndications(lcs_array, ind, o));
	}

	/** Send a device request */
	@Override
	public void sendRequest(BeaconImpl b, DeviceRequest r) {
		switch (r) {
		case QUERY_STATUS:
			addOperation(new OpQueryBeaconState(b));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Set the flashing state of a beacon */
	@Override
	public void setFlashing(BeaconImpl b, boolean f) {
		addOperation(new OpChangeBeaconState(b, f));
	}

	/** Get the protocol debug log */
	@Override
	protected DebugLog protocolLog() {
		return DIN_LOG;
	}
}
