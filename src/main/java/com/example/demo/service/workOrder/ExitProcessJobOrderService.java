package com.example.demo.service.workOrder;

import com.example.demo.models.*;
import com.example.demo.repository.ExitJobOrderRepository;
import com.example.demo.repository.ExitProcessJobOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExitProcessJobOrderService {

    @Autowired
    ExitProcessJobOrderRepository exitProcessJobOrderRepository;

    @Autowired
    ExitJobOrderRepository exitJobOrderRepository;


    @Transactional
    public ResponseEntity<List<ExitProcessJobOrder>> saveExitProcessJobOrder(JobOrderParent exitJobOrders, String unifiedSerial) {
        try {
            double total;

            exitProcessJobOrderRepository.deleteAll();
            System.out.println("starting saveExitProcessJobOrder");
            List<ExitProcessJobOrder> exitProcessJobOrders = new ArrayList<>();
            for (int i = 0; i < exitJobOrders.getPandsToJobOrderList().size(); i++) {
                ExitProcessJobOrder exitJobOrder = new ExitProcessJobOrder();
                exitJobOrder = mappingJobOrder(exitJobOrders.getPandsToJobOrderList().get(i));

                if (exitJobOrders.getPandsToJobOrderList().get(i).getUnit().equals("Square Meter")) {
                    total = (Double.parseDouble(exitJobOrders.getPandsToJobOrderList().get(i).getHeight()) * Double.parseDouble(exitJobOrders.getPandsToJobOrderList().get(i).getWidth()) * exitJobOrders.getPandsToJobOrderList().get(i).getQuantity()) / 10000;
                } else if (exitJobOrders.getPandsToJobOrderList().get(i).getUnit().equals("Longitudinal meter")) {
                    total = (Double.parseDouble(exitJobOrders.getPandsToJobOrderList().get(i).getHeight()) * exitJobOrders.getPandsToJobOrderList().get(i).getQuantity()) / 100;
                } else {
                    total = exitJobOrders.getPandsToJobOrderList().get(i).getQuantity();
                }

                exitJobOrder.setSerialNumber(exitJobOrderRepository.getLastSerialNumber());
                exitJobOrder.setTotal(String.valueOf(total));
                exitJobOrder.setUnifiedSerial(unifiedSerial);
                System.out.println("unifiedSerial: " + unifiedSerial);
                exitProcessJobOrderRepository.save(exitJobOrder);
                exitProcessJobOrders.add(exitJobOrder);
            }
            System.out.println("ending saveExitProcessJobOrder");
            return new ResponseEntity<>(exitProcessJobOrders, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }

    public ExitProcessJobOrder mappingJobOrder(PandsToJobOrder updatedJobOrder) {

        ExitProcessJobOrder jobOrder = new ExitProcessJobOrder();

        jobOrder.setJobOrderId(updatedJobOrder.getJobOrderId());
        jobOrder.setUnifiedSerial(updatedJobOrder.getUnifiedSerial());
        jobOrder.setProjectProfileId((updatedJobOrder.getProjectProfileId()));
        jobOrder.setProjectCode(updatedJobOrder.getProjectCode());
        jobOrder.setProjectName(updatedJobOrder.getProjectName());
        jobOrder.setEngineerName(updatedJobOrder.getEngineerName());
        jobOrder.setJobOrderType(updatedJobOrder.getJobOrderType());
        jobOrder.setInstallationArea(updatedJobOrder.getInstallationArea());
        jobOrder.setUniqueId(updatedJobOrder.getUniqueId());
        jobOrder.setPandCode(updatedJobOrder.getPandCode());
        jobOrder.setDescription(updatedJobOrder.getDescription());
        jobOrder.setManufacturing(updatedJobOrder.getManufacturing());
        jobOrder.setRawType(updatedJobOrder.getRawType());
        jobOrder.setRawUsed(updatedJobOrder.getRawUsed());
        jobOrder.setFinishType(updatedJobOrder.getFinishType());
        jobOrder.setThickness(updatedJobOrder.getThickness());
        jobOrder.setBlockNumber(updatedJobOrder.getBlockNumber());
        jobOrder.setFloor(updatedJobOrder.getFloor());
        jobOrder.setOfficerName(updatedJobOrder.getOfficerName());
        jobOrder.setHeight(updatedJobOrder.getHeight());
        jobOrder.setWidth(updatedJobOrder.getWidth());
        jobOrder.setQuantity(updatedJobOrder.getQuantity());
        jobOrder.setRepetition(updatedJobOrder.getRepetition());
        jobOrder.setUnit(updatedJobOrder.getUnit());

        jobOrder.setAdditionalDescription(updatedJobOrder.getAdditionalDescription());

        return jobOrder;
    }

    public void deleteAll() {
        exitProcessJobOrderRepository.deleteAll();
    }
}
