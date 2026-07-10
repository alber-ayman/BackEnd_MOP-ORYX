package com.example.demo.service.workOrder;


import com.example.demo.models.*;
import com.example.demo.payload.CheckLimitResponse;
import com.example.demo.repository.ExitJobOrderRepository;
import com.example.demo.repository.PandsToJobOrderRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ExitJobOrderService {

    @Autowired
    ExitJobOrderRepository exitJobOrderRepository;

    @Autowired
    PandsToJobOrderService pandsToJobOrderService;

    @Autowired
    PandsToJobOrderRepository pandsToJobOrderRepository;

    @Autowired
    JobOrderService jobOrderService;

    private static final AtomicInteger counter = new AtomicInteger(1000);

    public ResponseEntity<List<ExitJobOrder>> getAll() {

        return new ResponseEntity<>(exitJobOrderRepository.findAll(), HttpStatus.OK);

    }

    public ResponseEntity<JobOrderParent> saveChildPand(JobOrderParent exitJobOrders, String unifiedSerial) {
        try {

            List<ExitJobOrder> jobOrders = exitJobOrderRepository.findByUnifiedSerial(unifiedSerial);
            System.out.println("starting saveExitJobOrder");
            if (!jobOrders.isEmpty()) {
                exitJobOrders.setFlag(1);
                exitJobOrders.setMessage("Bill Number is Already Taken");
                return new ResponseEntity<>(exitJobOrders, HttpStatus.OK);
            }

            for (int i = 0; i < exitJobOrders.getPandsToJobOrderList().size(); i++) {
                if (exitJobOrders.getPandsToJobOrderList().get(i).getQuantityUsedRaws() == null) {
                    exitJobOrders.setFlag(1);
                    exitJobOrders.setMessage("Please enter the used quantity for pand:" + exitJobOrders.getPandsToJobOrderList().get(i).getPandCode());
                    return new ResponseEntity<>(exitJobOrders, HttpStatus.OK);
                }
            }

            Double total ;
            DecimalFormat df = new DecimalFormat("#.###");
//        NumberFormat format = NumberFormat.getInstance(Locale.US);
            String serialNumber = "";
            List<PandsToJobOrder> pandsToJobOrder = pandsToJobOrderService.getByJobOrderId(exitJobOrders.getPandsToJobOrderList().getFirst().getJobOrderId());

            int count = 0;
            for (int i = 0; i < exitJobOrders.getPandsToJobOrderList().size(); i++) {
                count++;
                ExitJobOrder exitJobOrder ;
                exitJobOrder = mappingJobOrder(exitJobOrders.getPandsToJobOrderList().get(i));
                System.out.println("unit: " + exitJobOrders.getPandsToJobOrderList().get(i).getUnit());
                if (exitJobOrders.getPandsToJobOrderList().get(i).getUnit().equals("Square Meter")) {
                    total = (Double.parseDouble(exitJobOrders.getPandsToJobOrderList().get(i).getHeight()) * Double.parseDouble(exitJobOrders.getPandsToJobOrderList().get(i).getWidth()) * exitJobOrders.getPandsToJobOrderList().get(i).getQuantity()) / 10000.0;
                } else if (exitJobOrders.getPandsToJobOrderList().get(i).getUnit().equals("Longitudinal meter")) {
                    total = (Double.parseDouble(exitJobOrders.getPandsToJobOrderList().get(i).getHeight()) * exitJobOrders.getPandsToJobOrderList().get(i).getQuantity()) / 100.0;
                } else {
                    total = exitJobOrders.getPandsToJobOrderList().get(i).getQuantity();
                    System.out.println("?????????????????");
                }

                total = Double.valueOf(df.format(total));
                System.out.println(":::::: " + total + " ::::::");
                System.out.println("quantyyyy:" + exitJobOrders.getPandsToJobOrderList().get(i).getQuantity());

                for (PandsToJobOrder toJobOrder : pandsToJobOrder) {
                    if ((exitJobOrder.getPandCode().equals(toJobOrder.getPandCode()))
                            && (exitJobOrder.getProjectCode().equals(toJobOrder.getProjectCode()))
                            && (exitJobOrder.getHeight().equals(toJobOrder.getHeight()))
                            && (exitJobOrder.getWidth().equals(toJobOrder.getWidth()))
                            && (exitJobOrder.getUniqueId().equals(toJobOrder.getUniqueId()))
                    ) {
                        exitJobOrder.setManufacturingCode(toJobOrder.getManufacturingCode());
                        if (toJobOrder.getQuantity() < exitJobOrder.getQuantity()) {
                            exitJobOrders.setFlag(1);
                            exitJobOrders.setMessage("The Required Quantity " + exitJobOrder.getQuantity() + " Exceeding Quantity In BOQ Item " + toJobOrder.getPandCode());
                            return new ResponseEntity<>(exitJobOrders, HttpStatus.OK);
                        }
                    }
                }
                if (i == 0) {
                    serialNumber = generateSerialNumber();
                }
                Date dNow = new Date();
                SimpleDateFormat ft =
                        new SimpleDateFormat("hh:mm:ss a");

                DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");

                String formattedNumber = df.format(total);
                exitJobOrder.setTotal(formattedNumber);
                exitJobOrder.setQuantity(exitJobOrders.getPandsToJobOrderList().get(i).getQuantity());
//                exitJobOrder.setUniqueId(exitJobOrders.getPandsToJobOrderList().get(i).getUniqueId());
                exitJobOrder.setSerialNumber(serialNumber);
                exitJobOrder.setUnifiedSerial(unifiedSerial);
                exitJobOrder.setExitDate(formatter1.format(dNow)+ " " + ft.format(dNow));

                exitJobOrderRepository.save(exitJobOrder);
            }
            System.out.println("ending saveExitJobOrder");
            return new ResponseEntity<>(exitJobOrders, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ExitJobOrder mappingJobOrder(PandsToJobOrder updatedJobOrder) {

        ExitJobOrder jobOrder = new ExitJobOrder();

//        jobOrder.setExitJobOrderId(generateUniqueId());
        jobOrder.setUniqueId(updatedJobOrder.getUniqueId());
        jobOrder.setJobOrderId(updatedJobOrder.getJobOrderId());
        jobOrder.setProjectProfileId((updatedJobOrder.getProjectProfileId()));
        jobOrder.setProjectCode(updatedJobOrder.getProjectCode());
        jobOrder.setProjectName(updatedJobOrder.getProjectName());
        jobOrder.setEngineerName(updatedJobOrder.getEngineerName());
        jobOrder.setJobOrderType(updatedJobOrder.getJobOrderType());
        jobOrder.setInstallationArea(updatedJobOrder.getInstallationArea());
        jobOrder.setQuantityUsedRaws(updatedJobOrder.getQuantityUsedRaws());
        jobOrder.setPandCode(updatedJobOrder.getPandCode());
        jobOrder.setDescription(updatedJobOrder.getDescription());
        jobOrder.setManufacturing(updatedJobOrder.getManufacturing());
//        jobOrder.setManufacturingCode(updatedJobOrder.getManufacturingCode());
        jobOrder.setRawType(updatedJobOrder.getRawType());
        jobOrder.setRawUsed(updatedJobOrder.getRawUsed());
        jobOrder.setFinishType(updatedJobOrder.getFinishType());
        jobOrder.setThickness(updatedJobOrder.getThickness());
        jobOrder.setBlockNumber(updatedJobOrder.getBlockNumber());
        jobOrder.setFloor(updatedJobOrder.getFloor());
        jobOrder.setOfficerName(updatedJobOrder.getOfficerName());
        jobOrder.setHeight(updatedJobOrder.getHeight());
        jobOrder.setWidth(updatedJobOrder.getWidth());
        jobOrder.setRepetition(updatedJobOrder.getRepetition());
        jobOrder.setTotal(updatedJobOrder.getTotal());
        jobOrder.setUnit(updatedJobOrder.getUnit());
        jobOrder.setQuantity(updatedJobOrder.getQuantity());
        jobOrder.setAdditionalDescription(updatedJobOrder.getAdditionalDescription());

        return jobOrder;
    }

    public static String generateSerialNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHss");
        String timestamp = sdf.format(new Date());
        int uniqueId = counter.getAndIncrement();
        return timestamp + uniqueId;
    }

    public List<ExitJobOrder> getByJobOrderId(String id) {

        return exitJobOrderRepository.findByJobOrderId(id);
    }

    public ResponseEntity<List<ExitJobOrder>> getAllExitJobOrders(String jobOrderid) {
        return new ResponseEntity<>(exitJobOrderRepository.findByJobOrderId(jobOrderid), HttpStatus.OK);
    }

    public ResponseEntity<ExitJobOrder> getExitById(Long jobOrderid) {
        Optional<ExitJobOrder> jobOrder = exitJobOrderRepository.findById(jobOrderid);
        return new ResponseEntity<>(jobOrder.get(), HttpStatus.OK);
    }

    public void checkQuantity() {
        List<ExitJobOrder> jobOrder = exitJobOrderRepository.findAll();
        for (ExitJobOrder exitJobOrder : jobOrder) {
            if (exitJobOrder.getQuantity() == 0) {
                exitJobOrderRepository.delete(exitJobOrder);
            }
        }
    }


    public CheckLimitResponse returnJobOrder(JobOrderParent jobOrderParent) {

        CheckLimitResponse checkLimitResponse = new CheckLimitResponse();

        DecimalFormat df = new DecimalFormat("#.###");

        for (int i = 0; i < jobOrderParent.getPandsToJobOrderList().size(); i++) {

            PandsToJobOrder requestItem =
                    jobOrderParent.getPandsToJobOrderList().get(i);

        /*
         |--------------------------------------------------------------------------
         | GET EXIT JOB ORDER
         |--------------------------------------------------------------------------
         */

            ExitJobOrder exitJobOrder = exitJobOrderRepository
                    .findByUniqueId(requestItem.getUniqueId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Exit Job Order not found for uniqueId: "
                                            + requestItem.getUniqueId()
                            )
                    );

        /*
         |--------------------------------------------------------------------------
         | GET ORIGINAL PANDS JOB ORDER
         |--------------------------------------------------------------------------
         */

            PandsToJobOrder pandsToJobOrder = pandsToJobOrderRepository
                    .findByUniqueId(exitJobOrder.getUniqueId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Pands To Job Order not found for uniqueId: "
                                            + exitJobOrder.getUniqueId()
                            )
                    );

        /*
         |--------------------------------------------------------------------------
         | CALCULATE TOTAL
         |--------------------------------------------------------------------------
         */

            double total;

            if ("Square Meter".equals(exitJobOrder.getUnit())) {

                total =
                        (
                                Double.parseDouble(exitJobOrder.getHeight())
                                        * Double.parseDouble(exitJobOrder.getWidth())
                                        * requestItem.getQuantity()
                        ) / 10000;

            } else if ("Longitudinal meter".equals(exitJobOrder.getUnit())) {

                total =
                        (
                                Double.parseDouble(exitJobOrder.getHeight())
                                        * requestItem.getQuantity()
                        ) / 100;

            } else {

                total = requestItem.getQuantity();
            }

        /*
         |--------------------------------------------------------------------------
         | VALIDATION
         |--------------------------------------------------------------------------
         */

            double mainTotal =
                    Double.parseDouble(pandsToJobOrder.getMainTotal());

            if (total > mainTotal) {

                checkLimitResponse.setFlag(1);
                checkLimitResponse.setMessage(
                        "Returned Number Incorrect For: "
                                + pandsToJobOrder.getPandCode()
                );

                return checkLimitResponse;
            }

        /*
         |--------------------------------------------------------------------------
         | UPDATE PANDS TO JOB ORDER
         |--------------------------------------------------------------------------
         */

            pandsToJobOrder.setQuantity(
                    pandsToJobOrder.getQuantity()
                            + requestItem.getQuantity()
            );

            pandsToJobOrderRepository.save(pandsToJobOrder);

        /*
         |--------------------------------------------------------------------------
         | UPDATE EXIT JOB ORDER
         |--------------------------------------------------------------------------
         */

            exitJobOrder.setQuantity(
                    exitJobOrder.getQuantity()
                            + requestItem.getQuantity()
            );

            exitJobOrder.setTotal(
                    String.valueOf(
                            Double.parseDouble(exitJobOrder.getTotal())
                                    + Double.parseDouble(df.format(total))
                    )
            );

            exitJobOrderRepository.save(exitJobOrder);

        /*
         |--------------------------------------------------------------------------
         | SUCCESS RESPONSE
         |--------------------------------------------------------------------------
         */

            checkLimitResponse.setFlag(0);
            checkLimitResponse.setMessage(
                    "Returned Successfully For: "
                            + pandsToJobOrder.getPandCode()
            );
        }

        return checkLimitResponse;
    }


//    public CheckLimitResponse returnJobOrderOld(JobOrderParent jobOrderParent) {
//
//        CheckLimitResponse checkLimitResponse = new CheckLimitResponse();
//        for (int i = 0; i < jobOrderParent.getPandsToJobOrderList().size(); i++) {
//            Optional<ExitJobOrder> jobOrder = exitJobOrderRepository.findByUniqueId(jobOrderParent.getPandsToJobOrderList().get(i).getUniqueId());
//            Optional<PandsToJobOrder> pandsToJobOrder = pandsToJobOrderRepository.findByUniqueId(jobOrder.get().getUniqueId());
////            double value = Double.valueOf(pandsToJobOrder.getTotal()) + Double.valueOf(jobOrderParent.getPandsToJobOrderList().get(i).getTotal());
//
//            double total;
//            DecimalFormat df = new DecimalFormat("#.###");
//
//            if (jobOrder.get().getUnit().equals("Square Meter")) {
//                total = (Double.parseDouble(jobOrder.get().getHeight()) * Double.parseDouble(jobOrder.get().getWidth()) * jobOrder.get().getQuantity()) / 10000;
//            } else if (jobOrder.get().getUnit().equals("Longitudinal meter")) {
//                total = (Double.parseDouble(jobOrder.get().getHeight()) * jobOrder.get().getQuantity()) / 100;
//            } else {
//                total = jobOrder.get().getQuantity();
//            }
//
//            if (total > Double.parseDouble(pandsToJobOrder.get().getMainTotal())) {
//                checkLimitResponse.setFlag(1);
//                checkLimitResponse.setMessage("Returned Number Incorrect For: " + pandsToJobOrder.get().getPandCode());
//                return checkLimitResponse;
//            } else {
//                checkLimitResponse.setFlag(0);
//                checkLimitResponse.setMessage("Returned Successfully" + pandsToJobOrder.get().getPandCode());
//            }
//            pandsToJobOrder.get().setQuantity(pandsToJobOrder.get().getQuantity() + (jobOrderParent.getPandsToJobOrderList().get(i).getQuantity()));
////            pandsToJobOrder.setTotal(String.valueOf(Double.valueOf(pandsToJobOrder.getTotal()) + Double.valueOf(df.format(total))));
//            pandsToJobOrderRepository.save(pandsToJobOrder.get());
//            jobOrder.get(jobOrder.size() - 1).setQuantity(jobOrder.get(jobOrder.size() - 1).getQuantity() + jobOrderParent.getPandsToJobOrderList().get(i).getQuantity());
//            jobOrder.get(jobOrder.size() - 1).setTotal(String.valueOf(Double.parseDouble(jobOrder.get(jobOrder.size() - 1).getTotal()) + Double.parseDouble(df.format(total))));
//            exitJobOrderRepository.save(jobOrder.get(jobOrder.size() - 1));
//
//        }
//        return checkLimitResponse;
//    }

    public void deleteJobOrder(Long id) {
        try {
            DecimalFormat df = new DecimalFormat("#,##0.000");

            ExitJobOrder jobOrder = exitJobOrderRepository.getReferenceById(id);
//            PandsToJobOrder pandsToJobOrder = pandsToJobOrderRepository.findAllByJobOrderIdAndPandCode(jobOrder.getJobOrderId(), jobOrder.getPandCode(), jobOrder.getHeight(), jobOrder.getWidth());
            Optional<PandsToJobOrder> pandsToJobOrder = pandsToJobOrderRepository.findByUniqueIdAndJobOrderIdAndWidthAndHeight(jobOrder.getUniqueId(), jobOrder.getJobOrderId(),jobOrder.getWidth(), jobOrder.getHeight());

            pandsToJobOrder.get().setQuantity(pandsToJobOrder.get().getQuantity() + jobOrder.getQuantity());
            NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
            Number number = format.parse(pandsToJobOrder.get().getTotal());
            double value = number.doubleValue();
            double result = value + Double.parseDouble(jobOrder.getTotal());
            pandsToJobOrder.get().setTotal(df.format(result));

            exitJobOrderRepository.delete(jobOrder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteJobOrderBySerialNumber(String serialNumber) {
        try {
            double total;
            DecimalFormat df = new DecimalFormat("#.###");

            List<ExitJobOrder> jobOrder = exitJobOrderRepository.findBySerialNumber(serialNumber);
            for (int i = 0; i < jobOrder.size(); i++) {
//                PandsToJobOrder pandsToJobOrder = pandsToJobOrderRepository.findAllByJobOrderIdAndPandCode(jobOrder.getJobOrderId(), jobOrder.getPandCode(), jobOrder.getHeight(), jobOrder.getWidth());
                Optional<PandsToJobOrder> pandsToJobOrder = pandsToJobOrderRepository.findByUniqueIdAndJobOrderIdAndWidthAndHeight(jobOrder.get(i).getUniqueId(), jobOrder.get(i).getJobOrderId(),jobOrder.get(i).getWidth(),jobOrder.get(i).getHeight());

                pandsToJobOrder.get().setQuantity(pandsToJobOrder.get().getQuantity() + jobOrder.get(i).getQuantity());
                pandsToJobOrder.get().setTotal(df.format(Double.parseDouble(pandsToJobOrder.get().getTotal()) + Double.parseDouble(jobOrder.get(i).getTotal())));

                exitJobOrderRepository.delete(jobOrder.get(i));
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public ByteArrayOutputStream buildFile(String id) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("exit Orders");
            sheet.setRightToLeft(false);


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


            XSSFPrintSetup printSetup = (XSSFPrintSetup) sheet.getPrintSetup();
            printSetup.setLandscape(true);

            // Header
            Row firstRow = sheet.createRow(0);
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


            CellStyle cellStyle = workbook.createCellStyle();
//            cellStyle.setFont(font);
            cellStyle.setAlignment(HorizontalAlignment.CENTER);


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

            firstRow.createCell(5).setCellValue("Unit");
            firstRow6.setCellStyle(cellStyle);
            firstRow.createCell(6).setCellValue("Manufacturing");
            firstRow7.setCellStyle(cellStyle);
            firstRow.createCell(7).setCellValue("Material");
            firstRow8.setCellStyle(cellStyle);
            firstRow.createCell(8).setCellValue("Material Used");
            firstRow9.setCellStyle(cellStyle);
            firstRow.createCell(9).setCellValue("Finishing");
            firstRow10.setCellStyle(cellStyle);
            firstRow.createCell(10).setCellValue("Thickness");
            firstRow11.setCellStyle(cellStyle);
            firstRow.createCell(11).setCellValue("Quantity");
            firstRow5.setCellStyle(cellStyle);
            firstRow.createCell(12).setCellValue("Height");
            firstRow12.setCellStyle(cellStyle);
            firstRow.createCell(13).setCellValue("Width");
            firstRow13.setCellStyle(cellStyle);
            firstRow.createCell(14).setCellValue("Total");
            firstRow14.setCellStyle(cellStyle);
            firstRow.createCell(15).setCellValue("Quantity Used");
            firstRow15.setCellStyle(cellStyle);


            int rowIdx = 1;

            List<ExitJobOrder> pands = getByJobOrderId(id);


            for (int i = 0; i < pands.size(); i++) {
                Row row = sheet.createRow(rowIdx);
                Cell cellRawType = row.createCell(0);
                cellRawType.setCellValue(pands.get(i).getProjectName());
                cellRawType.setCellStyle(cellStyle);

                Cell projectCode = row.createCell(1);
                projectCode.setCellValue(pands.get(i).getProjectCode());
                projectCode.setCellStyle(cellStyle);

                Cell engName = row.createCell(2);
                engName.setCellValue(pands.get(i).getEngineerName());
                engName.setCellStyle(cellStyle);

                Cell pandCode = row.createCell(3);
                pandCode.setCellValue(pands.get(i).getPandCode());
                pandCode.setCellStyle(cellStyle);

                Cell description = row.createCell(4);
                description.setCellValue(pands.get(i).getDescription());
                description.setCellStyle(cellStyle);


                Cell unit = row.createCell(5);
                unit.setCellValue(pands.get(i).getUnit());
                unit.setCellStyle(cellStyle);

                Cell manufacturing = row.createCell(6);
                manufacturing.setCellValue(pands.get(i).getManufacturing());
                manufacturing.setCellStyle(cellStyle);

                Cell rawType = row.createCell(7);
                rawType.setCellValue(pands.get(i).getRawType());
                rawType.setCellStyle(cellStyle);

                Cell rawUsed = row.createCell(8);
                rawUsed.setCellValue(pands.get(i).getRawUsed());
                rawUsed.setCellStyle(cellStyle);

                Cell finishType = row.createCell(9);
                finishType.setCellValue(pands.get(i).getFinishType());
                finishType.setCellStyle(cellStyle);

                Cell thickness = row.createCell(10);
                thickness.setCellValue(pands.get(i).getThickness());
                thickness.setCellStyle(cellStyle);

                Cell quantity = row.createCell(11);
                quantity.setCellValue(pands.get(i).getQuantity());
                quantity.setCellStyle(cellStyle);

                Cell height = row.createCell(12);
                height.setCellValue(pands.get(i).getHeight());
                height.setCellStyle(cellStyle);

                Cell width = row.createCell(13);
                width.setCellValue(pands.get(i).getWidth());
                width.setCellStyle(cellStyle);

                Cell total = row.createCell(14);
                total.setCellValue(pands.get(i).getTotal());
                total.setCellStyle(cellStyle);

                Cell quantityUsedRaws = row.createCell(15);
                quantityUsedRaws.setCellValue(pands.get(i).getQuantityUsedRaws());
                quantityUsedRaws.setCellStyle(cellStyle);

                rowIdx++;
            }

            System.out.println("////////////////////////////");
//            sheet.autoSizeColumn(0);
//            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
//            sheet.autoSizeColumn(3);
            sheet.autoSizeColumn(8);
            sheet.autoSizeColumn(9);
            sheet.autoSizeColumn(15);

            System.err.println("rrrrrrrrrrrrrrrrrrrrrrrrrrr");
            ByteArrayOutputStream fileOut = new ByteArrayOutputStream();
            workbook.write(fileOut);
            workbook.close();

            System.err.println("zzzzzzzzzzzzzzzzzzzzzzzzzzzz");
            return fileOut;

        } catch (Exception e) {

            System.err.println("xxxxxxxxxxxxxxxxxxxxxxx");
            System.out.println("catch");
            e.printStackTrace();
        }
        return null;
    }

    public ResponseEntity<ExitJobOrder> updateChildPand(ExitJobOrder updatedJobOrder) {
        try {
            Optional<ExitJobOrder> jobOrder = exitJobOrderRepository.findByUniqueId(updatedJobOrder.getUniqueId());

            double total;
            DecimalFormat df = new DecimalFormat("#.###");

            if (updatedJobOrder.getUnit().equals("Square Meter")) {
                total = (Double.parseDouble(updatedJobOrder.getHeight()) * Double.parseDouble(updatedJobOrder.getWidth()) * Double.parseDouble(String.valueOf(updatedJobOrder.getQuantity()))) / 10000;
            } else if (updatedJobOrder.getUnit().equals("Longitudinal meter")) {
                total = (Double.parseDouble(updatedJobOrder.getHeight()) * Double.parseDouble(String.valueOf(updatedJobOrder.getQuantity()))) / 100;
            } else {
                total = Double.parseDouble(String.valueOf(updatedJobOrder.getQuantity()));
            }

//        jobOrder.setJobOrderId(updatedJobOrder.getJobOrderId());
//        jobOrder.setProjectProfileId((updatedJobOrder.getProjectProfileId()));
//        jobOrder.setProjectCode(updatedJobOrder.getProjectCode());
//        jobOrder.setProjectName(updatedJobOrder.getProjectName());
//        jobOrder.setEngineerName(updatedJobOrder.getEngineerName());
//        jobOrder.setJobOrderType(updatedJobOrder.getJobOrderType());
//        jobOrder.setInstallationArea(updatedJobOrder.getInstallationArea());
//        jobOrder.setQuantityUsedRaws(updatedJobOrder.getQuantityUsedRaws());
//        jobOrder.setPandCode(updatedJobOrder.getPandCode());
//        jobOrder.setDescription(updatedJobOrder.getDescription());
//        jobOrder.setManufacturing(updatedJobOrder.getManufacturing());
//        jobOrder.setRawType(updatedJobOrder.getRawType());
//        jobOrder.setRawUsed(updatedJobOrder.getRawUsed());
//        jobOrder.setFinishType(updatedJobOrder.getFinishType());
            jobOrder.get().setThickness(updatedJobOrder.getThickness());
//        jobOrder.setBlockNumber(updatedJobOrder.getBlockNumber());
//        jobOrder.setFloor(updatedJobOrder.getFloor());
//        jobOrder.setOfficerName(updatedJobOrder.getOfficerName());
            jobOrder.get().setHeight(updatedJobOrder.getHeight());
            jobOrder.get().setWidth(updatedJobOrder.getWidth());
            jobOrder.get().setRepetition(updatedJobOrder.getRepetition());
            jobOrder.get().setQuantityUsedRaws(updatedJobOrder.getQuantityUsedRaws());
            jobOrder.get().setTotal(df.format(total));
            jobOrder.get().setUnit(updatedJobOrder.getUnit());
            jobOrder.get().setQuantity(updatedJobOrder.getQuantity());
            jobOrder.get().setQuantityDelivered(updatedJobOrder.getQuantityDelivered());


//        jobOrder.setAdditionalDescription(updatedJobOrder.getAdditionalDescription());

            exitJobOrderRepository.save(jobOrder.get());

            return new ResponseEntity<>(jobOrder.get(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        }

    }

    public ResponseEntity<List<String>> getAllExitJobOrdersBySerial(String jobOrderid) {
        List<String> exitJobOrders = exitJobOrderRepository.findDistinctSerialNumbersByJobOrderId(jobOrderid);
        return new ResponseEntity<>(exitJobOrders, HttpStatus.OK);
    }

    public ResponseEntity<List<ExitJobOrder>> getAllExitJobOrdersBySpacificSerial(String serialNumber) {
        List<ExitJobOrder> exitJobOrders = exitJobOrderRepository.findBySerialNumber(serialNumber);
        return new ResponseEntity<>(exitJobOrders, HttpStatus.OK);
    }

    public ResponseEntity<List<String>> getAllExitJobOrdersByProject(Long id) {
        List<String> exitJobOrders = exitJobOrderRepository.findDistinctSerialNumbersByProjectProfileId(id);
        return new ResponseEntity<>(exitJobOrders, HttpStatus.OK);
    }

    public List<ExitJobOrder> getReturnsById(Long jobOrderNumber) {
        JobOrder jobOrder = jobOrderService.getJobOrderById(jobOrderNumber);
        return exitJobOrderRepository.findReturnedByJobOrderId(jobOrder.getJobOrderNumber());

    }
}
