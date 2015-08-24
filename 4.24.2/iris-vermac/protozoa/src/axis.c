/*
 * protozoa -- CCTV transcoder / mixer for PTZ
 * Copyright (C) 2007  Traffic Technologies
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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
#include <stdbool.h>
#include <string.h>
#include "ccreader.h"
#include "axis.h"

#define AXIS_MAX_SPEED (100)

static const char *default_speed = "100";
static const char *axis_header = "GET /axis-cgi/com/ptz.cgi?";
static const char *axis_header_config = "GET /axis-cgi/com/ptzconfig.cgi?";
static const char *axis_trailer = " HTTP/1.0";
static const char *axis_auth = "\r\nAuthorization: Basic ";
static const char *axis_ending = "\r\n\r\n";

/*
 * axis_add_to_buffer	Add a string to the transmit buffer.
 *
 * msg: char string to add to the buffer
 */
static void axis_add_to_buffer(struct ccwriter *wtr, const char *msg) {
	char *mess = ccwriter_append(wtr, strlen(msg));
	if(mess)
		memcpy(mess, msg, strlen(msg));
}

/*
 * axis_prepare_buffer	Prepare the transmit buffer for writing.
 *
 * somein: Flag to indicate data present in buffer
 * config: Flag to indicate a config request
 * return: new value of somein
 */
static bool axis_prepare_buffer(struct ccwriter *wtr, bool somein, bool config){
	if(somein)
		axis_add_to_buffer(wtr, "&");
	else {
		if(config)
			axis_add_to_buffer(wtr, axis_header_config);
		else
			axis_add_to_buffer(wtr, axis_header);
	}
	return true;
}

/*
 * axis_encode_speed	Encode pan/tilt speed.
 */
static int axis_encode_speed(int speed) {
	return ((speed * AXIS_MAX_SPEED) / (SPEED_MAX + 1)) + 1;
}

/*
 * encode_pan		Encode the pan speed.
 *
 * pkt: Packet to encode pan speed from
 * mess: string to append to
 */
static void encode_pan(struct ccpacket *pkt, char *mess) {
	char speed_str[8];

	int speed = axis_encode_speed(ccpacket_get_pan_speed(pkt));
	if (ccpacket_get_pan_mode(pkt) == CC_PAN_LEFT)
		speed = -speed;
	if (snprintf(speed_str, 8, "%d,", speed) > 0)
		strcat(mess, speed_str);
	else
		strcat(mess, "0,");
}

/*
 * encode_tilt		Encode the tilt speed.
 *
 * pkt: Packet to encode tilt speed from
 * mess: string to append to
 */
static void encode_tilt(struct ccpacket *pkt, char *mess) {
	char speed_str[8];

	int speed = axis_encode_speed(ccpacket_get_tilt_speed(pkt));
	if (ccpacket_get_tilt_mode(pkt) == CC_TILT_DOWN)
		speed = -speed;
	if (snprintf(speed_str, 8, "%d,", speed) > 0)
		strcat(mess, speed_str);
	else
		strcat(mess, "0,");
}

/*
 * encode_pan_tilt	Encode an axis pan/tilt request.
 *
 * pkt: Packet with pan/tilt values to encode.
 * somein: Flag to determine whether some data is already in the buffer
 * return: new somein value
 */
static bool encode_pan_tilt(struct ccwriter *wtr, struct ccpacket *pkt,
	bool somein)
{
	char mess[64];
	mess[0] = '\0';
	if (ccpacket_has_pan(pkt) || ccpacket_has_tilt(pkt)) {
		somein = axis_prepare_buffer(wtr, somein, false);
		strcat(mess, "continuouspantiltmove=");
		if (ccpacket_has_pan(pkt))
			encode_pan(pkt, mess);
		else
			strcat(mess, "0,");
		if (ccpacket_get_tilt_speed(pkt))
			encode_tilt(pkt, mess);
		else
			strcat(mess, "0");
		axis_add_to_buffer(wtr, mess);
	}
	return somein;
}

/*
 * encode_stop	Encode an axis pan/tilt stop request.
 *
 * somein: Flag to determine whether some data is already in the buffer
 * return: new somein value
 */
static bool encode_stop(struct ccwriter *wtr, bool somein) {
	somein = axis_prepare_buffer(wtr, somein, false);
	axis_add_to_buffer(wtr, "continuouspantiltmove=0,0");
	return somein;
}

/*
 * encode_focus		Encode an axis focus request.
 *
 * pkt: Packet with focus value to encode.
 * somein: Flag to determine whether some data is already in the buffer
 * return: new somein value
 */
