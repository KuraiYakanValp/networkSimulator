package com.graduationproject.model;

import lombok.Getter;
import lombok.Setter;

public class Setting {
    @Getter
    @Setter
    private Components components;
    @Getter
    @Setter
    private BoardProperties boardProperties;

    public Setting(Components components, BoardProperties boardProperties) {
        this.components = components;
        this.boardProperties = boardProperties;
    }

    public Setting(){
        super();
    }
}
