/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2009  Minnesota Department of Transportation
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

package us.mn.state.dot.sonar;

import junit.framework.TestCase;
import us.mn.state.dot.sonar.Name;

/** 
 * Name test cases
 * @author Michael Darter, AHMCT
 * @created 04/20/09
 */
public class NameTest extends TestCase {

	/** constructor */
	public NameTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {

		// simple tests of Name functionality
		TestObj o = new TestObjImpl("aaa");
		Name n = new Name(o);
		//System.err.println(n.toString());
		assertTrue(!n.isRoot());
		assertTrue(!n.isType());
		assertTrue(n.isObject());
		assertTrue(!n.isAttribute());
		assertTrue(n.getTypePart().equals("testobj"));
		assertTrue(n.getObjectPart().equals("aaa"));
		assertTrue(n.getAttributePart().equals(""));
	}
}
