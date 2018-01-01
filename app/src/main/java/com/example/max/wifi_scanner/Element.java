package com.example.max.wifi_scanner;

public class Element {

    private String title;
    private String security;
    private int level;
    private boolean network;

    public Element(String title, String security, int level, boolean network) {
        this.title = title;
        this.security = security;
        this.level = level;
        this.network = network;
    }

    public String getTitle() {
        return title;
    }

    public String getSecurity() {
        return security;
    }

    public int getLevel() {
        return level;
    }

    public boolean getNetwork() {
        return network;
    }
}
