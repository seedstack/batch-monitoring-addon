/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.monitoring.batch.internal.rest.stepexecution;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.util.Assert;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The Class StepExecutionDetailsRepresentation.
 *
 * @author aymen.abbes@ext.mpsa.com
 */
public class StepExecutionDetailsRepresentation {

    /**
     * The step name.
     */
    private String stepName;

    /**
     * The status.
     */
    private BatchStatus status = BatchStatus.STARTING;

    /**
     * The date format.
     */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * The time format.
     */
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * The read count.
     */
    private int readCount = 0;

    /**
     * The write count.
     */
    private int writeCount = 0;

    /**
     * The commit count.
     */
    private int commitCount = 0;

    /**
     * The rollback count.
     */
    private int rollbackCount = 0;

    /**
     * The read skip count.
     */
    private int readSkipCount = 0;

    /**
     * The process skip count.
     */
    private int processSkipCount = 0;

    /**
     * The write skip count.
     */
    private int writeSkipCount = 0;

    /**
     * The start time.
     */
    private String startTime = null;

    /**
     * The end time.
     */
    private String endTime = null;

    /**
     * The last updated.
     */
    private String lastUpdated = null;

    /**
     * The execution context.
     */
    private ExecutionContext executionContext = new ExecutionContext();

    /**
     * The terminate only.
     */
    private boolean terminateOnly;

    /**
     * The filter count.
     */
    private int filterCount;

    /**
     * The exit code.
     */
    private String statusExitCode;

    /**
     * The exit description.
     */
    private String statusExitDescription;

    /**
     * The failure exceptions.
     */
    private transient List<Throwable> failureExceptions = new CopyOnWriteArrayList<Throwable>();

    /**
     * Constructor that substitutes in null for the execution id.
     *
     * @param stepExecution the step execution
     */
    public StepExecutionDetailsRepresentation(StepExecution stepExecution) {
        Assert.notNull(stepExecution.getId(),
                "The entity Id must be provided to re-hydrate an existing StepExecution");
        this.stepName = stepExecution.getStepName();
        this.commitCount = stepExecution.getCommitCount();
        this.endTime = stepExecution.getEndTime() == null ? "" : timeFormat
                .format(stepExecution.getEndTime());
        this.executionContext = stepExecution.getExecutionContext();

        this.statusExitCode = stepExecution.getExitStatus() != null ? stepExecution
                .getExitStatus().getExitCode() : "";
        this.statusExitDescription = stepExecution.getExitStatus() != null ? stepExecution
                .getExitStatus().getExitDescription() : "";
        this.failureExceptions = stepExecution.getFailureExceptions();
        this.filterCount = stepExecution.getFilterCount();
        this.lastUpdated = stepExecution.getLastUpdated() == null ? ""
                : dateFormat.format(stepExecution.getLastUpdated());
        this.processSkipCount = stepExecution.getProcessSkipCount();
        this.readCount = stepExecution.getReadCount();
        this.readSkipCount = stepExecution.getReadSkipCount();
        this.rollbackCount = stepExecution.getRollbackCount();
        this.startTime = timeFormat.format(stepExecution.getStartTime());
        this.status = stepExecution.getStatus();
        this.stepName = stepExecution.getStepName();
        this.terminateOnly = stepExecution.isTerminateOnly();
        this.writeCount = stepExecution.getWriteCount();
        this.writeSkipCount = stepExecution.getWriteSkipCount();
    }

    public String getStatusExitCode() {
        return statusExitCode;
    }

    public void setStatusExitCode(String statusExitCode) {
        this.statusExitCode = statusExitCode;
    }

    public String getStatusExitDescription() {
        return statusExitDescription;
    }

    public void setStatusExitDescription(String statusExitDescription) {
        this.statusExitDescription = statusExitDescription;
    }

    /**
     * Returns the {@link ExecutionContext} for this execution.
     *
     * @return the attributes
     */
    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    /**
     * Sets the {@link ExecutionContext} for this execution.
     *
     * @param executionContext the attributes
     */
    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * Returns the current number of commits for this execution.
     *
     * @return the current number of commits
     */
    public int getCommitCount() {
        return commitCount;
    }

    /**
     * Sets the current number of commits for this execution.
     *
     * @param commitCount the current number of commits
     */
    public void setCommitCount(int commitCount) {
        this.commitCount = commitCount;
    }

    /**
     * Returns the time that this execution ended.
     *
     * @return the time that this execution ended
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * Sets the time that this execution ended.
     *
     * @param endTime the time that this execution ended
     */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
     * Returns the current number of items read for this execution.
     *
     * @return the current number of items read for this execution
     */
    public int getReadCount() {
        return readCount;
    }

