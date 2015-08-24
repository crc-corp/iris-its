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
#include <stdbool.h>
#include <stdint.h>	/* for uint8_t */
#include "ccreader.h"
#include "vicon.h"
#include "bitarray.h"

#define FLAG (0x80)
#define SIZE_STATUS (2)
#define SIZE_COMMAND (6)
#define SIZE_EXTENDED (10)

enum vicon_special_presets {
	VICON_PRESET_MENU_OPEN = 94,
};

/*
 * Packet bit positions for PTZ functions.
 */
enum vicon_bit_t {
	BIT_COMMAND = 12,
	BIT_ACK_ALARM = 13,
	BIT_EXTENDED = 14,
	BIT_AUTO_IRIS = 17,
	BIT_AUTO_PAN = 18,
	BIT_TILT_DOWN = 19,
	BIT_TILT_UP = 20,
	BIT_PAN_RIGHT = 21,
	BIT_PAN_LEFT = 22,
	BIT_LENS_SPEED = 24,
	BIT_IRIS_CLOSE = 25,
	BIT_IRIS_OPEN = 26,
	BIT_FOCUS_NEAR = 27,
	BIT_FOCUS_FAR = 28,
	BIT_ZOOM_IN = 29,
	BIT_ZOOM_OUT = 30,
	BIT_AUX_6 = 33,
	BIT_AUX_5 = 34,
	BIT_AUX_4 = 35,
	BIT_AUX_3 = 36,
	BIT_AUX_2 = 37,
	BIT_AUX_1 = 38,
	BIT_RECALL = 45,
	BIT_STORE = 46,
	BIT_STAT_V15UVS = 48,
	BIT_EX_STORE = 48,
	BIT_EX_STATUS = 49,
	BIT_EX_REQUEST = 52,
	BIT_STAT_SECTOR = 56,
	BIT_STAT_PRESET = 57,
	BIT_STAT_AUX_SET_2 = 58,
};

/**
 * Decode the receiver address.
 *
 * @param pkt		Packet.
 * @param mess		Message buffer.
 */
static inline void decode_receiver(struct ccpacket *pkt, uint8_t *mess) {
	int receiver = ((mess[0] & 0x0f) << 4) | (mess[1] & 0x0f);
	ccpacket_set_receiver(pkt, receiver);
}

/*
 * is_command		Test if the message is a command.
 */
static inline bool is_command(uint8_t *mess) {
	return bit_is_set(mess, BIT_COMMAND);
}

/*
 * is_extended_command	Test if the message is an extend command.
 */
static inline bool is_extended_command(uint8_t *mess) {
	return bit_is_set(mess, BIT_COMMAND) && bit_is_set(mess, BIT_EXTENDED);
}

/*
 * decode_pan		Decode the pan speed (and command).
 */
static inline void decode_pan(struct ccpacket *pkt, uint8_t *mess) {
	if (bit_is_set(mess, BIT_PAN_RIGHT))
		ccpacket_set_pan(pkt, CC_PAN_RIGHT, SPEED_MAX);
	else if (bit_is_set(mess, BIT_PAN_LEFT))
		ccpacket_set_pan(pkt, CC_PAN_LEFT, SPEED_MAX);
	else
		ccpacket_set_pan(pkt, CC_PAN_LEFT, 0);
}

/*
 * decode_tilt		Decode the tilt speed (and command).
 */
static inline void decode_tilt(struct ccpacket *pkt, uint8_t *mess) {
	if (bit_is_set(mess, BIT_TILT_UP))
		ccpacket_set_tilt(pkt, CC_TILT_UP, SPEED_MAX);
	else if (bit_is_set(mess, BIT_TILT_DOWN))
		ccpacket_set_tilt(pkt, CC_TILT_DOWN, SPEED_MAX);
	else
		ccpacket_set_tilt(pkt, CC_TILT_DOWN, 0);
}

/*
 * decode_lens		Decode any lens commands.
 */
