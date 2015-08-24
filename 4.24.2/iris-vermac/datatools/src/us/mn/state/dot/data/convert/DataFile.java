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

// ----------------------------------------------------------------------------
//
// File        :  DataFile.java
//
// Description :  This is the base class for reading traffic data from the 
//                new standard file format used by MN/DOT.  For each file
//                type, there is a subclass for reading data, such as 
//                DataFile5Minute, DataFileStation, and DataFileMeter.
//
// ----------------------------------------------------------------------------
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;


// The DataFile class is a subclass of java.io.RandomAccessFile
public class DataFile extends RandomAccessFile {


   // Protected data
   protected String serverRev = null;
   protected String fileRev = null;
   protected int zero = 0;
   protected short count = 0;
   protected short size = 0;
   protected short intervals = 0;
   protected short type = 0;
   protected short year = 0;
   protected byte month = 0;
   protected byte day = 0;
   protected int recordSize = 0;
   protected short record = 0;
   protected int recordPos = 0;
   protected short startRecord = 0;
   protected short endRecord = 0;


   // Getter methods
   public final String getServerRev() { return serverRev; };
   public final String getFileRev() { return fileRev; };
   public final int getZero() { return zero; };
   public final short getCount() { return count; };
   public final short getSize() { return size; };
   public final short getIntervals() { return intervals; };
   public final short getType() { return type; };
   public final short getYear() { return year; };
   public final byte getMonth() { return month; };
   public final byte getDay() { return day; };
   public final short getRecord() { return record; };
   public final short getStartRecord() { return startRecord; };
   public final short getEndRecord() { return endRecord; };


   // Method to get a Date object for this file
   public final String getDate( int style ) {

      // Get a Calendar object
      Calendar calDay = Calendar.getInstance();

      // Set the date for the Calendar object to the date from the data file
      calDay.set( year, month - 1, day );

      // Get a date object from the calendar
      Date day = calDay.getTime();

      // Return a string representation of the date
      return DateFormat.getDateInstance( style ).format( day );
   }


   // Setter methods
   public final void setRecord( short newRecord ) { 
      record = newRecord; 
      recordPos = 34 + record * recordSize;
   }

   public final void setStartRecord( short newStartRecord ) {
      startRecord = newStartRecord;
   }

   public final void setEndRecord( short newEndRecord ) {
      endRecord = newEndRecord;
   } 


   // Constructor method, opens a traffic data file in read-only mode
   public DataFile( String fileName ) throws IOException {

      // Open the file in read-only random-access mode
      super( fileName, "r" );

      // Allocate a temporary byte array
      byte buffer[] = new byte [ 10 ];

      // Read the comm server revision from the header
      read( buffer, 0, 9 );

      // Create a string of the comm server rev
      serverRev = new String( buffer );

      // Read the data file revision from the header
      read( buffer, 0, 9 );

      // Create a string of the data file rev
      fileRev = new String( buffer );

      // Read the rest of the header
      zero = readInt();

      // If "zero" is not 0, this file is not in proper format
      if( zero != 0 ) throw new IOException( "File not in proper format" );

      count = (short)readByteSwappedUnsignedShort();
      size = (short)readByteSwappedUnsignedShort();
      intervals = (short)readByteSwappedUnsignedShort();
      type = (short)readByteSwappedUnsignedShort();
      year = (short)readByteSwappedUnsignedShort();
      month = readByte();
      day = readByte();

      // Compute the record size
      recordSize = count * size;
   }


   // Special method to read an unsigned short in byte-swapped format 
   public final int readByteSwappedUnsignedShort() throws IOException {

      // Read the unsigned short from the file (16 bits)
      int temp = readUnsignedShort();
       
      // Swap the bytes and return the value
      return ( temp >>> 8 ) | ( ( temp & 0xff ) << 8 );
   }
}
