package com.hospital.registration.model.enums;

import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色枚举（用于展示/校验）
 */
public enum UserRoleEnum {
    PATIENT("患者", "PATIENT"),
    DOCTOR("医生", "DOCTOR"),
    ADMIN("管理员", "ADMIN"),
    BAN("禁用", "BAN");

    private final String text;
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public static UserRoleEnum fromValueOrText(String rawRole) {
        if (ObjectUtils.isEmpty(rawRole)) {
            return null;
        }
        String candidate = rawRole.trim();
        if (candidate.regionMatches(true, 0, "ROLE_", 0, 5)) {
            candidate = candidate.substring(5);
        }
        if ("USER".equalsIgnoreCase(candidate)) {
            candidate = PATIENT.getValue();
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equalsIgnoreCase(candidate)
                    || anEnum.name().equalsIgnoreCase(candidate)
                    || anEnum.text.equalsIgnoreCase(candidate)) {
                return anEnum;
            }
        }
        return null;
    }

    public static String normalizeRoleValue(String rawRole) {
        UserRoleEnum roleEnum = fromValueOrText(rawRole);
        return roleEnum == null ? null : roleEnum.getValue();
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
