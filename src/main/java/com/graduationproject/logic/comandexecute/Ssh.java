package com.graduationproject.logic.comandexecute;

import com.graduationproject.logic.Interfaces.SshExceptionSender;
import com.graduationproject.logic.Interfaces.StringSender;
import com.graduationproject.logic.exceptions.SshExceptions;
import com.jcraft.jsch.*;
import lombok.Getter;
import lombok.Setter;

import java.io.*;

public class Ssh {
    public static final int TIMEOUT = 25000;

    private Session session;
    private String username;
    private String password;
    private String host;

    public Ssh(String username, String password, String host) throws SshExceptions {
        this.username = username;
        this.password = password;
        this.host = host;
        connect();
    }

    public void connect() throws SshExceptions {
        long start = System.currentTimeMillis();
        boolean inTimeout;
        do {
            inTimeout = System.currentTimeMillis() - start < TIMEOUT;
            try {
                session = new JSch().getSession(username, host, 22);
                session.setTimeout(TIMEOUT);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                System.out.println("SSH connected to \"" + host + "\"");//TODO use logger
                return;
            } catch (JSchException e) {
                if (e.getMessage().equals("java.net.NoRouteToHostException: No route to host (Host unreachable)")) {
                    if (!inTimeout)
                        throw new SshExceptions(e.getMessage(), SshExceptions.SSH_WRONG_HOST, e);
                } else if (e.getMessage().equals("Auth fail")) {
                    throw new SshExceptions(e.getMessage(), SshExceptions.SSH_WRONG_AUTHORISATION, e);
                } else if (e.getMessage().equals("timeout: socket is not established")) {
                    throw new SshExceptions(e.getMessage(), SshExceptions.SSH_TIMEOUT, e);
                } else {
                    throw new SshExceptions(e.getMessage(), SshExceptions.UNEXPECTED, e);
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);
    }

    public void disconnect() {
        if (session != null)
            session.disconnect();
        session = null;
    }

    public class CommandWaitingResponse extends Thread {//TODO stop thread
        @Getter
        @Setter
        private String deviceName;
        @Getter
        @Setter
        private String command;
        private ChannelShell channel;
        private StringSender newLine;
        private SshExceptionSender sshException;

        public CommandWaitingResponse(String deviceName, String command, StringSender newLine, SshExceptionSender sshException) {
            this.deviceName = deviceName;
            this.command = command;
            this.newLine = newLine;
            this.sshException = sshException;
        }

        @Override
        public void run() {
            try {
                commandRunner();
            } catch (SshExceptions e) {
                sshException.send(e);
            }
        }


        public void commandRunner() throws SshExceptions {
            boolean contin;
            boolean connect = false;
            do {
                contin = false;
                try {
                    if (channel != null && !channel.isClosed())
                        channel.disconnect();
                    channel = (ChannelShell) session.openChannel("shell");
                    InputStream in = channel.getInputStream();
                    PrintStream out = new PrintStream(channel.getOutputStream());
                    channel.connect();


                    byte[] buffer = new byte[1024];
                    String line = "";
                    boolean commandEnd = false;
                    boolean commandSend = false;
                    while (true) {
                        while (in.available() > 0) {
                            int i = in.read(buffer, 0, 1024);
                            if (i < 0) {
                                break;
                            }
                            line = new String(buffer, 0, i);
                            String[] lines = line.split("\r\n");
                            for (int j = 0; j < lines.length; j++) {
                                if (lines[j].contains(username + "@" + deviceName + ":~#")) {
                                    if (!commandSend) {
                                        out.println(command);
                                        out.flush();
                                        commandSend = true;
                                    } else {
                                        commandEnd = true;
                                        break;
                                    }
                                } else if (commandSend && !lines[j].contains(command)) {
                                    newLine.send(lines[j]);
                                   /* if (lines[j].contains("rint $1\": \"$3\" \"$4\" \"$5\" \"$6}'")) {//TODO strange chars
                                        System.out.println("startbuffer");
                                        for (int k = 0; k < buffer.length; k++) {
                                            System.out.println((char)buffer[k]);
                                            System.out.println(buffer[k]);
                                        }
                                        System.out.println("endbuffer");
                                        System.out.println(line);
                                    }*/
                                }
                            }
                            if (commandEnd)
                                break;
                        }


                        if (commandEnd)
                            break;
                        if (line.contains("logout"))
                            break;
                        if (channel.isClosed())
                            break;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    }
                    if (!channel.isClosed())
                        channel.disconnect();


                } catch (JSchException e) {
                    if (e.getMessage().equals("session is down") || e.getMessage().equals("channel is not opened.")) {
                        if (!connect) {
                            contin = true;
                            connect = true;
                            connect();
                        } else {
                            throw new SshExceptions("Session is down", SshExceptions.SESSION_IS_DOWN, e);
                        }
                    } else {
                        throw new SshExceptions(e.getMessage(), SshExceptions.UNEXPECTED, e);
                    }
                } catch (IOException e) {
                    throw new SshExceptions(e.getMessage(), SshExceptions.IO_EXCEPTION);
                }
            } while (contin);
        }
    }

    public class CommandExecuter extends Command {

        public CommandExecuter(String command) throws SshExceptions {
            boolean contin;
            boolean connect = false;
            do {
                contin = false;
                try {
                    ChannelExec channel = (ChannelExec) session.openChannel("exec");
                    channel.setCommand(command);
                    InputStream in = channel.getInputStream();
                    InputStream err = channel.getErrStream();
                    channel.connect();
                    ArrayListStringConsumer consumer = new ArrayListStringConsumer();
                    ArrayListStringConsumer errorConsumer = new ArrayListStringConsumer();
                    new BufferedReader(new InputStreamReader(in)).lines().forEach(consumer);
                    new BufferedReader(new InputStreamReader(err)).lines().forEach(errorConsumer);
                    channel.disconnect();
                    setCommand(command, consumer.getValues(), errorConsumer.getValues());
                } catch (JSchException e) {
                    if (e.getMessage().equals("session is down") || e.getMessage().equals("channel is not opened.")) {
                        if (!connect) {
                            contin = true;
                            connect = true;
                            connect();
                        } else {
                            throw new SshExceptions("Session is down", SshExceptions.SESSION_IS_DOWN, e);
                        }
                    } else {
                        throw new SshExceptions(e.getMessage(), SshExceptions.UNEXPECTED, e);
                    }
                } catch (IOException e) {
                    throw new SshExceptions("Problem with comand execution", SshExceptions.IO_EXCEPTION, e);
                }
            } while (contin);
        }
    }
}
