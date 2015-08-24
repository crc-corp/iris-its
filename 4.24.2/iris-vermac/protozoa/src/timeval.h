#ifndef TIMEVAL_H
#define TIMEVAL_H

#include <sys/time.h>
#include <stdbool.h>
#include "clump.h"

void timeval_set_now(struct timeval *tv);
void timeval_adjust(struct timeval *tv, unsigned int ms);
long time_elapsed(const struct timeval *start, const struct timeval *end);
long time_from_now(const struct timeval *tv);
long time_since(const struct timeval *tv);
cl_compare_t timeval_compare(const void *value0, const void *value1);

#endif
