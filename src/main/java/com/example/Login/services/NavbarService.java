package com.example.Login.services;

import com.example.Login.dto.MenuItem;
import com.example.Login.model.Webhook;
import com.example.Login.repo.WebhookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NavbarService {
    @Autowired
    private WebhookRepository webhookRepository;

    public List<MenuItem> fetchAllNavBarMenus() {
        List<MenuItem> menuItems = new ArrayList<>();
        for (Webhook webhook : webhookRepository.findAll()) {
            menuItems.add(new MenuItem(webhook.getEventName(), "/index/" + webhook.getEventKey(), "fa-chart-bar", null));
        }
        menuItems.add(new MenuItem("Create new dashboard", "/webhooks/new", "fa fa-plus",  null));
        return menuItems;
    }
}
