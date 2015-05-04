/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.batch.monitoring.rest.jobexecution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;

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

import org.slf4j.Logger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;

import org.seedstack.batch.monitoring.service.JobService;
import org.seedstack.seed.core.api.Logging;
import org.seedstack.seed.security.api.annotations.RequiresPermissions;

/**
 * Resource for job executions.
 * 
 * @author aymen.abbes@ext.mpsa.com
 */
@Path("/jobs/{jobName}/job-executions")
public class JobExecutionResource {

	/** The logger. */
	@Logging
	private static Logger logger;

	/** The job service. */
	@Inject
	@Named("jobService")
	private JobService jobService;

	/**
	 * job-Executions By JobName
	 * 
	 * @param jobName
	 * @param startJob
	 * @param pageSize
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@RequiresPermissions("seed:monitoring:batch:read")
	public Response jobExecutionsByJobName(
			@PathParam("jobName") String jobName,
			@DefaultValue("1") @QueryParam("pageIndex") int pageIndex,
			@DefaultValue("20") @QueryParam("pageSize") int pageSize) {

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
			logger.error(" Error has occured {} ", e);
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("There is no such job (" + jobName + ") ")
					.type(MediaType.TEXT_PLAIN).build();
		}
		JobExecutionRepresentation jobExecutionRepresentation = new JobExecutionRepresentation(
				pageIndex, pageSize, totalItems, jobExecutionInfos);
		return Response.ok(jobExecutionRepresentation).build();
	}

	/**
	 * JobExecution by jobExecutionId.
	 * 
	 * @param jobExecutionId
	 *            the job execution id
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

			logger.error(" Error has occured {} ", e);
			return Response
					.status(Response.Status.BAD_REQUEST)
					.entity("There is no such job execution (" + jobExecutionId
							+ ")").type(MediaType.TEXT_PLAIN).build();
		}

		ArrayList<JobExecutionInfo> executionInfos = new ArrayList<JobExecutionInfo>();
		executionInfos.add(new JobExecutionInfo(jobExecution,
				new GregorianCalendar().getTimeZone()));

		JobExecutionRepresentation jobExecutionRepresentation = new JobExecutionRepresentation(
				0, 0, totalItems, executionInfos);
		return Response.ok(jobExecutionRepresentation).build();
	}
}
