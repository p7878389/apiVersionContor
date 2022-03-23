package com.github.lkqm.spring.api.version.handlermapping;

import com.github.lkqm.spring.api.version.annotations.ApiVersion;
import com.github.lkqm.spring.api.version.config.ApiVersionProperties;
import com.github.lkqm.spring.api.version.utils.ApiVersionConverterUtils;
import com.github.lkqm.spring.api.version.utils.InnerUtils;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author martin.peng
 */
@Slf4j
@AllArgsConstructor
public class VersionedRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    /**
     * 多版本配置属性
     */
    private ApiVersionProperties apiVersionProperties;

    @Override
    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
        return createRequestCondition(handlerType);
    }

    @Override
    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        return createRequestCondition(method);
    }

    private RequestCondition<ApiVersionRequestCondition> createRequestCondition(AnnotatedElement target) {
        if (apiVersionProperties.getType() == ApiVersionProperties.Type.URI) {
            return null;
        }
        ApiVersion apiVersion = AnnotationUtils.findAnnotation(target, ApiVersion.class);
        return Objects.isNull(apiVersion) ? null : new ApiVersionRequestCondition(ApiVersionConverterUtils.convert(apiVersion), apiVersionProperties);
    }

    //--------------------- 动态注册URI -----------------------//
    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = this.createRequestMappingInfo(method);
        if (Objects.isNull(info)) {
            return null;
        }

        RequestMappingInfo typeInfo = this.createRequestMappingInfo(handlerType);
        if (typeInfo != null) {
            info = typeInfo.combine(info);
        }

        // 指定URL前缀
        if (!Objects.equals(apiVersionProperties.getType(), ApiVersionProperties.Type.URI)) {
            return info;
        }

        ApiVersion apiVersion = AnnotationUtils.getAnnotation(method, ApiVersion.class);
        if (apiVersion == null) {
            apiVersion = AnnotationUtils.getAnnotation(handlerType, ApiVersion.class);
        }
        if (Objects.isNull(apiVersion)) {
            return info;
        }

        String version = apiVersion.version().trim();
        InnerUtils.checkVersionNumber(version, method);

        String prefix = "/v" + version;
        if (apiVersionProperties.getUriLocation() == ApiVersionProperties.UriLocation.END) {
            info = info.combine(RequestMappingInfo.paths(new String[]{prefix}).build());
        } else {
            if (!StringUtils.isEmpty(apiVersionProperties.getUriPrefix())) {
                prefix = apiVersionProperties.getUriPrefix().trim() + prefix;
            }
            info = RequestMappingInfo.paths(new String[]{prefix}).build().combine(info);
        }
        return info;
    }

    private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        RequestCondition<?> condition = element instanceof Class ? this.getCustomTypeCondition((Class) element) : this.getCustomMethodCondition((Method) element);
        return requestMapping != null ? this.createRequestMappingInfo(requestMapping, condition) : null;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        Set<String> existConditionSet = new HashSet<>();
        Set<String> allConditionSet = new HashSet<>();

        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = getHandlerMethods();
        handlerMethodMap.entrySet().stream().filter(handlerMethodEntry -> {
            RequestMappingInfo requestMappingInfo = handlerMethodEntry.getKey();
            return requestMappingInfo.getCustomCondition() instanceof ApiVersionRequestCondition;
        }).forEach(handlerMethodEntry -> {
            RequestMappingInfo requestMappingInfo = handlerMethodEntry.getKey();
            String apiVersionItemKey = buildCustomConditionKey(requestMappingInfo);
            if (allConditionSet.contains(apiVersionItemKey)) {
                existConditionSet.add(apiVersionItemKey);
            } else {
                allConditionSet.add(apiVersionItemKey);
            }
        });

        if (!CollectionUtils.isEmpty(existConditionSet)) {
            throw new RuntimeException(String.format("There are multiple identical version information , [ %s ]", String.join(",", existConditionSet)));
        }
    }

    private String buildCustomConditionKey(@NonNull RequestMappingInfo requestMappingInfo) {
        ApiVersionRequestCondition apiVersionRequestCondition = (ApiVersionRequestCondition) requestMappingInfo.getCustomCondition();
        ApiVersionItem apiVersionItem = apiVersionRequestCondition.getApiVersion();
        Assert.notNull(apiVersionItem, "ApiVersion annotation not found");

        PatternsRequestCondition patternsRequestCondition = requestMappingInfo.getPatternsCondition();
        Assert.notNull(patternsRequestCondition, "patternsRequestCondition is null");

        StringBuilder conditionKey = new StringBuilder();

        String pattern = String.join(",", patternsRequestCondition.getPatterns());
        conditionKey.append("pattern").append(" = {").append(pattern).append("}").append(" , ");

        conditionKey.append("version = {").append(apiVersionItem.getLevel1())
                .append(".").append(apiVersionItem.getLevel2())
                .append(".").append(apiVersionItem.getLevel3()).append("}").append(" , ");

        conditionKey.append("methodName = {").append(apiVersionItem.getMethodName()).append("}");

        return conditionKey.toString();
    }
}