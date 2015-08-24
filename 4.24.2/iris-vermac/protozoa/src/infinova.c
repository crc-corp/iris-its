/*
 * protozoa -- CCTV transcoder / mixer for PTZ
 * Copyright (C) 2011-2012  Minnesota Department of Transportation
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
#include <stdint.h>	/* for uint8_t */
#include "pelco_d.h"

#define HEADER_SZ (12)
#define AUTH_SZ (64)
#define MSG_ID_AUTH (0x01)
#define MSG_ID_PTZ (0x13)

/*
 * infinova_header	Write out an infinova header.
 */
static int infinova_header(struct ccwriter *wtr, uint8_t msg_id,
	unsigned int n_bytes)
{
	uint8_t *mess = ccwriter_append(wtr, HEADER_SZ);
	if(mess) {
		mess[0] = 'I';		/* magic numbers */
		mess[1] = 'N';
		mess[2] = 'F';
		mess[3] = msg_id;
		if(msg_id == MSG_ID_AUTH) {
			mess[5] = 1;	/* don't know what these do ... */
			mess[7] = 1;
		}
		mess[11] = n_bytes;
		return 1;
	} else
		return 0;
}

/*
 * infinova_authenticate	Authenticate an infinova socket connection.
 */
static void infinova_authenticate(struct ccwriter *wtr) {
	if(infinova_header(wtr, MSG_ID_AUTH, AUTH_SZ)) {
		/* We don't know why, but we need two extra bytes here */
		ccwriter_append(wtr, AUTH_SZ + 2);
		/* FIXME: fill in user name and password here??? */
	}
}

/*
 * infinova_d_header		Write a header for a pelco D PTZ packet.
 */
static int infinova_d_header(struct ccwriter *wtr) {
	if(infinova_header(wtr, MSG_ID_PTZ, HEADER_SZ + PELCO_D_SZ)) {
		/* PTZ packets need an extra header */
		uint8_t *mess = ccwriter_append(wtr, HEADER_SZ);
		if(mess) {
			mess[0] = 1;	/* don't know what this means */
			mess[7] = PELCO_D_SZ;
			return 1;
		}
	}
	return 0;
}

/*
 * infinova_d_do_write	Write a packet in the infinova_d protocol.
 */
unsigned int infinova_d_do_write(struct ccwriter *wtr, struct ccpacket *pkt) {
	/* We need to authenticate if the channel is currently closed.
	 * Camera will close the socket after 90 seconds of inactivity. */
	if(!channel_is_open(wtr->chn))
		infinova_authenticate(wtr);
	return pelco_d_do_write_cb(wtr, pkt, infinova_d_header);
}
