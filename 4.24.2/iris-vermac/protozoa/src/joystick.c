/*
 * protozoa -- CCTV transcoder / mixer for PTZ
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
#include <stdint.h>	/* for uint8_t */
#include "ccreader.h"
#include "joystick.h"

/*
 * Linux joystick input event driver.
 *
 * Event records are 8 octets long:
 *
 *	0-3	timestamp
 *	4-5	value (-32767 to 32767)
 *	6	event type {0x01: button, 0x02: joystick, 0x80: initial value}
 *	7	number (button [0 - N] or axis {0: x, 1: y, 2: z})
 */
#define JEVENT_OCTETS	(8)

#define JEVENT_BUTTON	(0x01)
#define JEVENT_AXIS	(0x02)
#define JEVENT_INITIAL	(0x80)

#define JAXIS_PAN	(0)
#define JAXIS_TILT	(1)
#define JAXIS_ZOOM	(2)

#define JSPEED_MAX	(32767)

#define JBUTTON_FOCUS_NEAR	(0)
#define JBUTTON_FOCUS_FAR	(1)
#define JBUTTON_IRIS_CLOSE	(2)
#define JBUTTON_IRIS_OPEN	(3)
#define JBUTTON_WIPER		(4)
#define JBUTTON_CAMERA		(5)
#define JBUTTON_PRESET_1	(6)
#define JBUTTON_PRESET_2	(7)
#define JBUTTON_PRESET_3	(8)
#define JBUTTON_PRESET_4	(9)
#define JBUTTON_PREVIOUS	(10)
#define JBUTTON_NEXT		(11)

/*
 * decode_speed		Decode a pan/tilt speed.
 */
static inline int decode_speed(uint8_t *mess) {
	return *(short *)(mess + 4);
}

/*
 * decode_pressed	Decode a button pressed code.
 */
static inline bool decode_pressed(uint8_t *mess) {
	return *(short *)(mess + 4) != 0;
}

/*
 * remap_int		Remap an integer from input -> output range.
 */
static inline int remap_int(int value, int irange, int orange) {
	int v = abs(value) * orange;
	return v / irange;
}

/*
 * remap_speed		Remap pan/tilt speed value.
 */
static inline int remap_speed(int value) {
	return remap_int(value, JSPEED_MAX, SPEED_MAX);
}

/** Decode pan mode.
 */
static enum cc_flags decode_pan_mode(short speed) {
	return speed <= 0 ? CC_PAN_LEFT : CC_PAN_RIGHT;
}

/** Decode tilt mode.
 */
static enum cc_flags decode_tilt_mode(short speed) {
	return speed < 0 ? CC_TILT_UP : CC_TILT_DOWN;
}

/*
 * decode_pan_tilt_zoom		Decode a pan/tilt/zoom event.
 */
static inline bool decode_pan_tilt_zoom(struct ccpacket *pkt, uint8_t *mess) {
	uint8_t number = mess[7];
	short speed = decode_speed(mess);

	switch(number) {
		case JAXIS_PAN:
			ccpacket_set_pan(pkt, decode_pan_mode(speed),
				remap_speed(speed));
			break;
		case JAXIS_TILT:
			ccpacket_set_tilt(pkt, decode_tilt_mode(speed),
				remap_speed(speed));
			break;
		case JAXIS_ZOOM:
			if(speed < 0)
				ccpacket_set_zoom(pkt, CC_ZOOM_OUT);
			else if(speed > 0)
				ccpacket_set_zoom(pkt, CC_ZOOM_IN);
			else
				ccpacket_set_zoom(pkt, 0);
			break;
	}
	ccpacket_set_preset(pkt, 0, 0);
	return true;
}

/*
 * moved_since_pressed	Test if the joystick has moved since a preset button
 *			was pressed.
 */
static bool moved_since_pressed(struct ccpacket *pkt) {
	return ccpacket_get_preset_mode(pkt) != CC_PRESET_RECALL;
}

/*
 * decode_button	Decode a button pressed event.
 */
