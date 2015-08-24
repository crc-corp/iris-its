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
#include <strings.h>
#include "ccreader.h"
#include "stats.h"
#include "joystick.h"
#include "manchester.h"
#include "pelco_d.h"
#include "pelco_p.h"
#include "vicon.h"

/*
 * ccreader_set_timeout		Set the timeout for the reader's protocol.
 */
static void ccreader_set_timeout(struct ccreader *rdr, unsigned int timeout) {
	rdr->timeout = timeout;
}

/*
 * ccreader_set_protocol	Set protocol of the camera control reader.
 *
 * protocol: protocol name
 * return: 0 on success; -1 if protocol not found
 */
static int ccreader_set_protocol(struct ccreader *rdr, const char *protocol) {
	if(strcasecmp(protocol, "joystick") == 0) {
		rdr->do_read = joystick_do_read;
		ccreader_set_timeout(rdr, JOYSTICK_TIMEOUT);
	} else if(strcasecmp(protocol, "manchester") == 0) {
		rdr->do_read = manchester_do_read;
		ccreader_set_timeout(rdr, MANCHESTER_TIMEOUT);
	} else if(strcasecmp(protocol, "pelco_d") == 0) {
		rdr->do_read = pelco_d_do_read;
		ccreader_set_timeout(rdr, PELCO_D_TIMEOUT);
	} else if(strcasecmp(protocol, "pelco_p") == 0) {
		rdr->do_read = pelco_p_do_read;
		ccreader_set_timeout(rdr, PELCO_D_TIMEOUT);
	} else if(strcasecmp(protocol, "pelco_p7") == 0) {
		rdr->do_read = pelco_p_do_read;
		rdr->flags = PT_DEADZONE;
		ccreader_set_timeout(rdr, PELCO_P_TIMEOUT);
	} else if(strcasecmp(protocol, "vicon") == 0) {
		rdr->do_read = vicon_do_read;
		ccreader_set_timeout(rdr, VICON_TIMEOUT);
	} else {
		log_println(rdr->log, "Unknown protocol: %s", protocol);
		return -1;
	}
	return 0;
}

static struct ccnode *ccnode_init(struct ccnode *node) {
	node->writer = NULL;
	node->range_first = 1;
	node->range_last = 1024;
	node->shift = 0;
	node->next = NULL;
	return node;
}

static void ccnode_set_range(struct ccnode *node, const char *range) {
	int first, last;

	if(sscanf(range, "%d%d", &first, &last) == 2) {
		node->range_first = first;
		node->range_last = -last;
	} else if(sscanf(range, "%d", &first) == 1) {
		node->range_first = first;
		node->range_last = first;
	}
}  

static void ccnode_set_shift(struct ccnode *node, const char *shift) {
	sscanf(shift, "%d", &node->shift);
}

void ccreader_previous_camera(struct ccreader *rdr) {
	int receiver = ccpacket_get_receiver(rdr->packet);
	if(receiver > 0)
		ccpacket_set_receiver(rdr->packet, receiver - 1);
}

void ccreader_next_camera(struct ccreader *rdr) {
	int receiver = ccpacket_get_receiver(rdr->packet);
	if(receiver < 1024)
		ccpacket_set_receiver(rdr->packet, receiver + 1);
}

/*
 * ccreader_init	Initialize a camera control reader.
 *
 * name: name of reader
 * log: message logger
 * protocol: protocol name
 * return: pointer to struct ccreader on success; NULL on failure
 */
struct ccreader *ccreader_init(struct ccreader *rdr, const char *name,
	struct log *log, const char *protocol)
{
	rdr->packet = ccpacket_create();
	rdr->timeout = DEFAULT_TIMEOUT;
	rdr->flags = 0;
	rdr->head = NULL;
	rdr->name = name;
	rdr->log = log;
	if(ccreader_set_protocol(rdr, protocol) < 0)
		return NULL;
	else
		return rdr;
}

/** Destroy a ccreader.
 */
void ccreader_destroy(struct ccreader *rdr) {
	if (rdr->packet) {
		ccpacket_destroy(rdr->packet);
		rdr->packet = NULL;
	}
}

/*
 * ccreader_add_writer		Add a writer to the camera control reader.
 *
 * wtr: camera control writer to link with the reader
 * range: range of receiver addresses
 * shift: receiver address shift offset
 */
void ccreader_add_writer(struct ccreader *rdr, struct ccnode *node,
	struct ccwriter *wtr, const char *range, const char *shift)
{
	ccnode_init(node);
	node->writer = wtr;
	ccnode_set_range(node, range);
	ccnode_set_shift(node, shift);
	node->next = rdr->head;
	rdr->head = node;
	ccpacket_set_receiver(rdr->packet, node->range_first);
}

/*
 * ccnode_get_receiver	Get receiver address adjusted for the node.
 *
 * receiver: input receiver address
 * return: output receiver address; 0 indicates drop packet
 */
static int ccnode_get_receiver(const struct ccnode *node, int receiver) {
	if(receiver < node->range_first || receiver > node->range_last)
		return 0;	/* Ignore if receiver address is out of range */
	receiver += node->shift;
	if(receiver < 0)
		return 0;
	else
		return receiver;
}

/*
 * ccreader_do_writers		Write a packet to all linked writers.
 *
 * return: number of writers that wrote the packet
 */
static unsigned int ccreader_do_writers(struct ccreader *rdr) {
	unsigned int res = 0;
	struct ccpacket *pkt = rdr->packet;
	const int receiver = ccpacket_get_receiver(pkt);  /* "true" receiver */
	struct ccnode *node = rdr->head;
	while(node) {
		int r = ccnode_get_receiver(node, receiver);
		if(r) {
			ccpacket_set_receiver(pkt, r);
			res += ccwriter_do_write(node->writer, pkt);
		}
		node = node->next;
	}
	ccpacket_set_receiver(pkt, receiver);	/* restore "true" receiver */
	return res;
}

/*
 * ccreader_process_packet_no_clear	Process a packet (but don't clear it)
 *					from the camera control reader.
 *
 * return: number of writers that wrote the packet
 */
unsigned int ccreader_process_packet_no_clear(struct ccreader *rdr) {
	struct ccpacket *pkt = rdr->packet;
	if (rdr->log->packet)
		ccpacket_log(pkt, rdr->log, "IN", rdr->name);
	ptz_stats_count(pkt, CC_DOM_IN);
	ccpacket_set_timeout(pkt, rdr->timeout);
	return ccreader_do_writers(rdr);
}

/*
 * ccreader_process_packet	Process and clear a packet from the camera
 *				control reader.
 *
 * return: number of writers that wrote the packet
 */
unsigned int ccreader_process_packet(struct ccreader *rdr) {
	unsigned int res = ccreader_process_packet_no_clear(rdr);
	ccpacket_clear(rdr->packet);
	return res;
}
