package com.example.demo.service.workOrder;

import com.aspose.cells.*;
import com.example.demo.DTO.MarbleItemDto;
import com.example.demo.DTO.MarbleItemRequestDto;
import com.example.demo.DTO.ThicknessUnitDTO;
import com.example.demo.models.*;
import com.example.demo.payload.CheckLimitResponse;
import com.example.demo.payload.excel.message.ResponseMessage;
import com.example.demo.repository.*;
import com.example.demo.service.ChangeHistoryLog;
import com.example.demo.service.pand.PandsService;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PandsToJobOrderService.class);
    private static final int FLAG_ERROR = 1;

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

    @Autowired
    JobOrderProcedureRepository procedureRepository;

    @RequestScope
    @Transactional
    public PandsToJobOrder saveChildPand(PandsToJobOrder pandsToJobOrder, int flag, HttpServletRequest request) {

        JobOrder isJobOrderExist = null;
        Optional<JobOrder> jobOrder;
        jobOrder = jobOrderRepository.findById(Long.valueOf(pandsToJobOrder.getJobOrderId()));

        if(jobOrder.isEmpty()) {
            pandsToJobOrder.setFlag(1);
            pandsToJobOrder.setMessage(" Work Order " + pandsToJobOrder.getJobOrderId() + "Not exists");
            return pandsToJobOrder;
        }

        if (jobOrder.get().isApproved()) {
            pandsToJobOrder.setFlag(1);
            pandsToJobOrder.setMessage(" cannot add/edit on approved work order ");
            return pandsToJobOrder;
        }

        Pand pand = pandsService.getPandByPandCode(pandsToJobOrder.getPandCode(), pandsToJobOrder.getProjectProfileId());

        pandsToJobOrder.setFlag(0);
//        Integer number = jobOrderService.getTheMaxNumber();
        GregorianCalendar gcalendar = new GregorianCalendar();


        ProjectProfile projectProfile = projectProfileRepository.getReferenceById(pandsToJobOrder.getProjectProfileId());
        Integer number = jobOrderRepository.findMaxNumber(pandsToJobOrder.getProjectProfileId());
        if (number == null) {
            number = 1;
        }
        String pendingApproval = "Pending Approval";
        String jobOrderNumber = "";

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

            List<PandsToJobOrder> pandsToJobOrderList = pandsToJobOrderRepository.findByJobOrderId(jobOrder.get().getJobOrderNumber());
            if (pandsToJobOrderList != null) {
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
            total = (Double.parseDouble(pandsToJobOrder.getHeight()) * Double.parseDouble(pandsToJobOrder.getWidth()) * (pandsToJobOrder.getQuantity() * Double.parseDouble(pandsToJobOrder.getRepetition()))) / 10000;
        } else if (pandsToJobOrder.getUnit().equals("Longitudinal meter")) {
            total = (Double.parseDouble(pandsToJobOrder.getHeight()) * (pandsToJobOrder.getQuantity() * Double.parseDouble(pandsToJobOrder.getRepetition()))) / 100;
        } else {
            total = pandsToJobOrder.getQuantity() * Double.parseDouble(pandsToJobOrder.getRepetition());
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
        pandsToJobOrder.setQuantity(pandsToJobOrder.getQuantity() * Double.parseDouble(pandsToJobOrder.getRepetition()));
        pandsToJobOrder.setQuantityInPand(Double.parseDouble(df.format(pand.getMockQuantity())));
        pandsToJobOrder.setProjectName(projectProfile.getProjectName());
        pandsToJobOrder.setProjectCode(projectProfile.getProjectCode());

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

    @Transactional
    public ResponseEntity<PandsToJobOrder> updateJobOrder(Long id, PandsToJobOrder updatedJobOrder, HttpServletRequest request) {

        PandsToJobOrder pandsToJobOrder = pandsToJobOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkOrder Not Found for ID: " + id));

        JobOrder fatherJobOrder = jobOrderService.getJobOrderByNumber(pandsToJobOrder.getJobOrderId());

        if (fatherJobOrder.isApproved()) {
            return errorResponse(updatedJobOrder, "cannot add/edit on approved work order", HttpStatus.CONFLICT);
        }

        List<ExitJobOrder> exitJobOrders = exitJobOrderRepository.findByJobOrderId(pandsToJobOrder.getJobOrderId());
        if (!exitJobOrders.isEmpty()) {
            return errorResponse(pandsToJobOrder,
                    "Not Allowed .... for permit job orders " + pandsToJobOrder.getJobOrderId(),
                    HttpStatus.BAD_REQUEST);
        }

        // --- Parse numeric string fields once, with a clear error if they're bad ---
        final double height;
        final double width;
        final double repetition;
        try {
            height = parseRequiredDouble(updatedJobOrder.getHeight(), "height");
            width = parseRequiredDouble(updatedJobOrder.getWidth(), "width");
            repetition = parseRequiredDouble(updatedJobOrder.getRepetition(), "repetition");
        } catch (NumberFormatException nfe) {
            logger.warn("Invalid numeric field on job order {}: {}", id, nfe.getMessage());
            return errorResponse(updatedJobOrder, "Invalid numeric value: " + nfe.getMessage(), HttpStatus.BAD_REQUEST);
        }

        double total = calculateTotal(updatedJobOrder.getUnit(), height, width, updatedJobOrder.getMainQuantity(), repetition);

        Pand pand = pandsService.getPandByPandCode(updatedJobOrder.getPandCode(), updatedJobOrder.getProjectProfileId());

        copyBaseFields(pandsToJobOrder, updatedJobOrder);

        boolean quantityFieldsChanged =
                !Objects.equals(pandsToJobOrder.getHeight(), updatedJobOrder.getHeight())
                        || !Objects.equals(pandsToJobOrder.getWidth(), updatedJobOrder.getWidth())
                        || !Objects.equals(pandsToJobOrder.getRepetition(), updatedJobOrder.getRepetition())
                        || pandsToJobOrder.getMainQuantity() != updatedJobOrder.getMainQuantity();

        if (quantityFieldsChanged) {
            DecimalFormat df = new DecimalFormat("#.###");
            double oldMainTotal = parseRequiredDouble(pandsToJobOrder.getMainTotal(), "mainTotal");

            double quantityInBand = pand.getRestQuantity() - total;
            if (quantityInBand < 0) {
                return errorResponse(pandsToJobOrder,
                        "The Required Quantity exceeding the remaining Quantity in Band " + pand.getPandCode(),
                        HttpStatus.BAD_REQUEST);
            }

            // Adjust the pand's remaining quantity by the delta between old and new totals
            double delta = oldMainTotal - total; // positive => quantity freed up, negative => quantity consumed
            pand.setRestQuantity(Double.parseDouble(df.format(pand.getRestQuantity() + delta)));

            pandsToJobOrder.setTotal(df.format(total));
            pandsToJobOrder.setMainTotal(df.format(total));
            pandsToJobOrder.setQuantityInPand(Double.parseDouble(df.format(pand.getRestQuantity() - total)));
            pandsToJobOrder.setQuantity(updatedJobOrder.getMainQuantity() * repetition);
            pandsToJobOrder.setMainQuantity(updatedJobOrder.getMainQuantity());
        }

        if (!Objects.equals(pandsToJobOrder.getInstallationArea(), updatedJobOrder.getInstallationArea())) {
            pandsToJobOrder.setInstallationArea(updatedJobOrder.getInstallationArea());
            fatherJobOrder.setInstallementArea(updatedJobOrder.getInstallationArea());
            jobOrderRepository.save(fatherJobOrder);
        }

        pandsToJobOrder.setHeight(updatedJobOrder.getHeight());
        pandsToJobOrder.setWidth(updatedJobOrder.getWidth());
        pandsToJobOrder.setRepetition(updatedJobOrder.getRepetition());

        // Single save now (removed the duplicate call that existed before this refactor)
        pandsRepository.save(pand);

        pandsToJobOrder.setMessage("The Remaining Quantity is: " + pand.getRestQuantity());
        pandsToJobOrderRepository.save(pandsToJobOrder);

        changeHistoryLog.saveChange(id.toString(), updatedJobOrder.toString(), pandsToJobOrder.toString(), "update", request);

        return new ResponseEntity<>(pandsToJobOrder, HttpStatus.OK);
    }

    // --- helpers -------------------------------------------------------------

    private double parseRequiredDouble(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new NumberFormatException(fieldName + " is missing");
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(fieldName + " is not a valid number: '" + value + "'");
        }
    }

    private double calculateTotal(String unit, double height, double width, double mainQuantity, double repetition) {
        return switch (unit) {
            case "M2" -> (height * width * (mainQuantity * repetition)) / 10000;
            case "LM" -> (height * (mainQuantity * repetition)) / 100;
            default -> mainQuantity * repetition;
        };
    }

    private void copyBaseFields(PandsToJobOrder target, PandsToJobOrder source) {
        target.setProjectCode(source.getProjectCode());
        target.setProjectName(source.getProjectName());
        target.setEngineerName(source.getEngineerName());
        target.setJobOrderType(source.getJobOrderType());
        target.setManufacturingCode(source.getManufacturingCode());
        target.setPandCode(source.getPandCode());
        target.setDescription(source.getDescription());
        target.setManufacturing(source.getManufacturing());
        target.setRawType(source.getRawType());
        target.setRawUsed(source.getRawUsed());
        target.setFinishType(source.getFinishType());
        target.setThickness(source.getThickness());
        target.setBlockNumber(source.getBlockNumber());
        target.setFloor(source.getFloor());
        target.setUnit(source.getUnit());
        target.setAdditionalDescription(source.getAdditionalDescription());
    }

    private ResponseEntity<PandsToJobOrder> errorResponse(PandsToJobOrder entity, String message, HttpStatus status) {
        entity.setFlag(FLAG_ERROR);
        entity.setMessage(message);
        return new ResponseEntity<>(entity, status);
    }


//    public ResponseEntity<PandsToJobOrder> updateJobOrder(Long id, PandsToJobOrder updatedJobOrder, HttpServletRequest request) {
//
//        try {
//            PandsToJobOrder pandsToJobOrder = pandsToJobOrderRepository.findById(id)
//                    .orElseThrow(() -> new ResourceNotFoundException("WorkOrder Not Found for ID: " + id));
//
//            JobOrder fatherJobOrder = jobOrderService.getJobOrderByNumber(pandsToJobOrder.getJobOrderId());
//
//            if (fatherJobOrder.isApproved()) {
//                updatedJobOrder.setFlag(1);
//                updatedJobOrder.setMessage(" cannot add/edit on approved work order ");
//                return new ResponseEntity<>(updatedJobOrder, HttpStatus.OK);
//            }
//
//            List<ExitJobOrder> exitJobOrders = exitJobOrderRepository.findByJobOrderId(pandsToJobOrder.getJobOrderId());
//            if (!exitJobOrders.isEmpty()) {
//                pandsToJobOrder.setFlag(1);
//                pandsToJobOrder.setMessage(" Not Allowed .... for permit job orders " + pandsToJobOrder.getJobOrderId());
//                return new ResponseEntity<>(pandsToJobOrder, HttpStatus.BAD_REQUEST);
//            }
//
//            double total;
//            DecimalFormat df = new DecimalFormat("#.###");
//
//            Pand pand = pandsService.getPandByPandCode(updatedJobOrder.getPandCode(), updatedJobOrder.getProjectProfileId());
//            if (updatedJobOrder.getUnit().equals("Square Meter")) {
//                total = (Double.parseDouble(updatedJobOrder.getHeight()) * Double.parseDouble(updatedJobOrder.getWidth()) * (updatedJobOrder.getMainQuantity() * Double.parseDouble(updatedJobOrder.getRepetition()))) / 10000;
//            } else if (updatedJobOrder.getUnit().equals("Longitudinal meter")) {
//                total = (Double.parseDouble(updatedJobOrder.getHeight()) * (updatedJobOrder.getMainQuantity() * Double.parseDouble(updatedJobOrder.getRepetition()))) / 100;
//            } else {
//                total = updatedJobOrder.getMainQuantity() * Double.parseDouble(updatedJobOrder.getRepetition());
//            }
//
//            pandsToJobOrder.setProjectCode(updatedJobOrder.getProjectCode());
//            pandsToJobOrder.setProjectName(updatedJobOrder.getProjectName());
//            pandsToJobOrder.setEngineerName(updatedJobOrder.getEngineerName());
//            pandsToJobOrder.setJobOrderType(updatedJobOrder.getJobOrderType());
//            pandsToJobOrder.setManufacturingCode(updatedJobOrder.getManufacturingCode());
//            pandsToJobOrder.setPandCode(updatedJobOrder.getPandCode());
//            pandsToJobOrder.setDescription(updatedJobOrder.getDescription());
//            pandsToJobOrder.setManufacturing(updatedJobOrder.getManufacturing());
//            pandsToJobOrder.setRawType(updatedJobOrder.getRawType());
//            pandsToJobOrder.setRawUsed(updatedJobOrder.getRawUsed());
//            pandsToJobOrder.setFinishType(updatedJobOrder.getFinishType());
//            pandsToJobOrder.setThickness(updatedJobOrder.getThickness());
//            pandsToJobOrder.setBlockNumber(updatedJobOrder.getBlockNumber());
//            pandsToJobOrder.setFloor(updatedJobOrder.getFloor());
//
//            pandsToJobOrder.setUnit(updatedJobOrder.getUnit());
//
//            pandsToJobOrder.setAdditionalDescription(updatedJobOrder.getAdditionalDescription());
//            if (!pandsToJobOrder.getHeight().equals(updatedJobOrder.getHeight())
//                    || !pandsToJobOrder.getWidth().equals(updatedJobOrder.getWidth())
//                    || !pandsToJobOrder.getRepetition().equals(updatedJobOrder.getRepetition())
//                    || pandsToJobOrder.getMainQuantity() != updatedJobOrder.getMainQuantity()
//            ) {
//
//                double quantityInBand = pand.getRestQuantity() - total;
//                if (quantityInBand < 0) {
//                    pandsToJobOrder.setFlag(1);
//                    pandsToJobOrder.setMessage(" The Required Quantity exceeding the remaining Quantity in Band " + pand.getPandCode());
//                    return new ResponseEntity<>(pandsToJobOrder, HttpStatus.BAD_REQUEST);
//                }
//                if (Double.parseDouble(pandsToJobOrder.getMainTotal()) > total) {
//                    double restQuantityInPand = Double.parseDouble(pandsToJobOrder.getMainTotal()) - total;
//                    pand.setRestQuantity(Double.parseDouble(df.format(pand.getRestQuantity() + restQuantityInPand)));
//                } else {
//                    if (Double.parseDouble(pandsToJobOrder.getMainTotal()) < total) {
//                        double restQuantityInBand = total - Double.parseDouble(pandsToJobOrder.getMainTotal());
//                        pand.setRestQuantity(Double.parseDouble(df.format(pand.getRestQuantity() - restQuantityInBand)));
//                    }
//                }
//
//                pandsRepository.save(pand);
//
//                pandsToJobOrder.setTotal(df.format(total));
//                pandsToJobOrder.setMainTotal(df.format(total));
//                pandsToJobOrder.setQuantityInPand(Double.parseDouble(df.format(pand.getRestQuantity() - total)));
//                pandsToJobOrder.setQuantity(updatedJobOrder.getMainQuantity() * Double.parseDouble(updatedJobOrder.getRepetition()));
//                pandsToJobOrder.setMainQuantity(updatedJobOrder.getMainQuantity());
//
//            }
//            if (!pandsToJobOrder.getInstallationArea().equals(updatedJobOrder.getInstallationArea())) {
//                pandsToJobOrder.setInstallationArea(updatedJobOrder.getInstallationArea());
//
//                fatherJobOrder.setInstallementArea(updatedJobOrder.getInstallationArea());
//                jobOrderRepository.save(fatherJobOrder);
//            }
//            pandsToJobOrder.setHeight(updatedJobOrder.getHeight());
//            pandsToJobOrder.setWidth(updatedJobOrder.getWidth());
//            pandsToJobOrder.setRepetition(updatedJobOrder.getRepetition());
//
//            pandsRepository.save(pand);
//            pandsToJobOrder.setMessage(" The Remaining Quantity is: " + pand.getRestQuantity());
//            pandsToJobOrderRepository.save(pandsToJobOrder);
//
//            changeHistoryLog.saveChange(id.toString(), updatedJobOrder.toString(), pandsToJobOrder.toString(), "update", request);
//
//            return new ResponseEntity<>(pandsToJobOrder, HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error("Error while processing request updateBandsToJobOrders", e);
//
//            return new ResponseEntity<>(null, HttpStatus.OK);
//        }
//    }

    public List<PandsToJobOrder> getByJobOrderId(String id) {

        return pandsToJobOrderRepository.findByJobOrderId(id);
    }

    public ResponseEntity<List<PandsToJobOrder>> getAllByJobOrderId(Long id) {
        Optional<JobOrder> jobOrder = jobOrderRepository.findById(id);

        return jobOrder.map(order -> new ResponseEntity<>(pandsToJobOrderRepository.findByProjectProfileIdAndJobOrderId(order.getProjectProfileId(), order.getJobOrderNumber()), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

    }

    public List<PandsToJobOrder> getByJobOrderIdWzNoZeros(UnifiedSerial unifiedSerial) {
        List<PandsToJobOrder> pandsToJobOrders = pandsToJobOrderRepository.findByJobOrderId(unifiedSerial.getJobOrderNumber());
        List<PandsToJobOrder> pandsToJobOrdersNoZeros = new ArrayList<>();
        for (PandsToJobOrder pandsToJobOrder : pandsToJobOrders) {
            if (pandsToJobOrder.getQuantity() > 0) {
                pandsToJobOrdersNoZeros.add(pandsToJobOrder);
            }
        }
        return pandsToJobOrdersNoZeros;
    }

    public PandsToJobOrder getByJobOrderId(Long id) {

        return pandsToJobOrderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("rawType Not Found for ID: " + id));
    }

    public Optional<PandsToJobOrder> getByJobOrderAndBandId(String jobOrderid, String pandId) {

        return pandsToJobOrderRepository.findByJobOrderIdAndPandCode(jobOrderid, pandId);
    }

    public List<PandsToJobOrder> getByProjectId(Long id) {
        try {
            return pandsToJobOrderRepository.findByProjectProfileId(id);
        } catch (Exception e) {
            logger.error("Error while processing request getBandsToJobOrderByProjectId", e);

            return null;
        }

    }


    public ResponseEntity<ResponseMessage> deletePandToJobOrder(Long id, HttpServletRequest request) {
        ResponseMessage responseMessage = null;
        try {
            PandsToJobOrder pandsToJobOrder = pandsToJobOrderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("pand Not Found for ID: " + id));

            List<ExitJobOrder> exitJobOrders = exitJobOrderRepository.findByJobOrderId(pandsToJobOrder.getJobOrderId());
            if (!exitJobOrders.isEmpty()) {
                return new ResponseEntity<>(new ResponseMessage("Not Allowed for existing permit job order","1"),
                        HttpStatus.BAD_REQUEST);
            }

            double total;
            DecimalFormat df = new DecimalFormat("#.###");
            Pand pand = pandsService.getPandByPandCode(pandsToJobOrder.getPandCode(), pandsToJobOrder.getProjectProfileId());
            pand.setRestQuantity(Double.parseDouble(df.format(pand.getRestQuantity() + Double.parseDouble(pandsToJobOrder.getTotal()))));

            pandsToJobOrderRepository.deleteRelationByPandsToJobOrderId(id);
            pandsRepository.save(pand);
            pandsToJobOrderRepository.deleteById(id);

            changeHistoryLog.saveChange(id.toString(), pandsToJobOrder.toString(), pandsToJobOrder.toString(), "delete", request);

            responseMessage = new ResponseMessage("Deleted Successfully","0");

            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while processing delete work order ",e);
            return new ResponseEntity<>(new ResponseMessage("Error while processing delete work order ","1"),
                    HttpStatus.EXPECTATION_FAILED);
        }

    }

    public CheckLimitResponse returnJobOrder(String id, PandsToJobOrder jobOrderParent) {
        CheckLimitResponse checkLimitResponse = new CheckLimitResponse();

        try {

            if (Objects.equals(jobOrderParent.getReturnReason(), "")) {
                checkLimitResponse.setFlag(1);
                checkLimitResponse.setMessage("Please enter the return reason");
                return checkLimitResponse;
            }

            List<ExitJobOrder> actualPandsToJobOrder = exitJobOrderRepository.findBySerialNumber(id);
            System.out.println(actualPandsToJobOrder.size());
            DecimalFormat df = new DecimalFormat("#.###");
            for (int i = 0; i < actualPandsToJobOrder.size(); i++) {
                System.out.println("actualPandsToJobOrder: " + i);
                Optional<PandsToJobOrder> pandsToJobOrder = pandsToJobOrderRepository.findByUniqueIdAndJobOrderIdAndWidthAndHeight(actualPandsToJobOrder.get(i).getUniqueId()
                        , actualPandsToJobOrder.get(i).getJobOrderId(), actualPandsToJobOrder.get(i).getWidth(), actualPandsToJobOrder.get(i).getHeight());
                pandsToJobOrder.get().setQuantity(pandsToJobOrder.get().getQuantity() + actualPandsToJobOrder.get(i).getQuantity());
                String result = df.format(Double.parseDouble(pandsToJobOrder.get().getTotal()) + Double.parseDouble(actualPandsToJobOrder.get(i).getTotal()));
                pandsToJobOrder.get().setTotal(result);
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
            logger.error("Error while processing return JobOrder",e);
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

    public InputStreamResource getPdf(String id) throws Exception {

        JobOrder jobOrder = jobOrderService.getByJobOrder(id);

        List<String> pandsToJobOrdersRaws =
                pandsToJobOrderRepository.findDistinctRawTypesByProjectAndJobOrder(
                        jobOrder.getProjectProfileId(),
                        id
                );

        Workbook workbook = new Workbook();
        Worksheet sheet = workbook.getWorksheets().get(0);
        Cells cells = sheet.getCells();

        sheet.setDisplayRightToLeft(false);

        ////////////////// PAGE SETUP //////////////////

        PageSetup pageSetup = sheet.getPageSetup();

        pageSetup.setOrientation(PageOrientationType.LANDSCAPE);
        pageSetup.setPaperSize(PaperSizeType.PAPER_A_3);

        pageSetup.setTopMargin(0.3);
        pageSetup.setBottomMargin(0.3);
        pageSetup.setLeftMargin(0.3);
        pageSetup.setRightMargin(0.3);

        pageSetup.setFitToPagesWide(1);
        pageSetup.setFitToPagesTall(0);

        ////////////////// COLUMN WIDTHS //////////////////

        cells.setColumnWidth(0, 8);   // A
        cells.setColumnWidth(1, 14);  // B
        cells.setColumnWidth(2, 18);  // C
        cells.setColumnWidth(3, 16);  // D
        cells.setColumnWidth(4, 12);  // E
        cells.setColumnWidth(5, 16);  // F
        cells.setColumnWidth(6, 12);  // G
        cells.setColumnWidth(7, 12);  // H
        cells.setColumnWidth(8, 12);  // I
        cells.setColumnWidth(9, 12);  // J
        cells.setColumnWidth(10, 12); // K
        cells.setColumnWidth(11, 12); // L
        cells.setColumnWidth(12, 18); // M

        ////////////////// ROW HEIGHTS //////////////////

        for (int i = 0; i <= 8; i++) {
            cells.setRowHeight(i, 28);
        }

        ////////////////// STYLES //////////////////

        Style labelStyle = workbook.createStyle();

        labelStyle.getFont().setName("Arial");
        labelStyle.getFont().setBold(true);
        labelStyle.getFont().setSize(10);

        labelStyle.setHorizontalAlignment(TextAlignmentType.LEFT);
        labelStyle.setVerticalAlignment(TextAlignmentType.CENTER);

        Style valueStyle = workbook.createStyle();

        valueStyle.copy(labelStyle);

        valueStyle.getFont().setBold(false);

        Style tableHeaderStyle = workbook.createStyle();

        tableHeaderStyle.getFont().setBold(true);
        tableHeaderStyle.getFont().setSize(10);

        tableHeaderStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        tableHeaderStyle.setVerticalAlignment(TextAlignmentType.CENTER);

        tableHeaderStyle.setForegroundColor(Color.getLightGray());
        tableHeaderStyle.setPattern(BackgroundType.SOLID);

        tableHeaderStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
        tableHeaderStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

        Style rowStyle = workbook.createStyle();

        rowStyle.getFont().setSize(10);

        rowStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
        rowStyle.setVerticalAlignment(TextAlignmentType.CENTER);

        rowStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
        rowStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
        rowStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
        rowStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

        rowStyle.setTextWrapped(true);

        Style grayRowStyle = workbook.createStyle();

        grayRowStyle.copy(rowStyle);

        grayRowStyle.setForegroundColor(Color.getLightGray());
        grayRowStyle.setPattern(BackgroundType.SOLID);

        ////////////////// MERGES //////////////////

        cells.merge(0, 7, 4, 3);   // H1:J4 logo
        cells.merge(1, 11, 5, 2);  // K1:M4 reference

        ////////////////// DATE //////////////////

        Date now = new Date();

        SimpleDateFormat timeFormat =
                new SimpleDateFormat("hh:mm:ss a");

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd.MM.yy");

        ////////////////// HEADER //////////////////

        cells.merge(2, 1, 1, 2);
        cells.get("A2").putValue("Work Order: " + " " + id);
        cells.get("A2").setStyle(labelStyle);

//        cells.get("B2").putValue(
//                "  " + id
//        );
//        cells.get("B2").setStyle(valueStyle);

        cells.get("A3").putValue("Type");
        cells.get("A3").setStyle(labelStyle);


        cells.get("B3").setStyle(valueStyle);

        cells.get("A4").putValue("Created");
        cells.get("A4").setStyle(labelStyle);

        cells.merge(3, 1, 1, 2);

        cells.get("B4").putValue(jobOrder.getJobOrderTime() + " " + jobOrder.getJobOrderDate());
        cells.get("B4").setStyle(valueStyle);

//        cells.get("C4").putValue(jobOrder.getJobOrderDate());
//        cells.get("C4").setStyle(valueStyle);

        cells.get("A5").putValue("Print");
        cells.get("A5").setStyle(labelStyle);

        cells.merge(4, 1, 1, 2);
        cells.get("B5").putValue(timeFormat.format(now) + " " + dateFormat.format(now));
        cells.get("B5").setStyle(valueStyle);

//        cells.get("C5").putValue(dateFormat.format(now));
//        cells.get("C5").setStyle(valueStyle);

        ////////////////// MIDDLE //////////////////

        cells.get("D2").putValue("Installation Area");
        cells.get("D2").setStyle(labelStyle);

//        cells.get("E2").putValue(
//                pandsToJobOrdersRaws.getFirst().getInstallationArea()
//        );
        cells.get("E2").setStyle(valueStyle);

        cells.get("D3").putValue("Floor");
        cells.get("D3").setStyle(labelStyle);

//        cells.get("E3").putValue(
//                pandsToJobOrdersRaws.getFirst().getFloor()
//        );
        cells.get("E3").setStyle(valueStyle);

        cells.get("D4").putValue("Block");
        cells.get("D4").setStyle(labelStyle);

//        cells.get("E4").putValue(
//                pandsToJobOrdersRaws.getFirst().getBlockNumber()
//        );
        cells.get("E4").setStyle(valueStyle);

        ////////////////// RIGHT //////////////////

        cells.get("F2").putValue("Project Name");
        cells.get("F2").setStyle(labelStyle);

        cells.get("G2").putValue(jobOrder.getProjectName());
        cells.get("G2").setStyle(valueStyle);

        cells.get("F3").putValue("Project Code");
        cells.get("F3").setStyle(labelStyle);

        cells.get("G3").putValue(jobOrder.getProjectCode());
        cells.get("G3").setStyle(valueStyle);

        cells.get("F4").putValue("Engineer Name");
        cells.get("F4").setStyle(labelStyle);

//        cells.get("G4").putValue(
//                pandsToJobOrdersRaws.getFirst().getEngineerName()
//        );
        cells.get("G4").setStyle(valueStyle);

        ////////////////// REFERENCE //////////////////

        Style referenceStyle = workbook.createStyle();

        referenceStyle.getFont().setSize(10);

        referenceStyle.setTextWrapped(true);

        referenceStyle.setVerticalAlignment(TextAlignmentType.TOP);

        cells.get("L2").putValue(
                "Reference: Ref.No. OR/WO/04/26\n" +
                        "Revision: Rev.00\n" +
                        "Date of Issue: " + timeFormat.format(now) + "\n" +
                        "Note: Internal use only"
        );

        cells.get("L2").setStyle(referenceStyle);

        ////////////////// LOGO //////////////////

        InputStream imageStream =
                new ClassPathResource("static/ORYX.jpeg").getInputStream();

        int pictureIndex =
                sheet.getPictures().add(0, 9, imageStream);

        Picture picture =
                sheet.getPictures().get(pictureIndex);

        picture.setPlacement(PlacementType.FREE_FLOATING);

        picture.setWidthScale(45);
        picture.setHeightScale(45);

//        cells.merge(7, 5, 1, 2);

        cells.get("F5")
                .putValue("Work Order");

        Style titleStyle =
                workbook.createStyle();

//        titleStyle.copy(borderStyle);

        titleStyle.getFont().setBold(true);

        titleStyle.getFont().setSize(20);

        titleStyle.setHorizontalAlignment(
                TextAlignmentType.CENTER
        );

        titleStyle.setVerticalAlignment(
                TextAlignmentType.CENTER
        );

        cells.get("F5")
                .setStyle(titleStyle);

        ////////////////// TABLE HEADER //////////////////

        cells.setColumnWidth(2, 35);   // Desc
        cells.setColumnWidth(12, 35);  // Notes
        String[] headers = {
                "#",
                "Ref.",
                "Desc.",
                "Material",
                "Thick",
                "Finish",
                "Unit",
                "Number",
                "Height",
                "Width",
                "Rep.",
                "Total",
                "Notes"
        };

        for (int i = 0; i < headers.length; i++) {

            Cell cell = cells.get(6, i);

            cell.putValue(headers[i]);

            cell.setStyle(tableHeaderStyle);
        }

        ////////////////// TABLE DATA //////////////////

        int rowIdx = 7;

        List<PandsToJobOrder> pandsToJobOrders =
                new ArrayList<>();

        for (String raw : pandsToJobOrdersRaws) {
            pandsToJobOrders.addAll(
                    pandsToJobOrderRepository.findByProjectProfileIdAndJobOrderIdAndRawType(
                            jobOrder.getProjectProfileId(),
                            id,
                            raw
                    )
            );
        }

        for (int i = 0; i < pandsToJobOrders.size(); i++) {

            PandsToJobOrder item = pandsToJobOrders.get(i);

            if (i == 0) {
                cells.get("B3").putValue(
                        pandsToJobOrders.get(i).getJobOrderType()
                );

                cells.get("E2").putValue(
                        pandsToJobOrders.get(i).getInstallationArea()
                );
                cells.get("E2").setStyle(valueStyle);

                cells.get("E3").putValue(
                        pandsToJobOrders.get(i).getFloor()
                );
                cells.get("E3").setStyle(valueStyle);

                cells.get("E4").putValue(
                        pandsToJobOrders.get(i).getBlockNumber()
                );

                cells.get("G4").putValue(
                        pandsToJobOrders.get(i).getEngineerName()
                );
            }

            Style currentStyle =
                    (i % 2 == 0)
                            ? grayRowStyle
                            : rowStyle;

            String[] values = {
                    String.valueOf(i + 1),
                    item.getPandCode(),
                    item.getDescription(),
                    item.getRawType(),
                    item.getThickness(),
                    item.getFinishType(),
                    item.getUnit(),
                    String.valueOf(item.getMainQuantity()),
                    item.getHeight(),
                    item.getWidth(),
                    item.getRepetition(),
                    item.getMainTotal(),
                    item.getAdditionalDescription()
            };

            for (int col = 0; col < values.length; col++) {

                Cell cell = cells.get(rowIdx, col);

                cell.putValue(values[col]);

                cell.setStyle(currentStyle);
            }

            cells.setRowHeight(rowIdx, 30);

            rowIdx++;
        }

        rowIdx += 2;

        cells.get("G" + rowIdx).putValue("Material");
        cells.get("H" + rowIdx).putValue("Thick");
        cells.get("I" + rowIdx).putValue("Unit");
        cells.get("J" + rowIdx).putValue("Total by unit");
        cells.get("K" + rowIdx).putValue("M2");

        cells.get("G" + rowIdx).setStyle(tableHeaderStyle);
        cells.get("H" + rowIdx).setStyle(tableHeaderStyle);
        cells.get("I" + rowIdx).setStyle(tableHeaderStyle);
        cells.get("J" + rowIdx).setStyle(tableHeaderStyle);
        cells.get("K" + rowIdx).setStyle(tableHeaderStyle);

        rowIdx++;

        DecimalFormat df = new DecimalFormat("#.###");

        List<ThicknessUnitDTO> summaryList = new ArrayList<>();

        for (String raw : pandsToJobOrdersRaws) {

            summaryList.addAll(
                    pandsToJobOrderRepository.findDistinctThicknessAndUnit(
                            jobOrder.getProjectProfileId(),
                            id,
                            raw
                    )
            );
        }

        for (ThicknessUnitDTO item : summaryList) {

            List<PandsToJobOrder> details =
                    pandsToJobOrderRepository.findByThicknessAndRawTypeAndUnit(
                            jobOrder.getProjectProfileId(),
                            id,
                            item.getRawType(),
                            item.getThickness(),
                            item.getUnit()
                    );

            double total = 0;
            double sqm = 0;

            for (PandsToJobOrder d : details) {

                total += Double.parseDouble(d.getMainTotal());

                sqm += (
                        d.getMainQuantity()
                                * Double.parseDouble(d.getRepetition())
                                * Double.parseDouble(d.getHeight())
                                * Double.parseDouble(d.getWidth())
                ) / 10000;
            }

            cells.get("G" + rowIdx).putValue(item.getRawType());
            cells.get("H" + rowIdx).putValue(item.getThickness());
            cells.get("I" + rowIdx).putValue(item.getUnit());
            cells.get("J" + rowIdx).putValue(df.format(total));
            cells.get("K" + rowIdx).putValue(df.format(sqm));

            cells.get("G" + rowIdx).setStyle(rowStyle);
            cells.get("H" + rowIdx).setStyle(rowStyle);
            cells.get("I" + rowIdx).setStyle(rowStyle);
            cells.get("J" + rowIdx).setStyle(rowStyle);
            cells.get("K" + rowIdx).setStyle(rowStyle);

            rowIdx++;
        }

        AutoFitterOptions options = new AutoFitterOptions();
        options.setAutoFitMergedCellsType(AutoFitMergedCellsType.EACH_LINE);

        sheet.autoFitRows(options);

        pageSetup.setFooter(
                0,
                "&\"Arial,Bold\"&12" + id
        );

        pageSetup.setFooter(
                1,
                "&\"Arial,Bold\"&16Page &P of &N"
        );

        pageSetup.setFooter(
                2,
                "&\"Arial,Bold\"&12ORYX"
        );

        ////////////////// PDF //////////////////

        ByteArrayOutputStream pdfOutputStream =
                new ByteArrayOutputStream();

        workbook.save(
                pdfOutputStream,
                SaveFormat.PDF
        );

        ByteArrayInputStream pdfInputStream =
                new ByteArrayInputStream(
                        pdfOutputStream.toByteArray()
                );

        return new InputStreamResource(pdfInputStream);
    }

    // PDF File
//    public InputStreamResource getPdf(String id) throws Exception {
//
//        JobOrder jobOrder = jobOrderService.getByJobOrder(id);
//
//        List<PandsToJobOrder> pandsToJobOrdersRaws = pandsToJobOrderRepository.findByProjectProfileIdAndJobOrderId(jobOrder.getProjectProfileId(), id);
//
//
//        Workbook workbook = new Workbook();
//
//        WorksheetCollection worksheets = workbook.getWorksheets();
//        Worksheet sheet = worksheets.get(0);
//        sheet.setDisplayRightToLeft(false);
//        sheet.getPageSetup().setPrintTitleRows("$9:$9");
//        Cells cells = sheet.getCells();
//
////            sheet.getCells().setRowHeight(7, 20);
//
//        PageSetup pageSetup = sheet.getPageSetup();
//        pageSetup.setOrientation(PageOrientationType.LANDSCAPE);
//        pageSetup.setPaperSize(PaperSizeType.PAPER_A_3);
//
//
//        pageSetup.setTopMargin(1);
//        pageSetup.setBottomMargin(1);
//        pageSetup.setLeftMargin(1);
//        pageSetup.setRightMargin(1);
//
//        pageSetup.setFitToPagesWide(1);     // Fit to one page wide
//        pageSetup.setFitToPagesTall(0);     // Don't force page height
//        pageSetup.setZoom(100);             // Prevent zoom from shrinking
//        pageSetup.setPercentScale(false);   // Don't scale by percent
//
//        Date dNow = new Date();
//        SimpleDateFormat ft =
//                new SimpleDateFormat("hh:mm:ss a");
//
//        DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");
//
//
//        Style tableHeaderStyle = sheet.getCells().get("C1").getStyle();
//        tableHeaderStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
//        tableHeaderStyle.setVerticalAlignment(TextAlignmentType.CENTER);
//        tableHeaderStyle.getFont().setItalic(true);
//        tableHeaderStyle.getFont().setSize(15);
//        tableHeaderStyle.getFont().setBold(false);
//        tableHeaderStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
//        tableHeaderStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
//        tableHeaderStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
//        tableHeaderStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());
//
//        Style discriptionDataStyle = sheet.getCells().get("F3").getStyle();
//        discriptionDataStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
//        discriptionDataStyle.setVerticalAlignment(TextAlignmentType.CENTER);
//        discriptionDataStyle.getFont().setSize(15);
//
//        sheet.getCells().get("A1").putValue("Work Order");
//        sheet.getCells().get("A1").setStyle(discriptionDataStyle);
//
//
//        sheet.getCells().get("B1").putValue(pandsToJobOrdersRaws.get(0).getJobOrderId());
//        sheet.getCells().get("B1").setStyle(discriptionDataStyle);
//
//
//        sheet.getCells().get("A3").putValue("Type");
//        sheet.getCells().get("A3").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("B3").putValue(pandsToJobOrdersRaws.get(0).getJobOrderType());
//        sheet.getCells().get("B3").setStyle(discriptionDataStyle);
//
//
//        sheet.getCells().get("A5").putValue("Created");
//        sheet.getCells().get("A5").setStyle(discriptionDataStyle);
//
////        sheet.getCells().merge(5, 2, 1, 2);  // (startRow, startColumn, totalRows, totalColumns)
//
//        sheet.getCells().get("C5").putValue(jobOrder.getJobOrderDate());
//        sheet.getCells().get("C5").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("B5").putValue(jobOrder.getJobOrderTime());
//        sheet.getCells().get("B5").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("A7").putValue("Print");
//        sheet.getCells().get("A7").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("C7").putValue(formatter1.format(dNow));
//        sheet.getCells().get("C7").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("B7").putValue(ft.format(dNow).toString());
//        sheet.getCells().get("B7").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("D1").putValue("Installation Area");
//        sheet.getCells().get("D1").setStyle(discriptionDataStyle);
//
////        sheet.getCells().merge(0, 4, 1, 5);  // (startRow, startColumn, totalRows, totalColumns)
//
//        sheet.getCells().get("E1").putValue(pandsToJobOrdersRaws.get(0).getInstallationArea());
//        sheet.getCells().get("E1").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("D3").putValue("Floor");
//        sheet.getCells().get("D3").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("E3").putValue(pandsToJobOrdersRaws.get(0).getFloor());
//        sheet.getCells().get("E3").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("D5").putValue("Block");
//        sheet.getCells().get("D5").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("E5").putValue(pandsToJobOrdersRaws.get(0).getBlockNumber());
//        sheet.getCells().get("E5").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("F1").putValue("Project Name");
//        sheet.getCells().get("F1").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("G1").putValue(pandsToJobOrdersRaws.get(0).getProjectName());
//        sheet.getCells().get("G1").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("F3").putValue("Project Code");
//        sheet.getCells().get("F3").setStyle(discriptionDataStyle);
//
////        sheet.getCells().merge(2, 6, 1, 2);  // (startRow, startColumn, totalRows, totalColumns)
//
//        sheet.getCells().get("G3").putValue(pandsToJobOrdersRaws.get(0).getProjectCode());
//        sheet.getCells().get("G3").setStyle(discriptionDataStyle);
//
//        sheet.getCells().get("F5").putValue("Engineer Name");
//        sheet.getCells().get("F5").setStyle(discriptionDataStyle);
//
////        sheet.getCells().merge(4, 6, 1, 2);  // (startRow, startColumn, totalRows, totalColumns)
//
//        sheet.getCells().get("G5").putValue(pandsToJobOrdersRaws.get(0).getOfficerName());
//        sheet.getCells().get("G5").setStyle(discriptionDataStyle);
//
////        sheet.getCells().get("F7").putValue("أسم المستخدم");
////        sheet.getCells().get("F7").setStyle(discriptionDataStyle);
////
////        sheet.getCells().get("G7").putValue(jwtUtils.userName);
////        sheet.getCells().get("G7").setStyle(discriptionDataStyle);
//
//
//        InputStream imageStream = new ClassPathResource("static/ORYX.jpeg").getInputStream();
//
//        // Add the image to the worksheet (X, Y coordinates in pixels)
//        // Place the image inside the merged cells (A1:C5)
//        int pictureIndex = sheet.getPictures().add(0, 10, imageStream);
//
//        // Get the added picture
//        Picture picture = sheet.getPictures().get(pictureIndex);
//
//        // Optionally, set the picture to fit within the merged area
//        picture.setPlacement(PlacementType.MOVE);
//        picture.setWidthScale(40); // Scale the image to fit width
//        picture.setHeightScale(40);
//
//
//        Style shadowStyle = workbook.createStyle();
//        shadowStyle.setPattern(BackgroundType.SOLID);
//        shadowStyle.setForegroundColor(Color.getDarkGray());
//        shadowStyle.setHorizontalAlignment(TextAlignmentType.CENTER);
//        shadowStyle.getFont().setSize(15);
//
//        sheet.getCells().get("A9").putValue("#");
//        sheet.getCells().get("A9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("B9").putValue("BOQ Ref");
//        sheet.getCells().get("B9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().merge(8, 2, 1, 1);
//
//        sheet.getCells().get("C9").putValue("BOQ Desc");
//        sheet.getCells().get("C9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("D9").putValue("Material Type");
//        sheet.getCells().get("D9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("E9").putValue("Thickness");
//        sheet.getCells().get("E9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("F9").putValue("Finish Type");
//        sheet.getCells().get("F9").setStyle(tableHeaderStyle);
//
//
//        sheet.getCells().get("G9").putValue("Unit");
//        sheet.getCells().get("G9").setStyle(tableHeaderStyle);
//
//
//        sheet.getCells().get("H9").putValue("Quantity");
//        sheet.getCells().get("H9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("I9").putValue("Repetition");
//        sheet.getCells().get("I9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("J9").putValue("Height");
//        sheet.getCells().get("J9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("K9").putValue("Width");
//        sheet.getCells().get("K9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("L9").putValue("Total");
//        sheet.getCells().get("L9").setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("M9").putValue("Notes");
//        sheet.getCells().get("M9").setStyle(tableHeaderStyle);
//
//
//        int rowIdx = 11;
//
//        int totalQuantity = 0;
//        double finalTotal = 0.0;
//        DecimalFormat df = new DecimalFormat("#.###");
//
//        List<PandsToJobOrder> pandsToJobOrders = new ArrayList<>();
//
//        for (int i = 0; i < pandsToJobOrdersRaws.size(); i++) {
//            pandsToJobOrders.addAll(pandsToJobOrderRepository.findByProjectProfileIdAndJobOrderIdAndRawType(pandsToJobOrdersRaws.get(i).getProjectProfileId()
//                    , pandsToJobOrdersRaws.get(i).getJobOrderId(),
//                    pandsToJobOrdersRaws.get(i).getRawType()));
//
////            pandsToJobOrders.add(null);
//
//        }
//
//        int flag = 0;
//
//        for (int i = 0; i < pandsToJobOrders.size(); i++) {
//
//            sheet.getCells().get("A" + rowIdx).putValue(i + 1);
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("A" + rowIdx).setStyle(shadowStyle);
//
//            } else {
//                sheet.getCells().get("A" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            sheet.getCells().get("B" + rowIdx).putValue(pandsToJobOrders.get(i).getPandCode());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("B" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("B" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//
//            int count = 0;
////            if (pandsToJobOrders.get(i).getDescription().length() > 30) {
////                int rowIndex = rowIdx;
////                String input = pandsToJobOrders.get(i).getDescription();
////                int partLength = 30;
////                for (int d = 0; d < input.length(); d += partLength) {
////                    int end = Math.min(d + partLength, input.length());
////                    String part = input.substring(d, end);
////                    count++;
////
////                    Row row = cells.getRows().get(rowIndex);
////                    row.setHeight(100);
////
////                    sheet.getCells().get("C" + rowIndex).putValue(part + "\n");
//////                    if (rowIndex % 2 != 0) {
////                    sheet.getCells().get("C" + rowIndex).setStyle(shadowStyle);
//////                    } else {
//////                        sheet.getCells().get("C" + rowIndex).setStyle(discriptionDataStyle);
//////                    }
////                    rowIndex++;
////                }
////            } else {
//            sheet.getCells().get("C" + rowIdx).putValue(pandsToJobOrders.get(i).getDescription());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("C" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("C" + rowIdx).setStyle(discriptionDataStyle);
//            }
////            }
////            if (count > 0) {
////                sheet.getCells().merge(rowIdx, 2, count - 1, 2);
////            }
//
//
//            sheet.getCells().get("D" + rowIdx).putValue(pandsToJobOrders.get(i).getRawType());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("D" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("D" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
////            String value = pandsToJobOrders.get(i).getAdditionalDescription();
////
////            if (value.length() > 30) {
////
////                int splitIndex = value.indexOf(" ", 30);
////
////                if (splitIndex == -1) {
////                    // Fallback to character 50 if no space found
////                    splitIndex = 30;
////                }
////
////                String firstPart = value.substring(0, splitIndex).trim();
////                String secondPart = value.substring(splitIndex).trim();
////
////                // Set the split values
////                sheet.getCells().get("E" + rowIdx).setValue(firstPart);
////                sheet.getCells().get("E" + rowIdx + 1).setValue(secondPart);
////                flag = 1;
////
////                // Double the height of the original row
////                double originalHeight = sheet.getCells().getRowHeight(rowIdx);
////                sheet.getCells().setRowHeight(rowIdx, originalHeight * 2);
////            } else {
////                sheet.getCells().get("E" + rowIdx).putValue(pandsToJobOrders.get(i).getAdditionalDescription());
////            }
////
////            if (rowIdx % 2 != 0) {
////                sheet.getCells().get("E" + rowIdx).setStyle(shadowStyle);
////                if(flag == 1){
////                    sheet.getCells().get("E" + rowIdx + 1 ).setStyle(shadowStyle);
////                }
////            } else {
////                sheet.getCells().get("E" + rowIdx).setStyle(discriptionDataStyle);
////                if(flag == 1){
////                    sheet.getCells().get("E" + rowIdx + 1 ).setStyle(discriptionDataStyle);
////                }
////            }
//
//            sheet.getCells().get("E" + rowIdx).putValue(pandsToJobOrders.get(i).getThickness());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("E" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("E" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            sheet.getCells().get("F" + rowIdx).putValue(pandsToJobOrders.get(i).getFinishType());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("F" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("F" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            sheet.getCells().get("G" + rowIdx).putValue(pandsToJobOrders.get(i).getUnit());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("G" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
////            totalQuantity += pandsToJobOrders.get(i).getMainQuantity();
//            sheet.getCells().get("H" + rowIdx).putValue(pandsToJobOrders.get(i).getMainQuantity());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("H" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            sheet.getCells().get("I" + rowIdx).putValue(pandsToJobOrders.get(i).getRepetition());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("I" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("I" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            sheet.getCells().get("J" + rowIdx).putValue(pandsToJobOrders.get(i).getHeight());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("J" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("J" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            sheet.getCells().get("K" + rowIdx).putValue(pandsToJobOrders.get(i).getWidth());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("K" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("K" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            sheet.getCells().get("L" + rowIdx).putValue(pandsToJobOrders.get(i).getMainTotal());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("L" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("L" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
////            finalTotal += Double.valueOf(pandsToJobOrders.get(i).getMainTotal());
//            sheet.getCells().get("M" + rowIdx).putValue(pandsToJobOrders.get(i).getAdditionalDescription());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("M" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("M" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            if (count > 0) {
//                rowIdx += count + 1;
//            } else {
//                rowIdx += 2;
//            }
//
//
//        }
//
//
//        ////////////////adding the cube summetion//////////////////////////
//
//        ImageOrPrintOptions printOptions = new ImageOrPrintOptions();
//        printOptions.setPrintingPage(PrintingPageType.DEFAULT);
//        WorkbookRender render = new WorkbookRender(workbook, printOptions);
//        int totalPages = render.getPageCount();
//
//        // Calculate where the last page starts
//        SheetRender sheetRender = new SheetRender(sheet, printOptions);
//        int lastUsedRow = sheet.getCells().getMaxDataRow();
//
//
//        rowIdx = lastUsedRow + 3;
//
//        sheet.getCells().get("G" + rowIdx).putValue("Material");
//        sheet.getCells().get("G" + rowIdx).setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("H" + rowIdx).setStyle(tableHeaderStyle);
//        sheet.getCells().get("H" + rowIdx).putValue("Thickness");
//
//        sheet.getCells().get("I" + rowIdx).putValue("Unit");
//        sheet.getCells().get("I" + rowIdx).setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("J" + rowIdx).putValue("Total by unit");
//        sheet.getCells().get("J" + rowIdx).setStyle(tableHeaderStyle);
//
//        sheet.getCells().get("K" + rowIdx).putValue("Square Meter");
//        sheet.getCells().get("K" + rowIdx).setStyle(tableHeaderStyle);
//
//
//        List<ThicknessUnitDTO> pandsToJobOrdersByRawType = new ArrayList<>();
//
//        for (int i = 0; i < pandsToJobOrdersRaws.size(); i++) {
//            pandsToJobOrdersByRawType.addAll(pandsToJobOrderRepository.findDistinctThicknessAndUnit(pandsToJobOrdersRaws.get(i).getProjectProfileId()
//                    , pandsToJobOrdersRaws.get(i).getJobOrderId(),
//                    pandsToJobOrdersRaws.get(i).getRawType()));
//        }
//
//        rowIdx += 1;
//        for (ThicknessUnitDTO pandsToJobOrder : pandsToJobOrdersByRawType) {
//
//            sheet.getCells().get("G" + rowIdx).putValue(pandsToJobOrder.getRawType());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("G" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("G" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            sheet.getCells().get("H" + rowIdx).putValue(pandsToJobOrder.getThickness());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("H" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("H" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            sheet.getCells().get("I" + rowIdx).putValue(pandsToJobOrder.getUnit());
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("I" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("I" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            List<PandsToJobOrder> getByThicknessAndRawTypeAndUnit = pandsToJobOrderRepository.findByThicknessAndRawTypeAndUnit(pandsToJobOrdersRaws.getFirst().getProjectProfileId()
//                    , pandsToJobOrdersRaws.getFirst().getJobOrderId(),
//                    pandsToJobOrder.getRawType(),
//                    pandsToJobOrder.getThickness(),
//                    pandsToJobOrder.getUnit());
//
//            Double totalQuantityInCube = 0.0;
//            Double totalSum = 0.0;
//
//            if (!getByThicknessAndRawTypeAndUnit.isEmpty()) {
//                for (PandsToJobOrder toJobOrder : getByThicknessAndRawTypeAndUnit) {
//                    totalSum += Double.parseDouble(toJobOrder.getMainTotal());
//                    totalQuantityInCube += (toJobOrder.getMainQuantity() *
//                            Double.parseDouble(toJobOrder.getRepetition()) *
//                            Double.parseDouble(toJobOrder.getHeight()) *
//                            Double.parseDouble(toJobOrder.getWidth())) / 10000;
//                }
//            }
//
//
//            sheet.getCells().get("J" + rowIdx).putValue(df.format(totalSum));
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("J" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("J" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
////            finalTotal += Double.valueOf(pandsToJobOrders.get(i).getMainTotal());
//
//            sheet.getCells().get("K" + rowIdx).putValue(df.format(totalQuantityInCube));
//            if (rowIdx % 2 != 0) {
//                sheet.getCells().get("K" + rowIdx).setStyle(shadowStyle);
//            } else {
//                sheet.getCells().get("K" + rowIdx).setStyle(discriptionDataStyle);
//            }
//
//            rowIdx++;
//        }
//
//        sheet.getCells().setRowHeight(8, 18);
//
////        for (int i = 10; i < rowIdx; i++) {
////            sheet.getCells().setColumnWidth(0, 5);
////        }
//
//
//        //////////////////////////////////////////////////////////////////////
//        // Adjust column widths to fit content
//
//        sheet.autoFitColumns();
//

    /// /        sheet.getHorizontalPageBreaks().clear();
    /// /
    /// /        int lastRow = cells.getMaxDataRow();
    /// /        for (int row = 22; row <= lastRow; row += 22) {
    /// /            sheet.getHorizontalPageBreaks().add(row);
    /// /        }
//
//        for (int i = 10; i < rowIdx; i++) {
//            sheet.getCells().setRowHeight(i, 35);
//        }
//
//        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
//        workbook.save(pdfOutputStream, SaveFormat.PDF); // Save as PDF
//
//        // 4. Return the PDF as a response
//        ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
//        InputStreamResource resource = new InputStreamResource(pdfInputStream);
//
//        return resource;
//    }
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

        sheet.getCells().get("B3").putValue(pandsToJobOrdersPreview.getFirst().getJobOrderType());
        sheet.getCells().get("B3").setStyle(discriptionDataStyle);


        sheet.getCells().get("D1").putValue("Installation Area");
        sheet.getCells().get("D1").setStyle(discriptionDataStyle);

//        sheet.getCells().merge(0, 4, 1, 5);  // (startRow, startColumn, totalRows, totalColumns)

        sheet.getCells().get("E1").putValue(pandsToJobOrdersPreview.getFirst().getInstallationArea());
        sheet.getCells().get("E1").setStyle(discriptionDataStyle);

        sheet.getCells().get("D3").putValue("Location");
        sheet.getCells().get("D3").setStyle(discriptionDataStyle);

        sheet.getCells().get("E3").putValue(pandsToJobOrdersPreview.getFirst().getFloor());
        sheet.getCells().get("E3").setStyle(discriptionDataStyle);

        sheet.getCells().get("D5").putValue("Block");
        sheet.getCells().get("D5").setStyle(discriptionDataStyle);

        sheet.getCells().get("E5").putValue(pandsToJobOrdersPreview.getFirst().getBlockNumber());
        sheet.getCells().get("E5").setStyle(discriptionDataStyle);

        sheet.getCells().get("F1").putValue("Project Name");
        sheet.getCells().get("F1").setStyle(discriptionDataStyle);

        sheet.getCells().get("G1").putValue(pandsToJobOrdersPreview.getFirst().getProjectName());
        sheet.getCells().get("G1").setStyle(discriptionDataStyle);

        sheet.getCells().get("F3").putValue("Project Code");
        sheet.getCells().get("F3").setStyle(discriptionDataStyle);

//        sheet.getCells().merge(2, 6, 1, 2);  // (startRow, startColumn, totalRows, totalColumns)

        sheet.getCells().get("G3").putValue(pandsToJobOrdersPreview.getFirst().getProjectCode());
        sheet.getCells().get("G3").setStyle(discriptionDataStyle);

        sheet.getCells().get("F5").putValue("Engineer Name");
        sheet.getCells().get("F5").setStyle(discriptionDataStyle);

//        sheet.getCells().merge(4, 6, 1, 2);  // (startRow, startColumn, totalRows, totalColumns)

        sheet.getCells().get("G5").putValue(pandsToJobOrdersPreview.getFirst().getOfficerName());
        sheet.getCells().get("G5").setStyle(discriptionDataStyle);


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

        //////////////////////////////////////////////////////////////////////
        // Adjust column widths to fit content

        sheet.autoFitColumns();

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
    public List<PandsToJobOrder> saveListJobOrderPands(MarbleItemRequestDto pandsToJobOrder, HttpServletRequest request) throws SQLException {
        try {


            List<PandsToJobOrder> pandsToJobOrders = mapTopandsToJobOrder(pandsToJobOrder);

            if (pandsToJobOrders.getFirst().getFlag() == 1) {
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
            isJobOrderExist.setProjectCode(pandsToJobOrder.getProjectCode());
            isJobOrderExist.setInstallementArea(pandsToJobOrder.getInstallationArea());
            isJobOrderExist.setCreatedBy(username);
            isJobOrderExist.setYear(gcalendar.get(Calendar.YEAR));
            isJobOrderExist.setApproved(false);
            isJobOrderExist.setPandsToJobOrders(new ArrayList<>());
            isJobOrderExist.setEngineerName(pandsToJobOrder.getEngineerName());
            changeHistoryLog.saveChange(pandsToJobOrder.getProjectCode().concat("/") + nextNumber + "/" + gcalendar.get(Calendar.YEAR)
                    , pandsToJobOrder.toString(), pandsToJobOrder.toString(), "save", request);

            isJobOrderExist.getPandsToJobOrders().addAll(pandsToJobOrders);

            pandsToJobOrderRepository.saveAll(pandsToJobOrders);
            jobOrderRepository.save(isJobOrderExist);

            return pandsToJobOrders;
        } catch (Exception e) {
            logger.error("Error while processing saveListJobOrderPands",e);
            List<PandsToJobOrder> pandsToJobOrders = new ArrayList<>();
            PandsToJobOrder pandsToJobOrder1 = new PandsToJobOrder();
            pandsToJobOrder1.setFlag(1);
            pandsToJobOrder1.setMessage("Something Went Wrong");
            pandsToJobOrders.add(pandsToJobOrder1);
            return pandsToJobOrders;
        }
    }

    private List<PandsToJobOrder> mapTopandsToJobOrder(MarbleItemRequestDto marbleItemDtos) {

        double totalQuantity = 0;

        List<String> distinctPands = marbleItemDtos.getItems().stream()
                .map(MarbleItemDto::getPandCode)   // extract the unit field
                .filter(Objects::nonNull)      // optional: skip null units
                .distinct()                    // keep only unique values
                .toList();

        for (String distinctPand : distinctPands) {
            double restQuantity = pandsRepository.findRestQuantityByPandCodeAndProjectProfileId(distinctPand, marbleItemDtos.getProjectProfileId());
            for (MarbleItemDto marbleItemDto : marbleItemDtos.getItems()) {
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
            double restQuantity = pandsRepository.findRestQuantityByPandCodeAndProjectProfileId(distinctPand, marbleItemDtos.getProjectProfileId());
            for (MarbleItemDto marbleItemDto : marbleItemDtos.getItems()) {
                if (marbleItemDto.getPandCode().equals(distinctPand)) {
                    totalQuantity += Double.parseDouble(String.valueOf(marbleItemDto.getTotal()));
                }
            }
            Pand pand = pandsService.getPandByPandCode(distinctPand, marbleItemDtos.getProjectProfileId());

            pand.setRestQuantity(Double.parseDouble(df.format(restQuantity - totalQuantity)));
            pandsRepository.save(pand);
        }

        List<PandsToJobOrder> pandsToJobOrderList = new ArrayList<>();

        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("hh:mm:ss a");
//        Integer number = jobOrderService.getTheMaxNumber(marbleItemDtos.getFirst().getProjectProfileId());
        GregorianCalendar gcalendar = new GregorianCalendar();
        Integer number = jobOrderRepository.findMaxNumber(marbleItemDtos.getProjectProfileId());

        int nextNumber = number + 1;


        for (int i = 0; i < marbleItemDtos.getItems().size(); i++) {
            Pand pand = pandsService.getPandByPandCode(marbleItemDtos.getItems().get(i).getPandCode(), marbleItemDtos.getProjectProfileId());

            PandsToJobOrder pandsToJobOrder = new PandsToJobOrder();

            pandsToJobOrder.setJobOrderTime(ft.format(dNow));

            UUID uuid = UuidCreator.getTimeBased();

            pandsToJobOrder.setRepetition(String.valueOf(marbleItemDtos.getItems().get(i).getRepetition()));
            pandsToJobOrder.setMainQuantity(marbleItemDtos.getItems().get(i).getQuantity());
            pandsToJobOrder.setUnit(marbleItemDtos.getItems().get(i).getUnit());
            pandsToJobOrder.setHeight(marbleItemDtos.getItems().get(i).getHeight());
            pandsToJobOrder.setWidth(marbleItemDtos.getItems().get(i).getWidth());

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
            pandsToJobOrder.setTotal(df.format(marbleItemDtos.getItems().get(i).getTotal()));

            pandsToJobOrder.setMainTotal(df.format(marbleItemDtos.getItems().get(i).getTotal()));
            pandsToJobOrder.setThickness(String.valueOf(marbleItemDtos.getItems().get(i).getThickness()));
            pandsToJobOrder.setPandCode(pand.getPandCode());
//            pandsToJobOrder.setManufacturingCode(marbleItemDtos.getManufacturingCode());
            pandsToJobOrder.setManufacturing(pand.getManufacturing());
            pandsToJobOrder.setJobOrderType(marbleItemDtos.getJobOrderType());
            pandsToJobOrder.setProjectCode(pand.getProjectCode());
            pandsToJobOrder.setProjectName(pand.getProjectName());
//            pandsToJobOrder.setQuantityInPand(restTotal);
            pandsToJobOrder.setProjectProfileId(pand.getProjectProfileId());
            pandsToJobOrder.setFinishType(pand.getFinishType());
            pandsToJobOrder.setRawType(pand.getRawType());
            pandsToJobOrder.setRawUsed(pand.getRawUsed());
            pandsToJobOrder.setEngineerName(marbleItemDtos.getEngineerName());
            pandsToJobOrder.setInstallationArea(marbleItemDtos.getInstallationArea());
            pandsToJobOrder.setDescription(marbleItemDtos.getItems().get(i).getDescription());
            pandsToJobOrder.setAdditionalDescription(marbleItemDtos.getItems().get(i).getAdditionalDescription());
            pandsToJobOrderList.add(pandsToJobOrder);

//            pandsToJobOrderRepository.save(pandsToJobOrder);

        }
        return pandsToJobOrderList;
    }
}