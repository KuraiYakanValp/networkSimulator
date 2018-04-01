package com.graduationproject.logic;

import com.graduationproject.logic.comandexecute.LinuxCommandExecuter;
import com.graduationproject.logic.comandexecute.Ssh;
import com.graduationproject.logic.exceptions.CodedExceptions;
import com.graduationproject.logic.exceptions.SshExceptions;
import com.graduationproject.logic.exceptions.VirtualBoxExceptions;
import com.graduationproject.logic.exceptions.VirtualMachineExceptions;
import com.graduationproject.model.Adapter;
import com.graduationproject.model.Adapters;
import com.graduationproject.model.NetworkInformation;
import com.graduationproject.model.NetworksInformation;

import java.io.IOException;
import java.util.*;

public class VirtualBoxStaticEvents {

    public static final String VIRTUAL_MACHINE_NAME_PREFIX = "networkSimulator";
    public static final String CLONEABLE_VIRTUAL_MACHINE_NAME_SUFFIX = "default";
    public static final String SNAPSHOT_FOR_CLONING = "production";
    public static final String DHCP_LEASES_FILE_LOCATION = "/tmp/dhcp.leases";
    public static final int DISABLED_NETWORK_ADAPTER_NUMBER = 1;

    public static void cloneVirtualMachine(String clonedVirtualMachineName, String newVirtualMachineName) throws VirtualBoxExceptions {
        try {
            LinuxCommandExecuter command = new LinuxCommandExecuter("VBoxManage clonevm \"" + clonedVirtualMachineName + "\" --snapshot " + SNAPSHOT_FOR_CLONING + " --options link --name \"" + newVirtualMachineName + "\" --register");
            if (command.getResponseRows().size() == 1 && command.getResponseRows().get(0).equals(virtualMachineClonedText(newVirtualMachineName))) {
                //cloned
            } else if (command.getErrorResponseRows().get(0).equals(virtualMachineNotFoundText(clonedVirtualMachineName)) || command.getErrorResponse().contains("Could not open the medium") || command.getErrorResponse().contains("FILE_NOT_FOUND")) {
                throw new VirtualBoxExceptions("Virtual machine \"" + clonedVirtualMachineName + "\" not found", VirtualBoxExceptions.VIRTUAL_MACHINE_NOT_FOUND);
            } else if (command.getErrorResponseRows().get(0).contains("Machine settings file") && command.getErrorResponseRows().get(0).contains(newVirtualMachineName) && command.getErrorResponseRows().get(0).contains("already exists")) {
                throw new VirtualBoxExceptions("Virtual machine \"" + newVirtualMachineName + "\" already exist", VirtualBoxExceptions.VIRTUAL_MACHINE_ALREADY_EXIST);
            } else if (command.getErrorResponseRows().get(0).equals("VBoxManage: error: Could not find a snapshot named '" + SNAPSHOT_FOR_CLONING + "'")) {
                throw new VirtualBoxExceptions("Could not find snapshot for cloning", VirtualBoxExceptions.VIRTUAL_MACHINE_CLONING_SNAPSHOT_NOT_FOUND);
            } else {
                throw new VirtualBoxExceptions(command.getAllResponse(), VirtualBoxExceptions.UNEXPECTED);
            }
        } catch (IOException e) {
            throw new VirtualBoxExceptions("Problem with command execution", VirtualBoxExceptions.IO_EXCEPTION, e);
        }
    }

    public static void addNetworkAdapter(String virtualMachineName) throws VirtualBoxExceptions {
        Integer adapterNumber = null;
        Integer before = null;
        boolean found = false;
        Adapters networksInformation = virtualMachineAdapters(virtualMachineName);
        for (Adapter adapter : networksInformation.getAdapters()) {
            before = adapterNumber;
            adapterNumber = adapter.getNumber();
            if (before != null && before + 1 != adapterNumber) {
                adapterNumber = before + 1;
                found = true;
                break;
            }
        }
        if (adapterNumber == null) {
            adapterNumber = 2;
        } else if (!found) {
            adapterNumber++;
        }

        setNetworkAdapterWithoutParameters(virtualMachineName,adapterNumber,"null");
    }

    public static void removeNetworkAdapter(String virtualMachineName, Integer adapterNumber) throws VirtualBoxExceptions {
        setNetworkAdapterWithoutParameters(virtualMachineName,adapterNumber,"none");
    }

