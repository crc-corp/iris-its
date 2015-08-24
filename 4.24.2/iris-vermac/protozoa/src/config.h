#ifndef CONFIG_H
#define CONFIG_H

#include "channel.h"
#include "ccpacket.h"
#include "defer.h"

#define LINE_LENGTH (256)

struct config {
	struct cl_pool		reader_pool;
	struct cl_pool		node_pool;
	struct cl_pool		writer_pool;
	char			*line;
	struct channel		*chns;
	struct log		*log;
	struct ptz_stats	*stats;
	struct defer		*defer;
	struct ccwriter		*writer_head;
	int			n_channels;
};

const char *config_file(void);
struct config *config_init(struct config *cfg, struct log *log);
void config_destroy(struct config *cfg);
int config_read(struct config *cfg, const char *filename);
struct channel *config_cede_channels(struct config *cfg);
int config_verify(const char *filename);

#endif
