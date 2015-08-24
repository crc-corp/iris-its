#ifndef STATS_H
#define STATS_H

#include "log.h"
#include "ccpacket.h"

void ptz_stats_init(struct log *log);
void ptz_stats_count(const struct ccpacket *pkt, enum domain d);

#endif
