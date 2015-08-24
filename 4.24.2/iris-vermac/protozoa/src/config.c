/*
 * protozoa -- CCTV transcoder / mixer for PTZ
 * Copyright (C) 2006-2014  Minnesota Department of Transportation
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
#include <string.h>		/* for memset */
#include <sys/errno.h>		/* for errno */
#include "config.h"
#include "ccreader.h"
#include "ccwriter.h"

/* Default config file */
#define CONF_FILE "/etc/protozoa.conf"

/**
 * config_file		Get name of config file.
 */
const char *config_file(void) {
	char *cf = getenv("PROTOZOA_CONFIG");
	return (cf) ? cf : CONF_FILE;
}

/*
 * config_init		Initialize a new configuration reader.
 *
 * log: message logger (borrowed pointer)
 * return: pointer to struct config or NULL on error
 */
struct config *config_init(struct config *cfg, struct log *log) {
	memset(cfg, 0, sizeof(struct config));
	cfg->line = malloc(LINE_LENGTH);
	if(cfg->line == NULL)
		return NULL;
	memset(cfg->line, 0, LINE_LENGTH);
	cfg->log = log;
	cfg->defer = malloc(sizeof(struct defer));
	if(defer_init(cfg->defer) == NULL)
		goto fail;
	cl_pool_init(&cfg->reader_pool, sizeof(struct ccreader));
	cl_pool_init(&cfg->node_pool, sizeof(struct ccnode));
	cl_pool_init(&cfg->writer_pool, sizeof(struct ccwriter));
	cfg->writer_head = NULL;
	return cfg;
fail:
	free(cfg->line);
	return NULL;
}

/*
 * config_destroy	Destroy a previously initialized config.
 */
void config_destroy(struct config *cfg) {
	struct ccwriter *writer = cfg->writer_head;
	struct channel *chn = cfg->chns;
	while(chn) {
		struct channel *nchn = chn->next;
		channel_destroy(chn);
		free(chn);
		chn = nchn;
	}
	while(writer) {
		struct ccwriter *next = writer->next;
		ccwriter_destroy(writer);
		writer = next;
	}
	cl_pool_destroy(&cfg->reader_pool);
	cl_pool_destroy(&cfg->node_pool);
	cl_pool_destroy(&cfg->writer_pool);
	defer_destroy(cfg->defer);
	free(cfg->defer);
	free(cfg->line);
	memset(cfg, 0, sizeof(struct config));
}

/*
 * config_find_channel	Find a configured channel by name.
 *
 * name: name of channel to find
 * service: service (port or serial baud rate)
 * flags: flags to for special channel options
 * return: pointer to channel; or NULL if not found
 */
static struct channel *config_find_channel(struct config *cfg,
	const char *name, const char *service, enum ch_flag_t flags)
{
	struct channel *chn = cfg->chns;
	while(chn) {
		if(channel_matches(chn, name, service, flags))
			return chn;
		chn = chn->next;
	}
	return NULL;
}

/*
 * config_new_channel	Create a new channel in the configuration.
 *
 * name: name of the channel
 * service: service (port or serial baud rate)
 * flags: flags to for special channel options
 * return: pointer to channel; or NULL on error
 */
static struct channel *config_new_channel(struct config *cfg, const char *name,
	const char *service, enum ch_flag_t flags)
{
	struct channel *chn = malloc(sizeof(struct channel));
	if(chn == NULL)
		goto fail;
	if(channel_init(chn, name, service, flags, cfg->log) == NULL)
		goto fail;
	chn->next = cfg->chns;
	cfg->chns = chn;
	cfg->n_channels++;
	return chn;
fail:
	log_println(cfg->log, "config: channel %s init error", name);
	return NULL;
}

/*
 * copy_name		Copy a port/host name.
 */
static void copy_name(const char *name, char *pname, const int plen) {
	char *c = strrchr(name, ':');
	if(c) {
		size_t len = c - name;
		if(len >= plen)
			len = plen - 1;
		strncpy(pname, name, len);
		pname[len] = '\0';
	} else {
		strncpy(pname, name, plen);
		pname[plen - 1] = '\0';
	}
}

/*
 * starts_with		Check if a string starts with a specified prefix.
 */
static bool starts_with(const char *str, const char *pre) {
	while(*pre) {
		if(*str != *pre)
			return false;
		str++;
		pre++;
	}
	return true;
}

/*
 * parse_name		Parse the channel name.
 *
 * name: channel name (port:baud or host:port pair)
 * pname: parsed value of the channel name.
 * plen: length of pname buffer.
 */
static enum ch_flag_t parse_name(const char *name, char *pname, const int plen){
	enum ch_flag_t flags = 0;
	if(starts_with(name, "udp://")) {
		copy_name(name + 6, pname, plen);
		flags |= FLAG_UDP;
	} else if(starts_with(name, "tcp://")) {
		copy_name(name + 6, pname, plen);
		flags |= FLAG_TCP;
	} else
		copy_name(name, pname, plen);
	return flags;
}

/*
 * parse_service	Parse the service value after a colon.
 *
 * name: channel name (port:baud or host:service pair)
 * service: parsed value of service (or baud rate)
 * plen: length of service buffer.
 */
static void parse_service(const char *name, char *service, int plen) {
	char *c = strrchr(name, ':');
	if(c) {
		strncpy(service, c + 1, plen);
		service[plen - 1] = '\0';
	} else
		service[0] = '\0';
}