static inline void decode_lens(struct ccpacket *pkt, uint8_t *mess) {
	if(bit_is_set(mess, BIT_IRIS_OPEN))
		ccpacket_set_iris(pkt, CC_IRIS_OPEN);
	else if(bit_is_set(mess, BIT_IRIS_CLOSE))
		ccpacket_set_iris(pkt, CC_IRIS_CLOSE);
	if(bit_is_set(mess, BIT_FOCUS_NEAR))
		ccpacket_set_focus(pkt, CC_FOCUS_NEAR);
	else if(bit_is_set(mess, BIT_FOCUS_FAR))
		ccpacket_set_focus(pkt, CC_FOCUS_FAR);
	if(bit_is_set(mess, BIT_ZOOM_IN))
		ccpacket_set_zoom(pkt, CC_ZOOM_IN);
	else if(bit_is_set(mess, BIT_ZOOM_OUT))
		ccpacket_set_zoom(pkt, CC_ZOOM_OUT);
}

/*
 * decode_toggles	Decode toggle functions.
 */
static inline void decode_toggles(struct ccpacket *pkt, uint8_t *mess) {
	if(bit_is_set(mess, BIT_ACK_ALARM))
		ccpacket_set_ack(pkt, CC_ACK_ALARM);
	if(bit_is_set(mess, BIT_AUTO_IRIS))
		ccpacket_set_iris(pkt, CC_IRIS_AUTO);
	if(bit_is_set(mess, BIT_AUTO_PAN))
		ccpacket_set_pan(pkt, CC_PAN_AUTO, 0);
	if(bit_is_set(mess, BIT_LENS_SPEED))
		ccpacket_set_lens(pkt, CC_LENS_SPEED);
}

/** Decode auxiliary functions.
 */
static void decode_aux(struct ccpacket *pkt, uint8_t *mess) {
	if (bit_is_set(mess, BIT_AUX_6))
		ccpacket_set_wiper(pkt, CC_WIPER_ON);
}

/*
 * decode_preset	Decode preset functions.
 */
static inline void decode_preset(struct ccpacket *pkt, uint8_t *mess) {
	int p_num = mess[5] & 0x0f;
	if(bit_is_set(mess, BIT_RECALL))
		ccpacket_set_preset(pkt, CC_PRESET_RECALL, p_num);
	else if(bit_is_set(mess, BIT_STORE))
		ccpacket_set_preset(pkt, CC_PRESET_STORE, p_num);
}

/*
 * decode_ex_speed	Decode extended speed functions.
 */
static inline void decode_ex_speed(struct ccpacket *pkt, uint8_t *mess) {
	int pan = ((mess[6] & 0x0f) << 7) | (mess[7] & 0x7f);
	int tilt = ((mess[8] & 0x0f) << 7) | (mess[9] & 0x7f);
	ccpacket_set_pan_speed(pkt, pan);
	ccpacket_set_tilt_speed(pkt, tilt);
}

/** Decode extended status functions.
 */
static void decode_ex_status(struct ccpacket *pkt, uint8_t *mess) {
	/* Ignore status messages */
}

/*
 * decode_ex_preset	Decode extended preset functions.
 */
static inline void decode_ex_preset(struct ccpacket *pkt, uint8_t *mess) {
	int p_num = mess[7] & 0x7f;
	int pan = mess[8] & 0x7f;
	int tilt = mess[9] & 0x7f;
	if(bit_is_set(mess, BIT_EX_STORE))
		ccpacket_set_preset(pkt, CC_PRESET_STORE, p_num);
	else
		ccpacket_set_preset(pkt, CC_PRESET_RECALL, p_num);
	ccpacket_set_pan_speed(pkt, pan);
	ccpacket_set_tilt_speed(pkt, tilt);
}

/** Decode an extended packet.
 */
