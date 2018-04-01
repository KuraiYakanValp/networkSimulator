package com.graduationproject.model;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

public class NetworkInformation {
    @Getter
    @Setter
    private Adapter adapter;

    @Getter
    @Setter
    private LinkedList<String> ipv4 = new LinkedList<>();

    @Getter
    @Setter
    private LinkedList<String> ipv6 = new LinkedList<>();

    @Getter
    @Setter
    private String link;


    public NetworkInformation(Adapter adapter, String link) {
        this.adapter = adapter;
        this.link = link;
    }

    public NetworkInformation(Adapter adapter) {
        this.adapter = adapter;
        this.link = null;
    }

    public NetworkInformation(){
        super();
    }

    public void addIPv4(String ipv4) {
        this.ipv4.add(ipv4);
    }

    public void addIPv6(String ipv6) {
        this.ipv6.add(ipv6);
    }
}
