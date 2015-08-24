#ifndef AXIS_H
#define AXIS_H

#include "ccwriter.h"

#define AXIS_GAPTIME (250)
#define AXIS_TIMEOUT (30000)
#define AXIS_MAX_ADDRESS (1)

/* There is no reader for axis protocol (http output only) */
unsigned int axis_do_write(struct ccwriter *wtr, struct ccpacket *pkt);

#endif
