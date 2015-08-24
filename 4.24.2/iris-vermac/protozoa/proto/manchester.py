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

class Command(object):

	RECEIVER_HI = 0		# 2-bit HI receiver address
	RECEIVER_MI = 8		# 1-bit MI receiver address
	RECEIVER_LO = 18	# 5-bit LO receiver address

	FLAG = 7
	PAN_TILT_SPEED = 9
	FUNC_BITS = 9
	FUNCTION = 12		# 2-bit function indicator

	FUNC_LENS = 0		# lens control mask 00
	FUNC_AUX = 1		# auxilliary control mask 01
	FUNC_RECALL_PRESET = 2	# preset recall mask 10
	FUNC_STORE_PRESET = 3	# preset store mask 11

	PAN_TILT = 17

	def __init__(self):
		self.bits = BitArray(24)
		self.bits[Command.FLAG] = 1

	def get_octets(self):
		return self.bits.get_bytes()

	def __str__(self):
		return ':'.join(['%02X' % ord(c) for c in self.get_octets()])

	def set_receiver(self, receiver):
		assert receiver > 0 and receiver < 256
		r = receiver - 1
		self.bits.map_bits(self.RECEIVER_HI, 2, r >> 6)
		self.bits.map_bits(self.RECEIVER_MI, 1, r >> 5)
		self.bits.map_bits(self.RECEIVER_LO, 5, r)

	receiver = property(None, set_receiver)

class PanTiltCommand(Command):

	TILT_DOWN = 0	# tilt down bit mask 00
	TILT_UP = 1	# tilt up bit mask 01
	PAN_LEFT = 2	# pan left bit mask 10
	PAN_RIGHT = 3	# pan right bit mask 11

	def __init__(self):
		Command.__init__(self)
		self.bits[self.PAN_TILT] = 1

	def set_pan(self, pan):
		if pan < 0:
			self.bits.map_bits(self.FUNCTION, 2, self.PAN_LEFT)
		elif pan > 0:
			self.bits.map_bits(self.FUNCTION, 2, self.PAN_RIGHT)
		self.bits.map_bits(self.PAN_TILT_SPEED, 3, abs(pan))

	def set_tilt(self, tilt):
		if tilt < 0:
			self.bits.map_bits(self.FUNCTION, 2, self.TILT_DOWN)
		elif tilt > 0:
			self.bits.map_bits(self.FUNCTION, 2, self.TILT_UP)
		self.bits.map_bits(self.PAN_TILT_SPEED, 3, abs(tilt))

	pan = property(None, set_pan)
	tilt = property(None, set_tilt)

class LensCommand(Command):

	IRIS_OPEN = 1		# bit mask 001
	FOCUS_FAR = 2		# bit mask 010
	ZOOM_IN = 3		# bit mask 011
	IRIS_CLOSE = 4		# bit mask 100
	FOCUS_NEAR = 5		# bit mask 101
	ZOOM_OUT = 6		# bit mask 110

	def __init__(self):
		Command.__init__(self)
		self.bits[self.PAN_TILT] = 0
		self.bits.map_bits(self.FUNCTION, 2, self.FUNC_LENS)

	def set_zoom(self, zoom):
		if zoom > 0:
			self.bits.map_bits(self.FUNC_BITS, 3, self.ZOOM_IN)
		elif zoom < 0:
			self.bits.map_bits(self.FUNC_BITS, 3, self.ZOOM_OUT)

	def set_focus(self, focus):
		if focus < 0:
			self.bits.map_bits(self.FUNC_BITS, 3, self.FOCUS_NEAR)
		elif focus > 0:
			self.bits.map_bits(self.FUNC_BITS, 3, self.FOCUS_FAR)

	def set_iris(self, iris):
		if iris > 0:
			self.bits.map_bits(self.FUNC_BITS, 3, self.IRIS_OPEN)
		elif iris < 0:
			self.bits.map_bits(self.FUNC_BITS, 3, self.IRIS_CLOSE)

	zoom = property(None, set_zoom)
	focus = property(None, set_focus)
	iris = property(None, set_iris)

class AuxCommand(Command):

	AUX_MASK = {
		1: 2,	# aux 1 bit mask 010
		2: 4,	# aux 2 bit mask 100
		3: 6,	# aux 3 bit mask 110
		4: 3,	# aux 4 bit mask 011
		5: 5,	# aux 5 bit mask 101
		6: 7,	# aux 6 bit mask 111
	}

	def __init__(self):
		Command.__init__(self)
		self.bits[self.PAN_TILT] = 0
		self.bits.map_bits(self.FUNCTION, 2, self.FUNC_AUX)

	def set_aux(self, aux):
		assert aux > 0 and aux <= 6
		self.bits.map_bits(self.FUNC_BITS, 3, self.AUX_MASK[aux])

	aux = property(None, set_aux)

def repeat(out, command):
	o = command.get_octets()
	while True:
		out.write(o)
		print str(command)
		sleep(0.150)

s = Serial(0, 9600)

cmnd = PanTiltCommand()
cmnd.pan = -6
cmnd.tilt = 6
#cmnd = LensCommand()
#cmnd.zoom = 1
cmnd.receiver = 1

repeat(s, cmnd)
