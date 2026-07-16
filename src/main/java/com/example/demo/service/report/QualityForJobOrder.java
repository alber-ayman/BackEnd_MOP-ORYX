package com.example.demo.service.report;

import com.aspose.cells.*;
import com.aspose.cells.Color;
import com.aspose.cells.Font;
import com.aspose.cells.Picture;
import com.example.demo.models.ExitJobOrder;
import com.example.demo.repository.ExitJobOrderRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

@Service
public class QualityForJobOrder {

    @Autowired
    ExitJobOrderRepository exitJobOrderRepository;

    public ByteArrayInputStream buildReport(Long id) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("rest quantity");
            sheet.setRightToLeft(false);

            XSSFPrintSetup printSetup = (XSSFPrintSetup) sheet.getPrintSetup();
            printSetup.setLandscape(true);

//            Font font = workbook.createFont();
//            font.setBold(true);
            // Header
            Row firstRow = sheet.createRow(0);

            CellStyle cellStyle = workbook.createCellStyle();
//            cellStyle.setFont(font);
            cellStyle.setAlignment(HorizontalAlignment.CENTER);

            int widthInCharacters = 12;
            sheet.setColumnWidth(0, widthInCharacters * 256);
            sheet.setColumnWidth(1, widthInCharacters * 256);
            sheet.setColumnWidth(2, widthInCharacters * 256);
            sheet.setColumnWidth(3, widthInCharacters * 256);
            sheet.setColumnWidth(4, widthInCharacters * 256);
            sheet.setColumnWidth(5, widthInCharacters * 256);
            sheet.setColumnWidth(6, widthInCharacters * 256);
            sheet.setColumnWidth(7, widthInCharacters * 256);
            sheet.setColumnWidth(8, widthInCharacters * 256);
            sheet.setColumnWidth(9, widthInCharacters * 256);
            sheet.setColumnWidth(10, widthInCharacters * 256);
            sheet.setColumnWidth(11, widthInCharacters * 256);
            sheet.setColumnWidth(12, widthInCharacters * 256);
            sheet.setColumnWidth(13, widthInCharacters * 256);
            sheet.setColumnWidth(14, widthInCharacters * 256);
            sheet.setColumnWidth(15, widthInCharacters * 256);
            sheet.setColumnWidth(16, widthInCharacters * 256);

            Cell firstRow01 = firstRow.createCell(0);
            Cell firstRow1 = firstRow.createCell(1);
            Cell firstRow2 = firstRow.createCell(2);
            Cell firstRow3 = firstRow.createCell(3);
            Cell firstRow4 = firstRow.createCell(4);
            Cell firstRow5 = firstRow.createCell(5);
            Cell firstRow6 = firstRow.createCell(6);
            Cell firstRow7 = firstRow.createCell(7);
            Cell firstRow8 = firstRow.createCell(8);
            Cell firstRow9 = firstRow.createCell(9);
            Cell firstRow10 = firstRow.createCell(10);
            Cell firstRow11 = firstRow.createCell(11);
            Cell firstRow12 = firstRow.createCell(12);
            Cell firstRow13 = firstRow.createCell(13);
            Cell firstRow14 = firstRow.createCell(14);
            Cell firstRow15 = firstRow.createCell(15);
            Cell firstRow16 = firstRow.createCell(16);

            firstRow.createCell(0).setCellValue("Project Name");
            firstRow01.setCellStyle(cellStyle);
            firstRow.createCell(1).setCellValue("Project Code");
            firstRow1.setCellStyle(cellStyle);
            firstRow.createCell(2).setCellValue("Engineer Name");
            firstRow2.setCellStyle(cellStyle);
            firstRow.createCell(3).setCellValue("Pand Code");
            firstRow3.setCellStyle(cellStyle);
            firstRow.createCell(4).setCellValue("Description");
            firstRow4.setCellStyle(cellStyle);

//            firstRow.createCell(5).setCellValue("Quantity");
//            firstRow5.setCellStyle(cellStyle);
//            firstRow.createCell(6).setCellValue("Unit");
//            firstRow6.setCellStyle(cellStyle);
//            firstRow.createCell(7).setCellValue("Manufacturing");
//            firstRow7.setCellStyle(cellStyle);