static inline bool decode_button(struct ccreader *rdr, uint8_t *mess) {
	struct ccpacket *pkt = rdr->packet;
	uint8_t number = mess[7];
	bool pressed = decode_pressed(mess);
	bool moved = moved_since_pressed(pkt);

	switch(number) {
		case JBUTTON_FOCUS_NEAR:
			if(pressed)
				ccpacket_set_focus(pkt, CC_FOCUS_NEAR);
			else
				ccpacket_set_focus(pkt, 0);
			return true;
		case JBUTTON_FOCUS_FAR:
			if(pressed)
				ccpacket_set_focus(pkt, CC_FOCUS_FAR);
			else
				ccpacket_set_focus(pkt, 0);
			return true;
		case JBUTTON_IRIS_CLOSE:
			if(pressed)
				ccpacket_set_iris(pkt, CC_IRIS_CLOSE);
			else
				ccpacket_set_iris(pkt, 0);
			return true;
		case JBUTTON_IRIS_OPEN:
			if(pressed)
				ccpacket_set_iris(pkt, CC_IRIS_OPEN);
			else
				ccpacket_set_iris(pkt, 0);
			return true;
		case JBUTTON_WIPER:
			if(pressed)
				ccpacket_set_wiper(pkt, CC_WIPER_ON);
			else
				ccpacket_set_wiper(pkt, 0);
			return true;
		case JBUTTON_CAMERA:
			if(pressed)
				ccpacket_set_camera(pkt, CC_CAMERA_ON);
			else
				ccpacket_set_camera(pkt, 0);
			return true;
		case JBUTTON_PRESET_1:
			if(pressed)
				ccpacket_set_preset(pkt, CC_PRESET_RECALL, 1);
			else if(moved)
				ccpacket_set_preset(pkt, CC_PRESET_STORE, 1);
			else
				break;
			return true;
		case JBUTTON_PRESET_2:
			if(pressed)
				ccpacket_set_preset(pkt, CC_PRESET_RECALL, 2);
			else if(moved)
				ccpacket_set_preset(pkt, CC_PRESET_STORE, 2);
			else
				break;
			return true;
		case JBUTTON_PRESET_3:
			if(pressed)
				ccpacket_set_preset(pkt, CC_PRESET_RECALL, 3);
			else if(moved)
				ccpacket_set_preset(pkt, CC_PRESET_STORE, 3);
			else
				break;
			return true;
		case JBUTTON_PRESET_4:
			if(pressed)
				ccpacket_set_preset(pkt, CC_PRESET_RECALL, 4);
			else if(moved)
				ccpacket_set_preset(pkt, CC_PRESET_STORE, 4);
			else
				break;
			return true;
		case JBUTTON_PREVIOUS:
			if(pressed)
				ccreader_previous_camera(rdr);
			break;
		case JBUTTON_NEXT:
			if(pressed)
				ccreader_next_camera(rdr);
			break;
	}
	ccpacket_set_preset(pkt, 0, 0);
	return false;
}

/*
 * joystick_decode_event	Decode a joystick event.
 */
static inline bool joystick_decode_event(struct ccreader *rdr, uint8_t *mess) {
	uint8_t ev_type = mess[6];

	if(ev_type & JEVENT_AXIS)
		return decode_pan_tilt_zoom(rdr->packet, mess);
	else if((ev_type & JEVENT_BUTTON) && !(ev_type & JEVENT_INITIAL))
		return decode_button(rdr, mess);
	else
		return false;
}

/*
 * joystick_read_message	Read a joystick event message.
 */
static inline bool joystick_read_message(struct ccreader *rdr,
	struct buffer *rxbuf)
{
	uint8_t *mess = buffer_output(rxbuf);
	bool m = joystick_decode_event(rdr, mess);
	buffer_consume(rxbuf, JEVENT_OCTETS);
	return m;
}

/*
 * joystick_do_read		Read joystick events.
 */
void joystick_do_read(struct ccreader *rdr, struct buffer *rxbuf) {
	int c = 0;
	while(buffer_available(rxbuf) >= JEVENT_OCTETS)
		c += joystick_read_message(rdr, rxbuf);
	if(c)
		ccreader_process_packet_no_clear(rdr);
	ccpacket_set_preset(rdr->packet, 0, 0);
}
