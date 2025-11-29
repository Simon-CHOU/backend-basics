package com.example.dec.service;

import com.example.dec.domain.ProjectTask;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectTaskService {
  private final JdbcTemplate jdbc;
  private final RowMapper<ProjectTask> mapper = (rs, i) -> {
    ProjectTask t = new ProjectTask();
    t.setId(rs.getString("id"));
    t.setProcessId(rs.getString("process_id"));
    t.setProcessNodeCode(rs.getString("process_node_code"));
    t.setAssignRoleName(rs.getString("assign_role_name"));
    t.setAssignRoleId(rs.getString("assign_role_id"));
    t.setStatus(rs.getString("status"));
    t.setResult(rs.getString("result"));
    t.setMeta(rs.getString("meta"));
    return t;
  };

  public ProjectTaskService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  public ProjectTask create(String processId, String nodeCode, String roleName, String roleId) {
    String id = UUID.randomUUID().toString();
    jdbc.update("insert into project_task(id, process_id, process_node_code, assign_role_name, assign_role_id, status) values (?,?,?,?,?,?)",
      id, processId, nodeCode, roleName, roleId, "PENDING");
    List<ProjectTask> list = jdbc.query("select * from project_task where id=?", mapper, id);
    return list.get(0);
  }

  public ProjectTask completeTask(String operator, String taskId, String result, String meta) {
    jdbc.update("update project_task set result=?, status=?, meta=? where id=?",
      result, "PENDING", meta, taskId);
    List<ProjectTask> list = jdbc.query("select * from project_task where id=?", mapper, taskId);
    return list.isEmpty()? null : list.get(0);
  }

  public List<ProjectTask> findByProcess(String processId) {
    return jdbc.query("select * from project_task where process_id=?", mapper, processId);
  }

  public ProjectTask get(String id) {
    List<ProjectTask> list = jdbc.query("select * from project_task where id=?", mapper, id);
    return list.isEmpty()? null : list.get(0);
  }
}
