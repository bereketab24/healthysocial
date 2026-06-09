package com.university.healthysocial.analytics;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the BigQuery client only when {@code bigquery.enabled=true}.
 * Credentials are resolved via Application Default Credentials — on GCP this comes
 * from the VM's attached service account; locally it falls back to
 * {@code GOOGLE_APPLICATION_CREDENTIALS} if set.
 */
@Configuration
@EnableConfigurationProperties(BigQueryProperties.class)
public class BigQueryConfig {

    @Bean
    @ConditionalOnProperty(prefix = "bigquery", name = "enabled", havingValue = "true")
    public BigQuery bigQuery(BigQueryProperties props) {
        BigQueryOptions.Builder builder = BigQueryOptions.newBuilder();
        if (props.projectId() != null && !props.projectId().isBlank()) {
            builder.setProjectId(props.projectId());
        }
        if (props.location() != null && !props.location().isBlank()) {
            builder.setLocation(props.location());
        }
        return builder.build().getService();
    }
}
