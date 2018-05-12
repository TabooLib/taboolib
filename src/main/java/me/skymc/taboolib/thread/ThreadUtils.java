package me.skymc.taboolib.thread;

import java.util.LinkedList;

/**
 * @author sky
 */
public class ThreadUtils {

    private static final LinkedList<Runnable> queue = new LinkedList<>();
    private static PoolWorker[] threads;

    /**
     * 构造方法
     *
     * @param number 线程数量
     */
    public ThreadUtils(int number) {
        threads = new PoolWorker[number];

        for (int i = 0; i < number; i++) {
            threads[i] = new PoolWorker();
            threads[i].setName("TabooLib WorkThread - " + i);
            threads[i].start();
        }
    }

    /**
     * 停止工作
     */
    public void stop() {
        for (PoolWorker p : threads) {
            p.stop();
        }
    }

    /**
     * 添加任务
     *
     * @param r
     */
    public void execute(Runnable r) {
        // 线程锁
        synchronized (queue) {
            // 添加任务
            queue.addLast(r);
            // 开始任务
            queue.notify();
        }
    }

    private class PoolWorker extends Thread {

        @Override
        public void run() {
            Runnable runnable;

            while (true) {

                // 线程锁
                synchronized (queue) {

                    // 如果任务为空
                    while (queue.isEmpty()) {
                        // 等待任务
                        try {
                            queue.wait();
                        } catch (InterruptedException ignored) {

                        }
                    }
                    // 获取任务
                    runnable = queue.removeFirst();
                }

                // 运行任务
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}