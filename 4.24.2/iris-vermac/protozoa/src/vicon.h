#ifndef VICON_H
#define VICON_H

#include "ccwriter.h"

#define VICON_GAPTIME (80)
#define VICON_TIMEOUT (15000)
#define VICON_MAX_ADDRESS (255)

void vicon_do_read(struct ccreader *rdr, struct buffer *rxbuf);
unsigned int vicon_do_write(struct ccwriter *wtr, struct ccpacket *pkt);

#endif
