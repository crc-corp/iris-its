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
package us.mn.state.dot.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import us.mn.state.dot.util.XmlWriter;

/**
 * @author John3Tim
 *
 * This class reads the aries text file the Steve Misgen
 * provided and creates a system config file in xml that
 * the datatools can read.
 */
public class AriesReader {

	SimpleDateFormat formatter =
		new SimpleDateFormat( "EEE, dd MMM yyyy kk':'mm':'ss z" );

	protected Set<Zone> zones;
	
	public AriesReader(){
		zones = new TreeSet<Zone>(new Comparator(){
			public int compare(Object o1, Object o2){
				Zone z1 = (Zone)o1;
				Zone z2 = (Zone)o2;
				return new Integer(z1.getIndex()).compareTo(
						new Integer(z2.getIndex())); 
			}
		});
		try{
			File input = new File("aries.txt");
			FileReader fr = new FileReader(input);
			BufferedReader r = new BufferedReader(fr);
			File output = new File("arterials.xml");
			FileWriter fw = new FileWriter(output);
			BufferedWriter w = new BufferedWriter(fw);
			String l = r.readLine();
			Zone z = null;
			while(l != null){
				if(l.toLowerCase().indexOf("zone")>-1){
					z = createZone(l);
					zones.add(z);
				}else if(l.toLowerCase().indexOf("int")>-1){
					if(z != null){
						z.addIntersection(createIntersection(l));
					}
				}
				l = r.readLine();
			}
			w.write("<?xml version='1.0'?>\n");
			Calendar c = Calendar.getInstance();
			w.write("<arterials system='Arterials' time_stamp='" +
					formatter.format(c.getTime()) + "' detector_prefix='E' >\n");
			int detCount = 0;
			for(Zone zone : zones){
				detCount = zone.getDetectors();
				if(detCount == 0){
					System.out.println("Skipping zone " + zone.index + ": no detectors");
					continue;
				}
				System.out.println("Writing zone " + zone.getIndex());
				w.write("<zone index='" + zone.getIndex() +
						"' label='" +
						XmlWriter.toXML(zone.getLabel()) + "'>\n");
				for(int i=1; i<detCount+1; i++){
					System.out.println("  writing det " + i);
					w.write("\t<detector index='E" +
							zone.getIndex() + "_" + i +
							"' label='Detector " + i +
							"'/>\n");
				}
				w.write("</zone>\n");
			}
			w.write("</arterials>");
			w.flush();
			w.close();
			System.out.println("Finished.");
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	private Zone createZone(String s){
		System.out.println("Creating zone: " + s);
		s = s.trim();
		int index = Integer.parseInt(
				s.substring(
				s.indexOf("Zone") + 4,
				s.indexOf(':')).trim());
		String label = s.substring(
				s.indexOf(":") + 1).trim();
		label = removeDoubleSpaces(label);
		return new Zone(index, label);
	}

	private Intersection createIntersection(String s){
		System.out.println("  Creating intersection: " + s);
		s = s.trim();
		StringTokenizer t = new StringTokenizer(s, "\t: ", false);
		t.nextToken();//throw away the 'int' prefix
		int index = Integer.parseInt(t.nextToken());
		String label = "";
		while(t.hasMoreTokens()){
			label = label.concat(t.nextToken()) + " ";
		}
		label.trim();
		label = removeDoubleSpaces(label);
		return new Intersection(index, label);
	}

	private int getDetectorCount(final int zoneId){
		File v30Dir = new File("/tmp/20081008");
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.toLowerCase().endsWith("v30") &&
						name.toLowerCase().startsWith("e"+ zoneId + "_"));
			}
		};
		File[] v30Files = v30Dir.listFiles(filter);
		if(v30Files == null) return 0;
		return v30Files.length;
	}

	private String removeDoubleSpaces(String s){
		if(s.indexOf("  ")>-1){
			s = s.replaceAll("  ", " ");
			s = removeDoubleSpaces(s);
		}
		return s;
	}
	
	protected class Zone{
		private int index = -1;
		private String label = null;
		private ArrayList<Intersection> intersections =
			new ArrayList<Intersection>(); 
		private int detectors = 0;
		
		public Zone(int index, String label){
			this.index = index;
			this.label = label;
			detectors = getDetectorCount(index);
		}
		
		public int getDetectors(){
			return detectors;
		}

		public void addIntersection(Intersection i){
			if(i != null) intersections.add(i);
		}
		public Intersection[] getIntersections(){
			return (Intersection[])intersections.toArray(new Intersection[0]);
		}
		public int getIndex(){
			return index;
		}
		public String getLabel(){
			return label;
		}
	}
	
	public class Intersection{
		private String label = null;
		private int index = -1;
		
		public Intersection(int index, String label){
			this.index = index;
			this.label = label;
		}
		
		public int getIndex(){
			return index;
		}
		public String getLabel(){
			return label;
		}
	}
	
	public static void main(String[] args){
		new AriesReader();
	}
}
