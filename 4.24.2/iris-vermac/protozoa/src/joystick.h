#ifndef JOYSTICK_H
#define JOYSTICK_H

#include "ccwriter.h"

#define JOYSTICK_TIMEOUT (30000)

void joystick_do_read(struct ccreader *rdr, struct buffer *rxbuf);

#endif
