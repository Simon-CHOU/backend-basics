```mermaid
  classDiagram
      %% 领域模型层
      class Project {
          -String id
          -String projectCode
          -String buType
          -String projectType
          -String status
          +getId()
          +setId(String id)
          +getProjectCode()
          +setProjectCode(String projectCode)
          +getBuType()
          +setBuType(String buType)
          +getProjectType()
          +setProjectType(String projectType)
          +getStatus()
          +setStatus(String status)
      }

      class ProjectProcess {
          -String id
          -String projectId
          -String processCode
          -String status
          +getId()
          +setId(String id)
          +getProjectId()
          +setProjectId(String projectId)
          +getProcessCode()
          +setProcessCode(String processCode)
          +getStatus()
          +setStatus(String status)
      }

      class ProjectTask {
          -String id
          -String processId
          -String processNodeCode
          -String assignRoleName
          -String assignRoleId
          -String status
          -String result
          -String meta
          +getId()
          +setId(String id)
          +getProcessId()
          +setProcessId(String processId)
          +getProcessNodeCode()
          +setProcessNodeCode(String processNodeCode)
          +getAssignRoleName()
          +setAssignRoleName(String assignRoleName)
          +getAssignRoleId()
          +setAssignRoleId(String assignRoleId)
          +getStatus()
          +setStatus(String status)
          +getResult()
          +setResult(String result)
          +getMeta()
          +setMeta(String meta)
      }

      %% 引擎核心组件
      class ProcessEngine {
          -ProjectTaskService projectTaskService
          -ProjectProcessService projectProcessService
          -ApplicationContext ctx
          +createTaskAndAssign(TaskEngine engine, String processId)
          +createStartParallelTasks(String processId)
          +endApproved(String processId)
          +endRejected(String processId)
          +getEngineByNode(String nodeBeanName)
      }

      class TaskEngine {
          <<interface>>
          +start(ProcessRequest req, ProcessResponse resp)
          +complete(ProcessRequest req, ProcessResponse resp)
          +next(ProcessRequest req, ProcessResponse resp): List~String~
          +getTaskAssignRoleCode(ProcessRequest req): String
          +getNodeCode(): String
      }

      class AbstractTaskEngine {
          <<abstract>>
          #ProjectTaskService projectTaskService
          +complete(ProcessRequest req, ProcessResponse resp)
      }

      class ProcessRequest {
          -String operator
          -String taskId
          -String processId
          -WorkflowResultType result
          -Map~String,Object~ meta
          +getOperator()
          +setOperator(String operator)
          +getTaskId()
          +setTaskId(String taskId)
          +getProcessId()
          +setProcessId(String processId)
          +getResult()
          +setResult(WorkflowResultType result)
          +getMeta()
          +setMeta(Map~String,Object~ meta)
      }

      class ProcessResponse {
          -String status
          +getStatus()
          +setStatus(String status)
      }

      enum WorkflowResultType {
          APPROVED
          REJECTED
      }

      %% 具体任务引擎实现
      class EaOverseasConflictStartTaskEngine {
          -ProcessEngine processEngine
          +start(ProcessRequest req, ProcessResponse resp)
          +next(ProcessRequest req, ProcessResponse resp): List~String~
          +getTaskAssignRoleCode(ProcessRequest req): String
          +getNodeCode(): String
      }

      class EaOverseasConflictChinaSalesManagerTaskEngine {
      }

      class EaOverseasConflictPAEManagerTaskEngine {
      }

      class EaOverseasConflictResubmitTaskEngine {
      }

      %% 服务层
      class ProjectService {
          -JdbcTemplate jdbc
          -RowMapper~Project~ mapper
          +ProjectService(JdbcTemplate jdbc)
          +create(String projectCode, String buType, String projectType, String status): Project
          +get(String id): Project
      }

      class ProjectProcessService {
      }

      class ProjectTaskService {
          +create(String processId, String nodeCode, String role, String assigneeId): ProjectTask
          +completeTask(String operator, String taskId, String result, String meta): ProjectTask
      }

      %% 门面层
      class EAOverseasProjectFacade {
          <<interface>>
          +startConflictProcess(String projectId): ProjectProcess
          +listTasks(String processId): List~ProjectTask~
      }

      class EAOverseasProjectFacadeImpl {
          +startConflictProcess(String projectId): ProjectProcess
          +listTasks(String processId): List~ProjectTask~
      }

      %% 关系定义
      Project ||--o{ ProjectProcess : "1..*"
      ProjectProcess ||--o{ ProjectTask : "1..*"

      TaskEngine <|.. AbstractTaskEngine
      AbstractTaskEngine <|-- EaOverseasConflictStartTaskEngine
      AbstractTaskEngine <|-- EaOverseasConflictChinaSalesManagerTaskEngine
      AbstractTaskEngine <|-- EaOverseasConflictPAEManagerTaskEngine
      AbstractTaskEngine <|-- EaOverseasConflictResubmitTaskEngine

      ProcessEngine --> TaskEngine : "uses"
      ProcessEngine --> ProjectTaskService : "uses"
      ProcessEngine --> ProjectProcessService : "uses"

      AbstractTaskEngine --> ProjectTaskService : "uses"

      ProcessEngine --> ApplicationContext : "uses"

      EAOverseasProjectFacade <|.. EAOverseasProjectFacadeImpl

      EAOverseasProjectFacadeImpl --> ProcessEngine : "uses"
      EAOverseasProjectFacadeImpl --> ProjectService : "uses"
      EAOverseasProjectFacadeImpl --> ProjectProcessService : "uses"
      EAOverseasProjectFacadeImpl --> ProjectTaskService : "uses"

      ProjectService --> JdbcTemplate : "uses"

      ProcessRequest --> WorkflowResultType : "contains"
      TaskEngine ..> ProcessRequest : "receives"
      TaskEngine ..> ProcessResponse : "creates"

```