package com.graduationproject.model;

import com.graduationproject.model.interfaces.InterfaceComponent;
import lombok.Getter;
import lombok.Setter;

public class Component implements InterfaceComponent {

    @Getter
    @Setter
    private String type;

    @Getter
    @Setter
    private PositionOnBoard positionOnBoard;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private boolean running;

    @Getter
    @Setter
    private NetworksInformation networksInformation;

}
