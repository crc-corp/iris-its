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
#include <stdbool.h>	/* for bool */
#include <stdint.h>	/* for uint8_t */
#include <string.h>	/* for strlen, strncat */
#include "ccreader.h"
#include "pelco_d.h"
#include "bitarray.h"

#define FLAG (0xff)
#define TURBO_SPEED (1 << 6)

enum pelco_special_presets {
	PELCO_PRESET_MENU_OPEN = 95,
};

/*
 * Packet bit positions for PTZ functions.
 */
enum pelco_bit_t {
	BIT_FOCUS_NEAR = 16,
	BIT_IRIS_OPEN = 17,
	BIT_IRIS_CLOSE = 18,
	BIT_CAMERA_ON_OFF = 19,
	BIT_AUTO_PAN = 20,
	BIT_SENSE = 23,
	BIT_EXTENDED = 24,
	BIT_PAN_RIGHT = 25,
	BIT_PAN_LEFT = 26,
	BIT_TILT_UP = 27,
	BIT_TILT_DOWN = 28,
	BIT_ZOOM_IN = 29,
	BIT_ZOOM_OUT = 30,
	BIT_FOCUS_FAR = 31,
};

/*
 * Extended pelco_d functions
 */
enum extended_t {
	EX_NONE,		/* 00000 no function */
	EX_STORE,		/* 00001 store preset */
	EX_CLEAR,		/* 00010 clear preset */
	EX_RECALL,		/* 00011 recall preset */
	EX_AUX_SET,		/* 00100 set auxiliary */
	EX_AUX_CLEAR,		/* 00101 clear auxiliary */
	EX_RESERVED,		/* 00110 reserved */
	EX_RESET,		/* 00111 remote reset */
	EX_ZONE_START,		/* 01000 set zone start */
	EX_ZONE_END,		/* 01001 set zone end */
	EX_CHAR_WRITE,		/* 01010 write character */
	EX_CHAR_CLEAR,		/* 01011 clear all characters */
	EX_ACK_ALARM,		/* 01100 acknowledge alarm */
	EX_ZONE_SCAN_ON,	/* 01101 zone scan on */
	EX_ZONE_SCAN_OFF,	/* 01110 zone scan off */
	EX_PATTERN_START,	/* 01111 set pattern start */
	EX_PATTERN_STOP,	/* 10000 set pattern stop */
	EX_PATTERN_RUN,		/* 10001 run pattern */
	EX_ZOOM_SPEED,		/* 10010 set zoom speed */
	EX_FOCUS_SPEED,		/* 10011 set focus speed */
};

/* Auxiliary functions */
enum ex_aux_t {
	EX_AUX_AUTO_SCAN,	/* 0 (auto scan) */
	EX_AUX_WIPER,		/* 1 (wiper) */
};

/*
 * calculate_checksum	Calculate the checksum for a pelco_d packet.
 */
static uint8_t calculate_checksum(uint8_t *mess) {
	int i;
	int checksum = 0;
	for(i = 1; i < 6; i++)
		checksum += mess[i];
	return checksum;
}

/**
 * Decode the receiver address from a pelco_d packet.
 *
 * @param pkt		Packet.
 * @param mess		Message buffer.
 */
static inline void decode_receiver(struct ccpacket *pkt, uint8_t *mess) {
	ccpacket_set_receiver(pkt, mess[1]);
}

/*
 * decode_speed		Decode pan or tilt speed.
 */
static inline int decode_speed(uint8_t val) {
	int speed = val << 5;
	if(speed > SPEED_MAX)
		return SPEED_MAX;
	else
		return speed;
}

/*
 * decode_pan		Decode the pan speed (and command).
 */
static inline void decode_pan(struct ccpacket *pkt, uint8_t *mess) {
	int pan = decode_speed(mess[4]);
	if (bit_is_set(mess, BIT_PAN_RIGHT))
		ccpacket_set_pan(pkt, CC_PAN_RIGHT, pan);
	else if (bit_is_set(mess, BIT_PAN_LEFT))
		ccpacket_set_pan(pkt, CC_PAN_LEFT, pan);
	else
		ccpacket_set_pan(pkt, CC_PAN_LEFT, 0);
}

/*
 * decode_tilt		Decode the tilt speed (and command).
 */
static inline void decode_tilt(struct ccpacket *pkt, uint8_t *mess) {
	int tilt = decode_speed(mess[5]);
	if (bit_is_set(mess, BIT_TILT_UP))
		ccpacket_set_tilt(pkt, CC_TILT_UP, tilt);
	else if (bit_is_set(mess, BIT_TILT_DOWN))
		ccpacket_set_tilt(pkt, CC_TILT_DOWN, tilt);
	else
		ccpacket_set_tilt(pkt, CC_TILT_DOWN, 0);
}

