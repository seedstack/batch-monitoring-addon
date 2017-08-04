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

<<<<<<< Updated upstream
You can use the monitoring add-on batch module to inspect execution results of Spring batch jobs, executions and 
steps.<!--more-->
 
# Dependencies 
 
To add this module in you project add the following dependency to your Web application:
||||||| merged common ancestors
You can use the monitoring add-on batch module to inspect jobs, executions and steps. Execution statistics are also
provided. To add this module in you project add the following dependency to your Web application:
=======
You can use the monitoring add-on batch module to inspect jobs, executions and steps. Execution statistics are also
provided.
>>>>>>> Stashed changes

# Dependency

<<<<<<< Updated upstream
{{% callout info %}}
This module will provide a REST API to query for batch execution information. You can use the UI provided by this add-on 
or write your own.
{{% /callout %}}
||||||| merged common ancestors
{{% callout info %}}
This module will provide a REST API to query for batch execution information. 
You can use the UI provided by this add-on or write your own.
{{% /callout %}}
=======
If you only need batch monitoring REST APIs, add the following dependency: 

{{< dependency g="org.seedstack.addons.monitoring" a="monitoring-batch" >}}
>>>>>>> Stashed changes

<<<<<<< Updated upstream
# Configuration 
||||||| merged common ancestors
# Integration
=======
If you also need the W20 Web UI provided by this add-on, add the following dependency instead:
>>>>>>> Stashed changes

<<<<<<< Updated upstream
The add-on must be configured to access the metadata tables written by your batch jobs. It will uses this information
to display batch execution information: 
||||||| merged common ancestors
This module must be configured
=======
{{< dependency g="org.seedstack.addons.monitoring" a="monitoring-web" >}}
>>>>>>> Stashed changes

<<<<<<< Updated upstream
{{% config p="monitoring.batch" %}}
```yaml
monitoring:
  batch:
    # The JDBC add-on datasource name to use to access batch metadata tables
    datasourceName: (String)
    # The prefix of Spring Batch metadata tables (defaults to BATCH_)
    tablePrefix: (String)
||||||| merged common ancestors
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
=======
# Creation of Spring Batch metadata tables

If you don't have already existing Spring Batch tables, you can create them with the SQL scripts that are located in
the **spring-batch-core** JAR inside **org.springframework.batch.core** package.

{{% callout info %}}
The table `BATCH_` prefix can be changed but this requires a change in two places:

* The `tablePrefix` property within batch `jobRepository` bean configuration,
* And the `table.prefix` property within props `[org.seedstack.seed.monitoring.batch.datasource]` section of the Web appplication.
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
>>>>>>> Stashed changes
```
{{% /config %}}

# Security

All batch monitoring REST resources are secured with permissions. These permissions have to be bound to application 
[roles]({{< ref "docs/seed/manual/security.md" >}}) in order to allow access to the user interface. In the configuration
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
