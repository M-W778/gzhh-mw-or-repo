package com.hospital.registration.model.enums;

import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 医生职称枚举
 */
    public enum DoctorTitleEnum {
        CHIEF("主任医师", "CHIEF"),
        ASSOCIATE_CHIEF("副主任医师", "ASSOCIATE_CHIEF"),
        ATTENDING("主治医师", "ATTENDING"),
        RESIDENT("住院医师", "RESIDENT");

        private final String text;
        private final String value;

    DoctorTitleEnum(String text, String value) {
            this.text = text;
            this.value = value;
        }

        public static List<String> getValues() {
            return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
        }

        public static com.hospital.registration.model.enums.DoctorTitleEnum getEnumByValue(String value) {
            if (ObjectUtils.isEmpty(value)) {
                return null;
            }
            for (com.hospital.registration.model.enums.DoctorTitleEnum anEnum : com.hospital.registration.model.enums.DoctorTitleEnum.values()) {
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
