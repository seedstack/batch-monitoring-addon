/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.batch.monitoring.util;

import java.util.Properties;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.support.PropertiesConverter;

/**
 * Helper class for extracting a String representation of {@link JobParameters}
 * for rendering.
 * 
 * @author aymen.abbes@ext.mpsa.com
 */
public class JobParametersExtractor {

	private JobParametersConverter converter = new DefaultJobParametersConverter();

	private static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	/**
	 * from Job Parameters
	 * 
	 * @param oldParameters
	 *            the latest job parameters
	 * @return a String representation for rendering the job parameters from the
	 *         last instance
	 */
	public String fromJobParameters(JobParameters oldParameters) {

		String properties = PropertiesConverter.propertiesToString(converter
				.getProperties(oldParameters));
		if (properties.startsWith("#")) {
			properties = properties.substring(properties
					.indexOf(LINE_SEPARATOR) + LINE_SEPARATOR.length());
		}
		properties = properties.replace("\\:", ":");
		return properties;

	}

	public JobParameters fromString(String params) {
		Properties properties = PropertiesConverter.stringToProperties(params);
		return converter.getJobParameters(properties);
	}

}
