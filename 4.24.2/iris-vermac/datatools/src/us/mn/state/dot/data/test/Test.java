/*
 * DataExtract
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.data.test;

import java.util.Calendar;

import us.mn.state.dot.data.PlotDetector;

/**
 * @author john3tim
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Test {

	public Test(){
		try{
/*			URL url = new URL("http://data.dot.state.mn.us:8080/dds/tms.xml.gz");
			SystemConfig[] configs = new SystemConfig[1];
			configs[0] = new TmsConfig("RTMC", url);
			HttpDataFactory fact = new HttpDataFactory(
				"http://@@DATATOOLS.TRAFDAT.HOST@@:8080/trafdat", configs);
			PlotDetector d1 = PlotDetector.createPlotDetector( fact, "D1788" );
			PlotDetector d2 = PlotDetector.createPlotDetector( fact, "D1789" );
			Sensor[] sensors = new Sensor[2];
			sensors[0] = new Sensor("D1788", null);
			sensors[0].setCategory( ' ' );
			sensors[1] = new Sensor("D1789", null);
			sensors[1].setCategory( ' ' );
			PlotDetector d3 = PlotDetector.createStationDetector( fact, "S341", sensors);
			printData( d1 );
			printData( d2 );
			printData( d3 );*/
		}catch( Exception e ){
			e.printStackTrace();
		}
	}
	
	private void printData( PlotDetector d ){
		Calendar c = Calendar.getInstance();
		float[] s = d.getSpeedSet( c );
		for( int i=0; i<6; i++ ){
			System.out.print( s[i] + " " );
		}
		System.out.println();
	}
	
	public static void main(String[] args) {
		new Test();
	}
}
