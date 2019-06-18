package com.local.deal.service.impl;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.local.deal.service.ExcelService;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

@Service
public class ExcelServiceImpl implements ExcelService {

	private final static String excel2003L = ".xls"; // 2003- 版本的excel
	private final static String excel2007U = ".xlsx"; // 2007+ 版本的excel

	@Override
	public Map<String, List<List<Object>>> getBankListByExcel(InputStream in, String filename) {

		Map<String, List<List<Object>>> exportExcel = new HashMap<>();
		try {

			// 创建Excel工作薄
			Workbook work = this.getWorkbook(in, filename);
			if (null == work) {
				throw new Exception("创建Excel工作薄为空！");
			}
			Sheet sheet = null;
			Row row = null;
			Cell cell = null;

			// 遍历Excel中所有的sheet
			for (int i = 0; i < work.getNumberOfSheets(); i++) {

				List<List<Object>> list = new ArrayList<List<Object>>();

				sheet = work.getSheetAt(i);
				if (sheet == null) {
					continue;
				}

				// 遍历当前sheet中的所有行
				for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum() - 1; j++) {

					row = sheet.getRow(j);
					
					List<Object> li = new ArrayList<Object>();
					for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
						cell = row.getCell(y);
						if (this.isMergedRegion(sheet, j, y)) {
							li.add(this.getMergedRegionValue(sheet, j, y));
						} else {
							li.add(StringUtils.isEmpty(cell) ? "" :this.getCellValue(cell));
						}

					}
					list.add(li);
				}

				exportExcel.put(sheet.getSheetName(), list);
			}
			work.close();
			return exportExcel;

		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return null;
	}

	/**
	 * 描述：根据文件后缀，自适应上传文件的版本
	 * 
	 * @param inStr,fileName
	 * @return
	 * @throws Exception
	 */
	private Workbook getWorkbook(InputStream inStr, String fileName) throws Exception {
		Workbook wb = null;
		String fileType = fileName.substring(fileName.lastIndexOf("."));
		if (excel2003L.equals(fileType)) {
			wb = new HSSFWorkbook(inStr); // 2003-
		} else if (excel2007U.equals(fileType)) {
			wb = new XSSFWorkbook(inStr); // 2007+
		} else {
			throw new Exception("解析的文件格式有误！");
		}
		return wb;
	}

	/**
	 * 描述：对表格中数值进行格式化
	 * 
	 * @param cell
	 * @return
	 */
	private Object getCellValue(Cell cell) {
		Object value = null;
		DecimalFormat df = new DecimalFormat("0"); // 格式化number String字符
		SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd"); // 日期格式化
		DecimalFormat df2 = new DecimalFormat("0"); // 格式化数字

		switch (cell.getCellType()) {
		case STRING:
			value = cell.getRichStringCellValue().getString();
			break;
		case NUMERIC:
			if ("General".equals(cell.getCellStyle().getDataFormatString())) {
				value = df.format(cell.getNumericCellValue());
			} else if ("m/d/yy".equals(cell.getCellStyle().getDataFormatString())) {
				value = sdf.format(cell.getDateCellValue());
			} else {
				value = df2.format(cell.getNumericCellValue());
			}
			break;
		case BOOLEAN:
			value = cell.getBooleanCellValue();
			break;
		case BLANK:
			value = "";
			break;
		default:
			break;
		}
		return value;
	}

	/**
	 * 获取合并单元格的内容
	 * 
	 * @param sheet
	 * @param row
	 * @param column
	 * @return
	 */
	private Object getMergedRegionValue(Sheet sheet, int row, int column) {
		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress ca = sheet.getMergedRegion(i);
			int firstColumn = ca.getFirstColumn();
			int lastColumn = ca.getLastColumn();
			int firstRow = ca.getFirstRow();
			int lastRow = ca.getLastRow();
			if (row >= firstRow && row <= lastRow) {
				if (column >= firstColumn && column <= lastColumn) {
					Row fRow = sheet.getRow(firstRow);
					Cell fCell = fRow.getCell(firstColumn);
					return this.getCellValue(fCell);
				}
			}
		}
		return null;
	}

	/**
	 * 判断是否是合并单元格
	 * 
	 * @param sheet
	 * @param row
	 * @param column
	 * @return
	 */
	private boolean isMergedRegion(Sheet sheet, int row, int column) {
		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstColumn = range.getFirstColumn();
			int lastColumn = range.getLastColumn();
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			if (row >= firstRow && row <= lastRow) {
				if (column >= firstColumn && column <= lastColumn) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取字符串拼音的第一个字母
	 * 
	 * @param chinese
	 * @return
	 */
	private static String ToFirstChar(String chinese) {
		if(!StringUtils.isEmpty(chinese.trim())){
			String pinyinStr = "";
			char[] newChar = chinese.toCharArray(); // 转为单个字符
			HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
			defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
			defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			for (int i = 0; i < newChar.length; i++) {
				if (newChar[i] > 128) {
					try {
						pinyinStr += PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)[0].charAt(0);
					} catch (BadHanyuPinyinOutputFormatCombination e) {
						e.printStackTrace();
					} catch (NullPointerException ex){
						pinyinStr += newChar[i];
						continue;
					}
				} else {
					pinyinStr += newChar[i];
				}
			}
			return pinyinStr;
		}
		return "";
	}

	/**
	 * 汉字转为拼音
	 * 
	 * @param chinese
	 * @return
	 */
	private static String ToPinyin(String chinese) {
		if(!StringUtils.isEmpty(chinese.trim())){
			String pinyinStr = "";
			char[] newChar = chinese.toCharArray();
			HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
			defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
			defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			for (int i = 0; i < newChar.length; i++) {
				if (newChar[i] > 128) {
					try {
						pinyinStr += PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)[0];
					} catch (BadHanyuPinyinOutputFormatCombination e) {
						e.printStackTrace();
					} catch (NullPointerException ex){
						pinyinStr += newChar[i];
						continue;
					}
				} else {
					pinyinStr += newChar[i];
				}
			}
			return pinyinStr;
		}
		return "";
	}

	@Override
	public void exportExcel(Map<String, List<List<Object>>> resMap,HttpServletResponse response) {
		
		@SuppressWarnings("resource")
		HSSFWorkbook wb =new HSSFWorkbook();
		
		for (String key : resMap.keySet()) {
			List<List<Object>> listob = resMap.get(key);

			listob.forEach(ob -> {
				if (ob!=null && ob.size()>9 && !StringUtils.isEmpty(ob.get(8))) {
					ob.add(ToFirstChar(ob.get(8).toString()));
					ob.add(ToPinyin(ob.get(8).toString()));
				}
			});

			HSSFSheet sheet = wb.createSheet(key);

			for(int i = 0;i<listob.size();i++){

				HSSFRow row1=sheet.createRow(i);
				
				for(int j = 0; j<listob.get(i).size();j++){
					HSSFCell cell=row1.createCell(j);
					cell.setCellValue(listob.get(i).get(j) == null ? "": listob.get(i).get(j).toString()); 
				}
			}
		}
		
		try{

			OutputStream output=response.getOutputStream();  
			response.reset();  
			response.setHeader("Content-disposition", "attachment; filename=details.xls");  
			response.setContentType("application/msexcel");          
			wb.write(output);  
			output.close();
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void exportSickExcel(Map<String, List<List<Object>>> resMap, HttpServletResponse response) {
		
		@SuppressWarnings("resource")
		HSSFWorkbook wb =new HSSFWorkbook();
		
		for (String key : resMap.keySet()) {
			List<List<Object>> listob = resMap.get(key);

			listob.forEach(ob -> {
				if (ob!=null && ob.size()>1 && !StringUtils.isEmpty(ob.get(1))) {
					ob.add(ToFirstChar(ob.get(1).toString()));
					ob.add(ToPinyin(ob.get(1).toString()));
				}
			});

			HSSFSheet sheet = wb.createSheet(key);

			for(int i = 0;i<listob.size();i++){

				HSSFRow row1=sheet.createRow(i);
				
				for(int j = 0; j<listob.get(i).size();j++){
					HSSFCell cell=row1.createCell(j);
					cell.setCellValue(listob.get(i).get(j) == null ? "": listob.get(i).get(j).toString()); 
				}
			}
		}
		
		try{

			OutputStream output=response.getOutputStream();  
			response.reset();  
			response.setHeader("Content-disposition", "attachment; filename=details.xls");  
			response.setContentType("application/msexcel");          
			wb.write(output);  
			output.close();
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
}
