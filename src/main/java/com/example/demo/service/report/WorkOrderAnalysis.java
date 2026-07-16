package com.example.demo.service.report;

import com.aspose.cells.*;
import com.aspose.cells.Color;
import com.aspose.cells.Font;
import com.aspose.cells.Picture;
import com.aspose.cells.Workbook;
import com.example.demo.models.ExitJobOrder;
import com.example.demo.models.JobOrder;
import com.example.demo.models.PandsToJobOrder;
import com.example.demo.repository.ExitJobOrderRepository;
import com.example.demo.repository.JobOrderRepository;
import com.example.demo.repository.PandsToJobOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkOrderAnalysis {

    private final ExitJobOrderRepository exitJobOrderRepository;

    private final PandsToJobOrderRepository pandsToJobOrderRepository;

    private final JobOrderRepository jobOrderRepository;


//    public ByteArrayInputStream buildReport(Long id) {
//        try {
//            Workbook workbook = new XSSFWorkbook();
//            Sheet sheet = workbook.createSheet("rest quantity");
//            sheet.setRightToLeft(false);
//
//            XSSFPrintSetup printSetup = (XSSFPrintSetup) sheet.getPrintSetup();
//            printSetup.setLandscape(true);
//
////            Font font = workbook.createFont();
////            font.setBold(true);
//            // Header
//            Row firstRow = sheet.createRow(0);
//
//            CellStyle cellStyle = workbook.createCellStyle();
////            cellStyle.setFont(font);
//            cellStyle.setAlignment(HorizontalAlignment.CENTER);
//
//            int widthInCharacters = 12;
//            sheet.setColumnWidth(0, widthInCharacters * 256);
//            sheet.setColumnWidth(1, widthInCharacters * 256);
//            sheet.setColumnWidth(2, widthInCharacters * 256);
//            sheet.setColumnWidth(3, widthInCharacters * 256);
//            sheet.setColumnWidth(4, widthInCharacters * 256);
//            sheet.setColumnWidth(5, widthInCharacters * 256);
//            sheet.setColumnWidth(6, widthInCharacters * 256);
//            sheet.setColumnWidth(7, widthInCharacters * 256);
//            sheet.setColumnWidth(8, widthInCharacters * 256);
//            sheet.setColumnWidth(9, widthInCharacters * 256);
//            sheet.setColumnWidth(10, widthInCharacters * 256);
//            sheet.setColumnWidth(11, widthInCharacters * 256);
//            sheet.setColumnWidth(12, widthInCharacters * 256);
//            sheet.setColumnWidth(13, widthInCharacters * 256);
//            sheet.setColumnWidth(14, widthInCharacters * 256);
//            sheet.setColumnWidth(15, widthInCharacters * 256);
//            sheet.setColumnWidth(16, widthInCharacters * 256);
//
//            Cell firstRow01 = firstRow.createCell(0);
//            Cell firstRow1 = firstRow.createCell(1);
//            Cell firstRow2 = firstRow.createCell(2);
//            Cell firstRow3 = firstRow.createCell(3);
//            Cell firstRow4 = firstRow.createCell(4);
//            Cell firstRow8 = firstRow.createCell(8);
//            Cell firstRow9 = firstRow.createCell(9);
//            Cell firstRow10 = firstRow.createCell(10);
//            Cell firstRow11 = firstRow.createCell(11);
//            Cell firstRow12 = firstRow.createCell(12);
//            Cell firstRow13 = firstRow.createCell(13);
//            Cell firstRow14 = firstRow.createCell(14);
//            Cell firstRow15 = firstRow.createCell(15);
//
//            firstRow.createCell(0).setCellValue("Project Name");
//            firstRow01.setCellStyle(cellStyle);
//            firstRow.createCell(1).setCellValue("Project Code");
//            firstRow1.setCellStyle(cellStyle);
//            firstRow.createCell(2).setCellValue("Engineer Name");
//            firstRow2.setCellStyle(cellStyle);
//            firstRow.createCell(3).setCellValue("Band Code");
//            firstRow3.setCellStyle(cellStyle);
//            firstRow.createCell(4).setCellValue("Description");
//            firstRow4.setCellStyle(cellStyle);
//
//            firstRow.createCell(5).setCellValue("Material");
//            firstRow8.setCellStyle(cellStyle);
//            firstRow.createCell(6).setCellValue("Material used");
//            firstRow9.setCellStyle(cellStyle);
//            firstRow.createCell(7).setCellValue("Finishing");
//            firstRow10.setCellStyle(cellStyle);
//            firstRow.createCell(8).setCellValue("Thickness");
//            firstRow11.setCellStyle(cellStyle);
//            firstRow.createCell(9).setCellValue("Height");
//            firstRow12.setCellStyle(cellStyle);
//            firstRow.createCell(10).setCellValue("Width");
//            firstRow13.setCellStyle(cellStyle);
//
//            firstRow.createCell(11).setCellValue("Unit");

    /// /            firstRow6.setCellStyle(cellStyle);
//
//            firstRow.createCell(12).setCellValue("Total permit");
//            firstRow14.setCellStyle(cellStyle);
//
//            firstRow.createCell(13).setCellValue("Total used Material");
//            firstRow15.setCellStyle(cellStyle);
//
//            int rowIdx = 1;
//
//            List<String> bandsToRaws = exitJobOrderRepository.findDistinctRawTypes(id);
//
//            for (String bandsToRaw : bandsToRaws) {
//
//                List<ExitJobOrder> bands = exitJobOrderRepository.findAllByRawType(id, pandsToRaw);
//
//                for (ExitJobOrder band : pands) {
//
//                    Row row = sheet.createRow(rowIdx);
//                    Cell cellRawType = row.createCell(0);
//                    cellRawType.setCellValue(pand.getProjectName());
//                    cellRawType.setCellStyle(cellStyle);
//
//                    Cell projectCode = row.createCell(1);
//                    projectCode.setCellValue(pand.getProjectCode());
//                    projectCode.setCellStyle(cellStyle);
//
//                    Cell engName = row.createCell(2);
//                    engName.setCellValue(pand.getEngineerName());
//                    engName.setCellStyle(cellStyle);
//
//                    Cell pandCode = row.createCell(3);
//                    pandCode.setCellValue(pand.getPandCode());
//                    pandCode.setCellStyle(cellStyle);
//
//                    Cell discription = row.createCell(4);
//                    discription.setCellValue(pand.getDescription());
//                    discription.setCellStyle(cellStyle);
//
//
//                    Cell rawType = row.createCell(5);
//                    rawType.setCellValue(pand.getRawType());
//                    rawType.setCellStyle(cellStyle);
//
//                    Cell rawUsed = row.createCell(6);
//                    rawUsed.setCellValue(pand.getRawUsed());
//                    rawUsed.setCellStyle(cellStyle);
//
//                    Cell finishType = row.createCell(7);
//                    finishType.setCellValue(pand.getFinishType());
//                    finishType.setCellStyle(cellStyle);
//
//                    Cell thickness = row.createCell(8);
//                    thickness.setCellValue(pand.getThickness());
//                    thickness.setCellStyle(cellStyle);
//
//                    Cell height = row.createCell(9);
//                    height.setCellValue(pand.getHeight());
//                    height.setCellStyle(cellStyle);
//
//                    Cell width = row.createCell(10);
//                    width.setCellValue(pand.getWidth());
//                    width.setCellStyle(cellStyle);
//
//                    Cell unit = row.createCell(11);
//                    unit.setCellValue(pand.getUnit());
//                    unit.setCellStyle(cellStyle);
//
//                    String totalAmount = String.valueOf(exitJobOrderRepository.sumQuantityByRawType(pand.getProjectCode(), pand.getRawType()));
//
//                    if (totalAmount == null) {
//                        totalAmount = "0";
//                    }
//
//                    Cell total = row.createCell(12);
//                    total.setCellValue(totalAmount);
//                    total.setCellStyle(cellStyle);
//
//                    String totalAmountProducted = String.valueOf(exitJobOrderRepository.sumQuantityUsedRaws(pand.getProjectCode(), pand.getRawType()));
//
//                    if (totalAmountProducted == null) {
//                        totalAmountProducted = "0";
//                    }
//
//                    Cell totalProducted = row.createCell(13);
//                    totalProducted.setCellValue(totalAmountProducted);
//                    totalProducted.setCellStyle(cellStyle);
//
//                    rowIdx++;
//                }
//                rowIdx++;
//            }
//
//            System.out.println("////////////////////////////");
//            sheet.autoSizeColumn(0);
//            sheet.autoSizeColumn(1);
//            sheet.autoSizeColumn(2);
//            sheet.autoSizeColumn(3);
//            sheet.autoSizeColumn(4);
//            sheet.autoSizeColumn(5);
//            sheet.autoSizeColumn(6);
//            sheet.autoSizeColumn(7);
//            sheet.autoSizeColumn(8);
//            sheet.autoSizeColumn(9);
//            sheet.autoSizeColumn(10);
//            sheet.autoSizeColumn(11);
//            sheet.autoSizeColumn(12);
//            sheet.autoSizeColumn(13);
//
//            ByteArrayOutputStream fileOut = new ByteArrayOutputStream();
//            workbook.write(fileOut);
//            workbook.close();
//
//            return new ByteArrayInputStream(fileOut.toByteArray());
//
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        return null;
//    }
    public InputStreamResource getPdf(Long id, String fromDate, String toDate, String status) throws Exception {

        Workbook workbook = new Workbook();
        Worksheet sheet = workbook.getWorksheets().get(0);
        sheet.setDisplayRightToLeft(false);

        configurePage(sheet);

        Style headerStyle = createHeaderStyle(sheet);
        Style dataStyle = createDataStyle(sheet);
        Style alternateStyle = createAlternateRowStyle(workbook);

        addLogo(sheet);
        createTitle(sheet);
        createHeader(sheet, headerStyle, Objects.equals(status, "1"));

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");

        String from = LocalDate.parse(fromDate, inputFormatter).format(outputFormatter);
        String to = LocalDate.parse(toDate, inputFormatter).format(outputFormatter);

        List<JobOrder> jobOrders;

        if (id == 0) {
            if (Objects.equals(status, "0")) {
                jobOrders = jobOrderRepository.findByAllProjectProfileIdByDate(from, to);
            } else {
                jobOrders = jobOrderRepository.findByAllProjectAndDateAndPercentageStatus( from, to, status);
            }
        } else {
            if (Objects.equals(status, "0")) {
                jobOrders = jobOrderRepository.findByProjectProfileIdByDate(id, from, to);
            } else {
                jobOrders = jobOrderRepository.findByProjectAndDateAndPercentageStatus(id, from, to, status);
            }
        }
        int row = 7;

        double totalSqm = 0;
        double totalExitSqm = 0;
        double totalDelivered = 0;

        for (JobOrder jobOrder : jobOrders) {

            List<PandsToJobOrder> pands =
                    pandsToJobOrderRepository.findByJobOrderId(jobOrder.getJobOrderNumber());

            List<ExitJobOrder> exits =
                    exitJobOrderRepository.findByJobOrderId(jobOrder.getJobOrderNumber());

            double sqm = getTotalInSquareMeter(pands);
            double exitSqm = getExitTotalInSquareMeter(exits);

            double delivered =
                    Optional.ofNullable(jobOrder.getTotalDelivered()).orElse(0.0);

            totalSqm += sqm;
            totalExitSqm += exitSqm;
            totalDelivered += delivered;

            writeJobOrderRow(
                    sheet,
                    row,
                    jobOrder,
                    sqm,
                    exitSqm,
                    delivered,
                    dataStyle,
                    alternateStyle,
                    Objects.equals(status, "1")
            );

            row++;
        }

        writeTotalsRow(
                sheet,
                row + 1,
                totalSqm,
                totalExitSqm,
                totalDelivered,
                dataStyle,
                Objects.equals(status, "1")
        );

        for (int i = 4; i <= row + 1; i++) {
            sheet.getCells().setRowHeight(i, 18);
        }

        sheet.autoFitColumns();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.save(out, SaveFormat.PDF);

        return new InputStreamResource(
                new ByteArrayInputStream(out.toByteArray()));
    }

    private void configurePage(Worksheet sheet) {

        PageSetup pageSetup = sheet.getPageSetup();

        pageSetup.setOrientation(PageOrientationType.LANDSCAPE);

        pageSetup.setFitToPagesWide(1);
        pageSetup.setFitToPagesTall(0);

        pageSetup.setFooter(1, "&B&12Page &P of &N");

        pageSetup.setTopMargin(1);
        pageSetup.setBottomMargin(1);
        pageSetup.setLeftMargin(1);
        pageSetup.setRightMargin(1);
    }

    private Style createHeaderStyle(Worksheet sheet) {

        Style style = sheet.getCells().get("A1").getStyle();

        style.setHorizontalAlignment(TextAlignmentType.CENTER);
        style.setVerticalAlignment(TextAlignmentType.CENTER);

        style.getFont().setBold(true);
        style.getFont().setSize(12);

        style.setBorder(BorderType.TOP_BORDER,
                CellBorderType.THIN,
                Color.getBlack());

        style.setBorder(BorderType.BOTTOM_BORDER,
                CellBorderType.THIN,
                Color.getBlack());

        style.setBorder(BorderType.LEFT_BORDER,
                CellBorderType.THIN,
                Color.getBlack());

        style.setBorder(BorderType.RIGHT_BORDER,
                CellBorderType.THIN,
                Color.getBlack());

        return style;
    }

    private Style createDataStyle(Worksheet sheet) {

        Style style = sheet.getCells().get("A1").getStyle();

        style.setHorizontalAlignment(TextAlignmentType.CENTER);
        style.setVerticalAlignment(TextAlignmentType.CENTER);

        style.getFont().setSize(11);

        style.setBorder(BorderType.TOP_BORDER,
                CellBorderType.THIN,
                Color.getBlack());

        style.setBorder(BorderType.BOTTOM_BORDER,
                CellBorderType.THIN,
                Color.getBlack());

        style.setBorder(BorderType.LEFT_BORDER,
                CellBorderType.THIN,
                Color.getBlack());

        style.setBorder(BorderType.RIGHT_BORDER,
                CellBorderType.THIN,
                Color.getBlack());

        return style;
    }

    private Style createAlternateRowStyle(Workbook workbook) {

        Style style = workbook.createStyle();

        style.setPattern(BackgroundType.SOLID);
        style.setForegroundColor(Color.getLightGray());

        style.setHorizontalAlignment(TextAlignmentType.CENTER);
        style.setVerticalAlignment(TextAlignmentType.CENTER);

        style.getFont().setSize(11);

        style.setBorder(BorderType.TOP_BORDER,
                CellBorderType.THIN,
                Color.getBlack());

        style.setBorder(BorderType.BOTTOM_BORDER,
                CellBorderType.THIN,
                Color.getBlack());

        style.setBorder(BorderType.LEFT_BORDER,
                CellBorderType.THIN,
                Color.getBlack());

        style.setBorder(BorderType.RIGHT_BORDER,
                CellBorderType.THIN,
                Color.getBlack());

        return style;
    }

    private void addLogo(Worksheet sheet) throws Exception {

        InputStream imageStream =
                new ClassPathResource("static/ORYX.jpeg").getInputStream();

        int pictureIndex =
                sheet.getPictures().add(0, 7, imageStream);

        Picture picture =
                sheet.getPictures().get(pictureIndex);

        picture.setPlacement(PlacementType.MOVE);

        picture.setWidthScale(60);
        picture.setHeightScale(40);
    }

    private void createTitle(Worksheet sheet) {

        Cells cells = sheet.getCells();

        cells.merge(2, 2, 2, 3);

        Cell cell = cells.get(2, 2);

        cell.putValue("Work Orders Analysis");

        Style style = cell.getStyle();

        style.getFont().setBold(true);
        style.getFont().setSize(16);

        style.setHorizontalAlignment(TextAlignmentType.CENTER);

        cell.setStyle(style);
    }

    private void createHeader(
            Worksheet sheet,
            Style style,
            boolean showPercentage) {

        String[] headers = {
                "Project Name",
                "Project Code",
                "Engineer Name",
                "Work Order",
                "M²",
                "Permitted in M²"
        };

        for (int i = 0; i < headers.length; i++) {

            Cell cell = sheet.getCells().get(5, i + 2);

            cell.putValue(headers[i]);

            cell.setStyle(style);
        }

        if (showPercentage) {

            Cell cell = sheet.getCells().get(5, 8);

            cell.putValue("Permitted %");

            cell.setStyle(style);
        }
    }

    private void writeJobOrderRow(
            Worksheet sheet,
            int row,
            JobOrder job,
            double sqm,
            double exitSqm,
            double delivered,
            Style normalStyle,
            Style alternateStyle,
            boolean showPercentage) {

        Style style = (row % 2 == 0) ? normalStyle : alternateStyle;

        Cells cells = sheet.getCells();

        writeCell(cells, row, 2, job.getProjectName(), style);
        writeCell(cells, row, 3, job.getProjectCode(), style);
        writeCell(cells, row, 4, job.getEngineerName(), style);
        writeCell(cells, row, 5, job.getJobOrderNumber(), style);

        writeCell(cells, row, 6, round(sqm), style);
        writeCell(cells, row, 7, round(exitSqm), style);

        if (showPercentage) {
            writeCell(cells, row, 8,
                    String.format("%.2f %%", delivered),
                    style);
        }
    }

    private void writeTotalsRow(
            Worksheet sheet,
            int row,
            double totalSqm,
            double totalExitSqm,
            double totalDelivered,
            Style style,
            boolean showPercentage) {

        Cells cells = sheet.getCells();

        writeCell(cells, row, 6, round(totalSqm), style);
        writeCell(cells, row, 7, round(totalExitSqm), style);

        if (showPercentage) {
            writeCell(
                    cells,
                    row,
                    8,
                    String.format("%.2f %%", totalDelivered),
                    style
            );
        }
    }

    private void writeCell(
            Cells cells,
            int row,
            int column,
            Object value,
            Style style) {

        Cell cell = cells.get(row, column);

        cell.putValue(value);

        cell.setStyle(style);
    }

    private double round(double value) {

        return BigDecimal
                .valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double getExitTotalInSquareMeter(
            List<ExitJobOrder> list) {

        if (list == null || list.isEmpty())
            return 0;

        return list.stream()

                .mapToDouble(item ->

                        item.getQuantity()

                                * Double.parseDouble(item.getHeight())

                                * Double.parseDouble(item.getWidth())

                                / 10000

                ).sum();
    }

    private double getTotalInSquareMeter(List<PandsToJobOrder> list) {

        if (list == null || list.isEmpty())
            return 0;

        return list.stream()

                .mapToDouble(item ->

                        item.getMainQuantity()

                                * Double.parseDouble(item.getRepetition())

                                * Double.parseDouble(item.getHeight())

                                * Double.parseDouble(item.getWidth())

                                / 10000

                ).sum();
    }

    private InputStreamResource exportPdf(Workbook workbook)
            throws Exception {

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        workbook.save(out, SaveFormat.PDF);

        return new InputStreamResource(

                new ByteArrayInputStream(out.toByteArray())

        );
    }
}
