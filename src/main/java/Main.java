import javax.swing.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.BlockingQueue;

public class Main {

    private static void withoutConcurrency() {
        float[] arr = new float[10_000_000];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = 1f;
        }
        long before = System.currentTimeMillis();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (float) (arr[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
        }
        long after = System.currentTimeMillis();
        System.out.println("без многопоточности = " + (after - before));
    }

    private static void withConcurrency() {
        float[] arr = new float[10_000_000];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = 1f;
        }
        long before = System.currentTimeMillis();
        float[] arr01 = new float[arr.length / 2];
        float[] arr02 = new float[arr.length / 2];
        System.arraycopy(arr, 0, arr01, 0, arr.length / 2);
        System.arraycopy(arr, arr.length / 2, arr02, 0, arr.length / 2);

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < arr02.length; i++) {
                    arr02[i] = (float) (arr02[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
                }
            }
        });
        thread2.start();
        for (int i = 0; i < arr01.length; i++) {
            arr01[i] = (float) (arr01[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
        }

        try {
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.arraycopy(arr01, 0, arr, 0, arr01.length);
        System.arraycopy(arr02, 0, arr, arr.length / 2, arr02.length);

        long after = System.currentTimeMillis();
        System.out.println("с многопоточностью = " + (after - before));
    }

    private static void startTimer() {
        Thread timer = new Thread(new Runnable() {
            @Override
            public void run() {
                int seconds = 0;
                try {
                    while (true) {
                        System.out.println(seconds++);
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        timer.setDaemon(true);
        timer.start();
    }

    private static void taskWithExecutorService() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch countDownLatch = new CountDownLatch(3);
        long before = System.currentTimeMillis();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long sum = 0;
                for (int i = 0; i < 1_000_000; i++) {
                    if (i % 2 == 0) {
                        sum += i;
                    }
                }
                System.out.println("сумма четных чисел " + sum);
                countDownLatch.countDown();
            }
        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long sum = 0;
                for (int i = 0; i < 1_000_000; i++) {
                    if (i % 7 == 0) {
                        sum += i;
                    }
                }
                System.out.println("сумма чисел, которые без остатка делятся на 7 " + sum);
                countDownLatch.countDown();
            }
        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                List<Integer> randomNumbers = new ArrayList<>();
                for (int i = 0; i < 1_000; i++) {
                    int random = (int) (Math.random() * 1000);
                    randomNumbers.add(random);
                    if (random % 2 == 0) {
                        count++;
                    }
                }
                System.out.println("количество четных чисел в коллекции " + count);
                countDownLatch.countDown();
            }
        });

        executorService.shutdown();

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long after = System.currentTimeMillis();
        System.out.println("время выполнения " + (after - before));
    }

    private static void taskExecutorServiceSubmitAndFuture() {
        ExecutorService executorService = Executors.newFixedThreadPool(3,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setDaemon(true);
                        return thread;
                    }
                });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                int seconds = 0;
                try {
                    while (true) {
                        System.out.print(".");
                        Thread.sleep(300);
                        seconds++;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Future<String> futureName = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(5000);
                return "John";
            }
        });
        Future<Integer> futureAge = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(4000);
                return 32;
            }
        });

        try {
            String name = futureName.get();
            Integer age = futureAge.get();

            System.out.println("\nName: " + name + " Age: " + age);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    private static void blockingQueueWaitNotify() {
        BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (true) {
                    System.out.println("Counter " + i);
                    i++;
                    Runnable task = null;
                    try {
                        task = blockingQueue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    new Thread(task).start();
                }
            }
        }).start();
        for (int i = 0; i < 10; i++) {
            final int index = i;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            blockingQueue.add(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("---" + index);

                }
            });
        }
    }

    private static final Object MONITOR = new Object();
    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";
    private static String nextLetter = A;

    private static void threeThreadTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (MONITOR) {
                    for (int i = 0; i < 5; i++) {
                        try {
                            while (!nextLetter.equals(A)) {
                                MONITOR.wait();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.print(A);
                        nextLetter = B;
                        MONITOR.notifyAll();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (MONITOR) {
                    for (int i = 0; i < 5; i++) {
                        try {
                            while (!nextLetter.equals(B)) {
                                MONITOR.wait();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.print(B);
                        nextLetter = C;
                        MONITOR.notifyAll();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (MONITOR) {
                    for (int i = 0; i < 5; i++) {
                        try {
                            while (!nextLetter.equals(C)) {
                                MONITOR.wait();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.print(C);
                        nextLetter = A;
                        MONITOR.notifyAll();
                    }
                }
            }
        }).start();
    }

    private static void concurrentCollections() {
        List<Integer> numbers = new CopyOnWriteArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 100; i++) {
                        Thread.sleep(100);
                        numbers.add(i);
                    }
                    countDownLatch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 100; i++) {
                        Thread.sleep(100);
                        numbers.add(i);
                    }
                    countDownLatch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(numbers.size());

    }


    public static void main(String[] args) {
        List<Long> listResults = new CopyOnWriteArrayList<>();

        CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
        Semaphore semaphore = new Semaphore(3);
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long before = System.currentTimeMillis();
                    String name = Thread.currentThread().getName();
                    long millisPrep = (long) (Math.random() * 5000 + 1000);
                    long millisFirst = (long) (Math.random() * 3000 + 1500);
                    long millisTunnel = (long) (Math.random() * 4000 + 2000);
                    long millisSecond = (long) (Math.random() * 7000 + 900);

                    System.out.println(name + " starts preparing");
                    try {
                        Thread.sleep(millisPrep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(name + " finish preparing");

                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    System.out.println(name + " first part of road");
                    try {
                        Thread.sleep(millisFirst);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {
                        semaphore.acquire();
                        System.out.println(name + " Tunnel!");
                        Thread.sleep(millisTunnel);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        semaphore.release();
                    }
                    System.out.println(name + " second part of path");

                    try {
                        Thread.sleep(millisSecond);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(name + " finish");
                    long after = System.currentTimeMillis();
                    long time = after - before;

                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    listResults.add(time);

                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                    Long min = Collections.min(listResults);
                    System.out.println("Winner's time = " + min);

                    System.out.println("Time of " + name + " = " + time);
                }
            }).start();

        }

    }

    private static void cyclicBarrier() {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long millis = (long) (Math.random() * 5000 + 1000);
                    String name = Thread.currentThread().getName();
                    System.out.println(name + ": Data is being prepared.");
                    try {
                        Thread.sleep(millis);
                        System.out.println(name + ": Data is ready.");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    System.out.println(name + ": Continue work.");

                }
            }).start();
        }
    }

    private static void semaphorePractice() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Semaphore semaphore = new Semaphore(3);
        for (int i = 0; i < 10; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    String name = Thread.currentThread().getName();
                    System.out.println(name + " started working.");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        semaphore.acquire();
                        workWithFileSystem();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        semaphore.release();
                    }

                    System.out.println(name + " finished working.");

                }
            });
        }
        executorService.shutdown();
    }

    private static void workWithFileSystem() {
        String name = Thread.currentThread().getName();
        System.out.println(name + " started working with file system");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(name + " finished working with file system");
    }
}
