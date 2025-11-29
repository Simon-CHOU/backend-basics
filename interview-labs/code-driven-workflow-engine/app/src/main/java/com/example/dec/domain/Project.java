package com.example.dec.domain;

public class Project {
  private String id;
  private String projectCode;
  private String buType;
  private String projectType;
  private String status;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getProjectCode() { return projectCode; }
  public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
  public String getBuType() { return buType; }
  public void setBuType(String buType) { this.buType = buType; }
  public String getProjectType() { return projectType; }
  public void setProjectType(String projectType) { this.projectType = projectType; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
