package com.example.dec.engine;

import com.example.dec.constant.ProcessNodeConstants;
import com.example.dec.constant.RoleConstants;
import com.example.dec.constant.WorkflowConstants;
import com.example.dec.domain.ProjectTask;
import com.example.dec.service.ProjectProcessService;
import com.example.dec.service.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessEngine {
  @Autowired private ProjectTaskService projectTaskService;
  @Autowired private ProjectProcessService projectProcessService;
  @Autowired private ApplicationContext ctx;

  public ProjectTask createTaskAndAssign(TaskEngine engine, String processId) {
    String role = engine.getTaskAssignRoleCode(null);
    return projectTaskService.create(processId, engine.getNodeCode(), role, null);
  }

  public List<ProjectTask> createStartParallelTasks(String processId) {
    List<ProjectTask> tasks = new ArrayList<>();
    tasks.add(projectTaskService.create(processId, ProcessNodeConstants.EA_OVERSEAS_CONFLICT_CHINA_SALES,
      RoleConstants.EA_CHINA_SALES_MANAGER, null));
    tasks.add(projectTaskService.create(processId, ProcessNodeConstants.EA_OVERSEAS_CONFLICT_PAE,
      RoleConstants.EA_PAE_MANAGER, null));
    return tasks;
  }

  public void endApproved(String processId) {
    projectProcessService.updateStatus(processId, WorkflowConstants.APPROVED);
  }

  public void endRejected(String processId) {
    projectProcessService.updateStatus(processId, WorkflowConstants.REJECTED);
  }

  public TaskEngine getEngineByNode(String nodeBeanName) {
    return (TaskEngine) ctx.getBean(nodeBeanName);
  }
}
