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

class Message(object):

	def get_octets(self):
		fields = [self.code] + self.params
		for f in fields:
			for c in f:
				if c == '\0' or c == '\v':
					raise ValueError, f
		return '\v'.join(fields) + '\0'

class Logon(Message):

	code = 'l'

	def __init__(self, name, password):
		self.params = [name, password]

class Quit(Message):

	code = 'q'

	def __init__(self):
		self.params = []

class Show(Message):

	code = 's'

	def __init__(self, text):
		self.params = [text]

class Enumerate(Message):

	code = 'e'

	def __init__(self, name):
		self.params = []
		if name:
			self.params.append(name)

class Ignore(Message):

	code = 'i'

	def __init__(self, name):
		self.params = [name]

class Add(Message):

	code = 'a'

	def __init__(self, name, *params):
		self.params = [name] + list(params)

class Remove(Message):

	code = 'r'

	def __init__(self, name):
		self.params = [name]

class Update(Message):

	code = 'u'

	def __init__(self, name):
		self.params = []
		if name:
			self.params.append(name)

# Initialize the dict of message codes
_MESSAGES = [Logon, Quit, Show, Enumerate, Ignore, Add, Remove, Update]
_CODES = {}
for m in _MESSAGES:
	_CODES[m.code] = m
del m

def parse(octets):
	if octets[-1] == '\0':
		octets = octets[:-1]
	fields = octets.split('\v')
	code = fields.pop(0)
	if code in _CODES:
		return _CODES[code](*fields)
	raise ParseError

if __name__ == '__main__':
	def test(m):
		octets = m.get_octets()
		m = parse(octets)
		print m, m.params
	test(Logon('user', 'password'))
	test(Quit())
	test(Show('help me!'))
	test(Enumerate('ramp_meter'))
	test(Ignore('dms'))
	test(Add('camera.C037'))
	test(Add('camera.C037', 'status', 'OK'))
	test(Remove('comm_line.93'))
	test(Update('lcs.L94W03'))
