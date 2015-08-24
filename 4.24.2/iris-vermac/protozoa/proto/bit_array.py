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
class BitArray(object):

	def __init__(self, n_bits):
		self.n_bits = n_bits
		self.octets = [0] * (1 + (n_bits - 1) // 8)

	def __str__(self):
		b = [str(self[i]) for i in range(self.n_bits)]
		return '[' + ','.join(b) + ']'

	def __setitem__(self, bit, value):
		byte = bit // 8
		octet = self.octets[byte]
		mask = 1 << (bit % 8)
		if value:
			octet |= mask
		else:
			octet &= (mask ^ 0xFF)
		self.octets[byte] = octet

	def __getitem__(self, bit):
		byte = bit // 8
		octet = self.octets[byte]
		bit8 = bit % 8
		return (octet >> bit8) & 0x01

	def get_bytes(self):
		return ''.join([chr(o) for o in self.octets])

	def map_bits(self, first, count, value):
		for i in range(count):
			v = (value >> i) & 0x01
			self[first + i] = v

if __name__ == '__main__':
	b = BitArray(20)
	b.map_bits(5, 3, 3)
	print str(b)
	print ':'.join(['%02X' % ord(b) for b in b.get_bytes()])
