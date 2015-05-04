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
package org.seedstack.batch.monitoring.exception;

import org.springframework.batch.core.JobExecutionException;


/**
 * The Class NoSuchStepExecutionException.
 * 
 * @author : aymen.abbes@ext.mpsa.com.
 */
@SuppressWarnings("serial")
public class NoSuchStepExecutionException extends JobExecutionException {

	/**
	 * Instantiates a new no such step execution exception.
	 * 
	 * @param msg
	 *            the msg
	 * @param cause
	 *            the cause
	 */
	public NoSuchStepExecutionException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Instantiates a new no such step execution exception.
	 * 
	 * @param msg
	 *            the msg
	 */
	public NoSuchStepExecutionException(String msg) {
		super(msg);
	}

}
