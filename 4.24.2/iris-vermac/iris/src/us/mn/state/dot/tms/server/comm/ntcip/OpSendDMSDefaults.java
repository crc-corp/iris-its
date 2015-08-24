/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
 * Copyright (C) 2015  Castle Rock Associates, Inc.
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
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.Multi.JustificationLine;
import us.mn.state.dot.tms.Multi.JustificationPage;
import static us.mn.state.dot.tms.SystemAttrEnum.*;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibledstar.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mibledstar.MIB.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibskyline.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mibskyline.MIB.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.BadValue;
import us.mn.state.dot.tms.server.comm.snmp.NoSuchName;
import us.mn.state.dot.tms.server.comm.snmp.SNMP;

/**
 * Operation to send default parameters to a DMS.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class OpSendDMSDefaults extends OpDMS {

	/** Create a new operation to send DMS default parameters */
	public OpSendDMSDefaults(DMSImpl d) {
		super(PriorityLevel.DOWNLOAD, d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new SetCommPowerLoss();
	}

	/** Phase to set the comm and power loss times */
	protected class SetCommPowerLoss extends Phase {

		/** Set the comm loss action */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer power_time =dmsShortPowerLossTime.makeInt();
			ASN1Integer comm_time = dmsTimeCommLoss.makeInt();
			MessageIDCode end_msg = new MessageIDCode(
				dmsEndDurationMessage.node);
			power_time.setInteger(0);
			comm_time.setInteger(DMS_COMM_LOSS_MINUTES.getInt());
			end_msg.setMemoryType(DmsMessageMemoryType.blank);
			end_msg.setNumber(1);
			end_msg.setCrc(0);
			addForStore(mess, power_time);
			addForStore(mess, comm_time);
			addForStore(mess, end_msg);
			logStore(power_time);
			logStore(comm_time);
			logStore(end_msg);
			mess.storeProps();
			return new PixelService();
		}
	}

	/** Phase to set the pixel service schedule */
	protected class PixelService extends Phase {

		/** Set the pixel service schedule */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer dur = vmsPixelServiceDuration.makeInt();
			ASN1Integer freq = vmsPixelServiceFrequency.makeInt();
			ASN1Integer time = vmsPixelServiceTime.makeInt();
			dur.setInteger(10);
			freq.setInteger(1440);
			time.setInteger(180);
			addForStore(mess, dur);
			addForStore(mess, freq);
			addForStore(mess, time);
			logStore(dur);
			logStore(freq);
			logStore(time);
			mess.storeProps();
			return new MessageDefaults();
		}
	}

	/** Phase to set the message defaults */
	protected class MessageDefaults extends Phase {

		/** Set the message defaults */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<JustificationLine> line = new ASN1Enum<
				JustificationLine>(JustificationLine.class,
				defaultJustificationLine.node);
			ASN1Enum<JustificationPage> page = new ASN1Enum<
				JustificationPage>(JustificationPage.class,
				defaultJustificationPage.node);
			ASN1Integer on_time = defaultPageOnTime.makeInt();
			ASN1Integer off_time = defaultPageOffTime.makeInt();
			line.setInteger(DMS_DEFAULT_JUSTIFICATION_LINE.getInt());
			page.setInteger(DMS_DEFAULT_JUSTIFICATION_PAGE.getInt());
			on_time.setInteger(Math.round(10 *
				DMS_PAGE_ON_DEFAULT_SECS.getFloat()));
			off_time.setInteger(Math.round(10 *
				DMS_PAGE_OFF_DEFAULT_SECS.getFloat()));
			addForStore(mess, line);
			addForStore(mess, page);
			addForStore(mess, on_time);
			addForStore(mess, off_time);
			logStore(line);
			logStore(page);
			logStore(on_time);
			logStore(off_time);
			mess.storeProps();
			return new LedstarDefaults();
		}
	}

	/** Phase to set Ledstar-specific object defaults */
	protected class LedstarDefaults extends Phase {

		/** Set Ledstar-specific object defaults */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer temp = ledHighTempCutoff.makeInt();
			ASN1Integer override = ledSignErrorOverride.makeInt();
			ASN1Integer limit = ledBadPixelLimit.makeInt();
			temp.setInteger(DMS_HIGH_TEMP_CUTOFF.getInt());
			limit.setInteger(500);
			try {
				addForStore(mess, temp);
				addForStore(mess, override);
				addForStore(mess, limit);
				mess.storeProps();
				logStore(temp);
				logStore(override);
				logStore(limit);
			}
			catch (NoSuchName e) {
				// Must not be a Ledstar sign
				return new SkylineDefaults();
			}
			catch (BadValue e) {
				// Daktronics uses this instead of NoSuchName
				// Is there a better way to check for that?
			}
			return null;
		}
	}

	/** Phase to set Skyline-specific object defaults */
	protected class SkylineDefaults extends Phase {

		/** Set Skyline-specific object defaults */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer temp = dmsTempCritical.makeInt();
			ASN1Integer day_night = dynBrightDayNight.makeInt();
			ASN1Integer day_rate = dynBrightDayRate.makeInt();
			ASN1Integer night_rate = dynBrightNightRate.makeInt();
			ASN1Integer max_lvl = dynBrightMaxNightManLvl.makeInt();
			temp.setInteger(DMS_HIGH_TEMP_CUTOFF.getInt());
			day_night.setInteger(32);
			day_rate.setInteger(1);
			night_rate.setInteger(15);
			max_lvl.setInteger(20);
			try {
				addForStore(mess, temp);
				addForStore(mess, day_night);
				addForStore(mess, day_rate);
				addForStore(mess, night_rate);
				addForStore(mess, max_lvl);
				mess.storeProps();
				logStore(temp);
				logStore(day_night);
				logStore(day_rate);
				logStore(night_rate);
				logStore(max_lvl);
			}
			catch (NoSuchName e) {
				// Must not be a Skyline sign
				return new AddcoDefaults();
			}
			return null;
		}
	}

	/** Phase to set ADDCO-specific object defaults */
	protected class AddcoDefaults extends Phase {

		/** Set ADDCO-specific object defaults */
		protected Phase poll(CommMessage mess) throws IOException {
			// ADDCO brick signs have these dimensions
			String make = dms.getMake();
			// NOTE: setting these objects requires use of the
			//       "administrator" community name.  We need to
			//       check that the password is not null before
			//       attempting to set them.
			if (make != null &&
			    make.startsWith("ADDCO") &&
			    dms.getDmsType() == DMSType.VMS_CHAR.ordinal() &&
			    controller.getPassword() != null)
			{
				ASN1Integer h_border =
					dmsHorizontalBorder.makeInt();
				ASN1Integer v_border =
					dmsVerticalBorder.makeInt();
				ASN1Integer h_pitch =
					vmsHorizontalPitch.makeInt();
				ASN1Integer v_pitch =vmsVerticalPitch.makeInt();
				h_border.setInteger(50);
				v_border.setInteger(69);
				h_pitch.setInteger(69);
				v_pitch.setInteger(69);
				addForStore(mess, h_border);
				addForStore(mess, v_border);
				addForStore(mess, h_pitch);
				addForStore(mess, v_pitch);
				mess.storeProps();
				logStore(h_border);
				logStore(v_border);
				logStore(h_pitch);
				logStore(v_pitch);
			}
			return null;
		}
	}
}
