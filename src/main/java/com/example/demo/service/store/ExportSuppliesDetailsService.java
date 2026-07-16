package com.example.demo.service.store;

import com.aspose.cells.*;
import com.example.demo.models.*;
import com.example.demo.repository.ExportSuppliesDetailsRepository;
import com.example.demo.repository.ExportSupplyRepository;
import com.example.demo.repository.SupplierRepository;
import com.example.demo.repository.SuppliesDetailsRepository;
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
import java.util.Date;
import java.util.List;

@Service
public class ExportSuppliesDetailsService {

    @Autowired
    ExportSuppliesDetailsRepository exportSuppliesDetailsRepository;

    @Autowired
    SuppliesDetailsRepository suppliesDetailsRepository;

    @Autowired
    ExportSupplyRepository exportSupplyRepository;

    public ResponseEntity<List<ExportSupplyDetails>> getAllSuppliesDetails(String supplyNumber) {
        try{
            List<ExportSupplyDetails> supplyDetails = exportSuppliesDetailsRepository.getAllBySupplyNumber(supplyNumber);

            return new ResponseEntity<>(supplyDetails, HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.OK);
        }

    }

    public ResponseEntity<List<ExportSupplyDetails>> getAllSuppliesDetailsById(String supplierCode) {
        try{
            List<ExportSupplyDetails> supplyDetails = exportSuppliesDetailsRepository.getAllBySupplierCode(supplierCode);

            return new ResponseEntity<>(supplyDetails, HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.OK);
        }

    }

//    public SupplyDetails getSuppliesDetailsById(Long id) {
//        return suppliesDetailsRepository.findAllById(id);
//    }

    public ResponseEntity<ExportSupplyDetails> addNewSupplydetails(ExportSupplyDetails supplyDetails) {
        try {

            SupplyDetails details = suppliesDetailsRepository.getAllBySupplyCode(supplyDetails.getSupplyCode());
            ExportSupply exportSupply = exportSupplyRepository.getBySupplyNumber(supplyDetails.getSupplyNumber());

            Date dNow = new Date();
            SimpleDateFormat ft =
                    new SimpleDateFormat("hh:mm:ss a");
            DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");

            supplyDetails.setData(formatter1.format(dNow));
            supplyDetails.setTime(ft.format(dNow));

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

            supplyDetails.setTotal(formattedNumber);

            double restNumber = Double.parseDouble(details.getRestNumber()) - Double.parseDouble(supplyDetails.getNumber());

            double restTotal = Double.parseDouble(details.getRestTotal()) - Double.parseDouble(formattedNumber);

            if(( restNumber < 0) ||
                    ( restTotal < 0)) {
                supplyDetails.setResponseFlag(1);
                supplyDetails.setResponseMessage("Quantity in store is " + details.getRestNumber());
                return new ResponseEntity<>(supplyDetails, HttpStatus.BAD_REQUEST);
            }else {
                details.setRestNumber(df.format(restNumber));
                details.setRestTotal(df.format(restTotal));
            }

            String supplyCode = supplyDetails.getThickness().concat(supplyDetails.getFinishing())
                    .concat(supplyDetails.getShape()).concat(supplyDetails.getHeight())
                    .concat(supplyDetails.getWidth()).concat(supplyDetails.getCategory())
                    .concat(supplyDetails.getSupplierCode());

            supplyDetails.setSupplyCode(supplyCode);

            double cost = Double.parseDouble(formattedNumber) * Double.parseDouble(supplyDetails.getPrice());

            supplyDetails.setCost(df.format(cost));
            supplyDetails.setProjectCode(exportSupply.getProjectCode());
            supplyDetails.setWorkOrder(exportSupply.getWorkOrder());

            return new ResponseEntity<>(exportSuppliesDetailsRepository.save(supplyDetails), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void deleteSupplier(Long id) {
        exportSuppliesDetailsRepository.deleteById(id);
    }

    public ResponseEntity<List<SupplyDetailsProjection>> getSuppliesDetailsByMaterial(String id, String materialName) {
        try {
            return new ResponseEntity<>(exportSuppliesDetailsRepository.getSuppliesDetailsByMaterial(id, materialName), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }


    }

    public List<String> materialsByProjectId(String id) {
        try{
            List<String> materials = exportSuppliesDetailsRepository.getMaterialByProjectCode(id);
            return materials;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public List<String> materialsByWorkOrder(String id,String workOrder) {
        try {
            List<String> materials = exportSuppliesDetailsRepository.getMaterialByProjectCodeAndWorkOrder(id, workOrder);
            return materials;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public List<String> workOrderByProjectId(String id) {
        try{
            return exportSuppliesDetailsRepository.getWorkOrderByProjectCode(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public List<String> getExportedProjects() {
        try{
            List<String> exportedProjects = exportSuppliesDetailsRepository.getExportedProjects();
            return exportedProjects;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public InputStreamResource getPdf(String id) throws Exception {

        List<ExportSupplyDetails> supplyDetails = exportSuppliesDetailsRepository.getAllBySupplyNumber(id);


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

        sheet.getCells().get("A1").putValue("Supply Number");
        sheet.getCells().get("A1").setStyle(discriptionDataStyle);

        sheet.getCells().get("B1").putValue(id);
        sheet.getCells().get("B1").setStyle(discriptionDataStyle);

        sheet.getCells().get("C1").putValue("Supplier Name");
        sheet.getCells().get("C1").setStyle(discriptionDataStyle);

        sheet.getCells().get("D1").putValue(supplyDetails.getFirst().getSupplierName());
        sheet.getCells().get("D1").setStyle(discriptionDataStyle);

        sheet.getCells().get("A3").putValue("Supplier Code");
        sheet.getCells().get("A3").setStyle(discriptionDataStyle);

        sheet.getCells().get("B3").putValue(supplyDetails.getFirst().getSupplierCode());
        sheet.getCells().get("B3").setStyle(discriptionDataStyle);

        sheet.getCells().get("C3").putValue("Date: ");
        sheet.getCells().get("C3").setStyle(discriptionDataStyle);

        sheet.getCells().get("D3").putValue(supplyDetails.getFirst().getData() + " " + supplyDetails.getFirst().getTime());
        sheet.getCells().get("D3").setStyle(discriptionDataStyle);

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
}
