package com.graduationproject.model;

import lombok.Getter;
import lombok.Setter;

public class PositionOnBoard {
    @Getter
    @Setter
    private int left;

    @Getter
    @Setter
    private int top;

    public PositionOnBoard(){
        this(0,0);
    }
    public PositionOnBoard(int left, int top){
        this.left=left;
        this.top=top;
    }
}
