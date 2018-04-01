package com.graduationproject.model;

import com.graduationproject.logic.exceptions.CodedExceptions;
import com.graduationproject.logic.exceptions.MainRouterExceptions;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class ErrorModule {
    @Getter
    @Setter
    private String message;

    public ErrorModule(CodedExceptions e){
        if (e instanceof MainRouterExceptions){
            message = "Fatal error";
        } if (Objects.equals(e.getErrorCode(),CodedExceptions.UNEXPECTED)){
            message = "Error";
        }else {
            message = e.getMessage();
        }
    }
}
