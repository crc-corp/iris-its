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

import socket
import select

class Server(object):

	def __init__(self, port):
		self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.socket.bind(('', port))
		self.socket.listen(10)
		self.poll = select.poll()
		self.clients = {}

	def do_write(self, fd):
		c = self.clients[fd]
		try:
			if c.flush():
				self.poll.register(c.fd, select.POLLIN)
		except socket.error, data:
			print 'SERVER => Socket error:', data
			self.disconnect(c)

	def do_read(self, fd):
		if fd == self.socket.fileno():
			self.accept()
			return
		c = self.clients[fd]
		try:
			c.receive()
		except socket.error, data:
			print 'SERVER => Socket error:', data
			self.disconnect(c)

	def do_work(self):
		for fd, event in self.poll.poll():
			if event == select.POLLOUT:
				self.do_write(fd)
			if event == select.POLLIN:
				self.do_read(fd)
			if event == select.POLLHUP or event == select.POLLERR:
				c = self.clients[fd]
				self.disconnect(c)

	def start(self):
		self.poll.register(self.socket.fileno())
		while True:
			self.do_work()

	def accept(self):
		sock, address = self.socket.accept()
		c = Connection(sock, address)
		self.poll.register(c.fd, select.POLLIN)
		self.clients[c.fd] = c

	def disconnect(self, c):
		del self.clients[c.fd]
		self.poll.unregister(c.fd)

class Connection(object):
	def __init__(self, socket, address):
		print 'connect from', address
		self.socket = socket
		self.address = address
		self.fd = socket.fileno()
		self.rbuf = ''
		self.xbuf = ''
		self.watch = {}
	def receive(self):
		data = self.socket.recv(40)
		if self.rbuf:
			rbuf = self.rbuf + data
		else:
			rbuf = data
		if '\n' in rbuf:
			i = rbuf.index('\n')
			print 'received:', rbuf[:i]
			rbuf = rbuf[i+1:]
		self.rbuf = rbuf
	def flush(self):
		print 'flushing data'
		return True

Server(12345).start()
