package com.example.Login.services.QueueWorker;

import com.example.Login.dto.ProductInfo;
import com.example.Login.services.Oueue.SQLiteWriteQueue;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SQLiteWriteWorker {
    @Autowired
    private SQLiteWriteQueue writeQueue;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${WORKER_COUNT}")
    private int workerCount;

    private final ConcurrentHashMap<String, ExecutorService> workerPools = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        List<String> tables = jdbcTemplate.queryForList("SELECT event_key FROM webhooks", String.class);
        for (String table : tables) {
            table = table+"_pid";
            writeQueue.createQueue(table);
            startWorker(table);
        }
    }

    public void startWorker(String tableName) {
        workerPools.putIfAbsent(tableName, Executors.newFixedThreadPool(5));
        for (int i = 0; i < workerCount; i++) {
            submitWorkerTask(tableName);
        }
    }

    private void submitWorkerTask(String tableName) {
        workerPools.get(tableName).submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        AbstractMap.SimpleEntry<String, List<ProductInfo>> jsonMsg = writeQueue.getMessageFromQueue(tableName);
                        if (jsonMsg == null) {
                            Thread.sleep(999);
                            continue;
                        }

                        saveToDatabase(tableName, jsonMsg.getValue());
                        System.gc();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                restartWorkerIfNeeded(tableName);
            }
        });
    }

    private void restartWorkerIfNeeded(String tableName) {
        workerPools.get(tableName).submit(() -> {
            System.out.println("Restarting worker for table: " + tableName);
            submitWorkerTask(tableName);
        });
    }


    private void saveToDatabase(String tableName, List<ProductInfo> productInfoBulk) {
            try {
                String sql = "INSERT INTO " + tableName + " (id, jsonData, receivedAt) VALUES (?, ?, ?) " +
                        "ON CONFLICT(id) DO UPDATE SET jsonData = excluded.jsonData, receivedAt = excluded.receivedAt;";

                jdbcTemplate.batchUpdate(sql, productInfoBulk, productInfoBulk.size(), (ps, productInfo) -> {
                    ps.setString(1, productInfo.getProductId());
                    ps.setString(2, productInfo.getProductDetails());
                    ps.setTimestamp(3, Timestamp.valueOf(productInfo.getReceivedAt()));
                });
                return;
            } catch (Exception e) {
                if (e.getMessage().contains("database is locked")) {
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                } else {
                }
            }
        try {
            writeQueue.addToQueue(tableName, productInfoBulk);
        } catch (InterruptedException ignored) {}
    }
}
