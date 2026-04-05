package com.example.demo.service.store;

import com.aspose.cells.*;
import com.example.demo.models.*;
import com.example.demo.payload.FilterResponse;
import com.example.demo.repository.ExportSuppliesDetailsRepository;
import com.example.demo.repository.SuppliesDetailsRepository;
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
import java.util.Objects;

@Service
public class BalanceService {

    @Autowired
    SuppliesDetailsRepository suppliesDetailsRepository;

    @Autowired
    ExportSuppliesDetailsRepository exportSuppliesDetailsRepository;

    public InputStreamResource getPdf(BalanceFilter balanceFilter) throws Exception {

        List<String> materials = new ArrayList<>();
        if (balanceFilter.getMaterial().equals("all")) {
            materials = suppliesDetailsRepository.getMaterials();
        } else {
            materials.add(balanceFilter.getMaterial());
        }

        Workbook workbook = new Workbook();

        WorksheetCollection worksheets = workbook.getWorksheets();
        Worksheet sheet = worksheets.get(0);
        sheet.setDisplayRightToLeft(false);
        sheet.getPageSetup().setPrintTitleRows("$9:$9");
        Cells cells = sheet.getCells();

//            sheet.getCells().setRowHeight(7, 20);

        PageSetup pageSetup = sheet.getPageSetup();
        pageSetup.setOrientation(PageOrientationType.LANDSCAPE);
        pageSetup.setPaperSize(PaperSizeType.PAPER_A_3);


        pageSetup.setTopMargin(1);
        pageSetup.setBottomMargin(1);
        pageSetup.setLeftMargin(1);
        pageSetup.setRightMargin(1);

        pageSetup.setFitToPagesWide(1);     // Fit to one page wide
        pageSetup.setFitToPagesTall(0);     // Don't force page height
        pageSetup.setZoom(100);             // Prevent zoom from shrinking
        pageSetup.setPercentScale(false);   // Don't scale by percent

        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("hh:mm:ss a");

        DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");


        Style tableHeaderStyle = sheet.getCells().get("C1").getStyle();
        tableHeaderStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        tableHeaderStyle.setVerticalAlignment(TextAlignmentType.CENTER);
        tableHeaderStyle.getFont().setItalic(true);
        tableHeaderStyle.getFont().setSize(15);
        tableHeaderStyle.getFont().setBold(false);
        tableHeaderStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

        Style discriptionDataStyle = sheet.getCells().get("F3").getStyle();
        discriptionDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        discriptionDataStyle.setVerticalAlignment(TextAlignmentType.CENTER);
        discriptionDataStyle.getFont().setSize(15);


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
        shadowStyle.getFont().setSize(15);

        int rowNumber = 3;

        sheet.getCells().get("A" + rowNumber).putValue("#");
        sheet.getCells().get("A" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("B" + rowNumber).putValue("Material");
        sheet.getCells().get("B" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("C" + rowNumber).putValue("Code");
        sheet.getCells().get("C" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("D" + rowNumber).putValue("Number");
        sheet.getCells().get("D" + rowNumber).setStyle(tableHeaderStyle);


        sheet.getCells().get("E" + rowNumber).putValue("Price");
        sheet.getCells().get("E" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("F" + rowNumber).putValue("Total");
        sheet.getCells().get("F" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("G" + rowNumber).putValue("Cost");
        sheet.getCells().get("G" + rowNumber).setStyle(tableHeaderStyle);

        int rowIdx = 4;

        DecimalFormat df = new DecimalFormat("#.###");

        sheet.getCells().merge(4, 2, 1, 1);

        for (int k = 0; k < materials.size(); k++) {

            List<SupplyDetails> supplyDetails = suppliesDetailsRepository.filterSupplyDetails(
                    emptyToNull(materials.get(k)),
                    emptyToNull(balanceFilter.getCategory()),
                    emptyToNull(balanceFilter.getShape()),
                    emptyToNull(balanceFilter.getFinish()),
                    emptyToNull(balanceFilter.getUnit()),
                    emptyToNull(balanceFilter.getThickness())
            );

            if (!supplyDetails.isEmpty()) {
//        List<ExportSupplyDetails> exportSupplyDetails = exportSuppliesDetailsRepository.getAllByMaterial(materials.get(k));

                sheet.getCells().get("A" + rowIdx).putValue(k + 1);
                sheet.getCells().get("A" + rowIdx).setStyle(shadowStyle);

                sheet.getCells().get("B" + rowIdx).putValue(materials.get(k));
                sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);

                rowIdx++;

                double totalNumber = 0.0;
                double totalSum = 0.0;
                double totalCost = 0.0;

                for (int i = 0; i < supplyDetails.size(); i++) {

                    sheet.getCells().get("C" + rowIdx).putValue(supplyDetails.get(i).getSupplyCode());
                    sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);

                    double deductedNumber = 0.0;
                    double deductedTotal = 0.0;

                    BalanceReport importNumber = suppliesDetailsRepository.getDeductedNumberBySupplyCode(supplyDetails.get(i).getSupplyCode());
                    BalanceReport exportNumber = exportSuppliesDetailsRepository.getDeductedNumberBySupplyCode(supplyDetails.get(i).getSupplyCode());

                    if (exportNumber.getNumber() == null) {
                        deductedNumber = importNumber.getNumber() - 0;

                        deductedTotal = importNumber.getTotal() - 0;
                    } else {
                        deductedNumber = importNumber.getNumber() - exportNumber.getNumber();
                        deductedTotal = importNumber.getTotal() - exportNumber.getTotal();
                    }

                    totalNumber += deductedNumber;
                    totalSum += deductedTotal;


                    sheet.getCells().get("D" + rowIdx).putValue(df.format(deductedNumber));

                    sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);


                    sheet.getCells().get("E" + rowIdx).putValue(supplyDetails.get(i).getPrice());

                    sheet.getCells().get("E" + rowIdx).setStyle(discriptionDataStyle);


                    sheet.getCells().get("F" + rowIdx).putValue(df.format(deductedTotal));

                    sheet.getCells().get("F" + rowIdx).setStyle(discriptionDataStyle);

                    double cost = Double.parseDouble(df.format(deductedTotal * Double.parseDouble(supplyDetails.get(i).getPrice())));

                    totalCost += cost;

                    sheet.getCells().get("G" + rowIdx).putValue(cost);

                    sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);

                    rowIdx += 1;
                }
                rowIdx += 2;

                sheet.getCells().get("D" + rowIdx).putValue(df.format(totalNumber));

                sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);

                sheet.getCells().get("F" + rowIdx).putValue(df.format(totalSum));

                sheet.getCells().get("F" + rowIdx).setStyle(discriptionDataStyle);

                sheet.getCells().get("G" + rowIdx).putValue(totalCost);

                sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);

                rowIdx += 2;

            }
        }

