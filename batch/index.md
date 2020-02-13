---
title: "Batch monitoring"
parent: "Monitoring"
weight: -1
zones:
    - Addons
menu:
    MonitoringAddon:
        weight: 20
---

You can use the monitoring add-on batch module to inspect execution results of Spring batch jobs, executions and 
steps.<!--more-->
 
# Dependency 
 
To add this module in you project add the following dependency to your Web application:

{{< dependency g="org.seedstack.addons.monitoring" a="monitoring-batch" >}}

{{% callout info %}}
This module will provide a REST API to query for batch execution information. You can use the UI provided by this add-on 
or write your own.
{{% /callout %}}

# Configuration 

The add-on must be configured to access the metadata tables written by your batch jobs. It will uses this information
to display batch execution information: 

{{% config p="monitoring.batch" %}}
```yaml
monitoring:
  batch:
    # The JDBC add-on datasource name to use to access batch metadata tables
    datasourceName: (String)
    # The prefix of Spring Batch metadata tables (defaults to BATCH_)
    tablePrefix: (String)
```
{{% /config %}}

# Security

All batch monitoring REST resources are secured with permissions. These permissions have to be bound to application 
[roles]({{< ref "docs/core/security.md" >}}) in order to allow access to the user interface. In the configuration
file of your web application:

```yaml
security:
  permissions:
    someApplicationRole: 'seed:monitoring:batch:read' 
```

Example:

```yaml
security:
  users:
    jane: 
      password: somePassword
      roles: ADMIN
  roles:
    monitoring: ADMIN
  permissions:
    monitoring: 'seed:monitoring:batch:read' 
```
