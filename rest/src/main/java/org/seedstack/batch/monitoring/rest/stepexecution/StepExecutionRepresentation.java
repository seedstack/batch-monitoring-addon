/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.batch.monitoring.rest.stepexecution;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.batch.core.StepExecution;

/**
 * The Class StepExecutionRepresentation.
 *
 * @author aymen.abbes@ext.mpsa.com
 */
public class StepExecutionRepresentation {

	/** The date format. */
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	/** The time format. */
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	/** The duration format. */
	private SimpleDateFormat durationFormat = new SimpleDateFormat("HH:mm:ss");

	/** The id. */
	private Long id;

	/** The job execution id. */
	private Long jobExecutionId;

	/** The job name. */
	private String jobName;

	/** The name. */
	private String name;

	/** The start date. */
	private String startDate = "-";

	/** The start time. */
	private String startTime = "-";

	/** The duration. */
	private String duration = "-";

	/** The duration millis. */
	private long durationMillis;

	/** The end time. */
	private String endTime;

	/** The step execution details representaion. */
	private StepExecutionDetailsRepresentation stepExecutionDetailsRepresentation;

	/**
	 * Instantiates a new step execution representation.
	 *
	 * @param stepExecution
	 *            the step execution
	 * @param timeZone
	 *            the time zone
	 */
	public StepExecutionRepresentation(StepExecution stepExecution,
			TimeZone timeZone) {

		this.setStepExecutionDetailsRepresentation(new StepExecutionDetailsRepresentation(stepExecution));
		this.id = stepExecution.getId();
		this.name = stepExecution.getStepName();
		this.jobName = stepExecution.getJobExecution() == null
				|| stepExecution.getJobExecution().getJobInstance() == null ? "?"
				: stepExecution.getJobExecution().getJobInstance().getJobName();
		this.jobExecutionId = stepExecution.getJobExecutionId();
		// Duration is always in GMT
		durationFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		timeFormat.setTimeZone(timeZone);
		dateFormat.setTimeZone(timeZone);
		if (stepExecution.getStartTime() != null) {
			this.startDate = dateFormat.format(stepExecution.getStartTime());
			this.startTime = timeFormat.format(stepExecution.getStartTime());
			Date endTime = stepExecution.getEndTime() != null ? stepExecution
					.getEndTime() : new Date();

			this.durationMillis = endTime.getTime()
					- stepExecution.getStartTime().getTime();
			this.duration = durationFormat.format(new Date(durationMillis));
		}
		if (stepExecution.getEndTime() != null) {
			this.endTime = timeFormat.format(stepExecution.getEndTime());
		}

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
	 * Gets the job execution id.
	 *
	 * @return the job execution id
	 */
	public Long getJobExecutionId() {
		return jobExecutionId;
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
	 * Gets the job name.
	 *
	 * @return the job name
	 */
	public String getJobName() {
		return jobName;
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

	/**
	 * Gets the duration millis.
	 *
	 * @return the duration millis
	 */
	public long getDurationMillis() {
		return durationMillis;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public String getStatus() {
		if (id != null) {
			return getStepExecutionDetailsRepresentation().getStatus()
					.toString();
		}
		return "NONE";
	}

	/**
	 * Gets the exit code.
	 *
	 * @return the exit code
	 */
	public String getExitCode() {
		if (id != null) {
			return getStepExecutionDetailsRepresentation().getStatusExitCode();
		}
		return "NONE";
	}

	/**
	 * Gets the end time.
	 *
	 * @return the end time
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * Sets the end time.
	 *
	 * @param endTime
	 *            the new end time
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the new id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Sets the job execution id.
	 *
	 * @param jobExecutionId
	 *            the new job execution id
	 */
	public void setJobExecutionId(Long jobExecutionId) {
		this.jobExecutionId = jobExecutionId;
	}

	/**
	 * Sets the job name.
	 *
	 * @param jobName
	 *            the new job name
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the start date.
	 *
	 * @param startDate
	 *            the new start date
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * Sets the start time.
	 *
	 * @param startTime
	 *            the new start time
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	/**
	 * Sets the duration.
	 *
	 * @param duration
	 *            the new duration
	 */
	public void setDuration(String duration) {
		this.duration = duration;
	}

	/**
	 * Sets the duration millis.
	 *
	 * @param durationMillis
	 *            the new duration millis
	 */
	public void setDurationMillis(long durationMillis) {
		this.durationMillis = durationMillis;
	}

	/**
	 * Gets the step execution details representaion.
	 *
	 * @return the step execution details representaion
	 */
	public StepExecutionDetailsRepresentation getStepExecutionDetailsRepresentation() {
		return stepExecutionDetailsRepresentation;
	}

	/**
	 * Sets the step execution details representaion.
	 *
	 * @param stepExecutionDetailsRepresentaion
	 *            the new step execution details representaion
	 */
	public void setStepExecutionDetailsRepresentation(
			StepExecutionDetailsRepresentation stepExecutionDetailsRepresentaion) {
		this.stepExecutionDetailsRepresentation = stepExecutionDetailsRepresentaion;
	}
}