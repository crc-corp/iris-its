#ifndef MANCHESTER_H
#define MANCHESTER_H

#include "ccwriter.h"

#define MANCHESTER_GAPTIME (0)
#define MANCHESTER_TIMEOUT (80)
#define MANCHESTER_MAX_ADDRESS (1024)

void manchester_do_read(struct ccreader *rdr, struct buffer *rxbuf);
unsigned int manchester_do_write(struct ccwriter *wtr, struct ccpacket *pkt);

#endif
