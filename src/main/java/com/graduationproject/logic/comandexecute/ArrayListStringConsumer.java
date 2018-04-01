package com.graduationproject.logic.comandexecute;

import lombok.Getter;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ArrayListStringConsumer implements Consumer<String> {
    @Getter
    private ArrayList<String> values = new ArrayList<String>();

    @Override
    public void accept(String s) {
        values.add(s);
    }
}
