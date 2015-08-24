#ifndef LOG_H
#define LOG_H

#include <stdbool.h>	/* for bool */
#include <stdio.h>	/* for FILE */

struct log {
	FILE	*out;		/* output stream */
	bool	debug;		/* debug raw input/output data */
	bool	packet;		/* log packet details */
	bool	stats;		/* log packet statistics */
};

struct log *log_init(struct log *log);
struct log *log_open_file(struct log *log, const char *filename);
void log_destroy(struct log *log);
void log_line_start(struct log *log);
void log_line_end(struct log *log);
void log_printf(struct log *log, const char *format, ...);
void log_println(struct log *log, const char *format, ...);

#endif
