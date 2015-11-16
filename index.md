---
title: "Overview"
addon: "Monitoring"
repo: "https://github.com/seedstack/monitoring-addon"
author: "SeedStack"
description: "Provides self-monitoring Web UI for applications."
min-version: "15.11+"
backend: true
frontend: "responsive"
menu:
    MonitoringAddon:
        weight: 10
---

The SeedStack batch monitoring add-on provides a Web user interface to monitor the execution of Spring Batch jobs. You
can use it to inspect jobs, executions and steps. Execution statistics are provided.

{{< dependency g="org.seedstack.addons.monitoring" a="monitoring-web" >}}

If you only want the REST API, use the following dependency instead:

    <dependency>
    	<groupId>org.seedstack.addons.monitoring</groupId>
    	<artifactId>monitoring-batch</artifactId>
    </dependency>
