package com.graduationproject.controller;

import com.graduationproject.model.Wait;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class SendWait implements Runnable {
    private SimpMessagingTemplate template;
    private Wait wait;
    private boolean stop;

    public SendWait(SimpMessagingTemplate template, Wait wait) {
        this(template, wait, false);
    }

    public SendWait(SimpMessagingTemplate template, Wait wait, boolean stop) {
        this.template = template;
        this.stop = stop;
        this.wait = wait;
    }

    public void run() {
        this.template.convertAndSend("/serverToUsers/" + (stop ? "stopWait" : "wait"), wait);
    }
}