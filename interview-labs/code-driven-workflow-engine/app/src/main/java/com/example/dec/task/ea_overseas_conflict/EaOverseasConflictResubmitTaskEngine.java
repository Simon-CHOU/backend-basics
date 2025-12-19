package com.example.dec.task.ea_overseas_conflict;

import com.example.dec.constant.ProcessNodeConstants;
import com.example.dec.constant.RoleConstants;
import com.example.dec.engine.AbstractTaskEngine;
import com.example.dec.engine.ProcessRequest;
import com.example.dec.engine.ProcessResponse;
import com.example.dec.engine.TaskEngine;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component(ProcessNodeConstants.EA_OVERSEAS_CONFLICT_RESUBMIT)
public class EaOverseasConflictResubmitTaskEngine extends AbstractTaskEngine {
  @Override
  public void start(ProcessRequest req, ProcessResponse resp) { }

  @Override
  public List<String> next(ProcessRequest req, ProcessResponse resp) { return Collections.emptyList(); }
  @Override
  public String getTaskAssignRoleCode(ProcessRequest req) { return RoleConstants.STARTER_ROLE; }
  @Override
  public String getNodeCode() { return ProcessNodeConstants.EA_OVERSEAS_CONFLICT_RESUBMIT; }
}
