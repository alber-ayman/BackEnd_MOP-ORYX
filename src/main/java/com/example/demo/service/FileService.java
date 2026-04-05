package com.example.demo.service;

//import com.itextpdf.io.image.ImageDataFactory;
//import com.itextpdf.layout.element.Image;
//import com.itextpdf.layout.element.Table;

import com.example.demo.models.*;
import com.example.demo.repository.ExitProcessJobOrderRepository;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.layout.Document;
//import com.itextpdf.layout.element.Paragraph;
//import com.itextpdf.layout.property.UnitValue;
import com.example.demo.service.workOrder.JobOrderService;
import com.example.demo.service.workOrder.PandsToJobOrderService;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
//import org.apache.poi.wp.usermodel.Paragraph;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    @Autowired
    PandsToJobOrderService pandsToJobOrderService;
    @Autowired
    ExitProcessJobOrderRepository exitProcessJobOrderRepository;

    @Autowired
    JobOrderService jobOrderService;

    public InputStreamResource buildFile(JobOrderParent jobOrderParent) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("أمر الشغل");
            sheet.setRightToLeft(false);

            XSSFPrintSetup printSetup = (XSSFPrintSetup) sheet.getPrintSetup();
            printSetup.setLandscape(true);

            Font font = workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 14);

            Font font2 = workbook.createFont();
//            font2.setBold(true);
            font2.setFontHeightInPoints((short) 14);


            JobOrder lastJobOrder = jobOrderService.getByJobOrder(jobOrderParent.getPandsToJobOrderList().get(0).getJobOrderId());
            // Header
            Row firstRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setFont(font);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            headerCellStyle.setBorderRight(BorderStyle.THIN);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font2);


            cellStyle.setAlignment(HorizontalAlignment.CENTER);

            firstRow.createCell(0).setCellValue("     ");

            Cell cell2 = firstRow.createCell(2);
            cell2.setCellValue("عرض فنى");
            cell2.setCellStyle(cellStyle);

//            firstRow.createCell(2).setCellValue("عرض فنى");
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 3, 4));
            firstRow.createCell(3).setCellValue(jobOrderParent.getPandsToJobOrderList().get(0).getJobOrderId());

            Cell cell3 = firstRow.createCell(3);
            cell3.setCellValue(jobOrderParent.getPandsToJobOrderList().get(0).getJobOrderId());
            cell3.setCellStyle(cellStyle);

            firstRow.createCell(4).setCellValue("     ");
            firstRow.createCell(6).setCellValue("رقم الكود");

            Cell cell4 = firstRow.createCell(6);
            cell4.setCellValue("رقم الكود");
            cell4.setCellStyle(cellStyle);

            int widthInCharacters = 12;
            sheet.setColumnWidth(7, widthInCharacters * 256);
//            firstRow.createCell(7).setCellValue();
            Cell cell7 = firstRow.createCell(7);
            cell7.setCellValue(jobOrderParent.getPandsToJobOrderList().get(0).getProjectCode() + " " + jobOrderParent.getPandsToJobOrderList().get(0).getProjectName());
            cell7.setCellStyle(cellStyle);

//            firstRow.createCell(10).setCellValue("مسلسل:");
            Cell cell10 = firstRow.createCell(10);
            cell10.setCellValue("مسلسل: ");
            cell10.setCellStyle(cellStyle);


            Cell cell11 = firstRow.createCell(11);
            cell11.setCellValue(jobOrderParent.getPandsToJobOrderList().get(0).getJobOrderId());
            cell11.setCellStyle(cellStyle);


//            firstRow.createCell(11).setCellValue(jobOrderParent.getPandsToJobOrderList().get(0).getJobOrderId());

            Row secondRow = sheet.createRow(1);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 3, 4));

            Cell cell074 = secondRow.createCell(2);
            cell074.setCellValue("تاريخ الاصدار");
            cell074.setCellStyle(cellStyle);

            Cell cell064 = secondRow.createCell(3);
            cell064.setCellValue(lastJobOrder.getJobOrderDate() + " " + lastJobOrder.getJobOrderTime());
            cell064.setCellStyle(cellStyle);

            Cell cell054 = secondRow.createCell(6);
            cell054.setCellValue("أسم الفراغ");
            cell054.setCellStyle(cellStyle);

            Cell cell044 = secondRow.createCell(7);
            cell044.setCellValue(jobOrderParent.getPandsToJobOrderList().get(0).getInstallationArea());
            cell044.setCellStyle(cellStyle);

