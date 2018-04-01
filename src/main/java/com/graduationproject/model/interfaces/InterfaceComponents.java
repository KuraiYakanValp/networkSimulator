package com.graduationproject.model.interfaces;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.graduationproject.model.Components;
import com.graduationproject.model.Connection;

import java.util.HashMap;


public interface InterfaceComponents {
    HashMap<Integer, InterfaceComponent> getComponents();

    HashMap<String, Connection> getConnections();

}