static enum decode_t vicon_decode_extended(struct ccreader *rdr,
	uint8_t *mess, struct buffer *rxbuf)
{
	if (buffer_available(rxbuf) < SIZE_EXTENDED)
		return DECODE_DONE;
	decode_receiver(rdr->packet, mess);
	decode_pan(rdr->packet, mess);
	decode_tilt(rdr->packet, mess);
	decode_lens(rdr->packet, mess);
	decode_toggles(rdr->packet, mess);
	decode_aux(rdr->packet, mess);
	decode_preset(rdr->packet, mess);
	if (bit_is_set(mess, BIT_EX_REQUEST)) {
		if (bit_is_set(mess, BIT_EX_STATUS))
			decode_ex_status(rdr->packet, mess);
		else
			decode_ex_preset(rdr->packet, mess);
	} else
		decode_ex_speed(rdr->packet, mess);
	buffer_consume(rxbuf, SIZE_EXTENDED);
	ccreader_process_packet(rdr);
	return DECODE_MORE;
}

/*
 * vicon_decode_command		Decode a command packet.
 */
static enum decode_t vicon_decode_command(struct ccreader *rdr,
	uint8_t *mess, struct buffer *rxbuf)
{
	if (buffer_available(rxbuf) < SIZE_COMMAND)
		return DECODE_DONE;
	decode_receiver(rdr->packet, mess);
	decode_pan(rdr->packet, mess);
	decode_tilt(rdr->packet, mess);
	decode_lens(rdr->packet, mess);
	decode_toggles(rdr->packet, mess);
	decode_aux(rdr->packet, mess);
	decode_preset(rdr->packet, mess);
	buffer_consume(rxbuf, SIZE_COMMAND);
	ccreader_process_packet(rdr);
	return DECODE_MORE;
}

/** Decode a status packet.
 */
static enum decode_t vicon_decode_status(struct ccreader *rdr,
	uint8_t *mess, struct buffer *rxbuf)
{
	if(buffer_available(rxbuf) < SIZE_STATUS)
		return DECODE_DONE;
	else {
		/* Ignore status messages */
		buffer_consume(rxbuf, SIZE_STATUS);
		return DECODE_MORE;
	}
}

/** Decode a vicon message.
 */
static enum decode_t vicon_decode_message(struct ccreader *rdr,
	struct buffer *rxbuf)
{
	uint8_t *mess = buffer_output(rxbuf);
	if ((mess[0] & FLAG) == 0) {
		log_println(rdr->log, "Vicon: unexpected byte %02X", mess[0]);
		buffer_consume(rxbuf, 1);
		return DECODE_MORE;
	}
	if (is_extended_command(mess))
		return vicon_decode_extended(rdr, mess, rxbuf);
	else if (is_command(mess))
		return vicon_decode_command(rdr, mess, rxbuf);
	else
		return vicon_decode_status(rdr, mess, rxbuf);
}

/*
 * vicon_do_read	Read messages in vicon protocol format.
 */
void vicon_do_read(struct ccreader *rdr, struct buffer *rxbuf) {
	while(buffer_available(rxbuf) >= SIZE_STATUS) {
		if(vicon_decode_message(rdr, rxbuf) == DECODE_DONE)
			break;
	}
}

/**
 * Encode the receiver address.
 */
static inline void encode_receiver(uint8_t *mess, const struct ccpacket *pkt) {
	int receiver = ccpacket_get_receiver(pkt);
	mess[0] = FLAG | ((receiver >> 4) & 0x0f);
	mess[1] = receiver & 0x0f;
}

/*
 * encode_pan_tilt	Encode a pan/tilt command.
 */
static void encode_pan_tilt(uint8_t *mess, struct ccpacket *pkt) {
	if (ccpacket_has_pan(pkt)) {
		if (ccpacket_get_pan_mode(pkt) == CC_PAN_LEFT)
			bit_set(mess, BIT_PAN_LEFT);
		else if (ccpacket_get_pan_mode(pkt) == CC_PAN_RIGHT)
			bit_set(mess, BIT_PAN_RIGHT);
	}
	if (ccpacket_has_tilt(pkt)) {
		if (ccpacket_get_tilt_mode(pkt) == CC_TILT_UP)
			bit_set(mess, BIT_TILT_UP);
		else if (ccpacket_get_tilt_mode(pkt) == CC_TILT_DOWN)
			bit_set(mess, BIT_TILT_DOWN);
	}
}

