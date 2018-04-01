package com.graduationproject.logic.exceptions;

import com.graduationproject.logic.exceptions.CodedExceptions;

public class SshExceptions extends CodedExceptions {
    public static final int SSH_WRONG_HOST = 2001;
    public static final int SSH_WRONG_AUTHORISATION = 2002;
    public static final int SSH_TIMEOUT = 2003;
    public static final int MAC_NOT_FOUND = 2004;
    public static final int SESSION_IS_DOWN = 2005;

    public SshExceptions(String message, Integer errorCode) {
        super(message, errorCode);
    }

    public SshExceptions(String message, Integer errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }


}
