package com.skysec.kafka;

import com.skysec.io.ReadFile;
import com.skysec.queue.MessageQueue;
import com.skysec.queue.Termination;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class KafkaSink {

    public static final String DEFAULT_KEY_SERIALIZER =
            "org.apache.kafka.common.serialization.StringSerializer";
    public static final String DEFAULT_VALUE_SERIAIZER =
            "org.apache.kafka.common.serialization.ByteArraySerializer";

    private final KafkaProducer<String, byte[]> producer;

    private final String topic;
    private final List<Future<RecordMetadata>> kafkaFutures = new LinkedList<>();

    private final ReadFile readFile;

    private final AtomicLong sendTime = new AtomicLong(0L);
    private long count = 0L;

    public KafkaSink(String topic,
                     ReadFile readFile,
                     Properties properties) {
        this.topic = topic;
        this.readFile = readFile;
        Properties kafkaProps = new Properties();
        kafkaProps.putAll(properties);

        kafkaProps.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, DEFAULT_KEY_SERIALIZER);
        kafkaProps.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DEFAULT_VALUE_SERIAIZER);
        this.producer = new KafkaProducer<>(kafkaProps);
    }

    public void start() {
        // 异步读取文件到 MessageQueue
        CompletableFuture.runAsync(readFile::read);

        Object obj;
        ProducerRecord<String, byte[]> record;
        while (true) {
            try {
                obj = MessageQueue.take();

                kafkaFutures.clear();
                long startTs = System.currentTimeMillis();
                if (obj instanceof Termination) {
                    producer.close();
                    break;
                } else if (obj instanceof String) {
                    record = new ProducerRecord<>(topic, ((String) obj).getBytes(StandardCharsets.UTF_8));
                    kafkaFutures.add(producer.send(record, (recordMetadata, e) -> {
                        // nothing to do
                    }));
                    count++;
                }

                // 可能受 linger.ms 参数影响
                producer.flush();

                for (Future<RecordMetadata> future : kafkaFutures) {
                    future.get();
                }

                long endTs = System.currentTimeMillis();
                sendTime.addAndGet((endTs - startTs));
            } catch (Exception e) {
                // nothing to do
            }
        }

        System.out.println("发送数据到 kafka 大小为 " + count + " 花费时间为 " + sendTime.get());
    }

    public void close() {
        if (producer != null) {
            producer.close();
        }
    }

}
