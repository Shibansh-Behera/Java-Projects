import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MiniEventStreamProcessor {

    // Shared thread-safe queue
    private static final BlockingQueue<String> eventQueue = new LinkedBlockingQueue<>(100);

    // Control flag to stop threads gracefully
    private static final AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) throws InterruptedException {
        int producerCount = 2;
        int consumerCount = 3;

        ExecutorService executor = Executors.newFixedThreadPool(producerCount + consumerCount + 1);

        // Start producers
        for (int i = 1; i <= producerCount; i++) {
            final int id = i;
            executor.submit(() -> runProducer(id));
        }

        // Start consumers
        for (int i = 1; i <= consumerCount; i++) {
            final int id = i;
            executor.submit(() -> runConsumer(id));
        }

        // Start metrics reporter
        executor.submit(MiniEventStreamProcessor::runMetrics);

        // Let it run for 10 seconds
        Thread.sleep(10000);
        running.set(false);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Processor stopped.");
    }

    // Producer: generates fake events
    private static void runProducer(int id) {
        Random random = new Random();
        while (running.get()) {
            try {
                String event = "Producer" + id + ": StockPrice=" + (100 + random.nextInt(50));
                eventQueue.put(event);
                Thread.sleep(200 + random.nextInt(200)); // simulate varying speed
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Consumer: processes events
    private static void runConsumer(int id) {
        while (running.get() || !eventQueue.isEmpty()) {
            try {
                String event = eventQueue.poll(500, TimeUnit.MILLISECONDS);
                if (event != null) {
                    System.out.println("Consumer" + id + " processed: " + event);
                    Thread.sleep(300); // simulate processing time
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Metrics Reporter
    private static void runMetrics() {
        while (running.get()) {
            try {
                System.out.println("[Metrics] Queue Size: " + eventQueue.size());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
