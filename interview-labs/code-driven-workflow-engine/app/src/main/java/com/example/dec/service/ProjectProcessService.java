package com.example.dec.service;

import com.example.dec.domain.ProjectProcess;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectProcessService {
  private final JdbcTemplate jdbc;
  private final RowMapper<ProjectProcess> mapper = (rs, i) -> {
    ProjectProcess p = new ProjectProcess();
    p.setId(rs.getString("id"));
    p.setProjectId(rs.getString("project_id"));
    p.setProcessCode(rs.getString("process_code"));
    p.setStatus(rs.getString("status"));
    return p;
  };

  public ProjectProcessService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  public ProjectProcess create(String projectId, String processCode, String status) {
    String id = UUID.randomUUID().toString();
    jdbc.update("insert into project_process(id, project_id, process_code, status) values (?,?,?,?)",
      id, projectId, processCode, status);
    ProjectProcess p = new ProjectProcess();
    p.setId(id); p.setProjectId(projectId); p.setProcessCode(processCode); p.setStatus(status);
    return p;
  }

  public void updateStatus(String processId, String status) {
    jdbc.update("update project_process set status=? where id=?", status, processId);
  }

  public ProjectProcess get(String id) {
    List<ProjectProcess> list = jdbc.query("select * from project_process where id=?", mapper, id);
    return list.isEmpty()? null : list.get(0);
  }
}
