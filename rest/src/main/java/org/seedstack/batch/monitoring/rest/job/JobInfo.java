/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.batch.monitoring.rest.job;

/**
 * The Class JobRepresentation.
 * 
 * @author aymen.abbes@ext.mpsa.com
 */
public class JobInfo {

	/** The name. */
	private final String name;

	/** The execution count. */
	private final int executionCount;

	/** The launchable. */
	private boolean launchable = false;

	/** The incrementable. */
	private boolean incrementable = false;

	/** The job instance id. */
	private final Long jobInstanceId;

	/**
	 * Instantiates a new job representation.
	 * 
	 * @param name
	 *            the name
	 * @param executionCount
	 *            the execution count
	 */
	public JobInfo(String name, int executionCount) {
		this(name, executionCount, false);
	}

	/**
	 * Instantiates a new job representation.
	 * 
	 * @param name
	 *            the name
	 * @param executionCount
	 *            the execution count
	 * @param launchable
	 *            the launchable
	 */
	public JobInfo(String name, int executionCount, boolean launchable) {
		this(name, executionCount, null, launchable, false);
	}

	/**
	 * Instantiates a new job representation.
	 * 
	 * @param name
	 *            the name
	 * @param executionCount
	 *            the execution count
	 * @param launchable
	 *            the launchable
	 * @param incrementable
	 *            the incrementable
	 */
	public JobInfo(String name, int executionCount,
			boolean launchable, boolean incrementable) {
		this(name, executionCount, null, launchable, incrementable);
	}

	/**
	 * Instantiates a new job representation.
	 * 
	 * @param name
	 *            the name
	 * @param executionCount
	 *            the execution count
	 * @param jobInstanceId
	 *            the job instance id
	 * @param launchable
	 *            the launchable
	 * @param incrementable
	 *            the incrementable
	 */
	public JobInfo(String name, int executionCount,
			Long jobInstanceId, boolean launchable, boolean incrementable) {
		super();
		this.name = name;
		this.executionCount = executionCount;
		this.jobInstanceId = jobInstanceId;
		this.launchable = launchable;
		this.incrementable = incrementable;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the execution count.
	 * 
	 * @return the execution count
	 */
	public int getExecutionCount() {
		return executionCount;
	}

	/**
	 * Gets the job instance id.
	 * 
	 * @return the job instance id
	 */
	public Long getJobInstanceId() {
		return jobInstanceId;
	}

	/**
	 * Checks if is launchable.
	 * 
	 * @return true, if is launchable
	 */
	public boolean isLaunchable() {
		return launchable;
	}

	/**
	 * Checks if is incrementable.
	 * 
	 * @return true, if is incrementable
	 */
	public boolean isIncrementable() {
		return incrementable;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

}