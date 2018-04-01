package com.graduationproject.logic.exceptions;

public class VirtualMachineExceptions extends CodedExceptions {
    public static final int DHCP_LEASES_FILE_NOT_FOUND = 3001;
    public static final int INVALID_IP = 3002;
    public static final int IP_ALREADY_EXIST = 3003;
    public static final int INVALID_NETWORK_LINK = 3004;
    public static final int CANNOT_ADD_IP = 3005;
    public static final int IP_DONT_EXIST = 3005;


    public VirtualMachineExceptions(String message, Integer errorCode) {
        super(message, errorCode);
    }

    public VirtualMachineExceptions(String message, Integer errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
