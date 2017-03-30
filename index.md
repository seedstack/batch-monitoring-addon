---
title: "Monitoring"
repo: "https://github.com/seedstack/monitoring-addon"
author: Aymen ABBES
description: "Provides self-monitoring Web UI for applications."
zones:
    - Addons
menu:
    MonitoringAddon:
        weight: 10
---

The SeedStack monitoring add-on provides modules exposing API which report the monitoring status of your application:

* [Batch monitoring module]({{< ref "batch.md" >}}) which reports upon the status of Spring Batch jobs.
* MQTT monitoring module which reports upon the status of MQTT connections.

# Monitoring UI

You can write your custom UI or benefit from the built-in W20 UI provided by this add-on. To do so, add the following dependency to
your Web application:

{{< dependency g="org.seedstack.addons.monitoring" a="monitoring-web" >}}

{{% callout info %}}
This dependency will not provide the monitoring itself, only the UI. You must also add one or more monitoring modules
provided by this add-on.
{{% /callout %}}
