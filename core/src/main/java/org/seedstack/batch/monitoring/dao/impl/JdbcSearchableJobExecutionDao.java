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

import org.seedstack.batch.monitoring.dao.SearchableJobExecutionDao;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameter.ParameterType;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.repository.dao.JdbcJobExecutionDao;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.incrementer.AbstractDataFieldMaxValueIncrementer;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class JdbcSearchableJobExecutionDao.
 *
 * @author : aymen.abbes@ext.mpsa.com.
 */
public class JdbcSearchableJobExecutionDao extends JdbcJobExecutionDao
        implements SearchableJobExecutionDao {

    /**
     * The Constant GET_COUNT.
     */
    private static final String GET_COUNT = "SELECT COUNT(1) from %PREFIX%JOB_EXECUTION";

    /**
     * The Constant GET_COUNT_BY_JOB_NAME.
     */
    private static final String GET_COUNT_BY_JOB_NAME = "SELECT COUNT(1) from %PREFIX%JOB_EXECUTION E, %PREFIX%JOB_INSTANCE I "
            + "where E.JOB_INSTANCE_ID=I.JOB_INSTANCE_ID and I.JOB_NAME=?";

    /**
     * The Constant FIELDS.
     */
    private static final String FIELDS = "E.JOB_EXECUTION_ID, E.START_TIME, E.END_TIME, E.STATUS, E.EXIT_CODE, E.EXIT_MESSAGE, "
            + "E.CREATE_TIME, E.LAST_UPDATED, E.VERSION, I.JOB_INSTANCE_ID, I.JOB_NAME";

    /**
     * The Constant GET_RUNNING_EXECUTIONS.
     */
    private static final String GET_RUNNING_EXECUTIONS = "SELECT "
            + FIELDS
            + " from %PREFIX%JOB_EXECUTION E, %PREFIX%JOB_INSTANCE I "
            + "where E.JOB_INSTANCE_ID=I.JOB_INSTANCE_ID and E.END_TIME is NULL";

    /**
     * The Constant JOB_NAME_JUMP_TO_ITEM_QUERY.
     */
    private static final String JOB_NAME_JUMP_TO_ITEM_QUERY = "SELECT JOB_EXECUTION_ID FROM "
            + " (SELECT JOB_EXECUTION_ID, " +
            "ROWNUM as TMP_ROW_NUM FROM (SELECT E.JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION E, "
            + "BATCH_JOB_INSTANCE I "
            + "WHERE E.JOB_INSTANCE_ID=I.JOB_INSTANCE_ID and "
            + "I.JOB_NAME = ? ORDER BY E.JOB_EXECUTION_ID DESC)) WHERE TMP_ROW_NUM = ?";

    /**
     * The Constant REMAININ_GPAGES_QUERY.
     */
    private static final String REMAININ_GPAGES_QUERY = " SELECT * " + "FROM "
            + "  (SELECT E.Job_Execution_Id, " + "    E.Start_Time, "
            + "    E.End_Time, " + "    E.Status, " + "    E.Exit_Code, "
            + "    E.Exit_Message, " + "    E.Create_Time, "
            + "    E.Last_Updated, " + "    E.Version, "
            + "    I.Job_Instance_Id, " + "    I.Job_Name "
            + "  FROM Batch_Job_Execution E, " + "    Batch_Job_Instance I "
            + "  WHERE E.Job_Instance_Id=I.Job_Instance_Id "
            + "  AND I.Job_Name  = ? " + "  ORDER BY E.Job_Execution_Id DESC "
            + "  ) " + "WHERE Rownum  <= ? " + "AND ((Job_Execution_Id < ?))";

    /**
     * The all executions paging query provider.
     */
    private PagingQueryProvider allExecutionsPagingQueryProvider;

    /**
     * The by job name paging query provider.
     */
    private PagingQueryProvider byJobNamePagingQueryProvider;

    /**
     * The data source.
     */
    private DataSource dataSource;

    /**
     * Sets the data source.
     *
     * @param dataSource the dataSource to set
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * After properties set.
     *
     * @throws Exception the exception
     * @see JdbcJobExecutionDao#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        Assert.state(dataSource != null, "DataSource must be provided");

        if (getJdbcTemplate() == null) {
            setJdbcTemplate(new JdbcTemplate(dataSource));
        }
        setJobExecutionIncrementer(new AbstractDataFieldMaxValueIncrementer() {
            @Override
            protected long getNextKey() {
                return 0;
            }
        });

        allExecutionsPagingQueryProvider = getPagingQueryProvider();
        byJobNamePagingQueryProvider = getPagingQueryProvider("I.JOB_NAME=?");

        super.afterPropertiesSet();

    }

    /**
     * Gets the paging query provider.
     *
     * @return a {@link PagingQueryProvider} for all job executions
     * @throws Exception the exception
     */
    private PagingQueryProvider getPagingQueryProvider() throws Exception {
        return getPagingQueryProvider(null);
    }

    /**
     * Gets the paging query provider.
     *
     * @param whereClause the where clause
     * @return a {@link PagingQueryProvider} for all job executions with the
     * provided where clause
     * @throws Exception the exception
     */
    private PagingQueryProvider getPagingQueryProvider(String whereClause)
            throws Exception {
        return getPagingQueryProvider(null, whereClause);
    }

    /**
     * Gets the paging query provider.
     *
     * @param fromClause  the from clause
     * @param whereClause the where clause
     * @return a {@link PagingQueryProvider} with a where clause to narrow the
     * query
     * @throws Exception the exception
     */
    private PagingQueryProvider getPagingQueryProvider(String fromClause,
                                                       String whereClause) throws Exception {
        SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
        factory.setDataSource(dataSource);
        String fromCl = "%PREFIX%JOB_EXECUTION E, %PREFIX%JOB_INSTANCE I"
                + (fromClause == null ? "" : ", " + fromClause);
        factory.setFromClause(getQuery(fromCl));
        factory.setSelectClause(FIELDS);
        Map<String, Order> sortKeys = new HashMap<String, Order>();
        sortKeys.put("JOB_EXECUTION_ID", Order.DESCENDING);
        factory.setSortKeys(sortKeys);

        String whereCl = "E.JOB_INSTANCE_ID=I.JOB_INSTANCE_ID"
                + (whereClause == null ? "" : " and " + whereClause);
        if (!whereCl.isEmpty()) {
            factory.setWhereClause(whereCl);
        }
        return (PagingQueryProvider) factory.getObject();
    }

    /**
     * Count job executions.
     *
     * @return the int
     * @see SearchableJobExecutionDao#countJobExecutions()
     */
    public int countJobExecutions() {
        return getJdbcTemplate().queryForObject(getQuery(GET_COUNT),
                Integer.class);
    }

    /**
     * Count job executions.
     *
     * @param jobName the job name
     * @return the int
     * @see SearchableJobExecutionDao#countJobExecutions(String)
     */
    public int countJobExecutions(String jobName) {
        return getJdbcTemplate().queryForObject(
                getQuery(GET_COUNT_BY_JOB_NAME), Integer.class, jobName);
    }

    /**
     * Gets the running job executions.
     *
     * @return the running job executions
     * @see SearchableJobExecutionDao#getRunningJobExecutions()
     */
    public Collection<JobExecution> getRunningJobExecutions() {
        return getJdbcTemplate().query(getQuery(GET_RUNNING_EXECUTIONS),
                new JobExecutionRowMapper());
    }

    /**
     * Gets the job executions.
     *
     * @param jobName the job name
     * @param start   the start
     * @param count   the count
     * @return the job executions
     * @see SearchableJobExecutionDao#getJobExecutions(String, int, int)
     */
    public List<JobExecution> getJobExecutions(String jobName, int start,
                                               int count) {
        if (start <= 0) {
            return getJdbcTemplate().query(
                    byJobNamePagingQueryProvider.generateFirstPageQuery(count),
                    new JobExecutionRowMapper(), jobName);
        }
        try {

//			Long startAfterValue = getJdbcTemplate().queryForLong(
//					JOB_NAME_JUMP_TO_ITEM_QUERY, jobName, count);
//
//			return getJdbcTemplate().query(REMAININ_GPAGES_QUERY,
//					new JobExecutionRowMapper(), jobName, count,
//					startAfterValue);
            Long startAfterValue = getJdbcTemplate().queryForObject(
                    byJobNamePagingQueryProvider.generateJumpToItemQuery(start, count), Long.class, jobName);

            return getJdbcTemplate().query(byJobNamePagingQueryProvider
                            .generateRemainingPagesQuery(count),
                    new JobExecutionRowMapper(),
                    jobName,
                    startAfterValue);
        } catch (IncorrectResultSizeDataAccessException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Gets the job executions.
     *
     * @param start the start
     * @param count the count
     * @return the job executions
     * @see SearchableJobExecutionDao#getJobExecutions(int, int)
     */
    public List<JobExecution> getJobExecutions(int start, int count) {
        if (start <= 0) {
            return getJdbcTemplate()
                    .query(allExecutionsPagingQueryProvider
                                    .generateFirstPageQuery(count),
                            new JobExecutionRowMapper());
        }
        try {
            Long startAfterValue = getJdbcTemplate().queryForLong(
                    allExecutionsPagingQueryProvider.generateJumpToItemQuery(
                            start, count));
            return getJdbcTemplate()
                    .query(allExecutionsPagingQueryProvider
                                    .generateRemainingPagesQuery(count),
                            new JobExecutionRowMapper(), startAfterValue);
        } catch (IncorrectResultSizeDataAccessException e) {
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.batch.core.repository.dao.JdbcJobExecutionDao#saveJobExecution(org.springframework.batch.core.JobExecution)
     */
    @Override
    public void saveJobExecution(JobExecution jobExecution) {
        throw new UnsupportedOperationException(
                "SearchableJobExecutionDao is read only");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.batch.core.repository.dao.JdbcJobExecutionDao#synchronizeStatus(org.springframework.batch.core.JobExecution)
     */
    @Override
    public void synchronizeStatus(JobExecution jobExecution) {
        throw new UnsupportedOperationException(
                "SearchableJobExecutionDao is read only");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.batch.core.repository.dao.JdbcJobExecutionDao#updateJobExecution(org.springframework.batch.core.JobExecution)
     */
    @Override
    public void updateJobExecution(JobExecution jobExecution) {
        throw new UnsupportedOperationException(
                "SearchableJobExecutionDao is read only");
    }

    /**
     * Re-usable mapper for {@link JobExecution} instances.
     *
     * @author Dave Syer
     */
    protected class JobExecutionRowMapper implements RowMapper<JobExecution> {

        /**
         * The Constant FIND_PARAMS_FROM_ID.
         */
        private static final String FIND_PARAMS_FROM_ID = "SELECT JOB_EXECUTION_ID, KEY_NAME, TYPE_CD, "
                + "STRING_VAL, DATE_VAL, LONG_VAL, DOUBLE_VAL, IDENTIFYING from %PREFIX%JOB_EXECUTION_PARAMS where JOB_EXECUTION_ID = ?";

        /**
         * The Constant JOB_PARAM_PROCESS_7.
         */
        private static final int JOB_PARAM_PROCESS_7 = 7;

        /**
         * The Constant JOB_PARAM_PROCESS_5.
         */
        private static final int JOB_PARAM_PROCESS_5 = 5;

        /**
         * The Constant JOB_PARAM_PROCESS_8.
         */
        private static final int JOB_PARAM_PROCESS_8 = 8;

        /**
         * The Constant JOB_PARAM_PROCESS_6.
         */
        private static final int JOB_PARAM_PROCESS_6 = 6;

        /**
         * The Constant JOB_PARAM_PROCESS_4.
         */
        private static final int JOB_PARAM_PROCESS_4 = 4;

        /**
         * The Constant RESULT_SET_ROW_MAP_9.
         */
        private static final int RESULT_SET_ROW_MAP_9 = 9;

        /**
         * The Constant RESULT_SET_ROW_MAP_8.
         */
        private static final int RESULT_SET_ROW_MAP_8 = 8;

        /**
         * The Constant RESULT_SET_ROW_MAP_7.
         */
        private static final int RESULT_SET_ROW_MAP_7 = 7;

        /**
         * The Constant RESULT_SET_ROW_MAP_6.
         */
        private static final int RESULT_SET_ROW_MAP_6 = 6;

        /**
         * The Constant RESULT_SET_ROW_MAP_5.
         */
        private static final int RESULT_SET_ROW_MAP_5 = 5;

        /**
         * The Constant RESULT_SET_ROW_MAP_4.
         */
        private static final int RESULT_SET_ROW_MAP_4 = 4;

        /**
         * The Constant RESULT_SET_ROW_MAP_3.
         */
        private static final int RESULT_SET_ROW_MAP_3 = 3;

        /**
         * The Constant RESULT_SET_ROW_MAP_2.
         */
        private static final int RESULT_SET_ROW_MAP_2 = 2;

        /**
         * The Constant RESULT_SET_ROW_MAP_11.
         */
        private static final int RESULT_SET_ROW_MAP_11 = 11;

        /**
         * The Constant _RESULT_SET_ROW_MAP_10.
         */
        private static final int RESULT_SET_ROW_MAP_10 = 10;

        /**
         * The Constant JOB_PARAM_PROCESS_ROW_Y.
         */
        private static final String JOB_PARAM_PROCESS_ROW_Y = "Y";

        /**
         * The Constant PARAM_TYPE_PROCESS_ROW_3.
         */
        private static final int PARAM_TYPE_PROCESS_ROW_3 = 3;

        /**
         * Instantiates a new job execution row mapper.
         */
        public JobExecutionRowMapper() {
        }

        /**
         * {@inheritDoc}
         *
         * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet,
         * int)
         */
        @Override
        public JobExecution mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            Long id = rs.getLong(1);
            JobExecution jobExecution;

            JobParameters jobParameters = getJobParameters(id);

            JobInstance jobInstance = new JobInstance(
                    rs.getLong(RESULT_SET_ROW_MAP_10),
                    rs.getString(RESULT_SET_ROW_MAP_11));
            jobExecution = new JobExecution(jobInstance, jobParameters);
            jobExecution.setId(id);

            jobExecution.setStartTime(rs.getTimestamp(RESULT_SET_ROW_MAP_2));
            jobExecution.setEndTime(rs.getTimestamp(RESULT_SET_ROW_MAP_3));
            jobExecution.setStatus(BatchStatus.valueOf(rs
                    .getString(RESULT_SET_ROW_MAP_4)));
            jobExecution.setExitStatus(new ExitStatus(rs
                    .getString(RESULT_SET_ROW_MAP_5), rs
                    .getString(RESULT_SET_ROW_MAP_6)));
            jobExecution.setCreateTime(rs.getTimestamp(RESULT_SET_ROW_MAP_7));
            jobExecution.setLastUpdated(rs.getTimestamp(RESULT_SET_ROW_MAP_8));
            jobExecution.setVersion(rs.getInt(RESULT_SET_ROW_MAP_9));
            return jobExecution;
        }

        /**
         * Gets the job parameters.
         *
         * @param executionId the execution id
         * @return the job parameters
         */
        protected JobParameters getJobParameters(Long executionId) {
            final Map<String, JobParameter> map = new HashMap<String, JobParameter>();
            RowCallbackHandler handler = new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    ParameterType type = ParameterType.valueOf(rs
                            .getString(PARAM_TYPE_PROCESS_ROW_3));
                    JobParameter value = null;

                    if (type == ParameterType.STRING) {
                        value = new JobParameter(
                                rs.getString(JOB_PARAM_PROCESS_4), rs
                                .getString(JOB_PARAM_PROCESS_8)
                                .equalsIgnoreCase(
                                        JOB_PARAM_PROCESS_ROW_Y));
                    } else if (type == ParameterType.LONG) {
                        value = new JobParameter(
                                rs.getLong(JOB_PARAM_PROCESS_6), rs.getString(
                                JOB_PARAM_PROCESS_8).equalsIgnoreCase(
                                JOB_PARAM_PROCESS_ROW_Y));
                    } else if (type == ParameterType.DOUBLE) {
                        value = new JobParameter(
                                rs.getDouble(JOB_PARAM_PROCESS_7), rs
                                .getString(JOB_PARAM_PROCESS_8)
                                .equalsIgnoreCase(
                                        JOB_PARAM_PROCESS_ROW_Y));
                    } else if (type == ParameterType.DATE) {
                        value = new JobParameter(
                                rs.getTimestamp(JOB_PARAM_PROCESS_5), rs
                                .getString(JOB_PARAM_PROCESS_8)
                                .equalsIgnoreCase(
                                        JOB_PARAM_PROCESS_ROW_Y));
                    }

                    // No need to assert that value is not null because it's an
                    // enum
                    map.put(rs.getString(2), value);
                }
            };

            getJdbcTemplate().query(getQuery(FIND_PARAMS_FROM_ID),
                    new Object[]{executionId}, handler);

            return new JobParameters(map);
        }

    }
}
