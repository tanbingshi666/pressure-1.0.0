package com.skysec;

import com.skysec.io.ReadFile;
import com.skysec.kafka.KafkaSink;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;

public class ApplicationKafka {
    public static void main(String[] args) {

        Option path = new Option("path", "path", true, "数据路径");
        Option bootstrapServers = new Option("bootstrapServers", "bootstrapServers", true, "kafka 连接地址");
        Option topic = new Option("topic", "topic", true, "主题");
        Option acks = new Option(ProducerConfig.ACKS_CONFIG, "acks", true, "acks 参数");

        Options options = new Options();
        options.addOption(path);
        options.addOption(bootstrapServers);
        options.addOption(topic);
        options.addOption(acks);

        CommandLine commandLine;
        DefaultParser parser = new DefaultParser();

        KafkaSink kafkaSink = null;
        try {
            commandLine = parser.parse(options, args);
            String tarPath = commandLine.getOptionValue("path");

            String tarBootstrapServers = commandLine.getOptionValue("bootstrapServers");
            String tarTopic = commandLine.getOptionValue("topic");
            String tarAcks = commandLine.getOptionValue("acks");

            Properties properties = new Properties();
            properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, tarBootstrapServers);
            properties.setProperty(ProducerConfig.ACKS_CONFIG, tarAcks);

            ReadFile readFile = new ReadFile(tarPath);

            kafkaSink = new KafkaSink(tarTopic, readFile, properties);
            kafkaSink.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (kafkaSink != null) {
            kafkaSink.close();
        }

    }

}
