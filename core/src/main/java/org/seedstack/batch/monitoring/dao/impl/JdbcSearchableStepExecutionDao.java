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
package org.seedstack.batch.monitoring.dao.impl;

import org.seedstack.batch.monitoring.dao.SearchableStepExecutionDao;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.JdbcJobExecutionDao;
import org.springframework.batch.core.repository.dao.JdbcStepExecutionDao;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.support.PatternMatcher;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.incrementer.AbstractDataFieldMaxValueIncrementer;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class JdbcSearchableStepExecutionDao.
 * 
 * @author : aymen.abbes@ext.mpsa.com.
 */
public class JdbcSearchableStepExecutionDao extends JdbcStepExecutionDao
		implements SearchableStepExecutionDao {

	/**
	 * The Constant STEP_EXECUTIONS_FOR_JOB.
	 */
	private static final String STEP_EXECUTIONS_FOR_JOB = "SELECT distinct STEP_NAME from %PREFIX%STEP_EXECUTION S, %PREFIX%JOB_EXECUTION E, %PREFIX%JOB_INSTANCE I "
			+ "where S.JOB_EXECUTION_ID = E.JOB_EXECUTION_ID AND E.JOB_INSTANCE_ID = I.JOB_INSTANCE_ID AND I.JOB_NAME = ?";

	/**
	 * The Constant COUNT_STEP_EXECUTIONS_FOR_STEP.
	 */
	private static final String COUNT_STEP_EXECUTIONS_FOR_STEP = "SELECT COUNT(STEP_EXECUTION_ID) from %PREFIX%STEP_EXECUTION S, %PREFIX%JOB_EXECUTION E, %PREFIX%JOB_INSTANCE I "
			+ "where S.JOB_EXECUTION_ID = E.JOB_EXECUTION_ID AND E.JOB_INSTANCE_ID = I.JOB_INSTANCE_ID AND I.JOB_NAME = ? AND S.STEP_NAME = ?";

	/**
	 * The Constant COUNT_STEP_EXECUTIONS_FOR_STEP_PATTERN.
	 */
	private static final String COUNT_STEP_EXECUTIONS_FOR_STEP_PATTERN = "SELECT COUNT(STEP_EXECUTION_ID) from %PREFIX%STEP_EXECUTION S, %PREFIX%JOB_EXECUTION E, %PREFIX%JOB_INSTANCE I"
			+ " where S.JOB_EXECUTION_ID = E.JOB_EXECUTION_ID AND E.JOB_INSTANCE_ID = I.JOB_INSTANCE_ID AND I.JOB_NAME = ? AND S.STEP_NAME like ?";

	/**
	 * The Constant FIELDS.
	 */
	private static final String FIELDS = "S.STEP_EXECUTION_ID, S.STEP_NAME, S.START_TIME, S.END_TIME, S.STATUS, S.COMMIT_COUNT,"
			+ " S.READ_COUNT, S.FILTER_COUNT, S.WRITE_COUNT, S.EXIT_CODE, S.EXIT_MESSAGE, S.READ_SKIP_COUNT, S.WRITE_SKIP_COUNT,"
			+ " S.PROCESS_SKIP_COUNT, S.ROLLBACK_COUNT, S.LAST_UPDATED, S.VERSION";

	/**
	 * The data source.
	 */
	private DataSource dataSource;

	/**
	 * Sets the data source.
	 * 
	 * @param dataSource
	 *            the dataSource to set
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * After properties set.
	 * 
	 * @throws Exception
	 *             the exception
	 * @see JdbcJobExecutionDao#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		Assert.state(dataSource != null, "DataSource must be provided");

		if (getJdbcTemplate() == null) {
			setJdbcTemplate(new JdbcTemplate(dataSource));
		}
		setStepExecutionIncrementer(new AbstractDataFieldMaxValueIncrementer() {
			@Override
			protected long getNextKey() {
				return 0;
			}
		});

		super.afterPropertiesSet();

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.dao.SearchableStepExecutionDao#findStepNamesForJobExecution(java.lang.String,
	 *      java.lang.String)
	 */
	public Collection<String> findStepNamesForJobExecution(String jobName,
			String excludesPattern) {

		List<String> list = getJdbcTemplate().query(
				getQuery(STEP_EXECUTIONS_FOR_JOB), new RowMapper<String>() {
					public String mapRow(java.sql.ResultSet rs, int rowNum)
							throws java.sql.SQLException {
						return rs.getString(1);
					}
				}, jobName);

		Set<String> stepNames = new LinkedHashSet<String>(list);
		for (Iterator<String> iterator = stepNames.iterator(); iterator
				.hasNext();) {
			String name = iterator.next();
			if (PatternMatcher.match(excludesPattern, name)) {
				iterator.remove();
			}
		}

		return stepNames;

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.dao.SearchableStepExecutionDao#findStepExecutions(java.lang.String,
	 *      java.lang.String, int, int)
	 */
	public Collection<StepExecution> findStepExecutions(String jobName,
			String stepName, int start, int count) {

		String whereClause;
		String jobN = jobName;
		String stepN = stepName;

		if (jobName.contains("*")) {
			whereClause = "JOB_NAME like ?";
			jobN = jobName.replace("*", "%");
		} else {
			whereClause = "JOB_NAME = ?";
		}

		if (stepName.contains("*")) {
			whereClause = whereClause + " AND STEP_NAME like ?";
			stepN = stepName.replace("*", "%");
		} else {
			whereClause = whereClause + " AND STEP_NAME = ?";
		}

		PagingQueryProvider queryProvider = getPagingQueryProvider(whereClause);

		List<StepExecution> stepExecutions;
		if (start <= 0) {
			stepExecutions = getJdbcTemplate().query(
					queryProvider.generateFirstPageQuery(count),
					new StepExecutionRowMapper(), jobN, stepN);
		} else {
			try {
				Long startAfterValue = getJdbcTemplate().queryForLong(
						queryProvider.generateJumpToItemQuery(start, count),
						jobN, stepN);
				stepExecutions = getJdbcTemplate().query(
						queryProvider.generateRemainingPagesQuery(count),
						new StepExecutionRowMapper(), jobN, stepN,
						startAfterValue);
			} catch (IncorrectResultSizeDataAccessException e) {
				return Collections.emptyList();
			}
		}

		return stepExecutions;

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.seedstack.seed.batch.monitoring.dao.SearchableStepExecutionDao#countStepExecutions(java.lang.String,
	 *      java.lang.String)
	 */
	public int countStepExecutions(String jobName, String stepName) {
		if (stepName.contains("*")) {
			return getJdbcTemplate().queryForInt(
					getQuery(COUNT_STEP_EXECUTIONS_FOR_STEP_PATTERN), jobName,
					stepName.replace("*", "%"));
		}
		return getJdbcTemplate().queryForInt(
				getQuery(COUNT_STEP_EXECUTIONS_FOR_STEP), jobName, stepName);
	}

	/**
	 * Gets the paging query provider.
	 * 
	 * @param whereClause
	 *            the where clause
	 * @return a {@link PagingQueryProvider} with a where clause to narrow the
	 *         query
	 */
	private PagingQueryProvider getPagingQueryProvider(String whereClause) {
		SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
		factory.setDataSource(dataSource);
		factory.setFromClause(getQuery("%PREFIX%STEP_EXECUTION S, %PREFIX%JOB_EXECUTION J, %PREFIX%JOB_INSTANCE I"));
		factory.setSelectClause(FIELDS);
		Map<String, Order> sortKeys = new HashMap<String, Order>();
		sortKeys.put("STEP_EXECUTION_ID", Order.DESCENDING);
		factory.setSortKeys(sortKeys);
		if (whereClause != null) {
			factory.setWhereClause(whereClause
					+ " AND S.JOB_EXECUTION_ID = J.JOB_EXECUTION_ID AND J.JOB_INSTANCE_ID = I.JOB_INSTANCE_ID");
		}
		try {
			return (PagingQueryProvider) factory.getObject();
		} catch (Exception e) {
			throw new IllegalStateException(
					"Unexpected exception creating paging query provide", e);
		}
	}

	/**
	 * The Class StepExecutionRowMapper.
	 */
	private static class StepExecutionRowMapper implements
			RowMapper<StepExecution> {

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet,
		 *      int)
		 */
		public StepExecution mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			StepExecution stepExecution = new StepExecution(rs.getString(2),
					null);
			stepExecution.setId(rs.getLong(1));
			stepExecution.setStartTime(rs.getTimestamp(3));
			stepExecution.setEndTime(rs.getTimestamp(4));
			stepExecution.setStatus(BatchStatus.valueOf(rs.getString(5)));
			stepExecution.setCommitCount(rs.getInt(6));
			stepExecution.setReadCount(rs.getInt(7));
			stepExecution.setFilterCount(rs.getInt(8));
			stepExecution.setWriteCount(rs.getInt(9));
			stepExecution.setExitStatus(new ExitStatus(rs.getString(10), rs
					.getString(11)));
			stepExecution.setReadSkipCount(rs.getInt(12));
			stepExecution.setWriteSkipCount(rs.getInt(13));
			stepExecution.setProcessSkipCount(rs.getInt(14));
			stepExecution.setRollbackCount(rs.getInt(15));
			stepExecution.setLastUpdated(rs.getTimestamp(16));
			stepExecution.setVersion(rs.getInt(17));

			return stepExecution;

		}

	}

}
