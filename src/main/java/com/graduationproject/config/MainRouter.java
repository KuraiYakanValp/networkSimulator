package com.graduationproject.config;


import com.graduationproject.logic.Mac;
import com.graduationproject.logic.VirtualBoxStaticEvents;
import com.graduationproject.logic.comandexecute.Ssh;
import com.graduationproject.logic.exceptions.MainRouterExceptions;
import com.graduationproject.logic.exceptions.SshExceptions;
import com.graduationproject.logic.exceptions.VirtualBoxExceptions;
import com.graduationproject.logic.exceptions.VirtualMachineExceptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.ArrayList;

@Configuration
public class MainRouter extends VirtualBoxStaticEvents {//TODO dhcp time
    public static final String IP = "192.168.56.1";
    public static final String NETWORK = "192.168.56.0/24";
    private static final String VIRTUAL_MACHINE_NAME = virtualMachineNameCreator("main", "router");
    private static final String SSH_USERNAME = "root";
    private static final String SSH_PASSWORD = "q6aRTZy8yMNw2bnquZUgmWeK";
    private static Ssh ssh = null;

    public MainRouter() {
        connectSSH();
    }

    private static void connectSSH() {
        if (ssh != null)
            ssh.disconnect();
        try {
            startVirtualMachine(VIRTUAL_MACHINE_NAME);
        } catch (VirtualBoxExceptions e) {
            if (e.getErrorCode() != VirtualBoxExceptions.VIRTUAL_MACHINE_ALREADY_RUNNING) {
                throw new Error(e);
            }
        }
        try {
            ssh = new Ssh(SSH_USERNAME, SSH_PASSWORD, IP);
        } catch (SshExceptions e) {
            throw new Error(e);
        }
    }

    public static String ipByMac(Mac mac) throws MainRouterExceptions {
        try {
            long start = System.currentTimeMillis();
            do {
                ArrayList<String> dhcpLeases = VirtualBoxStaticEvents.dhcpLeases(ssh);
                for (String dhcpLease : dhcpLeases) {
                    try {
                        String[] dhcpLeaseParts = dhcpLease.split(" ");
                        Mac contolledMac = new Mac(dhcpLeaseParts[1]);
                        if (mac.equals(contolledMac)) {
                            return dhcpLeaseParts[2];
                        }

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (System.currentTimeMillis() - start < Ssh.TIMEOUT);
            throw new MainRouterExceptions("Mac not found.", MainRouterExceptions.MAC_NOT_FOUND);
        } catch (SshExceptions e) {
            throw new MainRouterExceptions(e);
        } catch (VirtualMachineExceptions e) {
            if (e.getErrorCode() == VirtualMachineExceptions.DHCP_LEASES_FILE_NOT_FOUND) {
                throw new MainRouterExceptions(e.getMessage(), MainRouterExceptions.DHCP_LEASES_FILE_NOT_FOUND, e);
            } else {
                throw new MainRouterExceptions(e.getMessage(), MainRouterExceptions.UNEXPECTED, e);
            }
        }
    }

    @PreDestroy
    public void destroy() throws Exception {
        if (ssh != null)
            ssh.disconnect();
        powerOffVirtualMachine(VIRTUAL_MACHINE_NAME);
    }
}
