/*
 * protozoa -- CCTV transcoder / mixer for PTZ
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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
#include "timer.h"
#include "timeval.h"
#include "defer.h"
#include "ccwriter.h"

/*
 * compare_pkts		Compare two packets for sorting them by time.
 */
static cl_compare_t compare_pkts(const void *value0, const void *value1) {
	const struct deferred_pkt *dpkt0 = value0;
	const struct deferred_pkt *dpkt1 = value1;

	return timeval_compare(&dpkt0->tv, &dpkt1->tv);
}

void deferred_pkt_init(struct deferred_pkt *dpkt) {
	dpkt->tv.tv_sec = 0;
	dpkt->tv.tv_usec = 0;
	timeval_set_now(&dpkt->sent);
	dpkt->packet = ccpacket_create();
	dpkt->writer = NULL;
	dpkt->n_cnt = 0;
}

void deferred_pkt_destroy(struct deferred_pkt *dpkt) {
	free(dpkt->packet);
}

/*
 * defer_init		Initialize the deferred packet engine.
 */
struct defer *defer_init(struct defer *dfr) {
	if(cl_rbtree_init(&dfr->tree, CL_DUP_ALLOW, compare_pkts))
		return dfr;
	else
		return NULL;
}

/*
 * defer_destroy	Destroy the deferred packet engine.
 */
void defer_destroy(struct defer *dfr) {
	cl_rbtree_clear(&dfr->tree, NULL, NULL);
}

/*
 * defer_rearm		Rearm the timer for the next deferred packet.
 */
static int defer_rearm(struct defer *dfr) {
	struct deferred_pkt *dpkt = cl_rbtree_peek(&dfr->tree);
	if(dpkt)
		return timer_arm(time_from_now(&dpkt->tv));
	else
		return timer_disarm();
}

/*
 * defer_packet		Defer one packet to be sent at a later time.
 */
int defer_packet(struct defer *dfr, struct deferred_pkt *dpkt,
	struct ccpacket *pkt, unsigned int ms)
{
	cl_rbtree_remove(&dfr->tree, dpkt);
	if(pkt) {
		timeval_set_now(&dpkt->tv);
		timeval_adjust(&dpkt->tv, ms);
		ccpacket_copy(dpkt->packet, pkt);
		if(cl_rbtree_add(&dfr->tree, dpkt) == NULL)
			return -1;
	}
	return defer_rearm(dfr);
}

/*
 * defer_packet_now		Send a deferred packet right now.
 */
static void defer_packet_now(struct defer *dfr, struct deferred_pkt *dpkt) {
	cl_rbtree_remove(&dfr->tree, dpkt);
	timeval_set_now(&dpkt->tv);
	timeval_adjust(&dpkt->tv, dpkt->writer->timeout);
	ccwriter_do_write(dpkt->writer, dpkt->packet);
}

/*
 * defer_next		Process the next deferred packet
 */
int defer_next(struct defer *dfr) {
	struct deferred_pkt *dpkt;

	if(timer_read() < 0)
		return -1;

	dpkt = cl_rbtree_peek(&dfr->tree);
	if(dpkt)
		defer_packet_now(dfr, dpkt);
	return defer_rearm(dfr);
}

/*
 * defer_get_fd		Get the file descriptor for deferred events.
 */
int defer_get_fd(struct defer *dfr) {
	return timer_get_fd();
}
