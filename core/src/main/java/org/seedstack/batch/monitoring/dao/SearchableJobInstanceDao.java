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

package org.seedstack.batch.monitoring.dao;

import org.springframework.batch.core.repository.dao.JobInstanceDao;


/**
 * The Interface SearchableJobInstanceDao.
 * 
 * @author : aymen.abbes@ext.mpsa.com.
 */
public interface SearchableJobInstanceDao extends JobInstanceDao {

	/**
	 * Count job instances.
	 * 
	 * @param name
	 *            the name of the job instances
	 * @return the number of instances
	 */
	int countJobInstances(String name);

}
