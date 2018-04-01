package com.graduationproject.model;

import com.graduationproject.model.interfaces.InterfaceComponent;
import com.graduationproject.model.interfaces.InterfaceComponentRouter;
import com.graduationproject.model.interfaces.InterfaceComponents;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

public class Components implements InterfaceComponents {

    @Getter
    @Setter
    private HashMap<Integer, InterfaceComponent> components;
    @Getter
    @Setter
    private HashMap<String, Connection> connections;

}