    public static void setNetworkAdapterWithoutParameters(String virtualMachineName, Integer adapterNumber, String adapterType) throws VirtualBoxExceptions {//TODO catch max adapter numver
        try {
            LinuxCommandExecuter command = new LinuxCommandExecuter("VBoxManage modifyvm \"" + virtualMachineName + "\" --nic" + adapterNumber + " " + adapterType);
            if (command.getResponseRows().size() == 0 && command.getErrorResponseRows().size() == 0) {
                return;
            } else if (command.getErrorResponseRows().get(0).equals(virtualMachineNotFoundText(virtualMachineName))) {
                throw new VirtualBoxExceptions("Virtual machine \"" + virtualMachineName + "\" not found", VirtualBoxExceptions.VIRTUAL_MACHINE_NOT_FOUND);
            } else if (command.getErrorResponse().contains(virtualMachineAlreadyRunningText(virtualMachineName))) {
                throw new VirtualBoxExceptions("Virtual machine \"" + virtualMachineName + "\" is locked", VirtualBoxExceptions.VIRTUAL_MACHINE_IS_LOCKED);
            } else {
                throw new VirtualBoxExceptions(command.getAllResponse(), VirtualBoxExceptions.UNEXPECTED);
            }
        } catch (IOException e) {
            throw new VirtualBoxExceptions("Problem with command execution", VirtualBoxExceptions.IO_EXCEPTION, e);
        }
    }

    public static void startVirtualMachine(String virtualMachineName) throws VirtualBoxExceptions {
        startVirtualMachine(virtualMachineName, "headless");
    }


    public static void startVirtualMachine(String virtualMachineName, String runningType) throws VirtualBoxExceptions {
        try {
            LinuxCommandExecuter command = new LinuxCommandExecuter("VBoxManage startvm \"" + virtualMachineName + "\" --type " + runningType);
            if (command.getResponseRows().size() == 2 && command.getResponseRows().get(1).equals(virtualMachineStartedText(virtualMachineName))) {
                System.out.println("Starting virtual machine \"" + virtualMachineName + "\"");
            } else if (command.getErrorResponseRows().get(0).equals(virtualMachineAlreadyRunningText(virtualMachineName))) {
                throw new VirtualBoxExceptions("\"" + virtualMachineName + "\" is already running", VirtualBoxExceptions.VIRTUAL_MACHINE_ALREADY_RUNNING);
            } else if (command.getErrorResponseRows().get(0).equals(virtualMachineNotFoundText(virtualMachineName)) || command.getErrorResponse().contains("Could not open the medium") || command.getErrorResponse().contains("FILE_NOT_FOUND")) {
                throw new VirtualBoxExceptions("Virtual machine \"" + virtualMachineName + "\" not found", VirtualBoxExceptions.VIRTUAL_MACHINE_NOT_FOUND);
            } else {
                throw new VirtualBoxExceptions(command.getAllResponse(), VirtualBoxExceptions.UNEXPECTED);
            }

        } catch (IOException e) {
            throw new VirtualBoxExceptions("Problem with command execution", VirtualBoxExceptions.IO_EXCEPTION, e);
        }
    }

    public static void openSshTerminal(String user, String password, String host) throws CodedExceptions {
        try {
            LinuxCommandExecuter command = new LinuxCommandExecuter("gnome-terminal -e 'sshpass -p \"" + password + "\" ssh \"" + user + "\"@\"" + host + "\" -o StrictHostKeyChecking=no'");
        } catch (IOException e) {
            throw new CodedExceptions("Problem with command execution", CodedExceptions.IO_EXCEPTION, e);
        }
    }

    public static void removeVirtualMachine(String virtualMachineName) throws VirtualBoxExceptions {
        try {
            LinuxCommandExecuter command = new LinuxCommandExecuter("vboxmanage unregistervm \"" + virtualMachineName + "\" --delete");
            if (command.getResponseRows().size() == 0 && command.getErrorResponseRows().get(0).contains("100%")) {
                //removed
            } else if (command.getErrorResponseRows().get(0).equals(virtualMachineNotFoundText(virtualMachineName))) {
                throw new VirtualBoxExceptions("Virtual machine \"" + virtualMachineName + "\" not found", VirtualBoxExceptions.VIRTUAL_MACHINE_NOT_FOUND);
            } else if (command.getErrorResponseRows().get(0).equals(cantUnregisterLockedVirtualMachineText(virtualMachineName))) {
                throw new VirtualBoxExceptions("Virtual machine \"" + virtualMachineName + "\" is locked.", VirtualBoxExceptions.VIRTUAL_MACHINE_IS_LOCKED);
            } else {
                throw new VirtualBoxExceptions(command.getAllResponse(), VirtualBoxExceptions.UNEXPECTED);
            }
        } catch (IOException e) {
            throw new VirtualBoxExceptions("Problem with command execution", VirtualBoxExceptions.IO_EXCEPTION, e);
        }
    }


