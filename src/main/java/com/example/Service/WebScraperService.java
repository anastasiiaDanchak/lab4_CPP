package com.example.Service;

import com.example.Model.TaskResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WebScraperService {
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final CompletionService<TaskResult> completionService = new ExecutorCompletionService<>(executorService);

    public void scrapeUrls(BlockingQueue<String> urlQueue, Consumer<TaskResult> resultConsumer, BiConsumer<String, String> threadStatusConsumer) {
        while (!urlQueue.isEmpty()) {
            String url = urlQueue.poll();
            if (url != null) {
                String threadName = "Thread-" + url;
                threadStatusConsumer.accept(threadName, "Started");

                // Додаємо завдання до CompletionService
                Future<TaskResult> future = completionService.submit(() -> {
                    threadStatusConsumer.accept(threadName, "Processing");
                    return processUrl(url);
                });

                try {
                    TaskResult result = future.get(5, TimeUnit.SECONDS); // Тайм-аут 5 секунд
                    threadStatusConsumer.accept(threadName, "Completed");
                    resultConsumer.accept(result);
                } catch (TimeoutException e) {
                    future.cancel(true); // Скасовуємо завдання, якщо час перевищено
                    threadStatusConsumer.accept(threadName, "Timeout: Skipped");
                    resultConsumer.accept(new TaskResult(url, "Timeout", 5000, 0));
                } catch (InterruptedException | ExecutionException e) {
                    threadStatusConsumer.accept(threadName, "Error");
                    resultConsumer.accept(new TaskResult(url, "Error: " + e.getMessage(), 0, 0));
                }
            }
        }
    }


    private TaskResult processUrl(String url) {
        long startTime = System.currentTimeMillis();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();
                long endTime = System.currentTimeMillis();
                return new TaskResult(url, "Success", endTime - startTime, content.length());
            } else {
                return new TaskResult(url, "Failed: HTTP " + responseCode, 0, 0);
            }
        } catch (Exception e) {
            return new TaskResult(url, "Error: " + e.getMessage(), System.currentTimeMillis() - startTime, 0);
        }
    }

    public void shutdownService() {
        executorService.shutdown();
    }
}



