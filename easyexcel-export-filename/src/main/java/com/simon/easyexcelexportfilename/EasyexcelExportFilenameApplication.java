package com.simon.easyexcelexportfilename;

import com.alibaba.excel.EasyExcel;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@SpringBootApplication
public class EasyexcelExportFilenameApplication {

	public static void main(String[] args) {
		SpringApplication.run(EasyexcelExportFilenameApplication.class, args);
	}


}
