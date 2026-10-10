import java.util.concurrent.atomic.AtomicLong;

/**
 * Problem # SEPM-001: Comparing Static and Non-Static Variables in Multithreading
 * Single-class implementation: Tahmid_Thread
 */
public class Tahmid_Thread implements Runnable {

    // Shared static counters (belong to the class, shared by all threads)
    private static final AtomicLong safeStaticCounter = new AtomicLong(0);
    private static long unsafeStaticCounter = 0;

    // Non-static (instance) counter (each thread has its own object/instance)
    private long nonStaticCounter = 0;

    private final long incrementsPerThread;
    private final boolean useThreadSafe;

    public Tahmid_Thread(long incrementsPerThread, boolean useThreadSafe) {
        this.incrementsPerThread = incrementsPerThread;
        this.useThreadSafe = useThreadSafe;
    }

    @Override
    public void run() {
        for (long i = 0; i < incrementsPerThread; i++) {
            // Increment shared static counter
            if (useThreadSafe) {
                safeStaticCounter.incrementAndGet();
            } else {
                unsafeStaticCounter++; // Unsynchronized update (race condition)
            }
            // Increment this thread's own instance counter
            nonStaticCounter++;
        }
    }

    public long getNonStaticCounter() {
        return nonStaticCounter;
    }

    public static void resetStaticCounters() {
        safeStaticCounter.set(0);
        unsafeStaticCounter = 0;
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 3) {
            System.out.println("Usage: java Tahmid_Thread <threads> <incrementsPerThread> <true|false>");
            return;
        }

        int numThreads = Integer.parseInt(args[0]);
        long incrementsPerThread = Long.parseLong(args[1]);
        boolean useThreadSafe = Boolean.parseBoolean(args[2]);

        resetStaticCounters();

        Tahmid_Thread[] tasks = new Tahmid_Thread[numThreads];
        Thread[] threads = new Thread[numThreads];

        // Create T threads, each with its own Tahmid_Thread task instance
        for (int i = 0; i < numThreads; i++) {
            tasks[i] = new Tahmid_Thread(incrementsPerThread, useThreadSafe);
            threads[i] = new Thread(tasks[i], "Worker-" + (i + 1));
        }

        // Start all threads
        for (int i = 0; i < numThreads; i++) {
            threads[i].start();
        }

        // Wait for all threads to finish execution using join()
        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }

        // Calculate totals
        long expectedCount = (long) numThreads * incrementsPerThread;
        long finalStaticCount = useThreadSafe ? safeStaticCounter.get() : unsafeStaticCounter;

        long totalNonStaticCount = 0;
        for (int i = 0; i < numThreads; i++) {
            totalNonStaticCount += tasks[i].getNonStaticCounter();
        }

        long absDifference = Math.abs(finalStaticCount - totalNonStaticCount);

        String mode = useThreadSafe ? "Thread-Safe (AtomicLong)" : "Unsynchronized (static long)";
        System.out.println("====================================================");
        System.out.println("Mode                : " + mode);
        System.out.println("Threads (T)         : " + numThreads);
        System.out.println("Increments/Thread(N): " + incrementsPerThread);
        System.out.println("Expected Count (T*N): " + expectedCount);
        System.out.println("Static Counter (S)  : " + finalStaticCount);
        System.out.println("Non-Static Total (I): " + totalNonStaticCount);
        System.out.println("Absolute Diff |S-I| : " + absDifference);

        if (totalNonStaticCount == 0) {
            if (finalStaticCount == 0) {
                System.out.printf("Percentage Diff (%%) : %.4f%%%n", 0.0);
            } else {
                System.out.println("Percentage Diff (%) : Undefined");
            }
        } else {
            double percentageDiff = ((double) absDifference / totalNonStaticCount) * 100.0;
            System.out.printf("Percentage Diff (%%) : %.4f%%%n", percentageDiff);
        }
        System.out.println("====================================================");
    }
}
