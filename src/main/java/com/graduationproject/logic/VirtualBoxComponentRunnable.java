package com.graduationproject.logic;

import com.graduationproject.config.MainRouter;
import com.graduationproject.logic.comandexecute.Ssh;
import com.graduationproject.logic.exceptions.*;
import com.graduationproject.model.Adapter;
import com.graduationproject.model.Adapters;
import com.graduationproject.model.NetworksInformation;
import com.graduationproject.model.PositionOnBoard;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class VirtualBoxComponentRunnable extends VirtualBoxComponent {
    private static final int MAIN_NETWORK_ADAPTER_NUMBER = 1;
    private static final String SSH_USERNAME = "root";
    private static final String SSH_PASSWORD = "networkSimulator";

    @Getter
    private String virtualMachineName;


    @Getter
    @Setter
    private NetworksInformation networksInformation;

    @Getter
    private String ip;

    @Getter
    private Mac mac;

    @Getter
    private Ssh ssh = null;

    @Getter
    @Setter
    private String deviceName;


    public VirtualBoxComponentRunnable(String type, String deviceName, PositionOnBoard positionOnBoard) throws VirtualBoxExceptions, SshExceptions {
        super(type, positionOnBoard);
        this.deviceName = deviceName;
        virtualMachineName = VirtualBoxStaticEvents.virtualMachineNameCreator(type, Integer.toString(getId()));
        createVirtualMachine();
    }

    private void createVirtualMachine() throws VirtualBoxExceptions, SshExceptions {
        boolean removedSameVirtualMachine = false;
        boolean contin;
        do {
            contin = false;
            try {
                VirtualBoxStaticEvents.cloneVirtualMachine(VirtualBoxStaticEvents.virtualMachineNameCreator(getType(), VirtualBoxStaticEvents.CLONEABLE_VIRTUAL_MACHINE_NAME_SUFFIX), virtualMachineName);
                setRunning(false);
            } catch (VirtualBoxExceptions e) {
                if (e.getErrorCode() == VirtualBoxExceptions.VIRTUAL_MACHINE_ALREADY_EXIST) {
                    if (!removedSameVirtualMachine) {
                        contin = true;
                        removedSameVirtualMachine = true;
                        removeVirtualMachine();
                    } else {
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        } while (contin);
    }


    public NetworksInformation loadNetworksInformation() throws VirtualBoxExceptions, SshExceptions {
        networksInformation = VirtualBoxStaticEvents.networksInformation(ssh, getVirtualMachineName());
        return networksInformation;
    }

    public void removeVirtualMachine() throws VirtualBoxExceptions {
        if (ssh != null)
            ssh.disconnect();
        boolean powerOffVirtualMachine = false;
        boolean contin;
        do {
            contin = false;
            try {
                VirtualBoxStaticEvents.removeVirtualMachine(virtualMachineName);
            } catch (VirtualBoxExceptions e) {
                if (e.getErrorCode() == VirtualBoxExceptions.VIRTUAL_MACHINE_IS_LOCKED) {
                    if (!powerOffVirtualMachine) {
                        try {
                            contin = true;
                            powerOffVirtualMachine = true;
                            VirtualBoxStaticEvents.powerOffVirtualMachine(virtualMachineName);
                        } catch (VirtualBoxExceptions ex) {
                            if (ex.getErrorCode() != VirtualBoxExceptions.VIRTUAL_MACHINE_IS_NOT_RUNNING) {
                                throw ex;
                            }
                        }
                    } else {
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        } while (contin);
    }

    public void runVirtualMachine(boolean run) throws VirtualBoxExceptions, SshExceptions {
        if (run) {
            startVirtualMachine();
        } else
            powerOffVirtualMachine();
    }

    public void startVirtualMachine() throws VirtualBoxExceptions, SshExceptions {
        boolean contin;
        boolean powerOff = false;
        boolean clone = false;
        do {
            contin = false;
            try {
                VirtualBoxStaticEvents.startVirtualMachine(virtualMachineName);
            } catch (VirtualBoxExceptions e) {
                if (e.getErrorCode() == VirtualBoxExceptions.VIRTUAL_MACHINE_ALREADY_RUNNING && !powerOff) {
                    powerOff = true;
                    contin = true;
                    powerOffVirtualMachine();
                } else if (e.getErrorCode() == VirtualBoxExceptions.VIRTUAL_MACHINE_NOT_FOUND && !clone) {
                    clone = true;
                    contin = true;
                    createVirtualMachine();
                } else {
                    throw e;
                }
            }
        } while (contin);
        setRunning(true);
    }

    public void powerOffVirtualMachine() throws VirtualBoxExceptions, SshExceptions {
        if (ssh != null)
            ssh.disconnect();
        boolean contin;
        boolean clone = false;
        do {
            contin = false;
            try {
                VirtualBoxStaticEvents.powerOffVirtualMachine(virtualMachineName);
                setRunning(false);
            } catch (VirtualBoxExceptions e) {
                if (e.getErrorCode() != VirtualBoxExceptions.VIRTUAL_MACHINE_IS_NOT_RUNNING) {
                    if (e.getErrorCode() == VirtualBoxExceptions.VIRTUAL_MACHINE_NOT_FOUND && !clone) {
                        contin = true;
                        clone = true;
                        createVirtualMachine();
                    } else {
                        throw e;
                    }
                }
            }
        } while (contin);
    }

    @Override
    public Adapters getAdapters() throws VirtualBoxExceptions {
        return VirtualBoxStaticEvents.virtualMachineAdapters(virtualMachineName);
    }

    private void connectSSH() throws VirtualBoxExceptions, SshExceptions {
        if (ssh != null)
            ssh.disconnect();

        mac = VirtualBoxStaticEvents.macOfVirtualMachineNetworkAdapter(virtualMachineName, MAIN_NETWORK_ADAPTER_NUMBER);
        try {
            ip = MainRouter.ipByMac(mac);
        } catch (MainRouterExceptions e) {
            if (e.getErrorCode() == MainRouterExceptions.MAC_NOT_FOUND) {
                throw new SshExceptions(e.getMessage(), e.getErrorCode(), e);
            } else {
                throw e;
            }
        }
        ssh = new Ssh(SSH_USERNAME, SSH_PASSWORD, ip);
    }

    public void connectNetworkAdapter(Adapter adapter, String networkName) throws VirtualBoxExceptions, SshExceptions {
        boolean again;
        do {
            again = false;
            try {
                VirtualBoxStaticEvents.setVirtualBoxNetworkAdapter(virtualMachineName, adapter.getNumber(), networkName, isRunning());
            } catch (VirtualBoxExceptions e) {
                runningControllerException(e);
                again = true;
            }
        } while (again);
        loadNetworksInformation();
    }

    public void disconnectNetworkAdapter(Adapter adapter) throws VirtualBoxExceptions, SshExceptions {
        boolean again;
        do {
            again = false;
            try {
                VirtualBoxStaticEvents.unsetVirtualBoxNetworkAdapter(virtualMachineName, adapter.getNumber(), isRunning());
            } catch (VirtualBoxExceptions e) {
                runningControllerException(e);
                again = true;
            }
        } while (again);
        loadNetworksInformation();
    }

    private void runningControllerException(VirtualBoxExceptions e) throws VirtualBoxExceptions, SshExceptions {
        if (Objects.equals(e.getErrorCode(), VirtualBoxExceptions.VIRTUAL_MACHINE_IS_LOCKED)) {
            setRunning(true);
        } else if (Objects.equals(e.getErrorCode(), VirtualBoxExceptions.VIRTUAL_MACHINE_IS_NOT_RUNNING)) {
            setRunning(false);
        } else if (e != null)
            throw e;
    }

    public void addNetworkAdapter() throws VirtualBoxExceptions, SshExceptions {
        if (!isRunning())
            VirtualBoxStaticEvents.addNetworkAdapter(this.virtualMachineName);
        loadNetworksInformation();
    }

    public void removeNetworkAdapter(Integer adapterNumber) throws VirtualBoxExceptions, SshExceptions {
        if (!isRunning())
            VirtualBoxStaticEvents.removeNetworkAdapter(this.virtualMachineName, adapterNumber);
        loadNetworksInformation();
    }

    public void openVirtualMachine() throws VirtualBoxExceptions {
        VirtualBoxStaticEvents.startVirtualMachine(virtualMachineName, "separate");
    }

    public void openSshTerminal() throws CodedExceptions {
        VirtualBoxStaticEvents.openSshTerminal(SSH_USERNAME, SSH_PASSWORD, ip);
    }

    @Override
    public boolean isRunning() throws VirtualBoxExceptions, SshExceptions {
        boolean actualState = VirtualBoxStaticEvents.doesVirtualMachineRunning(virtualMachineName);
        if (super.isRunning() != actualState)
            setRunning(actualState);
        return super.isRunning();
    }

    @Override
    public void setRunning(boolean running) throws SshExceptions, VirtualBoxExceptions {
        super.setRunning(running);
        if (running) {
            connectSSH();
        } else {
            if (ssh != null)
                ssh.disconnect();
            ssh = null;
        }
        loadNetworksInformation();
    }
}
