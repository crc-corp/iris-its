/*
 * protozoa -- CCTV transcoder / mixer for PTZ
 * Copyright (C) 2006-2014  Minnesota Department of Transportation
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
#include <string.h>	/* for memset */
#include <stdint.h>
#include "stats.h"

/** Packet classifications */
enum pkt_class {
	PC_PAN,
	PC_TILT,
	PC_ZOOM,
	PC_FOCUS,
	PC_IRIS,
	PC_WIPER,
	PC_PRESET,
	PC_TOTAL,
};

/** Names of packet classes */
static const char *pc_name[] = {
	"pan",
	"tilt",
	"zoom",
	"focus",
	"iris",
	"wiper",
	"preset",
	"total",
};

/** Message logger */
static struct log *log;

/** Count of packets */
static uint64_t n_pkts[PC_TOTAL + 1][CC_DOM_OUT + 1];

/** Initialize packet stats.
 *
 * @param log		Message logger
 */
void ptz_stats_init(struct log *lg) {
	memset(&n_pkts, 0, sizeof(n_pkts));
	log = lg;
}

/** Print packet statistics.
 *
 * @param pc		Packet class
 */
static void ptz_stats_print(int pc) {
	if (n_pkts[pc][CC_DOM_IN] || n_pkts[pc][CC_DOM_OUT]) {
		uint64_t n_in = n_pkts[pc][CC_DOM_IN];
		uint64_t n_out = n_pkts[pc][CC_DOM_OUT];
		float prc_in = 100.0f * n_in / n_pkts[PC_TOTAL][CC_DOM_IN];
		float prc_out = 100.0f * n_out / n_pkts[PC_TOTAL][CC_DOM_OUT];
		log_println(log, "%8s: %10lld  %6.2f%% %10lld  %6.2f%%",
			pc_name[pc], n_in, prc_in, n_out, prc_out);
	}
}

/**
 * Display all packet statistics.
 */
static void ptz_stats_display(void) {
	int i;
	log_println(log, "%8s  %10s %8s %10s %8s", "Class", "Count IN",
		"IN \%", "Count OUT", "OUT \%");
	for (i = 0; i <= PC_TOTAL; i++)
		ptz_stats_print(i);
}

/** Count one packet in the packet stats.
 *
 * @param pkt		Packet to count
 * @param d		Packet domain: CC_DOM_IN or CC_DOM_OUT
 */
void ptz_stats_count(const struct ccpacket *pkt, enum domain d) {
	if (log) {
		if (ccpacket_has_pan(pkt))
			n_pkts[PC_PAN][d]++;
		if (ccpacket_has_tilt(pkt))
			n_pkts[PC_TILT][d]++;
		if (ccpacket_get_zoom(pkt))
			n_pkts[PC_ZOOM][d]++;
		if (ccpacket_get_focus(pkt))
			n_pkts[PC_FOCUS][d]++;
		if (ccpacket_get_iris(pkt))
			n_pkts[PC_IRIS][d]++;
		if (ccpacket_get_wiper(pkt))
			n_pkts[PC_WIPER][d]++;
		if (ccpacket_get_preset_mode(pkt))
			n_pkts[PC_PRESET][d]++;
		n_pkts[PC_TOTAL][d]++;
		if ((n_pkts[PC_TOTAL][d] % 100) == 0)
			ptz_stats_display();
	}
}
