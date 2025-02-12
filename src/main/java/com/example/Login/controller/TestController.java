package com.example.Login.controller;

import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import java.util.HashMap;
import java.sql.*;


@RestController
@RequestMapping("/webhook")
public class TestController {

    // Map to store queues and their associated processing threads
    private final Map<String, BlockingQueue<String>> webhookQueues = new ConcurrentHashMap<>();
    private final Map<String, ExecutorService> queueProcessors = new ConcurrentHashMap<>();
    private final Map<String, String> dbConnections = new HashMap<>();

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int STRING_LENGTH = 100000;

    public static String generateRandomString() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(STRING_LENGTH);

        for (int i = 0; i < STRING_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }

    @PostMapping("/create")
    public String createWebhook(@RequestParam String webhookId) {
        if (webhookQueues.containsKey(webhookId)) {
            return "Webhook already exists.";
        }

//        // Create a new SQLite database for this webhook
//        String dbUrl = "jdbc:sqlite:" + webhookId + ".db";
//        createDatabase(dbUrl);
//        dbConnections.put(webhookId, dbUrl);

        // Create a limited BlockingQueue (fixing memory leak issue)
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(); // Set an appropriate limit
        webhookQueues.put(webhookId, queue);

        // Start a consumer thread to process queue data
        ExecutorService executor = Executors.newSingleThreadExecutor();
//        executor.submit(() -> processQueue(webhookId, queue, dbUrl));
        queueProcessors.put(webhookId, executor);

        return "Webhook created successfully.";
    }

    @PostMapping("/receive")
    public String receiveWebhook(@RequestParam String webhookId, @RequestBody String data) {
        BlockingQueue<String> queue = webhookQueues.get(webhookId);
        if (queue == null) {
            return "Webhook not found.";
        }

        // Offer data to the queue, preventing memory leaks if the queue is full
        boolean added = queue.offer(data);

        for(int i = 0 ; i< 100;i++){
            queue.offer(generateRandomString());
        }
        System.out.println(queue.size());
        return added ? "Data received." : "Queue is full, data dropped!";
    }

    @DeleteMapping("/delete")
    public String deleteWebhook(@RequestParam String webhookId) {
        if (!webhookQueues.containsKey(webhookId)) {
            return "Webhook not found.";
        }

        // Stop and remove the queue processor
        ExecutorService executor = queueProcessors.remove(webhookId);
        if (executor != null) {
            executor.shutdownNow();
        }

        // Remove queue and database reference
        webhookQueues.remove(webhookId);
        dbConnections.remove(webhookId);

        return "Webhook deleted successfully.";
    }

    private void createDatabase(String dbUrl) {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS WebhookData (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT)";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void processQueue(String webhookId, BlockingQueue<String> queue, String dbUrl) {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            while (!Thread.currentThread().isInterrupted()) {
                String data = queue.poll(5, TimeUnit.SECONDS); // Prevents blocking indefinitely
                if (data != null) {
                    saveToDatabase(conn, data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveToDatabase(Connection conn, String data) {
        String insertSQL = "INSERT INTO WebhookData (data) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, data);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
