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

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;

/**
 * The Class JobSupport.
 * 
 * @author : aymen.abbes@ext.mpsa.com.
 */
public class JobSupport implements Job {

	/**
	 * The name.
	 */
	private String name;

	/**
	 * The incrementer.
	 */
	private JobParametersIncrementer incrementer;

	/**
	 * Instantiates a new job support.
	 * 
	 * @param name
	 *            the name
	 */
	public JobSupport(String name) {
		this(name, null);
	}

	/**
	 * Instantiates a new job support.
	 * 
	 * @param name
	 *            the name
	 * @param incrementer
	 *            the incrementer
	 */
	public JobSupport(String name, JobParametersIncrementer incrementer) {
		super();
		this.name = name;
		this.incrementer = incrementer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.core.Job#execute(org.springframework.batch.
	 * core.JobExecution)
	 */
	/**
	 * {@inheritDoc}
	 * 
	 * @see org.springframework.batch.core.Job#execute(org.springframework.batch.core.JobExecution)
	 */
	public void execute(JobExecution execution) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.Job#getJobParametersIncrementer()
	 */
	/**
	 * {@inheritDoc}
	 * 
	 * @see org.springframework.batch.core.Job#getJobParametersIncrementer()
	 */
	public JobParametersIncrementer getJobParametersIncrementer() {
		return incrementer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.Job#getName()
	 */
	/**
	 * {@inheritDoc}
	 * 
	 * @see org.springframework.batch.core.Job#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.Job#isRestartable()
	 */
	/**
	 * {@inheritDoc}
	 * 
	 * @see org.springframework.batch.core.Job#isRestartable()
	 */
	public boolean isRestartable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.Job#getJobParametersValidator()
	 */
	/**
	 * {@inheritDoc}
	 * 
	 * @see org.springframework.batch.core.Job#getJobParametersValidator()
	 */
	public JobParametersValidator getJobParametersValidator() {
		return new DefaultJobParametersValidator();
	}

}
