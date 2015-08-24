#ifndef CCPACKET_H
#define CCPACKET_H

#include "log.h"
#include "timeval.h"

enum domain {
	CC_DOM_IN,
	CC_DOM_OUT,
};

enum cc_flags {
	CC_PAN_LEFT = 1 << 0,
	CC_PAN_RIGHT = 1 << 1,
	CC_PAN_AUTO = 1 << 2,
	CC_PAN_MANUAL = 1 << 3,
	CC_PAN = CC_PAN_LEFT | CC_PAN_RIGHT | CC_PAN_AUTO | CC_PAN_MANUAL,
	CC_TILT_UP = 1 << 4,
	CC_TILT_DOWN = 1 << 5,
	CC_TILT = CC_TILT_UP | CC_TILT_DOWN,
	CC_PRESET_RECALL = 1 << 6,
	CC_PRESET_STORE = 1 << 7,
	CC_PRESET_CLEAR = 1 << 8,
	CC_PRESET = CC_PRESET_RECALL | CC_PRESET_STORE | CC_PRESET_CLEAR,
	CC_MENU_OPEN = 1 << 9,
	CC_MENU_ENTER = 1 << 10,
	CC_MENU_CANCEL = 1 << 11,
	CC_MENU = CC_MENU_OPEN | CC_MENU_ENTER | CC_MENU_CANCEL,
	CC_CAMERA_ON = 1 << 12,
	CC_CAMERA_OFF = 1 << 13,
	CC_CAMERA = CC_CAMERA_ON | CC_CAMERA_OFF,
	CC_ACK_ALARM = 1 << 14,
	CC_ACK = CC_ACK_ALARM,
	CC_ZOOM_IN = 1 << 15,
	CC_ZOOM_OUT = 1 << 16,
	CC_ZOOM = CC_ZOOM_IN | CC_ZOOM_OUT,
	CC_FOCUS_NEAR = 1 << 17,
	CC_FOCUS_FAR = 1 << 18,
	CC_FOCUS_AUTO = 1 << 19,
	CC_FOCUS = CC_FOCUS_NEAR | CC_FOCUS_FAR | CC_FOCUS_AUTO,
	CC_IRIS_CLOSE = 1 << 20,
	CC_IRIS_OPEN = 1 << 21,
	CC_IRIS_AUTO = 1 << 22,
	CC_IRIS = CC_IRIS_CLOSE | CC_IRIS_OPEN | CC_IRIS_AUTO,
	CC_LENS_SPEED = 1 << 23,
	CC_LENS = CC_LENS_SPEED,
	CC_WIPER_ON = 1 << 24,
	CC_WIPER_OFF = 1 << 25,
	CC_WIPER = CC_WIPER_ON | CC_WIPER_OFF,
};

#define SPEED_MAX ((1 << 11) - 1)

struct ccpacket *ccpacket_create(void);
void ccpacket_destroy(struct ccpacket *self);
void ccpacket_clear(struct ccpacket *pkt);
void ccpacket_set_receiver(struct ccpacket *self, int receiver);
int ccpacket_get_receiver(const struct ccpacket *self);
void ccpacket_set_menu(struct ccpacket *self, enum cc_flags mc);
enum cc_flags ccpacket_get_menu(const struct ccpacket *self);
void ccpacket_set_camera(struct ccpacket *self, enum cc_flags cc);
enum cc_flags ccpacket_get_camera(const struct ccpacket *self);
void ccpacket_set_pan(struct ccpacket *self, enum cc_flags pm, int speed);
enum cc_flags ccpacket_get_pan_mode(const struct ccpacket *self);
void ccpacket_set_pan_speed(struct ccpacket *self, int speed);
int ccpacket_get_pan_speed(const struct ccpacket *self);
bool ccpacket_has_pan(const struct ccpacket *self);
void ccpacket_set_tilt(struct ccpacket *self, enum cc_flags tm, int speed);
enum cc_flags ccpacket_get_tilt_mode(const struct ccpacket *self);
void ccpacket_set_tilt_speed(struct ccpacket *self, int speed);
int ccpacket_get_tilt_speed(const struct ccpacket *self);
bool ccpacket_has_tilt(const struct ccpacket *self);
void ccpacket_set_timeout(struct ccpacket *pkt, unsigned int timeout);
bool ccpacket_is_expired(struct ccpacket *self, unsigned int timeout);
void ccpacket_set_preset(struct ccpacket *self, enum cc_flags pm, int p_num);
enum cc_flags ccpacket_get_preset_mode(const struct ccpacket *self);
int ccpacket_get_preset_number(const struct ccpacket *self);
bool ccpacket_is_stop(struct ccpacket *pkt);
void ccpacket_set_zoom(struct ccpacket *self, enum cc_flags zm);
enum cc_flags ccpacket_get_zoom(const struct ccpacket *self);
void ccpacket_set_focus(struct ccpacket *self, enum cc_flags fm);
enum cc_flags ccpacket_get_focus(const struct ccpacket *self);
void ccpacket_set_iris(struct ccpacket *self, enum cc_flags im);
enum cc_flags ccpacket_get_iris(const struct ccpacket *self);
void ccpacket_set_lens(struct ccpacket *self, enum cc_flags lm);
enum cc_flags ccpacket_get_lens(const struct ccpacket *self);
void ccpacket_set_wiper(struct ccpacket *self, enum cc_flags wm);
enum cc_flags ccpacket_get_wiper(const struct ccpacket *self);
void ccpacket_set_ack(struct ccpacket *self, enum cc_flags a);
enum cc_flags ccpacket_get_ack(const struct ccpacket *self);
bool ccpacket_has_command(const struct ccpacket *pkt);
bool ccpacket_has_autopan(const struct ccpacket *pkt);
bool ccpacket_has_power(const struct ccpacket *pkt);
void ccpacket_log(struct ccpacket *pkt, struct log *log, const char *dir,
	const char *name);
void ccpacket_copy(struct ccpacket *dest, struct ccpacket *src);

#endif
