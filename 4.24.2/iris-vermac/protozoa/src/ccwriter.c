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
#include <string.h>		/* for strcpy, strlen */
#include <strings.h>		/* for strcasecmp */
#include "ccwriter.h"
#include "stats.h"
#include "defer.h"
#include "axis.h"
#include "infinova.h"
#include "manchester.h"
#include "pelco_d.h"
#include "pelco_p.h"
#include "timeval.h"
#include "vicon.h"

/*
 * ccwriter_set_receivers	Set the number of receivers for the writer.
 */
static int ccwriter_set_receivers(struct ccwriter *wtr, const int n_rcv) {
	int i;

	wtr->deferred = malloc(sizeof(struct deferred_pkt) * n_rcv);
	if(wtr->deferred == NULL)
		return -1;
	for(i = 0; i < n_rcv; i++) {
		struct deferred_pkt *dpkt = wtr->deferred + i;
		deferred_pkt_init(dpkt);
		dpkt->writer = wtr;
	}
	wtr->n_rcv = n_rcv;
	return 0;
}

/*
 * ccwriter_set_protocol	Set protocol of the camera control writer.
 *
 * protocol: protocol name
 * return: 0 on success; -1 if protocol not found or allocation error
 */
static int ccwriter_set_protocol(struct ccwriter *wtr, const char *protocol) {
	if(strcasecmp(protocol, "manchester") == 0) {
		wtr->do_write = manchester_do_write;
		wtr->gaptime = MANCHESTER_GAPTIME;
		wtr->timeout = MANCHESTER_TIMEOUT;
		return ccwriter_set_receivers(wtr, MANCHESTER_MAX_ADDRESS);
	} else if(strcasecmp(protocol, "infinova_d") == 0) {
		wtr->do_write = infinova_d_do_write;
		wtr->gaptime = PELCO_D_GAPTIME;
		wtr->timeout = PELCO_D_TIMEOUT;
		return ccwriter_set_receivers(wtr, PELCO_D_MAX_ADDRESS);
	} else if(strcasecmp(protocol, "pelco_d") == 0) {
		wtr->do_write = pelco_d_do_write;
		wtr->gaptime = PELCO_D_GAPTIME;
		wtr->timeout = PELCO_D_TIMEOUT;
		return ccwriter_set_receivers(wtr, PELCO_D_MAX_ADDRESS);
	} else if(strcasecmp(protocol, "pelco_p") == 0) {
		wtr->do_write = pelco_p_do_write;
		wtr->gaptime = PELCO_P_GAPTIME;
		wtr->timeout = PELCO_P_TIMEOUT;
		return ccwriter_set_receivers(wtr, PELCO_P_MAX_ADDRESS);
	} else if(strcasecmp(protocol, "vicon") == 0) {
		wtr->do_write = vicon_do_write;
		wtr->gaptime = VICON_GAPTIME;
		wtr->timeout = VICON_TIMEOUT;
		return ccwriter_set_receivers(wtr, VICON_MAX_ADDRESS);
	} else if(strcasecmp(protocol, "axis") == 0) {
		wtr->do_write = axis_do_write;
		wtr->chn->flags |= FLAG_RESP_REQUIRED;
		wtr->gaptime = AXIS_GAPTIME;
		wtr->timeout = AXIS_TIMEOUT;
		return ccwriter_set_receivers(wtr, AXIS_MAX_ADDRESS);
	} else {
		log_println(wtr->chn->log, "Unknown protocol: %s", protocol);
		return -1;
	}
}

/*
 * ccwriter_init	Initialize a camera control writer.
 *
 * chn: channel to write camera control output
 * protocol: protocol name
 * auth: authentication token
 * return: pointer to struct ccwriter on success; NULL on error
 */
