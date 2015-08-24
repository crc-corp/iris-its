/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.sched;

import junit.framework.TestCase;

/** 
 * Job tests
 *
 * @author Doug Lau
 */
public class JobTest extends TestCase {

	protected final Scheduler scheduler = new Scheduler();

	public JobTest(String name) {
		super(name);
	}

	public void test() {
		long start = System.currentTimeMillis();
		Job job = new Job(500) {
			public void perform() {}
		};
		scheduler.addJob(job);
		try {
			job.waitForCompletion(30000);
		}
		catch(java.util.concurrent.TimeoutException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		long end = System.currentTimeMillis();
		System.err.println("elapsed: " + (end - start));
		assertTrue(end >= start + 500);
	}
}
