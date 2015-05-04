/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.batch.monitoring.history;

import java.math.BigDecimal;

/**
 * The Class CumulativeHistory.
 * 
 * @author aymen.abbes@ext.mpsa.com
 * 
 */
public class CumulativeHistory {

	/** The count. */
	private int count;

	/** The sum. */
	private double sum;

	/** The sum squares. */
	private double sumSquares;

	/** The min. */
	private double min;

	/** The max. */
	private double max;

	/**
	 * Append.
	 * 
	 * @param value
	 *            the value
	 */
	public void append(double value) {
		if (value > max || count == 0)
			max = value;
		if (value < min || count == 0)
			min = value;
		sum += value;
		sumSquares += value * value;
		count++;
	}

	/**
	 * Gets the count.
	 * 
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Gets the mean.
	 * 
	 * @return the mean
	 */
	public double getMean() {
		return doubleWithDicimalplace((count > 0 ? sum / count : 0), 2);
	}

	/**
	 * Gets the standard deviation.
	 * 
	 * @return the standard deviation
	 */
	public double getStandardDeviation() {
		double mean = getMean();
		double sqrt = Math.sqrt(sumSquares / count - mean * mean);
		return doubleWithDicimalplace((count > 0 && sqrt > 0 ? sqrt : 0), 2);
	}

	/**
	 * Gets the max.
	 * 
	 * @return the max
	 */
	public double getMax() {
		return max;
	}

	/**
	 * Gets the min.
	 * 
	 * @return the min
	 */
	public double getMin() {
		return min;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("[N=%d, min=%f, max=%f, mean=%f, sigma=%f]",
				count, min, max, getMean(), getStandardDeviation());
	}

	/**
	 * @param num
	 * @param decimalPlace
	 * @return
	 */
	private double doubleWithDicimalplace(double num, int decimalPlace) {
		BigDecimal bd = new BigDecimal(num);
		return bd.setScale(decimalPlace, BigDecimal.ROUND_UP).doubleValue();
	}
}
