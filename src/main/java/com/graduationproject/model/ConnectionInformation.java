package com.graduationproject.model;

import lombok.Getter;
import lombok.Setter;

public class ConnectionInformation implements Cloneable {
    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private Adapter adapter;

    public ConnectionInformation() {
        super();
    }

    public ConnectionInformation clone() throws CloneNotSupportedException {
        return (ConnectionInformation) super.clone();
    }
}