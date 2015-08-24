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
#include <string.h>	/* for memcpy */
#include <stdlib.h>
#include "ccpacket.h"

/*
 * Special preset numbers for on-screen menu functions
 */
enum special_presets {
	MENU_OPEN_PRESET = 77,
	MENU_ENTER_PRESET = 78,
	MENU_CANCEL_PRESET = 79,
};

/*
 * A camera control packet is a protocol-neutral representation of a single
 * message to a camera receiver driver.
 */
struct ccpacket {
	int		receiver;	/* receiver address: 1 to 1024 */
	enum cc_flags	flags;		/* bitmask of flags */
	int		pan;		/* 0 (none) to SPEED_MAX (fast) */
	int		tilt;		/* 0 (none) to SPEED_MAX (fast) */
	int		preset;		/* preset number */
	struct timeval	expire;		/* expiration time */
};

/** Create a camera control packet.
 */
struct ccpacket *ccpacket_create(void) {
	struct ccpacket *self = malloc(sizeof(struct ccpacket));
	if (self) {
		timeval_set_now(&self->expire);
		ccpacket_clear(self);
	}
	return self;
}

/** Destroy a camera control packet.
 */
void ccpacket_destroy(struct ccpacket *self) {
	free(self);
}

/** Clear the camera control packet.
 */
void ccpacket_clear(struct ccpacket *pkt) {
	pkt->receiver = 0;
	pkt->flags = 0;
	pkt->pan = 0;
	pkt->tilt = 0;
	pkt->preset = 0;
}

/** Set receiver for packet.
 *
 * @param receiver	Receiver address.
 */
void ccpacket_set_receiver(struct ccpacket *self, int receiver) {
	self->receiver = receiver;
}

/** Get receiver for packet.
 *
 * @return Receiver address.
 */
int ccpacket_get_receiver(const struct ccpacket *self) {
	return self->receiver;
}

/** Get a valid menu command */
static enum cc_flags ccpacket_menu(enum cc_flags mc) {
	enum cc_flags m = mc & CC_MENU;
	switch(m) {
	case CC_MENU_OPEN:
	case CC_MENU_ENTER:
	case CC_MENU_CANCEL:
		return m;
	default:
		return 0;
	}
}

/** Set menu command.
 *
 * @param mc		Menu command.
 */
void ccpacket_set_menu(struct ccpacket *self, enum cc_flags mc) {
	self->flags = ccpacket_menu(mc) | (self->flags & ~CC_MENU);
}

/** Get menu command.
 */
enum cc_flags ccpacket_get_menu(const struct ccpacket *self) {
	return ccpacket_menu(self->flags);
}

/** Get a valid camera command */
static enum cc_flags ccpacket_camera(enum cc_flags cc) {
	enum cc_flags c = cc & CC_CAMERA;
	switch(c) {
	case CC_CAMERA_ON:
	case CC_CAMERA_OFF:
		return c;
	default:
		return 0;
	}
}

/** Set camera on/off command.
 *
 * @param cc		Camera command.
 */
void ccpacket_set_camera(struct ccpacket *self, enum cc_flags cc) {
	self->flags = ccpacket_camera(cc) | (self->flags & ~CC_CAMERA);
}

/** Get camera command.
 */
enum cc_flags ccpacket_get_camera(const struct ccpacket *self) {
	return ccpacket_camera(self->flags);
}

/** Clamp speed value */
static int clamp_speed(int speed) {
	if (speed < 0)
		return 0;
	else if (speed > SPEED_MAX)
		return SPEED_MAX;
	else
		return speed;
}

/** Get a valid pan mode */
static enum cc_flags ccpacket_pan(enum cc_flags pm) {
	enum cc_flags p = pm & CC_PAN;
	switch(p) {
	case CC_PAN_LEFT:
	case CC_PAN_RIGHT:
	case CC_PAN_AUTO:
		return p;
	default:
		return 0;
	}
}

/** Set pan mode and speed.
 */
