/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.monitoring.batch.internal.rest.job;

import org.springframework.batch.admin.web.JobInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class JobRepresentation.
 *
 * @author aymen.abbes@ext.mpsa.com
 */
public class JobRepresentation {
    private int pageIndex;
    private int pageSize;
    private int totalItems;
    private List<JobInfo> results;

    public JobRepresentation(int pageIndex, int pageSize, int totalItems,
                             ArrayList<JobInfo> results) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.results = results;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public List<JobInfo> getResults() {
        return results;
    }

}