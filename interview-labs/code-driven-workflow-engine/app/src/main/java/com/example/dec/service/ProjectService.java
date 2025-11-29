package com.example.dec.service;

import com.example.dec.domain.Project;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {
  private final JdbcTemplate jdbc;
  private final RowMapper<Project> mapper = (rs, i) -> {
    Project p = new Project();
    p.setId(rs.getString("id"));
    p.setProjectCode(rs.getString("project_code"));
    p.setBuType(rs.getString("bu_type"));
    p.setProjectType(rs.getString("project_type"));
    p.setStatus(rs.getString("status"));
    return p;
  };

  public ProjectService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  public Project create(String projectCode, String buType, String projectType, String status) {
    String id = UUID.randomUUID().toString();
    jdbc.update("insert into project(id, project_code, bu_type, project_type, status) values (?,?,?,?,?)",
      id, projectCode, buType, projectType, status);
    Project p = new Project();
    p.setId(id); p.setProjectCode(projectCode); p.setBuType(buType); p.setProjectType(projectType); p.setStatus(status);
    return p;
  }

  public Project get(String id) {
    List<Project> list = jdbc.query("select * from project where id=?", mapper, id);
    return list.isEmpty()? null : list.get(0);
  }
}
