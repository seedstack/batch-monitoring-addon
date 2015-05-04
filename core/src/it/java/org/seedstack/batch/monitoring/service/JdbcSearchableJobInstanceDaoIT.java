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
import org.seedstack.batch.monitoring.dao.impl.JdbcSearchableJobInstanceDao;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring/test-config.xml")
public class JdbcSearchableJobInstanceDaoIT {


	private JdbcSearchableJobInstanceDao dao;


	@Autowired
	private JobRepositoryTestUtils jobRepositoryUtils;


	private List<JobExecution> list;


	@Autowired
	public void setDataSource(DataSource dataSource) throws Exception {
		dao = new JdbcSearchableJobInstanceDao();
		dao.setJdbcTemplate(new JdbcTemplate(dataSource));
		dao.afterPropertiesSet();
	}

	// Need to use @BeforeTransaction because the job repository defaults to
	// propagation=REQUIRES_NEW for createJobExecution()

	@BeforeTransaction
	public void prepareExecutions() throws Exception {
		list = jobRepositoryUtils.createJobExecutions(3);
	}


	@AfterTransaction
	public void removeExecutions() throws Exception {
		jobRepositoryUtils.removeJobExecutions(list);
	}


	@Test
	@Transactional
	public void testGetJobInstancesByName() {
		assertEquals(3, dao.getJobInstances("job", 0, 10).size());
	}


	@Test
	@Transactional
	public void testCountJobInstancesByName() {
		assertEquals(3, dao.countJobInstances("job"));
	}


	@Test
	@Transactional
	public void testGetJobInstancesByNamePaged() {
		List<JobInstance> jobInstances = dao.getJobInstances("job", 2, 2);
		assertEquals(1, jobInstances.size());
		assertEquals(list.get(0), jobInstances.get(0));
	}

}
