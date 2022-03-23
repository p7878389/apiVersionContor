package com.github.lkqm.spring.api.version.handlermapping;

import com.github.lkqm.spring.api.version.config.ApiVersionProperties;
import com.github.lkqm.spring.api.version.constant.ApiVersionConstant;
import com.github.lkqm.spring.api.version.utils.ApiVersionConverterUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.servlet.http.HttpServletRequest;

/**
 * @author martin.peng
 */
@Getter
@Slf4j
@ToString
public class ApiVersionRequestCondition implements RequestCondition<ApiVersionRequestCondition> {

    private final ApiVersionItem apiVersion;
    private final ApiVersionProperties apiVersionProperties;
    private final boolean flag;

    public ApiVersionRequestCondition(@NonNull ApiVersionItem apiVersion, @NonNull ApiVersionProperties apiVersionProperties) {
        this.apiVersion = apiVersion;
        this.apiVersionProperties = apiVersionProperties;
        flag = false;
    }

    public ApiVersionRequestCondition(ApiVersionItem apiVersion, ApiVersionProperties apiVersionProperties, boolean flag) {
        this.apiVersion = apiVersion;
        this.apiVersionProperties = apiVersionProperties;
        this.flag = flag;
    }

    @Override
    public ApiVersionRequestCondition combine(ApiVersionRequestCondition other) {
        // 选择版本最大的接口
        if (other.flag) {
            return this;
        }
        return other;
    }

    @Override
    public int compareTo(ApiVersionRequestCondition other, HttpServletRequest request) {
        // 获取到多个符合条件的接口后，会按照这个排序，然后get(0)获取最大版本对应的接口.自定义条件会最后比较
        if (other.getApiVersion().getMethodName().equals(this.apiVersion.getMethodName())) {
            return -1;
        }
        int compare = other.getApiVersion().compareTo(this.apiVersion);
        if (compare == 0) {
            log.warn("RequestMappingInfo相同，请检查！version:{}", other.getApiVersion());
        }
        return compare;
    }

    @Override
    public ApiVersionRequestCondition getMatchingCondition(HttpServletRequest request) {

        String methodName = getParameter(request, apiVersionProperties.getParamMethodName(), "");

        // 获取所有小于等于版本的接口;如果前端不指定版本号，则默认请求1.0.0版本的接口
        String version = getParameter(request, apiVersionProperties.getParamVersion(), ApiVersionConstant.DEFAULT_VERSION);

        ApiVersionItem apiVersionItem = ApiVersionConverterUtils.convert(version, methodName);
        if (apiVersionItem.compareTo(ApiVersionItem.DEFAULT_API_VERSION) < 0) {
            throw new IllegalArgumentException(String.format("API版本[%s]错误，最低版本[%s]", version, ApiVersionConstant.DEFAULT_VERSION));
        }

        if (apiVersionCompareTo(apiVersionItem)) {
            return this;
        }
        return null;
    }

    private String getParameter(HttpServletRequest request, String key, String defaultValue) {
        String value = request.getParameter(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    private boolean apiVersionCompareTo(ApiVersionItem requestApiVersionItem) {
        return requestApiVersionItem.getMethodName().equals(apiVersion.getMethodName())
                && requestApiVersionItem.compareTo(this.apiVersion) >= 0;
    }
}