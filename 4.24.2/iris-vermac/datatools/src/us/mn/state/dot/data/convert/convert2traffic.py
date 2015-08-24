#!/usr/bin/env python

def convert_one_day( year, month, day ):
	import os
	os.system( 'java Converter ' + year + month + day + ' /data' )

def convert_one_month( year, month ):
	for d in range( 31 ):
		day = str( d + 1 )
		if len( day ) < 2:
			day = '0' + day
		convert_one_day( year, month, day )

def convert_three_months( year, start ):
	for m in range( 3 ):
		month = str( start + m )
		if len( month ) < 2:
			month = '0' + month
		convert_one_month( year, month )
		
def convert_six_months( year, start ):
	for m in range( 6 ):
		month = str( start + m )
		if len( month ) < 2:
			month = '0' + month
		convert_one_month( year, month )
