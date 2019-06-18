package com.local.deal.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public interface ExcelService {
	
	Map<String, List<List<Object>>> getBankListByExcel(InputStream in, String filename);

	void exportExcel(Map<String, List<List<Object>>> resMap,HttpServletResponse response);

	void exportSickExcel(Map<String, List<List<Object>>> resMap,HttpServletResponse response);
}
