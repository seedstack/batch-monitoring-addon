/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 26 juin 2014
 */
package org.seedstack.monitoring.batch.internal.rest.job;

import java.util.Collection;
import java.util.List;


/**
 * The Class JobsTreeRepresentation.
 *
 * @author aymen.abbes@ext.mpsa.com
 */
public class JobsTreeRepresentation {

    /**
     * The name.
     */
    private String name;

    /**
     * The link.
     */
    private String link;

    /**
     * The status.
     */
    private String status;

    /**
     * The size.
     */
    private Integer size;

    /**
     * The children.
     */
    private List<JobsTreeRepresentation> children;

    /**
     * The job name list.
     */
    private Collection<String> jobNameList;

    /**
     * Instantiates a new jobs tree representation.
     *
     * @param name     the name
     * @param link     the link
     * @param status   the status
     * @param size     the size
     * @param children the children
     */
    public JobsTreeRepresentation(String name, String link, String status,
                                  Integer size, List<JobsTreeRepresentation> children) {
        this.name = name;
        this.link = link;
        this.status = status;
        this.size = size;
        this.setChildren(children);
    }

    /**
     * Instantiates a new jobs tree representation.
     */
    public JobsTreeRepresentation() {
        super();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the link.
     *
     * @return the link
     */
    public String getLink() {
        return link;
    }

    /**
     * Sets the link.
     *
     * @param link the new link
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the new status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public Integer getSize() {
        return size;
    }

    /**
     * Sets the size.
     *
     * @param size the new size
     */
    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * Gets the job name list.
     *
     * @return the job name list
     */
    public Collection<String> getJobNameList() {
        return jobNameList;
    }

    /**
     * Sets the job name list.
     *
     * @param jobNameList the new job name list
     */
    public void setJobNameList(Collection<String> jobNameList) {
        this.jobNameList = jobNameList;
    }

    /**
     * Gets the children.
     *
     * @return the children
     */
    public List<JobsTreeRepresentation> getChildren() {
        return children;
    }

    /**
     * Sets the children.
     *
     * @param children the new children
     */
    public void setChildren(List<JobsTreeRepresentation> children) {
        this.children = children;
    }
}
