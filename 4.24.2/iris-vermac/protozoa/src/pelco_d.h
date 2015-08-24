#ifndef PELCO_D_H
#define PELCO_D_H

#include "ccwriter.h"

#define PELCO_D_SZ (7)
#define PELCO_D_GAPTIME (80)
#define PELCO_D_TIMEOUT (15000)		/* Tested with Pelco Esprit */
#define PELCO_D_MAX_ADDRESS (254)

void pelco_d_do_read(struct ccreader *rdr, struct buffer *rxbuf);
unsigned int pelco_d_do_write_cb(struct ccwriter *wtr, struct ccpacket *pkt,
	ccwriter_cb *prepare_writer);
unsigned int pelco_d_do_write(struct ccwriter *wtr, struct ccpacket *pkt);

#endif
