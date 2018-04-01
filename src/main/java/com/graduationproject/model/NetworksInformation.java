package com.graduationproject.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

public class NetworksInformation {

    @Getter
    @Setter
    private HashMap<String, NetworkInformation> networksInformation = new HashMap<String, NetworkInformation>();

    public NetworksInformation(HashMap<String, NetworkInformation> networksInformation) {
        this.networksInformation = networksInformation;
    }

    public NetworksInformation() {
        super();
    }
}
