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
// File        :  DetectorData.java
//
// Description :  This class defines the detector data objects created from 
//                the standard file format used by MN/DOT.
//
// ----------------------------------------------------------------------------


// Class to keep track of one piece of detector data
public class DetectorData implements java.io.Serializable{

   // Private data
   private short volume = 0;
   private short occupancy = 0;
   private short status = 0;
   private short flag = 0;

   // Getter methods
   public final short getVolume() { return volume; };
   public final short getOccupancy() { return occupancy; };
   public final short getStatus() { return status; };
   public final short getFlag() {return flag;};

   // Setter methods
   public final void setVolume( short newVolume ) { volume = newVolume; };
   public final void setOccupancy( short newOccupancy ) {
      occupancy = newOccupancy; 
   };
   public final void setStatus( short newStatus ) { status = newStatus; };
   public final void setFlag( short newFlag ) { flag = newFlag; };
}
    


