    /**
     * Sets the current number of read items for this execution.
     *
     * @param readCount the current number of read items for this execution
     */
    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    /**
     * Returns the current number of items written for this execution.
     *
     * @return the current number of items written for this execution
     */
    public int getWriteCount() {
        return writeCount;
    }

    /**
     * Sets the current number of written items for this execution.
     *
     * @param writeCount the current number of written items for this execution
     */
    public void setWriteCount(int writeCount) {
        this.writeCount = writeCount;
    }

    /**
     * Returns the current number of rollbacks for this execution.
     *
     * @return the current number of rollbacks for this execution
     */
    public int getRollbackCount() {
        return rollbackCount;
    }

    /**
     * Returns the current number of items filtered out of this execution.
     *
     * @return the current number of items filtered out of this execution
     */
    public int getFilterCount() {
        return filterCount;
    }

    /**
     * Public setter for the number of items filtered out of this execution.
     *
     * @param filterCount the number of items filtered out of this execution to set
     */
    public void setFilterCount(int filterCount) {
        this.filterCount = filterCount;
    }

    /**
     * Setter for number of rollbacks for this execution.
     *
     * @param rollbackCount the new rollback count
     */
    public void setRollbackCount(int rollbackCount) {
        this.rollbackCount = rollbackCount;
    }

    /**
     * Gets the time this execution started.
     *
     * @return the time this execution started
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Sets the time this execution started.
     *
     * @param startTime the time this execution started
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the current status of this step.
     *
     * @return the current status of this step
     */
    public BatchStatus getStatus() {
        return status;
    }

    /**
     * Sets the current status of this step.
     *
     * @param status the current status of this step
     */
    public void setStatus(BatchStatus status) {
        this.status = status;
    }

    /**
     * Upgrade the status field if the provided value is greater than the
     * existing one. Clients using this method to set the status can be sure
     * that they don't overwrite a failed status with an successful one.
     *
     * @param status the new status value
     */
    public void upgradeStatus(BatchStatus status) {
        this.status = this.status.upgradeTo(status);
    }

    /**
     * Gets the step name.
     *
     * @return the name of the step
     */
    public String getStepName() {
        return stepName;
    }

    /**
     * Checks if is terminate only.
     *
     * @return flag to indicate that an execution should halt
     */
    public boolean isTerminateOnly() {
        return this.terminateOnly;
    }

    /**
     * Set a flag that will signal to an execution environment that this
     * execution (and its surrounding job) wishes to exit.
     */
    public void setTerminateOnly() {
        this.terminateOnly = true;
    }

    /**
     * Gets the skip count.
     *
     * @return the total number of items skipped.
     */
    public int getSkipCount() {
        return readSkipCount + processSkipCount + writeSkipCount;
    }

    /**
     * Increment the number of commits.
     */
    public void incrementCommitCount() {
        commitCount++;
    }

    /**
     * Gets the read skip count.
     *
     * @return the number of records skipped on read
     */
    public int getReadSkipCount() {
        return readSkipCount;
    }

    /**
     * Gets the write skip count.
     *
     * @return the number of records skipped on write
     */
    public int getWriteSkipCount() {
        return writeSkipCount;
    }

    /**
     * Set the number of records skipped on read.
     *
     * @param readSkipCount the new read skip count
     */
    public void setReadSkipCount(int readSkipCount) {
        this.readSkipCount = readSkipCount;
    }

    /**
     * Set the number of records skipped on write.
     *
     * @param writeSkipCount the new write skip count
     */
    public void setWriteSkipCount(int writeSkipCount) {
        this.writeSkipCount = writeSkipCount;
    }

    /**
     * Gets the process skip count.
     *
     * @return the number of records skipped during processing
     */
    public int getProcessSkipCount() {
        return processSkipCount;
    }

    /**
     * Set the number of records skipped during processing.
     *
     * @param processSkipCount the new process skip count
     */
    public void setProcessSkipCount(int processSkipCount) {
        this.processSkipCount = processSkipCount;
    }

    /**
     * Gets the last updated.
     *
     * @return the Date representing the last time this execution was persisted.
     */
    public String getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Set the time when the StepExecution was last updated before persisting.
     *
     * @param lastUpdated the new last updated
     */
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Gets the failure exceptions.
     *
     * @return the failure exceptions
     */
    public List<Throwable> getFailureExceptions() {
        return failureExceptions;
    }

    /**
     * Adds the failure exception.
     *
     * @param throwable the throwable
     */
    public void addFailureException(Throwable throwable) {
        this.getFailureExceptions().add(throwable);
    }

    public void setFailureExceptions(List<Throwable> failureExceptions) {
        this.failureExceptions = failureExceptions;
    }

}