void ccpacket_set_pan(struct ccpacket *self, enum cc_flags pm, int speed) {
	enum cc_flags flags = self->flags & ~CC_PAN;
	self->flags = flags | ccpacket_pan(pm);
	ccpacket_set_pan_speed(self, speed);
}

/** Get pan mode.
 */
enum cc_flags ccpacket_get_pan_mode(const struct ccpacket *self) {
	return ccpacket_pan(self->flags);
}

/** Set pan speed.
 *
 * @param speed		Pan speed.
 */
void ccpacket_set_pan_speed(struct ccpacket *self, int speed) {
	self->pan = clamp_speed(speed);
}

/** Get pan speed.
 */
int ccpacket_get_pan_speed(const struct ccpacket *self) {
	return self->pan;
}

/** Check if packet has pan.
 */
bool ccpacket_has_pan(const struct ccpacket *self) {
	return ccpacket_get_pan_mode(self) && self->pan;
}

/** Get a valid tilt mode */
static enum cc_flags ccpacket_tilt(enum cc_flags tm) {
	enum cc_flags t = tm & CC_TILT;
	switch(t) {
	case CC_TILT_UP:
	case CC_TILT_DOWN:
		return t;
	default:
		return 0;
	}
}

/** Set tilt mode and speed.
 */
void ccpacket_set_tilt(struct ccpacket *self, enum cc_flags tm, int speed) {
	enum cc_flags flags = self->flags & ~CC_TILT;
	self->flags = flags | ccpacket_tilt(tm);
	ccpacket_set_tilt_speed(self, speed);
}

/** Get tilt mode.
 */
enum cc_flags ccpacket_get_tilt_mode(const struct ccpacket *self) {
	return ccpacket_tilt(self->flags);
}

/** Set tilt speed.
 *
 * @param speed		Tilt speed.
 */
void ccpacket_set_tilt_speed(struct ccpacket *self, int speed) {
	self->tilt = speed;
}

/** Get tilt speed.
 */
int ccpacket_get_tilt_speed(const struct ccpacket *self) {
	return self->tilt;
}

/** Check if packet has tilt.
 */
bool ccpacket_has_tilt(const struct ccpacket *self) {
	return ccpacket_get_tilt_mode(self) && self->tilt;
}

/*
 * ccpacket_set_timeout	Set the timeout for a camera control packet.
 */
void ccpacket_set_timeout(struct ccpacket *pkt, unsigned int timeout) {
	timeval_set_now(&pkt->expire);
	timeval_adjust(&pkt->expire, timeout);
}

/** Check is packet is expired.
 *
 * @param timeout	Timeout in ms.
 * @return true if packet is expired.
 */
bool ccpacket_is_expired(struct ccpacket *self, unsigned int timeout) {
	return time_from_now(&self->expire) > timeout;
}

/** Decode a store preset command.  Predefined presets are replaced with menu
 * commands.
 */
static bool ccpacket_menu_preset(struct ccpacket *self, int p_num) {
	switch(p_num) {
	case MENU_OPEN_PRESET:
		ccpacket_set_menu(self, CC_MENU_OPEN);
		return true;
	case MENU_ENTER_PRESET:
		ccpacket_set_menu(self, CC_MENU_ENTER);
		return true;
	case MENU_CANCEL_PRESET:
		ccpacket_set_menu(self, CC_MENU_CANCEL);
		return true;
	default:
		return false;
	}
}

/** Get a valid preset mode */
static enum cc_flags ccpacket_preset(enum cc_flags pm, int p_num) {
	if(p_num <= 0)
		return 0;
	enum cc_flags p = pm & CC_PRESET;
	switch(p) {
	case CC_PRESET_RECALL:
	case CC_PRESET_STORE:
	case CC_PRESET_CLEAR:
		return p;
	default:
		return 0;
	}
}

/** Set the preset mode and number.
 *
 * @param pm		Preset mode
 * @param p_num		Preset number
 */
