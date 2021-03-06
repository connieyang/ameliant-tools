package com.ameliant.tools.kafka.perftool.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author jkorab
 */
public class TestProfileDefinition extends Configurable {

    /**
     * Maximum test duration in seconds. Applies to concurrent tests only.
     */
    private int maxDuration = 30;
    /**
     * Whether producers and consumers execute concurrently. If false, producers will executed before consumers.
     */
    private boolean concurrent = true;

    /**
     * Whether or not the test should use an auto-generated topic name. If true, consumers and producers will all
     * use the auto-generated one in preference to any defined within their config during this run.
     */
    private boolean autogenerateTopic = false;

    @JsonManagedReference
    private ProducersDefinition producers = new ProducersDefinition();

    @JsonManagedReference
    private ConsumersDefinition consumers = new ConsumersDefinition();

    public boolean isConcurrent() {
        return concurrent;
    }

    public void setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
    }

    public ConsumersDefinition getConsumers() {
        return consumers;
    }

    public void setConsumers(ConsumersDefinition consumers) {
        this.consumers = consumers;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    public ProducersDefinition getProducers() {
        return producers;
    }

    public void setProducers(ProducersDefinition producers) {
        this.producers = producers;
    }

    public boolean isAutogenerateTopic() {
        return autogenerateTopic;
    }

    public void setAutogenerateTopic(boolean autogenerateTopic) {
        this.autogenerateTopic = autogenerateTopic;
    }

    @JsonIgnore
    private String _autogeneratedTopic; // lazily initialised

    @Override
    public String getTopic() {
        if (autogenerateTopic) {
            if (_autogeneratedTopic == null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                _autogeneratedTopic = "kafka-perf-test_" + LocalDateTime.now().format(formatter);
            }
            return _autogeneratedTopic;
        } else {
            return super.getTopic();
        }
    }

}
