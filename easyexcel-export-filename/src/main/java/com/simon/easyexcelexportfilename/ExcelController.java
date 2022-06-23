package com.simon.easyexcelexportfilename;

import com.alibaba.excel.EasyExcel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping
public class ExcelController {
    /**
     * curl http://localhost:8080/export
     * https://cloud.tencent.com/developer/article/1640279
     * @param response
     * @throws IOException
     */
    @GetMapping("/export")
    void export(HttpServletResponse response) throws IOException {
        final String filename = "下载列表20220624.xlsx";
        List<DemoData> resList = new ArrayList<>();
        DemoData dto = new DemoData();
        dto.setDate(new Date());
        dto.setDoubleData(100.9);
        dto.setString("amanda");
        dto.setIgnore("stuff");
        resList.add(dto);
        response.setHeader("Content-Type", "application/vnd.ms-excel");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);
        EasyExcel.write(response.getOutputStream(), DemoData.class).sheet("列表").doWrite(resList);
    }// int postman, Content-Disposition is attachment;filename=????20220624.xlsx
    // chinese string "下载列表" converted to ????


}