/*
 * encode_lens		Encode a lens command.
 */
static void encode_lens(uint8_t *mess, struct ccpacket *pkt) {
	if (ccpacket_get_iris(pkt) == CC_IRIS_OPEN)
		bit_set(mess, BIT_IRIS_OPEN);
	else if (ccpacket_get_iris(pkt) == CC_IRIS_CLOSE)
		bit_set(mess, BIT_IRIS_CLOSE);
	if (ccpacket_get_focus(pkt) == CC_FOCUS_NEAR)
		bit_set(mess, BIT_FOCUS_NEAR);
	else if (ccpacket_get_focus(pkt) == CC_FOCUS_FAR)
		bit_set(mess, BIT_FOCUS_FAR);
	if (ccpacket_get_zoom(pkt) == CC_ZOOM_IN)
		bit_set(mess, BIT_ZOOM_IN);
	else if (ccpacket_get_zoom(pkt) == CC_ZOOM_OUT)
		bit_set(mess, BIT_ZOOM_OUT);
}

/*
 * encode_toggles	Encode toggle functions.
 */
static void encode_toggles(uint8_t *mess, struct ccpacket *pkt) {
	if (ccpacket_get_ack(pkt) == CC_ACK_ALARM)
		bit_set(mess, BIT_ACK_ALARM);
	if (ccpacket_get_iris(pkt) == CC_IRIS_AUTO)
		bit_set(mess, BIT_AUTO_IRIS);
	if (ccpacket_get_pan_mode(pkt) == CC_PAN_AUTO)
		bit_set(mess, BIT_AUTO_PAN);
	if (ccpacket_get_lens(pkt) == CC_LENS_SPEED)
		bit_set(mess, BIT_LENS_SPEED);
}

/** Encode auxiliary functions.
 */
static void encode_aux(uint8_t *mess, struct ccpacket *pkt) {
	if (ccpacket_get_wiper(pkt) == CC_WIPER_ON)
		bit_set(mess, BIT_AUX_6);
}

/*
 * encode_preset	Encode preset functions.
 */
static void encode_preset(uint8_t *mess, struct ccpacket *pkt) {
	enum cc_flags pm = ccpacket_get_preset_mode(pkt);
	if (pm == CC_PRESET_RECALL)
		bit_set(mess, BIT_RECALL);
	else if (pm == CC_PRESET_STORE)
		bit_set(mess, BIT_STORE);
	mess[5] |= ccpacket_get_preset_number(pkt) & 0x0f;
}

/*
 * encode_command	Encode command functions.
 */
static void encode_command(struct ccwriter *wtr, struct ccpacket *pkt) {
	uint8_t *mess = ccwriter_append(wtr, SIZE_COMMAND);
	if(mess) {
		encode_receiver(mess, pkt);
		bit_set(mess, BIT_COMMAND);
		encode_pan_tilt(mess, pkt);
		encode_lens(mess, pkt);
		encode_toggles(mess, pkt);
		encode_aux(mess, pkt);
		encode_preset(mess, pkt);
	}
}

/*
 * vicon_encode_speed	Encode pan/tilt speed.
 */
static int vicon_encode_speed(int speed) {
	return speed & 0x7ff;
}

/*
 * encode_speeds	Encode the pan and tilt speeds.
 */
static void encode_speeds(uint8_t *mess, struct ccpacket *pkt) {
	int pan = vicon_encode_speed(ccpacket_get_pan_speed(pkt));
	int tilt = vicon_encode_speed(ccpacket_get_tilt_speed(pkt));

	mess[6] = (pan >> 7) & 0x0f;
	mess[7] = pan & 0x7f;
	mess[8] = (tilt >> 7) & 0x0f;
	mess[9] = tilt & 0x7f;
}

