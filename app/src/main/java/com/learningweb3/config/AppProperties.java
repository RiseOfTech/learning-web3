package com.learningweb3.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String baseUrl;
    private String apiPrefix;
    private String errorsPath;
    private String errorsUrl;
}
