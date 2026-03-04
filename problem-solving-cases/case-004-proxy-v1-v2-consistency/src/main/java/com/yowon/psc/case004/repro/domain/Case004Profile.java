package com.yowon.psc.case004.repro.domain;

public enum Case004Profile {
    V1_P3("v1-p3"),
    V1_P4("v1-p4"),
    V2_TARGET("v2-target");

    private final String code;

    Case004Profile(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Case004Profile from(String value) {
        if (value == null) {
            return V2_TARGET;
        }
        String normalized = value.trim().toLowerCase();
        for (Case004Profile profile : values()) {
            if (profile.code.equals(normalized)) {
                return profile;
            }
        }
        throw new IllegalArgumentException("Unsupported profile: " + value);
    }
}
