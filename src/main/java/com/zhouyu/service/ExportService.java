package com.zhouyu.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhouyu.domain.Salaries;
import com.zhouyu.mapper.SalariesMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 作者：周瑜大都督
 */
@Service
public class ExportService {
    public static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Resource
    private SalariesMapper salariesMapper;

    public void exportExcel1(HttpServletResponse response) throws IOException {

        setExportHeader(response);

        List<Salaries> salaries = salariesMapper.selectList(null);

        EasyExcel.write(response.getOutputStream(), Salaries.class).sheet().doWrite(salaries);
    }



    public void exportExcel2(HttpServletResponse response) throws IOException {

        setExportHeader(response);

        List<Salaries> salaries = salariesMapper.selectList(null);

        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), Salaries.class).build()) {
            WriteSheet writeSheet1 = EasyExcel.writerSheet(1, "模板1").build();
            WriteSheet writeSheet2 = EasyExcel.writerSheet(2, "模板2").build();
            WriteSheet writeSheet3 = EasyExcel.writerSheet(3, "模板3").build();

            List<Salaries> data1 = salaries.subList(0, salaries.size() / 3);
            List<Salaries> data2 = salaries.subList(salaries.size() / 3, salaries.size() * 2 / 3);
            List<Salaries> data3 = salaries.subList(salaries.size() * 2 / 3, salaries.size());


            excelWriter.write(data1, writeSheet1);
            excelWriter.write(data2, writeSheet2);
            excelWriter.write(data3, writeSheet3);
        }
    }



    public void exportExcel3(HttpServletResponse response) throws IOException {

        setExportHeader(response);

        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), Salaries.class).build()) {

            Long count = salariesMapper.selectCount(null);
            Integer pages = 10;
            Long size = count / pages;

            for (int i = 0; i < pages; i++) {
                WriteSheet writeSheet = EasyExcel.writerSheet(i, "模板" + i).build();

                Page<Salaries> page = new Page<>();
                page.setCurrent(i + 1);
                page.setSize(size);
                Page<Salaries> selectPage = salariesMapper.selectPage(page, null);

                excelWriter.write(selectPage.getRecords(), writeSheet);
            }
        }
    }


    public void exportExcel4(HttpServletResponse response) throws IOException, InterruptedException {

        setExportHeader(response);

        Long count = salariesMapper.selectCount(null);

        Integer pages = 20;
        Long size = count / pages;

        ExecutorService executorService = Executors.newFixedThreadPool(pages);
        CountDownLatch countDownLatch = new CountDownLatch(pages);

        Map<Integer, Page<Salaries>> pageMap = new HashMap<>();
        for (int i = 0; i < pages; i++) {
            int finalI = i;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Page<Salaries> page = new Page<>();
                    page.setCurrent(finalI + 1);
                    page.setSize(size);
                    Page<Salaries> selectPage = salariesMapper.selectPage(page, null);

                    pageMap.put(finalI, selectPage);
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), Salaries.class).build()) {
            for (Map.Entry<Integer, Page<Salaries>> entry : pageMap.entrySet()) {
                Integer num = entry.getKey();
                Page<Salaries> salariesPage = entry.getValue();
                WriteSheet writeSheet = EasyExcel.writerSheet(num, "模板" + num).build();
                excelWriter.write(salariesPage.getRecords(), writeSheet);
            }
        }

        // https://github.com/alibaba/easyexcel/issues/1040
    }

    private static void setExportHeader(HttpServletResponse response) {
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + "zhouyu.xlsx");
    }
}
