/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.data.convert;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
  * Converter: A program to convert old 30-second and/or 5-minute traffic data
  * files to the new .traffic data file format.
  *
  * @author Douglas Lau
  */
public class Converter {

	/** 30-second data file (old file format) */
	protected DataFile30Second file30;

	/** 5-minute data file (old file format) */
	protected DataFile5Minute file5;

	/** Traffic output file name */
	protected final File traffic;

	/** File output stream to write out the new .traffic file */
	protected final FileOutputStream fos;

	/** Zip output stream to compress data in the new file */
	protected final ZipOutputStream zos;

	/** Data output stream to write traffic data to the new file */
	protected final DataOutputStream dos;

	/** Volume data for one detector */
	protected final byte[] volume = new byte[ 2880 ];

	/** 24-hour accumulator for all 30-second volumes */
	protected int v30 = 0;

	/** 24-hour accumulator for all 5-minute volumes */
	protected int v5 = 0;

	/** Occupancy data for one detector */
	protected final short[] occupancy = new short[ 2880 ];

	/** Detector count */
	protected int detectors = 0;

	/** Time stamp for file creation */
	protected final long stamp;

	/** Create a new data file converter */
	public Converter( String date, String root )
		throws NumberFormatException, IOException
	{
		int year = Integer.parseInt( date.substring( 0, 4 ) );
		int month = Integer.parseInt( date.substring( 4, 6 ) ) - 1;
		int day = Integer.parseInt( date.substring( 6 ) );
		GregorianCalendar cal =
			new GregorianCalendar( year, month, day );
		stamp = cal.getTime().getTime();
		try {
			String f = root + File.separator + "det" +
				File.separator + date + ".DET";
			System.out.print( "Input file: " + f + " -- " );
			file30 = new DataFile30Second( f );
			detectors = file30.getCount();
			System.out.println( "OK" );
		}
		catch( IOException e ) {
			file30 = null;
			System.out.println( "not found" );
		}
		try {
			String f = root + File.separator + "5mn" +
				File.separator + date + ".5MN";
			System.out.print( "Input file: " + f + " -- " );
			file5 = new DataFile5Minute( f );
			detectors = file5.getCount();
			System.out.println( "OK" );
		}
		catch( IOException e ) {
			file5 = null;
			System.out.println( "not found" );
			try {
				String f = root + File.separator + "5mn" +
					File.separator + date + ".5mn";
				System.out.print( "Input file: " + f + " -- " );
				file5 = new DataFile5Minute( f );
				detectors = file5.getCount();
				System.out.println( "OK" );
			}
			catch( IOException e2 ) {
				file5 = null;
				System.out.println( "not found" );
				if( file30 == null ) throw e;
			}
		}
		try {
			String f = root + File.separator + "traffic" +
				File.separator + date + ".traffic";
			System.out.print( "Output file: " + f + " -- " );
			traffic = new File( f );
			if( traffic.exists() ) {
				System.out.println( "EXISTS" );
				System.exit( -1 );
			}
			fos = new FileOutputStream( traffic );
			System.out.println( "OK" );
		}
		catch( IOException e ) {
			System.out.println( "error" );
			throw e;
		}
		zos = new ZipOutputStream( fos );
		zos.setMethod( ZipOutputStream.DEFLATED );
		zos.setLevel( Deflater.BEST_COMPRESSION );
		dos = new DataOutputStream( zos );
		resetData();
	}

	/** Close all the files */
	protected void close() {
		try {
			zos.close();
			fos.close();
			if( file5 != null ) file5.close();
			if( file30 != null ) file30.close();
			traffic.setLastModified( stamp );
		}
		catch( IOException e ) {
			e.printStackTrace();
		}
	}

	/** Finalize the converter */
	protected void finalize() {
		close();
	}

	/** Reset the volume and occupancy data */
	protected void resetData() {
		for( int i = 0; i < 2880; i++ ) {
			volume[ i ] = (byte)-1;
			occupancy[ i ] = -1;
		}
		v30 = 0;
		v5 = 0;
	}