/*
 * decode_lens		Decode a lens command.
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
 * decode_sense		Decode a sense command.
 */
static inline void decode_sense(struct ccpacket *pkt, uint8_t *mess) {
	if (bit_is_set(mess, BIT_SENSE)) {
		if (bit_is_set(mess, BIT_CAMERA_ON_OFF))
			ccpacket_set_camera(pkt, CC_CAMERA_ON);
		if (bit_is_set(mess, BIT_AUTO_PAN))
			ccpacket_set_pan(pkt, CC_PAN_AUTO, 0);
	} else {
		if (bit_is_set(mess, BIT_CAMERA_ON_OFF))
			ccpacket_set_camera(pkt, CC_CAMERA_OFF);
		if (bit_is_set(mess, BIT_AUTO_PAN))
			ccpacket_set_pan(pkt, CC_PAN_MANUAL, 0);
	}
}

/*
 * pelco_decode_command	Decode a pelco_d command.
 */
static inline enum decode_t pelco_decode_command(struct ccreader *rdr,
	uint8_t *mess)
{
	decode_receiver(rdr->packet, mess);
	decode_pan(rdr->packet, mess);
	decode_tilt(rdr->packet, mess);
	decode_lens(rdr->packet, mess);
	decode_sense(rdr->packet, mess);
	ccreader_process_packet(rdr);
	return DECODE_MORE;
}

/** Decode an auxiliary command set.
 */
static void decode_aux_set(struct ccpacket *pkt, int p0) {
	if (p0 == EX_AUX_WIPER)
		ccpacket_set_wiper(pkt, CC_WIPER_ON);
}

/** Decode an auxiliary command clear.
 */
static void decode_aux_clear(struct ccpacket *pkt, int p0) {
	if (p0 == EX_AUX_WIPER)
		ccpacket_set_wiper(pkt, CC_WIPER_OFF);
}

/*
 * decode_extended	Decode an extended command.
 */
static inline void decode_extended(struct ccpacket *pkt, enum extended_t ex,
	int p0, int p1)
{
	switch(ex) {
	case EX_STORE:
		ccpacket_set_preset(pkt, CC_PRESET_STORE, p0);
		break;
	case EX_RECALL:
		ccpacket_set_preset(pkt, CC_PRESET_RECALL, p0);
		break;
	case EX_CLEAR:
		ccpacket_set_preset(pkt, CC_PRESET_CLEAR, p0);
		break;
	case EX_AUX_SET:
		decode_aux_set(pkt, p0);
		break;
	case EX_AUX_CLEAR:
		decode_aux_clear(pkt, p0);
		break;
	/* FIXME: add other extended functions */
	default:
		break;
	}
}

/*
 * pelco_decode_extended	Decode an extended message.
 */
static inline enum decode_t pelco_decode_extended(struct ccreader *rdr,
	uint8_t *mess)
{
	decode_receiver(rdr->packet, mess);
	int ex = mess[3] >> 1 & 0x1f;
	int p0 = mess[5];
	int p1 = mess[4];
	decode_extended(rdr->packet, ex, p0, p1);
	ccreader_process_packet(rdr);
	return DECODE_MORE;
}

/*
 * pelco_log_discard	Log discarded data.
 */
static void pelco_log_discard(struct ccreader *rdr, uint8_t *mess, int n_bytes,
	const char *msg)
{
	char lbuf[256];
	int i;
	snprintf(lbuf, 256, "Pelco(D) %s; discarding %d bytes: ", msg, n_bytes);
	for(i = 0; i < n_bytes && i < 24 && strlen(lbuf) <= 250; i++) {
		char hchar[4];
		snprintf(hchar, 4, "%02X ", mess[i]);
		strncat(lbuf, hchar, 250 - strlen(lbuf));
	}
	if(n_bytes > 8)
		strncat(lbuf, "...", 250 - strlen(lbuf));
	log_println(rdr->log, lbuf);
}

/*
 * pelco_discard_garbage	Scan receive buffer for garbage data.
 */
static void pelco_discard_garbage(struct ccreader *rdr, struct buffer *rxbuf,
	const char *msg)
{
	uint8_t *mess = buffer_output(rxbuf);
	int n_bytes = 1;
	while(n_bytes < buffer_available(rxbuf)) {
		if(mess[n_bytes] == FLAG)
			break;
		n_bytes++;
	}
	buffer_consume(rxbuf, n_bytes);
	pelco_log_discard(rdr, mess, n_bytes, msg);
}

