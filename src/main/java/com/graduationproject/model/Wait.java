package com.graduationproject.model;

import lombok.Getter;
import lombok.Setter;

public class Wait  {
    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private String message;

    public Wait(){super();}

    public Wait(String message){
        id = System.identityHashCode(this);
        this.message=message;
    }
}