	/** Read the 30-second data for a given detector */
	protected boolean read30SecondData( short d ) throws IOException {
		if( file30 == null ) return false;
		boolean exists = false;
		file30.setRecordRange( (short)0, (short)2879 );
		file30.addDetector( d );
		file30.readDataSet();
		DetectorDataSet set = file30.getDetectorDataSet( 0 );
		for( int rec = 0; rec < 2880; rec++ ) {
			DetectorData data = set.getData( rec );
			int status = data.getStatus();
			if( status == 0 || status == 1 ) exists = true;
			else continue;
			short vol = data.getVolume();
			short occ = data.getOccupancy();
			volume[ rec ] = (byte)( vol & 0xFF );
			occupancy[ rec ] = occ;
		}
		return exists;
	}

	/** Read the 5-minute data for a given detector */
	protected boolean read5MinuteData( short d ) throws IOException {
		if( file5 == null ) return false;
		boolean exists = false;
		file5.setRecordRange( (short)0, (short)287 );
		file5.addDetector( d );
		file5.readDataSet();
		DetectorDataSet set = file5.getDetectorDataSet( 0 );
		for( int rec = 0; rec < 288; rec++ ) {
			DetectorData data = set.getData( rec );
			int status = data.getStatus();
			if( status == 0 || status == 1 ) exists = true;
			else continue;
			short vol = data.getVolume();
			v5 += vol;
			short occ = data.getOccupancy();
			merge5MinuteData( rec, vol, occ );
		}
		for( int rec = 0; rec < 2880; rec++ )
			if( volume[ rec ] > 0 ) v30 += volume[ rec ];
		return exists;
	}

	/** Merge 5-minute data with existing 30-second data */
	protected void merge5MinuteData( int rec5, short vol5, short occ5 ) {
		int rec30 = rec5 * 10;
		int vol30 = 0;
		int occ30 = 0;
		int missing = 0;
		for( int i = rec30; i < rec30 + 10; i++ ) {
			if( volume[ i ] < 0 ) {
				missing++;
				continue;
			}
			vol30 += volume[ i ];
			occ30 += occupancy[ i ];
		}
		if( missing < 1 ) return;
		int vmis5 = vol5 - vol30;
		if( vmis5 < 0 ) vmis5 = 0;
		byte vmis30 = (byte)( vmis5 / missing );
		byte vmod30 = (byte)( vmis5 % missing );
		int omis5 = 10 * occ5 - occ30;
		if( omis5 < 0 ) omis5 = 0;
		short omis30 = (short)( omis5 / missing );
		short omod30 = (short)( omis5 % missing );
		for( int i = rec30; i < rec30 + 10; i++ ) {
			if( volume[ i ] < 0 ) {
				volume[ i ] = vmis30;
				if( vmod30-- > 0 ) volume[ i ]++;
				if( vmis30 > 0 ) {
					occupancy[ i ] = omis30;
					if( omod30-- > 0 ) occupancy[ i ]++;
				}
				else {
					if( volume[ i ] == 0 ) omis5 = 0;
					occupancy[ i ] = (short)omis5;
					omis5 = 0;
				}
			}
		}
	}

	/** Convert the files to the .traffic format */
	public void convert() throws IOException {
		int dets = 0;
		for( short d = 1; d <= detectors; d++ ) {
			resetData();
			String det = String.valueOf( d );
			boolean d30 = read30SecondData( d );
			boolean d5 = read5MinuteData( d );
			if( !( d30 | d5 ) ) continue;
			dets++;
			if( v30 != v5 ) System.out.println( det + ": vol: " +
				( v5 - v30 ) );
			ZipEntry zip = new ZipEntry( det + ".v30" );
			zip.setTime( stamp );
			zos.putNextEntry( zip );
			dos.write( volume, 0, 2880 );
			zos.closeEntry();
			zip = new ZipEntry( det + ".o30" );
			zip.setTime( stamp );
			zos.putNextEntry( zip );
			for( int r = 0; r < 2880; r++ )
				dos.writeShort( occupancy[ r ] );
			zos.closeEntry();
		}
		System.out.println( "Total detectors: " + dets );
	}

	/** Main program method */
	static public void main( String[] args ) {
		try {
			Converter c = new Converter( args[ 0 ], args[ 1 ] );
			c.convert();
			c.close();
		}
		catch( ArrayIndexOutOfBoundsException e ) {
			System.out.println( "Syntax: java -jar convert.jar" +
				" [8-digit date] [path]" );
			System.exit( 0 );
		}
		catch( Exception e ) {
			e.printStackTrace();
			System.exit( -1 );
		}
	}
}
