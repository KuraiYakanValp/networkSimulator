package com.graduationproject.model.interfaces;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.graduationproject.logic.exceptions.SshExceptions;
import com.graduationproject.logic.exceptions.VirtualBoxExceptions;
import com.graduationproject.logic.exceptions.VirtualMachineExceptions;
import com.graduationproject.model.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ComponentPC.class, name = "PC"),
        @JsonSubTypes.Type(value = ComponentRouter.class, name = "router"),
        @JsonSubTypes.Type(value = ComponentSwitch.class, name = "switch")
})
public interface InterfaceComponent {

    String getType();

    void setType(String type);

    PositionOnBoard getPositionOnBoard();

    void setPositionOnBoard(PositionOnBoard positionOnBoard);

    String getName();

    void setName(String name);

    Integer getId();

    void setId(Integer id);

    boolean isRunning() throws VirtualBoxExceptions, SshExceptions;

    void setRunning(boolean running) throws SshExceptions, VirtualBoxExceptions;

    NetworksInformation getNetworksInformation();

    void setNetworksInformation(NetworksInformation networksInformation);

}
