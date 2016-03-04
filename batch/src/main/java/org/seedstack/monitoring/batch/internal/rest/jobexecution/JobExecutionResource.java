/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.monitoring.batch.internal.rest.jobexecution;

import org.seedstack.seed.security.RequiresPermissions;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.admin.web.JobExecutionInfo;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;

/**
 * Resource for job executions.
 *
 * @author aymen.abbes@ext.mpsa.com
 */
@Path("/seed-monitoring/jobs/{jobName}/job-executions")
public class JobExecutionResource {
    @Inject
    @Named("jobService")
    private JobService jobService;

    /**
     * Retrieves the list of job executions by job name.
     *
     * @param jobName  the job name
     * @param pageSize the size of the result page
     * @return the list of {@link JobExecutionInfo}
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermissions("seed:monitoring:batch:read")
    public Response jobExecutionsByJobName(
            @PathParam("jobName") String jobName,
            @DefaultValue("1") @QueryParam("pageIndex") int pageIndex,
            @DefaultValue("1000") @QueryParam("pageSize") int pageSize) {

        int startJob = (pageIndex - 1) * pageSize;
        ArrayList<JobExecutionInfo> jobExecutionInfos = new ArrayList<JobExecutionInfo>();
        int totalItems;
        try {
            totalItems = jobService.countJobExecutionsForJob(jobName);
            Collection<JobExecution> jobExecutions = jobService
                    .listJobExecutionsForJob(jobName, startJob, pageSize);
            for (JobExecution jobExecution : jobExecutions) {

                JobExecutionInfo jobExecutionInfo = new JobExecutionInfo(
                        jobExecution, new GregorianCalendar().getTimeZone());
                jobExecutionInfos.add(jobExecutionInfo);

            }

        } catch (NoSuchJobException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("There is no such job (" + jobName + ") ").type(MediaType.TEXT_PLAIN).build();
        }
        JobExecutionRepresentation jobExecutionRepresentation = new JobExecutionRepresentation(
                pageIndex, pageSize, totalItems, jobExecutionInfos);
        return Response.ok(jobExecutionRepresentation).build();
    }

    /**
     * Retrieves the job execution by id.
     *
     * @param jobExecutionId the job execution id
     * @return JobExecutionRepresentation
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{jobExecutionId}")
    @RequiresPermissions("seed:monitoring:batch:read")
    public Response jobExecutionById(
            @PathParam("jobExecutionId") long jobExecutionId) {

        JobExecution jobExecution;
        int totalItems;
        try {
            totalItems = jobService.countJobExecutions();
            jobExecution = jobService.getJobExecution(jobExecutionId);

        } catch (NoSuchJobExecutionException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("There is no such job execution (" + jobExecutionId + ")").type(MediaType.TEXT_PLAIN).build();
        }

        ArrayList<JobExecutionInfo> executionInfos = new ArrayList<JobExecutionInfo>();
        executionInfos.add(new JobExecutionInfo(jobExecution,
                new GregorianCalendar().getTimeZone()));

        JobExecutionRepresentation jobExecutionRepresentation = new JobExecutionRepresentation(
                0, 0, totalItems, executionInfos);
        return Response.ok(jobExecutionRepresentation).build();
    }
}
