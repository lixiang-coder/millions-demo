package com.zhouyu.domain;

/**
 * 单个 sheet 的配置：名称与行数
 */
public class SheetSpec {
    private String name;
    private int rowCount;

    public SheetSpec(String name, int rowCount) {
        this.name = name;
        this.rowCount = rowCount;
    }

    public String getName() {
        return name;
    }

    public int getRowCount() {
        return rowCount;
    }
}