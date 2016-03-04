/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.monitoring.batch.internal.rest.job;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import org.seedstack.monitoring.batch.internal.rest.jobexecution.JobExecutionRepresentation;
import org.seedstack.seed.security.RequiresPermissions;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.admin.web.JobExecutionInfo;
import org.springframework.batch.admin.web.JobInfo;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
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
import java.util.List;
import java.util.regex.Pattern;

/**
 * Resource for listing jobs and job executions by jobName.
 *
 * @author aymen.abbes@ext.mpsa.com
 */
@Path("/seed-monitoring/jobs")
public class JobResource {

    /**
     * The Constant TREE_SIZE_STEP.
     */
    private static final int TREE_SIZE_STEP = 1000;

    /**
     * The Constant TREE_SIZE_JOBEXECUTION.
     */
    private static final int TREE_SIZE_JOBEXECUTION = 2000;

    /**
     * The job service.
     */
    @Inject
    @Named("jobService")
    private JobService jobService;

    /**
     * The Constant TREE_URL_BATCH_JOBS_LIST.
     */
    private static final String TREE_URL_BATCH_JOBS_LIST = "#!/batch/jobs-list";

    /**
     * The Constant TREE_MAINNODE.
     */
    private static final String TREE_MAINNODE = "MAINNODE";

    /**
     * Retrieves the list of jobs.
     *
     * @param pageIndex the page index
     * @param pageSize  the page size
     * @return the response
     */

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermissions("seed:monitoring:batch:read")
    public Response jobs(
            @DefaultValue("1") @QueryParam("pageIndex") int pageIndex,
            @DefaultValue("20") @QueryParam("pageSize") int pageSize,
            @QueryParam("searchedJob") String searchedJob) {

        Collection<String> names;
        int totalItems = jobService.countJobs();

        if (searchedJob != null) {
            names = Collections2.filter(jobService.listJobs(0, totalItems), Predicates.contains(Pattern.compile(searchedJob, Pattern.CASE_INSENSITIVE)));
            totalItems = names.size();
        } else {
            int startJob = (pageIndex - 1) * pageSize;
            names = jobService.listJobs(startJob, pageSize);
        }

        ArrayList<JobInfo> jobs = new ArrayList<JobInfo>();

        for (String name : names) {
            int count;
            try {
                count = jobService.countJobExecutionsForJob(name);
            } catch (NoSuchJobException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("There is no such jobs ")
                        .type(MediaType.TEXT_PLAIN).build();
            }
            boolean launchable = jobService.isLaunchable(name);
            boolean incrementable = jobService.isIncrementable(name);
            JobInfo jobsInfo = new JobInfo(name, count, null, launchable,
                    incrementable);
            jobs.add(jobsInfo);

        }
        JobRepresentation jobRepresentation = new JobRepresentation(pageIndex,
                pageSize, totalItems, jobs);
        return Response.ok(jobRepresentation).build();
    }

