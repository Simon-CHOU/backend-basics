package com.example.dec.task.ea_overseas_conflict;

import com.example.dec.constant.ProcessNodeConstants;
import com.example.dec.constant.RoleConstants;
import com.example.dec.engine.AbstractTaskEngine;
import com.example.dec.engine.ProcessRequest;
import com.example.dec.engine.ProcessResponse;
import com.example.dec.engine.TaskEngine;
import com.example.dec.engine.WorkflowResultType;
import com.example.dec.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component(ProcessNodeConstants.EA_OVERSEAS_CONFLICT_START)
public class EaOverseasConflictStartTaskEngine extends AbstractTaskEngine {
  @Autowired private ProcessEngine processEngine;

  @Override
  public void start(ProcessRequest req, ProcessResponse resp) { }

  @Override
  public List<String> next(ProcessRequest req, ProcessResponse resp) {
    return Arrays.asList(ProcessNodeConstants.EA_OVERSEAS_CONFLICT_CHINA_SALES,
      ProcessNodeConstants.EA_OVERSEAS_CONFLICT_PAE);
  }

  @Override
  public String getTaskAssignRoleCode(ProcessRequest req) { return RoleConstants.STARTER_ROLE; }

  @Override
  public String getNodeCode() { return ProcessNodeConstants.EA_OVERSEAS_CONFLICT_START; }
}