struct ccwriter *ccwriter_init(struct ccwriter *wtr, struct channel *chn,
	const char *protocol, const char *auth)
{
	wtr->chn = chn;
	wtr->deferred = NULL;
	wtr->n_rcv = 0;
	wtr->timeout = DEFAULT_TIMEOUT;
	wtr->auth = NULL;
	if(auth && strlen(auth) > 0) {
		wtr->auth = malloc(strlen(auth) + 1);
		if(wtr->auth == NULL)
			return NULL;
		else
			strcpy(wtr->auth, auth);
	}
	if(ccwriter_set_protocol(wtr, protocol) < 0)
		return NULL;
	else
		return wtr;
}

/*
 * ccwriter_destroy	Destroy a camera control writer.
 */
void ccwriter_destroy(struct ccwriter *wtr) {
	int i;
	free(wtr->auth);
	for(i = 0; i < wtr->n_rcv; i++) {
		struct deferred_pkt *dpkt = wtr->deferred + i;
		deferred_pkt_destroy(dpkt);
	}
	free(wtr->deferred);
	memset(wtr, 0, sizeof(struct ccwriter));
}

/*
 * ccwriter_append	Append data to the camera control writer.
 *
 * n_bytes: number of bytes to append
 * return: borrowed pointer to appended data
 */
void *ccwriter_append(struct ccwriter *wtr, size_t n_bytes) {
	void *mess = buffer_append(&wtr->chn->txbuf, n_bytes);
	if(mess) {
		memset(mess, 0, n_bytes);
		return mess;
	} else {
		log_println(wtr->chn->log,
			"ccwriter_append (%s): output buffer full",
			wtr->chn->name);
		return NULL;
	}
}

/*
 * ccwriter_too_soon	Test if the current packet is too soon after the
 * 			previous packet.
 */
static bool ccwriter_too_soon(struct ccwriter *wtr,
	const struct deferred_pkt *dpkt)
{
	return time_since(&dpkt->sent) < wtr->gaptime;
}

/*
 * ccwriter_check_deferred	Check if a packet should be deferred for later.
 */
static void ccwriter_check_deferred(struct ccwriter *wtr, struct ccpacket *pkt,
	struct deferred_pkt *dpkt)
{
	timeval_set_now(&dpkt->sent);
	/* If the packet expires after the protocol timeout, defer it to
	 * be sent again after the "defer" interval passes. */
	if(ccpacket_is_stop(pkt)) {
		if(dpkt->n_cnt < 1) {
			defer_packet(wtr->defer, dpkt, pkt, wtr->gaptime);
			dpkt->n_cnt++;
			return;
		} else
			dpkt->n_cnt = 0;
	} else if(ccpacket_is_expired(pkt, wtr->timeout)) {
		defer_packet(wtr->defer, dpkt, pkt, wtr->timeout);
		return;
	}
	defer_packet(wtr->defer, dpkt, NULL, 0);
}

/*
 * ccwriter_do_write_	Process one packet for the writer.
 */
static int ccwriter_do_write_(struct ccwriter *wtr, struct ccpacket *pkt) {
	unsigned int c;
	struct deferred_pkt *dpkt =
		wtr->deferred + ccpacket_get_receiver(pkt) - 1;

	/* If it is too soon after the previous packet, defer until later */
	if(ccwriter_too_soon(wtr, dpkt)) {
		defer_packet(wtr->defer, dpkt, pkt, wtr->gaptime);
		return 0;
	}
	c = wtr->do_write(wtr, pkt);
	if(c > 0) {
		ptz_stats_count(pkt, CC_DOM_OUT);
		ccwriter_check_deferred(wtr, pkt, dpkt);
		if(wtr->chn->log->packet)
			ccpacket_log(pkt, wtr->chn->log, "OUT", wtr->chn->name);
	}
	return c;
}

/*
 * ccwriter_do_write	Process one packet for the writer.
 */
int ccwriter_do_write(struct ccwriter *wtr, struct ccpacket *pkt) {
	int receiver = ccpacket_get_receiver(pkt);
	if(receiver > 0 && receiver <= wtr->n_rcv)
		return ccwriter_do_write_(wtr, pkt);
	else
		return 0;
}
