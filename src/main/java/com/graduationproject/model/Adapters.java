package com.graduationproject.model;


import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

public class Adapters {
    @Getter
    @Setter
    private LinkedList<Adapter> adapters = new LinkedList<Adapter>();

    public Adapters() {
        super();
    }

    public Adapters(boolean setNull) {
        this.adapters = null;
    }

    public void addAdapter(Adapter adapter){
        adapters.add(adapter);
    }
}
