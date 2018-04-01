package com.graduationproject.model;

import lombok.Getter;
import lombok.Setter;

public class Size {

    @Getter
    @Setter
    private int width;

    @Getter
    @Setter
    private int height;

    public Size(int width,int height){
        this.width=width;
        this.height=height;
    }

    public Size(){

    }
}
