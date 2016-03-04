/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.monitoring.batch.internal.rest.stepexecution;

import org.seedstack.seed.security.RequiresPermissions;
import org.springframework.batch.admin.history.StepExecutionHistory;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.admin.service.NoSuchStepExecutionException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TimeZone;

/**
 * Resource for step executions.
 *
 * @author aymen.abbes@ext.mpsa.com
 */
@Path("/seed-monitoring/jobs/executions/{jobExecutionId}/steps")
public class StepExecutionResource {
    @Inject
    @Named("jobService")
    private JobService jobService;

    /**
     * Retrieves the list of all steps by job execution id.
     *
     * @param jobExecutionId the job execution id
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermissions("seed:monitoring:batch:read")
    public Response list(@PathParam("jobExecutionId") long jobExecutionId) {

        Collection<StepExecutionRepresentation> stepExecutionRepresentations = new ArrayList<StepExecutionRepresentation>();
        try {
            for (StepExecution stepExecution : jobService
                    .getStepExecutions(jobExecutionId)) {
                if (stepExecution.getId() != null) {
                    stepExecutionRepresentations
                            .add(new StepExecutionRepresentation(stepExecution,
                                    TimeZone.getTimeZone("GMT")));
                }
            }

        } catch (NoSuchJobExecutionException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("There is no such job execution (" + jobExecutionId
                            + ")").type(MediaType.TEXT_PLAIN).build();
        }
        return Response.ok(stepExecutionRepresentations).build();

    }

    /**
     * Retrieves the detail of a step by id.
     *
     * @param jobExecutionId  the job execution id
     * @param stepExecutionId the step execution id
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{stepExecutionId}")
    @RequiresPermissions("seed:monitoring:batch:read")
    public Response detail(@PathParam("jobExecutionId") long jobExecutionId,
                           @PathParam("stepExecutionId") long stepExecutionId) {

        StepExecutionRepresentation stepExecutionRepresentation;
        try {
            StepExecution stepExecution = jobService.getStepExecution(
                    jobExecutionId, stepExecutionId);
            stepExecutionRepresentation = new StepExecutionRepresentation(
                    stepExecution, TimeZone.getTimeZone("GMT"));
        } catch (NoSuchStepExecutionException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("There is no such step execution ("
                            + stepExecutionId + ")").type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (NoSuchJobExecutionException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("There is no such job execution (" + jobExecutionId
                            + ")").type(MediaType.TEXT_PLAIN).build();
        }

        return Response.ok(stepExecutionRepresentation).build();
    }

    /**
     * Retrieves the history by job execution id and step execution id.
     *
     * @param jobExecutionId  the job execution id
     * @param stepExecutionId the step execution id
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{stepExecutionId}/progress")
    @RequiresPermissions("seed:monitoring:batch:read")
    public Response history(@PathParam("jobExecutionId") long jobExecutionId,
                            @PathParam("stepExecutionId") long stepExecutionId) {

        StepExecutionProgressPresentation stepExecutionProgress;
        try {
            StepExecution stepExecution = jobService.getStepExecution(
                    jobExecutionId, stepExecutionId);

            String stepName = stepExecution.getStepName();
            if (stepName.contains(":partition")) {
                // assume we want to compare all partitions
                stepName = stepName.replaceAll("(:partition).*", "$1*");
            }
            String jobName = stepExecution.getJobExecution().getJobInstance()
                    .getJobName();
            StepExecutionHistory stepExecutionHistory = computeHistory(jobName,
                    stepName);
            stepExecutionProgress = new StepExecutionProgressPresentation(
                    stepExecution, stepExecutionHistory);
        } catch (NoSuchStepExecutionException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("There is no such step execution ("
                            + stepExecutionId + ")").type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (NoSuchJobExecutionException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("There is no such job execution (" + jobExecutionId
                            + ")").type(MediaType.TEXT_PLAIN).build();
        }

        return Response.ok(stepExecutionProgress).build();

    }

    /**
     * Compute history.
     *
     * @param jobName  the job name
     * @param stepName the step name
     * @return the step execution history
     */
    private StepExecutionHistory computeHistory(String jobName, String stepName) {
        int total = jobService.countStepExecutionsForStep(jobName, stepName);
        StepExecutionHistory stepExecutionHistory = new StepExecutionHistory(
                stepName);
        for (int i = 0; i < total; i += 1000) {
            for (StepExecution stepExecution : jobService
                    .listStepExecutionsForStep(jobName, stepName, i, 1000)) {
                stepExecutionHistory.append(stepExecution);
            }
        }
        return stepExecutionHistory;
    }

}
