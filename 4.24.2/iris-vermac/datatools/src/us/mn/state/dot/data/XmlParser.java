/*
 * Project: DataTools
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.data;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

/**
 * Parser for GZIP'd xml files
 *
 * @author Tim Johnson, Doug Lau
 * @author Michael Darter
 */
public class XmlParser {

	protected final Document document;

	protected final URL url;

	/** Create a new XML document parser */
	public XmlParser(URL url) throws ParserConfigurationException {
		this.url = url;
		document = parse();
	}

	/** Parse the XML document */
	protected Document parse() throws ParserConfigurationException {
		DocumentBuilder builder = DocumentBuilderFactory.
			newInstance().newDocumentBuilder();

		// try to open gzip stream
		try {
			URLConnection conn = url.openConnection();
			InputStream in = conn.getInputStream();
			return builder.parse(new GZIPInputStream(in));
		} catch(FileNotFoundException ex1) {
			System.err.println("Warning: XmlParser.Parse(): "+ex1);
			return null;
		} catch(UnknownHostException ex2) {
			System.err.println("Warning: XmlParser.Parse(): "+ex2);
			return null;

		// try to open as uncompressed stream
		}catch(Exception ex3) {
			try {
				URLConnection conn = url.openConnection();
				return builder.parse(conn.getInputStream());

			// FIXME: probably shouldn't catch this
			} catch(Exception ex4) {
				System.err.println(
					"XmlParser.Parse(): exception 4="+ex4);
				ex4.printStackTrace();
				return builder.newDocument();
			}
		}
	}

	/* return the document
	 * @return null On failure */
	public Document getDocument() {
		return document;
	}
}
