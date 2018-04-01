package com.graduationproject.logic;

import com.graduationproject.logic.exceptions.SshExceptions;
import com.graduationproject.logic.exceptions.VirtualBoxExceptions;
import com.graduationproject.model.NetworksInformation;
import com.graduationproject.model.PositionOnBoard;
import com.graduationproject.model.interfaces.InterfaceComponentSwitch;
import lombok.Getter;
import lombok.Setter;

public class VirtualBoxComponentSwitch extends VirtualBoxComponent implements InterfaceComponentSwitch {
    @Getter
    @Setter
    private String networkName;

    public VirtualBoxComponentSwitch(PositionOnBoard positionOnBoard) throws VirtualBoxExceptions, SshExceptions {
        super(VirtualBoxComponents.TYPE_SWITCH,positionOnBoard);
        setRunning(true);
        networkName = getId().toString();
    }
}
