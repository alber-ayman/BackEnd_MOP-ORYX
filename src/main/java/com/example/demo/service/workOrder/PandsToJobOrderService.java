package com.example.demo.service.workOrder;

import com.aspose.cells.*;
import com.example.demo.DTO.MarbleItemDto;
import com.example.demo.models.*;
import com.example.demo.payload.CheckLimitResponse;
import com.example.demo.repository.*;
import com.example.demo.security.jwt.JwtUtils;
import com.example.demo.service.ChangeHistoryLog;
import com.example.demo.service.pand.PandsService;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PandsToJobOrderService {

    @Autowired
    private ChangeHistoryLog changeHistoryLog;

    @Autowired
    PandsToJobOrderRepository pandsToJobOrderRepository;

    @Autowired
    PreviewJobOrderRepository previewJobOrderRepository;

    @Autowired
    ExitJobOrderRepository exitJobOrderRepository;

    @Autowired
    PandsService pandsService;

    @Autowired
    PandsRepository pandsRepository;

    @Autowired
    JobOrderRepository jobOrderRepository;

    @Autowired
    JobOrderService jobOrderService;

    @Autowired
    ProjectProfileRepository projectProfileRepository;

    @RequestScope
    public PandsToJobOrder saveChildPand(PandsToJobOrder pandsToJobOrder, int flag, HttpServletRequest request) throws SQLException {

        Pand pand = pandsService.getPandByPandCode(pandsToJobOrder.getPandCode(), pandsToJobOrder.getProjectProfileId());

        pandsToJobOrder.setFlag(0);
//        Integer number = jobOrderService.getTheMaxNumber();
        GregorianCalendar gcalendar = new GregorianCalendar();

        JobOrder isJobOrderExist = null;

        ProjectProfile projectProfile = projectProfileRepository.getById(pandsToJobOrder.getProjectProfileId());
        Integer number = jobOrderRepository.findMaxNumber(pandsToJobOrder.getProjectProfileId());
        if(number == null){
            number = 1;
        }
        String pendingApproval = "Pending Approval";
        String jobOrderNumber = "";
        Optional<JobOrder> jobOrder = null;
        if (flag == 0) {
            jobOrderNumber = number + "/" + gcalendar.get(Calendar.YEAR);
            isJobOrderExist = jobOrderRepository.getByProjectCodeAndJobOrderNumber(projectProfile.getId(), jobOrderNumber);

            pandsToJobOrder.setJobOrderId(jobOrderNumber);
            pandsToJobOrder.setJobOrderName(pendingApproval);
            pandsToJobOrder.setProjectName(projectProfile.getProjectName());
            pandsToJobOrder.setProjectCode(projectProfile.getProjectCode());
            if (isJobOrderExist == null) {

                isJobOrderExist = new JobOrder();
                Date dNow = new Date();
                SimpleDateFormat ft =
                        new SimpleDateFormat("hh:mm:ss a");

                DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");
                isJobOrderExist.setProjectProfileId(pandsToJobOrder.getProjectProfileId());
                isJobOrderExist.setJobOrderDate(formatter1.format(dNow));
                isJobOrderExist.setJobOrderTime(ft.format(dNow));
                isJobOrderExist.setInstallementArea(pandsToJobOrder.getInstallationArea());
                isJobOrderExist.setNumber(number);
                isJobOrderExist.setWorkOrderHeader(jobOrderNumber);
                isJobOrderExist.setJobOrderNumber(number + "/" + gcalendar.get(Calendar.YEAR));
                if (isJobOrderExist.getPandsToJobOrders() == null) {
                    isJobOrderExist.setPandsToJobOrders(new ArrayList<>());
                }
            } else {
                flag = 1;
            }
        } else {
            jobOrder = jobOrderRepository.findById(Long.valueOf(pandsToJobOrder.getJobOrderId()));
            List<PandsToJobOrder> pandsToJobOrderList = pandsToJobOrderRepository.getByJobOrderId(jobOrder.get().getJobOrderNumber());
            if (pandsToJobOrderList != null) {
//                pandsToJobOrder.setBlockNumber(pandsToJobOrderList.get(0).getBlockNumber());
//                pandsToJobOrder.setFloor(pandsToJobOrderList.get(0).getFloor());
                pandsToJobOrder.setJobOrderId(pandsToJobOrderList.getFirst().getJobOrderId());
                pandsToJobOrder.setJobOrderType(pandsToJobOrderList.getFirst().getJobOrderType());
                pandsToJobOrder.setEngineerName(pandsToJobOrderList.getFirst().getEngineerName());
                pandsToJobOrder.setOfficerName(pandsToJobOrderList.getFirst().getOfficerName());
                pandsToJobOrder.setInstallationArea(pandsToJobOrderList.getFirst().getInstallationArea());
            }
        }

        double total;
        DecimalFormat df = new DecimalFormat("#.###");

        if (pandsToJobOrder.getUnit().equals("Square Meter")) {
            total = (Double.valueOf(pandsToJobOrder.getHeight()) * Double.valueOf(pandsToJobOrder.getWidth()) * Double.valueOf(pandsToJobOrder.getQuantity() * Double.valueOf(pandsToJobOrder.getRepetition()))) / 10000;
        } else if (pandsToJobOrder.getUnit().equals("Longitudinal meter")) {
            total = (Double.valueOf(pandsToJobOrder.getHeight()) * Double.valueOf(pandsToJobOrder.getQuantity() * Double.valueOf(pandsToJobOrder.getRepetition()))) / 100;
        } else {
            total = Double.valueOf(pandsToJobOrder.getQuantity() * Double.valueOf(pandsToJobOrder.getRepetition()));
        }

        if (total > pand.getMockQuantity()) {
            pandsToJobOrder.setFlag(1);
            pandsToJobOrder.setMessage(" The Required Quantity exceeding the remaining Quantity in pand " + pand.getPandCode());
            return pandsToJobOrder;
        }

        pand.setMockQuantity(pand.getMockQuantity() - total);
        pandsRepository.save(pand);

        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("hh:mm:ss a");
        pandsToJobOrder.setJobOrderTime(ft.format(dNow));

        UUID uuid = UuidCreator.getTimeBased();

        pandsToJobOrder.setUniqueId(uuid.toString());
        pandsToJobOrder.setTotal(df.format(total));
        pandsToJobOrder.setMainTotal(df.format(total));
        pandsToJobOrder.setMainQuantity(pandsToJobOrder.getQuantity());
        pandsToJobOrder.setRawUsed(pand.getRawUsed());
        pandsToJobOrder.setRawType(pand.getRawType());
        pandsToJobOrder.setFinishType(pand.getFinishType());
        pandsToJobOrder.setQuantity(pandsToJobOrder.getQuantity() * Double.valueOf(pandsToJobOrder.getRepetition()));
        pandsToJobOrder.setQuantityInPand(Double.valueOf(df.format(pand.getMockQuantity())));
        pandsToJobOrder.setProjectName(projectProfile.getProjectName());
        pandsToJobOrder.setProjectCode(projectProfile.getProjectCode());
//        if (flag == 1 && jobOrder.get().isApproved()) {
//        pand.setRestQuantity(Double.valueOf(df.format(pand.getRestQuantity() - Double.valueOf(total))));
//        pandsRepository.save(pand);
//        pandsToJobOrder.setQuantityInPand(Double.valueOf(df.format(pand.getRestQuantity())));
//        }

        pandsToJobOrderRepository.save(pandsToJobOrder);
        changeHistoryLog.saveChange(pandsToJobOrder.getId().toString(), pandsToJobOrder.toString(), pandsToJobOrder.toString(), "save", request);


        if (flag == 0) {
            isJobOrderExist.getPandsToJobOrders().add(pandsToJobOrder);
            isJobOrderExist.setIncNumber(number);
            isJobOrderExist.setJobOrderName("Pending Approval");
            isJobOrderExist.setProjectName(projectProfile.getProjectName());
            isJobOrderExist.setProjectCode(projectProfile.getProjectCode());

//            isJobOrderExist.setJobOrderNumber(jobOrderNumber);

            String username = changeHistoryLog.getUser(request);

            isJobOrderExist.setCreatedBy(username);
            isJobOrderExist.setYear(gcalendar.get(Calendar.YEAR));

            jobOrderRepository.save(isJobOrderExist);
        }
        return pandsToJobOrder;
    }


    public ResponseEntity<PandsToJobOrder> updateJobOrder(Long id, PandsToJobOrder updatedJobOrder, int flag, HttpServletRequest request) {

        try {
            PandsToJobOrder jobOrder = pandsToJobOrderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("rawType Not Found for ID: " + id));

            List<ExitJobOrder> exitJobOrders = exitJobOrderRepository.getByJobOrderId(jobOrder.getJobOrderId());
            if (exitJobOrders.size() > 0) {
                jobOrder.setFlag(1);
                jobOrder.setMessage(" Not Allowed .... for permit job orders " + jobOrder.getJobOrderId());
                return new ResponseEntity<>(jobOrder, HttpStatus.BAD_REQUEST);
            }

            double total;
            DecimalFormat df = new DecimalFormat("#.###");

            Pand pand = pandsService.getPandByPandCode(updatedJobOrder.getPandCode(), updatedJobOrder.getProjectProfileId());
            if (updatedJobOrder.getUnit().equals("Square Meter")) {
                total = (Double.valueOf(updatedJobOrder.getHeight()) * Double.valueOf(updatedJobOrder.getWidth()) * Double.valueOf(updatedJobOrder.getMainQuantity() * Double.valueOf(updatedJobOrder.getRepetition()))) / 10000;
            } else if (updatedJobOrder.getUnit().equals("Longitudinal meter")) {
                total = (Double.valueOf(updatedJobOrder.getHeight()) * Double.valueOf(updatedJobOrder.getMainQuantity() * Double.valueOf(updatedJobOrder.getRepetition()))) / 100;
            } else {
                total = Double.valueOf(updatedJobOrder.getMainQuantity() * Double.valueOf(updatedJobOrder.getRepetition()));
            }

            if (jobOrder.getMainQuantity() != updatedJobOrder.getMainQuantity()) {
                double quantityInPand = pand.getRestQuantity() + updatedJobOrder.getQuantity();
                if (total > quantityInPand) {
                    jobOrder.setFlag(1);
                    jobOrder.setMessage(" The Required Quantity exceeding the remaining Quantity in pand " + pand.getPandCode());
                    return new ResponseEntity<>(jobOrder, HttpStatus.BAD_REQUEST);
                }
                pand.setRestQuantity(Double.valueOf(df.format(pand.getRestQuantity() + jobOrder.getQuantity())));
                pandsRepository.save(pand);
            }

            jobOrder.setProjectCode(updatedJobOrder.getProjectCode());
            jobOrder.setProjectName(updatedJobOrder.getProjectName());
            jobOrder.setEngineerName(updatedJobOrder.getEngineerName());
            jobOrder.setJobOrderType(updatedJobOrder.getJobOrderType());
            jobOrder.setManufacturingCode(updatedJobOrder.getManufacturingCode());
            jobOrder.setPandCode(updatedJobOrder.getPandCode());
            jobOrder.setDescription(updatedJobOrder.getDescription());
            jobOrder.setManufacturing(updatedJobOrder.getManufacturing());
            jobOrder.setRawType(updatedJobOrder.getRawType());
            jobOrder.setRawUsed(updatedJobOrder.getRawUsed());
            jobOrder.setFinishType(updatedJobOrder.getFinishType());
            jobOrder.setThickness(updatedJobOrder.getThickness());
            jobOrder.setBlockNumber(updatedJobOrder.getBlockNumber());
            jobOrder.setFloor(updatedJobOrder.getFloor());

            jobOrder.setUnit(updatedJobOrder.getUnit());

            jobOrder.setAdditionalDescription(updatedJobOrder.getAdditionalDescription());
            if (!jobOrder.getHeight().equals(updatedJobOrder.getHeight())
                    || !jobOrder.getWidth().equals(updatedJobOrder.getWidth())
                    || !jobOrder.getRepetition().equals(updatedJobOrder.getRepetition())
                    || jobOrder.getMainQuantity() != updatedJobOrder.getMainQuantity()
            ) {
                jobOrder.setTotal(df.format(total));
                jobOrder.setMainTotal(df.format(total));
                jobOrder.setQuantityInPand(Double.valueOf(df.format(pand.getRestQuantity() - total)));
                jobOrder.setQuantity(updatedJobOrder.getMainQuantity() * Double.valueOf(updatedJobOrder.getRepetition()));
                jobOrder.setMainQuantity(updatedJobOrder.getMainQuantity());
                if (total != Double.valueOf(jobOrder.getTotal())) {
                    if (total < Double.valueOf(jobOrder.getTotal())) {
                        double newVal = total - Double.valueOf(jobOrder.getTotal());
                        pand.setRestQuantity(Double.valueOf(df.format(pand.getRestQuantity() + newVal)));
                    } else {
                        pand.setRestQuantity(Double.valueOf(df.format(pand.getRestQuantity() - total)));
                    }
                }

            }
            if (!jobOrder.getInstallationArea().equals(updatedJobOrder.getInstallationArea())) {
                jobOrder.setInstallationArea(updatedJobOrder.getInstallationArea());
                JobOrder fatherJobOrder = jobOrderService.getByJobOrder(updatedJobOrder.getJobOrderId());
                fatherJobOrder.setInstallementArea(updatedJobOrder.getInstallationArea());
                jobOrderRepository.save(fatherJobOrder);
            }
            jobOrder.setHeight(updatedJobOrder.getHeight());
            jobOrder.setWidth(updatedJobOrder.getWidth());
            jobOrder.setRepetition(updatedJobOrder.getRepetition());
//        } else {
//            jobOrder.setQuantity(jobOrder.getQuantity());
//        }

            pandsRepository.save(pand);
            jobOrder.setMessage(" The Remaining Quantity is: " + pand.getRestQuantity());
            pandsToJobOrderRepository.save(jobOrder);

            changeHistoryLog.saveChange(id.toString(), updatedJobOrder.toString(), jobOrder.toString(), "update", request);

            return new ResponseEntity<>(jobOrder, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    public List<PandsToJobOrder> getByJobOrderId(String id) {

        return pandsToJobOrderRepository.getByJobOrderId(id);
    }

    public List<PandsToJobOrder> getAllByJobOrderId(Long id) {
        Optional<JobOrder> jobOrder = jobOrderRepository.findById(id);

        return pandsToJobOrderRepository.jobOrdersByJobOrderId(jobOrder.get().getProjectProfileId(),jobOrder.get().getJobOrderNumber());
    }

    public List<PandsToJobOrder> getByJobOrderIdWzNoZeros(UnifiedSerial unifiedSerial) {
        List<PandsToJobOrder> pandsToJobOrders = pandsToJobOrderRepository.getByJobOrderId(unifiedSerial.getJobOrderNumber());
        List<PandsToJobOrder> pandsToJobOrdersNoZeros = new ArrayList<>();
        for (int i = 0; i < pandsToJobOrders.size(); i++) {
            if (pandsToJobOrders.get(i).getQuantity() > 0) {
                pandsToJobOrdersNoZeros.add(pandsToJobOrders.get(i));
            }
        }
        return pandsToJobOrdersNoZeros;
    }

    public PandsToJobOrder getByJobOrderId(Long id) {

        return pandsToJobOrderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("rawType Not Found for ID: " + id));
    }

    public PandsToJobOrder getByjobOrderAndPandId(String jobOrderid, String pandId) {

        return pandsToJobOrderRepository.findByJobOrderIdAndPandCode(jobOrderid, pandId);
    }

    public List<PandsToJobOrder> getByProjectId(Long id) {
        try {
            return pandsToJobOrderRepository.getByProjectProfileId(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    public ResponseEntity<PandsToJobOrder> deletePandToJobOrder(Long id, HttpServletRequest request) {
        PandsToJobOrder pandsToJobOrder = pandsToJobOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("pand Not Found for ID: " + id));

        List<ExitJobOrder> exitJobOrders = exitJobOrderRepository.getByJobOrderId(pandsToJobOrder.getJobOrderId());
        if (exitJobOrders.size() > 0) {
            pandsToJobOrder.setFlag(1);
            pandsToJobOrder.setMessage("Not Allowed for existing permit job order " + pandsToJobOrder.getJobOrderId());
            return new ResponseEntity<>(pandsToJobOrder, HttpStatus.BAD_REQUEST);
        }

        changeHistoryLog.saveChange(id.toString(), pandsToJobOrder.toString(), pandsToJobOrder.toString(), "delete", request);


        double total;
        DecimalFormat df = new DecimalFormat("#.###");
        Pand pand = pandsService.getPandByPandCode(pandsToJobOrder.getPandCode(), pandsToJobOrder.getProjectProfileId());
        pand.setRestQuantity(Double.valueOf(df.format(pand.getRestQuantity() + Double.valueOf(pandsToJobOrder.getTotal()))));

        pandsToJobOrderRepository.deleteFromMutuleTable(id);
        pandsRepository.save(pand);
        pandsToJobOrderRepository.deleteById(id);

        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

    }

    public CheckLimitResponse returnJobOrder(String id, PandsToJobOrder jobOrderParent) {
        CheckLimitResponse checkLimitResponse = new CheckLimitResponse();

        try {

            if (jobOrderParent.getReturnReason() == "") {
                checkLimitResponse.setFlag(1);
                checkLimitResponse.setMessage("Please enter the return reason");
                return checkLimitResponse;
            }

            List<ExitJobOrder> actualPandsToJobOrder = exitJobOrderRepository.getBySerial(id);
            System.out.println(actualPandsToJobOrder.size());
            DecimalFormat df = new DecimalFormat("#.###");
            for (int i = 0; i < actualPandsToJobOrder.size(); i++) {
                System.out.println("actualPandsToJobOrder: " + i );
                PandsToJobOrder pandsToJobOrder = pandsToJobOrderRepository.findByUniqueIdAndJobOrderIdAndWidthAndHeight(actualPandsToJobOrder.get(i).getUniqueId()
                        , actualPandsToJobOrder.get(i).getJobOrderId(),actualPandsToJobOrder.get(i).getWidth(),actualPandsToJobOrder.get(i).getHeight());
                pandsToJobOrder.setQuantity(pandsToJobOrder.getQuantity() + actualPandsToJobOrder.get(i).getQuantity());
                String result = df.format(Double.valueOf(pandsToJobOrder.getTotal()) + Double.valueOf(actualPandsToJobOrder.get(i).getTotal()));
                pandsToJobOrder.setTotal(result);
                actualPandsToJobOrder.get(i).setSerialNumber(actualPandsToJobOrder.get(i).getSerialNumber().concat("  (Returned) "));
                actualPandsToJobOrder.get(i).setReturnFlag(true);
                actualPandsToJobOrder.get(i).setReturnReason(jobOrderParent.getReturnReason());
                exitJobOrderRepository.save(actualPandsToJobOrder.get(i));
//                ReturnJobOrders returnJobOrders = new ReturnJobOrders();
//                returnJobOrders = mappingJobOrder(actualPandsToJobOrder.get(i), jobOrderParent.getReturnReason());
//                returnRepository.save(returnJobOrders);
            }

            checkLimitResponse.setFlag(0);
            checkLimitResponse.setMessage("Work Order Returned");

            return checkLimitResponse;
        } catch (Exception e) {
            e.printStackTrace();
            checkLimitResponse.setFlag(1);
            checkLimitResponse.setMessage("Something went wrong");
            return checkLimitResponse;
        }
    }

    public ReturnJobOrders mappingJobOrder(ExitJobOrder updatedJobOrder, String returnReason) {

        ReturnJobOrders jobOrder = new ReturnJobOrders();
        jobOrder.setJobOrderId(updatedJobOrder.getJobOrderId());
        jobOrder.setProjectProfileId((updatedJobOrder.getProjectProfileId()));
        jobOrder.setProjectCode(updatedJobOrder.getProjectCode());
        jobOrder.setProjectName(updatedJobOrder.getProjectName());
        jobOrder.setPandCode(updatedJobOrder.getPandCode());
        jobOrder.setThickness(updatedJobOrder.getThickness());
        jobOrder.setHeight(updatedJobOrder.getHeight());
        jobOrder.setWidth(updatedJobOrder.getWidth());
        jobOrder.setQuantity(updatedJobOrder.getQuantity());
        jobOrder.setUnit(updatedJobOrder.getUnit());
        jobOrder.setReturnReason(returnReason);
        jobOrder.setRawType(updatedJobOrder.getRawType());
        jobOrder.setSerialNumber(updatedJobOrder.getSerialNumber());
        return jobOrder;
    }

    // PDF File
    public InputStreamResource getPdf(String id) throws Exception {

        JobOrder jobOrder = jobOrderService.getByJobOrder(id);

        List<PandsToJobOrder> pandsToJobOrdersRaws = pandsToJobOrderRepository.getByJobOrderIdGroupByRawType(jobOrder.getProjectProfileId(), id);


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

        sheet.getCells().get("A1").putValue("Work Order");
        sheet.getCells().get("A1").setStyle(discriptionDataStyle);


        sheet.getCells().get("B1").putValue(pandsToJobOrdersRaws.get(0).getJobOrderId());
        sheet.getCells().get("B1").setStyle(discriptionDataStyle);


        sheet.getCells().get("A3").putValue("Type");
        sheet.getCells().get("A3").setStyle(discriptionDataStyle);

        sheet.getCells().get("B3").putValue(pandsToJobOrdersRaws.get(0).getJobOrderType());
        sheet.getCells().get("B3").setStyle(discriptionDataStyle);


        sheet.getCells().get("A5").putValue("Created");
        sheet.getCells().get("A5").setStyle(discriptionDataStyle);

//        sheet.getCells().merge(5, 2, 1, 2);  // (startRow, startColumn, totalRows, totalColumns)

        sheet.getCells().get("C5").putValue(jobOrder.getJobOrderDate());
        sheet.getCells().get("C5").setStyle(discriptionDataStyle);

        sheet.getCells().get("B5").putValue(jobOrder.getJobOrderTime());
        sheet.getCells().get("B5").setStyle(discriptionDataStyle);

        sheet.getCells().get("A7").putValue("Print");
        sheet.getCells().get("A7").setStyle(discriptionDataStyle);

        sheet.getCells().get("C7").putValue(formatter1.format(dNow));
        sheet.getCells().get("C7").setStyle(discriptionDataStyle);

        sheet.getCells().get("B7").putValue(ft.format(dNow).toString());
        sheet.getCells().get("B7").setStyle(discriptionDataStyle);

        sheet.getCells().get("D1").putValue("Installation Area");
        sheet.getCells().get("D1").setStyle(discriptionDataStyle);

//        sheet.getCells().merge(0, 4, 1, 5);  // (startRow, startColumn, totalRows, totalColumns)

        sheet.getCells().get("E1").putValue(pandsToJobOrdersRaws.get(0).getInstallationArea());
        sheet.getCells().get("E1").setStyle(discriptionDataStyle);

        sheet.getCells().get("D3").putValue("Floor");
        sheet.getCells().get("D3").setStyle(discriptionDataStyle);

        sheet.getCells().get("E3").putValue(pandsToJobOrdersRaws.get(0).getFloor());
        sheet.getCells().get("E3").setStyle(discriptionDataStyle);

        sheet.getCells().get("D5").putValue("Block");
        sheet.getCells().get("D5").setStyle(discriptionDataStyle);

        sheet.getCells().get("E5").putValue(pandsToJobOrdersRaws.get(0).getBlockNumber());
        sheet.getCells().get("E5").setStyle(discriptionDataStyle);

        sheet.getCells().get("F1").putValue("Project Name");
        sheet.getCells().get("F1").setStyle(discriptionDataStyle);

        sheet.getCells().get("G1").putValue(pandsToJobOrdersRaws.get(0).getProjectName());
        sheet.getCells().get("G1").setStyle(discriptionDataStyle);

        sheet.getCells().get("F3").putValue("Project Code");
        sheet.getCells().get("F3").setStyle(discriptionDataStyle);

//        sheet.getCells().merge(2, 6, 1, 2);  // (startRow, startColumn, totalRows, totalColumns)

        sheet.getCells().get("G3").putValue(pandsToJobOrdersRaws.get(0).getProjectCode());
        sheet.getCells().get("G3").setStyle(discriptionDataStyle);

        sheet.getCells().get("F5").putValue("Engineer Name");
        sheet.getCells().get("F5").setStyle(discriptionDataStyle);

//        sheet.getCells().merge(4, 6, 1, 2);  // (startRow, startColumn, totalRows, totalColumns)

        sheet.getCells().get("G5").putValue(pandsToJobOrdersRaws.get(0).getOfficerName());
        sheet.getCells().get("G5").setStyle(discriptionDataStyle);

//        sheet.getCells().get("F7").putValue("أسم المستخدم");
//        sheet.getCells().get("F7").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("G7").putValue(jwtUtils.userName);
//        sheet.getCells().get("G7").setStyle(discriptionDataStyle);


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

        sheet.getCells().get("A9").putValue("#");
        sheet.getCells().get("A9").setStyle(tableHeaderStyle);

        sheet.getCells().get("B9").putValue("BOQ Ref");
        sheet.getCells().get("B9").setStyle(tableHeaderStyle);

        sheet.getCells().merge(8, 2, 1, 1);

        sheet.getCells().get("C9").putValue("BOQ Desc");
        sheet.getCells().get("C9").setStyle(tableHeaderStyle);

        sheet.getCells().get("D9").putValue("Material Type");
        sheet.getCells().get("D9").setStyle(tableHeaderStyle);

        sheet.getCells().get("E9").putValue("Thickness");
        sheet.getCells().get("E9").setStyle(tableHeaderStyle);

        sheet.getCells().get("F9").putValue("Finish Type");
        sheet.getCells().get("F9").setStyle(tableHeaderStyle);


        sheet.getCells().get("G9").putValue("Unit");
        sheet.getCells().get("G9").setStyle(tableHeaderStyle);


        sheet.getCells().get("H9").putValue("Quantity");
        sheet.getCells().get("H9").setStyle(tableHeaderStyle);

        sheet.getCells().get("I9").putValue("Repetition");
        sheet.getCells().get("I9").setStyle(tableHeaderStyle);

        sheet.getCells().get("J9").putValue("Height");
        sheet.getCells().get("J9").setStyle(tableHeaderStyle);

        sheet.getCells().get("K9").putValue("Width");
        sheet.getCells().get("K9").setStyle(tableHeaderStyle);

        sheet.getCells().get("L9").putValue("Total");
        sheet.getCells().get("L9").setStyle(tableHeaderStyle);

        sheet.getCells().get("M9").putValue("Notes");
        sheet.getCells().get("M9").setStyle(tableHeaderStyle);


        int rowIdx = 11;

        int totalQuantity = 0;
        double finalTotal = 0.0;
        DecimalFormat df = new DecimalFormat("#.###");

        List<PandsToJobOrder> pandsToJobOrders = new ArrayList<>();

        for (int i = 0; i < pandsToJobOrdersRaws.size(); i++) {
            pandsToJobOrders.addAll(pandsToJobOrderRepository.getByJobOrderIdAndRawType(pandsToJobOrdersRaws.get(i).getProjectProfileId()
                    , pandsToJobOrdersRaws.get(i).getJobOrderId(),
                    pandsToJobOrdersRaws.get(i).getRawType()));

//            pandsToJobOrders.add(null);

        }

        int flag = 0;

        for (int i = 0; i < pandsToJobOrders.size(); i++) {

            sheet.getCells().get("A" + rowIdx).putValue(i + 1);
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("A" + rowIdx).setStyle(shadowStyle);

            } else {
                sheet.getCells().get("A" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("B" + rowIdx).putValue(pandsToJobOrders.get(i).getPandCode());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("B" + rowIdx).setStyle(discriptionDataStyle);
            }


            int count = 0;
//            if (pandsToJobOrders.get(i).getDescription().length() > 30) {
//                int rowIndex = rowIdx;
//                String input = pandsToJobOrders.get(i).getDescription();
//                int partLength = 30;
//                for (int d = 0; d < input.length(); d += partLength) {
//                    int end = Math.min(d + partLength, input.length());
//                    String part = input.substring(d, end);
//                    count++;
//
//                    Row row = cells.getRows().get(rowIndex);
//                    row.setHeight(100);
//
//                    sheet.getCells().get("C" + rowIndex).putValue(part + "\n");
////                    if (rowIndex % 2 != 0) {
//                    sheet.getCells().get("C" + rowIndex).setStyle(shadowStyle);
////                    } else {
////                        sheet.getCells().get("C" + rowIndex).setStyle(discriptionDataStyle);
////                    }
//                    rowIndex++;
//                }
//            } else {
                sheet.getCells().get("C" + rowIdx).putValue(pandsToJobOrders.get(i).getDescription());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("C" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);
                }
//            }
//            if (count > 0) {
//                sheet.getCells().merge(rowIdx, 2, count - 1, 2);
//            }


            sheet.getCells().get("D" + rowIdx).putValue(pandsToJobOrders.get(i).getRawType());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("D" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);
            }

//            String value = pandsToJobOrders.get(i).getAdditionalDescription();
//
//            if (value.length() > 30) {
//
//                int splitIndex = value.indexOf(" ", 30);
//
//                if (splitIndex == -1) {
//                    // Fallback to character 50 if no space found
//                    splitIndex = 30;
//                }
//
//                String firstPart = value.substring(0, splitIndex).trim();
//                String secondPart = value.substring(splitIndex).trim();
//
//                // Set the split values
//                sheet.getCells().get("E" + rowIdx).setValue(firstPart);
//                sheet.getCells().get("E" + rowIdx + 1).setValue(secondPart);
//                flag = 1;
//
//                // Double the height of the original row
//                double originalHeight = sheet.getCells().getRowHeight(rowIdx);
//                sheet.getCells().setRowHeight(rowIdx, originalHeight * 2);
//            } else {
//                sheet.getCells().get("E" + rowIdx).putValue(pandsToJobOrders.get(i).getAdditionalDescription());
//            }
//
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("E" + rowIdx).setStyle(shadowStyle);
//                if(flag == 1){
//                    sheet.getCells().get("E" + rowIdx + 1 ).setStyle(shadowStyle);
//                }
//            } else {
//                sheet.getCells().get("E" + rowIdx).setStyle(discriptionDataStyle);
//                if(flag == 1){
//                    sheet.getCells().get("E" + rowIdx + 1 ).setStyle(discriptionDataStyle);
//                }
//            }

            sheet.getCells().get("E" + rowIdx).putValue(pandsToJobOrders.get(i).getThickness());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("E" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("E" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("F" + rowIdx).putValue(pandsToJobOrders.get(i).getFinishType());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("F" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("F" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("G" + rowIdx).putValue(pandsToJobOrders.get(i).getUnit());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("G" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);
            }

//            totalQuantity += pandsToJobOrders.get(i).getMainQuantity();
            sheet.getCells().get("H" + rowIdx).putValue(pandsToJobOrders.get(i).getMainQuantity());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("H" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("I" + rowIdx).putValue(pandsToJobOrders.get(i).getRepetition());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("I" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("I" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("J" + rowIdx).putValue(pandsToJobOrders.get(i).getHeight());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("J" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("J" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("K" + rowIdx).putValue(pandsToJobOrders.get(i).getWidth());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("K" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("K" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("L" + rowIdx).putValue(pandsToJobOrders.get(i).getMainTotal());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("L" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("L" + rowIdx).setStyle(discriptionDataStyle);
            }

//            finalTotal += Double.valueOf(pandsToJobOrders.get(i).getMainTotal());
            sheet.getCells().get("M" + rowIdx).putValue(pandsToJobOrders.get(i).getAdditionalDescription());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("M" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("M" + rowIdx).setStyle(discriptionDataStyle);
            }

            if (count > 0) {
                rowIdx += count + 1;
            } else {
                rowIdx += 2;
            }


        }


        ////////////////adding the cube summetion//////////////////////////

        ImageOrPrintOptions printOptions = new ImageOrPrintOptions();
        printOptions.setPrintingPage(PrintingPageType.DEFAULT);
        WorkbookRender render = new WorkbookRender(workbook, printOptions);
        int totalPages = render.getPageCount();

        // Calculate where the last page starts
        SheetRender sheetRender = new SheetRender(sheet, printOptions);
        int lastUsedRow = sheet.getCells().getMaxDataRow();


        rowIdx = lastUsedRow + 3;

        sheet.getCells().get("G" + rowIdx).putValue("Material");
        sheet.getCells().get("G" + rowIdx).setStyle(tableHeaderStyle);

        sheet.getCells().get("H" + rowIdx).setStyle(tableHeaderStyle);
        sheet.getCells().get("H" + rowIdx).putValue("Thickness");

        sheet.getCells().get("I" + rowIdx).putValue("Unit");
        sheet.getCells().get("I" + rowIdx).setStyle(tableHeaderStyle);

        sheet.getCells().get("J" + rowIdx).putValue("Total by unit");
        sheet.getCells().get("J" + rowIdx).setStyle(tableHeaderStyle);

        sheet.getCells().get("K" + rowIdx).putValue("Square Meter");
        sheet.getCells().get("K" + rowIdx).setStyle(tableHeaderStyle);


        List<PandsToJobOrder> pandsToJobOrdersByRawType = new ArrayList<>();

        for (int i = 0; i < pandsToJobOrdersRaws.size(); i++) {
            pandsToJobOrdersByRawType.addAll(pandsToJobOrderRepository.getByThicknessAndRawType(pandsToJobOrdersRaws.get(i).getProjectProfileId()
                    , pandsToJobOrdersRaws.get(i).getJobOrderId(),
                    pandsToJobOrdersRaws.get(i).getRawType()));
        }

        rowIdx += 1;
        for (int i = 0; i < pandsToJobOrdersByRawType.size(); i++) {

            sheet.getCells().get("G" + rowIdx).putValue(pandsToJobOrdersByRawType.get(i).getRawType());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("G" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("H" + rowIdx).putValue(pandsToJobOrdersByRawType.get(i).getThickness());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("H" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("I" + rowIdx).putValue(pandsToJobOrdersByRawType.get(i).getUnit());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("I" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("I" + rowIdx).setStyle(discriptionDataStyle);
            }

            List<PandsToJobOrder> getByThicknessAndRawTypeAndUnit = pandsToJobOrderRepository.getByThicknessAndRawTypeAndUnit(pandsToJobOrdersRaws.get(0).getProjectProfileId()
                    , pandsToJobOrdersRaws.get(0).getJobOrderId(),
                    pandsToJobOrdersByRawType.get(i).getRawType(),
                    pandsToJobOrdersByRawType.get(i).getThickness(),
                    pandsToJobOrdersByRawType.get(i).getUnit());

            Double totalQuantityInCube = 0.0;
            Double totalSum = 0.0;

            if (getByThicknessAndRawTypeAndUnit.size() > 0) {
                for (int k = 0; k < getByThicknessAndRawTypeAndUnit.size(); k++) {
                    totalSum += Double.valueOf(getByThicknessAndRawTypeAndUnit.get(k).getMainTotal());
                    totalQuantityInCube += (getByThicknessAndRawTypeAndUnit.get(k).getMainQuantity() *
                            Double.valueOf(getByThicknessAndRawTypeAndUnit.get(k).getRepetition()) *
                            Double.valueOf(getByThicknessAndRawTypeAndUnit.get(k).getHeight()) *
                            Double.valueOf(getByThicknessAndRawTypeAndUnit.get(k).getWidth())) / 10000;
                }
            }


            sheet.getCells().get("J" + rowIdx).putValue(df.format(totalSum));
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("J" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("J" + rowIdx).setStyle(discriptionDataStyle);
            }

//            finalTotal += Double.valueOf(pandsToJobOrders.get(i).getMainTotal());

            sheet.getCells().get("K" + rowIdx).putValue(df.format(totalQuantityInCube));
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("K" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("K" + rowIdx).setStyle(discriptionDataStyle);
            }

            rowIdx++;
        }

        sheet.getCells().setRowHeight(8, 18);

//        for (int i = 10; i < rowIdx; i++) {
//            sheet.getCells().setColumnWidth(0, 5);
//        }


        //////////////////////////////////////////////////////////////////////
        // Adjust column widths to fit content

        sheet.autoFitColumns();

//        sheet.getHorizontalPageBreaks().clear();
//
//        int lastRow = cells.getMaxDataRow();
//        for (int row = 22; row <= lastRow; row += 22) {
//            sheet.getHorizontalPageBreaks().add(row);
//        }

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

    public InputStreamResource previewJobOrder(String id, int flag) throws Exception {

        List<PreviewJobOrder> pandsToJobOrdersPreview = new ArrayList<>();
        if (flag == 0) {
            pandsToJobOrdersPreview = previewJobOrderRepository.getByProjectCode(id);
        } else {
            pandsToJobOrdersPreview = previewJobOrderRepository.getByJobOrderId(id);
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


        sheet.getCells().get("A3").putValue("Type");
        sheet.getCells().get("A3").setStyle(discriptionDataStyle);

        sheet.getCells().get("B3").putValue(pandsToJobOrdersPreview.get(0).getJobOrderType());
        sheet.getCells().get("B3").setStyle(discriptionDataStyle);


        sheet.getCells().get("D1").putValue("Installation Area");
        sheet.getCells().get("D1").setStyle(discriptionDataStyle);

//        sheet.getCells().merge(0, 4, 1, 5);  // (startRow, startColumn, totalRows, totalColumns)

        sheet.getCells().get("E1").putValue(pandsToJobOrdersPreview.get(0).getInstallationArea());
        sheet.getCells().get("E1").setStyle(discriptionDataStyle);

        sheet.getCells().get("D3").putValue("Location");
        sheet.getCells().get("D3").setStyle(discriptionDataStyle);

        sheet.getCells().get("E3").putValue(pandsToJobOrdersPreview.get(0).getFloor());
        sheet.getCells().get("E3").setStyle(discriptionDataStyle);

        sheet.getCells().get("D5").putValue("Block");
        sheet.getCells().get("D5").setStyle(discriptionDataStyle);

        sheet.getCells().get("E5").putValue(pandsToJobOrdersPreview.get(0).getBlockNumber());
        sheet.getCells().get("E5").setStyle(discriptionDataStyle);

        sheet.getCells().get("F1").putValue("Project Name");
        sheet.getCells().get("F1").setStyle(discriptionDataStyle);

        sheet.getCells().get("G1").putValue(pandsToJobOrdersPreview.get(0).getProjectName());
        sheet.getCells().get("G1").setStyle(discriptionDataStyle);

        sheet.getCells().get("F3").putValue("Project Code");
        sheet.getCells().get("F3").setStyle(discriptionDataStyle);

//        sheet.getCells().merge(2, 6, 1, 2);  // (startRow, startColumn, totalRows, totalColumns)

        sheet.getCells().get("G3").putValue(pandsToJobOrdersPreview.get(0).getProjectCode());
        sheet.getCells().get("G3").setStyle(discriptionDataStyle);

        sheet.getCells().get("F5").putValue("Engineer Name");
        sheet.getCells().get("F5").setStyle(discriptionDataStyle);

//        sheet.getCells().merge(4, 6, 1, 2);  // (startRow, startColumn, totalRows, totalColumns)

        sheet.getCells().get("G5").putValue(pandsToJobOrdersPreview.get(0).getOfficerName());
        sheet.getCells().get("G5").setStyle(discriptionDataStyle);

//        sheet.getCells().get("F7").putValue("أسم المستخدم");
//        sheet.getCells().get("F7").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("G7").putValue(jwtUtils.userName);
//        sheet.getCells().get("G7").setStyle(discriptionDataStyle);


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

        sheet.getCells().get("A9").putValue("#");
        sheet.getCells().get("A9").setStyle(tableHeaderStyle);

        sheet.getCells().get("B9").putValue("BOQ Item");
        sheet.getCells().get("B9").setStyle(tableHeaderStyle);

        sheet.getCells().merge(8, 2, 1, 1);

        sheet.getCells().get("C9").putValue("Description");
        sheet.getCells().get("C9").setStyle(tableHeaderStyle);


//        sheet.getCells().get("D9").putValue("Manufacturing Code");
//        sheet.getCells().get("D9").setStyle(tableHeaderStyle);

//        sheet.getCells().get("E9").putValue("الملاحظات الفنية");
//        sheet.getCells().get("E9").setStyle(tableHeaderStyle);


        sheet.getCells().get("D9").putValue("Total");
        sheet.getCells().get("D9").setStyle(tableHeaderStyle);

        sheet.getCells().get("E9").putValue("Unit");
        sheet.getCells().get("E9").setStyle(tableHeaderStyle);

        sheet.getCells().get("F9").putValue("Material Name");
        sheet.getCells().get("F9").setStyle(tableHeaderStyle);

        sheet.getCells().get("G9").putValue("Finish Type");
        sheet.getCells().get("G9").setStyle(tableHeaderStyle);

        sheet.getCells().get("H9").putValue("Number");
        sheet.getCells().get("H9").setStyle(tableHeaderStyle);

        sheet.getCells().get("I9").putValue("Height");
        sheet.getCells().get("I9").setStyle(tableHeaderStyle);

        sheet.getCells().get("J9").putValue("Width");
        sheet.getCells().get("J9").setStyle(tableHeaderStyle);

        sheet.getCells().get("K9").putValue("Thickness");
        sheet.getCells().get("K9").setStyle(tableHeaderStyle);

        sheet.getCells().get("L9").putValue("Repetition");
        sheet.getCells().get("L9").setStyle(tableHeaderStyle);


        int rowIdx = 11;

        int totalQuantity = 0;
        double finalTotal = 0.0;
        DecimalFormat df = new DecimalFormat("#.###");

        for (int i = 0; i < pandsToJobOrdersPreview.size(); i++) {

            sheet.getCells().get("A" + rowIdx).putValue(i + 1);
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("A" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("A" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("B" + rowIdx).putValue(pandsToJobOrdersPreview.get(i).getPandCode());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("B" + rowIdx).setStyle(discriptionDataStyle);
            }


            int count = 0;
            if (pandsToJobOrdersPreview.get(i).getDescription().length() > 30) {
                int rowIndex = rowIdx;
                String input = pandsToJobOrdersPreview.get(i).getDescription();
                int partLength = 30;
                for (int d = 0; d < input.length(); d += partLength) {
                    int end = Math.min(d + partLength, input.length());
                    String part = input.substring(d, end);
                    count++;

                    Row row = cells.getRows().get(rowIndex);
                    row.setHeight(100);

                    sheet.getCells().get("C" + rowIndex).putValue(part + "\n");
//                    if (rowIndex % 2 != 0) {
                    sheet.getCells().get("C" + rowIndex).setStyle(shadowStyle);
//                    } else {
//                        sheet.getCells().get("C" + rowIndex).setStyle(discriptionDataStyle);
//                    }
                    rowIndex++;
                }
            } else {
                sheet.getCells().get("C" + rowIdx).putValue(pandsToJobOrdersPreview.get(i).getDescription());
                if (rowIdx % 2 != 0) {
                    sheet.getCells().get("C" + rowIdx).setStyle(shadowStyle);
                } else {
                    sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);
                }
            }
//            if (count > 0) {
//                sheet.getCells().merge(rowIdx, 2, count - 1, 2);
//            }


//            sheet.getCells().get("D" + rowIdx).putValue(pandsToJobOrdersPreview.get(i).getManufacturingCode());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("D" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);
//            }

            sheet.getCells().get("D" + rowIdx).putValue(pandsToJobOrdersPreview.get(i).getMainTotal());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("D" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("E" + rowIdx).putValue(pandsToJobOrdersPreview.get(i).getUnit());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("E" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("E" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("F" + rowIdx).putValue(pandsToJobOrdersPreview.get(i).getRawType());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("F" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("F" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("G" + rowIdx).putValue(pandsToJobOrdersPreview.get(i).getFinishType());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("G" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);
            }

//            totalQuantity += pandsToJobOrders.get(i).getMainQuantity();
            sheet.getCells().get("H" + rowIdx).putValue(pandsToJobOrdersPreview.get(i).getMainQuantity());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("H" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("I" + rowIdx).putValue(pandsToJobOrdersPreview.get(i).getHeight());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("I" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("I" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("J" + rowIdx).putValue(pandsToJobOrdersPreview.get(i).getWidth());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("J" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("J" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("K" + rowIdx).putValue(pandsToJobOrdersPreview.get(i).getThickness());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("K" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("K" + rowIdx).setStyle(discriptionDataStyle);
            }

            sheet.getCells().get("L" + rowIdx).putValue(pandsToJobOrdersPreview.get(i).getRepetition());
            if (rowIdx % 2 != 0) {
                sheet.getCells().get("L" + rowIdx).setStyle(shadowStyle);
            } else {
                sheet.getCells().get("L" + rowIdx).setStyle(discriptionDataStyle);
            }

//            finalTotal += Double.valueOf(pandsToJobOrders.get(i).getMainTotal());


            if (count > 0) {
                rowIdx += count + 1;
            } else {
                rowIdx += 2;
            }


        }

        sheet.getCells().setRowHeight(8, 18);

//        for (int i = 10; i < rowIdx; i++) {
//            sheet.getCells().setColumnWidth(0, 5);
//        }


        //////////////////////////////////////////////////////////////////////
        // Adjust column widths to fit content

        sheet.autoFitColumns();

//        sheet.getHorizontalPageBreaks().clear();
//
//        int lastRow = cells.getMaxDataRow();
//        for (int row = 22; row <= lastRow; row += 22) {
//            sheet.getHorizontalPageBreaks().add(row);
//        }

        for (int i = 10; i < rowIdx; i++) {
            sheet.getCells().setRowHeight(i, 35);
        }

        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        workbook.save(pdfOutputStream, SaveFormat.PDF); // Save as PDF

        // 4. Return the PDF as a response
        ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
        InputStreamResource resource = new InputStreamResource(pdfInputStream);
        previewJobOrderRepository.deleteByProjectId(pandsToJobOrdersPreview.get(0).getProjectProfileId());
        return resource;
    }


    @RequestScope
    @Transactional
    public List<PandsToJobOrder> saveListJobOrderPands(List<MarbleItemDto> pandsToJobOrder, HttpServletRequest request) throws SQLException {
        try {


            List<PandsToJobOrder> pandsToJobOrders = mapTopandsToJobOrder(pandsToJobOrder);

            if(pandsToJobOrders.getFirst().getFlag() == 1){
                return pandsToJobOrders;
            }

            pandsToJobOrders.getFirst().setFlag(0);
            Integer number = jobOrderService.getTheMaxNumber(pandsToJobOrders.getFirst().getProjectProfileId());
            GregorianCalendar gcalendar = new GregorianCalendar();

            JobOrder isJobOrderExist = new JobOrder();

            String username = changeHistoryLog.getUser(request);

            Date dNow = new Date();
            SimpleDateFormat ft =
                    new SimpleDateFormat("hh:mm:ss a");

            DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");
            isJobOrderExist.setProjectProfileId(pandsToJobOrders.getFirst().getProjectProfileId());
            isJobOrderExist.setJobOrderDate(formatter1.format(dNow));
            isJobOrderExist.setJobOrderTime(ft.format(dNow));
            int nextNumber = number + 1;
            isJobOrderExist.setJobOrderNumber(nextNumber + "/" + gcalendar.get(Calendar.YEAR));
            isJobOrderExist.setNumber(number + 1);
            isJobOrderExist.setProjectName(pandsToJobOrders.getFirst().getProjectName());
            isJobOrderExist.setProjectCode(pandsToJobOrder.getFirst().getProjectCode());
            isJobOrderExist.setInstallementArea(pandsToJobOrder.getFirst().getInstallationArea());
            isJobOrderExist.setCreatedBy(username);
            isJobOrderExist.setYear(gcalendar.get(Calendar.YEAR));
            isJobOrderExist.setApproved(false);
            isJobOrderExist.setPandsToJobOrders(new ArrayList<>());

            changeHistoryLog.saveChange(pandsToJobOrder.getFirst().getProjectCode().concat("/") + nextNumber + "/" + gcalendar.get(Calendar.YEAR)
                    , pandsToJobOrder.toString(), pandsToJobOrder.toString(), "save", request);

            isJobOrderExist.getPandsToJobOrders().addAll(pandsToJobOrders);

            pandsToJobOrderRepository.saveAll(pandsToJobOrders);
            jobOrderRepository.save(isJobOrderExist);

            return pandsToJobOrders;
        }catch (Exception e){
            e.printStackTrace();
            List<PandsToJobOrder> pandsToJobOrders = new ArrayList<>();
            PandsToJobOrder pandsToJobOrder1 = new PandsToJobOrder();
            pandsToJobOrder1.setFlag(1);
            pandsToJobOrder1.setMessage("Something Went Wrong");
            pandsToJobOrders.add(pandsToJobOrder1);
            return pandsToJobOrders;
        }
    }

    private List<PandsToJobOrder> mapTopandsToJobOrder(List<MarbleItemDto> marbleItemDtos) {

        double totalQuantity = 0;

        List<String> distinctPands = marbleItemDtos.stream()
                .map(MarbleItemDto::getPandCode)   // extract the unit field
                .filter(Objects::nonNull)      // optional: skip null units
                .distinct()                    // keep only unique values
                .toList();

        for (String distinctPand : distinctPands) {
            double restQuantity = pandsRepository.findRestQuantityByPandCodeAndProjectProfileId(distinctPand, marbleItemDtos.getFirst().getProjectProfileId());
            for (MarbleItemDto marbleItemDto : marbleItemDtos) {
                if (marbleItemDto.getPandCode().equals(distinctPand)) {
                    totalQuantity += marbleItemDto.getTotal();
                }
            }

            if (totalQuantity > restQuantity) {
                List<PandsToJobOrder> pandsToJobOrders = new ArrayList<>();
                PandsToJobOrder pandsToJobOrder1 = new PandsToJobOrder();
                pandsToJobOrder1.setFlag(1);
                pandsToJobOrder1.setMessage(" The Required Quantity exceeding the remaining Quantity in pand " + distinctPand);
                pandsToJobOrders.add(pandsToJobOrder1);
                return pandsToJobOrders;
            }
        }
        DecimalFormat df = new DecimalFormat("#.###");
        for (String distinctPand : distinctPands) {
            totalQuantity = 0;
            double restQuantity = pandsRepository.findRestQuantityByPandCodeAndProjectProfileId(distinctPand, marbleItemDtos.getFirst().getProjectProfileId());
            for (MarbleItemDto marbleItemDto : marbleItemDtos) {
                if (marbleItemDto.getPandCode().equals(distinctPand)) {
                    totalQuantity += Double.parseDouble(String.valueOf(marbleItemDto.getTotal()));
                }
            }
            Pand pand = pandsService.getPandByPandCode(distinctPand, marbleItemDtos.getFirst().getProjectProfileId());

            pand.setRestQuantity(Double.parseDouble(df.format(restQuantity - totalQuantity)));
            pandsRepository.save(pand);
        }

        List<PandsToJobOrder> pandsToJobOrderList = new ArrayList<>();

        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("hh:mm:ss a");
//        Integer number = jobOrderService.getTheMaxNumber(marbleItemDtos.getFirst().getProjectProfileId());
        GregorianCalendar gcalendar = new GregorianCalendar();
        Integer number = jobOrderRepository.findMaxNumber(marbleItemDtos.getFirst().getProjectProfileId());

        int nextNumber = number + 1;


        for (int i = 0; i < marbleItemDtos.size(); i++) {
            Pand pand = pandsService.getPandByPandCode(marbleItemDtos.get(i).getPandCode(), marbleItemDtos.get(i).getProjectProfileId());

            PandsToJobOrder pandsToJobOrder = new PandsToJobOrder();

            pandsToJobOrder.setJobOrderTime(ft.format(dNow));

            UUID uuid = UuidCreator.getTimeBased();

            pandsToJobOrder.setRepetition(String.valueOf(marbleItemDtos.get(i).getRepetition()));
            pandsToJobOrder.setMainQuantity(marbleItemDtos.get(i).getQuantity());
            pandsToJobOrder.setUnit(marbleItemDtos.get(i).getUnit());
            pandsToJobOrder.setHeight(marbleItemDtos.get(i).getHeight());
            pandsToJobOrder.setWidth(marbleItemDtos.get(i).getWidth());

            double repetation = 0;
            if (pandsToJobOrder.getRepetition().equals("0") || pandsToJobOrder.getRepetition().isEmpty()) {
                repetation = 1;
                pandsToJobOrder.setRepetition(String.valueOf(Integer.parseInt("1")));
            } else {
                repetation = Double.parseDouble(pandsToJobOrder.getRepetition());
            }
            pandsToJobOrder.setQuantity(pandsToJobOrder.getMainQuantity() * repetation);



//            restTotal = Double.parseDouble(df.format(pand.getRestQuantity() - total));
            pandsToJobOrder.setJobOrderId(nextNumber + "/" + gcalendar.get(Calendar.YEAR));
            pandsToJobOrder.setUniqueId(uuid.toString());
            pandsToJobOrder.setTotal(df.format(marbleItemDtos.get(i).getTotal()));

            pandsToJobOrder.setMainTotal(df.format(marbleItemDtos.get(i).getTotal()));
            pandsToJobOrder.setThickness(String.valueOf(marbleItemDtos.get(i).getThickness()));
            pandsToJobOrder.setPandCode(pand.getPandCode());
            pandsToJobOrder.setManufacturingCode(marbleItemDtos.get(i).getManufacturingCode());
            pandsToJobOrder.setManufacturing(pand.getManufacturing());
            pandsToJobOrder.setJobOrderType(marbleItemDtos.getFirst().getJobOrderTybe());
            pandsToJobOrder.setProjectCode(pand.getProjectCode());
            pandsToJobOrder.setProjectName(pand.getProjectName());
//            pandsToJobOrder.setQuantityInPand(restTotal);
            pandsToJobOrder.setProjectProfileId(pand.getProjectProfileId());
            pandsToJobOrder.setFinishType(pand.getFinishType());
            pandsToJobOrder.setRawType(pand.getRawType());
            pandsToJobOrder.setRawUsed(pand.getRawUsed());
//            pandsToJobOrder.setBlockNumber(pandsToJobOrderList.getFirst().getBlockNumber());
//            pandsToJobOrder.setFloor(pandsToJobOrderList.getFirst().getFloor());
            pandsToJobOrder.setJobOrderType(marbleItemDtos.getFirst().getJobOrderTybe());
            pandsToJobOrder.setEngineerName(marbleItemDtos.getFirst().getEngineerName());
            pandsToJobOrder.setInstallationArea(marbleItemDtos.getFirst().getInstallationArea());
            pandsToJobOrder.setDescription(marbleItemDtos.get(i).getDescription());
            pandsToJobOrderList.add(pandsToJobOrder);

//            pandsToJobOrderRepository.save(pandsToJobOrder);

        }
        return pandsToJobOrderList;
    }
}
