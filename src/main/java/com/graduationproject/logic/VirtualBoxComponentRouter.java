package com.graduationproject.logic;

import com.graduationproject.logic.exceptions.SshExceptions;
import com.graduationproject.logic.exceptions.VirtualBoxExceptions;
import com.graduationproject.model.NetworksInformation;
import com.graduationproject.model.PositionOnBoard;
import com.graduationproject.model.interfaces.InterfaceComponentRouter;

public class VirtualBoxComponentRouter extends VirtualBoxComponentRunnable implements InterfaceComponentRouter {
    public VirtualBoxComponentRouter(PositionOnBoard positionOnBoard) throws VirtualBoxExceptions, SshExceptions {
        super(VirtualBoxComponents.TYPE_ROUTER,"LEDE",positionOnBoard);
    }

    @Override
    public NetworksInformation loadNetworksInformation() throws VirtualBoxExceptions, SshExceptions {
        setNetworksInformation(VirtualBoxStaticEvents.networksInformation(null, getVirtualMachineName()));
        return getNetworksInformation();
    }
}
