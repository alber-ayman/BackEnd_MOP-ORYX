package com.example.demo.service.workOrder;

import com.aspose.cells.*;
import com.example.demo.WorkOrderStatus;
import com.example.demo.models.*;
import com.example.demo.payload.SendToBody;
import com.example.demo.payload.login.response.MessageResponse;
import com.example.demo.repository.*;
import com.example.demo.service.ChangeHistoryLog;
import com.example.demo.service.pand.PandsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
@Slf4j
@RequiredArgsConstructor
public class JobOrderService {

    private final ChangeHistoryLog changeHistoryLog;

    private final JobOrderRepository jobOrderRepository;

    private final PandsToJobOrderRepository pandsToJobOrderRepository;

    private final PandsRepository pandsRepository;

    private final PandsService pandsService;

    private final ExitJobOrderRepository exitJobOrderRepository;

    public ResponseEntity<List<JobOrder>> getAllJobOrders() {
        return new ResponseEntity<>(jobOrderRepository.FindAllGroupByProject(), HttpStatus.OK);
    }

    public JobOrder getJobOrderById(Long id) {
        return jobOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("job order Not Found for ID: " + id));
    }

    public JobOrder getJobOrderByNumber(String number) {
        return jobOrderRepository.getByJobOrderNumber(number);
    }

    public List<JobOrder> getByProjectCode(String projectCode) {
        try {
            return jobOrderRepository.getByProjectCode(projectCode);
        } catch (Exception e) {
            log.error("error while JobOrderService getByProjectCode {}: " ,e.getMessage());
            return null;
        }
    }

    @Transactional
    public List<JobOrder> getByProjectId(Long id) {
        try {
            DecimalFormat df = new DecimalFormat("#.###");
            List<JobOrder> jobOrders = jobOrderRepository.getByProjectProfileId(id);
            for (JobOrder jobOrder : jobOrders) {
                if (!jobOrder.isApproved()) {
                    if (jobOrder.isManufacturingManager()) {
                        jobOrder.setPendingFor("Manufacturing Manager");
                    } else if (jobOrder.isStoreManager()) {
                        jobOrder.setPendingFor("Store Manager");
                    } else if (jobOrder.isPurchasingManager()) {
                        jobOrder.setPendingFor("Purchasing Manager");
                    } else if (jobOrder.isGeneralManager()) {
                        jobOrder.setPendingFor("General Manager");
                    }
                    jobOrderRepository.save(jobOrder);
                }

                double totalWorkOrder = 0.0;
                Double totalWorkOrderExit = exitJobOrderRepository.sumQuantity(jobOrders.getFirst().getProjectCode(), jobOrder.getJobOrderNumber());

                for (PandsToJobOrder bandToJobOrder : jobOrder.getPandsToJobOrders()) {
                    totalWorkOrder += (bandToJobOrder.getMainQuantity() * Double.parseDouble(bandToJobOrder.getRepetition()));
                }

                int result;
                if (jobOrder.isApproved()) {
                    if (totalWorkOrderExit == null) {
                        jobOrder.setTotalDelivered((double) (0));
                    } else {
                        result = (int) Double.parseDouble(df.format((totalWorkOrderExit / totalWorkOrder) * 100));
                        jobOrder.setTotalDelivered((double) result);
                        jobOrder.setStatus(WorkOrderStatus.DELIVERED.getValue());
                        jobOrder.setStatusName("DELIVERED " + result + " %");
                        if (result == 100) {
                            jobOrder.setStatus(WorkOrderStatus.CLOSED.getValue());
                            jobOrder.setStatusName("CLOSED");
                        }
                    }
                }
                jobOrderRepository.save(jobOrder);
            }
            return jobOrders;
        } catch (Exception e) {
            log.error("error while JobOrderService getByProjectId {}: " ,e.getMessage());
            return null;
        }
    }

    public ResponseEntity<JobOrder> addNewJobOrder(JobOrder jobOrder, HttpServletRequest request)  {

        GregorianCalendar calendar = new GregorianCalendar();
        Integer nextWorkOrder = jobOrderRepository.findMaxNumber(jobOrder.getProjectProfileId());
        if(nextWorkOrder == null){
            nextWorkOrder = 0;
        }
        String jobOrderNumber = nextWorkOrder + 1 + "/" + calendar.get(Calendar.YEAR);

        String username = changeHistoryLog.getUser(request);

        jobOrder.setCreatedBy(username);
        jobOrder.setYear(calendar.get(Calendar.YEAR));
        jobOrder.setJobOrderNumber(jobOrderNumber);
        jobOrder.setApproved(false);
        jobOrder.setNumber(nextWorkOrder + 1);
        jobOrder.setStatus(WorkOrderStatus.UNDER_FABRICATION.getValue());
        jobOrder.setStatusName("UNDER_FABRICATION");
        jobOrderRepository.save(jobOrder);
        changeHistoryLog.saveChange(jobOrder.getJobOrderNumber(), jobOrder.toString(), jobOrder.toString(), "save", request);
        return new ResponseEntity<>(jobOrder, HttpStatus.OK);
    }

    public ResponseEntity<JobOrder> updateJobOrder(Long id, JobOrder updatedJobOrder, HttpServletRequest request) {

        JobOrder jobOrder = jobOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work Order Not Found for ID: " + id));

        jobOrderRepository.save(jobOrder);

        changeHistoryLog.saveChange(updatedJobOrder.getJobOrderNumber(), updatedJobOrder.toString(), jobOrder.toString(), "update", request);

        return new ResponseEntity<>(jobOrder, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> deleteJobOrder(Long id, HttpServletRequest request) {
        try {

            JobOrder jobOrder = jobOrderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Work Order Not Found for ID: " + id));

            if (jobOrder.isApproved()) {
                return new ResponseEntity<>(new MessageResponse(" cannot remove on approved work order ", 1), HttpStatus.BAD_REQUEST);
            }

            List<ExitJobOrder> exitJobOrders = exitJobOrderRepository.findByJobOrderId(jobOrder.getJobOrderNumber());
            if (!exitJobOrders.isEmpty()) {
                return new ResponseEntity<>(new MessageResponse(" Not Allowed .... for permit job orders " + jobOrder.getJobOrderNumber(), 1), HttpStatus.OK);
            }

            returnInDelete(jobOrder, request);
            changeHistoryLog.saveChange(jobOrder.getJobOrderNumber(), jobOrder.toString(), jobOrder.toString(), "delete", request);

            jobOrderRepository.deleteById(id);

            return new ResponseEntity<>(new MessageResponse("Work Order Successfully Deleted",0),HttpStatus.OK);
        } catch (Exception e) {
            Logger.getLogger(JobOrderService.class.getName()).log(Level.SEVERE, null, e);
            return new ResponseEntity<>("SomeThing Wrong During Deleting Work Order",HttpStatus.EXPECTATION_FAILED);
        }
    }

    public void returnInDelete(JobOrder jobOrder, HttpServletRequest request) {
        try {
            DecimalFormat df = new DecimalFormat("#.###");
            List<PandsToJobOrder> bandsToJobOrders = pandsToJobOrderRepository.findByProjectProfileIdAndJobOrderId(jobOrder.getProjectProfileId(), jobOrder.getJobOrderNumber());
            for (PandsToJobOrder item : bandsToJobOrders) {
                // First delete child entries
                pandsToJobOrderRepository.deleteRelationByPandsToJobOrderId(item.getId());

                // Then update the related Pand
                Pand pand = pandsService.getPandByPandCode(item.getPandCode(), item.getProjectProfileId());
                pand.setRestQuantity(Double.parseDouble(df.format(pand.getRestQuantity() + Double.parseDouble(item.getTotal()))));
                pandsRepository.save(pand);

                changeHistoryLog.saveChange(jobOrder.getJobOrderNumber(), item.toString(), item.toString(), "delete", request);

                // Now safely delete the parent
                pandsToJobOrderRepository.deleteById(item.getId());
            }
        } catch (Exception e) {
            log.error("error while JobOrderService returnInDelete {}: " ,e.getMessage());
        }
    }

    public JobOrder getByJobOrder(String jobOrderNumber) {
        return jobOrderRepository.getByJobOrderNumber(jobOrderNumber);
    }

    public List<JobOrder> getPendingJobOrder() {
        return jobOrderRepository.getPendingJobOrder();
    }

    public List<JobOrder> getPendingManufacturingJobOrder() {
        return jobOrderRepository.getPendingManufacturingJobOrder();
    }

    public List<JobOrder> getPendingStoreJobOrder() {
        return jobOrderRepository.getPendingStoreJobOrder();
    }

    public List<JobOrder> getPendingPurchaseJobOrder() {
        return jobOrderRepository.getPendingPurchaseJobOrder();
    }

    public JobOrder revertWorkOrder(Long jobOrderId) {

        Optional<JobOrder> jobOrder = jobOrderRepository.findById(jobOrderId);
        if(jobOrder.isEmpty()){
            throw new ResourceNotFoundException();
        }
        jobOrder.get().setReverted(true);
        jobOrderRepository.save(jobOrder.get());

        return jobOrder.get();
    }

    public List<JobOrder> getRevertedJobOrder(String userName) {

        return jobOrderRepository.getRevertedWorkOrdersByUser(userName);
    }

    public JobOrder sendJobOrderToUser(SendToBody sendToBody, Long id) {
        Optional<JobOrder> jobOrder = jobOrderRepository.findById(id);
        if(jobOrder.isEmpty()){
            throw new ResourceNotFoundException();
        }
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
        if(jobOrder.isEmpty()){
            throw new ResourceNotFoundException();
        }
        jobOrder.get().setJobOrderName(jobOrder.get().getProjectCode());
        jobOrder.get().setApproved(true);
        jobOrder.get().setReverted(false);
        jobOrder.get().setManufacturingManager(true);
        jobOrder.get().setStoreManager(true);
        jobOrder.get().setPurchasingManager(false);
        jobOrder.get().setGeneralManager(false);
        jobOrder.get().setStatusName("DELIVERED " + 0 + " %");
        List<PandsToJobOrder> bandsToJobOrders = pandsToJobOrderRepository.findByProjectProfileIdAndJobOrderId(jobOrder.get().getProjectProfileId(), jobOrder.get().getJobOrderNumber());
        if (bandsToJobOrders != null) {
            for (PandsToJobOrder pandsToJobOrder : bandsToJobOrders) {
                pandsToJobOrder.setJobOrderName(jobOrder.get().getProjectCode());
                pandsToJobOrder.setJobOrderId(jobOrder.get().getProjectCode().concat("/").concat(jobOrder.get().getJobOrderNumber()));
                pandsToJobOrderRepository.save(pandsToJobOrder);
            }

            jobOrder.get().setJobOrderNumber(jobOrder.get().getProjectCode().concat("/").concat(jobOrder.get().getJobOrderNumber()));
            jobOrderRepository.save(jobOrder.get());

        }
        return jobOrder.get();
    }

    public JobOrder copyJobORder(String jobOrder) throws BadRequestException {

        GregorianCalendar calendar = new GregorianCalendar();
        JobOrder jobOrder1 = jobOrderRepository.getByJobOrderNumber(jobOrder);
        if(!jobOrder1.isApproved()){
            throw new BadRequestException();
        }
        Integer number = getTheMaxNumber(jobOrder1.getProjectProfileId());
        int nextNumber = number + 1;
        String jobOrderNumber =jobOrder1.getProjectCode().concat("/") + nextNumber + "/" + calendar.get(Calendar.YEAR);
        JobOrder newJobOrder = new JobOrder();
        newJobOrder.setJobOrderNumber(jobOrderNumber);
        newJobOrder.setNumber(number + 1);


        newJobOrder.setInstallementArea(jobOrder1.getInstallementArea());
        newJobOrder.setProjectCode(jobOrder1.getProjectCode());
        newJobOrder.setProjectName(jobOrder1.getProjectName());
        newJobOrder.setProjectProfileId(jobOrder1.getProjectProfileId());
        newJobOrder.setYear(calendar.get(Calendar.YEAR));
        newJobOrder.setCommit(true);
        newJobOrder.setApproved(true);
        DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");

        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("hh:mm:ss a");

        newJobOrder.setJobOrderDate(formatter1.format(dNow));
        newJobOrder.setJobOrderTime(ft.format(dNow));

        List<PandsToJobOrder> bandsToJobOrders = pandsToJobOrderRepository.findByJobOrderId(jobOrder);


        jobOrderRepository.save(newJobOrder);

        DecimalFormat df = new DecimalFormat("#.###");

        List<PandsToJobOrder> pandsToJobOrderList = new ArrayList<>();

        for (PandsToJobOrder toJobOrder : bandsToJobOrders) {
            PandsToJobOrder pandsToJobOrder = new PandsToJobOrder();
            Pand pand = pandsService.getPandByPandCode(toJobOrder.getPandCode(), toJobOrder.getProjectProfileId());
            pand.setRestQuantity(Double.parseDouble(df.format(pand.getRestQuantity() - Double.parseDouble(toJobOrder.getTotal()))));
            pandsRepository.save(pand);
            long leastSigBits = System.currentTimeMillis();
            long mostSigBits = Instant.now().getEpochSecond();
            UUID uuid = new UUID(mostSigBits, leastSigBits);
            pandsToJobOrder.setUniqueId(uuid.toString());
            pandsToJobOrder.setTotal(toJobOrder.getMainTotal());
            pandsToJobOrder.setMainTotal(toJobOrder.getMainTotal());
            pandsToJobOrder.setMainQuantity(toJobOrder.getMainQuantity());
            pandsToJobOrder.setQuantity(toJobOrder.getMainQuantity());
            pandsToJobOrder.setJobOrderId(jobOrderNumber);
            pandsToJobOrder.setRawType(toJobOrder.getRawType());
            pandsToJobOrder.setInstallationArea(toJobOrder.getInstallationArea());
            pandsToJobOrder.setFinishType(toJobOrder.getFinishType());
            pandsToJobOrder.setRawUsed(toJobOrder.getRawUsed());
            pandsToJobOrder.setManufacturing(toJobOrder.getManufacturing());
            pandsToJobOrder.setManufacturingCode(toJobOrder.getManufacturingCode());
            pandsToJobOrder.setThickness(toJobOrder.getThickness());
            pandsToJobOrder.setDescription(toJobOrder.getDescription());
            pandsToJobOrder.setAdditionalDescription(toJobOrder.getAdditionalDescription());
            pandsToJobOrder.setFloor(toJobOrder.getFloor());
            pandsToJobOrder.setHeight(toJobOrder.getHeight());
            pandsToJobOrder.setWidth(toJobOrder.getWidth());
            pandsToJobOrder.setUnit(toJobOrder.getUnit());
            pandsToJobOrder.setRepetition(toJobOrder.getRepetition());
            pandsToJobOrder.setPandCode(toJobOrder.getPandCode());
            pandsToJobOrder.setProjectName(toJobOrder.getProjectName());
            pandsToJobOrder.setProjectCode(toJobOrder.getProjectCode());
            pandsToJobOrder.setOfficerName(toJobOrder.getOfficerName());
            pandsToJobOrder.setJobOrderType(toJobOrder.getJobOrderType());
            pandsToJobOrder.setBlockNumber(toJobOrder.getBlockNumber());
            pandsToJobOrder.setProjectProfileId(toJobOrder.getProjectProfileId());
            pandsToJobOrder.setEngineerName(toJobOrder.getEngineerName());
            pandsToJobOrder.setQuantityInPand(toJobOrder.getQuantityInPand());
            pandsToJobOrderRepository.save(pandsToJobOrder);
            pandsToJobOrderList.add(pandsToJobOrder);
        }
        newJobOrder.setPandsToJobOrders(pandsToJobOrderList);
        return newJobOrder;
    }

    public InputStreamResource getJobOrderDetails(Long jobOrderId) {
        try {
            com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook();
            WorksheetCollection worksheets = workbook.getWorksheets();
            Worksheet sheet = worksheets.get(0);
            sheet.setDisplayRightToLeft(false);
            PageSetup pageSetup = sheet.getPageSetup();
            pageSetup.setFooter(1, "Page &P of &N");
            pageSetup.setOrientation(PageOrientationType.LANDSCAPE);
            pageSetup.setFitToPagesWide(1);
            pageSetup.setFitToPagesTall(0);

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
            Date dNow = new Date();
            SimpleDateFormat ft =
                    new SimpleDateFormat("hh:mm:ss a");

            DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");

            Optional<JobOrder> jobOrderNumber = jobOrderRepository.findById(jobOrderId);
            if(jobOrderNumber.isEmpty()){
                throw new ResourceNotFoundException();
            }
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

            sheet.getCells().get("F1").putValue(formatter1.format(dNow) + " " + ft.format(dNow));
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


            sheet.getCells().get("A5").putValue("Band Code");
            sheet.getCells().get("A5").setStyle(tableHeaderStyle);

            sheet.getCells().get("B5").putValue("Quantity");
            sheet.getCells().get("B5").setStyle(tableHeaderStyle);

            sheet.getCells().get("C5").putValue("Exit Quantity");
            sheet.getCells().get("C5").setStyle(tableHeaderStyle);

            sheet.getCells().get("D5").putValue("Remaining");
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

            List<String> bandsToJobOrders = pandsToJobOrderRepository.findDistinctPandCodesByProjectProfileIdAndJobOrderId(jobOrderNumber.get().getProjectProfileId(), jobOrder);
            Double totalQuantityInJobOrders = 0.0;
            Double totalQuantityInExitJobOrders = 0.0;
            if (!bandsToJobOrders.isEmpty()) {
                for (String entry : bandsToJobOrders) {

                    sheet.getCells().get("A" + rowIdx).putValue(entry);  // رقم امر شغل
                    if (rowIdx % 2 != 0) {
                        sheet.getCells().get("A" + rowIdx).setStyle(shadowStyle);
                    } else {
                        sheet.getCells().get("A" + rowIdx).setStyle(discriptionDataStyle);
                    }

                    Double totalSumInJobOrders = pandsToJobOrderRepository.sumMainTotalByPandCodeAndJobOrder(jobOrderNumber.get().getProjectProfileId(), entry, jobOrder);

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

                    Double totalExit = exitJobOrderRepository.sumTotalByJobOrderAndPand(jobOrderNumber.get().getProjectCode(), entry, jobOrder);

                    if (totalExit == null) {
                        totalExit = 0.0;
                    }
                    totalQuantityInExitJobOrders += totalExit;

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
            log.error("error while JobOrderService getJobOrderDetails {}: " ,e.getMessage());
            return null;
        }
    }


    public String approveStoreJobOrder(Long id) {
        Optional<JobOrder> jobOrder = jobOrderRepository.findById(id);
        if(jobOrder.isEmpty()){
            throw new ResourceNotFoundException();
        }
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

    public String deleteJobOrderImage(Long id) {
        Optional<JobOrder> jobOrder = jobOrderRepository.findById(id);
        if(jobOrder.isEmpty()){
            throw new ResourceNotFoundException();
        }
        try {
            jobOrder.get().setImage(null);
            jobOrderRepository.save(jobOrder.get());
            System.out.println("deleted");
            return "Image Deleted Successfully";
        } catch (Exception e) {
            System.out.println("not deleted");
            return "Failed To Delete The Image";
        }
    }
}
