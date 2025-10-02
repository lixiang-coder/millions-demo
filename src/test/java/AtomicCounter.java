
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 使用AtomicInteger来保证原子性自增
 */
public class AtomicCounter {

    public static void main(String[] args) throws InterruptedException {
        // 使用 AtomicInteger 的并发自增（正确用法）
        testAtomicIncrement();

        // 使用 ++ 的并发自增反例（会丢计数）
        demoNonAtomicPlusPlus();
    }

    /**
     * 并发递增反例：使用非原子操作（++/count = count + 1）在多线程下存在竞态条件。
     * 现象：最终结果通常小于 workCount，说明 ++ 是线程不安全的（读-改-写会被并发打断，导致丢失更新）。
     */
    private static void demoNonAtomicPlusPlus() {
        final int workCount = 50000;
        final int[] counter = {0}; // 引用是final，元素可变；多个线程对 counter[0] 做非原子自增

        ExecutorService executor = Executors.newFixedThreadPool(10);
        long start = System.currentTimeMillis();

        for (int i = 0; i < workCount; i++) {
            executor.execute(() -> {
                // 非原子：读取 -> +1 -> 写回，竞争下会丢失更新
                counter[0] = counter[0] + 1; // 等价于 counter[0]++
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("耗时：" + (System.currentTimeMillis() - start) + "ms");
        System.out.println("执行结果：count=" + counter[0] + "（期望=" + workCount + "）");
    }

    /**
     * 模拟一个递增的任务，递增目标为50000
     */
    /**
     * 并发递增示例（目标 50000）：使用 AtomicInteger.incrementAndGet() 确保原子性。
     * 预期：最终结果等于 workCount，说明在多线程场景下使用原子类能避免丢失更新。
     */
    private static void testAtomicIncrement() throws InterruptedException {
        final AtomicCounter counter = new AtomicCounter();
        int workCount = 50000;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        long start = System.currentTimeMillis();
        for (int i = 0; i < workCount; i++) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    counter.increment();
                }
            };
            executor.execute(runnable);
        }
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        System.out.println("耗时：" + (System.currentTimeMillis() - start) + "ms");
        System.out.println("执行结果：count=" + counter.getCount());
    }


    private AtomicInteger count = new AtomicInteger(0);

    // 使用AtomicInteger之后，不需要加锁，也可以实现线程安全。
    public void increment() {
        //获取当前的值并自增
        count.incrementAndGet();
    }

    /**
     * 获取当前的值
     *
     * @return
     */
    public int getCount() {
        return count.get();
    }

    //递减
    public void deIncrement() {
        count.decrementAndGet();
    }

}