package com.graduationproject.logic;

import com.graduationproject.config.MainRouter;
import com.graduationproject.logic.comandexecute.Ssh;
import com.graduationproject.logic.exceptions.MainRouterExceptions;
import com.graduationproject.logic.exceptions.SshExceptions;
import com.graduationproject.logic.exceptions.VirtualBoxExceptions;
import com.graduationproject.logic.exceptions.VirtualMachineExceptions;
import com.graduationproject.model.Adapters;
import com.graduationproject.model.NetworksInformation;
import com.graduationproject.model.PositionOnBoard;
import com.graduationproject.model.interfaces.InterfaceComponent;
import lombok.Getter;
import lombok.Setter;

public class VirtualBoxComponent implements InterfaceComponent {

    @Getter
    @Setter
    private String type;

    @Getter
    @Setter
    private PositionOnBoard positionOnBoard;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Integer id;


    @Getter
    @Setter
    private NetworksInformation networksInformation;

    private boolean running;

    public VirtualBoxComponent(String type, PositionOnBoard positionOnBoard){
        id = System.identityHashCode(this);
        this.positionOnBoard = positionOnBoard;
        this.type = type;
        name = type + "-" + id;
    }

    public Adapters getAdapters() throws VirtualBoxExceptions {
        return new Adapters(true);
    }

    public boolean isRunning() throws VirtualBoxExceptions, SshExceptions {
        return running;
    }

    public void setRunning(boolean running) throws SshExceptions, VirtualBoxExceptions {
        this.running = running;
    }
}
