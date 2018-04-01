package com.graduationproject.controller;

import com.graduationproject.model.Wait;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class SendTCPdumpRow implements Runnable {
    private SimpMessagingTemplate template;
    private Integer id;
    private String networkLink;
    private String row;

    public SendTCPdumpRow(SimpMessagingTemplate template, Integer id, String networkLink,String row) {
        this.template = template;
        this.id = id;
        this.networkLink = networkLink;
        this.row = row;
    }

    public void run() {
        this.template.convertAndSend("/serverToUsers/component/" + id.toString()+"/"+networkLink+"/tcpdump", row);
    }
}
