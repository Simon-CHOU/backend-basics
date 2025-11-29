package com.example.dec.task.ea_overseas_conflict;

import com.example.dec.constant.ProcessNodeConstants;
import com.example.dec.constant.RoleConstants;
import com.example.dec.constant.WorkflowConstants;
import com.example.dec.domain.ProjectTask;
import com.example.dec.engine.AbstractTaskEngine;
import com.example.dec.engine.ProcessRequest;
import com.example.dec.engine.ProcessResponse;
import com.example.dec.engine.TaskEngine;
import com.example.dec.engine.ProcessEngine;
import com.example.dec.service.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(ProcessNodeConstants.EA_OVERSEAS_CONFLICT_PAE)
public class EaOverseasConflictPAEManagerTaskEngine extends AbstractTaskEngine {
  @Autowired private ProcessEngine processEngine;
  @Autowired private ProjectTaskService taskService;

  @Override
  public void complete(ProcessRequest req, ProcessResponse resp) {
    super.complete(req, resp);
    List<ProjectTask> tasks = taskService.findByProcess(req.getProcessId());
    ProjectTask pae = tasks.stream().filter(t -> ProcessNodeConstants.EA_OVERSEAS_CONFLICT_PAE.equals(t.getProcessNodeCode())).findFirst().orElse(null);
    ProjectTask cs = tasks.stream().filter(t -> ProcessNodeConstants.EA_OVERSEAS_CONFLICT_CHINA_SALES.equals(t.getProcessNodeCode())).findFirst().orElse(null);
    if (pae != null && pae.getResult() != null && cs != null && cs.getResult() != null) {
      if ("APPROVED".equals(pae.getResult()) && "APPROVED".equals(cs.getResult())) {
        processEngine.endApproved(req.getProcessId());
      } else if ("REJECTED".equals(pae.getResult()) && "REJECTED".equals(cs.getResult())) {
        processEngine.endRejected(req.getProcessId());
      } else {
        taskService.create(req.getProcessId(), ProcessNodeConstants.EA_OVERSEAS_CONFLICT_RESUBMIT, RoleConstants.STARTER_ROLE, null);
      }
    }
  }

  @Override
  public java.util.List<String> next(ProcessRequest req, ProcessResponse resp) { return java.util.Collections.emptyList(); }

  @Override
  public String getTaskAssignRoleCode(ProcessRequest req) { return RoleConstants.EA_PAE_MANAGER; }

  @Override
  public String getNodeCode() { return ProcessNodeConstants.EA_OVERSEAS_CONFLICT_PAE; }
}
