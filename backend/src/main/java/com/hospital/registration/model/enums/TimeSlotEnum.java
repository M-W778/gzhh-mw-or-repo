package com.hospital.registration.model.enums;

import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排班时段枚举
 */
public enum TimeSlotEnum {

    MORNING("上午", "MORNING"),
    AFTERNOON("下午", "AFTERNOON");

    private final String text;
    private final String value;

    TimeSlotEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static TimeSlotEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (TimeSlotEnum anEnum : TimeSlotEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public static TimeSlotEnum fromValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (TimeSlotEnum anEnum : TimeSlotEnum.values()) {
            if (anEnum.value.equalsIgnoreCase(value) || anEnum.name().equalsIgnoreCase(value)) {
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
