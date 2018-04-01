package com.graduationproject.model;


import lombok.Getter;
import lombok.Setter;

public class Adapter {
    @Getter
    @Setter
    private String mac;
    @Getter
    @Setter
    private Integer number;
    @Getter
    @Setter
    private Boolean set;
    @Getter
    @Setter
    private Boolean disabled;

    public Adapter(String mac, Integer number, Boolean set, Boolean disabled) {
        this.mac = mac;
        this.number = number;
        this.set = set;
        this.disabled = disabled;
    }

    public Adapter(String mac){
        this.mac=mac;
        this.number = null;
        this.set = null;
        this.disabled = null;
    }

    public Adapter() {
        super();
    }
}
