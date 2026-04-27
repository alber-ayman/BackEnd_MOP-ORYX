package com.example.demo.service.workOrder;

import com.aspose.cells.*;
import com.example.demo.models.*;
import com.example.demo.payload.SendToBody;
import com.example.demo.repository.*;
import com.example.demo.security.jwt.JwtUtils;
import com.example.demo.service.ChangeHistoryLog;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.data.RawTypeService;
import com.example.demo.service.pand.PandsService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
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
import java.time.Instant;
import java.util.*;


@Service
public class JobOrderService {

    @Autowired
    private ChangeHistoryLog changeHistoryLog;

    @Autowired
    private RawTypeService rawTypeService;

    @Autowired
    JobOrderRepository jobOrderRepository;

    @Autowired
    PandsToJobOrderRepository pandsToJobOrderRepository;

    @Autowired
    PandsRepository pandsRepository;

    @Autowired
    PandsService pandsService;

    @Autowired
    ExitJobOrderRepository exitJobOrderRepository;

    @Autowired
    ProjectProfileRepository projectProfileRepository;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private FileStorageService storageService;

//    private final AtomicLong counter = new AtomicLong();


    public ResponseEntity<List<JobOrder>> getAllJobOrders() {
        return new ResponseEntity<>(jobOrderRepository.findAll(), HttpStatus.OK);
    }

    public JobOrder getJobOrderById(Long id) {
        JobOrder jobOrder = jobOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("job order Not Found for ID: " + id));

        String fileurl = storageService.getFileByJobOrder(id);
        jobOrder.setFileDB(fileurl);

