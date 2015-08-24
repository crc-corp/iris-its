#ifndef PELCO_P_H
#define PELCO_P_H

#include "ccwriter.h"

#define PELCO_P_GAPTIME (80)
#define PELCO_P_TIMEOUT (15000)
#define PELCO_P_MAX_ADDRESS (254)

void pelco_p_do_read(struct ccreader *rdr, struct buffer *rxbuf);
unsigned int pelco_p_do_write(struct ccwriter *wtr, struct ccpacket *pkt);

#endif
