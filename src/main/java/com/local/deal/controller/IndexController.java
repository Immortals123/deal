package com.local.deal.controller;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.local.deal.service.ExcelService;


@Controller
@RequestMapping("/")
public class IndexController {

	@Autowired
	private ExcelService excelService;

	@GetMapping({ "/", "/index" })
	public String index() {
		return "index";
	}

	@PostMapping("/dealExcel")
	public String uploadExcel(MultipartHttpServletRequest multipartRequest,HttpServletResponse response) throws Exception {
		InputStream in = null;
		Map<String, List<List<Object>>> listob = null;
		MultipartFile file = multipartRequest.getFile("file");
		if (file.isEmpty()) {
			throw new Exception("文件不存在！");
		}
		in = file.getInputStream();
		listob = excelService.getBankListByExcel(in, file.getOriginalFilename());
		in.close();
		
		excelService.exportExcel(listob,response);

		return "result";
	}

	@PostMapping("/dealSickExcel")
	public String dealSickExcel(MultipartHttpServletRequest multipartRequest,HttpServletResponse response) throws Exception {
		InputStream in = null;
		Map<String, List<List<Object>>> listob = null;
		MultipartFile file = multipartRequest.getFile("file");
		if (file.isEmpty()) {
			throw new Exception("文件不存在！");
		}
		in = file.getInputStream();
		listob = excelService.getBankListByExcel(in, file.getOriginalFilename());
		in.close();
		
		excelService.exportSickExcel(listob,response);

		return "result";
	}
}
