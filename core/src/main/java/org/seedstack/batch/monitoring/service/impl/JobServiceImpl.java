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
package org.seedstack.batch.monitoring.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.CollectionUtils;

import org.seedstack.batch.monitoring.dao.SearchableJobExecutionDao;
import org.seedstack.batch.monitoring.dao.SearchableJobInstanceDao;
import org.seedstack.batch.monitoring.dao.SearchableStepExecutionDao;
import org.seedstack.batch.monitoring.exception.NoSuchStepExecutionException;
import org.seedstack.batch.monitoring.service.JobService;

/**
 * Implementation of {@link JobService} that delegates most of its work to other
 * off-the-shelf components.
 * 
 * @author : aymen.abbes@ext.mpsa.com.
 * 
 */
public class JobServiceImpl implements JobService, DisposableBean {

	/**
	 * The Constant logger. @author aymen.abbes@ext.mpsa.com Date: 27 juin 2014 @author
	 * aymen.abbes@ext.mpsa.com Date: 27 juin 2014
	 */
	private static final Log LOGGER = LogFactory.getLog(JobServiceImpl.class);

	// 60 seconds
	/**
	 * The Constant DEFAULT_SHUTDOWN_TIMEOUT.
	 */
	private static final int DEFAULT_SHUTDOWN_TIMEOUT = 60 * 1000;

	/**
	 * The job instance dao.
	 */
	private final SearchableJobInstanceDao jobInstanceDao;

	/**
	 * The job execution dao.
	 */
	private final SearchableJobExecutionDao jobExecutionDao;

	/**
	 * The job repository.
	 */
	private final JobRepository jobRepository;

	/**
	 * The job launcher.
	 */
	private final JobLauncher jobLauncher;

	/**
	 * The job locator.
	 */
	private final ListableJobLocator jobLocator;

	/**
	 * The step execution dao.
	 */
	private final SearchableStepExecutionDao stepExecutionDao;

	/**
	 * The execution context dao.
	 */
	private final ExecutionContextDao executionContextDao;

	/**
	 * The active executions.
	 */
	private Collection<JobExecution> activeExecutions = Collections
			.synchronizedList(new ArrayList<JobExecution>());

	/**
	 * The shutdown timeout.
	 */
	private int shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT;

	/**
	 * Timeout for shutdown waiting for jobs to finish processing.
	 * 
	 * @param shutdownTimeout
	 *            in milliseconds (default 60 secs)
	 */
	public void setShutdownTimeout(int shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
	}

