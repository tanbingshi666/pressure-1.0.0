package com.skysec;

import com.skysec.netty.UdpServer;
import com.skysec.sender.Sender;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Application {
    public static void main(String[] args) {

        Option path = new Option("path", "path", true, "数据路径");
        Option remoteHost = new Option("remoteHost", "remoteHost", true, "远程IP");
        Option remotePort = new Option("remotePort", "remotePort", true, "远程Port");
        Option localHost = new Option("localHost", "localHost", true, "本地IP");
        Option localPort = new Option("localPort", "localPort", true, "本地Port");

        Options options = new Options();
        options.addOption(path);
        options.addOption(remoteHost);
        options.addOption(remotePort);
        options.addOption(localHost);
        options.addOption(localPort);

        CommandLine commandLine;
        DefaultParser parser = new DefaultParser();
        UdpServer udpServer = null;
        try {
            commandLine = parser.parse(options, args);
            String tarPath = commandLine.getOptionValue("path");
            String tarRemoteHost = commandLine.getOptionValue("remoteHost");
            int tarRemotePort = Integer.parseInt(commandLine.getOptionValue("remotePort"));
            String tarLocalHost = commandLine.getOptionValue("localHost", "0.0.0.0");
            int tarLocalPort = Integer.parseInt(commandLine.getOptionValue("localPort", "10000"));

            // --remoteHost 127.0.0.1 --remotePort 10001 --localHost 127.0.0.1 --localPort 10000
            udpServer = new UdpServer(tarLocalHost, tarLocalPort);
            udpServer.startup();

            Sender sender = new Sender(tarPath, tarRemoteHost, tarRemotePort, udpServer);
            sender.send();
        } catch (Exception e) {
            if (udpServer != null) {
                udpServer.shutdown();
            }
            e.printStackTrace();
        }

    }

}