            firstRow.createCell(5).setCellValue("Material");
            firstRow8.setCellStyle(cellStyle);
            firstRow.createCell(6).setCellValue("Material used");
            firstRow9.setCellStyle(cellStyle);
            firstRow.createCell(7).setCellValue("Finishing");
            firstRow10.setCellStyle(cellStyle);
            firstRow.createCell(8).setCellValue("Thickness");
            firstRow11.setCellStyle(cellStyle);
            firstRow.createCell(9).setCellValue("Height");
            firstRow12.setCellStyle(cellStyle);
            firstRow.createCell(10).setCellValue("Width");
            firstRow13.setCellStyle(cellStyle);

            firstRow.createCell(11).setCellValue("Unit");
//            firstRow6.setCellStyle(cellStyle);

            firstRow.createCell(12).setCellValue("Total permit");
            firstRow14.setCellStyle(cellStyle);

            firstRow.createCell(13).setCellValue("Total used Material");
            firstRow15.setCellStyle(cellStyle);
//
//            firstRow.createCell(16).setCellValue("Rest In Job Order");
//            firstRow16.setCellStyle(cellStyle);

            int rowIdx = 1;

            List<String> pandsToRaws = exitJobOrderRepository.findDistinctRawTypes(id);

            for (String pandsToRaw : pandsToRaws) {

                List<ExitJobOrder> pands = exitJobOrderRepository.findAllByRawType(id, pandsToRaw);

                for (ExitJobOrder pand : pands) {

                    Row row = sheet.createRow(rowIdx);
                    Cell cellRawType = row.createCell(0);
                    cellRawType.setCellValue(pand.getProjectName());
                    cellRawType.setCellStyle(cellStyle);

                    Cell projectCode = row.createCell(1);
                    projectCode.setCellValue(pand.getProjectCode());
                    projectCode.setCellStyle(cellStyle);

                    Cell engName = row.createCell(2);
                    engName.setCellValue(pand.getEngineerName());
                    engName.setCellStyle(cellStyle);

                    Cell pandCode = row.createCell(3);
                    pandCode.setCellValue(pand.getPandCode());
                    pandCode.setCellStyle(cellStyle);

                    Cell discription = row.createCell(4);
                    discription.setCellValue(pand.getDescription());
                    discription.setCellStyle(cellStyle);


                    Cell rawType = row.createCell(5);
                    rawType.setCellValue(pand.getRawType());
                    rawType.setCellStyle(cellStyle);

                    Cell rawUsed = row.createCell(6);
                    rawUsed.setCellValue(pand.getRawUsed());
                    rawUsed.setCellStyle(cellStyle);

                    Cell finishType = row.createCell(7);
                    finishType.setCellValue(pand.getFinishType());
                    finishType.setCellStyle(cellStyle);

                    Cell thickness = row.createCell(8);
                    thickness.setCellValue(pand.getThickness());
                    thickness.setCellStyle(cellStyle);

                    Cell height = row.createCell(9);
                    height.setCellValue(pand.getHeight());
                    height.setCellStyle(cellStyle);

                    Cell width = row.createCell(10);
                    width.setCellValue(pand.getWidth());
                    width.setCellStyle(cellStyle);

                    Cell unit = row.createCell(11);
                    unit.setCellValue(pand.getUnit());
                    unit.setCellStyle(cellStyle);

                    String totalAmount = String.valueOf(exitJobOrderRepository.sumQuantityByRawType(pand.getProjectCode(), pand.getRawType()));

                    if (totalAmount == null) {
                        totalAmount = "0";
                    }

                    Cell total = row.createCell(12);
                    total.setCellValue(totalAmount);
                    total.setCellStyle(cellStyle);

                    String totalAmountProducted = String.valueOf(exitJobOrderRepository.sumQuantityUsedRaws(pand.getProjectCode(), pand.getRawType()));

                    if (totalAmountProducted == null) {
                        totalAmountProducted = "0";
                    }

                    Cell totalProducted = row.createCell(13);
                    totalProducted.setCellValue(totalAmountProducted);
                    totalProducted.setCellStyle(cellStyle);

                    rowIdx++;
                }
                rowIdx++;
            }

            System.out.println("////////////////////////////");
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            sheet.autoSizeColumn(3);
            sheet.autoSizeColumn(4);
            sheet.autoSizeColumn(5);
            sheet.autoSizeColumn(6);
            sheet.autoSizeColumn(7);
            sheet.autoSizeColumn(8);
            sheet.autoSizeColumn(9);
            sheet.autoSizeColumn(10);
            sheet.autoSizeColumn(11);
            sheet.autoSizeColumn(12);
            sheet.autoSizeColumn(13);


