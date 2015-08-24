/*
 * protozoa -- CCTV transcoder / mixer for PTZ
 * Copyright (C) 2008  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
#include <stdlib.h>
#include "timeval.h"

/*
 * timeval_set_now	Set a timeval to current time.
 */
void timeval_set_now(struct timeval *tv) {
	gettimeofday(tv, NULL);
}

/*
 * timeval_adjust	Adjust a timeval by the given number of ms
 */
void timeval_adjust(struct timeval *tv, unsigned int ms) {
	tv->tv_usec += ms * 1000;
	while(tv->tv_usec >= 1000000) {
		tv->tv_sec++;
		tv->tv_usec -= 1000000;
	}
}

/*
 * time_elapsed		Calculate the time elapsed between two timevals
 */
long time_elapsed(const struct timeval *start, const struct timeval *end) {
	return (end->tv_sec - start->tv_sec) * 1000 +
		(end->tv_usec - start->tv_usec) / 1000;
}

/*
 * time_from_now	Determine milliseconds a timeval is in the future.
 */
long time_from_now(const struct timeval *tv) {
	struct timeval now;
	long ms;

	gettimeofday(&now, NULL);

	ms = time_elapsed(&now, tv);
	if(ms < 0)
		return 0;
	else
		return ms;
}

/*
 * time_since		Determine milliseconds a timeval is in the past.
 */
long time_since(const struct timeval *tv) {
	struct timeval now;
	long ms;

	gettimeofday(&now, NULL);

	ms = time_elapsed(tv, &now);
	if(ms < 0)
		return 0;
	else
		return ms;
}

/*
 * timeval_compare	Compare two timevals.
 */
cl_compare_t timeval_compare(const void *value0, const void *value1) {
	const struct timeval *t0 = value0;
	const struct timeval *t1 = value1;

	if(t0->tv_sec < t1->tv_sec)
		return CL_LESS;
	if(t0->tv_sec > t1->tv_sec)
		return CL_GREATER;
	if(t0->tv_usec < t1->tv_usec)
		return CL_LESS;
	if(t0->tv_usec > t1->tv_usec)
		return CL_GREATER;
	return CL_EQUAL;
}
