#ifndef CHANNEL_H
#define CHANNEL_H

#include <stdbool.h>
#include "buffer.h"
#include "ccreader.h"

enum ch_flag_t {
	FLAG_UDP = 1 << 0,		/* flag for UDP datagram protocol */
	FLAG_TCP = 1 << 1,		/* flag for TCP stream protocol */
	FLAG_LISTEN = 1 << 2,		/* flag for TCP listen channel */
	FLAG_RESP_REQUIRED = 1 << 3,	/* flag for response required */
	FLAG_NEEDS_RESP = 1 << 4,	/* flag for needs response */
};

struct channel {
	char		name[32];		/* channel name */
	char		service[32];		/* service (port / baud rate) */
	int		sfd;			/* server file descriptor */
	int		fd;			/* file descriptor */
	enum ch_flag_t	flags;			/* channel flags */

	struct buffer	rxbuf;			/* receive buffer */
	struct buffer	txbuf;			/* transmit buffer */

	struct ccreader *reader;		/* camera control reader */
	struct log	*log;			/* message logger */
	struct channel	*next;			/* next channel in list */
};

struct channel* channel_init(struct channel *chn, const char *name,
	const char *service, enum ch_flag_t flags, struct log *log);
void channel_destroy(struct channel *chn);
bool channel_matches(struct channel *chn, const char *name, const char *service,
	enum ch_flag_t flags);
int channel_open(struct channel *chn);
int channel_close(struct channel *chn);
bool channel_is_open(const struct channel *chn);
bool channel_has_reader(const struct channel *chn);
bool channel_needs_reading(const struct channel *chn);
bool channel_needs_writing(const struct channel *chn);
bool channel_is_waiting(const struct channel *chn);
ssize_t channel_read(struct channel *chn);
ssize_t channel_write(struct channel *chn);

#endif
