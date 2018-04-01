package com.graduationproject.logic.exceptions;

public class MainRouterExceptions extends SshExceptions {
    public static final int DHCP_LEASES_FILE_NOT_FOUND = VirtualMachineExceptions.DHCP_LEASES_FILE_NOT_FOUND;

    public MainRouterExceptions(String message, Integer errorCode) {
        super(message, errorCode);
    }

    public MainRouterExceptions(String message, Integer errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }

    public MainRouterExceptions(SshExceptions cause) {
        this(cause.getMessage(),cause.getErrorCode(),cause.getCause());
    }

}
