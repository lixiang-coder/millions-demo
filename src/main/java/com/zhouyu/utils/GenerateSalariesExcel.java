package com.zhouyu.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.zhouyu.domain.Salaries;
import com.zhouyu.domain.SheetSpec;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 生成多 Sheet 的 Excel：全局连续 empNo，且每个 Sheet 写入表头
 */
public class GenerateSalariesExcel {

    public static void main(String[] args) throws ParseException {
        String output = "D:/code/millions-demo/docs/salaries.xlsx";

        List<SheetSpec> sheets = Arrays.asList(
                new SheetSpec("Sheet-1", 50000),
                new SheetSpec("Sheet-2", 50000),
                new SheetSpec("Sheet-3", 50000),
                new SheetSpec("Sheet-4", 50000),
                new SheetSpec("Sheet-5", 50000),
                new SheetSpec("Sheet-6", 50000),
                new SheetSpec("Sheet-7", 50000),
                new SheetSpec("Sheet-8", 50000),
                new SheetSpec("Sheet-9", 50000),
                new SheetSpec("Sheet-10", 50000),
                new SheetSpec("Sheet-11", 50000),
                new SheetSpec("Sheet-12", 50000),
                new SheetSpec("Sheet-13", 50000),
                new SheetSpec("Sheet-14", 50000),
                new SheetSpec("Sheet-15", 50000),
                new SheetSpec("Sheet-16", 50000),
                new SheetSpec("Sheet-17", 50000),
                new SheetSpec("Sheet-18", 50000),
                new SheetSpec("Sheet-19", 50000),
                new SheetSpec("Sheet-20", 6000)
        );

        int startEmpNo = 1; // 全局起始 empNo
        Date fromDate = parseDate("2023-01-01");
        Date toDate = parseDate("2023-12-31");

        writeMultiSheet(
                output,
                sheets,
                startEmpNo,
                fromDate,
                toDate,
                50000,    // salaryMin
                120000    // salaryMax
        );

        System.out.println("生成完成: " + output);
    }

    /**
     * 生成多 sheet 的 Excel，empNo 全局连续，每个 sheet 写表头
     */
    public static void writeMultiSheet(String outputPath,
                                       List<SheetSpec> sheetSpecs,
                                       int globalStartEmpNo,
                                       Date fromDate,
                                       Date toDate,
                                       int salaryMin,
                                       int salaryMax) {

        Objects.requireNonNull(outputPath, "outputPath 不能为空");
        Objects.requireNonNull(sheetSpecs, "sheetSpecs 不能为空");
        if (sheetSpecs.isEmpty()) throw new IllegalArgumentException("sheetSpecs 为空");
        if (globalStartEmpNo <= 0) throw new IllegalArgumentException("globalStartEmpNo 必须为正数");
        if (salaryMin <= 0 || salaryMax <= 0 || salaryMin >= salaryMax) {
            throw new IllegalArgumentException("salaryMin/salaryMax 配置非法");
        }

        ExcelWriter writer = null;
        try {
            writer = EasyExcel.write(outputPath, Salaries.class).build();

            int sheetIndex = 0;
            int currentEmpNo = globalStartEmpNo;

            for (SheetSpec spec : sheetSpecs) {
                if (spec == null) continue;
                int rows = Math.max(0, spec.getRowCount());
                String name = (spec.getName() == null || spec.getName().trim().isEmpty())
                        ? ("Sheet-" + (sheetIndex + 1)) : spec.getName().trim();

                List<Salaries> data = generateRows(currentEmpNo, rows, fromDate, toDate, salaryMin, salaryMax);
                if (!data.isEmpty()) {
                    WriteSheet sheet = EasyExcel.writerSheet(sheetIndex, name).build();
                    writer.write(data, sheet);
                    int endEmpNo = currentEmpNo + rows - 1;
                    System.out.println(String.format(Locale.ROOT,
                            "写入 %s 行数=%d，empNo范围=[%d..%d]",
                            name, rows, currentEmpNo, endEmpNo));
                    currentEmpNo += rows; // 全局连续推进
                } else {
                    // 空 sheet 也会包含表头
                    WriteSheet sheet = EasyExcel.writerSheet(sheetIndex, name).build();
                    writer.write(Collections.emptyList(), sheet);
                    System.out.println(String.format(Locale.ROOT,
                            "写入 %s 行数=0（仅表头）", name));
                }

                sheetIndex++;
            }
        } finally {
            if (writer != null) {
                writer.finish();
            }
        }
    }

    private static List<Salaries> generateRows(int startEmpNo,
                                               int count,
                                               Date fromDate,
                                               Date toDate,
                                               int salaryMin,
                                               int salaryMax) {
        if (count <= 0) return Collections.emptyList();

        List<Salaries> list = new ArrayList<>(count);
        Random random = new Random(20231001L); // 固定种子，便于复现
        for (int i = 0; i < count; i++) {
            Salaries s = new Salaries();
            s.setEmpNo(startEmpNo + i);
            s.setSalary(randBetween(random, salaryMin, salaryMax));
            s.setFromDate(fromDate);
            s.setToDate(toDate);
            list.add(s);
        }
        return list;
    }

    private static int randBetween(Random r, int minIncl, int maxIncl) {
        return minIncl + r.nextInt(maxIncl - minIncl + 1);
    }

    private static Date parseDate(String s) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(s);
    }
}