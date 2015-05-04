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
package org.seedstack.batch.monitoring.service;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.batch.monitoring.dao.impl.JdbcSearchableJobExecutionDao;
import org.seedstack.batch.monitoring.dao.impl.JdbcSearchableStepExecutionDao;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring/test-config.xml")
public class JdbcSearchableStepExecutionDaoIT {


	private JdbcSearchableStepExecutionDao dao;


	@Autowired
	private JobRepositoryTestUtils jobRepositoryUtils;


	private JdbcSearchableJobExecutionDao jobExecutionDao;


	private List<JobExecution> list;


	@Autowired
	public void setDataSource(DataSource dataSource) throws Exception {
		dao = new JdbcSearchableStepExecutionDao();
		dao.setDataSource(dataSource);
		dao.afterPropertiesSet();
		jobExecutionDao = new JdbcSearchableJobExecutionDao();
		jobExecutionDao.setDataSource(dataSource);
		jobExecutionDao.afterPropertiesSet();
	}


	@BeforeTransaction
	public void prepareExecutions() throws Exception {
		jobRepositoryUtils.removeJobExecutions(jobExecutionDao
                .getJobExecutions(0, 1000));
		list = jobRepositoryUtils.createJobExecutions(3);
	}


	@AfterTransaction
	public void removeExecutions() throws Exception {
		jobRepositoryUtils.removeJobExecutions(list);
	}


	@Test
	@Transactional
	public void testFindStepNames() {
		assertEquals("[step]", dao.findStepNamesForJobExecution("job", "-")
                .toString());
	}


	@Test
	@Transactional
	public void testFindStepNamesWithMoreJobs() throws Exception {
		list.addAll(jobRepositoryUtils.createJobExecutions("other",
				new String[] { "step" }, 1));
		assertEquals("[step]", dao.findStepNamesForJobExecution("job", "-")
				.toString());
	}


	@Test
	@Transactional
	public void testFindStepNamesWithMatch() {
		assertEquals("[]", dao.findStepNamesForJobExecution("job", "*")
				.toString());
	}


	@Test
	@Transactional
	public void testFindStepExecutionsByName() {
		assertEquals(1, dao.findStepExecutions("job", "step", 2, 2).size());
	}


	@Test
	@Transactional
	public void testFindStepExecutionsByNameWithMoreJobs() throws Exception {
		list.addAll(jobRepositoryUtils.createJobExecutions("other",
				new String[] { "step" }, 2));
		assertEquals(1, dao.findStepExecutions("job", "step", 2, 2).size());
	}


	@Test
	@Transactional
	public void testFindStepExecutionsByPattern() {
		assertEquals(1, dao.findStepExecutions("job", "s*", 2, 2).size());
	}


	@Test
	@Transactional
	public void testFindStepExecutionsPastEnd() {
		assertEquals(0, dao.findStepExecutions("job", "step", 100, 100).size());
	}


	@Test
	@Transactional
	public void testCountStepExecutionsByName() {
		assertEquals(3, dao.countStepExecutions("job", "step"));
	}


	@Test
	@Transactional
	public void testCountStepExecutionsByNameWithMoreJobs() throws Exception {
		list.addAll(jobRepositoryUtils.createJobExecutions("other",
				new String[] { "step" }, 2));
		assertEquals(3, dao.countStepExecutions("job", "step"));
	}


	@Test
	@Transactional
	public void testCountStepExecutionsByPattern() {
		assertEquals(3, dao.countStepExecutions("job", "s*"));
	}


	@Test
	@Transactional
	public void testCountStepExecutionsByPatternWithMoreJobs() throws Exception {
		list.addAll(jobRepositoryUtils.createJobExecutions("other",
				new String[] { "step" }, 2));
		assertEquals(3, dao.countStepExecutions("job", "s*"));
	}

}
