#ifndef POLLER_H
#define POLLER_H

#include <sys/poll.h>		/* for struct pollfd */
#include "channel.h"		/* for struct channel */
#include "defer.h"

struct poller {
	int		n_channels;
	struct channel	*chns;
	struct pollfd	*pollfds;
	struct defer	*defer;
	int		fd_null;
	int		fd_inotify;
	int		wd_inotify;
};

struct poller *poller_init(struct poller *plr, int n_channels,
	struct channel *chns, struct defer *dfr);
void poller_destroy(struct poller *plr);
int poller_loop(struct poller *plr);

#endif
