package com.hospital.registration.model.enums;

import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 号源状态枚举
 */
public enum ScheduleEnum {

    AVAILABLE("可预约", "AVAILABLE"),
    FULL("已约满", "FULL"),
    SUSPENDED("停诊中", "SUSPENDED");

    private final String text;
    private final String value;

    ScheduleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static ScheduleEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (ScheduleEnum anEnum : ScheduleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
