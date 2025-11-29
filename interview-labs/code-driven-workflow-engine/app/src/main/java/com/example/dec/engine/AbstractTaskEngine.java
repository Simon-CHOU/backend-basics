package com.example.dec.engine;

import com.example.dec.constant.WorkflowConstants;
import com.example.dec.domain.ProjectTask;
import com.example.dec.service.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractTaskEngine implements TaskEngine {
  @Autowired protected ProjectTaskService projectTaskService;

  @Override
  public void complete(ProcessRequest req, ProcessResponse resp) {
    String meta = req.getMeta() == null ? null : req.getMeta().toString();
    ProjectTask projectTask = projectTaskService.completeTask(req.getOperator(), req.getTaskId(),
      req.getResult().name(), meta);
    resp.setStatus(WorkflowConstants.PENDING);
  }
}
