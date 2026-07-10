package com.example.demo.service.store;

import com.example.demo.models.*;
import com.example.demo.repository.ExportSuppliesDetailsRepository;
import com.example.demo.repository.ExportSupplyRepository;
import com.example.demo.repository.SupplierRepository;
import com.example.demo.repository.SuppliesDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
}
