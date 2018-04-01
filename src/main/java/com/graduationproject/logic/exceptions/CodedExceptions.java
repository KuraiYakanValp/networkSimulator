package com.graduationproject.logic.exceptions;

import lombok.Getter;
import lombok.Setter;

public class CodedExceptions extends Exception {
    public static final int UNEXPECTED = 0;
    public static final int IO_EXCEPTION = 1;
    public static final int CLONE_NOT_SUPPORTED_EXCEPTION = 2;
    public static final int IMPORT_EXCEPTION = 3;

    @Getter
    @Setter
    private Integer errorCode;

    public CodedExceptions(String message, Integer errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public CodedExceptions(String message, Integer errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