    public static void powerOffVirtualMachine(String virtualMachineName) throws VirtualBoxExceptions {
        try {
            LinuxCommandExecuter command = new LinuxCommandExecuter("VBoxManage controlvm \"" + virtualMachineName + "\" poweroff");
            if (command.getResponseRows().size() == 0 && command.getErrorResponseRows().get(0).contains("100%")) {
                //powered off
            } else if (command.getErrorResponseRows().get(0).equals(virtualMachineNotFoundText(virtualMachineName))) {
                throw new VirtualBoxExceptions("Virtual machine \"" + virtualMachineName + "\" not found", VirtualBoxExceptions.VIRTUAL_MACHINE_NOT_FOUND);
            } else if (command.getErrorResponseRows().get(0).equals(virtualMachineNotRunningText(virtualMachineName))) {
                throw new VirtualBoxExceptions("Virtual machine \"" + virtualMachineName + "\" is not running", VirtualBoxExceptions.VIRTUAL_MACHINE_IS_NOT_RUNNING);
            } else {
                throw new VirtualBoxExceptions(command.getAllResponse(), VirtualBoxExceptions.UNEXPECTED);
            }
        } catch (IOException e) {
            throw new VirtualBoxExceptions("Problem with command execution", VirtualBoxExceptions.IO_EXCEPTION, e);
        }
    }

    public static Mac macOfVirtualMachineNetworkAdapter(String virtualMachineName, int networkAdapterNumber) throws VirtualBoxExceptions {
        ArrayList<String> info = virtualMachineInfo(virtualMachineName);
        for (String anInfo : info) {
            String[] infoRow = anInfo.split("=");
            if (infoRow[0].equals("macaddress" + networkAdapterNumber)) {
                return new Mac(infoRow[1].substring(1, infoRow[1].length() - 1));
            }
        }
        throw new VirtualBoxExceptions("Network adapter \"" + networkAdapterNumber + "\" not found in virtual machine \"" + virtualMachineName + "\"", VirtualBoxExceptions.NETWORK_ADAPTER_NOT_FOUND);
    }

    public static void setVirtualBoxNetworkAdapter(String virtualMachineName, Integer adapterNumber, String networkName, boolean runing) throws VirtualBoxExceptions {
        if (adapterNumber == null)
            throw new VirtualBoxExceptions("No adapter number selected", VirtualBoxExceptions.NETWORK_ADAPTER_NOT_SELECTED);

        long start = System.currentTimeMillis();
        boolean inTimeout;
        do {
            inTimeout = System.currentTimeMillis() - start < 500;
            try {
                LinuxCommandExecuter command;
                if (runing) {
                    command = new LinuxCommandExecuter("VBoxManage controlvm \"" + virtualMachineName + "\" nic" + adapterNumber + " intnet  \"" + networkName + "\"");
                } else {
                    command = new LinuxCommandExecuter("VBoxManage modifyvm \"" + virtualMachineName + "\" --nic" + adapterNumber + " intnet  --intnet" + adapterNumber + " \"" + networkName + "\"");
                }
                if (command.getResponseRows().size() == 0 && command.getErrorResponseRows().size() == 0) {
                    return;
                } else if (command.getErrorResponseRows().get(0).equals(virtualMachineNotFoundText(virtualMachineName))) {
                    throw new VirtualBoxExceptions("Virtual machine \"" + virtualMachineName + "\" not found", VirtualBoxExceptions.VIRTUAL_MACHINE_NOT_FOUND);
                } else if (command.getErrorResponse().contains(virtualMachineAlreadyRunningText(virtualMachineName))) {
                    if (!inTimeout)
                        throw new VirtualBoxExceptions("Virtual machine \"" + virtualMachineName + "\" is locked", VirtualBoxExceptions.VIRTUAL_MACHINE_IS_LOCKED);
                } else if (command.getErrorResponse().contains(virtualMachineNotRunningText(virtualMachineName))) {
                    throw new VirtualBoxExceptions("Virtual machine \"" + virtualMachineName + "\" is not running", VirtualBoxExceptions.VIRTUAL_MACHINE_IS_NOT_RUNNING);
                } else {
                    throw new VirtualBoxExceptions(command.getAllResponse(), VirtualBoxExceptions.UNEXPECTED);
                }
            } catch (IOException e) {
                throw new VirtualBoxExceptions("Problem with command execution", VirtualBoxExceptions.IO_EXCEPTION, e);
            }
        } while (true);
    }

