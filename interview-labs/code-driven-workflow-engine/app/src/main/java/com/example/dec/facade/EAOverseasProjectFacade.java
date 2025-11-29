package com.example.dec.facade;

import com.example.dec.domain.ProjectProcess;
import com.example.dec.domain.ProjectTask;

import java.util.List;

public interface EAOverseasProjectFacade {
  ProjectProcess startConflictProcess(String projectId);
  List<ProjectTask> listTasks(String processId);
}
