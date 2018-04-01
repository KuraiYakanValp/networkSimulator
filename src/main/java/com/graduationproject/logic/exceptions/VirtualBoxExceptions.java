package com.graduationproject.logic.exceptions;


import com.graduationproject.logic.exceptions.CodedExceptions;

public class VirtualBoxExceptions extends CodedExceptions {
    public static final int VIRTUAL_MACHINE_ALREADY_RUNNING = 1001;
    public static final int VIRTUAL_MACHINE_NOT_FOUND = 1002;
    public static final int VIRTUAL_MACHINE_ALREADY_EXIST = 10083;
    public static final int VIRTUAL_MACHINE_CLONING_SNAPSHOT_NOT_FOUND = 1004;
    public static final int VIRTUAL_MACHINE_IS_LOCKED = 1005;
    public static final int VIRTUAL_MACHINE_IS_NOT_RUNNING = 1006;
    public static final int NETWORK_ADAPTER_NOT_FOUND = 1007;
    public static final int NETWORK_ADAPTER_NOT_SELECTED = 1008;


    public VirtualBoxExceptions(String message, Integer errorCode) {
        super(message, errorCode);
    }

    public VirtualBoxExceptions(String message, Integer errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
