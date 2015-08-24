/*
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.util.xml;

import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlParser {

	protected static Document document;
	
	public XmlParser(URL url) throws InstantiationException {
		try{
			DocumentBuilder builder = 
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			URLConnection conn = url.openConnection();
			document = builder.parse(conn.getInputStream());
		}catch(Exception e){
			throw new InstantiationException(e.getMessage());
		}
	}

	protected void printElements(Document d){
		NodeList list = d.getChildNodes();
		printNodeList(list);
	}
	
	protected void printNodeList(NodeList list){
		for(int i=0; i<list.getLength(); i++){
			Node n = list.item(i);
			print(n);
			printNodeList(n.getChildNodes());
		}
	}

	protected void print(Node n){
		System.out.println("Node: " + n.getNodeName());
	}
}