void ccpacket_set_preset(struct ccpacket *self, enum cc_flags pm, int p_num) {
	enum cc_flags flags = self->flags & ~CC_PRESET;
	enum cc_flags p = ccpacket_preset(pm, p_num);
	if (p == CC_PRESET_STORE && ccpacket_menu_preset(self, p_num)) {
		self->flags = flags;
		self->preset = 0;
	} else {
		self->flags = flags | p;
		self->preset = p_num;
	}
}

/** Get the preset mode.
 */
enum cc_flags ccpacket_get_preset_mode(const struct ccpacket *self) {
	return ccpacket_preset(self->flags, self->preset);
}

/** Get the preset number.
 */
int ccpacket_get_preset_number(const struct ccpacket *self) {
	return self->preset;
}

/** Test if a packet is a stop command.
 */
bool ccpacket_is_stop(struct ccpacket *pkt) {
	enum cc_flags pm = ccpacket_get_pan_mode(pkt);
	return pkt->pan == 0 &&
	       pkt->tilt == 0 &&
	       pm != CC_PAN_AUTO &&
	       pm != CC_PAN_MANUAL &&
	       ccpacket_get_preset_mode(pkt) == 0 &&
	       ccpacket_get_menu(pkt) == 0 &&
	       ccpacket_get_ack(pkt) == 0 &&
	       ccpacket_get_camera(pkt) == 0 &&
	       ccpacket_get_zoom(pkt) == 0 &&
	       ccpacket_get_focus(pkt) == 0 &&
	       ccpacket_get_iris(pkt) == 0 &&
	       ccpacket_get_wiper(pkt) == 0;
}

/** Get a valid zoom mode */
static enum cc_flags ccpacket_zoom(enum cc_flags zm) {
	enum cc_flags z = zm & CC_ZOOM;
	switch(z) {
	case CC_ZOOM_IN:
	case CC_ZOOM_OUT:
		return z;
	default:
		return 0;
	}
}

/** Set the zoom mode.
 *
 * @param zm		Zoom mode
 */
void ccpacket_set_zoom(struct ccpacket *self, enum cc_flags zm) {
	self->flags = ccpacket_zoom(zm) | (self->flags & ~CC_ZOOM);
}

/** Get the zoom mode.
 */
enum cc_flags ccpacket_get_zoom(const struct ccpacket *self) {
	return ccpacket_zoom(self->flags);
}

/** Get a valid focus mode */
static enum cc_flags ccpacket_focus(enum cc_flags fm) {
	enum cc_flags f = fm & CC_FOCUS;
	switch(f) {
	case CC_FOCUS_NEAR:
	case CC_FOCUS_FAR:
	case CC_FOCUS_AUTO:
		return f;
	default:
		return 0;
	}
}

/** Set the focus mode.
 *
 * @param fm		Focus mode
 */
void ccpacket_set_focus(struct ccpacket *self, enum cc_flags fm) {
	self->flags = ccpacket_focus(fm) | (self->flags & ~CC_FOCUS);
}

/** Get the focus mode.
 */
enum cc_flags ccpacket_get_focus(const struct ccpacket *self) {
	return ccpacket_focus(self->flags);
}

/** Get a valid iris mode */
static enum cc_flags ccpacket_iris(enum cc_flags im) {
	enum cc_flags i = im & CC_IRIS;
	switch(i) {
	case CC_IRIS_CLOSE:
	case CC_IRIS_OPEN:
	case CC_IRIS_AUTO:
		return i;
	default:
		return 0;
	}
}

/** Set the iris mode.
 *
 * @param im		Iris mode
 */
void ccpacket_set_iris(struct ccpacket *self, enum cc_flags im) {
	self->flags = ccpacket_iris(im) | (self->flags & ~CC_IRIS);
}

/** Get the iris mode.
 */
enum cc_flags ccpacket_get_iris(const struct ccpacket *self) {
	return ccpacket_iris(self->flags);
}

/** Get a valid lens mode */
static enum cc_flags ccpacket_lens(enum cc_flags lm) {
	enum cc_flags l = lm & CC_LENS;
	switch(l) {
	case CC_LENS_SPEED:
		return l;
	default:
		return 0;
	}
}