/*
 * checksum_is_valid	Test if a message checksum is valid.
 */
static inline bool checksum_is_valid(uint8_t *mess) {
	return calculate_checksum(mess) == mess[6];
}

/*
 * pelco_decode_message	Decode a pelco_d message.
 */
static inline enum decode_t pelco_decode_message(struct ccreader *rdr,
	struct buffer *rxbuf)
{
	uint8_t *mess = buffer_output(rxbuf);
	if(mess[0] != FLAG) {
		pelco_discard_garbage(rdr, rxbuf, "Invalid FLAG");
		return DECODE_MORE;
	}
	if(!checksum_is_valid(mess)) {
		pelco_discard_garbage(rdr, rxbuf, "Invalid checksum");
		return DECODE_MORE;
	}
	buffer_consume(rxbuf, PELCO_D_SZ);
	if(bit_is_set(mess, BIT_EXTENDED))
		return pelco_decode_extended(rdr, mess);
	else
		return pelco_decode_command(rdr, mess);
}

/*
 * pelco_d_do_read	Read messages in pelco_d protocol.
 */
void pelco_d_do_read(struct ccreader *rdr, struct buffer *rxbuf) {
	while(buffer_available(rxbuf) >= PELCO_D_SZ) {
		if(pelco_decode_message(rdr, rxbuf) == DECODE_DONE)
			break;
	}
}

/*
 * encode_receiver	Encode the receiver address.
 */
static inline void encode_receiver(uint8_t *mess, const struct ccpacket *pkt) {
	mess[0] = FLAG;
	mess[1] = ccpacket_get_receiver(pkt);
}

/*
 * pelco_d_encode_speed	Encode pan or tilt speed.
 */
static int pelco_d_encode_speed(int speed) {
	/* round to the nearest speed level */
	int s = (speed >> 5) + ((speed % 32) >> 4);
	if(s < TURBO_SPEED)
		return s;
	else
		return TURBO_SPEED - 1;
}

/** Encode pan speed.
 *
 * @param speed		Protocol independent speed (0 - SPEED_MAX).
 * @return Pan speed for Pelco-D protocol.
 */
static int pelco_d_encode_pan_speed(int speed) {
	if(speed > SPEED_MAX - 8)
		return TURBO_SPEED;
	else
		return pelco_d_encode_speed(speed);
}

/*
 * encode_pan		Encode the pan speed and command.
 */
static void encode_pan(uint8_t *mess, struct ccpacket *pkt) {
	int pan = pelco_d_encode_pan_speed(ccpacket_get_pan_speed(pkt));
	mess[4] = pan;
	if (ccpacket_has_pan(pkt)) {
		if (ccpacket_get_pan_mode(pkt) == CC_PAN_LEFT)
			bit_set(mess, BIT_PAN_LEFT);
		else if (ccpacket_get_pan_mode(pkt) == CC_PAN_RIGHT)
			bit_set(mess, BIT_PAN_RIGHT);
		else
			mess[4] = 0;
	}
}

/*
 * encode_tilt		Encode the tilt speed and command.
 */
static void encode_tilt(uint8_t *mess, struct ccpacket *pkt) {
	int tilt = pelco_d_encode_speed(ccpacket_get_tilt_speed(pkt));
	mess[5] = tilt;
	if (tilt) {
		if (ccpacket_get_tilt_mode(pkt) == CC_TILT_UP)
			bit_set(mess, BIT_TILT_UP);
		else if (ccpacket_get_tilt_mode(pkt) == CC_TILT_DOWN)
			bit_set(mess, BIT_TILT_DOWN);
		else
			mess[5] = 0;
	}
}

/*
 * encode_lens		Encode the lens commands.
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
 * encode_sense		Encode a sense command.
 */
static inline void encode_sense(uint8_t *mess, struct ccpacket *pkt) {
	enum cc_flags cc = ccpacket_get_camera(pkt);
	enum cc_flags pm = ccpacket_get_pan_mode(pkt);
	if (cc == CC_CAMERA_ON || pm == CC_PAN_AUTO) {
		bit_set(mess, BIT_SENSE);
		if (cc == CC_CAMERA_ON)
			bit_set(mess, BIT_CAMERA_ON_OFF);
		if (pm == CC_PAN_AUTO)
			bit_set(mess, BIT_AUTO_PAN);
	} else if (cc == CC_CAMERA_OFF || pm == CC_PAN_MANUAL) {
		if (cc == CC_CAMERA_OFF)
			bit_set(mess, BIT_CAMERA_ON_OFF);
		if (pm == CC_PAN_MANUAL)
			bit_set(mess, BIT_AUTO_PAN);
	}
}

