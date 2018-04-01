package com.graduationproject.logic.comandexecute;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class LinuxCommandExecuter extends Command {

    public LinuxCommandExecuter(String command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", command);
        builder.directory(new File(System.getProperty("user.home")));
        Process process = builder.start();
        ArrayListStringConsumer consumer = new ArrayListStringConsumer();
        ArrayListStringConsumer errorConsumer = new ArrayListStringConsumer();
        new BufferedReader(new InputStreamReader(process.getInputStream())).lines()
                .forEach(consumer);
        new BufferedReader(new InputStreamReader(process.getErrorStream())).lines()
                .forEach(errorConsumer);
        setCommand(command,consumer.getValues(),errorConsumer.getValues());
    }

}