//            secondRow.createCell(2).setCellValue("تاريخ الاصدار");
//            secondRow.createCell(3).setCellValue(lastJobOrder.getJobOrderDate() + " " + lastJobOrder.getJobOrderTime());
//            secondRow.createCell(6).setCellValue("أسم الفراغ");
//            secondRow.createCell(7).setCellValue(jobOrderParent.getPandsToJobOrderList().get(0).getInstallationArea());

            Row thirdRow = sheet.createRow(2);

            Cell cell043 = thirdRow.createCell(2);
            cell043.setCellValue("مكتب فنى");
            cell043.setCellStyle(cellStyle);

            sheet.addMergedRegion(new CellRangeAddress(2, 2, 3, 4));

            Cell cell033 = thirdRow.createCell(3);

            cell033.setCellStyle(cellStyle);

            Cell cell036 = thirdRow.createCell(6);
            cell036.setCellValue("أسم الدور");
            cell036.setCellStyle(cellStyle);

            Cell cell037 = thirdRow.createCell(7);
            cell037.setCellValue(jobOrderParent.getPandsToJobOrderList().get(0).getFloor());
            cell037.setCellStyle(cellStyle);

            Cell cell0371 = thirdRow.createCell(10);
            cell0371.setCellValue("ملاحظات فنية");
            cell0371.setCellStyle(cellStyle);

            sheet.addMergedRegion(new CellRangeAddress(2, 2, 11, 14));

            Cell cell0372 = thirdRow.createCell(11);

            cell0372.setCellStyle(cellStyle);

            Row headRow = sheet.createRow(4);
            headRow.createCell(0).setCellValue("البند");
            headRow.createCell(1).setCellValue("م");
            headRow.setHeightInPoints(20);
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 2, 3));

            Cell cell = headRow.createCell(2);
            cell.setCellValue("اسم الخامة");
            cell.setCellStyle(headerCellStyle);

            Cell cell101 = headRow.createCell(3);
            cell101.setCellStyle(headerCellStyle);

            sheet.addMergedRegion(new CellRangeAddress(4, 4, 4, 6));

            Cell cell02 = headRow.createCell(4);
            cell02.setCellValue("التوصيف");
            cell02.setCellStyle(headerCellStyle);

            Cell cell22 = headRow.createCell(5);
            cell22.setCellStyle(headerCellStyle);

            Cell cell33 = headRow.createCell(6);
            cell33.setCellStyle(headerCellStyle);
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 7, 8));

            Cell cell03 = headRow.createCell(7);
            cell03.setCellValue("المنطقة");
            cell03.setCellStyle(headerCellStyle);

            Cell cell44 = headRow.createCell(8);
            cell44.setCellStyle(headerCellStyle);


//            headRow.createCell(10).setCellValue("العدد");
            Cell cell04 = headRow.createCell(10);
            cell04.setCellValue("العدد");
            cell04.setCellStyle(headerCellStyle);
//            headRow.createCell(11).setCellValue("طول");
            Cell cell5 = headRow.createCell(11);
            cell5.setCellValue("طول");
            cell5.setCellStyle(headerCellStyle);
//            headRow.createCell(12).setCellValue("عرض");
            Cell cell6 = headRow.createCell(12);
            cell6.setCellValue("عرض");
            cell6.setCellStyle(headerCellStyle);
//            headRow.createCell(13).setCellValue("سمك");
            Cell cell07 = headRow.createCell(13);
            cell07.setCellValue("سمك");
            cell07.setCellStyle(headerCellStyle);
//            headRow.createCell(14).setCellValue("المعالجة");
            Cell cell8 = headRow.createCell(14);
            cell8.setCellValue("المعالجة");
            cell8.setCellStyle(headerCellStyle);
