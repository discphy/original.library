package com.eliall.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
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
				
				for (int r=0 ; r<sheet.getPhysicalNumberOfRows() ; r++) row(sheet, sheet.getRow(r), list);
				
				sheets.add(sheets);
			}
		} finally { Tool.release(book); }
	}
	
	public void write(OutputStream output) throws IOException {
		List sheets = getList("sheets");
		
		if (sheets == null) throw new IOException("Not found sheet(s)");
	}

	private void row(Sheet sheet, Row row, List list) {
		EliObject object = new EliObject();

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
			
			list.add(object.set(title.getStringCellValue(), value));
		}
	}
}
