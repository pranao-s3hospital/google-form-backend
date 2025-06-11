package com.s3hospitals.feedback.utility;

import org.apache.poi.ss.usermodel.*;
        import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
        import java.util.List;

public class ExcelGenerator {
    public static byte[] generateExcelBytes(List<List<Object>> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Feedback Responses");

        int rowNum = 0;
        int maxColumns = 0;
        for (List<Object> rowData : data) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (Object cellData : rowData) {
                Cell cell = row.createCell(colNum++);
                cell.setCellValue(cellData.toString());
            }
            maxColumns = Math.max(maxColumns, rowData.size()); // track max columns
        }

        // Auto-size all columns after filling in data
        for (int i = 0; i < maxColumns; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }
}