/** Set the lens mode.
 *
 * @param lm		Lens mode
 */
void ccpacket_set_lens(struct ccpacket *self, enum cc_flags lm) {
	self->flags = ccpacket_lens(lm) | (self->flags & ~CC_LENS);
}

/** Get the lens mode.
 */
enum cc_flags ccpacket_get_lens(const struct ccpacket *self) {
	return ccpacket_lens(self->flags);
}

/** Get a valid wiper mode */
static enum cc_flags ccpacket_wiper(enum cc_flags wm) {
	enum cc_flags w = wm & CC_WIPER;
	switch(w) {
	case CC_WIPER_ON:
	case CC_WIPER_OFF:
		return w;
	default:
		return 0;
	}
}

/** Set the wiper mode.
 *
 * @param wm		Wiper mode
 */
void ccpacket_set_wiper(struct ccpacket *self, enum cc_flags wm) {
	self->flags = ccpacket_wiper(wm) | (self->flags & ~CC_WIPER);
}

/** Get the wiper mode.
 */
enum cc_flags ccpacket_get_wiper(const struct ccpacket *self) {
	return ccpacket_wiper(self->flags);
}

/** Get a valid alarm ack */
static enum cc_flags ccpacket_ack(enum cc_flags am) {
	enum cc_flags a = am & CC_ACK_ALARM;
	switch(a) {
	case CC_ACK_ALARM:
		return a;
	default:
		return 0;
	}
}

/** Set the alarm ack.
 *
 * @param am		Alarm ack.
 */
void ccpacket_set_ack(struct ccpacket *self, enum cc_flags am) {
	self->flags = ccpacket_ack(am) | (self->flags & ~CC_ACK);
}

/** Get the alarm ack.
 */
enum cc_flags ccpacket_get_ack(const struct ccpacket *self) {
	return ccpacket_ack(self->flags);
}

/** Test if a packet has a command to encode.
 *
 * @param pkt	Packet to check for command
 * @return	True if command is present; false otherwise
 */
bool ccpacket_has_command(const struct ccpacket *pkt) {
	return ccpacket_get_pan_mode(pkt) ||
	       ccpacket_get_tilt_mode(pkt) ||
	       ccpacket_get_zoom(pkt) ||
	       ccpacket_get_focus(pkt) ||
	       ccpacket_get_iris(pkt);
}

/*
 * ccpacket_has_autopan	Test if the packet has an autopan command.
 */
bool ccpacket_has_autopan(const struct ccpacket *pkt) {
	enum cc_flags pm = ccpacket_get_pan_mode(pkt);
	return pm == CC_PAN_AUTO || pm == CC_PAN_MANUAL;
}

/*
 * ccpacket_has_power	Test if the packet has a power command.
 */
bool ccpacket_has_power(const struct ccpacket *pkt) {
	return ccpacket_get_camera(pkt);
}

/*
 * ccpacket_log_pan	Log any pan command in the camera control packet.
 *
 * log: message logger
 */
static inline void ccpacket_log_pan(struct ccpacket *pkt, struct log *log) {
	if (pkt->pan == 0)
		log_printf(log, " pan: 0");
	else if (ccpacket_get_pan_mode(pkt) == CC_PAN_LEFT)
		log_printf(log, " pan left: %d", pkt->pan);
	else if (ccpacket_get_pan_mode(pkt) == CC_PAN_RIGHT)
		log_printf(log, " pan right: %d", pkt->pan);
}

/*
 * ccpacket_log_tilt	Log any tilt command in the camera control packet.
 *
 * log: message logger
 */
static inline void ccpacket_log_tilt(struct ccpacket *pkt, struct log *log) {
	if (pkt->tilt == 0)
		log_printf(log, " tilt: 0");
	else if (ccpacket_get_tilt_mode(pkt) == CC_TILT_UP)
		log_printf(log, " tilt up: %d", pkt->tilt);
	else if (ccpacket_get_tilt_mode(pkt) == CC_TILT_DOWN)
		log_printf(log, " tilt down: %d", pkt->tilt);
}

