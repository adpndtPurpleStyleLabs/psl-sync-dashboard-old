package com.example.Login.services.Oueue;

import com.example.Login.dto.ProductInfo;
import com.example.Login.services.QueueWorker.SQLiteWriteWorker;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class SQLiteWriteQueue {
    private final ConcurrentHashMap<String, BlockingQueue<AbstractMap.SimpleEntry<String, List<ProductInfo>>>> queueMap = new ConcurrentHashMap<>();

    public void createQueue(String tableName) {
        queueMap.putIfAbsent(tableName, new LinkedBlockingQueue<>());
        System.out.println(tableName + " added to queue");
    }

    public void addToQueue(String tableName, List<ProductInfo> productInfo) throws InterruptedException {

        if (!queueMap.containsKey(tableName)){
            createQueue(tableName);
        }
        AbstractMap.SimpleEntry<String, List<ProductInfo>> data = new AbstractMap.SimpleEntry<>(tableName, productInfo);
        queueMap.computeIfAbsent(tableName, k -> new LinkedBlockingQueue<>()).put(data);
    }


    public AbstractMap.SimpleEntry<String, List<ProductInfo>> getMessageFromQueue(String tableName) throws InterruptedException  {
        if (!queueMap.containsKey(tableName)) {
            return null;
        }
        AbstractMap.SimpleEntry<String, List<ProductInfo>> entry = queueMap.computeIfAbsent(tableName, k -> new LinkedBlockingQueue<>()).poll(1, TimeUnit.SECONDS);
        if (null == entry) {
            queueMap.remove(tableName);
            System.out.println(tableName + " removed from queue because empty");
        }
        return entry;
    }

    public int getQueueSize(String tableName) {
        try{
            return queueMap.get(tableName).size();
        }catch (Exception ex){
            return 0;
        }
    }
}