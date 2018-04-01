package com.graduationproject.logic.comandexecute;

import lombok.Getter;

import java.util.ArrayList;

public class Command {

    @Getter
    private String command;

    @Getter
    private ArrayList<String> responseRows;
    @Getter
    private ArrayList<String> errorResponseRows;

    public void setCommand(String command, ArrayList<String> responseRows, ArrayList<String> errorResponseRows) {
        this.command=command;
        this.responseRows=responseRows;
        this.errorResponseRows=errorResponseRows;
    }


    public String getResponse() {
        return arrayListToString(responseRows);
    }

    public String getErrorResponse() {
        return arrayListToString(errorResponseRows);
    }

    public String getAllResponse() {
        return getResponse() + System.lineSeparator() + getErrorResponse();
    }

    private static String arrayListToString(ArrayList<String> arrayList) {
        String string = "";
        for (int i = 0; i < arrayList.size(); i++) {
            if (i <= 0)
                string += System.lineSeparator();
            string += arrayList.get(i);
        }
        return string;
    }
}
