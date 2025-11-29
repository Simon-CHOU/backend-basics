package com.example.dec.domain;

public class ProjectProcess {
  private String id;
  private String projectId;
  private String processCode;
  private String status;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getProjectId() { return projectId; }
  public void setProjectId(String projectId) { this.projectId = projectId; }
  public String getProcessCode() { return processCode; }
  public void setProcessCode(String processCode) { this.processCode = processCode; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