static bool encode_focus(struct ccwriter *wtr, struct ccpacket *pkt,
	bool somein)
{
	char mess[32];
	strcpy(mess, "continuousfocusmove=");
	if (ccpacket_get_focus(pkt) == CC_FOCUS_NEAR) {
		somein = axis_prepare_buffer(wtr, somein, false);
		strcat(mess, default_speed);
	} else if (ccpacket_get_focus(pkt) == CC_FOCUS_FAR) {
		somein = axis_prepare_buffer(wtr, somein, false);
		strcat(mess, "-");
		strcat(mess, default_speed);
	} else {
		somein = axis_prepare_buffer(wtr, somein, false);
		strcat(mess, "0");
	}
	axis_add_to_buffer(wtr, mess);
	return somein;
}

/*
 * encode_zoom		Encode an axis zoom request.
 *
 * pkt: Packet with zoom value to encode.
 * somein: Flag to determine whether some data is already in the buffer
 * return: new somein value
 */
static bool encode_zoom(struct ccwriter *wtr, struct ccpacket *pkt,
	bool somein)
{
	char mess[32];
	strcpy(mess, "continuouszoommove=");
	if(ccpacket_get_zoom(pkt) == CC_ZOOM_IN) {
		somein = axis_prepare_buffer(wtr, somein, false);
		strcat(mess, default_speed);
	} else if(ccpacket_get_zoom(pkt) == CC_ZOOM_OUT) {
		somein = axis_prepare_buffer(wtr, somein, false);
		strcat(mess, "-");
		strcat(mess, default_speed);
	} else {
		somein = axis_prepare_buffer(wtr, somein, false);
		strcat(mess, "0");
	}
	axis_add_to_buffer(wtr, mess);
	return somein;
}

/*
 * encode_command	Encode an axis pan/tilt/zoom or focus request.
 *
 * pkt: Packet with zoom value to encode.
 * somein: Flag to determine whether some data is already in the buffer
 * return: new somein value
 */
static bool encode_command(struct ccwriter *wtr, struct ccpacket *pkt,
	bool somein)
{
	somein = encode_pan_tilt(wtr, pkt, somein);
	somein = encode_focus(wtr, pkt, somein);
	return encode_zoom(wtr, pkt, somein);
}

/*
 * encode_preset	Encode an axis preset request.
 *
 * pkt: Packet with preset value to encode.
 * somein: Flag to determine whether some data is already in the buffer
 * return: new somein value
 */
static bool encode_preset(struct ccwriter *wtr, struct ccpacket *pkt,
	bool somein)
{
	char num[16];
	char mess[32];

	enum cc_flags pm = ccpacket_get_preset_mode(pkt);
	if (pm == CC_PRESET_RECALL) {
		somein = axis_prepare_buffer(wtr, somein, false);
		strcpy(mess, "goto");
	} else if (pm == CC_PRESET_STORE) {
		somein = axis_prepare_buffer(wtr, somein, true);
		strcpy(mess, "set");
	} else if (pm == CC_PRESET_CLEAR) {
		somein = axis_prepare_buffer(wtr, somein, true);
		strcpy(mess, "remove");
	}
	strcat(mess, "serverpresetname=");
	sprintf(num, "Pos%d", ccpacket_get_preset_number(pkt));
	strcat(mess, num);
	axis_add_to_buffer(wtr, mess);
	return somein;
}

/*
 * axis_do_write	Encode a packet to the axis protocol.
 *
 * pkt: Packet to encode.
 * return: count of encoded packets
 */
unsigned int axis_do_write(struct ccwriter *wtr, struct ccpacket *pkt) {
	bool somein = false;
	if(!buffer_is_empty(&wtr->chn->txbuf)) {
		log_println(wtr->chn->log, "axis: dropping packet(s)");
		buffer_clear(&wtr->chn->txbuf);
	}
	if (ccpacket_get_preset_mode(pkt))
		somein = encode_preset(wtr, pkt, somein);
	else if(ccpacket_has_command(pkt))
		somein = encode_command(wtr, pkt, somein);
	else
		somein = encode_stop(wtr, somein);
	if(somein) {
		axis_add_to_buffer(wtr, axis_trailer);
		if(wtr->auth) {
			axis_add_to_buffer(wtr, axis_auth);
			axis_add_to_buffer(wtr, wtr->auth);
		}
		axis_add_to_buffer(wtr, axis_ending);
		return 1;
	} else
		return 0;
}