/*
 * encode_extended_speed	Encode extended speed message.
 */
static void encode_extended_speed(struct ccwriter *wtr, struct ccpacket *pkt) {
	uint8_t *mess = ccwriter_append(wtr, SIZE_EXTENDED);
	if(mess) {
		encode_receiver(mess, pkt);
		bit_set(mess, BIT_COMMAND);
		bit_set(mess, BIT_EXTENDED);
		encode_pan_tilt(mess, pkt);
		encode_lens(mess, pkt);
		encode_toggles(mess, pkt);
		encode_aux(mess, pkt);
		encode_preset(mess, pkt);
		encode_speeds(mess, pkt);
	}
}

/*
 * encode_extended_preset	Encode extended preset functions.
 */
static void encode_extended_preset(struct ccwriter *wtr, struct ccpacket *pkt) {
	uint8_t *mess = ccwriter_append(wtr, SIZE_EXTENDED);
	if(mess) {
		encode_receiver(mess, pkt);
		bit_set(mess, BIT_COMMAND);
		bit_set(mess, BIT_EXTENDED);
		bit_set(mess, BIT_EX_REQUEST);
		if (ccpacket_get_preset_mode(pkt) == CC_PRESET_STORE)
			bit_set(mess, BIT_EX_STORE);
		encode_lens(mess, pkt);
		encode_toggles(mess, pkt);
		encode_aux(mess, pkt);
		mess[7] |= ccpacket_get_preset_number(pkt) & 0x7f;
		mess[8] |= ccpacket_get_pan_speed(pkt) & 0x7f;
		mess[9] |= ccpacket_get_tilt_speed(pkt) & 0x7f;
	}
}

/*
 * is_extended_preset	Test if a command is an extended preset.
 */
static inline bool is_extended_preset(struct ccpacket *pkt) {
	enum cc_flags pm = ccpacket_get_preset_mode(pkt);
	if(pm == CC_PRESET_RECALL || pm == CC_PRESET_STORE) {
		int pan = ccpacket_get_pan_speed(pkt);
		int tilt = ccpacket_get_tilt_speed(pkt);
		return (ccpacket_get_preset_number(pkt) > 15) || pan || tilt;
	} else
		return false;
}

/** Test if a command is an extended speed.
 */
static bool is_extended_speed(struct ccpacket *pkt) {
	// NOTE: for certain receivers, it appears that auxiliary functions
	//       will not work unless they are in an extended packet.
	return ccpacket_has_pan(pkt) ||
	       ccpacket_has_tilt(pkt) ||
	       ccpacket_get_wiper(pkt);
}

/*
 * adjust_menu_commands	Adjust menu commands for vicon protocol.
 */
static inline void adjust_menu_commands(struct ccpacket *pkt) {
	enum cc_flags mc = ccpacket_get_menu(pkt);
	if (mc == CC_MENU_OPEN)
		ccpacket_set_preset(pkt,CC_PRESET_STORE,VICON_PRESET_MENU_OPEN);
	else if (mc == CC_MENU_ENTER)
		ccpacket_set_pan(pkt, CC_PAN_AUTO, 0);
	else if (mc == CC_MENU_CANCEL)
		ccpacket_set_iris(pkt, CC_IRIS_AUTO);
}

/** Write a packet in vicon protocol.
 */
unsigned int vicon_do_write(struct ccwriter *wtr, struct ccpacket *pkt) {
	int receiver = ccpacket_get_receiver(pkt);
	if(receiver < 1 || receiver > VICON_MAX_ADDRESS)
		return 0;
	adjust_menu_commands(pkt);
	if (is_extended_preset(pkt))
		encode_extended_preset(wtr, pkt);
	else if (is_extended_speed(pkt))
		encode_extended_speed(wtr, pkt);
	else
		encode_command(wtr, pkt);
	return 1;
}
