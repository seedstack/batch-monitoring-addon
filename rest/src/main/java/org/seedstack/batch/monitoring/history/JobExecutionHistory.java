/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.batch.monitoring.history;

import java.util.Date;

import org.springframework.batch.core.JobExecution;

/**
 * The Class JobExecutionHistory.
 * 
 * @author aymen.abbes@ext.mpsa.com
 */
public class JobExecutionHistory {

	/** The job name. */
	private final String jobName;

	/** The duration. */
	private CumulativeHistory duration = new CumulativeHistory();

	/**
	 * Instantiates a new job execution history.
	 * 
	 * @param jobName
	 *            the job name
	 */
	public JobExecutionHistory(String jobName) {
		this.jobName = jobName;
	}

	/**
	 * Gets the job name.
	 * 
	 * @return the job name
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * Gets the duration.
	 * 
	 * @return the duration
	 */
	public CumulativeHistory getDuration() {
		return duration;
	}

	/**
	 * Append.
	 * 
	 * @param jobExecution
	 *            the job execution
	 */
	public void append(JobExecution jobExecution) {
		if (jobExecution.getEndTime() == null) {
			// ignore unfinished executions
			return;
		}
		Date startTime = jobExecution.getStartTime();
		Date endTime = jobExecution.getEndTime();
		long time = endTime.getTime() - startTime.getTime();
		duration.append(time);
	}

}