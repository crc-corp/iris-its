/*
 * protozoa -- CCTV transcoder / mixer for PTZ
 * Copyright (C) 2006-2012  Minnesota Department of Transportation
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
#include <stdarg.h>	/* for va_list, va_start, va_end */
#include <stdint.h>	/* for uint8_t */
#include <time.h>	/* for localtime, strftime */
#include <sys/time.h>	/* for gettimeofday */
#include "log.h"	/* for struct log, prototypes */

/*
 * log_init		Initialize a new message log.
 *
 * return: pointer to struct log or NULL on failure
 */
struct log *log_init(struct log *log) {
	log->out = stderr;
	log->debug = false;
	log->packet = false;
	log->stats = false;
	return log;
}

/*
 * log_open_file	Open a message log file.
 *
 * filename: name of file to append messages
 * return: pointer to struct log or NULL on failure
 */
struct log *log_open_file(struct log *log, const char *filename) {
	FILE *out = fopen(filename, "a");
	if(out) {
		log->out = out;
		return log;
	} else
		return NULL;
}

/*
 * log_destroy		Destroy a previously initialized message log.
 */
void log_destroy(struct log *log) {
	if(log->out != stderr) {
		fclose(log->out);
		log->out = stderr;
	}
}

/*
 * log_line_start	Start a new line in the message log.
 */
void log_line_start(struct log *log) {
	struct timeval tv;
	struct timezone tz;
	struct tm *now;
	char buf[22];

	gettimeofday(&tv, &tz);
	now = localtime(&tv.tv_sec);
	strftime(buf, 22, "%Y %b %d %H:%M:%S ", now);
	fprintf(log->out, buf);
}

/*
 * log_line_end		End the current line in the message log.
 */
void log_line_end(struct log *log) {
	fprintf(log->out, "\n");
	fflush(log->out);
}

/*
 * log_printf		Print a message on the current line in the message log.
 *
 * format: printf format string
 * ...: printf arguments
 */
void log_printf(struct log *log, const char *format, ...) {
	va_list va;
	va_start(va, format);
	if(log)
		vfprintf(log->out, format, va);
	va_end(va);
}

/*
 * log_println		Print a full message on a new line in the message log.
 *
 * format: printf format string
 * ...: printf arguments
 */
void log_println(struct log *log, const char *format, ...) {
	va_list va;
	va_start(va, format);
	if(log) {
		log_line_start(log);
		vfprintf(log->out, format, va);
		log_line_end(log);
	}
	va_end(va);
}
