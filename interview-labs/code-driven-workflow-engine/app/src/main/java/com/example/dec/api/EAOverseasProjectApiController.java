package com.example.dec.api;

import com.example.dec.domain.ProjectProcess;
import com.example.dec.domain.ProjectTask;
import com.example.dec.engine.ProcessRequest;
import com.example.dec.engine.ProcessResponse;
import com.example.dec.engine.WorkflowResultType;
import com.example.dec.engine.ProjectTaskFacade;
import com.example.dec.engine.TaskEngine;
import com.example.dec.facade.EAOverseasProjectFacade;
import com.example.dec.service.ProjectTaskService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EAOverseasProjectApiController {
  private final EAOverseasProjectFacade facade;
  private final ProjectTaskService taskService;
  private final ProjectTaskFacade taskFacade;
  private final com.example.dec.service.ProjectService projectService;

  public EAOverseasProjectApiController(EAOverseasProjectFacade facade, ProjectTaskService taskService, ProjectTaskFacade taskFacade, com.example.dec.service.ProjectService projectService) {
    this.facade = facade;
    this.taskService = taskService;
    this.taskFacade = taskFacade;
    this.projectService = projectService;
  }

  @PostMapping("/ea-overseas/processes/conflict/start")
  public ProjectProcess start(@RequestParam String projectId) {
    return facade.startConflictProcess(projectId);
  }

  @PostMapping("/projects/demo")
  public com.example.dec.domain.Project demoProject() {
    return projectService.create("DEMO-001", "EA", "overseas", "DRAFT");
  }

  @GetMapping("/processes/{processId}/tasks")
  public List<ProjectTask> tasks(@PathVariable String processId) {
    return facade.listTasks(processId);
  }

  @PostMapping(value = "/tasks/{taskId}/complete", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ProjectTask complete(@PathVariable String taskId, @RequestBody Map<String, Object> body) {
    String operator = (String) body.getOrDefault("operator", "user");
    String resultStr = (String) body.get("result");
    WorkflowResultType result = WorkflowResultType.valueOf(resultStr);
    String meta = body.containsKey("meta") ? body.get("meta").toString() : null;
    ProjectTask task = taskService.get(taskId);
    ProcessRequest req = new ProcessRequest();
    req.setOperator(operator);
    req.setTaskId(taskId);
    req.setProcessId(task.getProcessId());
    req.setResult(result);
    ProcessResponse resp = new ProcessResponse();
    TaskEngine engine = taskFacade.getEngine(task.getProcessNodeCode());
    engine.complete(req, resp);
    return taskService.get(taskId);
  }
}
