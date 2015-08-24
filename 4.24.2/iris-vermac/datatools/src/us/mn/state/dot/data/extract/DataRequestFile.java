/*
 * DataExtract
 * Copyright (C) 2002-2008  Minnesota Department of Transportation
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
 */
package us.mn.state.dot.data.extract;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import us.mn.state.dot.data.XmlParser;
import us.mn.state.dot.util.XmlWriter;

/**
 * @author John3Tim
 *
 */
public class DataRequestFile extends File{

	public DataRequestFile( String name ){
		super( name );
	}

	public void writeData( DataRequest r ){
		PrintWriter writer = null;
		try{
			writer = new PrintWriter( new FileWriter( this ) );
			writer.println( "<?xml version=\"1.0\"?>" );
			writer.println( "<datarequest>" );
			writeDates( writer, r.getDates() );
			writeTimeRanges( writer, r.getTimeRanges() );
			writeSensors(writer, r.getSensorIds());
			writeDataSets( writer, r.getDataSets() );
			writeOutputParams( writer, r );
			writer.println( "</datarequest>" );
		}catch( Exception e ){
			e.printStackTrace();
		}finally{
			try{
				writer.flush();
				writer.close();
			}catch( Exception e2 ){}
		}
	}
	
	private void writeDates( PrintWriter writer, Collection<Calendar> dates ){
		for(Calendar c : dates){
			try{
				writer.println( "\t<calendar " +
					"date=\"" + c.get( Calendar.DATE ) + "\" " +
					"month=\"" + ( c.get( Calendar.MONTH ) + 1 ) + "\" " +
					"year=\"" + c.get( Calendar.YEAR ) + "\"/>" );
			}catch( Exception ex ){}
		}	
	}

	private void writeTimeRanges( PrintWriter writer, Collection<TimeRange> ranges ){
		for(TimeRange r : ranges){
			try{
				writer.println( "\t<timerange " +
					"begin=\"" + r.getStart() + "\" " +
					"end=\"" + r.getEnd() + "\" " +
					"smoothing=\"" + r.getSmoothing() + "\"/>" );
			}catch( Exception ex ){}
		}	
	}

	private void writeSensors( PrintWriter writer, Collection<String> detectors ){
		for(String id : detectors){
			try{
				writer.println( "\t<sensor id=\"" + id + "\"/>" );
			}catch( Exception e ){
				e.printStackTrace();
			}
		}
	}

	private void writeDataSets( PrintWriter writer, Collection<String> sets ){
		for(String dataSet : sets){
			try{
				writer.println( "\t<dataset id='" + dataSet + "'/>" );
			}catch( Exception e ){
				e.printStackTrace();
			}
		}
	}

	private void writeOutputParams( PrintWriter writer, DataRequest r ){
		try{
			String orientation = null;
			if( r.getFileFormat().getColumns().contains( "Times" ) ){
				orientation = "Columns";
			}else {
				orientation = "Rows";
			}
			String format = r.getFileFormat().toString().replaceAll( " ", "" );
			writer.println( "\t<output " +
				"format='" + format + "' " +
				"path='" + XmlWriter.toXML(r.getOutputDir().toString()) + File.separator + "' " +
				"time='" + orientation + "'>" );
			for(String dataElement : r.getFileFormat().getDataElements()){
				writer.println( "\t\t<option name='" + dataElement + "'/>" );
			}
			String [] files = r.getFileNames();
			for( int i=0; i<files.length; i++ ){
				writer.println( "\t\t<file name='" + files[ i ] + "'/>" );
			}
			writer.println( "\t</output>" );
		}catch( Exception e ){
			e.printStackTrace();
		}
	}

	public DataRequest readData(){
		DataRequest request = new DataRequest();
		try{
			XmlParser parser = new XmlParser(this.toURI().toURL());
			Document doc = parser.getDocument();
			Element requestInfo = doc.getDocumentElement();
			readDates(request, requestInfo.getElementsByTagName("calendar"));
			readTimeRanges(request, requestInfo.getElementsByTagName("timerange"));
			readDetectors(request, requestInfo.getElementsByTagName("sensor"));
			readDataSets(request, requestInfo.getElementsByTagName("dataset"));
			readOutputParams(request, requestInfo.getElementsByTagName("output"));
		}catch( Exception ex ){
			ex.printStackTrace();
		}
		return request;
	}
	
	private void readDates(DataRequest r, NodeList dates){
		for(int i=0; i<dates.getLength(); i++){
			try{
				Element e = (Element)dates.item(i);
				int year = Integer.parseInt(e.getAttribute("year"));
				int month =
					Integer.parseInt(e.getAttribute("month")) - 1;
				int date = Integer.parseInt(e.getAttribute("date"));
				Calendar c = Calendar.getInstance();
				c.set( year, month, date );
				r.addDate( c );
			}catch( Exception ex ){
				ex.printStackTrace();
			}
		}
	}

	private void readTimeRanges(DataRequest r, NodeList ranges){
		for(int i=0; i<ranges.getLength(); i++){
			try{
				Element e = (Element)ranges.item(i);
				int begin = Integer.parseInt(e.getAttribute("begin"));
				int end = Integer.parseInt(e.getAttribute("end"));
				int smoothing = 
					Integer.parseInt(e.getAttribute("smoothing"));
				r.addTimeRange(
					TimeRange.createTimeRange(begin, end, smoothing));
			}catch( Exception ex ){
				ex.printStackTrace();
			}
		}
	}

	private void readDetectors(DataRequest r, NodeList detectors){
		for(int i=0; i<detectors.getLength(); i++){
			Element e = (Element)detectors.item(i);
			r.addSensorId(e.getAttribute("id"));
		}
	}

	private void readDataSets(DataRequest r, NodeList sets){
		for(int i=0; i<sets.getLength(); i++){
			Element e = (Element)sets.item(i);
			try{
				r.addDataSet(e.getAttribute("id"));
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}

	private void readOutputParams(DataRequest r, NodeList outputs){
		for(int x=0; x<outputs.getLength(); x++){
			Element e = (Element)outputs.item(x);
			try{
				Class formatClass = Class.forName( 
					"us.mn.state.dot.data.extract." + e.getAttribute( "format" ) );
				FileFormat format = (FileFormat)( formatClass.newInstance() );
				NodeList optList = e.getElementsByTagName("option");
				Collection options = new Vector();
				for(int i=0; i< optList.getLength(); i++){
					Element option = (Element)optList.item(i);
					options.add(option.getAttribute("name"));
				}
				format.setDataElements( options );
				NodeList fileList = e.getElementsByTagName("file");
				for(int i=0; i<fileList.getLength(); i++){
					Element file = (Element)fileList.item(i);
					r.setFileName(file.getAttribute("name"));
				}
				r.setOutputDir(
						new File(e.getAttribute("path")));
				String time = e.getAttribute("time");
				Collection c = new Vector();
				c.add( "Times" );
				if( time.equals( "Columns" ) ){
					format.setColumns( c );
				}else {
					format.setRows( c );
				}
				r.setFileFormat( format );
			}catch( Exception ex ){
				ex.printStackTrace();
			}
		}
	}
}