            System.err.println("rrrrrrrrrrrrrrrrrrrrrrrrrrr");
            ByteArrayOutputStream fileOut = new ByteArrayOutputStream();
            workbook.write(fileOut);
            workbook.close();

            System.err.println("zzzzzzzzzzzzzzzzzzzzzzzzzzzz");
            return new ByteArrayInputStream(fileOut.toByteArray());

        } catch (Exception e) {

            System.err.println("xxxxxxxxxxxxxxxxxxxxxxx");
            System.out.println("catch");
            e.printStackTrace();
        }
        return null;
    }

    public InputStreamResource getPdf(Long id) throws Exception {

        com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook();
        WorksheetCollection worksheets = workbook.getWorksheets();
        Worksheet sheet = worksheets.get(0);
        sheet.setDisplayRightToLeft(false);

        PageSetup pageSetup = sheet.getPageSetup();
        pageSetup.setFooter(1, "Page &P of &N");
        pageSetup.setOrientation(PageOrientationType.LANDSCAPE);
        pageSetup.setFitToPagesWide(1); // Fit to 1 page width
        pageSetup.setFitToPagesTall(0); // Set to 0 for automatic height

        pageSetup.setTopMargin(1);
        pageSetup.setBottomMargin(1);
        pageSetup.setLeftMargin(1);
        pageSetup.setRightMargin(1);

        Style tableHeaderStyle = sheet.getCells().get("C1").getStyle();
        tableHeaderStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        tableHeaderStyle.setVerticalAlignment(TextAlignmentType.CENTER);
        tableHeaderStyle.getFont().setSize(12);
        tableHeaderStyle.getFont().setBold(false);
        tableHeaderStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());

        Style discriptionDataStyle = sheet.getCells().get("F3").getStyle();
        discriptionDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        discriptionDataStyle.setVerticalAlignment(TextAlignmentType.CENTER);
        discriptionDataStyle.getFont().setSize(11);

        List<String> rawTypes = exitJobOrderRepository.findDistinctRawTypes(id);

        sheet.getCells().get("A1").putValue("Project Name");
        sheet.getCells().get("A1").setStyle(discriptionDataStyle);

        sheet.getCells().get("A3").putValue("Project Code");
        sheet.getCells().get("A3").setStyle(discriptionDataStyle);

        sheet.getCells().get("D1").putValue("Engineer Name");
        sheet.getCells().get("D1").setStyle(discriptionDataStyle);

        Style shadowStyle = workbook.createStyle();
        shadowStyle.setPattern(BackgroundType.SOLID);
        shadowStyle.setForegroundColor(Color.getDarkGray());
        shadowStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        shadowStyle.getFont().setSize(11);

        sheet.getCells().get("A6").putValue("Pand Code");
        sheet.getCells().get("A6").setStyle(tableHeaderStyle);

        sheet.getCells().get("B6").putValue("Description");
        sheet.getCells().get("B6").setStyle(tableHeaderStyle);

        sheet.getCells().get("C6").putValue("Material");
        sheet.getCells().get("C6").setStyle(tableHeaderStyle);

//        sheet.getCells().get("D5").putValue("Material used");
//        sheet.getCells().get("D5").setStyle(tableHeaderStyle);

        sheet.getCells().get("D6").putValue("Finishing");
        sheet.getCells().get("D6").setStyle(tableHeaderStyle);

        sheet.getCells().get("E6").putValue("Thickness");
        sheet.getCells().get("E6").setStyle(tableHeaderStyle);

        sheet.getCells().get("F6").putValue("Height");
        sheet.getCells().get("F6").setStyle(tableHeaderStyle);

        sheet.getCells().get("G6").putValue("Width");
        sheet.getCells().get("G6").setStyle(tableHeaderStyle);

        sheet.getCells().get("H6").putValue("Unit");
        sheet.getCells().get("H6").setStyle(tableHeaderStyle);

        sheet.getCells().get("I6").putValue("Total Permit");
        sheet.getCells().get("I6").setStyle(tableHeaderStyle);

        sheet.getCells().get("J6").putValue("Total Used Material");
        sheet.getCells().get("J6").setStyle(tableHeaderStyle);

        InputStream imageStream = new ClassPathResource("static/ORYX.jpeg").getInputStream();

        // Add the image to the worksheet (X, Y coordinates in pixels)
        // Place the image inside the merged cells (A1:C5)
        int pictureIndex = sheet.getPictures().add(0, 8, imageStream);

        // Get the added picture
        Picture picture = sheet.getPictures().get(pictureIndex);

        // Optionally, set the picture to fit within the merged area
        picture.setPlacement(PlacementType.MOVE);
        picture.setWidthScale(60); // Scale the image to fit width
        picture.setHeightScale(40);

        Cells cells = sheet.getCells();
        cells.merge(2, 3, 2, 3);

        // Assign a value to the merged cell
