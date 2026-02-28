package com.yowon.psc.case003.repro.domain;

public class ExplainRow {

    private final Long id;
    private final String selectType;
    private final String tableName;
    private final String accessType;
    private final String keyName;
    private final Long estimatedRows;
    private final String extra;

    public ExplainRow(
        Long id,
        String selectType,
        String tableName,
        String accessType,
        String keyName,
        Long estimatedRows,
        String extra
    ) {
        this.id = id;
        this.selectType = selectType;
        this.tableName = tableName;
        this.accessType = accessType;
        this.keyName = keyName;
        this.estimatedRows = estimatedRows;
        this.extra = extra;
    }

    public Long getId() {
        return id;
    }

    public String getSelectType() {
        return selectType;
    }

    public String getTableName() {
        return tableName;
    }

    public String getAccessType() {
        return accessType;
    }

    public String getKeyName() {
        return keyName;
    }

    public Long getEstimatedRows() {
        return estimatedRows;
    }

    public String getExtra() {
        return extra;
    }
}
