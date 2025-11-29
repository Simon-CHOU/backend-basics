package com.example.dec.engine;

import java.util.List;

public interface TaskEngine {
  void start(ProcessRequest req, ProcessResponse resp);
  void complete(ProcessRequest req, ProcessResponse resp);
  List<String> next(ProcessRequest req, ProcessResponse resp);
  String getTaskAssignRoleCode(ProcessRequest req);
  String getNodeCode();
}