//            headRow.createCell(15).setCellValue("البلوك");
            Cell cell9 = headRow.createCell(15);
            cell9.setCellValue("البلوك");
            cell9.setCellStyle(headerCellStyle);


            int rowIdx = 6;

            List<String> jobOrdersByRawType = exitProcessJobOrderRepository.jobOrdersByRawType
                    (jobOrderParent.getPandsToJobOrderList().get(0).getProjectCode()
                            , jobOrderParent.getPandsToJobOrderList().get(0).getJobOrderId());



            List<ExitProcessJobOrder> jobOrdersByThickness = new ArrayList<>();
            String addDisc = "";

            for (int i = 0; i < jobOrdersByRawType.size(); i++) {
                jobOrdersByThickness.addAll(exitProcessJobOrderRepository.jobOrdersByUnit(jobOrderParent.getPandsToJobOrderList().get(0).getProjectCode()
                        , jobOrderParent.getPandsToJobOrderList().get(0).getJobOrderId()
                        , jobOrdersByRawType.get(i)));
            }
            int m = 1;
            for (int i = 0; i < jobOrdersByThickness.size(); i++) {

                Row row = sheet.createRow(rowIdx);
                row.setHeightInPoints(20);
                sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 1, 3));
                Cell cellRawType = row.createCell(1);
                cellRawType.setCellValue(jobOrdersByThickness.get(i).getRawType());
                cellRawType.setCellStyle(headerCellStyle);

                System.out.println(jobOrdersByThickness.get(i).getRawType());
                System.out.println("33333333333333333333333333");
                Cell cellStyile = row.createCell(2);
                cellStyile.setCellStyle(headerCellStyle);

                Cell cellStyile2 = row.createCell(3);
                cellStyile2.setCellStyle(headerCellStyle);

                Cell cellThickness = row.createCell(4);
                cellThickness.setCellValue(jobOrdersByThickness.get(i).getThickness() + "سم ");
                cellThickness.setCellStyle(headerCellStyle);

                rowIdx++;
                String discreption = "";
                if (jobOrderParent.getPandsToJobOrderList().get(i).getAdditionalDescription() == null) {
                    discreption = "";
                } else {
                    discreption = jobOrderParent.getPandsToJobOrderList().get(i).getAdditionalDescription();
                }

                addDisc += discreption + "\n";
                System.out.println("5555555555555555");

                Row jobOrderRow = sheet.createRow(rowIdx);
                jobOrderRow.setHeightInPoints(20);

                jobOrderRow.createCell(0).setCellValue(jobOrdersByThickness.get(i).getPandCode());
                jobOrderRow.createCell(1).setCellValue(m);
                System.out.println(jobOrdersByThickness.get(i).getPandCode());
//                sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 2, 3));
                Cell cellRawType2 = jobOrderRow.createCell(2);
                cellRawType2.setCellValue(jobOrdersByThickness.get(i).getRawType());
                cellRawType2.setCellStyle(headerCellStyle);
                System.out.println(jobOrdersByThickness.get(i).getRawType());

//                jobOrderRow.createCell(2).setCellValue(jobOrdersByThickness.get(i).getRawType());
                sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 4, 6));

                Cell cellDescription = jobOrderRow.createCell(4);
                cellDescription.setCellValue(jobOrdersByThickness.get(i).getDescription());
                cellDescription.setCellStyle(cellStyle);

                System.out.println(jobOrdersByThickness.get(i).getDescription());


                Cell cellDescription2 = jobOrderRow.createCell(5);
                cellDescription2.setCellStyle(cellStyle);

                Cell cellDescription3 = jobOrderRow.createCell(6);
                cellDescription3.setCellStyle(cellStyle);

                Cell cellRepetition = jobOrderRow.createCell(10);
                cellRepetition.setCellValue(jobOrdersByThickness.get(i).getQuantity());
                cellRepetition.setCellStyle(headerCellStyle);

                System.out.println(jobOrdersByThickness.get(i).getQuantity());


                Cell cellHeight = jobOrderRow.createCell(11);
                cellHeight.setCellValue(jobOrdersByThickness.get(i).getHeight());
                cellHeight.setCellStyle(cellStyle);
                System.out.println(jobOrdersByThickness.get(i).getHeight());

