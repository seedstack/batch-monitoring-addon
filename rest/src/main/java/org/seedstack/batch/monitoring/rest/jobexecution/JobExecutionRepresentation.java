/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.batch.monitoring.rest.jobexecution;

import org.springframework.batch.admin.web.JobExecutionInfo;

import java.util.ArrayList;

/**
 * The Class JobExecutionRepresentation.
 *
 * @author aymen.abbes@ext.mpsa.com
 */
public class JobExecutionRepresentation {

    private int pageIndex;
    private int pageSize;

    private int totalItems;
    private ArrayList<JobExecutionInfo> results;

    public JobExecutionRepresentation(int pageIndex, int pageSize,
                                      int totalItems, ArrayList<JobExecutionInfo> results) {
        super();
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.results = results;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public ArrayList<JobExecutionInfo> getResults() {
        return results;
    }

}