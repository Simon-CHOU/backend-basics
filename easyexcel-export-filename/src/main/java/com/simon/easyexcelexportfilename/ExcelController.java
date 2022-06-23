package com.simon.easyexcelexportfilename;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping
@Slf4j
public class ExcelController {
    /**
     * curl http://localhost:8080/export
     * https://cloud.tencent.com/developer/article/1640279
     *
     * @param response http response
     * @throws IOException get OutputStream failed
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
        String preEncodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.name());
        StringBuilder contentDispositionBuilder = new StringBuilder();
        contentDispositionBuilder.append("attachment;filename=")
                .append(preEncodedFilename).append(";")
                .append("filename*=").append("utf-8''")
                .append(preEncodedFilename);
        log.info("#Content-Disposition={}", contentDispositionBuilder);

        response.setHeader("Content-Disposition", contentDispositionBuilder.toString());
        EasyExcel.write(response.getOutputStream(), DemoData.class).sheet("列表").doWrite(resList);
    }// int postman, Content-Disposition is attachment;filename=????20220624.xlsx
    // chinese string "下载列表" converted to ????


    // ref https://segmentfault.com/a/1190000023601065  works !!  read RFC
    // https://stackoverflow.com/questions/50408723/content-disposition-filename-in-chinese-not-supported doesn't work
}
