package com.example.Login.dto;

import java.util.List;

public class MenuItem {
    private String name;
    private String link;
    private String icon;
    private List<MenuItem> subMenuItems;

    public MenuItem(String name, String link, String icon, List<MenuItem> subMenuItems) {
        this.name = name;
        this.link = link;
        this.icon = icon;
        this.subMenuItems = subMenuItems;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<MenuItem> getSubMenuItems() {
        return subMenuItems;
    }

    public void setSubMenuItems(List<MenuItem> subMenuItems) {
        this.subMenuItems = subMenuItems;
    }
}
