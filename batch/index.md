---
title: "Batch monitoring"
name: "Monitoring"
zones:
    - Addons
tags:
    - "monitoring"
    - "batch"
    - "job"
    - "spring"
menu:
    MonitoringAddon:
        weight: 20
---

You can use the monitoring add-on batch module to inspect jobs, executions and steps. Execution statistics are also
provided. To add this module in you project add the following dependency to your Web application:

{{< dependency g="org.seedstack.addons.monitoring" a="monitoring-batch" >}}

{{% callout info %}}
This module will provide a REST API to query for batch execution information. 
You can use the UI provided by this add-on or write your own.
{{% /callout %}}

# Integration

This module must be configured

## Create Spring Batch metadata tables

If you don't have already existing Spring Batch tables, you can create them with the SQL scripts that are located in
the **spring-batch-core** JAR inside **org.springframework.batch.core** package.

{{% callout info %}}
The tables *(BATCH _)* prefix can be changed but this requires a change in two places:

* `tablePrefix` property within batch `jobRepository` bean configuration.
* `table.prefix` property within props `[org.seedstack.seed.monitoring.batch.datasource]` section of the Web appplication.
{{% /callout %}}

## Configure the datasource

### In a batch module

Each batch module must be configured to write its batch execution information to a specified datasource:

```xml
<bean id="jobRepository"
class="org.springframework.batch.core.repository.support.JobRepositoryFactoryBean">
    <property name="dataSource" ref="dataSource" />
    <property name="transactionManager" ref="transactionManager" />
    <property name="databaseType" value="...."/>
    <property name="tablePrefix" value="....." />
</bean>

<bean id="dataSource"
class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
    <property name="driverClassName" value="....." />
    <property name="url" value="....." />
    <property name="username" value="...." />
    <property name="password" value="...." />
</bean>

<bean id="transactionManager"
class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <property name="dataSource" ref="dataSource" />
</bean>
```

### In a Web module

The Web module should be configured to access the same datasource as your batch jobs. It will use the tables to fetch
and display batch execution information.

```ini
[org.seedstack.batch.monitoring.datasource]
driver=
url=
user=
password=
table.prefix=
pool.size=
```

Example:

```ini
[org.seedstack.batch.monitoring.datasource]
driver= oracle.jdbc.OracleDriver
url=jdbc:oracle:thin:@TEST:1521:test
user=test
password=test
table.prefix=BATCH_
pool.size=6
```

## Security

All batch monitoring REST resources are secured with permissions. These permissions have to be bound
to application [roles]({{< ref "docs/seed/manual/security.md" >}}) in order to allow access to the user interface. In the configuration
file of your web application:

```ini
[org.seedstack.seed.security.permissions]
monitoring = seed:monitoring:batch:read
```

ConfigurationRealm example:

```ini
[org.seedstack.seed.security.users]
jane = password, SEED.MONITORING
admin = password, SEED.MONITORING

[org.seedstack.seed.security.roles]
monitoring = SEED.MONITORING

[org.seedstack.seed.security.permissions]
monitoring = seed:monitoring:batch:read
```

# Usage

By default, all views are loaded at once with current available data. The "Update" button allows
to force a refresh data every 5 seconds when clicked (red).

## Jobs detail

The "Jobs detail" view allows a user to inspect jobs that are known to the system (ie. monitoring
data in same DB tables set):

![Jobs](img/jobsDetails.png)

## Job executions detail

The "Job executions detail" view shows all jobs executions ordered by date (descending order)
and a brief summary of their status (*STARTED, COMPLETED, FAILED*, etc.).

![Executions](img/jobExecutions.png)

## Steps detail

The "Steps detail" view offers two kinds of feedback:

* global feedback: A list of all steps and their average time consumption (ms) across all past
job executions as a bar chart. this provides a statistical feel of global performance characteristics.

> For example, a developer running a job in an integration test environment might use the statistics
here to compare different configurations of a job in order to optimize those (eg. commit interval in
an item processing step).

* steps feedback: Upon selection of a step in the list (first step selected by default), the bottom
part of the screen gives detail on this step with a progression bar and figures about read/write/commit/rollback.
For more details, this section also provides **View full detail** and **View history** buttons.
Corresponding views are described below.

![Steps](img/stepsDetails.png)

## Step full detail

 The "Step detail" view has the detailed meta-data for the step (status, read count, write count,
 commit count, skip count, etc.) as well as an extract of the stacktrace from any exception that
 caused a failure of the step (**statusExitDescription** value).

![Step details](img/stepDetails.png)

## Step history

The "Step history" view shows the history of the execution of this step across all job executions
(eg. max, min and average of commit/rollback/read/write counts...).

![Step history](img/history.png)
