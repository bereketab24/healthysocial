package com.university.healthysocial.analytics;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bigquery")
public record BigQueryProperties(
        boolean enabled,
        String projectId,
        String dataset,
        String location
) {}