    public static void unsetVirtualBoxNetworkAdapter(String virtualMachineName, Integer adapterNumber, boolean running) throws VirtualBoxExceptions {
        if (adapterNumber == null)
            throw new VirtualBoxExceptions("No adapter number selected", VirtualBoxExceptions.NETWORK_ADAPTER_NOT_SELECTED);

        try {
            LinuxCommandExecuter command;
            if (running) {
                command = new LinuxCommandExecuter("VBoxManage controlvm \"" + virtualMachineName + "\" nic" + adapterNumber + " null");
            } else {
                command = new LinuxCommandExecuter("VBoxManage modifyvm \"" + virtualMachineName + "\" --nic" + adapterNumber + " null");
            }
            if (command.getResponseRows().size() == 0 && command.getErrorResponseRows().size() == 0) {
                //adapter unset
            } else if (command.getErrorResponseRows().get(0).equals(virtualMachineNotFoundText(virtualMachineName))) {
                throw new VirtualBoxExceptions("Virtual machine \"" + virtualMachineName + "\" not found", VirtualBoxExceptions.VIRTUAL_MACHINE_NOT_FOUND);
            } else if (command.getErrorResponse().contains(virtualMachineAlreadyRunningText(virtualMachineName))) {
                throw new VirtualBoxExceptions("Virtual machine \"" + virtualMachineName + "\" is locked", VirtualBoxExceptions.VIRTUAL_MACHINE_IS_LOCKED);
            } else if (command.getErrorResponse().contains(virtualMachineNotRunningText(virtualMachineName))) {
                throw new VirtualBoxExceptions("Virtual machine \"" + virtualMachineName + "\" is not running", VirtualBoxExceptions.VIRTUAL_MACHINE_IS_NOT_RUNNING);
            } else {
                throw new VirtualBoxExceptions(command.getAllResponse(), VirtualBoxExceptions.UNEXPECTED);
            }
        } catch (IOException e) {
            throw new VirtualBoxExceptions("Problem with command execution", VirtualBoxExceptions.IO_EXCEPTION, e);
        }
    }

    public static Adapters virtualMachineAdapters(String virtualMachineName) throws VirtualBoxExceptions {
        Adapters adapters = new Adapters();
        ArrayList<String> info = virtualMachineInfo(virtualMachineName);
        for (String anInfo : info) {
            String[] infoRow = anInfo.split("=");
            if (infoRow[0].contains("macaddress")) {
                Mac mac = new Mac(infoRow[1].substring(1, infoRow[1].length() - 1));
                Integer number;
                try {
                    number = Integer.parseInt(infoRow[0].substring(infoRow[0].length() - 1));
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    number = null;
                }
                adapters.addAdapter(new Adapter(mac.toString(), number, true, false));
            }
        }

        for (String anInfo : info) {
            String[] infoRow = anInfo.split("=");
            for (Adapter adapter : adapters.getAdapters()) {
                if (infoRow[0].equals("nic" + adapter.getNumber())) {
                    if (adapter.getNumber() == DISABLED_NETWORK_ADAPTER_NUMBER) {
                        adapter.setSet(false);
                        adapter.setDisabled(true);
                    } else if (infoRow[1].equals("\"null\""))
                        adapter.setSet(false);
                }
            }
        }
        return adapters;
    }


