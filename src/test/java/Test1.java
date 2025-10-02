public class Test1 {
    public static void main(String[] args) {
        // 返回纳秒级的时间，通常用于测量时间间隔（如性能测试），因为它基于系统高精度计时器
        long startTime = System.nanoTime();
        System.out.println("startTime = " + startTime);
        // 返回毫秒级的时间，基于Unix纪元（1970-01-01），用于获取当前时间戳
        long currentTimeMillis = System.currentTimeMillis();
        System.out.println("currentTimeMillis = " + currentTimeMillis);
    }
}
