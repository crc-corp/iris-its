# SONAR -- Simple Object Notification And Replication
# Copyright (C) 2006-2007  Minnesota Department of Transportation
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

import os
import select
import socket
import sys

def translate(c):
	if c == ' ':
		return '\v'
	elif c == '\n':
		return '\0'
	else:
		return c

def unvtabbify(c):
	if c == '\v':
		return ' '
	else:
		return c

class Client(object):

	def __init__(self, port):
		self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.socket.connect(('151.111.8.69', port))
		self.sfd = self.socket.fileno()
		self.poll = select.poll()
		self.ssl = socket.ssl(self.socket)
		self.rbuf = ''
		self.wbuf = ''
		self.infd = sys.stdin.fileno()

	def start(self):
		self.poll.register(self.sfd, select.POLLIN)
		self.poll.register(self.infd, select.POLLIN)
		while True:
			self.do_work()

	def do_work(self):
		for fd, ev in self.poll.poll():
			if fd == self.infd:
				wbuf = os.read(self.infd, 80)
				self.wbuf = ''.join(translate(c) for c in wbuf)
				self.poll.register(self.sfd)
			if fd == self.sfd:
				if ev == select.POLLOUT:
					self.do_write()
				if ev == select.POLLIN:
					self.do_read()
				if ev == select.POLLHUP or ev == select.POLLERR:
					self.socket.close()

	def do_read(self):
		while self._do_read():
			pass

	def _do_read(self):
		data = self.ssl.read()
		data = ''.join(unvtabbify(c) for c in data)
		if self.rbuf:
			rbuf = self.rbuf + data
		else:
			rbuf = data
		while '\0' in rbuf:
			i = rbuf.index('\0')
			print '=>', rbuf[:i]
			rbuf = rbuf[i+1:]
		self.rbuf = rbuf
		return bool(rbuf)

	def do_write(self):
		try:
			if self.ssl.write(self.wbuf):
				self.poll.register(self.sfd, select.POLLIN)
				self.wbuf = ''
		except socket.error, data:
			print 'SERVER => Socket error:', data
			self.socket.close()

Client(1037).start()
