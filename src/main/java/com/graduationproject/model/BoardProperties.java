package com.graduationproject.model;

import lombok.Getter;
import lombok.Setter;

public class BoardProperties {
    @Getter
    @Setter
    private Size size = null;

    @Getter
    @Setter
    private Size minSize=new Size(100,100);
}
