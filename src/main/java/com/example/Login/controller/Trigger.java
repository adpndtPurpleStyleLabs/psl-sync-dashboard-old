package com.example.Login.controller;

import com.example.Login.dto.*;
import com.example.Login.enums.Status;
import com.example.Login.services.WebhookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trigger")
public class Trigger {

    @Autowired
    private WebhookService webhookService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/a")
    public boolean triggerWebhook(@RequestBody Map<String, Object> payload) {
        WebhookApiPayload webhookApiPayload = new WebhookApiPayload();
        webhookApiPayload.setEventKey(payload.get("eventKey").toString());
        webhookApiPayload.setEventStatus(Status.valueOf(payload.get("eventStatus").toString()));
        List<?> productIdsJson = (List<?>) payload.get("productIds"); // Get List of LinkedHashMap
        List<String> productIds = new ArrayList<>();
        webhookApiPayload.setProductInfo(productIdsJson.stream()
                .map(x -> {
                    try {
                        String jsonString = objectMapper.writeValueAsString(x);
                        JsonNode jsonNode = objectMapper.readTree(jsonString);
                        String id = jsonNode.has("id") ? jsonNode.get("id").asText() : "Unknown";
                        productIds.add(id);
                        return new ProductInfo(id, jsonString, LocalDateTime.now());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Error processing JSON: " + x, e);
                    }
                })
                .collect(Collectors.toList()));
        return webhookService.triggerWebhook(webhookApiPayload);
    }

    @GetMapping("/webhook-stats/{table_name}/{timeRate}")
    public CounterDto triggerWebhook(@PathVariable String table_name, @PathVariable String timeRate) {
        return webhookService.getWebhookCountLastMinute(table_name, timeRate);
    }

    @GetMapping("/dayChart/{table_name}")
    public List<DayWiseCountDto> getSalesData(@PathVariable String table_name) {
        return webhookService.getLastDayData(table_name);
    }

    @GetMapping("/search/{table_name}")
    public List<SearchDto> search(@PathVariable String table_name, @RequestParam int page, @RequestParam int size,@RequestParam String productId) {
        return webhookService.getSyncedProductIds(table_name, page, size, productId);
    }

    @GetMapping("/remove/{event_key}")
    public boolean removeWebhook(@PathVariable String event_key) {
        return webhookService.removeWebhook(event_key);
    }

    @GetMapping("/json-data/{event_key}")
    public String getJsonData(@PathVariable String event_key, @RequestParam String productId) {
        return webhookService.getProductDetails(event_key, productId);
    }
}