/*
 * encode_checksum	Encode the message checksum.
 */
static inline void encode_checksum(uint8_t *mess) {
	mess[6] = calculate_checksum(mess);
}

/*
 * encode_command	Encode a command message.
 */
static void encode_command(struct ccwriter *wtr, struct ccpacket *pkt) {
	uint8_t *mess = ccwriter_append(wtr, PELCO_D_SZ);
	if(mess) {
		encode_receiver(mess, pkt);
		encode_pan(mess, pkt);
		encode_tilt(mess, pkt);
		encode_lens(mess, pkt);
		encode_sense(mess, pkt);
		encode_checksum(mess);
	}
}

/*
 * encode_preset	Encode a preset message.
 */
static void encode_preset(struct ccwriter *wtr, struct ccpacket *pkt) {
	uint8_t *mess = ccwriter_append(wtr, PELCO_D_SZ);
	if(mess) {
		enum cc_flags pm = ccpacket_get_preset_mode(pkt);
		encode_receiver(mess, pkt);
		bit_set(mess, BIT_EXTENDED);
		if (pm == CC_PRESET_RECALL)
			mess[3] |= EX_RECALL << 1;
		else if (pm == CC_PRESET_STORE)
			mess[3] |= EX_STORE << 1;
		else if (pm == CC_PRESET_CLEAR)
			mess[3] |= EX_CLEAR << 1;
		mess[5] = ccpacket_get_preset_number(pkt);
		encode_checksum(mess);
	}
}

/** Encode a wiper command.
 */
static void encode_wiper(struct ccwriter *wtr, struct ccpacket *pkt) {
	uint8_t *mess = ccwriter_append(wtr, PELCO_D_SZ);
	if (mess) {
		enum cc_flags wm = ccpacket_get_wiper(pkt);
		enum extended_t ex = (wm == CC_WIPER_ON)
			? EX_AUX_SET : EX_AUX_CLEAR;
		encode_receiver(mess, pkt);
		bit_set(mess, BIT_EXTENDED);
		mess[3] |= ex << 1;
		mess[5] = EX_AUX_WIPER;
		encode_checksum(mess);
	}
}

/*
 * adjust_menu_commands	Adjust menu commands for pelco d protocol.
 */
static inline void adjust_menu_commands(struct ccpacket *pkt) {
	enum cc_flags mc = ccpacket_get_menu(pkt);
	if (mc == CC_MENU_OPEN)
		ccpacket_set_preset(pkt,CC_PRESET_STORE,PELCO_PRESET_MENU_OPEN);
	else if (mc == CC_MENU_ENTER)
		ccpacket_set_iris(pkt, CC_IRIS_OPEN);
	else if (mc == CC_MENU_CANCEL)
		ccpacket_set_iris(pkt, CC_IRIS_CLOSE);
}

/*
 * pelco_d_do_write_cb	Write a packet in the pelco_d protocol.
 */
unsigned int pelco_d_do_write_cb(struct ccwriter *wtr, struct ccpacket *pkt,
	ccwriter_cb *prepare_writer)
{
	int receiver = ccpacket_get_receiver(pkt);
	if(receiver < 1 || receiver > PELCO_D_MAX_ADDRESS)
		return 0;
	adjust_menu_commands(pkt);
	if (ccpacket_has_command(pkt) || ccpacket_has_autopan(pkt) ||
	    ccpacket_has_power(pkt))
	{
		if (prepare_writer(wtr))
			encode_command(wtr, pkt);
	}
	if (ccpacket_get_preset_mode(pkt)) {
		if (prepare_writer(wtr))
			encode_preset(wtr, pkt);
	}
	if (ccpacket_get_wiper(pkt)) {
		if (prepare_writer(wtr))
			encode_wiper(wtr, pkt);
	}
	return 1;
}

/*
 * pelco_d_prepare_true	Prepare a write for pelco_d protocol.
 */
static int pelco_d_prepare_true(struct ccwriter *wtr) {
	return 1;
}

/*
 * pelco_d_do_write	Write a packet in the pelco_d protocol.
 */
unsigned int pelco_d_do_write(struct ccwriter *wtr, struct ccpacket *pkt) {
	return pelco_d_do_write_cb(wtr, pkt, pelco_d_prepare_true);
}