    /**
     * Retrieves the list of all job executions.
     *
     * @param pageIndex the page index
     * @param pageSize  the page size
     * @return JobExecutionRepresentation
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/executions")
    @RequiresPermissions("seed:monitoring:batch:read")
    public Response jobExecutions(
            @DefaultValue("1") @QueryParam("pageIndex") int pageIndex,
            @DefaultValue("20") @QueryParam("pageSize") int pageSize) {

        int startJobExecution = (pageIndex - 1) * pageSize;
        ArrayList<JobExecutionInfo> jobExecutionInfos = new ArrayList<JobExecutionInfo>();
        int totalItems;

        totalItems = jobService.countJobExecutions();

        for (JobExecution jobExecution : jobService.listJobExecutions(
                startJobExecution, pageSize)) {
            JobExecutionInfo jobExecutionInfo = new JobExecutionInfo(
                    jobExecution, new GregorianCalendar().getTimeZone());
            jobExecutionInfos.add(jobExecutionInfo);
        }
        JobExecutionRepresentation jobExecutionRepresentation = new JobExecutionRepresentation(
                pageIndex, pageSize, totalItems, jobExecutionInfos);
        return Response.ok(jobExecutionRepresentation).build();
    }

    /**
     * Retrieves the jobs tree.
     *
     * @param jobName the job name
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/jobs-tree/{jobName}")
    @RequiresPermissions("seed:monitoring:batch:read")
    public Response jobsTree(@PathParam("jobName") String jobName) {

        int countJobInstances;
        try {
            countJobInstances = jobService.countJobExecutionsForJob(jobName);
        } catch (NoSuchJobException e1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("There is no such jobs ")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        if (countJobInstances == 0) {
            String error = "wrong job name " + jobName;
            return Response.status(Response.Status.BAD_REQUEST).entity(error)
                    .type(MediaType.TEXT_PLAIN).build();
        }

        JobsTreeRepresentation jobsTreeRepresentation = new JobsTreeRepresentation();

        int countJobs = jobService.countJobs();

        Collection<String> listJobs = jobService.listJobs(0, countJobs);
        jobsTreeRepresentation.setJobNameList(listJobs);
        jobsTreeRepresentation.setName(jobName);
        jobsTreeRepresentation.setStatus(TREE_MAINNODE);
        jobsTreeRepresentation.setLink(TREE_URL_BATCH_JOBS_LIST);

        Collection<JobExecution> listJobExecutionsForJob;
        try {
            listJobExecutionsForJob = jobService.listJobExecutionsForJob(
                    jobName, 0, countJobInstances);
        } catch (NoSuchJobException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("There is no such job execution by jobname  ("
                            + jobName + ")").type(MediaType.TEXT_PLAIN).build();
        }

		/* list of job execution by job */
        List<JobsTreeRepresentation> childrenJobExecution = new ArrayList<JobsTreeRepresentation>();

        for (JobExecution jobExecution : listJobExecutionsForJob) {

            JobsTreeRepresentation jobExecTreeRepresentation = new JobsTreeRepresentation();

            jobExecTreeRepresentation.setName("[id= "
                    + jobExecution.getId()
                    + "];"
                    + jobExecution.getJobParameters().getParameters()
                    .toString());
            jobExecTreeRepresentation.setLink(TREE_URL_BATCH_JOBS_LIST + "/"
                    + jobName);
            jobExecTreeRepresentation.setSize(TREE_SIZE_JOBEXECUTION);
            jobExecTreeRepresentation.setStatus(jobExecution.getExitStatus()
                    .getExitCode());

            Collection<StepExecution> stepExecutions;
            try {
                stepExecutions = jobService.getStepExecutions(jobExecution
                        .getId());
            } catch (NoSuchJobExecutionException e) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("There is no such job execution ("
                                + jobExecution.getId() + ")")
                        .type(MediaType.TEXT_PLAIN).build();
            }

			/* list of step by job execution */
            List<JobsTreeRepresentation> childrenStep = new ArrayList<JobsTreeRepresentation>();
            for (StepExecution stepExecution : stepExecutions) {

                JobsTreeRepresentation stepTreeRepresentation = new JobsTreeRepresentation();
                stepTreeRepresentation.setName(stepExecution.getStepName());
                stepTreeRepresentation.setLink(TREE_URL_BATCH_JOBS_LIST + "/"
                        + jobName + "/" + jobExecution.getId());
                stepTreeRepresentation.setSize(TREE_SIZE_STEP);
                stepTreeRepresentation.setStatus(stepExecution.getExitStatus()
                        .getExitCode());
                childrenStep.add(stepTreeRepresentation);
            }
            jobExecTreeRepresentation.setChildren(childrenStep);
            childrenJobExecution.add(jobExecTreeRepresentation);
        }
        jobsTreeRepresentation.setChildren(childrenJobExecution);

        return Response.ok(jobsTreeRepresentation).build();
    }
}
