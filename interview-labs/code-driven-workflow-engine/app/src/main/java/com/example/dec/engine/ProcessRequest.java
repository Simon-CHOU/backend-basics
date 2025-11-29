package com.example.dec.engine;

import java.util.Map;

public class ProcessRequest {
  private String operator;
  private String taskId;
  private String processId;
  private WorkflowResultType result;
  private Map<String, Object> meta;

  public String getOperator() { return operator; }
  public void setOperator(String operator) { this.operator = operator; }
  public String getTaskId() { return taskId; }
  public void setTaskId(String taskId) { this.taskId = taskId; }
  public String getProcessId() { return processId; }
  public void setProcessId(String processId) { this.processId = processId; }
  public WorkflowResultType getResult() { return result; }
  public void setResult(WorkflowResultType result) { this.result = result; }
  public Map<String, Object> getMeta() { return meta; }
  public void setMeta(Map<String, Object> meta) { this.meta = meta; }
}