	/**
	 * Instantiates a new job service impl.
	 * 
	 * @param jobInstanceDao
	 *            the job instance dao
	 * @param jobExecutionDao
	 *            the job execution dao
	 * @param stepExecutionDao
	 *            the step execution dao
	 * @param jobRepository
	 *            the job repository
	 * @param jobLauncher
	 *            the job launcher
	 * @param jobLocator
	 *            the job locator
	 * @param executionContextDao
	 *            the execution context dao
	 */
	public JobServiceImpl(SearchableJobInstanceDao jobInstanceDao,
			SearchableJobExecutionDao jobExecutionDao,
			SearchableStepExecutionDao stepExecutionDao,
			JobRepository jobRepository, JobLauncher jobLauncher,
			ListableJobLocator jobLocator,
			ExecutionContextDao executionContextDao) {
		super();
		this.jobInstanceDao = jobInstanceDao;
		this.jobExecutionDao = jobExecutionDao;
		this.stepExecutionDao = stepExecutionDao;
		this.jobRepository = jobRepository;
		this.jobLauncher = jobLauncher;
		this.jobLocator = jobLocator;
		this.executionContextDao = executionContextDao;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#getStepExecutions(java.lang.Long)
	 */
	public Collection<StepExecution> getStepExecutions(Long jobExecutionId)
			throws NoSuchJobExecutionException {

		JobExecution jobExecution = jobExecutionDao
				.getJobExecution(jobExecutionId);
		if (jobExecution == null) {
			throw new NoSuchJobExecutionException("No JobExecution with id="
					+ jobExecutionId);
		}

		stepExecutionDao.addStepExecutions(jobExecution);

		// String jobName = jobExecution.getJobInstance() == null ? null
		// : jobExecution.getJobInstance().getJobName();

		JobInstance jobInstance = jobInstanceDao.getJobInstance(jobExecution);
		jobExecution.setJobInstance(jobInstance);
		return jobExecution.getStepExecutions();

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#isLaunchable(java.lang.String)
	 */
	public boolean isLaunchable(String jobName) {
		return jobLocator.getJobNames().contains(jobName);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#isIncrementable(java.lang.String)
	 */
	public boolean isIncrementable(String jobName) {
		try {
			return jobLocator.getJobNames().contains(jobName)
					&& jobLocator.getJob(jobName).getJobParametersIncrementer() != null;
		} catch (NoSuchJobException e) {
			// Should not happen
			throw new IllegalStateException("Unexpected non-existent job: "
					+ jobName);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#restart(java.lang.Long)
	 */
	public JobExecution restart(Long jobExecutionId)
			throws NoSuchJobExecutionException,
			JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, NoSuchJobException,
			JobParametersInvalidException {

		JobExecution target = getJobExecution(jobExecutionId);
		JobInstance lastInstance = target.getJobInstance();

		Job job = jobLocator.getJob(lastInstance.getJobName());

		JobExecution jobExecution = jobLauncher.run(job,
				target.getJobParameters());

		if (jobExecution.isRunning()) {
			activeExecutions.add(jobExecution);
		}
		return jobExecution;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#launch(java.lang.String,
	 *      org.springframework.batch.core.JobParameters)
	 */
	public JobExecution launch(String jobName, JobParameters jobParameters)
			throws NoSuchJobException, JobExecutionAlreadyRunningException,
			JobRestartException, JobInstanceAlreadyCompleteException,
			JobParametersInvalidException {

		Job job = jobLocator.getJob(jobName);

		JobParameters jobParameters2 = jobParameters;
		JobExecution lastJobExecution = jobRepository.getLastJobExecution(
				jobName, jobParameters2);
		boolean restart = false;
		if (lastJobExecution != null) {
			BatchStatus status = lastJobExecution.getStatus();
			if (status.isUnsuccessful() && status != BatchStatus.ABANDONED) {
				restart = true;
			}
		}

		if (job.getJobParametersIncrementer() != null && !restart) {
			jobParameters2 = job.getJobParametersIncrementer().getNext(
					jobParameters2);
		}

		JobExecution jobExecution = jobLauncher.run(job, jobParameters2);

		if (jobExecution.isRunning()) {
			activeExecutions.add(jobExecution);
		}
		return jobExecution;

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#getLastJobParameters(java.lang.String)
	 */
	public JobParameters getLastJobParameters(String jobName)
			throws NoSuchJobException {

		Collection<JobExecution> executions = jobExecutionDao.getJobExecutions(
				jobName, 0, 1);

		JobExecution lastExecution = null;
		if (!CollectionUtils.isEmpty(executions)) {
			lastExecution = executions.iterator().next();
		}

		JobParameters oldParameters = new JobParameters();
		if (lastExecution != null) {
			oldParameters = lastExecution.getJobParameters();
		}

		return oldParameters;

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#listJobExecutions(int,
	 *      int)
	 */
	public Collection<JobExecution> listJobExecutions(int start, int count) {
		return jobExecutionDao.getJobExecutions(start, count);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#countJobExecutions()
	 */
	public int countJobExecutions() {
		return jobExecutionDao.countJobExecutions();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#listJobs(int,
	 *      int)
	 */
	public Collection<String> listJobs(int start, int count) {
		Collection<String> jobNames = new LinkedHashSet<String>(
				jobLocator.getJobNames());
		int start2 = start;
		int count2 = count;
		if (start2 + count2 > jobNames.size()) {
			jobNames.addAll(jobInstanceDao.getJobNames());
		}
		if (start2 >= jobNames.size()) {
			start2 = jobNames.size();
		}
		if (start2 + count2 >= jobNames.size()) {
			count2 = jobNames.size() - start2;
		}
		return new ArrayList<String>(jobNames).subList(start2, start2 + count2);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#countJobs()
	 */
	public int countJobs() {
		Collection<String> names = new HashSet<String>(jobLocator.getJobNames());
		names.addAll(jobInstanceDao.getJobNames());
		return names.size();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#stopAll()
	 */
	public int stopAll() {
		Collection<JobExecution> result = jobExecutionDao
				.getRunningJobExecutions();
		for (JobExecution jobExecution : result) {
			jobExecution.stop();
			jobRepository.update(jobExecution);
		}
		return result.size();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#stop(java.lang.Long)
	 */
	public JobExecution stop(Long jobExecutionId)
			throws NoSuchJobExecutionException, JobExecutionNotRunningException {

		JobExecution jobExecution = getJobExecution(jobExecutionId);
		if (!jobExecution.isRunning()) {
			throw new JobExecutionNotRunningException(
					"JobExecution is not running and therefore cannot be stopped");
		}

		LOGGER.info("Stopping job execution: " + jobExecution);
		jobExecution.stop();
		jobRepository.update(jobExecution);
		return jobExecution;

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#abandon(java.lang.Long)
	 */
	public JobExecution abandon(Long jobExecutionId)
			throws NoSuchJobExecutionException,
			JobExecutionAlreadyRunningException {

		JobExecution jobExecution = getJobExecution(jobExecutionId);
		if (jobExecution.getStatus().isLessThan(BatchStatus.STOPPING)) {
			throw new JobExecutionAlreadyRunningException(
					"JobExecution is running or complete and therefore cannot be aborted");
		}

		LOGGER.info("Aborting job execution: " + jobExecution);
		jobExecution.upgradeStatus(BatchStatus.ABANDONED);
		jobRepository.update(jobExecution);
		return jobExecution;

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#countJobExecutionsForJob(java.lang.String)
	 */
	public int countJobExecutionsForJob(String name) throws NoSuchJobException {
		checkJobExists(name);
		return jobExecutionDao.countJobExecutions(name);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#countJobInstances(java.lang.String)
	 */
	public int countJobInstances(String name) throws NoSuchJobException {
		return jobInstanceDao.countJobInstances(name);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#getJobExecution(java.lang.Long)
	 */
	public JobExecution getJobExecution(Long jobExecutionId)
			throws NoSuchJobExecutionException {
		JobExecution jobExecution = jobExecutionDao
				.getJobExecution(jobExecutionId);
		if (jobExecution == null) {
			throw new NoSuchJobExecutionException(
					"There is no JobExecution with id=" + jobExecutionId);
		}
		jobExecution
				.setJobInstance(jobInstanceDao.getJobInstance(jobExecution));
		try {
			jobExecution.setExecutionContext(executionContextDao
					.getExecutionContext(jobExecution));
		} catch (Exception e) {
			LOGGER.info("Cannot load execution context for job execution: "
					+ jobExecution);
		}
		stepExecutionDao.addStepExecutions(jobExecution);
		return jobExecution;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#getJobExecutionsForJobInstance(java.lang.String,
	 *      java.lang.Long)
	 */
	public Collection<JobExecution> getJobExecutionsForJobInstance(String name,
			Long jobInstanceId) throws NoSuchJobException {
		checkJobExists(name);
		List<JobExecution> jobExecutions = jobExecutionDao
				.findJobExecutions(jobInstanceDao.getJobInstance(jobInstanceId));
		for (JobExecution jobExecution : jobExecutions) {
			stepExecutionDao.addStepExecutions(jobExecution);
		}
		return jobExecutions;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#getStepExecution(java.lang.Long,
	 *      java.lang.Long)
	 */
	public StepExecution getStepExecution(Long jobExecutionId,
			Long stepExecutionId) throws NoSuchJobExecutionException,
			NoSuchStepExecutionException {
		JobExecution jobExecution = getJobExecution(jobExecutionId);
		StepExecution stepExecution = stepExecutionDao.getStepExecution(
				jobExecution, stepExecutionId);
		if (stepExecution == null) {
			throw new NoSuchStepExecutionException(
					"There is no StepExecution with jobExecutionId="
							+ jobExecutionId + " and id=" + stepExecutionId);
		}
		try {
			stepExecution.setExecutionContext(executionContextDao
					.getExecutionContext(stepExecution));
		} catch (Exception e) {
			LOGGER.info("Cannot load execution context for step execution: "
					+ stepExecution);
		}
		return stepExecution;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#listJobExecutionsForJob(java.lang.String,
	 *      int, int)
	 */
	public Collection<JobExecution> listJobExecutionsForJob(String jobName,
			int start, int count) throws NoSuchJobException {
		checkJobExists(jobName);
		List<JobExecution> jobExecutions = jobExecutionDao.getJobExecutions(
				jobName, start, count);
		for (JobExecution jobExecution : jobExecutions) {
			stepExecutionDao.addStepExecutions(jobExecution);
		}
		return jobExecutions;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#listStepExecutionsForStep(java.lang.String,
	 *      java.lang.String, int, int)
	 */
	public Collection<StepExecution> listStepExecutionsForStep(String jobName,
			String stepName, int start, int count) throws NoSuchStepException {
		if (stepExecutionDao.countStepExecutions(jobName, stepName) == 0) {
			throw new NoSuchStepException(
					"No step executions exist with this step name: " + stepName);
		}
		return stepExecutionDao.findStepExecutions(jobName, stepName, start,
				count);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#countStepExecutionsForStep(java.lang.String,
	 *      java.lang.String)
	 */
	public int countStepExecutionsForStep(String jobName, String stepName)
			throws NoSuchStepException {
		return stepExecutionDao.countStepExecutions(jobName, stepName);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#getJobInstance(long)
	 */
	public JobInstance getJobInstance(long jobInstanceId)
			throws NoSuchJobInstanceException {
		JobInstance jobInstance = jobInstanceDao.getJobInstance(jobInstanceId);
		if (jobInstance == null) {
			throw new NoSuchJobInstanceException("JobInstance with id="
					+ jobInstanceId + " does not exist");
		}
		return jobInstance;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#listJobInstances(java.lang.String,
	 *      int, int)
	 */
	public Collection<JobInstance> listJobInstances(String jobName, int start,
			int count) throws NoSuchJobException {
		checkJobExists(jobName);
		return jobInstanceDao.getJobInstances(jobName, start, count);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.service.JobService#getStepNamesForJob(java.lang.String)
	 */
	public Collection<String> getStepNamesForJob(String jobName)
			throws NoSuchJobException {
		try {
			Job job = jobLocator.getJob(jobName);
			if (job instanceof StepLocator) {
				return ((StepLocator) job).getStepNames();
			}
		} catch (NoSuchJobException e) {
			// ignore
		}
		Collection<String> stepNames = new LinkedHashSet<String>();
		for (JobExecution jobExecution : listJobExecutionsForJob(jobName, 0,
				100)) {
			for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
				stepNames.add(stepExecution.getStepName());
			}
		}
		return Collections.unmodifiableList(new ArrayList<String>(stepNames));
	}

	/**
	 * Check job exists.
	 * 
	 * @param jobName
	 *            the job name
	 * @throws NoSuchJobException
	 *             the no such job exception
	 */
	private void checkJobExists(String jobName) throws NoSuchJobException {
		if (jobLocator.getJobNames().contains(jobName)) {
			return;
		}
		if (jobInstanceDao.countJobInstances(jobName) > 0) {
			return;
		}
		throw new NoSuchJobException(
				"No Job with that name either current or historic: [" + jobName
						+ "]");
	}

	/**
	 * Stop all the active jobs and wait for them (up to a time out) to finish
	 * processing.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void destroy() throws Exception {

		Exception firstException = null;

		for (JobExecution jobExecution : activeExecutions) {
			try {
				if (jobExecution.isRunning()) {
					stop(jobExecution.getId());
				}
			} catch (JobExecutionNotRunningException e) {
				LOGGER.info("JobExecution is not running so it cannot be stopped");
			} catch (Exception e) {
				LOGGER.error("Unexpected exception stopping JobExecution", e);
				if (firstException == null) {
					firstException = e;
				}
			}
		}

		int count = 0;
		int maxCount = (shutdownTimeout + 1000) / 1000;
		while (!activeExecutions.isEmpty() && ++count < maxCount) {
			LOGGER.error("Waiting for " + activeExecutions.size()
					+ " active executions to complete");
			removeInactiveExecutions();
			Thread.sleep(1000L);
		}

		if (firstException != null) {
			throw firstException;
		}

	}

	/**
	 * Check all the active executions and see if they are still actually
	 * running. Remove the ones that have completed.
	 */
	public void removeInactiveExecutions() {

		for (Iterator<JobExecution> iterator = activeExecutions.iterator(); iterator
				.hasNext();) {
			JobExecution jobExecution = iterator.next();
			try {
				jobExecution = getJobExecution(jobExecution.getId());
			} catch (NoSuchJobExecutionException e) {
				LOGGER.error("Unexpected exception loading JobExecution", e);
			}
			if (!jobExecution.isRunning()) {
				iterator.remove();
			}
		}

	}

}
