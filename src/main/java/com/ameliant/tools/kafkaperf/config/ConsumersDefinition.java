package com.ameliant.tools.kafkaperf.config;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jkorab
 */
public class ConsumersDefinition {

    private Map<String, Object> config = new HashMap<>();
    private List<ConsumerDefinition> instances = new ArrayList<>();

    @JsonBackReference
    private TestProfileDefinition testProfileDefinition;

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public List<ConsumerDefinition> getInstances() {
        return instances;
    }

    public void setInstances(List<ConsumerDefinition> instances) {
        this.instances = instances;
    }

    public TestProfileDefinition getTestProfileDefinition() {
        return testProfileDefinition;
    }

    public void setTestProfileDefinition(TestProfileDefinition testProfileDefinition) {
        this.testProfileDefinition = testProfileDefinition;
    }

    @JsonIgnore
    public Map<String, Object> getMergedConfig() {
        return ConfigMerger.merge(testProfileDefinition.getConfig(), config);
    }

}
