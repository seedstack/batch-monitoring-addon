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
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.batch.monitoring.dao.impl.JdbcSearchableJobExecutionDao;
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
public class JdbcSearchableJobExecutionDaoIT {


	private JdbcSearchableJobExecutionDao dao;


	@Autowired
	private JobRepositoryTestUtils jobRepositoryUtils;


	private List<JobExecution> list;


	@Autowired
	public void setDataSource(DataSource dataSource) throws Exception {
		dao = new JdbcSearchableJobExecutionDao();
		dao.setDataSource(dataSource);
		dao.afterPropertiesSet();
	}


	@BeforeTransaction
	public void prepareExecutions() throws Exception {
		jobRepositoryUtils.removeJobExecutions();
        list = jobRepositoryUtils.createJobExecutions(3);
	}


	@AfterTransaction
	public void removeExecutions() throws Exception {
		jobRepositoryUtils.removeJobExecutions(list);
	}


	@Test
	@Transactional
	public void testCountJobExecutions() {
		assertEquals(3, dao.countJobExecutions());
    }


    @Test
	@Transactional
	public void testGetJobExecutions() {
		List<JobExecution> jobExecutions = dao.getJobExecutions(0, 10);
		assertEquals(3, jobExecutions.size());
		assertNotNull(jobExecutions.get(0).getJobInstance());
    }


    @Test
	@Transactional
	public void testGetJobExecutionsPaged() {
		List<JobExecution> jobExecutions = dao.getJobExecutions(2, 2);
		assertEquals(1, jobExecutions.size());
		assertEquals(list.get(0), jobExecutions.get(0));
    }


    @Test
	@Transactional
	public void testGetJobExecutionsLatest() throws Exception {
		list.addAll(jobRepositoryUtils.createJobExecutions("other",
				new String[] { "step" }, 1));
		List<JobExecution> jobExecutions = dao.getJobExecutions(0, 10);
		assertEquals(4, jobExecutions.size());
		assertEquals(list.get(list.size() - 1), jobExecutions.get(0));
	}


	@Test
	@Transactional
	public void testGetJobExecutionsLatestForJob() throws Exception {
		list.addAll(jobRepositoryUtils.createJobExecutions("job",
				new String[] { "step" }, 1));
		List<JobExecution> jobExecutions = dao.getJobExecutions("job", 0, 1);
		assertEquals(1, jobExecutions.size());
		assertEquals(list.get(list.size() - 1), jobExecutions.get(0));
	}


	@Test
	@Transactional
	public void testGetJobExecutionsByName() {
		assertEquals(3, dao.getJobExecutions("job", 0, 10).size());
	}


	@Test
	@Transactional
	public void testCountJobExecutionsByName() {
		assertEquals(3, dao.countJobExecutions("job"));
	}


    @Test
	@Transactional
	public void testGetJobExecutionsByNamePaged() {
		List<JobExecution> jobExecutions = dao.getJobExecutions("job", 2, 2);
		assertEquals(1, jobExecutions.size());
		assertEquals(list.get(0), jobExecutions.get(0));
	}


	@Test
	@Transactional
	public void testGetJobExecutionsPastEnd() {
		List<JobExecution> jobExecutions = dao
				.getJobExecutions("job", 100, 100);
        assertEquals(0, jobExecutions.size());
	}


	@Test
    @Transactional
	public void testGetJobExecutionsByNamePastEnd() {
		List<JobExecution> jobExecutions = dao.getJobExecutions(100, 100);
		assertEquals(0, jobExecutions.size());
	}

}
