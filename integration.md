---
title: "Integration"
addon: "Monitoring" 
menu:
    MonitoringAddon:
        weight: 20
---

# Persistence

## Create Spring Batch metadata tables:

In order to create DB tables for Spring Batch metadata, check the schemas (schema-10g.sql oracle, schema-mysql.sql, ...)
that are described in the **spring-batch-core** JAR inside **org.springframework.batch.core** pakage.

{{% callout info %}}
The tables *(BATCH _)* prefix can be changed but requires a definition in two places:

* `tablePrefix` property within batch `jobRepository` bean configuration.
* `table.prefix` property within props `[org.seedstack.seed.monitoring.batch.datasource]` section of the Web appplication.
{{% /callout %}}

## Data source configuration 

### In batch application 

```xml
<bean id="jobRepository"
class="org.springframework.batch.core.repository.support.JobRepositoryFactoryBean">
    <property name="dataSource" ref="dataSource" />
    <property name="transactionManager" ref="transactionManager" />
    <property name="databaseType" value="...."/>
    <property name="tablePrefix" value="....." />
</bean>
```

```xml
<bean id="dataSource"
class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
    <property name="driverClassName" value="....." />
    <property name="url" value="....." />
    <property name="username" value="...." />
    <property name="password" value="...." />
</bean>
```

```xml
<bean id="transactionManager"
class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <property name="dataSource" ref="dataSource" />
</bean>
```

### in web application

In .props file:

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

# Security

All batch monitoring REST resources are secured with permissions. These permissions have to be bound
to application [roles](#!/seed-doc/security#role) in order to allow access to the user interface.

## Read Permission:

In .props file (of your web application):

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