        sheet.autoFitColumns();

        for (int i = 2; i < rowIdx; i++) {
            sheet.getCells().setRowHeight(i, 18);
        }

        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        workbook.save(pdfOutputStream, SaveFormat.PDF); // Save as PDF

        // 4. Return the PDF as a response
        ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
        InputStreamResource resource = new InputStreamResource(pdfInputStream);

        return resource;
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    public List<FilterResponse> getFilterResult(BalanceFilter balanceFilter) {
        try {
            List<String> materials = new ArrayList<>();
            if (balanceFilter.getMaterial().equals("all")) {
                materials = suppliesDetailsRepository.getMaterials();
            } else {
                materials.add(balanceFilter.getMaterial());
            }

            DecimalFormat df = new DecimalFormat("#.###");

            List<FilterResponse> filterResponses = new ArrayList<>();
            for (int k = 0; k < materials.size(); k++) {

                List<SupplyDetails> supplyDetails = suppliesDetailsRepository.filterSupplyDetails(
                        emptyToNull(materials.get(k)),
                        emptyToNull(balanceFilter.getCategory()),
                        emptyToNull(balanceFilter.getShape()),
                        emptyToNull(balanceFilter.getFinish()),
                        emptyToNull(balanceFilter.getUnit()),
                        emptyToNull(balanceFilter.getThickness())
                );

                if (!supplyDetails.isEmpty()) {

                    for (int i = 0; i < supplyDetails.size(); i++) {

                        FilterResponse filterResponse = new FilterResponse();
                        double deductedNumber = 0.0;
                        double deductedTotal = 0.0;

                        BalanceReport importNumber = suppliesDetailsRepository.getDeductedNumberBySupplyCode(supplyDetails.get(i).getSupplyCode());
                        BalanceReport exportNumber = exportSuppliesDetailsRepository.getDeductedNumberBySupplyCode(supplyDetails.get(i).getSupplyCode());

                        if (exportNumber.getNumber() == null) {
                            deductedNumber = importNumber.getNumber() - 0;

                            deductedTotal = importNumber.getTotal() - 0;
                        } else {
                            deductedNumber = importNumber.getNumber() - exportNumber.getNumber();
                            deductedTotal = importNumber.getTotal() - exportNumber.getTotal();
                        }

                        filterResponse.setMaterial(materials.get(k));
                        filterResponse.setCode(supplyDetails.get(i).getSupplyCode());
                        filterResponse.setPrice(supplyDetails.get(i).getPrice());
                        filterResponse.setTotal(df.format(deductedTotal));
                        filterResponse.setNumber(df.format(deductedNumber));
                        filterResponse.setCost(df.format(deductedTotal * Double.parseDouble(supplyDetails.get(i).getPrice())));

                        filterResponses.add(filterResponse);
                    }
                }
            }
            return filterResponses;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public InputStreamResource projectMaterialBalance(MaterialFilter materialFilter) throws Exception {

        try {
            Workbook workbook = new Workbook();

            WorksheetCollection worksheets = workbook.getWorksheets();
            Worksheet sheet = worksheets.get(0);
            sheet.setDisplayRightToLeft(false);
            sheet.getPageSetup().setPrintTitleRows("$9:$9");
            Cells cells = sheet.getCells();

            PageSetup pageSetup = sheet.getPageSetup();
            pageSetup.setOrientation(PageOrientationType.LANDSCAPE);
            pageSetup.setPaperSize(PaperSizeType.PAPER_A_3);


            pageSetup.setTopMargin(1);
            pageSetup.setBottomMargin(1);
            pageSetup.setLeftMargin(1);
            pageSetup.setRightMargin(1);

            pageSetup.setFitToPagesWide(1);     // Fit to one page wide
            pageSetup.setFitToPagesTall(0);     // Don't force page height
            pageSetup.setZoom(100);             // Prevent zoom from shrinking
            pageSetup.setPercentScale(false);   // Don't scale by percent

            Date dNow = new Date();
            SimpleDateFormat ft =
                    new SimpleDateFormat("hh:mm:ss a");

            DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");


            Style tableHeaderStyle = sheet.getCells().get("C1").getStyle();
            tableHeaderStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            tableHeaderStyle.setVerticalAlignment(TextAlignmentType.CENTER);
            tableHeaderStyle.getFont().setItalic(true);
            tableHeaderStyle.getFont().setSize(15);
            tableHeaderStyle.getFont().setBold(false);
            tableHeaderStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
            tableHeaderStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

            Style discriptionDataStyle = sheet.getCells().get("F3").getStyle();
            discriptionDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            discriptionDataStyle.setVerticalAlignment(TextAlignmentType.CENTER);
            discriptionDataStyle.getFont().setSize(15);


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
            shadowStyle.getFont().setSize(15);

            int rowNumber = 3;

            sheet.getCells().get("A" + rowNumber).putValue("#");
            sheet.getCells().get("A" + rowNumber).setStyle(tableHeaderStyle);

            sheet.getCells().get("B" + rowNumber).putValue("Material");
            sheet.getCells().get("B" + rowNumber).setStyle(tableHeaderStyle);

            sheet.getCells().get("C" + rowNumber).putValue("Number");
            sheet.getCells().get("C" + rowNumber).setStyle(tableHeaderStyle);

            sheet.getCells().get("D" + rowNumber).putValue("Total");
            sheet.getCells().get("D" + rowNumber).setStyle(tableHeaderStyle);


            int rowIdx = 4;

            DecimalFormat df = new DecimalFormat("#.###");

            sheet.getCells().merge(4, 2, 1, 1);

            double deductedNumber = 0.0;
            double deductedTotal = 0.0;

            if (materialFilter.getMaterial() != null && !materialFilter.getMaterial().isBlank()) {

                BalanceReport exportNumber = exportSuppliesDetailsRepository.materialFilter(
                        emptyToNull(materialFilter.getProject()),
                        emptyToNull(materialFilter.getJobOrder()),
                        emptyToNull(materialFilter.getMaterial()));


                sheet.getCells().get("B" + rowIdx).putValue(materialFilter.getMaterial());
                sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);

                rowIdx++;


                if (exportNumber.getNumber() == null) {
                    deductedNumber = 0;

                    deductedTotal = 0;
                } else {
                    deductedNumber = exportNumber.getNumber();

                    deductedTotal = exportNumber.getTotal();
                }

                sheet.getCells().get("C" + rowIdx).putValue(df.format(deductedNumber));

                sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);


                sheet.getCells().get("D" + rowIdx).putValue(df.format(deductedTotal));

                sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);

                rowIdx += 1;


            } else {

                System.out.println("in else block");

                List<String> materials = exportSuppliesDetailsRepository.getMaterialsByProjectCode(
                        emptyToNull(materialFilter.getProject()),
                        emptyToNull(materialFilter.getJobOrder())
                       );

                for (int i = 0; i < materials.size(); i++) {

                    System.out.println("materials.get(i) " + materials.get(i));
                    sheet.getCells().get("B" + rowIdx).putValue(materials.get(i));
                    sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);

                    rowIdx++;

                    BalanceReport balanceReport = exportSuppliesDetailsRepository.getDeductedNumberByProjectAndMaterial(materialFilter.getProject(), materials.get(i));

                    if (balanceReport.getNumber() == null) {
                        deductedNumber = 0;

                        deductedTotal = 0;
                    } else {
                        deductedNumber = balanceReport.getNumber();

                        deductedTotal = balanceReport.getTotal();
                    }


                    sheet.getCells().get("C" + rowIdx).putValue(df.format(deductedNumber));

                    sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);


                    sheet.getCells().get("D" + rowIdx).putValue(df.format(deductedTotal));

                    sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);

