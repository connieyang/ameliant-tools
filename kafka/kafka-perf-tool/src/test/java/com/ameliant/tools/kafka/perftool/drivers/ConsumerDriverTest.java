package com.ameliant.tools.kafka.perftool.drivers;

import com.ameliant.tools.kafka.testdsl.config.ConsumerConfigsBuilder;
import com.ameliant.tools.kafka.perftool.config.ConsumerDefinition;
import com.ameliant.tools.kafka.testdsl.config.ProducerConfigsBuilder;
import com.ameliant.tools.kafka.perftool.config.ProducerDefinition;
import com.ameliant.tools.kafka.testdsl.EmbeddedKafkaBroker;
import com.ameliant.tools.zookeeper.testdsl.EmbeddedZooKeeper;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jkorab
 */
public class ConsumerDriverTest {

    @Rule
    public EmbeddedZooKeeper zooKeeper = new EmbeddedZooKeeper();

    @Rule
    public EmbeddedKafkaBroker broker = EmbeddedKafkaBroker.builder()
            .zookeeperConnect("127.0.0.1:" + zooKeeper.getPort())
            //.logFlushIntervalMessages(1)
            .build();

    @Test
    public void testReceive_sequential() throws InterruptedException {
        String topic = "foo";
        int messageCount = 1000;

        CountDownLatch latch = new CountDownLatch(2);

        // fill up the topic
        ProducerDriver producerDriver = createProducerDriver(latch, topic, messageCount);
        producerDriver.run();

        ConsumerDriver consumerDriver = createConsumerDriver(latch, topic, messageCount);
        consumerDriver.run();

        assertEquals(messageCount, consumerDriver.getMessagesReceived());
    }

    @Test
    public void testReceive_parallel() throws InterruptedException {
        String topic = "foo";
        int messageCount = 1000;

        int numWorkers = 2;
        CountDownLatch latch = new CountDownLatch(numWorkers);
        ExecutorService executorService = Executors.newFixedThreadPool(numWorkers);

        // fill up the topic
        ConsumerDriver consumerDriver = createConsumerDriver(latch, topic, messageCount);
        executorService.submit(consumerDriver);

        ProducerDriver producerDriver = createProducerDriver(latch, topic, messageCount);
        executorService.submit(producerDriver);

        latch.await();
    }

    private ConsumerDriver createConsumerDriver(CountDownLatch latch, String topic, int messageCount) {
        Map<String, Object> configs = new ConsumerConfigsBuilder()
                .groupId("bar")
                .bootstrapServers("127.0.0.1:" + broker.getPort())
                .enableAutoCommit(true)
                .autoCommitIntervalMs(1000)
                .sessionTimeoutMs(30000)
                .keyDeserializer(ByteArrayDeserializer.class)
                .valueDeserializer(ByteArrayDeserializer.class)
                .autoOffsetReset(ConsumerConfigsBuilder.OffsetReset.earliest)
                .build();

        ConsumerDefinition consumerDefinition = new ConsumerDefinition();
        consumerDefinition.setConfig(configs);
        consumerDefinition.setTopic(topic);
        consumerDefinition.setMessagesToReceive(messageCount);
        return new ConsumerDriver(consumerDefinition, latch);
    }

    public ProducerDriver createProducerDriver(CountDownLatch latch, String topic, int messageCount) {
        Map<String, Object> producerConfigs = new ProducerConfigsBuilder()
                .bootstrapServers("127.0.0.1:" + broker.getPort())
                .requestRequiredAcks(ProducerConfigsBuilder.RequestRequiredAcks.ackFromLeader)
                .producerType(ProducerConfigsBuilder.ProducerType.sync)
                .keySerializer(ByteArraySerializer.class)
                .valueSerializer(ByteArraySerializer.class)
                .batchSize(0)
                .build();

        ProducerDefinition producerDefinition = new ProducerDefinition();
        producerDefinition.setConfig(producerConfigs);
        producerDefinition.setTopic(topic);
        producerDefinition.setMessageSize(100 * 1024);
        producerDefinition.setMessagesToSend(messageCount);
        producerDefinition.setSendBlocking(true);

        return new ProducerDriver(producerDefinition, latch);
    }

}