package com.graduationproject.logic.Interfaces;

import com.graduationproject.logic.exceptions.SshExceptions;

public interface SshExceptionSender {
    void send(SshExceptions e);
}