                    rowIdx += 2;

                }

            }


            sheet.autoFitColumns();

            for (int i = 2; i < rowIdx; i++) {
                sheet.getCells().setRowHeight(i, 18);
            }

            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
            workbook.save(pdfOutputStream, SaveFormat.PDF); // Save as PDF

            // 4. Return the PDF as a response
            ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(pdfInputStream);

            return resource;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<FilterResponse> showProjectMaterialFilter(MaterialFilter materialFilter) {

        List<FilterResponse> filterResponses = new ArrayList<>();

        try {

            double deductedNumber = 0.0;
            double deductedTotal = 0.0;
            DecimalFormat df = new DecimalFormat("#.###");

            if (materialFilter.getMaterial() != null && !materialFilter.getMaterial().isBlank()) {

                BalanceReport exportNumber = exportSuppliesDetailsRepository.materialFilter(
                        emptyToNull(materialFilter.getProject()),
                        emptyToNull(materialFilter.getJobOrder()),
                        emptyToNull(materialFilter.getMaterial()));


                if (exportNumber.getNumber() == null) {
                    deductedNumber = 0;

                    deductedTotal = 0;
                } else {
                    deductedNumber = exportNumber.getNumber();

                    deductedTotal = exportNumber.getTotal();
                }

                FilterResponse filterResponse = new FilterResponse();

                filterResponse.setMaterial(materialFilter.getMaterial());
                filterResponse.setTotal(df.format(deductedTotal));
                filterResponse.setNumber(df.format(deductedNumber));

                filterResponses.add(filterResponse);

            } else {

                System.out.println("in else block");

                List<String> materials = exportSuppliesDetailsRepository.getMaterialsByProjectCode(
                        emptyToNull(materialFilter.getProject()),
                        emptyToNull(materialFilter.getJobOrder())
                        );

                System.out.println("material: "+ materials.size());
                for (String material : materials) {

                    System.out.println("material: "+ material);
                    BalanceReport balanceReport = exportSuppliesDetailsRepository.getDeductedNumberByProjectAndMaterial(materialFilter.getProject(), material);

                    if (balanceReport.getNumber() == null) {
                        deductedNumber = 0;

                        deductedTotal = 0;
                    } else {
                        deductedNumber = balanceReport.getNumber();

                        deductedTotal = balanceReport.getTotal();
                    }

                    FilterResponse filterResponse = new FilterResponse();

                    filterResponse.setMaterial(material);
                    filterResponse.setTotal(df.format(deductedTotal));
                    filterResponse.setNumber(df.format(deductedNumber));

                    filterResponses.add(filterResponse);

                }
            }

            return filterResponses;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
