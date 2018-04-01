package com.graduationproject.logic;

import com.graduationproject.logic.Interfaces.SshExceptionSender;
import com.graduationproject.logic.Interfaces.StringSender;
import com.graduationproject.logic.comandexecute.Ssh;
import com.graduationproject.logic.exceptions.SshExceptions;
import com.graduationproject.logic.exceptions.VirtualBoxExceptions;
import com.graduationproject.logic.exceptions.VirtualMachineExceptions;
import com.graduationproject.model.NetworkInformation;
import com.graduationproject.model.NetworksInformation;
import com.graduationproject.model.PositionOnBoard;
import com.graduationproject.model.interfaces.InterfaceComponentPC;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class VirtualBoxComponentPC extends VirtualBoxComponentRunnable implements InterfaceComponentPC {

    private HashMap<String, LinkedList<String>> tcpdumps = new HashMap<>();

    private HashMap<String, StringSender> newTCPdumpRowAction = new HashMap<>();


    public VirtualBoxComponentPC(PositionOnBoard positionOnBoard) throws VirtualBoxExceptions, SshExceptions {
        super(VirtualBoxComponents.TYPE_PC, "networkSimulator-PC", positionOnBoard);
    }

    @Override
    public NetworksInformation loadNetworksInformation() throws VirtualBoxExceptions, SshExceptions {
        NetworksInformation networksInformation = super.loadNetworksInformation();
        if (isRunning()) {
            for (Map.Entry<String, NetworkInformation> entry : networksInformation.getNetworksInformation().entrySet()) {
                Boolean disabled = entry.getValue().getAdapter().getDisabled();
                if (disabled != null && !disabled && !newTCPdumpRowAction.containsKey(entry.getValue().getLink())) {
                    Ssh.CommandExecuter startNetworkLink = getSsh().new CommandExecuter("ip link set \"" + entry.getValue().getLink() + "\" up");
                    tcpdumps.put(entry.getValue().getLink(), new LinkedList<String>());
                    StringSender newLine = (a -> addTCPdumpRow(entry.getValue().getLink(), a));
                    SshExceptionSender sshException = Throwable::printStackTrace;
                    Thread thread = getSsh().new CommandWaitingResponse(getDeviceName(), "tcpdump -l --number -n -t -i \"" + entry.getValue().getLink() + "\" icmp | mawk -W interactive '{print $1\": \"$3\" \"$4\" \"$5\" \"$6}'", newLine, sshException);
                    thread.start();
                }
            }
        } else {
            newTCPdumpRowAction = new HashMap<>();
        }
        return networksInformation;
    }

    public void addIP(String ip, String link) throws SshExceptions, VirtualBoxExceptions, VirtualMachineExceptions {
        if (getSsh() != null)
            VirtualBoxStaticEvents.addIP(getSsh(), ip, link);
        loadNetworksInformation();
    }

    public void removeIP(String ip, String link) throws SshExceptions, VirtualBoxExceptions, VirtualMachineExceptions {
        if (getSsh() != null)
            VirtualBoxStaticEvents.removeIP(getSsh(), ip, link);
        loadNetworksInformation();
    }

    public void addTCPdumpRow(String networkLink, String row) {
        LinkedList<String> tcpdump = tcpdumps.get(networkLink);
        tcpdump.add(row);
        while (tcpdump.size() > 50) {
            tcpdump.removeFirst();
        }
        StringSender action = newTCPdumpRowAction.get(networkLink);
        if (action != null)
            action.send(row);
    }

    public void addNewTCPdumpRowAction(String networkLink, StringSender action) {
        newTCPdumpRowAction.put(networkLink, action);
    }

    public LinkedList<String> getTCPdump(String networkLink) {
        return tcpdumps.get(networkLink);
    }
}