    public static ArrayList<String> virtualMachineInfo(String virtualMachineName) throws VirtualBoxExceptions {
        try {
            LinuxCommandExecuter command = new LinuxCommandExecuter("VBoxManage showvminfo \"" + virtualMachineName + "\" --machinereadable");
            if (command.getErrorResponseRows().size() == 0) {
                return command.getResponseRows();
            } else if (command.getErrorResponseRows().get(0).equals(virtualMachineNotFoundText(virtualMachineName))) {
                throw new VirtualBoxExceptions("Virtual machine \"" + virtualMachineName + "\" not found", VirtualBoxExceptions.VIRTUAL_MACHINE_NOT_FOUND);
            } else {
                throw new VirtualBoxExceptions(command.getAllResponse(), VirtualBoxExceptions.UNEXPECTED);
            }
        } catch (IOException e) {
            throw new VirtualBoxExceptions("Problem with command execution", VirtualBoxExceptions.IO_EXCEPTION, e);
        }
    }

    public static boolean doesVirtualMachineRunning(String virtualMachineName) throws VirtualBoxExceptions {
        ArrayList<String> virtualMachineInfo = virtualMachineInfo(virtualMachineName);
        for (int i = 0; i < virtualMachineInfo.size(); i++) {
            if (virtualMachineInfo.get(i).contains("VMState=\"running\""))
                return true;
        }
        return false;
    }

    public static String cantUnregisterLockedVirtualMachineText(String virtualMachineName) {
        return "VBoxManage: error: Cannot unregister the machine '" + virtualMachineName + "' while it is locked";
    }

    public static String virtualMachineStartedText(String virtualMachineName) {
        return "VM \"" + virtualMachineName + "\" has been successfully started.";
    }

    public static String virtualMachineAlreadyRunningText(String virtualMachineName) {
        return "VBoxManage: error: The machine '" + virtualMachineName + "' is already locked by a session (or being locked or unlocked)";
    }

    public static String virtualMachineClonedText(String newVirtualMachineName) {
        return "Machine has been successfully cloned as \"" + newVirtualMachineName + "\"";
    }

    public static String virtualMachineNotFoundText(String virtualMachineName) {
        return "VBoxManage: error: Could not find a registered machine named '" + virtualMachineName + "'";
    }

    public static String virtualMachineNotRunningText(String virtualMachineName) {
        return "VBoxManage: error: Machine '" + virtualMachineName + "' is not currently running";
    }

    public static String virtualMachineNameCreator(String type, String id) {
        return VIRTUAL_MACHINE_NAME_PREFIX + "-" + type + "-" + id;
    }

    public static ArrayList<String> dhcpLeases(Ssh ssh) throws SshExceptions, VirtualMachineExceptions {
        Ssh.CommandExecuter command = ssh.new CommandExecuter("cat " + DHCP_LEASES_FILE_LOCATION);
        if (command.getErrorResponseRows().size() == 0) {
            return command.getResponseRows();
        } else if (command.getErrorResponseRows().get(0).equals("cat: can't open '" + DHCP_LEASES_FILE_LOCATION + "': No such file or directory")) {
            throw new VirtualMachineExceptions("DHCP leases file not found", VirtualMachineExceptions.DHCP_LEASES_FILE_NOT_FOUND);
        } else {
            throw new VirtualMachineExceptions(command.getAllResponse(), VirtualMachineExceptions.UNEXPECTED);
        }
    }


    public static NetworksInformation networksInformation(Ssh ssh, String virtualMachineName) throws SshExceptions, VirtualBoxExceptions {
        HashMap<String, NetworkInformation> networksInformation = new HashMap<String, NetworkInformation>();
        if (ssh == null) {
            LinkedList<Adapter> adapters = virtualMachineAdapters(virtualMachineName).getAdapters();
            for (Adapter adapter : adapters) {
                networksInformation.put(adapter.getNumber().toString(), new NetworkInformation(adapter));
            }
        } else {
            Ssh.CommandExecuter macCommand = ssh.new CommandExecuter("ip -o -0 addr show | awk -F '[ ]+' '{print $2 \" \" $15}'");
            for (String row : macCommand.getResponseRows()) {
                String[] rowSplit = row.split(" ");
                rowSplit[0] = rowSplit[0].substring(0, rowSplit[0].length() - 1);
                Adapter mac = new Adapter(rowSplit[1]);
                networksInformation.put(rowSplit[0], new NetworkInformation(mac, rowSplit[0]));
            }

            Ssh.CommandExecuter ipv4Command = ssh.new CommandExecuter("ip -o -4 addr show | awk -F '[ ]+' '{print $2 \" \" $4}'");
            for (String row : ipv4Command.getResponseRows()) {
                String[] rowSplit = row.split(" ");
                networksInformation.get(rowSplit[0]).addIPv4(rowSplit[1]);
            }

            Ssh.CommandExecuter ipv6Command = ssh.new CommandExecuter("ip -o -6 addr show | awk -F '[ ]+' '{print $2 \" \" $4}'");
            for (String row : ipv6Command.getResponseRows()) {
                String[] rowSplit = row.split(" ");
                networksInformation.get(rowSplit[0]).addIPv6(rowSplit[1]);
            }

            LinkedList<Adapter> adapters = virtualMachineAdapters(virtualMachineName).getAdapters();
            for (Adapter adapter : adapters) {
                for (Map.Entry<String, NetworkInformation> entry : networksInformation.entrySet()) {
                    if (Objects.equals(adapter.getMac(), entry.getValue().getAdapter().getMac())) {
                        entry.getValue().setAdapter(adapter);
                        break;
                    }
                }
            }
        }
        return new NetworksInformation(networksInformation);
    }

