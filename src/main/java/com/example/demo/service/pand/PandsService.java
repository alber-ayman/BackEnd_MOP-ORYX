package com.example.demo.service.pand;

import com.aspose.cells.*;
import com.aspose.cells.Color;
import com.aspose.cells.Picture;
import com.example.demo.models.*;
import com.example.demo.payload.CheckLimitResponse;
import com.example.demo.repository.*;

import com.example.demo.service.ChangeHistoryLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class PandsService {

    @Autowired
    PandsRepository pandsRepository;
    @Autowired
    PandsToJobOrderRepository pandsToJobOrderRepository;

    @Autowired
    ProjectProfileRepository projectProfileRepository;

    @Autowired
    ExitJobOrderRepository exitJobOrderRepository;

    @Autowired
    private ChangeHistoryLog changeHistoryLog;

    public ResponseEntity<List<Pand>> getAllPands() {
        try {
            List<Pand> pands = pandsRepository.findAll();

            if (pands.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(pands, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Pand getPandByPandCode(String pandCode, Long projectId) {

        return pandsRepository.findByPandCodeAndProjectProfileId(pandCode, projectId);
    }

    public Optional<Pand> getPandById(Long id) {

//        String fileurl = storageService.getFileByPandId(id);
//        pands.get().setFileDB(fileurl);

        return pandsRepository.findById(id);
    }

    public List<Pand> getPandByProjectId(Long id) {

        return pandsRepository.findByProjectProfileId(id);
    }

    public ResponseEntity<Pand> addNewPand(
            Pand pand, HttpServletRequest request) {
        try {

            ProjectProfile projectProfile = projectProfileRepository.getById(pand.getProjectProfileId());

            pand.setProjectProfileId(projectProfile.getId());
            pand.setProjectCode(projectProfile.getProjectCode());
            pand.setProjectName(projectProfile.getProjectName());
            pand.setRestQuantity(pand.getMainQuantity()); // restQuantity
            pand.setTotalQuantity(pand.getMainQuantity());
            pand.setMockQuantity(pand.getMainQuantity());
            pand.setTotalPrice(String.valueOf(pand.getMainQuantity() * pand.getPrice()));
            String pandCode = pand.getPandCode();

            if (pandCode.contains("/")) {
                pandCode = pandCode.replace("/", "-");
                pand.setPandCode(pandCode);
            }

            pand.setPandCode(pandCode.trim());
            pand.setEngineerName(projectProfile.getEngineerName());
//            pand.setAdditionalQuantity(0);

            changeHistoryLog.saveChange(pandCode, pand.toString(), pand.toString(), "save", request);

            pandsRepository.save(pand);

            return new ResponseEntity<>(pand, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(pand, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Pand> updatePand(Long id, Pand updatedPand, HttpServletRequest request) throws ResourceNotFoundException, SQLException {
        try {
            Optional<Pand> pand = getPandById(id);
            String requestMessage = pand.toString();

            DecimalFormat df = new DecimalFormat("#.###");


            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = myDateObj.format(myFormatObj);

//            String height;
//            String width;
//            if (updatedPand.getHeight() == null) {
//                height = "1";
//            } else {
//                height = updatedPand.getHeight();
//            }
//
//            if (updatedPand.getWidth() == null) {
//                width = "1";
//            } else {
//                width = updatedPand.getWidth();
//            }

            if(pand.isEmpty()){
                throw new ResourceNotFoundException("Pand not found");
            }

            pand.get().setUpdatedDate(formattedDate);
            pand.get().setUnit(updatedPand.getUnit());

            pand.get().setDescription(updatedPand.getDescription());
            pand.get().setAdditionalDescription(updatedPand.getAdditionalDescription());
            pand.get().setProjectProfileId(updatedPand.getProjectProfileId());
            pand.get().setRawType(updatedPand.getRawType());
            pand.get().setRawUsed(updatedPand.getRawUsed());
            pand.get().setFinishType(updatedPand.getFinishType());
            pand.get().setThickness(updatedPand.getThickness());
//            pand.get().setHeight(height);
//            pand.get().setWidth(width);
            pand.get().setRepetition(updatedPand.getRepetition());
            pand.get().setPrice(updatedPand.getPrice());

//            double total;
//
//            if (updatedPand.getUnit().equals("Square Meter")) {
//                total = (Double.valueOf(height) * Double.valueOf(width) * Double.valueOf(updatedPand.getMainQuantity())) / 10000;
//            } else if (updatedPand.getUnit().equals("Longitudinal meter")) {
//                total = (Double.valueOf(height) * Double.valueOf(updatedPand.getMainQuantity())) / 100;
//            } else {
//                total = Double.valueOf(updatedPand.getMainQuantity());
//            }
//
//            String formattedNumber = df.format(total);
//            pand.ifPresent(oldPand -> pand.get().setTotal(Double.valueOf(formattedNumber)));

            String pandCode = updatedPand.getPandCode();

            if (pandCode.contains("/")) {
                pandCode = pandCode.replace("/", "-");
                pand.get().setPandCode(pandCode);
            }

            changeHistoryLog.saveChange(pandCode, requestMessage, pand.toString(), "update", request);

            List<PandsToJobOrder> pandsToJobOrders = pandsToJobOrderRepository.findByProjectProfileIdAndPandCode(pand.get().getProjectProfileId(), pand.get().getPandCode());
            for (PandsToJobOrder pandsToJobOrder : pandsToJobOrders) {
                pandsToJobOrder.setPandCode(updatedPand.getPandCode());
                pandsToJobOrder.setRawType(pand.get().getRawType());
                pandsToJobOrder.setRawUsed(pand.get().getRawUsed());
                pandsToJobOrder.setFinishType(pand.get().getFinishType());
                pandsToJobOrder.setThickness(pand.get().getThickness());
                pandsToJobOrderRepository.save(pandsToJobOrder);
            }

            pand.get().setPandCode(updatedPand.getPandCode());
            pand.get().setTotalPrice(df.format(updatedPand.getMainQuantity() * updatedPand.getPrice()));
            pandsRepository.save(pand.get());
            return new ResponseEntity<>(pand.get(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    public void deletePand(Long id) {
        pandsRepository.deleteById(id);
    }

    public CheckLimitResponse checkLimit() {
        List<Pand> pands = pandsRepository.findAll();
        CheckLimitResponse checkLimitResponse = new CheckLimitResponse();
        for (Pand pand : pands) {
            if (pand.getRestQuantity() < 0) {
                checkLimitResponse.setMessage("تم تخطى الكمية المحدوده للبند رقم " + pand.getPandCode() + " للمشروع  " + pand.getProjectName());
                checkLimitResponse.setFlag(1);
                return checkLimitResponse;
            }
        }
        return new CheckLimitResponse("", 0);
    }


    public InputStreamResource getPandDetailsForEachJobOrder(String id, Long projectId) {
        try {
            com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook();
            WorksheetCollection worksheets = workbook.getWorksheets();
            Worksheet sheet = worksheets.get(0);
            sheet.setDisplayRightToLeft(true);
//            sheet.getCells().setRowHeight(7, 20);

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
            tableHeaderStyle.getFont().setItalic(true);
            tableHeaderStyle.getFont().setSize(11);
            tableHeaderStyle.getFont().setBold(false);
            tableHeaderStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());

            Date dNow = new Date();
            SimpleDateFormat ft =
                    new SimpleDateFormat("hh:mm:ss a");

            DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");

            Style discriptionDataStyle = sheet.getCells().get("F3").getStyle();
            discriptionDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            discriptionDataStyle.setVerticalAlignment(TextAlignmentType.CENTER);
            discriptionDataStyle.getFont().setSize(11);

            Pand pand = pandsRepository.findByPandCodeAndProjectProfileId(id, projectId);

            sheet.getCells().get("A1").putValue("Project Name");
            sheet.getCells().get("A1").setStyle(discriptionDataStyle);

            sheet.getCells().get("B1").putValue(pand.getProjectName());
            sheet.getCells().get("B1").setStyle(discriptionDataStyle);

            sheet.getCells().get("A3").putValue("Project Code");
            sheet.getCells().get("A3").setStyle(discriptionDataStyle);

            sheet.getCells().get("B3").putValue(pand.getProjectCode());
            sheet.getCells().get("B3").setStyle(discriptionDataStyle);

            sheet.getCells().get("A5").putValue("Print Date");
            sheet.getCells().get("A5").setStyle(discriptionDataStyle);

            sheet.getCells().get("B5").putValue(formatter1.format(dNow) + " " + ft.format(dNow));
            sheet.getCells().get("B5").setStyle(discriptionDataStyle);

            sheet.getCells().get("C1").putValue("Pand Code");
            sheet.getCells().get("C1").setStyle(discriptionDataStyle);

            sheet.getCells().get("D1").putValue(pand.getPandCode());
            sheet.getCells().get("D1").setStyle(discriptionDataStyle);

            sheet.getCells().get("C3").putValue("Unit");
            sheet.getCells().get("C3").setStyle(discriptionDataStyle);

            sheet.getCells().get("D3").putValue(pand.getUnit());
            sheet.getCells().get("D3").setStyle(discriptionDataStyle);

            sheet.getCells().get("E1").putValue("Quantity");
            sheet.getCells().get("E1").setStyle(discriptionDataStyle);

            sheet.getCells().get("F1").putValue(pand.getMainQuantity());
            sheet.getCells().get("F1").setStyle(discriptionDataStyle);

            sheet.getCells().get("E3").putValue("Remaining Quantity In Pand");
            sheet.getCells().get("E3").setStyle(discriptionDataStyle);

            sheet.getCells().get("F3").putValue(pand.getRestQuantity());
            sheet.getCells().get("F3").setStyle(discriptionDataStyle);

            Style shadowStyle = workbook.createStyle();
            shadowStyle.setPattern(BackgroundType.SOLID);
            shadowStyle.setForegroundColor(Color.getDarkGray());
            shadowStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            shadowStyle.getFont().setSize(11);

            sheet.getCells().get("A7").putValue("Job Order");
            sheet.getCells().get("A7").setStyle(tableHeaderStyle);

            sheet.getCells().get("B7").putValue("Job Order Quantity");
            sheet.getCells().get("B7").setStyle(tableHeaderStyle);

            sheet.getCells().get("C7").putValue("Permited Quantity");
            sheet.getCells().get("C7").setStyle(tableHeaderStyle);

            sheet.getCells().get("D7").putValue("Remaining Quantity");
            sheet.getCells().get("D7").setStyle(tableHeaderStyle);

            InputStream logoStream = new ClassPathResource("static/ORYX.jpeg").getInputStream();

            // Add the image to the worksheet (X, Y coordinates in pixels)
            // Place the image inside the merged cells (A1:C5)
            int logoIndex = sheet.getPictures().add(0, 7, logoStream);

            // Get the added picture
            Picture logo = sheet.getPictures().get(logoIndex);

            // Optionally, set the picture to fit within the merged area
            logo.setPlacement(PlacementType.MOVE);
            logo.setWidthScale(40); // Scale the image to fit width
            logo.setHeightScale(40);

            if(pand.getImage() != null) {

                byte[] imageBytes = pand.getImage();

                ByteArrayInputStream imageStream = new ByteArrayInputStream(imageBytes);

                // Add image to the sheet (at cell A1)
                int pictureIndex = sheet.getPictures().add(7, 7, imageStream);
                Picture picture = sheet.getPictures().get(pictureIndex);

                // Optional: Resize or position the image
                picture.setWidthScale(40); // Scale the image to fit width
                picture.setHeightScale(40);
            }
            int rowIdx = 9;


            List<String> pandsToJobOrders = pandsToJobOrderRepository.findPandDetails(projectId, id);
            Double totalQuantityInJobOrders = 0.0;
            Double totalQuantityInExitJobOrders = 0.0;
            for (String entry : pandsToJobOrders) {

                sheet.getCells().get("A" + rowIdx).putValue(entry);  // رقم امر شغل
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("A" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("A" + rowIdx).setStyle(discriptionDataStyle);
                }

                Double totalSumInJobOrders = pandsToJobOrderRepository.sumMainTotalByPandCodeAndJobOrder(projectId, id, entry);

                if (totalSumInJobOrders == null) {
                    totalSumInJobOrders = 0.0;
                }

                totalQuantityInJobOrders += totalSumInJobOrders;

                sheet.getCells().get("B" + rowIdx).putValue(totalSumInJobOrders); // الكميه لكل امر شغل
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("B" + rowIdx).setStyle(discriptionDataStyle);
                }

                Double totalExit = exitJobOrderRepository.sumTotalByJobOrderAndPand(pand.getProjectCode(), id, entry);

                if (totalExit == null) {
                    totalExit = 0.0;
                }
                totalQuantityInExitJobOrders += totalExit;
//                DecimalFormat df = new DecimalFormat("#.###");
//                String formattedNumber = df.format(totalExit);
                sheet.getCells().get("C" + rowIdx).putValue(totalExit); // الكميه المصروفه
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("C" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);
                }

                Double value = totalSumInJobOrders - totalExit;

                sheet.getCells().get("D" + rowIdx).putValue(value);
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("D" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);
                }
//
//                sheet.getCells().get("J" + rowIdx).putValue(pands.get(i).getUnit());
//                if (rowIdx % 2 != 0) {
//                    sheet.getCells().get("J" + rowIdx).setStyle(shadowStyle);
//                } else {
//                    sheet.getCells().get("J" + rowIdx).setStyle(discriptionDataStyle);
//                }

//                sheet.getCells().get("K" + rowIdx).putValue(pands.get(i).getTotal());
//                if (rowIdx % 2 != 0) {
//                    sheet.getCells().get("K" + rowIdx).setStyle(shadowStyle);
//                } else {
//                    sheet.getCells().get("K" + rowIdx).setStyle(discriptionDataStyle);
//                }


                rowIdx++;
            }

            rowIdx++;

            sheet.getCells().get("B" + rowIdx).putValue(totalQuantityInJobOrders); // الكميه لكل امر شغل
            sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);


            sheet.getCells().get("C" + rowIdx).putValue(totalQuantityInExitJobOrders); // الكميه لكل امر شغل
            sheet.getCells().get("C" + rowIdx).setStyle(shadowStyle);


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
            System.out.println("1111111111111111111111111");
            return resource;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public InputStreamResource getPdf(Long id) throws Exception {
        try {
            com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook();
            WorksheetCollection worksheets = workbook.getWorksheets();
            Worksheet sheet = worksheets.get(0);
            sheet.setDisplayRightToLeft(false);
//            sheet.getCells().setRowHeight(7, 20);

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
            tableHeaderStyle.getFont().setItalic(true);
            tableHeaderStyle.getFont().setSize(11);
            tableHeaderStyle.getFont().setBold(false);
            tableHeaderStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());

            Style tableHeaderStyle2 = sheet.getCells().get("C1").getStyle();
            tableHeaderStyle2.setHorizontalAlignment(TextAlignmentType.CENTER);
            tableHeaderStyle2.setVerticalAlignment(TextAlignmentType.CENTER);
            tableHeaderStyle2.getFont().setItalic(true);
            tableHeaderStyle2.getFont().setSize(9);
            tableHeaderStyle2.getFont().setBold(false);
            tableHeaderStyle2.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
            tableHeaderStyle2.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
            tableHeaderStyle2.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
            tableHeaderStyle2.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());


            Style discriptionDataStyle = sheet.getCells().get("F3").getStyle();
            discriptionDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            discriptionDataStyle.setVerticalAlignment(TextAlignmentType.CENTER);
            discriptionDataStyle.getFont().setSize(11);

            Date dNow = new Date();
            SimpleDateFormat ft =
                    new SimpleDateFormat("hh:mm:ss a");

            DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");

            List<Pand> pands = pandsRepository.findByProjectProfileId(id);

            sheet.getCells().get("A1").putValue("Project Name");
            sheet.getCells().get("A1").setStyle(discriptionDataStyle);

            sheet.getCells().get("B1").putValue(pands.getFirst().getProjectName());
            sheet.getCells().get("B1").setStyle(discriptionDataStyle);

            sheet.getCells().get("A3").putValue("Project Code");
            sheet.getCells().get("A3").setStyle(discriptionDataStyle);

            sheet.getCells().get("B3").putValue(pands.getFirst().getProjectCode());
            sheet.getCells().get("B3").setStyle(discriptionDataStyle);

            sheet.getCells().get("D1").putValue("Engineer Name");
            sheet.getCells().get("D1").setStyle(discriptionDataStyle);

            sheet.getCells().get("E1").putValue(pands.getFirst().getEngineerName());
            sheet.getCells().get("E1").setStyle(discriptionDataStyle);

            sheet.getCells().get("D3").putValue("Print Date");
            sheet.getCells().get("D3").setStyle(discriptionDataStyle);

            sheet.getCells().get("E3").putValue(formatter1.format(dNow) + " " + ft.format(dNow));
            sheet.getCells().get("E3").setStyle(discriptionDataStyle);

            Style shadowStyle = workbook.createStyle();
            shadowStyle.setPattern(BackgroundType.SOLID);
            shadowStyle.setForegroundColor(Color.getDarkGray());
            shadowStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            shadowStyle.getFont().setSize(11);

            sheet.getCells().get("A5").putValue("BOQ Ref");
            sheet.getCells().get("A5").setStyle(tableHeaderStyle);

            sheet.getCells().get("B5").putValue("BOQ Description");
            sheet.getCells().get("B5").setStyle(tableHeaderStyle);

            sheet.getCells().get("C5").putValue("Material Name");
            sheet.getCells().get("C5").setStyle(tableHeaderStyle);

            sheet.getCells().get("D5").putValue("Thickness");
            sheet.getCells().get("D5").setStyle(tableHeaderStyle);

            sheet.getCells().get("E5").putValue("Finish Type");
            sheet.getCells().get("E5").setStyle(tableHeaderStyle);

            sheet.getCells().get("F5").putValue("Store Reference");
            sheet.getCells().get("F5").setStyle(tableHeaderStyle);

            sheet.getCells().get("G5").putValue("Price");
            sheet.getCells().get("G5").setStyle(tableHeaderStyle);

            sheet.getCells().get("H5").putValue("Quantity");
            sheet.getCells().get("H5").setStyle(tableHeaderStyle);

            sheet.getCells().get("I5").putValue("Unit");
            sheet.getCells().get("I5").setStyle(tableHeaderStyle);

            sheet.getCells().get("J5").putValue("Remaining Quantity");
            sheet.getCells().get("J5").setStyle(tableHeaderStyle);

            sheet.getCells().get("K5").putValue("Ordered");
            sheet.getCells().get("K5").setStyle(tableHeaderStyle);

            sheet.getCells().get("L5").putValue("Delivered");
            sheet.getCells().get("L5").setStyle(tableHeaderStyle);

            InputStream imageStream =
                    new ClassPathResource("static/ORYX.jpeg").getInputStream();

            int pictureIndex =
                    sheet.getPictures().add(0, 12, imageStream);

            Picture picture =
                    sheet.getPictures().get(pictureIndex);

            picture.setPlacement(PlacementType.FREE_FLOATING);

            picture.setWidthScale(45);
            picture.setHeightScale(45);

            int rowIdx = 7;
            double accomulatiedTotalOrdered = 0;
            double accomulatiedTotalDelivered = 0;
            for (Pand pand : pands) {

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

                sheet.getCells().get("D" + rowIdx).putValue(pand.getThickness());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("D" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);
                }

                sheet.getCells().get("E" + rowIdx).putValue(pand.getFinishType());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("E" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("E" + rowIdx).setStyle(discriptionDataStyle);
                }

                sheet.getCells().get("F" + rowIdx).putValue(pand.getRawUsed());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("F" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("F" + rowIdx).setStyle(discriptionDataStyle);
                }

                sheet.getCells().get("G" + rowIdx).putValue(pand.getPrice());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("G" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);
                }

                sheet.getCells().get("H" + rowIdx).putValue(pand.getMainQuantity());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("H" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);
                }

                sheet.getCells().get("I" + rowIdx).putValue(pand.getUnit());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("I" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("I" + rowIdx).setStyle(discriptionDataStyle);
                }

//                sheet.getCells().get("J" + rowIdx).putValue(pands.get(i).getUnit());
//                if (rowIdx % 2 != 0) {
//                    sheet.getCells().get("J" + rowIdx).setStyle(shadowStyle);
//                } else {
//                    sheet.getCells().get("J" + rowIdx).setStyle(discriptionDataStyle);
//                }

//                sheet.getCells().get("K" + rowIdx).putValue(pands.get(i).getTotal());
//                if (rowIdx % 2 != 0) {
//                    sheet.getCells().get("K" + rowIdx).setStyle(shadowStyle);
//                } else {
//                    sheet.getCells().get("K" + rowIdx).setStyle(discriptionDataStyle);
//                }

                sheet.getCells().get("J" + rowIdx).putValue(pand.getRestQuantity());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("J" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("J" + rowIdx).setStyle(discriptionDataStyle);
                }

                double totalOrdered = pandsToJobOrderRepository.sumMainTotalByPandCode(pand.getProjectProfileId(), pand.getPandCode());
                accomulatiedTotalOrdered += totalOrdered;
                sheet.getCells().get("K" + rowIdx).putValue(totalOrdered);
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("K" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("K" + rowIdx).setStyle(discriptionDataStyle);
                }
                double totalDelivered = exitJobOrderRepository.sumTotalByPandCode(pand.getProjectCode(), pand.getPandCode());

                accomulatiedTotalDelivered += totalDelivered;
                sheet.getCells().get("L" + rowIdx).putValue(totalDelivered);
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("L" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("L" + rowIdx).setStyle(discriptionDataStyle);
                }
                rowIdx++;
            }

            sheet.getCells().get("K" + rowIdx).putValue(accomulatiedTotalOrdered);
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("K" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("K" + rowIdx).setStyle(discriptionDataStyle);
            }
            sheet.getCells().get("L" + rowIdx).putValue(accomulatiedTotalDelivered);
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("L" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("L" + rowIdx).setStyle(discriptionDataStyle);
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
            System.out.println("1111111111111111111111111");
            return resource;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public InputStreamResource getPandImage(Long id, Long projectId) {
        try {
            com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook();
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

            Style tableHeaderStyle = sheet.getCells().get("C1").getStyle();
            tableHeaderStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            tableHeaderStyle.setVerticalAlignment(TextAlignmentType.CENTER);
            tableHeaderStyle.getFont().setItalic(true);
            tableHeaderStyle.getFont().setSize(11);
            tableHeaderStyle.getFont().setBold(false);
            tableHeaderStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, com.aspose.cells.Color.getBlack());


            Date dNow = new Date();
            SimpleDateFormat ft =
                    new SimpleDateFormat("hh:mm:ss a");

            DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");

            Style discriptionDataStyle = sheet.getCells().get("F3").getStyle();
            discriptionDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            discriptionDataStyle.setVerticalAlignment(TextAlignmentType.CENTER);
            discriptionDataStyle.getFont().setSize(11);

            Pand pand = pandsRepository.findByIdAndProjectProfileId(id, projectId);

            sheet.getCells().get("A1").putValue("Project Name");
            sheet.getCells().get("A1").setStyle(discriptionDataStyle);

            sheet.getCells().get("B1").putValue(pand.getProjectName());
            sheet.getCells().get("B1").setStyle(discriptionDataStyle);

            sheet.getCells().get("A3").putValue("Project Code");
            sheet.getCells().get("A3").setStyle(discriptionDataStyle);

            sheet.getCells().get("B3").putValue(pand.getProjectCode());
            sheet.getCells().get("B3").setStyle(discriptionDataStyle);

            sheet.getCells().get("A5").putValue("Print Date");
            sheet.getCells().get("A5").setStyle(discriptionDataStyle);

            sheet.getCells().get("B5").putValue(formatter1.format(dNow) + " " + ft.format(dNow));
            sheet.getCells().get("B5").setStyle(discriptionDataStyle);

            sheet.getCells().get("C1").putValue("Pand Code");
            sheet.getCells().get("C1").setStyle(discriptionDataStyle);

            sheet.getCells().get("D1").putValue(pand.getPandCode());
            sheet.getCells().get("D1").setStyle(discriptionDataStyle);

            sheet.getCells().get("C3").putValue("Unit");
            sheet.getCells().get("C3").setStyle(discriptionDataStyle);

            sheet.getCells().get("D3").putValue(pand.getUnit());
            sheet.getCells().get("D3").setStyle(discriptionDataStyle);

            sheet.getCells().get("E1").putValue("Pand Main Quantity");
            sheet.getCells().get("E1").setStyle(discriptionDataStyle);

            sheet.getCells().get("F1").putValue(pand.getMainQuantity());
            sheet.getCells().get("F1").setStyle(discriptionDataStyle);

            sheet.getCells().get("E3").putValue("Remaining Quantity In Pand");
            sheet.getCells().get("E3").setStyle(discriptionDataStyle);

            sheet.getCells().get("F3").putValue(pand.getRestQuantity());
            sheet.getCells().get("F3").setStyle(discriptionDataStyle);

            Style shadowStyle = workbook.createStyle();
            shadowStyle.setPattern(BackgroundType.SOLID);
            shadowStyle.setForegroundColor(Color.getDarkGray());
            shadowStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            shadowStyle.getFont().setSize(11);


            sheet.getCells().get("A7").putValue("Material Name");
            sheet.getCells().get("A7").setStyle(tableHeaderStyle);

            sheet.getCells().get("B7").putValue("Thickness");
            sheet.getCells().get("B7").setStyle(tableHeaderStyle);

            sheet.getCells().get("C7").putValue("Notes");
            sheet.getCells().get("C7").setStyle(tableHeaderStyle);


            InputStream imageStream = new ClassPathResource("static/ORYX.jpeg").getInputStream();

            // Add the image to the worksheet (X, Y coordinates in pixels)
            // Place the image inside the merged cells (A1:C5)
            int pictureIndex = sheet.getPictures().add(0, 7, imageStream);

            // Get the added picture
            Picture picture = sheet.getPictures().get(pictureIndex);

            // Optionally, set the picture to fit within the merged area
            picture.setPlacement(PlacementType.MOVE);
            picture.setWidthScale(40); // Scale the image to fit width
            picture.setHeightScale(40);


            byte[] imageBytes = pand.getImage();

            int pictureIndex2 = sheet.getPictures().add(7, 5, new ByteArrayInputStream(imageBytes));
            Picture picture2 = sheet.getPictures().get(pictureIndex2);

            // Optionally, set the picture to fit within the merged area
            picture2.setPlacement(PlacementType.MOVE);
            picture2.setWidthScale(30); // Scale the image to fit width
            picture2.setHeightScale(30);

            int rowIdx = 9;


            sheet.getCells().get("A" + rowIdx).putValue(pand.getRawType());  // رقم امر شغل
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("A" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("A" + rowIdx).setStyle(discriptionDataStyle);
            }


            sheet.getCells().get("B" + rowIdx).putValue(pand.getThickness()); // الكميه لكل امر شغل
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("B" + rowIdx).setStyle(discriptionDataStyle);
            }


            if (pand.getImageDescription().length() > 30) {
                int rowIndex = rowIdx;
                String input = pand.getImageDescription();
                int partLength = 30;
                for (int d = 0; d < input.length(); d += partLength) {
                    int end = Math.min(d + partLength, input.length());
                    String part = input.substring(d, end);

//                    Row row = cells.getRows().get(rowIndex);
//                    row.setHeight(50);

                    sheet.getCells().get("C" + rowIndex).putValue(part + "\n");
                    sheet.getCells().get("C" + rowIndex).setStyle(shadowStyle);

                    rowIndex++;
                }
            } else {
                sheet.getCells().get("C" + rowIdx).putValue(pand.getImageDescription());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("C" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);
                }
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
            System.out.println("1111111111111111111111111");
            return resource;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String deletePandImage(Long id) {
        Optional<Pand> pand = pandsRepository.findById(id);
        try {
            pand.get().setImage(null);
            pandsRepository.save(pand.get());
            System.out.println("deleted");
            return "Image Deleted Successfully";
        } catch (Exception e) {
            System.out.println("not deleted");
            return "Failed To Delete The Image";
        }
    }


}
