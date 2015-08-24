#ifndef CCREADER_H
#define CCREADER_H

#include "buffer.h"
#include "ccpacket.h"
#include "log.h"

#define DEFAULT_TIMEOUT (1000)

enum rdr_flags_t {
	PT_DEADZONE = (1 << 0),	/* pan/tilt values skip over deadzone */
};

enum decode_t {
	DECODE_MORE = 0,	/* more buffered packets may be decoded */
	DECODE_DONE = 1,	/* buffered packet decoding is done */
};

struct ccnode {
	struct	ccwriter	*writer;	/* writer for this node */
	int			range_first;	/* first address in range */
	int			range_last;	/* last address in range */
	int			shift;		/* receiver address shift */
	struct	ccnode		*next;		/* next node in the list */
};

struct ccreader {
	void	(*do_read)	(struct ccreader *rdr, struct buffer *rxbuf);
	struct	ccpacket	*packet;	/* camera control packet */
	unsigned int		timeout;	/* time to hold commands (ms) */
	enum rdr_flags_t	flags;		/* special reader flags */
	struct	ccnode		*head;		/* head of writer list */
	const char		*name;		/* channel name */
	struct	log		*log;		/* message logger */
};

struct ccreader *ccreader_init(struct ccreader *rdr, const char *name,
	struct log *log, const char *protocol);
void ccreader_destroy(struct ccreader *rdr);
void ccreader_previous_camera(struct ccreader *rdr);
void ccreader_next_camera(struct ccreader *rdr);
void ccreader_add_writer(struct ccreader *rdr, struct ccnode *node,
	struct ccwriter *wtr, const char *range, const char *shift);
unsigned int ccreader_process_packet_no_clear(struct ccreader *rdr);
unsigned int ccreader_process_packet(struct ccreader *rdr);

#endif
