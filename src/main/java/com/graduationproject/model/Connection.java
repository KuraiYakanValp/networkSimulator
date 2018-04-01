package com.graduationproject.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class Connection {

    @Getter
    @Setter
    private ConnectionInformation fromComponent;

    @Getter
    @Setter
    private ConnectionInformation toComponent;

    @Getter
    @Setter
    private String color;

    public Connection(ConnectionInformation fromComponent, ConnectionInformation toComponent,String color){
        this.fromComponent=fromComponent;
        this.toComponent=toComponent;
        this.color=color;
    }

    public Connection() {
        super();
    }

    public ConnectionInformation getOtherByID(Integer id) {
        if(Objects.equals(id, fromComponent.getId())){
            return toComponent;
        } else {
            return fromComponent;
        }
    }
}
