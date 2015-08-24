#ifndef CCWRITER_H
#define CCWRITER_H

#include "ccpacket.h"	/* for struct ccpacket */
#include "channel.h"	/* for struct channel */
#include "defer.h"	/* for struct deferred_pkt, defer */

struct ccwriter {
	unsigned int (*do_write) (struct ccwriter *wtr, struct ccpacket *pkt);
	struct channel		*chn;		/* channel to write */
	struct deferred_pkt	*deferred;	/* deferred packets */
	unsigned int		n_rcv;		/* number of receivers */
	unsigned int		gaptime;	/* packet gap time (ms) */
	unsigned int		timeout;	/* time command is held (ms) */
	char			*auth;		/* authentication token */
	struct defer		*defer;		/* deferred packet handler */
	struct ccwriter		*next;		/* next writer */
};

typedef int (ccwriter_cb) (struct ccwriter *wtr);

struct ccwriter *ccwriter_init(struct ccwriter *writer, struct channel *chn,
	const char *protocol, const char *auth);
void ccwriter_destroy(struct ccwriter *wtr);
void *ccwriter_append(struct ccwriter *wtr, size_t n_bytes);
int ccwriter_do_write(struct ccwriter *wtr, struct ccpacket *pkt);

#endif
