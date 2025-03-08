package com.example.Login.controller;

import com.example.Login.dto.MenuItem;
import com.example.Login.services.NavbarService;
import com.example.Login.services.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class LoginSontroller {

    @Autowired
    public WebhookService webhookService;

    @Autowired
    private NavbarService navbarService;

    @GetMapping("/req/login")
    public String login() {
        return "login";
    }

    @GetMapping("/req/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping({"/","/index"})
    public String home(  Model model) {
        List<MenuItem> menuItems = navbarService.fetchAllNavBarMenus();
        ArrayList<MenuItem> mainMenus = new ArrayList<MenuItem>();
        mainMenus.add(new MenuItem("Product Sync", "","fa-chart-bar", menuItems));
        model.addAttribute("menuItems", mainMenus);
        model.addAttribute("webhookCountLastMinute", "count");

        mainMenus.add(new MenuItem("Psl Crons", "","fa-chart-bar", menuItems));
        model.addAttribute("menuItems", mainMenus);
        model.addAttribute("webhookCountLastMinute", "count");
        return "welcome";
    }

    @GetMapping("/index/{table_name}")
    public String homeCustom(@PathVariable String table_name,  Model model) {
        List<MenuItem> menuItems = navbarService.fetchAllNavBarMenus();

        Optional<MenuItem> newMenuItem = menuItems.stream()
                .filter(item -> ("/index/"+table_name).equals(item.getLink()))
                .findFirst();

        ArrayList<MenuItem> mainMenus = new ArrayList<MenuItem>();
        mainMenus.add(new MenuItem("Product Sync", "","fa-chart-bar", menuItems));

        mainMenus.add(new MenuItem("Psl Crons", "","fa-chart-bar", menuItems));
        model.addAttribute("menuItems", mainMenus);
        model.addAttribute("webhookCountLastMinute", "count");

        model.addAttribute("menuItems", mainMenus);
        model.addAttribute("dashboardName", newMenuItem.get().getName());
        model.addAttribute("tableName", table_name);
        return "index";
    }

    @GetMapping("/createWebHook")
    public String createWebHook() {
        return "webhook-form";
    }
}