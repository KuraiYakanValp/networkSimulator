package com.graduationproject.controller;

import com.graduationproject.controller.BoardPropertiesController;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class SendBoardProperties implements Runnable {
    private SimpMessagingTemplate template;

    public SendBoardProperties(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void run() {
        this.template.convertAndSend(BoardPropertiesController.DESTINATION, BoardPropertiesController.boardProperties);
    }
}