package com.zhouyu.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.cache.Ehcache;
import com.alibaba.excel.cache.MapCache;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhouyu.domain.Salaries;
import com.zhouyu.listener.SalariesListener;
import com.zhouyu.mapper.SalariesMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 作者：周瑜大都督
 */
@Service
public class ImportService {

    @Resource
    private SalariesListener salariesListener;

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    public void importExcel(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), Salaries.class, salariesListener).doReadAll();
    }


    public void importExcelAsync(MultipartFile file) {
        // 开20个线程分别处理20个sheet

        List<Callable<Object>> tasks = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            int num = i;
            tasks.add(() -> {
                EasyExcel.read(file.getInputStream(), Salaries.class, salariesListener)
                        .sheet(num).doRead();
                return null;
            });
        }

        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


}