        return jobOrder;
    }

    public List<JobOrder> getByProjectCode(String projectCode) {
        try {
            return jobOrderRepository.getByProjectCode(projectCode);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<JobOrder> getByProjectId(Long id) {
        try {
            List<JobOrder> jobOrders = jobOrderRepository.getByProjectProfileId(id);
            for (int i = 0; i < jobOrders.size(); i++) {
                if (!jobOrders.get(i).isApproved()) {
                    if (jobOrders.get(i).isManufacturingManager()) {
                        jobOrders.get(i).setPendingFor("Manufacturing Manager");
                    } else if (jobOrders.get(i).isStoreManager()) {
                        jobOrders.get(i).setPendingFor("Store Manager");
                    } else if (jobOrders.get(i).isPurchasingManager()) {
                        jobOrders.get(i).setPendingFor("Purchasing Manager");
                    } else if (jobOrders.get(i).isGeneralManager()) {
                        jobOrders.get(i).setPendingFor("General Manager");
                    }
                    jobOrderRepository.save(jobOrders.get(i));
                }
            }
            return jobOrders;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResponseEntity<JobOrder> addNewJobORder(JobOrder jobOrder, HttpServletRequest request) throws SQLException {

        GregorianCalendar gcalendar = new GregorianCalendar();
//        ProjectProfile projectProfile = projectProfileRepository.getById(jobOrder.getProjectProfileId());
        Integer nextWorkOrder = jobOrderRepository.findMaxNumber(jobOrder.getProjectProfileId());
        if(nextWorkOrder == null){
            nextWorkOrder = 0;
        }
        String jobOrderNumber = nextWorkOrder + 1 + "/" + gcalendar.get(Calendar.YEAR);

        String username = changeHistoryLog.getUser(request);

        jobOrder.setCreatedBy(username);
        jobOrder.setYear(gcalendar.get(Calendar.YEAR));
        jobOrder.setJobOrderNumber(jobOrderNumber);
        jobOrder.setApproved(false);
        jobOrder.setNumber(nextWorkOrder + 1);
        jobOrderRepository.save(jobOrder);
        changeHistoryLog.saveChange(jobOrder.getJobOrderNumber(), jobOrder.toString(), jobOrder.toString(), "save", request);
//        projectProfile.setJobOrderSerial(projectProfile.getJobOrderSerial() + 1);
//        projectProfileRepository.save(projectProfile);
        return new ResponseEntity<>(jobOrder, HttpStatus.OK);
    }

    public ResponseEntity<JobOrder> updateJobOrder(Long id, JobOrder updatedJobOrder, int flag, HttpServletRequest request) {

        JobOrder jobOrder = jobOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("rawType Not Found for ID: " + id));

        jobOrderRepository.save(jobOrder);

        changeHistoryLog.saveChange(updatedJobOrder.getJobOrderNumber(), updatedJobOrder.toString(), jobOrder.toString(), "update", request);

        return new ResponseEntity<>(jobOrder, HttpStatus.OK);
    }

    public void deleteJobOrder(Long id, HttpServletRequest request) {
        try {
            JobOrder jobOrder = jobOrderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("rawType Not Found for ID: " + id));

            returnInDelete(jobOrder, request);
            changeHistoryLog.saveChange(jobOrder.getJobOrderNumber(), jobOrder.toString(), jobOrder.toString(), "delete", request);

            jobOrderRepository.deleteById(id);
        } catch (Exception e) {
            System.out.printf("deleteJobOrder");
            e.printStackTrace();
        }
    }

    public void returnInDelete(JobOrder jobOrder, HttpServletRequest request) {
        try {
            DecimalFormat df = new DecimalFormat("#.###");
            List<PandsToJobOrder> pandsToJobOrders = pandsToJobOrderRepository.jobOrdersByJobOrderId(jobOrder.getProjectProfileId(), jobOrder.getJobOrderNumber());
            for (PandsToJobOrder item : pandsToJobOrders) {
                // First delete child entries
                pandsToJobOrderRepository.deleteFromMutuleTable(item.getId());

                // Then update the related Pand
                Pand pand = pandsService.getPandByPandCode(item.getPandCode(), item.getProjectProfileId());
                pand.setRestQuantity(Double.valueOf(df.format(pand.getRestQuantity() + Double.valueOf(item.getTotal()))));
                pandsRepository.save(pand);

                changeHistoryLog.saveChange(jobOrder.getJobOrderNumber(), item.toString(), item.toString(), "delete", request);

                // Now safely delete the parent
                pandsToJobOrderRepository.deleteById(item.getId());
            }
        } catch (Exception e) {
            System.out.println("returnInDelete");
            e.printStackTrace();
        }
    }


//    public Integer getTheMaxNumber() {
//
//        Integer number = jobOrderRepository.findMaxNumber();
//
//        if (number == null) {
//            number = 0;
//        }
//
//        return number;
//    }

    public JobOrder getByJobOrder(String jobOrderNumber) {
        JobOrder jobOrder = jobOrderRepository.getByJobOrderNumber(jobOrderNumber);
        return jobOrder;
    }



    public List<JobOrder> getPendingJobOrder() {
        List<JobOrder> jobOrder = jobOrderRepository.getPendingJobOrder();
        return jobOrder;
    }

    public List<JobOrder> getPendingManufacturingJobOrder() {
        List<JobOrder> jobOrder = jobOrderRepository.getPendingManufacturingJobOrder();
        return jobOrder;
    }

    public List<JobOrder> getPendingStoreJobOrder() {
        List<JobOrder> jobOrder = jobOrderRepository.getPendingStoreJobOrder();
        return jobOrder;
    }

    public List<JobOrder> getPendingPurchaseJobOrder() {
        List<JobOrder> jobOrder = jobOrderRepository.getPendingPurchaseJobOrder();
        return jobOrder;
    }

    public JobOrder revertWorkOrder(Long jobOrderId) {

        Optional<JobOrder> jobOrder = jobOrderRepository.findById(jobOrderId);
        jobOrder.get().setReverted(true);
        jobOrderRepository.save(jobOrder.get());

        return jobOrder.get();
    }

    public List<JobOrder> getRevertedJobOrder(String userName) {
        List<JobOrder> revertedWorkOrdersByUser = jobOrderRepository.getRevertedWorkOrdersByUser(userName);

        return revertedWorkOrdersByUser;
    }

    public JobOrder sendJobOrderToUser(SendToBody sendToBody, Long id) {
        Optional<JobOrder> jobOrder = jobOrderRepository.findById(id);
        jobOrder.get().setSendingNote(sendToBody.getNote());
        jobOrder.get().setManufacturingManager(sendToBody.isManufacturingManager());
        jobOrder.get().setStoreManager(sendToBody.isStoreManager());
        jobOrder.get().setPurchasingManager(sendToBody.isPurchasingManager());
        jobOrder.get().setGeneralManager(sendToBody.isGeneralManager());
        jobOrderRepository.save(jobOrder.get());
        return jobOrder.get();
    }

    public JobOrder approveWorkOrder(Long jobOrderId) {

        Optional<JobOrder> jobOrder = jobOrderRepository.findById(jobOrderId);
        jobOrder.get().setJobOrderName(jobOrder.get().getProjectCode());
        jobOrder.get().setApproved(true);
        jobOrder.get().setReverted(false);
        jobOrder.get().setManufacturingManager(false);
        jobOrder.get().setStoreManager(false);
        jobOrder.get().setPurchasingManager(false);
        jobOrder.get().setGeneralManager(false);

        List<PandsToJobOrder> pandsToJobOrders = pandsToJobOrderRepository.jobOrdersByJobOrderId(jobOrder.get().getProjectProfileId(), jobOrder.get().getJobOrderNumber());
        DecimalFormat df = new DecimalFormat("#.###");
        if (pandsToJobOrders != null) {
            for (int i = 0; i < pandsToJobOrders.size(); i++) {


                pandsToJobOrders.get(i).setJobOrderName(jobOrder.get().getProjectCode());
                pandsToJobOrders.get(i).setJobOrderId(jobOrder.get().getProjectCode().concat("/").concat(jobOrder.get().getJobOrderNumber()));
                pandsToJobOrderRepository.save(pandsToJobOrders.get(i));
            }

            jobOrder.get().setJobOrderNumber(jobOrder.get().getProjectCode().concat("/").concat(jobOrder.get().getJobOrderNumber()));
            jobOrderRepository.save(jobOrder.get());

            Pand pand = pandsService.getPandByPandCode(pandsToJobOrders.get(0).getPandCode(), pandsToJobOrders.get(0).getProjectProfileId());

            pand.setRestQuantity(Double.valueOf(df.format(pand.getMockQuantity())));
            pandsRepository.save(pand);



        }
        return jobOrder.get();
    }

    public JobOrder getByProjectCodeAndIncNumber(String projectCode, int incNumber) {
        JobOrder jobOrder = jobOrderRepository.getByProjectCodeAndIncNumber(projectCode, incNumber);
        return jobOrder;
    }

    public JobOrder copyJobORder(String jobOrder) throws SQLException, BadRequestException {

        GregorianCalendar gcalendar = new GregorianCalendar();
        JobOrder jobOrder1 = jobOrderRepository.getByJobOrderNumber(jobOrder);
        if(!jobOrder1.isApproved()){
            throw new BadRequestException();
        }
        Integer number = getTheMaxNumber(jobOrder1.getProjectProfileId());
        int nextNumber = number + 1;
        String jobOrderNumber =jobOrder1.getProjectCode().concat("/") + nextNumber + "/" + gcalendar.get(Calendar.YEAR);
        JobOrder newJobOrder = new JobOrder();
        newJobOrder.setJobOrderNumber(jobOrderNumber);
        newJobOrder.setNumber(number + 1);


        newJobOrder.setInstallementArea(jobOrder1.getInstallementArea());
        newJobOrder.setProjectCode(jobOrder1.getProjectCode());
        newJobOrder.setProjectName(jobOrder1.getProjectName());
        newJobOrder.setProjectProfileId(jobOrder1.getProjectProfileId());
        newJobOrder.setYear(gcalendar.get(Calendar.YEAR));
        newJobOrder.setCommit(true);
        newJobOrder.setApproved(true);
        DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");

        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("hh:mm:ss a");

        newJobOrder.setJobOrderDate(formatter1.format(dNow));
        newJobOrder.setJobOrderTime(ft.format(dNow));

        List<PandsToJobOrder> pandsToJobOrders = pandsToJobOrderRepository.getByJobOrderId(jobOrder);


        jobOrderRepository.save(newJobOrder);

        DecimalFormat df = new DecimalFormat("#.###");

        List<PandsToJobOrder> pandsToJobOrderList = new ArrayList<PandsToJobOrder>();

        for (int i = 0; i < pandsToJobOrders.size(); i++) {
            PandsToJobOrder pandsToJobOrder = new PandsToJobOrder();
            Pand pand = pandsService.getPandByPandCode(pandsToJobOrders.get(i).getPandCode(), pandsToJobOrders.get(i).getProjectProfileId());
            pand.setRestQuantity(Double.parseDouble(df.format(pand.getRestQuantity() - Double.parseDouble(pandsToJobOrders.get(i).getTotal()))));
            pandsRepository.save(pand);
            long leastSigBits = System.currentTimeMillis();
            long mostSigBits = Instant.now().getEpochSecond();
            UUID uuid = new UUID(mostSigBits, leastSigBits);
//            pandsToJobOrderRepository.getByUniqueId(uuid.toString());
            pandsToJobOrder.setUniqueId(uuid.toString());

//            double total;
//
//            if (pandsToJobOrders.get(i).getUnit().equals("متر مربع")) {
//                total = (Double.parseDouble(pandsToJobOrders.get(i).getHeight()) * Double.parseDouble(pandsToJobOrders.get(i).getWidth()) * (pandsToJobOrders.get(i).getQuantity() * Double.parseDouble(pandsToJobOrders.get(i).getRepetition()))) / 10000;
//            } else if (pandsToJobOrders.get(i).getUnit().equals("متر طولى")) {
//                total = (Double.parseDouble(pandsToJobOrders.get(i).getHeight()) * (pandsToJobOrders.get(i).getQuantity() * Double.parseDouble(pandsToJobOrders.get(i).getRepetition()))) / 100;
//            } else {
//                total = pandsToJobOrders.get(i).getQuantity() * Double.parseDouble(pandsToJobOrders.get(i).getRepetition());
//            }

            pandsToJobOrder.setTotal(pandsToJobOrders.get(i).getMainTotal());
            pandsToJobOrder.setMainTotal(pandsToJobOrders.get(i).getMainTotal());
            pandsToJobOrder.setMainQuantity(pandsToJobOrders.get(i).getMainQuantity());
            pandsToJobOrder.setQuantity(pandsToJobOrders.get(i).getMainQuantity());

            pandsToJobOrder.setJobOrderId(jobOrderNumber);
            pandsToJobOrder.setRawType(pandsToJobOrders.get(i).getRawType());
//            pandsToJobOrder.setUnifiedSerial(pandsToJobOrders.get(i).getUnifiedSerial());
            pandsToJobOrder.setInstallationArea(pandsToJobOrders.get(i).getInstallationArea());
            pandsToJobOrder.setFinishType(pandsToJobOrders.get(i).getFinishType());
            pandsToJobOrder.setRawUsed(pandsToJobOrders.get(i).getRawUsed());
            pandsToJobOrder.setManufacturing(pandsToJobOrders.get(i).getManufacturing());
            pandsToJobOrder.setManufacturingCode(pandsToJobOrders.get(i).getManufacturingCode());
            pandsToJobOrder.setThickness(pandsToJobOrders.get(i).getThickness());
            pandsToJobOrder.setDescription(pandsToJobOrders.get(i).getDescription());
            pandsToJobOrder.setAdditionalDescription(pandsToJobOrders.get(i).getAdditionalDescription());
            pandsToJobOrder.setFloor(pandsToJobOrders.get(i).getFloor());
            pandsToJobOrder.setHeight(pandsToJobOrders.get(i).getHeight());
            pandsToJobOrder.setWidth(pandsToJobOrders.get(i).getWidth());
            pandsToJobOrder.setUnit(pandsToJobOrders.get(i).getUnit());
            pandsToJobOrder.setRepetition(pandsToJobOrders.get(i).getRepetition());
            pandsToJobOrder.setPandCode(pandsToJobOrders.get(i).getPandCode());
            pandsToJobOrder.setProjectName(pandsToJobOrders.get(i).getProjectName());
            pandsToJobOrder.setProjectCode(pandsToJobOrders.get(i).getProjectCode());
            pandsToJobOrder.setOfficerName(pandsToJobOrders.get(i).getOfficerName());
            pandsToJobOrder.setJobOrderType(pandsToJobOrders.get(i).getJobOrderType());
            pandsToJobOrder.setBlockNumber(pandsToJobOrders.get(i).getBlockNumber());
            pandsToJobOrder.setProjectProfileId(pandsToJobOrders.get(i).getProjectProfileId());
            pandsToJobOrder.setEngineerName(pandsToJobOrders.get(i).getEngineerName());
            pandsToJobOrder.setQuantityInPand(pandsToJobOrders.get(i).getQuantityInPand());
            pandsToJobOrderRepository.save(pandsToJobOrder);
            pandsToJobOrderList.add(pandsToJobOrder);
        }
        newJobOrder.setPandsToJobOrders(pandsToJobOrderList);
        return newJobOrder;
    }

    public InputStreamResource getJobOrderDetails(Long jobOrderId) throws Exception {
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


            Style discriptionDataStyle = sheet.getCells().get("F3").getStyle();
            discriptionDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            discriptionDataStyle.setVerticalAlignment(TextAlignmentType.CENTER);
            discriptionDataStyle.getFont().setSize(11);

//            String jobOrder = id.concat("/").concat(year);

            Date dNow = new Date();
            SimpleDateFormat ft =
                    new SimpleDateFormat("hh:mm:ss a");

            DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");

            Optional<JobOrder> jobOrderNumber = jobOrderRepository.findById(jobOrderId);
            String jobOrder = jobOrderNumber.get().getJobOrderNumber();

            sheet.getCells().get("A1").putValue("Project Name");
            sheet.getCells().get("A1").setStyle(discriptionDataStyle);

            sheet.getCells().get("B1").putValue(jobOrderNumber.get().getProjectName());
            sheet.getCells().get("B1").setStyle(discriptionDataStyle);

            sheet.getCells().get("A3").putValue("Project Code");
            sheet.getCells().get("A3").setStyle(discriptionDataStyle);

            sheet.getCells().get("B3").putValue(jobOrderNumber.get().getProjectCode());
            sheet.getCells().get("B3").setStyle(discriptionDataStyle);

            sheet.getCells().get("E1").putValue("Print Date");
            sheet.getCells().get("E1").setStyle(discriptionDataStyle);

            sheet.getCells().get("F1").putValue(formatter1.format(dNow) + " " + ft.format(dNow).toString());
            sheet.getCells().get("F1").setStyle(discriptionDataStyle);

            sheet.getCells().get("E3").putValue("Job Order #");
            sheet.getCells().get("E3").setStyle(discriptionDataStyle);

            sheet.getCells().get("F3").putValue(jobOrderNumber.get().getJobOrderNumber());
            sheet.getCells().get("F3").setStyle(discriptionDataStyle);


            Style shadowStyle = workbook.createStyle();
            shadowStyle.setPattern(BackgroundType.SOLID);
            shadowStyle.setForegroundColor(Color.getDarkGray());
            shadowStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
            shadowStyle.getFont().setSize(11);


            sheet.getCells().get("A5").putValue("Pand Code");
            sheet.getCells().get("A5").setStyle(tableHeaderStyle);

            sheet.getCells().get("B5").putValue("Quantity");
            sheet.getCells().get("B5").setStyle(tableHeaderStyle);

            sheet.getCells().get("C5").putValue("Rest Quantity");
            sheet.getCells().get("C5").setStyle(tableHeaderStyle);

            sheet.getCells().get("D5").putValue("Balance");
            sheet.getCells().get("D5").setStyle(tableHeaderStyle);


            InputStream imageStream = new ClassPathResource("static/ORYX.jpeg").getInputStream();

            // Add the image to the worksheet (X, Y coordinates in pixels)
            // Place the image inside the merged cells (A1:C5)
            int pictureIndex = sheet.getPictures().add(0, 7, imageStream);

            // Get the added picture
            Picture picture = sheet.getPictures().get(pictureIndex);

            // Optionally, set the picture to fit within the merged area
            picture.setPlacement(PlacementType.MOVE);
            picture.setWidthScale(40); // Scale the image to fit width
            picture.setHeightScale(20);

            int rowIdx = 7;

            List<String> pandsToJobOrders = pandsToJobOrderRepository.getJobOrderDetails(jobOrderNumber.get().getProjectProfileId(), jobOrder);
            Double totalQuantityInJobOrders = 0.0;
            Double totalQuantityInExitJobOrders = 0.0;
            if (pandsToJobOrders.size() > 0) {
                for (String entry : pandsToJobOrders) {

                    sheet.getCells().get("A" + rowIdx).putValue(entry);  // رقم امر شغل
                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("A" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("A" + rowIdx).setStyle(discriptionDataStyle);
                    }

                    Double totalSumInJobOrders = pandsToJobOrderRepository.getSumByPandCodeAndJobOrder(jobOrderNumber.get().getProjectProfileId(), entry, jobOrder);

                    if (totalSumInJobOrders == null) {
                        totalSumInJobOrders = 0.0;
                    }

                    totalQuantityInJobOrders += Double.valueOf(totalSumInJobOrders);

                    sheet.getCells().get("B" + rowIdx).putValue(totalSumInJobOrders); // الكميه لكل امر شغل
                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("B" + rowIdx).setStyle(discriptionDataStyle);
                    }

                    Double totalExit = exitJobOrderRepository.getSumByJobOrderAndPand(jobOrderNumber.get().getProjectCode(), entry, jobOrder);

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


                    rowIdx++;
                }
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


    public String approveStoreJobOrder(Long id) throws SQLException {
        Optional<JobOrder> jobOrder = jobOrderRepository.findById(id);
//        List<RawTypeDTO> materials = pandsToJobOrderRepository.getMaterialsByWorkOrder(jobOrder.get().getProjectProfileId(), jobOrder.get().getJobOrderNumber());

//        for (int i =0; i < materials.size() ; i++ ) {
//            RawTypes rawTypes = rawTypeService.getRawTypeByName(materials.get(i).getRawType());
//            if(rawTypes.getQuantity() - materials.get(i).getTotal() < 0){
//                return "No enough quantity for " + materials.get(i).getRawType();
//            }
//            rawTypes.setQuantity(rawTypes.getQuantity() - materials.get(i).getTotal());

//            rawTypeService.saveRawType(rawTypes);
//        }

        jobOrder.get().setStoreApproved(true);
        jobOrderRepository.save(jobOrder.get());

        return "Approved";
    }

    public JobOrder commitJobOrder(Long id) {
        Integer maxNumber = jobOrderRepository.findMaxNumber(id);

        JobOrder jobOrder = jobOrderRepository.findLastInserted(id);
        jobOrder.setCommit(true);
        jobOrder.setNumber(maxNumber + 1);
        jobOrderRepository.save(jobOrder);

        return jobOrder;
    }

    public Integer getTheMaxNumber(Long projectId) {

        Integer number = jobOrderRepository.findMaxNumber(projectId);

        if (number == null) {
            number = 0;
        }

        return number;
    }
}
