package com.graduationproject.controller;
//1.3.2018  java        1887 rows    72552 characters with blank space    57336 characters    30.38 characters per row
//          javascript   909 rows    34294 characters with blank space    23374 character     25.71 characters per row
//          total       2796 rows   106846 characters with blank space    80710 characters    28.87 characters per row


import com.graduationproject.config.Mappers;
import com.graduationproject.logic.Interfaces.StringSender;
import com.graduationproject.logic.VirtualBoxComponents;
import com.graduationproject.logic.exceptions.CodedExceptions;
import com.graduationproject.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PreDestroy;
import java.util.LinkedList;

@Controller
public class ComponentsController { //TODO disable grub router
    protected static VirtualBoxComponents components = new VirtualBoxComponents();
    public static final String DESTINATION = "/serverToUsers/components";
    @Autowired
    private SimpMessagingTemplate template;


    @SubscribeMapping("/components")
    public Components subscribeComponents() {
        return Mappers.componentsMapper.map(components);
    }

    @SubscribeMapping("/component/{id}/{networkLink}/tcpdump")
    public LinkedList<String> subscribeComponentstcp(@DestinationVariable Integer id, @DestinationVariable String networkLink) {
        if (!components.containsID(id))
            return null;
        StringSender newTCPdumpRowAction = a -> new SendTCPdumpRow(template, id, networkLink, a).run();
        components.addNewTCPdumpRowAction(id, networkLink, newTCPdumpRowAction);
        return components.getTCPdump(id, networkLink);
    }


    @MessageMapping("/components/addConnection")
    @SendTo(DESTINATION)
    public Components addConnection(Connection connection) {
        try {
            components.addConnection(connection);
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
        return Mappers.componentsMapper.map(components);
    }

    @MessageMapping("/components/removeConnection")
    @SendTo(DESTINATION)
    public Components removeConnection(Connection connection) {
        try {
            components.removeConnection(connection);
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
        return Mappers.componentsMapper.map(components);
    }

    @MessageMapping("/components/refresh")
    @SendTo(DESTINATION)
    public Components refresh() {
        Wait wait = new Wait("Refreshing components.");
        new SendWait(template, wait).run();
        try {
            components.refresh();
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
        new SendWait(template, wait, true).run();
        return Mappers.componentsMapper.map(components);
    }

    @MessageMapping("/components/colorConnection")
    @SendTo(DESTINATION)
    public Components colorConnection(Connection connection) {
        components.colorConnection(connection);
        return Mappers.componentsMapper.map(components);
    }

    @MessageMapping("/component/create")
    @SendTo(DESTINATION)
    public Components createComponent(CreateComponent createComponent) {
        try {
            components.createComponent(createComponent.getType(), createComponent.getPositionOnBoard());
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
        return Mappers.componentsMapper.map(components);
    }

    @MessageMapping("/component/{id}/run")
    @SendTo(DESTINATION)
    public Components runComponent(@DestinationVariable Integer id, Boolean run) {
        if (!components.containsID(id))
            return null;
        Wait wait = new Wait("Component is starting.");
        new SendWait(template, wait).run();
        try {
            components.runComponent(id, run);
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
        new SendWait(template, wait, true).run();
        return Mappers.componentsMapper.map(components);
    }

    @MessageMapping("/component/{id}/remove")
    @SendTo(DESTINATION)
    public Components removeComponent(@DestinationVariable Integer id) {
        if (!components.containsID(id))
            return null;
        try {
            components.removeComponent(id);
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
        return Mappers.componentsMapper.map(components);
    }

    @RequestMapping(value = "/component/{id}/adapters", method = RequestMethod.GET)
    @ResponseBody
    public Adapters componentAdapters(@PathVariable Integer id) {
        if (!components.containsID(id))
            throw new ResourceNotFoundException();
        try {
            Adapters a = components.getComponentAdapters(id);
            return a;
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
        throw new InternalServerErrorException();
    }

    @MessageMapping("/component/{id}/moveOnBoard")
    @SendTo(DESTINATION)
    public Components moveComponentOnBoard(@DestinationVariable Integer id, PositionOnBoard positionOnBoard) {
        if (!components.containsID(id))
            return null;
        components.moveComponentOnBoard(id, positionOnBoard);
        return Mappers.componentsMapper.map(components);
    }

    @MessageMapping("/component/{id}/changeName")
    @SendTo(DESTINATION)
    public Components changeComponentName(@DestinationVariable Integer id, String newName) {
        if (!components.containsID(id))
            return null;
        components.changeName(id, newName);
        return Mappers.componentsMapper.map(components);
    }

    @MessageMapping("/component/{id}/openVirtualMachine")
    public void openVirtualMachine(@DestinationVariable Integer id) {
        if (!components.containsID(id))
            return;
        try {
            components.openVirtualMachine(id);
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
    }

    @MessageMapping("/component/{id}/openSshTerminal")
    public void openSshTerminal(@DestinationVariable Integer id) {
        if (!components.containsID(id))
            return;
        try {
            components.openSshTerminal(id);
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
    }

    @MessageMapping("/component/{id}/addIP")
    @SendTo(DESTINATION)
    public Components addComponentIP(@DestinationVariable Integer id, ChangeIP changeIP) {//TODO restart remove IP
        if (!components.containsID(id))
            return null;
        try {
            components.addIP(id, changeIP.getIp(), changeIP.getLink());
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
        return Mappers.componentsMapper.map(components);
    }

    @MessageMapping("/component/{id}/removeIP")
    @SendTo(DESTINATION)
    public Components removeComponentIP(@DestinationVariable Integer id, ChangeIP changeIP) {
        if (!components.containsID(id))
            return null;
        try {
            components.removeIP(id, changeIP.getIp(), changeIP.getLink());
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
        return Mappers.componentsMapper.map(components);
    }

    @RequestMapping(value = {"/component/{id}/LuCi"})
    public ModelAndView openLuci(@PathVariable(value = "id") Integer id) {
        try {
            if (components.containsID(id) && components.getType(id).equals(VirtualBoxComponents.TYPE_ROUTER) && components.isRunning(id)) {
                return new ModelAndView("redirect:http://" + components.getIp(id));
            } else {
                throw new ResourceNotFoundException();
            }
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
        throw new InternalServerErrorException();

    }

    @MessageMapping("/component/{id}/addNetworkAdapter")
    @SendTo(DESTINATION)
    public Components addComponentNetworkAdapter(@DestinationVariable Integer id) {
        if (!components.containsID(id))
            return null;
        try {
            components.addNetworkAdapter(id);
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
        return Mappers.componentsMapper.map(components);
    }

    @MessageMapping("/component/{id}/removeNetworkAdapter")
    @SendTo(DESTINATION)
    public Components removeComponentNetworkAdapter(@DestinationVariable Integer id,Integer adapterNumber) {
        if (!components.containsID(id))
            return null;
        try {
            components.removeNetworkAdapter(id,adapterNumber);
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
        return Mappers.componentsMapper.map(components);
    }

    @MessageMapping("/component/{id}/refreshNetworksInformation")
    @SendTo(DESTINATION)
    public Components refreshNetworksInformation(@DestinationVariable Integer id) {
        if (!components.containsID(id))
            return null;
        try {
            components.refreshNetworksInformation(id);
        } catch (CodedExceptions e) {
            e.printStackTrace();
            new SendError(template, new ErrorModule(e)).run();
        }
        return Mappers.componentsMapper.map(components);
    }

    @PreDestroy
    public void destroy() throws Exception {
        components.removeComponents();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public class ResourceNotFoundException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public class InternalServerErrorException extends RuntimeException {
    }


}