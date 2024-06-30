package com.aws.cloudx_tasks.iam_task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PolicyStatementDTO {
    private String effect;
    private Object action;

    private String resource;

    @JsonProperty("Effect")
    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = effect; }

    @JsonProperty("Resource")
    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @JsonProperty("Action")
    public Object getAction() { return action; }
}


