/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.batch.monitoring.rest.jobinstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.seedstack.batch.monitoring.rest.jobexecution.JobExecutionInfo;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;

import org.seedstack.batch.monitoring.util.JobParametersExtractor;

/**
 * The Class JobInstanceRepresentation.
 * 
 * @author aymen.abbes@ext.mpsa.com
 */
public class JobInstanceRepresentation {

	/** The job name. */
	private final String jobName;

	/** The id. */
	private final Long id;

	/** The job executions representations. */
	private final Collection<JobExecutionInfo> jobExecutionsInfos;

	/** The job parameters. */
	private final Properties jobParameters;

	/** The job parameters string. */
	private final String jobParametersString;

	/**
	 * Instantiates a new job instance representation.
	 * 
	 * @param jobName
	 *            the job name
	 * @param jobInstanceId
	 *            the job instance id
	 * @param jobParameters
	 *            the job parameters
	 * @param jobExecutions
	 *            the job executions
	 */
	public JobInstanceRepresentation(String jobName, Long jobInstanceId,
			JobParameters jobParameters,
			Collection<JobExecutionInfo> jobExecutions) {
		this.jobName = jobName;
		this.jobExecutionsInfos = jobExecutions != null ? jobExecutions
				: new ArrayList<JobExecutionInfo>();
		this.id = jobInstanceId;
		this.jobParameters = new DefaultJobParametersConverter()
				.getProperties(jobParameters);
		this.jobParametersString = new JobParametersExtractor()
				.fromJobParameters(jobParameters);
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
	 * Gets the job execution count.
	 * 
	 * @return the job execution count
	 */
	public int getJobExecutionCount() {
		return jobExecutionsInfos.size();
	}

	/**
	 * Gets the job execution representation.
	 * 
	 * @return the job execution representation
	 */
	public Collection<JobExecutionInfo> getJobExecutioniInfos() {
		return jobExecutionsInfos;
	}

	// public JobExecutionRepresentation getLastJobExecutionRepresentation() {
	// return jobExecutionsRepresentations.isEmpty() ? null
	// : jobExecutionsRepresentations.iterator().next();
	// }

	/**
	 * Gets the job parameters string.
	 * 
	 * @return the job parameters string
	 */
	public String getJobParametersString() {
		return jobParametersString;
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
	 * Gets the job parameters.
	 * 
	 * @return the job parameters
	 */
	public Properties getJobParameters() {
		return jobParameters;
	}
}
