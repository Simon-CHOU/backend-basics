package com.example.dec.facade;

import com.example.dec.constant.ProcessConstants;
import com.example.dec.constant.ProcessNodeConstants;
import com.example.dec.constant.WorkflowConstants;
import com.example.dec.domain.ProjectProcess;
import com.example.dec.domain.ProjectTask;
import com.example.dec.engine.ProjectTaskFacade;
import com.example.dec.engine.TaskEngine;
import com.example.dec.service.ProjectProcessService;
import com.example.dec.service.ProjectTaskService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EAOverseasProjectFacadeImpl implements EAOverseasProjectFacade {
  private final ProjectProcessService processService;
  private final ProjectTaskService taskService;
  private final ProjectTaskFacade taskFacade;

  public EAOverseasProjectFacadeImpl(ProjectProcessService processService, ProjectTaskService taskService, ProjectTaskFacade taskFacade) {
    this.processService = processService;
    this.taskService = taskService;
    this.taskFacade = taskFacade;
  }

  @Override
  public ProjectProcess startConflictProcess(String projectId) {
    ProjectProcess pp = processService.create(projectId, ProcessConstants.EA_OVERSEAS_CONFLICT, WorkflowConstants.PENDING);
    TaskEngine start = taskFacade.getStartEngine();
    taskService.create(pp.getId(), ProcessNodeConstants.EA_OVERSEAS_CONFLICT_START, null, null);
    taskService.create(pp.getId(), ProcessNodeConstants.EA_OVERSEAS_CONFLICT_CHINA_SALES, null, null);
    taskService.create(pp.getId(), ProcessNodeConstants.EA_OVERSEAS_CONFLICT_PAE, null, null);
    return pp;
  }

  @Override
  public List<ProjectTask> listTasks(String processId) {
    return taskService.findByProcess(processId);
  }
}