//                jobOrderRow.createCell(11).setCellValue(jobOrdersByThickness.get(i).getHeight());
                Cell cellWidth = jobOrderRow.createCell(12);
                cellWidth.setCellValue(jobOrdersByThickness.get(i).getWidth());
                cellWidth.setCellStyle(cellStyle);

                System.out.println(jobOrdersByThickness.get(i).getWidth());


                Cell cellThicknes = jobOrderRow.createCell(13);
                cellThicknes.setCellValue(jobOrdersByThickness.get(i).getThickness());
                cellThicknes.setCellStyle(cellStyle);

                System.out.println(jobOrdersByThickness.get(i).getThickness());


                Cell cellFinishType = jobOrderRow.createCell(14);
                cellFinishType.setCellValue(jobOrdersByThickness.get(i).getFinishType());
                cellFinishType.setCellStyle(cellStyle);

                System.out.println(jobOrdersByThickness.get(i).getFinishType());


                Cell cellBlock = jobOrderRow.createCell(15);
                cellBlock.setCellValue(jobOrdersByThickness.get(i).getBlockNumber());
                cellBlock.setCellStyle(cellStyle);

                System.out.println(jobOrdersByThickness.get(i).getBlockNumber());


                double result = 0.0;
                String unit = "";
                DecimalFormat df = new DecimalFormat("#,###.000");
                String formattedNumber = "";
                System.out.println("5555555555666666");

                System.out.println("unit " + (Double.valueOf(jobOrdersByThickness.get(i).getUnit())));
                System.out.println("height " + (Double.valueOf(jobOrdersByThickness.get(i).getHeight())));
                System.out.println("Width " + (Double.valueOf(jobOrdersByThickness.get(i).getWidth())));
                System.out.println("Quantity " + (Double.valueOf(jobOrdersByThickness.get(i).getQuantity())));
                if (jobOrdersByThickness.get(i).getUnit().equals("متر طولى")) {
                    result = (Double.valueOf(jobOrdersByThickness.get(i).getHeight())) *
                            (Double.valueOf(jobOrdersByThickness.get(i).getQuantity()));
                    formattedNumber = df.format(result / 100);
//                    row.createCell(13).setCellValue((result)/100);
                    Cell cell77 = row.createCell(16);
                    cell77.setCellValue((formattedNumber));
                    cell77.setCellStyle(headerCellStyle);
                    unit = "متر طولى";
                    System.out.println(formattedNumber);

                } else if (jobOrdersByThickness.get(i).getUnit().equals("متر مربع")) {
                    result = (Double.valueOf(jobOrdersByThickness.get(i).getHeight())) *
                            (Double.valueOf(jobOrdersByThickness.get(i).getWidth())) *
                            (Double.valueOf(jobOrdersByThickness.get(i).getQuantity()));
//                    row.createCell(13).setCellValue((result)/10000);
                    formattedNumber = df.format(result / 10000);
                    Cell cell77 = row.createCell(16);
                    cell77.setCellValue(formattedNumber);
                    cell77.setCellStyle(headerCellStyle);
                    unit = "متر مربع";
                    System.out.println(formattedNumber);

                } else {
                    Cell cell77 = row.createCell(16);
                    cell77.setCellValue(jobOrdersByThickness.get(i).getQuantity());
                    cell77.setCellStyle(headerCellStyle);
                    unit = "وحدة";
                    formattedNumber = String.valueOf(jobOrdersByThickness.get(i).getQuantity());
                    System.out.println(unit);
                }

                PandsToJobOrder pandsToJobOrder = pandsToJobOrderService.getByjobOrderAndPandId(jobOrdersByThickness.get(i).getJobOrderId(), jobOrdersByThickness.get(i).getPandCode());
                System.out.println("7777777777 " + result);
                System.out.println("9999999999999999" + Double.valueOf(pandsToJobOrder.getTotal()));
                System.out.println("00000000000" + Double.valueOf(formattedNumber));
                pandsToJobOrder.setTotal(String.valueOf(Double.valueOf(pandsToJobOrder.getTotal()) - Double.valueOf(formattedNumber)));
                System.out.println("888888888888888888");

                double result2 = pandsToJobOrder.getQuantity() - (jobOrdersByThickness.get(i).getQuantity());
                pandsToJobOrder.setQuantity(result2);

                Cell cell77 = row.createCell(17);
                cell77.setCellValue(unit);
                cell77.setCellStyle(headerCellStyle);

                System.out.println(unit);

                rowIdx++;
                m++;
            }

            cell0372.setCellValue(addDisc);
            thirdRow.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints())); // Adjust as necessary

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
            sheet.autoSizeColumn(14);

            System.err.println("rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");
            ByteArrayOutputStream fileOut = new ByteArrayOutputStream();
//            workbook = createCorePay(workbook);

            workbook.write(fileOut);
            workbook.close();

            exitProcessJobOrderRepository.deleteAll();
            System.err.println("zzzzzzzzzzzzzzzzzz");

            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
//            workbook.save(pdfOutputStream, SaveFormat.PDF);

//            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document();
//            PdfWriter pdfWriter = new PdfWriter(document, pdfOutputStream);
            PdfWriter.getInstance(document, pdfOutputStream);
//                Iterator<Row> rowIterator = sheet.iterator();
//                while (rowIterator.hasNext()) {
//                    Row currentRow = rowIterator.next();
//                    Iterator<Cell> cellIterator = currentRow.cellIterator();
//                    while (cellIterator.hasNext()) {
//                        Cell currentCell = cellIterator.next();
//                        document.add(new Paragraph(currentCell.toString()));
//                    }
//                }


            // Add a Table with 3 columns


            // Close the document
            document.close();

            // Return the PDF as a response
            ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(pdfInputStream);

//            byte[] pdfBytes = pdfOutputStream.toByteArray();

            return resource;

        } catch (Exception e) {

            System.err.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            System.out.println("cath");
            e.printStackTrace();
        }
        return null;
    }

    private List<PandsToJobOrder> getByJobOrder(String jobOrderId) {
        List<PandsToJobOrder> jobOrders = pandsToJobOrderService.getByJobOrderId(jobOrderId);

        return jobOrders;
    }

    public InputStreamResource getLastJobOrder(JobOrderParent jobOrderParent) {
        try {
            return buildFile(jobOrderParent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
