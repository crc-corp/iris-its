/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;

/**
 * Job for the scheduler to perform.  When the scheduler is ready, the perform
 * method is called.  The perform method must be implemented by any subclasses.
 *
 * @author Douglas Lau
 */
abstract public class Job implements Comparable<Job> {

	/** Calculate a time interval from a Calendar field and value.
	 * @param field java.util.Calendar field constant.
	 * @param value Amount of specified field.
	 * @return Number of milliseconds in interval. */
	static private long calculateInterval(int field, int value) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0);
		c.add(field, value);
		return c.getTimeInMillis();
	}

	/** Next available job identifier */
	static private long next_id = 0;

	/** Unique job identifier */
	private final long id = next_id++;

	/** Time interval to perform this job, in milliseconds.  For
	 * non-repeating jobs, this must be 0. */
	private final long interval;

	/** Time offset from whole interval boundary, in milliseconds. */
	private final long offset;

	/** Next time this job must be performed */
	private long next_time;

	/** Count of how many times the job has completed */
	private int n_complete = 0;

	/**
	 * Create a new scheduler job.
	 * @param iField java.util.Calendar field for time interval
	 * @param i Time interval to schedule the job
	 * @param oField java.util.Calendar field for interval offset
	 * @param o Time interval offset to schedule the job
	 */
	public Job(int iField, int i, int oField, int o) {
		assert i >= 0;
		assert o >= 0;
		interval = calculateInterval(iField, i);
		offset = calculateInterval(oField, o);
		assert offset < interval;
		computeNextTime();
	}

	/**
	 * Create a new scheduler job.
	 * @param iField java.util.Calendar field for time interval
	 * @param i Time interval to schedule the job
	 */
	public Job(int iField, int i) {
		this(iField, i, Calendar.SECOND, 0);
	}

	/** Create a one-shot scheduler job */
	public Job(int milliseconds) {
		interval = 0;
		offset = 0;
		next_time = TimeSteward.currentTimeMillis() + milliseconds;
	}

	/** Create a one-shot job to schedule immediately */
	public Job() {
		this(0);
	}

	/** Check if this is a repeating job */
	public boolean isRepeating() {
		return interval > 0;
	}

	/** Get the delay time before performing the job, in milliseconds */
	public long delay() {
		return next_time - TimeSteward.currentTimeMillis();
	}

	/** Compute the next time this job will be scheduled.  Warning: the
	 * sort order for the Comparable interface will change. */
	void computeNextTime() {
		next_time = computePastTime() + interval;
	}

	/** Compute the most recently past scheduled time */
	private long computePastTime() {
		long now = TimeSteward.currentTimeMillis();
		// We need to adjust the offset time by the time zone offset
		// and the DST offset so that daily jobs are performed at the
		// proper hour during DST changes.
		long off = offset - TimeZone.getDefault().getOffset(now);
		return (now - off) / interval * interval + off;
	}

	/** Perform the task for this job */
	void performTask() throws Exception {
		try {
			perform();
		}
		finally {
			complete();
			synchronized(this) {
				n_complete++;
				notify();
			}
		}
	}

	/** Actual "job" to be performed */
	abstract public void perform() throws Exception;

	/** Do this upon completion of the job */
	public void complete() {}

	/** Compare this job with another one */
	@Override
	public int compareTo(Job other) {
		long c = next_time - other.next_time;
		if(c == 0)
			c = interval - other.interval;
		if(c == 0)
			c = offset - other.offset;
		if(c == 0)
			c = id - other.id;
		if(c < 0)
			return -1;
		if(c > 0)
			return 1;
		return 0;
	}

	/** Wait for the job to complete.
	 * @param ms Time to wait before giving up.
	 * @throws TimeoutExcepiton if timeout expires before completion. */
	public synchronized void waitForCompletion(long ms)
		throws TimeoutException
	{
		long waited = 0;
		while(n_complete == 0) {
			if(waited >= ms)
				throw new TimeoutException();
			long w = Math.min(100, ms - waited);
			try {
				TimeSteward.wait(this, w);
				waited += w;
			}
			catch(InterruptedException e) {
				// keep waiting
			}
		}
	}

	/** Get the name of the job */
	public String getName() {
		return getClass().getSimpleName();
	}
}