//        cells.get(3, 4).setValue("اجمالى الكميات بالوحدات");
//        sheet.getCells().get("E3").setStyle(titleStyle);

        com.aspose.cells.Cell mergedCell = cells.get(2, 3);
        mergedCell.setValue("Used Material Analysis");

        // Modify the style to set font size to 16
        Style style = mergedCell.getStyle();
        Font font = style.getFont();
        font.setSize(15);
        mergedCell.setStyle(style);

        int rowIdx = 8;

        for (String rawType : rawTypes) {

            List<ExitJobOrder> pands = exitJobOrderRepository.findAllByRawType(id, rawType);

            sheet.getCells().get("B1").putValue(pands.getFirst().getProjectName());
            sheet.getCells().get("B1").setStyle(discriptionDataStyle);

            sheet.getCells().get("B3").putValue(pands.getFirst().getProjectCode());
            sheet.getCells().get("B3").setStyle(discriptionDataStyle);

            sheet.getCells().get("F1").putValue(pands.getFirst().getEngineerName());
            sheet.getCells().get("F1").setStyle(discriptionDataStyle);

            for (ExitJobOrder pand : pands) {

                sheet.getCells().get("A" + rowIdx).putValue(pand.getPandCode());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("A" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("A" + rowIdx).setStyle(discriptionDataStyle);
                }

                sheet.getCells().get("B" + rowIdx).putValue(pand.getDescription());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("B" + rowIdx).setStyle(discriptionDataStyle);
                }


                sheet.getCells().get("C" + rowIdx).putValue(pand.getRawType());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("C" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);
                }

                sheet.getCells().get("D" + rowIdx).putValue(pand.getFinishType());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("D" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);
                }

                sheet.getCells().get("E" + rowIdx).putValue(pand.getThickness());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("E" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("E" + rowIdx).setStyle(discriptionDataStyle);
                }

                sheet.getCells().get("F" + rowIdx).putValue(pand.getHeight());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("F" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("F" + rowIdx).setStyle(discriptionDataStyle);
                }

                sheet.getCells().get("G" + rowIdx).putValue(pand.getWidth());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("G" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);
                }

                sheet.getCells().get("H" + rowIdx).putValue(pand.getUnit());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("H" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);
                }

                String totalAmount = String.valueOf(exitJobOrderRepository.sumQuantityByRawType(pand.getProjectCode(), pand.getRawType()));

                if (totalAmount == null) {
                    totalAmount = "0";
                }

                String totalAmountProducted = String.valueOf(exitJobOrderRepository.sumQuantityUsedRaws(pand.getProjectCode(), pand.getRawType()));

                if (totalAmountProducted == null) {
                    totalAmountProducted = "0";
                }

                sheet.getCells().get("I" + rowIdx).putValue(totalAmount);
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("I" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("I" + rowIdx).setStyle(discriptionDataStyle);
                }

                sheet.getCells().get("J" + rowIdx).putValue(totalAmountProducted);
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("J" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("J" + rowIdx).setStyle(discriptionDataStyle);
                }
                rowIdx++;
            }
            rowIdx += 2;
        }

        for (int i = 4; i < rowIdx; i++) {
            sheet.getCells().setRowHeight(i, 18);
        }

        //////////////////////////////////////////////////////////////////////
        // Adjust column widths to fit content
        sheet.autoFitColumns();

        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        workbook.save(pdfOutputStream, SaveFormat.PDF); // Save as PDF

        // 4. Return the PDF as a response
        ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
        InputStreamResource resource = new InputStreamResource(pdfInputStream);

        return resource;
    }
}
