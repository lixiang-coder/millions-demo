package com.zhouyu.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhouyu.domain.Salaries;
import com.zhouyu.mapper.SalariesMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 作者：周瑜大都督
 */

@Component
public class SalariesListener extends ServiceImpl<SalariesMapper, Salaries> implements ReadListener<Salaries>, IService<Salaries> {

    private static final Log logger = LogFactory.getLog(SalariesListener.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    private ThreadLocal<ArrayList<Salaries>> salariesList = ThreadLocal.withInitial(ArrayList::new);
    private static AtomicInteger count = new AtomicInteger(1);
    private static final int batchSize = 10000;

    @Resource
    private SalariesListener salariesListener;


    /**
     * 重写父类或接口中的invoke方法，用于处理薪资数据并实现批量保存逻辑。
     * 该方法在事务中执行，发生异常时自动回滚。将单条数据暂存至线程局部变量列表，
     * 当累积数据量达到预设批量阈值时触发同步和异步批量保存操作。
     *
     * @param data    当前读取到的一行数据，EasyExcel 已按 Salaries 模型完成类型转换
     * @param context 解析过程上下文（当前 Sheet、行号、表头信息等）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(Salaries data, AnalysisContext context) {
        // 单线程逐行插入库
//        saveOne(data);

        // 单线程（多线程）批量插入数据
        salariesList.get().add(data);

        if (salariesList.get().size() >= batchSize) {
            // 单线程（多线程）批量插入数据
//            saveData();

            // 异步批量保存当前线程缓冲区中的数据
            asyncSaveData();
        }
    }

    public void saveOne(Salaries data) {
        // 单线程逐行插入数据
        save(data);
        logger.info("第" + count.getAndAdd(1) + "次插入1条数据");
    }

    /**
     * 单线程批量保存当前线程缓冲区中的数据。
     * 适用场景：追求一致性（出错即可回滚）、数据量较小或对实时可见性要求高。
     * 行为说明：
     * - 当本线程的缓冲列表不为空时，直接调用 saveBatch 按当前大小批量入库
     * - 记录本次批量插入的次数与条数日志
     * - 清空本线程缓冲，准备接受后续数据
     */
    public void saveData() {
        if (!salariesList.get().isEmpty()) {
            // 单线程（多线程）批量插入数据
            saveBatch(salariesList.get(), salariesList.get().size());
            logger.info("第" + count.getAndAdd(1) + "次插入" + salariesList.get().size() + "条数据");
            // 清空缓冲，避免重复写入与内存占用
            salariesList.get().clear();
        }
    }

    /**
     * 异步批量保存当前线程缓冲区中的数据。
     * 适用场景：追求吞吐量（减少读取线程阻塞时间）、数据量较大。
     * 行为说明：
     * - 当本线程的缓冲列表不为空时，先克隆一份快照（避免后续读入的数据被并发修改）
     * - 将保存任务提交到线程池异步执行，当前线程立即返回继续读取
     * - 清空本线程缓冲
     * 注意：
     * - 异步任务与当前事务隔离，异常需要在任务中自行处理与重试；如需强一致性可改用 saveData()
     */
    public void asyncSaveData() {
        if (!salariesList.get().isEmpty()) {
            // 克隆当前缓冲，确保提交给线程池的是读到此刻的稳定快照
            ArrayList<Salaries> salaries = (ArrayList<Salaries>) salariesList.get().clone();
            // 异步执行批量保存，降低读取线程阻塞，提高整体吞吐
            executorService.execute(new SaveTask(salaries, salariesListener));
            // 清空缓冲，准备下一批累积
            salariesList.get().clear();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!salariesList.get().isEmpty()) {
            // 兜底：无论不足batchsize与否，都要把余量同步落库
            saveData();
        }
        String sheetName = context.readSheetHolder().getSheetName();
        Integer sheetNo = context.readSheetHolder().getSheetNo();
        logger.info("第" + (sheetNo + 1) + "个Sheet(" + sheetName + ")全部处理完");
    }

    static class SaveTask implements Runnable {

        private List<Salaries> salariesList;
        private SalariesListener salariesListener;

        public SaveTask(List<Salaries> salariesList, SalariesListener salariesListener) {
            this.salariesList = salariesList;
            this.salariesListener = salariesListener;
        }

        @Override
        public void run() {
            salariesListener.saveBatch(salariesList);
            logger.info("第" + count.getAndAdd(1) + "次插入" + salariesList.size() + "条数据");
        }
    }
}
