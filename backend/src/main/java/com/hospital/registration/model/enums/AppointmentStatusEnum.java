package com.hospital.registration.model.enums;

import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Appointment status enum.
 */
public enum AppointmentStatusEnum {

    PENDING("待就诊", "PENDING"),
    COMPLETED("已完成", "COMPLETED"),
    CANCELLED("已取消", "CANCELLED"),
    EXCEED("已超时", "EXCEED");

    private final String text;
    private final String value;

    AppointmentStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static com.hospital.registration.model.enums.AppointmentStatusEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (com.hospital.registration.model.enums.AppointmentStatusEnum anEnum : com.hospital.registration.model.enums.AppointmentStatusEnum.values()) {
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