/*
 * ccpacket_log_lens	Log any lens commands in the camera control packet.
 *
 * log: message logger
 */
static inline void ccpacket_log_lens(struct ccpacket *pkt, struct log *log) {
	enum cc_flags zm = ccpacket_get_zoom(pkt);
	enum cc_flags fm = ccpacket_get_focus(pkt);
	enum cc_flags im = ccpacket_get_iris(pkt);
	enum cc_flags lm = ccpacket_get_lens(pkt);
	if (zm == CC_ZOOM_IN)
		log_printf(log, " zoom IN");
	if (zm == CC_ZOOM_OUT)
		log_printf(log, " zoom OUT");
	if (fm == CC_FOCUS_NEAR)
		log_printf(log, " focus NEAR");
	if (fm == CC_FOCUS_FAR)
		log_printf(log, " focus FAR");
	if (fm == CC_FOCUS_AUTO)
		log_printf(log, " focus AUTO");
	if (im == CC_IRIS_CLOSE)
		log_printf(log, " iris CLOSE");
	if (im == CC_IRIS_OPEN)
		log_printf(log, " iris OPEN");
	if (im == CC_IRIS_AUTO)
		log_printf(log, " iris AUTO");
	if (lm == CC_LENS_SPEED)
		log_printf(log, " lens SPEED");
}

/*
 * ccpacket_log_preset	Log any preset commands in the camera control packet.
 *
 * log: message logger
 */
static inline void ccpacket_log_preset(struct ccpacket *pkt, struct log *log) {
	enum cc_flags pm = ccpacket_get_preset_mode(pkt);
	if (pm == CC_PRESET_RECALL)
		log_printf(log, " recall");
	else if (pm == CC_PRESET_STORE)
		log_printf(log, " store");
	else if (pm == CC_PRESET_CLEAR)
		log_printf(log, " clear");
	log_printf(log, " preset: %d", pkt->preset);
}

/*
 * ccpacket_log_special	Log any special commands in the camera control packet.
 *
 * log: message logger
 */
static inline void ccpacket_log_special(struct ccpacket *pkt, struct log *log) {
	enum cc_flags pm = ccpacket_get_pan_mode(pkt);
	enum cc_flags mc = ccpacket_get_menu(pkt);
	if (pm == CC_PAN_AUTO)
		log_printf(log, " auto-pan");
	if (pm == CC_PAN_MANUAL)
		log_printf(log, " manual-pan");
	if (mc == CC_MENU_OPEN)
		log_printf(log, " menu-open");
	if (mc == CC_MENU_ENTER)
		log_printf(log, " menu-enter");
	if (mc == CC_MENU_CANCEL)
		log_printf(log, " menu-cancel");
	if (ccpacket_get_ack(pkt) == CC_ACK_ALARM)
		log_printf(log, " ack-alarm");
}

/*
 * ccpacket_log		Log the camera control packet.
 *
 * log: message logger
 */
void ccpacket_log(struct ccpacket *pkt, struct log *log, const char *dir,
	const char *name)
{
	log_line_start(log);
	log_printf(log, "packet: %s %s rcv: %d", dir, name, pkt->receiver);
	ccpacket_log_pan(pkt, log);
	ccpacket_log_tilt(pkt, log);
	ccpacket_log_lens(pkt, log);
	if (ccpacket_get_camera(pkt))
		log_printf(log, " camera");
	if (ccpacket_get_wiper(pkt))
		log_printf(log, " wiper");
	if(pkt->preset)
		ccpacket_log_preset(pkt, log);
	ccpacket_log_special(pkt, log);
	log_line_end(log);
}

/*
 * ccpacket_copy	Copy a camera control packet.
 */
void ccpacket_copy(struct ccpacket *dest, struct ccpacket *src) {
	memcpy(dest, src, sizeof(struct ccpacket));
}
