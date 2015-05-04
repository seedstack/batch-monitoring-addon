/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * 
 */

package org.seedstack.batch.monitoring.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.repository.dao.JobExecutionDao;


/**
 * The Interface SearchableJobExecutionDao.
 * 
 * @author : aymen.abbes@ext.mpsa.com.
 */
public interface SearchableJobExecutionDao extends JobExecutionDao {

	/**
	 * Count job executions.
	 * 
	 * @return the int
	 */
	int countJobExecutions();

	/**
	 * Gets the job executions.
	 * 
	 * @param jobName
	 *            the job name
	 * @param start
	 *            the start
	 * @param count
	 *            the count
	 * @return the job executions
	 */
	List<JobExecution> getJobExecutions(String jobName, int start, int count);

	/**
	 * Gets the job executions.
	 * 
	 * @param start
	 *            the start
	 * @param count
	 *            the count
	 * @return the job executions
	 */
	List<JobExecution> getJobExecutions(int start, int count);

	/**
	 * Count job executions.
	 * 
	 * @param jobName
	 *            the job name
	 * @return the int
	 */
	int countJobExecutions(String jobName);

	/**
	 * Gets the running job executions.
	 * 
	 * @return the running job executions
	 */
	Collection<JobExecution> getRunningJobExecutions();

}
