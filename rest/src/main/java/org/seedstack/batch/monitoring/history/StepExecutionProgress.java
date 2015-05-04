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

import java.util.Date;

import org.springframework.batch.core.StepExecution;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.StringUtils;

/**
 * Step Execution Progress
 * 
 * @author aymen.abbes@ext.mpsa.com
 */
public class StepExecutionProgress {

	/**
	 * The Enum PercentCompleteBasis.
	 */
	private enum PercentCompleteBasis {

		/** The unknown. */
		UNKNOWN("unknown"), /** The noinformation. */
		NOINFORMATION("percent.no.information,no.information", "no information"),
		/** The endtime. */
		ENDTIME("percent.end.time,end.time", "end time (already finished)"),
		/** The duration. */
		DURATION("percent.duration,duration", "extrapolated duration"),
		/** The readcount. */
		READCOUNT("percent.read.count,read.count", "extrapolated read count"),
		/** The nohistory. */
		NOHISTORY("percent.no.history,no.history", "no history");

		/** The codes. */
		private final String[] codes;

		/** The message. */
		private final String message;

		/**
		 * Instantiates a new percent complete basis.
		 * 
		 * @param code
		 *            the code
		 */
		private PercentCompleteBasis(String code) {
			this(code, code);
		}

		/**
		 * Instantiates a new percent complete basis.
		 * 
		 * @param codes
		 *            the codes
		 * @param message
		 *            the message
		 */
		private PercentCompleteBasis(String codes, String message) {
			this(StringUtils.commaDelimitedListToStringArray(codes), message);
		}

		/**
		 * Instantiates a new percent complete basis.
		 * 
		 * @param code
		 *            the code
		 * @param message
		 *            the message
		 */
		private PercentCompleteBasis(String[] code, String message) {
			this.codes = code;
			this.message = message;
		}

		/**
		 * Gets the message.
		 * 
		 * @return the message
		 */
		public MessageSourceResolvable getMessage() {
			return new DefaultMessageSourceResolvable(codes, message);
		}

	}

	/** The step execution. */
	private final StepExecution stepExecution;

	/** The step execution history. */
	private final StepExecutionHistory stepExecutionHistory;

	/** The duration. */
	private double duration;

	/** The percentage complete. */
	private double percentageComplete = 0.5;

	/** The is finished. */
	private boolean isFinished = false;

	/** The percent complete basis. */
	private PercentCompleteBasis percentCompleteBasis = PercentCompleteBasis.UNKNOWN;

	/**
	 * Instantiates a new step execution progress.
	 * 
	 * @param stepExecution
	 *            the step execution
	 * @param stepExecutionHistory
	 *            the step execution history
	 */
	public StepExecutionProgress(StepExecution stepExecution,
			StepExecutionHistory stepExecutionHistory) {
		this.stepExecution = stepExecution;
		this.stepExecutionHistory = stepExecutionHistory;
		Date startTime = stepExecution.getStartTime();
		Date endTime = stepExecution.getEndTime();
		if (endTime == null) {
			endTime = new Date();
		} else {
			isFinished = true;
		}
		if (startTime == null) {
			startTime = new Date();
		}
		duration = endTime.getTime() - startTime.getTime();
		percentageComplete = calculatePercentageComplete();
	}

	/**
	 * Gets the estimated percent complete message.
	 * 
	 * @return the estimated percent complete message
	 */
	public MessageSourceResolvable getEstimatedPercentCompleteMessage() {

		String defaultMessage = String
				.format("This execution is estimated to be %.0f%% complete after %.0f ms based on %s",
						percentageComplete * 100, duration,
						percentCompleteBasis.getMessage().getDefaultMessage());

		DefaultMessageSourceResolvable message = new DefaultMessageSourceResolvable(
				new String[] { "step.execution.estimated.progress" },
				new Object[] { percentageComplete, duration,
						percentCompleteBasis.getMessage() }, defaultMessage);

		return message;

	}

	/**
	 * Gets the estimated percent complete.
	 * 
	 * @return the estimated percent complete
	 */
	public double getEstimatedPercentComplete() {
		return percentageComplete;
	}

	/**
	 * Calculate percentage complete.
	 * 
	 * @return the double
	 */
	private double calculatePercentageComplete() {

		if (isFinished) {
			percentCompleteBasis = PercentCompleteBasis.ENDTIME;
			return 1;
		}

		if (stepExecutionHistory.getCount() == 0) {
			percentCompleteBasis = PercentCompleteBasis.NOHISTORY;
			return 0.5;
		}

		CumulativeHistory readHistory = stepExecutionHistory.getReadCount();

		if (readHistory.getMean() == 0) {
			percentCompleteBasis = PercentCompleteBasis.DURATION;
			return getDurationBasedEstimate(duration);
		}

		percentCompleteBasis = PercentCompleteBasis.READCOUNT;
		return stepExecution.getReadCount() / readHistory.getMean();

	}

	/**
	 * Gets the duration based estimate.
	 * 
	 * @param duration
	 *            the duration
	 * @return the duration based estimate
	 */
	private double getDurationBasedEstimate(double duration) {

		CumulativeHistory durationHistory = stepExecutionHistory.getDuration();
		if (durationHistory.getMean() == 0) {
			percentCompleteBasis = PercentCompleteBasis.NOINFORMATION;
			return 0.5;
		}
		return duration / durationHistory.getMean();

	}

}
