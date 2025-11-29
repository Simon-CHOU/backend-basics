package com.example.dec.domain;

public class ProjectTask {
  private String id;
  private String processId;
  private String processNodeCode;
  private String assignRoleName;
  private String assignRoleId;
  private String status;
  private String result;
  private String meta;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getProcessId() { return processId; }
  public void setProcessId(String processId) { this.processId = processId; }
  public String getProcessNodeCode() { return processNodeCode; }
  public void setProcessNodeCode(String processNodeCode) { this.processNodeCode = processNodeCode; }
  public String getAssignRoleName() { return assignRoleName; }
  public void setAssignRoleName(String assignRoleName) { this.assignRoleName = assignRoleName; }
  public String getAssignRoleId() { return assignRoleId; }
  public void setAssignRoleId(String assignRoleId) { this.assignRoleId = assignRoleId; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getResult() { return result; }
  public void setResult(String result) { this.result = result; }
  public String getMeta() { return meta; }
  public void setMeta(String meta) { this.meta = meta; }
}
