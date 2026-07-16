package com.example.demo.service.store;

import com.aspose.cells.*;
import com.example.demo.models.*;
import com.example.demo.repository.SupplierRepository;
import com.example.demo.repository.SuppliesDetailsRepository;
import com.example.demo.repository.SupplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class SuppliesDetailsService {

    @Autowired
    SuppliesDetailsRepository suppliesDetailsRepository;

    @Autowired
    SupplyRepository supplyRepository;

    public ResponseEntity<List<SupplyDetails>> getAllSuppliesDetails(String supplyNumber) {
        try{
            List<SupplyDetails> supplyDetails = suppliesDetailsRepository.getAllBySupplyNumber(supplyNumber);

            return new ResponseEntity<>(supplyDetails, HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.OK);
        }

    }

    public ResponseEntity<List<SupplyDetails>> getAllSuppliesDetailsById(String supplierCode) {
        try{
            List<SupplyDetails> supplyDetails = suppliesDetailsRepository.getAllBySupplierCode(supplierCode);

            return new ResponseEntity<>(supplyDetails, HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.OK);
        }

    }

    public ResponseEntity<List<String>> getSuppliesCode(String material) {
        try{
            List<String> supplyCodes = suppliesDetailsRepository.getAllSuppliesCodeByMaterial(material);

            return new ResponseEntity<>(supplyCodes, HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.OK);
        }

    }

    public ResponseEntity<SupplyDetails> getSupplyDetailsBySupplyCode(String id) {
        try {
            SupplyDetails supplyDetails = suppliesDetailsRepository.getAllBySupplyCode(id);
            return new ResponseEntity<>(supplyDetails, HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    public ResponseEntity<SupplyDetails> addNewSupplydetails(SupplyDetails supplyDetails) {
        try {
            Supply supply = supplyRepository.getAllBySupplyNumber(supplyDetails.getSupplyNumber());
            supplyDetails.setSupplierCode(supply.getSupplierCode());

            double total;
            DecimalFormat df = new DecimalFormat("#.###");

            String height;
            String width;
            if (supplyDetails.getHeight() == null) {
                height = "1";
            } else {
                height = supplyDetails.getHeight();
            }

            if (supplyDetails.getWidth() == null) {
                width = "1";
            } else {
                width = supplyDetails.getWidth();
            }

            if (supplyDetails.getUnit().equals("Square Meter")) {
                total = (Double.valueOf(height) * Double.valueOf(width) * Double.valueOf(supplyDetails.getNumber())) / 10000;
            } else if (supplyDetails.getUnit().equals("Longitudinal meter")) {
                total = (Double.valueOf(height) * Double.valueOf(supplyDetails.getNumber())) / 100;
            } else {
                total = Double.valueOf(supplyDetails.getNumber());
            }

            String formattedNumber = df.format(total);

            String supplyCode = supplyDetails.getThickness()
                    .concat(supplyDetails.getShape()).concat(supplyDetails.getHeight())
                    .concat(supplyDetails.getWidth()).concat(supplyDetails.getSupplierCode())
                    .concat(supplyDetails.getFinishing())
                    .concat(supplyDetails.getCategory());

            SupplyDetails supplyDetails1 = suppliesDetailsRepository.getAllBySupplyCode(supplyCode);

            if(supplyDetails1 != null) {
                supplyDetails1.setRestTotal(supplyDetails1.getRestTotal() + formattedNumber);
                supplyDetails1.setRestNumber(supplyDetails1.getRestNumber() + supplyDetails.getNumber());
                if (Double.parseDouble(supplyDetails1.getNumber()) > 0) {

                    double price = Double.parseDouble(supplyDetails1.getPrice()) + Double.parseDouble(supplyDetails.getPrice()) / 2;

                    double cost = Double.parseDouble(formattedNumber) * price;

                    supplyDetails1.setCost(df.format(cost));
                } else {
                    double cost = Double.parseDouble(formattedNumber) * Double.parseDouble(supplyDetails.getPrice());

                    supplyDetails1.setCost(df.format(cost));
                }

                return new ResponseEntity<>(suppliesDetailsRepository.save(supplyDetails1), HttpStatus.OK);
            }

            Date dNow = new Date();
            SimpleDateFormat ft =
                    new SimpleDateFormat("hh:mm:ss a");
            DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");

            supplyDetails.setData(formatter1.format(dNow));
            supplyDetails.setTime(ft.format(dNow));

            supplyDetails.setTotal(formattedNumber);

            supplyDetails.setRestTotal(formattedNumber);

            supplyDetails.setRestNumber(supplyDetails.getNumber());

            supplyDetails.setSupplyCode(supplyCode);

            supplyDetails.setStoreOfficer(supply.getStoreOfficer());

            double cost = Double.parseDouble(formattedNumber) * Double.parseDouble(supplyDetails.getPrice());

            supplyDetails.setCost(df.format(cost));

            return new ResponseEntity<>(suppliesDetailsRepository.save(supplyDetails), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void deleteSupplier(Long id) {
        suppliesDetailsRepository.deleteById(id);
    }

    public ResponseEntity<List<SupplyDetailsProjection>> getSuppliesDetailsByMaterial(String id, String materialName) {
        try {
            return new ResponseEntity<>(suppliesDetailsRepository.getSuppliesDetailsByMaterial(id, materialName), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }


    }

    public InputStreamResource getPdf(String id) throws Exception {

        List<SupplyDetails> supplyDetails = suppliesDetailsRepository.getAllBySupplyNumber(id);


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

        sheet.getCells().get("A1").putValue("Supplier Name");
        sheet.getCells().get("A1").setStyle(discriptionDataStyle);

        sheet.getCells().get("B1").putValue(supplyDetails.getFirst().getSupplierName());
        sheet.getCells().get("B1").setStyle(discriptionDataStyle);

        sheet.getCells().get("C1").putValue("Supplier Code");
        sheet.getCells().get("C1").setStyle(discriptionDataStyle);

        sheet.getCells().get("D1").putValue(supplyDetails.getFirst().getSupplierCode());
        sheet.getCells().get("D1").setStyle(discriptionDataStyle);

        sheet.getCells().get("A3").putValue("Store Officer");
        sheet.getCells().get("A3").setStyle(discriptionDataStyle);

        sheet.getCells().get("B3").putValue(supplyDetails.getFirst().getStoreOfficer());
        sheet.getCells().get("B3").setStyle(discriptionDataStyle);

        sheet.getCells().get("C3").putValue("Supply Number");
        sheet.getCells().get("C3").setStyle(discriptionDataStyle);

        sheet.getCells().get("D3").putValue(id);
        sheet.getCells().get("D3").setStyle(discriptionDataStyle);

        sheet.getCells().get("A5").putValue("Date: ");
        sheet.getCells().get("A5").setStyle(discriptionDataStyle);

        sheet.getCells().get("B5").putValue(supplyDetails.getFirst().getData() + " " + supplyDetails.getFirst().getTime());
        sheet.getCells().get("B5").setStyle(discriptionDataStyle);

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

        int rowNumber = 7;

        sheet.getCells().get("A" + rowNumber).putValue("#");
        sheet.getCells().get("A" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("B" + rowNumber).putValue("Material");
        sheet.getCells().get("B" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().merge(8, 2, 1, 1);

        sheet.getCells().get("C" + rowNumber).putValue("Category");
        sheet.getCells().get("C" + rowNumber).setStyle(tableHeaderStyle);


        sheet.getCells().get("D" + rowNumber).putValue("Shape");
        sheet.getCells().get("D" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("E" + rowNumber).putValue("Unit");
        sheet.getCells().get("E" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("F" + rowNumber).putValue("Thickness");
        sheet.getCells().get("F" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("G" + rowNumber).putValue("Finishing");
        sheet.getCells().get("G" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("H" + rowNumber).putValue("Number");
        sheet.getCells().get("H" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("I" + rowNumber).putValue("Height");
        sheet.getCells().get("I" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("J" + rowNumber).putValue("Width");
        sheet.getCells().get("J" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("K" + rowNumber).putValue("Price");
        sheet.getCells().get("K" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("L" + rowNumber).putValue("Total");
        sheet.getCells().get("L" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("M" + rowNumber).putValue("Cost");
        sheet.getCells().get("M" + rowNumber).setStyle(tableHeaderStyle);

        sheet.getCells().get("N" + rowNumber).putValue("Supply Code");
        sheet.getCells().get("N" + rowNumber).setStyle(tableHeaderStyle);


        int rowIdx = 9;

        DecimalFormat df = new DecimalFormat("#.###");

        for (int i = 0; i < supplyDetails.size(); i++) {

            sheet.getCells().get("A" + rowIdx).putValue(i + 1);
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("A" + rowIdx).setStyle(shadowStyle);

            } else {
                sheet.getCells().get("A" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("B" + rowIdx).putValue(supplyDetails.get(i).getMaterial());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("B" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("C" + rowIdx).putValue(supplyDetails.get(i).getCategory());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("C" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("D" + rowIdx).putValue(supplyDetails.get(i).getShape());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("D" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("E" + rowIdx).putValue(supplyDetails.get(i).getUnit());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("E" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("E" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("F" + rowIdx).putValue(supplyDetails.get(i).getThickness());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("F" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("F" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("G" + rowIdx).putValue(supplyDetails.get(i).getFinishing());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("G" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("H" + rowIdx).putValue(supplyDetails.get(i).getNumber());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("H" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("I" + rowIdx).putValue(supplyDetails.get(i).getHeight());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("I" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("I" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("J" + rowIdx).putValue(supplyDetails.get(i).getWidth());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("J" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("J" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("K" + rowIdx).putValue(supplyDetails.get(i).getPrice());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("K" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("K" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("L" + rowIdx).putValue(supplyDetails.get(i).getTotal());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("L" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("L" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("M" + rowIdx).putValue(supplyDetails.get(i).getCost());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("M" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("M" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("N" + rowIdx).putValue(supplyDetails.get(i).getSupplyCode());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("N" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("N" + rowIdx).setStyle(discriptionDataStyle);
            }

            rowIdx+=2;
        }



        sheet.getCells().setRowHeight(8, 18);

        sheet.autoFitColumns();

        for (int i = 10; i < rowIdx; i++) {
            sheet.getCells().setRowHeight(i, 35);
        }

        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        workbook.save(pdfOutputStream, SaveFormat.PDF); // Save as PDF

        // 4. Return the PDF as a response
        ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
        InputStreamResource resource = new InputStreamResource(pdfInputStream);

        return resource;
    }


    public ResponseEntity<List<String>> getThickness() {
        try{
            List<String> thickness = suppliesDetailsRepository.getThickness();

            return new ResponseEntity<>(thickness, HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    public ResponseEntity<List<String>> getImportedMaterials() {
        try{
            List<String> importedMaterials = suppliesDetailsRepository.getMaterials();

            return new ResponseEntity<>(importedMaterials, HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }
}
