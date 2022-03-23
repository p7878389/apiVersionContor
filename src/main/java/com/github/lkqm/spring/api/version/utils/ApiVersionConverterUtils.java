package com.github.lkqm.spring.api.version.utils;

import com.github.lkqm.spring.api.version.annotations.ApiVersion;
import com.github.lkqm.spring.api.version.handlermapping.ApiVersionItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author martin.peng
 */
public class ApiVersionConverterUtils {

    public static ApiVersionItem convert(String api) {
        ApiVersionItem apiVersionItem = new ApiVersionItem();
        if (StringUtils.isBlank(api)) {
            return apiVersionItem;
        }

        String[] cells = StringUtils.split(api, ".");
        apiVersionItem.setLevel1(Integer.parseInt(cells[0]));

        if (cells.length >= ApiVersionLevel.FIRST.getLevel()) {
            apiVersionItem.setLevel2(Integer.parseInt(cells[1]));
        } else {
            apiVersionItem.setLevel2(0);
            apiVersionItem.setLevel3(0);
            return apiVersionItem;
        }

        if (cells.length >= ApiVersionLevel.SECOND.getLevel()) {
            apiVersionItem.setLevel3(Integer.parseInt(cells[2]));
        } else {
            apiVersionItem.setLevel3(0);
        }
        return apiVersionItem;
    }


    /**
     * @param apiVersion
     * @return
     */
    public static ApiVersionItem convert(@NonNull ApiVersion apiVersion) {
        String methodName = "";
        String apiVersionValue = "";
        if (Objects.nonNull(apiVersion)) {
            methodName = apiVersion.methodName();
            apiVersionValue = apiVersion.version();
        }
        ApiVersionItem apiVersionItem = convert(apiVersionValue);
        apiVersionItem.setMethodName(methodName);
        return apiVersionItem;
    }


    public static ApiVersionItem convert(String api, String methodName) {
        ApiVersionItem apiVersionItem = convert(api);
        apiVersionItem.setMethodName(methodName);
        return apiVersionItem;
    }

    @Getter
    @AllArgsConstructor
    enum ApiVersionLevel {
        FIRST(1), SECOND(2);
        private final int level;
    }
}
