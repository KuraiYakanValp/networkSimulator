package com.graduationproject.controller;


import com.graduationproject.model.BoardProperties;
import com.graduationproject.model.Size;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class BoardPropertiesController {
    protected static BoardProperties boardProperties = new BoardProperties();
    public static final String DESTINATION = "/serverToUsers/boardProperties";

    @MessageMapping("/boardProperties/size/loadPage")
    @SendTo(DESTINATION)
    public BoardProperties loadPageBoardPropertiesSize(Size size) {
        if (boardProperties.getSize() == null)
            boardProperties.setSize(size);
        return boardProperties;
    }

    @MessageMapping("/boardProperties/size/change")
    @SendTo(DESTINATION)
    public BoardProperties changeBoardPropertiesSize(Size size) {
        boardProperties.setSize(size);
        return boardProperties;
    }

    @MessageMapping("/boardProperties/minSize/change")
    @SendTo(DESTINATION)
    public BoardProperties changeBoardPropertiesMinSize(Size size) {
        boardProperties.setMinSize(size);
        return boardProperties;
    }
}

