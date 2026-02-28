package com.yowon.psc.case003.repro.domain;

public class ExplainJsonPair {

    private final String withoutHint;
    private final String withHint;

    public ExplainJsonPair(String withoutHint, String withHint) {
        this.withoutHint = withoutHint;
        this.withHint = withHint;
    }

    public String getWithoutHint() {
        return withoutHint;
    }

    public String getWithHint() {
        return withHint;
    }
}
