package com.github.lkqm.spring.api.version.handlermapping;

import com.github.lkqm.spring.api.version.constant.ApiVersionConstant;
import com.github.lkqm.spring.api.version.utils.ApiVersionConverterUtils;
import lombok.Data;

import java.io.Serializable;

/**
 * @author martin.peng
 */
@Data
public class ApiVersionItem implements Serializable, Comparable<ApiVersionItem> {

    /**
     * 定义大版本号
     */
    private int level1 = 1;

    private int level2 = 0;

    private int level3 = 0;

    private String methodName = "";

    public static final ApiVersionItem DEFAULT_API_VERSION = ApiVersionConverterUtils.convert(ApiVersionConstant.DEFAULT_VERSION);

    @Override
    public int compareTo(ApiVersionItem right) {
        if (this.getLevel1() > right.getLevel1()) {
            return 1;
        } else if (this.getLevel1() < right.getLevel1()) {
            return -1;
        }

        if (this.getLevel2() > right.getLevel2()) {
            return 1;
        } else if (this.getLevel2() < right.getLevel2()) {
            return -1;
        }

        if (this.getLevel3() > right.getLevel3()) {
            return 1;
        } else if (this.getLevel3() < right.getLevel3()) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "ApiVersionItem{" +
                "version=" + level1 +
                "." + level2 +
                "." + level3 +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}
