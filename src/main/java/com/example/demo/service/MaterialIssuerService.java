package com.example.demo.service;

import com.aspose.cells.*;
import com.example.demo.DTO.MaterialIssueItemDto;
import com.example.demo.DTO.MaterialIssueRequestDto;
import com.example.demo.models.MaterialIssueRequest;
import com.example.demo.repository.MaterialIssueRequestReporitory;
import com.example.demo.repository.login.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaterialIssuerService {

    @Autowired
    MaterialIssueRequestReporitory materialIssueRequestReporitory;
    @Autowired
    UserRepository userRepository;

    public byte[] export(MaterialIssueRequestDto dto) throws Exception {

        Workbook workbook = new Workbook();
        Worksheet sheet = workbook.getWorksheets().get(0);
        Cells cells = sheet.getCells();

        sheet.setName("Material Issue");

        // =====================================================
        // PAGE SETUP
        // =====================================================

        PageSetup ps = sheet.getPageSetup();

        ps.setPaperSize(PaperSizeType.PAPER_A_4);
        ps.setOrientation(PageOrientationType.PORTRAIT);

        ps.setFitToPagesWide(1);
        ps.setFitToPagesTall(1);

        ps.setTopMargin(0.2);
        ps.setBottomMargin(0.2);
        ps.setLeftMargin(0.2);
        ps.setRightMargin(0.2);

        // =====================================================
        // COLUMN WIDTHS
        // =====================================================

        double[] widths = {
                6,   // A
                32,  // B
                10,  // C
                18,  // D
                10,  // E
                7,   // F
                7,   // G
                14,  // H
                18   // I
        };

        for (int i = 0; i < widths.length; i++) {
            cells.setColumnWidth(i, widths[i]);
        }

        // =====================================================
        // DEFAULT STYLE
        // =====================================================

        Style normal = workbook.createStyle();

        normal.getFont().setName("Arial");
        normal.getFont().setSize(10);

        // =====================================================
        // BORDERS STYLE
        // =====================================================

        Style borderStyle = workbook.createStyle();

        borderStyle.copy(normal);

        borderStyle.setHorizontalAlignment(
                TextAlignmentType.CENTER
        );

        borderStyle.setVerticalAlignment(
                TextAlignmentType.CENTER
        );

        borderStyle.setTextWrapped(true);

        borderStyle.getBorders()
                .getByBorderType(BorderType.TOP_BORDER)
                .setLineStyle(CellBorderType.THIN);

        borderStyle.getBorders()
                .getByBorderType(BorderType.BOTTOM_BORDER)
                .setLineStyle(CellBorderType.THIN);

        borderStyle.getBorders()
                .getByBorderType(BorderType.LEFT_BORDER)
                .setLineStyle(CellBorderType.THIN);

        borderStyle.getBorders()
                .getByBorderType(BorderType.RIGHT_BORDER)
                .setLineStyle(CellBorderType.THIN);

        // =====================================================
        // HEADER STYLE
        // =====================================================

        Style headerStyle = workbook.createStyle();

        headerStyle.copy(borderStyle);

        headerStyle.getFont().setBold(true);

        headerStyle.setPattern(BackgroundType.SOLID);

        headerStyle.setForegroundColor(
                Color.getLightGray()
        );

        // =====================================================
        // LEFT COMPANY INFO
        // A1:B5
        // =====================================================

        cells.merge(1, 0, 5, 2);

        cells.get("A2").putValue(
                "Oryx for Marble & Granite\n" +
                        "Tel        : +974 4415 4386\n" +
                        "Fax       : +974 4482 1721\n" +
                        "CR         : 76129\n" +
                        "P.O. BOX : 17982, Doha-Qatar\n" +
                        "Email    : info@oryxmarble.qa\n" +
                        "Web      : www.oryxmarble.qa"
        );

        Style leftStyle = workbook.createStyle();

        leftStyle.copy(normal);

        leftStyle.setTextWrapped(true);

        leftStyle.setVerticalAlignment(
                TextAlignmentType.TOP
        );

        cells.get("A2").setStyle(leftStyle);

        // =====================================================
        // RIGHT ARABIC INFO
        // H1:I5
        // =====================================================

        cells.merge(0, 7, 5, 2);

        cells.get("H1").putValue(
                "أوركس للرخام والجرانيت\n" +
                        "Tel        : +974 4415 4386\n" +
                        "Fax       : +974 4482 1721\n" +
                        "س.ت : 76129\n" +
                        "ص.ب : 17982 - الدوحة قطر\n" +
                        "البريد : info@oryxmarble.qa\n" +
                        "الموقع : www.oryxmarble.qa"
        );

        Style arabicStyle = workbook.createStyle();

        arabicStyle.copy(leftStyle);

        arabicStyle.setHorizontalAlignment(
                TextAlignmentType.RIGHT
        );

        cells.get("H1").setStyle(arabicStyle);

        // =====================================================
        // LOGO
        // C1:G3
        // =====================================================

        InputStream imageStream =
                new ClassPathResource("static/ORYX.jpeg")
                        .getInputStream();

        int pictureIndex =
                sheet.getPictures().add(
                        1,
                        2,
                        imageStream
                );

        Picture logo =
                sheet.getPictures().get(pictureIndex);

        logo.setWidthScale(55);
        logo.setHeightScale(55);

        // =====================================================
        // TITLE
        // C5:G5
        // =====================================================

        cells.merge(6, 2, 1, 5);

        cells.get("C7")
                .putValue("MATERIAL ISSUE REQUEST");

        Style titleStyle =
                workbook.createStyle();

//        titleStyle.copy(borderStyle);

        titleStyle.getFont().setBold(true);

        titleStyle.getFont().setSize(16);

        titleStyle.setHorizontalAlignment(
                TextAlignmentType.CENTER
        );

        titleStyle.setVerticalAlignment(
                TextAlignmentType.CENTER
        );

        cells.get("C7")
                .setStyle(titleStyle);

        // =====================================================
        // SERIAL + DATE
        // =====================================================

        cells.get("A7").putValue("Sl. No.");
        cells.get("B7").putValue("FCT-01");

        cells.get("H7").putValue("DATE");

        if (dto.getDate() != null) {
            cells.get("I7")
                    .putValue(dto.getDate().toString());
        }

        Style redStyle =
                workbook.createStyle();

        redStyle.copy(normal);

        redStyle.getFont().setBold(true);

        redStyle.getFont().setColor(
                Color.getRed()
        );

        cells.get("B7").setStyle(redStyle);

        // =====================================================
        // PROJECT INFO TABLE
        // =====================================================

        // ROW 8
        cells.merge(8, 0, 1, 2);
        cells.get("A9").putValue("PROJECT NAME");

        cells.merge(8, 2, 1, 4);
        cells.get("C9").putValue(dto.getProjectName());

        cells.merge(8, 6, 1, 1);
        cells.get("G9").putValue("WORK ORDER NO.");

        cells.merge(8, 7, 1, 2);
        cells.get("H9").putValue(dto.getWorkOrderNo());

        // ROW 9
        cells.merge(9, 0, 1, 2);
        cells.get("A10").putValue("PROJECT NUMBER");

        cells.merge(9, 2, 1, 4);
        cells.get("C10").putValue(dto.getProjectNumber());

        cells.merge(9, 6, 1, 1);
        cells.get("G10").putValue("REQUESTED BY :");

        cells.merge(9, 7, 1, 2);
        cells.get("H10").putValue(dto.getRequestedBy());
        // APPLY STYLE
        for (int r = 8; r <= 9; r++) {

            for (int c = 0; c <= 8; c++) {

                cells.get(r, c)
                        .setStyle(borderStyle);
            }
        }

        // =====================================================
        // TABLE HEADER
        // =====================================================

        int headerRow = 11;

        cells.merge(headerRow, 0, 2, 1);
        cells.get(headerRow, 0).putValue("No.");

        cells.merge(headerRow, 1, 2, 1);
        cells.get(headerRow, 1).putValue("Item");

        cells.merge(headerRow, 2, 2, 1);
        cells.get(headerRow, 2).putValue("Thick\n(cm)");

        cells.merge(headerRow, 3, 2, 1);
        cells.get(headerRow, 3).putValue("Bundle No.");

        cells.merge(headerRow, 4, 2, 1);
        cells.get(headerRow, 4).putValue("No. of\nSLABS");

        cells.merge(headerRow, 5, 1, 2);
        cells.get(headerRow, 5).putValue("Size (cm)");

        cells.get(headerRow + 1, 5).putValue("L");
        cells.get(headerRow + 1, 6).putValue("W");

        cells.merge(headerRow, 7, 2, 1);
        cells.get(headerRow, 7).putValue("Total Qty\n(m²)");

        cells.merge(headerRow, 8, 2, 1);
        cells.get(headerRow, 8).putValue("Issue / Return");

        // APPLY HEADER STYLE
        for (int r = headerRow; r <= headerRow + 1; r++) {

            for (int c = 0; c <= 8; c++) {

                cells.get(r, c)
                        .setStyle(headerStyle);
            }
        }

        // =====================================================
        // DATA ROWS
        // =====================================================

        int startRow = 13;

        for (int i = 0; i < dto.getItems().size(); i++) {

            int row = startRow + i;

            MaterialIssueItemDto item = null;

            item = dto.getItems().get(i);

            cells.get(row, 0)
                    .putValue(i + 1);

            if (item != null) {

                cells.get(row, 1)
                        .putValue(item.getItem());

                cells.get(row, 2)
                        .putValue(item.getThickCm());

                cells.get(row, 3)
                        .putValue(item.getBundleNo());

                cells.get(row, 4)
                        .putValue(item.getNoOfSlabs());

                cells.get(row, 5)
                        .putValue(item.getL());

                cells.get(row, 6)
                        .putValue(item.getW());

                cells.get(row, 7)
                        .putValue(item.getTotalSqm());

                cells.get(row, 8)
                        .putValue(item.getIssueReturn());
            }

            for (int c = 0; c <= 8; c++) {

                cells.get(row, c)
                        .setStyle(borderStyle);
            }

            cells.setRowHeight(row, 24);
        }

        // =====================================================
        // APPROVAL SECTION
        // =====================================================

        int approveRow = startRow + dto.getItems().size() + 1;

        cells.merge(approveRow, 0, 1, 4);
        cells.get(approveRow, 0)
                .putValue("Approved by:");

        List<String> factoryManager = userRepository.getFactoryManagerName();
        String factoryManagerName = "";
        if(!factoryManager.isEmpty()){
            factoryManagerName = factoryManager.getFirst();
        }

        cells.merge(approveRow + 1, 0, 2, 4);
        cells.get(approveRow + 1, 0)
                .putValue(
                        "Name:  ENG. " + factoryManagerName +" \n\n" +
                                "Factory Manager"
                );

        for (int r = approveRow;
             r <= approveRow + 2;
             r++) {

            for (int c = 0;
                 c <= 3;
                 c++) {

                cells.get(r, c)
                        .setStyle(borderStyle);
            }
        }

        cells.get(approveRow, 0)
                .setStyle(headerStyle);

        // =====================================================
        // PDF SAVE
        // =====================================================

        PdfSaveOptions pdfOptions =
                new PdfSaveOptions();

        pdfOptions.setOnePagePerSheet(true);

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        workbook.save(
                out,
                pdfOptions
        );

        saveMaterialIssueRequest(dto);
        return out.toByteArray();
    }

    private void saveMaterialIssueRequest(MaterialIssueRequestDto dto) {
        List<MaterialIssueRequest> materialIssueRequestList = new ArrayList<>();
        for(int i =0; i< dto.getItems().size();i++){
            MaterialIssueRequest materialIssueRequest = new MaterialIssueRequest();
            materialIssueRequest.setProjectName(dto.getProjectName());
            materialIssueRequest.setProjectNumber(dto.getProjectNumber());
            materialIssueRequest.setWorkOrderNo(dto.getWorkOrderNo());
            materialIssueRequest.setRequestedBy(dto.getRequestedBy());
            materialIssueRequest.setDate(dto.getDate());

            materialIssueRequest.setNo(dto.getItems().get(i).getNo());
            materialIssueRequest.setItem(dto.getItems().get(i).getItem());
            materialIssueRequest.setThickCm(dto.getItems().get(i).getThickCm());
            materialIssueRequest.setBundleNo(dto.getItems().get(i).getBundleNo());
            materialIssueRequest.setNoOfSlabs(dto.getItems().get(i).getNoOfSlabs());
            materialIssueRequest.setL(dto.getItems().get(i).getL());
            materialIssueRequest.setW(dto.getItems().get(i).getW());
            materialIssueRequest.setTotalSqm(dto.getItems().get(i).getTotalSqm());
            materialIssueRequest.setIssueReturn(dto.getItems().get(i).getIssueReturn());
            materialIssueRequestList.add(materialIssueRequest);
        }
        materialIssueRequestReporitory.saveAll(materialIssueRequestList);
    }

    public byte[] exportToSales(String workOrder) throws Exception {

        Workbook workbook = new Workbook();
        Worksheet sheet = workbook.getWorksheets().get(0);
        Cells cells = sheet.getCells();

        sheet.setName("Material Issue");

        // =====================================================
        // PAGE SETUP
        // =====================================================

        PageSetup ps = sheet.getPageSetup();

        ps.setPaperSize(PaperSizeType.PAPER_A_4);
        ps.setOrientation(PageOrientationType.PORTRAIT);

        ps.setFitToPagesWide(1);
        ps.setFitToPagesTall(1);

        ps.setTopMargin(0.2);
        ps.setBottomMargin(0.2);
        ps.setLeftMargin(0.2);
        ps.setRightMargin(0.2);

        // =====================================================
        // COLUMN WIDTHS
        // =====================================================

        double[] widths = {
                6,   // A
                32,  // B
                10,  // C
                18,  // D
                10,  // E
                7,   // F
                7,   // G
                14,  // H
                18   // I
        };

        for (int i = 0; i < widths.length; i++) {
            cells.setColumnWidth(i, widths[i]);
        }

        // =====================================================
        // DEFAULT STYLE
        // =====================================================

        Style normal = workbook.createStyle();

        normal.getFont().setName("Arial");
        normal.getFont().setSize(10);

        // =====================================================
        // BORDERS STYLE
        // =====================================================

        Style borderStyle = workbook.createStyle();

        borderStyle.copy(normal);

        borderStyle.setHorizontalAlignment(
                TextAlignmentType.CENTER
        );

        borderStyle.setVerticalAlignment(
                TextAlignmentType.CENTER
        );

        borderStyle.setTextWrapped(true);

        borderStyle.getBorders()
                .getByBorderType(BorderType.TOP_BORDER)
                .setLineStyle(CellBorderType.THIN);

        borderStyle.getBorders()
                .getByBorderType(BorderType.BOTTOM_BORDER)
                .setLineStyle(CellBorderType.THIN);

        borderStyle.getBorders()
                .getByBorderType(BorderType.LEFT_BORDER)
                .setLineStyle(CellBorderType.THIN);

        borderStyle.getBorders()
                .getByBorderType(BorderType.RIGHT_BORDER)
                .setLineStyle(CellBorderType.THIN);

        // =====================================================
        // HEADER STYLE
        // =====================================================

        Style headerStyle = workbook.createStyle();

        headerStyle.copy(borderStyle);

        headerStyle.getFont().setBold(true);

        headerStyle.setPattern(BackgroundType.SOLID);

        headerStyle.setForegroundColor(
                Color.getLightGray()
        );

        // =====================================================
        // LEFT COMPANY INFO
        // A1:B5
        // =====================================================

        cells.merge(1, 0, 5, 2);

        cells.get("A2").putValue(
                "Oryx for Marble & Granite\n" +
                        "Tel        : +974 4415 4386\n" +
                        "Fax       : +974 4482 1721\n" +
                        "CR         : 76129\n" +
                        "P.O. BOX : 17982, Doha-Qatar\n" +
                        "Email    : info@oryxmarble.qa\n" +
                        "Web      : www.oryxmarble.qa"
        );

        Style leftStyle = workbook.createStyle();

        leftStyle.copy(normal);

        leftStyle.setTextWrapped(true);

        leftStyle.setVerticalAlignment(
                TextAlignmentType.TOP
        );

        cells.get("A2").setStyle(leftStyle);

        // =====================================================
        // RIGHT ARABIC INFO
        // H1:I5
        // =====================================================

        cells.merge(0, 7, 5, 2);

        cells.get("H1").putValue(
                "أوركس للرخام والجرانيت\n" +
                        "Tel        : +974 4415 4386\n" +
                        "Fax       : +974 4482 1721\n" +
                        "س.ت : 76129\n" +
                        "ص.ب : 17982 - الدوحة قطر\n" +
                        "البريد : info@oryxmarble.qa\n" +
                        "الموقع : www.oryxmarble.qa"
        );

        Style arabicStyle = workbook.createStyle();

        arabicStyle.copy(leftStyle);

        arabicStyle.setHorizontalAlignment(
                TextAlignmentType.RIGHT
        );

        cells.get("H1").setStyle(arabicStyle);

        // =====================================================
        // LOGO
        // C1:G3
        // =====================================================

        InputStream imageStream =
                new ClassPathResource("static/ORYX.jpeg")
                        .getInputStream();

        int pictureIndex =
                sheet.getPictures().add(
                        1,
                        2,
                        imageStream
                );

        Picture logo =
                sheet.getPictures().get(pictureIndex);

        logo.setWidthScale(55);
        logo.setHeightScale(55);

        // =====================================================
        // TITLE
        // C5:G5
        // =====================================================

        cells.merge(6, 2, 1, 5);

        cells.get("C7")
                .putValue("MATERIAL ISSUE REQUEST");

        Style titleStyle =
                workbook.createStyle();

//        titleStyle.copy(borderStyle);

        titleStyle.getFont().setBold(true);

        titleStyle.getFont().setSize(16);

        titleStyle.setHorizontalAlignment(
                TextAlignmentType.CENTER
        );

        titleStyle.setVerticalAlignment(
                TextAlignmentType.CENTER
        );

        cells.get("C7")
                .setStyle(titleStyle);

        // =====================================================
        // SERIAL + DATE
        // =====================================================

        cells.get("A7").putValue("Sl. No.");
        cells.get("B7").putValue("FCT-01");

        cells.get("H7").putValue("DATE");

        List<MaterialIssueRequest> dto = materialIssueRequestReporitory.findAllByWorkOrderNo(workOrder);

        if (dto.getFirst().getDate() != null) {
            cells.get("I7")
                    .putValue(dto.getFirst().getDate().toString());
        }

        Style redStyle =
                workbook.createStyle();

        redStyle.copy(normal);

        redStyle.getFont().setBold(true);

        redStyle.getFont().setColor(
                Color.getRed()
        );

        cells.get("B7").setStyle(redStyle);

        // =====================================================
        // PROJECT INFO TABLE
        // =====================================================

        // ROW 8
        cells.merge(8, 0, 1, 2);
        cells.get("A9").putValue("PROJECT NAME");

        cells.merge(8, 2, 1, 4);
        cells.get("C9").putValue(dto.getFirst().getProjectName());

        cells.merge(8, 6, 1, 1);
        cells.get("G9").putValue("WORK ORDER NO.");

        cells.merge(8, 7, 1, 2);
        cells.get("H9").putValue(dto.getFirst().getWorkOrderNo());

        // ROW 9
        cells.merge(9, 0, 1, 2);
        cells.get("A10").putValue("PROJECT NUMBER");

        cells.merge(9, 2, 1, 4);
        cells.get("C10").putValue(dto.getFirst().getProjectNumber());

        cells.merge(9, 6, 1, 1);
        cells.get("G10").putValue("REQUESTED BY :");

        cells.merge(9, 7, 1, 2);
        cells.get("H10").putValue(dto.getFirst().getRequestedBy());
        // APPLY STYLE
        for (int r = 8; r <= 9; r++) {

            for (int c = 0; c <= 8; c++) {

                cells.get(r, c)
                        .setStyle(borderStyle);
            }
        }

        // =====================================================
        // TABLE HEADER
        // =====================================================

        int headerRow = 11;

        cells.merge(headerRow, 0, 2, 1);
        cells.get(headerRow, 0).putValue("No.");

        cells.merge(headerRow, 1, 2, 1);
        cells.get(headerRow, 1).putValue("Item");

        cells.merge(headerRow, 2, 2, 1);
        cells.get(headerRow, 2).putValue("Thick\n(cm)");

        cells.merge(headerRow, 3, 2, 1);
        cells.get(headerRow, 3).putValue("Bundle No.");

        cells.merge(headerRow, 4, 2, 1);
        cells.get(headerRow, 4).putValue("No. of\nSLABS");

        cells.merge(headerRow, 5, 1, 2);
        cells.get(headerRow, 5).putValue("Size (cm)");

        cells.get(headerRow + 1, 5).putValue("L");
        cells.get(headerRow + 1, 6).putValue("W");

        cells.merge(headerRow, 7, 2, 1);
        cells.get(headerRow, 7).putValue("Total Qty\n(m²)");

        cells.merge(headerRow, 8, 2, 1);
        cells.get(headerRow, 8).putValue("Issue / Return");

        // APPLY HEADER STYLE
        for (int r = headerRow; r <= headerRow + 1; r++) {

            for (int c = 0; c <= 8; c++) {

                cells.get(r, c)
                        .setStyle(headerStyle);
            }
        }

        // =====================================================
        // DATA ROWS
        // =====================================================

        int startRow = 13;

        for (int i = 0; i < dto.size(); i++) {

            int row = startRow + i;

            MaterialIssueRequest item = null;

            item = dto.get(i);

            cells.get(row, 0)
                    .putValue(i + 1);

            if (item != null) {

                cells.get(row, 1)
                        .putValue(item.getItem());

                cells.get(row, 2)
                        .putValue(item.getThickCm());

                cells.get(row, 3)
                        .putValue(item.getBundleNo());

                cells.get(row, 4)
                        .putValue(item.getNoOfSlabs());

                cells.get(row, 5)
                        .putValue(item.getL());

                cells.get(row, 6)
                        .putValue(item.getW());

                cells.get(row, 7)
                        .putValue(item.getTotalSqm());

                cells.get(row, 8)
                        .putValue(item.getIssueReturn());
            }

            for (int c = 0; c <= 8; c++) {

                cells.get(row, c)
                        .setStyle(borderStyle);
            }

            cells.setRowHeight(row, 24);
        }

        // =====================================================
        // APPROVAL SECTION
        // =====================================================

        int approveRow = startRow + dto.size() + 1;

        cells.merge(approveRow, 0, 1, 4);
        cells.get(approveRow, 0)
                .putValue("Approved by:");

        List<String> factoryManager = userRepository.getFactoryManagerName();
        String factoryManagerName = "";
        if(!factoryManager.isEmpty()){
            factoryManagerName = factoryManager.getFirst();
        }

        cells.merge(approveRow + 1, 0, 2, 4);
        cells.get(approveRow + 1, 0)
                .putValue(
                        "Name:  ENG. "+factoryManagerName+" \n\n" +
                                "Factory Manager"
                );

        for (int r = approveRow;
             r <= approveRow + 2;
             r++) {

            for (int c = 0;
                 c <= 3;
                 c++) {

                cells.get(r, c)
                        .setStyle(borderStyle);
            }
        }

        cells.get(approveRow, 0)
                .setStyle(headerStyle);

        // =====================================================
        // PDF SAVE
        // =====================================================

        PdfSaveOptions pdfOptions =
                new PdfSaveOptions();

        pdfOptions.setOnePagePerSheet(true);

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        workbook.save(
                out,
                pdfOptions
        );
        return out.toByteArray();
    }

}