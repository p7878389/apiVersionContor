package com.github.lkqm.spring.api.version.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置api版本
 *
 * @author martin.peng
 */
@Configuration
@EnableConfigurationProperties(ApiVersionProperties.class)
public class ApiVersionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApiVersionWebMvcRegistrations apiVersionWebMvcRegistrations(ApiVersionProperties apiVersionProperties) {
        return new ApiVersionWebMvcRegistrations(apiVersionProperties);
    }
}
