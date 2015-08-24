#!/usr/bin/env python
#
# protozoa -- CCTV transcoder / mixer for PTZ
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
#
from __future__ import division

from time import sleep
from serial import Serial
from bit_array import BitArray

class Header(object):

	RECEIVER_HI = 0
	FLAG = 7
	RECEIVER_LO = 8
	COMMAND = 12
	ACK_ALARM = 13
	EXTENDED = 14

	def __init__(self, n_bits):
		n_bits += 16
		self.bits = BitArray(n_bits)
		self.bits[Header.FLAG] = 1

	def set_receiver(self, receiver):
		assert receiver > 0 and receiver < 256
		self.bits.map_bits(Header.RECEIVER_HI, 4, receiver >> 4)
		self.bits.map_bits(Header.RECEIVER_LO, 4, receiver)

	receiver = property(None, set_receiver)

	def get_octets(self):
		return self.bits.get_bytes()

	def __str__(self):
		return ':'.join(['%02X' % ord(c) for c in self.get_octets()])

class Status(Header):

	def __init__(self):
		Header.__init__(self, 0)

class Command(Header):

	AUTO_IRIS = 17
	AUTO_PAN = 18
	TILT_DOWN = 19
	TILT_UP = 20
	PAN_RIGHT = 21
	PAN_LEFT = 22
	LENS_SPEED = 24
	IRIS_CLOSE = 25
	IRIS_OPEN = 26
	FOCUS_NEAR = 27
	FOCUS_FAR = 28
	ZOOM_IN = 29
	ZOOM_OUT = 30
	AUX_6 = 33
	AUX_5 = 34
	AUX_4 = 35
	AUX_3 = 36
	AUX_2 = 37
	AUX_1 = 38

	def __init__(self, n_bits=0):
		Header.__init__(self, 32 + n_bits)
		self.bits[Header.COMMAND] = 1

	def set_pan(self, pan):
		self.bits[Command.PAN_LEFT] = 0
		self.bits[Command.PAN_RIGHT] = 0
		if pan < 0:
			self.bits[Command.PAN_LEFT] = 1
		elif pan > 0:
			self.bits[Command.PAN_RIGHT] = 1

	def set_tilt(self, tilt):
		self.bits[Command.TILT_DOWN] = 0
		self.bits[Command.TILT_UP] = 0
		if tilt < 0:
			self.bits[Command.TILT_DOWN] = 1
		elif tilt > 0:
			self.bits[Command.TILT_UP] = 1

	def set_zoom(self, zoom):
		self.bits[Command.ZOOM_IN] = 0
		self.bits[Command.ZOOM_OUT] = 0
		if zoom > 0:
			self.bits[Command.ZOOM_IN] = 1
		elif zoom < 0:
			self.bits[Command.ZOOM_OUT] = 1

	def set_auto_iris(self, auto_iris):
		if auto_iris:
			self.bits[Command.AUTO_IRIS] = 1
		else:
			self.bits[Command.AUTO_IRIS] = 0

	def set_auto_pan(self, auto_pan):
		if auto_pan:
			self.bits[Command.AUTO_PAN] = 1
		else:
			self.bits[Command.AUTO_PAN] = 0

	def set_lens_speed(self, lens_speed):
		if lens_speed:
			self.bits[Command.LENS_SPEED] = 1
		else:
			self.bits[Command.LENS_SPEED] = 0

	def set_focus(self, focus):
		self.bits[Command.FOCUS_NEAR] = 0
		self.bits[Command.FOCUS_FAR] = 0
		if focus < 0:
			self.bits[Command.FOCUS_NEAR] = 1
		elif focus > 0:
			self.bits[Command.FOCUS_FAR] = 1

	def set_iris(self, iris):
		self.bits[Command.IRIS_OPEN] = 0
		self.bits[Command.IRIS_CLOSE] = 0
		if iris > 0:
			self.bits[Command.IRIS_OPEN] = 1
		elif iris < 0:
			self.bits[Command.IRIS_CLOSE] = 1

	def set_aux_1(self, aux):
		if aux:
			self.bits[Command.AUX_1] = 1
		else:
			self.bits[Command.AUX_1] = 0

	def set_aux_2(self, aux):
		if aux:
			self.bits[Command.AUX_2] = 1
		else:
			self.bits[Command.AUX_2] = 0

	def set_aux_3(self, aux):
		if aux:
			self.bits[Command.AUX_3] = 1
		else:
			self.bits[Command.AUX_3] = 0

	def set_aux_4(self, aux):
		if aux:
			self.bits[Command.AUX_4] = 1
		else:
			self.bits[Command.AUX_4] = 0

	def set_aux_5(self, aux):
		if aux:
			self.bits[Command.AUX_5] = 1
		else:
			self.bits[Command.AUX_5] = 0

	def set_aux_6(self, aux):
		if aux:
			self.bits[Command.AUX_6] = 1
		else:
			self.bits[Command.AUX_6] = 0

	pan = property(None, set_pan)
	tilt = property(None, set_tilt)
	zoom = property(None, set_zoom)
	auto_iris = property(None, set_auto_iris)
	auto_pan = property(None, set_auto_pan)
	lens_speed = property(None, set_lens_speed)
	focus = property(None, set_focus)
	iris = property(None, set_iris)
	aux_1 = property(None, set_aux_1)
	aux_2 = property(None, set_aux_2)
	aux_3 = property(None, set_aux_3)
	aux_4 = property(None, set_aux_4)
	aux_5 = property(None, set_aux_5)
	aux_6 = property(None, set_aux_6)

class SpeedCommand(Command):

	PAN_SPEED_HI = 48
	PAN_SPEED_LO = 56
	TILT_SPEED_HI = 64
	TILT_SPEED_LO = 72

	def __init__(self):
		Command.__init__(self, 32)
		self.bits[Header.EXTENDED] = 1

	def set_pan(self, pan):
		Command.set_pan(self, pan)
		self.bits.map_bits(self.PAN_SPEED_HI, 4, abs(pan) >> 7)
		self.bits.map_bits(self.PAN_SPEED_LO, 7, abs(pan))

	def set_tilt(self, tilt):
		Command.set_tilt(self, tilt)
		self.bits.map_bits(self.TILT_SPEED_HI, 4, abs(tilt) >> 7)
		self.bits.map_bits(self.TILT_SPEED_LO, 7, abs(tilt))

	pan = property(None, set_pan)
	tilt = property(None, set_tilt)

def repeat(out, command):
	o = command.get_octets()
	while True:
		out.write(o)
		print str(command)
		sleep(0.150)

s = Serial(0, 9600)

cmnd = SpeedCommand()
cmnd.receiver = 1
cmnd.pan = 200
cmnd.zoom = -1

repeat(s, cmnd)
