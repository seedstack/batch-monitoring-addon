/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.batch.monitoring.rest.jobexecution;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.support.PropertiesConverter;

/**
 * The Class JobExecutionRepresentation.
 * 
 * @author aymen.abbes@ext.mpsa.com
 */
public class JobExecutionInfo {

	/** The date format. */
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	/** The time format. */
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	/** The duration format. */
	private SimpleDateFormat durationFormat = new SimpleDateFormat("HH:mm:ss");

	/** The id. */
	private Long id;

	/** The step execution count. */
	private int stepExecutionCount;

	/** The job id. */
	private Long jobId;

	/** The job name. */
	private String jobName;

	/** The start date. */
	private String startDate = "";

	/** The start time. */
	private String startTime = "";

	/** The duration. */
	private String duration = "";

	// private JobExecution jobExecution;

	/** The job parameters. */
	private String jobParameters;

	/** The restartable. */
	private boolean restartable = false;

	/** The abandonable. */
	private boolean abandonable = false;

	/** The stoppable. */
	private boolean stoppable = false;

	/** The converter. */
	private JobParametersConverter converter = new DefaultJobParametersConverter();

	/** The time zone. */
	private final TimeZone timeZone;

	/** The exit status. */
	private ExitStatus exitStatus = ExitStatus.UNKNOWN;

	/**
	 * Instantiates a new job execution representation.
	 * 
	 * @param jobExecution
	 *            the job execution
	 * @param timeZone
	 *            the time zone
	 */
	public JobExecutionInfo(JobExecution jobExecution,
			TimeZone timeZone) {

		// this.jobExecution = jobExecution;
		this.timeZone = timeZone;
		this.id = jobExecution.getId();
		this.jobId = jobExecution.getJobId();
		this.stepExecutionCount = jobExecution.getStepExecutions().size();

		JobInstance jobInstance = jobExecution.getJobInstance();
		if (jobInstance != null) {
			this.jobName = jobInstance.getJobName();
			this.exitStatus = jobExecution.getExitStatus();
			Properties properties = converter.getProperties(jobExecution
					.getJobParameters());
			this.jobParameters = PropertiesConverter
					.propertiesToString(properties);
			BatchStatus status = jobExecution.getStatus();
			this.restartable = status.isGreaterThan(BatchStatus.STOPPING)
					&& status.isLessThan(BatchStatus.ABANDONED);
			this.abandonable = status.isGreaterThan(BatchStatus.STARTED)
					&& status != BatchStatus.ABANDONED;
			this.stoppable = status.isLessThan(BatchStatus.STOPPING);
		} else {
			this.jobName = "?";
			this.jobParameters = null;
		}

		// Duration is always in GMT
		durationFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		// The others can be localized
		timeFormat.setTimeZone(timeZone);
		dateFormat.setTimeZone(timeZone);
		if (jobExecution.getStartTime() != null) {
			this.startDate = dateFormat.format(jobExecution.getStartTime());
			this.startTime = timeFormat.format(jobExecution.getStartTime());
			Date endTime = jobExecution.getEndTime() != null ? jobExecution
					.getEndTime() : new Date();
			this.duration = durationFormat.format(new Date(endTime.getTime()
					- jobExecution.getStartTime().getTime()));
		}

	}

	/**
	 * Gets the time zone.
	 * 
	 * @return the time zone
	 */
	public TimeZone getTimeZone() {
		return timeZone;
	}

	/**
	 * Gets the JobName.
	 * 
	 * @return the JobName
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Gets the step execution count.
	 * 
	 * @return the step execution count
	 */
	public int getStepExecutionCount() {
		return stepExecutionCount;
	}

	/**
	 * Gets the job id.
	 * 
	 * @return the job id
	 */
	public Long getJobId() {
		return jobId;
	}

	/**
	 * Gets the start date.
	 * 
	 * @return the start date
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * Gets the start time.
	 * 
	 * @return the start time
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * Gets the duration.
	 * 
	 * @return the duration
	 */
	public String getDuration() {
		return duration;
	}

	// public JobExecution getJobExecution() {
	// return jobExecution;
	// }

	/**
	 * Checks if is restartable.
	 * 
	 * @return true, if is restartable
	 */
	public boolean isRestartable() {
		return restartable;
	}

	/**
	 * Checks if is abandonable.
	 * 
	 * @return true, if is abandonable
	 */
	public boolean isAbandonable() {
		return abandonable;
	}

	/**
	 * Checks if is stoppable.
	 * 
	 * @return true, if is stoppable
	 */
	public boolean isStoppable() {
		return stoppable;
	}

	/**
	 * Gets the job parameters.
	 * 
	 * @return the job parameters
	 */
	public String getJobParameters() {
		return jobParameters;
	}

	/**
	 * Gets the exit status.
	 * 
	 * @return the exit status
	 */
	public ExitStatus getExitStatus() {
		return exitStatus;
	}

	/**
	 * Sets the exit status.
	 * 
	 * @param exitStatus
	 *            the new exit status
	 */
	public void setExitStatus(ExitStatus exitStatus) {
		this.exitStatus = exitStatus;
	}

}