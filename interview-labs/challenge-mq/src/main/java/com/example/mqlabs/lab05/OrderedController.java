package com.example.mqlabs.lab05;

import java.util.Arrays;
import java.util.concurrent.Executors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderedController {
  private final JdbcTemplate jdbc;
  private final KeySerialProcessor processor = new KeySerialProcessor();
  public OrderedController(JdbcTemplate jdbc) { this.jdbc = jdbc; }
  @GetMapping("/lab05/ordered")
  public String ordered(@RequestParam String keys, @RequestParam(defaultValue = "5") int perKey) throws Exception {
    var list = Arrays.asList(keys.split(","));
    try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
      for (var k : list) {
        for (int i=0;i<perKey;i++) {
          ex.submit(() -> processor.submit(k, () -> writeLog(k)));
        }
      }
    }
    return "OK";
  }
  private void writeLog(String k) {
    Integer max = jdbc.query("select coalesce(max(seq),0) from lab05_log where k=?", rs -> rs.next()? rs.getInt(1):0, k);
    jdbc.update("insert into lab05_log(k,seq,ts) values(?,?,?)", k, max+1, java.time.Instant.now());
  }
}
