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

import org.springframework.batch.core.StepExecution;

/**
 * The Class StepExecutionHistory.
 * 
 * @author aymen.abbes@ext.mpsa.com
 */
public class StepExecutionHistory {

	/** The step name. */
	private final String stepName;

	/** The count. */
	private int count;

	/** The commit count. */
	private CumulativeHistory commitCount = new CumulativeHistory();

	/** The rollback count. */
	private CumulativeHistory rollbackCount = new CumulativeHistory();

	/** The read count. */
	private CumulativeHistory readCount = new CumulativeHistory();

	/** The write count. */
	private CumulativeHistory writeCount = new CumulativeHistory();

	/** The filter count. */
	private CumulativeHistory filterCount = new CumulativeHistory();

	/** The read skip count. */
	private CumulativeHistory readSkipCount = new CumulativeHistory();

	/** The write skip count. */
	private CumulativeHistory writeSkipCount = new CumulativeHistory();

	/** The process skip count. */
	private CumulativeHistory processSkipCount = new CumulativeHistory();

	/** The duration. */
	private CumulativeHistory duration = new CumulativeHistory();

	/** The duration per read. */
	private CumulativeHistory durationPerRead = new CumulativeHistory();

	/**
	 * Instantiates a new step execution history.
	 * 
	 * @param stepName
	 *            the step name
	 */
	public StepExecutionHistory(String stepName) {
		this.stepName = stepName;
	}

	/**
	 * Append.
	 * 
	 * @param stepExecution
	 *            the step execution
	 */
	public void append(StepExecution stepExecution) {
		if (stepExecution.getEndTime() == null) {
			// ignore unfinished executions
			return;
		}
		Date startTime = stepExecution.getStartTime();
		Date endTime = stepExecution.getEndTime();
		long time = endTime.getTime() - startTime.getTime();
		duration.append(time);
		if (stepExecution.getReadCount() > 0) {
			durationPerRead.append(time / stepExecution.getReadCount());
		}
		count++;
		commitCount.append(stepExecution.getCommitCount());
		rollbackCount.append(stepExecution.getRollbackCount());
		readCount.append(stepExecution.getReadCount());
		writeCount.append(stepExecution.getWriteCount());
		filterCount.append(stepExecution.getFilterCount());
		readSkipCount.append(stepExecution.getReadSkipCount());
		writeSkipCount.append(stepExecution.getWriteSkipCount());
		processSkipCount.append(stepExecution.getProcessSkipCount());
	}

	/**
	 * Gets the step name.
	 * 
	 * @return the step name
	 */
	public String getStepName() {
		return stepName;
	}

	/**
	 * Gets the count.
	 * 
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Gets the commit count.
	 * 
	 * @return the commit count
	 */
	public CumulativeHistory getCommitCount() {
		return commitCount;
	}

	/**
	 * Gets the rollback count.
	 * 
	 * @return the rollback count
	 */
	public CumulativeHistory getRollbackCount() {
		return rollbackCount;
	}

	/**
	 * Gets the read count.
	 * 
	 * @return the read count
	 */
	public CumulativeHistory getReadCount() {
		return readCount;
	}

	/**
	 * Gets the write count.
	 * 
	 * @return the write count
	 */
	public CumulativeHistory getWriteCount() {
		return writeCount;
	}

	/**
	 * Gets the filter count.
	 * 
	 * @return the filter count
	 */
	public CumulativeHistory getFilterCount() {
		return filterCount;
	}

	/**
	 * Gets the read skip count.
	 * 
	 * @return the read skip count
	 */
	public CumulativeHistory getReadSkipCount() {
		return readSkipCount;
	}

	/**
	 * Gets the write skip count.
	 * 
	 * @return the write skip count
	 */
	public CumulativeHistory getWriteSkipCount() {
		return writeSkipCount;
	}

	/**
	 * Gets the process skip count.
	 * 
	 * @return the process skip count
	 */
	public CumulativeHistory getProcessSkipCount() {
		return processSkipCount;
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
	 * Gets the duration per read.
	 * 
	 * @return the duration per read
	 */
	public CumulativeHistory getDurationPerRead() {
		return durationPerRead;
	}

}
