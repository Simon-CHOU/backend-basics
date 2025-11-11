package com.simon.case_study_statistics_by_month;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 启用定时任务
@EnableAsync      // 启用异步处理
public class CaseStudyStatisticsByMonthApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaseStudyStatisticsByMonthApplication.class, args);
	}

}
