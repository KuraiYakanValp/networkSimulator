package com.graduationproject.controller;

import com.graduationproject.config.Mappers;
import com.graduationproject.controller.ComponentsController;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class SendComponents implements Runnable {
    private SimpMessagingTemplate template;

    public SendComponents(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void run() {
        this.template.convertAndSend(ComponentsController.DESTINATION, Mappers.componentsMapper.map(ComponentsController.components));
    }
}