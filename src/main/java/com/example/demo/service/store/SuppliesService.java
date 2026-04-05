package com.example.demo.service.store;

import com.example.demo.models.Suppliers;
import com.example.demo.models.Supply;
import com.example.demo.repository.SupplierRepository;
import com.example.demo.repository.SupplyRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SuppliesService {

    @Autowired
    SupplyRepository supplyRepository;

    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    SupplySerialService supplySerialService;

    public ResponseEntity<List<Supply>> getAllSupplies() {
        List<Supply> supplies = supplyRepository.findAll();
        return new ResponseEntity<>(supplies, HttpStatus.OK);
    }

    public Optional<Supply> getSupplyById(Long id) {
        return supplyRepository.findAllById(id);
    }

    public ResponseEntity<Supply> addNewSupply(Supply supply, HttpServletRequest request) {
        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("hh:mm:ss a");
        DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");
        GregorianCalendar gcalendar = new GregorianCalendar();
        Suppliers suppliers = supplierRepository.getBySupplierName(supply.getSupplierName());
        supply.setSupplyNumber(String.valueOf(supplySerialService.getMaxImportSerial() + 1)
                .concat("/").concat(String.valueOf(gcalendar.get(Calendar.YEAR))));
        supply.setSupplierCode(suppliers.getSupplierCode());
        supply.setData(formatter1.format(dNow));
        supply.setTime(ft.format(dNow));
        supplySerialService.increaseImportSerial();
        return new ResponseEntity<>(supplyRepository.save(supply), HttpStatus.OK);
    }


}
