/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.batch.monitoring.rest.jobinstance;

import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.admin.web.JobExecutionInfo;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;

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
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Resource for listing and launching jobs.
 *
 * @author aymen.abbes@ext.mpsa.com
 */
@Path("/jobs/{jobName}/job-instances")
public class JobInstanceResource {
    /**
     * The job service.
     */
    @Inject
    @Named("jobService")
    private JobService jobService;

    /**
     * Retrieves the list of job instances by job name.
     *
     * @param jobName  the job name
     * @param startJob the start job
     * @param pageSize the page size
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listJobInstancesForJobs(
            @PathParam("jobName") String jobName,
            @DefaultValue("0") @QueryParam("startJob") int startJob,
            @DefaultValue("20") @QueryParam("pageSize") int pageSize) {

        try {

            Collection<JobInstance> result = jobService.listJobInstances(
                    jobName, startJob, pageSize);
            Collection<JobInstanceRepresentation> jobInstancesRepresentations = new ArrayList<JobInstanceRepresentation>();
            for (JobInstance jobInstance : result) {
                Collection<JobExecutionInfo> executionRepresentations = new ArrayList<JobExecutionInfo>();

                Collection<JobExecution> jobExecutionsForJobInstance = jobService
                        .getJobExecutionsForJobInstance(jobName,
                                jobInstance.getId());

                for (JobExecution jobExecution : jobExecutionsForJobInstance) {
                    executionRepresentations
                            .add(new JobExecutionInfo(jobExecution,
                                    new GregorianCalendar().getTimeZone()));
                    jobInstancesRepresentations
                            .add(new JobInstanceRepresentation(jobInstance
                                    .getJobName(), jobInstance.getId(),
                                    jobExecution.getJobParameters(),
                                    executionRepresentations));
                }

            }
            return Response.ok(jobInstancesRepresentations).build();
        } catch (NoSuchJobException e) {
            String error = "There is no such job (" + jobName + ")";
            return Response.status(Response.Status.BAD_REQUEST).entity(error)
                    .type(MediaType.TEXT_PLAIN).build();
        }

    }

    /**
     * Retrieves the details of a job instance by id.
     *
     * @param jobName       the job name
     * @param jobInstanceId the job instance id
     * @return the response
     */
    @GET
    @Path("/{jobInstanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response details(@PathParam("jobName") String jobName,
                            @PathParam("jobInstanceId") long jobInstanceId) {

        JobInstance jobInstance;
        Calendar calendar = new GregorianCalendar();
        TimeZone timeZone = calendar.getTimeZone();
        try {
            jobInstance = jobService.getJobInstance(jobInstanceId);
            if (!jobInstance.getJobName().equals(jobName)) {

                String error = "wrong.job.name " + jobName
                        + " The JobInstance with id =" + jobInstanceId
                        + " has the wrong name " + jobInstance.getJobName()
                        + " not " + jobName;

                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(error).type(MediaType.TEXT_PLAIN).build();
            }
        } catch (NoSuchJobInstanceException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("There is no such job (" + jobName
                            + ") with JobInstanceID = " + jobInstanceId)
                    .type(MediaType.TEXT_PLAIN).build();
        }
        Collection<JobExecutionInfo> jobInstancesRepresentations = new ArrayList<JobExecutionInfo>();
        try {
            Collection<JobExecution> jobExecutions = jobService
                    .getJobExecutionsForJobInstance(jobName, jobInstanceId);
            for (JobExecution jobExecution : jobExecutions) {
                jobInstancesRepresentations.add(new JobExecutionInfo(
                        jobExecution, timeZone));
            }
        } catch (NoSuchJobException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("There is no such job (" + jobName + ")")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        return Response.ok(jobInstancesRepresentations).build();

    }
}
