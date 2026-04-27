package com.example.demo.service;

import com.aspose.cells.*;
import com.example.demo.models.*;
import com.example.demo.repository.*;
import com.example.demo.service.workOrder.ExitJobOrderService;
import com.example.demo.service.workOrder.JobOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PdfFileService {

    @Autowired
    private ChangeHistoryLog changeHistoryLog;

    @Autowired
    JobOrderService jobOrderService;

    @Autowired
    PandsRepository pandsRepository;

    @Autowired
    ProjectProfileRepository projectProfileRepository;


    @Autowired
    PandsToJobOrderRepository pandsToJobOrderRepository;

    @Autowired
    ExitJobOrderService exitJobOrderService;

    @Autowired
    ExitProcessJobOrderRepository exitProcessJobOrderRepository;

    @Autowired
    ExitJobOrderRepository exitJobOrderRepository;


    //اذن خروج الانتاج التام
    @Transactional
    public InputStreamResource getPdf(JobOrderParent jobOrderParent, HttpServletRequest request) {
        String unifiedSerial = "";
        try {
            System.out.println("innnn getPDF");
            JobOrder lastJobOrder = jobOrderService.getByJobOrder(jobOrderParent.getPandsToJobOrderList().get(0).getJobOrderId());
            Workbook workbook = new Workbook();
            WorksheetCollection worksheets = workbook.getWorksheets();
            Worksheet sheet = worksheets.get(0);
            sheet.setDisplayRightToLeft(false);
//            sheet.getCells().setRowHeight(7, 20);

            PageSetup pageSetup = sheet.getPageSetup();
            pageSetup.setOrientation(PageOrientationType.LANDSCAPE);

            pageSetup.setFitToPagesWide(1); // Fit to 1 page width
            pageSetup.setFitToPagesTall(0); // Set to 0 for automatic height

            pageSetup.setTopMargin(1);
            pageSetup.setBottomMargin(1);
            pageSetup.setLeftMargin(1);
            pageSetup.setRightMargin(1);

            Style discriptionStyle = sheet.getCells().get("C1").getStyle();
            discriptionStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            discriptionStyle.getFont().setItalic(true);
            discriptionStyle.getFont().setSize(15);
            discriptionStyle.getFont().setBold(true);

            Style tableHeaderStyle = sheet.getCells().get("C1").getStyle();
            tableHeaderStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            tableHeaderStyle.setVerticalAlignment(TextAlignmentType.CENTER);
//            tableHeaderStyle.getFont().setItalic(true);
            tableHeaderStyle.getFont().setSize(15);
            tableHeaderStyle.getFont().setBold(false);
            tableHeaderStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

            Style discriptionDataStyle = sheet.getCells().get("F3").getStyle();
            discriptionDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            discriptionDataStyle.getFont().setSize(15);
            discriptionDataStyle.setTextWrapped(true); // Enable word wrap


            Style tableDataStyle = sheet.getCells().get("C1").getStyle();
            tableDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            tableHeaderStyle.getFont().setBold(true);
            tableDataStyle.getFont().setSize(15);

            Style underLineStyle = sheet.getCells().get("C1").getStyle();
            underLineStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            underLineStyle.getFont().setBold(true);
            underLineStyle.getFont().setUnderline(2);
            underLineStyle.getFont().setSize(15);

            Style shadowStyle = workbook.createStyle();

            // Set the background color for the style
            shadowStyle.setPattern(BackgroundType.SOLID);
            shadowStyle.setForegroundColor(Color.getDarkGray());
            shadowStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
//            underLineStyle.getFont().setBold(true);
//            underLineStyle.getFont().setUnderline(2);
            shadowStyle.getFont().setSize(15);


//            sheet.getCells().setColumnWidth(1, 0);

            sheet.getCells().get("B1").putValue("Work Order #");
            sheet.getCells().get("B1").setStyle(discriptionStyle);
            sheet.getCells().get("C1").putValue(jobOrderParent.getPandsToJobOrderList().get(0).getJobOrderId());
            sheet.getCells().get("C1").setStyle(discriptionDataStyle);

            sheet.getCells().get("B3").putValue("Created Date: ");
            sheet.getCells().get("B3").setStyle(discriptionStyle);

//            sheet.getCells().get("C2").putValue(lastJobOrder.getJobOrderDate());
//            sheet.getCells().get("C2").setStyle(discriptionDataStyle);
            sheet.getCells().get("C3").putValue(lastJobOrder.getJobOrderDate() + " " + lastJobOrder.getJobOrderTime());
            sheet.getCells().get("C3").setStyle(discriptionDataStyle);

            sheet.getCells().get("B5").putValue("Print Date");
            sheet.getCells().get("B5").setStyle(discriptionStyle);

            Date dNow = new Date();
            SimpleDateFormat ft =
                    new SimpleDateFormat("hh:mm:ss a");

            DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");


            sheet.getCells().get("C5").putValue(formatter1.format(dNow) + " " + ft.format(dNow));
            sheet.getCells().get("C5").setStyle(discriptionDataStyle);

            sheet.getCells().get("B7").putValue("Engineer Name");
            sheet.getCells().get("B7").setStyle(discriptionStyle);

            sheet.getCells().get("C7").putValue(jobOrderParent.getPandsToJobOrderList().getFirst().getOfficerName());
            sheet.getCells().get("C7").setStyle(discriptionDataStyle);

            sheet.getCells().get("D1").putValue("Project");
            sheet.getCells().get("D1").setStyle(discriptionStyle);

            sheet.getCells().get("E1").putValue(jobOrderParent.getPandsToJobOrderList().getFirst().getProjectCode() + " " + jobOrderParent.getPandsToJobOrderList().getFirst().getProjectName());
            sheet.getCells().get("E1").setStyle(discriptionDataStyle);

//            sheet.getCells().get("E2").putValue(jobOrderParent.getPandsToJobOrderList().get(0).getProjectName());
//            sheet.getCells().get("E2").setStyle(discriptionDataStyle);

            sheet.getCells().get("D3").putValue("Installation Area");
            sheet.getCells().get("D3").setStyle(discriptionStyle);

            sheet.getCells().get("E3").putValue(jobOrderParent.getPandsToJobOrderList().getFirst().getInstallationArea());
            sheet.getCells().get("E3").setStyle(discriptionDataStyle);

            sheet.getCells().get("D5").putValue("Floor");
            sheet.getCells().get("D5").setStyle(discriptionStyle);

            sheet.getCells().get("E5").putValue(jobOrderParent.getPandsToJobOrderList().getFirst().getFloor());
            sheet.getCells().get("E5").setStyle(discriptionDataStyle);

            ProjectProfile projectProfile = projectProfileRepository.getById(jobOrderParent.getPandsToJobOrderList().getFirst().getProjectProfileId());
            sheet.getCells().get("F1").putValue("Serial:");
            sheet.getCells().get("F1").setStyle(discriptionDataStyle);

            sheet.getCells().get("G1").putValue(projectProfile.getSerial());
            sheet.getCells().get("G1").setStyle(discriptionDataStyle);

            projectProfile.setSerial(projectProfile.getSerial() + 1);
            projectProfileRepository.save(projectProfile);

//            sheet.getCells().merge(3, 5, 3, 3);  // (startRow, startColumn, totalRows, totalColumns)


            sheet.getCells().get("A9").putValue("#");
            sheet.getCells().get("A9").setStyle(tableHeaderStyle);

            sheet.getCells().get("B9").putValue("Pand");
            sheet.getCells().get("B9").setStyle(tableHeaderStyle);

            sheet.getCells().get("C9").putValue("Material");
            sheet.getCells().get("C9").setStyle(tableHeaderStyle);

            sheet.getCells().get("D9").putValue("Description");
            sheet.getCells().get("D9").setStyle(tableHeaderStyle);

            sheet.getCells().get("E9").putValue("Area");
            sheet.getCells().get("E9").setStyle(tableHeaderStyle);

            sheet.getCells().get("F9").putValue("Quantity");
            sheet.getCells().get("F9").setStyle(tableHeaderStyle);

            sheet.getCells().get("G9").putValue("Height");
            sheet.getCells().get("G9").setStyle(tableHeaderStyle);

            sheet.getCells().get("H9").putValue("Width");
            sheet.getCells().get("H9").setStyle(tableHeaderStyle);

            sheet.getCells().get("I9").putValue("Thickness");
            sheet.getCells().get("I9").setStyle(tableHeaderStyle);

            sheet.getCells().get("J9").putValue("Finishing");
            sheet.getCells().get("J9").setStyle(tableHeaderStyle);

            sheet.getCells().get("K9").putValue("Block");
            sheet.getCells().get("K9").setStyle(tableHeaderStyle);

            sheet.getCells().get("L9").putValue("Total");
            sheet.getCells().get("L9").setStyle(tableHeaderStyle);

            sheet.getCells().get("M9").putValue("Unit");
            sheet.getCells().get("M9").setStyle(tableHeaderStyle);


//            sheet.getCells().merge(0, 6, 3, 2);  // (startRow, startColumn, totalRows, totalColumns)

            InputStream imageStream = new ClassPathResource("static/ORYX.jpeg").getInputStream();

            // Add the image to the worksheet (X, Y coordinates in pixels)
            // Place the image inside the merged cells (A1:C5)
            int pictureIndex = sheet.getPictures().add(0, 11, imageStream);

            // Get the added picture
            Picture picture = sheet.getPictures().get(pictureIndex);

            // Optionally, set the picture to fit within the merged area
            picture.setPlacement(PlacementType.MOVE);
            picture.setWidthScale(60); // Scale the image to fit width
            picture.setHeightScale(40); // Scale the image to fit height

            int rowIdx = 11;

            List<String> jobOrdersByRawType = exitProcessJobOrderRepository.jobOrdersByRawType
                    (jobOrderParent.getPandsToJobOrderList().get(0).getProjectCode()
                            , jobOrderParent.getPandsToJobOrderList().get(0).getJobOrderId());

//            unifiedSerial = jobOrdersByRawType.get(0).getUnifiedSerial();


            sheet.getCells().merge(2, 6, 1, 4);  // (startRow, startColumn, totalRows, totalColumns)

//            sheet.getCells().get("G3").putValue(jobOrdersByRawType.get(0).getSerialNumber());
//            sheet.getCells().get("G3").setStyle(discriptionDataStyle);
//
//            sheet.getCells().get("G5").putValue(jobOrdersByRawType.get(0).getUnifiedSerial());
//            sheet.getCells().get("G5").setStyle(discriptionDataStyle);

            List<ExitProcessJobOrder> jobOrdersByThickness = new ArrayList<>();
            String addDisc = "";

            int m = 1;
            int i = 0;
            DecimalFormat df = new DecimalFormat("#.###");
            for (int k = 0; k < jobOrdersByRawType.size(); k++) {
                jobOrdersByThickness.addAll(exitProcessJobOrderRepository.jobOrdersByUnit(jobOrderParent.getPandsToJobOrderList().getFirst().getProjectCode()
                        , jobOrderParent.getPandsToJobOrderList().getFirst().getJobOrderId()
                        , jobOrdersByRawType.get(k)));
                System.out.println("jobOrdersByThickness size: " + jobOrdersByThickness.size());
                System.out.println("out k=0");
                if (k == 0) {
                    System.out.println("innn k=0");
                    unifiedSerial = jobOrdersByThickness.get(0).getUnifiedSerial();

                    sheet.getCells().get("G3").putValue(jobOrdersByThickness.getFirst().getSerialNumber());
                    sheet.getCells().get("G3").setStyle(discriptionDataStyle);

                    sheet.getCells().get("G5").putValue(jobOrdersByThickness.getFirst().getUnifiedSerial());
                    sheet.getCells().get("G5").setStyle(discriptionDataStyle);
                }

                double totalQuantity = 0;
                double total = 0.0;

                for (; i < jobOrdersByThickness.size(); i++) {
                    System.out.println("jobOrdersByThickness" + i);
//                rowIdx++;
                    String discreption = "";
                    if (jobOrderParent.getPandsToJobOrderList().get(i).getAdditionalDescription() == null) {
                        discreption = "";
                    } else {
                        discreption = jobOrderParent.getPandsToJobOrderList().get(i).getAdditionalDescription();
                    }

                    addDisc += discreption + "\n";
                    System.out.println("5555555555555555");

                    sheet.getCells().get("A" + rowIdx).putValue(m);

                    sheet.getCells().get("A" + rowIdx).setStyle(tableHeaderStyle);


                    sheet.getCells().get("B" + rowIdx).putValue(jobOrdersByThickness.get(i).getPandCode());

                    sheet.getCells().get("B" + rowIdx).setStyle(tableHeaderStyle);

                    sheet.getCells().get("C" + rowIdx).putValue(jobOrdersByThickness.get(i).getRawType());
                    sheet.getCells().get("C" + rowIdx).setStyle(tableHeaderStyle);

                    sheet.getCells().get("D" + rowIdx).putValue(jobOrdersByThickness.get(i).getDescription());
                    sheet.getCells().get("D" + rowIdx).setStyle(tableHeaderStyle);

                    sheet.getCells().get("E" + rowIdx).setStyle(tableHeaderStyle);

                    totalQuantity += jobOrdersByThickness.get(i).getQuantity();
                    sheet.getCells().get("F" + rowIdx).putValue(jobOrdersByThickness.get(i).getQuantity());

                    sheet.getCells().get("F" + rowIdx).setStyle(tableHeaderStyle);

                    sheet.getCells().get("G" + rowIdx).putValue(jobOrdersByThickness.get(i).getHeight());

                    sheet.getCells().get("G" + rowIdx).setStyle(tableHeaderStyle);

                    sheet.getCells().get("H" + rowIdx).putValue(jobOrdersByThickness.get(i).getWidth());

                    sheet.getCells().get("H" + rowIdx).setStyle(tableHeaderStyle);

                    sheet.getCells().get("I" + rowIdx).putValue(jobOrdersByThickness.get(i).getThickness());

                    sheet.getCells().get("I" + rowIdx).setStyle(tableHeaderStyle);

                    sheet.getCells().get("J" + rowIdx).putValue(jobOrdersByThickness.get(i).getFinishType());

                    sheet.getCells().get("J" + rowIdx).setStyle(tableHeaderStyle);

                    sheet.getCells().get("K" + rowIdx).putValue(jobOrdersByThickness.get(i).getBlockNumber());

                    sheet.getCells().get("K" + rowIdx).setStyle(tableHeaderStyle);


                    double result = 0.0;
                    String unit = "";

                    String formattedNumber = "";
                    if (jobOrdersByThickness.get(i).getUnit().equals("Longitudinal meter")) {
                        result = (Double.parseDouble(jobOrdersByThickness.get(i).getHeight())) *
                                (jobOrdersByThickness.get(i).getQuantity());
                        formattedNumber = df.format(result / 100);
                        unit = "Longitudinal meter";

                    } else if (jobOrdersByThickness.get(i).getUnit().equals("Square Meter")) {
                        result = (Double.parseDouble(jobOrdersByThickness.get(i).getHeight())) *
                                (Double.parseDouble(jobOrdersByThickness.get(i).getWidth())) *
                                (jobOrdersByThickness.get(i).getQuantity());
                        formattedNumber = df.format(result / 10000);
                        unit = "Square Meter";

                    } else {
                        unit = "Unit";
                        formattedNumber = String.valueOf(jobOrdersByThickness.get(i).getQuantity());
                        sheet.getCells().get("L" + rowIdx).putValue(formattedNumber + " " + unit);
                    }

                    total += Double.parseDouble(formattedNumber);

                    sheet.getCells().get("L" + rowIdx).putValue(formattedNumber);
                    sheet.getCells().get("M" + rowIdx).putValue(unit);


                    sheet.getCells().get("L" + rowIdx).setStyle(tableHeaderStyle);
                    sheet.getCells().get("M" + rowIdx).setStyle(tableHeaderStyle);

//                    PandsToJobOrder pandsToJobOrder = pandsToJobOrderRepository.getByUniqueIdAndJobOrderId(jobOrdersByThickness.get(i).getUniqueId(), jobOrdersByThickness.get(i).getJobOrderId());
//                    String totalAfterExit = String.valueOf(df.format(Double.valueOf(pandsToJobOrder.getTotal()) - Double.valueOf(formattedNumber)));
//                    pandsToJobOrder.setTotal(totalAfterExit);
//                    double result2 = pandsToJobOrder.getQuantity() - jobOrdersByThickness.get(i).getQuantity();
//                    pandsToJobOrder.setQuantity(result2);
//                    pandsToJobOrderRepository.save(pandsToJobOrder);

                    rowIdx++;
                    m++;
                }


                sheet.getCells().get("F" + rowIdx).putValue(totalQuantity);
                sheet.getCells().get("F" + rowIdx).setStyle(shadowStyle);

                sheet.getCells().get("L" + rowIdx).putValue(total);
                sheet.getCells().get("L" + rowIdx).setStyle(shadowStyle);
                rowIdx += 2;

            }

            sheet.getCells().setRowHeight(6, 25);


            ///////////////////////////////////////////////////////////////
            //BILL DETAILS

            System.out.println("11111111111111");

            sheet.getHorizontalPageBreaks().add("A" + rowIdx);


            sheet.getCells().get("D" + rowIdx).putValue("Project");
            sheet.getCells().get("D" + rowIdx).setStyle(discriptionStyle);

            sheet.getCells().get("E" + rowIdx).putValue(jobOrderParent.getPandsToJobOrderList().get(0).getProjectCode() + " " + jobOrderParent.getPandsToJobOrderList().get(0).getProjectName());
            sheet.getCells().get("E" + rowIdx).setStyle(discriptionDataStyle);

            sheet.getCells().get("G" + rowIdx).putValue("Work Order #");
            sheet.getCells().get("G" + rowIdx).setStyle(discriptionStyle);
            sheet.getCells().get("H" + rowIdx).putValue(jobOrderParent.getPandsToJobOrderList().get(0).getJobOrderId());
            sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);

            InputStream billImageStream = new ClassPathResource("static/ORYX.jpeg").getInputStream();

            // Add the image to the worksheet (X, Y coordinates in pixels)
            // Place the image inside the merged cells (A1:C5)
            int billPictureIndex = sheet.getPictures().add(rowIdx, 11, billImageStream);

            // Get the added picture
            Picture billPicture = sheet.getPictures().get(billPictureIndex);

            // Optionally, set the picture to fit within the merged area
            billPicture.setPlacement(PlacementType.MOVE);
            billPicture.setWidthScale(60); // Scale the image to fit width
            billPicture.setHeightScale(40); // Scale the image to fit height

            rowIdx += 2;

            sheet.getCells().get("D" + rowIdx).putValue("Pand");
            sheet.getCells().get("D" + rowIdx).setStyle(tableHeaderStyle);

            sheet.getCells().get("E" + rowIdx).putValue("Material");
            sheet.getCells().get("E" + rowIdx).setStyle(tableHeaderStyle);

            sheet.getCells().get("F" + rowIdx).putValue("Quantity");
            sheet.getCells().get("F" + rowIdx).setStyle(tableHeaderStyle);

            sheet.getCells().get("G" + rowIdx).putValue("Price");
            sheet.getCells().get("G" + rowIdx).setStyle(tableHeaderStyle);

            sheet.getCells().get("H" + rowIdx).putValue("Total");
            sheet.getCells().get("H" + rowIdx).setStyle(tableHeaderStyle);


            List<String> pands = exitProcessJobOrderRepository.getJobOrderDetails(jobOrderParent.getPandsToJobOrderList().getFirst().getProjectCode(), jobOrderParent.getPandsToJobOrderList().getFirst().getJobOrderId());

            System.out.println("2222222222222222");
            double totalBillQuantity = 0.0;
            double billTotalPrice = 0.0;
            double totalQuantityInMeter = 0.0;
            for (int k = 0; k < pands.size(); k++) {
                System.out.println("pands " + i);
                rowIdx++;
                Pand pand = pandsRepository.findByPandCodeAndProjectProfileId(pands.get(k), jobOrderParent.getPandsToJobOrderList().get(0).getProjectProfileId());
                System.out.println(pand.getPandCode());
                sheet.getCells().get("D" + rowIdx).putValue(pand.getPandCode());
                sheet.getCells().get("D" + rowIdx).setStyle(tableHeaderStyle);

                sheet.getCells().get("E" + rowIdx).putValue(pand.getRawType());
                sheet.getCells().get("E" + rowIdx).setStyle(tableHeaderStyle);

                totalQuantityInMeter = exitProcessJobOrderRepository.getTotalJobOrderForBill(jobOrderParent.getPandsToJobOrderList().get(0).getProjectCode(), jobOrderParent.getPandsToJobOrderList().get(0).getJobOrderId(), pand.getPandCode());

                totalBillQuantity += totalQuantityInMeter;
                sheet.getCells().get("F" + rowIdx).putValue(df.format(totalQuantityInMeter));
                sheet.getCells().get("F" + rowIdx).setStyle(tableHeaderStyle);

                String cost = df.format(totalQuantityInMeter * pand.getPrice());
                billTotalPrice += Double.parseDouble(cost);
                sheet.getCells().get("G" + rowIdx).putValue(pand.getPrice());
                sheet.getCells().get("G" + rowIdx).setStyle(tableHeaderStyle);

                sheet.getCells().get("H" + rowIdx).putValue(cost);
                sheet.getCells().get("H" + rowIdx).setStyle(tableHeaderStyle);
            }
            rowIdx++;

            sheet.getCells().get("F" + rowIdx).putValue(totalBillQuantity);
            sheet.getCells().get("F" + rowIdx).setStyle(tableHeaderStyle);

            sheet.getCells().get("H" + rowIdx).putValue(billTotalPrice);
            sheet.getCells().get("H" + rowIdx).setStyle(tableHeaderStyle);


            //////////////////////////////////////////////////////////////////////
            // Adjust column widths to fit content

            for (int j = 8; j < rowIdx; j++) {
                sheet.getCells().setRowHeight(j, 25);
            }

            sheet.autoFitColumns();

            // 3. Convert Excel to PDF
            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
            workbook.save(pdfOutputStream, SaveFormat.PDF); // Save as PDF

            // 4. Return the PDF as a response
            ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(pdfInputStream);

            deductingQuantityFromPands(jobOrderParent);

            exitProcessJobOrderRepository.deleteAll();

            changeHistoryLog.saveChange(jobOrderParent.getPandsToJobOrderList().getFirst().getJobOrderId(), jobOrderParent.getPandsToJobOrderList().toString(), jobOrderParent.getPandsToJobOrderList().toString(), "exit job order", request);


            return resource;
        } catch (Exception e) {
            exitProcessJobOrderRepository.deleteAll();
            exitJobOrderRepository.deleteByUnifiedSerial(unifiedSerial);
            e.printStackTrace();
        }
        return null;
    }

    public void deductingQuantityFromPands(JobOrderParent jobOrderParent) {
        try {

            List<String> jobOrdersByRawType = exitProcessJobOrderRepository.jobOrdersByRawType
                    (jobOrderParent.getPandsToJobOrderList().getFirst().getProjectCode()
                            , jobOrderParent.getPandsToJobOrderList().getFirst().getJobOrderId());

            List<ExitProcessJobOrder> jobOrdersByThickness = new ArrayList<>();

            int m = 1;

            DecimalFormat df = new DecimalFormat("#.###");
            int i = 0;
            for (int k = 0; k < jobOrdersByRawType.size(); k++) {
                System.out.println("jobOrdersByRawType " + k);
                jobOrdersByThickness.addAll(exitProcessJobOrderRepository.jobOrdersByUnit(jobOrderParent.getPandsToJobOrderList().getFirst().getProjectCode()
                        , jobOrderParent.getPandsToJobOrderList().getFirst().getJobOrderId()
                        , jobOrdersByRawType.get(k)));

                for (; i < jobOrdersByThickness.size(); i++) {
                    System.out.println("jobOrdersByThickness " + i);
                    double result = 0.0;
                    String formattedNumber = "";
                    if (jobOrdersByThickness.get(i).getUnit().equals("Longitudinal meter")) {
                        result = (Double.parseDouble(jobOrdersByThickness.get(i).getHeight())) *
                                (jobOrdersByThickness.get(i).getQuantity());
                        formattedNumber = df.format(result / 100);

                    } else if (jobOrdersByThickness.get(i).getUnit().equals("Square Meter")) {
                        result = (Double.parseDouble(jobOrdersByThickness.get(i).getHeight())) *
                                (Double.parseDouble(jobOrdersByThickness.get(i).getWidth())) *
                                (jobOrdersByThickness.get(i).getQuantity());
                        formattedNumber = df.format(result / 10000);

                    } else {
                        formattedNumber = String.valueOf(jobOrdersByThickness.get(i).getQuantity());
                    }

                    PandsToJobOrder pandsToJobOrder = pandsToJobOrderRepository.findByUniqueIdAndJobOrderIdAndWidthAndHeight
                            (jobOrdersByThickness.get(i).getUniqueId(), jobOrdersByThickness.get(i).getJobOrderId()
                            ,jobOrdersByThickness.get(i).getWidth(),jobOrdersByThickness.get(i).getHeight());
                    String totalAfterExit = String.valueOf(df.format(Double.parseDouble(pandsToJobOrder.getTotal()) - Double.parseDouble(formattedNumber)));
                    pandsToJobOrder.setTotal(totalAfterExit);
                    double result2 = pandsToJobOrder.getQuantity() - jobOrdersByThickness.get(i).getQuantity();
                    pandsToJobOrder.setQuantity(result2);
                    pandsToJobOrderRepository.save(pandsToJobOrder);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public InputStreamResource getRawsDetails(String id) throws Exception {

        Workbook workbook = new Workbook();
        WorksheetCollection worksheets = workbook.getWorksheets();
        Worksheet sheet = worksheets.get(0);
        sheet.setDisplayRightToLeft(true);
        Cells cells = sheet.getCells();

        PageSetup pageSetup = sheet.getPageSetup();
        pageSetup.setFooter(1, "Page &P of &N");
        pageSetup.setOrientation(PageOrientationType.LANDSCAPE);

        pageSetup.setFitToPagesWide(1); // Fit to 1 page width
        pageSetup.setFitToPagesTall(0); // Set to 0 for automatic height

        pageSetup.setTopMargin(1);
        pageSetup.setBottomMargin(1);
        pageSetup.setLeftMargin(1);
        pageSetup.setRightMargin(1);

        JobOrder jobOrder = jobOrderService.getByJobOrder(id);

//        List<PandsToJobOrder> pandsToJobOrders = pandsToJobOrderRepository.allJobOrderIdGroupByRawType(id);

        Style tableHeaderStyle = sheet.getCells().get("C1").getStyle();
        tableHeaderStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        tableHeaderStyle.setVerticalAlignment(TextAlignmentType.CENTER);
        tableHeaderStyle.getFont().setItalic(false);
        tableHeaderStyle.getFont().setSize(12);
        tableHeaderStyle.getFont().setBold(false);
        tableHeaderStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());


        Style discriptionDataStyle = sheet.getCells().get("F3").getStyle();
        discriptionDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        discriptionDataStyle.setVerticalAlignment(TextAlignmentType.CENTER);
        discriptionDataStyle.getFont().setSize(12);

        Style shadowStyle = workbook.createStyle();
        shadowStyle.setPattern(BackgroundType.SOLID);
        shadowStyle.setForegroundColor(Color.getDarkGray());
        shadowStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        shadowStyle.getFont().setSize(12);

//        List<ExitJobOrder> pands = exitJobOrderService.getByJobOrderId(id);

        sheet.getCells().get("A1").putValue("أسم المشروع: ");
        sheet.getCells().get("A1").setStyle(discriptionDataStyle);

//        sheet.getCells().get("B1").putValue(pandsToJobOrders.get(0).getProjectName());
//        sheet.getCells().get("B1").setStyle(discriptionDataStyle);

        sheet.getCells().get("A3").putValue("كود المشروع: ");
        sheet.getCells().get("A3").setStyle(discriptionDataStyle);

        sheet.getCells().get("B3").putValue(jobOrder.getProjectCode());
        sheet.getCells().get("B3").setStyle(discriptionDataStyle);

        sheet.getCells().get("A5").putValue("رقم أمر الشغل");
        sheet.getCells().get("A5").setStyle(discriptionDataStyle);

        sheet.getCells().get("B5").putValue(jobOrder.getJobOrderNumber());
        sheet.getCells().get("B5").setStyle(discriptionDataStyle);

//        sheet.getCells().get("D1").putValue("المهندس المسؤول: ");
//        sheet.getCells().get("D1").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("E1").putValue(pands.get(0).getEngineerName());
//        sheet.getCells().get("E1").setStyle(discriptionDataStyle);

        sheet.getCells().get("D3").putValue("تاريخ أمر الشغل");
        sheet.getCells().get("D3").setStyle(discriptionDataStyle);

        sheet.getCells().merge(2, 4, 1, 2);  // (startRow, startColumn, totalRows, totalColumns)

        sheet.getCells().get("E3").putValue(jobOrder.getJobOrderDate() + " " + jobOrder.getJobOrderTime());
        sheet.getCells().get("E3").setStyle(discriptionDataStyle);

        sheet.getCells().get("A5").putValue("كود البند");
        sheet.getCells().get("A5").setStyle(tableHeaderStyle);

//        sheet.getCells().get("B5").putValue("التوصيف");
//        sheet.getCells().get("B5").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("C5").putValue("التصنيع");
//        sheet.getCells().get("C5").setStyle(tableHeaderStyle);

        sheet.getCells().get("B5").putValue("الخامة الفعلية");
        sheet.getCells().get("B5").setStyle(tableHeaderStyle);

        sheet.getCells().get("C5").putValue("الخامة المستخدمة");
        sheet.getCells().get("C5").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("F5").putValue("نوع التشطيب");
//        sheet.getCells().get("F5").setStyle(tableHeaderStyle);

//        sheet.getCells().get("G5").putValue("السمك");
//        sheet.getCells().get("G5").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("H5").putValue("الطول");
//        sheet.getCells().get("H5").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("I5").putValue("العرض");
//        sheet.getCells().get("I5").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("J5").putValue("الوحدة");
//        sheet.getCells().get("J5").setStyle(tableHeaderStyle);

        sheet.getCells().get("D5").putValue("الاجمالى الفعلى");
        sheet.getCells().get("D5").setStyle(tableHeaderStyle);

        sheet.getCells().get("E5").putValue("الاجمالى المستخدم");
        sheet.getCells().get("E5").setStyle(tableHeaderStyle);

        sheet.getCells().get("F5").putValue("الاجمالى المتاح");
        sheet.getCells().get("F5").setStyle(tableHeaderStyle);

        sheet.getCells().get("G5").putValue("الكمية العلية");
        sheet.getCells().get("G5").setStyle(tableHeaderStyle);

        sheet.getCells().get("H5").putValue("الكمية المستخدمة");
        sheet.getCells().get("H5").setStyle(tableHeaderStyle);

        sheet.getCells().get("I5").putValue("الكمية المتاحة");
        sheet.getCells().get("I5").setStyle(tableHeaderStyle);

        InputStream imageStream = new ClassPathResource("static/Hossam-Zeitoun-Logo-Black.png").getInputStream();

        // Add the image to the worksheet (X, Y coordinates in pixels)
        // Place the image inside the merged cells (A1:C5)
        int pictureIndex = sheet.getPictures().add(0, 9, imageStream);

        // Get the added picture
        Picture picture = sheet.getPictures().get(pictureIndex);

        // Optionally, set the picture to fit within the merged area
        picture.setPlacement(PlacementType.MOVE);
        picture.setWidthScale(20); // Scale the image to fit width
        picture.setHeightScale(10);

        int rowIdx = 7;

        DecimalFormat df = new DecimalFormat("#,###.000");
        String formattedNumber = "";

//        for (int i = 0; i < pandsToJobOrders.size(); i++) {
//
//
//            sheet.getCells().get("A" + rowIdx).putValue(pandsToJobOrders.get(i).getPandCode());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("A" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("A" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
////            sheet.getCells().get("B" + rowIdx).putValue(pands.get(i).getDescription());
////            if (rowIdx % 2 != 0) {
////                sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);
////            } else {
////                sheet.getCells().get("B" + rowIdx).setStyle(discriptionDataStyle);
////            }
////
////            sheet.getCells().get("C" + rowIdx).putValue(pands.get(i).getManufacturing());
////            if (rowIdx % 2 != 0) {
////                sheet.getCells().get("C" + rowIdx).setStyle(shadowStyle);
////            } else {
////                sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);
////            }
//
//            sheet.getCells().get("B" + rowIdx).putValue(pandsToJobOrders.get(i).getRawType());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("B" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            sheet.getCells().get("C" + rowIdx).putValue(pandsToJobOrders.get(i).getRawUsed());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("C" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
////            sheet.getCells().get("F" + rowIdx).putValue(pands.get(i).getFinishType());
////            if (rowIdx % 2 != 0) {
////                sheet.getCells().get("F" + rowIdx).setStyle(shadowStyle);
////            } else {
////                sheet.getCells().get("F" + rowIdx).setStyle(discriptionDataStyle);
////            }
////
////            sheet.getCells().get("G" + rowIdx).putValue(pands.get(i).getThickness());
////            if (rowIdx % 2 != 0) {
////                sheet.getCells().get("G" + rowIdx).setStyle(shadowStyle);
////            } else {
////                sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);
////            }
////
////
////            sheet.getCells().get("H" + rowIdx).putValue(pands.get(i).getHeight());
////            if (rowIdx % 2 != 0) {
////                sheet.getCells().get("H" + rowIdx).setStyle(shadowStyle);
////            } else {
////                sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);
////            }
////
////            sheet.getCells().get("I" + rowIdx).putValue(pands.get(i).getWidth());
////            if (rowIdx % 2 != 0) {
////                sheet.getCells().get("I" + rowIdx).setStyle(shadowStyle);
////            } else {
////                sheet.getCells().get("I" + rowIdx).setStyle(discriptionDataStyle);
////            }
////
////            sheet.getCells().get("J" + rowIdx).putValue(pands.get(i).getUnit());
////            if (rowIdx % 2 != 0) {
////                sheet.getCells().get("J" + rowIdx).setStyle(shadowStyle);
////            } else {
////                sheet.getCells().get("J" + rowIdx).setStyle(discriptionDataStyle);
////            }
//
//            System.out.println("///////555555555555555555555/////////");
//            Double maintotal = pandsToJobOrderRepository.getSumByRawType(jobOrder.getProjectProfileId(), id, pandsToJobOrders.get(i).getRawType());
//            formattedNumber = df.format(maintotal);
//            sheet.getCells().get("D" + rowIdx).putValue(formattedNumber);
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("D" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            Double usedtotal = exitJobOrderRepository.getSumByRawType(jobOrder.getProjectCode(), id, pandsToJobOrders.get(i).getRawType());
//            formattedNumber = df.format(usedtotal);
//            sheet.getCells().get("E" + rowIdx).putValue(formattedNumber);
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("E" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("E" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            Double availableTotal = maintotal - usedtotal;
//
//            sheet.getCells().get("F" + rowIdx).putValue(df.format(availableTotal));
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("F" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("F" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            Double mainQuantity = pandsToJobOrderRepository.sumQuantityByRaw(jobOrder.getProjectProfileId(), id, pandsToJobOrders.get(i).getRawType());
//
//            sheet.getCells().get("G" + rowIdx).putValue(df.format(mainQuantity));
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("G" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            Double usedQuantity = exitJobOrderRepository.sumQuantityByRawType(jobOrder.getProjectCode(), id, pandsToJobOrders.get(i).getRawType());
//
//
//            sheet.getCells().get("H" + rowIdx).putValue(df.format(usedQuantity));
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("H" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            Double availableQuantity = mainQuantity - usedQuantity;
//
//            sheet.getCells().get("I" + rowIdx).putValue(df.format(availableQuantity));
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("I" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("I" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            rowIdx++;
//        }


//        for (
//                int i = 4;
//                i < rowIdx; i++) {
//            sheet.getCells().setRowHeight(i, 18);
//        }

        //////////////////////////////////////////////////////////////////////
        // Adjust column widths to fit content
        sheet.autoFitColumns();

//        for (
//                int i = 6;
//                i < rowIdx; i++) {
//            sheet.getCells().setRowHeight(i, 25);
//        }


        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
//        workbook.save(pdfOutputStream, SaveFormat.PDF); // Save as PDF

        // 4. Return the PDF as a response
        ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
        InputStreamResource resource = new InputStreamResource(pdfInputStream);
        System.out.println("exiiiiiit");
        return resource;
    }


    public InputStreamResource getPdfBySerial(String serial) {
        try {

            List<ExitJobOrder> allExitJobOrder = exitJobOrderRepository.getBySerial(serial);
            JobOrder lastJobOrder = jobOrderService.getByJobOrder(allExitJobOrder.get(0).getJobOrderId());
            Workbook workbook = new Workbook();
            WorksheetCollection worksheets = workbook.getWorksheets();
            Worksheet sheet = worksheets.get(0);
            sheet.setDisplayRightToLeft(false);
//            sheet.getCells().setRowHeight(7, 20);
            Date dNow = new Date();
            SimpleDateFormat ft =
                    new SimpleDateFormat("hh:mm:ss a");

            DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");

            PageSetup pageSetup = sheet.getPageSetup();
            pageSetup.setOrientation(PageOrientationType.LANDSCAPE);

            pageSetup.setFitToPagesWide(1); // Fit to 1 page width
            pageSetup.setFitToPagesTall(0); // Set to 0 for automatic height

            pageSetup.setTopMargin(1);
            pageSetup.setBottomMargin(1);
            pageSetup.setLeftMargin(1);
            pageSetup.setRightMargin(1);

            Style discriptionStyle = sheet.getCells().get("C1").getStyle();
            discriptionStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            discriptionStyle.getFont().setItalic(true);
            discriptionStyle.getFont().setSize(11);
//            discriptionStyle.getFont().setBold(true);

            Style tableHeaderStyle = sheet.getCells().get("C1").getStyle();
            tableHeaderStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            tableHeaderStyle.setVerticalAlignment(TextAlignmentType.CENTER);
//            tableHeaderStyle.getFont().setItalic(true);
            tableHeaderStyle.getFont().setSize(11);
            tableHeaderStyle.getFont().setBold(false);
            tableHeaderStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

            Style discriptionDataStyle = sheet.getCells().get("F3").getStyle();
            discriptionDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            discriptionDataStyle.getFont().setSize(11);
            discriptionDataStyle.setTextWrapped(true); // Enable word wrap


            Style tableDataStyle = sheet.getCells().get("C1").getStyle();
            tableDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            tableHeaderStyle.getFont().setBold(true);
            tableDataStyle.getFont().setSize(11);

            Style underLineStyle = sheet.getCells().get("C1").getStyle();
            underLineStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            underLineStyle.getFont().setBold(true);
            underLineStyle.getFont().setUnderline(2);
            underLineStyle.getFont().setSize(11);

            Style shadowStyle = workbook.createStyle();

            // Set the background color for the style
            shadowStyle.setPattern(BackgroundType.SOLID);
            shadowStyle.setForegroundColor(Color.getDarkGray());
            shadowStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
//            underLineStyle.getFont().setBold(true);
//            underLineStyle.getFont().setUnderline(2);
            shadowStyle.getFont().setSize(11);


//            sheet.getCells().setColumnWidth(1, 0);

            sheet.getCells().get("B1").putValue("Job Order #");
            sheet.getCells().get("B1").setStyle(discriptionStyle);
            sheet.getCells().get("C1").putValue(allExitJobOrder.get(0).getJobOrderId());
            sheet.getCells().get("C1").setStyle(discriptionDataStyle);

            sheet.getCells().get("B3").putValue("Created Date");
            sheet.getCells().get("B3").setStyle(discriptionStyle);

//            sheet.getCells().get("C2").putValue(lastJobOrder.getJobOrderDate());
//            sheet.getCells().get("C2").setStyle(discriptionDataStyle);
            sheet.getCells().get("C3").putValue(lastJobOrder.getJobOrderDate() + " " + lastJobOrder.getJobOrderTime());
            sheet.getCells().get("C3").setStyle(discriptionDataStyle);

            sheet.getCells().get("B5").putValue("Engineer Name");
            sheet.getCells().get("B5").setStyle(discriptionStyle);

            sheet.getCells().get("C5").putValue(allExitJobOrder.get(0).getOfficerName());
            sheet.getCells().get("C5").setStyle(discriptionDataStyle);

            sheet.getCells().get("B7").putValue("Print Date");
            sheet.getCells().get("B7").setStyle(discriptionDataStyle);

            sheet.getCells().get("C7").putValue(formatter1.format(dNow) + " " + ft.format(dNow).toString());
            sheet.getCells().get("C7").setStyle(discriptionDataStyle);

            sheet.getCells().get("D1").putValue("Pand Code");
            sheet.getCells().get("D1").setStyle(discriptionStyle);

            sheet.getCells().get("E1").putValue(allExitJobOrder.get(0).getProjectCode() + " " + allExitJobOrder.get(0).getProjectName());
            sheet.getCells().get("E1").setStyle(discriptionDataStyle);

//            sheet.getCells().get("E2").putValue(jobOrderParent.getPandsToJobOrderList().get(0).getProjectName());
//            sheet.getCells().get("E2").setStyle(discriptionDataStyle);

            sheet.getCells().get("D3").putValue("Area");
            sheet.getCells().get("D3").setStyle(discriptionStyle);

            sheet.getCells().get("E3").putValue(allExitJobOrder.get(0).getInstallationArea());
            sheet.getCells().get("E3").setStyle(discriptionDataStyle);

            sheet.getCells().get("D5").putValue("Floor");
            sheet.getCells().get("D5").setStyle(discriptionStyle);

            sheet.getCells().get("E5").putValue(allExitJobOrder.get(0).getFloor());
            sheet.getCells().get("E5").setStyle(discriptionDataStyle);

            ProjectProfile projectProfile = projectProfileRepository.getById(allExitJobOrder.get(0).getProjectProfileId());
            sheet.getCells().get("F1").putValue("Serial:");
            sheet.getCells().get("F1").setStyle(discriptionDataStyle);

            sheet.getCells().get("G1").putValue(projectProfile.getSerial() - 1);
            sheet.getCells().get("G1").setStyle(discriptionDataStyle);

//            projectProfile.setSerial(projectProfile.getSerial() + 1);
//            projectProfileRepository.save(projectProfile);

//            sheet.getCells().merge(3, 5, 3, 3);  // (startRow, startColumn, totalRows, totalColumns)


            sheet.getCells().get("A9").putValue("#");
            sheet.getCells().get("A9").setStyle(tableHeaderStyle);

            sheet.getCells().get("B9").putValue("Pand");
            sheet.getCells().get("B9").setStyle(tableHeaderStyle);

            sheet.getCells().get("C9").putValue("Material");
            sheet.getCells().get("C9").setStyle(tableHeaderStyle);

            sheet.getCells().get("D9").putValue("Description");
            sheet.getCells().get("D9").setStyle(tableHeaderStyle);

            sheet.getCells().get("E9").putValue("Area");
            sheet.getCells().get("E9").setStyle(tableHeaderStyle);

            sheet.getCells().get("F9").putValue("Quantity");
            sheet.getCells().get("F9").setStyle(tableHeaderStyle);

            sheet.getCells().get("G9").putValue("Height");
            sheet.getCells().get("G9").setStyle(tableHeaderStyle);

            sheet.getCells().get("H9").putValue("Width");
            sheet.getCells().get("H9").setStyle(tableHeaderStyle);

            sheet.getCells().get("I9").putValue("Thickness");
            sheet.getCells().get("I9").setStyle(tableHeaderStyle);

            sheet.getCells().get("J9").putValue("Finishing");
            sheet.getCells().get("J9").setStyle(tableHeaderStyle);

            sheet.getCells().get("K9").putValue("Block");
            sheet.getCells().get("K9").setStyle(tableHeaderStyle);

            sheet.getCells().get("L9").putValue("Total");
            sheet.getCells().get("L9").setStyle(tableHeaderStyle);

            sheet.getCells().get("M9").putValue("Unit");
            sheet.getCells().get("M9").setStyle(tableHeaderStyle);


//            sheet.getCells().merge(0, 6, 3, 2);  // (startRow, startColumn, totalRows, totalColumns)

            InputStream imageStream = new ClassPathResource("static/ORYX.jpeg").getInputStream();

            // Add the image to the worksheet (X, Y coordinates in pixels)
            // Place the image inside the merged cells (A1:C5)
            int pictureIndex = sheet.getPictures().add(0, 11, imageStream);

            // Get the added picture
            Picture picture = sheet.getPictures().get(pictureIndex);

            // Optionally, set the picture to fit within the merged area
            picture.setPlacement(PlacementType.MOVE);
            picture.setWidthScale(60); // Scale the image to fit width
            picture.setHeightScale(40); // Scale the image to fit height

            int rowIdx = 11;

            List<String> jobOrdersByRawType = exitJobOrderRepository.jobOrdersByRawType
                    (allExitJobOrder.get(0).getProjectCode()
                            , allExitJobOrder.get(0).getJobOrderId()
                            , serial);


            sheet.getCells().merge(2, 6, 1, 4);  // (startRow, startColumn, totalRows, totalColumns)

            sheet.getCells().get("G3").putValue(serial);
            sheet.getCells().get("G3").setStyle(discriptionDataStyle);

            sheet.getCells().get("G5").putValue(allExitJobOrder.get(0).getUnifiedSerial());
            sheet.getCells().get("G5").setStyle(discriptionDataStyle);

            String addDisc = "";

            List<ExitJobOrder> jobOrdersByThickness = new ArrayList<>();

            int m = 1;
            int i = 0;


            for (int k = 0; k < jobOrdersByRawType.size(); k++) {

                jobOrdersByThickness.addAll(exitJobOrderRepository.jobOrdersByUnit(allExitJobOrder.get(0).getProjectCode()
                        , allExitJobOrder.get(0).getJobOrderId()
                        , jobOrdersByRawType.get(k)
                        , serial));

                double totalQuantity = 0;
                double total = 0.0;

                for (; i < jobOrdersByThickness.size(); i++) {

//                sheet.getCells().get("C" + rowIdx).putValue(jobOrdersByThickness.get(i).getRawType());
//                sheet.getCells().get("C" + rowIdx).setStyle(style2);
//
//                sheet.getCells().get("D" + rowIdx).putValue(jobOrdersByThickness.get(i).getThickness() + "سم ");
//                sheet.getCells().get("D" + rowIdx).setStyle(style2);

//                rowIdx++;
                    String discreption = "";
                    if (jobOrdersByThickness.get(i).getAdditionalDescription() == null) {
                        discreption = "";
                    } else {
                        discreption = jobOrdersByThickness.get(i).getAdditionalDescription();
                    }

                    addDisc += discreption + "\n";
                    System.out.println("5555555555555555");

                    sheet.getCells().get("A" + rowIdx).putValue(m);
                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("A" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("A" + rowIdx).setStyle(discriptionDataStyle);
                    }

                    sheet.getCells().get("B" + rowIdx).putValue(jobOrdersByThickness.get(i).getPandCode());
                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("B" + rowIdx).setStyle(discriptionDataStyle);
                    }

                    sheet.getCells().get("C" + rowIdx).putValue(jobOrdersByThickness.get(i).getRawType());
                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("C" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);
                    }

                    sheet.getCells().get("D" + rowIdx).putValue(jobOrdersByThickness.get(i).getDescription());
                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("D" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);
                    }

//                sheet.getCells().get("E" + rowIdx).putValue(jobOrdersByThickness.get(i).getInstallationArea());
                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("E" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("E" + rowIdx).setStyle(discriptionDataStyle);
                    }

                    totalQuantity += jobOrdersByThickness.get(i).getQuantity();
                    sheet.getCells().get("F" + rowIdx).putValue(jobOrdersByThickness.get(i).getQuantity());
                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("F" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("F" + rowIdx).setStyle(discriptionDataStyle);
                    }

                    sheet.getCells().get("G" + rowIdx).putValue(jobOrdersByThickness.get(i).getHeight());
                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("G" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);
                    }

                    sheet.getCells().get("H" + rowIdx).putValue(jobOrdersByThickness.get(i).getWidth());
                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("H" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);
                    }

                    sheet.getCells().get("I" + rowIdx).putValue(jobOrdersByThickness.get(i).getThickness());
                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("I" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("I" + rowIdx).setStyle(discriptionDataStyle);
                    }

                    sheet.getCells().get("J" + rowIdx).putValue(jobOrdersByThickness.get(i).getFinishType());
                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("J" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("J" + rowIdx).setStyle(discriptionDataStyle);
                    }

                    sheet.getCells().get("K" + rowIdx).putValue(jobOrdersByThickness.get(i).getBlockNumber());
                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("K" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("K" + rowIdx).setStyle(discriptionDataStyle);
                    }


                    double result = 0.0;
                    String unit = "";
                    DecimalFormat df = new DecimalFormat("#,###.000");
                    String formattedNumber = "";
                    if (jobOrdersByThickness.get(i).getUnit().equals("Longitudinal meter")) {
                        result = (Double.valueOf(jobOrdersByThickness.get(i).getHeight())) *
                                (Double.valueOf(jobOrdersByThickness.get(i).getQuantity()));
                        formattedNumber = df.format(result / 100);
                        unit = "Longitudinal meter";
//                    sheet.getCells().get("L" + rowIdx).putValue(formattedNumber);
//                    sheet.getCells().get("M" + rowIdx).putValue(unit);
                    } else if (jobOrdersByThickness.get(i).getUnit().equals("Square Meter")) {
                        result = (Double.valueOf(jobOrdersByThickness.get(i).getHeight())) *
                                (Double.valueOf(jobOrdersByThickness.get(i).getWidth())) *
                                (Double.valueOf(jobOrdersByThickness.get(i).getQuantity()));
//                    row.createCell(13).setCellValue((result)/10000);
                        formattedNumber = df.format(result / 10000);
                        unit = "Square Meter";
//                    sheet.getCells().get("L" + rowIdx).putValue(formattedNumber + " " + unit);
//                    sheet.getCells().get("L" + rowIdx).putValue(formattedNumber);
//                    sheet.getCells().get("M" + rowIdx).putValue(unit);
                    } else {
                        unit = "Unit";
                        formattedNumber = String.valueOf(jobOrdersByThickness.get(i).getQuantity());
                        sheet.getCells().get("L" + rowIdx).putValue(formattedNumber + " " + unit);
                    }

                    total += Double.valueOf(formattedNumber);

                    sheet.getCells().get("L" + rowIdx).putValue(formattedNumber);
                    sheet.getCells().get("M" + rowIdx).putValue(unit);

                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("L" + rowIdx).setStyle(shadowStyle);
                        sheet.getCells().get("M" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("L" + rowIdx).setStyle(discriptionDataStyle);
                        sheet.getCells().get("M" + rowIdx).setStyle(discriptionDataStyle);
                    }

//                    PandsToJobOrder pandsToJobOrder = pandsToJobOrderRepository.getByUniqueId(jobOrdersByThickness.get(i).getUniqueId());
//                    pandsToJobOrder.setTotal(String.valueOf(df.format(Double.valueOf(pandsToJobOrder.getTotal()) - Double.valueOf(formattedNumber))));
//                    double result2 = pandsToJobOrder.getQuantity() - jobOrdersByThickness.get(i).getQuantity();
//                    pandsToJobOrder.setQuantity(result2);
//                sheet.getCells().get("M" + rowIdx).putValue(unit);
//                sheet.getCells().get("M" + rowIdx).setStyle(style);

                    rowIdx++;
                    m++;
                }

                rowIdx++;

                sheet.getCells().get("F" + rowIdx).putValue(totalQuantity);
                sheet.getCells().get("F" + rowIdx).setStyle(shadowStyle);

                sheet.getCells().get("L" + rowIdx).putValue(total);
                sheet.getCells().get("L" + rowIdx).setStyle(shadowStyle);
                rowIdx++;

            }

//            sheet.getCells().get("F3").putValue(addDisc);
//            sheet.getCells().get("F3").setStyle(discriptionDataStyle);

            for (int j = 6; j < rowIdx; j++) {
                sheet.getCells().setRowHeight(j, 18);
            }


            //////////////////////////////////////////////////////////////////////
            // Adjust column widths to fit content
            sheet.autoFitColumns();

            exitProcessJobOrderRepository.deleteAll();

            // 3. Convert Excel to PDF
            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
            workbook.save(pdfOutputStream, SaveFormat.PDF); // Save as PDF

            // 4. Return the PDF as a response
            ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(pdfInputStream);

            return resource;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public InputStreamResource getPdf(String id) throws Exception {

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

        JobOrder jobOrder = jobOrderService.getByJobOrder(id);

        Style tableHeaderStyle = sheet.getCells().get("C1").getStyle();
        tableHeaderStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        tableHeaderStyle.setVerticalAlignment(TextAlignmentType.CENTER);
        tableHeaderStyle.getFont().setItalic(false);
        tableHeaderStyle.getFont().setSize(12);
        tableHeaderStyle.getFont().setBold(false);
        tableHeaderStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());


        Style discriptionDataStyle = sheet.getCells().get("F3").getStyle();
        discriptionDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        discriptionDataStyle.setVerticalAlignment(TextAlignmentType.CENTER);
        discriptionDataStyle.getFont().setSize(12);

        Style shadowStyle = workbook.createStyle();
        shadowStyle.setPattern(BackgroundType.SOLID);
        shadowStyle.setForegroundColor(Color.getDarkGray());
        shadowStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        shadowStyle.getFont().setSize(12);

        List<ExitJobOrder> pands = exitJobOrderService.getByJobOrderId(id);

        sheet.getCells().get("A1").putValue("Project name: ");
        sheet.getCells().get("A1").setStyle(discriptionDataStyle);

        sheet.getCells().get("B1").putValue(pands.get(0).getProjectName());
        sheet.getCells().get("B1").setStyle(discriptionDataStyle);

        sheet.getCells().get("A3").putValue("Project Code: ");
        sheet.getCells().get("A3").setStyle(discriptionDataStyle);

        sheet.getCells().get("B3").putValue(pands.get(0).getProjectCode());
        sheet.getCells().get("B3").setStyle(discriptionDataStyle);

        sheet.getCells().get("A5").putValue("job Order #");
        sheet.getCells().get("A5").setStyle(discriptionDataStyle);

        sheet.getCells().get("B5").putValue(pands.get(0).getJobOrderId());
        sheet.getCells().get("B5").setStyle(discriptionDataStyle);

        sheet.getCells().get("D1").putValue("engineer name");
        sheet.getCells().get("D1").setStyle(discriptionDataStyle);

        sheet.getCells().get("E1").putValue(pands.get(0).getEngineerName());
        sheet.getCells().get("E1").setStyle(discriptionDataStyle);

        sheet.getCells().get("D3").putValue("Created Date");
        sheet.getCells().get("D3").setStyle(discriptionDataStyle);

        sheet.getCells().merge(2, 4, 1, 2);  // (startRow, startColumn, totalRows, totalColumns)

        sheet.getCells().get("E3").putValue(jobOrder.getJobOrderDate() + " " + jobOrder.getJobOrderTime());
        sheet.getCells().get("E3").setStyle(discriptionDataStyle);

        sheet.getCells().get("A5").putValue("Pand Code");
        sheet.getCells().get("A5").setStyle(tableHeaderStyle);

        sheet.getCells().get("B5").putValue("description");
        sheet.getCells().get("B5").setStyle(tableHeaderStyle);

        sheet.getCells().get("C5").putValue("Manufacturing");
        sheet.getCells().get("C5").setStyle(tableHeaderStyle);

        sheet.getCells().get("D5").putValue("Material");
        sheet.getCells().get("D5").setStyle(tableHeaderStyle);

        sheet.getCells().get("E5").putValue("Used Material");
        sheet.getCells().get("E5").setStyle(tableHeaderStyle);

        sheet.getCells().get("F5").putValue("Finishing");
        sheet.getCells().get("F5").setStyle(tableHeaderStyle);

        sheet.getCells().get("G5").putValue("Thickness");
        sheet.getCells().get("G5").setStyle(tableHeaderStyle);

        sheet.getCells().get("H5").putValue("Height");
        sheet.getCells().get("H5").setStyle(tableHeaderStyle);

        sheet.getCells().get("I5").putValue("Width");
        sheet.getCells().get("I5").setStyle(tableHeaderStyle);

        sheet.getCells().get("J5").putValue("unit");
        sheet.getCells().get("J5").setStyle(tableHeaderStyle);

        sheet.getCells().get("K5").putValue("Total");
        sheet.getCells().get("K5").setStyle(tableHeaderStyle);

        sheet.getCells().get("L5").putValue("Total Material Used");
        sheet.getCells().get("L5").setStyle(tableHeaderStyle);

        InputStream imageStream = new ClassPathResource("static/ORYX.jpeg").getInputStream();

        // Add the image to the worksheet (X, Y coordinates in pixels)
        // Place the image inside the merged cells (A1:C5)
        int pictureIndex = sheet.getPictures().add(0, 9, imageStream);

        // Get the added picture
        Picture picture = sheet.getPictures().get(pictureIndex);

        // Optionally, set the picture to fit within the merged area
        picture.setPlacement(PlacementType.MOVE);
        picture.setWidthScale(20); // Scale the image to fit width
        picture.setHeightScale(10);

        int rowIdx = 7;


        for (int i = 0; i < pands.size(); i++) {

            sheet.getCells().get("A" + rowIdx).putValue(pands.get(i).getPandCode());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("A" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("A" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("B" + rowIdx).putValue(pands.get(i).getDescription());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("B" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("C" + rowIdx).putValue(pands.get(i).getManufacturing());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("C" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("D" + rowIdx).putValue(pands.get(i).getRawType());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("D" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("E" + rowIdx).putValue(pands.get(i).getRawUsed());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("E" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("E" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("F" + rowIdx).putValue(pands.get(i).getFinishType());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("F" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("F" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("G" + rowIdx).putValue(pands.get(i).getThickness());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("G" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);
            }


            sheet.getCells().get("H" + rowIdx).putValue(pands.get(i).getHeight());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("H" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("I" + rowIdx).putValue(pands.get(i).getWidth());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("I" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("I" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("J" + rowIdx).putValue(pands.get(i).getUnit());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("J" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("J" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("K" + rowIdx).putValue(pands.get(i).getQuantity());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("K" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("K" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("L" + rowIdx).putValue(pands.get(i).getQuantityUsedRaws());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("L" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("L" + rowIdx).setStyle(discriptionDataStyle);
            }

            rowIdx++;
        }


        for (int i = 4; i < rowIdx; i++) {
            sheet.getCells().setRowHeight(i, 18);
        }

        //////////////////////////////////////////////////////////////////////
        // Adjust column widths to fit content
        sheet.autoFitColumns();

        for (int i = 6; i < rowIdx; i++) {
            sheet.getCells().setRowHeight(i, 25);
        }


        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
//        workbook.save(pdfOutputStream, SaveFormat.PDF); // Save as PDF

        // 4. Return the PDF as a response
        ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
        InputStreamResource resource = new InputStreamResource(pdfInputStream);

        return resource;
    }

    public InputStreamResource getPdfV2(String id) throws Exception {

        JobOrder jobOrder = jobOrderService.getByJobOrder(id);

        List<PandsToJobOrder> pandsToJobOrders = pandsToJobOrderRepository.allJobOrderIdGroupByRawType(id);

        Workbook workbook = new Workbook();
        WorksheetCollection worksheets = workbook.getWorksheets();
        Worksheet sheet = worksheets.get(0);
        sheet.setDisplayRightToLeft(false);
        Cells cells = sheet.getCells();

//            sheet.getCells().setRowHeight(7, 20);

        PageSetup pageSetup = sheet.getPageSetup();
        pageSetup.setOrientation(PageOrientationType.LANDSCAPE);

        pageSetup.setFitToPagesWide(1); // Fit to 1 page width
        pageSetup.setFitToPagesTall(0); // Set to 0 for automatic height

        pageSetup.setTopMargin(1);
        pageSetup.setBottomMargin(1);
        pageSetup.setLeftMargin(1);
        pageSetup.setRightMargin(1);

        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("hh:mm:ss a");

        DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");


        Style tableHeaderStyle = sheet.getCells().get("C1").getStyle();
        tableHeaderStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        tableHeaderStyle.setVerticalAlignment(TextAlignmentType.CENTER);
        tableHeaderStyle.getFont().setItalic(true);
        tableHeaderStyle.getFont().setSize(12);
        tableHeaderStyle.getFont().setBold(false);
        tableHeaderStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

        Style discriptionDataStyle = sheet.getCells().get("F3").getStyle();
        discriptionDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        discriptionDataStyle.setVerticalAlignment(TextAlignmentType.CENTER);
        discriptionDataStyle.getFont().setSize(12);

        sheet.getCells().get("A1").putValue("Project Name: ");
        sheet.getCells().get("A1").setStyle(discriptionDataStyle);

        sheet.getCells().get("B1").putValue(pandsToJobOrders.get(0).getProjectName());
        sheet.getCells().get("B1").setStyle(discriptionDataStyle);

        sheet.getCells().get("A3").putValue("Project Code: ");
        sheet.getCells().get("A3").setStyle(discriptionDataStyle);

        sheet.getCells().get("B3").putValue(jobOrder.getProjectCode());
        sheet.getCells().get("B3").setStyle(discriptionDataStyle);

        sheet.getCells().get("D1").putValue("job order number");
        sheet.getCells().get("D1").setStyle(discriptionDataStyle);

        sheet.getCells().get("E1").putValue(jobOrder.getJobOrderNumber());
        sheet.getCells().get("E1").setStyle(discriptionDataStyle);

//        sheet.getCells().get("D1").putValue("المهندس المسؤول: ");
//        sheet.getCells().get("D1").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("E1").putValue(pands.get(0).getEngineerName());
//        sheet.getCells().get("E1").setStyle(discriptionDataStyle);

        sheet.getCells().get("D3").putValue("Created Date");
        sheet.getCells().get("D3").setStyle(discriptionDataStyle);

        sheet.getCells().merge(2, 4, 1, 2);  // (startRow, startColumn, totalRows, totalColumns)

        sheet.getCells().get("E3").putValue(jobOrder.getJobOrderDate() + " " + jobOrder.getJobOrderTime());
        sheet.getCells().get("E3").setStyle(discriptionDataStyle);

        sheet.getCells().get("D5").putValue("Print Date");
        sheet.getCells().get("D5").setStyle(discriptionDataStyle);

        sheet.getCells().get("E5").putValue(formatter1.format(dNow) + " " + ft.format(dNow).toString());
        sheet.getCells().get("E5").setStyle(discriptionDataStyle);

        sheet.getCells().get("A7").putValue("Pand code");
        sheet.getCells().get("A7").setStyle(tableHeaderStyle);


        sheet.getCells().get("B7").putValue("Material");
        sheet.getCells().get("B7").setStyle(tableHeaderStyle);

        sheet.getCells().get("C7").putValue("Used Material");
        sheet.getCells().get("C7").setStyle(tableHeaderStyle);

        sheet.getCells().get("D7").putValue("Main Quantity");
        sheet.getCells().get("D7").setStyle(tableHeaderStyle);

        sheet.getCells().get("E7").putValue("Used");
        sheet.getCells().get("E7").setStyle(tableHeaderStyle);

        sheet.getCells().get("F7").putValue("Remaining");
        sheet.getCells().get("F7").setStyle(tableHeaderStyle);

        sheet.getCells().get("G7").putValue("Main Total");
        sheet.getCells().get("G7").setStyle(tableHeaderStyle);

        sheet.getCells().get("H7").putValue("Used");
        sheet.getCells().get("H7").setStyle(tableHeaderStyle);

        sheet.getCells().get("I7").putValue("Remaining");
        sheet.getCells().get("I7").setStyle(tableHeaderStyle);

        InputStream imageStream = new ClassPathResource("static/ORYX.jpeg").getInputStream();

        // Add the image to the worksheet (X, Y coordinates in pixels)
        // Place the image inside the merged cells (A1:C5)
        int pictureIndex = sheet.getPictures().add(0, 10, imageStream);

        // Get the added picture
        Picture picture = sheet.getPictures().get(pictureIndex);

        // Optionally, set the picture to fit within the merged area
        picture.setPlacement(PlacementType.MOVE);
        picture.setWidthScale(40); // Scale the image to fit width
        picture.setHeightScale(40);


        Style shadowStyle = workbook.createStyle();
        shadowStyle.setPattern(BackgroundType.SOLID);
        shadowStyle.setForegroundColor(Color.getDarkGray());
        shadowStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        shadowStyle.getFont().setSize(12);

//        sheet.getCells().get("A9").putValue("م");
//        sheet.getCells().get("A9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("B9").putValue("كود البند");
//        sheet.getCells().get("B9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("C9").putValue("التوصيف");
//        sheet.getCells().get("C9").setStyle(tableHeaderStyle);
//
//
//        sheet.getCells().get("D9").putValue("كود التصنيع");
//        sheet.getCells().get("D9").setStyle(tableHeaderStyle);
//
//
//        sheet.getCells().get("E9").putValue("الوحدة");
//        sheet.getCells().get("E9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("F9").putValue("الخامة الفعلية");
//        sheet.getCells().get("F9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("G9").putValue("نوع التشطيب");
//        sheet.getCells().get("G9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("H9").putValue("العدد");
//        sheet.getCells().get("H9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("I9").putValue("الطول");
//        sheet.getCells().get("I9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("J9").putValue("العرض");
//        sheet.getCells().get("J9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("K9").putValue("السمك");
//        sheet.getCells().get("K9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("L9").putValue("التكرار");
//        sheet.getCells().get("L9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("M9").putValue("الاجمالى");
//        sheet.getCells().get("M9").setStyle(tableHeaderStyle);


        DecimalFormat df = new DecimalFormat("#,###.000");
        String formattedNumber = "";
        int rowIdx = 9;

        for (int i = 0; i < pandsToJobOrders.size(); i++) {


            sheet.getCells().get("A" + rowIdx).putValue(pandsToJobOrders.get(i).getPandCode());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("A" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("A" + rowIdx).setStyle(discriptionDataStyle);
            }

//            sheet.getCells().get("B" + rowIdx).putValue(pands.get(i).getDescription());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("B" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            sheet.getCells().get("C" + rowIdx).putValue(pands.get(i).getManufacturing());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("C" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);
//            }

            sheet.getCells().get("B" + rowIdx).putValue(pandsToJobOrders.get(i).getRawType());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("B" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("C" + rowIdx).putValue(pandsToJobOrders.get(i).getRawUsed());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("C" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);
            }

//            sheet.getCells().get("F" + rowIdx).putValue(pands.get(i).getFinishType());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("F" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("F" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            sheet.getCells().get("G" + rowIdx).putValue(pands.get(i).getThickness());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("G" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//
//            sheet.getCells().get("H" + rowIdx).putValue(pands.get(i).getHeight());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("H" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            sheet.getCells().get("I" + rowIdx).putValue(pands.get(i).getWidth());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("I" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("I" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            sheet.getCells().get("J" + rowIdx).putValue(pands.get(i).getUnit());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("J" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("J" + rowIdx).setStyle(discriptionDataStyle);
//            }

            System.out.println("///////555555555555555555555/////////");
            Double maintotal = pandsToJobOrderRepository.getSumByRawType(jobOrder.getProjectProfileId(), id, pandsToJobOrders.get(i).getRawType());
            if (maintotal == null) {
                maintotal = 0.0;
            }
            formattedNumber = df.format(maintotal);
            sheet.getCells().get("D" + rowIdx).putValue(formattedNumber);
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("D" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);
            }

            Double usedtotal = exitJobOrderRepository.getSumByRawType(jobOrder.getProjectCode(), id, pandsToJobOrders.get(i).getRawType());
            if (usedtotal == null) {
                usedtotal = 0.0;
            }

            formattedNumber = df.format(usedtotal);
            sheet.getCells().get("E" + rowIdx).putValue(formattedNumber);
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("E" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("E" + rowIdx).setStyle(discriptionDataStyle);
            }

            Double availableTotal = maintotal - usedtotal;

            sheet.getCells().get("F" + rowIdx).putValue(df.format(availableTotal));
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("F" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("F" + rowIdx).setStyle(discriptionDataStyle);
            }

            Double mainQuantity = pandsToJobOrderRepository.sumQuantityByRaw(jobOrder.getProjectProfileId(), id, pandsToJobOrders.get(i).getRawType());

            if (mainQuantity == null) {
                mainQuantity = 0.0;
            }
            sheet.getCells().get("G" + rowIdx).putValue(df.format(mainQuantity * Integer.valueOf(pandsToJobOrders.get(i).getRepetition())));
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("G" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);
            }

            Double usedQuantity = exitJobOrderRepository.sumQuantityByRawType(jobOrder.getProjectCode(), id, pandsToJobOrders.get(i).getRawType());

            if (usedQuantity == null) {
                usedQuantity = 0.0;
            }
            sheet.getCells().get("H" + rowIdx).putValue(df.format(usedQuantity));
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("H" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);
            }

            Double availableQuantity = (mainQuantity * Integer.valueOf(pandsToJobOrders.get(i).getRepetition())) - usedQuantity;

            sheet.getCells().get("I" + rowIdx).putValue(df.format(availableQuantity));
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("I" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("I" + rowIdx).setStyle(discriptionDataStyle);
            }

            rowIdx++;
        }
//        sheet.getCells().setRowHeight(8, 18);

//        for (int i = 4; i < rowIdx; i++) {
//            sheet.getCells().setColumnWidth(0, 15);
//        }
//


        sheet.autoFitColumns();

        for (int j = 6; j < rowIdx; j++) {
            sheet.getCells().setRowHeight(j, 18);
        }


        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        workbook.save(pdfOutputStream, SaveFormat.PDF); // Save as PDF

        // 4. Return the PDF as a response
        ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
        InputStreamResource resource = new InputStreamResource(pdfInputStream);

        return resource;
    }


}
