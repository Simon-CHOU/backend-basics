package com.simon.byterange;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FileService {
    void fileChunkDownload(String range, HttpServletRequest request, HttpServletResponse response);
}
