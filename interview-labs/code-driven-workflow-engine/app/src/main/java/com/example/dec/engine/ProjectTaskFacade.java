package com.example.dec.engine;

import com.example.dec.constant.ProcessNodeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ProjectTaskFacade {
  @Autowired private ApplicationContext ctx;

  public TaskEngine getStartEngine() {
    return (TaskEngine) ctx.getBean(ProcessNodeConstants.EA_OVERSEAS_CONFLICT_START);
  }

  public TaskEngine getEngine(String nodeBeanName) {
    return (TaskEngine) ctx.getBean(nodeBeanName);
  }
}
