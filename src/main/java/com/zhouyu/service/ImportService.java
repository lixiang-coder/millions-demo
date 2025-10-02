package com.zhouyu.service;

import com.alibaba.excel.EasyExcel;
import com.zhouyu.domain.Salaries;
import com.zhouyu.listener.SalariesListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 作者：周瑜大都督
 */
@Service
public class ImportService {

    @Resource
    private SalariesListener salariesListener;

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    /**
     * 导入Excel文件并解析所有sheet页的数据
     *
     * @param file 要导入的Excel文件，封装在MultipartFile对象中
     */
    public void importExcel(MultipartFile file) throws IOException {
        /**
         * 1.打开你上传的 Excel 文件输入流（file.getInputStream()）。
         * 2.指定把每一行按 Salaries 模型去映射（列值填到 empNo/salary/fromDate/toDate）。
         * 3.指定用 salariesListener 来处理每一行（读到一行就回调 invoke，读完整个 Sheet 回调 doAfterAllAnalysed）。
         * 4.执行 doReadAll()，把 Excel 里的“所有 Sheet”都读一遍。
         */
        EasyExcel.read(file.getInputStream(), Salaries.class, salariesListener).doReadAll();
    }


    /**
     * 异步导入Excel文件，使用多线程分别处理前20个sheet页
     *
     * @param file 要导入的Excel文件，封装在MultipartFile对象中
     */
    public void importExcelAsync(MultipartFile file) {
        // 创建任务列表，用于存储20个并发处理任务
        List<Callable<Object>> tasks = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            int num = i;
            tasks.add(() -> {
                EasyExcel.read(file.getInputStream(), Salaries.class, salariesListener)
                        .sheet(num).doRead();
                return null;
            });
        }

        // 并发执行所有任务
        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
