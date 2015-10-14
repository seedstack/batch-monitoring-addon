---
title: "Overview"
addon: "Batch monitoring"
repo: "https://github.com/seedstack/batch-monitoring-addon"
author: "SeedStack"
description: "The SeedStack batch monitoring add-on provides a Web user interface to monitor the execution of Spring Batch jobs."
min-version: "15.7+"
menu:
    BatchMonitoringAddon:
        weight: 10
---

The SeedStack batch monitoring add-on provides a Web user interface to monitor the execution of Spring Batch jobs. You
can use it to inspect jobs, executions and steps. Execution statistics are provided.

To add the add-on to your application, add the following dependency to your Web module pom:

    <dependency>
    	<groupId>org.seedstack.addons</groupId>
    	<artifactId>batch-monitoring-web</artifactId>
    </dependency>

If you only want the REST API, use the following dependency instead:

    <dependency>
    	<groupId>org.seedstack.addons</groupId>
    	<artifactId>batch-monitoring-rest</artifactId>
    </dependency>
