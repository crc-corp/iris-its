/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

/**
 * DMS message priority levels. This enum is designed so that the ordinal
 * values can be used for NTCIP activation and run-time priority. NTCIP
 * priority values can range from 1 to 255, with higher numbers indicating
 * higher priority. The enum is also ordered from low to high priority.
 *
 * @author Michael Darter
 * @author Douglas Lau
 * @author Travis Swanston
 */
public enum DMSMessagePriority {
	INVALID,	/* 0: invalid priority */
	BLANK,		/* 1: blank message run-time priority */
	PREFIX_PAGE,	/* 2: prefix page combining (activation only) */
	PSA,		/* 3: public service announcement */
	TRAVEL_TIME,	/* 4: travel time priority */
	SPEED_LIMIT,	/* 5: variable speed limit priority */
	SCHEDULED,	/* 6: scheduled priority (planned events) */
	OTHER_SYSTEM,	/* 7: other system priority */
	ALERT,		/* 8: alert priority (AMBER alerts, etc.) */
	OPERATOR,	/* 9: operator priority */
	INCIDENT_LOW,	/* 10: low-priority incident */
	INCIDENT_MED,	/* 11: medium-priority incident */
	INCIDENT_HIGH,	/* 12: high-priority incident */
	AWS,		/* 13: automated warning system */
	OVERRIDE,	/* 14: override priority */
	P15, P16, P17, P18, P19, P20, P21, P22, P23, P24, P25, P26, P27, P28,
	P29, P30, P31, P32, P33, P34, P35, P36, P37, P38, P39, P40, P41, P42,
	P43, P44, P45, P46, P47, P48, P49, P50, P51, P52, P53, P54, P55, P56,
	P57, P58, P59, P60, P61, P62, P63, P64, P65, P66, P67, P68, P69, P70,
	P71, P72, P73, P74, P75, P76, P77, P78, P79, P80, P81, P82, P83, P84,
	P85, P86, P87, P88, P89, P90, P91, P92, P93, P94, P95, P96, P97, P98,
	P99, P100, P101, P102, P103, P104, P105, P106, P107, P108, P109, P110,
	P111, P112, P113, P114, P115, P116, P117, P118, P119, P120, P121, P122,
	P123, P124, P125, P126, P127, P128, P129, P130, P131, P132, P133, P134,
	P135, P136, P137, P138, P139, P140, P141, P142, P143, P144, P145, P146,
	P147, P148, P149, P150, P151, P152, P153, P154, P155, P156, P157, P158,
	P159, P160, P161, P162, P163, P164, P165, P166, P167, P168, P169, P170,
	P171, P172, P173, P174, P175, P176, P177, P178, P179, P180, P181, P182,
	P183, P184, P185, P186, P187, P188, P189, P190, P191, P192, P193, P194,
	P195, P196, P197, P198, P199, P200, P201, P202, P203, P204, P205, P206,
	P207, P208, P209, P210, P211, P212, P213, P214, P215, P216, P217, P218,
	P219, P220, P221, P222, P223, P224, P225, P226, P227, P228, P229, P230,
	P231, P232, P233, P234, P235, P236, P237, P238, P239, P240, P241, P242,
	P243, P244, P245, P246, P247, P248, P249, P250, P251, P252, P253, P254,
	P255;

	/** Get a DMSMessagePriority from an ordinal value */
	public static DMSMessagePriority fromOrdinal(int o) {
		for(DMSMessagePriority e: values()) {
			if(e.ordinal() == o)
				return e;
		}
		return INVALID;
	}

	/** Test if a run-time priority was "scheduled" */
	static public boolean isScheduled(DMSMessagePriority p) {
		switch(p) {
		case PREFIX_PAGE:
		case PSA:
		case TRAVEL_TIME:
		case SPEED_LIMIT:
		case SCHEDULED:
		case OTHER_SYSTEM:
		case INCIDENT_LOW:
		case INCIDENT_MED:
		case INCIDENT_HIGH:
			return true;
		default:
			return false;
		}
	}
}
