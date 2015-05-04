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

import static org.springframework.batch.support.DatabaseType.SYBASE;

import java.sql.Types;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seedstack.batch.monitoring.dao.SearchableJobExecutionDao;
import org.seedstack.batch.monitoring.dao.SearchableJobInstanceDao;
import org.seedstack.batch.monitoring.dao.SearchableStepExecutionDao;
import org.seedstack.batch.monitoring.dao.impl.JdbcSearchableJobExecutionDao;
import org.seedstack.batch.monitoring.dao.impl.JdbcSearchableJobInstanceDao;
import org.seedstack.batch.monitoring.dao.impl.JdbcSearchableStepExecutionDao;
import org.seedstack.batch.monitoring.service.JobService;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.repository.dao.JdbcExecutionContextDao;
import org.springframework.batch.core.repository.dao.JdbcJobExecutionDao;
import org.springframework.batch.core.repository.dao.JdbcStepExecutionDao;
import org.springframework.batch.item.database.support.DataFieldMaxValueIncrementerFactory;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.jdbc.support.lob.OracleLobHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A factory for a {@link JobService} that makes the configuration of its
 * various ingredients as convenient as possible.
 * 
 * @author : aymen.abbes@ext.mpsa.com.
 * 
 */
public class JobServiceImplFactoryBean implements FactoryBean<JobService>,
		InitializingBean {

	/**
	 * The Constant logger.
	 */
	private static final Log LOGGER = LogFactory
			.getLog(JobServiceImplFactoryBean.class);

	/**
	 * The data source.
	 */
	private DataSource dataSource;

	/**
	 * The jdbc template.
	 */
	private JdbcOperations jdbcTemplate;

	/**
	 * The database type.
	 */
	private String databaseType;

	/**
	 * The table prefix.
	 */
	private String tablePrefix = AbstractJdbcBatchMetadataDao.DEFAULT_TABLE_PREFIX;

	/**
	 * The incrementer factory.
	 */
	private DataFieldMaxValueIncrementerFactory incrementerFactory;

	/**
	 * The max var char length.
	 */
	private int maxVarCharLength = AbstractJdbcBatchMetadataDao.DEFAULT_EXIT_MESSAGE_LENGTH;

	/**
	 * The lob handler.
	 */
	private LobHandler lobHandler;

	/**
	 * The job repository.
	 */
	private JobRepository jobRepository;

	/**
	 * The job launcher.
	 */
	private JobLauncher jobLauncher;

	/**
	 * The job locator.
	 */
	private ListableJobLocator jobLocator;

	/**
	 * A special handler for large objects. The default is usually fine, except
	 * for some (usually older) versions of Oracle. The default is determined
	 * from the data base type.
	 * 
	 * @param lobHandler
	 *            the {@link LobHandler} to set
	 * 
	 * @see LobHandler
	 */
	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	/**
	 * Public setter for the length of long string columns in database. Do not
	 * set this if you haven't modified the schema. Note this value will be used
	 * for the exit message in both {@link JdbcJobExecutionDao} and
	 * 
	 * @param maxVarCharLength
	 *            the exitMessageLength to set {@link JdbcStepExecutionDao} and
	 *            also the short version of the execution context in
	 *            {@link JdbcExecutionContextDao} . For databases with
	 *            multi-byte character sets this number can be smaller (by up to
	 *            a factor of 2 for 2-byte characters) than the declaration of
	 *            the column length in the DDL for the tables.
	 */
	public void setMaxVarCharLength(int maxVarCharLength) {
		this.maxVarCharLength = maxVarCharLength;
	}

	/**
	 * Public setter for the {@link DataSource}.
	 * 
	 * @param dataSource
	 *            a {@link DataSource}
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Sets the database type.
	 * 
	 * @param dbType
	 *            as specified by
	 *            {@link DefaultDataFieldMaxValueIncrementerFactory}
	 */
	public void setDatabaseType(String dbType) {
		this.databaseType = dbType;
	}

	/**
	 * Sets the table prefix for all the batch meta-data tables.
	 * 
	 * @param tablePrefix
	 *            the new table prefix
	 */
	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	/**
	 * A factory for incrementers (used to build primary keys for meta data).
	 * Defaults to {@link DefaultDataFieldMaxValueIncrementerFactory}.
	 * 
	 * @param incrementerFactory
	 *            the incrementer factory to set
	 */
	public void setIncrementerFactory(
			DataFieldMaxValueIncrementerFactory incrementerFactory) {
		this.incrementerFactory = incrementerFactory;
	}

	/**
	 * The repository used to store and update jobs and step executions.
	 * 
	 * @param jobRepository
	 *            the {@link JobRepository} to set
	 */
	public void setJobRepository(JobRepository jobRepository) {
		this.jobRepository = jobRepository;
	}

	/**
	 * The launcher used to run jobs.
	 * 
	 * @param jobLauncher
	 *            a {@link JobLauncher}
	 */
	public void setJobLauncher(JobLauncher jobLauncher) {
		this.jobLauncher = jobLauncher;
	}

	/**
	 * A registry that can be used to locate jobs to run.
	 * 
	 * @param jobLocator
	 *            a {@link JobLocator}
	 */
	public void setJobLocator(ListableJobLocator jobLocator) {
		this.jobLocator = jobLocator;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {

		Assert.notNull(dataSource, "DataSource must not be null.");
		Assert.notNull(jobRepository, "JobRepository must not be null.");
		Assert.notNull(jobLocator, "JobLocator must not be null.");
		Assert.notNull(jobLauncher, "JobLauncher must not be null.");

		jdbcTemplate = new JdbcTemplate(dataSource);

		if (incrementerFactory == null) {
			incrementerFactory = new DefaultDataFieldMaxValueIncrementerFactory(
					dataSource);
		}

		if (databaseType == null) {
			databaseType = DatabaseType.fromMetaData(dataSource).name();
			LOGGER.info("No database type set, using meta data indicating: "
					+ databaseType);
		}

		if (lobHandler == null
				&& databaseType
						.equalsIgnoreCase(DatabaseType.ORACLE.toString())) {
			lobHandler = new OracleLobHandler();
		}

		Assert.isTrue(
				incrementerFactory.isSupportedIncrementerType(databaseType),
				"'"
						+ databaseType
						+ "' is an unsupported database type.  The supported database types are "
						+ StringUtils
								.arrayToCommaDelimitedString(incrementerFactory
										.getSupportedIncrementerTypes()));

	}

	/**
	 * Creates the job instance dao.
	 * 
	 * @return the searchable job instance dao
	 * @throws Exception
	 *             the exception
	 */
	protected SearchableJobInstanceDao createJobInstanceDao() throws Exception {
		JdbcSearchableJobInstanceDao dao = new JdbcSearchableJobInstanceDao();
		dao.setJdbcTemplate(jdbcTemplate);
		dao.setJobIncrementer(incrementerFactory.getIncrementer(databaseType,
				tablePrefix + "JOB_SEQ"));
		dao.setTablePrefix(tablePrefix);
		dao.afterPropertiesSet();
		return dao;
	}

	/**
	 * Creates the job execution dao.
	 * 
	 * @return the searchable job execution dao
	 * @throws Exception
	 *             the exception
	 */
	protected SearchableJobExecutionDao createJobExecutionDao()
			throws Exception {
		JdbcSearchableJobExecutionDao dao = new JdbcSearchableJobExecutionDao();
		dao.setDataSource(dataSource);
		dao.setJobExecutionIncrementer(incrementerFactory.getIncrementer(
				databaseType, tablePrefix + "JOB_EXECUTION_SEQ"));
		dao.setTablePrefix(tablePrefix);
		dao.setClobTypeToUse(determineClobTypeToUse(this.databaseType));
		dao.setExitMessageLength(maxVarCharLength);
		dao.afterPropertiesSet();
		return dao;
	}

	/**
	 * Creates the step execution dao.
	 * 
	 * @return the searchable step execution dao
	 * @throws Exception
	 *             the exception
	 */
	protected SearchableStepExecutionDao createStepExecutionDao()
			throws Exception {
		JdbcSearchableStepExecutionDao dao = new JdbcSearchableStepExecutionDao();
		dao.setDataSource(dataSource);
		dao.setStepExecutionIncrementer(incrementerFactory.getIncrementer(
				databaseType, tablePrefix + "STEP_EXECUTION_SEQ"));
		dao.setTablePrefix(tablePrefix);
		dao.setClobTypeToUse(determineClobTypeToUse(this.databaseType));
		dao.setExitMessageLength(maxVarCharLength);
		dao.afterPropertiesSet();
		return dao;
	}

	/**
	 * Creates the execution context dao.
	 * 
	 * @return the execution context dao
	 * @throws Exception
	 *             the exception
	 */
	protected ExecutionContextDao createExecutionContextDao() throws Exception {
		JdbcExecutionContextDao dao = new JdbcExecutionContextDao();
		dao.setJdbcTemplate(jdbcTemplate);
		dao.setTablePrefix(tablePrefix);
		dao.setClobTypeToUse(determineClobTypeToUse(this.databaseType));
		if (lobHandler != null) {
			dao.setLobHandler(lobHandler);
		}
		dao.afterPropertiesSet();
		// Assume the same length.
		dao.setShortContextLength(maxVarCharLength);
		return dao;
	}

	/**
	 * Determine clob type to use.
	 * 
	 * @param databaseType
	 *            the database type
	 * @return the int
	 */
	private int determineClobTypeToUse(String databaseType) {
		if (SYBASE == DatabaseType.valueOf(databaseType.toUpperCase())) {
			return Types.LONGVARCHAR;
		}
		return Types.CLOB;
	}

	/**
	 * Create a {@link JobServiceImpl} from the configuration provided.
	 * 
	 * @return the object
	 * @throws Exception
	 *             the exception
	 * @see FactoryBean#getObject()
	 */
	public JobService getObject() throws Exception {
		return new JobServiceImpl(createJobInstanceDao(),
				createJobExecutionDao(), createStepExecutionDao(),
				jobRepository, jobLauncher, jobLocator,
				createExecutionContextDao());
	}

	/**
	 * Tells the containing bean factory what kind of object is the product of.
	 * 
	 * @return JobServiceImpl {@link #getObject()}.
	 * @see FactoryBean#getObjectType()
	 */
	public Class<? extends JobService> getObjectType() {
		return JobServiceImpl.class;
	}

	/**
	 * Allows optimisation in the containing bean factory.
	 * 
	 * @return true
	 * @see FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}

}
