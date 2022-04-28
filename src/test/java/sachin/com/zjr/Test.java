package sachin.com.zjr;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.TtlExecutors;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author Sachin
 * @Date 2022/4/28
 **/
public class Test {

    static ExecutorService executorService = Executors.newFixedThreadPool(1);
    static ExecutorService asyncExecutorService = Executors.newFixedThreadPool(1);
    static  ExecutorService ttlExecutorService;
    static CountDownLatch asyncCtl = new CountDownLatch(1);
    static {


    }

  static    TransmittableThreadLocal<String> context = new TransmittableThreadLocal<String>();

    public static void main(String[] args) {


        System.out.println("GG");

        TransmittableThreadLocal<Integer> ttlA = new TransmittableThreadLocal<Integer>();
        ttlA.set(100);

        context.set("value-set-in-parent");

        new Thread(new Runnable() {
            @Override
            public void run() {

                System.out.println(context.get());

            }
        }).start();



    }



    @org.junit.Test
    public void test() throws Exception {

        //异步创建线程池
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                ttlExecutorService = TtlExecutors.getTtlExecutorService(asyncExecutorService);
                asyncCtl.countDown();

            }
        });
        try {
            asyncCtl.await();
        } catch (Exception e) {

        }
        //创建一个业务线程，业务线程往线程池中提交任务

        new Thread(new Runnable() {
            @Override
            public void run() {

                //业务线程中有自己的 ThreadLocal，而且这个ThreadLocal期望  任务在线程池中的线程执行的时候能够获取到这个 threadLocal的值
                //也就是说任务中能访问到这个threadLocal
                final TransmittableThreadLocal transmittableThreadLocal = new TransmittableThreadLocal();
                transmittableThreadLocal.set("Biz-Thread-A");

                final ThreadLocal<String> threadLocal = new ThreadLocal<String>();
                threadLocal.set("threadLocalValue");

                ttlExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("线程池执行 提交的任务--start");
                        final Object o = transmittableThreadLocal.get();
                        System.out.println("任务中获取到ttl值：" + o);
                        final String s = threadLocal.get();
                        System.out.println("任务中获取到的threadLocal值："+s);
                        System.out.println("任务执行结束");

                    }
                });
            }
        }).start();

        System.in.read();


    }
}
