import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PersonExcelExporter {

    public void export(List<Person> persons, HttpServletResponse response) throws IOException {

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Persons");

        // Create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Full Name");
        header.createCell(1).setCellValue("Date of Birth");
        header.createCell(2).setCellValue("Gender");

        // Style for text fields (prevents Excel auto-formatting)
        CellStyle textStyle = wb.createCellStyle();
        DataFormat fmt = wb.createDataFormat();
        textStyle.setDataFormat(fmt.getFormat("@"));

        // Optional: date style
        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(fmt.getFormat("yyyy-mm-dd"));

        DateTimeFormatter dobFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        int rowIdx = 1;
        for (Person p : persons) {
            Row row = sheet.createRow(rowIdx++);

            // Full name (text)
            Cell nameCell = row.createCell(0);
            nameCell.setCellValue(p.getFullName());
            nameCell.setCellStyle(textStyle);

            // DOB (date)
            Cell dobCell = row.createCell(1);
            dobCell.setCellValue(p.getDateOfBirth().format(dobFmt));
            dobCell.setCellStyle(dateStyle);

            // Gender (text)
            Cell genderCell = row.createCell(2);
            genderCell.setCellValue(p.getGender());
            genderCell.setCellStyle(textStyle);
        }

        // Auto-size columns
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }

        // Configure HTTP response
        response.setContentType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        response.setHeader("Content-Disposition", "attachment; filename=persons.xlsx");

        wb.write(response.getOutputStream());
        wb.close();
    }
}
