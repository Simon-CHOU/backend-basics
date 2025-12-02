package com.example.mqlabs.lab05;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class KeySerialProcessor {
  private final Map<String, ArrayDeque<Runnable>> queues = new ConcurrentHashMap<>();
  private final Map<String, Boolean> running = new ConcurrentHashMap<>();
  public void submit(String key, Runnable task) {
    var q = queues.computeIfAbsent(key, k -> new ArrayDeque<>());
    synchronized (q) { q.add(task); }
    startIfNeeded(key);
  }
  private void startIfNeeded(String key) {
    if (Boolean.TRUE.equals(running.putIfAbsent(key, true))) return;
    Executors.newVirtualThreadPerTaskExecutor().submit(() -> runLoop(key));
  }
  private void runLoop(String key) {
    var q = queues.get(key);
    for (;;) {
      Runnable r;
      synchronized (q) { r = q.poll(); }
      if (r == null) break;
      r.run();
    }
    running.remove(key);
    if (!queues.get(key).isEmpty()) startIfNeeded(key);
  }
}