/*
 * _config_get_channel	Find an existing channel or create a new one.
 *
 * name: name of the channel (device node or hostname)
 * service: service (port or serial baud rate)
 * flags: flags for special channel options
 * return: pointer to channel; or NULL on error
 */
static struct channel *_config_get_channel(struct config *cfg, const char *name,
	const char *service, enum ch_flag_t flags)
{
	struct channel *chn = config_find_channel(cfg, name, service, flags);
	if(chn)
		return chn;
	else
		return config_new_channel(cfg, name, service, flags);
}

/*
 * config_get_channel	Find an existing channel or create a new one.
 *
 * name: name of the channel (device node or hostname)
 * flags: flags for special channel options
 * return: pointer to channel; or NULL on error
 */
static struct channel *config_get_channel(struct config *cfg, const char *name,
	enum ch_flag_t flags)
{
	char pname[32];
	char service[32];

	flags |= parse_name(name, pname, 32);
	parse_service(name, service, 32);

	return _config_get_channel(cfg, pname, service, flags);
}

/* config_create_writer	Create a new ccwriter.
 */
static struct ccwriter *config_create_writer(struct config *cfg) {
	struct ccwriter *writer = cl_pool_alloc(&cfg->writer_pool);
	writer->next = cfg->writer_head;
	cfg->writer_head = writer;
	return writer;
}

/*
 * config_directive	Process one configuration directive.
 *
 * protocol_in: input protocol
 * port_in: input port:baud pair or TCP host:port
 * range: input receiver address range
 * protocol_out: output protocol
 * port_out: output port:baud pair or TCP host:port
 * shift: output receiver address shift offset
 * return: 0 on success; -1 on error
 */
static int config_directive(struct config *cfg, const char *protocol_in,
	const char *port_in, const char *range, const char *protocol_out,
	const char *port_out, const char *shift, const char *auth_out)
{
	struct channel *chn_in, *chn_out;
	struct ccreader *reader;
	struct ccwriter *writer;
	struct ccnode *node;

	log_println(cfg->log, "config: %s %s %s -> %s %s %s", protocol_in,
		port_in, range, protocol_out, port_out, shift);
	chn_in = config_get_channel(cfg, port_in, FLAG_LISTEN);
	if(chn_in == NULL)
		goto fail;
	if(chn_in->reader == NULL) {
		reader = cl_pool_alloc(&cfg->reader_pool);
		ccreader_init(reader, chn_in->name, cfg->log, protocol_in);
		chn_in->reader = reader;
	} else {
		/* FIXME: check for redefined protocol */
		reader = chn_in->reader;
	}
	chn_out = config_get_channel(cfg, port_out, 0);
	if(chn_out == NULL)
		goto fail;
	writer = config_create_writer(cfg);
	ccwriter_init(writer, chn_out, protocol_out, auth_out);
	writer->defer = cfg->defer;
	node = cl_pool_alloc(&cfg->node_pool);
	ccreader_add_writer(reader, node, writer, range, shift);
	return 0;
fail:
	return -1;
}

/*
 * config_skip_comments		Remove comments from the line being parsed.
 */
static void config_skip_comments(struct config *cfg) {
	int i;
	for(i = 0; i < LINE_LENGTH; i++) {
		if(cfg->line[i] == '#')
			cfg->line[i] = '\0';
	}
}

/*
 * config_scan_directive	Parse one directive in the configuration.
 *
 * return: 0 on success; -1 on error
 */
static int config_scan_directive(struct config *cfg) {
	int i;
	char protocol_in[16], protocol_out[16];
	char port_in[32], port_out[32];
	char range[8], shift[8];
	char auth_out[32];

	shift[0] = '\0';
	auth_out[0] = '\0';

	i = sscanf(cfg->line, "%15s %31s %7s %15s %31s %7s %31s", protocol_in,
		port_in, range, protocol_out, port_out, shift, auth_out);
	if(i == 5)
		strcpy(shift, "0");
	if(i >= 5)
		return config_directive(cfg, protocol_in, port_in, range,
			protocol_out, port_out, shift, auth_out);
	else if(i <= 0)
		return 0;
	else {
		log_println(cfg->log, "Invalid directive: %s", cfg->line);
		return -1;
	}
}

/*
 * config_read		Read the configuration file.
 *
 * filename: name of the configuration file
 * return: number of channels created by the configuration
 */
int config_read(struct config *cfg, const char *filename) {
	FILE *f = fopen(filename, "r");
	if(f == NULL)
		return -1;

	while(fgets(cfg->line, LINE_LENGTH, f) == cfg->line) {
		config_skip_comments(cfg);
		if(config_scan_directive(cfg))
			goto fail;
	}
	if(cfg->n_channels == 0) {
		log_println(cfg->log, "Error reading configuration file: %s",
			filename);
	}
	fclose(f);
	return cfg->n_channels;
fail:
	fclose(f);
	return -1;
}

/*
 * config_cede_channels		Cede ownership of channel array memory.
 *
 * return: ceded pointer to channel list
 */
struct channel *config_cede_channels(struct config *cfg) {
	struct channel *chn = cfg->chns;
	cfg->n_channels = 0;
	cfg->chns = NULL;
	return chn;
}

/*
 * config_verify	Verify the configuration file.
 *
 * filename: name of the configuration file
 * return: -1 on error; 0 on success
 */
int config_verify(const char *filename) {
	struct config cfg;
	int rc;

	if(config_init(&cfg, NULL) == NULL)
		return (errno ? errno : -1);
	if(config_read(&cfg, filename) <= 0)
		rc = (errno ? errno : -1);
	else
		rc = 0;
	config_destroy(&cfg);
	return rc;
}
