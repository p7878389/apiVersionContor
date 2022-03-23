package com.github.lkqm.spring.api.version.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * Api-Version配置
 *
 * @author martin.peng
 */
@Data
@ConfigurationProperties(prefix = "api.version")
public class ApiVersionProperties implements Serializable {

    /**
     * 实现多版本的方式
     */
    private Type type = Type.URI;

    /**
     * URI地址前缀, 例如: /api
     */
    private String uriPrefix;

    /**
     * URI的位置
     */
    private UriLocation uriLocation = UriLocation.BEGIN;

    /**
     * 版本请求头名
     */
    private String header = "X-API-VERSION";

    /**
     * 版本请求参数名
     */
    private String paramVersion = "apiVersion";

    private String paramMethodName = "methodName";

    public enum Type {
        /**
         * URI路径
         */
        URI,
        /**
         * 请求头
         */
        HEADER,
        /**
         * 请求参数
         */
        PARAM
    }

    public enum UriLocation {
        BEGIN, END
    }
}
