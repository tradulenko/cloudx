package com.aws.cloudx_tasks.iam_task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PolicyDocumentDTO {
    private String version;
    private List<PolicyStatementDTO> statement;
    @JsonProperty("Version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("Statement")
    public List<PolicyStatementDTO> getStatement() { return statement; }
    public void setStatement(List<PolicyStatementDTO> statement) { this.statement = statement; }
}