package com.eliall.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat.Style;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.eliall.common.EliObject;
import com.eliall.util.Tool;

@SuppressWarnings({"rawtypes","unchecked"})
public class Excel extends EliObject {
	public void read(InputStream input) throws IOException {
		Workbook book = null;
		List sheets = null;

		try {
			book = new XSSFWorkbook(input);
			sheets = set("sheets", new ArrayList()).getList("sheets");
			
			for (int s=0 ; s<book.getNumberOfSheets() ; s++) {
				List list = new ArrayList();
				Sheet sheet = book.getSheetAt(s);
				EliObject osheet = new EliObject();

				if (sheet.getPhysicalNumberOfRows() == 0) continue;
				else osheet.set("name", sheet.getSheetName()).set("list", list); sheets.add(osheet);
				
				for (int r=0 ; r<sheet.getPhysicalNumberOfRows() ; r++) readRow(sheet, sheet.getRow(r), list);
			}
		} finally { Tool.release(book); }
	}
	
	public void write(OutputStream output) throws IOException {
		write(output, null);
	}
	
	public void write(OutputStream output, CellStyle style) throws IOException { 
		Workbook book = null;
		List sheets = getList("sheets");
		
		if (sheets == null) throw new IOException("Not found sheet(s)");
	
		try {
			book = new XSSFWorkbook();
			if (style != null) book.createCellStyle().cloneStyleFrom(style);
			
			for (int s=0; s<sheets.size(); s++) {
				List list = null;
				Sheet sheet = null;
				EliObject osheet = new EliObject(sheets.get(s));
				
				if ((list = osheet.getList("list")) == null) continue; 
				else sheet = book.createSheet(osheet.getString("name", String.valueOf(s)));
				
				for (int r=0; r<list.size(); r++) { writeRow(sheet.createRow(r), (Map)list.get(r)); }
			}
			
			book.write(output);
		} finally { Tool.release(book); }
	}
	
	private void readRow(Sheet sheet, Row row, List list) {
		EliObject object = new EliObject();

		try { 
			for (int c=0 ; c<row.getPhysicalNumberOfCells() ; c++) {
				Cell title = sheet.getRow(sheet.getFirstRowNum()).getCell(c), cell = row.getCell(c);
				Object value = null;
				
				if (cell == null) continue;
	
				switch (cell.getCellType()) {
					case NUMERIC: value = cell.getNumericCellValue();  break;
					case FORMULA: value = cell.getCellFormula(); break;
					case BOOLEAN: value = cell.getBooleanCellValue(); break;
					case ERROR: value = cell.getErrorCellValue(); break;
					default: value = cell.getStringCellValue(); break;
				}
			
				object.set(title.getStringCellValue(), value);
			}
		} finally { list.add(object); }
	}
	
	private void writeRow(Row row, Map map) {
		List keys = new ArrayList(map.keySet()); 
		
		for (int c=0; c<keys.size(); c++) {
			Cell cell = row.createCell(c);
			Object value = map.get(keys.get(c));
			 
			 if (value instanceof Number) cell.setCellValue(((Number)value).doubleValue());
			 else if (value instanceof Boolean) cell.setCellValue(((Boolean)value).booleanValue());
			 else if (value instanceof Date) cell.setCellValue((Date)value);
			 else if (value instanceof Calendar) cell.setCellValue((Calendar)value);
			 else cell.setCellValue((String)value);
		}
	}
}