    public static void addIP(Ssh ssh, String ip, String link) throws SshExceptions, VirtualMachineExceptions {
        Ssh.CommandExecuter command = ssh.new CommandExecuter("ip addr add \"" + ip + "\" dev \"" + link + "\"");
        if (command.getResponseRows().size() == 0 && command.getErrorResponseRows().size() == 0) {
            //ip added
        } else if (command.getErrorResponseRows().get(0).equals(invalidNetworkLink(link))) {
            throw new VirtualMachineExceptions("Wrong network link", VirtualMachineExceptions.INVALID_NETWORK_LINK);
        } else if (command.getErrorResponseRows().get(0).equals(ipAlreadyExist(ip))) {
            throw new VirtualMachineExceptions("Cannot add existing IP", VirtualMachineExceptions.IP_ALREADY_EXIST);
        } else if (command.getErrorResponseRows().get(0).equals(invalidIPv4(ip)) || command.getErrorResponseRows().get(0).equals(invalidIPv6(ip))) {
            throw new VirtualMachineExceptions("Cannot add invalid IP", VirtualMachineExceptions.INVALID_IP);
        } else if (command.getErrorResponseRows().get(0).equals(cannotAssignIP())) {
            throw new VirtualMachineExceptions("Cannot add IP", VirtualMachineExceptions.CANNOT_ADD_IP);
        } else {
            throw new VirtualMachineExceptions(command.getAllResponse(), VirtualMachineExceptions.UNEXPECTED);
        }
    }

    public static void removeIP(Ssh ssh, String ip, String link) throws SshExceptions, VirtualMachineExceptions {
        Ssh.CommandExecuter command = ssh.new CommandExecuter("ip addr del \"" + ip + "\" dev \"" + link + "\"");
        if (command.getResponseRows().size() == 0 && command.getErrorResponseRows().size() == 0) {
            //ip added
        } else if (command.getErrorResponseRows().get(0).equals(invalidNetworkLink(link))) {
            throw new VirtualMachineExceptions("Wrong network link", VirtualMachineExceptions.INVALID_NETWORK_LINK);
        } else if (command.getErrorResponseRows().get(0).equals(cannotAssignIP())) {
            throw new VirtualMachineExceptions("IP don't exist", VirtualMachineExceptions.IP_DONT_EXIST);
        } else if (command.getErrorResponseRows().get(0).equals(invalidIPv4(ip)) || command.getErrorResponseRows().get(0).equals(invalidIPv6(ip))) {
            throw new VirtualMachineExceptions("Cannot remove invalid IP", VirtualMachineExceptions.INVALID_IP);
        } else {
            throw new VirtualMachineExceptions(command.getAllResponse(), VirtualMachineExceptions.UNEXPECTED);
        }
    }


    public static String invalidNetworkLink(String link) {
        return "Cannot find device \"" + link + "\"";
    }

    public static String ipAlreadyExist(String ip) {
        return "RTNETLINK answers: File exists";
    }

    public static String invalidIPv4(String ip) {
        return "Error: inet prefix is expected rather than \"" + ip + "\"";
    }

    public static String invalidIPv6(String ip) {
        return "Error: inet6 prefix is expected rather than \"" + ip + "\"";
    }

    public static String cannotAssignIP() {
        return "RTNETLINK answers: Cannot assign requested address";
    }
}
