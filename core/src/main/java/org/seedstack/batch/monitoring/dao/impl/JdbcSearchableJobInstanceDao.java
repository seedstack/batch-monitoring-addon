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

import org.springframework.batch.core.repository.dao.JdbcJobExecutionDao;
import org.springframework.batch.core.repository.dao.JdbcJobInstanceDao;
import org.springframework.jdbc.support.incrementer.AbstractDataFieldMaxValueIncrementer;

import org.seedstack.batch.monitoring.dao.SearchableJobInstanceDao;

/**
 * The Class JdbcSearchableJobInstanceDao.
 * 
 * @author : aymen.abbes@ext.mpsa.com.
 */
public class JdbcSearchableJobInstanceDao extends JdbcJobInstanceDao implements
		SearchableJobInstanceDao {

	/**
	 * The Constant GET_COUNT_BY_JOB_NAME.
	 */
	private static final String GET_COUNT_BY_JOB_NAME = "SELECT COUNT(1) from %PREFIX%JOB_INSTANCE "
			+ "where JOB_NAME=?";

	/**
	 * After properties set.
	 * 
	 * @throws Exception
	 *             the exception
	 * @see JdbcJobExecutionDao#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		setJobIncrementer(new AbstractDataFieldMaxValueIncrementer() {
			@Override
			protected long getNextKey() {
				return 0;
			}
		});

		super.afterPropertiesSet();

	}

	/**
	 * Count job instances.
	 * 
	 * @param name
	 *            the name
	 * @return the int
	 * @see SearchableJobInstanceDao#countJobInstances (String)
	 */
	public int countJobInstances(String name) {
		return getJdbcTemplate().queryForInt(getQuery(GET_COUNT_BY_JOB_NAME),
				name);
	}

}
