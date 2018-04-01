package com.graduationproject.controller;

import com.graduationproject.model.ErrorModule;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class SendError implements Runnable {
    private SimpMessagingTemplate template;
    private ErrorModule error;

    public SendError(SimpMessagingTemplate template,ErrorModule error) {
        this.template = template;
        this.error =error;
    }

    public void run() {
        this.template.convertAndSend("/serverToUsers/error", error);
    }
}